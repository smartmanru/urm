package org.urm.engine.executor;

import org.urm.action.xdoc.XDocCommand;
import org.urm.common.action.CommandMeta;
import org.urm.engine.ServerEngine;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandAction;
import org.urm.engine.action.CommandExecutor;
import org.urm.meta.product.Meta;

public class XDocCommandExecutor extends CommandExecutor {

	XDocCommand impl;
	
	public XDocCommandExecutor( ServerEngine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		defineAction( new DesignDoc() , "design" );
	}	

	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new XDocCommand();
		}
		catch( Throwable e ) {
			action.handle( e );
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
		Meta meta = action.getContextMeta();
		impl.createDesignDoc( action , meta , CMD , OUTDIR );
	}
	}

}
