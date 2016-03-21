package ru.egov.urm.action.monitor;

import ru.egov.urm.action.ActionInit;
import ru.egov.urm.action.CommandAction;
import ru.egov.urm.action.CommandBuilder;
import ru.egov.urm.action.CommandExecutor;
import ru.egov.urm.meta.MetaMonitoring;

public class MonitorCommandExecutor extends CommandExecutor {

	MonitorCommand impl;
	
	public MonitorCommandExecutor( CommandBuilder builder ) {
		super( builder );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new RunMonitor() , "start" , "start monitor server" , cmdOpts , "./start.sh [OPTIONS]" ) );
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
