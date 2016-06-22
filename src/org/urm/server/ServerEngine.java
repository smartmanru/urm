package org.urm.server;

import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.common.meta.BuildCommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.common.meta.MonitorCommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.common.meta.XDocCommandMeta;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandContext;
import org.urm.server.action.build.BuildCommandExecutor;
import org.urm.server.action.database.DatabaseCommandExecutor;
import org.urm.server.action.deploy.DeployCommandExecutor;
import org.urm.server.action.main.MainExecutor;
import org.urm.server.action.monitor.MonitorCommandExecutor;
import org.urm.server.action.release.ReleaseCommandExecutor;
import org.urm.server.action.xdoc.XDocCommandExecutor;
import org.urm.server.meta.Metadata;

public class ServerEngine {

	public RunContext execrc;
	public SessionContext serverSession;
	
	public ServerEngine() {
	}
	
	public boolean runArgs( String[] args ) throws Exception {
		execrc = new RunContext();
		execrc.load();

		if( !execrc.isMain() )
			throw new ExitException( "only main executor id expected" );

		CommandBuilder builder = new CommandBuilder( execrc , execrc );
		CommandExecutor executor = MainExecutor.create( this , builder , args );
		if( executor == null )
			return( false );
		
		serverSession = new SessionContext( execrc );
		ActionInit action = createAction( builder , builder.options , executor , serverSession );
		if( action == null )
			return( false );
		
		return( runAction( serverSession , executor , action ) );
	}
	
	public boolean runClientMode( CommandBuilder builder , CommandOptions options , RunContext clientrc , CommandMeta commandInfo ) throws Exception {
		execrc = clientrc;
		CommandExecutor executor = createExecutor( commandInfo );
		SessionContext session = new SessionContext( clientrc );
		
		if( clientrc.productDir.isEmpty() )
			session.setStandaloneLayout( options );
		else
			session.setServerProductLayout( clientrc.productDir );
		
		ActionInit action = createAction( builder , options , executor , session );
		if( action == null )
			return( false );
		
		action.meta.loadProduct( action );
		return( runAction( session , executor , action ) );
	}
		
	public boolean runClientRemote( CommandOptions options , RunContext clientrc ) throws Exception {
		CommandBuilder builder = new CommandBuilder( clientrc , execrc );
		CommandMeta commandInfo = builder.createMeta( options.command );
		if( commandInfo == null )
			return( false );
		
		CommandExecutor executor = createExecutor( commandInfo );
		SessionContext session = new SessionContext( clientrc );
		session.setServerClientLayout( serverSession );
		
		ActionInit action = createAction( builder , options , executor , session );
		if( action == null )
			return( false );
		
		action.meta.loadProduct( action );
		return( runAction( session , executor , action ) );
	}
		
	private boolean runAction( SessionContext session , CommandExecutor executor , ActionInit action ) throws Exception {
		// execute
		try {
			executor.run( action );
			action.context.killPool( action );
		}
		catch( Throwable e ) {
			action.log( e );
		}

		boolean res = ( session.isFailed() )? false : true;
		
		if( res )
			action.commentExecutor( "COMMAND SUCCESSFUL" );
		else
			action.commentExecutor( "COMMAND FAILED" );
			
		executor.finish( action );
		action.context.stopPool( action );

		return( res );
	}

	private CommandExecutor createExecutor( CommandMeta commandInfo ) throws Exception {
		CommandExecutor executor = null;
		String cmd = commandInfo.name;
		if( cmd.equals( BuildCommandMeta.NAME ) )
			executor = new BuildCommandExecutor( this , commandInfo );
		else if( cmd.equals( DeployCommandMeta.NAME ) )
			executor = new DeployCommandExecutor( this , commandInfo );
		else if( cmd.equals( DatabaseCommandMeta.NAME ) )
			executor = new DatabaseCommandExecutor( this , commandInfo );
		else if( cmd.equals( MonitorCommandMeta.NAME ) )
			executor = new MonitorCommandExecutor( this , commandInfo );
		else if( cmd.equals( ReleaseCommandMeta.NAME ) )
			executor = new ReleaseCommandExecutor( this , commandInfo );
		else if( cmd.equals( XDocCommandMeta.NAME ) )
			executor = new XDocCommandExecutor( this , commandInfo );
		else
			throw new ExitException( "Unexpected URM args - unknown command executor=" + cmd + " (expected one of build/deploy/database/monitor)" );
		
		return( executor );
	}

	public ActionInit createAction( CommandBuilder builder , CommandOptions options , CommandExecutor executor , SessionContext session ) throws Exception {
		// create context
		CommandContext context = new CommandContext( session.clientrc , execrc , options , session );
		if( !context.setRunContext() )
			return( null );
		
		if( !context.setAction( builder , executor ) )
			return( null );
		
		Metadata meta = new Metadata();
		ActionInit action = executor.prepare( context , meta , options.action );
		return( action );
	}
	
}
