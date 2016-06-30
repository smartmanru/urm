package org.urm.server;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
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
import org.urm.server.executor.BuildCommandExecutor;
import org.urm.server.executor.DatabaseCommandExecutor;
import org.urm.server.executor.DeployCommandExecutor;
import org.urm.server.executor.MainExecutor;
import org.urm.server.executor.MonitorCommandExecutor;
import org.urm.server.executor.ReleaseCommandExecutor;
import org.urm.server.executor.XDocCommandExecutor;
import org.urm.server.meta.Metadata;
import org.urm.server.shell.ShellCoreJNI;
import org.urm.server.shell.ShellExecutorPool;
import org.urm.server.storage.Artefactory;
import org.urm.server.storage.LocalFolder;

public class ServerEngine {

	public RunContext execrc;
	public SessionContext serverSession;
	public ActionInit serverAction;
	public ShellExecutorPool pool;
	public boolean running;
	
	public ServerEngine() {
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
		serverSession = new SessionContext( this , execrc , 0 );
		serverSession.setServerLayout( builder.options );

		// create server action
		serverAction = createAction( builder.options , executor , serverSession , "server" , null );
		if( serverAction == null )
			return( false );

		// run server action
		running = true;
		createPool();
		startAction( serverAction );
		return( runServerAction( serverSession , executor ) );
	}
	
	public boolean runClientMode( CommandBuilder builder , CommandOptions options , RunContext clientrc , CommandMeta commandInfo ) throws Exception {
		execrc = clientrc;
		CommandExecutor executor = createExecutor( commandInfo );
		serverSession = new SessionContext( this , clientrc , 0 );
		
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
		
	private boolean runServerAction( SessionContext session , CommandExecutor executor ) throws Exception {
		// execute
		try {
			executor.runAction( serverAction );
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
		killPool();

		return( res );
	}

	public CommandExecutor createExecutor( CommandMeta commandInfo ) throws Exception {
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
		action.debug( "action created: name=" + action.actionName + ", workfolder=" + artefactory.workFolder.folderPath );
		
		return( action );
	}
	
	public void createPool() throws Exception {
		pool = new ShellExecutorPool( this );
		pool.start( serverAction );
	}

	public void killPool() throws Exception {
		pool.killAll( serverAction );
	}
	
	public void deleteWorkFolder( ActionBase action , LocalFolder workFolder ) throws Exception {
		pool.master.removeDir( action , workFolder.folderPath );
	}
	
	private void stopPool() throws Exception {
		pool.stop( serverAction );
	}
	
	public void startAction( ActionBase action ) throws Exception {
		// create action shell
		if( action.shell == null )
			pool.createDedicatedLocalShell( action , action.context.stream + "::" + action.session.sessionId );
		
		// create work folder
		LocalFolder folder = action.artefactory.getWorkFolder( action );
		folder.recreateThis( action );
		
		// start action log
		action.tee();
		
		// print args
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
		
		pool.killDedicated( action );
	}

	private Artefactory createArtefactory( SessionContext session , CommandContext context ) throws Exception {
		String dirname;
		
		if( session.standalone ) {
			if( !context.CTX_WORKPATH.isEmpty() )
				dirname = context.CTX_WORKPATH;
			else {
				if( context.meta.product.CONFIG_WORKPATH.isEmpty() == false )
					dirname = context.meta.product.CONFIG_WORKPATH;
				else
					dirname = Common.getPath( session.execrc.userHome , "urm.work" , "session-" + ShellCoreJNI.getCurrentProcessId() );
			}
		}
		else {
			if( !session.product ) {
				if( !context.CTX_WORKPATH.isEmpty() )
					dirname = context.CTX_WORKPATH;
				else {
					dirname = Common.getPath( session.execrc.userHome , "urm.work" , "server" );
					dirname = Common.getPath( dirname , "session-" + ShellCoreJNI.getCurrentProcessId() );
				}
			}
			else {
				if( !serverAction.context.CTX_WORKPATH.isEmpty() )
					dirname = serverAction.context.CTX_WORKPATH;
				else {
					if( context.meta.product != null && context.meta.product.CONFIG_WORKPATH.isEmpty() == false ) {
						dirname = Common.getPath( "urm.work" , context.meta.product.CONFIG_WORKPATH );
						dirname = Common.getPath( dirname , "session-" + ShellCoreJNI.getCurrentProcessId() );
					}
					else {
						dirname = Common.getPath( session.execrc.userHome , "urm.work" , "client" );
						dirname = Common.getPath( dirname , session.productDir + "-" + session.timestamp + "-" + session.sessionId );
					}
				}
			}
		}
		
		LocalFolder folder = new LocalFolder( dirname , execrc.isWindows() );
		Artefactory artefactory = new Artefactory( context.meta , folder );
		return( artefactory );
	}

	public void stop() throws Exception {
		if( !running )
			return;
		
		stopPool();
	}
	
}
