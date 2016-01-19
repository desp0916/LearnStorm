/**
 * http://www.mkyong.com/java/jackson-2-convert-java-object-to-from-json/
 */
package com.pic.ala.gen;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class BatchJob extends Event {

	public BatchJob(final String systemID) {
		this.systemID = systemID;
		this.logger = Logger.getLogger(BatchJob.class);
		this.mapper = new ObjectMapper();
	}

	public void run() {
		while (true) {
			try {
				synchronized (this) {
					ApLog log = new ApLog(systemID, "batch");
//					logger.info(log.toString());
					wait(ThreadLocalRandom.current().nextInt(1, 20) * 1000);
//					logger.info(log.toString());
					logger.info(mapper.writeValueAsString(log));
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

	}

}