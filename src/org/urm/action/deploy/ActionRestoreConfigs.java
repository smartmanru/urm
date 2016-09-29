package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.conf.ConfBuilder;
import org.urm.common.Common;
import org.urm.engine.meta.MetaDistrComponentItem;
import org.urm.engine.meta.MetaDistrConfItem;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.meta.MetaEnvServerDeployment;
import org.urm.engine.meta.MetaEnvServerNode;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RuntimeStorage;
import org.urm.engine.storage.SourceStorage;

public class ActionRestoreConfigs extends ActionBase {

	String timestamp;
	String version;
	
	public ActionRestoreConfigs( ActionBase action , String stream ) {
		super( action , stream );
		timestamp = Common.getNameTimeStamp();
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer; 
		if( !server.isConfigurable( this ) ) {
			debug( "ignore server=" + server.NAME + ", type=" + server.getServerTypeName( this ) );
			return( true );
		}

		info( "============================================ " + getMode() + " server=" + server.NAME + " ..." );
		info( "rootpath=" + server.ROOTPATH );

		SourceStorage sourceStorage = artefactory.getSourceStorage( this , target.meta );

		// export templates
		String PATH = "config.live";
		PATH = Common.getPath( PATH , server.NAME );
		LocalFolder folder = artefactory.getWorkFolder( this , PATH );
		folder.recreateThis( this );
		if( context.CTX_LIVE ) {
			if( context.CTX_TAG.isEmpty() )
				version = "live-" + timestamp;
			else
				version = "live-tag-" + context.CTX_TAG;
		} else {
			if( context.CTX_TAG.isEmpty() )
				version = "prod-" + timestamp;
			else
				version = "prod-tag-" + context.CTX_TAG;
			sourceStorage.exportTemplates( this , folder , server );
		}
		
		// iterate by nodes - prepare
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			debug( "prepare server=" + server.NAME + " node=" + node.POS + " ..." );
			executeNode( folder , sourceStorage , server , node , true );
		}

		// iterate by nodes - rollout
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "execute server=" + server.NAME + " node=" + node.POS + " ..." );
			executeNode( folder , sourceStorage , server , node , false );
		}

		return( true );
	}

	private void executeNode( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , boolean prepare ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		
		for( MetaEnvServerDeployment deployment : server.getDeployments( this ) ) {
			if( deployment.confItem != null ) {
				String name = sourceStorage.getConfItemLiveName( this , node , deployment.confItem );
				executeNodeConf( parent , sourceStorage , server , node , deployment , deployment.confItem , name , prepare );
				continue;
			}
			
			// deployments
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem compItem : deployment.comp.getConfItems( this ).values() ) {
				if( compItem.confItem != null ) {
					String name = sourceStorage.getConfItemLiveName( this , node , compItem.confItem );
					executeNodeConf( parent , sourceStorage , server , node , deployment , compItem.confItem , name , prepare );
				}
			}
		}
		
		// restore system component
		String name = sourceStorage.getSysConfItemLiveName( this , node );
		executeNodeSysConf( parent , sourceStorage , server , node , name , prepare );
	}

	private void executeNodeSysConf( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , String name , boolean prepare ) throws Exception {
		if( !context.CTX_LIVE ) {
			if( prepare )
				debug( "skip restoring live system configs" );
			return;
		}

		if( prepare ) {
			debug( "prepare restore system configuraton component from live ..." );
			sourceStorage.exportLiveConfigItem( this , server , name , context.CTX_TAG , parent );
		}
		else {
			info( "restore system configuraton component from live ..." );
			RuntimeStorage runtime = artefactory.getRuntimeStorage( this , server , node );
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			runtime.restoreSysConfigs( this , redist , parent.getSubFolder( this , name ) );
		}
	}

	private void executeNodeConf( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name , boolean prepare ) throws Exception {
		if( context.CTX_LIVE )
			executeNodeConfLive( parent , sourceStorage , server , node , deployment , confItem , name , prepare );
		else
			executeNodeConfTemplates( parent , sourceStorage , server , node , deployment , confItem , name , prepare );
	}

	private void executeNodeConfLive( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name , boolean prepare ) throws Exception {
		LocalFolder live = parent.getSubFolder( this , name );
		if( prepare ) {
			debug( "prepare restore configuraton item=" + confItem.KEY + " from live ..." );
			sourceStorage.exportLiveConfigItem( this , server , name , context.CTX_TAG , parent );
	
			ConfBuilder builder = new ConfBuilder( this , server.meta );
			builder.configureLiveComponent( live , confItem , server , node );
		}
		else {
			info( "restore configuraton item=" + confItem.KEY + " from live ..." );
			
			RuntimeStorage runtime = artefactory.getRuntimeStorage( this , server , node );
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			runtime.restoreConfigItem( this , redist , live , deployment , confItem , version );
		}
	}
	
	private void executeNodeConfTemplates( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name , boolean prepare ) throws Exception {
		LocalFolder live = parent.getSubFolder( this , name );
		if( prepare ) {
			debug( "prepare restore configuraton item=" + confItem.KEY + " from templates ..." );
			
			LocalFolder template = parent.getSubFolder( this , confItem.KEY );
			live.recreateThis( this );
			
			ConfBuilder builder = new ConfBuilder( this , server.meta );
			builder.configureComponent( template , live , confItem , server , node );
		}
		else {
			info( "restore configuraton item=" + confItem.KEY + " from templates ..." );
			
			RuntimeStorage runtime = artefactory.getRuntimeStorage( this , server , node );
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			runtime.restoreConfigItem( this , redist , live , deployment , confItem , version );
		}
	}
	
}
