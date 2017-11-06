package org.urm.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.engine.EngineDB;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.System;

public class DBSystem {

	public static void save( ActionBase action , EngineDirectory directory , EngineDB db , System system ) throws Exception {
		Connection c = null;
		try {
			c = db.getConnection();
			
			Statement stmt = c.createStatement();
			stmt.executeUpdate( "insert into urm_system ( name , xdesc , offline ) values ( " + 
					db.getQuoted( system.NAME ) + ", " +
					db.getQuoted( system.DESC ) + ", " +
					db.getBoolean( system.OFFLINE ) + " )" );
			c.commit();
		}
		catch( Throwable e ) {
			action.log( "unable to save system" , e );
			c.rollback();
			throw new RuntimeException( "unable to save system" );
		}
		finally {
			db.releaseConnection( c );
		}
	}

	public static System[] load( ActionBase action , EngineDirectory directory , EngineDB db ) throws Exception {
		List<System> systems = new LinkedList<System>();
		
		Connection c = null;
		try {
			c = db.getConnection();
			
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "select name , xdesc , offline from urm_system");
			while( rs.next() ) {
				System system = new System( directory );
				system.NAME = rs.getString( 1 );
				system.DESC = rs.getString( 2 );
				system.OFFLINE = db.getBoolean( rs.getString( 3 ) );
				systems.add( system );
			}
		}
		finally {
			db.releaseConnection( c );
		}
		return( systems.toArray( new System[0] ) );
	}
	
}
