package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.conf.ConfBuilder;
import ru.egov.urm.meta.MetaDistrComponentItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerDeployment;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RuntimeStorage;
import ru.egov.urm.storage.SourceStorage;

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
			debug( "ignore server=" + server.NAME + ", type=" + Common.getEnumLower( server.TYPE ) );
			return( true );
		}

		log( "============================================ " + getMode() + " server=" + server.NAME + " ..." );
		log( "rootpath=" + server.ROOTPATH );

		SourceStorage sourceStorage = artefactory.getSourceStorage( this );

		// export templates
		String PATH = "config.live";
		PATH = Common.getPath( PATH , server.NAME );
		LocalFolder folder = artefactory.getWorkFolder( this , PATH );
		folder.recreateThis( this );
		if( options.OPT_LIVE ) {
			if( options.OPT_TAG.isEmpty() )
				version = "live-" + timestamp;
			else
				version = "live-tag-" + options.OPT_TAG;
		} else {
			if( options.OPT_TAG.isEmpty() )
				version = "prod-" + timestamp;
			else
				version = "prod-tag-" + options.OPT_TAG;
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
			log( "execute server=" + server.NAME + " node=" + node.POS + " ..." );
			executeNode( folder , sourceStorage , server , node , false );
		}

		return( true );
	}

	private void executeNode( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , boolean prepare ) throws Exception {
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
					String name = sourceStorage.getCompConfItemLiveName( this , node , deployment.comp , compItem.confItem );
					executeNodeConf( parent , sourceStorage , server , node , deployment , compItem.confItem , name , prepare );
				}
			}
		}
		
		// restore system component
		String name = sourceStorage.getSysConfItemLiveName( this , node );
		executeNodeSysConf( parent , sourceStorage , server , node , name , prepare );
	}

	private void executeNodeSysConf( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , String name , boolean prepare ) throws Exception {
		if( !options.OPT_LIVE ) {
			if( prepare )
				debug( "skip restoring live system configs" );
			return;
		}

		if( prepare ) {
			debug( "prepare restore system configuraton component from live ..." );
			sourceStorage.exportLiveConfigItem( this , server , name , options.OPT_TAG , parent );
		}
		else {
			log( "restore system configuraton component from live ..." );
			RuntimeStorage runtime = artefactory.getRuntimeStorage( this , server , node );
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			runtime.restoreSysConfigs( this , redist , parent.getSubFolder( this , name ) );
		}
	}

	private void executeNodeConf( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name , boolean prepare ) throws Exception {
		if( options.OPT_LIVE )
			executeNodeConfLive( parent , sourceStorage , server , node , deployment , confItem , name , prepare );
		else
			executeNodeConfTemplates( parent , sourceStorage , server , node , deployment , confItem , name , prepare );
	}

	private void executeNodeConfLive( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name , boolean prepare ) throws Exception {
		LocalFolder live = parent.getSubFolder( this , name );
		if( prepare ) {
			debug( "prepare restore configuraton item=" + confItem.KEY + " from live ..." );
			sourceStorage.exportLiveConfigItem( this , server , name , options.OPT_TAG , parent );
	
			ConfBuilder builder = new ConfBuilder( this );
			builder.configureLiveComponent( live , confItem , server , node );
		}
		else {
			log( "restore configuraton item=" + confItem.KEY + " from live ..." );
			
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
			
			ConfBuilder builder = new ConfBuilder( this );
			builder.configureComponent( template , live , confItem , server , node );
		}
		else {
			log( "restore configuraton item=" + confItem.KEY + " from templates ..." );
			
			RuntimeStorage runtime = artefactory.getRuntimeStorage( this , server , node );
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			runtime.restoreConfigItem( this , redist , live , deployment , confItem , version );
		}
	}
	
}
