package learn.storm.trident.aggregator;

import backtype.storm.tuple.Values;
import storm.trident.operation.BaseAggregator;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

/**
 *
 * The Aggregator interface first runs the global repartitioning operation on
 * the input stream to combine all the partitions of the same batch into a
 * single partition, and then runs the aggregation function on each batch. By
 * definition, the Aggregator interface looks very similar to the
 * ReduceAggregator interface. The BaseAggregator<State> interface contains the
 * following methods:
 *
 */

public class SumAsAggregator extends
	BaseAggregator<SumAsAggregator.State> {

	private static final long serialVersionUID = 1L;

	// state class
	static class State {
		long count = 0;
	}

	// Initialize the state
	public State init(Object batchId, TridentCollector collector) {
		return new State();
	}

	// Maintain the state of sum into count variable.
	public void aggregate(State state, TridentTuple tridentTuple,
			TridentCollector tridentCollector) {
		state.count = tridentTuple.getLong(0) + state.count;
	}

	// return a tuple with single value as output
	// after proccessing all the tuples of given batch.
	public void complete(State state, TridentCollector tridentCollector) {
		tridentCollector.emit(new Values(state.count));
	}
}