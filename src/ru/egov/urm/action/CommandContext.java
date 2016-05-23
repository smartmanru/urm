package ru.egov.urm.action;

import ru.egov.urm.Common;
import ru.egov.urm.RunContext;
import ru.egov.urm.action.CommandOptions.SQLMODE;
import ru.egov.urm.action.CommandOptions.SQLTYPE;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutorPool;
import ru.egov.urm.storage.LocalFolder;

public class CommandContext {
	
	public ShellExecutorPool pool;
	
	public String streamName;
	public Account account;
	public String userHome;
	public String productHome;
	public VarBUILDMODE buildMode = VarBUILDMODE.UNKNOWN;
	public String ENV;
	public MetaEnv env; 
	public String DC;
	public MetaEnvDC dc;

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

	public CommandContext() {
		this.streamName = "main";
	}

	public CommandContext( CommandContext context , String stream ) {
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
		this.env = context.env;
		this.dc = context.dc;

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
		action.options.updateContext( action );
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

}
