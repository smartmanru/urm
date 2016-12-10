package org.urm.engine.executor;

import org.urm.action.ActionScope;
import org.urm.action.deploy.DeployCommand;
import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.engine.ServerEngine;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandAction;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;

public class DeployCommandExecutor extends CommandExecutor {

	DeployCommand impl;
	MetaEnv env;
	MetaEnvDC dc;
	
	String propertyBasedMethods;
	
	public DeployCommandExecutor( ServerEngine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		super.defineAction( new BaseOps() , "base" );
		super.defineAction( new CheckEnv() , "checkenv" );
		super.defineAction( new ConfCheck() , "confcheck" );
		super.defineAction( new Configure() , "configure" );
		super.defineAction( new DeployRedist() , "deployredist" );
		super.defineAction( new DropRedist() , "dropredist" );
		super.defineAction( new GetDeployInfo() , "getdeployinfo" );
		super.defineAction( new GetRedistInfo() , "getredistinfo" );
		super.defineAction( new Hosts() , "hosts" );
		super.defineAction( new Key() , "key" );
		super.defineAction( new Redist() , "redist" );
		super.defineAction( new RestartEnv() , "restartenv" );
		super.defineAction( new Rollback() , "rollback" );
		super.defineAction( new Rollout() , "rollout" );
		super.defineAction( new RunCmd() , "runcmd" );
		super.defineAction( new Scp() , "scp" );
		super.defineAction( new List() , "list" );
		super.defineAction( new SendChatMsg() , "sendchatmsg" );
		super.defineAction( new StartEnv() , "startenv" );
		super.defineAction( new StopEnv() , "stopenv" );
		super.defineAction( new VerifyConfigs() , "verifyconfigs" );
		super.defineAction( new RestoreConfigs() , "restoreconfigs" );
		super.defineAction( new SaveConfigs() , "saveconfigs" );
		super.defineAction( new UpgradeEnv() , "upgradeenv" );
		super.defineAction( new VerifyDeploy() , "verifydeploy" );
		super.defineAction( new WaitEnv() , "waitenv" );
		super.defineAction( new WaitWeb() , "waitweb" );
		super.defineAction( new Login() , "login" );
		
		propertyBasedMethods = "confcheck configure deployredist redist restoreconfigs verifydeploy";
	}
	
	public boolean run( ActionInit action ) {
		try {
			// create implementation
			impl = new DeployCommand();
			
			boolean loadProps = Common.checkPartOfSpacedList( action.actionName , propertyBasedMethods ); 
			action.context.loadEnv( action , loadProps );
		}
		catch( Throwable e ) {
			action.handle( e );
			return( false );
		}
		
		// log action and run 
		boolean res = super.runMethod( action , action.commandAction );
		return( res );
	}

	private Dist getDist( ActionInit action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.artefactory.getDistStorageByLabel( action , meta , RELEASELABEL );
		return( dist );
	}
	
	private ActionScope getServerScope( ActionInit action ) throws Exception {
		return( getServerScope( action , 0 ) );
	}
	
	private ActionScope getServerScope( ActionInit action , int posFrom ) throws Exception {
		Dist dist = null;
		Meta meta = action.getContextMeta();
		if( !action.context.CTX_RELEASELABEL.isEmpty() )
			dist = action.artefactory.getDistStorageByLabel( action , meta , action.context.CTX_RELEASELABEL );
		
		return( getServerScope( action , posFrom , dist ) );
	}

	private ActionScope getServerScope( ActionInit action , int posFrom , Dist release ) throws Exception {
		String s = getArg( action , posFrom + 1 );
		if( s.matches( "[0-9]+" ) ) {
			String SERVER = getArg( action , posFrom );
			String[] NODES = getArgList( action , posFrom + 1 );
			if( action.context.dc == null ) {
				if( !SERVER.isEmpty() )
					action.exit0( _Error.MissingSegmentName0, "Segment name is required to use specific server" );
				return( ActionScope.getEnvScope( action , action.context.env , null , release ) );
			}
			return( ActionScope.getEnvServerNodesScope( action , action.context.dc , SERVER , NODES , release ) );
		}
		
		String[] SERVERS = getArgList( action , posFrom );
		Meta meta = action.getContextMeta();
		return( ActionScope.getEnvServersScope( action , meta , action.context.dc , SERVERS , release ) );
	}
	
	private class BaseOps extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "CMD" );
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
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.deployRedist( action , scope , dist );
	}
	}

	private class DropRedist extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String releaseDir = getRequiredArg( action , 0 , "release" );
		ActionScope scope = getServerScope( action , 1 );
		impl.dropRedist( action , scope , releaseDir );
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
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.getRedistInfo( action , scope , dist );
	}
	}

	private class Hosts extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "COMMAND" );
		String VALUE = getRequiredArg( action , 1 , "COMMAND" );
		
		String F_HOST_NAME = Common.getPartBeforeFirst( VALUE , "=" );
		String F_HOST_IP = "";
		if( VALUE.indexOf( '=' ) >= 0 ) {
			F_HOST_IP = Common.getPartAfterFirst( VALUE , "=" );
			if( !F_HOST_IP.matches( "[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+" ) )
				action.exit1( _Error.InvalidHostIP , "Hosts: invalid IP=" + F_HOST_IP , F_HOST_IP );
		}

		if( CMD.equals( "set" ) ) {
			if( F_HOST_NAME.isEmpty() || F_HOST_IP.isEmpty() )
				action.exit1( _Error.InvalidActionValue1 , "Hosts: invalid value=" + VALUE + ", expected in form host=address" , VALUE );
		}
		else 
		if( CMD.equals( "delete" ) || CMD.equals( "check" ) ) {
			if( F_HOST_NAME.isEmpty() )
				action.exit1( _Error.InvalidActionValue1 , "Hosts: invalid value=" + VALUE , VALUE );
		}
		else
			action.exit1( _Error.InvalidHostsCommand1 , "Hosts: invalid command=" + CMD , CMD );
		
		ActionScope scope = getServerScope( action , 2 );
		impl.changeHosts( action , scope , CMD , F_HOST_NAME , F_HOST_IP );
	}
	}

	private class Key extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "COMMAND" );
		ActionScope scope = getServerScope( action , 1 );
		impl.changeKeys( action , scope , CMD );
	}
	}

	private class Login extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String NODE = getArg( action , 1 );
		impl.login( action , action.context.dc , SERVER , NODE );
	}
	}

	private class Redist extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.redist( action , scope , dist );
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
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.rollback( action , scope , dist );
	}
	}

	private class Rollout extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.rollout( action , scope , dist );
	}
	}

	private class RunCmd extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "COMMAND" );
		ActionScope scope = getServerScope( action , 1 );
		impl.runCmd( action , scope , CMD );
	}
	}

	private class Scp extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		String SRCINFO = getRequiredArg( action , 0 , "SRCINFO" );
		String DSTPATH = getRequiredArg( action , 1 , "DSTPATH" );
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
		String msg = getRequiredArg( action , 0 , "MSG" );
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

	private class VerifyConfigs extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.verifyConfigs( action , scope );
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
		String PATCHID = getRequiredArg( action , 0 , "PATCHID" );
		ActionScope scope = getServerScope( action , 1 );
		impl.upgradeEnv( action , PATCHID , scope );
	}
	}

	private class VerifyDeploy extends CommandAction {
	public void run( ActionInit action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.verifyDeploy( action , scope , dist );
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
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String NODE = getArg( action , 1 );
		impl.waitWeb( action , SERVER , NODE );
	}
	}
	
}
