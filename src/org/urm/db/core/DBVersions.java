package org.urm.db.core;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumOwnerStatusType;
import org.urm.meta.OwnerObjectVersion;

public abstract class DBVersions {

	public static int APP_ID = 1;
	public static int CORE_ID = 2;
	public static int LOCAL_ID = 3;
	
	public static OwnerObjectVersion readObjectVersion( DBConnection c , int id , DBEnumObjectVersionType type ) throws Exception {
		ResultSet rc = c.query( DBQueries.QUERY_VERSIONS_GETVERSION1 , new String[] { "" + id } );
		try {
			OwnerObjectVersion version = new OwnerObjectVersion( id , type );
			if( !rc.next() ) {
				version.VERSION = 0;
				version.OWNER_STATUS_TYPE = DBEnumOwnerStatusType.ACTIVE;
				return( version );
			}
			
			version.VERSION = rc.getInt( 2 );
			version.LAST_IMPORT_ID = c.getNullInt( rc , 4 );
			version.LAST_NAME = rc.getString( 5 );
			version.OWNER_STATUS_TYPE = DBEnumOwnerStatusType.getValue( rc.getInt( 6 ) , true );
			return( version );
		}
		finally {
			c.closeQuery();
		}
	}

	public static void setNextVersion( DBConnection c , OwnerObjectVersion version , int value ) throws Exception {
		version.nextVersion = value;
		if( !c.update( DBQueries.MODIFY_VERSIONS_MERGEVERSION6 , new String[] { 
				EngineDB.getInteger( version.OWNER_OBJECT_ID ) , 
				EngineDB.getInteger( version.nextVersion ) , 
				EngineDB.getEnum( version.OBJECT_VERSION_TYPE ) ,
				EngineDB.getObject( version.LAST_IMPORT_ID ) , 
				EngineDB.getString( version.LAST_NAME ) , 
				EngineDB.getEnum( version.OWNER_STATUS_TYPE )
				} ) )
			Common.exitUnexpected();
	}

}
