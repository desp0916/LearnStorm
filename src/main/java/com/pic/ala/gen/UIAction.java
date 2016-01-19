/**
 * http://www.mkyong.com/java/jackson-2-convert-java-object-to-from-json/
 */
package com.pic.ala.gen;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class UIAction extends Event {

	public UIAction(final String systemID) {
		this.systemID = systemID;
		this.logger = Logger.getLogger(UIAction.class);
		this.mapper = new ObjectMapper();
	}

	public void take() {
		while (true) {
			try {
				synchronized (this) {
					wait(ThreadLocalRandom.current().nextInt(1, 20) * 1000);
					ApLog log = new ApLog(systemID, "ui");
//					logger.info(log.toString());
					logger.info(mapper.writeValueAsString(log));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}