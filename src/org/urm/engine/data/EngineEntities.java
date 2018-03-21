package org.urm.engine.data;

import org.urm.db.DBConnection;
import org.urm.db.core.DBVersions;

import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineAuth;
import org.urm.db.engine.DBEngineBase;
import org.urm.db.engine.DBEngineBuilders;
import org.urm.db.engine.DBEngineContext;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.engine.DBEngineInfrastructure;
import org.urm.db.engine.DBEngineLifecycles;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.db.engine.DBEngineMonitoring;
import org.urm.db.engine.DBEngineResources;
import org.urm.db.env.DBEnvData;
import org.urm.db.product.DBProductData;
import org.urm.db.release.DBReleaseData;
import org.urm.engine.Engine;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.BaseItem;

public class EngineEntities {

	public static String nameRunContextSet = "execrc";
	public static String nameEngineSettings = "engine";
	public static String nameProductSet = "product.defaults";
	public static String nameBuildSet = "build";
	public static String nameBuildBranchSet = "build.branch";
	public static String nameBuildDevBranchSet = "build.devbranch";
	public static String nameBuildDevTrunkSet = "build.devtrunk";
	public static String nameBuildMajorBranchSet = "build.majorbranch";
	public static String nameBuildTrunkSet = "build.trunk";
	public static String nameEngineMonitoring = "defmon";
	public static String nameBaseItem = "baseitem";
	public static String nameBaseItemDependency = "depitem";
	public static String nameSystem = "system";
	public static String nameLdap = "ldap";
	public static String nameProductContext = "ctx";
	public static String nameMetaProductSet = "product";
	public static String nameMetaMonitoringSet = "mon";
	public static String nameMetaEnvSet = "env";
	public static String nameMetaEnvSegmentSet = "sg";
	public static String nameMetaEnvServerSet = "server";
	public static String nameMetaEnvServerBaseSet = "base";
	public static String nameMetaEnvServerNodeSet = "node";

	public Engine engine;

	public PropertyEntity entityAppRC; 
	public PropertyEntity entityCustomRC;
	public PropertyEntity entityAppEngine;
	public PropertyEntity entityCustomEngine;
	public PropertyEntity entityAppProductContext;
	public PropertyEntity entityAppProductSettings;
	public PropertyEntity entityAppProductBuild;
	public PropertyEntity entityAppEngineMonitoring;
	public PropertyEntity entityAppBaseGroup;
	public PropertyEntity entityAppBaseItem;
	public PropertyEntity entityAppDirectorySystem;
	public PropertyEntity entityAppDirectoryProduct;
	public PropertyEntity entityAppDatacenter;
	public PropertyEntity entityAppNetwork;
	public PropertyEntity entityAppNetworkHost;
	public PropertyEntity entityAppHostAccount;
	public PropertyEntity entityAppReleaseLifecycle;
	public PropertyEntity entityAppLifecyclePhase;
	public PropertyEntity entityAppResource;
	public PropertyEntity entityAppMirror;
	public PropertyEntity entityAppProjectBuilder;
	public PropertyEntity entityAppLDAPSettings;
	public PropertyEntity entityAppAuthUser;
	public PropertyEntity entityAppAuthGroup;
	public PropertyEntity entityAppMeta;
	public PropertyEntity entityAppMetaVersion;
	public PropertyEntity entityAppMetaMonitoring;
	public PropertyEntity entityAppMetaUnit;
	public PropertyEntity entityAppMetaSchema;
	public PropertyEntity entityAppMetaSourceSet;
	public PropertyEntity entityAppMetaSourceProject;
	public PropertyEntity entityAppMetaSourceItem;
	public PropertyEntity entityAppMetaDoc;
	public PropertyEntity entityAppMetaPolicy;
	public PropertyEntity entityAppMetaDistrDelivery;
	public PropertyEntity entityAppMetaDistrBinaryItem;
	public PropertyEntity entityAppMetaDistrConfItem;
	public PropertyEntity entityAppMetaDistrComponent;
	public PropertyEntity entityAppMetaDistrCompItem;
	public PropertyEntity entityAppEnvPrimary;
	public PropertyEntity entityAppEnvExtra;
	public PropertyEntity entityAppSegmentPrimary;
	public PropertyEntity entityAppSegmentStartGroup;
	public PropertyEntity entityAppSegmentMonTarget;
	public PropertyEntity entityAppSegmentMonItem;
	public PropertyEntity entityAppServerPrimary;
	public PropertyEntity entityAppServerExtra;
	public PropertyEntity entityAppServerDeployment;
	public PropertyEntity entityAppNodePrimary;
	public PropertyEntity entityAppReleaseRepository;
	public PropertyEntity entityAppReleaseMain;
	public PropertyEntity entityAppReleaseDist;
	public PropertyEntity entityAppReleaseTarget;
	public PropertyEntity entityAppReleaseScopeSet;
	public PropertyEntity entityAppReleaseScopeTarget;
	public PropertyEntity entityAppReleaseScopeItem;
	public PropertyEntity entityAppReleaseSchedule;
	public PropertyEntity entityAppReleasePhase;
	public PropertyEntity entityAppReleaseTicketSet;
	public PropertyEntity entityAppReleaseTicketTarget;
	public PropertyEntity entityAppReleaseTicket;
	
	public EngineEntities( Engine engine ) {
		this.engine = engine;
	}
	
	public void upgradeMeta( EngineLoader loader ) throws Exception {
		entityAppRC = DBEngineContext.upgradeEntityRC( loader );
		entityAppEngine = DBEngineContext.upgradeEntityEngine( loader );
		entityAppProductContext = DBProductData.upgradeEntityProductContext( loader );
		entityAppProductSettings = DBProductData.upgradeEntityProductSettings( loader );
		entityAppProductBuild = DBProductData.upgradeEntityProductBuild( loader );
		entityAppEngineMonitoring = DBEngineMonitoring.upgradeEntityEngineMonitoring( loader );
		entityAppBaseGroup = DBEngineBase.upgradeEntityBaseGroup( loader );
		entityAppBaseItem = DBEngineBase.upgradeEntityBaseItem( loader );
		entityAppDirectorySystem = DBEngineDirectory.upgradeEntityDirectorySystem( loader );
		entityAppDirectoryProduct = DBEngineDirectory.upgradeEntityDirectoryProduct( loader );
		entityAppDatacenter = DBEngineInfrastructure.upgradeEntityDatacenter( loader );
		entityAppNetwork = DBEngineInfrastructure.upgradeEntityNetwork( loader );
		entityAppNetworkHost = DBEngineInfrastructure.upgradeEntityNetworkHost( loader );
		entityAppHostAccount = DBEngineInfrastructure.upgradeEntityHostAccount( loader );
		entityAppReleaseLifecycle = DBEngineLifecycles.upgradeEntityReleaseLifecycle( loader );
		entityAppLifecyclePhase = DBEngineLifecycles.upgradeEntityLifecyclePhase( loader );
		entityAppResource = DBEngineResources.upgradeEntityResource( loader );
		entityAppMirror = DBEngineMirrors.upgradeEntityMirror( loader );
		entityAppProjectBuilder = DBEngineBuilders.upgradeEntityBuilder( loader );
		entityAppLDAPSettings = DBEngineAuth.upgradeEntityLDAPSettings( loader );
		entityAppAuthUser = DBEngineAuth.upgradeEntityAuthUser( loader );
		entityAppAuthGroup = DBEngineAuth.upgradeEntityAuthGroup( loader );
		entityAppMeta = DBProductData.upgradeEntityMeta( loader );
		entityAppMetaVersion = DBProductData.upgradeEntityMetaVersion( loader );
		entityAppMetaMonitoring = DBProductData.upgradeEntityMetaMonitoring( loader );
		entityAppMetaUnit = DBProductData.upgradeEntityMetaUnit( loader );
		entityAppMetaSchema = DBProductData.upgradeEntityMetaSchema( loader );
		entityAppMetaSourceSet = DBProductData.upgradeEntityMetaSourceSet( loader );
		entityAppMetaSourceProject = DBProductData.upgradeEntityMetaSourceProject( loader );
		entityAppMetaSourceItem = DBProductData.upgradeEntityMetaSourceItem( loader );
		entityAppMetaDoc = DBProductData.upgradeEntityMetaDoc( loader );
		entityAppMetaPolicy = DBProductData.upgradeEntityMetaPolicy( loader );
		entityAppMetaDistrDelivery = DBProductData.upgradeEntityMetaDistrDelivery( loader );
		entityAppMetaDistrBinaryItem = DBProductData.upgradeEntityMetaDistrBinaryItem( loader );
		entityAppMetaDistrConfItem = DBProductData.upgradeEntityMetaDistrConfItem( loader );
		entityAppMetaDistrComponent = DBProductData.upgradeEntityMetaDistrComponent( loader );
		entityAppMetaDistrCompItem = DBProductData.upgradeEntityMetaDistrCompItem( loader );
		entityAppEnvPrimary = DBEnvData.upgradeEntityEnvPrimary( loader );
		entityAppEnvExtra = DBEnvData.upgradeEntityEnvExtra( loader );
		entityAppSegmentPrimary = DBEnvData.upgradeEntitySegmentPrimary( loader );
		entityAppSegmentStartGroup = DBEnvData.upgradeEntityStartGroup( loader );
		entityAppSegmentMonTarget = DBEnvData.upgradeEntityMonitoringTarget( loader );
		entityAppSegmentMonItem = DBEnvData.upgradeEntityMonitoringItem( loader );
		entityAppServerPrimary = DBEnvData.upgradeEntityServerPrimary( loader );
		entityAppServerExtra = DBEnvData.upgradeEntityServerExtra( loader );
		entityAppServerDeployment = DBEnvData.upgradeEntityServerDeployment( loader );
		entityAppNodePrimary = DBEnvData.upgradeEntityNodePrimary( loader );
		entityAppReleaseRepository = DBReleaseData.upgradeEntityReleaseRepository( loader );
		entityAppReleaseMain = DBReleaseData.upgradeEntityReleaseMain( loader );
		entityAppReleaseDist = DBReleaseData.upgradeEntityReleaseDist( loader );
		entityAppReleaseTarget = DBReleaseData.upgradeEntityReleaseTarget( loader );
		entityAppReleaseScopeSet = DBReleaseData.upgradeEntityReleaseScopeSet( loader );
		entityAppReleaseScopeTarget = DBReleaseData.upgradeEntityReleaseScopeTarget( loader );
		entityAppReleaseScopeItem = DBReleaseData.upgradeEntityReleaseScopeItem( loader );
		entityAppReleaseSchedule = DBReleaseData.upgradeEntityReleaseSchedule( loader );
		entityAppReleasePhase = DBReleaseData.upgradeEntityReleasePhase( loader );
		entityAppReleaseTicketSet = DBReleaseData.upgradeEntityReleaseTicketSet( loader );
		entityAppReleaseTicketTarget = DBReleaseData.upgradeEntityReleaseTicketTarget( loader );
		entityAppReleaseTicket = DBReleaseData.upgradeEntityReleaseTicket( loader );
		
		entityCustomRC = DBEngineContext.createEntityCustomRC( loader );
		entityCustomEngine = DBEngineContext.createEntityCustomEngine( loader );
	}
	
	public void useMeta( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		entityAppRC = DBEngineContext.loaddbEntityRC( c );
		entityAppEngine = DBEngineContext.loaddbEntityEngine( c );
		entityAppProductContext = DBProductData.loaddbEntityProductContext( c );
		entityAppProductSettings = DBProductData.loaddbEntityProductSettings( c );
		entityAppProductBuild = DBProductData.loaddbEntityProductBuild( c );
		entityAppEngineMonitoring = DBEngineMonitoring.loaddbEntityEngineMonitoring( c );
		entityAppBaseGroup = DBEngineBase.loaddbEntityBaseGroup( c );
		entityAppBaseItem = DBEngineBase.loaddbEntityBaseItem( c );
		entityAppDirectorySystem = DBEngineDirectory.loaddbEntityDirectorySystem( c );
		entityAppDirectoryProduct = DBEngineDirectory.loaddbEntityDirectoryProduct( c );
		entityAppDatacenter = DBEngineInfrastructure.loaddbEntityDatacenter( c );
		entityAppNetwork = DBEngineInfrastructure.loaddbEntityNetwork( c );
		entityAppNetworkHost = DBEngineInfrastructure.loaddbEntityNetworkHost( c );
		entityAppHostAccount = DBEngineInfrastructure.loaddbEntityHostAccount( c );
		entityAppReleaseLifecycle = DBEngineLifecycles.loaddbEntityReleaseLifecycle( c );
		entityAppLifecyclePhase = DBEngineLifecycles.loaddbEntityLifecyclePhase( c );
		entityAppResource = DBEngineResources.loaddbEntityResource( c );
		entityAppMirror = DBEngineMirrors.loaddbEntityMirror( c );
		entityAppProjectBuilder = DBEngineBuilders.loaddbEntityBuilder( c );
		entityAppLDAPSettings = DBEngineAuth.loaddbEntityLDAPSettings( c );
		entityAppAuthUser = DBEngineAuth.loaddbEntityAuthUser( c );
		entityAppAuthGroup = DBEngineAuth.loaddbEntityAuthGroup( c );
		entityAppMeta = DBProductData.loaddbEntityMeta( c );
		entityAppMetaVersion = DBProductData.loaddbEntityMetaVersion( c );
		entityAppMetaMonitoring = DBProductData.loaddbEntityMetaMonitoring( c );
		entityAppMetaUnit = DBProductData.loaddbEntityMetaUnit( c );
		entityAppMetaSchema = DBProductData.loaddbEntityMetaSchema( c );
		entityAppMetaSourceSet = DBProductData.loaddbEntityMetaSourceSet( c );
		entityAppMetaSourceProject = DBProductData.loaddbEntityMetaSourceProject( c );
		entityAppMetaSourceItem = DBProductData.loaddbEntityMetaSourceItem( c );
		entityAppMetaDoc = DBProductData.loaddbEntityMetaDoc( c );
		entityAppMetaPolicy = DBProductData.loaddbEntityMetaPolicy( c );
		entityAppMetaDistrDelivery = DBProductData.loaddbEntityMetaDistrDelivery( c );
		entityAppMetaDistrBinaryItem = DBProductData.loaddbEntityMetaDistrBinaryItem( c );
		entityAppMetaDistrConfItem = DBProductData.loaddbEntityMetaDistrConfItem( c );
		entityAppMetaDistrComponent = DBProductData.loaddbEntityMetaDistrComponent( c );
		entityAppMetaDistrCompItem = DBProductData.loaddbEntityMetaDistrCompItem( c );
		entityAppEnvPrimary = DBEnvData.loaddbEntityEnvPrimary( c );
		entityAppEnvExtra = DBEnvData.loaddbEntityEnvExtra( c );
		entityAppSegmentPrimary = DBEnvData.loaddbEntitySegmentPrimary( c );
		entityAppSegmentStartGroup = DBEnvData.loaddbEntityStartGroup( c );
		entityAppSegmentMonTarget = DBEnvData.loaddbEntityMonitoringTarget( c );
		entityAppSegmentMonItem = DBEnvData.loaddbEntityMonitoringItem( c );
		entityAppServerPrimary = DBEnvData.loaddbEntityServerPrimary( c );
		entityAppServerExtra = DBEnvData.loaddbEntityServerExtra( c );
		entityAppServerDeployment = DBEnvData.loaddbEntityServerDeployment( c );
		entityAppNodePrimary = DBEnvData.loaddbEntityNodePrimary( c );
		entityAppReleaseRepository = DBReleaseData.loaddbEntityReleaseRepository( c );
		entityAppReleaseMain = DBReleaseData.loaddbEntityReleaseMain( c );
		entityAppReleaseDist = DBReleaseData.loaddbEntityReleaseDist( c );
		entityAppReleaseTarget = DBReleaseData.loaddbEntityReleaseTarget( c );
		entityAppReleaseScopeSet = DBReleaseData.loaddbEntityReleaseScopeSet( c );
		entityAppReleaseScopeTarget = DBReleaseData.loaddbEntityReleaseScopeTarget( c );
		entityAppReleaseScopeItem = DBReleaseData.loaddbEntityReleaseScopeItem( c );
		entityAppReleaseSchedule = DBReleaseData.loaddbEntityReleaseSchedule( c );
		entityAppReleasePhase = DBReleaseData.loaddbEntityReleasePhase( c );
		entityAppReleaseTicketSet = DBReleaseData.loaddbEntityReleaseTicketSet( c );
		entityAppReleaseTicketTarget = DBReleaseData.loaddbEntityReleaseTicketTarget( c );
		entityAppReleaseTicket = DBReleaseData.loaddbEntityReleaseTicket( c );
		
		entityCustomRC = DBEngineContext.loaddbEntityCustomRC( c );
		entityCustomEngine = DBEngineContext.loaddbEntityCustomEngine( c );
	}

	public void updateEntity( PropertyEntity entity ) {
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.RC_CUSTOM )
			entityCustomRC = entity;
		else
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.ENGINE_CUSTOM )
			entityCustomEngine = entity;
	}
	
	public ObjectProperties createRunContextProps() throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.RC , nameRunContextSet , engine.execrc );
		props.create( null , entityAppRC , entityCustomRC , true );
		props.createCustom();
		props.setOwnerId( DBVersions.LOCAL_ID );
		return( props );
	}

	public ObjectProperties createEngineProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.ENGINE , nameEngineSettings , engine.execrc );
		props.create( parent , entityAppEngine , entityCustomEngine , true );
		props.createCustom();
		props.setOwnerId( DBVersions.CORE_ID );
		return( props );
	}

	public ObjectProperties createDefaultProductProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.PRODUCTDEFS , nameProductSet , engine.execrc );
		props.create( parent , new PropertyEntity[] { entityAppProductContext , entityAppProductSettings } , null , false );
		props.setOwnerId( DBVersions.CORE_ID );
		return( props );
	}

	public ObjectProperties createEngineMonitoringProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.MONITORING , nameEngineMonitoring , engine.execrc );
		props.create( parent , entityAppEngineMonitoring , null , false ); 
		props.setOwnerId( DBVersions.CORE_ID );
		return( props );
	}

	public ObjectProperties createLdapProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.LDAP , nameLdap , engine.execrc );
		props.create( parent , entityAppLDAPSettings , null , false );
		props.setOwnerId( DBVersions.CORE_ID );
		return( props );
	}

	public ObjectProperties createDefaultBuildCommonProps( ObjectProperties parent ) throws Exception {
		ObjectProperties ops = createBuildCommonProps( parent , DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , true );
		return( ops );
	}
	
	public ObjectProperties createDefaultBuildModeProps( ObjectProperties parent , DBEnumBuildModeType mode ) throws Exception {
		return( createBuildModeProps( parent , DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , mode , true ) );
	}
	
	private ObjectProperties createBuildCommonProps( ObjectProperties parent , DBEnumObjectType objectType , DBEnumObjectVersionType versionType , boolean defaults ) throws Exception {
		String set = nameBuildSet;
		if( defaults )
			set += ".defaults";
		
		ObjectProperties props = new ObjectProperties( objectType , versionType , DBEnumParamRoleType.BUILDMODE_COMMON , set , engine.execrc );
		props.create( parent , entityAppProductBuild , null , false );
		props.setOwnerId( parent.ownerId );
		return( props );
	}

	private ObjectProperties createBuildModeProps( ObjectProperties parent , DBEnumObjectType objectType , DBEnumObjectVersionType versionType , DBEnumBuildModeType mode , boolean defaults ) throws Exception {
		DBEnumParamRoleType role = null;
		String set = null;
		if( mode == DBEnumBuildModeType.BRANCH ) {
			role = DBEnumParamRoleType.BUILDMODE_BRANCH;
			set = nameBuildBranchSet;
		}
		else
		if( mode == DBEnumBuildModeType.DEVBRANCH ) {
			role = DBEnumParamRoleType.BUILDMODE_DEVBRANCH;
			set = nameBuildDevBranchSet;
		}
		else
		if( mode == DBEnumBuildModeType.DEVTRUNK ) {
			role = DBEnumParamRoleType.BUILDMODE_DEVTRUNK;
			set = nameBuildDevTrunkSet;
		}
		else
		if( mode == DBEnumBuildModeType.MAJORBRANCH ) {
			role = DBEnumParamRoleType.BUILDMODE_MAJORBRANCH;
			set = nameBuildMajorBranchSet;
		}
		else
		if( mode == DBEnumBuildModeType.TRUNK ) {
			role = DBEnumParamRoleType.BUILDMODE_TRUNK;
			set = nameBuildTrunkSet;
		}
		
		if( defaults )
			set += ".defaults";
		
		ObjectProperties props = new ObjectProperties( objectType , versionType , role , set , engine.execrc );
		props.create( parent , entityAppProductBuild , null , false ); 
		props.setOwnerId( parent.ownerId );
		return( props );
	}

	public ObjectProperties createBaseItemProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.DEFAULT , nameBaseItem , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.BASE_ITEM , DBEnumParamEntityType.BASEITEM_CUSTOM , DBVersions.CORE_ID , DBEnumObjectVersionType.CORE );
		props.create( parent , entityAppBaseItem , custom , true );
		return( props );
	}

	public ObjectProperties createSystemProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.SYSTEM , DBEnumParamRoleType.DEFAULT , nameSystem , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.APPSYSTEM , DBEnumParamEntityType.SYSTEM_CUSTOM , -1 , DBEnumObjectVersionType.SYSTEM );
		props.create( parent , entityAppDirectorySystem , custom , true );
		return( props );
	}

	public ObjectProperties createMetaMonitoringProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , DBEnumParamRoleType.METAMON , nameMetaMonitoringSet , engine.execrc );
		props.create( parent , entityAppMetaMonitoring , null , false );
		props.setOwnerId( parent.ownerId );
		return( props );
	}
	
	public ObjectProperties createMetaProductProps( int metaId , ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , DBEnumParamRoleType.DEFAULT , nameMetaProductSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_CUSTOM , -1 , DBEnumObjectVersionType.PRODUCT ); 
		props.create( parent , new PropertyEntity[] { entityAppProductContext , entityAppProductSettings } , custom , true );
		props.setOwnerId( metaId );
		return( props );
	}
	
	public ObjectProperties createMetaBuildCommonProps( ObjectProperties parent ) throws Exception {
		ObjectProperties ops = createBuildCommonProps( parent , DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , false );
		return( ops );
	}

	public ObjectProperties createMetaBuildModeProps( ObjectProperties parent , DBEnumBuildModeType mode ) throws Exception {
		return( createBuildModeProps( parent , DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , mode , false ) );
	}
	
	public ObjectProperties createMetaEnvProps( int envId , ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.DEFAULT , nameMetaEnvSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppEnvPrimary , entityAppEnvExtra } , custom , false );
		props.setOwnerId( envId );
		return( props );
	}
	
	public ObjectProperties createMetaEnvSegmentProps( int sgId , ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT_SEGMENT , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.DEFAULT , nameMetaEnvSegmentSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_SEGMENT_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppSegmentPrimary } , custom , false );
		props.setOwnerId( sgId );
		return( props );
	}
	
	public ObjectProperties createMetaEnvServerProps( int serverId , ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.DEFAULT , nameMetaEnvServerSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_SERVER_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppServerPrimary , entityAppServerExtra } , custom , false );
		props.setOwnerId( serverId );
		return( props );
	}
	
	public ObjectProperties createMetaEnvServerBaseProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.SERVERBASE , nameMetaEnvServerBaseSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_SERVER_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppBaseItem } , custom , false );
		props.setOwnerId( parent.ownerId );
		return( props );
	}
	
	public ObjectProperties createBaseItemDependencyProps( BaseItem item , BaseItem dep ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.BASE_ITEM , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.BASEITEM_DEPENDENCY , nameBaseItemDependency , engine.execrc );
		ObjectProperties depops = dep.getParameters();
		ObjectMeta meta = depops.getMeta();
		PropertyEntity custom = meta.getCustomEntity();
		props.create( item.getParameters() , new PropertyEntity[] {} , custom , true );
		props.setOwnerId( item.ID );
		return( props );
	}
	
	public ObjectProperties createMetaEnvServerNodeProps( int nodeId , ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT_NODE , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.DEFAULT , nameMetaEnvServerNodeSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_NODE_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppNodePrimary } , custom , false );
		props.setOwnerId( nodeId );
		return( props );
	}

	public boolean isRunContext( ObjectProperties ops ) {
		if( nameRunContextSet.equals( ops.getName() ) )
			return( true );
		return( false );
	}
	
	public boolean isEngineCore( ObjectProperties ops ) {
		if( nameEngineSettings.equals( ops.getName() ) )
			return( true );
		return( false );
	}
	
	public boolean isSystemCore( ObjectProperties ops ) {
		if( nameSystem.equals( ops.getName() ) )
			return( true );
		return( false );
	}
	
	public boolean isProductCore( ObjectProperties ops ) {
		if( nameMetaProductSet.equals( ops.getName() ) )
			return( true );
		return( false );
	}
	
}
