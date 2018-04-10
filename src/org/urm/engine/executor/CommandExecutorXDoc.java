package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.xdoc.XDocCommand;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.XDocCommandMeta;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.action.CommandExecutor;
import org.urm.meta.product.Meta;

public class CommandExecutorXDoc extends CommandExecutor {

	XDocCommand impl;
	
	public static CommandExecutorXDoc createExecutor( Engine engine ) throws Exception {
		XDocCommandMeta commandInfo = new XDocCommandMeta( engine.optionsMeta );
		return( new CommandExecutorXDoc( engine , commandInfo ) );
	}
		
	private CommandExecutorXDoc( Engine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		defineAction( new DesignDoc() , "design" );
		
		impl = new XDocCommand();
	}	

	@Override
	public boolean runExecutorImpl( ScopeState parentState , ActionBase action , CommandMethod method ) {
		boolean res = super.runMethod( parentState , action , method );
		return( res );
	}

	private class DesignDoc extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "CMD" );
		String OUTDIR = getRequiredArg( action , 1 , "OUTDIR" );
		checkNoArgs( action , 2 );
		Meta meta = action.getContextMeta();
		impl.createDesignDoc( action , meta , CMD , OUTDIR );
	}
	}

}
