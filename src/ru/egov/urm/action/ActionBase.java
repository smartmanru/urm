package ru.egov.urm.action;

import ru.egov.urm.Common;
import ru.egov.urm.custom.CommandCustom;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.Artefactory;
import ru.egov.urm.storage.DistStorage;

abstract public class ActionBase {

	public CommandExecutor executor;
	public CommandContext context;
	public Artefactory artefactory;
	public CommandOptions options;
	public CommandCustom custom;
	
	public ShellExecutor session;
	public Metadata meta;
	protected CommandOutput output;
	boolean actionFailed;

	public String NAME;
	public int commandTimeout;
	
	protected boolean executeSimple() throws Exception { trace( NAME + ": simple action execute is not implemented" ); return( false ); };
	protected boolean executeScope( ActionScope scope ) throws Exception { trace( NAME + ": full scope action execute is not implemented" ); return( false ); };
	protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception { trace( NAME + ": scope set action execute is not implemented" ); return( false ); };
	protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception { trace( NAME + ": scope target action execute is not implemented" ); return( false ); };
	protected boolean executeScopeTargetItem( ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception { trace( NAME + ": scope target item action execute is not implemented" ); return( false ); };
	protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception { trace( NAME + ": host action execute is not implemented" ); return( false ); };
	protected void runBefore() throws Exception { trace( NAME + ": blank execute before is not implemented" ); };
	protected void runAfter() throws Exception { trace( NAME + ": blank execute after is not implemented" ); };
	protected void runBefore( ActionScope scope ) throws Exception { trace( NAME + ": scope execute before is not implemented" ); };
	protected void runAfter( ActionScope scope ) throws Exception { trace( NAME + ": scope execute after is not implemented" ); };
	protected void runBefore( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception { trace( NAME + ": scope set target list execute before is not implemented" ); };
	protected void runAfter( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception { trace( NAME + ": scope set target list execute after is not implemented" ); };
	protected void runBefore( ActionScopeTarget target ) throws Exception { trace( NAME + ": scope target execute before is not implemented" ); };
	protected void runAfter( ActionScopeTarget target ) throws Exception { trace( NAME + ": scope target execute after is not implemented" ); };
	protected void runBefore( ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception { trace( NAME + ": scope target item execute before is not implemented" ); };
	protected void runAfter( ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception { trace( NAME + ": scope target item execute after is not implemented" ); };
	
	public ActionBase( CommandExecutor executor , CommandContext context , CommandOptions options , CommandOutput output , Metadata meta ) {
		this.executor = executor;
		this.context = context;
		this.options = options;
		this.output = output;
		this.meta = meta;
		
		this.artefactory = new Artefactory( meta , context );
		this.actionFailed = false;
		this.commandTimeout = 0;

		this.custom = new CommandCustom( meta );
		
		NAME = this.getClass().getSimpleName();
	}

	public ActionBase( ActionBase base , String stream ) {
		this.executor = base.executor;
		this.context = new CommandContext( base.context , stream );
		this.options = base.options;
		this.output = base.output;
		this.meta = base.meta;
		this.custom = base.custom;
		
		this.session = base.session;
		this.artefactory = new Artefactory( base.artefactory );
		this.actionFailed = false;
		this.commandTimeout = base.commandTimeout;
		
		NAME = this.getClass().getSimpleName();
	}
	
	public void finish() throws Exception {
		artefactory.deleteWorkFolder( this );
	}

	public void setShell( ShellExecutor session ) throws Exception {
		this.session = session;
		this.artefactory.setShell( session );
	}
	
	public boolean isFailed() {
		return( actionFailed );
	}
	
	protected void setFailed() {
		actionFailed = true;
		executor.setFailed();
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
	
	public void log( String prompt , Throwable e ) {
		try {
			String s = "[" + context.streamName + "]";
			if( !prompt.isEmpty() )
				s += " " + prompt;
			output.log( s , e );
		}
		catch( Throwable ez ) {
			System.err.println( "unable to log exception:" );
			ez.printStackTrace();
			System.err.println( "original exception:" );
			e.printStackTrace();
		}
	}
	
	public void log( Throwable e ) {
		log( "" , e );
	}

	public void logAction( String s ) {
		log( this.getClass().getSimpleName() + ": " + s );
	}

	public boolean isDebug() {
		return( context.CTX_SHOWALL );
	}
	
	public boolean isTrace() {
		return( context.CTX_TRACE );
	}
	
	public void debug( Throwable e ) {
		if( !context.CTX_SHOWALL )
			return;
		
		log( e );
	}

	public void trace( Throwable e ) {
		if( !context.CTX_TRACE )
			return;
		
		log( e );
	}

	public void out( String s , boolean debug ) throws Exception {
		if( debug )
			output.debug( s );
		else
			output.log( s );
	}

	public void trace( String s ) throws Exception {
		output.trace( s );
	}
	
	public void printExact( String s ) throws Exception {
		output.logExact( s );
	}
	
	public void comment( String s ) throws Exception {
		output.logExact( "# " + s );
	}
	
	public void log( String s ) {
		try {
			output.log( "[" + context.streamName + "] " + s );
		}
		catch( Throwable ez ) {
			System.err.println( "unable to log message:" );
			ez.printStackTrace();
			System.err.println( "original message: " + s );
		}
	}
	
	public void debug( String s ) {
		try {
			output.debug( "[" + context.streamName + "] " + s );
		}
		catch( Throwable ez ) {
			System.err.println( "unable to log message:" );
			ez.printStackTrace();
			System.err.println( "original message: " + s );
		}
	}
	
	public void exit( String s ) throws Exception {
		output.exit( s );
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
		try {
			VarCATEGORY[] categories = meta.getAllBuildableCategories( this );
			return( runCategories( scope , categories ) );
		}
		catch( Throwable e ) {
			log( e );
		}
		return( false );
	}
	
	public boolean runEachSourceProject( ActionScope scope ) {
		try {
			VarCATEGORY[] categories = meta.getAllSourceCategories( this );
			return( runCategories( scope , categories ) );
		}
		catch( Throwable e ) {
			log( e );
		}
		return( false );
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
		return( context.pool.getExecutor( this , account , context.streamName ) );
	}
	
	public ShellExecutor getShell( MetaEnvServerNode node ) throws Exception {
		Account account = Account.getAccount( this , node.HOSTLOGIN , node.server.osType );
		return( getShell( account ) );
	}
	
	public Account getNodeAccount( MetaEnvServerNode node ) throws Exception {
		return( Account.getAccount( this , node.HOSTLOGIN , node.server.osType ) );
	}
	
	public Account getSingleHostAccount( String host , VarOSTYPE OSTYPE ) throws Exception {
		String user = context.CTX_HOSTUSER;
		if( user.isEmpty() )
			user = "root";
		
		Account account = Account.getAccount( this , user , host , OSTYPE );
		return( account );
	}

	public void startRedirect( String title , String logFile ) throws Exception {
		String file = logFile;
		if( file.startsWith( "~/" ) )
			file = session.getHomePath() + file.substring( 1 ); 
		debug( "start logging to " + file );
		output.createOutputFile( title , file );
	}
	
	public void stopRedirect() throws Exception {
		debug( "stop logging to this file." );
		output.stopOutputFile();
	}
	
	public void teeTS( String title , String dir , String basename , String ext ) throws Exception {
		String fname = dir + "/" + output.getTimeStampedName( basename , ext );
		output.tee( title , fname );
	}
	
	public void redirectTS( String title , String dir , String basename , String ext ) throws Exception {
		String fname = dir + "/" + output.getTimeStampedName( basename , ext );
		startRedirect( title , fname );
	}
	
	public void createWorkFolder() throws Exception {
		artefactory.createWorkFolder( this );
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
		String flags = options.getFlagsSet();
		String params = options.getParamsSet();
		String args = options.getArgsSet();
		
		String log = "command=" + options.command + " action=" + options.action;
		if( !flags.isEmpty() )
			log += " " + flags;
		if( !params.isEmpty() )
			log += " " + params;
		if( !args.isEmpty() )
			log += " " + args;
		
		log( "run: " + log );
	}

	public void setBuildMode( VarBUILDMODE value ) throws Exception {
		context.setBuildMode( this , value );
		meta.updateProduct( this );
	}

	public ActionScope getFullScope( String set , String[] TARGETS , String RELEASELABEL ) throws Exception {
		ActionScope scope;
		if( !RELEASELABEL.isEmpty() ) {
			DistStorage release = artefactory.getDistStorageByLabel( this , RELEASELABEL );
			scope = ActionScope.getReleaseSetScope( this , release , set , TARGETS );
		}
		else
			scope = ActionScope.getProductSetScope( this , set , TARGETS );
		return( scope );
	}

	public void executeLogLive( ShellExecutor shell , String msg ) throws Exception {
		if( !isExecute() ) {
			log( shell.name + ": " + msg + " (showonly)" );
			return;
		}

		log( shell.name + ": " + msg + " (execute)" );
		shell.appendExecuteLog( this , msg );
	}
	
	public void executeLogLive( Account account , String msg ) throws Exception {
		ShellExecutor shell = getShell( account );
		if( !isExecute() ) {
			log( account.HOSTLOGIN + ": " + msg + " (showonly)" );
			return;
		}

		log( account.HOSTLOGIN + ": " + msg + " (execute)" );
		shell.appendExecuteLog( this , msg );
	}
	
	public void executeCmdLive( Account account , String cmdRun ) throws Exception {
		if( !isExecute() ) {
			log( account.HOSTLOGIN + ": " + cmdRun + " (showonly)" );
			return;
		}

		log( account.HOSTLOGIN + ": " + cmdRun + " (execute)" );
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

	public int setTimeout( int timeout ) {
		int saveTimeout = commandTimeout;
		commandTimeout = timeout;
		return( saveTimeout );
	}
    
	public int setTimeoutUnlimited() {
		return( setTimeout( 0 ) );
	}
	
	public int setTimeoutDefault() {
		return( setTimeout( context.CTX_COMMANDTIMEOUT ) );
	}

	public Account getWinBuildAccount() throws Exception {
		Account account = Account.getAccount( this , meta.product.CONFIG_WINBUILD_HOSTLOGIN , VarOSTYPE.WINDOWS );
		return( account );
	}

	public String getOSPath( String dirPath ) throws Exception {
		return( session.getOSPath( this , dirPath ) );	
	}

	public boolean isWindows() {
		return( session.isWindows() );
	}

	public boolean isLinux() {
		return( session.isLinux() );
	}

	public void commentExecutor( String msg ) throws Exception {
		String name = "URM " + executor.name + "::" + executor.commandAction.name;
		comment( name + ": " + msg );
	}

	public boolean isLocal() {
		return( context.CTX_LOCAL );
	}
	
}
