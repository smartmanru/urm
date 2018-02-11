package org.urm.engine.properties;

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
import org.urm.engine.Engine;
import org.urm.meta.EngineLoader;

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
		props.create( null , entityAppRC , entityCustomRC );
		return( props );
	}

	public ObjectProperties createEngineProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.ENGINE , nameEngineSettings , engine.execrc );
		props.create( parent , entityAppEngine , entityCustomEngine ); 
		return( props );
	}

	public ObjectProperties createDefaultProductProps( ObjectProperties parent ) throws Exception {
		return( createProductProps( parent , DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE ) );
	}
	
	public ObjectProperties createProductProps( ObjectProperties parent , DBEnumObjectType objectType , DBEnumObjectVersionType versionType ) throws Exception {
		ObjectProperties props = new ObjectProperties( objectType , versionType , DBEnumParamRoleType.PRODUCTDEFS , nameProductSet , engine.execrc );
		props.create( parent , entityAppProductSettings , null ); 
		return( props );
	}

	public ObjectProperties createEngineMonitoringProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.MONITORING , nameEngineMonitoring , engine.execrc );
		props.create( parent , entityAppEngineMonitoring , null ); 
		return( props );
	}

	public ObjectProperties createDefaultBuildCommonProps( ObjectProperties parent ) throws Exception {
		return( createBuildCommonProps( parent , DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , true ) );
	}
	
	public ObjectProperties createDefaultBuildModeProps( ObjectProperties parent , DBEnumBuildModeType mode ) throws Exception {
		return( createBuildModeProps( parent , DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , mode , true ) );
	}
	
	private ObjectProperties createBuildCommonProps( ObjectProperties parent , DBEnumObjectType objectType , DBEnumObjectVersionType versionType , boolean defaults ) throws Exception {
		String set = nameBuildSet;
		if( defaults )
			set += ".defaults";
		
		ObjectProperties props = new ObjectProperties( objectType , versionType , DBEnumParamRoleType.BUILDMODE_COMMON , set , engine.execrc );
		props.create( parent , entityAppProductBuild , null );
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
		props.create( parent , entityAppProductBuild , null ); 
		return( props );
	}

	public ObjectProperties createBaseItemProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.DEFAULT , nameBaseItem , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.BASE_ITEM , DBEnumParamEntityType.BASEITEM_CUSTOM , DBVersions.CORE_ID , DBEnumObjectVersionType.CORE );
		props.create( parent , entityAppBaseItem , custom );
		return( props );
	}

	public ObjectProperties createSystemProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.DEFAULT , nameSystem , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.APPSYSTEM , DBEnumParamEntityType.SYSTEM_CUSTOM , -1 , DBEnumObjectVersionType.SYSTEM );
		props.create( parent , entityAppDirectorySystem , custom );
		return( props );
	}

	public ObjectProperties createLdapProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.LDAP , nameLdap , engine.execrc );
		props.create( parent , entityAppLDAPSettings , null );
		return( props );
	}

	public ObjectProperties createDefaultProductContextProps() throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ROOT , DBEnumObjectVersionType.CORE , DBEnumParamRoleType.PRODUCTCTX , nameProductContext , engine.execrc );
		props.create( null , entityAppProductContext , null );
		return( props );
	}
	
	public ObjectProperties createMetaMonitoringProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , DBEnumParamRoleType.METAMON , nameMetaMonitoringSet , engine.execrc );
		props.create( parent , entityAppMetaMonitoring , null );
		return( props );
	}
	
	public ObjectProperties createMetaProductProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , DBEnumParamRoleType.DEFAULT , nameMetaProductSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_CUSTOM , -1 , DBEnumObjectVersionType.PRODUCT ); 
		props.create( parent , new PropertyEntity[] { entityAppProductContext , entityAppProductSettings } , custom );
		return( props );
	}
	
	public ObjectProperties createMetaBuildCommonProps( ObjectProperties parent ) throws Exception {
		return( createBuildCommonProps( parent , DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , false ) );
	}

	public ObjectProperties createMetaBuildModeProps( ObjectProperties parent , DBEnumBuildModeType mode ) throws Exception {
		return( createBuildModeProps( parent , DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , mode , false ) );
	}
	
	public ObjectProperties createMetaEnvProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.DEFAULT , nameMetaEnvSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppEnvPrimary , entityAppEnvExtra } , custom );
		return( props );
	}
	
	public ObjectProperties createMetaEnvSegmentProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT_SEGMENT , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.DEFAULT , nameMetaEnvSegmentSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_SEGMENT_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppSegmentPrimary } , custom );
		return( props );
	}
	
	public ObjectProperties createMetaEnvServerProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.DEFAULT , nameMetaEnvServerSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_SERVER_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppServerPrimary , entityAppServerExtra } , custom );
		return( props );
	}
	
	public ObjectProperties createMetaEnvServerBaseProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT_SERVER , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.SERVERBASE , nameMetaEnvServerBaseSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_SERVER_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppBaseItem } , custom );
		return( props );
	}
	
	public ObjectProperties createMetaEnvServerNodeProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.ENVIRONMENT_NODE , DBEnumObjectVersionType.ENVIRONMENT , DBEnumParamRoleType.DEFAULT , nameMetaEnvServerNodeSet , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.ENV_NODE_CUSTOM , -1 , DBEnumObjectVersionType.ENVIRONMENT ); 
		props.create( parent , new PropertyEntity[] { entityAppNodePrimary } , custom );
		return( props );
	}
	
}
