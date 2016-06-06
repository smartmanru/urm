package org.urm.server.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.meta.Metadata.VarCATEGORY;

public class CommandOptions {

	public enum FLAG { DEFAULT , YES , NO }; 

	public enum SQLMODE { UNKNOWN , APPLY , ANYWAY , CORRECT , ROLLBACK , PRINT };
	public enum SQLTYPE { UNKNOWN , SQL , CTL , PUB };

	static int optDefaultCommandTimeout = 10;
	
	// standard command parameters 
	public String command;
	public String action;

	// implementation
	List<CommandVar> optionsDefined = new LinkedList<CommandVar>();
	List<CommandVar> optionsSet = new LinkedList<CommandVar>();
	Map<String,CommandVar> optionsByName = new HashMap<String,CommandVar>();
	Map<String,CommandVar> varByName = new HashMap<String,CommandVar>();
	
	protected Map<String,FLAG> flags = new HashMap<String,FLAG>();
	protected Map<String,String> enums = new HashMap<String,String>();
	protected Map<String,String> params = new HashMap<String,String>();
	protected List<String> args = new LinkedList<String>();
	private int genericOptionsCount;

	public CommandOptions() {
		optionsDefined = new LinkedList<CommandVar>();
		optionsSet = new LinkedList<CommandVar>();
		optionsByName = new HashMap<String,CommandVar>();
		
		flags = new HashMap<String,FLAG>();
		enums = new HashMap<String,String>();
		params = new HashMap<String,String>();
		args = new LinkedList<String>();
		
		defineGenericOption( CommandVar.newFlagYesOption( "trace" , "GETOPT_TRACE" , "show commands" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "showall" , "GETOPT_SHOWALL" , "show all log records" ) );
		defineGenericOption( CommandVar.newFlagNoOption( "showmain" , "GETOPT_SHOWALL" , "show only important log records (see also showall)" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "showonly" , "GETOPT_SHOWONLY" , "do not perform builds or change distributive" ) );
		defineGenericOption( CommandVar.newFlagNoOption( "execute" , "GETOPT_SHOWONLY" , "execute operations (see also showonly)" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "force" , "GETOPT_FORCE" , "ignore errors and constraints" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "ignore" , "GETOPT_SKIPERRORS" , "continue run disregarding errors" ) );
		defineGenericOption( CommandVar.newFlagNoOption( "strict" , "GETOPT_SKIPERRORS" , "stop execution after error" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "all" , "GETOPT_ALL" , "use all possible items in scope, ignore reduce defaults" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "local" , "GETOPT_LOCAL" , "any session is opened as local under current user" ) );
		defineGenericOption( CommandVar.newParam( "timeout" , "GETOPT_COMMANDTIMEOUT" , "use specific default timeout" ) );
		defineGenericOption( CommandVar.newParam( "key" , "GETOPT_KEY" , "use given key to connect to host" ) );
		defineGenericOption( CommandVar.newParam( "etcpath" , "GETOPT_ETCPATH" , "use given path to find metedata files" ) );
		defineGenericOption( CommandVar.newParam( "distpath" , "GETOPT_DISTPATH" , "use given path to find distributive files" ) );
		defineGenericOption( CommandVar.newParam( "hiddenpath" , "GETOPT_HIDDENPATH" , "use given path to find hidden files and properties" ) );
		defineGenericOption( CommandVar.newParam( "workpath" , "GETOPT_WORKPATH" , "use given path to store working files and logs" ) );
		genericOptionsCount = optionsDefined.size();
		
		defineOption( CommandVar.newFlagYesOption( "get" , "GETOPT_GET" , "run getall after build" ) );
		defineOption( CommandVar.newFlagYesOption( "dist" , "GETOPT_DIST" , "copy to distributive after getall" ) );
		defineOption( CommandVar.newFlagYesOption( "updatenexus" , "GETOPT_UPDATENEXUS" , "force reupload items to thirdparty repository" ) );
		defineOption( CommandVar.newFlagYesOption( "check" , "GETOPT_CHECK" , "run source checks before build" ) );
		defineOption( CommandVar.newFlagNoOption( "nocheck" , "GETOPT_CHECK" , "skip source checks before build (see also check)" ) );
		defineOption( CommandVar.newFlagYesOption( "move" , "GETOPT_MOVE_ERRORS" , "move wrong release source files to error folder" ) );
		defineOption( CommandVar.newFlagYesOption( "replace" , "GETOPT_REPLACE" , "replace all item contents on deploy" ) );
		defineOption( CommandVar.newFlagYesOption( "backup" , "GETOPT_BACKUP" , "prepare backup before deploy" ) );
		defineOption( CommandVar.newFlagNoOption( "nobackup" , "GETOPT_BACKUP" , "do not backup before deploy" ) );
		defineOption( CommandVar.newFlagYesOption( "obsolete" , "GETOPT_OBSOLETE" , "ignore new layout" ) );
		defineOption( CommandVar.newFlagNoOption( "noobsolete" , "GETOPT_OBSOLETE" , "use new layout" ) );
		defineOption( CommandVar.newFlagYesOption( "conf" , "GETOPT_DEPLOYCONF" , "deploy configuration files" ) );
		defineOption( CommandVar.newFlagNoOption( "noconf" , "GETOPT_DEPLOYCONF" , "do not deploy configuration files" ) );
		defineOption( CommandVar.newFlagYesOption( "partialconf" , "GETOPT_PARTIALCONF" , "ignore missing configuration files" ) );
		defineOption( CommandVar.newFlagYesOption( "binary" , "GETOPT_DEPLOYBINARY" , "deploy binary files" ) );
		defineOption( CommandVar.newFlagNoOption( "nobinary" , "GETOPT_DEPLOYBINARY" , "do not deploy binary files" ) );
		defineOption( CommandVar.newFlagYesOption( "hot" , "GETOPT_DEPLOYHOT" , "deploy hot files only" ) );
		defineOption( CommandVar.newFlagYesOption( "cold" , "GETOPT_DEPLOYCOLD" , "deploy cold files only" ) );
		defineOption( CommandVar.newFlagYesOption( "raw" , "GETOPT_DEPLOYRAW" , "internal use only" ) );
		defineOption( CommandVar.newFlagYesOption( "keepalive" , "GETOPT_KEEPALIVE" , "automatically maintain product configuration set" ) );
		defineOption( CommandVar.newFlagNoOption( "nokeepalive" , "GETOPT_KEEPALIVE" , "do not change product configuration set" ) );
		defineOption( CommandVar.newFlagNoOption( "downtime" , "GETOPT_ZERODOWNTIME" , "deploy with downtime" ) );
		defineOption( CommandVar.newFlagYesOption( "nodowntime" , "GETOPT_ZERODOWNTIME" , "deploy without downtime if possible" ) );
		defineOption( CommandVar.newFlagYesOption( "nonodes" , "GETOPT_NONODES" , "execute only on server-level, no nodes" ) );
		defineOption( CommandVar.newFlagYesOption( "nomsg" , "GETOPT_NOCHATMSG" , "do not notify in chat window" ) );
		defineOption( CommandVar.newFlagYesOption( "root" , "GETOPT_ROOTUSER" , "execute under root" ) );
		defineOption( CommandVar.newFlagYesOption( "sudo" , "GETOPT_SUDO" , "execute using sudo from specified hostuser" ) );
		defineOption( CommandVar.newFlagYesOption( "ignoreversion" , "GETOPT_IGNOREVERSION" , "ignore version information on deploy" ) );
		defineOption( CommandVar.newFlagYesOption( "live" , "GETOPT_LIVE" , "use saved live configuration" ) );
		defineOption( CommandVar.newFlagYesOption( "hidden" , "GETOPT_HIDDEN" , "use hidden files to restore configuration" ) );
		defineOption( CommandVar.newFlagEnumOption( "a" , "APPLY" , "GETOPT_DBMODE" , "execute database set - only new scipts" ) );
		defineOption( CommandVar.newFlagEnumOption( "x" , "ANYWAY" , "GETOPT_DBMODE" , "execute database set - both already applied and new scipts" ) );
		defineOption( CommandVar.newFlagEnumOption( "c" , "CORRECT" , "GETOPT_DBMODE" , "execute database set - only failed scripts" ) );
		defineOption( CommandVar.newFlagEnumOption( "r" , "ROLLBACK" , "GETOPT_DBMODE" , "execute database set - rollback" ) );
		defineOption( CommandVar.newFlagEnumOption( "p" , "PRINT" , "GETOPT_DBMODE" , "execute database set - show database status" ) );
		defineOption( CommandVar.newFlagYesOption( "m" , "GETOPT_DBMOVE" , "move erroneous scripts to error subfolder in source folder" ) );
		defineOption( CommandVar.newFlagYesOption( "auth" , "GETOPT_DBAUTH" , "do not use simple authorization" ) );
		defineOption( CommandVar.newFlagNoOption( "noauth" , "GETOPT_DBAUTH" , "use simple authorization" ) );
		defineOption( CommandVar.newFlagYesOption( "cumulative" , "GETOPT_CUMULATIVE" , "cumulative release" ) );
		defineOption( CommandVar.newParam( "aligned" , "GETOPT_DBALIGNED" , "use specific aligned set of scipts to apply" ) );
		defineOption( CommandVar.newParam( "db" , "GETOPT_DB" , "use specific database server to apply" ) );
		defineOption( CommandVar.newParam( "dbpwd" , "GETOPT_DBPASSWORD" , "use specified password to access database" ) );
		defineOption( CommandVar.newParam( "regions" , "GETOPT_REGIONS" , "use specific set of regions to apply" ) );
		defineOption( CommandVar.newFlagEnumOption( "sql" , "SQL" , "GETOPT_DBTYPE" , "execute database set - only scripts" ) );
		defineOption( CommandVar.newFlagEnumOption( "ctl" , "CTL" , "GETOPT_DBTYPE" , "execute database set - only load files" ) );
		defineOption( CommandVar.newFlagEnumOption( "pub" , "PUB" , "GETOPT_DBTYPE" , "execute database set - only publish files" ) );
		defineOption( CommandVar.newParam( "release" , "GETOPT_RELEASE" , "use specific release name" ) );
		defineOption( CommandVar.newParam( "branch" , "GETOPT_BRANCH" , "use specific codebase branch name" ) );
		defineOption( CommandVar.newParam( "tag" , "GETOPT_TAG" , "use specific codebase tag name" ) );
		defineOption( CommandVar.newParam( "date" , "GETOPT_DATE" , "use codebase state on given date (ISO-8601)" ) );
		defineOption( CommandVar.newParam( "group" , "GETOPT_GROUP" , "use specific codebase project group" ) );
		defineOption( CommandVar.newParam( "version" , "GETOPT_VERSION" , "use specific codebase version" ) );
		defineOption( CommandVar.newParam( "dc" , "GETOPT_DC" , "use datacenters which names meet given regular mask" ) );
		defineOption( CommandVar.newParam( "deploygroup" , "GETOPT_DEPLOYGROUP" , "use only nodes belonging to specified deploygroup" ) );
		defineOption( CommandVar.newParam( "startgroup" , "GETOPT_STARTGROUP" , "use only servers belonging to specified startgroup" ) );
		defineOption( CommandVar.newParam( "args" , "GETOPT_EXTRAARGS" , "extra arguments for server interface scripts" ) );
		defineOption( CommandVar.newParam( "unit" , "GETOPT_UNIT" , "use distributive items only from given unit" ) );
		defineOption( CommandVar.newParam( "buildinfo" , "GETOPT_BUILDINFO" , "use given build info parameter" ) );
		defineOption( CommandVar.newParam( "hostuser" , "GETOPT_HOSTUSER" , "use given user when connecting to host" ) );
		defineOption( CommandVar.newParam( "newkey" , "GETOPT_NEWKEY" , "use given key to change on host" ) );
		defineOption( CommandVar.newParam( "mode" , "GETOPT_BUILDMODE" , "use given build mode (branch, trunk, major, devbranch, devtrunk)" ) );
		defineOption( CommandVar.newParam( "over" , "GETOPT_COMPATIBILITY" , "previous release installed" ) );
	}

	public String getMetaPath( ActionBase action ) throws Exception {
		return( getParamValue( action , "GETOPT_ETCPATH" ) ); 
	}
	
	public void updateContext( ActionBase action ) throws Exception {
		boolean isproduct = ( action.meta == null || action.meta.product == null )? false : true; 
		boolean isenv = ( action.context.env == null )? false : true; 
		boolean def = ( isenv && action.context.env.PROD )? true : false;
		String value;
		
		CommandContext ctx = action.context;
		
		// generic
		ctx.CTX_TRACEINTERNAL = ( getFlagValue( action , "GETOPT_TRACE" ) && getFlagValue( action , "GETOPT_SHOWALL" ) )? true : false;
		ctx.CTX_TRACE = getFlagValue( action , "GETOPT_TRACE" );
		ctx.CTX_SHOWONLY = combineValue( action , "GETOPT_SHOWONLY" , ( isenv )? action.context.env.SHOWONLY : null , def );
		ctx.CTX_SHOWALL = getFlagValue( action , "GETOPT_SHOWALL" );
		if( ctx.CTX_TRACE )
			ctx.CTX_SHOWALL = true;
		ctx.CTX_FORCE = getFlagValue( action , "GETOPT_FORCE" );
		ctx.CTX_IGNORE = getFlagValue( action , "GETOPT_SKIPERRORS" );
		ctx.CTX_ALL = getFlagValue( action , "GETOPT_ALL" );
		ctx.CTX_LOCAL = getFlagValue( action , "GETOPT_LOCAL" );
		ctx.CTX_COMMANDTIMEOUT = getIntParamValue( action , "GETOPT_COMMANDTIMEOUT" , optDefaultCommandTimeout ) * 1000;
		value = getParamValue( action , "GETOPT_KEY" ); 
		ctx.CTX_KEYNAME = ( value.isEmpty() )? ( ( isenv )? action.context.env.KEYNAME : "" ) : value;
		String productValue = ( isproduct )? action.meta.product.CONFIG_DISTR_PATH : "";
		ctx.CTX_DISTPATH = getParamPathValue( action , "GETOPT_DISTPATH" , productValue );
		ctx.CTX_REDISTPATH = ( isproduct )? action.meta.product.CONFIG_REDISTPATH : null;
		if( isenv && !action.context.env.REDISTPATH.isEmpty() )
			ctx.CTX_REDISTPATH = action.context.env.REDISTPATH;
		value = getParamPathValue( action , "GETOPT_HIDDENPATH" );
		ctx.CTX_HIDDENPATH = ( value.isEmpty() )? ( ( isenv )? action.context.env.CONF_SECRETFILESPATH : "" ) : value;
		ctx.CTX_WORKPATH = getParamPathValue( action , "GETOPT_WORKPATH" , "" );
		
		// specific
		ctx.CTX_GET = getFlagValue( action , "GETOPT_GET" );
		ctx.CTX_DIST = getFlagValue( action , "GETOPT_DIST" );
		ctx.CTX_UPDATENEXUS = getFlagValue( action , "GETOPT_UPDATENEXUS" );
		ctx.CTX_CHECK = getFlagValue( action , "GETOPT_CHECK" , false );
		ctx.CTX_MOVE_ERRORS = getFlagValue( action , "GETOPT_MOVE_ERRORS" );
		ctx.CTX_REPLACE = getFlagValue( action , "GETOPT_REPLACE" );
		ctx.CTX_BACKUP = combineValue( action , "GETOPT_BACKUP" , ( isenv )? action.context.env.BACKUP : null , def );
		ctx.CTX_OBSOLETE = combineValue( action , "GETOPT_OBSOLETE" , ( isenv )? action.context.env.OBSOLETE : null , true );
		ctx.CTX_CONFDEPLOY = combineValue( action , "GETOPT_DEPLOYCONF" , ( isenv )? action.context.env.CONF_DEPLOY : null , true );
		ctx.CTX_PARTIALCONF = getFlagValue( action , "GETOPT_PARTIALCONF" );
		ctx.CTX_DEPLOYBINARY = getFlagValue( action , "GETOPT_DEPLOYBINARY" , true );
		ctx.CTX_DEPLOYHOT = getFlagValue( action , "GETOPT_DEPLOYHOT" );
		ctx.CTX_DEPLOYCOLD = getFlagValue( action , "GETOPT_DEPLOYCOLD" );
		ctx.CTX_DEPLOYRAW = getFlagValue( action , "GETOPT_DEPLOYRAW" );
		ctx.CTX_CONFKEEPALIVE = combineValue( action , "GETOPT_KEEPALIVE" , ( isenv )? action.context.env.CONF_KEEPALIVE : null , true );
		ctx.CTX_ZERODOWNTIME = getFlagValue( action , "GETOPT_ZERODOWNTIME" );
		ctx.CTX_NONODES = getFlagValue( action , "GETOPT_NONODES" );
		ctx.CTX_NOCHATMSG = getFlagValue( action , "GETOPT_NOCHATMSG" );
		ctx.CTX_ROOTUSER = getFlagValue( action , "GETOPT_ROOTUSER" );
		ctx.CTX_SUDO = getFlagValue( action , "GETOPT_SUDO" );
		ctx.CTX_IGNOREVERSION = getFlagValue( action , "GETOPT_IGNOREVERSION" );
		ctx.CTX_LIVE = getFlagValue( action , "GETOPT_LIVE" );
		ctx.CTX_HIDDEN = getFlagValue( action , "GETOPT_HIDDEN" );
		value = getEnumValue( action , "GETOPT_DBMODE" );
		ctx.CTX_DBMODE = ( value.isEmpty() )? SQLMODE.UNKNOWN : SQLMODE.valueOf( value );
		ctx.CTX_DBMOVE = getFlagValue( action , "GETOPT_DBMOVE" );
		ctx.CTX_DBAUTH = combineValue( action , "GETOPT_DBAUTH" , ( isenv )? action.context.env.DB_AUTH : null , false );
		ctx.CTX_CUMULATIVE = getFlagValue( action , "GETOPT_CUMULATIVE" );
		
		ctx.CTX_DBALIGNED = getParamValue( action , "GETOPT_DBALIGNED" );
		ctx.CTX_DB = getParamValue( action , "GETOPT_DB" );
		ctx.CTX_DBPASSWORD = getParamValue( action , "GETOPT_DBPASSWORD" );
		ctx.CTX_REGIONS = getParamValue( action , "GETOPT_REGIONS" );
		value = getEnumValue( action , "GETOPT_DBTYPE" );
		ctx.CTX_DBTYPE = ( value.isEmpty() )? SQLTYPE.UNKNOWN : SQLTYPE.valueOf( value );
		ctx.CTX_RELEASELABEL = getParamValue( action , "GETOPT_RELEASE" );
		ctx.CTX_BRANCH = getParamValue( action , "GETOPT_BRANCH" );
		ctx.CTX_TAG = getParamValue( action , "GETOPT_TAG" );
		ctx.CTX_DATE = getParamValue( action , "GETOPT_DATE" );
		ctx.CTX_GROUP = getParamValue( action , "GETOPT_GROUP" );
		ctx.CTX_VERSION = getParamValue( action , "GETOPT_VERSION" );
		ctx.CTX_DC = getParamValue( action , "GETOPT_DC" );
		ctx.CTX_DEPLOYGROUP = getParamValue( action , "GETOPT_DEPLOYGROUP" );
		ctx.CTX_STARTGROUP = getParamValue( action , "GETOPT_STARTGROUP" );
		ctx.CTX_EXTRAARGS = getParamValue( action , "GETOPT_EXTRAARGS" );
		ctx.CTX_UNIT = getParamValue( action , "GETOPT_UNIT" );
		ctx.CTX_BUILDINFO = getParamValue( action , "GETOPT_BUILDINFO" );
		ctx.CTX_HOSTUSER = getParamValue( action , "GETOPT_HOSTUSER" );
		ctx.CTX_NEWKEY = getParamValue( action , "GETOPT_NEWKEY" );
		ctx.CTX_BUILDMODE = action.meta.getBuildMode( action , getParamValue( action , "GETOPT_BUILDMODE" ) );
		ctx.CTX_OLDRELEASE = getParamValue( action , "GETOPT_COMPATIBILITY" );
		
		action.setTimeout( ctx.CTX_COMMANDTIMEOUT );
		
		int logLevelLimit = CommandOutput.LOGLEVEL_INFO;
		if( ctx.CTX_TRACE ) {
			if( ctx.CTX_TRACEINTERNAL )
				logLevelLimit = CommandOutput.LOGLEVEL_INTERNAL;
			else
				logLevelLimit = CommandOutput.LOGLEVEL_TRACE;
		}
		else
		if( ctx.CTX_SHOWALL )
			logLevelLimit = CommandOutput.LOGLEVEL_DEBUG;
		
		action.output.setLogLevel( logLevelLimit );
	}

	void print( String s ) {
		System.out.println( s );
	}

	private void printhelp( String s ) {
		print( "# " + s );
	}

	public void printRunningOptions( ActionBase action ) throws Exception {
		String values = "";
		for( CommandVar option : optionsSet ) {
			String value = getOptionValue( option );
			values = Common.addToList( values , value , ", " );
		}
		
		String info = "execute options={" + values + "}, args={" + 
				Common.getList( args.toArray( new String[0] ) , ", " ) + "}";
		action.commentExecutor( info );
	}
	
	public String getOptionValue( CommandVar var ) {
		String value;
		if( var.isFlag )
			value = Common.getEnumLower( flags.get( var.varName ) );
		else
		if( var.isEnum )
			value = enums.get( var.varName );
		else
			value = params.get( var.varName );
		return( var.varName + "=" + value );
	}
	
	public void defineGenericOption( CommandVar var ) {
		defineOption( var );
		var.setGeneric();
	}
	
	public void defineOption( CommandVar var ) {
		optionsDefined.add( var );
		optionsByName.put( var.optName , var );
		if( !varByName.containsKey( var.varName ) )
			varByName.put( var.varName , var );
	}

	public boolean isValidVar( String var ) {
		return( varByName.containsKey( var ) );
	}
	
	public boolean isFlagOption( String opt ) {
		CommandVar info = optionsByName.get( opt ); 
		if( info != null )
			if( info.isFlag )
				return( true );
		
		return( false );
	}
	
	public boolean isFlagVar( String var ) {
		CommandVar info = varByName.get( var ); 
		if( info != null )
			if( info.isFlag )
				return( true );
		
		return( false );
	}
	
	public boolean isEnumOption( String opt ) {
		CommandVar info = optionsByName.get( opt ); 
		if( info != null )
			if( info.isEnum )
				return( true );
		
		return( false );
	}
	
	public boolean isEnumVar( String var ) {
		CommandVar info = varByName.get( var ); 
		if( info != null )
			if( info.isEnum )
				return( true );
		
		return( false );
	}
	
	public boolean isParamOption( String opt ) {
		CommandVar info = optionsByName.get( opt ); 
		if( info != null )
			if( info.isParam )
				return( true );
		
		return( false );
	}
	
	public boolean isParamVar( String var ) {
		CommandVar info = varByName.get( var ); 
		if( info != null )
			if( info.isParam )
				return( true );
		
		return( false );
	}
	
	public boolean addFlagOption( String opt ) {
		if( !isFlagOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a flag" );
		
		CommandVar info = optionsByName.get( opt );
		
		if( flags.get( info.varName ) != null ) {
			print( "flag=" + info.varName + " is already set" );
			return( false );
		}
		
		flags.put( info.varName , info.varValue );
		optionsSet.add( info );
		
		return( true );
	}

	public boolean addEnumOption( String opt ) {
		if( !isEnumOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a enum" );
		
		CommandVar info = optionsByName.get( opt );
		
		if( enums.get( info.varName ) != null ) {
			print( "enum=" + info.varName + " is already set" );
			return( false );
		}
		
		enums.put( info.varName , info.varEnumValue );
		optionsSet.add( info );
		
		return( true );
	}

	public boolean addParamOption( String opt , String value ) {
		if( !isParamOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a parameter" );
		
		CommandVar info = optionsByName.get( opt );
		
		if( params.get( info.varName ) != null ) {
			print( "parameter=" + info.varName + " is already set" );
			return( false );
		}
		
		params.put( info.varName , value );
		optionsSet.add( info );

		return( true );
	}

	public boolean addArg( String value ) {
		args.add( value );
		return( true );
	}
	
	private void showOptionHelp( CommandVar var ) {
		String identity;
		if( var.isFlag )
			identity = "-" + var.optName + ": flag " + var.varName + "=" + var.varValue;
		else
		if( var.isEnum )
			identity = "-" + var.optName + ": enum " + var.varName + "=" + var.varEnumValue;
		else
			identity = "-" + var.optName + ": parameter";
		
		String spacing = Common.replicate( " " , 50 - identity.length() ); 
		printhelp( "\t" + identity + spacing + var.help ); 
	}
	
	public void showTopHelp( CommandExecutor executor ) {
		printhelp( "All options defined for command " + command + ":" );
		printhelp( "Generic options:" );
		for( int k = 0; k < genericOptionsCount; k++ ) {
			CommandVar var = optionsDefined.get( k );
			showOptionHelp( var );
		}
		
		printhelp( "Specific options:" );
		boolean specific = false;
		for( int k = genericOptionsCount; k < optionsDefined.size(); k++ ) {
			CommandVar var = optionsDefined.get( k );
			if( !executor.isOptionApplicaple( var ) )
				continue;
			
			showOptionHelp( var );
			specific = true;
		}
		if( !specific )
			printhelp( "\t(no specific options)" );
	}
	
	public void showActionOptionsHelp( CommandAction action ) {
		printhelp( "All options defined for " + action.name + ":" );
		printhelp( "Generic options:" );
		for( int k = 0; k < genericOptionsCount; k++ ) {
			CommandVar var = optionsDefined.get( k );
			showOptionHelp( var );
		}

		printhelp( "Specific options:" );
		boolean specific = false;
		for( int k = genericOptionsCount; k < optionsDefined.size(); k++ ) {
			CommandVar var = optionsDefined.get( k );
			if( action.isOptionApplicable( var ) ) {
				showOptionHelp( var );
				specific = true;
			}
		}

		if( !specific )
			printhelp( "\t(no specific options)" );
	}
	
	public boolean parseArgs( String[] cmdParams , boolean manualActions ) {
		if( cmdParams.length < 2 ) {
			command = "help";
			return( false );
		}
		
		// first item is command
		command = cmdParams[0];
		
		int k = 1;
		if( !manualActions ) {
			action = cmdParams[1];
			k++;
		}

		// next items are options
		for( ; k < cmdParams.length; k++ ) {
			String arg = cmdParams[k];
			if( !arg.startsWith( "-" ) )
				break;
			
			arg = arg.substring( 1 );
			if( isFlagOption( arg ) ) {
				if( !addFlagOption( arg ) )
					return( false );
			}
			else if( isEnumOption( arg ) ) {
				if( !addEnumOption( arg ) )
					return( false );
			}
			else if( isParamOption( arg ) ) {
				k++;
				if( k >= cmdParams.length ) {
					print( "invalid arguments - parameter=" + arg + " has no value defined" );
					return( false );
				}
				
				String value = cmdParams[k];
				if( !addParamOption( arg , value ) )
					return( false );
			}
			else {
				print( "invalid arguments - unknown option=" + arg );
				return( false );
			}
		}

		// next items are args
		for( ; k < cmdParams.length; k++ ) {
			String arg = cmdParams[k];
			if( !addArg( arg ) )
				return( false );
		}

		// check scripts
		return( true );
	}

	public boolean getFlagValue( ActionBase action , String var ) throws Exception {
		return( getFlagValue( action , var , false ) );
	}
	
	public boolean getFlagValue( ActionBase action , String var , boolean defValue ) throws Exception {
		if( !isFlagVar( var ) )
			action.exit( "unknown flag var=" + var );
		
		FLAG val = flags.get( var );
		if( val == null )
			return( defValue );
		
		if( val == FLAG.YES )
			return( true );
		
		return( false );
	}
	
	public String getEnumValue( ActionBase action , String var ) throws Exception {
		if( !isEnumVar( var ) )
			action.exit( "unknown enum var=" + var );
		
		String val = enums.get( var );
		if( val == null )
			return( "" );
		return( val );
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
		if( !isParamVar( var ) )
			action.exit( "unknown param var=" + var );
		
		String val = params.get( var );
		if( val == null )
			return( "" );
		return( val );
	}
	
	public int getIntParamValue( ActionBase action , String var , int defaultValue ) throws Exception {
		if( !isParamVar( var ) )
			action.exit( "unknown param var=" + var );
		
		String val = params.get( var );
		if( val == null || val.isEmpty() )
			return( defaultValue );
		return( Integer.parseInt( val ) );
	}
	
	String getFlagsSet() {
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandVar var = optionsSet.get( k );
			if( var.isFlag ) {
				if( !s.isEmpty() )
					s += " ";
				s += var.varName + "=" + flags.get( var.varName );
			}
			else if( var.isEnum ) {
				if( !s.isEmpty() )
					s += " ";
				s += var.varName + "=" + enums.get( var.varName );
			}
		}
		return( s );
	}
	
	String getParamsSet() {
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandVar var = optionsSet.get( k );
			if( var.isFlag || var.isEnum )
				continue;
			
			if( !s.isEmpty() )
				s += " ";
			s += var.varName + "=" + params.get( var.varName );
		}
		return( s );
	}

	public List<CommandVar> getOptionsSet() {
		return( optionsSet );
	}
	
	String getArgsSet() {
		String s = "";
		for( int k = 0; k < args.size(); k++ ) {
			if( !s.isEmpty() )
				s += " ";
			s += args.get( k );
		}
		return( s );
	}
	
	public String getArg( int pos ) {
		if( pos >= args.size() )
			return( "" );
		
		return( args.get( pos ) );
	}
	
	public void checkNoArgs( ActionBase action , int pos ) throws Exception {
		if( pos >= args.size() )
			return;
		
		action.exit( "unexpected extra arguments: " + Common.getQuoted( Common.getSubList( args , pos ) ) + "; see help to find syntax" );
	}
	
	public VarCATEGORY getRequiredCategoryArg( ActionBase action , int pos ) throws Exception {
		VarCATEGORY CATEGORY = getCategoryArg( action , pos );
		if( CATEGORY == null )
			action.exit( "CATEGORY argument is required" );
		return( CATEGORY );
	}
	
	public VarBUILDMODE getRequiredBuildModeArg( ActionBase action , int pos ) throws Exception {
		String value = getRequiredArg( action , pos , "BUILDMODE" );
		VarBUILDMODE BUILDMODE = null;
		for( VarBUILDMODE x : VarBUILDMODE.values() )
			if( value.equals( Common.getEnumLower( x ) ) ) {
				BUILDMODE = x;
				break;
			}
				
		if( BUILDMODE == null )
			action.exit( "unknown buildMode=" + value );
		return( BUILDMODE );
	}
	
	public VarCATEGORY getCategoryArg( ActionBase action , int pos ) throws Exception {
		if( pos >= args.size() )
			return( null );
		
		return( action.meta.getCategory( action, args.get( pos ) ) );
	}
	
	public String getRequiredArg( ActionBase action , int pos , String argName ) throws Exception {
		String value = getArg( pos );
		if( value.isEmpty() )
			action.exit( argName + " is empty" );
		
		return( value );
	}
	
	public int getIntArg( int pos , int defValue ) {
		String value = getArg( pos );
		if( value.isEmpty() )
			return( defValue );
		return( Integer.parseInt( value ) );
	}
	
	public String[] getArgList( int startFrom ) {
		if( startFrom >= args.size() )
			return( new String[0] );
		
		String[] list = new String[ args.size() - startFrom ];
		for( int k = startFrom; k < args.size(); k++ )
			list[ k - startFrom ] = args.get( k );

		return( list );
	}
	
	public boolean combineValue( ActionBase action , String optVar , FLAG confValue , boolean defValue ) throws Exception {
		if( !isValidVar( optVar ) )
			action.exit( "unknown flag var=" + optVar );
		
		FLAG optValue = flags.get( optVar );

		// option always overrides
		if( optValue != null && optValue != FLAG.DEFAULT )
			return( optValue == FLAG.YES );
		
		// if configuration is present
		if( confValue != null && confValue != FLAG.DEFAULT )
			return( confValue == FLAG.YES );
		
		return( defValue );
	}
	
}
