package org.urm.common.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.action.CommandOption.FLAG;

public class OptionsMeta {

	public List<CommandOption> optionsDefined = new LinkedList<CommandOption>();
	public Map<String,CommandOption> optionsByName = new HashMap<String,CommandOption>();
	public Map<String,CommandVar> varByName = new HashMap<String,CommandVar>();
	private int genericOptionsCount;

	public final static String OPT_TRACE = "OPT_TRACE";
	public final static String OPT_SHOWALL = "OPT_SHOWALL";
	public final static String OPT_SHOWONLY = "OPT_SHOWONLY";
	public final static String OPT_FORCE = "OPT_FORCE";
	public final static String OPT_SKIPERRORS = "OPT_SKIPERRORS";
	public final static String OPT_ALL = "OPT_ALL";
	public final static String OPT_LOCAL = "OPT_LOCAL";
	public final static String OPT_OFFLINE = "OPT_OFFLINE";
	public final static String OPT_TIMEOUT = "OPT_TIMEOUT";
	public final static String OPT_DISTPATH = "OPT_DISTPATH";
	public final static String OPT_HIDDENPATH = "OPT_HIDDENPATH";
	public final static String OPT_USER = "OPT_USER";
	public final static String OPT_KEY = "OPT_KEY";
	public final static String OPT_PASSWORD = "OPT_PASSWORD";

	public final static String OPT_GET = "OPT_GET";
	public final static String OPT_DIST = "OPT_DIST";
	public final static String OPT_UPDATENEXUS = "OPT_UPDATENEXUS";
	public final static String OPT_CHECK = "OPT_CHECK";
	public final static String OPT_REPLACE = "OPT_REPLACE";
	public final static String OPT_BACKUP = "OPT_BACKUP";
	public final static String OPT_OBSOLETE = "OPT_OBSOLETE";
	public final static String OPT_DEPLOYCONF = "OPT_DEPLOYCONF";
	public final static String OPT_PARTIALCONF = "OPT_PARTIALCONF";
	public final static String OPT_DEPLOYBINARY = "OPT_DEPLOYBINARY";
	public final static String OPT_DEPLOYHOT = "OPT_DEPLOYHOT";
	public final static String OPT_DEPLOYCOLD = "OPT_DEPLOYCOLD";
	public final static String OPT_DEPLOYRAW = "OPT_DEPLOYRAW";
	public final static String OPT_KEEPALIVE = "OPT_KEEPALIVE";
	public final static String OPT_ZERODOWNTIME = "OPT_ZERODOWNTIME";
	public final static String OPT_NONODES = "OPT_NONODES";
	public final static String OPT_NOCHATMSG = "OPT_NOCHATMSG";
	public final static String OPT_ROOTUSER = "OPT_ROOTUSER";
	public final static String OPT_SUDO = "OPT_SUDO";
	public final static String OPT_IGNOREVERSION = "OPT_IGNOREVERSION";
	public final static String OPT_LIVE = "OPT_LIVE";
	public final static String OPT_HIDDEN = "OPT_HIDDEN";
	public final static String OPT_DBMODE = "OPT_DBMODE";
	public final static String OPT_DBMOVE = "OPT_DBMOVE";
	public final static String OPT_DBAUTH = "OPT_DBAUTH";
	public final static String OPT_CUMULATIVE = "OPT_CUMULATIVE";
	public final static String OPT_DBALIGNED = "OPT_DBALIGNED";
	public final static String OPT_DB = "OPT_DB";
	public final static String OPT_DBPASSWORD = "OPT_DBPASSWORD";
	public final static String OPT_REGIONS = "OPT_REGIONS";
	public final static String OPT_DBTYPE = "OPT_DBTYPE";
	public final static String OPT_RELEASE = "OPT_RELEASE";
	public final static String OPT_BRANCH = "OPT_BRANCH";
	public final static String OPT_TAG = "OPT_TAG";
	public final static String OPT_DATE = "OPT_DATE";
	public final static String OPT_GROUP = "OPT_GROUP";
	public final static String OPT_VERSION = "OPT_VERSION";
	public final static String OPT_SG = "OPT_SG";
	public final static String OPT_DEPLOYGROUP = "OPT_DEPLOYGROUP";
	public final static String OPT_STARTGROUP = "OPT_STARTGROUP";
	public final static String OPT_EXTRAARGS = "OPT_EXTRAARGS";
	public final static String OPT_UNIT = "OPT_UNIT";
	public final static String OPT_BUILDINFO = "OPT_BUILDINFO";
	public final static String OPT_HOSTUSER = "OPT_HOSTUSER";
	public final static String OPT_NEWKEY = "OPT_NEWKEY";
	public final static String OPT_BUILDMODE = "OPT_BUILDMODE";
	public final static String OPT_COMPATIBILITY = "OPT_COMPATIBILITY";
	public final static String OPT_PORT = "OPT_PORT";
	public final static String OPT_HOST = "OPT_HOST";
	
	public OptionsMeta() {
		optionsDefined = new LinkedList<CommandOption>();
		optionsByName = new HashMap<String,CommandOption>();
		
		defineGenericVar( CommandVar.newFlagVar( OPT_TRACE , true , "Trace" , "show internal information" ) );
		defineGenericVar( CommandVar.newFlagVar( OPT_SHOWALL , true , "Show All" , "show detailed information" ) );
		defineGenericVar( CommandVar.newFlagVar( OPT_SHOWONLY , true , "Show Only" , "do not perform any changes" ) );
		defineGenericVar( CommandVar.newFlagVar( OPT_FORCE , true , "Force" , "ignore errors and constraints" ) );
		defineGenericVar( CommandVar.newFlagVar( OPT_SKIPERRORS , true , "Skip Errors" , "continue run disregarding errors" ) );
		defineGenericVar( CommandVar.newFlagVar( OPT_ALL , true , "Use All" , "use all possible items in scope, ignore reduce defaults" ) );
		defineGenericVar( CommandVar.newFlagVar( OPT_LOCAL , false , "Run Local" , "any session is opened as local under current user" ) );
		defineGenericVar( CommandVar.newFlagVar( OPT_OFFLINE , false , "Run Offline" , "do not use server even if configured" ) );
		defineGenericVar( CommandVar.newIntVar( OPT_TIMEOUT , true , "Timeout" , "use specific default timeout" ) );
		defineGenericVar( CommandVar.newVar( OPT_DISTPATH , false , "Dist Path" , "use given path to find distributive files" ) );
		defineGenericVar( CommandVar.newVar( OPT_HIDDENPATH , false , "Secure Path" , "use given path to find hidden files and properties" ) );
		defineGenericVar( CommandVar.newVar( OPT_USER , false , "User" , "use given user to connect to server" ) );
		defineGenericVar( CommandVar.newVar( OPT_KEY , false , "Key" , "use given private key file to connect to server" ) );
		defineGenericVar( CommandVar.newVar( OPT_PASSWORD , false , "Password" , "use given password to connect to server" ) );
		defineOption( CommandOption.newFlagYesOption( this , "trace" , OPT_TRACE , true , "show internals" ) );
		defineOption( CommandOption.newFlagNoOption( this , "ntrace" , OPT_TRACE , true , "hide internals" ) );
		defineOption( CommandOption.newFlagYesOption( this , "showall" , OPT_SHOWALL , true , "show all log records" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nshowall" , OPT_SHOWALL , true , "show only important log records (see also showall)" ) );
		defineOption( CommandOption.newFlagYesOption( this , "showonly" , OPT_SHOWONLY , true , "do not perform builds or change distributive" ) );
		defineOption( CommandOption.newFlagNoOption( this , "execute" , OPT_SHOWONLY , true , "execute operations (see also showonly)" ) );
		defineOption( CommandOption.newFlagYesOption( this , "force" , OPT_FORCE , true , "ignore errors and constraints" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nforce" , OPT_FORCE , true , "stop on errors and constraints" ) );
		defineOption( CommandOption.newFlagYesOption( this , "ignore" , OPT_SKIPERRORS , true , "continue run disregarding errors" ) );
		defineOption( CommandOption.newFlagNoOption( this , "strict" , OPT_SKIPERRORS , true , "stop execution after error" ) );
		defineOption( CommandOption.newFlagYesOption( this , "all" , OPT_ALL , true , "use all possible items in scope, ignore reduce defaults" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nall" , OPT_ALL , true , "use default scope" ) );
		defineOption( CommandOption.newFlagYesOption( this , "local" , OPT_LOCAL , false , "any session is opened as local under current user" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nlocal" , OPT_LOCAL , false , "any session is opened as local under current user" ) );
		defineOption( CommandOption.newFlagYesOption( this , "offline" , OPT_OFFLINE , false , "do not use server even if configured" ) );
		defineOption( CommandOption.newFlagNoOption( this , "online" , OPT_OFFLINE , false , "do not use server even if configured" ) );
		defineOption( CommandOption.newIntParam( this , "timeout" , OPT_TIMEOUT , true , "use specific default timeout" ) );
		defineOption( CommandOption.newParam( this , "distpath" , OPT_DISTPATH , false , "use given path to find distributive files" ) );
		defineOption( CommandOption.newParam( this , "hiddenpath" , OPT_HIDDENPATH , false , "use given path to find hidden files and properties" ) );
		defineOption( CommandOption.newParam( this , "user" , OPT_USER , false , "use given user to connect to server" ) );
		defineOption( CommandOption.newParam( this , "key" , OPT_KEY , false , "use given private key file to connect to server" ) );
		defineOption( CommandOption.newParam( this , "password" , OPT_PASSWORD , false , "use given password to connect to server" ) );
		genericOptionsCount = optionsDefined.size();

		defineVar( CommandVar.newFlagVar( OPT_GET , true , "Download" , "run getall after build" ) );
		defineVar( CommandVar.newFlagVar( OPT_DIST , true , "Change Distributive" , "copy to distributive after getall" ) );
		defineVar( CommandVar.newFlagVar( OPT_UPDATENEXUS , true , "Update Nexus" , "force reupload items to thirdparty repository" ) );
		defineVar( CommandVar.newFlagVar( OPT_CHECK , true , "Check Source" , "run source checks before build" ) );
		defineVar( CommandVar.newFlagVar( OPT_REPLACE , true , "Replace" , "replace all item contents on deploy" ) );
		defineVar( CommandVar.newFlagVar( OPT_BACKUP , true , "Backup" , "prepare backup before deploy" ) );
		defineVar( CommandVar.newFlagVar( OPT_OBSOLETE , true , "Obsolete" , "ignore new layout" ) );
		defineVar( CommandVar.newFlagVar( OPT_DEPLOYCONF , true , "Deploy Conf" , "deploy configuration files" ) );
		defineVar( CommandVar.newFlagVar( OPT_PARTIALCONF , true , "Partial Conf" , "ignore missing configuration files" ) );
		defineVar( CommandVar.newFlagVar( OPT_DEPLOYBINARY , true , "Deploy Binary" , "deploy binary files" ) );
		defineVar( CommandVar.newFlagVar( OPT_DEPLOYHOT , true , "Deploy Hot" , "deploy hot files only" ) );
		defineVar( CommandVar.newFlagVar( OPT_DEPLOYCOLD , true , "Deploy Cold" , "deploy cold files only" ) );
		defineVar( CommandVar.newFlagVar( OPT_DEPLOYRAW , false , "Deploy Raw" , "internal use only" ) );
		defineVar( CommandVar.newFlagVar( OPT_KEEPALIVE , true , "Keep Alive Conf" , "automatically maintain product configuration set" ) );
		defineVar( CommandVar.newFlagVar( OPT_ZERODOWNTIME , true , "Zero Downtime" , "deploy with downtime" ) );
		defineVar( CommandVar.newFlagVar( OPT_NONODES , true , "No Nodes" , "execute only on server-level, no nodes" ) );
		defineVar( CommandVar.newFlagVar( OPT_NOCHATMSG , true , "No Chat" , "do not notify in chat window" ) );
		defineVar( CommandVar.newFlagVar( OPT_ROOTUSER , true , "Use Root" , "execute under root" ) );
		defineVar( CommandVar.newFlagVar( OPT_SUDO , true , "Use sudo" , "execute using sudo from specified hostuser" ) );
		defineVar( CommandVar.newFlagVar( OPT_IGNOREVERSION , true , "Ignore Version" , "ignore version information on deploy" ) );
		defineVar( CommandVar.newFlagVar( OPT_LIVE , true , "Use Live" , "use saved live configuration" ) );
		defineVar( CommandVar.newFlagVar( OPT_HIDDEN , true , "Use Hidden" , "use hidden files to restore configuration" ) );
		defineVar( CommandVar.newEnumVar( OPT_DBMODE , true , "DB Mode" , "execute database set mode" ) );
		defineVar( CommandVar.newFlagVar( OPT_DBMOVE , true , "Move Errors" , "move erroneous scripts to error subfolder in source folder" ) );
		defineVar( CommandVar.newFlagVar( OPT_DBAUTH , true , "Use DB Auth" , "do not use simple authorization" ) );
		defineVar( CommandVar.newFlagVar( OPT_CUMULATIVE , true , "Cumulative" , "cumulative release" ) );
		defineVar( CommandVar.newVar( OPT_DBALIGNED , true , "DB Aligned" , "use specific aligned set of scipts to apply" ) );
		defineVar( CommandVar.newVar( OPT_DB , true , "DB Server" , "use specific database server to apply" ) );
		defineVar( CommandVar.newVar( OPT_DBPASSWORD , true , "DB Password" , "use specified password to access database" ) );
		defineVar( CommandVar.newVar( OPT_REGIONS , true , "Regions" , "use specific set of regions to apply" ) );
		defineVar( CommandVar.newEnumVar( OPT_DBTYPE , true , "DB Change Type" , "execute database set specific change type" ) );
		defineVar( CommandVar.newVar( OPT_RELEASE , true , "Release" , "use specific release name" ) );
		defineVar( CommandVar.newVar( OPT_BRANCH , true , "Branch" , "use specific codebase branch name" ) );
		defineVar( CommandVar.newVar( OPT_TAG , true , "Tag" , "use specific codebase tag name" ) );
		defineVar( CommandVar.newVar( OPT_DATE , true , "Date" , "use codebase state on given date (ISO-8601)" ) );
		defineVar( CommandVar.newVar( OPT_GROUP , true , "Project Group" , "use specific codebase project group" ) );
		defineVar( CommandVar.newVar( OPT_VERSION , true , "Code Version" , "use specific codebase version" ) );
		defineVar( CommandVar.newVar( OPT_SG , true , "Segment" , "use segments which names meet given regular mask" ) );
		defineVar( CommandVar.newVar( OPT_DEPLOYGROUP , true , "Node Group" , "use only nodes belonging to specified deploygroup" ) );
		defineVar( CommandVar.newVar( OPT_STARTGROUP , true , "Start Group" , "use only servers belonging to specified startgroup" ) );
		defineVar( CommandVar.newVar( OPT_EXTRAARGS , true , "Extra Args" , "extra arguments for server interface scripts" ) );
		defineVar( CommandVar.newVar( OPT_UNIT , true , "Product Unit" , "use distributive items only from given unit" ) );
		defineVar( CommandVar.newVar( OPT_BUILDINFO , true , "Build Info" , "use given build info parameter" ) );
		defineVar( CommandVar.newVar( OPT_HOSTUSER , true , "Host User" , "use given user when connecting to host" ) );
		defineVar( CommandVar.newVar( OPT_NEWKEY , true , "New Key" , "use given key to change on host" ) );
		defineVar( CommandVar.newVar( OPT_BUILDMODE , true , "Build Mode" , "use given build mode (branch, trunk, major, devbranch, devtrunk)" ) );
		defineVar( CommandVar.newVar( OPT_COMPATIBILITY , true , "Compatibility" , "previous release installed" ) );
		defineVar( CommandVar.newIntVar( OPT_PORT , false , "Port" , "server port" ) );
		defineVar( CommandVar.newVar( OPT_HOST , false , "Host" , "server host" ) );
		
		defineOption( CommandOption.newFlagYesOption( this , "get" , OPT_GET , true , "run getall after build" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nget" , OPT_GET , true , "build without get" ) );
		defineOption( CommandOption.newFlagYesOption( this , "dist" , OPT_DIST , true , "copy to distributive after getall" ) );
		defineOption( CommandOption.newFlagNoOption( this , "ndist" , OPT_DIST , true , "do not copy to distributive after getall" ) );
		defineOption( CommandOption.newFlagYesOption( this , "updatenexus" , OPT_UPDATENEXUS , true , "force reupload items to thirdparty repository" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nupdatenexus" , OPT_UPDATENEXUS , true , "do not reupload items to thirdparty repository" ) );
		defineOption( CommandOption.newFlagYesOption( this , "check" , OPT_CHECK , true , "run source checks before build" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nocheck" , OPT_CHECK , true , "skip source checks before build (see also check)" ) );
		defineOption( CommandOption.newFlagYesOption( this , "replace" , OPT_REPLACE , true , "replace all item contents on deploy" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nreplace" , OPT_REPLACE , true , "do not replace exiting items on deploy" ) );
		defineOption( CommandOption.newFlagYesOption( this , "backup" , OPT_BACKUP , true , "prepare backup before deploy" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nbackup" , OPT_BACKUP , true , "do not backup before deploy" ) );
		defineOption( CommandOption.newFlagYesOption( this , "obsolete" , OPT_OBSOLETE , true , "ignore new layout" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nobsolete" , OPT_OBSOLETE , true , "use new layout" ) );
		defineOption( CommandOption.newFlagYesOption( this , "conf" , OPT_DEPLOYCONF , true , "deploy configuration files" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nconf" , OPT_DEPLOYCONF , true , "do not deploy configuration files" ) );
		defineOption( CommandOption.newFlagYesOption( this , "partial" , OPT_PARTIALCONF , true , "ignore missing configuration files" ) );
		defineOption( CommandOption.newFlagNoOption( this , "npartial" , OPT_PARTIALCONF , true , "require complete configuration files" ) );
		defineOption( CommandOption.newFlagYesOption( this , "binary" , OPT_DEPLOYBINARY , true , "deploy binary files" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nobinary" , OPT_DEPLOYBINARY , true , "do not deploy binary files" ) );
		defineOption( CommandOption.newFlagYesOption( this , "hot" , OPT_DEPLOYHOT , true , "deploy hot files only" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nhot" , OPT_DEPLOYHOT , true , "do not deploy hot files" ) );
		defineOption( CommandOption.newFlagYesOption( this , "cold" , OPT_DEPLOYCOLD , true , "deploy cold files only" ) );
		defineOption( CommandOption.newFlagNoOption( this , "ncold" , OPT_DEPLOYCOLD , true , "do not deploy cold files" ) );
		defineOption( CommandOption.newFlagYesOption( this , "raw" , OPT_DEPLOYRAW , false , "internal use only" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nraw" , OPT_DEPLOYRAW , false , "internal use only" ) );
		defineOption( CommandOption.newFlagYesOption( this , "keepalive" , OPT_KEEPALIVE , true , "automatically maintain product configuration set" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nkeepalive" , OPT_KEEPALIVE , true , "do not change product configuration set" ) );
		defineOption( CommandOption.newFlagNoOption( this , "downtime" , OPT_ZERODOWNTIME , true , "deploy with downtime" ) );
		defineOption( CommandOption.newFlagYesOption( this , "ndowntime" , OPT_ZERODOWNTIME , true , "deploy without downtime if possible" ) );
		defineOption( CommandOption.newFlagYesOption( this , "nnodes" , OPT_NONODES , true , "execute only on server-level, no nodes" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nodes" , OPT_NONODES , true , "execute on server node level" ) );
		defineOption( CommandOption.newFlagYesOption( this , "nmsg" , OPT_NOCHATMSG , true , "do not notify in chat window" ) );
		defineOption( CommandOption.newFlagNoOption( this , "msg" , OPT_NOCHATMSG , true , "notify in chat window" ) );
		defineOption( CommandOption.newFlagYesOption( this , "root" , OPT_ROOTUSER , true , "execute under root" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nroot" , OPT_ROOTUSER , true , "execute under normal user" ) );
		defineOption( CommandOption.newFlagYesOption( this , "sudo" , OPT_SUDO , true , "execute using sudo from specified hostuser" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nsudo" , OPT_SUDO , true , "execute using direct access without sudo" ) );
		defineOption( CommandOption.newFlagYesOption( this , "nversion" , OPT_IGNOREVERSION , true , "ignore version information on deploy" ) );
		defineOption( CommandOption.newFlagNoOption( this , "version" , OPT_IGNOREVERSION , true , "use version information on deploy" ) );
		defineOption( CommandOption.newFlagYesOption( this , "live" , OPT_LIVE , true , "use saved live configuration" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nlive" , OPT_LIVE , true , "do not use saved live configuration" ) );
		defineOption( CommandOption.newFlagYesOption( this , "hidden" , OPT_HIDDEN , true , "use hidden files to restore configuration" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nhidden" , OPT_HIDDEN , true , "do not use hidden files to restore configuration" ) );
		defineOption( CommandOption.newFlagEnumOption( this , "a" , "APPLY" , OPT_DBMODE , true , "execute database set - only new scipts" ) );
		defineOption( CommandOption.newFlagEnumOption( this , "x" , "ANYWAY" , OPT_DBMODE , true , "execute database set - both already applied and new scipts" ) );
		defineOption( CommandOption.newFlagEnumOption( this , "c" , "CORRECT" , OPT_DBMODE , true , "execute database set - only failed scripts" ) );
		defineOption( CommandOption.newFlagEnumOption( this , "r" , "ROLLBACK" , OPT_DBMODE , true , "execute database set - rollback" ) );
		defineOption( CommandOption.newFlagEnumOption( this , "p" , "PRINT" , OPT_DBMODE , true , "execute database set - show database status" ) );
		defineOption( CommandOption.newFlagYesOption( this , "move" , OPT_DBMOVE , true , "move erroneous scripts to error subfolder in source folder" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nmove" , OPT_DBMOVE , true , "do not move erroneous scripts to error subfolder in source folder" ) );
		defineOption( CommandOption.newFlagYesOption( this , "auth" , OPT_DBAUTH , true , "do not use simple authorization" ) );
		defineOption( CommandOption.newFlagNoOption( this , "nauth" , OPT_DBAUTH , true , "use simple authorization" ) );
		defineOption( CommandOption.newFlagYesOption( this , "cumulative" , OPT_CUMULATIVE , true , "cumulative release" ) );
		defineOption( CommandOption.newFlagNoOption( this , "ncumulative" , OPT_CUMULATIVE , true , "cumulative release" ) );
		defineOption( CommandOption.newParam( this , "aligned" , OPT_DBALIGNED , true , "use specific aligned set of scipts to apply" ) );
		defineOption( CommandOption.newParam( this , "db" , OPT_DB , true , "use specific database server to apply" ) );
		defineOption( CommandOption.newParam( this , "dbpwd" , OPT_DBPASSWORD , true , "use specified password to access database" ) );
		defineOption( CommandOption.newParam( this , "regions" , OPT_REGIONS , true , "use specific set of regions to apply" ) );
		defineOption( CommandOption.newFlagEnumOption( this , "sql" , "SQL" , OPT_DBTYPE , true , "execute database set - only scripts" ) );
		defineOption( CommandOption.newFlagEnumOption( this , "ctl" , "CTL" , OPT_DBTYPE , true , "execute database set - only load files" ) );
		defineOption( CommandOption.newFlagEnumOption( this , "pub" , "PUB" , OPT_DBTYPE , true , "execute database set - only publish files" ) );
		defineOption( CommandOption.newParam( this , "release" , OPT_RELEASE , true , "use specific release name" ) );
		defineOption( CommandOption.newParam( this , "branch" , OPT_BRANCH , true , "use specific codebase branch name" ) );
		defineOption( CommandOption.newParam( this , "tag" , OPT_TAG , true , "use specific codebase tag name" ) );
		defineOption( CommandOption.newParam( this , "date" , OPT_DATE , true , "use codebase state on given date (ISO-8601)" ) );
		defineOption( CommandOption.newParam( this , "group" , OPT_GROUP , true , "use specific codebase project group" ) );
		defineOption( CommandOption.newParam( this , "version" , OPT_VERSION , true , "use specific codebase version" ) );
		defineOption( CommandOption.newParam( this , "sg" , OPT_SG , true , "use segments which names meet given regular mask" ) );
		defineOption( CommandOption.newParam( this , "deploygroup" , OPT_DEPLOYGROUP , true , "use only nodes belonging to specified deploygroup" ) );
		defineOption( CommandOption.newParam( this , "startgroup" , OPT_STARTGROUP , true , "use only servers belonging to specified startgroup" ) );
		defineOption( CommandOption.newParam( this , "args" , OPT_EXTRAARGS , true , "extra arguments for server interface scripts" ) );
		defineOption( CommandOption.newParam( this , "unit" , OPT_UNIT , true , "use distributive items only from given unit" ) );
		defineOption( CommandOption.newParam( this , "buildinfo" , OPT_BUILDINFO , true , "use given build info parameter" ) );
		defineOption( CommandOption.newParam( this , "hostuser" , OPT_HOSTUSER , true , "use given user when connecting to host" ) );
		defineOption( CommandOption.newParam( this , "newkey" , OPT_NEWKEY , true , "use given key to change on host" ) );
		defineOption( CommandOption.newParam( this , "mode" , OPT_BUILDMODE , true , "use given build mode (branch, trunk, major, devbranch, devtrunk)" ) );
		defineOption( CommandOption.newParam( this , "over" , OPT_COMPATIBILITY , true , "previous release installed" ) );
		defineOption( CommandOption.newIntParam( this , "port" , OPT_PORT , false , "server port" ) );
		defineOption( CommandOption.newParam( this , "host" , OPT_HOST , false , "server host" ) );
	}
	
	public void defineOption( CommandOption opt ) {
		optionsDefined.add( opt );
		optionsByName.put( opt.optName , opt );
	}

	public void defineVar( CommandVar var ) {
		varByName.put( var.varName , var );
	}

	public void defineGenericVar( CommandVar var ) {
		defineVar( var );
		var.setGeneric();
	}
	
	public CommandVar[] getVars() {
		return( varByName.values().toArray( new CommandVar[0] ) );
	}
	
	public CommandVar getVar( String varName ) {
		return( varByName.get( varName ) );
	}
	
	public CommandVar getParamVar( String varName ) {
		CommandVar var = getVar( varName );
		if( var.isParam == false )
			error( "wrong type var=" + varName );
		return( var );
	}
	
	public CommandVar getIntParamVar( String varName ) {
		CommandVar var = getVar( varName );
		if( var.isParam == false || var.isInteger == false )
			error( "wrong type var=" + varName );
		return( var );
	}
	
	public CommandVar getFlagVar( String varName ) {
		CommandVar var = getVar( varName );
		if( var.isFlag == false )
			error( "wrong type var=" + varName );
		return( var );
	}
	
	public CommandVar getEnumVar( String varName ) {
		CommandVar var = getVar( varName );
		if( var.isEnum == false )
			error( "wrong type var=" + varName );
		return( var );
	}
	
	public CommandOption getOption( String optName ) {
		return( optionsByName.get( optName ) );
	}
	
	public boolean isValidVar( String var ) {
		return( varByName.containsKey( var ) );
	}
	
	public boolean isFlagOption( String opt ) {
		CommandOption info = optionsByName.get( opt ); 
		if( info != null )
			if( info.var.isFlag )
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
		CommandOption info = optionsByName.get( opt ); 
		if( info != null )
			if( info.var.isEnum )
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
		CommandOption info = optionsByName.get( opt ); 
		if( info != null )
			if( info.var.isParam )
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

	public CommandOption getVarFlagOption( CommandVar var , boolean value ) {
		for( CommandOption opt : optionsDefined ) {
			if( opt.var == var && opt.var.isFlag ) {
				if( ( opt.varFlagValue == FLAG.YES ) == value )
					return( opt );
			}
		}
		error( "unable to identify option of var=" + var.varName + ", value=" + value );
		return( null );
	}
	
	public CommandOption getVarEnumOption( CommandVar var , String value ) {
		for( CommandOption opt : optionsDefined ) {
			if( opt.var == var && opt.var.isEnum ) {
				if( opt.varEnumValue.equals( value ) )
					return( opt );
			}
		}
		error( "unable to identify option of var=" + var.varName + ", value=" + value );
		return( null );
	}
	
	public CommandOption getVarParamOption( CommandVar var ) {
		for( CommandOption opt : optionsDefined ) {
			if( opt.var == var && opt.var.isParam )
				return( opt );
		}
		error( "unable to identify option of var=" + var.varName );
		return( null );
	}
	
	private void showOptionHelp( CommandBuilder builder , CommandOption opt ) {
		String identity;
		if( opt.var.isFlag )
			identity = "-" + opt.optName + ": flag " + opt.var.varName + "=" + opt.varFlagValue;
		else
		if( opt.var.isEnum )
			identity = "-" + opt.optName + ": enum " + opt.var.varName + "=" + opt.varEnumValue;
		else
			identity = "-" + opt.optName + ": parameter (" + opt.var.varName + ")";
		
		String spacing = Common.replicate( " " , 50 - identity.length() ); 
		printhelp( "\t" + identity + spacing + opt.help ); 
	}

	public void showTopHelp( CommandBuilder builder , CommandMeta main , CommandMeta[] commands , CommandOptions options ) {
		printhelp( "URM HELP (top)" );
		printhelp( "" );
		
		printhelp( "URM assists to administer codebase, release process and to maintain testing and production environments." );
		printhelp( "Release process consists of peforming builds from codebase and administration of distributives." );
		printhelp( "Distributive adminstration contains managing release repository and specific release engineering." );
		printhelp( "Release engineering are steps to populate distributive from ready and built items." );
		printhelp( "Items are configuration files, database changes, binary files and archives." );
		printhelp( "" );
		
		printhelp( "URM instance administration:" );
		showCommandHelp( builder , main , true , options );
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
	
	public void showCommandHelp( CommandBuilder builder , CommandMeta commandInfo , boolean main , CommandOptions options ) {
		printhelp( "URM HELP (command)" );
		printhelp( "" );
		
		printhelp( "Command: " + commandInfo.name );
		printhelp( "Functions: " + commandInfo.desc );
		printhelp( "" );
		printhelp( "Available actions are:" );
		for( CommandMethodMeta action : commandInfo.actionsList ) {
			String spacing = Common.replicate( " " , 50 - action.name.length() ); 
			printhelp( "\t" + action.name + spacing + action.help );
		}
		
		printhelp( "" );
		
		if( options.isFlagSet( OPT_ALL ) ) {
			printhelp( "Generic options:" );
			for( int k = 0; k < genericOptionsCount; k++ ) {
				CommandOption var = optionsDefined.get( k );
				showOptionHelp( builder , var );
			}
		}
		else
			printhelp( "(use -all to display generic options)" );
		
		printhelp( "Specific options:" );
		boolean specific = false;
		for( int k = genericOptionsCount; k < optionsDefined.size(); k++ ) {
			CommandOption var = optionsDefined.get( k );
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
	
	public void showActionHelp( CommandBuilder builder , CommandMethodMeta action , CommandOptions options ) {
		printhelp( "URM HELP (action)" );
		printhelp( "" );
		
		printhelp( "Action: " + action.name );
		printhelp( "Function: " + action.help );
		printhelp( "Syntax: " + action.getSyntax( builder.clientrc ) );
		printhelp( "" );
		
		// show action options
		if( options.isFlagSet( OPT_ALL ) ) {
			printhelp( "All options defined for " + action.name + ":" );
			printhelp( "Generic options:" );
			for( int k = 0; k < genericOptionsCount; k++ ) {
				CommandOption var = optionsDefined.get( k );
				showOptionHelp( builder , var );
			}
		}
		else
			printhelp( "(use -all to display generic options)" );
			

		printhelp( "Specific options:" );
		boolean specific = false;
		for( int k = genericOptionsCount; k < optionsDefined.size(); k++ ) {
			CommandOption var = optionsDefined.get( k );
			if( action.isOptionApplicable( var ) ) {
				showOptionHelp( builder , var );
				specific = true;
			}
		}

		if( !specific )
			printhelp( "\t(no specific options)" );
	}
	
	public void printhelp( String s ) {
		System.out.println( "# " + s );
	}

	public void error( String s ) {
		throw new RuntimeException( s );
	}
	
}
