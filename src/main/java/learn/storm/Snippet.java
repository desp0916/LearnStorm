package learn.storm;

import java.sql.Timestamp;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Snippet {

	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

	public static void main(String args[]) {
		String dateTimeString = new Timestamp(new Date().getTime()).toString();
		System.out.println(dateTimeString);
//		DateTime dt = new DateTime();
//		System.out.println(formatter.print(dt));
		DateTime dateTime = formatter.parseDateTime(dateTimeString);
		int hour = dateTime.getHourOfDay();
		int minute = dateTime.getMinuteOfHour();
		System.out.println("hour: " + hour + ", minute: " + minute);
	}

}
