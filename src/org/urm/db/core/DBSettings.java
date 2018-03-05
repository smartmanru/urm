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
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBSettings {

	public static String ELEMENT_PROPERTY = "property";
	public static String ELEMENT_CUSTOM = "custom";
	public static String ELEMENT_DEFINE = "define";
	public static String ATTR_NAME = "name"; 
	public static String ATTR_ENUMDEF = "enumdef";
	public static String ATTR_VALUE = "value";
	public static String ATTR_DESC = "desc";
	public static String ATTR_SECURED = "secured"; 
	public static String ATTR_TYPE = "type";
	public static String VALUE_ENUM = "enum";

	public static void loaddbValues( EngineLoader loader , int objectId , ObjectProperties properties ) throws Exception {
		DBConnection c = loader.getConnection();
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETOBJECTPARAMVALUES2 , new String[] { 
				EngineDB.getInteger( objectId ) , 
				EngineDB.getEnum( properties.roleType ) } );

		try {
			while( rs.next() ) {
				int param = rs.getInt( 2 );
				String exprValue = rs.getString( 3 );
				
				EntityVar var = properties.getVar( param );
				properties.setProperty( var , exprValue );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void importxml( EngineLoader loader , Node root , ObjectProperties properties , int paramObjectId , int metaObjectId , boolean importApp , boolean importCustom , int version ) throws Exception {
		importxmlLoad( loader , root , properties , importApp , importCustom , null );
		importxmlSave( loader , properties , paramObjectId , metaObjectId , importApp , importCustom , version , null );
	}

	public static void importxmlApp( EngineLoader loader , Node root , ObjectProperties properties , int paramObjectId , int metaObjectId , boolean importApp , boolean importCustom , int version ) throws Exception {
		importxmlLoad( loader , root , properties , true , false , null );
		importxmlSave( loader , properties , paramObjectId , -1 , true , false , version , null );
	}

	public static void importxml( EngineLoader loader , Node root , ObjectProperties properties , int paramObjectId , int metaObjectId , boolean importApp , boolean importCustom , int version , DBEnumParamEntityType entityType ) throws Exception {
		importxmlLoad( loader , root , properties , importApp , importCustom , entityType );
		importxmlSave( loader , properties , paramObjectId , metaObjectId , importApp , importCustom , version , entityType );
	}

	public static void importxmlApp( EngineLoader loader , Node root , ObjectProperties properties , int paramObjectId , int version , DBEnumParamEntityType entityType ) throws Exception {
		importxmlLoad( loader , root , properties , true , false , entityType );
		importxmlSave( loader , properties , paramObjectId , -1 , true , false , version , entityType );
	}

	public static void importxmlLoad( EngineLoader loader , Node root , ObjectProperties properties , boolean importApp , boolean importCustom ) throws Exception {
		importxmlLoad( loader , root , properties , importApp , importCustom , null );
	}
	
	public static void importxmlLoad( EngineLoader loader , Node root , ObjectProperties properties , boolean importApp , boolean importCustom , DBEnumParamEntityType entityType ) throws Exception {
		if( importApp )
			importxmlLoadApp( loader , root , properties , entityType );
		if( importCustom ) {
			Node custom = null;
			if( root != null )
				custom = ConfReader.xmlGetFirstChild( root , ELEMENT_CUSTOM );
			importxmlLoadCustom( loader , custom , properties );
		}
	}
	
	private static void importxmlLoadApp( EngineLoader loader , Node root , ObjectProperties properties , DBEnumParamEntityType entityType ) throws Exception {
		if( root == null )
			return;
		
		boolean ok = true;
		
		ObjectMeta meta = properties.getMeta();
		PropertyEntity entity = ( entityType == null )? null : meta.getAppEntity( entityType );
		boolean hasAttrs = ( entity == null )? meta.hasAppAttrs() : !entity.USE_PROPS; 
		boolean hasProps = ( entity == null )? meta.hasAppProps() : entity.USE_PROPS;
		
		// load attributes
		if( hasAttrs ) {
			Map<String,String> attrs = ConfReader.getAttributes( root );
			for( String prop : Common.getSortedKeys( attrs ) ) {
				String value = attrs.get( prop );
				try {
					importxmlGetAttr( loader , properties , prop , value , entity );
				}
				catch( Throwable e ) {
					loader.trace( "attribute load error: " + e.toString() );
					ok = false;
				}
			}
		}
		
		// load properties
		if( hasProps ) {
			Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PROPERTY );
			if( items != null ) {
				for( Node item : items ) {
					try {
						importxmlGetProperty( loader , item , properties , true , false , entity );
					}
					catch( Throwable e ) {
						loader.trace( "property load error: " + e.toString() );
						ok = false;
					}
				}
			}
		}

		if( !ok )
			Common.exit1( _Error.SettingsImportErrors1 , "Errors on settings import, set object=" + properties.objectType.name() , "" + properties.objectType.name() );
	}

	private static void importxmlLoadCustom( EngineLoader loader , Node root , ObjectProperties ops ) throws Exception {
		ObjectMeta meta = ops.getMeta();
		PropertyEntity custom = meta.getCustomEntity();
		
		if( custom != null ) {
			custom.clear();
			meta.rebuild();
		}

		if( root == null )
			return;
		
		boolean ok = true;
		
		// import property definition
		Node define = ConfReader.xmlGetFirstChild( root , ELEMENT_DEFINE );
		if( define != null ) {
			if( !ops.isCustomDefineAllowed() )
				Common.exitUnexpected();
			
			Node[] props = ConfReader.xmlGetChildren( define , ELEMENT_PROPERTY );
			if( props != null ) {
				for( Node item : props ) {
					try {
						importxmlGetDefineCustomProperty( loader , item , ops );
					}
					catch( Throwable e ) {
						loader.trace( "property definition load error: " + e.toString() );
						ok = false;
					}
				}
				
				meta.rebuild();
			}
			
			ops.createCustom();
		}
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PROPERTY );
		if( items != null ) {
			for( Node item : items ) {
				try {
					importxmlGetProperty( loader , item , ops , false , true , null );
				}
				catch( Throwable e ) {
					loader.trace( "property load error: " + e.toString() );
					ok = false;
				}
			}
			
			meta.rebuild();
		}

		if( !ok )
			Common.exit1( _Error.SettingsImportErrors1 , "Errors on settings import, set object=" + ops.objectType.name() , "" + ops.objectType.name() );
	}

	public static void importxmlSave( EngineLoader loader , ObjectProperties properties , int paramObjectId , int metaObjectId , boolean saveApp , boolean saveCustom , int version ) throws Exception {
		importxmlSave( loader , properties , paramObjectId , metaObjectId , saveApp , saveCustom , version , null );
	}
	
	public static void importxmlSave( EngineLoader loader , ObjectProperties properties , int paramObjectId , int metaObjectId , boolean saveApp , boolean saveCustom , int version , DBEnumParamEntityType entityType ) throws Exception {
		DBConnection c = loader.getConnection();
		if( saveCustom )
			savedbEntityCustom( c , properties , paramObjectId , metaObjectId , version );
		savedbPropertyValues( c , paramObjectId , properties , saveApp , saveCustom , version , entityType );
	}

	public static void exportxml( EngineLoader loader , Document doc , Element root , ObjectProperties properties , boolean appAsProperties ) throws Exception {
		exportxml( loader , doc , root , properties , true , true , appAsProperties , null );
	}
	
	public static void exportxml( EngineLoader loader , Document doc , Element root , ObjectProperties properties , boolean exportApp , boolean exportCustom , boolean appAsProperties , DBEnumParamEntityType entityType ) throws Exception {
		if( exportApp )
			exportxmlAppEntity( loader , doc , root , properties , appAsProperties , entityType );
		
		if( exportCustom ) {
			ObjectMeta meta = properties.getMeta();
			PropertyEntity custom = meta.getCustomEntity();
			
			if( custom != null ) {
				Element nodeCustom = Common.xmlCreateElement( doc , root , ELEMENT_CUSTOM );
				exportxmlCustomEntity( loader , doc , nodeCustom , properties );
			}
		}
	}

	public static void exportxmlCustomEntity( EngineLoader loader , Document doc , Element root , ObjectProperties properties ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity custom = meta.getCustomEntity();
		PropertySet set = properties.getProperties();

		Element nodeDefine = null;
		
		for( PropertyValue value : set.getAllProperties() ) {
			EntityVar var = properties.getVar( value.property );
			if( !var.isCustom() )
				continue;
			
			if( !var.isXml() )
				continue;
			
			boolean defineProp = ( var.isCustom() && var.entity == custom )? true : false;
			if( defineProp ) {
				if( !properties.isCustomDefineAllowed() )
					Common.exitUnexpected();
				
				if( nodeDefine == null )
					nodeDefine = Common.xmlCreateElement( doc , root , ELEMENT_DEFINE );
				
				Element nodeVar = Common.xmlCreateElement( doc , nodeDefine , ELEMENT_PROPERTY );
				Common.xmlSetElementAttr( doc , nodeVar , ATTR_NAME , var.NAME );
				Common.xmlSetElementAttr( doc , nodeVar , ATTR_DESC , var.DESC );
				Common.xmlSetElementAttr( doc , nodeVar , ATTR_TYPE , custom.exportxmlEnum( var.PARAMVALUE_TYPE ) );
				Common.xmlSetElementBooleanAttr( doc , nodeVar , ATTR_SECURED , var.SECURED );
				Common.xmlSetElementAttr( doc , nodeVar , ATTR_VALUE , var.EXPR_DEF );
				Common.xmlSetElementAttr( doc , nodeVar , ATTR_ENUMDEF , var.CUSTOMENUM_DEF );
			}
			
			String data = value.getOriginalValue();
			if( data == null || data.isEmpty() )
				continue;
			
			exportxmlSetProperty( loader , doc , root , var , data );
		}
	}

	public static void exportxmlAppEntity( EngineLoader loader , Document doc , Element root , ObjectProperties properties , boolean appAsProperties , DBEnumParamEntityType entityType ) throws Exception {
		PropertySet set = properties.getProperties();
		
		for( PropertyValue value : set.getAllProperties() ) {
			EntityVar var = properties.getVar( value.property );
			if( !var.isApp() )
				continue;
			
			if( !var.isXml() )
				continue;

			if( entityType != null && var.entity.PARAMENTITY_TYPE != entityType )
				continue;
			
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
				exportxmlSetProperty( loader , doc , root , var , data );
			else
				exportxmlSetAttr( loader , doc , root , var , data );
		}
	}
	
	private static void exportxmlSetAttr( EngineLoader loader, Document doc , Element root , EntityVar var , String data ) throws Exception {
		Common.xmlSetElementAttr( doc , root , var.XMLNAME , data );
	}
	
	private static void exportxmlSetProperty( EngineLoader loader , Document doc , Element root , EntityVar var , String data ) throws Exception {
		Common.xmlCreatePropertyElement( doc , root , var.XMLNAME , data );
	}
	
	private static void importxmlGetAttr( EngineLoader loader , ObjectProperties properties , String xmlprop , String value , PropertyEntity entityApp ) throws Exception {
		EntityVar var = null;
		if( entityApp != null )
			var = entityApp.findXmlVar( xmlprop );
		else {
			ObjectMeta meta = properties.getMeta();
			var = meta.findAppXmlVar( xmlprop );
		}
		
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
	
	private static void importxmlGetProperty( EngineLoader loader , Node item , ObjectProperties properties , boolean importApp , boolean importCustom , PropertyEntity entityApp ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		
		String prop = ConfReader.getAttrValue( item , ATTR_NAME );
		
		// this.app - set app value
		EntityVar var = meta.findAppXmlVar( prop );
		if( var != null ) {
			// this.app as custom
			if( !var.entity.USE_PROPS )
				Common.exit1( _Error.SetSystemVarAsCustom1 , "Attempt to set built-in variable=" + prop + " as custom variable" , prop );

			if( importApp == false )
				Common.exit1( _Error.UnexpectedAppVar1 , "Unexpected built-in variable=" + prop , prop );

			if( entityApp != null && entityApp != var.entity )
				Common.exit1( _Error.SetSystemVarWrongPlace1 , "Attempt to set built-in variable=" + prop + " in wrong place" , prop );
			
			String value = ConfReader.getAttrValue( item , ATTR_VALUE );
			importxmlSetProperty( loader , properties , var , value , false );
			return;
		}
		
		// this.custom - found
		var = meta.findCustomXmlVar( prop );
		if( var != null ) {
			if( importCustom == false )
				Common.exit1( _Error.UnexpectedCustomVar1 , "Unexpected custom variable=" + prop , prop );
			
			String value = ConfReader.getAttrValue( item , ATTR_VALUE );
			importxmlSetProperty( loader , properties , var , value , true );
			return;
		}

		// parent.app - override error
		var = findParentXmlVar( properties , prop );
		if( var != null && var.isApp() ) 
			Common.exit1( _Error.OverrideAppVar1 , "Attempt to override built-in variable=" + prop , prop );
		
		// parent.custom - normal override, set value as manual
		if( var != null && var.isCustom() ) {
			if( importCustom == false )
				Common.exit1( _Error.UnexpectedCustomVar1 , "Unexpected custom variable=" + prop , prop );
			
			String value = ConfReader.getAttrValue( item , ATTR_VALUE );
			importxmlSetProperty( loader , properties , var , value , true );
			return;
		}

		// custom properties should be defined before usage
		Common.exit1( _Error.UnexpectedCustom1 , "Custom variables cannot be defined here, variable=" + prop , prop );
	}		

	private static void importxmlGetDefineCustomProperty( EngineLoader loader , Node item , ObjectProperties properties ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity custom = meta.getCustomEntity();
		
		String prop = ConfReader.getAttrValue( item , ATTR_NAME );
		
		// this.app - set app value
		EntityVar var = meta.findAppXmlVar( prop );
		if( var != null )
			Common.exit1( _Error.UnexpectedAppVar1 , "Unexpected built-in variable=" + prop , prop );
		
		// this.custom - duplicate
		var = meta.findCustomXmlVar( prop );
		if( var != null )
			Common.exit1( _Error.DuplicateCustomVar1 , "Duplicate custom variable=" + prop , prop );

		// parent.app - override error
		var = findParentXmlVar( properties , prop );
		if( var != null && var.isApp() ) 
			Common.exit1( _Error.OverrideAppVar1 , "Attempt to override built-in variable=" + prop , prop );
		
		// custom properties cannot be defined
		if( custom == null )
			Common.exit1( _Error.UnexpectedCustom1 , "Custom variables cannot be defined here, variable=" + prop , prop );

		// new custom string property
		String desc = ConfReader.getAttrValue( item , ATTR_DESC );
		String typeName = ConfReader.getAttrValue( item , ATTR_TYPE );
		String exprDef = ConfReader.getAttrValue( item , ATTR_VALUE );
		String customEnumDef = ConfReader.getAttrValue( item , ATTR_ENUMDEF );
		boolean secured = ConfReader.getBooleanAttrValue( item , ATTR_SECURED , false );
		
		DBEnumParamValueType type = null;
		DBEnumParamValueSubType subType = null;
		String defValue = "";
		
		// special custom enum case
		String[] customEnums = null;
		if( typeName.equals( VALUE_ENUM ) ) {
			type = DBEnumParamValueType.STRING;
			subType = DBEnumParamValueSubType.CUSTOMENUM;
			customEnums = Common.splitSpaced( customEnumDef );
			if( customEnums.length == 0 )
				Common.exit1( _Error.UnexpectedEmptyCustomEnum1 , "Custom variables is defined as empty enum, variable=" + prop , prop );
		}
		else {
			if( typeName.isEmpty() )
				type = DBEnumParamValueType.STRING;
			else
				type = DBEnumParamValueType.getValue( typeName , true );
			subType = DBEnumParamValueSubType.DEFAULT;
			defValue = exprDef;
		}
		
		var = EntityVar.meta( prop , prop , prop , desc , type , subType , DBEnumObjectType.UNKNOWN , false , secured , defValue , null , customEnums );
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
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPOWNERVALUES1 , new String[] {
				EngineDB.getInteger( ownerId )
				} ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPOWNERPARAMS1 , new String[] {
				EngineDB.getInteger( ownerId )
				} ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPOWNERENTITIES1 , new String[] {
				EngineDB.getInteger( ownerId )
				} ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPOBJECTVALUES1 , new String[] {
				EngineDB.getInteger( ownerId )
				} ) )
			Common.exitUnexpected();
	}
	
	public static void dropdbEntity( DBConnection c , PropertyEntity entity ) throws Exception {
		int paramObjectId = entity.PARAM_OBJECT_ID;
		DBEnumParamEntityType entityType = entity.PARAMENTITY_TYPE;
		
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPENTITYVALUES2 , new String[] {
				EngineDB.getInteger( paramObjectId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPENTITYPARAMS2 , new String[] {
				EngineDB.getInteger( paramObjectId ) ,
				EngineDB.getEnum( entityType ) 
				} ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPENTITY2 , new String[] {
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
		if( !c.modify( DBQueries.MODIFY_PARAM_ADDENTITY11 , new String[] {
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
		
		if( !c.modify( DBQueries.MODIFY_PARAM_ADDPARAM17 , new String[] {
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
			EngineDB.getBoolean( var.SECURED ) ,
			EngineDB.getString( var.EXPR_DEF ) ,
			EngineDB.getString( var.CUSTOMENUM_DEF ) ,
			EngineDB.getInteger( version )
			} ) )
			Common.exitUnexpected();
	}

	private static void updateVar( DBConnection c , PropertyEntity entity , EntityVar var , int version ) throws Exception {
		DBNames.updateName( c , entity.PARAM_OBJECT_ID , var.NAME , var.PARAM_ID , DBEnumObjectType.PARAM );
		var.VERSION = version;
		String enumName = ( var.enumClass == null )? null : DBEnums.getEnumName( var.enumClass );
		if( !c.modify( DBQueries.MODIFY_PARAM_UPDATEPARAM17 , new String[] {
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
				EngineDB.getBoolean( var.SECURED ) ,
				EngineDB.getString( var.EXPR_DEF ) ,
				EngineDB.getString( var.CUSTOMENUM_DEF ) ,
				EngineDB.getInteger( version )
				} ) )
				Common.exitUnexpected();
	}

	private static void deleteVar( DBConnection c , EntityVar var , int version ) throws Exception {
		var.VERSION = version;
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPPARAMVALUES1 , new String[] {
				EngineDB.getInteger( var.PARAM_ID ) 
				} ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_PARAM_DROPPARAM1 , new String[] {
				EngineDB.getInteger( var.PARAM_ID ) 
				} ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_PARAM_DECREMENTENTITYINDEX3 , new String[] {
				EngineDB.getInteger( var.entity.PARAM_OBJECT_ID ) ,
				EngineDB.getEnum( var.entity.PARAMENTITY_TYPE ) ,
				EngineDB.getInteger( var.ENTITYCOLUMN )
				} ) )
			Common.exitUnexpected();
	}
	
	public static PropertyEntity loaddbCustomEntity( DBConnection c , ObjectMeta meta , int ownerId ) throws Exception {
		PropertyEntity entity = meta.getCustomEntity();
		DBSettings.loaddbEntity( c , entity , ownerId );
		meta.rebuild();
		return( entity );
	}

	public static void loaddbAppEntity( DBConnection c , PropertyEntity entity ) throws Exception {
		loaddbEntity( c , entity , DBVersions.APP_ID );
	}
	
	public static void loaddbEntity( DBConnection c , PropertyEntity entity , int paramObjectId ) throws Exception {
		entity.PARAM_OBJECT_ID = paramObjectId;
		entity.META_OBJECT_ID = ( entity.CUSTOM )? entity.PARAM_OBJECT_ID : DBVersions.APP_ID;
		entity.VERSION = verifyEntity( c , entity );
		
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETENTITYPARAMS2 , new String[] { 
				EngineDB.getInteger( entity.PARAM_OBJECT_ID ) , 
				EngineDB.getEnum( entity.PARAMENTITY_TYPE ) } );

		try {
			while( rs.next() ) {
				String enumName = rs.getString( 9 );
				Class<?> enumClass = ( enumName == null || enumName.isEmpty() )? null : DBEnums.getEnum( enumName );
				
				// handle custom enum case
				String[] customEnumList = null;
				String customEnumDef = "";
				DBEnumParamValueSubType subType = DBEnumParamValueSubType.getValue( rs.getInt( 7 ) , false );
				if( subType == DBEnumParamValueSubType.CUSTOMENUM ) {
					customEnumDef = rs.getString( 13 );
					customEnumList = Common.splitSpaced( customEnumDef );
				}
				
				EntityVar var = EntityVar.meta( 
						rs.getString( 2 ) , 
						rs.getString( 3 ) ,
						rs.getString( 4 ) , 
						rs.getString( 5 ) , 
						DBEnumParamValueType.getValue( rs.getInt( 6 ) , true ) , 
						subType ,
						DBEnumObjectType.getValue( rs.getInt( 8 ) , false ) ,
						rs.getBoolean( 10 ) , 
						rs.getBoolean( 11 ) ,
						rs.getString( 12 ) ,
						enumClass ,
						customEnumList );
				var.PARAM_ID = rs.getInt( 1 );
				var.VERSION = rs.getInt( 14 );
				entity.addVar( var );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void savedbPropertyValues( DBConnection c , int objectId , ObjectProperties properties , boolean saveApp , boolean saveCustom , int version ) throws Exception {
		savedbPropertyValues( c , objectId , properties , saveApp , saveCustom , version , null );
	}
	
	public static void savedbPropertyValues( DBConnection c , int objectId , ObjectProperties properties , boolean saveApp , boolean saveCustom , int version , DBEnumParamEntityType entityTypeApp ) throws Exception {
		if( saveApp && saveCustom == false && entityTypeApp != null ) {
			if( !c.modify( DBQueries.MODIFY_PARAM_DROPOBJECTENTITYPARAMVALUES3 , new String[] {
					EngineDB.getInteger( objectId ) ,
					EngineDB.getEnum( properties.roleType ) ,
					EngineDB.getEnum( entityTypeApp )
					} ) )
				Common.exitUnexpected();
		}
		else {
			if( saveApp && saveCustom ) {
				if( !c.modify( DBQueries.MODIFY_PARAM_DROPOBJECTPARAMVALUES2 , new String[] {
						EngineDB.getInteger( objectId ) ,
						EngineDB.getEnum( properties.roleType ) 
						} ) )
					Common.exitUnexpected();
			}
			else {
				if( saveApp ) {
					if( !c.modify( DBQueries.MODIFY_PARAM_DROPOBJECTPARAMVALUESAPP2 , new String[] {
							EngineDB.getInteger( objectId ) ,
							EngineDB.getEnum( properties.roleType ) 
							} ) )
						Common.exitUnexpected();
				}
				if( saveCustom ) {
					if( !c.modify( DBQueries.MODIFY_PARAM_DROPOBJECTPARAMVALUESCUSTOM2 , new String[] {
						EngineDB.getInteger( objectId ) ,
						EngineDB.getEnum( properties.roleType ) 
						} ) )
					Common.exitUnexpected();
				}
			}
		}

		ObjectMeta meta = properties.getMeta();
		PropertyEntity entityApp = ( entityTypeApp == null )? null : meta.getAppEntity( entityTypeApp ); 
		
		PropertySet set = properties.getProperties();
		for( PropertyValue value : set.getAllProperties() ) {
			EntityVar var = properties.getVar( value.property );
			if( saveApp == false && var.isApp() )
				continue;
			if( saveCustom == false && var.isCustom() )
				continue;
			if( saveApp == true && entityApp != null && var.entity != entityApp )
				continue;
			
			String data = value.getOriginalValue();
			if( data == null || data.isEmpty() )
				continue;
			
			if( !c.modify( DBQueries.MODIFY_PARAM_ADDOBJECTPARAMVALUE7 , new String[] {
					EngineDB.getInteger( objectId ) ,
					EngineDB.getEnum( properties.roleType ) ,
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

	public static void modifyAppValues( DBConnection c , int objectId , ObjectProperties properties , DBEnumParamEntityType entityType , int version , String[] dbonlyValues , boolean insert ) throws Exception {
		ObjectMeta meta = properties.getMeta();
		PropertyEntity entity = meta.getAppEntity( entityType );
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
		if( entity.META_OBJECTVERSION_TYPE == DBEnumObjectVersionType.PRODUCT ) {
			Meta meta = transaction.getTransactionMetadata( entity.META_OBJECT_ID );
			ProductMeta storage = transaction.getTransactionProductMetadata( meta );
			version = c.getNextProductVersion( storage );
		}
		else
			transaction.exitUnexpectedState();
		return( version );
	}

	public static void modifyCustomValues( EngineTransaction transaction , int objectId , ObjectProperties ops ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		int version = c.getNextCoreVersion();
		savedbPropertyValues( c , objectId , ops , false , true , version , null );
	}
	
	public static void modifyAppValues( EngineTransaction transaction , int objectId , ObjectProperties ops , DBEnumParamEntityType entityType ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		int version = c.getNextCoreVersion();
		savedbPropertyValues( c , objectId , ops , true , false , version , entityType );
	}

	private static int verifyEntity( DBConnection c , PropertyEntity entity ) throws Exception {
		ResultSet rc = c.query( DBQueries.QUERY_PARAM_ENTITY2 , new String[] {
				EngineDB.getInteger( entity.PARAM_OBJECT_ID ) ,
				EngineDB.getEnum( entity.PARAMENTITY_TYPE ) 
			} );
		
		if( rc == null )
			Common.exitUnexpected();
		
		try {
			if( rc.next() == false )
				Common.exitUnexpected();
				
			if( rc.getBoolean( 1 ) != entity.CUSTOM )
				Common.exitUnexpected();
			if( rc.getBoolean( 2 ) != entity.USE_PROPS )
				Common.exitUnexpected();
			if( !Common.equalsStrings( rc.getString( 3 ) , entity.APP_TABLE ) )
				Common.exitUnexpected();
			if( !Common.equalsStrings( rc.getString( 4 ) , entity.ID_FIELD ) )
				Common.exitUnexpected();
			if( rc.getInt( 5 ) != entity.OBJECT_TYPE.code() )
				Common.exitUnexpected();
			if( rc.getInt( 6 ) != entity.META_OBJECT_ID )
				Common.exitUnexpected();
			if( rc.getInt( 7 ) != entity.META_OBJECTVERSION_TYPE.code() )
				Common.exitUnexpected();
			if( rc.getInt( 8 ) != entity.DATA_OBJECTVERSION_TYPE.code() )
				Common.exitUnexpected();
		
			return( rc.getInt( 9 ) );
		}
		finally {
			c.closeQuery();
		}
	}
	
}

