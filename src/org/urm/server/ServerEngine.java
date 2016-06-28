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
import org.urm.server.action.CommandAction;
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
import org.urm.server.storage.Artefactory;
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
		serverAction = createAction( builder.options , executor , serverSession , "server" , null );
		if( serverAction == null )
			return( false );

		// run server action
		startAction( serverAction );
		return( runServerAction( serverSession , executor ) );
	}
	
	public boolean runClientMode( CommandBuilder builder , CommandOptions options , RunContext clientrc , CommandMeta commandInfo ) throws Exception {
		execrc = clientrc;
		CommandExecutor executor = createExecutor( commandInfo );
		serverSession = new SessionContext( clientrc , execrc );
		
		if( clientrc.productDir.isEmpty() )
			serverSession.setStandaloneLayout( options );
		else
			serverSession.setServerProductLayout( clientrc.productDir );
		
		serverAction = createAction( options , executor , serverSession , "client" , null );
		if( serverAction == null )
			return( false );
		
		startAction( serverAction );
		serverAction.meta.loadProduct( serverAction );
		return( runServerAction( serverSession , executor ) );
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
		
		ActionInit action = createAction( options , executor , session , "remote-" + data.clientrc.productDir , call );
		if( action == null )
			return( false );

		return( runClientAction( session , executor , action ) );
	}

	public boolean runClientJmx( String productDir , CommandMeta meta , CommandOptions options ) throws Exception {
		CommandExecutor executor = createExecutor( meta );
		SessionContext session = new SessionContext( execrc , execrc );
		session.setServerProductLayout( productDir );
		
		ActionInit action = createAction( options , executor , session , "jmx-" + execrc.productDir , null );
		if( action == null )
			return( false );

		return( runClientAction( session , executor , action ) );
	}
	
	private boolean runClientAction( SessionContext session , CommandExecutor executor , ActionInit clientAction ) throws Exception {
		startAction( clientAction );
		clientAction.meta.loadProduct( clientAction );
		
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
			
		finishAction( clientAction );

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
			
		finishAction( serverAction );
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

	public ActionInit createAction( CommandOptions options , CommandExecutor executor , SessionContext session , String stream , ServerCommandCall call ) throws Exception {
		CommandAction commandAction = executor.getAction( options.action );
		if( !options.checkValidOptions( commandAction.method ) )
			return( null );
		
		// create context
		Metadata meta = new Metadata();
		CommandContext context = new CommandContext( this , session , meta , options , stream , call );
		if( !context.setRunContext() )
			return( null );

		// create artefactory
		context.update();
		Artefactory artefactory = createArtefactory( session , context );
		
		// create action
		ActionInit action = executor.createAction( session , artefactory , context , options.action );
		
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
	
	public void startAction( ActionBase action ) throws Exception {
		// create work folder
		LocalFolder folder = action.artefactory.getWorkFolder( action );
		folder.recreateThis( action );
		
		action.tee();
		
		// print
		if( action.context.CTX_SHOWALL ) {
			String info = action.context.options.getRunningOptions();
			action.commentExecutor( info );
		}
	}
	
	public void finishAction( ActionBase action ) throws Exception {
		action.stopAllOutputs();
		
		if( action.context.session.isFailed() || action.context.CTX_SHOWALL )
			action.info( "saved work directory: " + action.artefactory.workFolder.folderPath );
		else
			action.artefactory.workFolder.removeThis( action );
	}

	private Artefactory createArtefactory( SessionContext session , CommandContext context ) throws Exception {
		String dirname;
		
		if( !context.CTX_WORKPATH.isEmpty() ) {
			dirname = context.CTX_WORKPATH;
		}
		else {
			if( context.meta.product != null && context.meta.product.CONFIG_WORKPATH.isEmpty() == false )
				dirname = context.meta.product.CONFIG_WORKPATH;
			else
				dirname = session.execrc.userHome;
		}
		
		LocalFolder folder = new LocalFolder( dirname , execrc.isWindows() );
		Artefactory artefactory = new Artefactory( context.meta , folder );
		return( artefactory );
	}

}
