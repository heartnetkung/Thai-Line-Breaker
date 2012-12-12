package hnk.lib.tlb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

public final class FileUtil {

	public static final Charset utf8 = Charset.forName("UTF-8");
	public static final Charset ms874 = Charset.forName("x-windows-874");

	/**
	 * Read the file as plain text for u. Also delete "Byte Order Mark"(BOM)
	 * automatically. Every possible line break symbol is converted to \n.
	 * 
	 * @param f
	 * @param c
	 * @return
	 * @throws IOException
	 */
	public static String readPlainText(File f, Charset c) throws IOException {
		validateInputFile(f, c);
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(f);
			byte[] in = new byte[(int) f.length()];
			stream.read(in);
			int offset = getOffset(in);
			String ans = new String(in, offset, in.length - offset, c);
			ans = ans.replaceAll(
					"(\\r\\n)|(\\u0085)|(\\u2028)|(\\u2029)|(\\r\\n{0})", "\n");
			return ans;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	public static String readPlainText(InputStream is, Charset c)
			throws IOException {
		if (is == null || c == null)
			throw new NullPointerException();
		try {
			byte[] in = new byte[is.available()];
			is.read(in);
			int offset = getOffset(in);
			String ans = new String(in, offset, in.length - offset, c);
			ans = ans.replaceAll(
					"(\\r\\n)|(\\u0085)|(\\u2028)|(\\u2029)|(\\r\\n{0})", "\n");
			return ans;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			is.close();
		}
	}

	private static int getOffset(byte[] in) {
		if (in.length < 3)
			return 0;
		// case UTF-8 in Windows
		if (in[0] == 0xEF && in[1] == 0xBB && in[2] == 0xBF)
			return 3;
		if (in[0] == 0xFE && in[1] == 0xFF)
			return 2;
		if (in[0] == 0xFF && in[1] == 0xFE)
			return 2;
		return 0;
	}

	public static Charset getSuggestedCharsetIfAny(File f) throws IOException {
		if (!f.canRead())
			throw new IllegalStateException("File " + f + " can't be read.");
		FileInputStream stream = new FileInputStream(f);
		byte[] in = new byte[3];
		try {
			stream.read(in);
			if (in[0] == 0xEF && in[1] == 0xBB && in[2] == 0xBF)
				return Charset.forName("UTF-8");
			if (in[0] == 0xFE && in[1] == 0xFF)
				return Charset.forName("UTF-16BE");
			if (in[0] == 0xFF && in[1] == 0xFE)
				return Charset.forName("UTF-16LE");
			return null;
		} finally {
			stream.close();
		}
	}

	private static void validateInputFile(File f, Charset c) {
		if (f == null || c == null)
			throw new NullPointerException();
		if (!f.canRead())
			throw new IllegalStateException("File " + f + " can't be read.");
	}

	private static OutputStreamWriter createWriter(File f, Charset c) {
		try {
			FileOutputStream stream = new FileOutputStream(f);
			OutputStreamWriter ans = new OutputStreamWriter(stream, c);
			return ans;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IllegalStateException();// never happen since the input is
												// already validated
		}
	}

	/**
	 * This method does 2 things. 1. Add \r in front of every \n in the given
	 * String in case it's not present. 2. Write processed String to File with
	 * given CharacterSet
	 * 
	 * @param f
	 * @param s
	 * @param c
	 * @throws IOException
	 */
	public static void writePlainText(File f, String s, Charset c)
			throws IOException {
		if (f == null || s == null || c == null)
			throw new NullPointerException();
		String postProcess = s.replaceAll("\\r*\\n", "\r\n");
		OutputStreamWriter writer = null;
		try {
			writer = createWriter(f, c);
			writer.write(postProcess);
			writer.close();
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (writer != null)
				writer.close();
		}

	}

	public static byte[] readBinary(File f) throws IOException {
		if (f == null)
			throw new NullPointerException();
		if (!f.canRead())
			throw new IOException("File can't be read");
		if (f.length() > 1000000000)
			throw new IOException("File can't be larger than 1GB.");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			byte[] ans = new byte[(int) f.length()];
			fis.read(ans);
			return ans;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (fis != null)
				fis.close();
		}
	}

	public static void printAvailableCharset() {
		Map<String, Charset> m = Charset.availableCharsets();
		Set<String> s = m.keySet();
		for (String string : s) {
			System.out.println(m.get(string));
		}
	}

	/**
	 * Fast file copy algorithm using java.nio
	 */
	@SuppressWarnings("resource")
	public static void copyFile(File source, File destination)
			throws FileNotFoundException, IOException {
		if (destination.exists())
			destination.createNewFile();
		FileChannel in = new FileInputStream(source).getChannel();
		FileChannel out = new FileOutputStream(destination).getChannel();
		if (in != null && out != null) {
			try {
				in.transferTo(0, in.size(), out);
			} finally {
				in.close();
				out.close();
			}
		} else if (in == null) {
			out.close();
		} else {
			in.close();
		}
	}

	public static void copyDirectory(File source, File destination,
			boolean andAllItsSubdirectories) throws FileNotFoundException,
			IOException {
		File[] allFiles = source.listFiles();
		for (File f : allFiles) {
			if (f.isDirectory() && andAllItsSubdirectories)
				copyDirectory(f, new File(destination, f.getName()),
						andAllItsSubdirectories);
			else if (f.isFile())
				copyFile(f, new File(destination, f.getName()));
		}
	}
}
