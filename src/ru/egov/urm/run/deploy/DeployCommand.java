package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;

public class DeployCommand {

	public DeployCommand() {
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

	public void deployRedist( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
		ActionDeployRedist ca = new ActionDeployRedist( action , null , scope.release );
		ca.runAll( scope );
	}

	public void dropRedist( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
		ActionDropRedist ca = new ActionDropRedist( action , null , scope.release );
		
		if( action.context.CTX_FORCE )
			ca.runEnvUniqueHosts( scope );
		else
			ca.runAll( scope );
	}

	public void getDeployInfo( ActionBase action , ActionScope scope ) throws Exception {
		ActionGetDeployInfo ca = new ActionGetDeployInfo( action , null );
		ca.runAll( scope );
	}

	public void getRedistInfo( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
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

	public void login( ActionBase action , String SERVER , String NODE ) throws Exception {
		MetaEnvServer server = action.meta.dc.getServer( action , SERVER );
		
		int nodePos = ( NODE.isEmpty() )? 1 : Integer.parseInt( NODE );
		MetaEnvServerNode node = server.getNode( action , nodePos );
		ActionLogin ca = new ActionLogin( action , null , node );
		ca.runSimple();
	}

	public void redist( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
		if( scope.isEmpty( action ) ) {
			action.log( "nothing to redist" );
			return;
		}
		
		// open distributive
		dist.open( action );
		
		// download configuration templates
		LocalFolder folder = null;
		if( action.context.CTX_CONFDEPLOY && !dist.info.isEmptyConfiguration( action ) ) {
			action.log( "prepare configuration ..." );
			folder = action.artefactory.getWorkFolder( action , "configuration" );
			ActionConfigure ca = new ActionConfigure( action , null , dist , folder ); 
			if( !ca.runAll( scope ) )
				action.exit( "unable to prepare configuration" );
		}
		
		action.log( "open sessions and create redist folders ..." );
		ActionPrepareRedist pa = new ActionPrepareRedist( action , null , dist , true );
		if( !pa.runAll( scope ) )
			action.exit( "unable to create folders" );
		
		action.log( "upload to redist ..." );
		ActionRedist ma = new ActionRedist( action , null , dist , folder );
		ma.runAll( scope );
	}

	public void restartEnv( ActionBase action , ActionScope scope ) throws Exception {
		sendMsg( action , "[restartenv] restarting " + scope.getScopeInfo( action ) + " ..." );

		ActionStopEnv stop = new ActionStopEnv( action , null );
		stop.context.CTX_NOCHATMSG = true;
		
		if( !stop.runAll( scope ) ) {
			if( !action.context.CTX_FORCE )
				action.exit( "restartEnv: stopenv failed, not trying to start" );
		}
		
		ActionStartEnv start = new ActionStartEnv( action , null );
		start.context.CTX_NOCHATMSG = true;
		
		if( !start.runAll( scope ) ) {
			if( !action.context.CTX_FORCE )
				action.exit( "restartEnv: startenv failed" );
		}
		
		sendMsg( action , "[restartenv] done." );
	}

	public void rollback( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
		ActionRollback ca = new ActionRollback( action , null , dist );
		ca.runAll( scope );
	}

	public void rollout( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
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
		ActionSendChatMsg.sendMsg( action , msg , false );
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

	public void verifyDeploy( ActionBase action , ActionScope scope , DistStorage dist ) throws Exception {
		dist.open( action );
		ActionVerifyDeploy ca = new ActionVerifyDeploy( action , null , dist );
		ca.runAll( scope );
	}

	public void waitEnv( ActionBase action , ActionScope scope ) throws Exception {
	}

	public void waitWeb( ActionBase action , String SERVER , String NODE ) throws Exception {
	}

}
