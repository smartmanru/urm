package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.monitor.MonitorCommand;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.MonitorCommandMeta;
import org.urm.engine.ServerEngine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.action.CommandExecutor;

public class MonitorCommandExecutor extends CommandExecutor {

	MonitorCommand impl;

	public static MonitorCommandExecutor createExecutor( ServerEngine engine ) throws Exception {
		MonitorCommandMeta commandInfo = new MonitorCommandMeta( engine.optionsMeta );
		return( new MonitorCommandExecutor( engine , commandInfo ) );
	}
		
	private MonitorCommandExecutor( ServerEngine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		impl = new MonitorCommand();
	}
	
	@Override
	public boolean runExecutorImpl( ActionBase action , CommandMethod method ) {
		boolean res = super.runMethod( action , method );
		return( res );
	}

}
