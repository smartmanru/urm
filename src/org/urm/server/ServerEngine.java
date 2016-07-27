package org.urm.server;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.common.jmx.ServerMBean;
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
import org.urm.server.action.CommandExecutor;
import org.urm.server.executor.BuildCommandExecutor;
import org.urm.server.executor.DatabaseCommandExecutor;
import org.urm.server.executor.DeployCommandExecutor;
import org.urm.server.executor.MainExecutor;
import org.urm.server.executor.MonitorCommandExecutor;
import org.urm.server.executor.ReleaseCommandExecutor;
import org.urm.server.executor.XDocCommandExecutor;
import org.urm.server.meta.FinalMetaLoader;
import org.urm.server.meta.Metadata;
import org.urm.server.shell.ShellCoreJNI;
import org.urm.server.shell.ShellPool;
import org.urm.server.storage.Artefactory;
import org.urm.server.storage.LocalFolder;

public class ServerEngine {

	public RunContext execrc;
	public SessionContext serverSession;
	public SessionController sessionController;
	public ServerMBean jmxController;
	
	public MainExecutor serverExecutor;
	public ActionInit serverAction;
	public ShellPool shellPool;
	public FinalMetaLoader metaLoader;
	public boolean running;
	
	public ServerEngine() {
		metaLoader = new FinalMetaLoader( this );
	}
	
	public void runServer( ActionBase action ) throws Exception {
		metaLoader.loadServerProducts( action );
		
		sessionController = new SessionController( action , this );
		jmxController = new ServerMBean( action , sessionController );
		sessionController.start();
		jmxController.start();
		
		serverAction.info( "server successfully started, accepting connections." );
		sessionController.waitFinished();
	}
	
	public boolean prepareWeb() throws Exception {
		// server environment
		execrc = new RunContext();
		execrc.load();

		// server run options
		serverExecutor = MainExecutor.createExecutor( this );
		CommandOptions options = serverExecutor.createOptionsStartServerByWeb( this );
		if( options == null )
			return( false );
		
		return( prepareServerExecutor( options ) );
	}

	public boolean runWeb() throws Exception {
		return( runServerAction() );
	}
	
	public ActionInit createWebSessionAction() throws Exception {
		CommandOptions options = serverExecutor.createOptionsWebSession( this );
		if( options == null )
			return( null );
		
		RunContext clientrc = RunContext.clone( execrc );
		SessionContext sessionContext = createSession( clientrc , true );
		ActionInit action = createAction( serverExecutor , options , sessionContext , "web" , null );
		startAction( action );
		
		return( action );
	}
	
	public boolean runServerExecutor( MainExecutor serverExecutor , RunContext execrc , CommandOptions options ) throws Exception {
		this.execrc = execrc;
		this.serverExecutor = serverExecutor;
		
		if( !prepareServerExecutor( options ) )
			return( false );
		
		return( runServerAction() );
	}
	
	public boolean prepareServerExecutor( CommandOptions options ) throws Exception {
		// server action environment
		serverSession = createSession( execrc , false );
		serverSession.setServerLayout( options );
		
		if( !options.action.equals( "configure" ) )
			metaLoader.loadServerSettings();

		// create server action
		serverAction = createAction( serverExecutor , options , serverSession , "server" , null );
		if( serverAction == null )
			return( false );

		// run server action
		running = true;
		createPool();
		startAction( serverAction );
		
		return( true );
	}

	public boolean runClientMode( RunContext execrc , CommandOptions options , CommandMeta commandInfo ) throws Exception {
		this.execrc = execrc;
		
		CommandExecutor commandExecutor = createExecutor( commandInfo );
		serverSession = createSession( execrc , false );
		
		if( execrc.standaloneMode )
			serverSession.setStandaloneLayout( options );
		else
			serverSession.setServerLayout( options );
		
		serverAction = createAction( commandExecutor , options , serverSession , "client" , null );
		if( serverAction == null )
			return( false );

		if( !execrc.standaloneMode )
			serverSession.setServerOfflineProductLayout( serverAction , options , execrc.product );
		
		createPool();
		startAction( serverAction );
		serverAction.meta.loadProduct( serverAction );
		
		return( runServerAction() );
	}
		
	private boolean runServerAction() throws Exception {
		// execute
		try {
			serverExecutor.runAction( serverAction );
		}
		catch( Throwable e ) {
			serverAction.log( e );
		}

		boolean res = ( serverSession.isFailed() )? false : true;
		
		if( res )
			serverAction.commentExecutor( "COMMAND SUCCESSFUL" );
		else
			serverAction.commentExecutor( "COMMAND FAILED" );

		finishAction( serverAction );
		killPool();

		return( res );
	}

	public CommandExecutor createExecutor( CommandMeta commandInfo ) throws Exception {
		String cmd = commandInfo.name;
		CommandExecutor executor = null;
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

	public ActionInit createAction( CommandExecutor actionExecutor , CommandOptions options , SessionContext session , String stream , ServerCall call ) throws Exception {
		CommandAction commandAction = actionExecutor.getAction( options.action );
		if( !options.checkValidOptions( commandAction.method ) )
			return( null );
		
		// create context
		Metadata meta = metaLoader.createMetadata( session );
		CommandContext context = new CommandContext( this , session , meta , options , stream , call );
		if( !context.setRunContext() )
			return( null );

		// create artefactory
		context.update();
		Artefactory artefactory = createArtefactory( session , context );
		
		// create action
		ActionInit action = actionExecutor.createAction( session , artefactory , context , options.action );
		action.debug( "action created: actionId=" + action.ID + ", name=" + action.actionName + ", workfolder=" + artefactory.workFolder.folderPath );
		
		return( action );
	}
	
	public void createPool() throws Exception {
		shellPool = new ShellPool( this );
		shellPool.start( serverAction );
	}

	public void killPool() throws Exception {
		shellPool.killAll( serverAction );
	}
	
	public void deleteWorkFolder( ActionBase action , LocalFolder workFolder ) throws Exception {
		shellPool.master.removeDir( action , workFolder.folderPath );
	}
	
	private void stopPool() throws Exception {
		shellPool.stop( serverAction );
	}
	
	public void startAction( ActionBase action ) throws Exception {
		// create action shell
		if( action.shell == null )
			shellPool.createDedicatedLocalShell( action , action.context.stream + "::" + action.session.sessionId );
		
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
		
		shellPool.releaseActionPool( action );
	}

	private Artefactory createArtefactory( SessionContext session , CommandContext context ) throws Exception {
		String dirname;
		
		if( session.standalone ) {
			if( !context.CTX_WORKPATH.isEmpty() )
				dirname = context.CTX_WORKPATH;
			else {
				if( context.meta.product != null && context.meta.product.CONFIG_WORKPATH.isEmpty() == false )
					dirname = context.meta.product.CONFIG_WORKPATH;
				else
					dirname = Common.getPath( session.execrc.userHome , "urm.work" , "session-" + ShellCoreJNI.getCurrentProcessId() );
			}
		}
		else {
			if( !session.client ) {
				if( !context.CTX_WORKPATH.isEmpty() )
					dirname = context.CTX_WORKPATH;
				else {
					dirname = Common.getPath( session.execrc.userHome , "urm.work" , "server" );
					dirname = Common.getPath( dirname , "session-" + ShellCoreJNI.getCurrentProcessId() );
				}
			}
			else {
				if( !context.CTX_WORKPATH.isEmpty() )
					dirname = context.CTX_WORKPATH;
				else {
					if( context.meta.product != null && context.meta.product.CONFIG_WORKPATH.isEmpty() == false ) {
						dirname = context.meta.product.CONFIG_WORKPATH;
						dirname = Common.getPath( dirname , "session-" + session.sessionId );
					}
					else {
						dirname = Common.getPath( session.execrc.userHome , "urm.work" , "client" );
						String name = "session-" + session.timestamp;
						if( !session.productName.isEmpty() )
							name += "-" + session.productName;
						name += "-" + session.sessionId; 
						dirname = Common.getPath( dirname , name );
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

	public SessionContext createSession( RunContext clientrc , boolean client ) {
		int sessionId = 0;
		if( sessionController != null )
			sessionId = sessionController.createSessionId();
		SessionContext session = new SessionContext( this , clientrc , sessionId , client );
		return( session );
	}
	
}
