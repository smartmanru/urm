package ru.egov.urm.run;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarCATEGORY;

public class CommandOptions {

	public enum FLAG { DEFAULT , YES , NO }; 

	public enum SQLMODE { UNKNOWN , APPLY , ANYWAY , CORRECT , ROLLBACK , PRINT };
	public enum SQLTYPE { UNKNOWN , SQL , CTL , PUB };

	static int optDefaultCommandTimeout = 10;
	
	// standard command parameters 
	public String command;
	public String action;

	// generic
	public boolean OPT_TRACE;
	public boolean OPT_SHOWALL;
	protected boolean OPT_SHOWONLY;
	public boolean OPT_FORCE;
	public boolean OPT_ALL;
	public int OPT_COMMANDTIMEOUT;

	// specific
	public boolean OPT_GET;
	public boolean OPT_DIST;
	public boolean OPT_UPDATENEXUS;
	public boolean OPT_CHECK;
	public boolean OPT_MOVE_ERRORS;
	public boolean OPT_REPLACE;
	
	protected boolean OPT_BACKUP;
	protected boolean OPT_OBSOLETE;
	protected boolean OPT_DEPLOYCONF;
	public boolean OPT_PARTIALCONF;
	public boolean OPT_DEPLOYBINARY;
	public boolean OPT_DEPLOYHOT;
	public boolean OPT_DEPLOYCOLD;
	public boolean OPT_DEPLOYRAW;
	protected boolean OPT_KEEPALIVE;
	public boolean OPT_ZERODOWNTIME;
	public boolean OPT_NONODES;
	public boolean OPT_NOCHATMSG;
	public boolean OPT_SUDO;
	public boolean OPT_ROOTUSER;
	public boolean OPT_IGNOREVERSION;
	public boolean OPT_LIVE;
	public boolean OPT_HIDDEN;
	
	public String OPT_RELEASELABEL;
	public String OPT_BRANCH;
	public String OPT_TAG;
	public String OPT_DATE;
	public String OPT_GROUP;
	public String OPT_VERSION;
	
	public String OPT_DCMASK;
	public String OPT_DEPLOYGROUP;
	public String OPT_STARTGROUP;
	public String OPT_EXTRAARGS;
	public String OPT_UNIT;
	public String OPT_BUILDINFO;
	public String OPT_HOSTUSER;
	public String OPT_KEY;
	public String OPT_NEWKEY;

	public SQLMODE OPT_DBMODE; 
	public boolean OPT_DBMOVE;
	protected boolean OPT_DBAUTH;
	public String OPT_DBALIGNED;
	public String OPT_DB;
	public String OPT_DBPASSWORD;
	public String OPT_REGIONS;
	public SQLTYPE OPT_DBTYPE;

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

	public CommandOptions( CommandOptions origin ) {
		command = origin.command;
		action = origin.action;
		
		optionsDefined = origin.optionsDefined;
		optionsSet = origin.optionsSet;
		optionsByName = origin.optionsByName;
		
		flags = origin.flags;
		enums = origin.enums;
		params = origin.params;
		args = origin.args;
		
		genericOptionsCount = origin.genericOptionsCount;
		scatter();
	}
	
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
		defineGenericOption( CommandVar.newFlagYesOption( "all" , "GETOPT_ALL" , "use all possible items in scope, ignore reduce defaults" ) );
		defineGenericOption( CommandVar.newParam( "timeout" , "GETOPT_COMMANDTIMEOUT" , "use specific default timeout" ) );
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
		defineOption( CommandVar.newFlagYesOption( "skiperrors" , "GETOPT_SKIPERRORS" , "continue run disregarding errors" ) );
		defineOption( CommandVar.newFlagNoOption( "strict" , "GETOPT_SKIPERRORS" , "stop execution after error" ) );
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
		defineOption( CommandVar.newParam( "aligned" , "GETOPT_DBALIGNED" , "use specific aligned set of scipts to apply" ) );
		defineOption( CommandVar.newParam( "db" , "GETOPT_DB" , "use specific database server to apply" ) );
		defineOption( CommandVar.newParam( "dbpassword" , "GETOPT_DBPASSWORD" , "use specified password to access database" ) );
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
		
		defineOption( CommandVar.newParam( "dc" , "GETOPT_DCMASK" , "use datacenters which names meet given regular mask" ) );
		defineOption( CommandVar.newParam( "deploygroup" , "GETOPT_DEPLOYGROUP" , "use only nodes belonging to specified deploygroup" ) );
		defineOption( CommandVar.newParam( "startgroup" , "GETOPT_STARTGROUP" , "use only servers belonging to specified startgroup" ) );
		defineOption( CommandVar.newParam( "args" , "GETOPT_EXTRAARGS" , "extra arguments for server interface scripts" ) );
		defineOption( CommandVar.newParam( "unit" , "GETOPT_UNIT" , "use distributive items only from given unit" ) );
		defineOption( CommandVar.newParam( "buildinfo" , "GETOPT_BUILDINFO" , "use given build info parameter" ) );
		defineOption( CommandVar.newParam( "tag" , "GETOPT_TAG" , "use configuration files from given tag" ) );
		defineOption( CommandVar.newParam( "hostuser" , "GETOPT_HOSTUSER" , "use given user when connecting to host" ) );
		defineOption( CommandVar.newParam( "key" , "GETOPT_KEY" , "use given key to connect to host" ) );
		defineOption( CommandVar.newParam( "newkey" , "GETOPT_NEWKEY" , "use given key to change on host" ) );
	}

	void print( String s ) {
		System.out.println( s );
	}

	private void printhelp( String s ) {
		print( "# " + s );
	}

	public void printRunningOptions() {
		String values = "";
		for( CommandVar option : optionsSet ) {
			String value = getOptionValue( option );
			values = Common.addToList( values , value , ", " );
		}
		
		String info = "execute options={" + values + "}, args={" + 
				Common.getList( args.toArray( new String[0] ) , ", " ) + "}";
		printhelp( info );
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
	
	public void scatter() {
		OPT_TRACE = getFlagValue( "GETOPT_TRACE" );
		OPT_SHOWALL = getFlagValue( "GETOPT_SHOWALL" );
		OPT_SHOWONLY = getFlagValue( "GETOPT_SHOWONLY" );
		OPT_FORCE = getFlagValue( "GETOPT_FORCE" );
		OPT_ALL = getFlagValue( "GETOPT_ALL" );
		OPT_COMMANDTIMEOUT = getIntParamValue( "GETOPT_COMMANDTIMEOUT" , optDefaultCommandTimeout ) * 1000;
		
		if( OPT_TRACE )
			OPT_SHOWALL = true;
		
		OPT_GET = getFlagValue( "GETOPT_GET" );
		OPT_DIST = getFlagValue( "GETOPT_DIST" );
		OPT_UPDATENEXUS = getFlagValue( "GETOPT_UPDATENEXUS" );
		OPT_CHECK = getFlagValue( "GETOPT_CHECK" , true );
		OPT_MOVE_ERRORS = getFlagValue( "GETOPT_MOVE_ERRORS" );
		OPT_REPLACE = getFlagValue( "GETOPT_REPLACE" );
		
		OPT_BACKUP = getFlagValue( "GETOPT_BACKUP" );
		OPT_OBSOLETE = getFlagValue( "GETOPT_OBSOLETE" );
		OPT_DEPLOYCONF = getFlagValue( "GETOPT_DEPLOYCONF" , true );
		OPT_PARTIALCONF = getFlagValue( "GETOPT_PARTIALCONF" );
		OPT_DEPLOYBINARY = getFlagValue( "GETOPT_DEPLOYBINARY" , true );
		OPT_DEPLOYHOT = getFlagValue( "GETOPT_DEPLOYHOT" );
		OPT_DEPLOYCOLD = getFlagValue( "GETOPT_DEPLOYCOLD" );
		OPT_DEPLOYRAW = getFlagValue( "GETOPT_DEPLOYRAW" );
		OPT_KEEPALIVE = getFlagValue( "GETOPT_KEEPALIVE" , true );
		OPT_ZERODOWNTIME = getFlagValue( "GETOPT_ZERODOWNTIME" );
		OPT_NONODES = getFlagValue( "GETOPT_NONODES" );
		OPT_NOCHATMSG = getFlagValue( "GETOPT_NOCHATMSG" );
		OPT_ROOTUSER = getFlagValue( "GETOPT_ROOTUSER" );
		OPT_SUDO = getFlagValue( "GETOPT_SUDO" );
		OPT_IGNOREVERSION = getFlagValue( "GETOPT_IGNOREVERSION" );
		OPT_LIVE = getFlagValue( "GETOPT_LIVE" );
		OPT_HIDDEN = getFlagValue( "GETOPT_HIDDEN" );
		
		OPT_RELEASELABEL = getParamValue( "GETOPT_RELEASE" );
		OPT_BRANCH = getParamValue( "GETOPT_BRANCH" );
		OPT_TAG = getParamValue( "GETOPT_TAG" );
		OPT_DATE = getParamValue( "GETOPT_DATE" );
		OPT_GROUP = getParamValue( "GETOPT_GROUP" );
		OPT_VERSION = getParamValue( "GETOPT_VERSION" );

		OPT_DCMASK = getParamValue( "GETOPT_DCMASK" );
		OPT_DEPLOYGROUP = getParamValue( "GETOPT_DEPLOYGROUP" );
		OPT_STARTGROUP = getParamValue( "GETOPT_STARTGROUP" );
		OPT_EXTRAARGS = getParamValue( "GETOPT_EXTRAARGS" );
		OPT_UNIT = getParamValue( "GETOPT_UNIT" );
		OPT_BUILDINFO = getParamValue( "GETOPT_BUILDINFO" );
		OPT_TAG = getParamValue( "GETOPT_TAG" );
		OPT_HOSTUSER = getParamValue( "GETOPT_HOSTUSER" );
		OPT_KEY = getParamValue( "GETOPT_KEY" );
		OPT_NEWKEY = getParamValue( "GETOPT_NEWKEY" );
		
		String value = getEnumValue( "GETOPT_DBMODE" );
		OPT_DBMODE = ( value.isEmpty() )? SQLMODE.UNKNOWN : SQLMODE.valueOf( value );
		OPT_DBMOVE = getFlagValue( "GETOPT_DBMOVE" );
		OPT_DBAUTH = getFlagValue( "GETOPT_DBAUTH" );
		OPT_DBALIGNED = getParamValue( "OPT_DBALIGNED" );
		OPT_DB = getParamValue( "GETOPT_DB" );
		OPT_DBPASSWORD = getParamValue( "GETOPT_DBPASSWORD" );
		OPT_REGIONS = getParamValue( "GETOPT_REGIONS" );
		value = getEnumValue( "GETOPT_DBTYPE" );
		OPT_DBTYPE = ( value.isEmpty() )? SQLTYPE.UNKNOWN : SQLTYPE.valueOf( value );
	}
	
	public boolean isFlag( String opt ) {
		CommandVar info = optionsByName.get( opt ); 
		if( info != null )
			if( info.isFlag )
				return( true );
		
		return( false );
	}
	
	public boolean isEnum( String opt ) {
		CommandVar info = optionsByName.get( opt ); 
		if( info != null )
			if( info.isEnum )
				return( true );
		
		return( false );
	}
	
	public boolean isParam( String opt ) {
		CommandVar info = optionsByName.get( opt ); 
		if( info != null )
			if( info.isParam )
				return( true );
		
		return( false );
	}
	
	public boolean addFlag( String opt ) {
		if( !isFlag( opt ) )
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

	public boolean addEnum( String opt ) {
		if( !isEnum( opt ) )
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

	public boolean addParam( String var , String value ) {
		if( !isParam( var ) )
			throw new RuntimeException( "option=" + var + " is not a parameter" );
		
		CommandVar info = optionsByName.get( var );
		
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
			if( isFlag( arg ) ) {
				if( !addFlag( arg ) )
					return( false );
			}
			else if( isEnum( arg ) ) {
				if( !addEnum( arg ) )
					return( false );
			}
			else if( isParam( arg ) ) {
				k++;
				if( k >= cmdParams.length ) {
					print( "invalid arguments - parameter=" + arg + " has no value defined" );
					return( false );
				}
				
				String value = cmdParams[k];
				if( !addParam( arg , value ) )
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

	public FLAG getFlagNativeValue( String var ) {
		return( flags.get( var ) );
	}

	public boolean getFlagValue( String var ) {
		return( getFlagValue( var , false ) );
	}
	
	public boolean getFlagValue( String var , boolean defValue ) {
		FLAG val = flags.get( var );
		if( val == null )
			return( defValue );
		
		if( val == FLAG.YES )
			return( true );
		
		return( false );
	}
	
	public String getEnumValue( String var ) {
		String val = enums.get( var );
		if( val == null )
			return( "" );
		return( val );
	}
	
	public String getParamValue( String var ) {
		String val = params.get( var );
		if( val == null )
			return( "" );
		return( val );
	}
	
	public int getIntParamValue( String var , int defaultValue ) {
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
	
	public String[] getArgList( int startFrom ) {
		if( startFrom >= args.size() )
			return( new String[0] );
		
		String[] list = new String[ args.size() - startFrom ];
		for( int k = startFrom; k < args.size(); k++ )
			list[ k - startFrom ] = args.get( k );

		return( list );
	}
	
}
