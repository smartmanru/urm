package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScope;
import ru.egov.urm.action.ActionScopeSet;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.meta.MetaDistrComponentItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerDeployment;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.SourceStorage;

public class ActionSaveConfigs extends ActionBase {

	public ActionSaveConfigs( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		if( scope.scopeFull && context.CTX_FORCE )
			deleteOldConfServers( scope );
		
		// check need to tag configuration
		if( !context.CTX_TAG.isEmpty() )
			sourceStorage.tagLiveConfigs( this , context.CTX_TAG , "ActionSaveConfigs" );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer; 
		if( !server.isConfigurable( this ) ) {
			debug( "ignore server=" + server.NAME + ", type=" + server.SERVERTYPE );
			return( true );
		}

		log( "============================================ execute server=" + server.NAME + " ..." );

		// iterate by nodes
		String F_REDIST_SAVEITEMS = "";
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			log( "execute server=" + server.NAME + " node=" + node.POS + " ..." );
			
			String confList = executeNode( server , node );
			F_REDIST_SAVEITEMS = Common.addItemToUniqueSpacedList( F_REDIST_SAVEITEMS , confList );
		}

		// delete old
		if( context.CTX_FORCE && target.itemFull )
			deleteOldConfItems( server , F_REDIST_SAVEITEMS );
		
		return( true );
	}

	private String executeNode( MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		String vcsCompList = "";
		
		for( MetaEnvServerDeployment deployment : server.getDeployments( this ) ) {
			if( deployment.confItem != null ) {
				String name = sourceStorage.getConfItemLiveName( this , node , deployment.confItem );
				executeNodeConf( sourceStorage , server , node , deployment , deployment.confItem , name );
				vcsCompList = Common.addItemToUniqueSpacedList( vcsCompList , name );
				continue;
			}
			
			// deployments
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem compItem : deployment.comp.getConfItems( this ).values() ) {
				if( compItem.confItem != null ) {
					String name = sourceStorage.getConfItemLiveName( this , node , compItem.confItem );
					executeNodeConf( sourceStorage , server , node , deployment , compItem.confItem , name );
					vcsCompList = Common.addItemToUniqueSpacedList( vcsCompList , name );
				}
			}
		}
		
		// add system component
		String name = sourceStorage.getSysConfItemLiveName( this , node );
		executeNodeSysConf( sourceStorage , server , node , name );
		vcsCompList = Common.addItemToUniqueSpacedList( vcsCompList , name );
		return( vcsCompList );
	}
	
	private void deleteOldConfItems( MetaEnvServer server , String saveItems ) throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		String existingItems = sourceStorage.getLiveConfigItems( this , server );

		for( String item : Common.splitSpaced( existingItems ) ) {
			if( Common.checkPartOfSpacedList( item , saveItems ) )
				continue;
			
			log( "delete obsolete configuration item=" + item + " ..." );
			sourceStorage.deleteLiveConfigItem( this , server , item , "ActionSaveConfigs" );
		}
	}

	private void deleteOldConfServers( ActionScope scope ) throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		
		for( ActionScopeSet set : scope.getEnvSets( this ) ) {
			String existingItems = sourceStorage.getLiveConfigServers( this , set.dc );

			for( String item : Common.splitSpaced( existingItems ) ) {
				if( set.dc.findServer( this , item ) != null )
					continue;
				
				log( "delete obsolete server=" + item + " ..." );
				sourceStorage.deleteLiveConfigServer( this , set.dc , item , "ActionSaveConfigs" );
			}
		}
	}

	private void executeNodeSysConf( SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , String name ) throws Exception {
		log( "extract system configuraton component ..." );

		LocalFolder folder = artefactory.getWorkFolder( this , "config.live" );
		folder.recreateThis( this );

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		if( !redist.getSysConfigs( this , folder ) ) {
			if( !context.CTX_FORCE )
				exit( "unable to get system files configuration" );
			return;
		}
		
		sourceStorage.saveLiveConfigItem( this , server , node , name , folder , "ActionSaveConfigs" );
	}
	
	private void executeNodeConf( SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name ) throws Exception {
		log( "extract configuraton item=" + name + " ..." );
		
		String LOCATION = deployment.getDeployPath( this );
		
		LocalFolder folder = artefactory.getWorkFolder( this , "config.live" );
		folder.recreateThis( this );

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		if( !redist.getConfigItem( this , folder , confItem , LOCATION ) ) {
			if( !context.CTX_FORCE )
				exit( "unable to get configuration item=" + confItem.KEY );
			return;
		}
		
		sourceStorage.saveLiveConfigItem( this , server , node , name , folder , "ActionSaveConfigs" );
	}
	
}
