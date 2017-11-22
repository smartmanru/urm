package org.urm.engine.properties;

import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineContext;
import org.urm.db.engine.DBEngineMonitoring;
import org.urm.db.engine.DBEngineSettings;
import org.urm.db.system.DBSystem;
import org.urm.engine.Engine;
import org.urm.meta.EngineCore;

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
	public static String nameSystem = "system";
	
	public Engine engine;
	public EngineCore core;

	private PropertyEntity entityAppRC; 
	private PropertyEntity entityCustomRC;
	private PropertyEntity entityAppEngine;
	private PropertyEntity entityCustomEngine;
	private PropertyEntity entityAppProduct;
	private PropertyEntity entityAppProductBuild;
	private PropertyEntity entityAppEngineMonitoring;
	private PropertyEntity entityAppSystem;
	
	public EngineEntities( EngineCore core ) {
		this.core = core;
		this.engine = core.engine;
	}
	
	public void upgradeData( DBConnection connection ) throws Exception {
		entityAppRC = DBEngineContext.upgradeEntityRC( connection );
		entityAppEngine = DBEngineContext.upgradeEntityEngine( connection );
		entityAppProduct = DBEngineSettings.upgradeEntityProduct( connection );
		entityAppProductBuild = DBEngineSettings.upgradeEntityProductBuild( connection );
		entityAppEngineMonitoring = DBEngineMonitoring.upgradeEntityEngineMonitoring( connection );
		entityAppSystem = DBSystem.upgradeEntitySystem( connection );
		useCustom( connection );
	}
	
	public void useData( DBConnection connection ) throws Exception {
		entityAppRC = DBSettings.loaddbEntity( connection , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.RC , false );
		entityAppEngine = DBSettings.loaddbEntity( connection , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.ENGINE , false );
		entityAppProduct = DBSettings.loaddbEntity( connection , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.PRODUCTDEFS , false );
		entityAppProductBuild = DBSettings.loaddbEntity( connection , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.PRODUCTBUILD , false );
		entityAppEngineMonitoring = DBSettings.loaddbEntity( connection , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.MONITORING , false );
		useCustom( connection );
	}
	
	private void useCustom( DBConnection connection ) throws Exception {
		entityCustomRC = DBSettings.loaddbEntity( connection , DBEnumObjectVersionType.APP , DBVersions.CORE_ID , DBEnumParamEntityType.RC_CUSTOM , true );
		entityCustomEngine = DBSettings.loaddbEntity( connection , DBEnumObjectVersionType.APP , DBVersions.CORE_ID , DBEnumParamEntityType.ENGINE_CUSTOM , true );
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
		props.create( parent , entityAppProduct , null ); 
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

	public ObjectProperties createSystemProps( ObjectProperties parent ) throws Exception {
		ObjectProperties props = new ObjectProperties( DBEnumParamRoleType.SYSTEM , "system" , engine.execrc );
		PropertyEntity custom = new PropertyEntity( DBEnumObjectVersionType.SYSTEM , 0 , DBEnumParamEntityType.SYSTEM_CUSTOM , true );
		props.create( parent , entityAppSystem , custom );
		return( props );
	}

}
