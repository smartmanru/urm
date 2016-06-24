package org.urm.server.action;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.CommandOptions.SQLMODE;
import org.urm.common.action.CommandOptions.SQLTYPE;
import org.urm.common.action.CommandVar.FLAG;
import org.urm.server.CommandExecutor;
import org.urm.server.SessionContext;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaEnvDC;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.shell.Account;
import org.urm.server.shell.ShellExecutorPool;
import org.urm.server.storage.LocalFolder;

public class CommandContext {
	
	public RunContext clientrc;
	public RunContext execrc;
	public CommandOptions options;
	public SessionContext session;
	public CommandMethod commandMethod;
	public CommandAction commandAction;

	public MetaEnv env; 
	public MetaEnvDC dc;
	
	public ShellExecutorPool pool;

	public String streamName;
	public Account account;
	public String userHome;
	public VarBUILDMODE buildMode = VarBUILDMODE.UNKNOWN;

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
	public String CTX_HOST = "";
	public int CTX_PORT = -1;

	public CommandContext( RunContext clientrc , RunContext execrc , CommandOptions options , SessionContext session , String stream ) {
		this.clientrc = clientrc;
		this.execrc = execrc;
		this.options = options;
		this.session = session;
		
		this.streamName = stream;
	}

	public CommandContext( CommandContext context , String stream ) {
		this.clientrc = context.clientrc;
		this.execrc = context.execrc;
		this.options = context.options;
		this.session = context.session;
		
		this.env = context.env;
		this.dc = context.dc;
		this.pool = context.pool;

		if( stream == null || stream.isEmpty() )
			this.streamName = context.streamName;
		else
			this.streamName = stream;
		
		// copy all properties
		this.account = context.account;
		this.userHome = context.userHome;
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
		this.CTX_PORT = context.CTX_PORT;
		this.CTX_HOST = context.CTX_HOST;
	}

	public void update( ActionBase action ) throws Exception {
		boolean isproduct = ( action.meta == null || action.meta.product == null )? false : true; 
		boolean isenv = ( env == null )? false : true; 
		boolean def = ( isenv && env.PROD )? true : false;
		String value;
		
		// generic
		CTX_TRACEINTERNAL = ( getFlagValue( action , "OPT_TRACE" ) && getFlagValue( action , "OPT_SHOWALL" ) )? true : false;
		CTX_TRACE = getFlagValue( action , "OPT_TRACE" );
		CTX_SHOWONLY = combineValue( action , "OPT_SHOWONLY" , ( isenv )? action.context.env.SHOWONLY : null , def );
		CTX_SHOWALL = getFlagValue( action , "OPT_SHOWALL" );
		if( CTX_TRACE )
			CTX_SHOWALL = true;
		CTX_FORCE = getFlagValue( action , "OPT_FORCE" );
		CTX_IGNORE = getFlagValue( action , "OPT_SKIPERRORS" );
		CTX_ALL = getFlagValue( action , "OPT_ALL" );
		CTX_LOCAL = getFlagValue( action , "OPT_LOCAL" );
		CTX_COMMANDTIMEOUT = getIntParamValue( action , "OPT_COMMANDTIMEOUT" , options.optDefaultCommandTimeout ) * 1000;
		value = getParamValue( action , "OPT_KEY" ); 
		CTX_KEYNAME = ( value.isEmpty() )? ( ( isenv )? action.context.env.KEYNAME : "" ) : value;
		String productValue = ( isproduct )? action.meta.product.CONFIG_DISTR_PATH : "";
		CTX_DISTPATH = getParamPathValue( action , "OPT_DISTPATH" , productValue );
		CTX_REDISTPATH = ( isproduct )? action.meta.product.CONFIG_REDISTPATH : null;
		if( isenv && !action.context.env.REDISTPATH.isEmpty() )
			CTX_REDISTPATH = action.context.env.REDISTPATH;
		value = getParamPathValue( action , "OPT_HIDDENPATH" );
		CTX_HIDDENPATH = ( value.isEmpty() )? ( ( isenv )? action.context.env.CONF_SECRETFILESPATH : "" ) : value;
		CTX_WORKPATH = getParamPathValue( action , "OPT_WORKPATH" , "" );
		
		// specific
		CTX_GET = getFlagValue( action , "OPT_GET" );
		CTX_DIST = getFlagValue( action , "OPT_DIST" );
		CTX_UPDATENEXUS = getFlagValue( action , "OPT_UPDATENEXUS" );
		CTX_CHECK = getFlagValue( action , "OPT_CHECK" , false );
		CTX_MOVE_ERRORS = getFlagValue( action , "OPT_MOVE_ERRORS" );
		CTX_REPLACE = getFlagValue( action , "OPT_REPLACE" );
		CTX_BACKUP = combineValue( action , "OPT_BACKUP" , ( isenv )? action.context.env.BACKUP : null , def );
		CTX_OBSOLETE = combineValue( action , "OPT_OBSOLETE" , ( isenv )? action.context.env.OBSOLETE : null , true );
		CTX_CONFDEPLOY = combineValue( action , "OPT_DEPLOYCONF" , ( isenv )? action.context.env.CONF_DEPLOY : null , true );
		CTX_PARTIALCONF = getFlagValue( action , "OPT_PARTIALCONF" );
		CTX_DEPLOYBINARY = getFlagValue( action , "OPT_DEPLOYBINARY" , true );
		CTX_DEPLOYHOT = getFlagValue( action , "OPT_DEPLOYHOT" );
		CTX_DEPLOYCOLD = getFlagValue( action , "OPT_DEPLOYCOLD" );
		CTX_DEPLOYRAW = getFlagValue( action , "OPT_DEPLOYRAW" );
		CTX_CONFKEEPALIVE = combineValue( action , "OPT_KEEPALIVE" , ( isenv )? action.context.env.CONF_KEEPALIVE : null , true );
		CTX_ZERODOWNTIME = getFlagValue( action , "OPT_ZERODOWNTIME" );
		CTX_NONODES = getFlagValue( action , "OPT_NONODES" );
		CTX_NOCHATMSG = getFlagValue( action , "OPT_NOCHATMSG" );
		CTX_ROOTUSER = getFlagValue( action , "OPT_ROOTUSER" );
		CTX_SUDO = getFlagValue( action , "OPT_SUDO" );
		CTX_IGNOREVERSION = getFlagValue( action , "OPT_IGNOREVERSION" );
		CTX_LIVE = getFlagValue( action , "OPT_LIVE" );
		CTX_HIDDEN = getFlagValue( action , "OPT_HIDDEN" );
		value = getEnumValue( action , "OPT_DBMODE" );
		CTX_DBMODE = ( value.isEmpty() )? SQLMODE.UNKNOWN : SQLMODE.valueOf( value );
		CTX_DBMOVE = getFlagValue( action , "OPT_DBMOVE" );
		CTX_DBAUTH = combineValue( action , "OPT_DBAUTH" , ( isenv )? action.context.env.DB_AUTH : null , false );
		CTX_CUMULATIVE = getFlagValue( action , "OPT_CUMULATIVE" );
		
		CTX_DBALIGNED = getParamValue( action , "OPT_DBALIGNED" );
		CTX_DB = getParamValue( action , "OPT_DB" );
		CTX_DBPASSWORD = getParamValue( action , "OPT_DBPASSWORD" );
		CTX_REGIONS = getParamValue( action , "OPT_REGIONS" );
		value = getEnumValue( action , "OPT_DBTYPE" );
		CTX_DBTYPE = ( value.isEmpty() )? SQLTYPE.UNKNOWN : SQLTYPE.valueOf( value );
		CTX_RELEASELABEL = getParamValue( action , "OPT_RELEASE" );
		CTX_BRANCH = getParamValue( action , "OPT_BRANCH" );
		CTX_TAG = getParamValue( action , "OPT_TAG" );
		CTX_DATE = getParamValue( action , "OPT_DATE" );
		CTX_GROUP = getParamValue( action , "OPT_GROUP" );
		CTX_VERSION = getParamValue( action , "OPT_VERSION" );
		CTX_DC = getParamValue( action , "OPT_DC" );
		CTX_DEPLOYGROUP = getParamValue( action , "OPT_DEPLOYGROUP" );
		CTX_STARTGROUP = getParamValue( action , "OPT_STARTGROUP" );
		CTX_EXTRAARGS = getParamValue( action , "OPT_EXTRAARGS" );
		CTX_UNIT = getParamValue( action , "OPT_UNIT" );
		CTX_BUILDINFO = getParamValue( action , "OPT_BUILDINFO" );
		CTX_HOSTUSER = getParamValue( action , "OPT_HOSTUSER" );
		CTX_NEWKEY = getParamValue( action , "OPT_NEWKEY" );
		CTX_BUILDMODE = action.meta.getBuildMode( action , getParamValue( action , "OPT_BUILDMODE" ) );
		CTX_OLDRELEASE = getParamValue( action , "OPT_COMPATIBILITY" );
		CTX_PORT = getIntParamValue( action , "OPT_PORT" , -1 );
		CTX_HOST = getParamValue( action , "OPT_HOST" );
		
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
		String useDC = session.DC;
		if( useDC.isEmpty() )
			useDC = CTX_DC;
		loadEnv( action , session.ENV , useDC , loadProps );
	}
	
	public void loadEnv( ActionBase action , String ENV , String DC , boolean loadProps ) throws Exception {
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
	
	public boolean setRunContext() {
		// read env
		if( execrc.hostName.isEmpty() ) {
			System.out.println( "HOSTNAME is not set. Exiting" );
			return( false );
		}

		if( execrc.userName.isEmpty() ) {
			System.out.println( "USER is not set. Exiting" );
			return( false );
		}

		VarOSTYPE osType = ( execrc.isWindows() )? VarOSTYPE.WINDOWS : VarOSTYPE.LINUX;
		this.account = Account.getLocalAccount( execrc.userName , execrc.hostName , osType );
		this.userHome = execrc.userHome;
		this.buildMode = ( clientrc.buildMode.isEmpty() )? VarBUILDMODE.UNKNOWN : VarBUILDMODE.valueOf( clientrc.buildMode );
		
		return( true );
	}
	
	public void logDebug( ActionBase action ) throws Exception {
		String contextInfo = "";
		if( !session.productPath.isEmpty() )
			contextInfo = "productHome=" + session.productPath;
		if( buildMode != VarBUILDMODE.UNKNOWN )
			contextInfo += ", buildMode=" + getBuildModeName();
		if( !session.ENV.isEmpty() )
			contextInfo += ", env=" + session.ENV;
		if( !session.DC.isEmpty() )
			contextInfo += ", dc=" + session.DC;
		action.debug( "context: " + contextInfo );
	}
	
	public void createPool( ActionBase action ) throws Exception {
		pool = new ShellExecutorPool();
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
	
	public boolean setAction( CommandBuilder builder , CommandExecutor executor ) throws Exception {
		String actionName = options.action;

		commandAction = executor.getAction( actionName );
		if( commandAction == null )
			throw new ExitException( "unknown action=" + actionName );

		if( !options.checkValidOptions( commandAction.method ) )
			return( false );
		
		options.action = commandAction.method.name;
		return( true );
	}

}
