package ru.egov.urm.run;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.run.CommandOptions.FLAG;
import ru.egov.urm.shell.ShellExecutorPool;

public class CommandContext {
	
	public ShellExecutorPool pool;
	
	public String streamName;
	public String hostLogin;
	public String productHome;
	public VarBUILDMODE buildMode;
	public String env;
	public String dc;

	// context-dependent options
	public boolean DB_AUTH;
	public boolean OBSOLETE;
	public boolean SHOWONLY;
	public boolean BACKUP;
	public boolean CONF_DEPLOY;
	public boolean CONF_KEEPALIVE;
	public String KEYNAME;
	
	public CommandContext() {
		this.streamName = "main";
		this.buildMode = VarBUILDMODE.UNKNOWN;
		this.KEYNAME = "";
	}

	public CommandContext( CommandContext context , String stream ) {
		this.pool = context.pool;

		if( stream == null || stream.isEmpty() )
			this.streamName = context.streamName;
		else
			this.streamName = stream;
		
		this.hostLogin = context.hostLogin;
		this.productHome = context.productHome;
		this.buildMode = context.buildMode;
		this.env = context.env;
		this.dc = context.dc;
		
		this.DB_AUTH = context.DB_AUTH;
		this.OBSOLETE = context.OBSOLETE;
		this.SHOWONLY = context.SHOWONLY;
		this.BACKUP = context.BACKUP;
		this.CONF_DEPLOY = context.CONF_DEPLOY;
		this.CONF_KEEPALIVE = context.CONF_KEEPALIVE;
		this.KEYNAME = context.KEYNAME;
	}

	public CommandContext getProductContext( String productHome , String stream ) {
		CommandContext context = new CommandContext( this , stream );
		context.productHome = productHome;
		return( context );
	}
	
	public String getBuildModeName() {
		return( Common.getEnumLower( buildMode ) );
	}
	
	public boolean loadDefaults() {
		// read env
		String hostName = System.getenv( "HOSTNAME" );
		if( hostName == null || hostName.isEmpty() ) {
			System.out.println( "HOSTNAME is not set. Exiting" );
			return( false );
		}

		String userName = System.getenv( "USER" );
		if( userName == null || userName.isEmpty() ) {
			System.out.println( "USER is not set. Exiting" );
			return( false );
		}

		String productHome = System.getProperty( "product.home" );
		if( productHome == null || productHome.isEmpty() ) {
			System.out.println( "you need to add -Dproduct.home=<your product home> to run" );
			return( false );
		}

		this.hostLogin = Common.getAccount( userName , hostName );
		this.productHome = productHome;
		String value = System.getProperty( "build.mode" ).toUpperCase();
		this.buildMode = ( value == null || value.isEmpty() )? VarBUILDMODE.UNKNOWN : VarBUILDMODE.valueOf( value );
		this.env = System.getProperty( "env" );
		if( env == null )
			env = "";
		this.dc = System.getProperty( "dc" );
		if( dc == null )
			dc = "";
		
		return( true );
	}
	
	public void logDebug( ActionBase action ) throws Exception {
		String contextInfo = "productHome=" + productHome;
		if( buildMode != VarBUILDMODE.UNKNOWN )
			contextInfo += "buildMode=" + getBuildModeName();
		if( !env.isEmpty() )
			contextInfo += "env=" + env;
		if( !dc.isEmpty() )
			contextInfo += "dc=" + dc;
		action.debug( "context: hostLogin=" + hostLogin + ", productHome=" + productHome + ", " + contextInfo );
	}
	
	public void createPool( ActionBase action ) throws Exception {
		pool = new ShellExecutorPool( productHome , action.options.OPT_COMMANDTIMEOUT );
		pool.createDedicatedLocalShell( action );
	}

	public void killPool( ActionBase action ) throws Exception {
		pool.kill( action );
	}
	
	public void setBuildMode( ActionBase action , VarBUILDMODE value ) throws Exception {
		if( buildMode != VarBUILDMODE.UNKNOWN && buildMode != value )
			action.exit( "release is defined for " + getBuildModeName() + " build mode, please use appropriate context folder" );
		
		buildMode = value;
	}
	
	public void updateProperties( ActionBase action ) throws Exception {
		boolean isenv = ( action.meta.env == null )? false : true; 
		DB_AUTH = combineValue( action , "GETOPT_DBAUTH" , ( isenv )? action.meta.env.DB_AUTH : null , false );
		OBSOLETE = combineValue( action , "GETOPT_OBSOLETE" , ( isenv )? action.meta.env.OBSOLETE : null , true );
		
		boolean def = ( isenv && action.meta.env.PROD )? true : false;
		SHOWONLY = combineValue( action , "GETOPT_SHOWONLY" , ( isenv )? action.meta.env.SHOWONLY : null , def );
		BACKUP = combineValue( action , "GETOPT_BACKUP" , ( isenv )? action.meta.env.BACKUP : null , def );
		CONF_DEPLOY = combineValue( action , "GETOPT_DEPLOYCONF" , ( isenv )? action.meta.env.CONF_DEPLOY : null , true );
		CONF_KEEPALIVE = combineValue( action , "GETOPT_KEEPALIVE" , ( isenv )? action.meta.env.CONF_KEEPALIVE : null , true );
		KEYNAME = ( action.options.OPT_KEY.isEmpty() )? ( ( isenv )? action.meta.env.KEYNAME : "" ) : action.options.OPT_KEY;
	}

	public boolean combineValue( ActionBase action , String optVar , FLAG confValue , boolean defValue ) throws Exception {
		if( !action.options.isValidVar( optVar ) )
			action.exit( "unknown flag var=" + optVar );
		
		FLAG optValue = action.options.getFlagNativeValue( optVar );

		// option always overrides
		if( optValue != null && optValue != FLAG.DEFAULT )
			return( optValue == FLAG.YES );
		
		// if configuration is present
		if( confValue != null && confValue != FLAG.DEFAULT )
			return( confValue == FLAG.YES );
		
		return( defValue );
	}
	
}
