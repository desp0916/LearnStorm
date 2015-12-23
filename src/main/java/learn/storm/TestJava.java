package learn.storm;

import java.util.HashMap;

public class TestJava {
	public static void main(String[] args) {
		String a = null;
		HashMap<String, Object> Config = new HashMap<String, Object>();
		Config.put("test", null);
		String test = (String) Config.get("test");
		if (test == null) {
			System.out.println(test);
		}
	}
}
