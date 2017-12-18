package org.urm.db.upgrade;

import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumParamRoleType;
import org.urm.db.engine.DBEngineSettings;
import org.urm.meta.EngineLoader;

public class DBUpgradeSet_102 extends DBUpgradeSet {

	public DBUpgradeSet_102() {
		super( 101 , 102 );
	}
	
	@Override
	public void upgrade( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		DBEnums.addEnumItem( c , DBEnumParamEntityType.PRODUCTCTX );
		DBEnums.addEnumItem( c , DBEnumParamRoleType.PRODUCTCTX );
		DBEngineSettings.upgradeEntityProductContext( loader );
	}
	
}
