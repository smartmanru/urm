package org.urm.action.deploy;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.conf.ConfBuilder;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.Types.*;

public class ActionConfigure extends ActionBase {

	Dist dist;
	LocalFolder baseFolder;
	LocalFolder templateFolder;
	
	public ActionConfigure( ActionBase action , String stream , LocalFolder baseFolder ) {
		super( action , stream , "Create product configuration files" );
		this.baseFolder = baseFolder;
	}
	
	public ActionConfigure( ActionBase action , String stream , Dist dist , LocalFolder baseFolder ) {
		super( action , stream , "Create configuration files, release=" + dist.RELEASEDIR );
		this.dist = dist;
		this.baseFolder = baseFolder;
	}

	public LocalFolder getLiveFolder() throws Exception {
		LocalFolder serverFolder = baseFolder.getSubFolder( this , "live" );
		return( serverFolder );
	}
	
	public LocalFolder getLiveFolder( MetaEnvServer server ) throws Exception {
		LocalFolder serverFolder = baseFolder.getSubFolder( this , Common.getPath( "live" , server.NAME ) );
		return( serverFolder );
	}
	
	public LocalFolder getLiveFolder( MetaEnvServerNode node , MetaDistrConfItem confItem ) throws Exception {
		LocalFolder serverFolder = baseFolder.getSubFolder( this , Common.getPath( "live" , node.server.NAME ) );
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , node.meta );
		String name = sourceStorage.getConfItemLiveName( this , node , confItem );
		LocalFolder live = serverFolder.getSubFolder( this , name );
		return( live );
	}
	
	@Override protected void runBefore( ScopeState state , ActionScope scope ) throws Exception {
		baseFolder.recreateThis( this );
		templateFolder = baseFolder.getSubFolder( this , "templates" );
		info( "prepare configuraton files in " + baseFolder.folderPath + " ..." );
		templateFolder.recreateThis( this );
		
		// collect components
		Map<String, MetaDistrConfItem> confs = new HashMap<String, MetaDistrConfItem>(); 
		for( ActionScopeSet set : scope.getSets( this ) )
			for( ActionScopeTarget target : set.getTargets( this ).values() )
				confs.putAll( target.envServer.getConfItems() );
		
		// export/copy to template folder
		for( MetaDistrConfItem conf : confs.values() )
			fillTemplateFolder( conf );
	}

	private void fillTemplateFolder( MetaDistrConfItem conf ) throws Exception {
		if( dist == null ) {
			// download configuration templates
			SourceStorage sourceStorage = artefactory.getSourceStorage( this , conf.meta );
			sourceStorage.exportTemplateConfigItem( this , null , conf.NAME , "" , templateFolder );
			return;
		}
		
		// copy from release
		if( dist.release.findCategoryTarget( this , VarCATEGORY.CONFIG , conf.NAME ) != null ) {
			LocalFolder folder = templateFolder.getSubFolder( this , conf.NAME );
			if( folder.checkExists( this ) )
				dist.copyDistConfToFolder( this , conf , folder );
			else
				super.debug( "missing configuration component=" + conf.NAME );
		}
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , target.meta );
		LocalFolder serverFolder = baseFolder.getSubFolder( this , Common.getPath( "live" , server.NAME ) );
		
		// configure live
		for( ActionScopeTargetItem node : target.getItems( this ) )
			executeNode( server , node.envServerNode , sourceStorage , serverFolder );
		
		return( SCOPESTATE.RunSuccess );
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , SourceStorage sourceStorage , LocalFolder parent ) throws Exception {
		for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
			if( deployment.confItem != null ) {
				String name = sourceStorage.getConfItemLiveName( this , node , deployment.confItem );
				executeNodeConf( parent , sourceStorage , server , node , deployment , deployment.confItem , name );
				continue;
			}
			
			// deployments
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem compItem : deployment.comp.getConfItems() ) {
				if( compItem.confItem != null ) {
					String name = sourceStorage.getConfItemLiveName( this , node , compItem.confItem );
					executeNodeConf( parent , sourceStorage , server , node , deployment , compItem.confItem , name );
				}
			}
		}
	}
	
	private void executeNodeConf( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name ) throws Exception {
		LocalFolder template = templateFolder.getSubFolder( this , confItem.NAME );
		if( !template.checkExists( this ) ) {
			if( dist == null )
				this.exitUnexpectedState();
			
			return;
		}
		
		info( "server=" + server.NAME + ", node" + node.POS + ": prepare configuraton item=" + confItem.NAME );
		LocalFolder live = parent.getSubFolder( this , name );
		live.recreateThis( this );
		
		ConfBuilder builder = new ConfBuilder( this , server.meta );
		builder.configureComponent( template , live , confItem , server , node );
	}
	
}
