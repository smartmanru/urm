package org.urm.db.engine;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class DBEngineEntities {

	public static void modifyAppObject( DBConnection c , PropertyEntity entity , int id , int version , String[] values , boolean insert ) throws Exception {
		if( insert )
			insertAppObject( c , entity , id , version , values );
		else
			updateAppObject( c , entity , id , version , values );
	}
	
	public static void insertAppObject( DBConnection c , PropertyEntity entity , int id , int version , String[] values ) throws Exception {
		EntityVar[] vars = entity.getDatabaseVars();
		if( vars.length != values.length )
			Common.exitUnexpected();

		String[] valuesFinal = new String[ values.length + 2 ];
		
		valuesFinal[ 0 ] = "" + id;
		for( int k = 0; k < vars.length; k++ )
			valuesFinal[ k + 1 ] = values[ k ];
		valuesFinal[ valuesFinal.length - 1 ] = "" + version;
		
		String query = "insert into " + entity.APP_TABLE + " ( ";
		query += getFieldList( entity );
		query += " ) values ( @values@ )";
		
		if( !c.update( query , valuesFinal ) )
			Common.exitUnexpected();
	}
	
	private static String getFieldList( PropertyEntity entity ) {
		EntityVar[] vars = entity.getDatabaseVars();
		String list = entity.getIdField();
		for( int k = 0; k < vars.length; k++ ) {
			EntityVar var = vars[ k ];
			list = Common.addToList( list , var.DBNAME , " , " );
		}
		
		String fieldVersion = entity.getVersionField();
		list = Common.addToList( list , fieldVersion , " , " );
		return( list );
	}
	
	public static void updateAppObject( DBConnection c , PropertyEntity entity , int id , int version , String[] values ) throws Exception {
		EntityVar[] vars = entity.getDatabaseVars();
		if( vars.length != values.length )
			Common.exitUnexpected();
		
		String query = "update " + entity.APP_TABLE + " set ";
		for( int k = 0; k < vars.length; k++ ) {
			EntityVar var = vars[ k ];
			query += var.DBNAME + " = " + values[ k ] + " , ";
		}
		
		String fieldVersion = entity.getVersionField();
		query += fieldVersion + " = " + version;
		query += " where " + entity.getIdField() + " = " + id;
		
		if( !c.update( query ) )
			Common.exitUnexpected();
	}

	public static ResultSet listAppObjects( DBConnection c , PropertyEntity entity ) throws Exception {
		String query = "select " + getFieldList( entity ) + " from " + entity.APP_TABLE; 
		ResultSet rs = c.query( query );
		return( rs );
	}

	public static void deleteAppObject( DBConnection c , PropertyEntity entity , int id , int version ) throws Exception {
		String query = "delete from " + entity.APP_TABLE + " where " + entity.getIdField() + " = " + id;
		if( !c.update( query ) )
			Common.exitUnexpected();
	}
	
	public static void dropAppObjects( DBConnection c , PropertyEntity entity ) throws Exception {
		String query = "delete from " + entity.APP_TABLE;
		if( !c.update( query ) )
			Common.exitUnexpected();
	}
	
	public static void exportxmlAppObject( Document doc , Element root , PropertyEntity entity , String[] values , boolean attrs ) throws Exception {
		EntityVar[] vars = entity.getXmlVars();
		if( vars.length != values.length )
			Common.exitUnexpected();
		
		for( int k = 0; k < vars.length; k++ ) {
			EntityVar var = vars[ k ];
			String value = values[ k ];
			if( value != null && !value.isEmpty() ) {
				if( attrs )
					Common.xmlSetElementAttr( doc , root , var.XMLNAME , value );
				else
					Common.xmlCreatePropertyElement( doc , root , var.XMLNAME , value );
			}
		}
	}
	
	public static void loaddbAppObject( ResultSet rs , ObjectProperties props ) throws Exception {
		ObjectMeta meta = props.getMeta();
		PropertyEntity entity = meta.getAppEntity();
		
		for( EntityVar var : entity.getVars() ) {
			if( var.isDatabaseOnly() || var.isXmlOnly() )
				continue;

			if( var.isString() ) {
				String value = rs.getString( var.databaseColumn );
				props.setStringProperty( var.NAME , value );
			}
			else
			if( var.isNumber() || var.isEnum() ) {
				int value = rs.getInt( var.databaseColumn );
				props.setIntProperty( var.NAME , value );
			}
			else
			if( var.isBoolean() ) {
				boolean value = rs.getBoolean( var.databaseColumn );
				props.setBooleanProperty( var.NAME , value );
			}
			else
				Common.exitUnexpected();
		}
	}

	public static EntityVar createCustomProperty( EngineTransaction transaction , EngineEntities entities , int ownerId , ObjectProperties ops , String name , String desc , String defvalue ) throws Exception {
		ObjectMeta meta = ops.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		if( entity.META_OBJECT_ID != ownerId )
			transaction.exitUnexpectedState();
			
		EntityVar var = EntityVar.metaString( name , desc , false , defvalue );
		DBSettings.createCustomProperty( transaction , entity , var );
		meta.rebuild();
		
		return( var );
	}
	
}
