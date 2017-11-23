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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBSettings {

	public static String ELEMENT_PROPERTY = "property"; 
	public static String ATTR_NAME = "name"; 
	public static String ATTR_VALUE = "value"; 
	public static String ATTR_DESC = "desc"; 
	
	public static void loaddbValues( DBConnection c , int objectId , ObjectProperties properties , boolean saveApp ) throws Exception {
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
	
	public static void importxml( Node root , ObjectProperties properties , boolean appAsProperties ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		
		// load attributes - app only
		if( !appAsProperties ) {
			Map<String,String> attrs = ConfReader.getAttributes( root );
			for( String prop : Common.getSortedKeys( attrs ) ) {
				String value = attrs.get( prop );
				loadxmlSetAttr( properties , prop , value );
			}
		}
		
		// load properties
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PROPERTY );
		if( items != null ) {
			for( Node item : items )
				loadxmlSetProperty( item , properties , appAsProperties );
			
			meta.rebuild();
		}
	}

	public static void exportxml( Document doc , Element root , ObjectProperties properties , boolean appAsProperties ) throws Exception {
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
					exportxmlSetProperty( doc , root , var , data , false );
				else
					exportxmlSetAttr( doc , root , var , data );
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
				
				exportxmlSetProperty( doc , root , var , data , defineProp );
			}
		}
	}

	private static void exportxmlSetAttr( Document doc , Element root , EntityVar var , String data ) throws Exception {
		Common.xmlSetElementAttr( doc , root , var.NAME , data );
	}
	
	private static void exportxmlSetProperty( Document doc , Element root , EntityVar var , String data , boolean defineProp ) throws Exception {
		Element property = Common.xmlCreatePropertyElement( doc , root , var.NAME , data );
		if( defineProp )
			Common.xmlSetElementAttr( doc , property , ATTR_DESC , var.DESC );
	}
	
	private static void loadxmlSetAttr( ObjectProperties properties , String prop , String value ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity app = meta.getAppEntity();
		EntityVar var = app.findVar( prop );
		if( var == null )
			Common.exit1( _Error.UnknownAppVar1 , "Attempt to override built-in variable=" + prop , prop );
		
		properties.setProperty( prop , value );
	}
	
	private static void loadxmlSetProperty( Node item , ObjectProperties properties , boolean appAsProperties ) throws Exception {
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
	
	public static PropertyEntity savedbEntity( DBConnection c , DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType , boolean custom , int version , EntityVar[] vars ) throws Exception {
		if( !c.update( DBQueries.MODIFY_PARAM_DROPENTITYVALUESS2 , new String[] {
				EngineDB.getInteger( ownerId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_PARAM_DROPENTITYPARAMS2 , new String[] {
				EngineDB.getInteger( ownerId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
				
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
		if( !c.update( DBQueries.MODIFY_PARAM_ADD11 , new String[] {
			EngineDB.getInteger( entity.ownerId ) ,
			EngineDB.getEnum( entity.entityType ) ,
			EngineDB.getInteger( var.ID ) ,
			EngineDB.getString( var.NAME ) ,
			EngineDB.getString( var.DESC ) ,
			EngineDB.getEnum( var.PARAMVALUE_TYPE ) ,
			EngineDB.getEnum( var.OBJECT_TYPE ) ,
			EngineDB.getBoolean( var.REQUIRED ) ,
			EngineDB.getBoolean( entity.custom ) ,
			EngineDB.getString( var.EXPR_DEF ) ,
			EngineDB.getInteger( version )
			} ) )
			Common.exitUnexpected();
	}

	public static PropertyEntity loaddbEntity( DBConnection c , DBEnumObjectVersionType ownerType , int ownerId , DBEnumParamEntityType entityType , boolean custom ) throws Exception {
		PropertyEntity entity = new PropertyEntity( ownerType , 0 , entityType , custom );
		loaddbEntity( c , entity , ownerId );
		return( entity );
	}
		
	public static void loaddbEntity( DBConnection c , PropertyEntity entity , int ownerId ) throws Exception {
		entity.ownerId = ownerId;
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETENTITYPARAMS2 , new String[] { EngineDB.getInteger( entity.ownerId ) , EngineDB.getEnum( entity.entityType ) } );
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
			var.ID = rs.getInt( 1 );
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
					EngineDB.getInteger( var.entity.ownerId ) ,
					EngineDB.getEnum( var.entity.entityType ) ,
					EngineDB.getInteger( var.ID ) ,
					EngineDB.getString( data ) ,
					EngineDB.getInteger( version )
					} ) )
				Common.exitUnexpected();
		}
	}

	public static void savedbEntityCustom( DBConnection c , ObjectProperties properties , int version ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		if( entity != null )
			savedbEntity( c , DBEnumObjectVersionType.CORE , DBVersions.CORE_ID , DBEnumParamEntityType.ENGINE_CUSTOM , true , version , entity.getVars() );
	}
	
}

