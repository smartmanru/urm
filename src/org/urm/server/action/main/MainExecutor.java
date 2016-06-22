package org.urm.server.action.main;

import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.server.CommandExecutor;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandAction;

public class MainExecutor extends CommandExecutor {

	RunContext rc;
	
	public static MainExecutor create( ServerEngine engine , CommandBuilder builder , String[] args ) throws Exception {
		MainMeta commandInfo = new MainMeta( builder );
		if( !builder.setOptions( commandInfo , args ) )
			return( null );
		
		return( new MainExecutor( engine , builder.execrc , commandInfo ) );
	}

	private MainExecutor( ServerEngine engine , RunContext rc , MainMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		this.rc = rc;
		super.defineAction( new Configure() , "configure" );
		super.defineAction( new SvnSave() , "svnsave" );
		super.defineAction( new ServerOp() , "server" );
	}

	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}
	
	// configure proxy files
	private class Configure extends CommandAction {
		boolean linux;
		
		public Configure() {
		}
		
		public void run( ActionInit action ) throws Exception {
			linux = action.isLinux();
			
			String ACTION = getRequiredArg( action , 0 , "ACTION" );
			String USEENV = "";
			String USEDC = "";
			if( ACTION.equals( "deploy" ) ) {
				USEENV = getArg( action , 1 );
				USEDC = getArg( action , 2 );
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

	// server operation
	private class ServerOp extends CommandAction {
		public void run( ActionInit action ) throws Exception {
			String OP = getRequiredArg( action , 0 , "ACTION" );
			ActionServer ca = new ActionServer( action , null , OP );
			ca.runSimple();
		}
	}

}
