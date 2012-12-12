package hnk.lib.tlb.util;

public final class ThaiUtil {

	private ThaiUtil() {
	}

	public static boolean isWhiteSpace(char c) {
		return c == ' ' || c == '\u00a0';
	}

	public static boolean isThaiChar(char c) {
		return c >= '\u0e01' && c <= '\u0e5b';
	}

	public static boolean isZeroWidth(char c) {
		// wannayuk
		if (c <= '\u0e4d' && c >= '\u0e47')
			return true;
		// sara
		if (c <= '\u0e3a' && c >= '\u0e31' && c != '\u0e32' && c != '\u0e33')
			return true;
		return false;
	}

	public static int countNonZeroWidth(CharSequence s, int from, int to) {
		int ans = 0;
		for (int i = from; i < to; i++)
			if (!isZeroWidth(s.charAt(i)))
				ans++;
		return ans;
	}

	public static boolean isPunctuation(char c) {
		// not dot
		return c == ' ' || c == '\'' || c == '"' || c == '\u00a0' || c == '’'
				|| c == '”' || c == '‘' || c == '“';
	}

	public static boolean isFrontDependentChar(char c) {
		return c == 'ะ' || c == 'า' || c == 'ำ' || c == '\'' || c == '"'
				|| c == '’' || c == '”' || c == 'ๆ' || isZeroWidth(c);
	}

	public static boolean isRearDependentChar(char c) {
		return (c <= 'ไ' && c >= 'เ') || c == 'ั' || c == '\'' || c == '"'
				|| c == '‘' || c == '“';
	}
}
