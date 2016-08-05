# Unofficial Storm and Kafka Best Practices Guide

- Author: [Wes Floyd](https://community.hortonworks.com/users/170/wfloyd.html)
- Source: <https://community.hortonworks.com/articles/550/unofficial-storm-and-kafka-best-practices-guide.html>

## 1. Short Description:

A collection of best practices from teams implementing Storm and Kafka in production.

## 2. Article

Please find the attached "Storm/Kafka Best Practices Guide". This document is intended to be an unofficial guide to developing and deploying streaming applications using Storm and Kafka. This is not the official Hortonworks documentation, but meant to be a collection of best practices from teams implementing Storm and Kafka in Production.

## 3. Pre-Reading

If you are new to Storm Development or operations, please review the Tutorial under the [Storm Documentation](https://storm.apache.org/documentation/Home.html) “Read these first” section. This document covers the basics of Storm components and operations.

## 4. Development

The Storm developer has much opportunity to make the topology fast, efficient, and easier to run in Production by his Operations team. This section will discuss the best practices from the developer’s perspective to achieve these goals.

## 4.1 Developing for highest Performance

  1. Keep the Tuple processing code as light as possible. Code so that the `execute()` method in your Bolts can run as quickly as possible. This will have multiplier effects when you deploy at scale.
  2. For bolt-local cache’s consider using Google’s Guava cache if the memory requirements are small (less than 1GB).
  3. For cache’s which share data across multiple bolts consider using HBase, Phoenix, Redis, or MemcacheD.
  4. Externalize as much of your Storm Topology configuration as possible to a properties file that is read when the topology is launched. This allows operations to help turn knobs to improve performance without re-compiling code.

## 4.2 Allowing for ease of re-deployment

Occasionally a live topology may need to be suspended, removed, modified and redeployed against the live stream of messages in order to change its core behavior. To allow for re-deploying an updated topology and avoid having to re-play messages from Kafka the developer should take the following steps when writing their topology:

When using `KafkaSpout` and preparing to deploy to production, ensure the following:

  1. In your `SpoutConfig` “id” and “ zkroot" do NOT change after redeploying the new version of the topology. Storm uses the“ zkroot”, “id” to store the topic offset into zookeeper.
  2. `KafkaConfig.forceFromStart` is set to `false`.

`KafkaSpout` stores the offsets into zookeeper. Be very careful during the re-deployment if you set `forceFromStart` to `true` ( which can be the case when you first deploy the topology) in `KafkaConfig` of the `KafkaSpout` it will ignore stored zookeeper offsets. Setting `forceFromStart` to `true` is useful in development when you need to test the same messages multiple times, but make sure you set it to false when deploying to production, else you’ll re-process already consumed messages each time you restart your topology. In some cases, you may want to run multiple versions of the same, or similar topologies. If that is so, you may specify different client ids for each topology version. Kafka will treat different client IDs as a different consumer applications and will track topic offsets independently of one another.

Consider writing your topology so that the `KafkaConfig.forceFromStart` value is read from a properties file when your Topology’s `main()` method executes. This will allow your administrators to control whether the Kafka messages are replayed or not.

## 4.3 How to redeploy an updated topology

The following steps will allow you to deactivate and redeploy an existing topology without losing its current place in the message queue. This will help avoid having to replay messages:

  1. Deactivate the topology: `storm deactivate [topology name]`
  2. Kill the topology: `storm kill [topology name]`
  3. Deploy the new version of the topology with the same `SpoutConfig` `id` and `zkroot` value.

To deploy a Storm topology and force it to replay messages from the beginning of the message queue:

  1. Deactivate the topology
  2. Kill the topology
  3. Option 1: Remove ZKRoot for your consumer group
    1. Run zookeeper CLI: `/usr/hdp/current/zookeeper-client/bin/zkCli.sh`
    2. Navigate to the the zkroot: `ls /storm/consumers/[my_consumer_id]/offsets/[my_topic]`
    3. Remove the zookeeper node for zkroot: `rmr /storm/[value of zkRoot]`
    4. Start topology per nomal
  4. Option 2: start topology starting from 0 offset
    1. Start topology with Kafka Spout `config.forceFromStart`=`true`.
    2. This is most often set via command line parameter into the application.

## 5. Initial Storm Setup

Storm Supervisors are recommended to run under “Supervision”. This means that the storm worker processes will be automatically restarted if they fail for any reason. Instructions below to setup Supervision:

To configure Storm for operating under supervision: Follow the documentation located here:

Documentation - [How To Configure Storm for Supervision](http://docs.hortonworks.com/HDPDocuments/Ambari-1.7.0.0/Ambari_Doc_Suite/ADS_v170.html#ref-f56e754d-4ff6-4d48-8029-5cfa20a31db6)

Before starting the ambari server and storm back up, due to a bug in the storm script that is targeted for HDP 2.2.3 the following change needs to be made manually on all hosts:

```
cd /usr/hdp/current/storm-supervisor/bin/storm.distro
# Backup this file
cp /usr/hdp/current/storm-storm/bin/storm.distro /usr/hdp/current/storm-storm/bin/storm.distro.orig
# Then edit this file to add the word "exec" to the last line like this so it should like this:
exec $PYTHON ${STORM_BIN_DIR}/storm.py $@
ambari-server start
service supervisord restart
# Start Storm through Ambari
```

Test that this is working:

```
[root@c6401 storm]# supervisorctl status
storm-supervisor RUNNING pid 8691, uptime 0:01:34
```

## 6. Sizing and Resizing Storm Resources

### 6.1 Control the number of workers per topology (post deployment) using “storm rebalance”

Existing workers are not killed to allow new topology submission. If new topology asks for something that can not be allocated, new topology will fail and existing ones will continue to run. You cannot change these jvm options for already running topologies.

Example:

```
# Reconfigure the topology "mytopology" to use 5 worker processes,
# the spout "blue-spout" to use 3 executors and
# the bolt "yellow-bolt" to use 10 executors.
storm rebalance mytopology -n 5 -e blue-spout=3 -e yellow-bolt=10
```

### 6.2 Control the number of workers per machine

The most impactful performance setting in Storm standalone is the number of worker slots available per machine. This setting is controlled with the parameter: “`supervisor.slots.ports`”. Each worker is assigned a port to use for communication and you can control the number of workers by assigning a new port. This is somewhat analogous to controlling the number of containers which run in a YARN topology.

### 6.3 Size of workers (in memory):

We recommend setting `worker.childopts` via Ambari similar to the following:

```
-Xmx2048m -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:NewSize=128m -XX:CMSInitiatingOccupancyFraction=70 -XX:-CMSConcurrentMTEnabled -Djava.net.preferIPv4Stack=true
```

Since workers are each allocated their own JVM, you can run many of them to in order to better utilize the available memory.

You can also change the worker settings per topology during submission by setting worker child opts “`-c topology.worker.childopts`”. Workers are per topology so existing workers for existing topologies will continue to run as is.

### 6.4 Supervisor memory settings

Large Memory allocation for Supervisors are not useful as it does not do much of the heavy lifting of Storm execution. Its responsibilities are to download topology jar when its a assignment and launch worker and monitor worker process. You can allocate more JVM mem for workers where the topology code executes. But even for workers allocating huge JVM not a good idea as the GC can trigger stop-the-world collection which can timeout workers from sending heartbeats telling nimbus/supervisor that the worker is dead causing it to restart.

How to auto-scale a topology while running in Production?

There is no support natively in storm itself for auto-scaling. Work is ongoing in the community with this JIRA <https://issues.apache.org/jira/browse/STORM-594>.

## 7. Kafka Tuning Recommendations

- Kafka Brokers per Server
  - Recommend 1 Kafka broker per server- Kafka not only disk-intensive but can be network intensive so if you run multiple broker in a single host network I/O can be the bottleneck . Running single broker per host and having a cluster will give you better availability.
- Increase Disks allocated to Kafka Broker
  - Kafka parallelism is largely driven by the number of disks and partitions per topic.
  - From the [Kafka documentation](http://kafka.apache.org/documentation.html#diskandfs): “We recommend using multiple drives to get good throughput and not sharing the same drives used for Kafka data with application logs or other OS filesystem activity to ensure good latency. As of 0.8 you can format and mount each drive as its own directory. If you configure multiple data directories partitions will be assigned round-robin to data directories. Each partition will be entirely in one of the data directories. If data is not well balanced among partitions this can lead to load imbalance between disks.”
- Number of Threads
  - Make sure you set `num.io.threads` to at least no.of disks you are going to use by default its `8`. It be can higher than the number of disks.
  - Set `num.network.threads` higher based on number of concurrent producers, consumers, and replication factor.
- Number of partitions
  - Ideally you want to assign the default number of partitions (`num.partitions`) to at least `n-1` servers. This can break up the write workload and it allows for greater parallelism on the consumer side. Remember that Kafka does total ordering within a partition, not over multiple partitions, so make sure you partition intelligently on the producer side to parcel up units of work that might span multiple messages/events.
- Message Size
  - Kafka is designed for small messages. I recommend you to avoid using kafka for larger messages. If thats not avoidable there are several ways to go about sending larger messages like 1MB. Use compression if the original message is json, xml or text using compression is the best option to reduce the size. Large messages will affect your performance and throughput. Check your topic partitions and `replica.fetch.size` to make sure it doesn’t go over your physical ram.
- Large Messages
  - Another approach is to break the message into smaller chunks and use the same message key to send it same partition. This way you are sending small messages and these can be re-assembled at the consumer side.
  - Broker side:
    1. `message.max.bytes` defaults to `1000000` . This indicates the maximum size of message that a kafka broker will accept.
    2. `replica.fetch.max.bytes` defaults to `1MB` . This has to be bigger than `message.max.bytes` otherwise brokers will not be able to replicate messages.
- Consumer side:
  1. `fetch.message.max.bytes` defaults to `1MB`. This indicates maximum size of a message that a consumer can read. This should be equal or larger than `message.max.bytes`.
- Kafka Heap Size
  - By default kafka-broker jvm is set to 1Gb this can be increased using Ambari kafka-env template. When you are sending large messages JVM garbage collection can be an issue. Try to keep the Kafka Heap size below 4GB.
  - Example: In `kafka-env.sh` add following settings.
    - `export KAFKA_HEAP_OPTS="-Xmx16g -Xms16g"`
    - `export KAFKA_JVM_PERFORMANCE_OPTS="-XX:MetaspaceSize=96m -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:G1HeapRegionSize=16M -XX:MinMetaspaceFreeRatio=50 -XX:MaxMetaspaceFreeRatio=80"`
- Dedicated Zookeeper
  - Have a separate zookeeper cluster dedicated to Storm/Kafka operations. This will improve Storm/Kafka’s performance for writing offsets to Zookeeper, it will not be competing with HBase or other components for read/write access.
- ZK on separate nodes from Kafka Broker
  - Do Not Install zk nodes on the same node as kafka broker if you want optimal Kafka performance. Disk I/O both kafka and zk are disk I/O intensive.
- Disk Tuning sections
  - Please review the Kafka documentation on filesystem tuning parameters here.
  - Disable THP according to documentation here.
  - Either ext4 or xfs filesystems are recommended for performance benefit.
- Minimal replication
  - If you are doing replication, start with 2x rather than 3x for Kafka clusters larger than 3 machines. Alternatively, use 2x even if a 3 node cluster if you are able to reprocess upstream from your source.
- Avoid Cross Rack Kafka deployments
  - Avoid cross-rack Kafka deployments for now until Kafka 0.8.2 - see: <https://issues.apache.org/jira/browse/KAFKA-1215>

## 7. Kafka Monitoring via JMX

Kafka exposes JMX interface for monitoring operation of Kafka broker process. In order to connect via JMX:

  1. Modify `kafka-env.sh` to add line: `export JMX_PORT=9999`
  2. Restart Kafka broker
  3. Connect to using JMX client of choice. Example service URL: `service:jmx:rmi:///jndi/rmi://[your_Kafka_host]:9999/jmxrmi`

<img src="images/Screen Shot 2015-07-22 at 9_36_15 AM.png" />

<img src="images/Screen Shot 2015-07-22 at 9_39_20 AM.png" />

## 8. Storm and Kafka Performance Testing

### 8.1 Recommendations on End to End testing

End to End testing of the Storm/Kafka ecosystem will occasionally require testing Producing messages to Kafka, processing messages in Storm, and then writing downstream to a data store such as HBase or Hive. In this scenario you will first want to test each discrete component’s performance before testing the full end to end performance times. For example you will want to test each of the following individually:

  - Kafka performance when producing (writing) directly to Kafka
  - Storm performance with a dummy spout which creates fake messages and writes downstream to your data store
  - Storm performance with a live Kafka spout which reads messages, connected to a bolt which only acks the messages from Kafka.

## 9. Storm Monitoring and Alerting

### 9.1 Ambari 2.0

Includes pre-built alerts for Storm services. These can be configured to send email or SNMP notifications when triggered.

<img src="images/Screen Shot 2015-04-07 at 4_39_04 PM.png" />

### 9.2 Custom monitoring for status of individual topologies

From HDP 2.2 we have rest api on Storm UI. One way I would recommend is to make a call to /api/v1/topology/summary get topology and its status (ACTIVE is good). More info on that api <https://github.com/apache/storm/blob/master/STORM-UI-REST-API.md>. If you are configuring nagios to monitor don’t make these calls too frequent as each call will be going to nimbus.

To check if they are not processing one option is to write a script which calls Nimbus via REST API, stores current total tuples (or at the component level), then in the next call checks if total tuples has increased from the previous state.

Example Script - please find the example below of how to automate checking for topologies which stop processing tuples:

<https://gist.github.com/wesfloyd/e662f1d166440f58ba5c>

How to measure and scale when the number of workers (parallelism) for when a given spout or bolt is undersized? Should we be able to infer this from Storm UI stats?

You can monitor the topology from Storm UI and check its capacity at the topology or component (bolt or spout) level. This is percentage if its equal or above 1.00 than that component is the bottleneck and depending what type of spout or bolt you might want to increase the parallelism. Incase of `kafkaSpout` increasing parallelism means you need to increase the topic partition.

### 9.3 Benchmarking

Running pre-established benchmarks can be a very helpful way to test scaling your cluster without having to develop a Storm topology from scratch.

Storm Benchmark tools authored by Taylor Goetz - <https://github.com/ptgoetz/storm-benchmark>

Tip: Modify the `pom.xml` file “`storm.version`” attribute to match your preferred version of Storm. HDP 2.2 currently ships with Storm version 0.9.3

Storm Benchmark authored by Manu Zhang - <https://github.com/manuzhang/storm-benchmark>

## 10. Failure Scenarios

### 10.1 Storm

<table>
  <tr>
    <th>Scenario</th>
    <th>Impact</th>
    <th>Monitoring Needs</th>
    <th>Recovery Actions</th>
  </tr>
  <tr>
    <td>Nimbus service goes down</td>
    <td>Running topologies continue to run.<br />
        No new topologies can be submitted.<br />
        No lifecycle management activities(get stats, balance, kill etc) can be performed on running topologies
    </td>
    <td>Monitor nimbus process using either mechanisms below<br />
      <ol>
        <li>nc -vz &lt;nimbus host&gt; &lt;nimbus port&gt;
        <li>Ganglia indicating no metrics captured from nimbus in last few minutes</li>
      </ol>
    </td>
    <td>‘supervisord’ is set to auto restart nimbus when it goes down</td>
  </tr>
  <tr>
    <td>Nimbus Host goes down</td>
    <td>
      Running topologies continue to run.<br />
      No new topologies can be submitted.<br />
      No lifecycle management activities (get stats, balance, kill etc) can be performed on running topologies
    </td>
    <td>same as above</td>
    <td>Need to bring back nimbus Host. In case of hardware failure it can be started on another host as long as new hosts is assigned same hostname /ip address as before (physically or virtually)</td>
  </tr>
  <tr>
    <td>Storm supervisor failure</td>
    <td>Host with supervisor running is not available for processing</td>
    <td>same as above but use supervisor host and port</td>
    <td>Storm is self healing. It will reschedule the work to other available supervisors and exiting topologies will continue to work</td>
  </tr>
  <tr>
    <td>Worker process failure</td>
    <td>JVM of worker process and all associated memory is lost</td>
    <td>Storm is self healing.</td>
    <td>Supervisor will relaunch the worker process and topology will continue to run. State maintained by topology components (if any) would need to be recreated</td>
  </tr>
  <tr>
    <td>Log dirs full</td>
    <td>Workers will fail</td>
    <td>
    Per host per disk monitoring for utilization.<br />
    Alert if disk usage goes beyond 80%<br />
    --- same as above ---
    </td>
    <td>Additionally disk monitors should be set to alert on disk usage going beyond 70%</td>
  </tr>
  <tr>
    <td>UI service goes down</td>
    <td>Will not be able to see UI of running topologies in cluster.<br />
    However running topologies continue to run.<br />
    New topologies can be submitted and managed.<br />
</td>
    <td>Monitor UI process using either mechanisms below <br />
    <ol>
      <li>nc -vz &lt;storm ui host&gt; &lt;storm ui port&gt;</li>
      <li>Ganglia indicating no metrics captured from UI service in last few minutes</li>
    </ol>
    </td>
    <td>‘supervisord’ is set to auto restart Storm UI when it goes down</td>
  </tr>
  <tr>
    <td>REST service goes down</td>
    <td>Will not be able to get metrics of running topologies in cluster<br />
    However running topologies continue to run.<br />
    New topologies can be submitted and managed.</td>
    <td>same as above</td>
    <td>‘supervisord’ should be set to auto restart REST server when it goes down</td>
  </tr>
  <tr>
    <td>Logviewer UI goes down</td>
    <td>Will not be able to get see logs in UI<br />
    However running topologies continue to run.<br />
    New topologies can be submitted and managed.</td>
    <td>same as above <br/>
    Logviewer may be disabled in production
    </td>
    <td>‘supervisord’ should is set to auto restart logviewer service when it goes down</td>
  </tr>
  <tr>
    <td>Topology fails with Kafka Offset Out of Sync err<br />
    Topology stalls<br />
    Topology acked events in last few minutes.</td>
    <td>If it is zero, topology is stalled and needs to be investigated.
    Proactively compare the event ingest rate in kafka with event processing rate in Storm per topology/topic.</td>
    <td>Alert if rate if ingest is higher than the rate of processing.</td>
    <td>Topology is running slower than ingest rate. Need to tune topology.<br />
    Temporarily ZK node storing kafka spout offset (/usually storm/topology/&lt;topology name&gt;) needs to be cleaned and topology to be restarted.<br />
    Consider increasing kafka retention period.
    </td>
  </tr>
</table>

### 10.2 Kafka

<table>
  <tr>
    <th>Scenario</th>
    <th>Impact</th>
    <th>Monitoring Needs</th>
    <th>Recovery Actions</th>
  </tr>
  <tr>
    <td>Disk failure</td>
    <td>Broker dies. All topics get replicated to another available brokers.<br />
      Topics which have only one replication factor and have any partition residing on this disk, would suffer data loss.<br />Broker going down would result into lowered throughput of the system
    </td>
    <td>Per host per disk utilization monitoring<br />
      These metrics are already captured in ganglia.
    </td>
    <td>Kafka recovers data by replication. So no immediate action needed. Recovery needs disk replacement and restart of Borker. Alternatively that specific brokers’ configuration can be modified to exclude bad disk and Broker can be restarted</td>
  </tr>
  <tr>
    <td>Disk full</td>
    <td>-- same as above --</td>
    <td>-- same as above --<br />Alert if disk utilization goes beyond 80%</td>
    <td>-- same as above --</td>
  </tr>
  <tr>
    <td>Broker dead</td>
    <td>-- same as above --</td>
    <td>Monitor broker process using either mechanisms below<br />
      <ol>
        <li>nc -vz &lt;host&gt; &lt;broker port&gt;</li>
        <li>Ganglia indicating no metrics captured from broker in last few minutes</li>
      </ol>
    </td>
    <td>Monitors like ‘monit‘or ‘supervisord’ can be set to auto restart broker on failure</td>
  </tr>
  <tr>
    <td>Host down</td>
    <td>-- same as above ---</td>
    <td>-- same as above ---Additionally ganglia host level metrics</td>
    <td>Need host reboot and broker restart.<br />
        Host and process monitors should alert unreachable hosts and unresponsive processes.
    </td>
  </tr>
</table>

### 10.3 Zookeeper

<table>
  <tr>
    <th>Scenario</th>
    <th>Impact</th>
    <th>Monitoring Needs</th>
    <th>Recovery Actions</th>
  </tr>
  <tr>
    <td>ZKeeper slowness</td>
    <td>Kafka and/or Storm services may see timeouts. They will retry.</td>
    <td>ZK 4 letter command such as `stat`</td>
    <td>Need to identify the slowness reason (disk, n/w, workload etc) and then rectify ZKeeper quorum availability</td>
  </tr>
  <tr>
    <td>ZK Service is unavailable</td>
    <td></td>
    <td>ZK 4 letter command such as `ruok`</td>
    <td>Need to start ZK services on zk hosts to meet quorum requirements</td>
  </tr>
  <tr>
    <td>Disk full</td>
    <td>same as above</td>
    <td>Disk level stats<br />Alert if disk utilization goes beyond 80%</td>
    <td>ZK is HA enabled and failure of a ZK node does not impact availability of ZK until all available nodes meet the quorum. Setup log retention period for all ZK nodes to auto rotate and clean old logs.</td>
  </tr>
</table>

## 11. Maintenance

## 11.1 Storm Upgrade Paths and Maintenance Outages

Neither Kafka nor Storm currently allows rolling upgrades without service interruption. However, the instructions below can be followed to achieve minimal downtime during upgrade and maintenance windows.

Default upgrade approach for major version changes:

  1. Shutdown (Kill) all storm topologies
  2. Shut down storm cluster
  3. Upgrade the storm software via Ambari or manually, or apply any patches needed
  4. Start storm cluster
  5. Re-submit storm topology

Storm Upgrade for Minor Patches/Fixes:

  1. Apply new software on all Storm nodes
  2. Restart supervisors, workers, nimbus etc one by one in rolling fashion.

Storm Upgrade with less downtime using Storm on YARN

  1. Stand Up a second storm cluster using Storm on YARN
  2. Start topologies on new cluster (in inactive mode)
  3. Deactivate all old topologies and then kill them
  4. Activate topologies on new cluster
  5. Shutdown old storm topology
  6. Shutdown old Storm cluster

## 11.2 Kafka Upgrade Paths and Maintenance Outages

Default upgrade approach:

  1. Shutdown external kafka producers. (shut down access to kafka cluster or block proxy producers if in place)
  2. Let Storm topology process all backlog data in kafka topics
  3. Shutdown storm topology
  4. Shut down kafka cluster
  5. Upgrade the kafka software or apply any patches needed
  6. Restart Kafka Brokers
  7. Restart storm topology
  8. Restart kafka producers

Kafka Upgrade for Minor Patches/Fixes:

  1. Apply new software on all kafka nodes
  2. Restart brokers one by one in rolling fashion.

## 12. References

### 12.1 Storm Documentation ([docs.hortonworks.com](http://docs.hortonworks.com/))

Includes additional best practices for initial development, performance debugging, and parallelism:

<http://docs.hortonworks.com/HDPDocuments/HDP2/HDP-2.2.0/Storm_UG_v22/index.html#Item1.4>

### 12.2 Web links

  - Scaling Apache Storm - <http://www.slideshare.net/ptgoetz/scaling-apache-storm-strata-hadoopworld-2014#45>
  - Understanding the Parallelism of a Storm Topology - <http://www.michael-noll.com/blog/2012/10/16/understanding-the-parallelism-of-a-storm-topology/#how-to-change-the-parallelism-of-a-running-topology>
