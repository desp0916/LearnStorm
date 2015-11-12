package learn.storm.trident.filter;

import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

public class CheckEvenSumFilter extends BaseFilter {

/**
 * dummyStream.each(new Fields("a", "b"), new CheckEvenSumFilter())
 *
 * The outputStream stream contains only those tuples whose sum
 * of the a and b fields is even.
 */
private static final long serialVersionUID = 7L;

	public boolean isKeep(TridentTuple tuple) {
		int number1 = tuple.getInteger(0);
		int number2 = tuple.getInteger(1);
		int sum = number1+number2;
		if (sum %2 == 0) {
			return true;
		}
		return false;
	}
}
