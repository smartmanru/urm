package ru.egov.urm.run.xdoc;

import ru.egov.urm.run.ActionInit;
import ru.egov.urm.run.CommandAction;
import ru.egov.urm.run.CommandBuilder;
import ru.egov.urm.run.CommandExecutor;

public class XDocCommandExecutor extends CommandExecutor {

	XDocCommand impl;
	
	public XDocCommandExecutor( CommandBuilder builder ) {
		super( builder );
		
		String releaseOpts = "";
		defineAction( CommandAction.newAction( new DesignDoc() , "design" , "create design docs" , releaseOpts , "./design.sh [OPTIONS] {dot|png} <outdir>" ) );
	}	

	public boolean run( ActionInit action ) {
		try {
			meta.loadDistr( action );
			
			// create implementation
			impl = new XDocCommand();
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private class DesignDoc extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = options.getRequiredArg( action , 0 , "CMD" );
		String OUTDIR = options.getRequiredArg( action , 1 , "OUTDIR" );
		options.checkNoArgs( action , 2 );
		impl.createDesignDoc( action , CMD , OUTDIR );
	}
	}

}
