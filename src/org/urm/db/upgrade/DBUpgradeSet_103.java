package org.urm.db.upgrade;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumParamValueSubType;
import org.urm.db.core.DBVersions;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;

public class DBUpgradeSet_103 extends DBUpgradeSet {

	private static String MODIFY_PARAM_SETSUBTYPE4 = "update urm_object_param set paramvalue_subtype = @4@ where param_object_id = @1@ and paramentity_type = @2@ and name = @3@";
	
	public DBUpgradeSet_103() {
		super( 102 , 103 );
	}
	
	@Override
	public void upgrade( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		DBEnums.addEnum( c , DBEnumParamValueSubType.class );
		setSubtype( c , DBEnumParamEntityType.ENGINE , MetaProductSettings.PROPERTY_DISTR_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , MetaProductSettings.PROPERTY_UPGRADE_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , MetaProductSettings.PROPERTY_BASE_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , MetaProductSettings.PROPERTY_MIRRORPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , MetaProductSettings.PROPERTY_SOURCE_RELEASEROOTDIR , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , MetaProductSettings.PROPERTY_SOURCE_CFG_ROOTDIR , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , MetaProductSettings.PROPERTY_SOURCE_CFG_LIVEROOTDIR , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , MetaProductSettings.PROPERTY_SOURCE_SQL_POSTREFRESH , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTDEFS , MetaProductSettings.PROPERTY_DISTR_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTDEFS , MetaProductSettings.PROPERTY_UPGRADE_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTDEFS , MetaProductSettings.PROPERTY_BASE_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTDEFS , MetaProductSettings.PROPERTY_MIRRORPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTDEFS , MetaProductSettings.PROPERTY_SOURCE_RELEASEROOTDIR , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTDEFS , MetaProductSettings.PROPERTY_SOURCE_CFG_ROOTDIR , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTDEFS , MetaProductSettings.PROPERTY_SOURCE_CFG_LIVEROOTDIR , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTDEFS , MetaProductSettings.PROPERTY_SOURCE_SQL_POSTREFRESH , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTBUILD , MetaProductBuildSettings.PROPERTY_ARTEFACTDIR , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTBUILD , MetaProductBuildSettings.PROPERTY_LOGPATH , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTBUILD , MetaProductBuildSettings.PROPERTY_MAVEN_CFGFILE , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.PRODUCTCTX , MetaProductCoreSettings.PROPERTY_PRODUCT_HOME , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.RC , RunContext.PROPERTY_USER_HOME , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.RC , RunContext.PROPERTY_INSTALL_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.RC , RunContext.PROPERTY_WORK_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.RC , RunContext.PROPERTY_AUTH_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.RC , RunContext.PROPERTY_DB_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.RC , RunContext.PROPERTY_SERVER_CONFPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.RC , RunContext.PROPERTY_SERVER_PRODUCTSPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.MONITORING , EngineMonitoring.PROPERTY_RESOURCE_PATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.MONITORING , EngineMonitoring.PROPERTY_DIR_DATA , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.MONITORING , EngineMonitoring.PROPERTY_DIR_REPORTS , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.MONITORING , EngineMonitoring.PROPERTY_DIR_LOGS , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
	}

	private void setSubtype( DBConnection c , DBEnumParamEntityType entity , String name , DBEnumParamValueSubType subtype ) throws Exception {
		if( !c.modify( MODIFY_PARAM_SETSUBTYPE4 , new String[] { 
				EngineDB.getInteger( DBVersions.APP_ID ) , 
				EngineDB.getEnum( entity ) ,
				EngineDB.getString( name ) ,
				EngineDB.getEnum( subtype ) } ) )
			Common.exitUnexpected();
	}
		
}
