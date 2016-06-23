package org.urm.common.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;

public class CommandOptions {

	public enum FLAG { DEFAULT , YES , NO }; 

	public enum SQLMODE { UNKNOWN , APPLY , ANYWAY , CORRECT , ROLLBACK , PRINT };
	public enum SQLTYPE { UNKNOWN , SQL , CTL , PUB };

	CommandMeta commandInfo;
	public int optDefaultCommandTimeout = 10;
	
	// implementation
	public List<CommandVar> optionsDefined = new LinkedList<CommandVar>();
	public Map<String,CommandVar> optionsByName = new HashMap<String,CommandVar>();
	public Map<String,CommandVar> varByName = new HashMap<String,CommandVar>();
	private int genericOptionsCount;
	
	protected List<CommandVar> optionsSet = new LinkedList<CommandVar>();
	public String command;
	public String action;
	public ActionData data = new ActionData();

	public CommandOptions( CommandMeta commandInfo ) {
		this.commandInfo = commandInfo;
		
		optionsDefined = new LinkedList<CommandVar>();
		optionsSet = new LinkedList<CommandVar>();
		optionsByName = new HashMap<String,CommandVar>();
		
		defineGenericOption( CommandVar.newFlagYesOption( "trace" , "OPT_TRACE" , true , "show commands" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "showall" , "OPT_SHOWALL" , true , "show all log records" ) );
		defineGenericOption( CommandVar.newFlagNoOption( "showmain" , "OPT_SHOWALL" , true , "show only important log records (see also showall)" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "showonly" , "OPT_SHOWONLY" , true , "do not perform builds or change distributive" ) );
		defineGenericOption( CommandVar.newFlagNoOption( "execute" , "OPT_SHOWONLY" , true , "execute operations (see also showonly)" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "force" , "OPT_FORCE" , true , "ignore errors and constraints" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "ignore" , "OPT_SKIPERRORS" , true , "continue run disregarding errors" ) );
		defineGenericOption( CommandVar.newFlagNoOption( "strict" , "OPT_SKIPERRORS" , true , "stop execution after error" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "all" , "OPT_ALL" , true , "use all possible items in scope, ignore reduce defaults" ) );
		defineGenericOption( CommandVar.newFlagYesOption( "local" , "OPT_LOCAL" , false , "any session is opened as local under current user" ) );
		defineGenericOption( CommandVar.newIntParam( "timeout" , "OPT_COMMANDTIMEOUT" , true , "use specific default timeout" ) );
		defineGenericOption( CommandVar.newParam( "key" , "OPT_KEY" , false , "use given key to connect to host" ) );
		defineGenericOption( CommandVar.newParam( "distpath" , "OPT_DISTPATH" , false , "use given path to find distributive files" ) );
		defineGenericOption( CommandVar.newParam( "hiddenpath" , "OPT_HIDDENPATH" , false , "use given path to find hidden files and properties" ) );
		defineGenericOption( CommandVar.newParam( "workpath" , "OPT_WORKPATH" , false , "use given path to store working files and logs" ) );
		genericOptionsCount = optionsDefined.size();
		
		defineOption( CommandVar.newFlagYesOption( "get" , "OPT_GET" , true , "run getall after build" ) );
		defineOption( CommandVar.newFlagYesOption( "dist" , "OPT_DIST" , true , "copy to distributive after getall" ) );
		defineOption( CommandVar.newFlagYesOption( "updatenexus" , "OPT_UPDATENEXUS" , true , "force reupload items to thirdparty repository" ) );
		defineOption( CommandVar.newFlagYesOption( "check" , "OPT_CHECK" , true , "run source checks before build" ) );
		defineOption( CommandVar.newFlagNoOption( "nocheck" , "OPT_CHECK" , true , "skip source checks before build (see also check)" ) );
		defineOption( CommandVar.newFlagYesOption( "move" , "OPT_MOVE_ERRORS" , true , "move wrong release source files to error folder" ) );
		defineOption( CommandVar.newFlagYesOption( "replace" , "OPT_REPLACE" , true , "replace all item contents on deploy" ) );
		defineOption( CommandVar.newFlagYesOption( "backup" , "OPT_BACKUP" , true , "prepare backup before deploy" ) );
		defineOption( CommandVar.newFlagNoOption( "nobackup" , "OPT_BACKUP" , true , "do not backup before deploy" ) );
		defineOption( CommandVar.newFlagYesOption( "obsolete" , "OPT_OBSOLETE" , true , "ignore new layout" ) );
		defineOption( CommandVar.newFlagNoOption( "noobsolete" , "OPT_OBSOLETE" , true , "use new layout" ) );
		defineOption( CommandVar.newFlagYesOption( "conf" , "OPT_DEPLOYCONF" , true , "deploy configuration files" ) );
		defineOption( CommandVar.newFlagNoOption( "noconf" , "OPT_DEPLOYCONF" , true , "do not deploy configuration files" ) );
		defineOption( CommandVar.newFlagYesOption( "partialconf" , "OPT_PARTIALCONF" , true , "ignore missing configuration files" ) );
		defineOption( CommandVar.newFlagYesOption( "binary" , "OPT_DEPLOYBINARY" , true , "deploy binary files" ) );
		defineOption( CommandVar.newFlagNoOption( "nobinary" , "OPT_DEPLOYBINARY" , true , "do not deploy binary files" ) );
		defineOption( CommandVar.newFlagYesOption( "hot" , "OPT_DEPLOYHOT" , true , "deploy hot files only" ) );
		defineOption( CommandVar.newFlagYesOption( "cold" , "OPT_DEPLOYCOLD" , true , "deploy cold files only" ) );
		defineOption( CommandVar.newFlagYesOption( "raw" , "OPT_DEPLOYRAW" , false , "internal use only" ) );
		defineOption( CommandVar.newFlagYesOption( "keepalive" , "OPT_KEEPALIVE" , true , "automatically maintain product configuration set" ) );
		defineOption( CommandVar.newFlagNoOption( "nokeepalive" , "OPT_KEEPALIVE" , true , "do not change product configuration set" ) );
		defineOption( CommandVar.newFlagNoOption( "downtime" , "OPT_ZERODOWNTIME" , true , "deploy with downtime" ) );
		defineOption( CommandVar.newFlagYesOption( "nodowntime" , "OPT_ZERODOWNTIME" , true , "deploy without downtime if possible" ) );
		defineOption( CommandVar.newFlagYesOption( "nonodes" , "OPT_NONODES" , true , "execute only on server-level, no nodes" ) );
		defineOption( CommandVar.newFlagYesOption( "nomsg" , "OPT_NOCHATMSG" , true , "do not notify in chat window" ) );
		defineOption( CommandVar.newFlagYesOption( "root" , "OPT_ROOTUSER" , true , "execute under root" ) );
		defineOption( CommandVar.newFlagYesOption( "sudo" , "OPT_SUDO" , true , "execute using sudo from specified hostuser" ) );
		defineOption( CommandVar.newFlagYesOption( "ignoreversion" , "OPT_IGNOREVERSION" , true , "ignore version information on deploy" ) );
		defineOption( CommandVar.newFlagYesOption( "live" , "OPT_LIVE" , true , "use saved live configuration" ) );
		defineOption( CommandVar.newFlagYesOption( "hidden" , "OPT_HIDDEN" , true , "use hidden files to restore configuration" ) );
		defineOption( CommandVar.newFlagEnumOption( "a" , "APPLY" , "OPT_DBMODE" , true , "execute database set - only new scipts" ) );
		defineOption( CommandVar.newFlagEnumOption( "x" , "ANYWAY" , "OPT_DBMODE" , true , "execute database set - both already applied and new scipts" ) );
		defineOption( CommandVar.newFlagEnumOption( "c" , "CORRECT" , "OPT_DBMODE" , true , "execute database set - only failed scripts" ) );
		defineOption( CommandVar.newFlagEnumOption( "r" , "ROLLBACK" , "OPT_DBMODE" , true , "execute database set - rollback" ) );
		defineOption( CommandVar.newFlagEnumOption( "p" , "PRINT" , "OPT_DBMODE" , true , "execute database set - show database status" ) );
		defineOption( CommandVar.newFlagYesOption( "m" , "OPT_DBMOVE" , true , "move erroneous scripts to error subfolder in source folder" ) );
		defineOption( CommandVar.newFlagYesOption( "auth" , "OPT_DBAUTH" , true , "do not use simple authorization" ) );
		defineOption( CommandVar.newFlagNoOption( "noauth" , "OPT_DBAUTH" , true , "use simple authorization" ) );
		defineOption( CommandVar.newFlagYesOption( "cumulative" , "OPT_CUMULATIVE" , true , "cumulative release" ) );
		defineOption( CommandVar.newParam( "aligned" , "OPT_DBALIGNED" , true , "use specific aligned set of scipts to apply" ) );
		defineOption( CommandVar.newParam( "db" , "OPT_DB" , true , "use specific database server to apply" ) );
		defineOption( CommandVar.newParam( "dbpwd" , "OPT_DBPASSWORD" , true , "use specified password to access database" ) );
		defineOption( CommandVar.newParam( "regions" , "OPT_REGIONS" , true , "use specific set of regions to apply" ) );
		defineOption( CommandVar.newFlagEnumOption( "sql" , "SQL" , "OPT_DBTYPE" , true , "execute database set - only scripts" ) );
		defineOption( CommandVar.newFlagEnumOption( "ctl" , "CTL" , "OPT_DBTYPE" , true , "execute database set - only load files" ) );
		defineOption( CommandVar.newFlagEnumOption( "pub" , "PUB" , "OPT_DBTYPE" , true , "execute database set - only publish files" ) );
		defineOption( CommandVar.newParam( "release" , "OPT_RELEASE" , true , "use specific release name" ) );
		defineOption( CommandVar.newParam( "branch" , "OPT_BRANCH" , true , "use specific codebase branch name" ) );
		defineOption( CommandVar.newParam( "tag" , "OPT_TAG" , true , "use specific codebase tag name" ) );
		defineOption( CommandVar.newParam( "date" , "OPT_DATE" , true , "use codebase state on given date (ISO-8601)" ) );
		defineOption( CommandVar.newParam( "group" , "OPT_GROUP" , true , "use specific codebase project group" ) );
		defineOption( CommandVar.newParam( "version" , "OPT_VERSION" , true , "use specific codebase version" ) );
		defineOption( CommandVar.newParam( "dc" , "OPT_DC" , true , "use datacenters which names meet given regular mask" ) );
		defineOption( CommandVar.newParam( "deploygroup" , "OPT_DEPLOYGROUP" , true , "use only nodes belonging to specified deploygroup" ) );
		defineOption( CommandVar.newParam( "startgroup" , "OPT_STARTGROUP" , true , "use only servers belonging to specified startgroup" ) );
		defineOption( CommandVar.newParam( "args" , "OPT_EXTRAARGS" , true , "extra arguments for server interface scripts" ) );
		defineOption( CommandVar.newParam( "unit" , "OPT_UNIT" , true , "use distributive items only from given unit" ) );
		defineOption( CommandVar.newParam( "buildinfo" , "OPT_BUILDINFO" , true , "use given build info parameter" ) );
		defineOption( CommandVar.newParam( "hostuser" , "OPT_HOSTUSER" , true , "use given user when connecting to host" ) );
		defineOption( CommandVar.newParam( "newkey" , "OPT_NEWKEY" , true , "use given key to change on host" ) );
		defineOption( CommandVar.newParam( "mode" , "OPT_BUILDMODE" , true , "use given build mode (branch, trunk, major, devbranch, devtrunk)" ) );
		defineOption( CommandVar.newParam( "over" , "OPT_COMPATIBILITY" , true , "previous release installed" ) );
		defineOption( CommandVar.newIntParam( "port" , "OPT_PORT" , false , "server port" ) );
		defineOption( CommandVar.newParam( "host" , "OPT_HOST" , false , "server host" ) );
	}

	public CommandVar getVar( String varName ) {
		return( varByName.get( varName ) );
	}
	
	public void print( String s ) {
		System.out.println( s );
	}

	private void printhelp( String s ) {
		print( "# " + s );
	}

	public String getRunningOptions() {
		String values = "";
		for( CommandVar option : optionsSet ) {
			String value = getOptionValue( option );
			values = Common.addToList( values , value , ", " );
		}
		
		String info = "execute options={" + values + "}, args={" + 
				Common.getList( data.args.toArray( new String[0] ) , ", " ) + "}";
		return( info );
	}
	
	public String getOptionValue( CommandVar var ) {
		return( data.getOptionValue( var ) );
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
		if( !data.addFlagOption( info ) ) {
			print( "flag=" + info.varName + " is already set" );
			return( false );
		}
		
		optionsSet.add( info );
		return( true );
	}

	public boolean addEnumOption( String opt ) {
		if( !isEnumOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a enum" );
		
		CommandVar info = optionsByName.get( opt );
		if( !data.addEnumOption( info ) ) {
			print( "enum=" + info.varName + " is already set" );
			return( false );
		}
		
		optionsSet.add( info );
		return( true );
	}

	public boolean addParamOption( String opt , String value ) {
		if( !isParamOption( opt ) )
			throw new RuntimeException( "option=" + opt + " is not a parameter" );
		
		CommandVar info = optionsByName.get( opt );
		if( !data.addParamOption( info , value ) ) {
			print( "parameter=" + info.varName + " is already set" );
			return( false );
		}
		
		optionsSet.add( info );
		return( true );
	}

	public boolean addArg( String value ) {
		return( data.addArg( value ) );
	}
	
	private void showOptionHelp( CommandBuilder builder , CommandVar var ) {
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

	public void showTopHelp( CommandBuilder builder , CommandMeta main , CommandMeta[] commands ) {
		printhelp( "URM HELP (top)" );
		printhelp( "" );
		
		printhelp( "URM assists to administer codebase, release process and to maintain testing and production environments." );
		printhelp( "Release process consists of peforming builds from codebase and administration of distributives." );
		printhelp( "Distributive adminstration contains managing release repository and specific release engineering." );
		printhelp( "Release engineering are steps to populate distributive from ready and built items." );
		printhelp( "Items are configuration files, database changes, binary files and archives." );
		printhelp( "" );
		
		printhelp( "URM instance administration:" );
		showCommandHelp( builder , main , true );
		printhelp( "" );
		
		printhelp( "Operation are split into commands corresponding to master subfolders" );
		printhelp( "Available commands are:" );
		for( CommandMeta meta : commands )
			printhelp( "\t" + meta.name + ": " + meta.desc );
		printhelp( "" );

		printhelp( "To get help on specific command, run:" );
		if( builder.clientrc.isLinux() )
			printhelp( "\t./help.sh <command>" );
		else
			printhelp( "\thelp.cmd <command>" );
		printhelp( "" );
		
		printhelp( "To get help on specific action, run:" );
		if( builder.clientrc.isLinux() )
			printhelp( "\t./help.sh <command> <action>" );
		else
			printhelp( "\thelp.cmd <command> <action>" );
	}
	
	public void showCommandHelp( CommandBuilder builder , CommandMeta commandInfo , boolean main ) {
		printhelp( "URM HELP (command)" );
		printhelp( "" );
		
		printhelp( "Command: " + commandInfo.name );
		printhelp( "Functions: " + commandInfo.desc );
		printhelp( "" );
		printhelp( "Available actions are:" );
		for( CommandMethod action : commandInfo.actionsList ) {
			String spacing = Common.replicate( " " , 50 - action.name.length() ); 
			printhelp( "\t" + action.name + spacing + action.help );
		}
		
		printhelp( "" );
		
		printhelp( "All options defined for command " + command + ":" );
		printhelp( "Generic options:" );
		for( int k = 0; k < genericOptionsCount; k++ ) {
			CommandVar var = optionsDefined.get( k );
			showOptionHelp( builder , var );
		}
		
		printhelp( "Specific options:" );
		boolean specific = false;
		for( int k = genericOptionsCount; k < optionsDefined.size(); k++ ) {
			CommandVar var = optionsDefined.get( k );
			if( !commandInfo.isOptionApplicaple( var ) )
				continue;
			
			showOptionHelp( builder , var );
			specific = true;
		}
		if( !specific )
			printhelp( "\t(no specific options)" );
		
		printhelp( "" );
		printhelp( "To get help on specific action, run:" );
		if( builder.clientrc.isLinux() ) {
			if( main )
				printhelp( "\t./help.sh <command> <action>" );
			else
				printhelp( "\t./help.sh <action> or ./<action>.sh help" );
		}
		else {
			if( main )
				printhelp( "\thelp.cmd <command> <action>" );
			else
				printhelp( "\thelp.cmd <action> or <action>.cmd help" );
		}
	}
	
	public void showActionHelp( CommandBuilder builder , CommandMethod action ) {
		printhelp( "URM HELP (action)" );
		printhelp( "" );
		
		printhelp( "Action: " + action.name );
		printhelp( "Function: " + action.help );
		printhelp( "Syntax: " + action.syntax );
		printhelp( "" );
		
		// show action options
		printhelp( "All options defined for " + action.name + ":" );
		printhelp( "Generic options:" );
		for( int k = 0; k < genericOptionsCount; k++ ) {
			CommandVar var = optionsDefined.get( k );
			showOptionHelp( builder , var );
		}

		printhelp( "Specific options:" );
		boolean specific = false;
		for( int k = genericOptionsCount; k < optionsDefined.size(); k++ ) {
			CommandVar var = optionsDefined.get( k );
			if( action.isOptionApplicable( var ) ) {
				showOptionHelp( builder , var );
				specific = true;
			}
		}

		if( !specific )
			printhelp( "\t(no specific options)" );
	}
	
	public boolean parseArgs( String[] cmdParams ) {
		if( cmdParams.length < 2 ) {
			command = "help";
			return( false );
		}
		
		// first item is command
		command = cmdParams[0];
		
		int k = 1;
		action = cmdParams[1];
		k++;

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

	public boolean getFlagValue( String var , boolean defValue ) {
		return( data.getFlagValue( var , defValue ) );
	}
	
	public String getEnumValue( String var ) {
		return( data.getEnumValue( var ) );
	}

	public String getParamValue( String var ) {
		return( data.getParamValue( var ) );
	}
	
	public int getIntParamValue( String var , int defaultValue ) {
		return( data.getIntParamValue( var , defaultValue ) );
	}
	
	public String getFlagsSet() {
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandVar var = optionsSet.get( k );
			if( var.isFlag ) {
				if( !s.isEmpty() )
					s += " ";
				s += var.varName + "=" + data.flags.get( var.varName );
			}
			else if( var.isEnum ) {
				if( !s.isEmpty() )
					s += " ";
				s += var.varName + "=" + data.enums.get( var.varName );
			}
		}
		return( s );
	}
	
	public String getParamsSet() {
		String s = "";
		for( int k = 0; k < optionsSet.size(); k++ ) {
			CommandVar var = optionsSet.get( k );
			if( var.isFlag || var.isEnum )
				continue;
			
			if( !s.isEmpty() )
				s += " ";
			s += var.varName + "=" + data.params.get( var.varName );
		}
		return( s );
	}

	public List<CommandVar> getOptionsSet() {
		return( optionsSet );
	}
	
	public String getArgsSet() {
		String s = "";
		for( int k = 0; k < data.args.size(); k++ ) {
			if( !s.isEmpty() )
				s += " ";
			s += data.args.get( k );
		}
		return( s );
	}
	
	public String getArg( int pos ) {
		return( data.getArg( pos ) );
	}
	
	public int getArgCount() {
		return( data.getArgCount() );
	}
	
	public int getIntArg( int pos , int defValue ) {
		return( data.getIntArg( pos , defValue ) );
	}
	
	public String[] getArgList( int startFrom ) {
		return( data.getArgList( startFrom ) );
	}
	
	public boolean combineValue( String optVar , FLAG confValue , boolean defValue ) {
		return( data.combineValue( optVar , confValue , defValue ) );
	}
	
	public boolean checkValidOptions( CommandMethod commandAction ) {
		for( CommandVar var : optionsSet ) {
			if( !commandAction.isOptionApplicable( var ) ) {
				print( "option " + var.varName + " is not applicable for action " + commandAction.name );
				return( false );
			}
		}

		// check defined options
		for( String varUsed : commandAction.vars ) {
			if( !isValidVar( varUsed ) ) {
				print( "unknown command var=" + varUsed );
				return( false );
			}
		}
		
		return( true );
	}
	
}
