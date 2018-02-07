package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;

public class DeployCommand {

	public DeployCommand() {
	}

	public void baseInstall( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseInstall ma = new ActionBaseInstall( action , null );
		ma.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void baseList( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseList ma = new ActionBaseList( action , null );
		ma.runEnvUniqueAccounts( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void baseClear( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseClear ma = new ActionBaseClear( action , null );
		ma.runEnvUniqueAccounts( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void checkEnv( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionCheckEnv ma = new ActionCheckEnv( action , null );
		ma.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void confCheck( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionConfCheck ma = new ActionConfCheck( action , null );
		ma.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void configure( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		LocalFolder folder = action.artefactory.getArtefactFolder( action , action.shell.account.osType , scope.meta , "configuration" );
		ActionConfigure ma = new ActionConfigure( action , null , folder );
		ma.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void deployRedist( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionDeployRedist ca = new ActionDeployRedist( action , null , dist );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void dropRedist( ScopeState parentState , ActionBase action , ActionScope scope , String releaseDir ) throws Exception {
		ActionDropRedist ca = new ActionDropRedist( action , null , releaseDir );
		
		if( action.isForced() )
			ca.runEnvUniqueHosts( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
		else
			ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void getDeployInfo( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionGetDeployInfo ca = new ActionGetDeployInfo( action , null );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void getRedistInfo( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionGetRedistInfo ca = new ActionGetRedistInfo( action , null , dist );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void changeHosts( ScopeState parentState , ActionBase action , ActionScope scope , String CMD , String host , String address ) throws Exception {
		ActionChangeHosts ca = new ActionChangeHosts( action , null , CMD , host , address );
		if( !Common.checkPartOfSpacedList( CMD , "set delete check" ) )
			ca.exit1( _Error.InvalidHostsCommand1 , "invalid command=" + CMD , CMD );
			
		ca.runEnvUniqueHosts( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void changeKeys( ScopeState parentState , ActionBase action , ActionScope scope , String CMD ) throws Exception {
		ActionChangeKeys ca = new ActionChangeKeys( action , null , CMD );
		if( !Common.checkPartOfSpacedList( CMD , "list change add set delete" ) )
			ca.exit1( _Error.InvalidKeysCommand1 , "invalid command=" + CMD , CMD );
			
		ca.runEnvUniqueAccounts( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void login( ScopeState parentState , ActionBase action , MetaEnvSegment sg , String SERVER , String NODE ) throws Exception {
		if( sg == null )
			action.exit0( _Error.UnknownSegment0 , "Unknown segment, missing specifier" );
		MetaEnvServer server = sg.getServer( SERVER );
		
		int nodePos = ( NODE.isEmpty() )? 1 : Integer.parseInt( NODE );
		MetaEnvServerNode node = server.getNodeByPos( nodePos );
		ActionLogin ca = new ActionLogin( action , null , node );
		ca.runSimpleEnv( parentState , sg.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void redist( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		if( scope.isEmpty() ) {
			action.info( "nothing to redist" );
			return;
		}
		
		// open distributive
		dist.openForUse( action );
		
		// download configuration templates
		LocalFolder folder = null;
		LocalFolder live = null;
		if( action.context.CTX_CONFDEPLOY && !dist.release.isEmptyConfiguration() ) {
			ActionConfCheck check = new ActionConfCheck( action , null );
			if( !check.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
				action.exit0( _Error.InvalidEnvironmentData0 , "configuration check failed: invalid environment data" );
			
			action.info( "prepare configuration ..." );
			folder = action.artefactory.getWorkFolder( action , "configuration" );
			ActionConfigure ca = new ActionConfigure( action , null , dist , folder ); 
			if( !ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
				action.exit0( _Error.UnablePrepareConfiguration0 , "unable to prepare configuration" );
			
			live = ca.getLiveFolder();
		}
		
		action.info( "open sessions and create redist folders ..." );
		ActionPrepareRedist pa = new ActionPrepareRedist( action , null , dist , true );
		if( !pa.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
			action.ifexit( _Error.UnableCreateFolders0 , "unable to create folders" , null );
		
		action.info( "upload to redist ..." );
		ActionRedist ma = new ActionRedist( action , null , dist , live );
		ma.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
		
		if( ma.isFailed() )
			action.error( "redist failed, see logs." );
		else
			action.info( "redist successfully done." );
	}

	public void restartEnv( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		sendMsg( parentState , action , "[restartenv] restarting " + scope.getScopeInfo( action ) + " ..." );

		ActionStopEnv stop = new ActionStopEnv( action , null );
		stop.context.CTX_NOCHATMSG = true;
		
		if( !stop.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
			action.ifexit( _Error.StopenvFailed0 , "restartEnv: stopenv failed" , null );
		
		ActionStartEnv start = new ActionStartEnv( action , null );
		start.context.CTX_NOCHATMSG = true;
		
		if( !start.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
			action.ifexit( _Error.StartenvFailed0 , "restartEnv: startenv failed" , null );
		
		sendMsg( parentState , action , "[restartenv] done." );
	}

	public void rollback( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionRollback ca = new ActionRollback( action , null , dist );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void rollout( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionRollout ca = new ActionRollout( action , null , dist );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void runCmd( ScopeState parentState , ActionBase action , ActionScope scope , String CMD ) throws Exception {
		ActionRunCmd ca = new ActionRunCmd( action , null , CMD );
		ca.runEnvUniqueAccounts( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void scp( ScopeState parentState , ActionBase action , ActionScope scope , String srcInfo , String dstPath ) throws Exception {
		ActionScp ca = new ActionScp( action , null , srcInfo , dstPath );
		ca.runEnvUniqueAccounts( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void list( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionList ca = new ActionList( action , null );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void sendMsg( ScopeState parentState , ActionBase action , String msg ) throws Exception {
		ActionSendChatMsg.sendMsg( parentState , action , msg , action.context.env , action.context.sg );
	}

	public void startEnv( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionStartEnv ca = new ActionStartEnv( action , null );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void stopEnv( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionStopEnv ca = new ActionStopEnv( action , null );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void verifyConfigs( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionVerifyConfigs ca = new ActionVerifyConfigs( action , null );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void restoreConfigs( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionRestoreConfigs ca = new ActionRestoreConfigs( action , null );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void saveConfigs( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
		ActionSaveConfigs ca = new ActionSaveConfigs( action , null );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void verifyDeploy( ScopeState parentState , ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		dist.openForUse( action );
		ActionVerifyDeploy ca = new ActionVerifyDeploy( action , null , dist );
		ca.runAll( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void waitEnv( ScopeState parentState , ActionBase action , ActionScope scope ) throws Exception {
	}

	public void waitWeb( ScopeState parentState , ActionBase action , String SERVER , String NODE ) throws Exception {
	}

}
