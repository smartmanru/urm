package org.urm.server;

import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandOptions;
import org.urm.common.jmx.ServerCommandCall;
import org.urm.common.meta.BuildCommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.common.meta.MonitorCommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.common.meta.XDocCommandMeta;
import org.urm.server.action.ActionBase;
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
import org.urm.server.shell.ShellExecutorPool;
import org.urm.server.storage.LocalFolder;

public class ServerEngine {

	public RunContext execrc;
	public SessionContext serverSession;
	public ActionInit serverAction;

	int invokeSequence = 0;
	
	public ShellExecutorPool pool;
	
	public ServerEngine() {
	}
	
	public synchronized int createSessionId() {
		invokeSequence++;
		return( invokeSequence );
	}
	
	public boolean runArgs( String[] args ) throws Exception {
		// server environment
		execrc = new RunContext();
		execrc.load();
		if( !execrc.isMain() )
			throw new ExitException( "only main executor id expected" );

		// server run options
		CommandBuilder builder = new CommandBuilder( execrc , execrc );
		CommandExecutor executor = MainExecutor.create( this , builder , args );
		if( executor == null )
			return( false );
		
		// server action environment
		serverSession = new SessionContext( execrc , execrc );
		serverSession.setServerLayout( builder.options );

		// create server action
		serverAction = createAction( builder , builder.options , executor , serverSession , "server" , null );
		if( serverAction == null )
			return( false );

		// run server action
		return( runServerAction( serverSession , executor ) );
	}
	
	public boolean runClientMode( CommandBuilder builder , CommandOptions options , RunContext clientrc , CommandMeta commandInfo ) throws Exception {
		execrc = clientrc;
		CommandExecutor executor = createExecutor( commandInfo );
		SessionContext session = new SessionContext( clientrc , execrc );
		
		if( clientrc.productDir.isEmpty() )
			session.setStandaloneLayout( options );
		else
			session.setServerProductLayout( clientrc.productDir );
		
		serverAction = createAction( builder , options , executor , session , "client" , null );
		if( serverAction == null )
			return( false );
		
		serverAction.meta.loadProduct( serverAction );
		return( runServerAction( session , executor ) );
	}
		
	public boolean runClientRemote( ServerCommandCall call , CommandMethod method , ActionData data ) throws Exception {
		CommandBuilder builder = new CommandBuilder( data.clientrc , execrc );
		
		CommandOptions options = new CommandOptions( serverAction.context.options.meta );
		options.setAction( call.command.meta.name , method , data );
		
		CommandMeta commandInfo = builder.createMeta( options.command );
		if( commandInfo == null )
			return( false );
		
		CommandExecutor executor = createExecutor( commandInfo );
		SessionContext session = new SessionContext( data.clientrc , execrc );
		session.setServerClientLayout( serverSession );
		
		ActionInit action = createAction( builder , options , executor , session , "remote-" + data.clientrc.productDir , call );
		if( action == null )
			return( false );

		action.meta.loadProduct( action );
		return( runClientAction( session , executor , action ) );
	}

	public boolean runClientJmx( String productDir , CommandMeta meta , CommandOptions options ) throws Exception {
		CommandExecutor executor = createExecutor( meta );
		SessionContext session = new SessionContext( execrc , execrc );
		session.setServerProductLayout( productDir );
		
		CommandBuilder builder = new CommandBuilder( execrc , execrc );
		ActionInit action = createAction( builder , options , executor , session , "jmx-" + execrc.productDir , null );
		if( action == null )
			return( false );

		action.meta.loadProduct( action );
		return( runClientAction( session , executor , action ) );
	}
	
	private boolean runClientAction( SessionContext session , CommandExecutor executor , ActionInit clientAction ) throws Exception {
		// execute
		try {
			executor.run( clientAction );
		}
		catch( Throwable e ) {
			clientAction.log( e );
		}

		boolean res = ( session.isFailed() )? false : true;
		
		if( res )
			clientAction.commentExecutor( "COMMAND SUCCESSFUL" );
		else
			clientAction.commentExecutor( "COMMAND FAILED" );
			
		executor.finish( clientAction );

		return( res );
	}

	private boolean runServerAction( SessionContext session , CommandExecutor executor ) throws Exception {
		// execute
		try {
			executor.run( serverAction );
			killPool();
		}
		catch( Throwable e ) {
			serverAction.log( e );
		}

		boolean res = ( session.isFailed() )? false : true;
		
		if( res )
			serverAction.commentExecutor( "COMMAND SUCCESSFUL" );
		else
			serverAction.commentExecutor( "COMMAND FAILED" );
			
		executor.finish( serverAction );
		stopPool();

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

	public ActionInit createAction( CommandBuilder builder , CommandOptions options , CommandExecutor executor , SessionContext session , String stream , ServerCommandCall call ) throws Exception {
		// create context
		CommandContext context = new CommandContext( this , options , session , stream , call );
		if( !context.setRunContext() )
			return( null );
		
		if( !context.setAction( builder , executor ) )
			return( null );
		
		Metadata meta = new Metadata();
		ActionInit action = executor.prepare( context , meta , options.action );
		
		return( action );
	}
	
	public void createPool() throws Exception {
		pool = new ShellExecutorPool( this );
		pool.start( serverAction );
	}

	public void killPool() throws Exception {
		pool.kill( serverAction );
	}
	
	public void deleteWorkFolder( ActionBase action , LocalFolder workFolder ) throws Exception {
		pool.master.removeDir( action , workFolder.folderPath );
	}
	
	public void stopPool() throws Exception {
		pool.stop( serverAction );
	}
	
}
