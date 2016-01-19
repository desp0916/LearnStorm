package learn.storm;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;

public class TestHBase {

	public static void main(String[] args) {

//		System.out.println(logicalKeyA.hashCode());
//		String logicalKeyB = "2015-04-26|ab";
//		System.out.println(logicalKeyB.hashCode());
//		Objects.hashCode(o)
		testSaltedRowKey();
	}

	// http://blog.cloudera.com/blog/2015/06/how-to-scan-salted-apache-hbase-tables-with-region-specific-key-ranges-in-mapreduce/
	private static void testSaltedRowKey() {
		String logicalKey = "2015-04-26|ab";
		String a = StringUtils.leftPad(Integer.toString(Math.abs(Objects.hash(logicalKey) % 1000)), 3, "0") + "|" + logicalKey;
		System.out.println(a);
	}

	private static void testBasicCRUD() {
//		HTableInterface usersTable = new HTable("users");
	}
}
