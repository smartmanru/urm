package org.urm.common.jmx;

import org.urm.common.action.ActionData;
import org.urm.engine.EngineCall;
import org.urm.engine.Engine;
import org.urm.engine.EngineSession;

public class RemoteServerCall extends EngineCall {

	public EngineCommandMBean command;
	public String clientId;
	
	public RemoteServerCall( Engine engine , EngineSession sessionContext , String clientId , EngineCommandMBean command , String actionName , ActionData data ) {
		super( engine , sessionContext , command.meta , actionName , data );
		this.command = command;
		this.clientId = clientId;
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
