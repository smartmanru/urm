package org.urm.common.jmx;

import javax.management.DynamicMBean;
import javax.management.Notification;

public class ActionStopNotification extends Notification {

	public static String EVENT = "action.stop";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 320615245874080340L;

	public int sessionId;
	
	public ActionStopNotification( DynamicMBean mbean , int sequence , int sessionId ) {
		super( EVENT , mbean , sequence , "stopped" );
		this.sessionId = sessionId;
	}
	
}
