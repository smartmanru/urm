package org.urm.client.meta;

import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class DeployCommandMeta extends CommandMeta {

	public static String NAME = "deploy";
	
	public DeployCommandMeta( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_BUILDINFO, GETOPT_TAG, GETOPT_HOSTUSER, GETOPT_KEY, GETOPT_NEWKEY, GETOPT_BACKUP, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_PARTIALCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD, GETOPT_KEEPALIVE, GETOPT_SKIPERRORS, GETOPT_ZERODOWNTIME, GETOPT_NONODES, GETOPT_NOCHATMSG, GETOPT_ROOTUSER, GETOPT_IGNOREVERSION";
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC";
		super.defineAction( CommandMethod.newAction( "base" , false , "base software operations" , cmdOpts , "./base.sh [OPTIONS] {install|list|clear} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DBPASSWORD, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_OBSOLETE, GETOPT_NONODES";
		super.defineAction( CommandMethod.newAction( "checkenv" , false , "check environment run status" , cmdOpts , "./checkenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DC, GETOPT_STARTGROUP, GETOPT_UNIT";
		super.defineAction( CommandMethod.newAction( "confcheck" , false , "check environment specification" , cmdOpts , "./confcheck.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_HIDDEN, GETOPT_TAG";
		super.defineAction( CommandMethod.newAction( "configure" , false , "create final configuration from templates without redist" , cmdOpts , "./configure.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_BACKUP, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD, GETOPT_KEEPALIVE, GETOPT_SKIPERRORS, GETOPT_ZERODOWNTIME, GETOPT_NOCHATMSG";
		super.defineAction( CommandMethod.newAction( "deployredist" , false , "copy items from staging area to runtime area and restart servers when required" , cmdOpts , "./deployredist.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_RELEASE, GETOPT_KEY, GETOPT_BACKUP";
		super.defineAction( CommandMethod.newAction( "dropredist" , false , "clean staging area" , cmdOpts , "./dropredist.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DBPASSWORD, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD";
		super.defineAction( CommandMethod.newAction( "getdeployinfo" , false , "get information about items deployed to environment by state information" , cmdOpts , "./getdeployinfo.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY, GETOPT_RELEASE, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD";
		super.defineAction( CommandMethod.newAction( "getredistinfo" , false , "get information about staging content" , cmdOpts , "./getredistinfo.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY";
		super.defineAction( CommandMethod.newAction( "hosts" , false , "get or change /etc/hosts information" , cmdOpts , "./hosts.sh [OPTIONS] {set|delete|check} host[=address] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_HOSTUSER, GETOPT_KEY, GETOPT_NEWKEY, GETOPT_ROOTUSER, GETOPT_SUDO";
		super.defineAction( CommandMethod.newAction( "key" , false , "change ssh keys" , cmdOpts , "./key.sh [OPTIONS] {list|change|add|set|delete} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY, GETOPT_BACKUP, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD, GETOPT_KEEPALIVE, GETOPT_SKIPERRORS, GETOPT_IGNOREVERSION";
		super.defineAction( CommandMethod.newAction( "redist" , false , "copy items from distributive to staging area" , cmdOpts , "./redist.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_ZERODOWNTIME, GETOPT_NOCHATMSG";
		super.defineAction( CommandMethod.newAction( "restartenv" , false , "restart servers" , cmdOpts , "./restartenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD, GETOPT_KEEPALIVE, GETOPT_SKIPERRORS";
		super.defineAction( CommandMethod.newAction( "rollback" , false , "rollback deployment from backup in staging area" , cmdOpts , "./rollback.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethod.newAction( "rollout" , false , "copy items from staging area to runtime area" , cmdOpts , "./rollout.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_HOSTUSER, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_ROOTUSER";
		super.defineAction( CommandMethod.newAction( "runcmd" , false , "run any shell command on environment hosts" , cmdOpts , "./runcmd.sh [OPTIONS] \"command\" {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethod.newAction( "scp" , false , "direct copy items from local host to environment hosts" , cmdOpts , "./scp.sh [OPTIONS] localpath remotepath {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC";
		super.defineAction( CommandMethod.newAction( "list" , false , "list environment specification" , cmdOpts , "./list.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "";
		super.defineAction( CommandMethod.newAction( "sendchatmsg" , false , "add message to environment chat" , cmdOpts , "./sendchatmsg.sh [OPTIONS] \"message\" {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_NOCHATMSG";
		super.defineAction( CommandMethod.newAction( "startenv" , false , "start servers" , cmdOpts , "./startenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethod.newAction( "stopenv" , false , "stop servers" , cmdOpts , "./stopenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_TAG, GETOPT_LIVE, GETOPT_HIDDEN, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_NOCHATMSG";
		super.defineAction( CommandMethod.newAction( "restoreconfigs" , false , "restore environment configuration files from configuration repository" , cmdOpts , "./restoreconfigs.sh {live|prod} [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_TAG, GETOPT_KEY";
		super.defineAction( CommandMethod.newAction( "saveconfigs" , false , "save environment configuration files in live configuration repository" , cmdOpts , "./saveconfigs.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_HOSTUSER, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_ROOTUSER";
		super.defineAction( CommandMethod.newAction( "upgradeenv" , false , "apply system patch to environment hosts" , cmdOpts , "./upgradeenv.sh [OPTIONS] PATCHID {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_RELEASE, GETOPT_STARTGROUP, GETOPT_DEPLOYGROUP, GETOPT_DBPASSWORD, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY, GETOPT_BACKUP, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_CHECK";
		super.defineAction( CommandMethod.newAction( "verifydeploy" , false , "check release items in environment runtime area" , cmdOpts , "./verifydeploy.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_SKIPERRORS";
		super.defineAction( CommandMethod.newAction( "waitenv" , false , "wait until specified servers have been successfully started" , cmdOpts , "./waitenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandMethod.newAction( "waitweb" , false , "track specified server startup progress" , cmdOpts , "./waitweb.sh [OPTIONS] <server> [<node>]" ) );
		cmdOpts = "GETOPT_ROOTUSER, GETOPT_HOSTUSER, GETOPT_KEY";
		super.defineAction( CommandMethod.newAction( "login" , false , "open ssh session to specified server node" , cmdOpts , "./login.sh [OPTIONS] <server> [node]" ) );
	}
	
}
