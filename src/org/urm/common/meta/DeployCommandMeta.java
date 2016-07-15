package org.urm.common.meta;

import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandMeta;

public class DeployCommandMeta extends CommandMeta {

	public static String NAME = "deploy";
	public static String DESC = "manage environments and deploy releases";
	
	public DeployCommandMeta() {
		super( NAME , DESC );
		
		String cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_EXTRAARGS, OPT_UNIT, OPT_BUILDINFO, OPT_TAG, OPT_HOSTUSER, OPT_KEY, OPT_NEWKEY, OPT_BACKUP, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_PARTIALCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD, OPT_KEEPALIVE, OPT_SKIPERRORS, OPT_ZERODOWNTIME, OPT_NONODES, OPT_NOCHATMSG, OPT_ROOTUSER, OPT_IGNOREVERSION";
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC";
		super.defineAction( CommandMethodMeta.newCritical( this , "base" , false , "base software operations" , cmdOpts , "./base.sh [OPTIONS] {install|list|clear} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DBPASSWORD, OPT_DC, OPT_EXTRAARGS, OPT_UNIT, OPT_KEY, OPT_OBSOLETE, OPT_NONODES";
		super.defineAction( CommandMethodMeta.newStatus( this , "checkenv" , false , "check environment run status" , cmdOpts , "./checkenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DC, OPT_STARTGROUP, OPT_UNIT";
		super.defineAction( CommandMethodMeta.newInfo( this , "confcheck" , false , "check environment specification" , cmdOpts , "./confcheck.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_HIDDEN, OPT_TAG";
		super.defineAction( CommandMethodMeta.newNormal( this , "configure" , false , "create final configuration from templates without redist" , cmdOpts , "./configure.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_EXTRAARGS, OPT_UNIT, OPT_KEY, OPT_BACKUP, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD, OPT_KEEPALIVE, OPT_SKIPERRORS, OPT_ZERODOWNTIME, OPT_NOCHATMSG";
		super.defineAction( CommandMethodMeta.newCritical( this , "deployredist" , false , "copy items from staging area to runtime area and restart servers when required" , cmdOpts , "./deployredist.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_RELEASE, OPT_KEY, OPT_BACKUP";
		super.defineAction( CommandMethodMeta.newCritical( this , "dropredist" , false , "clean staging area" , cmdOpts , "./dropredist.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DBPASSWORD, OPT_DC, OPT_UNIT, OPT_KEY, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD";
		super.defineAction( CommandMethodMeta.newStatus( this , "getdeployinfo" , false , "get information about items deployed to environment by state information" , cmdOpts , "./getdeployinfo.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_KEY, OPT_RELEASE, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD";
		super.defineAction( CommandMethodMeta.newStatus( this , "getredistinfo" , false , "get information about staging content" , cmdOpts , "./getredistinfo.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_KEY";
		super.defineAction( CommandMethodMeta.newNormal( this , "hosts" , false , "get or change /etc/hosts information" , cmdOpts , "./hosts.sh [OPTIONS] {set|delete|check} host[=address] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_HOSTUSER, OPT_KEY, OPT_NEWKEY, OPT_ROOTUSER, OPT_SUDO";
		super.defineAction( CommandMethodMeta.newNormal( this , "key" , false , "change ssh keys" , cmdOpts , "./key.sh [OPTIONS] {list|change|add|set|delete} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_KEY, OPT_BACKUP, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD, OPT_KEEPALIVE, OPT_SKIPERRORS, OPT_IGNOREVERSION";
		super.defineAction( CommandMethodMeta.newNormal( this , "redist" , false , "copy items from distributive to staging area" , cmdOpts , "./redist.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_EXTRAARGS, OPT_UNIT, OPT_KEY, OPT_SKIPERRORS, OPT_ZERODOWNTIME, OPT_NOCHATMSG";
		super.defineAction( CommandMethodMeta.newCritical( this , "restartenv" , false , "restart servers" , cmdOpts , "./restartenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_EXTRAARGS, OPT_UNIT, OPT_KEY, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_DEPLOYHOT, OPT_DEPLOYCOLD, OPT_KEEPALIVE, OPT_SKIPERRORS";
		super.defineAction( CommandMethodMeta.newCritical( this , "rollback" , false , "rollback deployment from backup in staging area" , cmdOpts , "./rollback.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "rollout" , false , "copy items from staging area to runtime area" , cmdOpts , "./rollout.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_HOSTUSER, OPT_KEY, OPT_SKIPERRORS, OPT_ROOTUSER";
		super.defineAction( CommandMethodMeta.newNormal( this , "runcmd" , false , "run any shell command on environment hosts" , cmdOpts , "./runcmd.sh [OPTIONS] \"command\" {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "scp" , false , "direct copy items from local host to environment hosts" , cmdOpts , "./scp.sh [OPTIONS] localpath remotepath {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC";
		super.defineAction( CommandMethodMeta.newInfo( this , "list" , false , "list environment specification" , cmdOpts , "./list.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "";
		super.defineAction( CommandMethodMeta.newNormal( this , "sendchatmsg" , false , "add message to environment chat" , cmdOpts , "./sendchatmsg.sh [OPTIONS] \"message\" {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_EXTRAARGS, OPT_UNIT, OPT_KEY, OPT_SKIPERRORS, OPT_NOCHATMSG";
		super.defineAction( CommandMethodMeta.newNormal( this , "startenv" , false , "start servers" , cmdOpts , "./startenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethodMeta.newCritical( this , "stopenv" , false , "stop servers" , cmdOpts , "./stopenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_TAG, OPT_LIVE, OPT_HIDDEN, OPT_KEY, OPT_SKIPERRORS, OPT_NOCHATMSG";
		super.defineAction( CommandMethodMeta.newCritical( this , "restoreconfigs" , false , "restore environment configuration files from configuration repository" , cmdOpts , "./restoreconfigs.sh {live|prod} [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_TAG, OPT_KEY";
		super.defineAction( CommandMethodMeta.newNormal( this , "saveconfigs" , false , "save environment configuration files in live configuration repository" , cmdOpts , "./saveconfigs.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_UNIT, OPT_HOSTUSER, OPT_KEY, OPT_SKIPERRORS, OPT_ROOTUSER";
		super.defineAction( CommandMethodMeta.newCritical( this , "upgradeenv" , false , "apply system patch to environment hosts" , cmdOpts , "./upgradeenv.sh [OPTIONS] PATCHID {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_RELEASE, OPT_STARTGROUP, OPT_DEPLOYGROUP, OPT_DBPASSWORD, OPT_DC, OPT_UNIT, OPT_KEY, OPT_BACKUP, OPT_OBSOLETE, OPT_DEPLOYCONF, OPT_DEPLOYBINARY, OPT_CHECK";
		super.defineAction( CommandMethodMeta.newStatus( this , "verifydeploy" , false , "check release items in environment runtime area" , cmdOpts , "./verifydeploy.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "OPT_ALL, OPT_DEPLOYGROUP, OPT_STARTGROUP, OPT_DC, OPT_EXTRAARGS, OPT_UNIT, OPT_KEY, OPT_SKIPERRORS";
		super.defineAction( CommandMethodMeta.newNormal( this , "waitenv" , false , "wait until specified servers have been successfully started" , cmdOpts , "./waitenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethodMeta.newNormal( this , "waitweb" , false , "track specified server startup progress" , cmdOpts , "./waitweb.sh [OPTIONS] <server> [<node>]" ) );
		cmdOpts = "OPT_ROOTUSER, OPT_HOSTUSER, OPT_KEY";
		super.defineAction( CommandMethodMeta.newInteractive( this , "login" , false , "open ssh session to specified server node" , cmdOpts , "./login.sh [OPTIONS] <server> [node]" ) );
	}
	
}
