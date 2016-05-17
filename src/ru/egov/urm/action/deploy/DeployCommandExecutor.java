package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionInit;
import ru.egov.urm.action.ActionScope;
import ru.egov.urm.action.CommandAction;
import ru.egov.urm.action.CommandBuilder;
import ru.egov.urm.action.CommandExecutor;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.storage.DistStorage;

public class DeployCommandExecutor extends CommandExecutor {

	public static String NAME = "deploy";
	DeployCommand impl;
	MetaEnv env;
	MetaEnvDC dc;
	
	String propertyBasedMethods;
	
	public DeployCommandExecutor( CommandBuilder builder ) {
		super( builder , NAME );
		
		String cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_BUILDINFO, GETOPT_TAG, GETOPT_HOSTUSER, GETOPT_KEY, GETOPT_NEWKEY, GETOPT_BACKUP, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_PARTIALCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD, GETOPT_KEEPALIVE, GETOPT_SKIPERRORS, GETOPT_ZERODOWNTIME, GETOPT_NONODES, GETOPT_NOCHATMSG, GETOPT_ROOTUSER, GETOPT_IGNOREVERSION";
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC";
		super.defineAction( CommandAction.newAction( new BaseOps() , "base" , false , "base software operations" , cmdOpts , "./base.sh [OPTIONS] {install|list|clear} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DBPASSWORD, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_OBSOLETE, GETOPT_NONODES";
		super.defineAction( CommandAction.newAction( new CheckEnv() , "checkenv" , false , "check environment run status" , cmdOpts , "./checkenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DC, GETOPT_STARTGROUP, GETOPT_UNIT";
		super.defineAction( CommandAction.newAction( new ConfCheck() , "confcheck" , false , "check environment specification" , cmdOpts , "./confcheck.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_HIDDEN, GETOPT_TAG";
		super.defineAction( CommandAction.newAction( new Configure() , "configure" , false , "create final configuration from templates without redist" , cmdOpts , "./configure.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_BACKUP, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD, GETOPT_KEEPALIVE, GETOPT_SKIPERRORS, GETOPT_ZERODOWNTIME, GETOPT_NOCHATMSG";
		super.defineAction( CommandAction.newAction( new DeployRedist() , "deployredist" , false , "copy items from staging area to runtime area and restart servers when required" , cmdOpts , "./deployredist.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_RELEASE, GETOPT_KEY, GETOPT_BACKUP";
		super.defineAction( CommandAction.newAction( new DropRedist() , "dropredist" , false , "clean staging area" , cmdOpts , "./dropredist.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD";
		super.defineAction( CommandAction.newAction( new GetDeployInfo() , "getdeployinfo" , false , "get information about items deployed to environment by state information" , cmdOpts , "./getdeployinfo.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY, GETOPT_RELEASE, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD";
		super.defineAction( CommandAction.newAction( new GetRedistInfo() , "getredistinfo" , false , "get information about staging content" , cmdOpts , "./getredistinfo.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY";
		super.defineAction( CommandAction.newAction( new Hosts() , "hosts" , false , "get or change /etc/hosts information" , cmdOpts , "./hosts.sh [OPTIONS] {set|delete|check} host[=address] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_HOSTUSER, GETOPT_KEY, GETOPT_NEWKEY, GETOPT_ROOTUSER, GETOPT_SUDO";
		super.defineAction( CommandAction.newAction( new Key() , "key" , false , "change ssh keys" , cmdOpts , "./key.sh [OPTIONS] {list|change|add|set|delete} {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY, GETOPT_BACKUP, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD, GETOPT_KEEPALIVE, GETOPT_SKIPERRORS, GETOPT_IGNOREVERSION";
		super.defineAction( CommandAction.newAction( new Redist() , "redist" , false , "copy items from distributive to staging area" , cmdOpts , "./redist.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_ZERODOWNTIME, GETOPT_NOCHATMSG";
		super.defineAction( CommandAction.newAction( new RestartEnv() , "restartenv" , false , "restart servers" , cmdOpts , "./restartenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_DEPLOYHOT, GETOPT_DEPLOYCOLD, GETOPT_KEEPALIVE, GETOPT_SKIPERRORS";
		super.defineAction( CommandAction.newAction( new Rollback() , "rollback" , false , "rollback deployment from backup in staging area" , cmdOpts , "./rollback.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandAction.newAction( new Rollout() , "rollout" , false , "copy items from staging area to runtime area" , cmdOpts , "./rollout.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_HOSTUSER, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_ROOTUSER";
		super.defineAction( CommandAction.newAction( new RunCmd() , "runcmd" , false , "run any shell command on environment hosts" , cmdOpts , "./runcmd.sh [OPTIONS] \"command\" {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandAction.newAction( new Scp() , "scp" , false , "direct copy items from local host to environment hosts" , cmdOpts , "./scp.sh [OPTIONS] localpath remotepath {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC";
		super.defineAction( CommandAction.newAction( new List() , "list" , false , "list environment specification" , cmdOpts , "./list.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "";
		super.defineAction( CommandAction.newAction( new SendChatMsg() , "sendchatmsg" , false , "add message to environment chat" , cmdOpts , "./sendchatmsg.sh [OPTIONS] \"message\" {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_NOCHATMSG";
		super.defineAction( CommandAction.newAction( new StartEnv() , "startenv" , false , "start servers" , cmdOpts , "./startenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandAction.newAction( new StopEnv() , "stopenv" , false , "stop servers" , cmdOpts , "./stopenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_TAG, GETOPT_LIVE, GETOPT_HIDDEN, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_NOCHATMSG";
		super.defineAction( CommandAction.newAction( new RestoreConfigs() , "restoreconfigs" , false , "restore environment configuration files from configuration repository" , cmdOpts , "./restoreconfigs.sh {live|prod} [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_TAG, GETOPT_KEY";
		super.defineAction( CommandAction.newAction( new SaveConfigs() , "saveconfigs" , false , "save environment configuration files in live configuration repository" , cmdOpts , "./saveconfigs.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_HOSTUSER, GETOPT_KEY, GETOPT_SKIPERRORS, GETOPT_ROOTUSER";
		super.defineAction( CommandAction.newAction( new UpgradeEnv() , "upgradeenv" , false , "apply system patch to environment hosts" , cmdOpts , "./upgradeenv.sh [OPTIONS] PATCHID {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_RELEASE, GETOPT_STARTGROUP, GETOPT_DEPLOYGROUP, GETOPT_DC, GETOPT_UNIT, GETOPT_KEY, GETOPT_BACKUP, GETOPT_OBSOLETE, GETOPT_DEPLOYCONF, GETOPT_DEPLOYBINARY, GETOPT_CHECK";
		super.defineAction( CommandAction.newAction( new VerifyDeploy() , "verifydeploy" , false , "check release items in environment runtime area" , cmdOpts , "./verifydeploy.sh [OPTIONS] <RELEASELABEL> {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		cmdOpts = "GETOPT_ALL, GETOPT_DEPLOYGROUP, GETOPT_STARTGROUP, GETOPT_DC, GETOPT_EXTRAARGS, GETOPT_UNIT, GETOPT_KEY, GETOPT_SKIPERRORS";
		super.defineAction( CommandAction.newAction( new WaitEnv() , "waitenv" , false , "wait until specified servers have been successfully started" , cmdOpts , "./waitenv.sh [OPTIONS] {all|<servers>|<server> <node1> ... <nodeN>}" ) );
		super.defineAction( CommandAction.newAction( new WaitWeb() , "waitweb" , false , "track specified server startup progress" , cmdOpts , "./waitweb.sh [OPTIONS] <server> [<node>]" ) );
		cmdOpts = "GETOPT_ROOTUSER, GETOPT_HOSTUSER, GETOPT_KEY";
		super.defineAction( CommandAction.newAction( new Login() , "login" , false , "open ssh session to specified server node" , cmdOpts , "./login.sh [OPTIONS] <server> [node]" ) );
		
		propertyBasedMethods = "confcheck configure deployredist redist restoreconfigs verifydeploy";
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new DeployCommand();
			meta.loadDistr( action );
			meta.loadSources( action );
			
			boolean loadProps = Common.checkPartOfSpacedList( commandAction.name , propertyBasedMethods ); 
			action.context.loadEnv( action , loadProps );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , commandAction );
		return( res );
	}

	private ActionScope getReleaseScope( ActionInit action ) throws Exception {
		String RELEASELABEL = options.getRequiredArg( action , 0 , "RELEASELABEL" );
		DistStorage dist = action.artefactory.getDistStorageByLabel( action , RELEASELABEL );
		return( getServerScope( action , 1 , dist ) );
	}
	
	private ActionScope getServerScope( ActionInit action ) throws Exception {
		return( getServerScope( action , 0 ) );
	}
	
	private ActionScope getServerScope( ActionInit action , int posFrom ) throws Exception {
		DistStorage dist = null;
		if( !action.context.CTX_RELEASELABEL.isEmpty() )
			dist = action.artefactory.getDistStorageByLabel( action , action.context.CTX_RELEASELABEL );
		
		return( getServerScope( action , posFrom , dist ) );
	}

	private ActionScope getServerScope( ActionInit action , int posFrom , DistStorage release ) throws Exception {
		String s = options.getArg( posFrom + 1 );
		if( s.matches( "[0-9]+" ) ) {
			String SERVER = options.getArg( posFrom );
			String[] NODES = options.getArgList( posFrom + 1 );
			return( ActionScope.getEnvServerNodesScope( action , action.context.dc , SERVER , NODES , release ) );
		}
		
		String[] SERVERS = options.getArgList( posFrom );
		return( ActionScope.getEnvServersScope( action , action.context.dc , SERVERS , release ) );
	}
	
	private class BaseOps extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = options.getRequiredArg( action , 0 , "CMD" );
		ActionScope scope = getServerScope( action , 1 );
		if( CMD.equals( "install" ) )
			impl.baseInstall( action , scope );
		else
		if( CMD.equals( "list" ) )
			impl.baseList( action , scope );
		else
		if( CMD.equals( "clear" ) )
			impl.baseClear( action , scope );
		else
			action.exitUnexpectedState();
	}
	}

	private class CheckEnv extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.checkEnv( action , scope );
	}
	}

	private class ConfCheck extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.confCheck( action , scope );
	}
	}

	private class Configure extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.configure( action , scope );
	}
	}

	private class DeployRedist extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getReleaseScope( action );
		impl.deployRedist( action , scope , scope.release );
	}
	}

	private class DropRedist extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.dropRedist( action , scope , scope.release );
	}
	}

	private class GetDeployInfo extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.getDeployInfo( action , scope );
	}
	}

	private class GetRedistInfo extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.getRedistInfo( action , scope , scope.release );
	}
	}

	private class Hosts extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = options.getRequiredArg( action , 0 , "COMMAND" );
		String VALUE = options.getRequiredArg( action , 1 , "COMMAND" );
		
		String F_HOST_NAME = Common.getPartBeforeFirst( VALUE , "=" );
		String F_HOST_IP = "";
		if( VALUE.indexOf( '=' ) >= 0 ) {
			F_HOST_IP = Common.getPartAfterFirst( VALUE , "=" );
			if( !F_HOST_IP.matches( "[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+" ) )
				action.exit( "Hosts: invalid IP=" + F_HOST_IP );
		}

		if( CMD.equals( "set" ) ) {
			if( F_HOST_NAME.isEmpty() || F_HOST_IP.isEmpty() )
				action.exit( "Hosts: invalid value=" + VALUE + ", expected in form host=address" );
		}
		else 
		if( CMD.equals( "delete" ) || CMD.equals( "check" ) ) {
			if( F_HOST_NAME.isEmpty() )
				action.exit( "Hosts: invalid value=" + VALUE );
		}
		else
			action.exit( "Hosts: invalid command=" + CMD );
		
		ActionScope scope = getServerScope( action , 2 );
		impl.changeHosts( action , scope , CMD , F_HOST_NAME , F_HOST_IP );
	}
	}

	private class Key extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = options.getRequiredArg( action , 0 , "COMMAND" );
		ActionScope scope = getServerScope( action , 1 );
		impl.changeKeys( action , scope , CMD );
	}
	}

	private class Login extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = options.getRequiredArg( action , 0 , "SERVER" );
		String NODE = options.getArg( 1 );
		impl.login( action , action.context.dc , SERVER , NODE );
	}
	}

	private class Redist extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getReleaseScope( action );
		impl.redist( action , scope , scope.release );
	}
	}

	private class RestartEnv extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.restartEnv( action , scope );
	}
	}

	private class Rollback extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getReleaseScope( action );
		impl.rollback( action , scope , scope.release );
	}
	}

	private class Rollout extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getReleaseScope( action );
		impl.rollout( action , scope , scope.release );
	}
	}

	private class RunCmd extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = options.getRequiredArg( action , 0 , "COMMAND" );
		ActionScope scope = getServerScope( action , 1 );
		impl.runCmd( action , scope , CMD );
	}
	}

	private class Scp extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SRCINFO = options.getRequiredArg( action , 0 , "SRCINFO" );
		String DSTPATH = options.getRequiredArg( action , 1 , "DSTPATH" );
		ActionScope scope = getServerScope( action , 2 );
		impl.scp( action , scope , SRCINFO , DSTPATH );
	}
	}

	private class List extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		action.context.CTX_ALL = true;
		ActionScope scope = getServerScope( action );
		impl.list( action , scope );
	}
	}

	private class SendChatMsg extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String msg = options.getRequiredArg( action , 0 , "MSG" );
		impl.sendMsg( action , msg );
	}
	}

	private class StartEnv extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.startEnv( action , scope );
	}
	}

	private class StopEnv extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.stopEnv( action , scope );
	}
	}

	private class RestoreConfigs extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.restoreConfigs( action , scope );
	}
	}

	private class SaveConfigs extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.saveConfigs( action , scope );
	}
	}

	private class UpgradeEnv extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String PATCHID = options.getRequiredArg( action , 0 , "PATCHID" );
		ActionScope scope = getServerScope( action , 1 );
		impl.upgradeEnv( action , PATCHID , scope );
	}
	}

	private class VerifyDeploy extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getReleaseScope( action );
		impl.verifyDeploy( action , scope , scope.release );
	}
	}

	private class WaitEnv extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.waitEnv( action , scope );
	}
	}

	private class WaitWeb extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = options.getRequiredArg( action , 0 , "SERVER" );
		String NODE = options.getArg( 1 );
		impl.waitWeb( action , SERVER , NODE );
	}
	}
	
}
