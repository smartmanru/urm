package org.urm.server.executor;

import org.urm.common.RunContext;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.MainCommandMeta;
import org.urm.server.CommandExecutor;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandAction;
import org.urm.server.action.main.ActionConfigure;
import org.urm.server.action.main.ActionSave;
import org.urm.server.action.main.ActionServer;

public class MainExecutor extends CommandExecutor {

	RunContext execrc;
	
	public static MainExecutor createByArgs( ServerEngine engine , CommandBuilder builder , String[] args ) throws Exception {
		MainCommandMeta commandInfo = new MainCommandMeta();
		
		CommandOptions options = new CommandOptions();
		if( !builder.setOptions( commandInfo , args , options ) )
			return( null );
		
		if( builder.checkHelp( options ) )
			return( null );

		return( new MainExecutor( engine , engine.execrc , commandInfo , options ) );
	}

	public static MainExecutor createByWeb( ServerEngine engine ) throws Exception {
		MainCommandMeta commandInfo = new MainCommandMeta();
		
		CommandOptions options = new CommandOptions();
		ActionData data = new ActionData( engine.execrc );
		data.addArg( "start" );
		options.setAction( commandInfo.getAction( "server" ) , data );
		
		return( new MainExecutor( engine , engine.execrc , commandInfo , options ) );
	}

	private MainExecutor( ServerEngine engine , RunContext execrc , MainCommandMeta commandInfo , CommandOptions options ) throws Exception {
		super( engine , commandInfo , options );
		
		this.execrc = execrc;
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
			linux = action.isLocalLinux();
			
			String USEENV = getArg( action , 0 );
			String USEDC = getArg( action , 1 );
			if( USEENV == null )
				USEENV = "";
			if( USEDC == null )
				USEDC = "";
	
			ActionConfigure ca = new ActionConfigure( action , null , linux , USEENV , USEDC );
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
