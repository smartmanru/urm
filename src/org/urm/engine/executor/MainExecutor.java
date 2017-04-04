package org.urm.engine.executor;

import org.urm.action.main.ActionConfigure;
import org.urm.action.main.ActionSave;
import org.urm.action.main.ActionServer;
import org.urm.action.main.ActionWebSession;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.MainCommandMeta;
import org.urm.engine.ServerEngine;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandAction;
import org.urm.engine.action.CommandExecutor;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.product.Meta;

public class MainExecutor extends CommandExecutor {

	public static MainExecutor createExecutor( ServerEngine engine ) throws Exception {
		MainCommandMeta commandInfo = new MainCommandMeta();
		return( new MainExecutor( engine , commandInfo ) );
	}
		
	private MainExecutor( ServerEngine engine , MainCommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		super.defineAction( new Configure() , "configure" );
		super.defineAction( new SvnSave() , "svnsave" );
		super.defineAction( new ServerOp() , "server" );
		super.defineAction( new WebSession() , "interactive" );
		super.defineAction( new Temporary() , "temporary" );
	}

	@Override
	public boolean run( ActionInit action ) {
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}
	
	public CommandOptions createOptionsByArgs( CommandBuilder builder , String[] args ) throws Exception {
		CommandOptions options = new CommandOptions();
		if( !builder.setOptions( commandInfo , args , options ) )
			return( null );
		
		if( builder.checkHelp( options ) )
			return( null );

		return( options );
	}

	public CommandOptions createOptionsStartServerByWeb( ServerEngine engine ) throws Exception {
		CommandOptions options = new CommandOptions();
		ActionData data = new ActionData( engine.execrc );
		data.addArg( "start" );
		options.setAction( commandInfo.getMethod( "server" ) , data );
		
		if( !options.setFromSystemProperties() )
			return( null );
		
		return( options );
	}

	public CommandOptions createOptionsTemporary( ServerEngine engine ) throws Exception {
		CommandOptions options = new CommandOptions();
		ActionData data = new ActionData( engine.execrc );
		options.setAction( commandInfo.getMethod( "temporary" ) , data );
		return( options );
	}

	public CommandOptions createOptionsInteractiveSession( ServerEngine engine ) throws Exception {
		CommandOptions options = new CommandOptions();
		ActionData data = new ActionData( engine.execrc );
		options.setAction( commandInfo.getMethod( "interactive" ) , data );
		return( options );
	}

	// configure proxy files
	private class Configure extends CommandAction {
		public Configure() {
		}
		
		public void run( ActionInit action ) throws Exception {
			String OSTYPE = getArg( action , 0 );
			String USEENV = getArg( action , 1 );
			String USESG = getArg( action , 2 );
			if( OSTYPE == null )
				OSTYPE = "";
			if( USEENV == null )
				USEENV = "";
			if( USESG == null )
				USESG = "";
	
			ActionConfigure ca = new ActionConfigure( action , null , OSTYPE , USEENV , USESG );
			ca.runSimpleServer( SecurityAction.ACTION_CONFIGURE , false );
		}
	}

	// save master to svn
	private class SvnSave extends CommandAction {
		public void run( ActionInit action ) throws Exception {
			Meta meta = action.getContextMeta();
			ActionSave ca = new ActionSave( action , null , meta );
			ca.runSimpleProduct( meta.name , SecurityAction.ACTION_CONFIGURE , false );
		}
	}

	// server operation
	private class ServerOp extends CommandAction {
		public void run( ActionInit action ) throws Exception {
			String OP = getRequiredArg( action , 0 , "ACTION" );
			ActionServer ca = new ActionServer( action , null , OP );
			ca.runSimpleServer( SecurityAction.ACTION_CONFIGURE , false );
		}
	}

	// server operation
	private class WebSession extends CommandAction {
		public void run( ActionInit action ) throws Exception {
			ActionWebSession ca = new ActionWebSession( action , null );
			ca.runSimpleServer( SecurityAction.ACTION_CONFIGURE , true );
		}
	}

	// server operation
	private class Temporary extends CommandAction {
		public void run( ActionInit action ) throws Exception {
		}
	}

}
