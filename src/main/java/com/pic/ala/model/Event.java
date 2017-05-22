package com.pic.ala.model;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AP Log 事件
 *
 * @author gary
 * @since  2017年5月22日 下午3:00:28
 */
public class Event {

	protected final ObjectMapper mapper;
	protected final String sysID; // 系統 ID
	protected final String outputFields;
	protected final String logType;
	protected final Logger logger;

	protected static boolean enableSleep = true;

	public Event(final String sysID, final String outputFields,
			final String logType, final Class<? extends Event> clazz) {
		this.sysID = sysID;
		this.outputFields = outputFields;
		this.logType = logType;
		this.logger = LoggerFactory.getLogger(clazz);
		this.mapper = new ObjectMapper();
	}

	public String getSysID() {
		return this.sysID;
	}

	public void go() {
		while (true) {
			try {
				ApLog log = new ApLog(sysID, outputFields, logType);
				// logger.info(log.toString());
				if (enableSleep) {
					Thread.sleep(ThreadLocalRandom.current().nextInt(1, 20) * 1000);
				}
				// 不顯示 null 內容的 fields
				mapper.setSerializationInclusion(Include.NON_NULL);

				// logger.info(log.toString());
				logger.info(mapper.writeValueAsString(log));
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (JsonMappingException jme) {
				jme.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	// 以下是內部靜態類別：

	public static class APIEvent extends Event {
		public APIEvent(final String sysID, final String outputFields) {
			super(sysID, outputFields, ApLogType.API.getValue(), APIEvent.class);
		}
	}

	public static class BatchJob extends Event {
		public BatchJob(final String sysID, final String outputFields) {
			super(sysID, outputFields, ApLogType.BATCH.getValue(), BatchJob.class);
		}
	}

	public static class TPIPASEvent extends Event {
		public TPIPASEvent(final String sysID, final String outputFields) {
			super(sysID, outputFields, ApLogType.TPIPAS.getValue(), TPIPASEvent.class);
		}
	}

	public static class UIAction extends Event {
		public UIAction(final String sysID, final String outputFields) {
			super(sysID, outputFields, ApLogType.UI.getValue(), UIAction.class);
		}
	}
}
