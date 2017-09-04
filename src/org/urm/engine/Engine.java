package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.OptionsMeta;
import org.urm.common.jmx.EngineMBean;
import org.urm.common.meta.BuildCommandMeta;
import org.urm.common.meta.DatabaseCommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.common.meta.MainCommandMeta;
import org.urm.common.meta.MonitorCommandMeta;
import org.urm.common.meta.ReleaseCommandMeta;
import org.urm.common.meta.XDocCommandMeta;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.ActionInit.RootActionType;
import org.urm.engine.blotter.EngineBlotter;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.action.CommandMethod;
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
import org.urm.engine.status.EngineStatus;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.ServerLoader;
import org.urm.meta.engine.ServerAuth;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.engine.ServerRegistry;
import org.urm.meta.engine.ServerResources;

public class Engine {

	public RunContext execrc;
	
	public EngineExecutor executor;
	public EngineSession serverSession;
	public SessionController sessionController;
	public EngineMBean jmxController;
	public EngineHouseKeeping houseKeeping;
	
	public EngineCache cache;
	public OptionsMeta optionsMeta;
	public MainExecutor serverExecutor;
	public ActionInit serverAction;
	public ShellPool shellPool;
	
	private ServerAuth auth;
	private EngineEvents events;
	private ServerLoader loader;
	private EngineStatus status;
	public boolean running;

	private TransactionBase currentTransaction = null;

	public EngineBlotter blotter;
	public BuildCommandExecutor buildExecutor;
	public DatabaseCommandExecutor databaseExecutor;
	public DeployCommandExecutor deployExecutor;
	public MonitorCommandExecutor monitorExecutor;
	public ReleaseCommandExecutor releaseExecutor;
	public XDocCommandExecutor xdocExecutor;
	
	public static int META_CHANGE_TIMEOUT = 5000;
	
	public Engine( RunContext execrc ) {
		this.execrc = execrc;
		
		executor = new EngineExecutor( this ); 
		houseKeeping = new EngineHouseKeeping( this );
		cache = new EngineCache( this ); 

		auth = new ServerAuth( this );
		events = new EngineEvents( this );
		loader = new ServerLoader( this );
		sessionController = new SessionController( this );
		status = new EngineStatus( this );
		blotter = new EngineBlotter( this );
		
		optionsMeta = new OptionsMeta();
	}
	
	public void init() throws Exception {
		cache.init();
		auth.init();
		events.init();
		status.init();
		loader.init();
		sessionController.init();
		blotter.init();
		
		buildExecutor = BuildCommandExecutor.createExecutor( this );
		databaseExecutor = DatabaseCommandExecutor.createExecutor( this );
		deployExecutor = DeployCommandExecutor.createExecutor( this );
		monitorExecutor = MonitorCommandExecutor.createExecutor( this );
		releaseExecutor = ReleaseCommandExecutor.createExecutor( this );
		xdocExecutor = XDocCommandExecutor.createExecutor( this );
	}
	
	public void runServer( ActionInit action ) throws Exception {
		serverAction.debug( "load server configuration ..." );
		auth.start( serverAction );
		loader.loadServerProducts( action.actionInit );
		blotter.start( serverAction );
		
		sessionController.start( serverAction );
		
		ServerMonitoring mon = loader.getMonitoring();
		mon.start();
		events.start();
		
		jmxController = new EngineMBean( action , this );
		jmxController.start();
		
		houseKeeping.start();
		
		serverAction.info( "server successfully started, accepting connections." );
		sessionController.waitFinished( serverAction );
	}
	
	public void stopServer() throws Exception {
		if( !running )
			return;
		
		serverAction.info( "stopping server ..." );
		
		houseKeeping.stop();
		
		events.stop();
		ServerMonitoring mon = loader.getMonitoring();
		mon.stop();
		shellPool.stop( serverAction );
		
		jmxController.stop();
		sessionController.stop( serverAction );
		jmxController = null;
		loader.clearServerProducts();
		blotter.clear();
		cache.clear();
		auth.stop( serverAction );
		
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

	public EngineSession createClientSession( SessionSecurity security , RunContext clientrc ) throws Exception {
		EngineSession sessionContext = sessionController.createSession( security , clientrc , true );
		return( sessionContext );
	}
	
	public ActionInit createInteractiveSessionAction( EngineSession session ) throws Exception {
		CommandOptions options = serverExecutor.createOptionsInteractiveSession( this );
		if( options == null )
			return( null );
		
		ActionInit action = createAction( RootActionType.InteractiveSession , options , session , "web" , null , false , "Interactive session id=" + session.sessionId + ", user=" + session.getLoginAuth().USER );
		startAction( action );
		
		return( action );
	}
	
	public ActionInit createTemporaryAction( String name , EngineSession session ) throws Exception {
		CommandOptions options = serverExecutor.createOptionsTemporary( this );
		if( options == null )
			return( null );
		
		ActionInit action = createAction( RootActionType.Temporary , options , session , name , null , true , "Temporary action, session=" + session.sessionId );
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
		serverAction = createAction( RootActionType.Core , options , serverSession , "server" , null , false , "Server instance" );
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
		
		serverAction = createAction( RootActionType.Command , options , serverSession , "client" , null , false , "Run local command=" + commandInfo.name + "::" + options.method );
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
			serverExecutor.runExecutor( serverAction , serverAction.commandAction );
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

	public CommandExecutor getExecutor( String command ) throws Exception {
		if( command.equals( MainCommandMeta.NAME ) )
			return( serverExecutor );
		
		if( command.equals( BuildCommandMeta.NAME ) )
			return( buildExecutor );
		if( command.equals( DatabaseCommandMeta.NAME ) )
			return( databaseExecutor );
		if( command.equals( DeployCommandMeta.NAME ) )
			return( deployExecutor );
		if( command.equals( MonitorCommandMeta.NAME ) )
			return( monitorExecutor );
		if( command.equals( ReleaseCommandMeta.NAME ) )
			return( releaseExecutor );
		if( command.equals( XDocCommandMeta.NAME ) )
			return( xdocExecutor );
		
		Common.exit1( _Error.UnknownCommandExecutor1 , "Unexpected URM args - unknown command executor=" + command + " (expected one of " +
				BuildCommandMeta.NAME + "/" +
				DeployCommandMeta.NAME + "/" + 
				DatabaseCommandMeta.NAME + "/" +
				MonitorCommandMeta.NAME + "/" +
				ReleaseCommandMeta.NAME + "/" +
				XDocCommandMeta.NAME + ")" , command );
		return( null );
	}
	
	public ActionInit createAction( RootActionType type , CommandOptions options , EngineSession session , String stream , EngineCall call , boolean memoryOnly , String actionInfo ) throws Exception {
		CommandExecutor actionExecutor = getExecutor( options.command );
		CommandMethod commandAction = actionExecutor.getAction( options.method );
		if( !options.checkValidOptions( commandAction.method ) )
			return( null );
		
		// create context
		CommandContext context = new CommandContext( this , session , options , stream , call );
		if( !context.setRunContext() )
			return( null );

		// create artefactory
		Artefactory artefactory = createArtefactory( session , context , memoryOnly );
		
		// create action
		ActionInit action = createRootAction( type , actionExecutor , session , artefactory , options.method , memoryOnly , actionInfo );
		action.setContext( context );
		context.update( action );
		actionExecutor.setActionContext( action , context );
		
		if( memoryOnly )
			action.debug( "memory action created: actionId=" + action.ID + ", name=" + action.actionName );
		else
			action.debug( "normal action created: actionId=" + action.ID + ", name=" + action.actionName + ", workfolder=" + artefactory.workFolder.folderPath );
		
		return( action );
	}
	
	public ActionInit createRootAction( RootActionType type , CommandExecutor executor , EngineSession session , Artefactory artefactory , String actionName , boolean memoryOnly , String actionInfo ) throws Exception { 
		CommandOutput output = new CommandOutput();
		CommandMethod commandAction = executor.getAction( actionName );
		ActionInit action = new ActionInit( type , loader , session , artefactory , executor , output , commandAction , commandAction.method.name , memoryOnly , actionInfo );
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
			
			shellPool.releaseActionPool( action );
			if( closeSession ) {
				sessionController.closeSession( action.session );
				action.clearSession();
			}
			
			boolean success = ( action.isFailed() )? false : true;
			blotter.stopAction( action , success );
		}
	}

	private Artefactory createArtefactory( EngineSession session , CommandContext context , boolean memoryOnly ) throws Exception {
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

	public EngineTransaction createTransaction( ActionInit action ) {
		if( action == null )
			return( null );
		
		EngineTransaction transaction = new EngineTransaction( this , action );
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

	public EngineEvents getEvents() {
		return( events );
	}

	public EngineCache getCache() {
		return( cache );
	}

	public ServerLoader getLoader( ActionInit action ) {
		return( loader );
	}

	public ServerResources getResources() {
		ServerRegistry registry = loader.getRegistry();
		return( registry.resources );
	}
	
	public void updatePermissions( ActionBase action , String user ) throws Exception {
		sessionController.updatePermissions( action , user );
	}

	public void log( String prompt , Throwable e ) {
		serverAction.log( prompt , e );
	}
	
	public void handle( Throwable e ) {
		serverAction.handle( "handle exception in action" , e );
	}

	public void handle( String prompt , Throwable e ) {
		serverAction.handle( prompt , e );
	}
	
	public void info( String s ) {
		if( serverAction != null )
			serverAction.info( s );
		else
			System.out.println( "engine (info): " + s );
	}
	
	public void debug( String s ) {
		if( serverAction != null )
			serverAction.debug( s );
		else
			System.out.println( "engine (debug): " + s );
	}
	
	public void error( String s ) {
		if( serverAction != null )
			serverAction.error( s );
		else
			System.out.println( "engine (error): " + s );
	}
	
	public void trace( String s ) {
		if( serverAction != null )
			serverAction.trace( s );
		else
			System.out.println( "engine (trace): " + s );
	}
	
	public void exit1( int e , String s , String p1 ) throws Exception {
		serverAction.exit1( e , s , p1 );
	}
	
}
