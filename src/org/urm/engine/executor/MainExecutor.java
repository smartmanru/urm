package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.main.ActionConfigure;
import org.urm.action.main.ActionSave;
import org.urm.action.main.ActionServer;
import org.urm.action.main.ActionWebSession;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.MainCommandMeta;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.action.CommandExecutor;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.product.Meta;

public class MainExecutor extends CommandExecutor {

	public static MainExecutor createExecutor( Engine engine ) throws Exception {
		MainCommandMeta commandInfo = new MainCommandMeta( engine.optionsMeta );
		return( new MainExecutor( engine , commandInfo ) );
	}
		
	private MainExecutor( Engine engine , MainCommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		super.defineAction( new Configure() , "configure" );
		super.defineAction( new SvnSave() , "svnsave" );
		super.defineAction( new ServerOp() , "server" );
		super.defineAction( new WebSession() , "interactive" );
		super.defineAction( new Temporary() , "temporary" );
	}

	@Override
	public boolean runExecutorImpl( ScopeState parentState , ActionBase action , CommandMethod method ) {
		// log action and run 
		boolean res = super.runMethod( parentState , action , method );
		return( res );
	}
	
	public CommandOptions createOptionsByArgs( CommandBuilder builder , String[] args ) throws Exception {
		CommandOptions options = new CommandOptions( commandInfo.options );
		if( !builder.setOptions( commandInfo , args , options ) )
			return( null );
		
		if( builder.checkHelp( options ) )
			return( null );

		return( options );
	}

	public CommandOptions createOptionsStartServerByWeb( Engine engine ) throws Exception {
		CommandOptions options = new CommandOptions( commandInfo.options );
		ActionData data = new ActionData( engine.execrc );
		data.addArg( "start" );
		options.setAction( commandInfo.getMethod( "server" ) , data );
		
		if( !options.setFromSystemProperties() )
			return( null );
		
		return( options );
	}

	public CommandOptions createOptionsTemporary( Engine engine , boolean useSystemProperties ) throws Exception {
		CommandOptions options = new CommandOptions( commandInfo.options );
		ActionData data = new ActionData( engine.execrc );
		options.setAction( commandInfo.getMethod( "temporary" ) , data );
		
		if( useSystemProperties ) {
			if( !options.setFromSystemProperties() )
				return( null );
		}
		
		return( options );
	}

	public CommandOptions createOptionsInteractiveSession( Engine engine ) throws Exception {
		CommandOptions options = new CommandOptions( commandInfo.options );
		ActionData data = new ActionData( engine.execrc );
		options.setAction( commandInfo.getMethod( "interactive" ) , data );
		return( options );
	}

	// configure proxy files
	private class Configure extends CommandMethod {
		public Configure() {
		}
		
		public void run( ScopeState parentState , ActionBase action ) throws Exception {
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
			ca.runSimpleServer( parentState , SecurityAction.ACTION_CONFIGURE , false );
		}
	}

	// save master to svn
	private class SvnSave extends CommandMethod {
		public void run( ScopeState parentState , ActionBase action ) throws Exception {
			Meta meta = action.getContextMeta();
			ActionSave ca = new ActionSave( action , null , meta );
			ca.runSimpleProduct( parentState , meta.name , SecurityAction.ACTION_CONFIGURE , false );
		}
	}

	// server operation
	private class ServerOp extends CommandMethod {
		public void run( ScopeState parentState , ActionBase action ) throws Exception {
			String OP = getRequiredArg( action , 0 , "ACTION" );
			ActionServer ca = new ActionServer( action , null , OP );
			ca.runSimpleServer( parentState , SecurityAction.ACTION_CONFIGURE , false );
		}
	}

	// server operation
	private class WebSession extends CommandMethod {
		public void run( ScopeState parentState , ActionBase action ) throws Exception {
			ActionWebSession ca = new ActionWebSession( action , null );
			ca.runSimpleServer( parentState , SecurityAction.ACTION_CONFIGURE , true );
		}
	}

	// server operation
	private class Temporary extends CommandMethod {
		public void run( ScopeState parentState , ActionBase action ) throws Exception {
		}
	}

}
