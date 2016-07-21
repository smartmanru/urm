package org.urm.server.executor;

import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandAction;
import org.urm.server.action.CommandExecutor;
import org.urm.server.action.monitor.MonitorCommand;
import org.urm.server.meta.MetaMonitoring;

public class MonitorCommandExecutor extends CommandExecutor {

	MonitorCommand impl;
	
	public MonitorCommandExecutor( ServerEngine engine , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		super( engine , commandInfo , options );
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
