package org.urm.action;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerSession;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandContext;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.action.CommandOutput;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.Folder;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerBuilders;
import org.urm.meta.engine.ServerContext;
import org.urm.meta.engine.ServerDirectory;
import org.urm.meta.engine.ServerMirrorRepository;
import org.urm.meta.engine.ServerMirrors;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.engine.ServerResources;
import org.urm.meta.engine.ServerSettings;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.Meta.VarBUILDMODE;
import org.urm.meta.product.Meta.VarCATEGORY;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

abstract public class ActionBase extends ActionCore {

	public ActionInit actionInit;
	
	public ServerSession session;
	public CommandExecutor executor;
	public CommandContext context;
	public Artefactory artefactory;
	
	public ShellExecutor shell;
	public CommandOutput output;

	public int commandTimeout;
	
	protected boolean executeSimple() throws Exception { return( false ); };
	protected boolean executeScope( ActionScope scope ) throws Exception { return( false ); };
	protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception { return( false ); };
	protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception { return( false ); };
	protected boolean executeScopeTargetItem( ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception { return( false ); };
	protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception { return( false ); };
	protected void runBefore() throws Exception {};
	protected void runAfter() throws Exception {};
	protected void runBefore( ActionScope scope ) throws Exception {};
	protected void runAfter( ActionScope scope ) throws Exception {};
	protected void runBefore( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {};
	protected void runAfter( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {};
	protected void runBefore( ActionScopeTarget target ) throws Exception {};
	protected void runAfter( ActionScopeTarget target ) throws Exception {};
	protected void runBefore( ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {};
	protected void runAfter( ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {};
	
	public ActionBase( ServerSession session , Artefactory artefactory , CommandExecutor executor , CommandOutput output ) {
		super( executor.engine , null );
		
		this.session = session;
		this.executor = executor;
		this.output = output;
		this.artefactory = artefactory;
		
		commandTimeout = 0;
	}

	public ActionBase( ActionBase base , String stream ) {
		super( base.engine , base );
		
		this.actionInit = base.actionInit;
		
		this.session = base.session;
		this.executor = base.executor;
		this.output = base.output;
		this.artefactory = base.artefactory;
		
		this.shell = base.shell;
		this.commandTimeout = base.commandTimeout;
		
		context = new CommandContext( base.context , stream );
	}

	public void setContext( CommandContext context ) {
		this.context = context;
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

	@Override
	public void handle( Throwable e , String s ) {
		log( s , e );
		super.handle( e , s );
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
		handle( "" , e );
	}

	public synchronized void handle( String prompt , Throwable e ) {
		String s = NAME;
		if( !prompt.isEmpty() )
			s += " " + prompt;
		output.log( context , s , e );
	}
	
	public synchronized void log( String prompt , Throwable e ) {
		output.log( context , prompt , e );
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
		output.logExact( context , s , logLevel );
	}
	
	public void logExactInteractive( String s , int logLevel ) {
		output.logExactInteractive( context , s , logLevel );
	}
	
	public void error( String s ) {
		output.error( context , s );
	}
	
	public void trace( String s ) {
		output.trace( context , s );
	}
	
	public void info( String s ) {
		output.info( context , s );
	}
	
	public void debug( String s ) {
		output.debug( context , s );
	}
	
	public void ifexit( int errorCode , String s , String[] params ) throws Exception {
		if( context.CTX_FORCE )
			error( s + ", ignored" );
		else
			exit( errorCode , s + ", exiting (use -force to override)" , params );
	}

	public boolean runSimple() {
		ScopeExecutor executor = new ScopeExecutor( this );
		if( executor.runSimple() )
			if( !executor.runFailed )
				return( true );
		return( false );
	}
	
	public boolean runAll( ActionScope scope ) {
		ScopeExecutor executor = new ScopeExecutor( this );
		return( executor.runAll( scope ) );
	}
	
	public boolean runAll( ActionScopeSet set ) {
		ScopeExecutor executor = new ScopeExecutor( this );
		return( executor.runAll( set ) );
	}
	
	public boolean runSingleTarget( ActionScopeTarget item ) {
		ScopeExecutor executor = new ScopeExecutor( this );
		if( executor.runSingleTarget( item ) )
			if( !executor.runFailed )
				return( true );
		return( false );
	}
	
	public boolean runTargetList( ActionScopeSet set , ActionScopeTarget[] items ) {
		ScopeExecutor executor = new ScopeExecutor( this );
		if( executor.runTargetList( set , items ) )
			if( !executor.runFailed )
				return( true );
		return( false );
	}
	
	public boolean runCategories( ActionScope scope , VarCATEGORY[] categories ) {
		ScopeExecutor executor = new ScopeExecutor( this );
		if( executor.runCategories( scope , categories ) )
			if( !executor.runFailed )
				return( true );
		return( false );
	}
	
	public boolean runEachBuildableProject( ActionScope scope ) {
		VarCATEGORY[] categories = Meta.getAllBuildableCategories();
		return( runCategories( scope , categories ) );
	}
	
	public boolean runEachSourceProject( ActionScope scope ) {
		VarCATEGORY[] categories = Meta.getAllSourceCategories();
		return( runCategories( scope , categories ) );
	}
	
	public boolean runEachCategoryTarget( ActionScope scope , VarCATEGORY category ) {
		VarCATEGORY[] categories = new VarCATEGORY[] { category };
		return( runCategories( scope , categories ) );
	}
	
	public boolean runEachCoreProject( ActionScope scope ) {
		return( runEachCategoryTarget( scope , VarCATEGORY.BUILD ) );
	}

	public boolean runEachPrebuiltProject( String methodName , ActionScope scope ) {
		return( runEachCategoryTarget( scope , VarCATEGORY.PREBUILT ) );
	}

	public boolean runEnvUniqueHosts( ActionScope scope ) {
		ScopeExecutor executor = new ScopeExecutor( this );
		if( executor.runEnvUniqueHosts( scope ) )
			if( !executor.runFailed )
				return( true );
		return( false );
	}
	
	public boolean runEnvUniqueAccounts( ActionScope scope ) {
		ScopeExecutor executor = new ScopeExecutor( this );
		if( executor.runEnvUniqueAccounts( scope ) )
			if( !executor.runFailed )
				return( true );
		return( false );
	}
	
	public ShellExecutor getShell( Account account ) throws Exception {
		return( engine.shellPool.getExecutor( this , account , context.stream ) );
	}

	public ShellExecutor createDedicatedShell( String name ) throws Exception {
		return( engine.shellPool.createDedicatedLocalShell( this , name ) );
	}
	
	public void killAllDedicated() {
		engine.shellPool.releaseActionPool( this );
	}
	
	public ShellExecutor getShell( MetaEnvServerNode node ) throws Exception {
		Account account = Account.getAccount( this , node.HOSTLOGIN , node.server.osType );
		return( getShell( account ) );
	}
	
	public Account getNodeAccount( MetaEnvServerNode node ) throws Exception {
		return( Account.getAccount( this , node.HOSTLOGIN , node.server.osType ) );
	}
	
	public Account getSingleHostAccount( String host , int port , VarOSTYPE OSTYPE ) throws Exception {
		String user = context.CTX_HOSTUSER;
		if( user.isEmpty() )
			user = "root";
		
		Account account = Account.getAccount( this , user , host , port , OSTYPE );
		return( account );
	}

	public void startRedirect( String title , String logFile ) throws Exception {
		String file = logFile;
		if( file.startsWith( "~/" ) )
			file = shell.getHomePath() + file.substring( 1 ); 
		debug( "start logging to " + file );
		output.createOutputFile( context , title , file );
	}
	
	public void stopRedirect() throws Exception {
		debug( "stop logging to this file." );
		output.stopOutputFile();
	}
	
	public void tee() throws Exception {
		LocalFolder folder = artefactory.getWorkFolder( this );
		String fname = folder.getFilePath( this , "executor.log" );
		output.tee( NAME , fname );
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
	
	public void checkRequired( VarBUILDMODE value , String name ) throws Exception {
		if( value == null || value == VarBUILDMODE.UNKNOWN )
			checkRequired( ( String )null , name );
	}
	
	public void logAction() throws Exception {
		String flags = context.options.getFlagsSet();
		String params = context.options.getParamsSet();
		String args = context.options.getArgsSet();
		
		String log = "command=" + context.options.command + " action=" + context.options.action;
		if( !flags.isEmpty() )
			log += " " + flags;
		if( !params.isEmpty() )
			log += " " + params;
		if( !args.isEmpty() )
			log += " " + args;
		
		info( "run: " + log );
	}

	public void setBuildMode( VarBUILDMODE value ) throws Exception {
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
	
	public void executeCmdLive( Account account , String cmdRun ) throws Exception {
		if( !isExecute() ) {
			info( account.getPrintName() + ": " + cmdRun + " (showonly)" );
			return;
		}

		info( account.getPrintName() + ": " + cmdRun + " (execute)" );
		ShellExecutor shell = getShell( account );
		shell.appendExecuteLog( this , cmdRun );

		shell.customCheckErrorsNormal( this , cmdRun );
	}

	public void executeCmd( Account hostLogin , String cmdRun ) throws Exception {
		ShellExecutor shell = getShell( hostLogin );
		shell.customCheckErrorsDebug( this , cmdRun );
	}

	public String executeCmdGetValue( Account hostLogin , String cmdRun ) throws Exception {
		ShellExecutor shell = getShell( hostLogin );
		return( shell.customGetValue( this , cmdRun ) );
	}

    public void sleep( long millis ) throws Exception {
        trace( "sleep: intentional delay - " + millis + " millis" );
        Thread.sleep(millis);
    }

	public void setLogLevel( int logLevelLimit ) {
		output.setLogLevel( this , logLevelLimit );
	}
    
	public int setTimeout( int timeout ) {
		int saveTimeout = commandTimeout;
		commandTimeout = timeout;
		return( saveTimeout );
	}
    
	public int setTimeoutUnlimited() {
		return( setTimeout( 0 ) );
	}
	
	public int setTimeoutDefault() {
		return( setTimeout( context.CTX_TIMEOUT ) );
	}

	public String getOSPath( String dirPath ) throws Exception {
		return( shell.getOSPath( this , dirPath ) );	
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

	public void commentExecutor( String msg ) {
		String name = "URM " + executor.commandInfo.name + "::" + context.options.action;
		info( name + ": " + msg );
	}

	public boolean isLocalRun() {
		return( context.CTX_LOCAL );
	}

	public boolean isLocalAccount() {
		return( shell.account.local );
	}

	public void stopAllOutputs() throws Exception {
		output.stopAllOutputs();
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
    
    public String getNameAttr( Node node , VarNAMETYPE nameType ) throws Exception {
    	return( Meta.getNameAttr( this , node , nameType ) );
    }

	public void printValues( PropertySet props ) throws Exception {
		for( String prop : props.getRunningKeys() ) {
			String value = props.getPropertyAny( prop );
			info( "property " + prop + "=" + value );
		}
	}

	public ServerResources getResources() {
		return( actionInit.getActiveResources() );
	}
	
	public ServerBuilders getBuilders() {
		return( actionInit.getActiveBuilders() );
	}
	
	public ServerDirectory getDirectory() {
		return( actionInit.getActiveDirectory() );
	}
	
	public ServerSettings getServerSettings() {
		return( actionInit.getActiveServerSettings() );
	}

	public ServerContext getServerContext() {
		return( actionInit.getActiveServerContext() );
	}
	
	public ServerMirrors getMirrors() {
		return( actionInit.getActiveMirrors() );
	}
	
	public MetaProductBuildSettings getBuildSettings( Meta meta ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( this );
		return( product.getBuildSettings( this ) );
	}

	public ServerMirrorRepository getProjectMirror( MetaSourceProject project ) throws Exception {
		ServerMirrors mirrors = getMirrors();
		ServerMirrorRepository repo = mirrors.findProjectRepository( project );
		return( repo );
	}

	public ServerMirrorRepository getMetaMirror( ServerProductMeta meta ) throws Exception {
		ServerMirrors mirrors = getMirrors();
		ServerMirrorRepository repo = mirrors.findProductMetaRepository( meta );
		return( repo );
	}

	public ServerMirrorRepository getConfigurationMirror( ServerProductMeta meta ) throws Exception {
		ServerMirrors mirrors = getMirrors();
		ServerMirrorRepository repo = mirrors.findProductConfigurationRepository( meta );
		return( repo );
	}

	public ServerProjectBuilder getBuilder( String name ) throws Exception {
		ServerBuilders builders = getBuilders();
		ServerProjectBuilder builder = builders.getBuilder( name );
		return( builder );
	}

	public ServerMirrorRepository getServerMirror() throws Exception {
		ServerMirrors mirrors = getMirrors();
		ServerMirrorRepository repo = mirrors.findServerRepository();
		return( repo );
	}
	
	public ServerAuthResource getResource( String name ) throws Exception {
		ServerResources resources = getResources();
		ServerAuthResource res = resources.getResource( name );
		return( res );
	}
	
	public Meta getContextMeta() throws Exception {
		if( !session.product )
			exitUnexpectedState();
		return( getProductMetadata( session.productName ) );
	}

	public Meta getProductMetadata( String productName ) throws Exception {
		return( actionInit.getActiveProductMetadata( productName ) );
	}

	public boolean isProductBroken( String productName ) {
		return( actionInit.isActiveProductBroken( productName ) );
	}
	
	public String getContextRedistPath( Account account ) throws Exception {
		if( account.isLinux() )
			return( context.CTX_REDISTLINUX_PATH );
		return( context.CTX_REDISTWIN_PATH );
	}
	
	public String getProductRedistPath( MetaEnvServer server ) throws Exception {
		MetaProductSettings product = server.meta.getProductSettings( this );
		if( server.isLinux() )
			return( product.CONFIG_REDISTLINUX_PATH );
		return( product.CONFIG_REDISTWIN_PATH );
	}
	
	public String getEnvRedistPath( MetaEnvServer server ) throws Exception {
		if( server.isLinux() )
			return( server.dc.env.REDISTLINUX_PATH );
		return( server.dc.env.REDISTWIN_PATH );
	}
	
}
