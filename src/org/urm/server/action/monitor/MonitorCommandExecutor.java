package org.urm.server.action.monitor;

import org.urm.common.action.CommandMeta;
import org.urm.server.CommandExecutor;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandAction;
import org.urm.server.meta.MetaMonitoring;

public class MonitorCommandExecutor extends CommandExecutor {

	MonitorCommand impl;
	
	public MonitorCommandExecutor( CommandMeta commandInfo ) throws Exception {
		super( commandInfo );
		super.defineAction( new RunMonitor() , "start" );
	}
	
	public boolean run( ActionInit action ) {
		try {
			action.meta.loadDistr( action );
			
			// create implementation
			MetaMonitoring mon = action.meta.loadMonitoring( action );
			impl = new MonitorCommand( mon );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}

	private class RunMonitor extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		impl.runMonitor( action );
	}
	}
	
}
