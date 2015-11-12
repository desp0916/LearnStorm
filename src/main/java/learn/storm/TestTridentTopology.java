package learn.storm;

/**
 * mvn compile exec:java
 */

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.FilterNull;
import storm.trident.operation.builtin.MapGet;
import storm.trident.operation.builtin.Sum;
import storm.trident.testing.FixedBatchSpout;
import storm.trident.testing.MemoryMapState;
import storm.trident.testing.Split;

public class TestTridentTopology {

	@Debug(level = Debug.Level.UNIT, name = "some test", tester = "justin")
	public void doSomething() {
	}

	public static void main(String args[]) throws Exception {

		TridentTopology topology = new TridentTopology();
		Config conf = new Config();

		@SuppressWarnings("unchecked")
		FixedBatchSpout spout = new FixedBatchSpout(
				new Fields("sentence"), 3,
				new Values("the cow jumped over the moon"),
				new Values("the man went to the store and bought some candy"),
				new Values("four score and seven years ago"),
				new Values("how many apples can you eat"));
		spout.setCycle(true);

		TridentState wordCounts = topology.newStream("spout1", spout)
				.each(new Fields("sentence"), new Split(), new Fields("word"))
				.groupBy(new Fields("word"))
				.persistentAggregate(new MemoryMapState.Factory(), new Count(), new Fields("count"))
				.parallelismHint(6);

		// MapGet() : gets the count for each word
		topology.newDRPCStream("words")
				.each(new Fields("args"), new Split(), new Fields("word"))
				.groupBy(new Fields("word"))
				.stateQuery(wordCounts, new Fields("word"), new MapGet(), new Fields("count"))
				.each(new Fields("count"), new FilterNull())
				.aggregate(new Fields("count"), new Sum(), new Fields("sum"));

//		DRPCClient client = new DRPCClient("drpc.server.location", 3772);
//		System.out.println(client.execute("words", "cat dog the man");

//		conf.setDebug(true);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("test", conf, topology.build());

		Utils.sleep(10000);

		cluster.killTopology("test");
		cluster.shutdown();

	}
}
