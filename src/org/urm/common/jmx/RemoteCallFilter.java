package org.urm.common.jmx;

import javax.management.Notification;
import javax.management.NotificationFilter;

public class RemoteCallFilter implements NotificationFilter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3905351363488461487L;
	
	String clientId;
	
	public RemoteCallFilter( String clientId ) {
		this.clientId = clientId;
	}
	
	public boolean isNotificationEnabled( Notification n ) {
		ActionNotification an = ( ActionNotification )n;
		if( clientId.equals( an.clientId ) )
			return( true );
		
		return( false );
	}

}
