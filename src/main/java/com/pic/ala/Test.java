package com.pic.ala;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class Test implements Serializable {

	private static final long serialVersionUID = -3162870358454064574L;

	public static void main(String args[]) {
		Timestamp t = new Timestamp(new Date().getTime());
		System.out.println(t.toString());
		Timestamp execTimeStamp = Timestamp.valueOf(cleanup(t.toString()));
		System.out.println(execTimeStamp.toString());
	}

	private static String cleanup(String str) {
		if (str != null) {
			return str.trim().replace("\n", "").replace("\t", "");
		} else {
			return str;
		}
	}
}
