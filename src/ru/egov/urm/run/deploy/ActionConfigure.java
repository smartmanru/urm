package ru.egov.urm.run.deploy;

import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.conf.ConfBuilder;
import ru.egov.urm.meta.MetaDistrComponentItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerDeployment;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class ActionConfigure extends ActionBase {

	DistStorage dist;
	LocalFolder baseFolder;
	LocalFolder templateFolder;
	
	public ActionConfigure( ActionBase action , String stream , LocalFolder baseFolder ) {
		super( action , stream );
		this.baseFolder = baseFolder;
	}
	
	public ActionConfigure( ActionBase action , String stream , DistStorage dist , LocalFolder baseFolder ) {
		super( action , stream );
		this.dist = dist;
		this.baseFolder = baseFolder;
	}

	public LocalFolder getLiveFolder( MetaEnvServer server ) throws Exception {
		LocalFolder serverFolder = baseFolder.getSubFolder( this , Common.getPath( "live" , server.NAME ) );
		return( serverFolder );
	}
	
	public LocalFolder getLiveFolder( MetaEnvServerNode node , MetaDistrConfItem confItem ) throws Exception {
		LocalFolder serverFolder = baseFolder.getSubFolder( this , Common.getPath( "live" , node.server.NAME ) );
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		String name = sourceStorage.getConfItemLiveName( this , node , confItem );
		LocalFolder live = serverFolder.getSubFolder( this , name );
		return( live );
	}
	
	@Override protected void runBefore( ActionScope scope ) throws Exception {
		baseFolder.recreateThis( this );
		templateFolder = baseFolder.getSubFolder( this , "templates" );
		log( "prepare configuraton files in " + baseFolder.folderPath + " ..." );
		templateFolder.recreateThis( this );
		
		// collect components
		Map<String, MetaDistrConfItem> confs = new HashMap<String, MetaDistrConfItem>(); 
		for( ActionScopeSet set : scope.getSets( this ) )
			for( ActionScopeTarget target : set.getTargets( this ).values() )
				confs.putAll( target.envServer.getConfItems( this ) );
		
		// export/copy to template folder
		for( MetaDistrConfItem conf : confs.values() )
			fillTemplateFolder( conf );
	}

	private void fillTemplateFolder( MetaDistrConfItem conf ) throws Exception {
		if( dist == null ) {
			// download configuration templates
			SourceStorage sourceStorage = artefactory.getSourceStorage( this );
			sourceStorage.exportTemplateConfigItem( this , conf.KEY , "" , templateFolder );
			return;
		}
		
		// copy from release
		MetaReleaseDelivery delivery = dist.info.findDelivery( this , conf.delivery.NAME );
		if( delivery == null )
			return;
		
		dist.copyDistConfToFolder( this , delivery , templateFolder.getSubFolder( this , conf.KEY ) );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this );
		LocalFolder serverFolder = baseFolder.getSubFolder( this , Common.getPath( "live" , server.NAME ) );
		
		// configure live
		for( ActionScopeTargetItem node : target.getItems( this ) )
			executeNode( server , node.envServerNode , sourceStorage , serverFolder );
		
		return( true );
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , SourceStorage sourceStorage , LocalFolder parent ) throws Exception {
		for( MetaEnvServerDeployment deployment : server.getDeployments( this ) ) {
			if( deployment.confItem != null ) {
				String name = sourceStorage.getConfItemLiveName( this , node , deployment.confItem );
				executeNodeConf( parent , sourceStorage , server , node , deployment , deployment.confItem , name );
				continue;
			}
			
			// deployments
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem compItem : deployment.comp.getConfItems( this ).values() ) {
				if( compItem.confItem != null ) {
					String name = sourceStorage.getConfItemLiveName( this , node , compItem.confItem );
					executeNodeConf( parent , sourceStorage , server , node , deployment , compItem.confItem , name );
				}
			}
		}
	}
	
	private void executeNodeConf( LocalFolder parent , SourceStorage sourceStorage , MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerDeployment deployment , MetaDistrConfItem confItem , String name ) throws Exception {
		LocalFolder template = templateFolder.getSubFolder( this , confItem.KEY );
		if( !template.checkExists( this ) ) {
			if( dist == null )
				this.exitUnexpectedState();
			
			return;
		}
		
		log( "node" + node.POS + ": prepare configuraton item=" + confItem.KEY );
		LocalFolder live = parent.getSubFolder( this , name );
		live.recreateThis( this );
		
		ConfBuilder builder = new ConfBuilder( this );
		builder.configureComponent( template , live , confItem , server , node );
	}
	
}
