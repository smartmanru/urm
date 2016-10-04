package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionSaveConfigs extends ActionBase {

	public ActionSaveConfigs( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , scope.meta );
		if( scope.scopeFull && context.CTX_FORCE )
			deleteOldConfServers( scope );
		
		// check need to tag configuration
		if( !context.CTX_TAG.isEmpty() )
			sourceStorage.tagLiveConfigs( this , context.CTX_TAG , "ActionSaveConfigs" );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer; 
		if( !server.isConfigurable( this ) ) {
			debug( "ignore server=" + server.NAME + ", type=" + server.getServerTypeName( this ) );
			return( true );
		}

		info( "============================================ execute server=" + server.NAME + " ..." );

		// iterate by nodes
		String F_REDIST_SAVEITEMS = "";
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "execute server=" + server.NAME + " node=" + node.POS + " ..." );
			
			String confList = executeNode( server , node );
			F_REDIST_SAVEITEMS = Common.addItemToUniqueSpacedList( F_REDIST_SAVEITEMS , confList );
		}

		// delete old
		if( context.CTX_FORCE && target.itemFull )
			deleteOldConfItems( server , F_REDIST_SAVEITEMS );
		
		return( true );
	}

	private String executeNode( MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , server.meta );
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
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , server.meta );
		String[] existingItems = sourceStorage.getLiveConfigItems( this , server );

		for( String item : existingItems ) {
			if( Common.checkPartOfSpacedList( item , saveItems ) )
				continue;
			
			info( "delete obsolete configuration item=" + item + " ..." );
			sourceStorage.deleteLiveConfigItem( this , server , item , "ActionSaveConfigs" );
		}
	}

	private void deleteOldConfServers( ActionScope scope ) throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , scope.meta );
		
		for( ActionScopeSet set : scope.getEnvSets( this ) ) {
			String[] existingItems = sourceStorage.getLiveConfigServers( this , set.dc );

			for( String item : existingItems ) {
				if( set.dc.findServer( item ) != null )
					continue;
				
				info( "delete obsolete server=" + item + " ..." );
				sourceStorage.deleteLiveConfigServer( this , set.dc , item , "ActionSaveConfigs" );
			}
		}
	}

	private void executeNodeSysConf( SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , String name ) throws Exception {
		info( "extract system configuraton component ..." );

		LocalFolder folder = artefactory.getWorkFolder( this , "config.live" );
		folder.recreateThis( this );

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		if( !redist.getSysConfigs( this , folder ) ) {
			ifexit( _Error.UnableGetSystemFiles0 , "unable to get system files configuration" , null );
			return;
		}
		
		sourceStorage.saveLiveConfigItem( this , server , node , name , folder , "ActionSaveConfigs" );
	}
	
	private void executeNodeConf( SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name ) throws Exception {
		info( "extract configuraton item=" + name + " ..." );
		
		String LOCATION = deployment.getDeployPath( this );
		
		LocalFolder folder = artefactory.getWorkFolder( this , "config.live" );
		folder.recreateThis( this );

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		if( !redist.getConfigItem( this , folder , confItem , LOCATION ) ) {
			ifexit( _Error.UnableGetApplicationConf1 , "unable to get configuration item=" + confItem.KEY , new String[] { confItem.KEY } );
			return;
		}
		
		sourceStorage.saveLiveConfigItem( this , server , node , name , folder , "ActionSaveConfigs" );
	}
	
}
