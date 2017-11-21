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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBSettings {

	public static void loaddb( DBConnection c , int objectId , ObjectProperties properties ) throws Exception {
	}
	
	public static void loadxml( Node root , ObjectProperties properties , boolean valuesAreDefaults , boolean appAsProperties ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		
		// load attributes - app only
		Map<String,String> attrs = ConfReader.getAttributes( root );
		for( String prop : Common.getSortedKeys( attrs ) ) {
			String value = attrs.get( prop );
			loadxmlSetAttr( properties , prop , value );
		}
		
		// load properties
		Node[] items = ConfReader.xmlGetChildren( root , "property" );
		if( items != null ) {
			for( Node item : items )
				loadxmlSetProperty( item , properties , valuesAreDefaults , appAsProperties );
			
			meta.rebuild();
		}
	}

	public static void savexml( Document doc , Element root , ObjectProperties properties , boolean valuesAreDefaults , boolean appAsProperties ) throws Exception {
	}
	
	private static void loadxmlSetAttr( ObjectProperties properties , String prop , String value ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity app = meta.getAppEntity();
		EntityVar var = app.findVar( prop );
		if( var == null )
			Common.exit1( _Error.UnknownAppVar1 , "Attempt to override built-in variable=" + prop , prop );
		
		properties.setProperty( prop , value , null );
	}
	
	private static void loadxmlSetProperty( Node item , ObjectProperties properties , boolean valuesAreDefaults , boolean appAsProperties ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity app = meta.getAppEntity();
		PropertyEntity custom = meta.getCustomEntity();
		
		String prop = ConfReader.getAttrValue( item , "name" );
		
		// this.app - set app value
		EntityVar var = app.findVar( prop );
		if( var != null && appAsProperties ) {
			String value = ConfReader.getAttrValue( item , "value" );
			properties.setProperty( prop , value , null );
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
			String value = ConfReader.getAttrValue( item , "value" );
			properties.setManualStringProperty( prop , value );
			return;
		}

		// custom properties cannot be defined
		if( custom == null )
			Common.exit1( _Error.UnexpectedCustom1 , "Custom variables are not expected here, variable==" + prop , prop );
		
		// new property
		String desc = ConfReader.getAttrValue( item , "desc" );
		String def = null;
		if( valuesAreDefaults )
			def = ConfReader.getAttrValue( item , "value" );
			
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
		PropertyEntity entity = new PropertyEntity( ownerType , ownerId , entityType , custom );
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETENTITYPARAMS3 , new String[] { EngineDB.getInteger( ownerId ) , EngineDB.getEnum( entityType ) , EngineDB.getBoolean( custom ) } );
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
		return( entity );
	}
	
}

