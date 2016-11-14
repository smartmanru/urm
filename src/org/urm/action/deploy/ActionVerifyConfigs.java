package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.action.conf.ConfBuilder;
import org.urm.action.conf.ConfDiffSet;
import org.urm.common.Common;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionVerifyConfigs extends ActionBase {

	String timestamp;
	String confVersion;
	
	public ActionVerifyConfigs( ActionBase action , String stream ) {
		super( action , stream );
		timestamp = Common.getNameTimeStamp();
	}

	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer; 
		if( !server.isConfigurable() ) {
			debug( "ignore server=" + server.NAME + ", type=" + server.getServerTypeName( this ) );
			return( SCOPESTATE.NotRun );
		}

		info( "============================================ " + getMode() + " server=" + server.NAME + " ..." );
		info( "rootpath=" + server.ROOTPATH );

		SourceStorage sourceStorage = artefactory.getSourceStorage( this , target.meta );

		// export templates
		LocalFolder folderAsis = artefactory.getWorkFolder( this , Common.getPath( "config.live" , server.NAME ) );
		LocalFolder folderTobe = artefactory.getWorkFolder( this , Common.getPath( "config.tobe" , server.NAME ) );
		folderAsis.recreateThis( this );
		folderTobe.recreateThis( this );
		if( context.CTX_LIVE ) {
			if( context.CTX_TAG.isEmpty() )
				confVersion = "live-" + timestamp;
			else
				confVersion = "live-tag-" + context.CTX_TAG;
		} else {
			if( context.CTX_TAG.isEmpty() )
				confVersion = "prod-" + timestamp;
			else
				confVersion = "prod-tag-" + context.CTX_TAG;
			sourceStorage.exportTemplates( this , folderTobe , server );
		}
		
		// iterate by nodes - prepare
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			debug( "prepare server=" + server.NAME + " node=" + node.POS + " ..." );
			executeNode( folderAsis , folderTobe , sourceStorage , server , node , true );
		}

		// iterate by nodes - compare
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "execute server=" + server.NAME + " node=" + node.POS + " ..." );
			executeNode( folderAsis , folderTobe , sourceStorage , server , node , false );
		}

		return( SCOPESTATE.RunSuccess );
	}

	private void executeNode( LocalFolder parentAsis , LocalFolder parentTobe , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , boolean prepare ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		
		for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
			if( deployment.confItem != null ) {
				String name = sourceStorage.getConfItemLiveName( this , node , deployment.confItem );
				executeNodeConf( parentAsis , parentTobe , sourceStorage , server , node , deployment , deployment.confItem , name , prepare );
				continue;
			}
			
			// deployments
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem compItem : deployment.comp.getConfItems() ) {
				if( compItem.confItem != null ) {
					String name = sourceStorage.getConfItemLiveName( this , node , compItem.confItem );
					executeNodeConf( parentAsis , parentTobe , sourceStorage , server , node , deployment , compItem.confItem , name , prepare );
				}
			}
		}
		
		// compare system component
		String name = sourceStorage.getSysConfItemLiveName( this , node );
		executeNodeSysConf( parentAsis , parentTobe , sourceStorage , server , node , name , prepare );
	}

	private void executeNodeSysConf( LocalFolder parentAsis , LocalFolder parentTobe , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , String name , boolean prepare ) throws Exception {
		if( !context.CTX_LIVE ) {
			if( prepare )
				debug( "skip compare live system configs" );
			return;
		}

		if( prepare ) {
			debug( "prepare system configuraton component from live ..." );
			sourceStorage.exportLiveConfigItem( this , server , name , context.CTX_TAG , parentAsis );
		}
		else {
			info( "compare system configuraton component with live ..." );
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			LocalFolder asis = parentAsis.getSubFolder( this , name );
			asis.ensureExists( this );
			
			if( !redist.getSysConfigs( this , asis ) )
				ifexit( _Error.UnableGetSystemFiles0 , "unable to get system configuration files" , new String[] {} );
			
			String nodePrefix = name;
			showConfDiffs( server , node , parentTobe , parentAsis , nodePrefix , false );
		}
	}

	private void executeNodeConf( LocalFolder parentAsis , LocalFolder parentTobe , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name , boolean prepare ) throws Exception {
		if( context.CTX_LIVE )
			executeNodeConfLive( parentAsis , parentTobe , sourceStorage , server , node , deployment , confItem , name , prepare );
		else
			executeNodeConfTemplates( parentAsis , parentTobe , sourceStorage , server , node , deployment , confItem , name , prepare );
	}

	private void executeNodeConfLive( LocalFolder parentAsis , LocalFolder parentTobe , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name , boolean prepare ) throws Exception {
		LocalFolder tobe = parentTobe.getSubFolder( this , name );
		if( prepare ) {
			debug( "prepare configuraton item=" + confItem.KEY + " from live ..." );
			sourceStorage.exportLiveConfigItem( this , server , name , context.CTX_TAG , parentTobe );
	
			ConfBuilder builder = new ConfBuilder( this , server.meta );
			builder.configureLiveComponent( tobe , confItem , server , node );
		}
		else {
			info( "compare configuraton item=" + confItem.KEY + " with live ..." );
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			LocalFolder asis = parentAsis.getSubFolder( this , name );
			asis.ensureExists( this );
			
			if( !redist.getConfigItem( this , asis , confItem , deployment.DEPLOYPATH ) )
				ifexit( _Error.UnableGetConfigurationItem1 , "unable to get configuration item=" + confItem.KEY , new String[] { confItem.KEY } );
			
			String nodePrefix = "node" + node.POS + "-";
			showConfDiffs( server , node , parentTobe , parentAsis , nodePrefix , true );
		}
	}
	
	private void executeNodeConfTemplates( LocalFolder parentAsis , LocalFolder parentTobe , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name , boolean prepare ) throws Exception {
		LocalFolder tobe = parentTobe.getSubFolder( this , name );
		if( prepare ) {
			debug( "prepare configuraton item=" + confItem.KEY + " from templates ..." );
			
			LocalFolder template = parentTobe.getSubFolder( this , confItem.KEY );
			tobe.recreateThis( this );
			
			ConfBuilder builder = new ConfBuilder( this , server.meta );
			builder.configureComponent( template , tobe , confItem , server , node );
		}
		else {
			info( "compare configuraton item=" + confItem.KEY + " with templates ..." );
			
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			LocalFolder asis = parentAsis.getSubFolder( this , name );
			asis.ensureExists( this );
			
			if( !redist.getConfigItem( this , asis , confItem , deployment.DEPLOYPATH ) )
				ifexit( _Error.UnableGetConfigurationItem1 , "unable to get configuration item=" + confItem.KEY , new String[] { confItem.KEY } );
			
			String nodePrefix = "node" + node.POS + "-";
			showConfDiffs( server , node , parentTobe , parentAsis , nodePrefix , true );
		}
	}

	private boolean showConfDiffs( MetaEnvServer server , MetaEnvServerNode node , LocalFolder tobeServerFolder , LocalFolder asisServerFolder , String nodePrefix , boolean comps ) throws Exception {
		boolean verifyNode = true;
		
		FileSet releaseSet = tobeServerFolder.getFileSet( this );
		FileSet prodSet = asisServerFolder.getFileSet( this );
		
		debug( "calculate diff between: " + tobeServerFolder.folderPath + " and " + asisServerFolder.folderPath + " ..." );
		ConfDiffSet diff = new ConfDiffSet( releaseSet , prodSet , nodePrefix , comps );
		diff.calculate( this , null );
		
		if( diff.isDifferent( this ) ) {
			verifyNode = false;
			String diffFile = asisServerFolder.getFilePath( this , "confdiff.txt" );
			diff.save( this , diffFile );
			if( context.CTX_SHOWALL )
				error( "found configuration differences in node=" + node.POS + ", see " + diffFile );
			else {
				error( "found configuration differences in node=" + node.POS + ":" );
				info( "see " + diffFile );
			}
		}
		
		if( verifyNode )
			debug( "node configuration is matched" );
		
		return( verifyNode );
	}

	
}
