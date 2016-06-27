package org.urm.common.jmx;

import javax.management.DynamicMBean;
import javax.management.Notification;

public class ActionNotification extends Notification {

	public static String EVENT = "action.log";
	
	/**
	 * serial
	 */
	private static final long serialVersionUID = -842716279929681284L;

	public int sessionId;
	public String clientId;
	public boolean logEvent = false;
	public boolean stopEvent = false;
	
	public ActionNotification( DynamicMBean mbean , int sequence , int sessionId , String clientId , String log ) {
		super( EVENT , mbean , sequence , log );
		this.sessionId = sessionId;
		this.clientId = clientId;
	}
	
	public void setLogEvent() {
		logEvent = true;
	}
	
	public void setStopEvent() {
		stopEvent = true;
	}
	
}
