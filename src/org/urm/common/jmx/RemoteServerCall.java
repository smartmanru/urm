package org.urm.common.jmx;

import org.urm.common.action.ActionData;
import org.urm.server.ServerCall;
import org.urm.server.ServerEngine;
import org.urm.server.SessionContext;

public class RemoteServerCall extends ServerCall {

	public ServerCommandMBean command;
	public String clientId;
	
	public RemoteServerCall( ServerEngine engine , SessionContext sessionContext , String clientId , ServerCommandMBean command , String actionName , ActionData data ) {
		super( engine , sessionContext , command.meta , actionName , data );
		this.command = command;
	}

	@Override
	protected void notifyLog( String msg ) {
		command.notifyLog( this , msg );
	}
	
	@Override
	protected void notifyLog( Throwable e ) {
		command.notifyLog( this , e );
	}
	
	@Override
	protected void notifyStop( String msg ) {
		try {
			int notificationSequence = command.getNextSequence();
			ActionNotification n = new ActionNotification( command , notificationSequence , sessionContext.sessionId , clientId , msg ); 
			n.setStopEvent();
			command.sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	@Override
	protected void notifyConnected( String msg ) {
		try {
			int notificationSequence = command.getNextSequence();
			ActionNotification n = new ActionNotification( command , notificationSequence , sessionContext.sessionId , clientId , msg ); 
			n.setConnectedEvent();
			command.sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	@Override
	protected void notifyCommandFinished( String msg ) {
		try {
			int notificationSequence = command.getNextSequence();
			ActionNotification n = new ActionNotification( command , notificationSequence , sessionContext.sessionId , clientId , msg ); 
			n.setCommandFinishedEvent();
			command.sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
}
