package org.urm.db.meta;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.db.DBConnection;
import org.urm.db.DBEnumTypes.DBEnumObjectType;
import org.urm.db.DBNames;
import org.urm.db.DBQueries;
import org.urm.engine.EngineDB;
import org.urm.engine.EngineTransaction;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.System;

public class DBSystem {

	public static void insert( EngineTransaction transaction , System system ) throws Exception {
		DBConnection c = transaction.connection;
		system.ID = DBNames.getNameIndex( c , 0 , system.NAME , DBEnumObjectType.SYSTEM );
		system.SV = transaction.SV;
		c.update( DBQueries.UPDATE_SYSTEM_ADD5 , new String[] {
				"" + system.ID , 
				EngineDB.getString( system.NAME ) , 
				EngineDB.getString( system.DESC ) ,
				EngineDB.getBoolean( system.OFFLINE ) ,
				"" + system.SV 
				} );
	}

	public static System[] load( EngineDirectory directory , DBConnection c ) throws Exception {
		List<System> systems = new LinkedList<System>();
		
		ResultSet rs = c.query( DBQueries.QUERY_SYSTEM_GETALL0 );
		while( rs.next() ) {
			System system = new System( directory );
			system.ID = rs.getInt( 1 );
			system.NAME = rs.getString( 2 );
			system.DESC = rs.getString( 3 );
			system.OFFLINE = rs.getBoolean( 4 );
			system.SV = rs.getInt( 5 );
			systems.add( system );
		}
		
		return( systems.toArray( new System[0] ) );
	}
	
}
