package learn.storm;

import backtype.storm.LocalCluster;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.testing.FixedBatchSpout;

public class TestTridentTopology2 {

	private LocalCluster cluster = new LocalCluster();

	private void setUp() {
		TridentTopology topology = new TridentTopology();

		@SuppressWarnings("unchecked")
		FixedBatchSpout spout = new FixedBatchSpout(
				new Fields("sentence"), 3,
				new Values("the cow jumped over the moon"),
				new Values("the man went to the store and bought some candy"),
				new Values("four score and seven years ago"),
				new Values("how many apples can you eat"));
		spout.setCycle(true);

		Stream stream = topology.newStream("spout1", spout);
	}

	public static void main(String args[]) {

		TridentTopology topology = new TridentTopology();
	}
}
