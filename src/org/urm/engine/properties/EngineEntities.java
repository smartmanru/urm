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
import org.urm.db.engine.DBEngineSettings;
import org.urm.db.product.DBProductData;
import org.urm.engine.Engine;
import org.urm.meta.EngineLoader;
import org.urm.meta.product.Meta;

public class EngineEntities {

	public static String nameRunContextSet = "execrc";
	public static String nameEngineSettings = "engine";
	public static String nameProductSet = "product.defaults";
	public static String nameBuildSet = "build.defaults";
	public static String nameBuildBranchSet = "build.branch.defaults";
	public static String nameBuildDevBranchSet = "build.devbranch.defaults";
	public static String nameBuildDevTrunkSet = "build.devtrunk.defaults";
	public static String nameBuildMajorBranchSet = "build.majorbranch.defaults";
	public static String nameBuildTrunkSet = "build.trunk.defaults";
	public static String nameEngineMonitoring = "defmon";
	public static String nameBaseItem = "baseitem";
	public static String nameSystem = "system";
	public static String nameLdap = "ldap";
	public static String nameProductContext = "ctx";
	public static String nameMeta = "meta";
	public static String nameMetaCoreSettings = "core";

	public static String FIELD_VERSION_APP = "av"; 
	public static String FIELD_VERSION_CORE = "cv"; 
	public static String FIELD_VERSION_SYSTEM = "sv"; 
	public static String FIELD_VERSION_PRODUCT = "pv"; 
	public static String FIELD_VERSION_ENVIRONMENT = "ev"; 
	public static String FIELD_VERSION_AUTH = "uv"; 
	
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
	
	public EngineEntities( Engine engine ) {
		this.engine = engine;
	}
	
	public void upgradeMeta( EngineLoader loader ) throws Exception {
		entityAppRC = DBEngineContext.upgradeEntityRC( loader );
		entityAppEngine = DBEngineContext.upgradeEntityEngine( loader );
		entityAppProductContext = DBEngineSettings.upgradeEntityProductContext( loader );
		entityAppProductSettings = DBEngineSettings.upgradeEntityProductSettings( loader );
		entityAppProductBuild = DBEngineSettings.upgradeEntityProductBuild( loader );
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
		entityAppMetaVersion = DBProductData.upgradeEntityVersion( loader );
		
		entityCustomRC = DBEngineContext.createEntityCustomRC( loader );
		entityCustomEngine = DBEngineContext.createEntityCustomEngine( loader );
	}
	
	public void useMeta( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		entityAppRC = DBEngineContext.loaddbEntityRC( c );
		entityAppEngine = DBEngineContext.loaddbEntityEngine( c );
		entityAppProductContext = DBEngineSettings.loaddbEntityProductContext( c );
		entityAppProductSettings = DBEngineSettings.loaddbEntityProductSettings( c );
		entityAppProductBuild = DBEngineSettings.loaddbEntityProductBuild( c );
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
		entityAppMetaVersion = DBProductData.loaddbEntityVersion( c );
		
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
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.RC , nameRunContextSet , engine.execrc );
		props.create( null , entityAppRC , entityCustomRC );
		return( props );
	}

	public ObjectProperties createEngineProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.ENGINE , nameEngineSettings , engine.execrc );
		props.create( parent , entityAppEngine , entityCustomEngine ); 
		return( props );
	}

	public ObjectProperties createDefaultProductProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.PRODUCTDEFS , nameProductSet , engine.execrc );
		props.create( parent , entityAppProductSettings , null ); 
		return( props );
	}

	public ObjectProperties createDefaultBuildCommonProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.BUILDMODE_COMMON , nameBuildSet , engine.execrc );
		props.create( parent , entityAppProductBuild , null );
		return( props );
	}

	public ObjectProperties createEngineMonitoringProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.MONITORING , nameEngineMonitoring , engine.execrc );
		props.create( parent , entityAppEngineMonitoring , null ); 
		return( props );
	}

	public ObjectProperties createDefaultBuildModeProps( ObjectProperties parent , DBEnumBuildModeType mode ) throws Exception {
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
		ObjectProperties props = new ObjectProperties( role , set , engine.execrc );
		props.create( parent , entityAppProductBuild , null ); 
		return( props );
	}

	public ObjectProperties createBaseItemProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.BASEITEM , nameBaseItem , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.BASE_ITEM , DBEnumParamEntityType.BASEITEM_CUSTOM , DBVersions.CORE_ID , DBEnumObjectVersionType.CORE );
		props.create( parent , entityAppBaseItem , custom );
		return( props );
	}

	public ObjectProperties createSystemProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.SYSTEM , nameSystem , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.APPSYSTEM , DBEnumParamEntityType.SYSTEM_CUSTOM , -1 , DBEnumObjectVersionType.SYSTEM );
		props.create( parent , entityAppDirectorySystem , custom );
		return( props );
	}

	public ObjectProperties createLdapProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.LDAP , nameLdap , engine.execrc );
		props.create( parent , entityAppLDAPSettings , null );
		return( props );
	}

	public ObjectProperties createProductContextProps() throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.PRODUCTCTX , nameProductContext , engine.execrc );
		props.create( null , entityAppProductContext , null );
		return( props );
	}
	
	public ObjectProperties createMetaProps( Meta meta , ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.DEFAULT , nameMeta , engine.execrc );
		PropertyEntity custom = PropertyEntity.getCustomEntity( -1 , DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_CUSTOM , -1 , DBEnumObjectVersionType.PRODUCT ); 
		props.create( parent , entityAppMeta , custom );
		return( props );
	}
	
	public ObjectProperties createMetaCoreSettingsProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.DEFAULT , nameMetaCoreSettings , engine.execrc );
		props.create( parent , entityAppProductContext , null );
		return( props );
	}

	public ObjectProperties createMetaBuildCommonProps( Meta meta , ObjectProperties parent ) throws Exception {
		return( createDefaultBuildCommonProps( parent ) );
	}

	public ObjectProperties createMetaBuildModeProps( Meta meta , ObjectProperties parent , DBEnumBuildModeType mode ) throws Exception {
		return( createDefaultBuildModeProps( parent , mode ) );
	}
	
}
