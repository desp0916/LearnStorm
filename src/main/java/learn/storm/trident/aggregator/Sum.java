package learn.storm.trident.aggregator;

import storm.trident.operation.ReducerAggregator;
import storm.trident.tuple.TridentTuple;

public class Sum implements ReducerAggregator<Long> {

	private static final long serialVersionUID = 1L;

	// return the initial value zero
	public Long init() {
		return 0L;
	}

	// Iterates on the input tuples, calculate the sum and
	// produce the single tuple with single field as output
	public Long reduce(Long curr, TridentTuple tuple) {
		return curr + tuple.getLong(0);
	}
}
