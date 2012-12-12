package hnk.lib.tlb._interface;

import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * This interface tells the client code using this interface to not use ordinary
 * set/getText and use our alternative set/get2 instead. For {@link BufferType},
 * we only support the normal one.
 * 
 * @author Touchchai Chotisorayuth
 * 
 */
public interface TextViewReplacementMethod {

	/**
	 * use this method instead
	 */
	void setText2(int resid);

	/**
	 * use this method instead
	 */
	void setText2(CharSequence text);

	/**
	 * use this method instead
	 */
	CharSequence getText2();

	@Deprecated
	void setText(int resid);

	@Deprecated
	void setText(char[] text, int start, int len);

	@Deprecated
	void setText(int resid, TextView.BufferType type);

	@Deprecated
	void setText(CharSequence text);

	@Deprecated
	void setText(CharSequence text, TextView.BufferType type);

	@Deprecated
	CharSequence getText();

}
