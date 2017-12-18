package org.urm.engine.properties;

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
import org.urm.engine.Engine;
import org.urm.meta.EngineLoader;

public class EngineEntities {

	public static String nameRunContextSet = "execrc";
	public static String nameEngineSettings = "engine";
	public static String nameDefaultProductSet = "product.defaults";
	public static String nameDefaultBuildSet = "build.defaults";
	public static String nameDefaultBuildBranchSet = "build.branch.defaults";
	public static String nameDefaultBuildDevBranchSet = "build.devbranch.defaults";
	public static String nameDefaultBuildDevTrunkSet = "build.devtrunk.defaults";
	public static String nameDefaultBuildMajorBranchSet = "build.majorbranch.defaults";
	public static String nameDefaultBuildTrunkSet = "build.trunk.defaults";
	public static String nameEngineMonitoring = "defmon";
	public static String nameBaseItem = "baseitem";
	public static String nameSystem = "system";
	public static String nameLdap = "ldap";

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
		
		entityCustomRC = DBEngineContext.createEntityCustomRC( loader );
		entityCustomEngine = DBEngineContext.createEntityCustomEngine( loader );
	}
	
	public void useMeta( EngineLoader loader ) throws Exception {
		entityAppRC = DBEngineContext.loaddbEntityRC( loader );
		entityAppEngine = DBEngineContext.loaddbEntityEngine( loader );
		entityAppProductContext = DBEngineSettings.loaddbEntityProductContext( loader );
		entityAppProductSettings = DBEngineSettings.loaddbEntityProductSettings( loader );
		entityAppProductBuild = DBEngineSettings.loaddbEntityProductBuild( loader );
		entityAppEngineMonitoring = DBEngineMonitoring.loaddbEntityEngineMonitoring( loader );
		entityAppBaseGroup = DBEngineBase.loaddbEntityBaseGroup( loader );
		entityAppBaseItem = DBEngineBase.loaddbEntityBaseItem( loader );
		entityAppDirectorySystem = DBEngineDirectory.loaddbEntityDirectorySystem( loader );
		entityAppDirectoryProduct = DBEngineDirectory.loaddbEntityDirectoryProduct( loader );
		entityAppDatacenter = DBEngineInfrastructure.loaddbEntityDatacenter( loader );
		entityAppNetwork = DBEngineInfrastructure.loaddbEntityNetwork( loader );
		entityAppNetworkHost = DBEngineInfrastructure.loaddbEntityNetworkHost( loader );
		entityAppHostAccount = DBEngineInfrastructure.loaddbEntityHostAccount( loader );
		entityAppReleaseLifecycle = DBEngineLifecycles.loaddbEntityReleaseLifecycle( loader );
		entityAppLifecyclePhase = DBEngineLifecycles.loaddbEntityLifecyclePhase( loader );
		entityAppResource = DBEngineResources.loaddbEntityResource( loader );
		entityAppMirror = DBEngineMirrors.loaddbEntityMirror( loader );
		entityAppProjectBuilder = DBEngineBuilders.loaddbEntityBuilder( loader );
		entityAppLDAPSettings = DBEngineAuth.loaddbEntityLDAPSettings( loader );
		entityAppAuthUser = DBEngineAuth.loaddbEntityAuthUser( loader );
		entityAppAuthGroup = DBEngineAuth.loaddbEntityAuthGroup( loader );
		
		entityCustomRC = DBEngineContext.loaddbEntityCustomRC( loader );
		entityCustomEngine = DBEngineContext.loaddbEntityCustomEngine( loader );
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
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.PRODUCTDEFS , nameDefaultProductSet , engine.execrc );
		props.create( parent , entityAppProductSettings , null ); 
		return( props );
	}

	public ObjectProperties createDefaultBuildCommonProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.BUILDMODE_COMMON , nameDefaultBuildSet , engine.execrc );
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
			set = nameDefaultBuildBranchSet;
		}
		else
		if( mode == DBEnumBuildModeType.DEVBRANCH ) {
			role = DBEnumParamRoleType.BUILDMODE_DEVBRANCH;
			set = nameDefaultBuildDevBranchSet;
		}
		else
		if( mode == DBEnumBuildModeType.DEVTRUNK ) {
			role = DBEnumParamRoleType.BUILDMODE_DEVTRUNK;
			set = nameDefaultBuildDevTrunkSet;
		}
		else
		if( mode == DBEnumBuildModeType.MAJORBRANCH ) {
			role = DBEnumParamRoleType.BUILDMODE_MAJORBRANCH;
			set = nameDefaultBuildMajorBranchSet;
		}
		else
		if( mode == DBEnumBuildModeType.TRUNK ) {
			role = DBEnumParamRoleType.BUILDMODE_TRUNK;
			set = nameDefaultBuildTrunkSet;
		}
		ObjectProperties props = new ObjectProperties( role , set , engine.execrc );
		props.create( null , entityAppProductBuild , null ); 
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

}
