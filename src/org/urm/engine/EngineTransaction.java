package org.urm.engine;

import java.util.List;

import org.urm.common.RunContext.VarOSTYPE;
import org.urm.db.DBEnumTypes.*;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductContext;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.BaseGroup;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.BaseItemData;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.Network;
import org.urm.meta.engine.NetworkHost;
import org.urm.meta.engine.Product;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.engine.ReleaseLifecyclePhase;
import org.urm.meta.engine.System;
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
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;

public class EngineTransaction extends TransactionBase {

	public EngineTransaction( Engine engine , ActionInit action ) {
		super( engine , action );
	}

	// transactional operations
	public void createMirrorRepository( MirrorRepository repo , String resource , String reponame , String reporoot , String dataroot , boolean push ) throws Exception {
		checkTransactionMirrors( repo.mirrors );
		repo.createMirrorRepository( this , resource , reponame  , reporoot , dataroot , push );
		if( !push ) {
			EngineLoader loader = engine.getLoader( action );
			loader.rereadMirror( repo );
		}
	}

	public void pushMirror( MirrorRepository repo ) throws Exception {
		repo.pushMirror( this );
	}

	public void refreshMirror( MirrorRepository repo ) throws Exception {
		repo.refreshMirror( this );
		EngineLoader loader = engine.getLoader( action );
		loader.rereadMirror( repo );
	}

	public void dropMirror( MirrorRepository repo , boolean dropOnServer ) throws Exception {
		checkTransactionMirrors( repo.mirrors );
		repo.dropMirror( this , dropOnServer );
		repo.deleteObject();
	}

	public void createResource( AuthResource res ) throws Exception {
		checkTransactionResources( res.resources );
		resources.createResource( this , res );
	}
	
	public void updateResource( AuthResource res , AuthResource resNew ) throws Exception {
		checkTransactionResources( res.resources );
		resources.updateResource( this , res , resNew );
	}
	
	public void deleteResource( AuthResource res ) throws Exception {
		checkTransactionResources( res.resources );
		resources.deleteResource( this , res );
		res.deleteObject();
	}

	public void verifyResource( AuthResource res ) throws Exception {
		checkTransactionResources( res.resources );
		res.setVerified( this );
	}
	
	public ProjectBuilder createBuilder( ProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		return( builders.createBuilder( this , builder ) );
	}
	
	public void updateBuilder( ProjectBuilder builder , ProjectBuilder builderNew ) throws Exception {
		checkTransactionBuilders();
		builder.setBuilderData( this , builderNew );
	}
	
	public void deleteBuilder( ProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		builders.deleteBuilder( this , builder );
		builder.deleteObject();
	}
	
	public ReleaseLifecycle createLifecycleType( ReleaseLifecycle lc ) throws Exception {
		checkTransactionReleaseLifecycles();
		return( lifecycles.createLifecycle( this , lc ) );
	}
	
	public void updateLifecycleType( ReleaseLifecycle lc , ReleaseLifecycle lcNew ) throws Exception {
		checkTransactionReleaseLifecycles();
		lc.setLifecycleData( this , lcNew );
	}
	
	public void deleteLifecycleType( ReleaseLifecycle lc ) throws Exception {
		checkTransactionReleaseLifecycles();
		lifecycles.deleteLifecycle( this , lc );
		lc.deleteObject();
	}
	
	public ReleaseLifecycle copyLifecycleType( ReleaseLifecycle lc , String name , String desc ) throws Exception {
		checkTransactionReleaseLifecycles();
		return( lifecycles.copyLifecycle( this , lc , name , desc ) );
	}
	
	public void enableLifecycleType( ReleaseLifecycle lc , boolean enable ) throws Exception {
		checkTransactionReleaseLifecycles();
		lc.enableLifecycle( this , enable );
	}
	
	public void changeLifecyclePhases( ReleaseLifecycle lc , ReleaseLifecyclePhase[] phases ) throws Exception {
		checkTransactionReleaseLifecycles();
		lc.changePhases( this , phases );
	}
	
	public void createSystem( System system ) throws Exception {
		checkTransactionDirectory();
		directory.addSystem( this , system );
	}
	
	public void modifySystem( System system ) throws Exception {
		checkTransactionDirectory();
		directory.modifySystem( this , system );
	}

	public void deleteSystem( System system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		
		EngineMirrors mirrors = action.getServerMirrors();
		for( String productName : system.getProductNames() ) {
			Product product = system.findProduct( productName );
			mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		}
		directory.deleteSystem( this , system );
		system.deleteObject();
	}
	
	public void createProduct( Product product , boolean forceClear ) throws Exception {
		checkTransactionDirectory();
		
		EngineMirrors mirrors = action.getServerMirrors();
		mirrors.addProductMirrors( this , product , forceClear );
		
		directory.createProduct( this , product );
		Meta meta = super.createProductMetadata( directory , product );
		ProductMeta storage = meta.getStorage( action );
		storage.createInitialRepository( this , forceClear );
	}
	
	public void modifyProduct( Product product ) throws Exception {
		checkTransactionDirectory();
		product.modifyProduct( this );
	}

	public void deleteProduct( EngineMirrors mirrors , Product product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		checkTransactionMirrors( mirrors );
		checkTransactionMetadata( product.NAME );
		
		mirrors.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		EngineAuth auth = action.getServerAuth();
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
	
	public void setServerProductBuildModeDefaultsProperties( DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildModeDefaultsProperties( this , mode , props );
	}
	
	public void setProductProperties( Meta meta , PropertySet props , boolean system ) throws Exception {
		ProductMeta metadata = getTransactionMetadata( meta );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setProperties( this , props , system );
	}
	
	public void setProductBuildCommonProperties( Meta meta , PropertySet props ) throws Exception {
		ProductMeta metadata = getTransactionMetadata( meta );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setBuildCommonProperties( this , props );
	}
	
	public void setProductBuildModeProperties( Meta meta , DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		ProductMeta metadata = getTransactionMetadata( meta );
		MetaProductSettings settings = metadata.getProductSettings();
		settings.setBuildModeProperties( this , mode , props );
	}

	public MetaProductVersion updateProductVersion( Meta meta , int majorFirstNumber , int majorSecondNumber , int majorNextFirstNumber , int majorNextSecondNumber , int lastProdTag , int nextProdTag ) throws Exception {
		ProductMeta metadata = getTransactionMetadata( meta );
		MetaProductVersion version = metadata.getVersion();
		version.updateVersion( this , majorFirstNumber , majorSecondNumber , majorNextFirstNumber , majorNextSecondNumber , lastProdTag , nextProdTag );
		
		ProductContext context = new ProductContext( meta );
		context.create( action , version );
		MetaProductSettings settings = meta.getProductSettings( action );
		settings.updateSettings( this , context );
		
		settings.recalculateChildProperties( action );
		return( version );
	}
	
	public MetaEnv createMetaEnv( Meta meta , String name , VarENVTYPE envType ) throws Exception {
		ProductMeta metadata = getTransactionMetadata( meta );
		MetaProductSettings settings = meta.getProductSettings( action );
		MetaEnv env = new MetaEnv( metadata , settings , metadata.meta );
		env.createEnv( action , name , envType );
		metadata.addEnv( this , env );
		return( env );
	}
	
	public void deleteMetaEnv( MetaEnv env ) throws Exception {
		ProductMeta metadata = getTransactionMetadata( env.meta );
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
	
	public void updateMetaEnvSegment( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.updateProperties( this );
	}
	
	public void createMetaEnvSegment( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.env.createSegment( this , sg );
	}
	
	public void deleteMetaEnvSegment( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.env.deleteSegment( this , sg );
		sg.deleteObject();
	}

	public void setMetaEnvSGProperties( MetaEnvSegment sg , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.setProperties( this , props , system );
	}
	
	public MetaEnvServer createMetaEnvServer( MetaEnvSegment sg , String name , String desc , VarOSTYPE osType , VarSERVERRUNTYPE runType , DBEnumServerAccessType accessType , String sysname ) throws Exception {
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
	
	public void createDatacenter( Datacenter datacenter ) throws Exception {
		checkTransactionInfrastructure();
		infra.createDatacenter( this , datacenter );
	}

	public void modifyDatacenter( Datacenter datacenter ) throws Exception {
		checkTransactionInfrastructure();
		infra.modifyDatacenter( this , datacenter );
	}

	public void deleteDatacenter( Datacenter datacenter ) throws Exception {
		checkTransactionInfrastructure();
		EngineAuth auth = action.getServerAuth();
		auth.deleteDatacenter( this , datacenter );
		infra.deleteDatacenter( this , datacenter );
		datacenter.deleteObject();
	}
	
	public void createNetwork( Datacenter datacenter , Network network ) throws Exception {
		checkTransactionInfrastructure();
		datacenter.createNetwork( this , network );
	}

	public void deleteNetwork( Network network ) throws Exception {
		checkTransactionInfrastructure();
		EngineAuth auth = action.getServerAuth();
		auth.deleteNetwork( this , network );
		network.datacenter.deleteNetwork( this , network );
		network.deleteObject();
	}

	public void modifyNetwork( Network network ) throws Exception {
		checkTransactionInfrastructure();
		network.datacenter.modifyNetwork( this , network );
	}

	public void createNetworkHost( NetworkHost host ) throws Exception {
		checkTransactionInfrastructure();
		host.network.createHost( this , host );
	}
	
	public void modifyNetworkHost( NetworkHost host , boolean renameReferences ) throws Exception {
		checkTransactionInfrastructure();
		host.network.modifyHost( this , host );
	}
	
	public void deleteNetworkHost( NetworkHost host ) throws Exception {
		checkTransactionInfrastructure();
		host.network.deleteHost( this , host );
	}
	
	public void createHostAccount( HostAccount account ) throws Exception {
		checkTransactionInfrastructure();
		account.host.createAccount( this , account );
	}
	
	public void createHostAccount( Network network , Account account , AuthResource resource ) throws Exception {
		checkTransactionInfrastructure();
		NetworkHost host = network.createHost( this , account ); 
		host.createAccount( this , account , resource );
	}
	
	public void modifyHostAccount( HostAccount account , boolean renameReferences ) throws Exception {
		checkTransactionInfrastructure();
		account.host.modifyAccount( this , account );
	}
	
	public void deleteHostAccount( HostAccount account ) throws Exception {
		checkTransactionInfrastructure();
		account.host.deleteAccount( this , account );
	}
	
	public void createBaseGroup( BaseGroup group ) throws Exception {
		checkTransactionBase();
		group.category.createGroup( this , group );
		action.saveBase( this );
	}

	public void deleteBaseGroup( BaseGroup group ) throws Exception {
		checkTransactionBase();
		group.category.deleteGroup( this , group );
		action.saveBase( this );
	}

	public void modifyBaseGroup( BaseGroup group ) throws Exception {
		checkTransactionBase();
		group.category.modifyGroup( this , group );
		action.saveBase( this );
	}

	public void createBaseItem( BaseItem item ) throws Exception {
		checkTransactionBase();
		item.group.category.base.createItem( this , item );
		item.group.createItem( this , item );
		action.saveBase( this );
	}

	public void deleteBaseItem( BaseItem item ) throws Exception {
		checkTransactionBase();
		item.group.deleteItem( this , item );
		action.saveBase( this );
	}

	public void saveBaseItemData( BaseItem item , BaseItemData data ) throws Exception {
		checkTransactionBase();
	}
	
	public void disableMonitoring() throws Exception {
		checkTransactionMonitoring();
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setEnabled( this , false );
		action.saveMonitoring( this );
	}
	
	public void enableMonitoring() throws Exception {
		checkTransactionMonitoring();
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setEnabled( this , true );
		action.saveMonitoring( this );
	}

	public void setMonitoringEnabled( Product product ) throws Exception {
		checkTransactionDirectory();
		product.setMonitoringEnabled( this , true );
	}
	
	public void setMonitoringDisabled( Product product ) throws Exception {
		checkTransactionDirectory();
		product.setMonitoringEnabled( this , false );
	}

	public MetaMonitoringTarget modifyMonitoringTarget( MetaMonitoring monMeta , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		checkTransactionMetadata( monMeta.meta.getStorage( action ) );
		MetaMonitoringTarget target = monMeta.modifyTarget( this , sg , major , enabled , maxTime , schedule );
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.modifyTarget( this , target );
		return( target );
	}

	public void setDefaultMonitoringProperties( PropertySet props ) throws Exception {
		checkTransactionMonitoring();
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setDefaultProperties( this , props );
		action.saveMonitoring( this );
	}

	public void setProductMonitoringProperties( Meta meta , PropertySet props ) throws Exception {
		checkTransactionMetadata( meta.getStorage( action ) );
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setProductMonitoringProperties( this , meta , props );
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

	public void setDeliveryDatabaseAll( MetaDistrDelivery delivery ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage( action ) );
		delivery.setDatabaseAll( this );
	}

	public void setDeliveryDatabaseSet( MetaDistrDelivery delivery , MetaDatabaseSchema[] set ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage( action ) );
		delivery.setDatabaseSet( this , set );
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
	
	public void createMirrorRepository( EngineMirrors mirrors , MetaSourceProject project ) throws Exception {
		checkTransactionMirrors( mirrors );
		mirrors.createProjectMirror( this , project );
	}

	public void changeMirrorRepository( EngineMirrors mirrors , MetaSourceProject project ) throws Exception {
		checkTransactionMirrors( mirrors );
		mirrors.changeProjectMirror( this , project );
	}

	public void deleteSourceProject( EngineMirrors mirrors , MetaSourceProject project , boolean leaveManual ) throws Exception {
		checkTransactionMetadata( project.meta.getStorage( action ) );
		checkTransactionMirrors( mirrors );
		
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

	public void setProductLifecycles( MetaProductCoreSettings core , String major , String minor , boolean urgentsAll , String[] urgents ) throws Exception {
		checkTransactionMetadata( core.meta.getStorage( action ) );
		core.setLifecycles( this , major , minor , urgentsAll , urgents );
	}
	
}
