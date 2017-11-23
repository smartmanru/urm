package org.urm.db.system;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;

public class DBSystemData {

	public static void dropSystemData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMVALUES0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMS0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPRODUCTS0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMS0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
}
