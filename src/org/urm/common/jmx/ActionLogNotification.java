package org.urm.common.jmx;

import javax.management.DynamicMBean;
import javax.management.Notification;

public class ActionLogNotification extends Notification {

	public static String EVENT = "action.log";
	
	/**
	 * serial
	 */
	private static final long serialVersionUID = -842716279929681284L;

	public int sessionId; 
	
	public ActionLogNotification( DynamicMBean mbean , int sequence , int sessionId , String log ) {
		super( EVENT , mbean , sequence , log );
		this.sessionId = sessionId;
	}
	
}
