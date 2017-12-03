package org.urm.engine;

import java.util.List;

import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineBase;
import org.urm.db.engine.DBEngineBuilders;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.engine.DBEngineInfrastructure;
import org.urm.db.engine.DBEngineLifecycles;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.db.engine.DBEngineResources;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.shell.Account;
import org.urm.meta.ProductContext;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.BaseGroup;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.Network;
import org.urm.meta.engine.NetworkHost;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.engine.LifecyclePhase;
import org.urm.meta.engine.AppSystem;
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

	// ################################################################################
	// ################################################################################
	// CONTENTS:
	//		core:
	//			SETTINGS
	//			RESOURCES
	//			BASE
	//			INFRASTRUCTURE
	//			MIRRORS
	//			BUILDERS
	//			LIFECYCLES
	//		directory:
	//			DIRECTORY
	//			MONITORING
	//		product:
	//			PRODUCT
	//			ENVIRONMENT
	
	// ################################################################################
	// ################################################################################
	// SETTINGS
	
	public void setEngineProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setEngineProperties( this , props );
	}
	
	public void setEngineProductDefaultsProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductDefaultsProperties( this , props );
	}
	
	public void setEngineProductBuildCommonDefaultsProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildCommonDefaultsProperties( this , props );
	}
	
	public void setEngineProductBuildModeDefaultsProperties( DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildModeDefaultsProperties( this , mode , props );
	}

	// ################################################################################
	// ################################################################################
	// RESOURCES
	
	public AuthResource createResource( AuthResource rcdata ) throws Exception {
		checkTransactionResources( resources );
		return( DBEngineResources.createResource( this , resources , rcdata ) );
	}
	
	public void updateResource( AuthResource rc , AuthResource rcdata ) throws Exception {
		checkTransactionResources( rc.resources );
		DBEngineResources.modifyResource( this , resources , rc , rcdata );
	}
	
	public void deleteResource( AuthResource rc ) throws Exception {
		checkTransactionResources( rc.resources );
		DBEngineResources.deleteResource( this , resources , rc );
	}

	public void verifyResource( AuthResource rc ) throws Exception {
		checkTransactionResources( rc.resources );
		DBEngineResources.verifyResource( this , resources , rc );
	}
	
	// ################################################################################
	// ################################################################################
	// MIRRORS
	
	public void createMirrorRepository( MirrorRepository repo , Integer resourceId , String reponame , String reporoot , String dataroot , boolean push ) throws Exception {
		checkTransactionMirrors( repo.mirrors );
		if( push )
			loader.exportRepo( repo );
		DBEngineMirrors.createRepository( this , mirrors , repo , resourceId , reponame  , reporoot , dataroot , push );
		if( !push )
			loader.importRepo( repo );
	}

	public void pushMirror( MirrorRepository repo ) throws Exception {
		loader.exportRepo( repo );
		DBEngineMirrors.pushMirror( this , mirrors , repo );
	}

	public void refreshMirror( MirrorRepository repo ) throws Exception {
		DBEngineMirrors.refreshMirror( this , mirrors , repo );
		loader.importRepo( repo );
	}

	public void dropMirror( MirrorRepository repo , boolean dropOnServer ) throws Exception {
		checkTransactionMirrors( repo.mirrors );
		DBEngineMirrors.dropMirror( this , mirrors , repo , dropOnServer );
	}

	public void createMirrorRepository( EngineMirrors mirrors , MetaSourceProject project ) throws Exception {
		checkTransactionMirrors( mirrors );
		DBEngineMirrors.createProjectMirror( this , mirrors , project );
	}

	public void changeMirrorRepository( EngineMirrors mirrors , MetaSourceProject project ) throws Exception {
		checkTransactionMirrors( mirrors );
		DBEngineMirrors.changeProjectMirror( this , mirrors , project );
	}

	public void deleteSourceProjectMirror( EngineMirrors mirrors , MetaSourceProject project , boolean leaveManual ) throws Exception {
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
		DBEngineMirrors.deleteProjectMirror( this , mirrors , project );
	}

	// ################################################################################
	// ################################################################################
	// BASE
	
	public BaseGroup createBaseGroup( DBEnumBaseCategoryType type , String name , String desc ) throws Exception {
		checkTransactionBase();
		return( DBEngineBase.createGroup( this , base , type , name , desc ) );
	}

	public void deleteBaseGroup( BaseGroup group ) throws Exception {
		checkTransactionBase();
		DBEngineBase.deleteGroup( this , base , group );
	}

	public void modifyBaseGroup( BaseGroup group , String name , String desc ) throws Exception {
		checkTransactionBase();
		DBEngineBase.modifyGroup( this , base , group , name , desc );
	}

	public BaseItem createBaseItem( BaseGroup group , String name , String desc ) throws Exception {
		checkTransactionBase();
		return( DBEngineBase.createItem( this , base , group , name , desc ) );
	}

	public void modifyBaseItem( BaseItem item , String name , String desc ) throws Exception {
		checkTransactionBase();
		DBEngineBase.modifyItem( this , base , item , name , desc );
	}

	public void deleteBaseItem( BaseItem item ) throws Exception {
		checkTransactionBase();
		DBEngineBase.deleteItem( this , base , item );
	}

	public void modifyBaseItemData( BaseItem item , String name , String version , DBEnumOSType ostype , DBEnumServerAccessType accessType , DBEnumBaseSrcType srcType , DBEnumBaseSrcFormatType srcFormat , String SRCFILE , String SRCFILEDIR , String INSTALLPATH , String INSTALLLINK ) throws Exception {
		checkTransactionBase();
		DBEngineBase.modifyItemData( this , item , name , version , ostype , accessType , srcType , srcFormat , SRCFILE , SRCFILEDIR , INSTALLPATH , INSTALLLINK );
	}
	
	// ################################################################################
	// ################################################################################
	// BUILDERS
	
	public ProjectBuilder createBuilder( ProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		return( DBEngineBuilders.createBuilder( this , builders , builder ) );
	}
	
	public void modifyBuilder( ProjectBuilder builder , ProjectBuilder builderNew ) throws Exception {
		checkTransactionBuilders();
		DBEngineBuilders.modifyBuilder( this , builders , builder , builderNew );
	}
	
	public void deleteBuilder( ProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		DBEngineBuilders.deleteBuilder( this , builders , builder );
	}
	
	// ################################################################################
	// ################################################################################
	// LIFECYCLES
	
	public ReleaseLifecycle createLifecycleType( String name , String desc , DBEnumLifecycleType type , boolean regular , int daysRelease , int daysDeploy , int shiftDays ) throws Exception {
		checkTransactionReleaseLifecycles();
		return( DBEngineLifecycles.createLifecycle( this , lifecycles , name , desc , type , regular , daysRelease , daysDeploy , shiftDays ) );
	}
	
	public void modifyLifecycleType( ReleaseLifecycle lc , String name , String desc , DBEnumLifecycleType type , boolean regular , int daysRelease , int daysDeploy , int shiftDays ) throws Exception {
		checkTransactionReleaseLifecycles();
		DBEngineLifecycles.modifyLifecycle( this , lifecycles , lc , name , desc , type , regular , daysRelease , daysDeploy , shiftDays );
	}
	
	public void deleteLifecycleType( ReleaseLifecycle lc ) throws Exception {
		checkTransactionReleaseLifecycles();
		DBEngineLifecycles.deleteLifecycle( this , lifecycles , lc );
		lc.deleteObject();
	}
	
	public ReleaseLifecycle copyLifecycleType( ReleaseLifecycle lc , String name , String desc ) throws Exception {
		checkTransactionReleaseLifecycles();
		return( DBEngineLifecycles.copyLifecycle( this , lifecycles , lc , name , desc ) );
	}
	
	public void enableLifecycleType( ReleaseLifecycle lc , boolean enable ) throws Exception {
		checkTransactionReleaseLifecycles();
		DBEngineLifecycles.enableLifecycle( this , lifecycles , lc , enable );
	}
	
	public void changeLifecyclePhases( ReleaseLifecycle lc , LifecyclePhase[] phases ) throws Exception {
		checkTransactionReleaseLifecycles();
		DBEngineLifecycles.changePhases( this , lifecycles , lc , phases );
	}
	
	// ################################################################################
	// ################################################################################
	// INFRASTRUCTURE
	
	public Datacenter createDatacenter( String name , String desc ) throws Exception {
		checkTransactionInfrastructure();
		return( DBEngineInfrastructure.createDatacenter( this , infra , name , desc ) );
	}

	public void modifyDatacenter( Datacenter datacenter , String name , String desc ) throws Exception {
		checkTransactionInfrastructure();
		DBEngineInfrastructure.modifyDatacenter( this , infra , datacenter , name , desc );
	}

	public void deleteDatacenter( Datacenter datacenter ) throws Exception {
		checkTransactionInfrastructure();
		EngineAuth auth = action.getServerAuth();
		auth.deleteDatacenter( this , datacenter );
		DBEngineInfrastructure.deleteDatacenter( this , infra , datacenter );
	}
	
	public Network createNetwork( Datacenter datacenter , String name , String desc , String mask ) throws Exception {
		checkTransactionInfrastructure();
		return( DBEngineInfrastructure.createNetwork( this , infra , datacenter , name , desc , mask ) );
	}

	public void modifyNetwork( Network network , String name , String desc , String mask ) throws Exception {
		checkTransactionInfrastructure();
		DBEngineInfrastructure.modifyNetwork( this , infra , network , name , desc , mask );
	}

	public void deleteNetwork( Network network ) throws Exception {
		checkTransactionInfrastructure();
		EngineAuth auth = action.getServerAuth();
		auth.deleteNetwork( this , network );
		DBEngineInfrastructure.deleteNetwork( this , infra , network );
	}

	public NetworkHost createNetworkHost( Network network , String name , String desc , DBEnumOSType osType , String ip , int port ) throws Exception {
		checkTransactionInfrastructure();
		return( DBEngineInfrastructure.createHost( this , infra , network , name , desc , osType , ip , port ) );
	}
	
	public void modifyNetworkHost( NetworkHost host , String name , String desc , DBEnumOSType osType , String ip , int port ) throws Exception {
		checkTransactionInfrastructure();
		DBEngineInfrastructure.modifyHost( this , infra , host , name , desc , osType , ip , port );
	}
	
	public void deleteNetworkHost( NetworkHost host ) throws Exception {
		checkTransactionInfrastructure();
		DBEngineInfrastructure.deleteHost( this , infra , host );
	}
	
	public HostAccount createHostAccount( NetworkHost host , String user , String desc , boolean admin , Integer resourceId ) throws Exception {
		checkTransactionInfrastructure();
		return( DBEngineInfrastructure.createAccount( this , infra , host , user , desc , admin , resourceId ) );
	}
	
	public void modifyHostAccount( HostAccount account , String user , String desc , boolean admin , Integer resourceId , boolean refRename ) throws Exception {
		checkTransactionInfrastructure();
		DBEngineInfrastructure.modifyAccount( this , infra , account , user , desc , admin , resourceId );
	}
	
	public HostAccount createHostAccount( Network network , Account account , AuthResource resource ) throws Exception {
		checkTransactionInfrastructure();
		NetworkHost host = network.findHost( account );
		if( host == null ) {
			DBEnumOSType type = DBEnumOSType.getValue( account.osType.name() , true );
			host = createNetworkHost( network , account.HOST , null , type , account.IP , account.PORT );
		}
		
		return( createHostAccount( host , account.USER , null , account.isAdmin() , resource.ID ) );
	}
	
	public void deleteHostAccount( HostAccount account ) throws Exception {
		checkTransactionInfrastructure();
		DBEngineInfrastructure.deleteAccount( this , infra , account );
	}
	
	// ################################################################################
	// ################################################################################
	// DIRECTORY
	
	public AppSystem createSystem( String name , String desc ) throws Exception {
		checkTransactionDirectory();
		return( DBEngineDirectory.createSystem( this , directory , name , desc ) );
	}
	
	public void modifySystem( AppSystem system , String name , String desc ) throws Exception {
		checkTransactionDirectory();
		DBEngineDirectory.modifySystem( this , directory , system , name , desc );
	}

	public void setSystemOffline( AppSystem system , boolean offline ) throws Exception {
		checkTransactionDirectory();
		DBEngineDirectory.setSystemOffline( this , directory , system , offline );
	}

	public void deleteSystem( AppSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		
		EngineMirrors mirrors = action.getServerMirrors();
		for( String productName : system.getProductNames() ) {
			AppProduct product = system.findProduct( productName );
			DBEngineMirrors.deleteProductResources( this , mirrors , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		}
		
		DBEngineDirectory.deleteSystem( this , directory , system );
	}
	
	public AppProduct createProduct( AppSystem system , String name , String desc , String path , boolean forceClear ) throws Exception {
		checkTransactionDirectory();
		AppProduct product = DBEngineDirectory.createProduct( this , directory , system , name , desc , path );
		
		EngineMirrors mirrors = action.getServerMirrors();
		DBEngineMirrors.addProductMirrors( this , mirrors , product , forceClear );
		
		Meta meta = super.createProductMetadata( product );
		ProductMeta storage = meta.getStorage( action );
		storage.createInitialRepository( this , forceClear );
		
		return( product );
	}
	
	public void modifyProduct( AppProduct product , String name , String desc , String path ) throws Exception {
		checkTransactionDirectory();
		DBEngineDirectory.modifyProduct( this , directory , product , name , desc , path );
	}

	public void setMonitoringEnabled( AppProduct product ) throws Exception {
		checkTransactionDirectory();
		DBEngineDirectory.setMonitoringEnabled( this , directory , product , true );
	}
	
	public void setMonitoringDisabled( AppProduct product ) throws Exception {
		checkTransactionDirectory();
		DBEngineDirectory.setMonitoringEnabled( this , directory , product , false );
	}

	public void setProductOffline( AppProduct product , boolean offline ) throws Exception {
		checkTransactionDirectory();
		DBEngineDirectory.setProductOffline( this , directory , product , offline );
	}

	public void deleteProduct( EngineMirrors mirrors , AppProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		checkTransactionMirrors( mirrors );
		checkTransactionMetadata( product.NAME );
		
		DBEngineMirrors.deleteProductResources( this , mirrors , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		EngineAuth auth = action.getServerAuth();
		auth.deleteProduct( this , product );
		DBEngineDirectory.deleteProduct( this , directory , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
	}

	// ################################################################################
	// ################################################################################
	// MONITORING
	
	public void disableMonitoring() throws Exception {
		checkTransactionMonitoring();
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setEnabled( this , false );
		loader.commitMonitoring();
	}
	
	public void enableMonitoring() throws Exception {
		checkTransactionMonitoring();
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setEnabled( this , true );
		loader.commitMonitoring();
	}

	public void setDefaultMonitoringProperties( PropertySet props ) throws Exception {
		checkTransactionMonitoring();
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setDefaultProperties( this , props );
		loader.commitMonitoring();
	}

	// ################################################################################
	// ################################################################################
	// PRODUCT
	
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

	public void setProductLifecycles( MetaProductCoreSettings core , String major , String minor , boolean urgentsAll , String[] urgents ) throws Exception {
		checkTransactionMetadata( core.meta.getStorage( action ) );
		core.setLifecycles( this , major , minor , urgentsAll , urgents );
	}
	
	public void setProductMonitoringProperties( Meta meta , PropertySet props ) throws Exception {
		checkTransactionMetadata( meta.getStorage( action ) );
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setProductMonitoringProperties( this , meta , props );
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

	// ################################################################################
	// ################################################################################
	// ENVIRONMENT
	
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
	
	public MetaEnvServer createMetaEnvServer( MetaEnvSegment sg , String name , String desc , DBEnumOSType osType , VarSERVERRUNTYPE runType , DBEnumServerAccessType accessType , String sysname ) throws Exception {
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
	
	public void setStartInfo( MetaEnvSegment sg , MetaEnvStartInfo startInfo ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage( action ) );
		sg.setStartInfo( this , startInfo );
	}

	public void modifyServerDeployments( MetaEnvServer server , List<MetaEnvServerDeployment> deployments ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage( action ) );
		server.setDeployments( this , deployments );
	}

	public MetaMonitoringTarget modifyMonitoringTarget( MetaMonitoring monMeta , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		checkTransactionMetadata( monMeta.meta.getStorage( action ) );
		MetaMonitoringTarget target = monMeta.modifyTarget( this , sg , major , enabled , maxTime , schedule );
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.modifyTarget( this , target );
		return( target );
	}

}
