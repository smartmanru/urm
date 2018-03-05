package org.urm.db.engine;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamValueSubType;
import org.urm.db.core.DBEnums.DBEnumParamValueType;
import org.urm.db.core.DBSettings;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.properties.PropertyValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class DBEngineEntities {

	public static String FIELD_VERSION_APP = "av"; 
	public static String FIELD_VERSION_CORE = "cv"; 
	public static String FIELD_VERSION_SYSTEM = "sv"; 
	public static String FIELD_VERSION_PRODUCT = "pv"; 
	public static String FIELD_VERSION_ENVIRONMENT = "ev"; 
	public static String FIELD_VERSION_AUTH = "uv"; 
	
	public static String getVersionField( DBEnumObjectVersionType versionType ) {
		if( versionType == DBEnumObjectVersionType.APP )
			return( FIELD_VERSION_APP );
		if( versionType == DBEnumObjectVersionType.CORE )
			return( FIELD_VERSION_CORE );
		if( versionType == DBEnumObjectVersionType.SYSTEM )
			return( FIELD_VERSION_SYSTEM );
		if( versionType == DBEnumObjectVersionType.PRODUCT )
			return( FIELD_VERSION_PRODUCT );
		if( versionType == DBEnumObjectVersionType.ENVIRONMENT )
			return( FIELD_VERSION_ENVIRONMENT );
		if( versionType == DBEnumObjectVersionType.LOCAL )
			return( FIELD_VERSION_AUTH );
		return( null );
	}

	public static void modifyAppObject( DBConnection c , PropertyEntity entity , int id , int version , String[] values , boolean insert ) throws Exception {
		if( id <= 0 || version <= 0 )
			Common.exitUnexpected();
		
		if( insert )
			insertAppObject( c , entity , id , version , values );
		else
			updateAppObject( c , entity , id , version , values );
	}
	
	public static void insertAppObject( DBConnection c , PropertyEntity entity , int id , int version , String[] values ) throws Exception {
		if( id <= 0 || version <= 0 )
			Common.exitUnexpected();
		
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
		
		if( !c.modify( query , valuesFinal ) )
			Common.exitUnexpected();
	}
	
	private static String getFieldList( PropertyEntity entity ) {
		EntityVar[] vars = entity.getDatabaseVars();
		String list = entity.getIdField();
		for( int k = 0; k < vars.length; k++ ) {
			EntityVar var = vars[ k ];
			list = Common.addToList( list , var.DBNAME , " , " );
		}
		
		String fieldVersion = getVersionField( entity.DATA_OBJECTVERSION_TYPE );
		list = Common.addToList( list , fieldVersion , " , " );
		return( list );
	}
	
	public static void updateAppObject( DBConnection c , PropertyEntity entity , int id , int version , String[] values ) throws Exception {
		if( id <= 0 || version <= 0 )
			Common.exitUnexpected();
		
		EntityVar[] vars = entity.getDatabaseVars();
		if( vars.length != values.length )
			Common.exitUnexpected();
		
		String query = "update " + entity.APP_TABLE + " set ";
		for( int k = 0; k < vars.length; k++ ) {
			EntityVar var = vars[ k ];
			query += var.DBNAME + " = " + values[ k ] + " , ";
		}
		
		String fieldVersion = getVersionField( entity.DATA_OBJECTVERSION_TYPE );
		query += fieldVersion + " = " + version;
		query += " where " + entity.getIdField() + " = " + id;
		
		if( !c.modify( query ) )
			Common.exitUnexpected();
	}

	public static ResultSet listAppObjects( DBConnection c , PropertyEntity entity ) throws Exception {
		String query = "select " + getFieldList( entity ) + " from " + entity.APP_TABLE; 
		ResultSet rs = c.query( query );
		return( rs );
	}

	public static ResultSet listAppObjectsFiltered( DBConnection c , PropertyEntity entity , String filter , String[] args ) throws Exception {
		String query = "select " + getFieldList( entity ) + " from " + entity.APP_TABLE + " " + filter; 
		ResultSet rs = c.query( query , args );
		return( rs );
	}
	
	public static ResultSet listSingleAppObject( DBConnection c , PropertyEntity entity , int id ) throws Exception {
		String query = "select " + getFieldList( entity ) + " from " + entity.APP_TABLE + " where " + entity.ID_FIELD + " = " + id; 
		ResultSet rs = c.query( query );
		if( rs == null )
			return( null );
		
		if( !rs.next() ) {
			c.closeQuery();
			Common.exitUnexpected();
		}
		
		return( rs );
	}

	public static void deleteAppObject( DBConnection c , PropertyEntity entity , int id , int version ) throws Exception {
		if( id <= 0 || version <= 0 )
			Common.exitUnexpected();
		
		String query = "delete from " + entity.APP_TABLE + " where " + entity.getIdField() + " = " + id;
		if( !c.modify( query ) )
			Common.exitUnexpected();
	}
	
	public static void dropAppObjects( DBConnection c , PropertyEntity entity ) throws Exception {
		String query = "delete from " + entity.APP_TABLE;
		if( !c.modify( query ) )
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
		
		for( EntityVar var : meta.getAppVars() ) {
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

	public static EntityVar createCustomProperty( EngineTransaction transaction , EngineEntities entities , int ownerId , ObjectProperties ops , String name , String desc , DBEnumParamValueType type , DBEnumParamValueSubType subtype , String defValue , boolean secured , String[] enumList ) throws Exception {
		ObjectMeta meta = ops.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		if( entity.META_OBJECT_ID != ownerId )
			transaction.exitUnexpectedState();

		// check unique
		if( entity.findVar( name ) != null )
			Common.exitUnexpected();

		if( enumList != null && defValue.isEmpty() == false ) {
			if( Common.getIndexOf( enumList , defValue ) < 0 )
				Common.exitUnexpected();
		}
		
		EntityVar var = EntityVar.meta( name , name , name , desc , type , subtype , DBEnumObjectType.UNKNOWN , false , secured , defValue , null , enumList );
		DBSettings.createCustomProperty( transaction , entity , var );
		meta.rebuild();
		
		ops.createProperty( var );
		return( var );
	}
	
	public static EntityVar modifyCustomProperty( EngineTransaction transaction , EngineEntities entities , int ownerId , ObjectProperties ops , int paramId , String name , String desc , DBEnumParamValueType type , DBEnumParamValueSubType subtype , String defValue , boolean secured , String[] enumList ) throws Exception {
		ObjectMeta meta = ops.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		if( entity.META_OBJECT_ID != ownerId )
			transaction.exitUnexpectedState();
			
		// check unique
		EntityVar var = meta.getVar( paramId );
		EntityVar check = meta.findVar( name );
		if( check != null && check != var )
			Common.exitUnexpected();
		
		String originalName = var.NAME;
		var.modifyCustom( name , desc , type , subtype , defValue , secured , enumList );
		DBSettings.modifyCustomProperty( transaction , var );

		PropertySet set = ops.getProperties();
		PropertyValue pv = set.renameCustomProperty( originalName , name );
		pv.setDefault( defValue );
		
		meta.rebuild();
		ops.recalculateProperties();
		
		return( var );
	}
	
	public static void deleteCustomProperty( EngineTransaction transaction , EngineEntities entities , int ownerId , ObjectProperties ops , int paramId ) throws Exception {
		ObjectMeta meta = ops.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		if( entity.META_OBJECT_ID != ownerId )
			transaction.exitUnexpectedState();
		
		EntityVar var = meta.getVar( paramId );
		DBSettings.deleteCustomProperty( transaction , var );
		PropertySet set = ops.getProperties();
		set.removeCustomProperty( var.NAME );
		
		meta.rebuild();
		ops.recalculateProperties();
	}
	
}
