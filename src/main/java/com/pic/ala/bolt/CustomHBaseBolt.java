/**
 * https://learnhbase.wordpress.com/2013/03/02/hbase-shell-commands/
 * scan 'aes3g_agg', {COLUMNS => ['min:15-29:toLong'] }
 */
package com.pic.ala.bolt;

import static org.apache.storm.hbase.common.Utils.toBytes;

import java.util.List;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.HBaseMapper;
import org.apache.storm.hbase.common.ColumnList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pic.ala.scheme.ApLogScheme;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class CustomHBaseBolt extends HBaseBolt {

	private static final String ONE  = "1";
    private static final Logger LOG = LoggerFactory.getLogger(CustomHBaseBolt.class);

	boolean writeToWAL = true;

	private static final long serialVersionUID = 1L;

	public CustomHBaseBolt(String tableName, HBaseMapper mapper) {
		super(tableName, mapper);
	}

    @Override
	public void execute(Tuple tuple) {

        ColumnList cols = new ColumnList();

        String agg_id = tuple.getValueByField(ApLogScheme.FIELD_AGG_ID).toString();

        byte[] rowKey = toBytes(tuple.getValueByField(ApLogScheme.FIELD_AGG_ID));
        byte[] columnFamily = new String("min").getBytes();

        // 從 tuple 取值做為欄位名稱
        Object hourMinute = tuple.getValueByField(ApLogScheme.FIELD_HOUR_MINUTE);
        Fields counterFields = new Fields(hourMinute.toString());

		LOG.error("GARYYYY: agg_id = " + agg_id + ", hourMinute: " + hourMinute);

		if (counterFields != null) {
//			LOG.error("GARYYYZ: " +  hourMinute);
			for (String field : counterFields) {
//				LOG.error("GARYYYB field.getBytes(): " +  field.getBytes());
                cols.addCounter(columnFamily, field.getBytes(), 1L);
			}
		}

        List<Mutation> mutations = hBaseClient.constructMutationReq(rowKey, cols, writeToWAL ? Durability.SYNC_WAL : Durability.SKIP_WAL);

        try {
            this.hBaseClient.batchMutate(mutations);
        } catch(Exception e){
            this.collector.reportError(e);
            this.collector.fail(tuple);
            return;
        }

        this.collector.ack(tuple);
    }

    @Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
