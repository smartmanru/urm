package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.OptionsMeta;
import org.urm.common.action.CommandMethodMeta.ACTION_ACCESS;
import org.urm.meta.engine.EngineAuth.SecurityAction;

public class DeployCommandMeta extends CommandMeta {

	public static String METHOD_BASE = "base";
	public static String METHOD_CHECKENV = "checkenv";
	public static String METHOD_CONFCHECK = "confcheck";
	public static String METHOD_CONFIGURE = "configure";
	public static String METHOD_DEPLOYREDIST = "deployredist";
	public static String METHOD_DROPREDIST = "dropredist";
	public static String METHOD_GETDEPLOYINFO = "getdeployinfo";
	public static String METHOD_GETREDISTINFO = "getredistinfo";
	public static String METHOD_HOSTS = "hosts";
	public static String METHOD_KEY = "key";
	public static String METHOD_REDIST = "redist";
	public static String METHOD_RESTARTENV = "restartenv";
	public static String METHOD_ROLLBACK = "rollback";
	public static String METHOD_ROLLOUT = "rollout";
	public static String METHOD_RUNCMD = "runcmd";
	public static String METHOD_SCP = "scp";
	public static String METHOD_LIST = "list";
	public static String METHOD_SENDCHAT = "sendchatmsg";
	public static String METHOD_STARTENV = "startenv";
	public static String METHOD_STOPENV = "stopenv";
	public static String METHOD_VERIFYCONFIGS = "verifyconfigs";
	public static String METHOD_RESTORECONFIGS = "restoreconfigs";
	public static String METHOD_SAVECONFIGS = "saveconfigs";
	public static String METHOD_UPGRADECONFIGS = "upgradeenv";
	public static String METHOD_VERIFYDEPLOY = "verifydeploy";
	public static String METHOD_WAITENV = "waitenv";
	public static String METHOD_WAITWEB = "waitweb";
	public static String METHOD_RELEASEHISTORY = "rhistory";
	public static String METHOD_HISTORY = "history";
	public static String METHOD_LOGIN = "login";
	
	public static String NAME = "deploy";
	public static String DESC = "manage environments and deploy releases";
	
	public DeployCommandMeta( OptionsMeta options ) {
		super( options , NAME , DESC );
		
		String cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_EXTRAARGS, OPT_UNIT, OPT_BUILDINFO, OPT_TAG, OPT_HOSTUSER, OPT_KEY, OPT_NEWKEY, OPT_BACKUP, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_PARTIALCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD, OPT_KEEPALIVE, OPT_SKIPERRORS, OPT_ZERODOWNTIME, OPT_NONODES, OPT_NOCHATMSG, OPT_ROOTUSER, OPT_IGNOREVERSION";
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_BASE , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "base software operations" , cmdOpts , "{install|list|clear} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DBPASSWORD, OPT_SG, OPT_EXTRAARGS, OPT_UNIT, OPT_OBSOLETE, OPT_NONODES";
		super.defineAction( CommandMethodMeta.newStatus( this , METHOD_CHECKENV , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "check environment run status" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_SG, OPT_STARTGROUP, OPT_UNIT";
		super.defineAction( CommandMethodMeta.newInfo( this , METHOD_CONFCHECK , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "check environment specification" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_HIDDEN, OPT_TAG";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_CONFIGURE , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "create final configuration from templates without redist" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_EXTRAARGS, OPT_UNIT, OPT_BACKUP, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD, OPT_KEEPALIVE, OPT_ZERODOWNTIME, OPT_NOCHATMSG";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DEPLOYREDIST , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "copy items from staging area to runtime area and restart servers when required" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_RELEASE, OPT_BACKUP";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_DROPREDIST , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "clean staging area" , cmdOpts , "{RELEASE|all} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DBPASSWORD, OPT_SG, OPT_UNIT, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD";
		super.defineAction( CommandMethodMeta.newStatus( this , METHOD_GETDEPLOYINFO , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "get information about items deployed to environment by state information" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_RELEASE, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD";
		super.defineAction( CommandMethodMeta.newStatus( this , METHOD_GETREDISTINFO , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "get information about staging content" , cmdOpts , "<RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_HOSTS , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "get or change /etc/hosts information" , cmdOpts , "{set|delete|check} host[=address] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_HOSTUSER, OPT_NEWKEY, OPT_ROOTUSER, OPT_SUDO";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_KEY , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "change ssh keys" , cmdOpts , "{list|change|add|set|delete} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_BACKUP, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD, OPT_KEEPALIVE, OPT_IGNOREVERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_REDIST , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "copy items from distributive to staging area" , cmdOpts , "<RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_EXTRAARGS, OPT_UNIT, OPT_ZERODOWNTIME, OPT_NOCHATMSG";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_RESTARTENV , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "restart servers" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_EXTRAARGS, OPT_UNIT, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD, OPT_KEEPALIVE";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_ROLLBACK , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "rollback deployment from backup in staging area" , cmdOpts , "<RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_ROLLOUT , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "copy items from staging area to runtime area" , cmdOpts , "<RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_HOSTUSER, OPT_ROOTUSER";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_RUNCMD , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "run any shell command on environment hosts" , cmdOpts , "\"<command>\" {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_SCP , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "direct copy items from local host to environment hosts" , cmdOpts , "<localpath> <remotepath> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG";
		super.defineAction( CommandMethodMeta.newInfo( this , METHOD_LIST , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "list environment specification" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_SENDCHAT , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "add message to environment chat" , cmdOpts , "\"<message>\" {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_EXTRAARGS, OPT_UNIT, OPT_NOCHATMSG";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_STARTENV , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "start servers" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_STOPENV , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "stop servers" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_TAG, OPT_LIVE, OPT_HIDDEN";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_VERIFYCONFIGS , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "compare environment configuration files with configuration repository" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_TAG, OPT_LIVE, OPT_HIDDEN, OPT_NOCHATMSG";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_RESTORECONFIGS , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "restore environment configuration files from configuration repository" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_TAG";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_SAVECONFIGS , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "save environment configuration files in live configuration repository" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_UNIT, OPT_HOSTUSER, OPT_ROOTUSER";
		super.defineAction( CommandMethodMeta.newCritical( this , METHOD_UPGRADECONFIGS , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "apply system patch to environment hosts" , cmdOpts , "PATCHID {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_RELEASE, OPT_STARTGROUP, OPT_DEPLOYGROUP, OPT_DBPASSWORD, OPT_SG, OPT_UNIT, OPT_BACKUP, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_CHECK";
		super.defineAction( CommandMethodMeta.newStatus( this , METHOD_VERIFYDEPLOY , ACTION_ACCESS.ENV , true , SecurityAction.ACTION_DEPLOY , false , "check release items in environment runtime area" , cmdOpts , "<RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_SG, OPT_EXTRAARGS, OPT_UNIT";
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_WAITENV , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "wait until specified servers have been successfully started" , cmdOpts , "{all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , METHOD_WAITWEB , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "track specified server startup progress" , cmdOpts , "<server> [<node>]" ) );
		cmdOpts = "OPT_ROOTUSER, OPT_HOSTUSER, OPT_SG";
		super.defineAction( CommandMethodMeta.newInteractive( this , METHOD_LOGIN , ACTION_ACCESS.ENV , false , SecurityAction.ACTION_DEPLOY , false , "open ssh session to specified server node" , cmdOpts , "<server> [node]" ) );
	}
	
}
