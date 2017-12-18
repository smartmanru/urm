package org.urm.db.upgrade;

import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums;
import org.urm.db.engine.DBEngineSettings;
import org.urm.meta.EngineLoader;

public class DBUpgradeSet_102 extends DBUpgradeSet {

	public DBUpgradeSet_102() {
		super( 101 , 102 );
	}
	
	@Override
	public void upgrade( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		DBEnums.updateDatabase( c );
		DBEngineSettings.upgradeEntityProductContext( loader );
	}
	
}
