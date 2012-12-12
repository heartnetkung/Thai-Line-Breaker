package hnk.lib.tlb.engine;

import hnk.lib.tlb.util.FileUtil;
import hnk.lib.tlb.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;

/**
 * the mapping from short words to larger words containing corresponding short
 * word as a resource of the calculation.
 * 
 * @author Touchchai Chotisorayuth
 * 
 */
public final class ImmutableContainmentSearcher {

	// small word > multiple large word containing small word with the indexOf
	private final MultiValueMap map = MultiValueMap
			.decorate(new HashMap<String, StringAndIndex>());

	private ImmutableContainmentSearcher() {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ImmutableContainmentSearcher build(String wordList) {
		if (wordList == null)
			throw new NullPointerException();
		List<String> theList = Utils.longStringToList(wordList);
		ImmutableContainmentSearcher ans = new ImmutableContainmentSearcher();
		Map map = ans.map;
		int indexOf;
		for (String s : theList)
			for (String a : theList)
				if ((indexOf = a.indexOf(s)) != -1 && a.length() != s.length())
					map.put(s, new StringAndIndex(a, indexOf));
		return ans;
	}

	@SuppressWarnings("unchecked")
	public String serialize() {
		StringBuilder ans = new StringBuilder();
		for (Object key : map.keySet()) {
			ans.append(key).append('\n');
			Collection<StringAndIndex> values = (Collection<StringAndIndex>) map
					.getCollection(key);
			for (StringAndIndex value : values)
				ans.append('\t').append(value.s).append('\t')
						.append(value.index).append('\n');
		}
		if (ans.length() != 0)
			ans.deleteCharAt(ans.length() - 1);
		return ans.toString();
	}

	@SuppressWarnings("unchecked")
	public WordPosition findWholeWord(CharSequence wholeText,
			int partialWordStart, int partialWordEnd) {
		int bestCandidateLength = -1;
		WordPosition bestCandidatePos = null;
		if (partialWordStart < 0 || partialWordEnd > wholeText.length())
			throw new IndexOutOfBoundsException();
		String keyword = wholeText
				.subSequence(partialWordStart, partialWordEnd).toString();
		Collection<StringAndIndex> values = (Collection<StringAndIndex>) map
				.getCollection(keyword);
		if (values == null)
			return null;

		for (StringAndIndex value : values) {
			String candidate = value.s;
			int wordStart = partialWordStart - value.index;
			if (stringEqual(wholeText, wordStart, candidate)) {
				if (candidate.length() > bestCandidateLength) {
					bestCandidateLength = candidate.length();
					bestCandidatePos = new WordPosition(wordStart, wordStart
							+ candidate.length());
				}
			}
		}
		return bestCandidatePos;
	}

	private static boolean stringEqual(CharSequence wholeText,
			int wholeTextOffset, String theOther) {
		int wordEnd = wholeTextOffset + theOther.length();
		if (wholeTextOffset < 0 || wordEnd > wholeText.length())
			return false;
		for (int i = wholeTextOffset; i < wordEnd; i++)
			if (theOther.charAt(i - wholeTextOffset) != wholeText.charAt(i))
				return false;
		return true;
	}

	public static ImmutableContainmentSearcher deserialize(String longString) {
		List<String> ls = Utils.longStringToList(longString);
		ImmutableContainmentSearcher ans = new ImmutableContainmentSearcher();
		String currentKey = null;
		for (String s : ls) {
			if (s.isEmpty())
				continue;
			if (s.charAt(0) == '\t') {
				// handle value
				if (currentKey == null)
					throw new RuntimeException(new ParseException(s, 0));
				int tabIndex = s.indexOf('\t', 1);
				if (tabIndex == -1)
					throw new RuntimeException(new ParseException(s, 0));
				String valueString = s.substring(1, tabIndex);
				int valueInt = Integer.parseInt(s.substring(tabIndex + 1,
						s.length()));
				ans.map.put(currentKey, new StringAndIndex(valueString,
						valueInt));
			} else {
				// handle key
				currentKey = s;
			}
		}
		return ans;
	}

	public static ImmutableContainmentSearcher deserialize(InputStream stream)
			throws IOException {
		ImmutableContainmentSearcher ans = new ImmutableContainmentSearcher();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream,
				FileUtil.ms874));
		String line, currentKey = null;
		try {
			while ((line = br.readLine()) != null) {
				if (line.isEmpty())
					continue;
				if (line.charAt(0) == '\t') {
					// handle value
					if (currentKey == null)
						throw new RuntimeException(new ParseException(line, 0));
					int tabIndex = line.indexOf('\t', 1);
					if (tabIndex == -1)
						throw new RuntimeException(new ParseException(line, 0));
					String valueString = line.substring(1, tabIndex);
					int valueInt = Integer.parseInt(line.substring(
							tabIndex + 1, line.length()));
					ans.map.put(currentKey, new StringAndIndex(valueString,
							valueInt));
				} else {
					// handle key
					currentKey = line;
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ans;
	}

	private static final class StringAndIndex {
		private final String s;
		private final int index;

		private StringAndIndex(String s, int index) {
			if (s == null)
				throw new NullPointerException();
			this.s = s;
			this.index = index;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || obj instanceof StringAndIndex == false)
				return false;
			StringAndIndex other = (StringAndIndex) obj;
			return other.s.equals(s) && other.index == index;
		}

		@Override
		public int hashCode() {
			return s.hashCode() * 10 + index;
		}
	}

}
