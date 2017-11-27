package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;

public abstract class DBEngineEntities {

	public static void insertAppObject( DBConnection c , PropertyEntity entity , String[] values ) throws Exception {
		String query = "insert into " + entity.APP_TABLE + " ( ";
		EntityVar[] vars = entity.getVars();
		String list = "";
		for( EntityVar var : vars ) {
			list = Common.addToList( list , var.NAME , " , " );
		}
		query += list;
		query += " ) values ( @values@ )";
		if( !c.update( query , values ) )
			Common.exitUnexpected();
	}
	
}
