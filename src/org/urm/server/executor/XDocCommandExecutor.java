package org.urm.server.executor;

import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.server.CommandExecutor;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandAction;
import org.urm.server.action.xdoc.XDocCommand;

public class XDocCommandExecutor extends CommandExecutor {

	XDocCommand impl;
	
	public XDocCommandExecutor( ServerEngine engine , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		super( engine , commandInfo , options );
		
		defineAction( new DesignDoc() , "design" );
	}	

	public boolean run( ActionInit action ) {
		try {
			action.meta.loadDistr( action );
			
			// create implementation
			impl = new XDocCommand();
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}

	private class DesignDoc extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "CMD" );
		String OUTDIR = getRequiredArg( action , 1 , "OUTDIR" );
		checkNoArgs( action , 2 );
		impl.createDesignDoc( action , CMD , OUTDIR );
	}
	}

}
