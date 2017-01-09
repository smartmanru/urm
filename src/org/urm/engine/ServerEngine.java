package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.common.jmx.ServerMBean;
import org.urm.common.meta.BuildCommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.common.meta.MainCommandMeta;
import org.urm.common.meta.MonitorCommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.common.meta.XDocCommandMeta;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandAction;
import org.urm.engine.action.CommandContext;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.action.CommandOutput;
import org.urm.engine.executor.BuildCommandExecutor;
import org.urm.engine.executor.DatabaseCommandExecutor;
import org.urm.engine.executor.DeployCommandExecutor;
import org.urm.engine.executor.MainExecutor;
import org.urm.engine.executor.MonitorCommandExecutor;
import org.urm.engine.executor.ReleaseCommandExecutor;
import org.urm.engine.executor.XDocCommandExecutor;
import org.urm.engine.shell.ShellCoreJNI;
import org.urm.engine.shell.ShellPool;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.ServerLoader;
import org.urm.meta.engine.ServerAuth;
import org.urm.meta.engine.ServerMonitoring;

public class ServerEngine {

	public RunContext execrc;
	public ServerSession serverSession;
	public SessionController sessionController;
	public ServerMBean jmxController;
	
	public MainExecutor serverExecutor;
	public ActionInit serverAction;
	public ShellPool shellPool;
	
	private ServerAuth auth;
	private ServerEvents events;
	private ServerLoader loader;
	public boolean running;

	private TransactionBase currentTransaction = null;

	public ServerBlotter blotter;
	public MainExecutor mainExecutor;
	public BuildCommandExecutor buildExecutor;
	public DatabaseCommandExecutor databaseExecutor;
	public DeployCommandExecutor deployExecutor;
	public MonitorCommandExecutor monitorExecutor;
	public ReleaseCommandExecutor releaseExecutor;
	public XDocCommandExecutor xdocExecutor;
	
	public static int META_CHANGE_TIMEOUT = 5000;
	
	public ServerEngine( RunContext execrc ) {
		this.execrc = execrc;
		
		auth = new ServerAuth( this );
		events = new ServerEvents( this );
		loader = new ServerLoader( this );
		sessionController = new SessionController( this );
		blotter = new ServerBlotter( this );
	}
	
	public void init() throws Exception {
		auth.init();
		events.init();
		loader.init();
		blotter.init();
		
		mainExecutor = MainExecutor.createExecutor( this );
		buildExecutor = BuildCommandExecutor.createExecutor( this );
		databaseExecutor = DatabaseCommandExecutor.createExecutor( this );
		deployExecutor = DeployCommandExecutor.createExecutor( this );
		monitorExecutor = MonitorCommandExecutor.createExecutor( this );
		releaseExecutor = ReleaseCommandExecutor.createExecutor( this );
		xdocExecutor = XDocCommandExecutor.createExecutor( this );
	}
	
	public void runServer( ActionInit action ) throws Exception {
		serverAction.debug( "load server configuration ..." );
		loader.loadServerProducts( action.actionInit );
		sessionController.start( serverAction );
		
		ServerMonitoring mon = loader.getMonitoring();
		mon.start();
		events.start();
		
		jmxController = new ServerMBean( action , this );
		jmxController.start();
		
		serverAction.info( "server successfully started, accepting connections." );
		sessionController.waitFinished( serverAction );
	}
	
	public void stopServer() throws Exception {
		if( !running )
			return;
		
		serverAction.info( "stopping server ..." );
		
		events.stop();
		ServerMonitoring mon = loader.getMonitoring();
		mon.stop();
		shellPool.stop( serverAction );
		
		jmxController.stop();
		sessionController.stop( serverAction );
		jmxController = null;
		loader.clearServerProducts();
		blotter.clear();
		
		running = false;
	}

	public boolean isRunning() {
		return( running );
	}
	
	public boolean prepareWeb() throws Exception {
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
	
	public void stopWeb() throws Exception {
		stopServer();
	}

	public ServerSession createClientSession( SessionSecurity security , RunContext clientrc ) throws Exception {
		ServerSession sessionContext = sessionController.createSession( security , clientrc , true );
		return( sessionContext );
	}
	
	public ActionInit createWebSessionAction( ServerSession session ) throws Exception {
		CommandOptions options = serverExecutor.createOptionsWebSession( this );
		if( options == null )
			return( null );
		
		ActionInit action = createAction( options , session , "web" , null , false );
		startAction( action );
		
		return( action );
	}
	
	public ActionInit createTemporaryAction( String name , ServerSession session ) throws Exception {
		CommandOptions options = serverExecutor.createOptionsTemporary( this );
		if( options == null )
			return( null );
		
		ActionInit action = createAction( options , session , name , null , true );
		startAction( action );
		
		return( action );
	}
	
	public boolean runServerExecutor( MainExecutor serverExecutor , CommandOptions options ) throws Exception {
		this.serverExecutor = serverExecutor;
		
		if( !prepareServerExecutor( options ) )
			return( false );
		
		return( runServerAction() );
	}
	
	public boolean prepareServerExecutor( CommandOptions options ) throws Exception {
		// server action environment
		SessionSecurity security = auth.createServerSecurity();
		serverSession = sessionController.createSession( security , execrc , false );
		serverSession.setServerLayout( options );
		
		// create server action
		serverAction = createAction( options , serverSession , "server" , null , false );
		if( serverAction == null )
			return( false );

		// run server action
		running = true;
		createPool();
		startAction( serverAction );
		
		return( true );
	}

	public boolean runClientMode( CommandOptions options , CommandMeta commandInfo ) throws Exception {
		SessionSecurity security = auth.createServerSecurity();
		serverSession = sessionController.createSession( security , execrc , false );
		
		if( execrc.isStandalone() )
			serverSession.setStandaloneLayout( options );
		else
			serverSession.setServerLayout( options );
		
		serverAction = createAction( options , serverSession , "client" , null , false );
		if( serverAction == null )
			return( false );

		createPool();
		if( !execrc.isStandalone() )
			serverSession.setServerOfflineProductLayout( serverAction , options , execrc.product );
		
		startAction( serverAction );
		return( runServerAction() );
	}
		
	private boolean runServerAction() throws Exception {
		// execute
		try {
			serverExecutor.runAction( serverAction );
		}
		catch( Throwable e ) {
			serverAction.handle( e );
		}

		boolean res = ( serverAction.isFailed() )? false : true;
		
		if( res )
			serverAction.commentExecutor( "COMMAND SUCCESSFUL" );
		else
			serverAction.commentExecutor( "COMMAND FAILED" );

		finishAction( serverAction , true );
		killPool();

		return( res );
	}

	public CommandExecutor getExecutor( CommandOptions options ) throws Exception {
		if( options.command.equals( MainCommandMeta.NAME ) )
			return( mainExecutor );
		if( options.command.equals( BuildCommandMeta.NAME ) )
			return( buildExecutor );
		if( options.command.equals( DatabaseCommandMeta.NAME ) )
			return( databaseExecutor );
		if( options.command.equals( DeployCommandMeta.NAME ) )
			return( deployExecutor );
		if( options.command.equals( MonitorCommandMeta.NAME ) )
			return( monitorExecutor );
		if( options.command.equals( ReleaseCommandMeta.NAME ) )
			return( releaseExecutor );
		if( options.command.equals( XDocCommandMeta.NAME ) )
			return( xdocExecutor );
		
		Common.exit1( _Error.UnknownCommandExecutor1 , "Unexpected URM args - unknown command executor=" + options.command + " (expected one of " +
				BuildCommandMeta.NAME + "/" +
				DeployCommandMeta.NAME + "/" + 
				DatabaseCommandMeta.NAME + "/" +
				MonitorCommandMeta.NAME + "/" +
				ReleaseCommandMeta.NAME + "/" +
				XDocCommandMeta.NAME + ")" , options.command );
		return( null );
	}
	
	public ActionInit createAction( CommandOptions options , ServerSession session , String stream , ServerCall call , boolean memoryOnly ) throws Exception {
		CommandExecutor actionExecutor = getExecutor( options );
		CommandAction commandAction = actionExecutor.getAction( options.action );
		if( !options.checkValidOptions( commandAction.method ) )
			return( null );
		
		// create context
		CommandContext context = new CommandContext( this , session , options , stream , call );
		if( !context.setRunContext() )
			return( null );

		// create artefactory
		Artefactory artefactory = createArtefactory( session , context , memoryOnly );
		
		// create action
		ActionInit action = createAction( actionExecutor , session , artefactory , options.action , memoryOnly );
		context.update( action );
		actionExecutor.setActionContext( action , context );
		
		if( memoryOnly )
			action.debug( "memory action created: actionId=" + action.ID + ", name=" + action.actionName );
		else
			action.debug( "normal action created: actionId=" + action.ID + ", name=" + action.actionName + ", workfolder=" + artefactory.workFolder.folderPath );
		
		return( action );
	}
	
	public ActionInit createAction( CommandExecutor executor , ServerSession session , Artefactory artefactory , String actionName , boolean memoryOnly ) throws Exception { 
		CommandOutput output = new CommandOutput();
		CommandAction commandAction = executor.getAction( actionName );
		ActionInit action = new ActionInit( loader , session , artefactory , executor , output , commandAction , commandAction.method.name , memoryOnly );
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
	
	public void startAction( ActionInit action ) throws Exception {
		if( !action.isMemoryOnly() ) {
			// create action shell
			if( action.shell == null )
				shellPool.createDedicatedLocalShell( action , action.context.stream );
			
			// start action log
			action.tee();
			blotter.startAction( action );
		}
		
		// print args
		if( action.context.CTX_SHOWALL ) {
			String info = action.context.options.getRunningOptions();
			action.commentExecutor( info );
		}
	}
	
	public void finishAction( ActionInit action , boolean closeSession ) throws Exception {
		action.stopAllOutputs();
		
		if( !action.isMemoryOnly() ) {
			if( action.isFailed() || action.context.CTX_SHOWALL )
				action.info( "saved work directory: " + action.artefactory.workFolder.folderPath );
			else
				action.artefactory.workFolder.removeThis( action );
			
			shellPool.releaseActionPool( action );
			if( closeSession ) {
				sessionController.closeSession( action.session );
				action.clearSession();
			}
		}
	}

	private Artefactory createArtefactory( ServerSession session , CommandContext context , boolean memoryOnly ) throws Exception {
		if( memoryOnly )
			return( new Artefactory( null ) );
			
		String dirname = "";
		
		if( session.standalone ) {
			if( !execrc.workPath.isEmpty() )
				dirname = execrc.workPath;
			else
				dirname = Common.getPath( session.execrc.userHome , "urm.work" , "session-" + ShellCoreJNI.getCurrentProcessId() );
		}
		else {
			if( !session.client ) {
				if( !execrc.workPath.isEmpty() )
					dirname = execrc.workPath;
				else {
					dirname = Common.getPath( session.execrc.userHome , "urm.work" , "server" );
					dirname = Common.getPath( dirname , "session-" + ShellCoreJNI.getCurrentProcessId() );
				}
			}
			else {
				if( !execrc.workPath.isEmpty() )
					dirname = execrc.workPath;
				else
					dirname = Common.getPath( session.execrc.userHome , "urm.work" , "client" );
				
				String name = "session-" + session.timestamp;
				if( !session.productName.isEmpty() )
					name += "-" + session.productName;
				name += "-" + session.sessionId; 
				dirname = Common.getPath( dirname , name );
			}
		}
		
		LocalFolder folder = new LocalFolder( dirname , execrc.isWindows() );
		Artefactory artefactory = new Artefactory( folder );
		artefactory.createWorkFolder();
		return( artefactory );
	}

	public ServerTransaction createTransaction( ActionInit action ) {
		if( action == null )
			return( null );
		
		ServerTransaction transaction = new ServerTransaction( this , action );
		return( transaction );
	}
	
	public boolean startTransaction( TransactionBase transaction ) {
		if( currentTransaction != null ) {
			try {
				synchronized( currentTransaction ) {
					currentTransaction.wait( META_CHANGE_TIMEOUT );
				}
			}
			catch( Throwable e ) {
				if( serverAction != null )
					serverAction.handle( e );
				else
					System.out.println( e.getMessage() );
			}
		}
		
		currentTransaction = transaction;
		return( true );
	}
	
	public void abortTransaction( TransactionBase transaction ) {
		if( currentTransaction == transaction ) {
			synchronized( currentTransaction ) {
				currentTransaction.notifyAll();
			}
			currentTransaction = null;
		}
	}
	
	public boolean commitTransaction( TransactionBase transaction ) {
		if( currentTransaction != transaction )
			return( false );
		
		synchronized( currentTransaction ) {
			currentTransaction.notifyAll();
		}
		currentTransaction = null;
		return( true );
	}
	
	public TransactionBase getTransaction() {
		return( currentTransaction );
	}

	public ServerAuth getAuth() {
		return( auth );
	}

	public ServerEvents getEvents() {
		return( events );
	}

	public ServerLoader getLoader( ActionInit action ) {
		return( loader );
	}

	public void updatePermissions( ActionBase action , String user ) throws Exception {
		sessionController.updatePermissions( action , user );
	}
	
}
