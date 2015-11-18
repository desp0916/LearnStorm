package learn.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.StormSubmitter;
import backtype.storm.drpc.LinearDRPCTopologyBuilder;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class DRPCTest {

	public static class ExclaimBolt extends BaseBasicBolt {
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			String input = tuple.getString(1);
			collector.emit(new Values(tuple.getValue(0), input + "!"));
		}

		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("id", "result"));
		}
	}

	public static void main(String args[]) throws Exception {
		LinearDRPCTopologyBuilder builder = new LinearDRPCTopologyBuilder("exclamation");
		builder.addBolt(new ExclaimBolt(), 3);

		Config conf = new Config();

		if (args == null || args.length == 0) {
			LocalDRPC drpc = new LocalDRPC();
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("drpc-demo", conf, builder.createLocalTopology(drpc));
			for (String word : new String[] { "hello", "goodbye" }) {
				System.out.println("Result for \"" + word + "\": " + drpc.execute("exclamation", word));
			}

			Thread.sleep(10000);
			drpc.shutdown();
			cluster.shutdown();
		} else {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createRemoteTopology());
		}
	}
}
