package hnk.lib.tlb.engine;

import hnk.lib.tlb.util.FileUtil;
import hnk.lib.tlb.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * the trie of inverse string used as a resource of the calculation.
 * 
 * @author Touchchai Chotisorayuth
 * 
 */
public final class ImmutableInverseTrie {

	private static final char TERMINATING_SYMBOL = '*';

	private final String[] data;
	private transient final int[] jumpPointCache;

	private ImmutableInverseTrie(String[] data) {
		this.data = data;
		int length = data.length;
		jumpPointCache = new int[length];
		if (length > 0) {
			jumpPointCache[0] = 1;
			for (int i = 1; i < length; i++) {
				String previous = data[i - 1];
				int previousLength = previous.length();
				if (previous.length() == 0)
					continue;
				int toAdd = previous.charAt(previousLength - 1) == TERMINATING_SYMBOL ? previousLength - 1
						: previousLength;
				jumpPointCache[i] = toAdd + jumpPointCache[i - 1];
			}
		}
	}

	public static ImmutableInverseTrie deserialize(InputStream is)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is,
				FileUtil.ms874));
		String line;
		ArrayList<String> ans = new ArrayList<String>();
		try {
			while ((line = br.readLine()) != null) {
				ans.add(line);
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
		return new ImmutableInverseTrie(ans.toArray(new String[ans.size()]));
	}

	public static ImmutableInverseTrie deserialize(String longString) {
		return new ImmutableInverseTrie(Utils.longStringToList(longString)
				.toArray(new String[0]));
	}

	public static ImmutableInverseTrie fromWordList(String longString) {
		return fromWordList2(Utils.longStringToList(longString).toArray(
				new String[0]));
	}

	@SuppressWarnings("unchecked")
	public static ImmutableInverseTrie fromWordList2(String[] words) {
		// create charnode tree
		CharNode root = new CharNode('!');// just a char
		CharNode current = root;
		for (int i = 0; i < words.length; i++, current = root) {
			String currentWord = words[i].trim();
			if (currentWord.length() == 0)
				continue;
			for (int j = currentWord.length() - 1; j >= 0; j--)
				current = current.putNode(currentWord.charAt(j));
			current.putNode(TERMINATING_SYMBOL);
		}

		// charnode tree to ans
		ArrayList<String> data = new ArrayList<String>();
		ArrayList<CharNode> buffer = new ArrayList<ImmutableInverseTrie.CharNode>();
		buffer.add(root);
		final CharNode terminator = new CharNode(TERMINATING_SYMBOL);
		while (buffer.size() != 0) {
			CharNode current2 = buffer.remove(0);
			data.add(current2.childToString());
			TreeSet<CharNode> clone = (TreeSet<CharNode>) current2.children
					.clone();
			clone.remove(terminator);
			buffer.addAll(clone);
		}

		return new ImmutableInverseTrie(data.toArray(new String[data.size()]));
	}

	public String serialize() {
		StringBuilder ans = new StringBuilder();
		for (int i = 0; i < data.length; i++)
			ans.append(data[i]).append('\n');
		if (data.length > 0)
			ans.deleteCharAt(ans.length() - 1);
		return ans.toString();
	}

	public String toWordList() {
		String[] data = toWordList2();
		StringBuilder ans = new StringBuilder();
		for (String s : data)
			ans.append(s).append('\n');
		if (data.length > 0)
			ans.deleteCharAt(ans.length() - 1);
		return ans.toString();
	}

	public String[] toWordList2() {
		CharNode root = new CharNode('!');// just a char
		dataToCharnode(root);

		ArrayList<String> ans = new ArrayList<String>();
		recursiveCharNodeToWordList(ans, root, "");
		return ans.toArray(new String[ans.size()]);
	}

	private void dataToCharnode(CharNode current) {
		ArrayList<CharNode> queue = new ArrayList<ImmutableInverseTrie.CharNode>();
		queue.add(current);
		int i = 0;
		while (!queue.isEmpty()) {
			CharNode current2 = queue.remove(0);
			String s = data[i];
			for (int j = 0; j < s.length(); j++) {
				char c = s.charAt(j);
				CharNode cn = current2.putNode(c);
				if (c != TERMINATING_SYMBOL)
					queue.add(cn);
			}
			i++;
		}
	}

	private static void recursiveCharNodeToWordList(List<String> ans,
			CharNode currentNode, String prefix) {
		for (CharNode c : currentNode.children) {
			if (c.c == TERMINATING_SYMBOL)
				ans.add(new StringBuilder(prefix).reverse().toString());
			else
				recursiveCharNodeToWordList(ans, c, prefix + c.c);
		}
	}

	@Override
	public String toString() {
		StringBuilder ans = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			ans.append(i).append(": ").append(data[i]).append(">>")
					.append(jumpPointCache[i]).append('\n');
		}
		return ans.toString();
	}

	/**
	 * 
	 * @param longString
	 * @param offset
	 * @return
	 */
	public int longestMatch(CharSequence longString, int offset)
			throws NullPointerException, IndexOutOfBoundsException {
		if (longString == null)
			throw new NullPointerException();
		if (offset > longString.length() || offset < 0)
			throw new IndexOutOfBoundsException();
		int ans = -1;
		for (int stringIndex = offset - 1, dataIndex = 0; stringIndex >= 0; stringIndex--) {
			// init values
			String possibleCharacters = data[dataIndex];
			char currentChar = longString.charAt(stringIndex);
			boolean isWordEnd = possibleCharacters.charAt(possibleCharacters
					.length() - 1) == TERMINATING_SYMBOL;
			int indexOf = possibleCharacters.indexOf(currentChar);

			// control statements
			if (isWordEnd)
				ans = stringIndex + 1;
			if (currentChar == TERMINATING_SYMBOL)
				break;// this character could cause bug, aborting now!
			if (indexOf == -1)
				break;// no more matching

			// increment
			dataIndex = jumpPointCache[dataIndex] + indexOf;
		}
		return ans;
	}

	// since fromWordList and toWordList is so hard to implement we need a real
	// trie to parse this to our format
	private static class CharNode implements Comparable<CharNode> {
		private final char c;
		private final TreeSet<CharNode> children;

		public CharNode(char c) {
			this.c = c;
			children = new TreeSet<CharNode>();
		}

		public CharNode putNode(char c) {
			CharNode newNode = new CharNode(c), ceil = children
					.ceiling(newNode);
			if (!newNode.equals(ceil)) {
				children.add(newNode);
				return newNode;
			}
			return ceil;
		}

		public String childToString() {
			StringBuilder ans = new StringBuilder();
			for (CharNode c : children)
				ans.append(c.c);
			return ans.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || obj instanceof CharNode == false)
				return false;
			return ((CharNode) obj).c == c;
		}

		@Override
		public int hashCode() {
			return c;
		}

		@Override
		public String toString() {
			return c + "";
		}

		public int compareTo(CharNode o) {
			return o.c - c;
		}

	}
}
