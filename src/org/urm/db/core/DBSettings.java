package org.urm.db.core;

import java.sql.ResultSet;
import java.util.Map;

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
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.properties.PropertyValue;
import org.urm.meta.EngineLoader;
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
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETOBJECTPARAMVALUES2 , new String[] { EngineDB.getInteger( objectId ) , EngineDB.getEnum( properties.type ) } );
		if( rs == null )
			Common.exitUnexpected();
		
		while( rs.next() ) {
			int param = rs.getInt( 2 );
			String exprValue = rs.getString( 3 );
			
			EntityVar var = properties.getVar( param );
			if( saveApp == false && var.isApp() )
				Common.exitUnexpected();
			
			properties.setProperty( var , exprValue );
		}
		rs.close();
	}
	
	public static void importxml( EngineLoader loader , Node root , ObjectProperties properties , boolean appAsProperties , int objectId , boolean saveApp , int version ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		
		// load attributes - app only
		if( !appAsProperties ) {
			Map<String,String> attrs = ConfReader.getAttributes( root );
			for( String prop : Common.getSortedKeys( attrs ) ) {
				String value = attrs.get( prop );
				loadxmlSetAttr( loader , properties , prop , value );
			}
		}
		
		// load properties
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PROPERTY );
		if( items != null ) {
			for( Node item : items )
				loadxmlSetProperty( loader , item , properties , appAsProperties );
			
			meta.rebuild();
		}

		// save to database
		DBConnection c = loader.getConnection();
		savedbEntityCustom( c , properties , objectId , version );
		savedbValues( c , objectId , properties , saveApp , version );
	}

	public static void exportxml( EngineLoader loader , Document doc , Element root , ObjectProperties properties , boolean appAsProperties ) throws Exception {
		PropertySet set = properties.getProperties();
		ObjectMeta meta = properties.getMeta();
		PropertyEntity custom = meta.getCustomEntity();
		for( PropertyValue value : set.getAllProperties() ) {
			EntityVar var = properties.getVar( value.property );
			if( var.isApp() ) {
				String data = value.getOriginalValue();
				if( data == null || data.isEmpty() )
					continue;
				
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
		Common.xmlSetElementAttr( doc , root , var.NAME , data );
	}
	
	private static void exportxmlSetProperty( EngineLoader loader , Document doc , Element root , EntityVar var , String data , boolean defineProp ) throws Exception {
		Element property = Common.xmlCreatePropertyElement( doc , root , var.NAME , data );
		if( defineProp )
			Common.xmlSetElementAttr( doc , property , ATTR_DESC , var.DESC );
	}
	
	private static void loadxmlSetAttr( EngineLoader loader , ObjectProperties properties , String prop , String value ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity app = meta.getAppEntity();
		EntityVar var = app.findVar( prop );
		if( var == null )
			Common.exit1( _Error.UnknownAppVar1 , "Attempt to override built-in variable=" + prop , prop );
		
		properties.setProperty( prop , value );
	}
	
	private static void loadxmlSetProperty( EngineLoader loader , Node item , ObjectProperties properties , boolean appAsProperties ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity app = meta.getAppEntity();
		PropertyEntity custom = meta.getCustomEntity();
		
		String prop = ConfReader.getAttrValue( item , ATTR_NAME );
		
		// this.app - set app value
		EntityVar var = app.findVar( prop );
		if( var != null && appAsProperties ) {
			String value = ConfReader.getAttrValue( item , ATTR_VALUE );
			properties.setProperty( prop , value );
			return;
		}

		// this.app as custom
		if( var != null && appAsProperties == false )
			Common.exit1( _Error.SetSystemVarAsCustom1 , "Attempt to set built-in variable=" + prop + " as custom variable" , prop );
		
		// this.custom - duplicate
		if( custom != null ) {
			var = custom.findVar( prop );
			if( var != null )
				Common.exit1( _Error.DuplicateCustomVar1 , "Duplicate custom variable=" + prop , prop );
		}

		// parent.app - override error
		var = findParentVar( properties , prop );
		if( var != null && var.isApp() ) 
			Common.exit1( _Error.OverrideAppVar1 , "Attempt to override built-in variable=" + prop , prop );
		
		// parent.custom - normal override, set value as manual
		if( var != null && var.isCustom() ) {
			String value = ConfReader.getAttrValue( item , ATTR_VALUE );
			properties.setManualStringProperty( prop , value );
			return;
		}

		// custom properties cannot be defined
		if( custom == null )
			Common.exit1( _Error.UnexpectedCustom1 , "Custom variables cannot be defined here, variable=" + prop , prop );
		
		// new custom property
		String desc = ConfReader.getAttrValue( item , ATTR_DESC );
		String def = ConfReader.getAttrValue( item , ATTR_VALUE );
			
		var = EntityVar.metaString( prop , desc , false , def );
		custom.addVar( var );
	}		

	private static EntityVar findParentVar( ObjectProperties properties , String prop ) {
		properties = properties.getParent();
		if( properties == null )
			return( null );
		
		ObjectMeta meta = properties.getMeta();
		EntityVar var = meta.findVar( prop );
		if( var != null )
			return( var );
		
		return( findParentVar( properties , prop ) );
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
	
	public static void dropdbEntity( DBConnection c , DBEnumParamEntityType entityType , int ownerId ) throws Exception {
		if( !c.update( DBQueries.MODIFY_PARAM_DROPENTITYVALUES2 , new String[] {
				EngineDB.getInteger( ownerId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPENTITYPARAMS2 , new String[] {
				EngineDB.getInteger( ownerId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPENTITY2 , new String[] {
				EngineDB.getInteger( ownerId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
	}
	
	public static PropertyEntity savedbEntity( DBConnection c , DBEnumParamEntityType entityType , DBEnumObjectVersionType ownerType , int ownerId , boolean custom , int version , boolean saveAppAsProps , String appTable , EntityVar[] vars ) throws Exception {
		dropdbEntity( c , entityType , ownerId );
				
		PropertyEntity entity = new PropertyEntity( ownerType , ownerId , entityType , custom , saveAppAsProps , appTable );
		insertEntity( c , entity , version );
		
		for( EntityVar var : vars ) {
			entity.addVar( var );
			insertVar( c , entity , var , version );
		}
		return( entity );
	}

	public static void insertEntity( DBConnection c , PropertyEntity entity , int version ) throws Exception {
		entity.VERSION = version;
		if( !c.update( DBQueries.MODIFY_PARAM_ADDENTITY7 , new String[] {
			EngineDB.getInteger( entity.OWNER_OBJECT_ID ) ,
			EngineDB.getEnum( entity.PARAMENTITY_TYPE ) ,
			EngineDB.getBoolean( entity.CUSTOM ) ,
			EngineDB.getBoolean( entity.APP_PROPS ) ,
			EngineDB.getString( entity.APP_TABLE ) ,
			EngineDB.getEnum( entity.OWNER_OBJECT_TYPE ) ,
			EngineDB.getInteger( version )
			} ) )
			Common.exitUnexpected();
	}

	public static void insertVar( DBConnection c , PropertyEntity entity , EntityVar var , int version ) throws Exception {
		var.PARAM_ID = DBNames.getNameIndex( c , entity.OWNER_OBJECT_ID , var.NAME , DBEnumObjectType.PARAM );
		var.VERSION = version;
		if( !c.update( DBQueries.MODIFY_PARAM_ADDPARAM10 , new String[] {
			EngineDB.getInteger( entity.OWNER_OBJECT_ID ) ,
			EngineDB.getEnum( entity.PARAMENTITY_TYPE ) ,
			EngineDB.getInteger( var.PARAM_ID ) ,
			EngineDB.getString( var.NAME ) ,
			EngineDB.getString( var.DESC ) ,
			EngineDB.getEnum( var.PARAMVALUE_TYPE ) ,
			EngineDB.getEnum( var.OBJECT_TYPE ) ,
			EngineDB.getBoolean( var.REQUIRED ) ,
			EngineDB.getString( var.EXPR_DEF ) ,
			EngineDB.getInteger( version )
			} ) )
			Common.exitUnexpected();
	}

	public static PropertyEntity loaddbEntity( EngineLoader loader , DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType , boolean custom , boolean saveAppAsProps , String appTable ) throws Exception {
		PropertyEntity entity = new PropertyEntity( ownerType , 0 , entityType , custom , saveAppAsProps , appTable );
		loaddbEntity( loader , entity , ownerId );
		return( entity );
	}
		
	public static void loaddbEntity( EngineLoader loader , PropertyEntity entity , int ownerId ) throws Exception {
		DBConnection c = loader.getConnection();
		entity.OWNER_OBJECT_ID = ownerId;
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETENTITYPARAMS2 , new String[] { EngineDB.getInteger( entity.OWNER_OBJECT_ID ) , EngineDB.getEnum( entity.PARAMENTITY_TYPE ) } );
		if( rs == null )
			Common.exitUnexpected();
		
		while( rs.next() ) {
			EntityVar var = EntityVar.meta( 
					rs.getString( 2 ) , 
					rs.getString( 3 ) , 
					DBEnumParamValueType.getValue( rs.getInt( 4 ) , true ) , 
					DBEnumObjectType.getValue( rs.getInt( 5 ) , false ) , 
					rs.getBoolean( 6 ) , 
					rs.getString( 7 ) );
			var.PARAM_ID = rs.getInt( 1 );
			var.VERSION = rs.getInt( 8 );
			entity.addVar( var );
		}
		rs.close();
	}

	public static void savedbValues( DBConnection c , int objectId , ObjectProperties properties , boolean saveApp , int version ) throws Exception {
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
					EngineDB.getInteger( var.entity.OWNER_OBJECT_ID ) ,
					EngineDB.getEnum( var.entity.PARAMENTITY_TYPE ) ,
					EngineDB.getInteger( var.PARAM_ID ) ,
					EngineDB.getString( data ) ,
					EngineDB.getInteger( version )
					} ) )
				Common.exitUnexpected();
		}
	}

	public static PropertyEntity createEntityCustom( DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType ) {
		PropertyEntity entity = new PropertyEntity( ownerType , ownerId , entityType , true , false , null );
		return( entity );
	}
	
	public static void savedbEntityCustom( DBConnection c , ObjectProperties properties , int ownerId , int version ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		if( entity == null )
			return;
		
		entity.OWNER_OBJECT_ID = ownerId;
		if( entity != null )
			savedbEntity( c , entity.PARAMENTITY_TYPE , entity.OWNER_OBJECT_TYPE , entity.OWNER_OBJECT_ID , true , version , false , null , entity.getVars() );
		meta.rebuild();
	}
	
}

