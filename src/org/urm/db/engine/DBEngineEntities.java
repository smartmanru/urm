package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;

public abstract class DBEngineEntities {

	public static void insertAppObject( DBConnection c , PropertyEntity entity , int id , int version , String[] values ) throws Exception {
		EntityVar[] vars = entity.getDatabaseVars();
		if( vars.length != values.length )
			Common.exitUnexpected();

		String[] valuesFinal = new String[ values.length + 2 ];
		
		String list = entity.getIdField();
		valuesFinal[ 0 ] = "" + id;
		for( int k = 0; k < vars.length; k++ ) {
			EntityVar var = vars[ k ];
			valuesFinal[ k + 1 ] = values[ k ];
			list = Common.addToList( list , var.DBNAME , " , " );
		}
		
		String fieldVersion = entity.getVersionField();
		list = Common.addToList( list , fieldVersion , " , " );
		valuesFinal[ valuesFinal.length - 1 ] = "" + version;
		
		String query = "insert into " + entity.APP_TABLE + " ( ";
		query += list;
		query += " ) values ( @values@ )";
		
		if( !c.update( query , valuesFinal ) )
			Common.exitUnexpected();
	}
	
	public static void updateAppObject( DBConnection c , PropertyEntity entity , int id , int version , String[] values ) throws Exception {
		EntityVar[] vars = entity.getDatabaseVars();
		if( vars.length != values.length )
			Common.exitUnexpected();
		
		String query = "update " + entity.APP_TABLE + " ( ";
		for( int k = 0; k < vars.length; k++ ) {
			EntityVar var = vars[ k ];
			query += "set " + var.DBNAME + " = " + values[ k ] + " , ";
		}
		
		String fieldVersion = entity.getVersionField();
		query += fieldVersion + " = " + version;
		query += " where " + entity.getIdField() + " = " + id;
		
		if( !c.update( query ) )
			Common.exitUnexpected();
	}
	
}
