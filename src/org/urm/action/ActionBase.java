package org.urm.action;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import org.urm.action.main.ActionMethod;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunError;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.OptionsMeta;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.BlotterService;
import org.urm.engine.CacheService;
import org.urm.engine.EventService;
import org.urm.engine.ScheduleService;
import org.urm.engine.StateService;
import org.urm.engine.BlotterService.BlotterType;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandContext;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.action.CommandOutput;
import org.urm.engine.blotter.EngineBlotterSet;
import org.urm.engine.cache.EngineCacheObject;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineBuilders;
import org.urm.engine.data.EngineContext;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.data.EngineResources;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductReleases;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.security.AuthResource;
import org.urm.engine.security.AuthUser;
import org.urm.engine.session.EngineSession;
import org.urm.engine.session.SessionSecurity;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.BaseRepository;
import org.urm.engine.storage.Folder;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.loader.Types.*;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.system.AppProduct;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

abstract public class ActionBase extends ActionCore {

	public ActionInit actionInit;
	
	public EngineSession session;
	public CommandExecutor executor;
	public CommandContext context;
	public EngineMethod method;
	public Artefactory artefactory;
	
	public ShellExecutor shell;
	public CommandOutput output;
	public int outputChannel;
	public ScopeExecutor scopeExecutor;

	protected SCOPESTATE executeSimple( ScopeState state ) throws Exception { return( SCOPESTATE.NotRun ); };
	protected SCOPESTATE executeScope( ScopeState state , ActionScope scope ) throws Exception { return( SCOPESTATE.NotRun ); };
	protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception { return( SCOPESTATE.NotRun ); };
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception { return( SCOPESTATE.NotRun ); };
	protected SCOPESTATE executeScopeTargetItem( ScopeState state , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception { return( SCOPESTATE.NotRun ); };
	protected SCOPESTATE executeAccount( ScopeState state , ActionScopeSet set , Account account ) throws Exception { return( SCOPESTATE.NotRun ); };
	protected void runBefore( ScopeState state ) throws Exception {};
	protected void runAfter( ScopeState state ) throws Exception {};
	protected void runBefore( ScopeState state , ActionScope scope ) throws Exception {};
	protected void runAfter( ScopeState state , ActionScope scope ) throws Exception {};
	protected void runBefore( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {};
	protected void runAfter( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {};
	protected void runBefore( ScopeState state , ActionScopeTarget target ) throws Exception {};
	protected void runAfter( ScopeState state , ActionScopeTarget target ) throws Exception {};
	protected void runBefore( ScopeState state , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {};
	protected void runAfter( ScopeState state , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {};
	
	public ActionBase( EngineSession session , Artefactory artefactory , CommandExecutor executor , CommandOutput output , String actionInfo ) {
		super( executor.engine , null , actionInfo );
		
		this.session = session;
		this.executor = executor;
		this.output = output;
		this.outputChannel = -1;
		this.artefactory = artefactory;
	}

	public ActionBase( ActionBase base , String stream , String actionInfo ) {
		super( base.engine , base , actionInfo );
		
		this.actionInit = base.actionInit;
		
		this.session = base.session;
		this.executor = base.executor;
		this.output = base.output;
		this.outputChannel = base.outputChannel;
		this.method = base.method;
		this.artefactory = base.artefactory;
		
		this.shell = base.shell;
		
		context = new CommandContext( this , base.context , stream );
	}

	@Override
	public void handle( Throwable e , String s ) {
		log( s , e );
		super.handle( e , s );
	}

	@Override
	public void fail( int errorCode , String s , String[] params ) {
		error( s );
		RunError error = new RunError( errorCode , s , params );
		fail( error );
	}

	@Override
	public void stopExecution() {
		if( scopeExecutor != null )
			scopeExecutor.stopExecution();
	}
	
	public String getUserName() {
		if( session == null )
			return( "" );
		SessionSecurity security = session.getSecurity();
		if( security == null )
			return( "" );
		AuthUser user = security.getUser();
		return( user.NAME );
	}
	
	public Integer getUserId() {
		if( session == null )
			return( null );
		SessionSecurity security = session.getSecurity();
		if( security == null )
			return( null );
		AuthUser user = security.getUser();
		return( user.ID );
	}
	
	public void setContext( CommandContext context ) {
		this.context = context;
	}
	
	public void setMethod( EngineMethod method ) {
		this.method = method;
	}
	
	public EngineMethod getMethod() {
		return( method );
	}
	
	public void setShell( ShellExecutor session ) throws Exception {
		this.shell = session;
	}

	public Account getLocalAccount() {
		return( shell.account );
	}
	
	public LocalFolder getLocalFolder( String path ) throws Exception {
		return( new LocalFolder( Common.getLinuxPath( path ) , isLocalWindows() ) );
	}

	public RemoteFolder getRemoteFolder( Account account , String folderPath ) {
		return( new RemoteFolder( account , Common.getLinuxPath( folderPath ) ) );
	}

	public boolean isForced() {
		if( context.CTX_FORCE )
			return( true );
		return( false );
	}
	
	public boolean continueRun() {
		if( !super.isFailed() )
			return( true );
		if( context.CTX_FORCE )
			return( true );
		return( false );
	}
	
	public String getMode() {
		if( isExecute() )
			return( "execute" );
		else
			return( "showonly" );
	}

	public boolean isExecute() {
		return( ( context.CTX_SHOWONLY )? false : true );
	}

	public void handle( Throwable e ) {
		handle( "handle exception in action" , e );
	}

	public synchronized void handle( String prompt , Throwable e ) {
		String s = NAME;
		if( !prompt.isEmpty() )
			s += " " + prompt;
		output.log( context , outputChannel , s , e );
		if( e instanceof RunError )
			fail( ( RunError )e );
		else
			fail1( _Error.InternalActionError1 , "Internal action error: " + s , s );
	}
	
	public synchronized void log( String prompt , Throwable e ) {
		output.log( context , outputChannel , prompt , e );
	}
	
	public void infoAction( String s ) {
		info( this.getClass().getSimpleName() + ": " + s );
	}

	public void errorAction( String s ) {
		error( this.getClass().getSimpleName() + ": " + s );
	}

	public boolean isDebug() {
		return( context.CTX_SHOWALL );
	}
	
	public boolean isTrace() {
		return( context.CTX_TRACE );
	}
	
	public void logExact( String s , int logLevel ) {
		output.logExact( context , outputChannel , s , logLevel );
	}
	
	public void logExactInteractive( String s , int logLevel ) {
		output.logExactInteractive( context , outputChannel , s , logLevel );
	}
	
	public int logStartCapture() {
		return( context.logStartCapture() );
	}
	
	public String[] logFinishCapture( int startIndex ) {
		return( context.logFinishCapture( startIndex ) );
	}
	
	public void error( String s ) {
		output.error( context , outputChannel , s );
	}
	
	public void trace( String s ) {
		output.trace( context , outputChannel , s );
	}
	
	public void info( String s ) {
		output.info( context , outputChannel , s );
	}
	
	public void debug( String s ) {
		output.debug( context , outputChannel , s );
	}
	
	public void ifexit( int errorCode , String s , String[] params ) throws Exception {
		if( context.CTX_FORCE )
			error( s + ", ignored" );
		else
			exit( errorCode , s + ", exiting (use -force to override)" , params );
	}

	public void ifexit0( int errorCode , String s ) throws Exception {
		ifexit( errorCode , s , null );
	}

	public void ifexit1( int errorCode , String s , String param1 ) throws Exception {
		ifexit( errorCode , s , new String[] { param1 } );
	}

	public void ifexit2( int errorCode , String s , String param1 , String param2 ) throws Exception {
		ifexit( errorCode , s , new String[] { param1 , param2 } );
	}

	public void ifexit3( int errorCode , String s , String param1 , String param2 , String param3 ) throws Exception {
		ifexit( errorCode , s , new String[] { param1 , param2 , param3 } );
	}

	public void ifexit4( int errorCode , String s , String param1 , String param2 , String param3 , String param4 ) throws Exception {
		ifexit( errorCode , s , new String[] { param1 , param2 , param3 , param4 } );
	}

	public boolean runSimpleServer( ScopeState parentState , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , null );
		return( executor.runSimpleServer( sa , readOnly ) );
	}

	public boolean runSimpleServerAsync( ScopeState parentState , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , null );
		return( executor.runSimpleServer( sa , readOnly ) );
	}

	public boolean runSimpleProduct( ScopeState parentState , Meta meta , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , meta );
		return( executor.runSimpleProduct( sa , readOnly ) );
	}

	public boolean runSimpleProductAsync( ScopeState parentState , Meta meta , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , meta );
		return( executor.runSimpleProduct( sa , readOnly ) );
	}

	public boolean runProductBuild( ScopeState parentState , Meta meta , SecurityAction sa , DBEnumBuildModeType mode , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , meta );
		return( executor.runProductBuild( sa , mode , readOnly ) );
	}
	
	public boolean runProductBuildAsync( ScopeState parentState , Meta meta , SecurityAction sa , DBEnumBuildModeType mode , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , meta );
		return( executor.runProductBuild( sa , mode , readOnly ) );
	}
	
	public boolean runSimpleEnv( ScopeState parentState , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , env.meta );
		return( executor.runSimpleEnv( env , sa , readOnly ) );
	}

	public boolean runSimpleEnvAsync( ScopeState parentState , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , env.meta );
		return( executor.runSimpleEnv( env , sa , readOnly ) );
	}

	public boolean runAll( ScopeState parentState , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , scope.meta );
		return( executor.runAll( scope , env , sa , readOnly ) );
	}
	
	public boolean runAllAsync( ScopeState parentState , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , scope.meta );
		return( executor.runAll( scope , env , sa , readOnly ) );
	}
	
	public boolean runAll( ScopeState parentState , ActionScopeSet set , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , set.meta );
		return( executor.runAll( set , env , sa , readOnly ) );
	}
	
	public boolean runAllAsync( ScopeState parentState , ActionScopeSet set , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , set.meta );
		return( executor.runAll( set , env , sa , readOnly ) );
	}
	
	public boolean runSingleTarget( ScopeState parentState , ActionScopeTarget item , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , item.meta );
		return( executor.runSingleTarget( item , env , sa , readOnly ) );
	}
	
	public boolean runSingleTargetAsync( ScopeState parentState , ActionScopeTarget item , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , item.meta );
		return( executor.runSingleTarget( item , env , sa , readOnly ) );
	}
	
	public boolean runTargetList( ScopeState parentState , ActionScopeSet set , ActionScopeTarget[] items , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , set.meta );
		return( executor.runTargetList( set , items , env , sa , readOnly ) );
	}
	
	public boolean runTargetListAsync( ScopeState parentState , ActionScopeSet set , ActionScopeTarget[] items , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , set.meta );
		return( executor.runTargetList( set , items , env , sa , readOnly ) );
	}
	
	public boolean runCategories( ScopeState parentState , ActionScope scope , DBEnumScopeCategoryType[] categories , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , scope.meta );
		return( executor.runCategories( scope , categories , sa , readOnly ) );
	}
	
	public boolean runCategoriesAsync( ScopeState parentState , ActionScope scope , DBEnumScopeCategoryType[] categories , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , scope.meta );
		return( executor.runCategories( scope , categories , sa , readOnly ) );
	}
	
	public boolean runEnvUniqueHosts( ScopeState parentState , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , scope.meta );
		return( executor.runEnvUniqueHosts( scope , env , sa , readOnly ) );
	}
	
	public boolean runEnvUniqueHostsAsync( ScopeState parentState , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , scope.meta );
		return( executor.runEnvUniqueHosts( scope , env , sa , readOnly ) );
	}
	
	public boolean runEnvUniqueAccounts( ScopeState parentState , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , false , scope.meta );
		return( executor.runEnvUniqueAccounts( scope , env , sa , readOnly ) );
	}
	
	public boolean runEnvUniqueAccountsAsync( ScopeState parentState , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) {
		ScopeExecutor executor = new ScopeExecutor( parentState , this , true , scope.meta );
		return( executor.runEnvUniqueAccounts( scope , env , sa , readOnly ) );
	}
	
	protected boolean runCustomTarget( ScopeState parentState , ActionScopeTarget target ) {
		return( scopeExecutor.runCustomTarget( parentState , target ) );
	}
	
	public boolean runEachBuildableProject( ScopeState parentState , ActionScope scope , SecurityAction sa , boolean readOnly ) {
		DBEnumScopeCategoryType[] categories = { DBEnumScopeCategoryType.SEARCH_SOURCEBUILDABLE };
		return( runCategories( parentState , scope , categories , sa , readOnly ) );
	}
	
	public boolean runEachSourceProject( ScopeState parentState , ActionScope scope , SecurityAction sa , boolean readOnly ) {
		DBEnumScopeCategoryType[] categories = DBEnumScopeCategoryType.getAllSourceCategories();
		return( runCategories( parentState, scope , categories , sa , readOnly ) );
	}
	
	public boolean runEachCategoryTarget( ScopeState parentState , ActionScope scope , DBEnumScopeCategoryType category , SecurityAction sa , boolean readOnly ) {
		DBEnumScopeCategoryType[] categories = new DBEnumScopeCategoryType[] { category };
		return( runCategories( parentState , scope , categories , sa , readOnly ) );
	}
	
	public boolean runEachPrebuiltProject( ScopeState parentState , String methodName , ActionScope scope , SecurityAction sa , boolean readOnly ) {
		return( runEachCategoryTarget( parentState , scope , DBEnumScopeCategoryType.SEARCH_SOURCEPREBUILT , sa , readOnly ) );
	}

	public ShellExecutor getShell( Account account ) throws Exception {
		return( engine.shellPool.getExecutor( this , account , context.stream ) );
	}

	public ShellExecutor getShell( RemoteFolder folder ) throws Exception {
		return( getShell( folder.account ) );
	}

	public ShellExecutor createDedicatedShell( String name ) throws Exception {
		return( engine.shellPool.createDedicatedLocalShell( this , name ) );
	}
	
	public ShellExecutor createDedicatedRemoteShell( String name , Account account , boolean setAction ) throws Exception {
		EngineResources res = getEngineResources();
		AuthResource ar = res.getResource( account.AUTHRESOURCE_ID );
		ar.loadAuthData();
		return( engine.shellPool.createDedicatedRemoteShell( this , name , account , ar , setAction ) );
	}
	
	public void killAllDedicated() {
		engine.shellPool.releaseActionPool( this );
	}
	
	public ShellExecutor getShell( MetaEnvServerNode node ) throws Exception {
		Account account = getNodeAccount( node );
		return( getShell( account ) );
	}
	
	public Account getNodeAccount( MetaEnvServerNode node ) throws Exception {
		HostAccount hostAccount = node.getHostAccount();
		return( Account.getHostAccount( hostAccount ) );
	}
	
	public Account getSingleHostAccount( Datacenter dc , String host , int port , DBEnumOSType OSTYPE ) throws Exception {
		String user = context.CTX_HOSTUSER;
		if( user.isEmpty() )
			user = "root";
		
		Account account = Account.getDatacenterAccount( dc , user , host , port , OSTYPE );
		return( account );
	}

	public void startExecutor( ScopeExecutor scopeExecutor , ScopeState state ) throws Exception {
		this.scopeExecutor = scopeExecutor;
		eventSource.setRootState( state );
		engine.blotter.startAction( this );
	}
	
	public void startRedirect( String title , String logFile ) throws Exception {
		String file = logFile;
		if( file.startsWith( "~/" ) )
			file = shell.getHomePath() + file.substring( 1 );
		
		String path = shell.getLocalPath( file );
		String msg = "logging started to " + path;
		outputChannel = output.startRedirect( context , outputChannel , file , msg , title );
	}
	
	public void stopRedirect() throws Exception {
		debug( "logging stopped." );
		outputChannel = output.stopRedirect( outputChannel );
	}
	
	public void redirectTS( String title , String dir , String basename , String ext ) throws Exception {
		String fname = dir + "/" + output.getTimeStampedName( basename , ext );
		startRedirect( title , fname );
	}
	
	public void checkRequired( String value , String var ) throws Exception {
		if( value == null || value.isEmpty() )
			exit1( _Error.RequiredVariable1 , var + " is empty" , var );
	}
	
	public void checkRequired( boolean value , String var ) throws Exception {
		if( value == false )
			checkRequired( ( String )null , var );
	}
	
	public void checkRequired( DBEnumBuildModeType value , String name ) throws Exception {
		if( value == null || value == DBEnumBuildModeType.UNKNOWN )
			checkRequired( ( String )null , name );
	}
	
	public void logAction() throws Exception {
		String flags = context.options.getFlagsSet();
		String params = context.options.getParamsSet();
		String args = context.options.getArgsSet();
		
		String log = "command=" + context.options.command + " action=" + context.options.method;
		if( !flags.isEmpty() )
			log += " " + flags;
		if( !params.isEmpty() )
			log += " " + params;
		if( !args.isEmpty() )
			log += " " + args;
		
		info( "run: " + log );
	}

	public void setBuildMode( DBEnumBuildModeType value ) throws Exception {
		if( value == DBEnumBuildModeType.UNKNOWN )
			super.exit0( _Error.MissingBuildMode0 , "Missing build mode" );
		context.setBuildMode( value );
	}

	public void executeLogLive( ShellExecutor shell , String msg ) throws Exception {
		if( !isExecute() ) {
			info( shell.name + ": " + msg + " (showonly)" );
			return;
		}

		info( shell.name + ": " + msg + " (execute)" );
		shell.appendExecuteLog( this , msg );
	}
	
	public void executeLogLive( Account account , String msg ) throws Exception {
		String loc = account.getPrintName();
		
		ShellExecutor shell = getShell( account );
		if( !isExecute() ) {
			info( loc + ": " + msg + " (showonly)" );
			return;
		}

		info( loc + ": " + msg + " (execute)" );
		shell.appendExecuteLog( this , msg );
	}
	
	public void executeCmdLive( Account account , String cmdRun , int commandTimeoutMillis ) throws Exception {
		if( !isExecute() ) {
			info( account.getPrintName() + ": " + cmdRun + " (showonly)" );
			return;
		}

		info( account.getPrintName() + ": " + cmdRun + " (execute)" );
		ShellExecutor shell = getShell( account );
		shell.appendExecuteLog( this , cmdRun );

		shell.customCheckErrorsNormal( this , cmdRun , commandTimeoutMillis );
	}

	public void executeCmd( Account hostLogin , String cmdRun , int commandTimeoutMillis ) throws Exception {
		ShellExecutor shell = getShell( hostLogin );
		shell.customCheckErrorsDebug( this , cmdRun , commandTimeoutMillis );
	}

	public String executeCmdGetValue( Account hostLogin , String cmdRun , int commandTimeoutMillis ) throws Exception {
		ShellExecutor shell = getShell( hostLogin );
		return( shell.customGetValue( this , cmdRun , commandTimeoutMillis ) );
	}

    public void sleep( long millis ) throws Exception {
        trace( "sleep: intentional delay - " + millis + " millis" );
        Thread.sleep(millis);
    }

	public void setLogLevel( int logLevelLimit ) {
		output.setLogLevel( this , logLevelLimit );
	}
    
	public boolean isLocalWindows() {
		return( context.account.isWindows() );
	}

	public boolean isLocalLinux() {
		return( context.account.isLinux() );
	}

	public boolean isShellWindows() {
		return( shell.isWindows() );
	}

	public boolean isShellLinux() {
		return( shell.isLinux() );
	}

	public String getFormalName() {
		return( executor.commandInfo.name + "::" + context.options.method );
	}
	
	public void commentExecutor( String msg ) {
		String name = "URM " + getFormalName();
		info( name + ": " + msg );
	}

	public boolean isLocalRun() {
		return( context.CTX_LOCAL );
	}

	public String getTmpFilePath( String name ) throws Exception {
		if( shell.account.local )
			return( getWorkFilePath( name ) );
		String path = ( shell.isLinux() )? context.CTX_REDISTLINUX_PATH : context.CTX_REDISTWIN_PATH;
		return( Common.getPath( path , "tmp" , name ) );
	}

	public Folder getTmpFolder( String folder ) throws Exception {
		if( shell.account.local )
			return( new LocalFolder( getWorkFilePath( folder ) , isLocalWindows() ) );
		RedistStorage redist = artefactory.getRedistStorage( this , shell.account );
		RemoteFolder rf = redist.getRedistTmpFolder( this );
		return( rf.getSubFolder( this , folder ) );
	}

	public LocalFolder getWorkFolder() {
		return( artefactory.workFolder );
	}
	
	public LocalFolder getWorkFolder( String subFolder ) throws Exception {
		return( artefactory.workFolder.getSubFolder( this , subFolder ) );
	}
	
	public String getWorkFilePath( String name ) throws Exception {
		String path = artefactory.workFolder.getFilePath( this , name );
		return( path );
	}
	
	public ShellExecutor getLocalShell() throws Exception {
		return( getShell( context.account ) );
	}
	
	public Dist getReleaseDist( AppProduct product , String RELEASELABEL ) throws Exception {
		EngineProduct ep = product.getEngineProduct();
		EngineProductReleases releases = ep.getReleases();
		return( releases.getDistByLabel( this , null , RELEASELABEL ) );
	}
	
	public Dist getMasterDist( AppProduct product ) throws Exception {
		EngineProduct ep = product.getEngineProduct();
		EngineProductReleases releases = ep.getReleases();
		return( releases.getDistByLabel( this , null , ReleaseLabelInfo.LABEL_MASTER ) );
	}
	
	public String readFile( String path ) throws Exception {
    	trace( "read file path=" + path + " ..." );
		return( ConfReader.readFile( engine.execrc , path ) );
	}
	
	public Document readXmlFile( String path ) throws Exception {
    	trace( "read xml file path=" + path + " ..." );
		return( ConfReader.readXmlFile( engine.execrc , path ) );
	}
	
    public Properties readPropertyFile( String path ) throws Exception {
    	trace( "read property file path=" + path + " ..." );
    	return( ConfReader.readPropertyFile( engine.execrc , path ) );
    }

    public List<String> readFileLines( String path ) throws Exception {
    	trace( "read file lines path=" + path + " ..." );
    	return( ConfReader.readFileLines( engine.execrc , path ) );
    }
    
	public List<String> readFileLines( String path , Charset charset ) throws Exception {
    	trace( "read file lines path=" + path + ", charset=" + charset.name() + " ..." );
		return( ConfReader.readFileLines( engine.execrc , path , charset ) );
	}

    public String readStringFile( String path ) throws Exception {
    	trace( "read string file path=" + path + " ..." );
    	return( ConfReader.readStringFile( engine.execrc , path ) );
    }
    
    public String getNameAttr( Node node , EnumNameType nameType ) throws Exception {
    	return( Meta.getNameAttr( this , node , nameType ) );
    }

	public void printValues( ObjectProperties ops ) throws Exception {
		printValues( ops.getProperties() );
	}
    
	public void printValues( PropertySet props ) throws Exception {
		for( String prop : props.getRunningKeys() ) {
			String value = props.getPropertyAny( prop );
			info( "property " + prop + "=" + value );
		}
	}

	public EngineCacheObject getCacheObject( String group , String item ) {
		CacheService cache = engine.getCache();
		return( cache.getObject( group , item ) );
	}
	
	public EngineCacheObject getProductCacheObject( String item ) {
		return( getCacheObject( "product" , item ) );
	}
	
	public EngineCacheObject getCacheObject( EngineObject object ) {
		if( object instanceof AppProduct ) {
			AppProduct xo = ( AppProduct )object; 
			return( getProductCacheObject( "" + xo.ID ) );
		}
		if( object instanceof Meta ) {
			Meta xo = ( Meta )object; 
			ProductMeta storage = xo.getStorage();
			return( getProductCacheObject( "" + storage.ID ) );
		}
		if( object instanceof MetaEnv ) {
			MetaEnv xo = ( MetaEnv )object; 
			return( getProductCacheObject( "" + xo.ID ) );
		}
		if( object instanceof MetaEnvSegment ) {
			MetaEnvSegment xo = ( MetaEnvSegment )object;
			return( getProductCacheObject( "" + xo.ID ) );
		}
		if( object instanceof MetaEnvServer ) {
			MetaEnvServer xo = ( MetaEnvServer )object; 
			return( getProductCacheObject( "" + xo.ID ) );
		}
		if( object instanceof MetaEnvServerNode ) {
			MetaEnvServerNode xo = ( MetaEnvServerNode )object; 
			return( getProductCacheObject( "" + xo.ID ) );
		}
		return( null );
	}
	
	public EngineEntities getEngineEntities() {
		return( actionInit.getActiveEntities() );
	}
	
	public EngineResources getEngineResources() {
		return( actionInit.getActiveResources() );
	}
	
	public EngineBuilders getEngineBuilders() {
		return( actionInit.getActiveBuilders() );
	}
	
	public EngineDirectory getEngineDirectory() {
		return( actionInit.getActiveDirectory() );
	}
	
	public EngineSettings getEngineSettings() {
		return( actionInit.getActiveServerSettings() );
	}

	public EngineContext getEngineContext() {
		return( actionInit.getActiveServerContext() );
	}
	
	public EngineMirrors getEngineMirrors() {
		return( actionInit.getActiveMirrors() );
	}
	
	public EngineBase getEngineBase() {
		return( actionInit.getEngineBase() );
	}
	
	public StateService getEngineStatus() {
		return( actionInit.getEngineStatus() );
	}
	
	public ScheduleService getEngineScheduler() {
		return( actionInit.getEngineScheduler() );
	}
	
	public EngineInfrastructure getEngineInfrastructure() {
		return( actionInit.getEngineInfrastructure() );
	}
	
	public EngineLifecycles getEngineLifecycles() {
		return( actionInit.getEngineLifecycles() );
	}
	
	public EngineMonitoring getEngineMonitoring() {
		return( actionInit.getEngineMonitoring() );
	}
	
	public EngineProduct getEngineProduct( String name ) throws Exception {
		AppProduct product = getProduct( name );
		return( product.getEngineProduct() );
	}
	
	public EngineProduct findEngineProduct( String name ) {
		AppProduct product = findProduct( name );
		if( product == null )
			return( null );
		return( product.findEngineProduct() );
	}
	
	public EngineProduct getEngineProduct( int id ) throws Exception {
		AppProduct product = getProduct( id );
		return( product.getEngineProduct() );
	}
	
	public AppProduct getProduct( String name ) throws Exception {
		EngineDirectory directory = getEngineDirectory();
		return( directory.getProduct( name ) );
	}
	
	public AppProduct getProduct( int id ) throws Exception {
		EngineDirectory directory = getEngineDirectory();
		return( directory.getProduct( id ) );
	}
	
	public AppProduct findProduct( String name ) {
		EngineDirectory directory = getEngineDirectory();
		return( directory.findProduct( name ) );
	}
	
	public MetaProductBuildSettings getBuildSettings( Meta meta ) throws Exception {
		MetaProductSettings product = meta.getProductSettings();
		return( product.getBuildSettings( this ) );
	}

	public MirrorRepository getProjectMirror( MetaSourceProject project ) throws Exception {
		EngineMirrors mirrors = getEngineMirrors();
		MirrorRepository repo = mirrors.findProjectRepository( project );
		return( repo );
	}

	public MirrorRepository getMetaMirror( ProductMeta meta ) throws Exception {
		EngineMirrors mirrors = getEngineMirrors();
		MirrorRepository repo = mirrors.findProductMetaRepository( meta.NAME );
		return( repo );
	}

	public MirrorRepository getConfigurationMirror( ProductMeta meta ) throws Exception {
		EngineMirrors mirrors = getEngineMirrors();
		MirrorRepository repo = mirrors.findProductDataRepository( meta.NAME );
		if( repo == null )
			exit0( _Error.MissingMirrorConfig0 , "Missing product configuration files mirror" );
		
		return( repo );
	}

	public ProjectBuilder getBuilder( MatchItem match ) throws Exception {
		if( match == null )
			return( null );
		
		EngineBuilders builders = getEngineBuilders();
		if( match.FKID != null )
			return( builders.getBuilder( match.FKID ) );
		return( builders.getBuilder( match.FKNAME ) );
	}

	public BlotterService getServerBlotter() throws Exception {
		return( engine.blotter );
	}
	
	public MirrorRepository getServerMirror() throws Exception {
		EngineMirrors mirrors = getEngineMirrors();
		MirrorRepository repo = mirrors.findServerRepository();
		return( repo );
	}
	
	public AuthResource getResource( String name ) throws Exception {
		EngineResources resources = getEngineResources();
		AuthResource res = resources.getResource( name );
		return( res );
	}
	
	public AuthResource getResource( Integer id ) throws Exception {
		EngineResources resources = getEngineResources();
		AuthResource res = resources.getResource( id );
		return( res );
	}
	
	public AppProduct getContextProduct() throws Exception {
		if( context.product != null )
			return( context.product );
		
		if( !session.product )
			exitUnexpectedState();
		return( getProduct( session.productName ) );
	}

	public Meta getContextMeta() throws Exception {
		if( context.meta != null )
			return( context.meta );
		
		AppProduct product = getContextProduct();
		EngineProduct ep = product.getEngineProduct();
		if( ep == null )
			return( null );
		
		EngineProductRevisions revisions = ep.getRevisions();
		ProductMeta storage = revisions.getDraftRevision();
		if( storage == null )
			return( null );
		
		return( ep.getSessionMeta( this , storage , false ) );
	}

	public boolean isProductOffline( Meta meta ) {
		return( isProductOffline( meta.name ) );
	}
	
	public boolean isProductOffline( String productName ) {
		AppProduct product = findProduct( productName );
		if( product == null )
			return( true );
		
		if( product.isOffline() )
			return( true );
		return( false );
	}
	
	public boolean isEnvOffline( MetaEnv env ) {
		if( env.OFFLINE )
			return( true );
		if( !isProductOffline( env.meta.name ) )
			return( false );
		return( false );
	}

	public boolean isSegmentOffline( MetaEnvSegment sg ) {
		if( sg.OFFLINE )
			return( true );
		return( isEnvOffline( sg.env ) );
	}
	
	public boolean isServerOffline( MetaEnvServer server ) {
		if( server.OFFLINE )
			return( true );
		return( isSegmentOffline( server.sg ) );
	}
	
	public boolean isServerNodeOffline( MetaEnvServerNode node ) {
		if( node.OFFLINE )
			return( true );
		return( isServerOffline( node.server ) );
	}
	
	public String getContextRedistPath( Account account ) {
		if( account.isLinux() )
			return( context.CTX_REDISTLINUX_PATH );
		return( context.CTX_REDISTWIN_PATH );
	}
	
	public String getProductRedistPath( MetaEnvServer server ) {
		MetaProductSettings product = server.meta.getProductSettings();
		MetaProductCoreSettings core = product.getCoreSettings();
		if( server.isLinux() )
			return( core.CONFIG_REDISTLINUX_PATH );
		return( core.CONFIG_REDISTWIN_PATH );
	}
	
	public String getEnvRedistPath( MetaEnvServer server ) throws Exception {
		if( server.isLinux() )
			return( server.sg.env.REDISTLINUX_PATH );
		return( server.sg.env.REDISTWIN_PATH );
	}

	public BaseRepository getBaseRepository() throws Exception {
		return( artefactory.getBaseRepository( this ) );
	}

	public EngineBlotterSet getBlotter( BlotterType type ) {
		return( engine.blotter.getBlotterSet( type ) );
	}
	
	public RunError runNotifyMethod( ScopeState parentState , int subMethod , Object subData , EngineEventsApp app , EngineEventsListener listener , Meta meta , MetaEnv env , MetaEnvSegment sg , String command , String method , String[] args , CommandOptions options , boolean async ) {
		try {
			CommandExecutor executor = engine.getExecutor( command );
			options.setMethod( command , method );
			options.setArgs( args );
	
			ActionMethod action = new ActionMethod( this , null , meta , executor , options );
			action.context.env = env;
			action.context.sg = sg;
			
			app.subscribe( action.eventSource , listener , subMethod , subData );
			if( async )
				action.runSimpleServerAsync( parentState , SecurityAction.ACTION_EXECUTE , true );
			else {
				action.runSimpleServer( parentState , SecurityAction.ACTION_EXECUTE , true );
				EventService events = engine.getEvents();
				events.waitDelivered( action.eventSource );
			}
			
			if( action.isOK() )
				return( null );
			
			return( action.getError() );
		}
		catch( Throwable e ) {
			log( "method " + super.NAME , e );
			return( new RunError( _Error.InternalError0 , "Internal action error" , new String[0] ) );
		}
	}

	public void updateOptionDefaults( CommandOptions options , Meta meta , MetaEnv env ) {
		if( env != null ) {
			if( env.isProd() )
				options.setFlag( OptionsMeta.OPT_BACKUP , true );
		}
	}

	public void updateContext( AppProduct product , Meta meta ) throws Exception {
		context.update( this , product , meta );
	}
	
}
