package org.urm.db.system;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.properties.EngineEntities;
import org.urm.meta.EngineLoader;

public class DBSystemData {

	public static void dropSystemData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMVALUES0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMS0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPRODUCTS0 ) : false;
		DBEngineEntities.dropAppObjects( c , entities.entityAppDirectorySystem );
		if( !res )
			Common.exitUnexpected();
	}
	
}
