package hnk.lib.tlb.engine;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import hnk.lib.tlb.util.FileUtil;

import org.junit.Test;

public class ImmutableInverseTrieTest {

	@Test
	public void testDeserialize() {
		// TODO test invalid format case

		// reflective test
		String test1 = "t\nao\nr\nr\nm\n*\n*";
		ImmutableInverseTrie ans = ImmutableInverseTrie.deserialize(test1);
		assertEquals(test1, ans.serialize());

		// empty string
		String test2 = "";
		ans = ImmutableInverseTrie.deserialize(test2);
		assertEquals(test2, ans.serialize());

		// null
		try {
			ImmutableInverseTrie.deserialize((InputStream)null);
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testFromWordList() {
		String[] wordList = new String[] { "tor", "tarm", "term", "ton",
				"hello" };
		ImmutableInverseTrie ans = ImmutableInverseTrie.fromWordList2(wordList);
		System.out.println(ans);
		System.out.println("=======");
		System.out.println(ans.toWordList());
	}

	@Test
	public void testToWordList() throws IOException {
		String file = FileUtil.readPlainText(new File("lexitron.txt"),
				FileUtil.ms874);
		FileUtil.writePlainText(new File("lexitron2.txt"), ImmutableInverseTrie
				.fromWordList(file).serialize(), FileUtil.ms874);
		FileUtil.writePlainText(new File("lexitron3.txt"), ImmutableInverseTrie
				.fromWordList(file).toWordList(), FileUtil.ms874);
	}

	@Test
	public void testLongestMatch() {
		String[] wordList = new String[] { "tor", "tarm", "term", "ton",
				"hello","หนุ่ย" };
		ImmutableInverseTrie ans = ImmutableInverseTrie.fromWordList2(wordList);
		String test = "tortarmtermtonnoหนุ่ย";
		System.out.println("======================");
		for (int i = 0; i <= test.length(); i++) {
			System.out.println(i);
			int pos = ans.longestMatch(test, i);
			if (pos != -1)
				System.out.println(new StringBuilder(test).insert(pos, '_'));
		}
	}

	@Test
	public void realWorldTest() {
		try {
			ImmutableInverseTrie ans = ImmutableInverseTrie
					.deserialize(FileUtil.readPlainText(new File(
							"trie_cache.txt"), FileUtil.ms874));
			String test = "ทยอยส่งความคิดเห็นจากผู้ทดลองใช้ครับ  โดยผมจะรวบรวมมาสรุปอีกครั้งครับ\r\n"
					+ "ต้องขอบคุณอุ้ย ที่ส่งมาเป็นคนแรกเลย\r\n"
					+ " \r\n"
					+ "สำหรับคนอื่นๆ ขอร้องให้ช่วยสรุปให้กับทาง อ.จอม ด้วยครับ ยิ่งได้ความคิดเห็นเร็ว ยิ่งทำให้ออกมาสมบูรณ์\r\n"
					+ "และรวดเร็วขึ้น\r\n"
					+ " \r\n"
					+ "ขอบคุณครับ\r\n"
					+ "หนุ่ย";
			for (int i = 0; i <= test.length(); i++) {
				System.out.println(i);
				int pos = ans.longestMatch(test, i);
				if (pos != -1)
					System.out
							.println(new StringBuilder(test).insert(pos, '_'));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
