package org.urm.db.upgrade;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumParamValueSubType;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.EngineContext;

public class DBUpgradeSet_104 extends DBUpgradeSet {

	private static String MODIFY_PARAM_SETSUBTYPE4 = "update urm_object_param set paramvalue_subtype = @4@ where param_object_id = @1@ and paramentity_type = @2@ and name = @3@";
	
	public DBUpgradeSet_104() {
		super( 103 , 104 );
	}
	
	@Override
	public void upgrade( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_DIST_ROOT , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_DIST_APPFOLDER , DBEnumParamValueSubType.PATHRELATIVEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_DIST_PLATFORMPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_SECURE_CONFPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_WORK_ARTEFACTS , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_WORK_MIRRORPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_WORK_BUILDLOGS , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_WORK_DEPLOYLOGS , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_MON_RESPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_MON_DATAPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_MON_REPORTPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
		setSubtype( c , DBEnumParamEntityType.ENGINE , EngineContext.PROPERTY_MON_LOGPATH , DBEnumParamValueSubType.PATHABSOLUTEENGINE );
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
