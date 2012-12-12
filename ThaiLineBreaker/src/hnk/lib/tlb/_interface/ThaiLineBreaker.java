package hnk.lib.tlb._interface;

/**
 * The Interface ThaiLineBreaker.
 */
public interface ThaiLineBreaker {

	/**
	 * Return the position somewhere near(mostly before) breaking attempt that
	 * is aesthetically appropriate to break the line. In the operation we count
	 * non-breaking-space(nbsp) as ordinary space. The exclusion of line break
	 * is not included in this method since it concern character width
	 * calculation.
	 * 
	 * @param longString
	 *            the whole text
	 * @param breakingAttempt
	 *            the position determined by the font glyph that has the
	 *            appropriate length to break line without considering the
	 *            language context
	 * @return the position to break text, return breaking attempt if no
	 *         appropriate position is found
	 */
	int breakLine(CharSequence longString, int breakingAttempt);

}
