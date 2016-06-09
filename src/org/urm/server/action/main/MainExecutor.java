package org.urm.server.action.main;

import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.server.CommandExecutor;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandAction;

public class MainExecutor extends CommandExecutor {

	RunContext rc;
	
	public static MainExecutor create( CommandBuilder builder , String[] args ) throws Exception {
		MainMeta commandInfo = new MainMeta( builder );
		if( !builder.setOptions( commandInfo , args ) )
			return( null );
		
		return( new MainExecutor( builder.rc , commandInfo ) );
	}

	private MainExecutor( RunContext rc , MainMeta commandInfo ) throws Exception {
		super( commandInfo );
		
		this.rc = rc;
		super.defineAction( new Configure( true ) , "configure-linux" );
		super.defineAction( new Configure( false ) , "configure-windows" );
		super.defineAction( new SvnSave() , "svnsave" );
	}

	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}
	
	// configure proxy files
	private class Configure extends CommandAction {
		boolean linux;
		
		public Configure( boolean linux ) {
			this.linux = linux;
		}
		
		public void run( ActionInit action ) throws Exception {
			String ACTION = getRequiredArg( action , 0 , "ACTION" );
			String USEENV = "";
			String USEDC = "";
			if( ACTION.equals( "deploy" ) ) {
				USEENV = getArg( action , 2 );
				USEDC = getArg( action , 3 );
				if( USEENV == null )
					USEENV = "";
				if( USEDC == null )
					USEDC = "";
			}
	
			ActionConfigure ca = new ActionConfigure( action , null , linux , ACTION , USEENV , USEDC );
			ca.runSimple();
		}
	}

	// save master to svn
	private class SvnSave extends CommandAction {
		public void run( ActionInit action ) throws Exception {
			ActionSave ca = new ActionSave( action , null );
			ca.runSimple();
		}
	}

}
