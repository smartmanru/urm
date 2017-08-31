package org.urm.engine.action;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.CommandOptions.SQLMODE;
import org.urm.common.action.CommandOptions.SQLTYPE;
import org.urm.common.action.CommandOption.FLAG;
import org.urm.engine.ServerCall;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerSession;
import org.urm.engine.shell.Account;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.Types;
import org.urm.meta.Types.*;

public class CommandContext {

	class CommandLogCapture {
		public List<String> logData;
		public int logCaptureCount;
		
		public CommandLogCapture() {
			logCaptureCount = 0;
		}
		
		public synchronized int logStartCapture() {
			if( logData == null )
				logData = new LinkedList<String>();
			logCaptureCount++;
			return( logData.size() );
		}

		public synchronized void logStopCapture() {
			logCaptureCount = 0;
			logData = null;
		}
		
		public synchronized String[] logFinishCapture( int startIndex ) {
			String[] data = null;
			if( startIndex > 0 )
				data = logData.subList( startIndex , logData.size() ).toArray( new String[0] );
			else
				data = logData.toArray( new String[0] );

			logCaptureCount--;
			if( logCaptureCount == 0 )
				logData = null;
			return( data );
		}

		public synchronized void outExact( String s ) {
			if( logData != null )
				logData.add( s );
		}
		
	};
	
	public ServerEngine engine;
	public CommandOptions options;
	public ServerSession session;

	public Meta meta;
	public MetaEnv env; 
	public MetaEnvSegment sg;
	
	public ServerCall call;
	public String stream;
	public String streamLog;
	public int logLevelLimit;
	private CommandLogCapture logCapture;
	
	public Account account;
	public String userHome;
	public VarBUILDMODE buildMode = VarBUILDMODE.UNKNOWN;

	// generic settings
	public boolean CTX_TRACEINTERNAL;
	public boolean CTX_TRACE;
	public boolean CTX_SHOWONLY;
	public boolean CTX_SHOWALL;
	public boolean CTX_FORCE;
	public boolean CTX_SKIPERRORS;
	public boolean CTX_ALL;
	public boolean CTX_LOCAL;
	public boolean CTX_OFFLINE;
	public int CTX_TIMEOUT;
	public String CTX_KEYNAME = "";
	public String CTX_DISTPATH = "";
	public String CTX_REDISTWIN_PATH = "";
	public String CTX_REDISTLINUX_PATH = "";
	public String CTX_HIDDENPATH = "";

	// specific settings
	public boolean CTX_GET;
	public boolean CTX_DIST;
	public boolean CTX_UPDATENEXUS;
	public boolean CTX_CHECK;
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
	public String CTX_SEGMENT = "";
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

	public CommandContext( ServerEngine engine , ServerSession session , CommandOptions options , String stream , ServerCall call ) {
		this.engine = engine;
		this.session = session;
		
		this.options = options;
		this.stream = stream;
		this.call = call;
		
		this.logLevelLimit = CommandOutput.LOGLEVEL_ERROR;
		
		logCapture = new CommandLogCapture();
		setLogStream();
		setLogLevel();
	}

	public CommandContext( CommandContext context , String stream ) {
		if( stream == null || stream.isEmpty() )
			this.stream = context.stream;
		else
			this.stream = stream;
		
		// copy all properties
		this.engine = context.engine;
		this.session = context.session;
		
		this.options = context.options;
		this.env = context.env;
		this.sg = context.sg;

		this.call = context.call;
		this.account = context.account;
		this.userHome = context.userHome;
		this.buildMode = context.buildMode;
		this.logLevelLimit = context.logLevelLimit;
		this.logCapture = context.logCapture;

		// generic
		this.CTX_TRACEINTERNAL = context.CTX_TRACEINTERNAL;
		this.CTX_TRACE = context.CTX_TRACE;
		this.CTX_SHOWONLY = context.CTX_SHOWONLY;
		this.CTX_SHOWALL = context.CTX_SHOWALL;
		this.CTX_FORCE = context.CTX_FORCE;
		this.CTX_SKIPERRORS = context.CTX_SKIPERRORS;
		this.CTX_ALL = context.CTX_ALL;
		this.CTX_LOCAL = context.CTX_LOCAL;
		this.CTX_OFFLINE = context.CTX_OFFLINE;
		this.CTX_TIMEOUT = context.CTX_TIMEOUT;
		this.CTX_KEYNAME = context.CTX_KEYNAME;
		this.CTX_DISTPATH = context.CTX_DISTPATH;
		this.CTX_REDISTWIN_PATH = context.CTX_REDISTWIN_PATH;
		this.CTX_REDISTLINUX_PATH = context.CTX_REDISTLINUX_PATH;
		this.CTX_HIDDENPATH = context.CTX_HIDDENPATH;
		
		// specific
		this.CTX_GET = context.CTX_GET;
		this.CTX_DIST = context.CTX_DIST;
		this.CTX_UPDATENEXUS = context.CTX_UPDATENEXUS;
		this.CTX_CHECK = context.CTX_CHECK;
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
		this.CTX_SEGMENT = context.CTX_SEGMENT;
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

	private void setLogStream() {
		streamLog = ( call != null )? "[" + stream + "," + call.sessionContext.sessionId + "]" : "[" + stream + "]";
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
	
	public void setOptions( ActionBase action , Meta meta , CommandOptions options ) throws Exception {
		this.options = options;
		update( action , meta );
	}
	
	public void update( ActionBase action , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		this.env = env;  
		this.sg = sg;
		update( action , env.meta );
	}

	public void update( ActionBase action ) throws Exception {
		Meta meta = ( session != null && session.product )? action.getContextMeta() : null;
		update( action , meta );
	}
	
	public void update( ActionBase action , Meta meta ) throws Exception {
		this.meta = meta;
		
		boolean isproduct = ( meta != null )? true : false; 
		boolean isenv = ( env == null )? false : true; 
		boolean def = ( isenv && env.isProd() )? true : false;
		String value;
		
		// generic
		MetaProductSettings product = ( isproduct )? meta.getProductSettings( action ) : null; 
		CTX_TRACEINTERNAL = ( getFlagValue( "OPT_TRACE" ) && getFlagValue( "OPT_SHOWALL" ) )? true : false;
		CTX_TRACE = getFlagValue( "OPT_TRACE" );
		CTX_SHOWONLY = combineValue( "OPT_SHOWONLY" , ( isenv )? env.SHOWONLY : null , def );
		CTX_SHOWALL = getFlagValue( "OPT_SHOWALL" );
		if( CTX_TRACE )
			CTX_SHOWALL = true;
		CTX_FORCE = getFlagValue( "OPT_FORCE" );
		CTX_SKIPERRORS = getFlagValue( "OPT_SKIPERRORS" );
		CTX_ALL = getFlagValue( "OPT_ALL" );
		CTX_LOCAL = getFlagValue( "OPT_LOCAL" );
		CTX_OFFLINE = getFlagValue( "OPT_OFFLINE" );
		CTX_TIMEOUT = getIntParamValue( "OPT_TIMEOUT" , options.optDefaultCommandTimeout ) * 1000;
		value = getParamValue( "OPT_KEY" ); 
		CTX_KEYNAME = ( value.isEmpty() )? ( ( isenv )? env.KEYFILE : "" ) : value;
		String productValue = ( isproduct )? product.CONFIG_DISTR_PATH : "";
		CTX_DISTPATH = getParamPathValue( "OPT_DISTPATH" , productValue );
		CTX_REDISTWIN_PATH = ( isproduct )? product.CONFIG_REDISTWIN_PATH : null;
		if( isenv && !env.REDISTWIN_PATH.isEmpty() )
			CTX_REDISTWIN_PATH = env.REDISTWIN_PATH;
		CTX_REDISTLINUX_PATH = ( isproduct )? product.CONFIG_REDISTLINUX_PATH : null;
		if( isenv && !env.REDISTLINUX_PATH.isEmpty() )
			CTX_REDISTLINUX_PATH = env.REDISTLINUX_PATH;
		value = getParamPathValue( "OPT_HIDDENPATH" );
		CTX_HIDDENPATH = ( value.isEmpty() )? ( ( isenv )? env.CONF_SECRETFILESPATH : "" ) : value;
		
		// specific
		CTX_GET = getFlagValue( "OPT_GET" );
		CTX_DIST = getFlagValue( "OPT_DIST" );
		CTX_UPDATENEXUS = getFlagValue( "OPT_UPDATENEXUS" );
		CTX_CHECK = getFlagValue( "OPT_CHECK" , false );
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
		CTX_SEGMENT = getParamValue( "OPT_SG" );
		CTX_DEPLOYGROUP = getParamValue( "OPT_DEPLOYGROUP" );
		CTX_STARTGROUP = getParamValue( "OPT_STARTGROUP" );
		CTX_EXTRAARGS = getParamValue( "OPT_EXTRAARGS" );
		CTX_UNIT = getParamValue( "OPT_UNIT" );
		CTX_BUILDINFO = getParamValue( "OPT_BUILDINFO" );
		CTX_HOSTUSER = getParamValue( "OPT_HOSTUSER" );
		CTX_NEWKEY = getParamValue( "OPT_NEWKEY" );
		CTX_BUILDMODE = Types.getBuildMode( getParamValue( "OPT_BUILDMODE" ) , false );
		CTX_OLDRELEASE = getParamValue( "OPT_COMPATIBILITY" );
		CTX_PORT = getIntParamValue( "OPT_PORT" , -1 );
		CTX_HOST = getParamValue( "OPT_HOST" );
		
		setLogStream();
		setLogLevel();
	}

	public void loadEnv( ActionBase action , boolean loadProps ) throws Exception {
		if( session.ENV.isEmpty() )
			return;
		
		String useSG = session.SG;
		if( useSG.isEmpty() )
			useSG = CTX_SEGMENT;
		loadEnv( action , session.ENV , useSG , loadProps );
	}
	
	public void loadEnv( ActionBase action , String ENV , String SG , boolean loadProps ) throws Exception {
		Meta meta = action.getContextMeta();
		env = meta.getEnvData( action , ENV , loadProps );
		
		if( SG == null || SG.isEmpty() ) {
			sg = null;
			return;
		}
		
		sg = env.getSG( action , SG );
		update( action );
	}
	
	public CommandContext getStreamContext( String stream ) {
		CommandContext context = new CommandContext( this , stream );
		return( context );
	}
	
	public String getBuildModeName() {
		return( Common.getEnumLower( buildMode ) );
	}
	
	public boolean setRunContext() {
		if( session == null )
			return( true );
		
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
		if( !session.productName.isEmpty() )
			contextInfo = "product=" + session.productName;
		if( buildMode != VarBUILDMODE.UNKNOWN )
			contextInfo += ", buildMode=" + getBuildModeName();
		if( !session.ENV.isEmpty() )
			contextInfo += ", env=" + session.ENV;
		if( !session.SG.isEmpty() )
			contextInfo += ", sg=" + session.SG;
		return( contextInfo );
	}
	
	public void setBuildMode( VarBUILDMODE value ) throws Exception {
		if( buildMode != VarBUILDMODE.UNKNOWN && buildMode != value ) {
			String name = getBuildModeName();
			Common.exit1( _Error.ReleaseWrongBuildMode1 , "release is defined for " + name + " build mode, please use appropriate context" , name );
		}
		
		buildMode = value;
	}

	public boolean getFlagValue( String var ) throws Exception {
		return( getFlagValue( var , false ) );
	}
	
	public boolean getFlagValue( String var , boolean defValue ) throws Exception {
		if( !options.isFlagVar( var ) )
			Common.exit1( _Error.UnknownFlagVar1 , "unknown flag var=" + var , var );
		return( options.getFlagValue( var , defValue ) );
	}

	public String getEnumValue( String var ) throws Exception {
		if( !options.isEnumVar( var ) )
			Common.exit1( _Error.UnknownEnumVar1 , "unknown enum var=" + var , var );
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
			Common.exit1( _Error.UnknownParamVar1 , "unknown param var=" + var , var );
		return( options.getParamValue( var ) );
	}		

	public int getIntParamValue( String var , int defaultValue ) throws Exception {
		if( !options.isParamVar( var ) )
			Common.exit1( _Error.UnknownParamVar1 , "unknown param var=" + var , var );
		return( options.getIntParamValue( var , defaultValue ) );
	}

	public boolean combineValue( String var , FLAG confValue , boolean defValue ) throws Exception {
		if( !options.isValidVar( var ) )
			Common.exit1( _Error.UnknownParamVar1 , "unknown param var=" + var , var );
		return( options.combineValue( var , confValue , defValue ) );
	}
	
	public int logStartCapture() {
		return( logCapture.logStartCapture() );
	}

	public void logStopCapture() {
		logCapture.logStopCapture();
	}
	
	public String[] logFinishCapture( int startIndex ) {
		return( logCapture.logFinishCapture( startIndex ) );
	}

	public void outExact( String s ) {
		logCapture.outExact( s );
	}
	
}
