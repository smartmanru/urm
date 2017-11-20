package org.urm.db.core;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumParamValueType;
import org.urm.engine.EngineDB;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.properties.PropertySet;
import org.w3c.dom.Node;

public abstract class DBSettings {

	public static void loaddb( DBConnection c , int objectId , ObjectProperties properties ) throws Exception {
	}
	
	public static void loadxml( Node root , ObjectProperties properties ) throws Exception {
		PropertySet props = properties.getProperties(); 
		
		// read fixed properties
		for( PropertyEntity entity : properties.getEntities() ) {
			if( entity.custom )
				continue;
			
			for( EntityVar var : entity.getVars() ) {
				String value = ConfReader.getAttrValue( root , var.NAME );
				props.setOriginalProperty( var.NAME , var.PARAMVALUE_TYPE , value , true , null );
			}
		}
	}

	public static PropertyEntity savedbEntity( DBConnection c , DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType , boolean custom , int version , EntityVar[] vars ) throws Exception {
		PropertyEntity entity = new PropertyEntity( ownerType , ownerId , entityType , custom );
		for( EntityVar var : vars ) {
			entity.addVar( var );
			addVar( c , entity , var , version );
		}
		return( entity );
	}

	public static void addVar( DBConnection c , PropertyEntity entity , EntityVar var , int version ) throws Exception {
		var.ID = DBNames.getNameIndex( c , entity.ownerId , var.NAME , DBEnumObjectType.PARAM );
		var.VERSION = version;
		if( !c.update( DBQueries.MODIFY_PARAM_ADD10 , new String[] {
			EngineDB.getInteger( entity.ownerId ) ,
			EngineDB.getInteger( entity.entityType.code() ) ,
			EngineDB.getInteger( var.ID ) ,
			EngineDB.getString( var.NAME ) ,
			EngineDB.getString( var.DESC ) ,
			EngineDB.getInteger( var.PARAMVALUE_TYPE.code() ) ,
			EngineDB.getBoolean( var.REQUIRED ) ,
			EngineDB.getBoolean( entity.custom ) ,
			EngineDB.getString( var.EXPR_DEF ) ,
			EngineDB.getInteger( version )
			} ) )
			Common.exitUnexpected();
	}

	public static PropertyEntity loaddbEntity( DBConnection c , DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType , boolean custom ) throws Exception {
		PropertyEntity entity = new PropertyEntity( ownerType , ownerId , entityType , custom );
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETENTITYFIXEDPARAMS2 , new String[] { EngineDB.getInteger( ownerId ) , EngineDB.getInteger( entityType.code() ) } );
		if( rs == null )
			Common.exitUnexpected();
		
		while( rs.next() ) {
			EntityVar var = EntityVar.meta( 
					rs.getString( 2 ) , 
					rs.getString( 3 ) , 
					DBEnumParamValueType.getValue( rs.getInt( 4 ) , true ) , 
					rs.getBoolean( 5 ) , 
					rs.getString( 6 ) );
			var.ID = rs.getInt( 1 );
			var.VERSION = rs.getInt( 7 );
			entity.addVar( var );
		}
		return( entity );
	}
	
}

