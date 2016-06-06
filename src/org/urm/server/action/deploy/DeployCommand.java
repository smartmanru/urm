package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScope;
import org.urm.server.dist.Dist;
import org.urm.server.meta.MetaEnvDC;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.storage.LocalFolder;

public class DeployCommand {

	public DeployCommand() {
	}

	public void baseInstall( ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseInstall ma = new ActionBaseInstall( action , null );
		ma.runAll( scope );
	}

	public void baseList( ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseList ma = new ActionBaseList( action , null );
		ma.runEnvUniqueAccounts( scope );
	}

	public void baseClear( ActionBase action , ActionScope scope ) throws Exception {
		ActionBaseClear ma = new ActionBaseClear( action , null );
		ma.runEnvUniqueAccounts( scope );
	}

	public void checkEnv( ActionBase action , ActionScope scope ) throws Exception {
		ActionCheckEnv ma = new ActionCheckEnv( action , null );
		ma.runAll( scope );
	}

	public void confCheck( ActionBase action , ActionScope scope ) throws Exception {
		ActionConfCheck ma = new ActionConfCheck( action , null );
		ma.runAll( scope );
	}

	public void configure( ActionBase action , ActionScope scope ) throws Exception {
		LocalFolder folder = action.artefactory.getArtefactFolder( action , "configuration" );
		ActionConfigure ma = new ActionConfigure( action , null , folder );
		ma.runAll( scope );
	}

	public void deployRedist( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionDeployRedist ca = new ActionDeployRedist( action , null , dist );
		ca.runAll( scope );
	}

	public void dropRedist( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionDropRedist ca = new ActionDropRedist( action , null , dist );
		
		if( action.context.CTX_FORCE )
			ca.runEnvUniqueHosts( scope );
		else
			ca.runAll( scope );
	}

	public void getDeployInfo( ActionBase action , ActionScope scope ) throws Exception {
		ActionGetDeployInfo ca = new ActionGetDeployInfo( action , null );
		ca.runAll( scope );
	}

	public void getRedistInfo( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionGetRedistInfo ca = new ActionGetRedistInfo( action , null , dist );
		ca.runAll( scope );
	}

	public void changeHosts( ActionBase action , ActionScope scope , String CMD , String host , String address ) throws Exception {
		ActionChangeHosts ca = new ActionChangeHosts( action , null , CMD , host , address );
		if( !Common.checkPartOfSpacedList( CMD , "set delete check" ) )
			ca.exit( "invalid command=" + CMD );
			
		ca.runEnvUniqueHosts( scope );
	}

	public void changeKeys( ActionBase action , ActionScope scope , String CMD ) throws Exception {
		ActionChangeKeys ca = new ActionChangeKeys( action , null , CMD );
		if( !Common.checkPartOfSpacedList( CMD , "list change add set delete" ) )
			ca.exit( "invalid command=" + CMD );
			
		ca.runEnvUniqueAccounts( scope );
	}

	public void login( ActionBase action , MetaEnvDC dc , String SERVER , String NODE ) throws Exception {
		MetaEnvServer server = dc.getServer( action , SERVER );
		
		int nodePos = ( NODE.isEmpty() )? 1 : Integer.parseInt( NODE );
		MetaEnvServerNode node = server.getNode( action , nodePos );
		ActionLogin ca = new ActionLogin( action , null , node );
		ca.runSimple();
	}

	public void redist( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		if( scope.isEmpty( action ) ) {
			action.info( "nothing to redist" );
			return;
		}
		
		// open distributive
		dist.open( action );
		
		// download configuration templates
		LocalFolder folder = null;
		LocalFolder live = null;
		if( action.context.CTX_CONFDEPLOY && !dist.release.isEmptyConfiguration( action ) ) {
			ActionConfCheck check = new ActionConfCheck( action , null );
			if( !check.runAll( scope ) )
				action.exit( "configuration check failed: invalid environment data" );
			
			action.info( "prepare configuration ..." );
			folder = action.artefactory.getWorkFolder( action , "configuration" );
			ActionConfigure ca = new ActionConfigure( action , null , dist , folder ); 
			if( !ca.runAll( scope ) )
				action.exit( "unable to prepare configuration" );
			
			live = ca.getLiveFolder();
		}
		
		action.info( "open sessions and create redist folders ..." );
		ActionPrepareRedist pa = new ActionPrepareRedist( action , null , dist , true );
		if( !pa.runAll( scope ) )
			action.ifexit( "unable to create folders" );
		
		action.info( "upload to redist ..." );
		ActionRedist ma = new ActionRedist( action , null , dist , live );
		ma.runAll( scope );
		
		if( action.executor.isFailed() )
			action.info( "redist successfully done." );
		else
			action.error( "redist failed, see logs." );
	}

	public void restartEnv( ActionBase action , ActionScope scope ) throws Exception {
		sendMsg( action , "[restartenv] restarting " + scope.getScopeInfo( action ) + " ..." );

		ActionStopEnv stop = new ActionStopEnv( action , null );
		stop.context.CTX_NOCHATMSG = true;
		
		if( !stop.runAll( scope ) )
			action.ifexit( "restartEnv: stopenv failed" );
		
		ActionStartEnv start = new ActionStartEnv( action , null );
		start.context.CTX_NOCHATMSG = true;
		
		if( !start.runAll( scope ) )
			action.ifexit( "restartEnv: startenv failed" );
		
		sendMsg( action , "[restartenv] done." );
	}

	public void rollback( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionRollback ca = new ActionRollback( action , null , dist );
		ca.runAll( scope );
	}

	public void rollout( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		ActionRollout ca = new ActionRollout( action , null , dist );
		ca.runAll( scope );
	}

	public void runCmd( ActionBase action , ActionScope scope , String CMD ) throws Exception {
		ActionRunCmd ca = new ActionRunCmd( action , null , CMD );
		ca.runEnvUniqueAccounts( scope );
	}

	public void scp( ActionBase action , ActionScope scope , String srcInfo , String dstPath ) throws Exception {
		ActionScp ca = new ActionScp( action , null , srcInfo , dstPath );
		ca.runEnvUniqueAccounts( scope );
	}

	public void list( ActionBase action , ActionScope scope ) throws Exception {
		ActionList ca = new ActionList( action , null );
		ca.runAll( scope );
	}

	public void sendMsg( ActionBase action , String msg ) throws Exception {
		ActionSendChatMsg.sendMsg( action , msg , null );
	}

	public void startEnv( ActionBase action , ActionScope scope ) throws Exception {
		ActionStartEnv ca = new ActionStartEnv( action , null );
		ca.runAll( scope );
	}

	public void stopEnv( ActionBase action , ActionScope scope ) throws Exception {
		ActionStopEnv ca = new ActionStopEnv( action , null );
		ca.runAll( scope );
	}

	public void restoreConfigs( ActionBase action , ActionScope scope ) throws Exception {
		ActionRestoreConfigs ca = new ActionRestoreConfigs( action , null );
		ca.runAll( scope );
	}

	public void saveConfigs( ActionBase action , ActionScope scope ) throws Exception {
		ActionSaveConfigs ca = new ActionSaveConfigs( action , null );
		ca.runAll( scope );
	}

	public void upgradeEnv( ActionBase action , String PATCHID , ActionScope scope ) throws Exception {
		ActionUpgradeEnv ca = new ActionUpgradeEnv( action , null , PATCHID );
		ca.runEnvUniqueAccounts( scope );
	}

	public void verifyDeploy( ActionBase action , ActionScope scope , Dist dist ) throws Exception {
		dist.open( action );
		ActionVerifyDeploy ca = new ActionVerifyDeploy( action , null , dist );
		ca.runAll( scope );
	}

	public void waitEnv( ActionBase action , ActionScope scope ) throws Exception {
	}

	public void waitWeb( ActionBase action , String SERVER , String NODE ) throws Exception {
	}

}
