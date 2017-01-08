package org.urm.engine;

import java.util.List;

import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.action.ActionInit;
import org.urm.engine.shell.Account;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerAuth;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerBaseGroup;
import org.urm.meta.engine.ServerBaseItem;
import org.urm.meta.engine.ServerDatacenter;
import org.urm.meta.engine.ServerHostAccount;
import org.urm.meta.engine.ServerMirrorRepository;
import org.urm.meta.engine.ServerMirrors;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.engine.ServerNetwork;
import org.urm.meta.engine.ServerNetworkHost;
import org.urm.meta.engine.ServerProduct;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.engine.ServerSystem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrComponentWS;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaEnvStartInfo;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;

public class ServerTransaction extends TransactionBase {

	public ServerTransaction( ServerEngine engine , ActionInit action ) {
		super( engine , action );
	}

	// transactional operations
	public void createMirrorRepository( ServerMirrorRepository repo , String resource , String reponame , String reporoot , String dataroot , String repobranch , boolean push ) throws Exception {
		checkTransactionMirrors();
		repo.createMirrorRepository( this , resource , reponame  , reporoot , dataroot , repobranch , push );
	}

	public void pushMirror( ServerMirrorRepository repo ) throws Exception {
		repo.pushMirror( this );
	}

	public void refreshMirror( ServerMirrorRepository repo ) throws Exception {
		repo.refreshMirror( this );
	}

	public void dropMirror( ServerMirrorRepository repo ) throws Exception {
		checkTransactionMirrors();
		repo.dropMirror( this );
		repo.deleteObject();
	}

	public void createResource( ServerAuthResource res ) throws Exception {
		checkTransactionResources();
		resources.createResource( this , res );
	}
	
	public void updateResource( ServerAuthResource res , ServerAuthResource resNew ) throws Exception {
		checkTransactionResources();
		res.updateResource( this , resNew );
	}
	
	public void deleteResource( ServerAuthResource res ) throws Exception {
		checkTransactionResources();
		resources.deleteResource( this , res );
		res.deleteObject();
	}
	
	public ServerProjectBuilder createBuilder( ServerProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		return( builders.createBuilder( this , builder ) );
	}
	
	public void updateBuilder( ServerProjectBuilder builder , ServerProjectBuilder builderNew ) throws Exception {
		checkTransactionBuilders();
		builder.setBuilderData( this , builderNew );
	}
	
	public void deleteBuilder( ServerProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		builders.deleteBuilder( this , builder );
		builder.deleteObject();
	}
	
	public void createSystem( ServerSystem system ) throws Exception {
		checkTransactionDirectory();
		directory.addSystem( this , system );
	}
	
	public void modifySystem( ServerSystem system ) throws Exception {
		checkTransactionDirectory();
		system.modifySystem( this );
	}

	public void deleteSystem( ServerSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		
		ServerMirrors mirrors = action.getMirrors();
		for( String productName : system.getProducts() ) {
			ServerProduct product = system.getProduct( productName );
			mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		}
		directory.deleteSystem( this , system );
		system.deleteObject();
	}
	
	public void createProduct( ServerProduct product , boolean forceClear ) throws Exception {
		checkTransactionDirectory();
		
		ServerMirrors mirrors = action.getMirrors();
		mirrors.addProductMirrors( this , product , forceClear );
		
		directory.createProduct( this , product );
		super.createProductMetadata( directory , product );
	}
	
	public void modifyProduct( ServerProduct product ) throws Exception {
		checkTransactionDirectory();
		product.modifyProduct( this );
	}

	public void deleteProduct( ServerProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		checkTransactionMirrors();
		checkTransactionMetadata( product.NAME );
		
		ServerMirrors mirrors = action.getMirrors();
		mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		ServerAuth auth = action.getServerAuth();
		auth.deleteProduct( this , product );
		directory.deleteProduct( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		product.deleteObject();
	}

	public void setServerProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setServerProperties( this , props );
	}
	
	public void setServerProductDefaultsProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductDefaultsProperties( this , props );
	}
	
	public void setServerProductBuildCommonDefaultsProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildCommonDefaultsProperties( this , props );
	}
	
	public void setServerProductBuildModeDefaultsProperties( VarBUILDMODE mode , PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildModeDefaultsProperties( this , mode , props );
	}
	
	public void setProductVersion( MetaProductVersion version ) throws Exception {
		checkTransactionMetadata( version.meta.getStorage( action ) );
		ServerProductMeta metadata = getTransactionMetadata( version.meta );
		metadata.setVersion( this , version );
	}

	public void setProductProperties( Meta meta , PropertySet props , boolean system ) throws Exception {
		ServerProductMeta metadata = getTransactionMetadata( meta );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setProperties( this , props , system );
	}
	
	public void setProductBuildCommonProperties( Meta meta , PropertySet props ) throws Exception {
		ServerProductMeta metadata = getTransactionMetadata( meta );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setBuildCommonProperties( this , props );
	}
	
	public void setProductBuildModeProperties( Meta meta , VarBUILDMODE mode , PropertySet props ) throws Exception {
		ServerProductMeta metadata = getTransactionMetadata( meta );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setBuildModeProperties( this , mode , props );
	}

	public MetaProductVersion createProductVersion( Meta meta , int majorFirstNumber , int majorSecondNumber , int majorNextFirstNumber , int majorNextSecondNumber , int lastProdTag , int nextProdTag ) throws Exception {
		ServerProductMeta metadata = getTransactionMetadata( meta );
		MetaProductVersion version = new MetaProductVersion( metadata , metadata.meta );
		version.createVersion( this , majorFirstNumber , majorSecondNumber , majorNextFirstNumber , majorNextSecondNumber , lastProdTag , nextProdTag );
		metadata.setVersion( this , version );
		return( version );
	}
	
	public MetaEnv createMetaEnv( Meta meta , String name , VarENVTYPE envType ) throws Exception {
		ServerProductMeta metadata = getTransactionMetadata( meta );
		MetaEnv env = new MetaEnv( metadata , metadata.meta );
		env.createEnv( getAction() , name , envType );
		metadata.addEnv( this , env );
		return( env );
	}
	
	public void deleteMetaEnv( MetaEnv env ) throws Exception {
		ServerProductMeta metadata = getTransactionMetadata( env.meta );
		metadata.deleteEnv( this , env );
		env.deleteObject();
	}

	public void setMetaEnvProperties( MetaEnv env , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( env.meta.getStorage( action ) );
		env.setProperties( this , props , system );
	}
	
	public void updateMetaEnv( MetaEnv env ) throws Exception {
		checkTransactionMetadata( env.meta.getStorage( action ) );
		env.updateProperties( this );
	}
	
	public void updateMetaEnvSG( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.updateProperties( this );
	}
	
	public void createMetaEnvSG( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.env.createSG( this , sg );
	}
	
	public void deleteMetaEnvSG( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.env.deleteSG( this , sg );
		sg.deleteObject();
	}

	public void setMetaEnvSGProperties( MetaEnvSegment sg , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.setProperties( this , props , system );
	}
	
	public MetaEnvServer createMetaEnvServer( MetaEnvSegment sg , String name , String desc , VarOSTYPE osType , VarSERVERRUNTYPE runType , VarSERVERACCESSTYPE accessType , String sysname ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		MetaEnvServer server = new MetaEnvServer( sg.meta , sg );
		server.createServer( action , name , desc , osType , runType , accessType , sysname );
		sg.createServer( this , server );
		return( server );
	}
	
	public void modifyMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.sg.modifyServer( this , server );
	}

	public void deleteMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.sg.deleteServer( this , server );
		server.deleteObject();
	}

	public void updateMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.updateProperties( this );
	}

	public void setMetaEnvServerProperties( MetaEnvServer server , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.setProperties( this , props , system );
	}
	
	public MetaEnvServerNode createMetaEnvServerNode( MetaEnvServer server , int pos , VarNODETYPE nodeType , Account account ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		MetaEnvServerNode node = new MetaEnvServerNode( server.meta , server , pos );
		node.createNode( action , nodeType , account );
		server.createNode( this , node );
		return( node );
	}
	
	public void modifyMetaEnvServerNode( MetaEnvServerNode node , int pos , VarNODETYPE nodeType , Account account ) throws Exception {
		checkTransactionMetadata( node.meta.getStorage( action ) );
		node.updateProperties( this );
		node.modifyNode( action , pos , nodeType , account );
		node.server.modifyNode( this , node );
	}

	public void updateMetaEnvServerNodeSetOffline( MetaEnvServerNode node , boolean newStatus ) throws Exception {
		node.setOffline( this , newStatus );
	}
	
	public void deleteMetaEnvServerNode( MetaEnvServerNode node ) throws Exception {
		checkTransactionMetadata( node.meta.getStorage( action ) );
		node.server.deleteNode( this , node );
		node.deleteObject();
	}

	public void setMetaEnvServerNodeProperties( MetaEnvServerNode node , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( node.meta.getStorage( action ) );
		node.setProperties( this , props , system );
	}
	
	public void createDatacenter( ServerDatacenter datacenter ) throws Exception {
		checkTransactionInfrastructure();
		infra.createDatacenter( this , datacenter );
		action.saveInfrastructure( this );
	}

	public void modifyDatacenter( ServerDatacenter datacenter ) throws Exception {
		checkTransactionInfrastructure();
		infra.modifyDatacenter( this , datacenter );
		action.saveInfrastructure( this );
	}

	public void deleteDatacenter( ServerDatacenter datacenter ) throws Exception {
		checkTransactionInfrastructure();
		ServerAuth auth = action.getServerAuth();
		auth.deleteDatacenter( this , datacenter );
		infra.deleteDatacenter( this , datacenter );
		datacenter.deleteObject();
	}
	
	public void createNetwork( ServerDatacenter datacenter , ServerNetwork network ) throws Exception {
		checkTransactionInfrastructure();
		datacenter.createNetwork( this , network );
		action.saveInfrastructure( this );
	}

	public void deleteNetwork( ServerNetwork network ) throws Exception {
		checkTransactionInfrastructure();
		ServerAuth auth = action.getServerAuth();
		auth.deleteNetwork( this , network );
		network.datacenter.deleteNetwork( this , network );
		network.deleteObject();
	}

	public void modifyNetwork( ServerNetwork network ) throws Exception {
		checkTransactionInfrastructure();
		network.datacenter.modifyNetwork( this , network );
		action.saveInfrastructure( this );
	}

	public void createNetworkHost( ServerNetworkHost host ) throws Exception {
		checkTransactionInfrastructure();
		host.network.createHost( this , host );
		action.saveInfrastructure( this );
	}
	
	public void modifyNetworkHost( ServerNetworkHost host , boolean renameReferences ) throws Exception {
		checkTransactionInfrastructure();
		host.network.modifyHost( this , host );
		action.saveInfrastructure( this );
	}
	
	public void deleteNetworkHost( ServerNetworkHost host ) throws Exception {
		checkTransactionInfrastructure();
		host.network.deleteHost( this , host );
		action.saveInfrastructure( this );
	}
	
	public void createHostAccount( ServerHostAccount account ) throws Exception {
		checkTransactionInfrastructure();
		account.host.createAccount( this , account );
		action.saveInfrastructure( this );
	}
	
	public void createHostAccount( ServerNetwork network , Account account , ServerAuthResource resource ) throws Exception {
		checkTransactionInfrastructure();
		ServerNetworkHost host = network.createHost( this , account ); 
		host.createAccount( this , account , resource );
		action.saveInfrastructure( this );
	}
	
	public void modifyHostAccount( ServerHostAccount account , boolean renameReferences ) throws Exception {
		checkTransactionInfrastructure();
		account.host.modifyAccount( this , account );
		action.saveInfrastructure( this );
	}
	
	public void deleteHostAccount( ServerHostAccount account ) throws Exception {
		checkTransactionInfrastructure();
		account.host.deleteAccount( this , account );
		action.saveInfrastructure( this );
	}
	
	public void createBaseGroup( ServerBaseGroup group ) throws Exception {
		checkTransactionBase();
		group.category.createGroup( this , group );
		action.saveBase( this );
	}

	public void deleteBaseGroup( ServerBaseGroup group ) throws Exception {
		checkTransactionBase();
		group.category.deleteGroup( this , group );
		action.saveBase( this );
	}

	public void modifyBaseGroup( ServerBaseGroup group ) throws Exception {
		checkTransactionBase();
		group.category.modifyGroup( this , group );
		action.saveBase( this );
	}

	public void createBaseItem( ServerBaseItem item ) throws Exception {
		checkTransactionBase();
		item.group.category.base.createItem( this , item );
		item.group.createItem( this , item );
		action.saveBase( this );
	}

	public void deleteBaseItem( ServerBaseItem item ) throws Exception {
		checkTransactionBase();
		item.group.deleteItem( this , item );
		action.saveBase( this );
	}

	public void disableMonitoring() throws Exception {
		checkTransactionMonitoring();
		ServerMonitoring mon = action.getActiveMonitoring();
		mon.setEnabled( this , false );
		action.saveMonitoring( this );
	}
	
	public void enableMonitoring() throws Exception {
		checkTransactionMonitoring();
		ServerMonitoring mon = action.getActiveMonitoring();
		mon.setEnabled( this , true );
		action.saveMonitoring( this );
	}

	public void setMonitoringEnabled( MetaMonitoring mon ) throws Exception {
		checkTransactionMetadata( mon.meta.getStorage( action ) );
		mon.setMonitoringEnabled( this , true );
	}
	
	public void setMonitoringDisabled( MetaMonitoring mon ) throws Exception {
		checkTransactionMetadata( mon.meta.getStorage( action ) );
		mon.setMonitoringEnabled( this , false );
	}

	public MetaMonitoringTarget createMonitoringTarget( MetaMonitoring mon , MetaEnvSegment sg , int MAXTIME ) throws Exception {
		checkTransactionMetadata( mon.meta.getStorage( action ) );
		return( mon.createTarget( this , sg , MAXTIME ) );
	}
	
	public void deleteMonitoringTarget( MetaMonitoringTarget target ) throws Exception {
		checkTransactionMetadata( target.meta.getStorage( action ) );
		ServerMonitoring mon = action.getActiveMonitoring();
		mon.deleteTarget( this , target );
	}
	
	public void modifyMonitoringTarget( MetaMonitoringTarget target , int MAXTIME ) throws Exception {
		checkTransactionMetadata( target.meta.getStorage( action ) );
		ServerMonitoring mon = action.getActiveMonitoring();
		target.modifyTarget( this , MAXTIME );
		mon.modifyTarget( this , target );
	}

	public void setDefaultMonitoringProperties( PropertySet props ) throws Exception {
		checkTransactionMonitoring();
		ServerMonitoring mon = action.getActiveMonitoring();
		mon.setDefaultProperties( this , props );
		action.saveMonitoring( this );
	}

	public void setProductMonitoringProperties( Meta meta , PropertySet props ) throws Exception {
		checkTransactionMetadata( meta.getStorage( action ) );
		MetaMonitoring mon = meta.getMonitoring( action );
		mon.setProductProperties( this , props );
	}

	public void setStartInfo( MetaEnvSegment sg , MetaEnvStartInfo startInfo ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.setStartInfo( this , startInfo );
	}

	public void modifyServerDeployments( MetaEnvServer server , List<MetaEnvServerDeployment> deployments ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.setDeployments( this , deployments );
	}

	public void createDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage( action ) );
		delivery.dist.createDelivery( this , delivery );
	}
	
	public void modifyDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage( action ) );
		delivery.dist.modifyDelivery( this , delivery );
	}
	
	public void deleteDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage( action ) );
		delivery.dist.deleteDelivery( this , delivery );
	}
	
	public MetaDistrBinaryItem createDistrBinaryItem( MetaDistrDelivery delivery , String key ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage( action ) );
		MetaDistrBinaryItem item = new MetaDistrBinaryItem( delivery.meta , delivery );
		item.createBinaryItem( this , key );
		delivery.dist.createDistrBinaryItem( this , delivery , item );
		return( item );
	}
	
	public void modifyDistrBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.delivery.modifyBinaryItem( this , item );
	}

	public void deleteDistrBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.delivery.dist.deleteBinaryItem( this , item );
	}
	
	public void createDistrConfItem( MetaDistrDelivery delivery , MetaDistrConfItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		delivery.dist.createDistrConfItem( this , delivery , item );
	}
	
	public void modifyDistrConfItem( MetaDistrConfItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.delivery.modifyConfItem( this , item );
	}

	public void deleteDistrConfItem( MetaDistrConfItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.delivery.dist.deleteConfItem( this , item );
	}
	
	public void createDatabaseSchema( MetaDatabaseSchema schema ) throws Exception {
		checkTransactionMetadata( schema.meta.getStorage( action ) );
		schema.database.createDatabaseSchema( this , schema );
	}
	
	public void modifyDatabaseSchema( MetaDatabaseSchema schema ) throws Exception {
		checkTransactionMetadata( schema.meta.getStorage( action ) );
		schema.database.modifyDatabaseSchema( this , schema );
	}

	public void deleteDatabaseSchema( MetaDatabaseSchema schema ) throws Exception {
		checkTransactionMetadata( schema.meta.getStorage( action ) );
		schema.database.deleteDatabaseSchema( this , schema );
	}
	
	public void createDistrComponent( MetaDistrComponent item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.dist.createDistrComponent( this , item );
	}
	
	public void modifyDistrComponent( MetaDistrComponent item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.dist.modifyDistrComponent( this , item );
	}

	public void deleteDistrComponent( MetaDistrComponent item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.dist.deleteDistrComponent( this , item );
	}
	
	public void createDistrComponentItem( MetaDistrComponent comp , MetaDistrComponentItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.comp.createItem( this , item );
	}
	
	public void modifyDistrComponentItem( MetaDistrComponentItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.comp.modifyItem( this , item );
	}

	public void deleteDistrComponentItem( MetaDistrComponentItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.comp.deleteItem( this , item );
	}

	public void createDistrComponentService( MetaDistrComponent comp , MetaDistrComponentWS service ) throws Exception {
		checkTransactionMetadata( service.meta.getStorage( action ) );
		service.comp.createWebService( this , service );
	}
	
	public void modifyDistrComponentService( MetaDistrComponentWS service ) throws Exception {
		checkTransactionMetadata( service.meta.getStorage( action ) );
		service.comp.modifyWebService( this , service );
	}

	public void deleteDistrComponentService( MetaDistrComponentWS service ) throws Exception {
		checkTransactionMetadata( service.meta.getStorage( action ) );
		service.comp.deleteWebService( this , service );
	}

	public void setDeliveryDatabase( MetaDistrDelivery delivery , List<MetaDatabaseSchema> set ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage( action ) );
		delivery.setDatabase( this , set );
	}

	public MetaSourceProjectSet createSourceProjectSet( MetaSource sources , String name ) throws Exception {
		checkTransactionMetadata( sources.meta.getStorage( action ) );
		return( sources.createProjectSet( this , name ) );
	}

	public void changeProjectSet( MetaSourceProject project , MetaSourceProjectSet setNew ) throws Exception {
		checkTransactionMetadata( project.meta.getStorage( action ) );
		MetaSourceProjectSet setOld = project.set;
		project.changeProjectSet( this , setNew );
		setOld.removeProject( this , project );
		setNew.addProject( this , project );
		
		if( setOld.isEmpty() )
			setOld.sources.removeProjectSet( this , setOld );
	}

	public MetaSourceProject createSourceProject( MetaSourceProjectSet set , String name , int POS ) throws Exception {
		checkTransactionMetadata( set.meta.getStorage( action ) );
		return( set.sources.createProject( this , set , name , POS ) );
	}

	public void changeProjectOrder( MetaSourceProject project , int POS ) throws Exception {
		checkTransactionMetadata( project.meta.getStorage( action ) );
		project.set.changeProjectOrder( this , project , POS );
	}

	public void changeProjectSetOrder( MetaSourceProjectSet set ) throws Exception {
		checkTransactionMetadata( set.meta.getStorage( action ) );
		set.reorderProjects( this );
	}
	
	public void createMirrorRepository( MetaSourceProject project ) throws Exception {
		checkTransactionMirrors();
		
		ServerMirrors mirrors = action.getActiveMirrors();
		mirrors.createProjectMirror( this , project );
	}

	public void changeMirrorRepository( MetaSourceProject project ) throws Exception {
		checkTransactionMirrors();
		
		ServerMirrors mirrors = action.getActiveMirrors();
		mirrors.changeProjectMirror( this , project );
	}

	public void deleteSourceProject( MetaSourceProject project , boolean leaveManual ) throws Exception {
		checkTransactionMetadata( project.meta.getStorage( action ) );
		checkTransactionMirrors();
		
		Meta meta = project.meta;
		MetaSource sources = project.set.sources;
		MetaDistr distr = meta.getDistr( action );
		for( MetaSourceProjectItem item : project.getItems() ) {
			MetaDistrBinaryItem distItem = item.distItem;
			if( leaveManual )
				distr.changeBinaryItemProjectToManual( this , distItem );
			else
				distr.deleteBinaryItem( this , distItem );
		}
		sources.removeProject( this , project );
		
		ServerMirrors mirrors = action.getActiveMirrors();
		mirrors.deleteProjectMirror( this , project );
	}

	public MetaSourceProjectItem createSourceProjectItem( MetaSourceProject project , String name ) throws Exception {
		checkTransactionMetadata( project.meta.getStorage( action ) );
		
		MetaSourceProjectItem item = new MetaSourceProjectItem( project.meta , project );
		item.createItem( this , name );
		project.addItem( this , item );
		return( item );
	}
	
	public void deleteSourceProjectItem( MetaSourceProjectItem item , boolean leaveManual ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		
		MetaDistrBinaryItem distItem = item.distItem;
		if( distItem != null ) {
			MetaDistr distr = distItem.delivery.dist;
			if( leaveManual )
				distr.changeBinaryItemProjectToManual( this , distItem );
			else
				distr.deleteBinaryItem( this , distItem );
		}
		
		item.project.removeItem( this , item );
	}

}
