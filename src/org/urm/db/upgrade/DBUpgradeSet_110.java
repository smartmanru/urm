package org.urm.db.upgrade;

import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumParamRoleType;
import org.urm.db.product.DBProductData;
import org.urm.meta.EngineLoader;

public class DBUpgradeSet_110 extends DBUpgradeSet {

	public DBUpgradeSet_110() {
		super( 104 , 110 );
	}
	
	@Override
	public void upgrade( EngineLoader loader ) throws Exception {
		super.applyScripts( loader , 110 );
		/*
		DBConnection c = loader.getConnection();
		DBEnums.addEnumItem( c , DBEnumParamRoleType.DEFAULT );
		DBEnums.addEnumItem( c , DBEnumObjectType.META );
		DBEnums.addEnumItem( c , DBEnumParamEntityType.PRODUCT_CORESETTINGS );
		DBProductData.upgradeEntityMeta( loader );
		DBProductData.upgradeEntityMetaCoreSettings( loader );
		*/
		DBProductData.upgradeEntityMeta( loader );
	}
	
}
