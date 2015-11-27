package ru.egov.urm.run.monitor;

import ru.egov.urm.meta.MetaMonitoring;
import ru.egov.urm.run.ActionInit;
import ru.egov.urm.run.CommandAction;
import ru.egov.urm.run.CommandBuilder;
import ru.egov.urm.run.CommandExecutor;

public class MonitorCommandExecutor extends CommandExecutor {

	MonitorCommandImpl impl;
	
	public MonitorCommandExecutor( CommandBuilder builder ) {
		super( builder );
		
		String cmdOpts = "";
		super.defineAction( CommandAction.newAction( new RunMonitor() , "start" , "start monitor server" , cmdOpts , "./start.sh [OPTIONS]" ) );
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			meta.loadProduct( action );
			MetaMonitoring mon = meta.loadMonitoring( action );
			impl = new MonitorCommandImpl( this , mon );
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
