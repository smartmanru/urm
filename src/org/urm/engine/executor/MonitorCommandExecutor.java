package org.urm.engine.executor;

import org.urm.action.monitor.MonitorCommand;
import org.urm.common.action.CommandMeta;
import org.urm.engine.ServerEngine;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandAction;
import org.urm.engine.action.CommandExecutor;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaMonitoring;

public class MonitorCommandExecutor extends CommandExecutor {

	MonitorCommand impl;

	public MonitorCommandExecutor( ServerEngine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		super.defineAction( new RunMonitor() , "start" );
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			Meta meta = action.getContextMeta();
			MetaMonitoring mon = meta.getMonitoring( action );
			impl = new MonitorCommand( mon );
		}
		catch( Throwable e ) {
			action.handle( e );
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
