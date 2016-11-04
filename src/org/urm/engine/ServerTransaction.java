package org.urm.engine;

import java.util.List;

import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.action.ActionInit;
import org.urm.engine.shell.Account;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerBaseGroup;
import org.urm.meta.engine.ServerBaseItem;
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
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaEnvStartInfo;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.Meta.VarBUILDMODE;
import org.urm.meta.product.Meta.VarNODETYPE;
import org.urm.meta.product.Meta.VarSERVERACCESSTYPE;
import org.urm.meta.product.Meta.VarSERVERRUNTYPE;

public class ServerTransaction extends TransactionBase {

	public ServerTransaction( ServerEngine engine , ActionInit action ) {
		super( engine , action );
	}

	// transactional operations
	public void createMirrorRepository( ServerMirrorRepository repo , String resource , String reponame , String reporoot , String dataroot , String repobranch , boolean push ) throws Exception {
		repo.createMirrorRepository( this , resource , reponame  , reporoot , dataroot , repobranch , push );
		action.saveMirrors( this );
	}

	public void pushMirror( ServerMirrorRepository repo ) throws Exception {
		repo.pushMirror( this );
	}

	public void refreshMirror( ServerMirrorRepository repo ) throws Exception {
		repo.refreshMirror( this );
	}

	public void dropMirror( ServerMirrorRepository repo ) throws Exception {
		repo.dropMirror( this );
		repo.deleteObject();
		action.saveMirrors( this );
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
	
	public void createBuilder( ServerProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		builders.createBuilder( this , builder );
	}
	
	public void updateBuilder( ServerProjectBuilder builder , ServerProjectBuilder builderNew ) throws Exception {
		checkTransactionBuilders();
		builder.updateBuilder( this , builderNew );
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
		checkTransactionMetadata( product.NAME );
		
		ServerMirrors mirrors = action.getMirrors();
		mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		directory.deleteProduct( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		action.saveMirrors( this );
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
	
	public MetaEnv createMetaEnv( Meta meta , String name , boolean prod ) throws Exception {
		ServerProductMeta metadata = getTransactionMetadata( meta );
		MetaEnv env = new MetaEnv( metadata , metadata.meta );
		env.createEnv( getAction() , name , prod );
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
	
	public void updateMetaEnvDC( MetaEnvDC dc ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		dc.updateProperties( this );
	}
	
	public void createMetaEnvDC( MetaEnvDC dc ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		dc.env.createDC( this , dc );
	}
	
	public void deleteMetaEnvDC( MetaEnvDC dc ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		dc.env.deleteDC( this , dc );
		dc.deleteObject();
	}

	public MetaEnvServer createMetaEnvServer( MetaEnvDC dc , String name , String desc , VarOSTYPE osType , VarSERVERRUNTYPE runType , VarSERVERACCESSTYPE accessType , String sysname ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		MetaEnvServer server = new MetaEnvServer( dc.meta , dc );
		server.createServer( action , name , desc , osType , runType , accessType , sysname );
		dc.createServer( this , server );
		return( server );
	}
	
	public void modifyMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.dc.modifyServer( this , server );
	}

	public void deleteMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.dc.deleteServer( this , server );
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

	public void createNetwork( ServerNetwork network ) throws Exception {
		checkTransactionInfrastructure();
		infra.createNetwork( this , network );
		action.saveInfrastructure( this );
	}

	public void deleteNetwork( ServerNetwork network ) throws Exception {
		checkTransactionInfrastructure();
		infra.deleteNetwork( this , network );
		network.deleteObject();
	}

	public void modifyNetwork( ServerNetwork network ) throws Exception {
		checkTransactionInfrastructure();
		infra.modifyNetwork( this , network );
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

	public MetaMonitoringTarget createMonitoringTarget( MetaMonitoring mon , MetaEnvDC dc , int MAXTIME ) throws Exception {
		checkTransactionMetadata( mon.meta.getStorage( action ) );
		return( mon.createTarget( this , dc , MAXTIME ) );
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

	public void setStartInfo( MetaEnvDC dc , MetaEnvStartInfo startInfo ) throws Exception {
		checkTransactionMetadata( dc.meta.getStorage( action ) );
		dc.setStartInfo( this , startInfo );
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
	
	public void createDistrBinaryItem( MetaDistrDelivery delivery , MetaDistrBinaryItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		delivery.dist.createDistrBinaryItem( this , delivery , item );
	}
	
	public void modifyDistrBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.delivery.modifyBinaryItem( this , item );
	}

	public void deleteDistrBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage( action ) );
		item.delivery.deleteBinaryItem( this , item );
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
		item.delivery.deleteConfItem( this , item );
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
		MetaDistr distr = schema.meta.getDistr( action );
		distr.deleteDatabaseSchema( this , schema );
		schema.database.deleteDatabaseSchema( this , schema );
	}
	
}
