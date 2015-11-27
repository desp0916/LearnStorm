package com.pic.ala;

import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.HBaseMapper;
import org.apache.storm.hbase.common.ColumnList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

public class CustomHBaseBolt extends HBaseBolt {

    private static final Logger LOG = LoggerFactory.getLogger(CustomHBaseBolt.class);

	boolean writeToWAL = true;

	private static final long serialVersionUID = 1L;

	public CustomHBaseBolt(String tableName, HBaseMapper mapper) {
		super(tableName, mapper);
	}

	public CustomHBaseBolt(String tableName, HBaseMapper mapper, ApLogScheme apLogScheme) {
		super(tableName, mapper);
	}

    @Override
	public void execute(Tuple tuple) {

        byte[] rowKey = this.mapper.rowKey(tuple);
        ColumnList cols = new ColumnList();
        String exec_time = tuple.getValueByField(ApLogScheme.FIELD_EXEC_TIME).toString();
        String agg_id = tuple.getValueByField(ApLogScheme.FIELD_EXEC_TIME).toString();


//        tuple.getValueByField(ApLogScheme.HourMinute);

//        ColumnList cols = new ColumnList();

//		if (this.columnFields != null) {
//			// TODO timestamps
//			for (String field : this.columnFields) {
//				if (field == apLogScheme.) {
//
//				}
//				cols.addColumn(this.columnFamily, field.getBytes(), toBytes(tuple.getValueByField(field)));
//			}
//		}
//        if(this.counterFields != null){
//            for(String field : this.counterFields){
//                cols.addCounter(this.columnFamily, field.getBytes(), toLong(tuple.getValueByField(field)));
//            }
//        }
//
//        byte[] rowKey = this.mapper.rowKey(tuple);
//        // 這裡會建立欄位，並插入欄位值
//        ColumnList cols = this.mapper.columns(tuple);
//        List<Mutation> mutations = hBaseClient.constructMutationReq(rowKey, cols, writeToWAL? Durability.SYNC_WAL : Durability.SKIP_WAL);
//
//        try {
//            this.hBaseClient.batchMutate(mutations);
//        } catch(Exception e){
//            this.collector.reportError(e);
//            this.collector.fail(tuple);
//            return;
//        }

        this.collector.ack(tuple);
    }

    @Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
