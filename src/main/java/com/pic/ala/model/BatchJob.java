/**
 * http://www.mkyong.com/java/jackson-2-convert-java-object-to-from-json/
 */
package com.pic.ala.model;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class BatchJob extends Event {

	public BatchJob(final String sysID, final boolean enableSleep) {
		this.sysID = sysID;
		this.logger = Logger.getLogger(BatchJob.class);
		this.mapper = new ObjectMapper();
		this.enableSleep = enableSleep;
	}

	public void run()  {
		while (true) {
			try {
				ApLog log = new ApLog(sysID, "batch");
//				logger.info(log.toString());
				if (enableSleep) {
					Thread.sleep(ThreadLocalRandom.current().nextInt(1, 20) * 1000);
				}
//				logger.info(log.toString());
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

}