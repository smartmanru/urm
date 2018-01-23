package org.urm.engine;

import java.util.List;

import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.*;
import org.urm.db.product.*;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineData;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.*;
import org.urm.meta.engine.EngineAuth.SpecialRights;
import org.urm.meta.product.*;
import org.urm.meta.Types.*;

public class EngineTransaction extends TransactionBase {

	public EngineTransaction( Engine engine , EngineData data , ActionInit action ) {
		super( engine , data , action );
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
	//		auth:
	//			AUTH
	//		product:
	//			PRODUCT
	//			ENVIRONMENT
	
	// ################################################################################
	// ################################################################################
	// SETTINGS
	
	public void setEngineProductBuildModeDefaultsProperties( DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		checkTransactionSettings();
		EngineSettings settings = super.getTransactionSettings();
		settings.setProductBuildModeDefaultsProperties( this , mode , props );
	}

	public EntityVar createCustomProperty( int ownerId , ObjectProperties ops , String name , String desc , String defvalue ) throws Exception {
		checkTransactionCustomProperty( ownerId , ops );
		EngineEntities entities = getEntities();
		EntityVar var = DBEngineEntities.createCustomProperty( this , entities , ownerId , ops , name , desc , defvalue );
		entityNew = var.entity;
		return( var );
	}
	
	public EntityVar modifyCustomProperty( int ownerId , ObjectProperties ops , int paramId , String name , String desc , String defvalue ) throws Exception {
		checkTransactionCustomProperty( ownerId , ops );
		EngineEntities entities = getEntities();
		EntityVar var = DBEngineEntities.modifyCustomProperty( this , entities , ownerId , ops , paramId , name , desc , defvalue );
		entityNew = var.entity;
		return( var );
	}
	
	public void deleteCustomProperty( int ownerId , ObjectProperties ops , int paramId ) throws Exception {
		checkTransactionCustomProperty( ownerId , ops );
		EngineEntities entities = getEntities();
		DBEngineEntities.deleteCustomProperty( this , entities , ownerId , ops , paramId );
		ObjectMeta meta = ops.getMeta();
		entityNew = meta.getCustomEntity();
	}
	
	public void updateCustomEngineProperties( EngineSettings settings ) throws Exception {
		checkTransactionSettings();
		ObjectProperties ops = settings.getEngineProperties();
		DBSettings.modifyCustomValues( this , DBVersions.CORE_ID , ops );
	}
	
	public void updateAppEngineProperties( EngineSettings settings ) throws Exception {
		checkTransactionSettings();
		ObjectProperties ops = settings.getEngineProperties();
		DBSettings.modifyAppValues( this , DBVersions.CORE_ID , ops );
	}
	
	public void updateProductDefaultProperties( EngineSettings settings ) throws Exception {
		checkTransactionSettings();
		ObjectProperties ops = settings.getDefaultProductSettigns();
		DBSettings.modifyAppValues( this , DBVersions.CORE_ID , ops );
	}
	
	public void updateProductDefaultBuildCommonProperties( EngineSettings settings ) throws Exception {
		checkTransactionSettings();
		ObjectProperties ops = settings.getDefaultProductBuildSettings();
		DBSettings.modifyAppValues( this , DBVersions.CORE_ID , ops );
	}
	
	public void updateProductDefaultBuildModeProperties( EngineSettings settings , DBEnumBuildModeType mode ) throws Exception {
		checkTransactionSettings();
		ObjectProperties ops = settings.getDefaultProductBuildObjectProperties( mode );
		DBSettings.modifyAppValues( this , DBVersions.CORE_ID , ops );
	}
	
	// ################################################################################
	// ################################################################################
	// RESOURCES
	
	public AuthResource createResource( EngineResources resources , AuthResource rcdata ) throws Exception {
		checkTransactionResources( resources );
		return( DBEngineResources.createResource( this , resources , rcdata ) );
	}
	
	public void updateResource( AuthResource rc , AuthResource rcdata ) throws Exception {
		checkTransactionResources( rc.resources );
		DBEngineResources.modifyResource( this , rc.resources , rc , rcdata );
	}
	
	public void deleteResource( AuthResource rc ) throws Exception {
		checkTransactionResources( rc.resources );
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.deleteResourceAccess( this , auth , rc );
		DBEngineResources.deleteResource( this , rc.resources , rc );
	}

	public void verifyResource( AuthResource rc ) throws Exception {
		checkTransactionResources( rc.resources );
		DBEngineResources.verifyResource( this , rc.resources , rc );
	}
	
	// ################################################################################
	// ################################################################################
	// MIRRORS
	
	public void createMirrorRepository( MirrorRepository repo , Integer resourceId , String reponame , String reporoot , String dataroot , boolean push ) throws Exception {
		checkTransactionMirrors( repo.mirrors );
		EngineLoader loader = engine.createLoader( this );
		if( push )
			loader.exportRepo( repo );
		DBEngineMirrors.createRepository( this , repo.mirrors , repo , resourceId , reponame  , reporoot , dataroot , push );
		if( !push )
			loader.importRepo( repo );
	}

	public void pushMirror( MirrorRepository repo ) throws Exception {
		EngineLoader loader = engine.createLoader( this );
		loader.exportRepo( repo );
		DBEngineMirrors.pushMirror( this , repo.mirrors , repo );
	}

	public void refreshMirror( MirrorRepository repo ) throws Exception {
		DBEngineMirrors.refreshMirror( this , repo.mirrors , repo );
		EngineLoader loader = engine.createLoader( this );
		loader.importRepo( repo );
	}

	public void dropMirrorWorkspace( MirrorRepository repo , boolean dropOnServer ) throws Exception {
		checkTransactionMirrors( repo.mirrors );
		DBEngineMirrors.dropMirrorWorkspace( this , repo.mirrors , repo , dropOnServer );
	}

	public void deleteDetachedMirror( MirrorRepository repo ) throws Exception {
		checkTransactionMirrors( repo.mirrors );
		DBEngineMirrors.dropDetachedMirror( this , repo.mirrors , repo );
	}

	public void deleteSourceProjectMirror( EngineMirrors mirrors , MetaSourceProject project , boolean leaveManual ) throws Exception {
		ProductMeta storage = project.meta.getStorage();
		checkTransactionMetadata( storage );
		checkTransactionMirrors( mirrors );
		
		Meta meta = project.meta;
		MetaSources sources = project.set.sources;
		MetaDistr distr = meta.getDistr();
		for( MetaSourceProjectItem item : project.getItems() ) {
			MetaDistrBinaryItem distItem = item.distItem;
			if( leaveManual )
				DBMetaDistr.changeBinaryItemProjectToManual( this , storage , distr , distItem );
			else
				DBMetaDistr.deleteBinaryItem( this , storage , distr , distItem );
		}
		
		DBMetaSources.deleteProject( this , storage , sources , project );
		DBEngineMirrors.deleteProjectMirror( this , mirrors , project );
	}

	public void createDetachedMirror( EngineMirrors mirrors , DBEnumMirrorType type , String product , String project ) throws Exception {
		checkTransactionMirrors( mirrors );
		DBEngineMirrors.createDetachedMirror( this , mirrors , type , product , project );
	}
	
	// ################################################################################
	// ################################################################################
	// BASE
	
	public BaseGroup createBaseGroup( DBEnumBaseCategoryType type , String name , String desc ) throws Exception {
		checkTransactionBase();
		EngineBase base = super.getTransactionBase();
		return( DBEngineBase.createGroup( this , base , type , name , desc ) );
	}

	public void deleteBaseGroup( BaseGroup group ) throws Exception {
		checkTransactionBase();
		EngineBase base = super.getTransactionBase();
		DBEngineBase.deleteGroup( this , base , group );
	}

	public void modifyBaseGroup( BaseGroup group , String name , String desc ) throws Exception {
		checkTransactionBase();
		EngineBase base = super.getTransactionBase();
		DBEngineBase.modifyGroup( this , base , group , name , desc );
	}

	public BaseItem createBaseItem( BaseGroup group , String name , String desc ) throws Exception {
		checkTransactionBase();
		EngineBase base = super.getTransactionBase();
		return( DBEngineBase.createItem( this , base , group , name , desc ) );
	}

	public void modifyBaseItem( BaseItem item , String name , String desc ) throws Exception {
		checkTransactionBase();
		EngineBase base = super.getTransactionBase();
		DBEngineBase.modifyItem( this , base , item , name , desc );
	}

	public void deleteBaseItem( BaseItem item ) throws Exception {
		checkTransactionBase();
		EngineBase base = super.getTransactionBase();
		DBEngineBase.deleteItem( this , base , item );
	}

	public void modifyBaseItemData( BaseItem item , String name , String version , DBEnumOSType ostype , DBEnumServerAccessType accessType , DBEnumBaseSrcType srcType , DBEnumBaseSrcFormatType srcFormat , String SRCFILE , String SRCFILEDIR , String INSTALLPATH , String INSTALLLINK ) throws Exception {
		checkTransactionBase();
		DBEngineBase.modifyItemData( this , item , name , version , ostype , accessType , srcType , srcFormat , SRCFILE , SRCFILEDIR , INSTALLPATH , INSTALLLINK );
	}

	public void addBaseItemDependency( BaseItem item , BaseItem dep ) throws Exception {
		checkTransactionBase();
		EngineBase base = super.getTransactionBase();
		DBEngineBase.addItemDependency( this , base , item , dep );
	}

	public void deleteBaseItemDependency( BaseItem item , BaseItem dep ) throws Exception {
		checkTransactionBase();
		EngineBase base = super.getTransactionBase();
		DBEngineBase.deleteItemDependency( this , base , item , dep );
	}

	// ################################################################################
	// ################################################################################
	// BUILDERS
	
	public ProjectBuilder createBuilder( EngineBuilders builders , ProjectBuilder builder ) throws Exception {
		checkTransactionBuilders( builders );
		return( DBEngineBuilders.createBuilder( this , builders , builder ) );
	}
	
	public void modifyBuilder( ProjectBuilder builder , ProjectBuilder builderNew ) throws Exception {
		checkTransactionBuilders( builder.builders );
		DBEngineBuilders.modifyBuilder( this , builder.builders , builder , builderNew );
	}
	
	public void deleteBuilder( ProjectBuilder builder ) throws Exception {
		checkTransactionBuilders( builder.builders );
		DBEngineBuilders.deleteBuilder( this , builder.builders , builder );
	}
	
	// ################################################################################
	// ################################################################################
	// LIFECYCLES
	
	public ReleaseLifecycle createLifecycleType( String name , String desc , DBEnumLifecycleType type , boolean regular , int daysRelease , int daysDeploy , int shiftDays ) throws Exception {
		checkTransactionReleaseLifecycles();
		EngineLifecycles lifecycles = super.getTransactionLifecycles();
		return( DBEngineLifecycles.createLifecycle( this , lifecycles , name , desc , type , regular , daysRelease , daysDeploy , shiftDays ) );
	}
	
	public void modifyLifecycleType( ReleaseLifecycle lc , String name , String desc , DBEnumLifecycleType type , boolean regular , int daysRelease , int daysDeploy , int shiftDays ) throws Exception {
		checkTransactionReleaseLifecycles();
		EngineLifecycles lifecycles = super.getTransactionLifecycles();
		DBEngineLifecycles.modifyLifecycle( this , lifecycles , lc , name , desc , type , regular , daysRelease , daysDeploy , shiftDays );
	}
	
	public void deleteLifecycleType( ReleaseLifecycle lc ) throws Exception {
		checkTransactionReleaseLifecycles();
		EngineLifecycles lifecycles = super.getTransactionLifecycles();
		DBEngineLifecycles.deleteLifecycle( this , lifecycles , lc );
	}
	
	public ReleaseLifecycle copyLifecycleType( ReleaseLifecycle lc , String name , String desc ) throws Exception {
		checkTransactionReleaseLifecycles();
		EngineLifecycles lifecycles = super.getTransactionLifecycles();
		return( DBEngineLifecycles.copyLifecycle( this , lifecycles , lc , name , desc ) );
	}
	
	public void enableLifecycleType( ReleaseLifecycle lc , boolean enable ) throws Exception {
		checkTransactionReleaseLifecycles();
		EngineLifecycles lifecycles = super.getTransactionLifecycles();
		DBEngineLifecycles.enableLifecycle( this , lifecycles , lc , enable );
	}
	
	public void changeLifecyclePhases( ReleaseLifecycle lc , LifecyclePhase[] phases ) throws Exception {
		checkTransactionReleaseLifecycles();
		EngineLifecycles lifecycles = super.getTransactionLifecycles();
		DBEngineLifecycles.changePhases( this , lifecycles , lc , phases );
	}
	
	// ################################################################################
	// ################################################################################
	// INFRASTRUCTURE
	
	public Datacenter createDatacenter( String name , String desc ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		return( DBEngineInfrastructure.createDatacenter( this , infra , name , desc ) );
	}

	public void modifyDatacenter( Datacenter datacenter , String name , String desc ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		DBEngineInfrastructure.modifyDatacenter( this , infra , datacenter , name , desc );
	}

	public void deleteDatacenter( Datacenter datacenter ) throws Exception {
		checkTransactionInfrastructure();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.deleteDatacenterAccess( this , auth , datacenter );
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		DBEngineInfrastructure.deleteDatacenter( this , infra , datacenter );
	}
	
	public Network createNetwork( Datacenter datacenter , String name , String desc , String mask ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		return( DBEngineInfrastructure.createNetwork( this , infra , datacenter , name , desc , mask ) );
	}

	public void modifyNetwork( Network network , String name , String desc , String mask ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		DBEngineInfrastructure.modifyNetwork( this , infra , network , name , desc , mask );
	}

	public void deleteNetwork( Network network ) throws Exception {
		checkTransactionInfrastructure();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.deleteNetworkAccess( this , auth , network );
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		DBEngineInfrastructure.deleteNetwork( this , infra , network );
	}

	public NetworkHost createNetworkHost( Network network , String name , String desc , DBEnumOSType osType , String ip , int port ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		return( DBEngineInfrastructure.createHost( this , infra , network , name , desc , osType , ip , port ) );
	}
	
	public void modifyNetworkHost( NetworkHost host , String name , String desc , DBEnumOSType osType , String ip , int port ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		DBEngineInfrastructure.modifyHost( this , infra , host , name , desc , osType , ip , port );
	}
	
	public void deleteNetworkHost( NetworkHost host ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		DBEngineInfrastructure.deleteHost( this , infra , host );
	}
	
	public HostAccount createHostAccount( NetworkHost host , String user , String desc , boolean admin , Integer resourceId ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		return( DBEngineInfrastructure.createAccount( this , infra , host , user , desc , admin , resourceId ) );
	}
	
	public void modifyHostAccount( HostAccount account , String user , String desc , boolean admin , Integer resourceId , boolean refRename ) throws Exception {
		checkTransactionInfrastructure();
		EngineInfrastructure infra = super.getTransactionInfrastructure();
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
		EngineInfrastructure infra = super.getTransactionInfrastructure();
		DBEngineInfrastructure.deleteAccount( this , infra , account );
	}
	
	// ################################################################################
	// ################################################################################
	// DIRECTORY
	
	public AppSystem createSystem( EngineDirectory directory , String name , String desc ) throws Exception {
		checkTransactionDirectory( directory );
		return( DBEngineDirectory.createSystem( this , directory , name , desc ) );
	}
	
	public void modifySystem( AppSystem system , String name , String desc ) throws Exception {
		checkTransactionDirectory( system.directory );
		DBEngineDirectory.modifySystem( this , system.directory , system , name , desc );
	}

	public void setSystemOffline( AppSystem system , boolean offline ) throws Exception {
		checkTransactionDirectory( system.directory );
		DBEngineDirectory.setSystemOffline( this , system.directory , system , offline );
	}

	public void deleteSystem( AppSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory( system.directory );
		
		EngineMirrors mirrors = action.getServerMirrors();
		for( String productName : system.getProductNames() ) {
			AppProduct product = system.findProduct( productName );
			DBEngineMirrors.deleteProductResources( this , mirrors , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		}
		
		DBEngineDirectory.deleteSystem( this , system.directory , system );
	}
	
	public AppProduct createProduct( AppSystem system , String name , String desc , String path , boolean forceClear ) throws Exception {
		checkTransactionDirectory( system.directory );
		AppProduct product = DBEngineDirectory.createProduct( this , system.directory , system , name , desc , path );
		
		EngineMirrors mirrors = action.getServerMirrors();
		changeMirrors( mirrors );
		DBEngineMirrors.createProductMirrors( this , mirrors , product , forceClear );
		
		Meta meta = super.createProductMetadata( product );
		ProductMeta storage = meta.getStorage();
		storage.createInitialRepository( this , forceClear );
		
		return( product );
	}
	
	public void modifyProduct( AppProduct product , String name , String desc , String path ) throws Exception {
		checkTransactionDirectory( product.directory );
		DBEngineDirectory.modifyProduct( this , product.directory , product , name , desc , path );
	}

	public void setProductOffline( AppProduct product , boolean offline ) throws Exception {
		checkTransactionDirectory( product.directory );
		DBEngineDirectory.setProductOffline( this , product.directory , product , offline );
	}

	public void deleteProduct( EngineMirrors mirrors , AppProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory( product.directory );
		checkTransactionMirrors( mirrors );
		checkTransactionMetadata( product.NAME );
		
		DBEngineMirrors.deleteProductResources( this , mirrors , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.deleteProductAccess( this , auth , product );
		DBEngineDirectory.deleteProduct( this , product.directory , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
	}

	// ################################################################################
	// ################################################################################
	// MONITORING
	
	public void disableMonitoring() throws Exception {
		checkTransactionMonitoring();
		EngineMonitoring mon = action.getActiveMonitoring();
		DBEngineMonitoring.enableMonitoring( this , mon , false );
	}
	
	public void enableMonitoring() throws Exception {
		checkTransactionMonitoring();
		EngineMonitoring mon = action.getActiveMonitoring();
		DBEngineMonitoring.enableMonitoring( this , mon , true );
	}

	public void setMonitoringProperties( EngineMonitoring mon ) throws Exception {
		checkTransactionMonitoring();
		DBEngineMonitoring.setProperties( this , mon );
	}

	public void setMonitoringEnabled( AppProduct product ) throws Exception {
		checkTransactionDirectory( product.directory );
		DBEngineDirectory.setMonitoringEnabled( this , product.directory , product , true );
		EngineMonitoring mon = super.getMonitoring();
		mon.setProductEnabled( action , product );
	}
	
	public void setMonitoringDisabled( AppProduct product ) throws Exception {
		checkTransactionDirectory( product.directory );
		DBEngineDirectory.setMonitoringEnabled( this , product.directory , product , false );
		EngineMonitoring mon = super.getMonitoring();
		mon.setProductDisabled( action , product );
	}

	public void setProductMonitoringProperties( Meta meta , PropertySet props ) throws Exception {
		checkTransactionMetadata( meta.getStorage() );
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.setProductMonitoringProperties( this , meta , props );
	}

	public MetaMonitoringTarget modifyMonitoringTarget( MetaMonitoring monMeta , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		checkTransactionMetadata( monMeta.meta.getStorage() );
		MetaMonitoringTarget target = monMeta.modifyTarget( this , sg , major , enabled , maxTime , schedule );
		EngineMonitoring mon = action.getActiveMonitoring();
		mon.modifyTarget( this , target );
		return( target );
	}

	// ################################################################################
	// ################################################################################
	// AUTH
	
	public void disableLdap() throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.disableLdap( this , auth );
	}
	
	public void enableLdap( ObjectProperties ops ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.enableLdap( this , auth , ops );
	}
	
	public AuthGroup createAuthGroup( String name , String desc ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		return( DBEngineAuth.createGroup( this , auth , name , desc ) );
	}
	
	public void modifyAuthGroup( AuthGroup group , String name , String desc ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.modifyGroup( this , auth , group , name , desc );
	}
	
	public void deleteAuthGroup( AuthGroup group ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.deleteGroup( this , auth , group );
	}
	
	public AuthUser createAuthLocalUser( String name , String desc , String full , String email , boolean admin ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		return( DBEngineAuth.createLocalUser( this , auth , name , desc , full , email , admin ) );
	}
	
	public void modifyAuthLocalUser( AuthUser user , String name , String desc , String full , String email , boolean admin ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.modifyLocalUser( this , auth , user , name , desc , full , email , admin );
	}
	
	public void deleteAuthLocalUser( AuthUser user ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.deleteLocalUser( this , auth , user );
	}
	
	public void addGroupLocalUsers( AuthGroup group , String[] users ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.addGroupLocalUsers( this , auth , group , users );
	}
	
	public void addGroupLdapUsers( AuthGroup group , String[] users ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.addGroupLdapUsers( this , auth , group , users );
	}
	
	public void deleteGroupUsers( AuthGroup group , String[] users ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.deleteGroupUsers( this , auth , group , users );
	}
	
	public void setGroupPermissions( AuthGroup group , AuthRoleSet roles , boolean allResources , String[] resources , boolean allProd , String[] products , boolean allNet , String[] networks , SpecialRights[] special ) throws Exception {
		checkTransactionAuth();
		EngineAuth auth = action.getServerAuth();
		DBEngineAuth.setGroupPermissions( this , auth , group , roles , allResources , resources , allProd , products , allNet , networks , special );
	}
	
	// ################################################################################
	// ################################################################################
	// PRODUCT
	
	public void setProductProperties( Meta meta , PropertySet props , boolean system ) throws Exception {
		ProductMeta metadata = getTransactionProductMetadata( meta );
		MetaProductSettings settings = metadata.getSettings();
		settings.setProperties( props , system );
	}
	
	public void setProductBuildCommonProperties( Meta meta , PropertySet props ) throws Exception {
		ProductMeta metadata = getTransactionProductMetadata( meta );
		MetaProductSettings settings = metadata.getSettings();
		settings.setBuildCommonProperties( props );
	}
	
	public void setProductBuildModeProperties( Meta meta , DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		ProductMeta metadata = getTransactionProductMetadata( meta );
		MetaProductSettings settings = metadata.getSettings();
		settings.setBuildModeProperties( mode , props );
	}

	public MetaProductVersion updateProductVersion( Meta meta , int majorFirstNumber , int majorSecondNumber , int lastProdTag , int lastUrgentTag , int majorNextFirstNumber , int majorNextSecondNumber , int nextProdTag , int nextUrgentTag ) throws Exception {
		ProductMeta metadata = getTransactionProductMetadata( meta );
		MetaProductVersion version = metadata.getVersion();
		version.updateVersion( majorFirstNumber , majorSecondNumber , lastProdTag , lastUrgentTag , majorNextFirstNumber , majorNextSecondNumber , nextProdTag , nextUrgentTag );
		
		MetaProductSettings settings = meta.getProductSettings();
		settings.updateSettings( version );
		
		return( version );
	}

	public void setProductLifecycles( MetaProductPolicy policy , String major , String minor , boolean urgentsAll , String[] urgents ) throws Exception {
		ProductMeta storage = policy.meta.getStorage();
		checkTransactionMetadata( storage );
		DBMetaPolicy.setProductLifecycles( this , storage , policy , major , minor , urgentsAll , urgents );
	}
	
	public void createDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		ProductMeta storage = delivery.meta.getStorage();
		checkTransactionMetadata( storage );
		DBMetaDistr.createDelivery( this , storage , delivery.dist , delivery );
	}
	
	public void modifyDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage() );
		delivery.dist.modifyDelivery( this , delivery );
	}
	
	public void deleteDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage() );
		delivery.dist.deleteDelivery( this , delivery );
	}
	
	public MetaDistrBinaryItem createDistrBinaryItem( MetaDistrDelivery delivery , String key ) throws Exception {
		checkTransactionMetadata( delivery.meta.getStorage() );
		MetaDistrBinaryItem item = new MetaDistrBinaryItem( delivery.meta , delivery );
		item.createBinaryItem( this , key );
		delivery.dist.createDistrBinaryItem( this , delivery , item );
		return( item );
	}
	
	public void modifyDistrBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage() );
		item.delivery.modifyBinaryItem( this , item );
	}

	public void deleteDistrBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage() );
		item.delivery.dist.deleteBinaryItem( this , item );
	}
	
	public void createDistrConfItem( MetaDistrDelivery delivery , MetaDistrConfItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage() );
		delivery.dist.createDistrConfItem( this , delivery , item );
	}
	
	public void modifyDistrConfItem( MetaDistrConfItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage() );
		item.delivery.modifyConfItem( this , item );
	}

	public void deleteDistrConfItem( MetaDistrConfItem item ) throws Exception {
		checkTransactionMetadata( item.meta.getStorage() );
		item.delivery.dist.deleteConfItem( this , item );
	}
	
	public MetaProductUnit createProductUnit( MetaUnits units , String name , String desc ) throws Exception {
		ProductMeta storage = units.meta.getStorage();
		checkTransactionMetadata( storage );
		return( DBMetaUnits.createUnit( this , storage , units , name , desc ) );
	}
	
	public MetaProductDoc createProductDoc( MetaDocs docs , String name , String desc , String ext , boolean unitbound ) throws Exception {
		ProductMeta storage = docs.meta.getStorage();
		checkTransactionMetadata( storage );
		return( DBMetaDocs.createDoc( this , storage , docs , name , desc , ext , unitbound ) );
	}
	
	public void modifyProductDoc( MetaProductDoc doc , String name , String desc , String ext , boolean unitbound ) throws Exception {
		ProductMeta storage = doc.meta.getStorage();
		checkTransactionMetadata( storage );
		DBMetaDocs.modifyDoc( this , storage , doc.docs , doc , name , desc , ext , unitbound );
	}
	
	public MetaDatabaseSchema createDatabaseSchema( MetaDatabase database , String name , String desc , DBEnumDbmsType type , String dbname , String dbuser ) throws Exception {
		ProductMeta storage = database.meta.getStorage();
		checkTransactionMetadata( storage );
		return( DBMetaDatabase.createSchema( this , storage , database , name , desc , type , dbname , dbuser ) );
	}
	
	public void modifyProductUnit( MetaProductUnit unit , String name , String desc ) throws Exception {
		ProductMeta storage = unit.meta.getStorage();
		checkTransactionMetadata( storage );
		DBMetaUnits.modifyUnit( this , storage , unit.units , unit , name , desc );
	}
	
	public void modifyDatabaseSchema( MetaDatabaseSchema schema , String name , String desc , DBEnumDbmsType type , String dbname , String dbuser ) throws Exception {
		ProductMeta storage = schema.meta.getStorage();
		checkTransactionMetadata( storage );
		DBMetaDatabase.modifySchema( this , storage , schema.database , schema , name , desc , type , dbname , dbuser );
	}

	public void deleteProductUnit( MetaProductUnit unit ) throws Exception {
		ProductMeta storage = unit.meta.getStorage();
		checkTransactionMetadata( storage );
		DBMetaUnits.deleteUnit( this , storage , unit.units , unit );
	}
	
	public void deleteProductDoc( MetaProductDoc doc ) throws Exception {
		ProductMeta storage = doc.meta.getStorage();
		checkTransactionMetadata( storage );
		DBMetaDocs.deleteDoc( this , storage , doc.docs , doc );
	}
	
	public void deleteDatabaseSchema( MetaDatabaseSchema schema ) throws Exception {
		ProductMeta storage = schema.meta.getStorage();
		checkTransactionMetadata( storage );
		DBMetaDatabase.deleteSchema( this , storage , schema.database , schema );
	}
	
	public void createDistrComponent( MetaDistrComponent item ) throws Exception {
		ProductMeta storage = item.meta.getStorage();
		checkTransactionMetadata( storage );
		item.dist.createDistrComponent( this , item );
	}
	
	public void modifyDistrComponent( MetaDistrComponent item ) throws Exception {
		ProductMeta storage = item.meta.getStorage();
		checkTransactionMetadata( storage );
		item.dist.modifyDistrComponent( this , item );
	}

	public void deleteDistrComponent( MetaDistrComponent item ) throws Exception {
		ProductMeta storage = item.meta.getStorage();
		checkTransactionMetadata( storage );
		item.dist.deleteDistrComponent( this , item );
	}
	
	public void createDistrComponentItem( MetaDistrComponent comp , MetaDistrComponentItem item ) throws Exception {
		ProductMeta storage = item.meta.getStorage();
		checkTransactionMetadata( storage );
		item.comp.createItem( this , item );
	}
	
	public void modifyDistrComponentItem( MetaDistrComponentItem item ) throws Exception {
		ProductMeta storage = item.meta.getStorage();
		checkTransactionMetadata( storage );
		item.comp.modifyItem( this , item );
	}

	public void deleteDistrComponentItem( MetaDistrComponentItem item ) throws Exception {
		ProductMeta storage = item.meta.getStorage();
		checkTransactionMetadata( storage );
		item.comp.deleteItem( this , item );
	}

	public void createDistrComponentService( MetaDistrComponent comp , MetaDistrComponentWS service ) throws Exception {
		ProductMeta storage = comp.meta.getStorage();
		checkTransactionMetadata( storage );
		service.comp.createWebService( this , service );
	}
	
	public void modifyDistrComponentService( MetaDistrComponentWS service ) throws Exception {
		ProductMeta storage = service.meta.getStorage();
		checkTransactionMetadata( storage );
		service.comp.modifyWebService( this , service );
	}

	public void deleteDistrComponentService( MetaDistrComponentWS service ) throws Exception {
		ProductMeta storage = service.meta.getStorage();
		checkTransactionMetadata( storage );
		service.comp.deleteWebService( this , service );
	}

	public void setDeliveryDatabaseAll( MetaDistrDelivery delivery ) throws Exception {
		ProductMeta storage = delivery.meta.getStorage();
		checkTransactionMetadata( storage );
		delivery.setDatabaseAll( this );
	}

	public void setDeliveryDatabaseSet( MetaDistrDelivery delivery , MetaDatabaseSchema[] set ) throws Exception {
		ProductMeta storage = delivery.meta.getStorage();
		checkTransactionMetadata( storage );
		delivery.setDatabaseSet( this , set );
	}

	public void setDeliveryDocumentationSet( MetaDistrDelivery delivery , MetaProductDoc[] set ) throws Exception {
		ProductMeta storage = delivery.meta.getStorage();
		checkTransactionMetadata( storage );
		delivery.setDocSet( this , set );
	}

	public MetaSourceProjectSet createSourceProjectSet( MetaSources sources , String name , String desc ) throws Exception {
		ProductMeta storage = sources.meta.getStorage();
		checkTransactionMetadata( storage );
		return( DBMetaSources.createProjectSet( this , storage , sources , name , desc ) );
	}

	public MetaSourceProject createSourceProject( MetaSourceProjectSet set , 
			String name , String desc , int pos , Integer unit , boolean prod , 
			DBEnumProjectType type , String tracker , Integer repoRes , String repoName , String repoPath , String codePath ,
			Integer builder , String addOptions , String branch ,
			boolean customBuild , boolean customGet ) throws Exception {
		ProductMeta storage = set.meta.getStorage();
		checkTransactionMetadata( storage );
		
		MetaSources sources = storage.getSources();
		return( DBMetaSources.createProject( this , storage , sources , set , 
				name , desc , pos , unit , prod , 
				type , tracker , repoRes , repoName , repoPath , codePath ,
				builder , addOptions , branch ,
				customBuild , customGet ) );
	}

	public void changeProjectSet( MetaSourceProject project , MetaSourceProjectSet setNew , int posNew ) throws Exception {
		ProductMeta storage = project.meta.getStorage();
		checkTransactionMetadata( storage );
		
		MetaSources sources = storage.getSources();
		DBMetaSources.changeProjectSet( this , storage , sources , project , setNew , posNew );
	}

	public void changeProjectOrder( MetaSourceProject project , int pos ) throws Exception {
		ProductMeta storage = project.meta.getStorage();
		checkTransactionMetadata( storage );
		
		MetaSources sources = storage.getSources();
		DBMetaSources.changeProjectOrder( this , storage , sources , project , pos );
	}

	public void modifySourceProject( MetaSourceProject project , 
			String name , String desc , int pos , Integer unit , boolean prod , 
			DBEnumProjectType type , String tracker , Integer repoRes , String repoName , String repoPath , String codePath ,
			Integer builder , String addOptions , String branch ,
			boolean customBuild , boolean customGet ) throws Exception {
		ProductMeta storage = project.meta.getStorage();
		checkTransactionMetadata( storage );
		
		MetaSources sources = storage.getSources();
		DBMetaSources.modifyProject( this , storage , sources , project , 
				name , desc , pos , unit , prod , 
				type , tracker , repoRes , repoName , repoPath , codePath ,
				builder , addOptions , branch ,
				customBuild , customGet );
	}

	public void changeProjectSetOrder( MetaSourceProjectSet set , String[] namesOrdered ) throws Exception {
		ProductMeta storage = set.meta.getStorage();
		checkTransactionMetadata( storage );
		
		MetaSources sources = storage.getSources();
		DBMetaSources.modifySetOrder( this , storage , sources , set , namesOrdered );
	}
	
	public MetaSourceProjectItem createSourceProjectItem( MetaSourceProject project , 
			String name , String desc ,  
			DBEnumSourceItemType srcType , String basename , String ext , String staticext , String path , String version , boolean internal ) throws Exception {
		ProductMeta storage = project.meta.getStorage();
		checkTransactionMetadata( storage );
		
		MetaSources sources = storage.getSources();
		return( DBMetaSources.createProjectItem ( this , storage , sources , project , name , desc , srcType , basename , ext , staticext , path , version , internal ) );
	}
	
	public void modifySourceProjectItem( MetaSourceProjectItem item , 
			String name , String desc ,  
			DBEnumSourceItemType srcType , String basename , String ext , String staticext , String path , String version , boolean internal ) throws Exception {
		ProductMeta storage = item.meta.getStorage();
		checkTransactionMetadata( storage );
		
		MetaSources sources = storage.getSources();
		DBMetaSources.modifyProjectItem ( this , storage , sources , item , name , desc , srcType , basename , ext , staticext , path , version , internal );
	}
	
	public void deleteSourceProjectItem( MetaSourceProjectItem item , boolean leaveManual ) throws Exception {
		ProductMeta storage = item.meta.getStorage();
		checkTransactionMetadata( storage );
		
		MetaDistrBinaryItem distItem = item.distItem;
		if( distItem != null ) {
			MetaDistr distr = distItem.delivery.dist;
			if( leaveManual )
				distr.changeBinaryItemProjectToManual( this , distItem );
			else
				distr.deleteBinaryItem( this , distItem );
		}
		
		MetaSources sources = storage.getSources();
		DBMetaSources.deleteProjectItem( this , storage , sources , item );
	}

	// ################################################################################
	// ################################################################################
	// ENVIRONMENT
	
	public MetaEnv createMetaEnv( Meta meta , String name , VarENVTYPE envType ) throws Exception {
		ProductMeta metadata = getTransactionProductMetadata( meta );
		MetaProductSettings settings = meta.getProductSettings();
		MetaEnv env = new MetaEnv( metadata , settings , metadata.meta );
		action.trace( "create meta env object, id=" + env.objectId );
		env.createEnv( action , name , envType );
		metadata.addEnv( env );
		return( env );
	}
	
	public void deleteMetaEnv( MetaEnv env ) throws Exception {
		ProductMeta metadata = getTransactionProductMetadata( env.meta );
		metadata.deleteEnv( this , env );
		env.deleteObject();
	}

	public void setMetaEnvProperties( MetaEnv env , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( env.meta.getStorage() );
		env.setProperties( this , props , system );
	}
	
	public void updateMetaEnv( MetaEnv env ) throws Exception {
		checkTransactionMetadata( env.meta.getStorage() );
		env.updateProperties( this );
	}
	
	public void updateMetaEnvSegment( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage() );
		sg.updateProperties( this );
	}
	
	public void createMetaEnvSegment( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage() );
		sg.env.createSegment( this , sg );
	}
	
	public void deleteMetaEnvSegment( MetaEnvSegment sg ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage() );
		sg.env.deleteSegment( this , sg );
		sg.deleteObject();
	}

	public void setMetaEnvSGProperties( MetaEnvSegment sg , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage() );
		sg.setProperties( this , props , system );
	}
	
	public MetaEnvServer createMetaEnvServer( MetaEnvSegment sg , String name , String desc , DBEnumOSType osType , VarSERVERRUNTYPE runType , DBEnumServerAccessType accessType , String sysname ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage() );
		MetaEnvServer server = new MetaEnvServer( sg.meta , sg );
		server.createServer( action , name , desc , osType , runType , accessType , sysname );
		sg.createServer( this , server );
		return( server );
	}
	
	public void modifyMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage() );
		server.sg.modifyServer( this , server );
	}

	public void deleteMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage() );
		server.sg.deleteServer( this , server );
		server.deleteObject();
	}

	public void updateMetaEnvServer( MetaEnvServer server ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage() );
		server.updateProperties( this );
	}

	public void setMetaEnvServerProperties( MetaEnvServer server , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage() );
		server.setProperties( this , props , system );
	}
	
	public MetaEnvServerNode createMetaEnvServerNode( MetaEnvServer server , int pos , VarNODETYPE nodeType , Account account ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage() );
		MetaEnvServerNode node = new MetaEnvServerNode( server.meta , server , pos );
		node.createNode( action , nodeType , account );
		server.createNode( this , node );
		return( node );
	}
	
	public void modifyMetaEnvServerNode( MetaEnvServerNode node , int pos , VarNODETYPE nodeType , Account account ) throws Exception {
		checkTransactionMetadata( node.meta.getStorage() );
		node.updateProperties( this );
		node.modifyNode( action , pos , nodeType , account );
		node.server.modifyNode( this , node );
	}

	public void updateMetaEnvServerNodeSetOffline( MetaEnvServerNode node , boolean newStatus ) throws Exception {
		node.setOffline( this , newStatus );
	}
	
	public void deleteMetaEnvServerNode( MetaEnvServerNode node ) throws Exception {
		checkTransactionMetadata( node.meta.getStorage() );
		node.server.deleteNode( this , node );
		node.deleteObject();
	}

	public void setMetaEnvServerNodeProperties( MetaEnvServerNode node , PropertySet props , boolean system ) throws Exception {
		checkTransactionMetadata( node.meta.getStorage() );
		node.setProperties( this , props , system );
	}
	
	public void setStartInfo( MetaEnvSegment sg , MetaEnvStartInfo startInfo ) throws Exception {
		checkTransactionMetadata( sg.meta.getStorage() );
		sg.setStartInfo( this , startInfo );
	}

	public void modifyServerDeployments( MetaEnvServer server , List<MetaEnvServerDeployment> deployments ) throws Exception {
		checkTransactionMetadata( server.meta.getStorage() );
		server.setDeployments( this , deployments );
	}

	public MetaDump createDump( MetaDatabase db , boolean export , String name , String desc , MetaEnvServer server , boolean standby , String setdbenv , String dataset , String dumpdir , String datapumpdir , boolean nfs , String postRefresh ) throws Exception {
		checkTransactionMetadata( db.meta.getStorage() );
		MetaDump dump = new MetaDump( db.meta , db );
		dump.create( name , desc , export );
		dump.setTarget( server , standby , setdbenv );
		dump.setFiles( dataset , dumpdir , datapumpdir , nfs , postRefresh );
		//db.createDump( this , dump );
		return( dump );
	}
	
	public void modifyDump( MetaDump dump , String name , String desc , MetaEnvServer server , boolean standby , String setdbenv , String dataset , String dumpdir , String datapumpdir , boolean nfs , String postRefresh ) throws Exception {
		checkTransactionMetadata( dump.database.meta.getStorage() );
		dump.modify( name , desc );
		dump.setTarget( server , standby , setdbenv );
		dump.setFiles( dataset , dumpdir , datapumpdir , nfs , postRefresh );
		dump.database.updateDump( dump );
	}
	
	public void deleteDump( MetaDump dump ) throws Exception {
		checkTransactionMetadata( dump.database.meta.getStorage() );
		//dump.database.deleteDump( this , dump );
	}

	public void createDumpTables( MetaDump dump , String schema , String tables ) throws Exception {
		checkTransactionMetadata( dump.database.meta.getStorage() );
		dump.addTables( schema , tables );
	}
	
	public void deleteDumpTables( MetaDump dump , int index ) throws Exception {
		checkTransactionMetadata( dump.database.meta.getStorage() );
		dump.deleteTables( index );
	}

	public void setDumpOnline( MetaDump dump , boolean online ) throws Exception {
		checkTransactionMetadata( dump.database.meta.getStorage() );
		dump.setOnline( online );
	}

	public void setDumpSchedule( MetaDump dump , ScheduleProperties schedule ) throws Exception {
		checkTransactionMetadata( dump.database.meta.getStorage() );
		dump.setSchedule( schedule );
	}

}
