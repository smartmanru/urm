package org.urm.common.jmx;

import javax.management.DynamicMBean;
import javax.management.Notification;

public class ActionNotification extends Notification {

	public static String EVENT = "action.log";
	
	/**
	 * serial
	 */
	private static final long serialVersionUID = -842716279929681284L;

	public enum EVENT_TYPE {
		LOG ,
		STOP ,
		CONNECTED ,
		COMMANDFINISHED
	};
	
	public int sessionId;
	public String clientId;
	public EVENT_TYPE eventType;
	
	public ActionNotification( DynamicMBean mbean , int sequence , int sessionId , String clientId , String log ) {
		super( EVENT , mbean , sequence , log );
		this.sessionId = sessionId;
		this.clientId = clientId;
	}
	
	public void setLogEvent() {
		eventType = EVENT_TYPE.LOG;
	}

	public boolean isLog() {
		return( eventType == EVENT_TYPE.LOG );
	}
	
	public void setStopEvent() {
		eventType = EVENT_TYPE.STOP;
	}
	
	public boolean isStop() {
		return( eventType == EVENT_TYPE.STOP );
	}
	
	public void setConnectedEvent() {
		eventType = EVENT_TYPE.CONNECTED;
	}

	public boolean isConnected() {
		return( eventType == EVENT_TYPE.CONNECTED );
	}
	
	public void setCommandFinishedEvent() {
		eventType = EVENT_TYPE.COMMANDFINISHED;
	}

	public boolean isCommandFinished() {
		return( eventType == EVENT_TYPE.COMMANDFINISHED );
	}
	
	
}
