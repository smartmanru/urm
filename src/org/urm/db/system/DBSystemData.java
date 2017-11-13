package org.urm.db.system;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBCoreData;

public class DBSystemData {

	public static int getCurrentSystemVersion( DBConnection c , int systemId ) throws Exception {
		return( DBCoreData.getCurrentVersion( c , systemId ) );
	}
	
	public static void setNextSystemVersion( DBConnection c , int systemId , int version ) throws Exception {
		DBCoreData.setNextVersion( c , systemId , version , DBEnumObjectVersionType.SYSTEM );
	}
	
	public static void dropSystemData( DBConnection c ) throws Exception {
		boolean res = true;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMPARAM0 ) : false;
		res = ( res )? c.update( DBQueries.MODIFY_APP_DROP_SYSTEMMETA0 ) : false;
		if( !res )
			Common.exitUnexpected();
	}
	
}
