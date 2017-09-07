package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.ActionEnvScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.deploy.DeployCommand;
import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;

public class DeployCommandExecutor extends CommandExecutor {

	DeployCommand impl;
	MetaEnv env;
	MetaEnvSegment sg;
	
	String propertyBasedMethods;
	
	public static DeployCommandExecutor createExecutor( Engine engine ) throws Exception {
		DeployCommandMeta commandInfo = new DeployCommandMeta( engine.optionsMeta );
		return( new DeployCommandExecutor( engine , commandInfo ) );
	}
		
	private DeployCommandExecutor( Engine engine , CommandMeta commandInfo ) throws Exception {
		super( engine , commandInfo );
		
		super.defineAction( new BaseOps() , DeployCommandMeta.METHOD_BASE );
		super.defineAction( new CheckEnv() , DeployCommandMeta.METHOD_CHECKENV );
		super.defineAction( new ConfCheck() , DeployCommandMeta.METHOD_CONFCHECK );
		super.defineAction( new Configure() , DeployCommandMeta.METHOD_CONFIGURE );
		super.defineAction( new DeployRedist() , DeployCommandMeta.METHOD_DEPLOYREDIST );
		super.defineAction( new DropRedist() , DeployCommandMeta.METHOD_DROPREDIST );
		super.defineAction( new GetDeployInfo() , DeployCommandMeta.METHOD_GETDEPLOYINFO );
		super.defineAction( new GetRedistInfo() , DeployCommandMeta.METHOD_GETREDISTINFO );
		super.defineAction( new Hosts() , DeployCommandMeta.METHOD_HOSTS );
		super.defineAction( new Key() , DeployCommandMeta.METHOD_KEY );
		super.defineAction( new Redist() , DeployCommandMeta.METHOD_REDIST );
		super.defineAction( new RestartEnv() , DeployCommandMeta.METHOD_RESTARTENV );
		super.defineAction( new Rollback() , DeployCommandMeta.METHOD_ROLLBACK );
		super.defineAction( new Rollout() , DeployCommandMeta.METHOD_ROLLOUT );
		super.defineAction( new RunCmd() , DeployCommandMeta.METHOD_RUNCMD );
		super.defineAction( new Scp() , DeployCommandMeta.METHOD_SCP );
		super.defineAction( new List() , DeployCommandMeta.METHOD_LIST );
		super.defineAction( new SendChatMsg() , DeployCommandMeta.METHOD_SENDCHAT );
		super.defineAction( new StartEnv() , DeployCommandMeta.METHOD_STARTENV );
		super.defineAction( new StopEnv() , DeployCommandMeta.METHOD_STOPENV );
		super.defineAction( new VerifyConfigs() , DeployCommandMeta.METHOD_VERIFYCONFIGS );
		super.defineAction( new RestoreConfigs() , DeployCommandMeta.METHOD_RESTORECONFIGS );
		super.defineAction( new SaveConfigs() , DeployCommandMeta.METHOD_SAVECONFIGS );
		super.defineAction( new UpgradeEnv() , DeployCommandMeta.METHOD_UPGRADECONFIGS );
		super.defineAction( new VerifyDeploy() , DeployCommandMeta.METHOD_VERIFYDEPLOY );
		super.defineAction( new WaitEnv() , DeployCommandMeta.METHOD_WAITENV );
		super.defineAction( new WaitWeb() , DeployCommandMeta.METHOD_WAITWEB );
		super.defineAction( new Login() , DeployCommandMeta.METHOD_LOGIN );
		
		propertyBasedMethods = "confcheck configure deployredist redist restoreconfigs verifydeploy";
		impl = new DeployCommand();
	}
	
	@Override
	public boolean runExecutorImpl( ActionBase action , CommandMethod method ) {
		// log action and run 
		boolean res = super.runMethod( action , method );
		return( res );
	}

	private Dist getDist( ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Meta meta = action.getContextMeta();
		Dist dist = action.getReleaseDist( meta , RELEASELABEL );
		return( dist );
	}
	
	private ActionScope getServerScope( ActionBase action ) throws Exception {
		return( getServerScope( action , 0 ) );
	}
	
	private ActionScope getServerScope( ActionBase action , int posFrom ) throws Exception {
		Dist dist = null;
		Meta meta = action.getContextMeta();
		if( !action.context.CTX_RELEASELABEL.isEmpty() )
			dist = action.getReleaseDist( meta , action.context.CTX_RELEASELABEL );
		
		return( getServerScope( action , posFrom , dist ) );
	}

	private ActionScope getServerScope( ActionBase action , int posFrom , Dist dist ) throws Exception {
		ActionEnvScopeMaker maker = new ActionEnvScopeMaker( action , action.context.env );
		
		String SERVER = getArg( action , posFrom );
		if( action.context.sg == null ) {
			if( !SERVER.isEmpty() )
				action.exit0( _Error.MissingSegmentName0, "Segment name is required to use specific server" );
			maker.addScopeEnv( null , dist );
		}
		else {
			String s = getArg( action , posFrom + 1 );
			if( s.matches( "[0-9]+" ) ) {
				String[] NODES = getArgList( action , posFrom + 1 );
				maker.addScopeEnvServerNodes( action.context.sg , SERVER , NODES , dist );
			}
			else {
				String[] SERVERS = getArgList( action , posFrom );
				maker.addScopeEnvServers( action.context.sg , SERVERS , dist );
			}
		}

		return( maker.getScope() );
	}
	
	private class BaseOps extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
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

	private class CheckEnv extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.checkEnv( action , scope );
	}
	}

	private class ConfCheck extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.confCheck( action , scope );
	}
	}

	private class Configure extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.configure( action , scope );
	}
	}

	private class DeployRedist extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.deployRedist( action , scope , dist );
	}
	}

	private class DropRedist extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String releaseDir = getRequiredArg( action , 0 , "release" );
		ActionScope scope = getServerScope( action , 1 );
		impl.dropRedist( action , scope , releaseDir );
	}
	}

	private class GetDeployInfo extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.getDeployInfo( action , scope );
	}
	}

	private class GetRedistInfo extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.getRedistInfo( action , scope , dist );
	}
	}

	private class Hosts extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
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

	private class Key extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "COMMAND" );
		ActionScope scope = getServerScope( action , 1 );
		impl.changeKeys( action , scope , CMD );
	}
	}

	private class Login extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String NODE = getArg( action , 1 );
		impl.login( action , action.context.sg , SERVER , NODE );
	}
	}

	private class Redist extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.redist( action , scope , dist );
	}
	}

	private class RestartEnv extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.restartEnv( action , scope );
	}
	}

	private class Rollback extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.rollback( action , scope , dist );
	}
	}

	private class Rollout extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.rollout( action , scope , dist );
	}
	}

	private class RunCmd extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "COMMAND" );
		ActionScope scope = getServerScope( action , 1 );
		impl.runCmd( action , scope , CMD );
	}
	}

	private class Scp extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String SRCINFO = getRequiredArg( action , 0 , "SRCINFO" );
		String DSTPATH = getRequiredArg( action , 1 , "DSTPATH" );
		ActionScope scope = getServerScope( action , 2 );
		impl.scp( action , scope , SRCINFO , DSTPATH );
	}
	}

	private class List extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		action.context.CTX_ALL = true;
		ActionScope scope = getServerScope( action );
		impl.list( action , scope );
	}
	}

	private class SendChatMsg extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String msg = getRequiredArg( action , 0 , "MSG" );
		impl.sendMsg( action , msg );
	}
	}

	private class StartEnv extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.startEnv( action , scope );
	}
	}

	private class StopEnv extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.stopEnv( action , scope );
	}
	}

	private class VerifyConfigs extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.verifyConfigs( action , scope );
	}
	}

	private class RestoreConfigs extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.restoreConfigs( action , scope );
	}
	}

	private class SaveConfigs extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.saveConfigs( action , scope );
	}
	}

	private class UpgradeEnv extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String PATCHID = getRequiredArg( action , 0 , "PATCHID" );
		ActionScope scope = getServerScope( action , 1 );
		impl.upgradeEnv( action , PATCHID , scope );
	}
	}

	private class VerifyDeploy extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		Dist dist = getDist( action );
		ActionScope scope = getServerScope( action , 1 );
		impl.verifyDeploy( action , scope , dist );
	}
	}

	private class WaitEnv extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.waitEnv( action , scope );
	}
	}

	private class WaitWeb extends CommandMethod {
	public void run( ActionBase action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String NODE = getArg( action , 1 );
		impl.waitWeb( action , SERVER , NODE );
	}
	}
	
}
