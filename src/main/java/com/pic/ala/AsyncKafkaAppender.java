package com.pic.ala;

import org.apache.log4j.AsyncAppender;

import kafka.producer.KafkaLog4jAppender;

public class AsyncKafkaAppender extends AsyncAppender {
	private java.lang.String topic;
	private java.lang.String serializerClass;
	private java.lang.String zkConnect;
	private java.lang.String brokerList;
	private java.lang.String hostname;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getSerializerClass() {
		return serializerClass;
	}

	public void setSerializerClass(String serializerClass) {
		this.serializerClass = serializerClass;
	}

	public String getZkConnect() {
		return zkConnect;
	}

	public void setZkConnect(String zkConnect) {
		this.zkConnect = zkConnect;
	}

	public String getBrokerList() {
		return brokerList;
	}

	public void setBrokerList(String brokerList) {
		this.brokerList = brokerList;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public void activateOptions() {
		super.activateOptions();
		synchronized (this) {
			KafkaLog4jAppender kafka = new KafkaLog4jAppender();
			kafka.setLayout(getLayout());
//			kafka.setHostname(getHostname());
			kafka.setBrokerList(getBrokerList());
//			kafka.setSerializerClass(getSerializerClass());
//			kafka.setZkConnect(getZkConnect());
			kafka.setTopic(getTopic());
			kafka.activateOptions();
			addAppender(kafka);
		}
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}
}
