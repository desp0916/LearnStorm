package learn.storm.trident.function;

import backtype.storm.tuple.Values;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

// 如果原先輸入 tuple 有  4 個欄位 (a, b, c, d)，
// 但經過此函式以後，則輸出 tuple 會變成 5 個欄位
// (a, b, c, d, sum

// 如何使用？
// dummyStream.each(new Fields("a", "b"), new SumFunction(), new Fileds("sum"))

public class SumFunction extends BaseFunction {
	private static final long serialVersionUID = 5L;

	public void execute(TridentTuple tuple, TridentCollector collector) {
		int number1 = tuple.getInteger(0);
		int number2 = tuple.getInteger(1);
		int sum = number1 + number2;
		// emit the sum of first tow fields
		collector.emit(new Values(sum));
	}
}
