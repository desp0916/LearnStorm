package learn.storm.trident.partition;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.storm.guava.collect.ImmutableList;
import org.apache.storm.guava.collect.ImmutableMap;

import backtype.storm.generated.GlobalStreamId;
import backtype.storm.grouping.CustomStreamGrouping;
import backtype.storm.task.WorkerTopologyContext;

public class CountryRepartition implements CustomStreamGrouping, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Map<String, Integer> countries =
		ImmutableMap.of(
			"India", 0,
			"Japan", 1,
			"United State", 2,
			"China", 3,
			"Brazil", 4
		);

	private int tasks = 0;

	public void prepare(WorkerTopologyContext context,
			GlobalStreamId stream, List<Integer> targetTasks) {
		tasks = targetTasks.size();
	}

	/**
	 * This method contains the repartitioning logic to identify the
	 * next task in the topology for the input tuple.
	 */
	public List<Integer> chooseTasks(int taskId, List<Object> values) {
		String country = (String) values.get(0);
		return ImmutableList.of(countries.get(country) % tasks);
	}

}
