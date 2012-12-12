package hnk.lib.tlb.engine;

import hnk.lib.tlb._interface.ThaiLineBreaker;
import hnk.lib.tlb.util.ThaiUtil;

public class ThaiLineBreakerImpl implements ThaiLineBreaker {
	private final ImmutableInverseTrie dictionaryWalker;
	private final ImmutableContainmentSearcher wholeWordSearcher;
	/**
	 * we count only non zero width character
	 */
	private static final int VISIBLE_WALKING_LIMIT = 15;
	private static final int WALKING_LIMIT = 20;

	public ThaiLineBreakerImpl(ImmutableInverseTrie dictionaryWalker,
			ImmutableContainmentSearcher wholeWordSearcher) {
		this.dictionaryWalker = dictionaryWalker;
		this.wholeWordSearcher = wholeWordSearcher;
	}

	public int breakLine(final CharSequence longString, int breakingAttempt) {
		if (longString == null)
			throw new NullPointerException();
		if (breakingAttempt < 0 || breakingAttempt > longString.length())
			throw new IndexOutOfBoundsException("index is " + breakingAttempt
					+ " string is " + longString);
		if (breakingAttempt == longString.length())
			return breakingAttempt;
		breakingAttempt = preBiaser(longString, breakingAttempt);
		WordPosition ans = doBreakLine(longString, breakingAttempt);
		// second attempt in case the word of length 2 accidentally has meaning
		if (ans.length() == 2 && breakingAttempt > 0) {
			WordPosition temp = doBreakLine(longString, breakingAttempt);
			if (temp.length() > 2)
				ans = temp;
		}
		int ansPos = ans.getEnd() > breakingAttempt ? ans.getStart() : ans
				.getEnd();
		return postBiaser(longString, ansPos);
	}

	private WordPosition doBreakLine(final CharSequence longString,
			final int breakingAttempt) {
		WordPosition previousKnownWord = findPreviousKnownWord(longString,
				breakingAttempt);
		if (previousKnownWord.isEmpty())
			return previousKnownWord;
		WordPosition p = wholeWordSearcher.findWholeWord(longString,
				previousKnownWord.getStart(), previousKnownWord.getEnd());
		return p == null ? previousKnownWord : p;
	}

	private static int preBiaser(final CharSequence longString,
			final int breakingAttempt) {
		// mai yamok
		boolean foundOnce = false;
		int ans;
		for (ans = breakingAttempt; ans >= 1; ans--) {
			char c = longString.charAt(ans - 1);
			if (c == 'ๆ')
				foundOnce = true;
			else if (foundOnce && ThaiUtil.isWhiteSpace(c))
				continue;
			else
				break;
		}
		if (foundOnce)
			return Math.max(0, ans - 1);
		return ans;
	}

	private static int postBiaser(final CharSequence longString,
			final int breakingAttempt) {
		// beware case ''
		// handle rear dep
		if (breakingAttempt != 0
				&& ThaiUtil.isRearDependentChar(longString
						.charAt(breakingAttempt - 1)))
			return breakingAttempt - 1;
		// handle front dep
		if (breakingAttempt <= longString.length() - 1
				&& ThaiUtil.isFrontDependentChar(longString
						.charAt(breakingAttempt)))
			return breakingAttempt - 1;
		return breakingAttempt;
	}

	private WordPosition findPreviousKnownWord(final CharSequence longString,
			final int breakingAttempt) {
		final int walkLimit = Math.max(0, breakingAttempt - WALKING_LIMIT);

		for (int i = breakingAttempt - 1, visibleWalkCount = 0; i >= walkLimit
				&& visibleWalkCount <= VISIBLE_WALKING_LIMIT; i--) {
			char c = longString.charAt(i);
			int nextFound = dictionaryWalker.longestMatch(longString, i + 1);
			if (nextFound != -1)
				return new WordPosition(nextFound, i + 1);
			if (ThaiUtil.isPunctuation(c))
				return new WordPosition(i + 1, i + 1);
			if (!ThaiUtil.isZeroWidth(c))
				visibleWalkCount++;
		}
		// in case of screaming words like arkkkkkkkkkkkkkkkkk just cut it
		// wherever is appropriated
		return new WordPosition(breakingAttempt, breakingAttempt);
	}
}
