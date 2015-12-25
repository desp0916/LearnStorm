package learn.storm;

import java.sql.Timestamp;
import java.util.HashMap;

public class TestJava {

	private static void testTimestamp() {
		System.out.println(new Timestamp(System.currentTimeMillis()));
//		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
//		System.out.println(System.currentTimeMillis());
	}

	private static void test1() {
		String a = null;
		HashMap<String, Object> Config = new HashMap<String, Object>();
		Config.put("test", null);
		String test = (String) Config.get("test");
		if (test == null) {
			System.out.println(test);
		}
	}

	public static void main(String[] args) {
		testTimestamp();
	}

}