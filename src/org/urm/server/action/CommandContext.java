package org.urm.server.action;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.CommandOptions.SQLMODE;
import org.urm.common.action.CommandOptions.SQLTYPE;
import org.urm.common.action.CommandVar.FLAG;
import org.urm.common.jmx.ServerCommandCall;
import org.urm.server.ServerEngine;
import org.urm.server.SessionContext;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaEnvDC;
import org.urm.server.meta.Metadata;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.shell.Account;

public class CommandContext {

	public ServerEngine engine;
	public CommandOptions options;
	public SessionContext session;
	public CommandMethod commandMethod;
	public CommandAction commandAction;

	public Metadata meta;
	public MetaEnv env; 
	public MetaEnvDC dc;
	
	public ServerCommandCall call;
	public String stream;
	public String streamLog;
	public int logLevelLimit;
	
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
	public boolean CTX_OFFLINE;
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

	public CommandContext( ServerEngine engine , SessionContext session , Metadata meta , CommandOptions options , String stream , ServerCommandCall call ) {
		this.engine = engine;
		this.session = session;
		this.meta = meta;
		
		this.options = options;
		this.stream = stream;
		this.call = call;
		
		this.logLevelLimit = CommandOutput.LOGLEVEL_ERROR;
		
		setLogStream();
		setLogLevel();
	}

	private void setLogStream() {
		streamLog = ( call != null )? "[" + stream + "," + call.sessionId + "]" : "[" + stream + "]";
	}
	
	private void setLogLevel() {
		logLevelLimit = CommandOutput.LOGLEVEL_INFO;
		if( CTX_TRACE ) {
			if( CTX_TRACEINTERNAL )
				logLevelLimit = CommandOutput.LOGLEVEL_INTERNAL;
			else
				logLevelLimit = CommandOutput.LOGLEVEL_TRACE;
		}
		else
		if( CTX_SHOWALL )
			logLevelLimit = CommandOutput.LOGLEVEL_DEBUG;
	}
	
	public CommandContext( CommandContext context , String stream ) {
		if( stream == null || stream.isEmpty() )
			this.stream = context.stream;
		else
			this.stream = stream;
		
		// copy all properties
		this.engine = context.engine;
		this.session = context.session;
		this.meta = context.meta;
		
		this.options = context.options;
		this.env = context.env;
		this.dc = context.dc;

		this.call = context.call;
		this.account = context.account;
		this.userHome = context.userHome;
		this.buildMode = context.buildMode;
		this.logLevelLimit = context.logLevelLimit;

		// generic
		this.CTX_TRACEINTERNAL = context.CTX_TRACEINTERNAL;
		this.CTX_TRACE = context.CTX_TRACE;
		this.CTX_SHOWONLY = context.CTX_SHOWONLY;
		this.CTX_SHOWALL = context.CTX_SHOWALL;
		this.CTX_FORCE = context.CTX_FORCE;
		this.CTX_IGNORE = context.CTX_IGNORE;
		this.CTX_ALL = context.CTX_ALL;
		this.CTX_LOCAL = context.CTX_LOCAL;
		this.CTX_OFFLINE = context.CTX_OFFLINE;
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
		
		setLogStream();
	}

	public void update() throws Exception {
		boolean isproduct = ( meta == null || meta.product == null )? false : true; 
		boolean isenv = ( env == null )? false : true; 
		boolean def = ( isenv && env.PROD )? true : false;
		String value;
		
		// generic
		CTX_TRACEINTERNAL = ( getFlagValue( "OPT_TRACE" ) && getFlagValue( "OPT_SHOWALL" ) )? true : false;
		CTX_TRACE = getFlagValue( "OPT_TRACE" );
		CTX_SHOWONLY = combineValue( "OPT_SHOWONLY" , ( isenv )? env.SHOWONLY : null , def );
		CTX_SHOWALL = getFlagValue( "OPT_SHOWALL" );
		if( CTX_TRACE )
			CTX_SHOWALL = true;
		CTX_FORCE = getFlagValue( "OPT_FORCE" );
		CTX_IGNORE = getFlagValue( "OPT_SKIPERRORS" );
		CTX_ALL = getFlagValue( "OPT_ALL" );
		CTX_LOCAL = getFlagValue( "OPT_LOCAL" );
		CTX_OFFLINE = getFlagValue( "OPT_OFFLINE" );
		CTX_COMMANDTIMEOUT = getIntParamValue( "OPT_COMMANDTIMEOUT" , options.optDefaultCommandTimeout ) * 1000;
		value = getParamValue( "OPT_KEY" ); 
		CTX_KEYNAME = ( value.isEmpty() )? ( ( isenv )? env.KEYNAME : "" ) : value;
		String productValue = ( isproduct )? meta.product.CONFIG_DISTR_PATH : "";
		CTX_DISTPATH = getParamPathValue( "OPT_DISTPATH" , productValue );
		CTX_REDISTPATH = ( isproduct )? meta.product.CONFIG_REDISTPATH : null;
		if( isenv && !env.REDISTPATH.isEmpty() )
			CTX_REDISTPATH = env.REDISTPATH;
		value = getParamPathValue( "OPT_HIDDENPATH" );
		CTX_HIDDENPATH = ( value.isEmpty() )? ( ( isenv )? env.CONF_SECRETFILESPATH : "" ) : value;
		CTX_WORKPATH = getParamPathValue( "OPT_WORKPATH" , "" );
		
		// specific
		CTX_GET = getFlagValue( "OPT_GET" );
		CTX_DIST = getFlagValue( "OPT_DIST" );
		CTX_UPDATENEXUS = getFlagValue( "OPT_UPDATENEXUS" );
		CTX_CHECK = getFlagValue( "OPT_CHECK" , false );
		CTX_MOVE_ERRORS = getFlagValue( "OPT_MOVE_ERRORS" );
		CTX_REPLACE = getFlagValue( "OPT_REPLACE" );
		CTX_BACKUP = combineValue( "OPT_BACKUP" , ( isenv )? env.BACKUP : null , def );
		CTX_OBSOLETE = combineValue( "OPT_OBSOLETE" , ( isenv )? env.OBSOLETE : null , true );
		CTX_CONFDEPLOY = combineValue( "OPT_DEPLOYCONF" , ( isenv )? env.CONF_DEPLOY : null , true );
		CTX_PARTIALCONF = getFlagValue( "OPT_PARTIALCONF" );
		CTX_DEPLOYBINARY = getFlagValue( "OPT_DEPLOYBINARY" , true );
		CTX_DEPLOYHOT = getFlagValue( "OPT_DEPLOYHOT" );
		CTX_DEPLOYCOLD = getFlagValue( "OPT_DEPLOYCOLD" );
		CTX_DEPLOYRAW = getFlagValue( "OPT_DEPLOYRAW" );
		CTX_CONFKEEPALIVE = combineValue( "OPT_KEEPALIVE" , ( isenv )? env.CONF_KEEPALIVE : null , true );
		CTX_ZERODOWNTIME = getFlagValue( "OPT_ZERODOWNTIME" );
		CTX_NONODES = getFlagValue( "OPT_NONODES" );
		CTX_NOCHATMSG = getFlagValue( "OPT_NOCHATMSG" );
		CTX_ROOTUSER = getFlagValue( "OPT_ROOTUSER" );
		CTX_SUDO = getFlagValue( "OPT_SUDO" );
		CTX_IGNOREVERSION = getFlagValue( "OPT_IGNOREVERSION" );
		CTX_LIVE = getFlagValue( "OPT_LIVE" );
		CTX_HIDDEN = getFlagValue( "OPT_HIDDEN" );
		value = getEnumValue( "OPT_DBMODE" );
		CTX_DBMODE = ( value.isEmpty() )? SQLMODE.UNKNOWN : SQLMODE.valueOf( value );
		CTX_DBMOVE = getFlagValue( "OPT_DBMOVE" );
		CTX_DBAUTH = combineValue( "OPT_DBAUTH" , ( isenv )? env.DB_AUTH : null , false );
		CTX_CUMULATIVE = getFlagValue( "OPT_CUMULATIVE" );
		
		CTX_DBALIGNED = getParamValue( "OPT_DBALIGNED" );
		CTX_DB = getParamValue( "OPT_DB" );
		CTX_DBPASSWORD = getParamValue( "OPT_DBPASSWORD" );
		CTX_REGIONS = getParamValue( "OPT_REGIONS" );
		value = getEnumValue( "OPT_DBTYPE" );
		CTX_DBTYPE = ( value.isEmpty() )? SQLTYPE.UNKNOWN : SQLTYPE.valueOf( value );
		CTX_RELEASELABEL = getParamValue( "OPT_RELEASE" );
		CTX_BRANCH = getParamValue( "OPT_BRANCH" );
		CTX_TAG = getParamValue( "OPT_TAG" );
		CTX_DATE = getParamValue( "OPT_DATE" );
		CTX_GROUP = getParamValue( "OPT_GROUP" );
		CTX_VERSION = getParamValue( "OPT_VERSION" );
		CTX_DC = getParamValue( "OPT_DC" );
		CTX_DEPLOYGROUP = getParamValue( "OPT_DEPLOYGROUP" );
		CTX_STARTGROUP = getParamValue( "OPT_STARTGROUP" );
		CTX_EXTRAARGS = getParamValue( "OPT_EXTRAARGS" );
		CTX_UNIT = getParamValue( "OPT_UNIT" );
		CTX_BUILDINFO = getParamValue( "OPT_BUILDINFO" );
		CTX_HOSTUSER = getParamValue( "OPT_HOSTUSER" );
		CTX_NEWKEY = getParamValue( "OPT_NEWKEY" );
		CTX_BUILDMODE = meta.getBuildMode( getParamValue( "OPT_BUILDMODE" ) );
		CTX_OLDRELEASE = getParamValue( "OPT_COMPATIBILITY" );
		CTX_PORT = getIntParamValue( "OPT_PORT" , -1 );
		CTX_HOST = getParamValue( "OPT_HOST" );
		
		setLogStream();
		setLogLevel();
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
		update();
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
		if( session.execrc.hostName.isEmpty() ) {
			System.out.println( "HOSTNAME is not set. Exiting" );
			return( false );
		}

		if( session.execrc.userName.isEmpty() ) {
			System.out.println( "USER is not set. Exiting" );
			return( false );
		}

		VarOSTYPE osType = ( session.execrc.isWindows() )? VarOSTYPE.WINDOWS : VarOSTYPE.LINUX;
		this.account = Account.getLocalAccount( session.execrc.userName , session.execrc.hostName , osType );
		this.userHome = session.execrc.userHome;
		this.buildMode = ( session.clientrc.buildMode.isEmpty() )? VarBUILDMODE.UNKNOWN : VarBUILDMODE.valueOf( session.clientrc.buildMode );
		
		return( true );
	}
	
	public String getInfo() {
		String contextInfo = "";
		if( !session.productPath.isEmpty() )
			contextInfo = "productHome=" + session.productPath;
		if( buildMode != VarBUILDMODE.UNKNOWN )
			contextInfo += ", buildMode=" + getBuildModeName();
		if( !session.ENV.isEmpty() )
			contextInfo += ", env=" + session.ENV;
		if( !session.DC.isEmpty() )
			contextInfo += ", dc=" + session.DC;
		return( contextInfo );
	}
	
	public void setBuildMode( VarBUILDMODE value ) throws Exception {
		if( buildMode != VarBUILDMODE.UNKNOWN && buildMode != value )
			throw new ExitException( "release is defined for " + getBuildModeName() + " build mode, please use appropriate context" );
		
		buildMode = value;
	}

	public boolean getFlagValue( String var ) throws Exception {
		return( getFlagValue( var , false ) );
	}
	
	public boolean getFlagValue( String var , boolean defValue ) throws Exception {
		if( !options.isFlagVar( var ) )
			throw new ExitException( "unknown flag var=" + var );
		return( options.getFlagValue( var , defValue ) );
	}

	public String getEnumValue( String var ) throws Exception {
		if( !options.isEnumVar( var ) )
			throw new ExitException( "unknown enum var=" + var );
		return( options.getEnumValue( var ) );
	}

	public String getParamPathValue( String var , String defaultValue ) throws Exception {
		String value = getParamPathValue( var );
		if( value.isEmpty() )
			value = defaultValue;
		
		return( value );
	}
	
	public String getParamPathValue( String var ) throws Exception {
		String dir = getParamValue( var );
		return( Common.getLinuxPath( dir ) );
	}
	
	public String getParamValue( String var ) throws Exception {
		if( !options.isParamVar( var ) )
			throw new ExitException( "unknown param var=" + var );
		return( options.getParamValue( var ) );
	}		

	public int getIntParamValue( String var , int defaultValue ) throws Exception {
		if( !options.isParamVar( var ) )
			throw new ExitException( "unknown param var=" + var );
		return( options.getIntParamValue( var , defaultValue ) );
	}

	public boolean combineValue( String optVar , FLAG confValue , boolean defValue ) throws Exception {
		if( !options.isValidVar( optVar ) )
			throw new ExitException( "unknown flag var=" + optVar );
		return( options.combineValue( optVar , confValue , defValue ) );
	}
	
}
