package org.urm.db.core;

import java.sql.ResultSet;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.properties.PropertyValue;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBSettings {

	public static String ELEMENT_PROPERTY = "property"; 
	public static String ATTR_NAME = "name"; 
	public static String ATTR_VALUE = "value"; 
	public static String ATTR_DESC = "desc"; 
	
	public static void loaddbValues( EngineLoader loader , int objectId , ObjectProperties properties , boolean saveApp ) throws Exception {
		DBConnection c = loader.getConnection();
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETOBJECTPARAMVALUES2 , new String[] { 
				EngineDB.getInteger( objectId ) , 
				EngineDB.getEnum( properties.type ) } );

		try {
			while( rs.next() ) {
				int param = rs.getInt( 2 );
				String exprValue = rs.getString( 3 );
				
				EntityVar var = properties.getVar( param );
				if( saveApp == false && var.isApp() )
					Common.exitUnexpected();
				
				properties.setProperty( var , exprValue );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void importxml( EngineLoader loader , Node root , ObjectProperties properties , int paramObjectId , int metaObjectId , boolean saveApp , int version ) throws Exception {
		importxmlLoad( loader , root , properties );
		importxmlSave( loader , properties , paramObjectId , metaObjectId , saveApp , version );
	}
	
	public static void importxmlLoad( EngineLoader loader , Node root , ObjectProperties properties ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity app = meta.getAppEntity();
		
		// load attributes - app only
		boolean ok = true;
		if( !app.USE_PROPS ) {
			Map<String,String> attrs = ConfReader.getAttributes( root );
			for( String prop : Common.getSortedKeys( attrs ) ) {
				String value = attrs.get( prop );
				try {
					importxmlSetAttr( loader , properties , prop , value );
				}
				catch( Throwable e ) {
					loader.trace( "attribute load error: " + e.getMessage() );
					ok = false;
				}
			}
		}
		
		// load properties
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PROPERTY );
		if( items != null ) {
			for( Node item : items ) {
				try {
					importxmlSetProperty( loader , item , properties , app.USE_PROPS );
				}
				catch( Throwable e ) {
					loader.trace( "property load error: " + e.getMessage() );
					ok = false;
				}
			}
			
			meta.rebuild();
		}

		if( !ok )
			Common.exit1( _Error.SettingsImportErrors1 , "Errors on settings import, set type=" + properties.type.name() , "" + properties.type.name() );
	}

	public static void importxmlSave( EngineLoader loader , ObjectProperties properties , int paramObjectId , int metaObjectId , boolean saveApp , int version ) throws Exception {
		DBConnection c = loader.getConnection();
		savedbEntityCustom( c , properties , paramObjectId , metaObjectId , version );
		savedbPropertyValues( c , paramObjectId , properties , saveApp , version );
	}
	
	public static void exportxml( EngineLoader loader , Document doc , Element root , ObjectProperties properties , boolean appAsProperties ) throws Exception {
		PropertySet set = properties.getProperties();
		ObjectMeta meta = properties.getMeta();
		PropertyEntity custom = meta.getCustomEntity();
		for( PropertyValue value : set.getAllProperties() ) {
			EntityVar var = properties.getVar( value.property );
			if( !var.isXml() )
				continue;
			
			if( var.isApp() ) {
				String data = value.getOriginalValue();
				if( data == null || data.isEmpty() )
					continue;
				
				if( var.isEnum() ) {
					int ev = Integer.parseInt( data );
					if( ev == DBEnums.VALUE_UNKNOWN )
						continue;
					
					data = DBEnums.getEnumValue( var.enumClass , ev );
				}
				else
				if( var.isObject() ) {
					int ev = Integer.parseInt( data );
					data = var.exportxmlObjectValue( loader , ev );
				}
					
				if( appAsProperties )
					exportxmlSetProperty( loader , doc , root , var , data , false );
				else
					exportxmlSetAttr( loader , doc , root , var , data );
			}
			else {
				boolean defineProp = ( var.isCustom() && var.entity == custom )? true : false;
				String data = null;
				if( defineProp )
					data = value.getExpressionValue();
				else {
					data = value.getOriginalValue();
					if( data == null || data.isEmpty() )
						continue;
				}
				
				exportxmlSetProperty( loader , doc , root , var , data , defineProp );
			}
		}
	}

	private static void exportxmlSetAttr( EngineLoader loader, Document doc , Element root , EntityVar var , String data ) throws Exception {
		Common.xmlSetElementAttr( doc , root , var.XMLNAME , data );
	}
	
	private static void exportxmlSetProperty( EngineLoader loader , Document doc , Element root , EntityVar var , String data , boolean defineProp ) throws Exception {
		Element property = Common.xmlCreatePropertyElement( doc , root , var.XMLNAME , data );
		if( defineProp )
			Common.xmlSetElementAttr( doc , property , ATTR_DESC , var.DESC );
	}
	
	private static void importxmlSetAttr( EngineLoader loader , ObjectProperties properties , String xmlprop , String value ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity app = meta.getAppEntity();
		EntityVar var = app.findXmlVar( xmlprop );
		if( var == null )
			Common.exit1( _Error.UnknownAppVar1 , "Attempt to override built-in variable=" + xmlprop , xmlprop );
		
		importxmlSetProperty( loader , properties , var , value , false );
	}
	
	private static void importxmlSetProperty( EngineLoader loader , ObjectProperties properties , EntityVar var , String value , boolean manual ) throws Exception {
		if( var.isEnum() ) {
			// convert enum name (xml value) to enum code
			int code = DBEnums.getEnumCode(  var.enumClass , value );
			if( manual )
				properties.setManualIntProperty( var.NAME , code );
			else
				properties.setIntProperty( var.NAME , code );
		}
		else
		if( var.isObject() ) {
			int id = var.importxmlObjectValue( loader , value );
			if( manual )
				properties.setManualIntProperty( var.NAME , id );
			else
				properties.setIntProperty( var.NAME , id );
		}
		else {
			if( manual )
				properties.setManualStringProperty( var.NAME , value );
			else
				properties.setStringProperty( var.NAME , value );
		}
	}
	
	private static void importxmlSetProperty( EngineLoader loader , Node item , ObjectProperties properties , boolean appAsProperties ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity app = meta.getAppEntity();
		PropertyEntity custom = meta.getCustomEntity();
		
		String prop = ConfReader.getAttrValue( item , ATTR_NAME );
		
		// this.app - set app value
		EntityVar var = app.findXmlVar( prop );
		if( var != null && appAsProperties ) {
			String value = ConfReader.getAttrValue( item , ATTR_VALUE );
			importxmlSetProperty( loader , properties , var , value , false );
			return;
		}

		// this.app as custom
		if( var != null && appAsProperties == false )
			Common.exit1( _Error.SetSystemVarAsCustom1 , "Attempt to set built-in variable=" + prop + " as custom variable" , prop );
		
		// this.custom - duplicate
		if( custom != null ) {
			var = custom.findXmlVar( prop );
			if( var != null )
				Common.exit1( _Error.DuplicateCustomVar1 , "Duplicate custom variable=" + prop , prop );
		}

		// parent.app - override error
		var = findParentXmlVar( properties , prop );
		if( var != null && var.isApp() ) 
			Common.exit1( _Error.OverrideAppVar1 , "Attempt to override built-in variable=" + prop , prop );
		
		// parent.custom - normal override, set value as manual
		if( var != null && var.isCustom() ) {
			String value = ConfReader.getAttrValue( item , ATTR_VALUE );
			importxmlSetProperty( loader , properties , var , value , true );
			return;
		}

		// custom properties cannot be defined
		if( custom == null )
			Common.exit1( _Error.UnexpectedCustom1 , "Custom variables cannot be defined here, variable=" + prop , prop );
		
		// new custom string property
		String desc = ConfReader.getAttrValue( item , ATTR_DESC );
		String def = ConfReader.getAttrValue( item , ATTR_VALUE );
		var = EntityVar.metaString( prop , desc , var.REQUIRED , def );
		custom.addVar( var );
	}		

	private static EntityVar findParentXmlVar( ObjectProperties properties , String xmlprop ) {
		properties = properties.getParent();
		if( properties == null )
			return( null );
		
		ObjectMeta meta = properties.getMeta();
		EntityVar var = meta.findXmlVar( xmlprop );
		if( var != null )
			return( var );
		
		return( findParentXmlVar( properties , xmlprop ) );
	}

	public static void dropObjectSettings( DBConnection c , int ownerId ) throws Exception {
		if( !c.update( DBQueries.MODIFY_PARAM_DROPOWNERVALUES1 , new String[] {
				EngineDB.getInteger( ownerId )
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPOWNERPARAMS1 , new String[] {
				EngineDB.getInteger( ownerId )
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPOWNERENTITIES1 , new String[] {
				EngineDB.getInteger( ownerId )
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPOBJECTVALUES1 , new String[] {
				EngineDB.getInteger( ownerId )
				} ) )
			Common.exitUnexpected();
	}
	
	public static void dropdbEntity( DBConnection c , PropertyEntity entity ) throws Exception {
		int paramObjectId = entity.PARAM_OBJECT_ID;
		DBEnumParamEntityType entityType = entity.PARAMENTITY_TYPE;
		
		if( !c.update( DBQueries.MODIFY_PARAM_DROPENTITYVALUES2 , new String[] {
				EngineDB.getInteger( paramObjectId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPENTITYPARAMS2 , new String[] {
				EngineDB.getInteger( paramObjectId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPENTITY2 , new String[] {
				EngineDB.getInteger( paramObjectId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
	}
	
	private static void savedbPropertyEntity( DBConnection c , PropertyEntity entity , EntityVar[] vars , int version ) throws Exception {
		dropdbEntity( c , entity );
		insertEntity( c , entity , version );
		
		for( EntityVar var : vars ) {
			entity.addVar( var );
			insertVar( c , entity , var , version  );
		}
	}

	public static PropertyEntity savedbObjectEntity( DBConnection c , PropertyEntity entity , EntityVar[] vars ) throws Exception {
		int version = EngineDB.APP_VERSION;
		
		dropdbEntity( c , entity );
		insertEntity( c , entity , version );
		
		for( EntityVar var : vars ) {
			entity.addVar( var );
			insertVar( c , entity , var , version );
		}
		return( entity );
	}

	private static void insertEntity( DBConnection c , PropertyEntity entity , int version ) throws Exception {
		entity.VERSION = version;
		if( !c.update( DBQueries.MODIFY_PARAM_ADDENTITY11 , new String[] {
			EngineDB.getInteger( entity.PARAM_OBJECT_ID ) ,
			EngineDB.getEnum( entity.PARAMENTITY_TYPE ) ,
			EngineDB.getBoolean( entity.CUSTOM ) ,
			EngineDB.getBoolean( entity.USE_PROPS ) ,
			EngineDB.getString( entity.APP_TABLE ) ,
			EngineDB.getString( entity.ID_FIELD ) ,
			EngineDB.getEnum( entity.OBJECT_TYPE ) ,
			EngineDB.getInteger( entity.META_OBJECT_ID ) ,
			EngineDB.getEnum( entity.META_OBJECTVERSION_TYPE ) ,
			EngineDB.getEnum( entity.DATA_OBJECTVERSION_TYPE ) ,
			EngineDB.getInteger( version )
			} ) )
			Common.exitUnexpected();
	}

	private static void insertVar( DBConnection c , PropertyEntity entity , EntityVar var , int version ) throws Exception {
		var.PARAM_ID = DBNames.getNameIndex( c , entity.PARAM_OBJECT_ID , var.NAME , DBEnumObjectType.PARAM );
		var.VERSION = version;
		String enumName = ( var.enumClass == null )? null : DBEnums.getEnumName( var.enumClass );
		if( !c.update( DBQueries.MODIFY_PARAM_ADDPARAM15 , new String[] {
			EngineDB.getInteger( entity.PARAM_OBJECT_ID ) ,
			EngineDB.getEnum( entity.PARAMENTITY_TYPE ) ,
			EngineDB.getInteger( var.PARAM_ID ) ,
			EngineDB.getInteger( var.ENTITYCOLUMN ) ,
			EngineDB.getString( var.NAME ) ,
			EngineDB.getString( var.DBNAME ) ,
			EngineDB.getString( var.XMLNAME ) ,
			EngineDB.getString( var.DESC ) ,
			EngineDB.getEnum( var.PARAMVALUE_TYPE ) ,
			EngineDB.getEnum( var.PARAMVALUE_SUBTYPE ) ,
			EngineDB.getEnum( var.OBJECT_TYPE ) ,
			EngineDB.getString( enumName ) ,
			EngineDB.getBoolean( var.REQUIRED ) ,
			EngineDB.getString( var.EXPR_DEF ) ,
			EngineDB.getInteger( version )
			} ) )
			Common.exitUnexpected();
	}

	private static void updateVar( DBConnection c , PropertyEntity entity , EntityVar var , int version ) throws Exception {
		DBNames.updateName( c , entity.PARAM_OBJECT_ID , var.NAME , var.PARAM_ID , DBEnumObjectType.PARAM );
		var.VERSION = version;
		String enumName = ( var.enumClass == null )? null : DBEnums.getEnumName( var.enumClass );
		if( !c.update( DBQueries.MODIFY_PARAM_UPDATEPARAM15 , new String[] {
				EngineDB.getInteger( entity.PARAM_OBJECT_ID ) ,
				EngineDB.getEnum( entity.PARAMENTITY_TYPE ) ,
				EngineDB.getInteger( var.PARAM_ID ) ,
				EngineDB.getInteger( var.ENTITYCOLUMN ) ,
				EngineDB.getString( var.NAME ) ,
				EngineDB.getString( var.DBNAME ) ,
				EngineDB.getString( var.XMLNAME ) ,
				EngineDB.getString( var.DESC ) ,
				EngineDB.getEnum( var.PARAMVALUE_TYPE ) ,
				EngineDB.getEnum( var.PARAMVALUE_SUBTYPE ) ,
				EngineDB.getEnum( var.OBJECT_TYPE ) ,
				EngineDB.getString( enumName ) ,
				EngineDB.getBoolean( var.REQUIRED ) ,
				EngineDB.getString( var.EXPR_DEF ) ,
				EngineDB.getInteger( version )
				} ) )
				Common.exitUnexpected();
	}

	private static void deleteVar( DBConnection c , EntityVar var , int version ) throws Exception {
		var.VERSION = version;
		if( !c.update( DBQueries.MODIFY_PARAM_DROPPARAMVALUES1 , new String[] {
				EngineDB.getInteger( var.PARAM_ID ) 
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPPARAM1 , new String[] {
				EngineDB.getInteger( var.PARAM_ID ) 
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DECREMENTENTITYINDEX3 , new String[] {
				EngineDB.getInteger( var.entity.PARAM_OBJECT_ID ) ,
				EngineDB.getEnum( var.entity.PARAMENTITY_TYPE ) ,
				EngineDB.getInteger( var.ENTITYCOLUMN )
				} ) )
			Common.exitUnexpected();
	}
	
	public static PropertyEntity loaddbAppPropsEntity( EngineLoader loader , int paramObjectId , DBEnumObjectType objectType , DBEnumParamEntityType entityType , DBEnumObjectVersionType dataVersionType ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( objectType , entityType , dataVersionType );
		loaddbEntity( loader , entity , paramObjectId );
		return( entity );
	}
		
	public static void loaddbEntity( EngineLoader loader , PropertyEntity entity , int paramObjectId ) throws Exception {
		DBConnection c = loader.getConnection();
		entity.PARAM_OBJECT_ID = paramObjectId;
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETENTITYPARAMS2 , new String[] { 
				EngineDB.getInteger( entity.PARAM_OBJECT_ID ) , 
				EngineDB.getEnum( entity.PARAMENTITY_TYPE ) } );

		try {
			while( rs.next() ) {
				String enumName = rs.getString( 9 );
				Class<?> enumClass = ( enumName == null || enumName.isEmpty() )? null : DBEnums.getEnum( enumName );					
				EntityVar var = EntityVar.meta( 
						rs.getString( 2 ) , 
						rs.getString( 3 ) ,
						rs.getString( 4 ) , 
						rs.getString( 5 ) , 
						DBEnumParamValueType.getValue( rs.getInt( 6 ) , true ) , 
						DBEnumParamValueSubtype.getValue( rs.getInt( 7 ) , true ) ,
						DBEnumObjectType.getValue( rs.getInt( 8 ) , false ) ,
						rs.getBoolean( 10 ) , 
						rs.getString( 11 ) ,
						enumClass );
				var.PARAM_ID = rs.getInt( 1 );
				var.VERSION = rs.getInt( 12 );
				entity.addVar( var );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void savedbPropertyValues( DBConnection c , int objectId , ObjectProperties properties , boolean saveApp , int version ) throws Exception {
		if( !c.update( DBQueries.MODIFY_PARAM_DROPOBJECTPARAMVALUES2 , new String[] {
				EngineDB.getInteger( objectId ) ,
				EngineDB.getEnum( properties.type ) 
				} ) )
			Common.exitUnexpected();

		PropertySet set = properties.getProperties();
		for( PropertyValue value : set.getAllProperties() ) {
			String data = value.getOriginalValue();
			if( data == null || data.isEmpty() )
				continue;
			
			EntityVar var = properties.getVar( value.property );
			if( saveApp == false && var.isApp() )
				continue;
			
			if( !c.update( DBQueries.MODIFY_PARAM_ADDOBJECTPARAMVALUE7 , new String[] {
					EngineDB.getInteger( objectId ) ,
					EngineDB.getEnum( properties.type ) ,
					EngineDB.getInteger( var.entity.PARAM_OBJECT_ID ) ,
					EngineDB.getEnum( var.entity.PARAMENTITY_TYPE ) ,
					EngineDB.getInteger( var.PARAM_ID ) ,
					EngineDB.getString( data ) ,
					EngineDB.getInteger( version )
					} ) )
				Common.exitUnexpected();
		}
	}

	public static void savedbEntityCustom( DBConnection c , ObjectProperties properties , int paramObjectId , int metaObjectId , int version ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		if( entity == null )
			return;
		
		entity.PARAM_OBJECT_ID = paramObjectId;
		entity.META_OBJECT_ID = metaObjectId;
		savedbPropertyEntity( c , entity , entity.getVars() , version );
		meta.rebuild();
	}

	public static void modifyAppValues( DBConnection c , int objectId , ObjectProperties properties , int version , String[] dbonlyValues , boolean insert ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity entity = meta.getAppEntity();
		EntityVar[] vars = entity.getDatabaseVars();
		String[] values = new String[ vars.length ];

		int dbonlyVarCount = entity.getDatabaseOnlyVarCount();
		int valuesCount = ( dbonlyValues == null )? 0 : dbonlyValues.length;
		if( dbonlyVarCount != valuesCount )
			Common.exitUnexpected();
			
		int dbonlyPos = 0;
		for( int k = 0; k < vars.length; k++ ) {
			EntityVar var = vars[ k ];
			String value = null;
			if( var.isDatabaseOnly() )
				value = dbonlyValues[ dbonlyPos++ ];
			else
				value = properties.getOriginalPropertyValue( var.NAME );
			
			if( var.isString() )
				values[ k ] = EngineDB.getString( value );
			else
			if( var.isNumber() )
				values[ k ] = EngineDB.getIntegerString( value );
			else
			if( var.isBoolean() )
				values[ k ] = EngineDB.getBooleanString( value );
			else
			if( var.isEnum() )
				values[ k ] = EngineDB.getEnumString( value );
			else
				Common.exitUnexpected();
		}

		DBEngineEntities.modifyAppObject( c , entity , objectId , version , values , insert );
	}

	public static EntityVar createCustomProperty( EngineTransaction transaction , PropertyEntity entity , EntityVar var ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		int version = getEntityVersion( transaction , entity );
		entity.addVar( var );
		insertVar( c , entity , var , version );
		return( var );
	}
	
	public static void modifyCustomProperty( EngineTransaction transaction , EntityVar var ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		int version = getEntityVersion( transaction , var.entity );
		updateVar( c , var.entity , var , version );
		var.entity.updateVar( var );
	}

	public static void deleteCustomProperty( EngineTransaction transaction , EntityVar var ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		int version = getEntityVersion( transaction , var.entity );
		deleteVar( c , var , version );
		var.entity.removeVar( var );
	}

	private static int getEntityVersion( EngineTransaction transaction , PropertyEntity entity ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		int version = 0;
		if( entity.META_OBJECTVERSION_TYPE == DBEnumObjectVersionType.LOCAL )
			version = c.getNextLocalVersion();
		else
		if( entity.META_OBJECTVERSION_TYPE == DBEnumObjectVersionType.CORE )
			version = c.getNextCoreVersion();
		else
		if( entity.META_OBJECTVERSION_TYPE == DBEnumObjectVersionType.SYSTEM ) {
			EngineDirectory directory = transaction.getTransactionDirectory();
			AppSystem system = directory.getSystem( entity.META_OBJECT_ID );
			version = c.getNextSystemVersion( system );
		}
		else
			transaction.exitUnexpectedState();
		return( version );
	}

}

