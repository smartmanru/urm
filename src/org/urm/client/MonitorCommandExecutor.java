package org.urm.client;

import org.urm.common.action.CommandAction;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandExecutor;
import org.urm.server.action.ActionInit;
import org.urm.server.action.monitor.MonitorCommand;
import org.urm.server.meta.MetaMonitoring;

public class MonitorCommandExecutor extends CommandExecutor {

	public static String NAME = "monitor";
	MonitorCommand impl;
	
	public MonitorCommandExecutor( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new RunMonitor() , "start" , true , "start monitor server" , cmdOpts , "./start.sh [OPTIONS]" ) );
	}
	
	public boolean run( ActionInit action ) {
		try {
			meta.loadDistr( action );
			
			// create implementation
			MetaMonitoring mon = meta.loadMonitoring( action );
			impl = new MonitorCommand( mon );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private class RunMonitor extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		impl.runMonitor( action );
	}
	}
	
}
