package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class DeployCommand {

	public DeployCommand() {
	}

	public void baseInstall( ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseInstall ma = new ActionBaseInstall( action , null );
		ma.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void baseList( ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseList ma = new ActionBaseList( action , null );
		ma.runEnvUniqueAccounts( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void baseClear( ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseClear ma = new ActionBaseClear( action , null );
		ma.runEnvUniqueAccounts( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void checkEnv( ActionBase action , ActionScope scope ) throws Exception {
		ActionCheckEnv ma = new ActionCheckEnv( action , null );
		ma.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void confCheck( ActionBase action , ActionScope scope ) throws Exception {
		ActionConfCheck ma = new ActionConfCheck( action , null );
		ma.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void configure( ActionBase action , ActionScope scope ) throws Exception {
		LocalFolder folder = action.artefactory.getArtefactFolder( action , action.shell.account.osType , scope.meta , "configuration" );
		ActionConfigure ma = new ActionConfigure( action , null , folder );
		ma.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void deployRedist( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionDeployRedist ca = new ActionDeployRedist( action , null , dist );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void dropRedist( ActionBase action , ActionScope scope , String releaseDir ) throws Exception {
		ActionDropRedist ca = new ActionDropRedist( action , null , releaseDir );
		
		if( action.isForced() )
			ca.runEnvUniqueHosts( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
		else
			ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void getDeployInfo( ActionBase action , ActionScope scope ) throws Exception {
		ActionGetDeployInfo ca = new ActionGetDeployInfo( action , null );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void getRedistInfo( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionGetRedistInfo ca = new ActionGetRedistInfo( action , null , dist );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void changeHosts( ActionBase action , ActionScope scope , String CMD , String host , String address ) throws Exception {
		ActionChangeHosts ca = new ActionChangeHosts( action , null , CMD , host , address );
		if( !Common.checkPartOfSpacedList( CMD , "set delete check" ) )
			ca.exit1( _Error.InvalidHostsCommand1 , "invalid command=" + CMD , CMD );
			
		ca.runEnvUniqueHosts( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void changeKeys( ActionBase action , ActionScope scope , String CMD ) throws Exception {
		ActionChangeKeys ca = new ActionChangeKeys( action , null , CMD );
		if( !Common.checkPartOfSpacedList( CMD , "list change add set delete" ) )
			ca.exit1( _Error.InvalidKeysCommand1 , "invalid command=" + CMD , CMD );
			
		ca.runEnvUniqueAccounts( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void login( ActionBase action , MetaEnvSegment sg , String SERVER , String NODE ) throws Exception {
		if( sg == null )
			action.exit0( _Error.UnknownSegment0 , "Unknown segment, missing specifier" );
		MetaEnvServer server = sg.getServer( action , SERVER );
		
		int nodePos = ( NODE.isEmpty() )? 1 : Integer.parseInt( NODE );
		MetaEnvServerNode node = server.getNode( action , nodePos );
		ActionLogin ca = new ActionLogin( action , null , node );
		ca.runSimpleEnv( sg.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void redist( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
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
			if( !check.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
				action.exit0( _Error.InvalidEnvironmentData0 , "configuration check failed: invalid environment data" );
			
			action.info( "prepare configuration ..." );
			folder = action.artefactory.getWorkFolder( action , "configuration" );
			ActionConfigure ca = new ActionConfigure( action , null , dist , folder ); 
			if( !ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
				action.exit0( _Error.UnablePrepareConfiguration0 , "unable to prepare configuration" );
			
			live = ca.getLiveFolder();
		}
		
		action.info( "open sessions and create redist folders ..." );
		ActionPrepareRedist pa = new ActionPrepareRedist( action , null , dist , true );
		if( !pa.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
			action.ifexit( _Error.UnableCreateFolders0 , "unable to create folders" , null );
		
		action.info( "upload to redist ..." );
		ActionRedist ma = new ActionRedist( action , null , dist , live );
		ma.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
		
		if( ma.isFailed() )
			action.error( "redist failed, see logs." );
		else
			action.info( "redist successfully done." );
	}

	public void restartEnv( ActionBase action , ActionScope scope ) throws Exception {
		sendMsg( action , "[restartenv] restarting " + scope.getScopeInfo( action ) + " ..." );

		ActionStopEnv stop = new ActionStopEnv( action , null );
		stop.context.CTX_NOCHATMSG = true;
		
		if( !stop.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
			action.ifexit( _Error.StopenvFailed0 , "restartEnv: stopenv failed" , null );
		
		ActionStartEnv start = new ActionStartEnv( action , null );
		start.context.CTX_NOCHATMSG = true;
		
		if( !start.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
			action.ifexit( _Error.StartenvFailed0 , "restartEnv: startenv failed" , null );
		
		sendMsg( action , "[restartenv] done." );
	}

	public void rollback( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionRollback ca = new ActionRollback( action , null , dist );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void rollout( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionRollout ca = new ActionRollout( action , null , dist );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void runCmd( ActionBase action , ActionScope scope , String CMD ) throws Exception {
		ActionRunCmd ca = new ActionRunCmd( action , null , CMD );
		ca.runEnvUniqueAccounts( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void scp( ActionBase action , ActionScope scope , String srcInfo , String dstPath ) throws Exception {
		ActionScp ca = new ActionScp( action , null , srcInfo , dstPath );
		ca.runEnvUniqueAccounts( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void list( ActionBase action , ActionScope scope ) throws Exception {
		ActionList ca = new ActionList( action , null );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void sendMsg( ActionBase action , String msg ) throws Exception {
		ActionSendChatMsg.sendMsg( action , msg , action.context.env , action.context.sg );
	}

	public void startEnv( ActionBase action , ActionScope scope ) throws Exception {
		ActionStartEnv ca = new ActionStartEnv( action , null );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void stopEnv( ActionBase action , ActionScope scope ) throws Exception {
		ActionStopEnv ca = new ActionStopEnv( action , null );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void verifyConfigs( ActionBase action , ActionScope scope ) throws Exception {
		ActionVerifyConfigs ca = new ActionVerifyConfigs( action , null );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void restoreConfigs( ActionBase action , ActionScope scope ) throws Exception {
		ActionRestoreConfigs ca = new ActionRestoreConfigs( action , null );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void saveConfigs( ActionBase action , ActionScope scope ) throws Exception {
		ActionSaveConfigs ca = new ActionSaveConfigs( action , null );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void upgradeEnv( ActionBase action , String PATCHID , ActionScope scope ) throws Exception {
		ActionUpgradeEnv ca = new ActionUpgradeEnv( action , null , PATCHID );
		ca.runEnvUniqueAccounts( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void verifyDeploy( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		dist.openForUse( action );
		ActionVerifyDeploy ca = new ActionVerifyDeploy( action , null , dist );
		ca.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false );
	}

	public void waitEnv( ActionBase action , ActionScope scope ) throws Exception {
	}

	public void waitWeb( ActionBase action , String SERVER , String NODE ) throws Exception {
	}

}
