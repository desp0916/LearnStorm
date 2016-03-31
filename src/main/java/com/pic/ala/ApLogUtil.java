package com.pic.ala;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class ApLogUtil {

	private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

	/**
	 * 檢查某字串是否為數字？
	 *
	 * http://stackoverflow.com/questions/2563608/check-whether-a-string-is-parsable-into-long-without-try-catch
	 *
	 * @param str	要檢查的字串
	 * @return		檢查結果
	 */
	public static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		if (sz == 0) {
			return false;
		}
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 檢查某字串是否為整數？
	 *
	 * http://stackoverflow.com/questions/237159/whats-the-best-way-to-check-to-see-if-a-string-represents-an-integer-in-java
	 *
	 * @param str	要檢查的字串
	 * @return		檢查結果
	 */
	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 將字串轉換為日期
	 *
	 * @param dateTimeFormatter		日期時間格式轉換器
	 * @param str					要轉換的字串
	 * @return						轉換結果
	 */
	public static String dateToString(String str, DateTimeFormatter dateTimeFormatter) {
		DateTime dt = dateTimeFormatter.parseDateTime(str);
		return dt.toString(fmt);
	}

	/**
	 * 將字串解析並格式化成某特定日期時間格式
	 *
	 * @param dateTimeString		字串（應為日期時間）
	 * @param dateTimeFormatter		日期時間格式轉換器
	 * @param validFormats			可接受哪些日期時間格式
	 * @param outputFormat			希望輸出的日期時間格式
	 * @return						格式化結果
	 */
	public static String parseDateTime(String dateTimeString, DateTimeFormatter dateTimeFormatter,
			String[] validFormats, String outputFormat) {
		for (int i = 0; i < validFormats.length; i++) {
			SimpleDateFormat format = new SimpleDateFormat(validFormats[i]);
			DateFormat df = new SimpleDateFormat(outputFormat);
			format.setLenient(false);
			try {
				Date temp = format.parse(dateTimeString);
				if (temp != null) {
					return df.format(temp);
				}
			} catch (ParseException e) {
			}
		}
		return null;
	}

	/**
	 * 檢查字串是否為空或 Null？
	 *
	 * @param dateString		字串
	 * @return					檢查結果
	 */
	public static boolean isNullOrEmpty(String str) {
		if (str == null || ("").equals(str)) {
			return true;
		}
		return false;
	}

	/**
	 * 檢查日期字串是否合法？
	 * http://stackoverflow.com/questions/4528047/checking-the-validity-of-a-date
	 *
	 * @param dateString		日期字串
	 * @param validFormat		合法的日期格式
	 * @return					檢查結果
	 */
	public static boolean isDateValid(String dateString, String validFormat) {
		DateFormat df;
		df = (validFormat == null) ? new SimpleDateFormat("yyyy-MM-dd") : new SimpleDateFormat(validFormat);
		df.setLenient(false);
		try {
			df.parse(dateString);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}
}
