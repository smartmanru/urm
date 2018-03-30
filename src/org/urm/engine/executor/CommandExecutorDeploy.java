package org.urm.engine.executor;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.deploy.DeployCommand;
import org.urm.common.Common;
import org.urm.common.action.CommandMeta;
import org.urm.common.meta.DeployCommandMeta;
import org.urm.engine.Engine;
import org.urm.engine.action.CommandMethod;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;

public class CommandExecutorDeploy extends CommandExecutor {

	DeployCommand impl;
	MetaEnv env;
	MetaEnvSegment sg;
	
	String propertyBasedMethods;
	
	public static CommandExecutorDeploy createExecutor( Engine engine ) throws Exception {
		DeployCommandMeta commandInfo = new DeployCommandMeta( engine.optionsMeta );
		return( new CommandExecutorDeploy( engine , commandInfo ) );
	}
		
	private CommandExecutorDeploy( Engine engine , CommandMeta commandInfo ) throws Exception {
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
		super.defineAction( new VerifyDeploy() , DeployCommandMeta.METHOD_VERIFYDEPLOY );
		super.defineAction( new WaitEnv() , DeployCommandMeta.METHOD_WAITENV );
		super.defineAction( new WaitWeb() , DeployCommandMeta.METHOD_WAITWEB );
		super.defineAction( new Login() , DeployCommandMeta.METHOD_LOGIN );
		
		propertyBasedMethods = "confcheck configure deployredist redist restoreconfigs verifydeploy";
		impl = new DeployCommand();
	}
	
	@Override
	public boolean runExecutorImpl( ScopeState parentState , ActionBase action , CommandMethod method ) {
		// log action and run 
		boolean res = super.runMethod( parentState , action , method );
		return( res );
	}

	private class BaseOps extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "CMD" );
		ActionScope scope = getServerScope( action , 1 );
		if( CMD.equals( DeployCommandMeta.BASEOPS_INSTALL ) )
			impl.baseInstall( parentState , action , scope );
		else
		if( CMD.equals( DeployCommandMeta.BASEOPS_LIST ) )
			impl.baseList( parentState , action , scope );
		else
		if( CMD.equals( DeployCommandMeta.BASEOPS_CLEAR ) )
			impl.baseClear( parentState , action , scope );
		else
			action.exitUnexpectedState();
	}
	}

	private class CheckEnv extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.checkEnv( parentState , action , scope );
	}
	}

	private class ConfCheck extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.confCheck( parentState , action , scope );
	}
	}

	private class Configure extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.configure( parentState , action , scope );
	}
	}

	private class DeployRedist extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = super.getDist( action , RELEASELABEL );
		ActionScope scope = getServerScope( action , 1 );
		impl.deployRedist( parentState , action , scope , dist );
	}
	}

	private class DropRedist extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String releaseDir = getRequiredArg( action , 0 , "release" );
		ActionScope scope = getServerScope( action , 1 );
		impl.dropRedist( parentState , action , scope , releaseDir );
	}
	}

	private class GetDeployInfo extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.getDeployInfo( parentState , action , scope );
	}
	}

	private class GetRedistInfo extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = super.getDist( action , RELEASELABEL );
		ActionScope scope = getServerScope( action , 1 );
		impl.getRedistInfo( parentState , action , scope , dist );
	}
	}

	private class Hosts extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
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
		impl.changeHosts( parentState , action , scope , CMD , F_HOST_NAME , F_HOST_IP );
	}
	}

	private class Key extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "COMMAND" );
		ActionScope scope = getServerScope( action , 1 );
		impl.changeKeys( parentState , action , scope , CMD );
	}
	}

	private class Login extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String NODE = getArg( action , 1 );
		impl.login( parentState , action , action.context.sg , SERVER , NODE );
	}
	}

	private class Redist extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = super.getDist( action , RELEASELABEL );
		ActionScope scope = getServerScope( action , 1 );
		impl.redist( parentState , action , scope , dist );
	}
	}

	private class RestartEnv extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.restartEnv( parentState , action , scope );
	}
	}

	private class Rollback extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = super.getDist( action , RELEASELABEL );
		ActionScope scope = getServerScope( action , 1 );
		impl.rollback( parentState , action , scope , dist );
	}
	}

	private class Rollout extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = super.getDist( action , RELEASELABEL );
		ActionScope scope = getServerScope( action , 1 );
		impl.rollout( parentState , action , scope , dist );
	}
	}

	private class RunCmd extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String CMD = getRequiredArg( action , 0 , "COMMAND" );
		ActionScope scope = getServerScope( action , 1 );
		impl.runCmd( parentState , action , scope , CMD );
	}
	}

	private class Scp extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String SRCINFO = getRequiredArg( action , 0 , "SRCINFO" );
		String DSTPATH = getRequiredArg( action , 1 , "DSTPATH" );
		ActionScope scope = getServerScope( action , 2 );
		impl.scp( parentState , action , scope , SRCINFO , DSTPATH );
	}
	}

	private class List extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		action.context.CTX_ALL = true;
		ActionScope scope = getServerScope( action );
		impl.list( parentState , action , scope );
	}
	}

	private class SendChatMsg extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String msg = getRequiredArg( action , 0 , "MSG" );
		impl.sendMsg( parentState , action , msg );
	}
	}

	private class StartEnv extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.startEnv( parentState , action , scope );
	}
	}

	private class StopEnv extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.stopEnv( parentState , action , scope );
	}
	}

	private class VerifyConfigs extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.verifyConfigs( parentState , action , scope );
	}
	}

	private class RestoreConfigs extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.restoreConfigs( parentState , action , scope );
	}
	}

	private class SaveConfigs extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.saveConfigs( parentState , action , scope );
	}
	}

	private class VerifyDeploy extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String RELEASELABEL = getRequiredArg( action , 0 , "RELEASELABEL" );
		Dist dist = super.getDist( action , RELEASELABEL );
		ActionScope scope = getServerScope( action , 1 );
		impl.verifyDeploy( parentState , action , scope , dist );
	}
	}

	private class WaitEnv extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		ActionScope scope = getServerScope( action );
		impl.waitEnv( parentState , action , scope );
	}
	}

	private class WaitWeb extends CommandMethod {
	public void run( ScopeState parentState , ActionBase action ) throws Exception {
		String SERVER = getRequiredArg( action , 0 , "SERVER" );
		String NODE = getArg( action , 1 );
		impl.waitWeb( parentState , action , SERVER , NODE );
	}
	}
	
}
