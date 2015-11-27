package ru.egov.urm.run;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.Artefactory;
import ru.egov.urm.storage.DistStorage;

abstract public class ActionBase {

	public CommandContext context;
	public Artefactory artefactory;
	public CommandOptions options;
	
	public ShellExecutor session;
	public Metadata meta;
	protected CommandOutput output;
	boolean actionFailed;

	public String NAME;
	
	protected boolean executeSimple() throws Exception { debug( NAME + ": simple action execute is not implemented" ); return( false ); };
	protected boolean executeScope( ActionScope scope ) throws Exception { debug( NAME + ": full scope action execute is not implemented" ); return( false ); };
	protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception { debug( NAME + ": scope set action execute is not implemented" ); return( false ); };
	protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception { debug( NAME + ": scope target action execute is not implemented" ); return( false ); };
	protected boolean executeScopeTargetItem( ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception { debug( NAME + ": scope target item action execute is not implemented" ); return( false ); };
	protected boolean executeAccount( ActionScopeSet set , String hostLogin ) throws Exception { debug( NAME + ": host action execute is not implemented" ); return( false ); };
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
	
	public ActionBase( CommandContext context , CommandOptions options , CommandOutput output , Metadata meta ) {
		this.context = context;
		this.options = options;
		this.output = output;
		this.meta = meta;
		
		this.artefactory = new Artefactory( meta );
		this.actionFailed = false;
		
		NAME = this.getClass().getSimpleName();
	}

	public ActionBase( ActionBase base , String stream ) {
		this.context = new CommandContext( base.context , stream );
		this.options = base.options;
		this.output = base.output;
		this.meta = base.meta;
		
		this.session = base.session;
		this.artefactory = new Artefactory( base.artefactory );
		this.actionFailed = false;
		
		NAME = this.getClass().getSimpleName();
	}
	
	public void finish() throws Exception {
		artefactory.deleteWorkFolder( this );
	}

	public void changeOptions() throws Exception {
		options = new CommandOptions( options );
	}
	
	public void setShell( ShellExecutor session ) throws Exception {
		this.session = session;
		this.artefactory.setShell( session );
	}
	
	public boolean isFailed() {
		return( actionFailed );
	}
	
	public String getMode() {
		if( context.SHOWONLY )
			return( "showonly" );
		else
			return( "execute" );
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
	
	public void debug( Throwable e ) {
		if( !options.OPT_SHOWALL )
			return;
		
		log( e );
	}

	public void trace( Throwable e ) {
		if( !options.OPT_TRACE )
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
	
	public void printComment( String s ) throws Exception {
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
	
	protected void setFailed() {
		actionFailed = true;
	}
	
	public boolean isOK() {
		return( ( actionFailed )? false : true );
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
	
	public ShellExecutor getShell( String hostLogin ) throws Exception {
		return( context.pool.getExecutor( this , hostLogin , context.streamName ) );
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

	public void executeLogLive( String hostLogin , String msg ) throws Exception {
		if( context.SHOWONLY ) {
			log( hostLogin + ": " + msg + " (showonly)" );
			return;
		}

		log( hostLogin + ": " + msg + " (execute)" );
		ShellExecutor shell = getShell( hostLogin );
		shell.appendExecuteLog( this , msg );
	}
	
	public void executeCmdLive( String hostLogin , String cmdRun ) throws Exception {
		if( context.SHOWONLY ) {
			log( hostLogin + ": " + cmdRun + " (showonly)" );
			return;
		}

		log( hostLogin + ": " + cmdRun + " (execute)" );
		ShellExecutor shell = getShell( hostLogin );
		shell.appendExecuteLog( this , cmdRun );

		shell.customCheckErrorsNormal( this , cmdRun );
	}

	public void executeCmd( String hostLogin , String cmdRun ) throws Exception {
		ShellExecutor shell = getShell( hostLogin );
		shell.customCheckErrorsDebug( this , cmdRun );
	}

	public String executeCmdGetValue( String hostLogin , String cmdRun ) throws Exception {
		ShellExecutor shell = getShell( hostLogin );
		return( shell.customGetValue( this , cmdRun ) );
	}

    public void sleep( long millis ) throws Exception {
        trace( "sleep: intentional delay - " + millis + " millis" );
        Thread.sleep(millis);
    }

	public boolean isLocal( String hostLogin ) throws Exception {
		if( hostLogin.equals( "local" ) || hostLogin.equals( context.hostLogin ) )
			return( true );
		return( false );
	}		
    
}
