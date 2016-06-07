package org.urm.server.action;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.CommandOptions.FLAG;
import org.urm.common.action.CommandOptions.SQLMODE;
import org.urm.common.action.CommandOptions.SQLTYPE;
import org.urm.server.CommandExecutor;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaEnvDC;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.meta.Metadata.VarOSTYPE;
import org.urm.server.shell.Account;
import org.urm.server.shell.ShellExecutorPool;
import org.urm.server.storage.LocalFolder;

public class CommandContext {
	
	public CommandOptions options;
	public CommandMethod commandMethod;
	public CommandAction commandAction;

	public MetaEnv env; 
	public MetaEnvDC dc;
	
	public ShellExecutorPool pool;

	public String streamName;
	public Account account;
	public String userHome;
	public String productHome;
	public VarBUILDMODE buildMode = VarBUILDMODE.UNKNOWN;
	public String ENV;
	public String DC;
	public boolean executorFailed;

	// generic settings
	public boolean CTX_TRACEINTERNAL;
	public boolean CTX_TRACE;
	public boolean CTX_SHOWONLY;
	public boolean CTX_SHOWALL;
	public boolean CTX_FORCE;
	public boolean CTX_IGNORE;
	public boolean CTX_ALL;
	public boolean CTX_LOCAL;
	public int CTX_COMMANDTIMEOUT;
	public String CTX_KEYNAME = "";
	public String CTX_ETCPATH = "";
	public String CTX_DISTPATH = "";
	public String CTX_REDISTPATH = "";
	public String CTX_HIDDENPATH = "";
	public String CTX_WORKPATH = "";

	// specific settings
	public boolean CTX_GET;
	public boolean CTX_DIST;
	public boolean CTX_UPDATENEXUS;
	public boolean CTX_CHECK;
	public boolean CTX_MOVE_ERRORS;
	public boolean CTX_REPLACE;
	public boolean CTX_BACKUP;
	public boolean CTX_OBSOLETE;
	public boolean CTX_CONFDEPLOY;
	public boolean CTX_PARTIALCONF;
	public boolean CTX_DEPLOYBINARY;
	public boolean CTX_DEPLOYHOT;
	public boolean CTX_DEPLOYCOLD;
	public boolean CTX_DEPLOYRAW;
	public boolean CTX_CONFKEEPALIVE;
	public boolean CTX_ZERODOWNTIME;
	public boolean CTX_NONODES;
	public boolean CTX_NOCHATMSG;
	public boolean CTX_ROOTUSER;
	public boolean CTX_SUDO;
	public boolean CTX_IGNOREVERSION;
	public boolean CTX_LIVE;
	public boolean CTX_HIDDEN;
	public SQLMODE CTX_DBMODE; 
	public boolean CTX_DBMOVE;
	public boolean CTX_DBAUTH;
	public boolean CTX_CUMULATIVE;
	
	public String CTX_DBALIGNED = "";
	public String CTX_DB = "";
	public String CTX_DBPASSWORD = "";
	public String CTX_REGIONS = "";
	public SQLTYPE CTX_DBTYPE;
	public String CTX_RELEASELABEL = "";
	public String CTX_BRANCH = "";
	public String CTX_TAG = "";
	public String CTX_DATE = "";
	public String CTX_GROUP = "";
	public String CTX_VERSION = "";
	public String CTX_DC = "";
	public String CTX_DEPLOYGROUP = "";
	public String CTX_STARTGROUP = "";
	public String CTX_EXTRAARGS = "";
	public String CTX_UNIT = "";
	public String CTX_BUILDINFO = "";
	public String CTX_HOSTUSER = "";
	public String CTX_NEWKEY = "";
	public VarBUILDMODE CTX_BUILDMODE = VarBUILDMODE.UNKNOWN;
	public String CTX_OLDRELEASE = "";

	public CommandContext( CommandOptions options ) {
		this.options = options;
		this.streamName = "main";
		this.executorFailed = false;
	}

	public CommandContext( CommandContext context , String stream ) {
		this.env = context.env;
		this.dc = context.dc;
		this.executorFailed = context.executorFailed;
		
		this.pool = context.pool;

		if( stream == null || stream.isEmpty() )
			this.streamName = context.streamName;
		else
			this.streamName = stream;
		
		// copy all properties
		this.account = context.account;
		this.userHome = context.userHome;
		this.productHome = context.productHome;
		this.buildMode = context.buildMode;

		// generic
		this.CTX_TRACEINTERNAL = context.CTX_TRACEINTERNAL;
		this.CTX_TRACE = context.CTX_TRACE;
		this.CTX_SHOWONLY = context.CTX_SHOWONLY;
		this.CTX_SHOWALL = context.CTX_SHOWALL;
		this.CTX_FORCE = context.CTX_FORCE;
		this.CTX_IGNORE = context.CTX_IGNORE;
		this.CTX_ALL = context.CTX_ALL;
		this.CTX_LOCAL = context.CTX_LOCAL;
		this.CTX_COMMANDTIMEOUT = context.CTX_COMMANDTIMEOUT;
		this.CTX_KEYNAME = context.CTX_KEYNAME;
		this.CTX_ETCPATH = context.CTX_ETCPATH;
		this.CTX_DISTPATH = context.CTX_DISTPATH;
		this.CTX_REDISTPATH = context.CTX_REDISTPATH;
		this.CTX_HIDDENPATH = context.CTX_HIDDENPATH;
		this.CTX_WORKPATH = context.CTX_WORKPATH;
		
		// specific
		this.CTX_GET = context.CTX_GET;
		this.CTX_DIST = context.CTX_DIST;
		this.CTX_UPDATENEXUS = context.CTX_UPDATENEXUS;
		this.CTX_CHECK = context.CTX_CHECK;
		this.CTX_MOVE_ERRORS = context.CTX_MOVE_ERRORS;
		this.CTX_REPLACE = context.CTX_REPLACE;
		this.CTX_BACKUP = context.CTX_BACKUP;
		this.CTX_OBSOLETE = context.CTX_OBSOLETE;
		this.CTX_CONFDEPLOY = context.CTX_CONFDEPLOY;
		this.CTX_PARTIALCONF = context.CTX_PARTIALCONF;
		this.CTX_DEPLOYBINARY = context.CTX_DEPLOYBINARY;
		this.CTX_DEPLOYHOT = context.CTX_DEPLOYHOT;
		this.CTX_DEPLOYCOLD = context.CTX_DEPLOYCOLD;
		this.CTX_DEPLOYRAW = context.CTX_DEPLOYRAW;
		this.CTX_CONFKEEPALIVE = context.CTX_CONFKEEPALIVE;
		this.CTX_ZERODOWNTIME = context.CTX_ZERODOWNTIME;
		this.CTX_NONODES = context.CTX_NONODES;
		this.CTX_NOCHATMSG = context.CTX_NOCHATMSG;
		this.CTX_ROOTUSER = context.CTX_ROOTUSER;
		this.CTX_SUDO = context.CTX_SUDO;
		this.CTX_IGNOREVERSION = context.CTX_IGNOREVERSION;
		this.CTX_LIVE = context.CTX_LIVE;
		this.CTX_HIDDEN = context.CTX_HIDDEN;
		this.CTX_DBMODE = context.CTX_DBMODE;
		this.CTX_DBMOVE = context.CTX_DBMOVE;
		this.CTX_DBAUTH = context.CTX_DBAUTH;
		this.CTX_CUMULATIVE = context.CTX_CUMULATIVE;
		
		this.CTX_DBALIGNED = context.CTX_DBALIGNED;
		this.CTX_DB = context.CTX_DB;
		this.CTX_DBPASSWORD = context.CTX_DBPASSWORD;
		this.CTX_REGIONS = context.CTX_REGIONS;
		this.CTX_DBTYPE = context.CTX_DBTYPE;
		this.CTX_RELEASELABEL = context.CTX_RELEASELABEL;
		this.CTX_BRANCH = context.CTX_BRANCH;
		this.CTX_TAG = context.CTX_TAG;
		this.CTX_DATE = context.CTX_DATE;
		this.CTX_GROUP = context.CTX_GROUP;
		this.CTX_VERSION = context.CTX_VERSION;
		this.CTX_DC = context.CTX_DC;
		this.CTX_DEPLOYGROUP = context.CTX_DEPLOYGROUP;
		this.CTX_STARTGROUP = context.CTX_STARTGROUP;
		this.CTX_EXTRAARGS = context.CTX_EXTRAARGS;
		this.CTX_UNIT = context.CTX_UNIT;
		this.CTX_BUILDINFO = context.CTX_BUILDINFO;
		this.CTX_HOSTUSER = context.CTX_HOSTUSER;
		this.CTX_NEWKEY = context.CTX_NEWKEY;
		this.CTX_BUILDMODE = context.CTX_BUILDMODE;
		this.CTX_OLDRELEASE = context.CTX_OLDRELEASE;
	}

	public void update( ActionBase action ) throws Exception {
		boolean isproduct = ( action.meta == null || action.meta.product == null )? false : true; 
		boolean isenv = ( env == null )? false : true; 
		boolean def = ( isenv && env.PROD )? true : false;
		String value;
		
		// generic
		CTX_TRACEINTERNAL = ( getFlagValue( action , "GETOPT_TRACE" ) && getFlagValue( action , "GETOPT_SHOWALL" ) )? true : false;
		CTX_TRACE = getFlagValue( action , "GETOPT_TRACE" );
		CTX_SHOWONLY = combineValue( action , "GETOPT_SHOWONLY" , ( isenv )? action.context.env.SHOWONLY : null , def );
		CTX_SHOWALL = getFlagValue( action , "GETOPT_SHOWALL" );
		if( CTX_TRACE )
			CTX_SHOWALL = true;
		CTX_FORCE = getFlagValue( action , "GETOPT_FORCE" );
		CTX_IGNORE = getFlagValue( action , "GETOPT_SKIPERRORS" );
		CTX_ALL = getFlagValue( action , "GETOPT_ALL" );
		CTX_LOCAL = getFlagValue( action , "GETOPT_LOCAL" );
		CTX_COMMANDTIMEOUT = getIntParamValue( action , "GETOPT_COMMANDTIMEOUT" , options.optDefaultCommandTimeout ) * 1000;
		value = getParamValue( action , "GETOPT_KEY" ); 
		CTX_KEYNAME = ( value.isEmpty() )? ( ( isenv )? action.context.env.KEYNAME : "" ) : value;
		String productValue = ( isproduct )? action.meta.product.CONFIG_DISTR_PATH : "";
		CTX_DISTPATH = getParamPathValue( action , "GETOPT_DISTPATH" , productValue );
		CTX_REDISTPATH = ( isproduct )? action.meta.product.CONFIG_REDISTPATH : null;
		if( isenv && !action.context.env.REDISTPATH.isEmpty() )
			CTX_REDISTPATH = action.context.env.REDISTPATH;
		value = getParamPathValue( action , "GETOPT_HIDDENPATH" );
		CTX_HIDDENPATH = ( value.isEmpty() )? ( ( isenv )? action.context.env.CONF_SECRETFILESPATH : "" ) : value;
		CTX_WORKPATH = getParamPathValue( action , "GETOPT_WORKPATH" , "" );
		
		// specific
		CTX_GET = getFlagValue( action , "GETOPT_GET" );
		CTX_DIST = getFlagValue( action , "GETOPT_DIST" );
		CTX_UPDATENEXUS = getFlagValue( action , "GETOPT_UPDATENEXUS" );
		CTX_CHECK = getFlagValue( action , "GETOPT_CHECK" , false );
		CTX_MOVE_ERRORS = getFlagValue( action , "GETOPT_MOVE_ERRORS" );
		CTX_REPLACE = getFlagValue( action , "GETOPT_REPLACE" );
		CTX_BACKUP = combineValue( action , "GETOPT_BACKUP" , ( isenv )? action.context.env.BACKUP : null , def );
		CTX_OBSOLETE = combineValue( action , "GETOPT_OBSOLETE" , ( isenv )? action.context.env.OBSOLETE : null , true );
		CTX_CONFDEPLOY = combineValue( action , "GETOPT_DEPLOYCONF" , ( isenv )? action.context.env.CONF_DEPLOY : null , true );
		CTX_PARTIALCONF = getFlagValue( action , "GETOPT_PARTIALCONF" );
		CTX_DEPLOYBINARY = getFlagValue( action , "GETOPT_DEPLOYBINARY" , true );
		CTX_DEPLOYHOT = getFlagValue( action , "GETOPT_DEPLOYHOT" );
		CTX_DEPLOYCOLD = getFlagValue( action , "GETOPT_DEPLOYCOLD" );
		CTX_DEPLOYRAW = getFlagValue( action , "GETOPT_DEPLOYRAW" );
		CTX_CONFKEEPALIVE = combineValue( action , "GETOPT_KEEPALIVE" , ( isenv )? action.context.env.CONF_KEEPALIVE : null , true );
		CTX_ZERODOWNTIME = getFlagValue( action , "GETOPT_ZERODOWNTIME" );
		CTX_NONODES = getFlagValue( action , "GETOPT_NONODES" );
		CTX_NOCHATMSG = getFlagValue( action , "GETOPT_NOCHATMSG" );
		CTX_ROOTUSER = getFlagValue( action , "GETOPT_ROOTUSER" );
		CTX_SUDO = getFlagValue( action , "GETOPT_SUDO" );
		CTX_IGNOREVERSION = getFlagValue( action , "GETOPT_IGNOREVERSION" );
		CTX_LIVE = getFlagValue( action , "GETOPT_LIVE" );
		CTX_HIDDEN = getFlagValue( action , "GETOPT_HIDDEN" );
		value = getEnumValue( action , "GETOPT_DBMODE" );
		CTX_DBMODE = ( value.isEmpty() )? SQLMODE.UNKNOWN : SQLMODE.valueOf( value );
		CTX_DBMOVE = getFlagValue( action , "GETOPT_DBMOVE" );
		CTX_DBAUTH = combineValue( action , "GETOPT_DBAUTH" , ( isenv )? action.context.env.DB_AUTH : null , false );
		CTX_CUMULATIVE = getFlagValue( action , "GETOPT_CUMULATIVE" );
		
		CTX_DBALIGNED = getParamValue( action , "GETOPT_DBALIGNED" );
		CTX_DB = getParamValue( action , "GETOPT_DB" );
		CTX_DBPASSWORD = getParamValue( action , "GETOPT_DBPASSWORD" );
		CTX_REGIONS = getParamValue( action , "GETOPT_REGIONS" );
		value = getEnumValue( action , "GETOPT_DBTYPE" );
		CTX_DBTYPE = ( value.isEmpty() )? SQLTYPE.UNKNOWN : SQLTYPE.valueOf( value );
		CTX_RELEASELABEL = getParamValue( action , "GETOPT_RELEASE" );
		CTX_BRANCH = getParamValue( action , "GETOPT_BRANCH" );
		CTX_TAG = getParamValue( action , "GETOPT_TAG" );
		CTX_DATE = getParamValue( action , "GETOPT_DATE" );
		CTX_GROUP = getParamValue( action , "GETOPT_GROUP" );
		CTX_VERSION = getParamValue( action , "GETOPT_VERSION" );
		CTX_DC = getParamValue( action , "GETOPT_DC" );
		CTX_DEPLOYGROUP = getParamValue( action , "GETOPT_DEPLOYGROUP" );
		CTX_STARTGROUP = getParamValue( action , "GETOPT_STARTGROUP" );
		CTX_EXTRAARGS = getParamValue( action , "GETOPT_EXTRAARGS" );
		CTX_UNIT = getParamValue( action , "GETOPT_UNIT" );
		CTX_BUILDINFO = getParamValue( action , "GETOPT_BUILDINFO" );
		CTX_HOSTUSER = getParamValue( action , "GETOPT_HOSTUSER" );
		CTX_NEWKEY = getParamValue( action , "GETOPT_NEWKEY" );
		CTX_BUILDMODE = action.meta.getBuildMode( action , getParamValue( action , "GETOPT_BUILDMODE" ) );
		CTX_OLDRELEASE = getParamValue( action , "GETOPT_COMPATIBILITY" );
		
		action.setTimeout( CTX_COMMANDTIMEOUT );
		
		int logLevelLimit = CommandOutput.LOGLEVEL_INFO;
		if( CTX_TRACE ) {
			if( CTX_TRACEINTERNAL )
				logLevelLimit = CommandOutput.LOGLEVEL_INTERNAL;
			else
				logLevelLimit = CommandOutput.LOGLEVEL_TRACE;
		}
		else
		if( CTX_SHOWALL )
			logLevelLimit = CommandOutput.LOGLEVEL_DEBUG;
		
		action.setLogLevel( logLevelLimit );
	}

	public void loadEnv( ActionBase action , boolean loadProps ) throws Exception {
		String useDC = DC;
		if( DC.isEmpty() )
			useDC = CTX_DC;
		loadEnv( action , ENV , useDC , loadProps );
	}
	
	public void loadEnv( ActionBase action , String ENV , String DC , boolean loadProps ) throws Exception {
		this.ENV = ENV;
		this.CTX_DC = DC;
		
		env = action.meta.loadEnvData( action , ENV , loadProps );
		
		if( DC == null || DC.isEmpty() ) {
			dc = null;
			return;
		}
		
		dc = env.getDC( action , DC );
		update( action );
	}
	
	public CommandContext getProductContext( String stream ) {
		CommandContext context = new CommandContext( this , stream );
		return( context );
	}
	
	public String getBuildModeName() {
		return( Common.getEnumLower( buildMode ) );
	}
	
	public boolean loadDefaults( RunContext rc ) {
		// read env
		if( rc.hostName.isEmpty() ) {
			System.out.println( "HOSTNAME is not set. Exiting" );
			return( false );
		}

		if( rc.userName.isEmpty() ) {
			System.out.println( "USER is not set. Exiting" );
			return( false );
		}

		if( rc.productHome.isEmpty() ) {
			System.out.println( "you need to add -Dproduct.home=<your product home> to run" );
			return( false );
		}

		VarOSTYPE osType = ( rc.isWindows() )? VarOSTYPE.WINDOWS : VarOSTYPE.LINUX;
		this.account = Account.getLocalAccount( rc.userName , rc.hostName , osType );
		this.userHome = rc.userHome;
		this.productHome = rc.productHome;
		this.buildMode = ( rc.buildMode.isEmpty() )? VarBUILDMODE.UNKNOWN : VarBUILDMODE.valueOf( rc.buildMode );
		this.ENV = rc.envName;
		this.DC = rc.dcName;
		
		return( true );
	}
	
	public void logDebug( ActionBase action ) throws Exception {
		String contextInfo = "productHome=" + productHome;
		if( buildMode != VarBUILDMODE.UNKNOWN )
			contextInfo += ", buildMode=" + getBuildModeName();
		if( !ENV.isEmpty() )
			contextInfo += ", env=" + ENV;
		if( !DC.isEmpty() )
			contextInfo += ", dc=" + DC;
		action.debug( "context: " + contextInfo );
	}
	
	public void createPool( ActionBase action ) throws Exception {
		pool = new ShellExecutorPool( productHome );
		pool.start( action );
	}

	public void killPool( ActionBase action ) throws Exception {
		pool.kill( action );
	}
	
	public void deleteWorkFolder( ActionBase action , LocalFolder workFolder ) throws Exception {
		pool.master.removeDir( action , workFolder.folderPath );
	}
	
	public void stopPool( ActionBase action ) throws Exception {
		pool.stop( action );
	}
	
	public void setBuildMode( ActionBase action , VarBUILDMODE value ) throws Exception {
		if( buildMode != VarBUILDMODE.UNKNOWN && buildMode != value )
			action.exit( "release is defined for " + getBuildModeName() + " build mode, please use appropriate context folder" );
		
		buildMode = value;
	}

	public boolean getFlagValue( ActionBase action , String var ) throws Exception {
		return( getFlagValue( action , var , false ) );
	}
	
	public boolean getFlagValue( ActionBase action , String var , boolean defValue ) throws Exception {
		if( !options.isFlagVar( var ) )
			action.exit( "unknown flag var=" + var );
		return( options.getFlagValue( var , defValue ) );
	}

	public String getEnumValue( ActionBase action , String var ) throws Exception {
		if( !options.isEnumVar( var ) )
			action.exit( "unknown enum var=" + var );
		return( options.getEnumValue( var ) );
	}

	public String getParamPathValue( ActionBase action , String var , String defaultValue ) throws Exception {
		String value = getParamPathValue( action , var );
		if( value.isEmpty() )
			value = defaultValue;
		
		return( value );
	}
	
	public String getParamPathValue( ActionBase action , String var ) throws Exception {
		String dir = getParamValue( action , var );
		return( Common.getLinuxPath( dir ) );
	}
	
	public String getParamValue( ActionBase action , String var ) throws Exception {
		if( !options.isParamVar( var ) )
			action.exit( "unknown param var=" + var );
		return( options.getParamValue( var ) );
	}		

	public int getIntParamValue( ActionBase action , String var , int defaultValue ) throws Exception {
		if( !options.isParamVar( var ) )
			action.exit( "unknown param var=" + var );
		return( options.getIntParamValue( var , defaultValue ) );
	}

	public boolean combineValue( ActionBase action , String optVar , FLAG confValue , boolean defValue ) throws Exception {
		if( !options.isValidVar( optVar ) )
			action.exit( "unknown flag var=" + optVar );
		return( options.combineValue( optVar , confValue , defValue ) );
	}
	
	public void setFailed() {
		executorFailed = true;
	}
	
	public boolean isFailed() {
		return( executorFailed );
	}
	
	public boolean isOK() {
		return( ( executorFailed )? false : true );
	}
	
	public boolean prepareExecution( CommandExecutor executor , CommandOptions options ) throws Exception {
		this.options = options;
		
		String actionName = options.action;
		String firstArg = options.getArg( 0 );
		
		// check action
		if( options.command.isEmpty() || actionName.isEmpty() || actionName.equals( "help" ) ) {
			if( !firstArg.isEmpty() ) {
				commandAction = executor.getAction( firstArg );
				if( commandAction == null )
					throw new ExitException( "unknown action=" + firstArg );
				
				options.showActionHelp( commandAction.method );
			}
			else
				options.showTopHelp( executor.commandInfo );
			
			return( false );
		}

		commandAction = executor.getAction( actionName );
		if( commandAction == null )
			throw new ExitException( "unknown action=" + actionName );

		if( firstArg.equals( "help" ) ) {
			options.showActionHelp( commandAction.method );
			return( false );
		}
		
		if( !options.checkValidOptions( commandAction.method ) )
			return( false );
		
		options.action = commandAction.method.name;
		return( true );
	}

}
