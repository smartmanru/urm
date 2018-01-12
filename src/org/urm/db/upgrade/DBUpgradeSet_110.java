package org.urm.db.upgrade;

import org.urm.meta.EngineLoader;

public class DBUpgradeSet_110 extends DBUpgradeSet {

	public DBUpgradeSet_110() {
		super( 104 , 110 );
	}
	
	@Override
	public void upgrade( EngineLoader loader ) throws Exception {
		super.applyScripts( loader , 110 );
	}
	
}
