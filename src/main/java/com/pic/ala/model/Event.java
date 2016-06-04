package com.pic.ala.model;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Event {

	protected final ObjectMapper mapper;
	protected final String sysID; // 系統 ID
	protected final String logType;
	protected final Logger logger;
	protected static boolean enableSleep = true;
	private int count = 0;
	private Lock lock = new ReentrantLock();

	public Event(final String sysID, final String logType, final Class<? extends Event> clazz) {
		this.sysID = sysID;
		this.logType = logType;
		this.logger = Logger.getLogger(clazz);
		this.mapper = new ObjectMapper();
	}

	public void increment() {
		lock.lock();
		try {
			this.count++;
		} finally {
			lock.unlock();
		}
	}

	public int getCount() {
		return this.count;
	}

	public String getSysID() {
		return this.sysID;
	}

	public void go() {
		while (true) {
			try {
				ApLog log = new ApLog(sysID, logType);
				// logger.info(log.toString());
				if (enableSleep) {
					Thread.sleep(ThreadLocalRandom.current().nextInt(1, 20) * 1000);
				}
				// logger.info(log.toString());
				logger.info(mapper.writeValueAsString(log));
				this.increment();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (JsonMappingException jme) {
				jme.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (Exception e) {
				System.out.println(this.getCount());
			}
		}
	}
}
