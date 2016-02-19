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

	public UIAction(final String sysID, final boolean enableSleep) {
		this.sysID = sysID;
		this.logger = Logger.getLogger(UIAction.class);
		this.mapper = new ObjectMapper();
		this.enableSleep = enableSleep;
	}

	public void take() {
		while (true) {
			try {
				if (enableSleep) {
					Thread.sleep(ThreadLocalRandom.current().nextInt(1, 20) * 1000);
				}
				ApLog log = new ApLog(sysID, "ui");
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