package hnk.lib.tlb.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public final class Utils {
	private Utils() {
	}

	public static List<String> longStringToList(String longString) {
		if (longString == null)
			throw new NullPointerException();
		BufferedReader reader = new BufferedReader(new StringReader(longString));
		ArrayList<String> data = new ArrayList<String>();
		String line;
		try {
			while ((line = reader.readLine()) != null)
				data.add(line);
			return data;
		} catch (IOException e) {
			throw new RuntimeException(
					"io error on string reader should never happen", e);
		}
	}

	public static CharSequence trimTrailingWhiteSpace(CharSequence s) {
		int lastPos = s.length();
		for (int i = s.length() - 1; i >= 0; i--) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c))
				lastPos--;
			else
				break;
		}
		return s.subSequence(0, lastPos);
	}
	
}
