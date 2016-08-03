package org.urm.server.action;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.ServerEngine;
import org.urm.server.SessionContext;
import org.urm.server.custom.CommandCustom;
import org.urm.server.meta.FinalMetaProduct;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.Metadata;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.meta.Metadata.VarCATEGORY;
import org.urm.server.meta.Metadata.VarNAMETYPE;
import org.urm.server.meta.Metadata.VarSERVERTYPE;
import org.urm.server.shell.Account;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.Artefactory;
import org.urm.server.storage.Folder;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.RedistStorage;
import org.urm.server.storage.RemoteFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

abstract public class ActionBase {

	public SessionContext session;
	public ServerEngine engine;
	public CommandExecutor executor;
	public CommandContext context;
	public Artefactory artefactory;
	public CommandCustom custom;
	
	public ShellExecutor shell;
	public Metadata meta;
	protected CommandOutput output;
	boolean actionFailed;

	private static int instanceSequence = 0;
	
	public int ID;
	public String NAME;
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
	
	public ActionBase( SessionContext session , Artefactory artefactory , CommandExecutor executor , CommandContext context , CommandOutput output ) {
		ID = instanceSequence++;
		
		this.session = session;
		this.executor = executor;
		this.output = output;
		this.artefactory = artefactory;
		
		this.context = context;
		
		custom = new CommandCustom( meta );
		engine = executor.engine;
		meta = context.meta;
		
		commandTimeout = 0;
		actionFailed = false;
		NAME = this.getClass().getSimpleName();
	}

	public ActionBase( ActionBase base , String stream ) {
		ID = instanceSequence++;
		
		this.session = base.session;
		this.executor = base.executor;
		this.output = base.output;
		this.artefactory = base.artefactory;
		
		this.custom = base.custom;
		this.engine = base.engine;
		this.meta = base.meta;
		
		this.shell = base.shell;
		this.commandTimeout = base.commandTimeout;
		
		context = new CommandContext( base.context , stream );
		NAME = this.getClass().getSimpleName();
		actionFailed = false;
	}

	public void setServerSystemProductLayout( String name ) throws Exception {
		FinalMetaProduct product = engine.metaLoader.getProductMeta( this , name ); 
		session.setServerSystemProductLayout( product.NAME , product.PATH );
		meta.clearAll();
	}
	
	public void clearServerProductLayout() throws Exception {
		session.clearServerProductLayout();
		meta.clearAll();
	}
	
	public void setShell( ShellExecutor session ) throws Exception {
		this.shell = session;
	}

	public boolean isFailed() {
		return( actionFailed );
	}
	
	protected void setFailed() {
		actionFailed = true;
		context.session.setFailed();
	}
	
	public boolean continueRun() {
		if( !actionFailed )
			return( true );
		if( context.CTX_FORCE )
			return( true );
		return( false );
	}
	
	public boolean isOK() {
		return( ( actionFailed )? false : true );
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

	public void log( Throwable e ) {
		log( "" , e );
	}

	public synchronized void log( String prompt , Throwable e ) {
		String s = NAME;
		if( !prompt.isEmpty() )
			s += " " + prompt;
		output.log( context , s , e );
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
	
	public void exit( String s ) throws Exception {
		output.exit( context , s );
	}

	public void ifexit( String s ) throws Exception {
		if( context.CTX_FORCE )
			error( s + ", ignored" );
		else
			output.exit( context , s + ", exiting (use -force to override)" );
	}

	public void exitAction( String s ) throws Exception {
		exit( this.getClass().getSimpleName() + ": " + s );
	}
	
	public void exitNotImplemented() throws Exception {
		exit( "sorry, code is not implemented yet" );
	}
	
	public void exitUnexpectedCategory( VarCATEGORY CATEGORY ) throws Exception {
		exit( "unexpected category=" + Common.getEnumLower( CATEGORY ) );
	}

	public void exitUnexpectedServerType( VarSERVERTYPE SERVERTYPE ) throws Exception {
		exit( "unexpected servertype=" + Common.getEnumLower( SERVERTYPE ) );
	}

	public void exitUnexpectedState() throws Exception {
		exit( "unexpected state" );
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
		VarCATEGORY[] categories = meta.getAllBuildableCategories();
		return( runCategories( scope , categories ) );
	}
	
	public boolean runEachSourceProject( ActionScope scope ) {
		VarCATEGORY[] categories = meta.getAllSourceCategories();
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
			exit( var + " is empty" );
	}
	
	public void checkRequired( boolean value , String var ) throws Exception {
		if( value == false )
			exit( var + " is empty" );
	}
	
	public void checkRequired( VarBUILDMODE value , String name ) throws Exception {
		if( value == null || value == VarBUILDMODE.UNKNOWN )
			exit( name + " is undefined. Exiting" );
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
		meta.updateProduct( this );
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

	public Account getWinBuildAccount() throws Exception {
		Account account = Account.getAccount( this , meta.product.CONFIG_WINBUILD_HOSTLOGIN , VarOSTYPE.WINDOWS );
		return( account );
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
		return( Common.getPath( context.CTX_REDISTPATH , "tmp" , name ) );
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
		return( ConfReader.readFile( context.session.execrc , path ) );
	}
	
	public Document readXmlFile( String path ) throws Exception {
    	trace( "read xml file path=" + path + " ..." );
		return( ConfReader.readXmlFile( context.session.execrc , path ) );
	}
	
    public Properties readPropertyFile( String path ) throws Exception {
    	trace( "read property file path=" + path + " ..." );
    	return( ConfReader.readPropertyFile( context.session.execrc , path ) );
    }

    public List<String> readFileLines( String path ) throws Exception {
    	trace( "read file lines path=" + path + " ..." );
    	return( ConfReader.readFileLines( context.session.execrc , path ) );
    }
    
	public List<String> readFileLines( String path , Charset charset ) throws Exception {
    	trace( "read file lines path=" + path + " ..." );
		return( ConfReader.readFileLines( context.session.execrc , path , charset ) );
	}

    public String getNameAttr( Node node , VarNAMETYPE nameType ) throws Exception {
    	return( meta.getNameAttr( this , node , nameType ) );
    }

    public String readStringFile( String path ) throws Exception {
    	return( ConfReader.readStringFile( context.session.execrc , path ) );
    }
    
	public void printValues( PropertySet props ) throws Exception {
		for( String prop : props.getKeySet() ) {
			String value = props.getPropertyAny( prop );
			info( "property " + prop + "=" + value );
		}
	}

}
