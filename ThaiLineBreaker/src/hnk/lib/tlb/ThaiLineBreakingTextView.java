package hnk.lib.tlb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import hnk.lib.tlb._interface.TextViewReplacementMethod;
import hnk.lib.tlb._interface.ThaiLineBreaker;
import hnk.lib.tlb.engine.ImmutableContainmentSearcher;
import hnk.lib.tlb.engine.ImmutableInverseTrie;
import hnk.lib.tlb.engine.ThaiLineBreakerImpl;
import hnk.lib.tlb.util.ThaiUtil;
import hnk.lib.tlb.util.Utils;
import hnk.seed.thailinebreaker.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class ThaiLineBreakingTextView extends TextView implements
		TextViewReplacementMethod {

	private static class Holder {
		private static volatile ThaiLineBreaker breaker;

		private synchronized static void init(Resources res) {
			ThaiLineBreaker temp = breaker;
			if (temp == null) {// second lock
				try {
					// create ans1
					InputStream is = res.openRawResource(RESOURCE_TRIE_CACHE);
					ImmutableInverseTrie ans1 = ImmutableInverseTrie
							.deserialize(is);
					// create ans2
					is = res.openRawResource(RESOURCE_WHOLE_WORD_CACHE);
					ImmutableContainmentSearcher ans2 = ImmutableContainmentSearcher
							.deserialize(is);
					breaker = new ThaiLineBreakerImpl(ans1, ans2);
				} catch (IOException e) {
					e.printStackTrace();
					breaker = null;
				}
			}
		}
	}

	private CharSequence originalString = "";
	private static final char NBSP = '\u00a0';
	private static final int JELLY_BEAN = 16;
	private volatile boolean shouldRefresh = false;
	private static final int RESOURCE_TRIE_CACHE = R.raw.trie_cache,
			RESOURCE_WHOLE_WORD_CACHE = R.raw.whole_word_cache;

	public ThaiLineBreakingTextView(Context context) {
		super(context);
		init(context);
	}

	public ThaiLineBreakingTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ThaiLineBreakingTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		AsyncTask<Context, Void, Boolean> atask = new AsyncTask<Context, Void, Boolean>() {
			protected Boolean doInBackground(Context... params) {
				// double lock lazy init pattern
				ThaiLineBreaker temp = Holder.breaker;
				if (temp == null) { // first lock
					Holder.init(params[0].getResources());
					return true;
				}
				return false;
			};

			@Override
			protected void onPostExecute(Boolean result) {
				if (result && shouldRefresh && getEllipsize() == null)
					setText(doBreakText(originalString, getInnerWidth(),
							getPaint()));
			}
		};
		atask.execute(context);
	}

	public void setText2(int resid) {
		setText2(getContext().getResources().getText(resid));

	}

	public void setText2(CharSequence text) {
		originalString = text;
		refresh();
	}

	@Override
	protected void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		// can't do break text here or it will cause infinite event loop
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		refresh();
	}

	public CharSequence getText2() {
		return originalString;
	}

	private int getInnerWidth() {
		int ans = getWidth() - getTotalPaddingLeft() - getTotalPaddingRight()
				- 5;
		// FIXME troublesome jellybean and correct this function when android
		// fix this problem
		return Build.VERSION.SDK_INT >= JELLY_BEAN ? ans - 5 : ans;
	}

	private void refresh() {
		if (Holder.breaker == null) {
			shouldRefresh = true;
			return;
		}
		if (getEllipsize() != null)
			setText(originalString);
		else
			setText(doBreakText(originalString, getInnerWidth(), getPaint()));
	}

	// ========================================================================
	// ======================end of obligatory methods=========================
	// ========================================================================

	private static CharSequence doBreakText(CharSequence originalString,
			int innerWidth, Paint textPainter) {
		if (innerWidth <= 0)
			return originalString;
		if (innerWidth < 100)
			System.err
					.println("inner width of less than 100 is highly not recommended");
		String nbspString = originalString.toString().replace(' ', NBSP);
		List<String> lines = Utils.longStringToList(nbspString);
		StringBuilder ans = new StringBuilder();
		for (String s : lines)
			ans.append(breakLine(s, innerWidth, textPainter)).append('\n');
		return Utils.trimTrailingWhiteSpace(ans);
	}

	private static CharSequence breakLine(String oneLine, int innerWidth,
			Paint textPainter) {
		ThaiLineBreaker breaker = Holder.breaker;
		StringBuilder ans = new StringBuilder(oneLine);
		int pos = 0, count = 0;
		/*
		 * FIXME count < oneLine.length() to prevent infinite loop in jb??
		 */
		while (pos < oneLine.length() && count < oneLine.length()) {
			//TODO trim leading space
			int maxText = textPainter.breakText(oneLine, pos, oneLine.length(),
					true, innerWidth, null);
			// FIXME troublesome jellybean and correct this function when
			// android fix this problem
			if (Build.VERSION.SDK_INT >= JELLY_BEAN)
				maxText = resolveCorrectPositionForJB(oneLine, pos,
						oneLine.length(), maxText);
			int breakAt = breaker.breakLine(oneLine, pos + maxText);
			count += addAdditionalSpacing(pos + maxText, breakAt, oneLine, ans,
					pos, count);
			pos = breakAt;
			ans.insert(pos + count, '\n');
			count++;
		}
		return ans;
	}

	/**
	 * Make all spaces in this line become double space if not already is. We do
	 * this only when the right side has too many space to spare from line
	 * breaking algorithm.
	 */
	private static int addAdditionalSpacing(int attempt, int actualBreak,
			String s, StringBuilder ans, int offset, int offset2) {
		final int countSpace = ThaiUtil.countNonZeroWidth(s, actualBreak,
				attempt);
		ArrayList<Integer> addingPlaces = new ArrayList<Integer>();
		boolean isPreviousOneSpace = false;
		for (int i = offset + offset2 + 1; i < actualBreak + offset2 - 2; i++) {
			if (!ThaiUtil.isWhiteSpace(ans.charAt(i))) {
				isPreviousOneSpace = false;
				continue;
			}
			if (isPreviousOneSpace) {
				if (addingPlaces.get(addingPlaces.size() - 1) == i - 1)
					addingPlaces.remove(addingPlaces.size() - 1);
				continue;
			}
			addingPlaces.add(i);
			isPreviousOneSpace = true;
		}
		if (countSpace < addingPlaces.size())
			return 0;
		for (int i = 0; i < addingPlaces.size(); i++)
			ans.insert(i + addingPlaces.get(i), NBSP);
		return addingPlaces.size();
	}

	/**
	 * SaraUm which is one character is misunderstood by native android to be 2
	 * chars so we need to convert it back. Damned u Android!!
	 * 
	 */
	private static int resolveCorrectPositionForJB(String s, int start,
			int end, int pos) {
		TreeSet<Integer> saraUmPos = new TreeSet<Integer>();
		for (int i = start; i < end; i++)
			if (s.charAt(i) == 'ำ')
				saraUmPos.add(i - start);
		return pos - saraUmPos.headSet(pos).size();
	}
}
