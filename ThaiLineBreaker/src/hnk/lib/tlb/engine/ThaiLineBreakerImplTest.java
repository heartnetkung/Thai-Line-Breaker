package hnk.lib.tlb.engine;

import java.io.File;
import java.io.IOException;

import hnk.lib.tlb._interface.ThaiLineBreaker;
import hnk.lib.tlb.util.FileUtil;

import org.junit.Test;

public class ThaiLineBreakerImplTest {

	private final ThaiLineBreaker tlb;

	public ThaiLineBreakerImplTest() {
		ImmutableContainmentSearcher ics = null;
		ImmutableInverseTrie iit = null;
		try {
			ics = ImmutableContainmentSearcher.deserialize(FileUtil
					.readPlainText(new File("whole_word_cache.txt"),
							FileUtil.ms874));
			iit = ImmutableInverseTrie.deserialize(FileUtil.readPlainText(
					new File("trie_cache.txt"), FileUtil.ms874));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tlb = new ThaiLineBreakerImpl(iit, ics);

	}

	@Test
	public void test1() {
		String test = "ช่วงนี้เป็นเทศกาลกินเจ เพื่อนๆ ทุกคนออกบ้านไปหาของแดก ไม่มีคนยอมกินเนื้อสัตว์เป็นเพื่อนเลย ไม่รู้จะทำยังไงเลยออกไปกิน MK GOLD RESTAURRANT คนเดียว เดี๋ยวนะ #แบบนี้ดีแล้วหรอ??? 1,000,100,000 "
				+ "ต้องขอบคุณอุ้ย ที่ส่งมาเป็นคนแรกเลย\r\n"
				+ " \r\n"
				+ "สำหรับคนอื่นๆ ขอร้องให้ช่วยสรุปให้กับทาง อ.จอม ด้วยครับ ยิ่งได้ความคิดเห็นเร็ว ยิ่งทำให้ออกมาสมบูรณ์\r\n"
				+ "และรวดเร็วขึ้น\r\n" + " \r\n" + "ขอบคุณครับ\r\n" + "หนุ่ย";
		for (int i = 10; i <= test.length(); i++)
			System.out.println(new StringBuilder(test).insert(i, 'x').insert(
					tlb.breakLine(test, i), '_'));
	}

}
