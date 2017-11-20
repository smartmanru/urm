package org.urm.db.system;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;

public class DBSystemData {

	public static void dropSystemData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMVALUE0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPARAM0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEM0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
}
