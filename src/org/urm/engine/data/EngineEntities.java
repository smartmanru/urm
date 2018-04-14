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
	public PropertyEntity entityAppBaseItemData;
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
	public PropertyEntity entityAppMetaMonitoring;
	public PropertyEntity entityAppMetaUnit;
	public PropertyEntity entityAppMetaSchema;
	public PropertyEntity entityAppMetaSourceSet;
	public PropertyEntity entityAppMetaSourceProject;
	public PropertyEntity entityAppMetaSourceItem;
	public PropertyEntity entityAppMetaDoc;
	public PropertyEntity entityAppMetaPolicy;
	public PropertyEntity entityAppMetaPolicyLifecycle;
	public PropertyEntity entityAppMetaDistrDelivery;
	public PropertyEntity entityAppMetaDistrDeliverySchema;
	public PropertyEntity entityAppMetaDistrDeliveryDoc;
	public PropertyEntity entityAppMetaDistrBinaryItem;
	public PropertyEntity entityAppMetaDistrConfItem;
	public PropertyEntity entityAppMetaDistrComponent;
	public PropertyEntity entityAppMetaDistrCompItem;
	public PropertyEntity entityAppEnvPrimary;
	public PropertyEntity entityAppEnvExtra;
	public PropertyEntity entityAppSegmentPrimary;
	public PropertyEntity entityAppSegmentStartGroup;
	public PropertyEntity entityAppSegmentStartGroupServer;
	public PropertyEntity entityAppSegmentMonTarget;
	public PropertyEntity entityAppSegmentMonItem;
	public PropertyEntity entityAppServerPrimary;
	public PropertyEntity entityAppServerExtra;
	public PropertyEntity entityAppServerDeployment;
	public PropertyEntity entityAppServerDependency;
	public PropertyEntity entityAppNodePrimary;
	public PropertyEntity entityAppReleaseRepository;
	public PropertyEntity entityAppReleaseMain;
	public PropertyEntity entityAppReleaseDist;
	public PropertyEntity entityAppReleaseBuildTarget;
	public PropertyEntity entityAppReleaseDistTarget;
	public PropertyEntity entityAppReleaseSchedule;
	public PropertyEntity entityAppReleasePhase;
	public PropertyEntity entityAppReleaseTicketSet;
	public PropertyEntity entityAppReleaseTicketTarget;
	public PropertyEntity entityAppReleaseTicket;
	public PropertyEntity entityAppReleaseDistItem;
	
	public EngineEntities( Engine engine ) {
		this.engine = engine;
	}
	
	public void upgradeMeta( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		makeMeta( c , true );
	}
		
	public void useMeta( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		makeMeta( c , false );
	}
		
	public void makeMeta( DBConnection c , boolean upgrade ) throws Exception {
		entityAppRC = DBEngineContext.makeEntityRC( c , upgrade );
		entityAppEngine = DBEngineContext.makeEntityEngine( c , upgrade );
		entityAppProductContext = DBProductData.makeEntityProductContext( c , upgrade );
		entityAppProductSettings = DBProductData.makeEntityProductSettings( c , upgrade );
		entityAppProductBuild = DBProductData.makeEntityProductBuild( c , upgrade );
		entityAppEngineMonitoring = DBEngineMonitoring.makeEntityEngineMonitoring( c , upgrade );
		entityAppBaseGroup = DBEngineBase.makeEntityBaseGroup( c , upgrade );
		entityAppBaseItem = DBEngineBase.makeEntityBaseItem( c , upgrade );
		entityAppBaseItemData = DBEngineBase.makeEntityBaseItemData( c , upgrade );
		entityAppDirectorySystem = DBEngineDirectory.makeEntityDirectorySystem( c , upgrade );
		entityAppDirectoryProduct = DBEngineDirectory.makeEntityDirectoryProduct( c , upgrade );
		entityAppDatacenter = DBEngineInfrastructure.makeEntityDatacenter( c , upgrade );
		entityAppNetwork = DBEngineInfrastructure.makeEntityNetwork( c , upgrade );
		entityAppNetworkHost = DBEngineInfrastructure.makeEntityNetworkHost( c , upgrade );
		entityAppHostAccount = DBEngineInfrastructure.makeEntityHostAccount( c , upgrade );
		entityAppReleaseLifecycle = DBEngineLifecycles.makeEntityReleaseLifecycle( c , upgrade );
		entityAppLifecyclePhase = DBEngineLifecycles.makeEntityLifecyclePhase( c , upgrade );
		entityAppResource = DBEngineResources.makeEntityResource( c , upgrade );
		entityAppMirror = DBEngineMirrors.makeEntityMirror( c , upgrade );
		entityAppProjectBuilder = DBEngineBuilders.makeEntityBuilder( c , upgrade );
		entityAppLDAPSettings = DBEngineAuth.makeEntityLDAPSettings( c , upgrade );
		entityAppAuthUser = DBEngineAuth.makeEntityAuthUser( c , upgrade );
		entityAppAuthGroup = DBEngineAuth.makeEntityAuthGroup( c , upgrade );
		entityAppMeta = DBProductData.makeEntityMeta( c , upgrade );
		entityAppMetaMonitoring = DBProductData.makeEntityMetaMonitoring( c , upgrade );
		entityAppMetaUnit = DBProductData.makeEntityMetaUnit( c , upgrade );
		entityAppMetaSchema = DBProductData.makeEntityMetaSchema( c , upgrade );
		entityAppMetaSourceSet = DBProductData.makeEntityMetaSourceSet( c , upgrade );
		entityAppMetaSourceProject = DBProductData.makeEntityMetaSourceProject( c , upgrade );
		entityAppMetaSourceItem = DBProductData.makeEntityMetaSourceItem( c , upgrade );
		entityAppMetaDoc = DBProductData.makeEntityMetaDoc( c , upgrade );
		entityAppMetaPolicy = DBProductData.makeEntityMetaPolicy( c , upgrade );
		entityAppMetaPolicyLifecycle = DBProductData.makeEntityMetaPolicyLifecycle( c , upgrade );
		entityAppMetaDistrDelivery = DBProductData.makeEntityMetaDistrDelivery( c , upgrade );
		entityAppMetaDistrDeliverySchema = DBProductData.makeEntityMetaDistrDeliverySchema( c , upgrade );
		entityAppMetaDistrDeliveryDoc = DBProductData.makeEntityMetaDistrDeliveryDoc( c , upgrade );
		entityAppMetaDistrBinaryItem = DBProductData.makeEntityMetaDistrBinaryItem( c , upgrade );
		entityAppMetaDistrConfItem = DBProductData.makeEntityMetaDistrConfItem( c , upgrade );
		entityAppMetaDistrComponent = DBProductData.makeEntityMetaDistrComponent( c , upgrade );
		entityAppMetaDistrCompItem = DBProductData.makeEntityMetaDistrCompItem( c , upgrade );
		entityAppEnvPrimary = DBEnvData.makeEntityEnvPrimary( c , upgrade );
		entityAppEnvExtra = DBEnvData.makeEntityEnvExtra( c , upgrade );
		entityAppSegmentPrimary = DBEnvData.makeEntitySegmentPrimary( c , upgrade );
		entityAppSegmentStartGroup = DBEnvData.makeEntityStartGroup( c , upgrade );
		entityAppSegmentStartGroupServer = DBEnvData.makeEntityStartGroupServer( c , upgrade );
		entityAppSegmentMonTarget = DBEnvData.makeEntityMonitoringTarget( c , upgrade );
		entityAppSegmentMonItem = DBEnvData.makeEntityMonitoringItem( c , upgrade );
		entityAppServerPrimary = DBEnvData.makeEntityServerPrimary( c , upgrade );
		entityAppServerExtra = DBEnvData.makeEntityServerExtra( c , upgrade );
		entityAppServerDeployment = DBEnvData.makeEntityServerDeployment( c , upgrade );
		entityAppServerDependency = DBEnvData.makeEntityServerDependency( c , upgrade );
		entityAppNodePrimary = DBEnvData.makeEntityNodePrimary( c , upgrade );
		entityAppReleaseRepository = DBReleaseData.makeEntityReleaseRepository( c , upgrade );
		entityAppReleaseMain = DBReleaseData.makeEntityReleaseMain( c , upgrade );
		entityAppReleaseDist = DBReleaseData.makeEntityReleaseDist( c , upgrade );
		entityAppReleaseBuildTarget = DBReleaseData.makeEntityReleaseBuildTarget( c , upgrade );
		entityAppReleaseDistTarget = DBReleaseData.makeEntityReleaseDistTarget( c , upgrade );
		entityAppReleaseSchedule = DBReleaseData.makeEntityReleaseSchedule( c , upgrade );
		entityAppReleasePhase = DBReleaseData.makeEntityReleasePhase( c , upgrade );
		entityAppReleaseTicketSet = DBReleaseData.makeEntityReleaseTicketSet( c , upgrade );
		entityAppReleaseTicketTarget = DBReleaseData.makeEntityReleaseTicketTarget( c , upgrade );
		entityAppReleaseTicket = DBReleaseData.makeEntityReleaseTicket( c , upgrade );
		entityAppReleaseDistItem = DBReleaseData.makeEntityReleaseDistItem( c , upgrade );
		
		entityCustomRC = DBEngineContext.createEntityCustomRC( c );
		entityCustomEngine = DBEngineContext.createEntityCustomEngine( c );
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
		props.create( parent , new PropertyEntity[] { entityAppBaseItem , entityAppBaseItemData } , custom , true );
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
	
	public ObjectProperties createMetaProductProps( int metaId , ObjectProperties parent , PropertyEntity custom ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumObjectType.META , DBEnumObjectVersionType.PRODUCT , DBEnumParamRoleType.DEFAULT , nameMetaProductSet , engine.execrc );
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
