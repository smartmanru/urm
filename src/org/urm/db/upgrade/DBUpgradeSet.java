package org.urm.db.upgrade;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumParamValueSubType;
import org.urm.meta.EngineLoader;

public abstract class DBUpgradeSet {

	private static String MODIFY_PARAM_SETSUBTYPE4 = "update urm_object_param set paramvalue_subtype = @4@ where param_object_id = @1@ and paramentity_type = @2@ and name = @3@";
	
	public int versionFrom;
	public int versionTo;
	abstract public void upgrade( EngineLoader loader ) throws Exception;
	
	public DBUpgradeSet( int versionFrom , int versionTo ) {
		this.versionFrom = versionFrom;
		this.versionTo = versionTo;
	}

	public void applyScripts( EngineLoader loader , int version ) throws Exception {
		DBUpgrade.applyScripts( loader , "database/upgrade/" + version );
	}
	
	protected void setSubtype( DBConnection c , DBEnumParamEntityType entity , String name , DBEnumParamValueSubType subtype ) throws Exception {
		if( !c.modify( MODIFY_PARAM_SETSUBTYPE4 , new String[] { 
				EngineDB.getInteger( DBVersions.APP_ID ) , 
				EngineDB.getEnum( entity ) ,
				EngineDB.getString( name ) ,
				EngineDB.getEnum( subtype ) } ) )
			Common.exitUnexpected();
	}
		
}
