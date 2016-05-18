package com.pic.ala.util;

import static org.apache.storm.hbase.common.Utils.toBytes;
import static org.apache.storm.hbase.common.Utils.toLong;

import org.apache.storm.hbase.bolt.mapper.HBaseMapper;
import org.apache.storm.hbase.common.ColumnList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class CustomeHBaseMapper implements HBaseMapper {

    private static final Logger LOG = LoggerFactory.getLogger(CustomeHBaseMapper.class);

    private String rowKeyField;
//  private String timestampField;
    private byte[] columnFamily;
    private Fields columnFields;
    private Fields counterFields;

    public CustomeHBaseMapper(){
    }

    public CustomeHBaseMapper withRowKeyField(String rowKeyField){
        this.rowKeyField = rowKeyField;
        return this;
    }

    public CustomeHBaseMapper withColumnFields(Fields columnFields){
        this.columnFields = columnFields;
        return this;
    }

    public CustomeHBaseMapper withCounterFields(Fields counterFields){
        this.counterFields = counterFields;
        return this;
    }

    public CustomeHBaseMapper withColumnFamily(String columnFamily){
        this.columnFamily = columnFamily.getBytes();
        return this;
    }

//    public SimpleTridentHBaseMapper withTimestampField(String timestampField){
//        this.timestampField = timestampField;
//        return this;
//    }

    public byte[] rowKey(Tuple tuple) {
        Object objVal = tuple.getValueByField(this.rowKeyField);
        return toBytes(objVal);
    }

    public ColumnList columns(Tuple tuple) {
        ColumnList cols = new ColumnList();
        if(this.columnFields != null){
            // TODO timestamps
            for(String field : this.columnFields){
                cols.addColumn(this.columnFamily, field.getBytes(), toBytes(tuple.getValueByField(field)));
            }
        }
        if(this.counterFields != null){
            for(String field : this.counterFields){
                cols.addCounter(this.columnFamily, field.getBytes(), toLong(tuple.getValueByField(field)));
            }
        }
        return cols;
    }
}
