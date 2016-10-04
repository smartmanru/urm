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
import org.urm.engine.registry.ServerAuth;
import org.urm.engine.shell.ShellCoreJNI;
import org.urm.engine.shell.ShellPool;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;

public class ServerEngine {

	public RunContext execrc;
	public ServerSession serverSession;
	public SessionController sessionController;
	public ServerMBean jmxController;
	
	public MainExecutor serverExecutor;
	public ActionInit serverAction;
	public ShellPool shellPool;
	
	private ServerAuth auth;
	private ServerLoader loader;
	public boolean running;

	private TransactionBase currentTransaction = null;

	public static int META_CHANGE_TIMEOUT = 5000;
	
	public ServerEngine( RunContext execrc ) {
		this.execrc = execrc;
		
		auth = new ServerAuth( this ); 
		loader = new ServerLoader( this );
		sessionController = new SessionController( this );
	}
	
	public void init() throws Exception {
		auth.init();
		loader.init();
	}
	
	public void runServer( ActionBase action ) throws Exception {
		serverAction.debug( "load server configuration ..." );
		loader.loadServerProducts( action.actionInit );
		
		jmxController = new ServerMBean( action , this );
		sessionController.start( serverAction );
		jmxController.start();
		
		serverAction.info( "server successfully started, accepting connections." );
		sessionController.waitFinished( serverAction );
	}
	
	public void stopServer() throws Exception {
		if( !running )
			return;
		
		serverAction.info( "stopping server ..." );
		
		shellPool.stop( serverAction );
		jmxController.stop();
		sessionController.stop( serverAction );
		jmxController = null;
		loader.clearServerProducts();
		
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
	
	public ActionInit createWebSessionAction() throws Exception {
		CommandOptions options = serverExecutor.createOptionsWebSession( this );
		if( options == null )
			return( null );
		
		RunContext clientrc = RunContext.clone( execrc );
		ServerSession sessionContext = sessionController.createSession( clientrc , true );
		ActionInit action = createAction( serverExecutor , options , sessionContext , "web" , null , false );
		startAction( action );
		
		return( action );
	}
	
	public ActionInit createTemporaryAction( String name ) throws Exception {
		CommandOptions options = serverExecutor.createOptionsTemporary( this );
		if( options == null )
			return( null );
		
		RunContext clientrc = RunContext.clone( execrc );
		ServerSession sessionContext = sessionController.createSession( clientrc , true );
		sessionContext.setServerLayout( null );
		ActionInit action = createAction( serverExecutor , options , sessionContext , name , null , true );
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
		serverSession = sessionController.createSession( execrc , false );
		serverSession.setServerLayout( options );
		
		// create server action
		serverAction = createAction( serverExecutor , options , serverSession , "server" , null , false );
		if( serverAction == null )
			return( false );

		// run server action
		running = true;
		createPool();
		startAction( serverAction );
		
		return( true );
	}

	public boolean runClientMode( CommandOptions options , CommandMeta commandInfo ) throws Exception {
		CommandExecutor commandExecutor = createExecutor( commandInfo );
		serverSession = sessionController.createSession( execrc , false );
		
		if( execrc.isStandalone() )
			serverSession.setStandaloneLayout( options );
		else
			serverSession.setServerLayout( options );
		
		serverAction = createAction( commandExecutor , options , serverSession , "client" , null , false );
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
			Common.exit1( _Error.UnknownCommandExecutor1 , "Unexpected URM args - unknown command executor=" + cmd + " (expected one of build/deploy/database/monitor)" , cmd );
		return( executor );
	}

	public ActionInit createAction( CommandExecutor actionExecutor , CommandOptions options , ServerSession session , String stream , ServerCall call , boolean memoryOnly ) throws Exception {
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
		ActionInit action = createAction( session , artefactory , actionExecutor , options.action , memoryOnly );
		context.update( action );
		actionExecutor.setActionContext( action , context );
		
		if( memoryOnly )
			action.debug( "memory action created: actionId=" + action.ID + ", name=" + action.actionName );
		else
			action.debug( "normal action created: actionId=" + action.ID + ", name=" + action.actionName + ", workfolder=" + artefactory.workFolder.folderPath );
		
		return( action );
	}
	
	public ActionInit createAction( CommandContext context , ActionBase action ) throws Exception {
		ActionInit actionInit = new ActionInit( loader , action.session , action.artefactory , action.executor , action.output , null , null , false );
		actionInit.setContext( context );
		actionInit.setShell( action.shell );
		return( actionInit );
	}
	
	public ActionInit createAction( ServerSession session , Artefactory artefactory , CommandExecutor executor , String actionName , boolean memoryOnly ) throws Exception { 
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
				shellPool.createDedicatedLocalShell( action , action.context.stream + "::" + action.session.sessionId );
			
			// create work folder
			LocalFolder folder = action.artefactory.getWorkFolder( action );
			folder.recreateThis( action );
			
			// start action log
			action.tee();
		}
		
		// print args
		if( action.context.CTX_SHOWALL ) {
			String info = action.context.options.getRunningOptions();
			action.commentExecutor( info );
		}
	}
	
	public void finishAction( ActionInit action ) throws Exception {
		action.stopAllOutputs();
		
		if( !action.isMemoryOnly() ) {
			if( action.isFailed() || action.context.CTX_SHOWALL )
				action.info( "saved work directory: " + action.artefactory.workFolder.folderPath );
			else
				action.artefactory.workFolder.removeThis( action );
			
			shellPool.releaseActionPool( action );
			sessionController.closeSession( action.session );
		}
	}

	private Artefactory createArtefactory( ServerSession session , CommandContext context , boolean memoryOnly ) throws Exception {
		if( memoryOnly )
			return( new Artefactory( null ) );
			
		String dirname = "";
		
		if( session.standalone ) {
			if( !context.CTX_WORKPATH.isEmpty() )
				dirname = context.CTX_WORKPATH;
			else
				dirname = Common.getPath( session.execrc.userHome , "urm.work" , "session-" + ShellCoreJNI.getCurrentProcessId() );
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
					dirname = Common.getPath( session.execrc.userHome , "urm.work" , "client" );
					String name = "session-" + session.timestamp;
					if( !session.productName.isEmpty() )
						name += "-" + session.productName;
					name += "-" + session.sessionId; 
					dirname = Common.getPath( dirname , name );
				}
			}
		}
		
		LocalFolder folder = new LocalFolder( dirname , execrc.isWindows() );
		Artefactory artefactory = new Artefactory( folder );
		return( artefactory );
	}

	public ServerTransaction createTransaction( ActionInit action ) {
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

	public ServerLoader getLoader( ActionInit action ) {
		return( loader );
	}
	
}
