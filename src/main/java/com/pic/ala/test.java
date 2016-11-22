package com.pic.ala;

import java.io.PrintWriter;
import java.io.StringWriter;

public class test {
	
	private static String genStackTrace() {
		StringWriter errors = new StringWriter();
		try {
			int i = 1 / 0;
		} catch (Exception ex) {
			ex.printStackTrace(new PrintWriter(errors));
		}
		return errors.toString();
	}
}
