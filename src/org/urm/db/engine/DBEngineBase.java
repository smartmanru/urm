package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.core.DBEnums.DBEnumBaseCategoryType;
import org.urm.db.core.DBEnums.DBEnumBaseSrcFormatType;
import org.urm.db.core.DBEnums.DBEnumBaseSrcType;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumServerAccessType;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.engine.EngineDB;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.BaseCategory;
import org.urm.meta.engine.BaseGroup;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineBase {

	public static String TABLE_BASEITEM = "urm_baseitem";
	public static String ELEMENT_CATEGORY = "category";
	public static String ELEMENT_GROUP = "group";
	public static String ELEMENT_ITEM = "item";
	public static String PROPERTY_GROUP_NAME = "id";
	public static String PROPERTY_GROUP_DESC = "desc";
	public static String PROPERTY_GROUP_OFFLINE = "offline";
	public static String PROPERTY_TYPE = "type";
	
	public static PropertyEntity upgradeEntityBaseItem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		return( DBSettings.savedbEntity( c , DBEnumParamEntityType.BASEITEM , DBEnumObjectVersionType.APP , DBVersions.APP_ID , false , EngineDB.APP_VERSION , true , TABLE_BASEITEM , new EntityVar[] { 
				EntityVar.metaString( BaseItem.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnum( BaseItem.PROPERTY_BASESRC_TYPE , "Base item type" , false , DBEnumBaseSrcType.UNKNOWN ) ,
				EntityVar.metaEnum( BaseItem.PROPERTY_BASESRCFORMAT_TYPE , "Base format type" , false , DBEnumBaseSrcFormatType.UNKNOWN ) ,
				EntityVar.metaEnum( BaseItem.PROPERTY_OS_TYPE , "Operating system" , false , DBEnumOSType.UNKNOWN ) ,
				EntityVar.metaEnum( BaseItem.PROPERTY_SERVERACCESS_TYPE , "Server access type" , false , DBEnumServerAccessType.UNKNOWN ) ,
				EntityVar.metaString( BaseItem.PROPERTY_BASENAME , "Base name" , false , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_BASEVERSION , "Base version" , false , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_SRCDIR , "Source directory" , false , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_SRCFILE , "Source file" , false , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_SRCFILEDIR , "Source file directory" , false , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_INSTALLSCRIPT , "Install script" , false , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_INSTALLPATH , "Install path" , false , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_INSTALLLINK , "Install link" , false , null ) ,
				EntityVar.metaString( BaseItem.PROPERTY_CHARSET , "Charset" , false , null ) ,
				EntityVar.metaBoolean( BaseItem.PROPERTY_OFFLINE , "Offline" , false , true )
		} ) );
	}

	public static PropertyEntity loaddbEntityBaseItem( EngineLoader loader ) throws Exception {
		return( DBSettings.loaddbEntity( loader , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.BASEITEM , false , true , DBEngineBase.TABLE_BASEITEM ) );
	}
	
	public static void importxml( EngineLoader loader , EngineBase base , Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_CATEGORY );
		if( list != null ) {
			for( Node node : list )
				importxmlCategory( loader , base , node );
		}
	}
	
	private static BaseCategory importxmlCategory( EngineLoader loader , EngineBase base , Node root ) throws Exception {
		DBEnumBaseCategoryType type = DBEnumBaseCategoryType.getValue( ConfReader.getAttrValue( root , PROPERTY_TYPE ) , true );
		BaseCategory category = base.getCategory( type );
		
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_GROUP );
		if( list != null ) {
			for( Node node : list ) {
				BaseGroup group = importxmlGroup( loader , base , category , node );
				base.addGroup( group );
			}
		}
		
		return( category );
	}

	private static BaseGroup importxmlGroup( EngineLoader loader , EngineBase base , BaseCategory category , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		
		BaseGroup group = new BaseGroup( category );
		group.NAME = ConfReader.getAttrValue( root , PROPERTY_GROUP_NAME );
		group.DESC = ConfReader.getAttrValue( root , PROPERTY_GROUP_DESC );
		group.OFFLINE = ConfReader.getBooleanAttrValue( root , PROPERTY_GROUP_OFFLINE , true );
		int groupId = getBaseGroupIdByName( c , group.NAME );
		insertGroup( c , groupId, group );
		
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_ITEM );
		if( list != null ) {
			for( Node node : list ) {
				BaseItem item = importxmlItem( loader , base , group , node );
				base.addItem( item );
			}
		}
		
		return( group );
	}
	
	private static BaseItem importxmlItem( EngineLoader loader , EngineBase base , BaseGroup group , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineSettings settings = loader.getData().getEngineSettings();
		
		ObjectProperties props = entities.createBaseItemProps( settings.getEngineProperties() ); 
		BaseItem item = new BaseItem( group , props );
		DBSettings.importxml( loader , root , props , false );
		
		item.NAME = props.getPropertyValue( BaseItem.PROPERTY_NAME );
		item.DESC = props.getPropertyValue( BaseItem.PROPERTY_DESC );
		item.BASESRC_TYPE = DBEnumBaseSrcType.getValue( props.getPropertyValue( BaseItem.PROPERTY_BASESRC_TYPE ) , false );
		item.BASESRCFORMAT_TYPE = DBEnumBaseSrcFormatType.getValue( props.getPropertyValue( BaseItem.PROPERTY_BASESRCFORMAT_TYPE ) , false );
		item.OS_TYPE = DBEnumOSType.getValue( props.getPropertyValue( BaseItem.PROPERTY_OS_TYPE ) , false );
		item.SERVERACCESS_TYPE = DBEnumServerAccessType.getValue( props.getPropertyValue( BaseItem.PROPERTY_SERVERACCESS_TYPE ) , false );
		item.BASENAME = props.getPropertyValue( BaseItem.PROPERTY_BASENAME );
		item.BASEVERSION = props.getPropertyValue( BaseItem.PROPERTY_BASEVERSION );
		item.SRCDIR = props.getPropertyValue( BaseItem.PROPERTY_SRCDIR );
		item.SRCFILE = props.getPropertyValue( BaseItem.PROPERTY_SRCFILE );
		item.SRCFILEDIR = props.getPropertyValue( BaseItem.PROPERTY_SRCFILEDIR );
		item.INSTALLSCRIPT = props.getPropertyValue( BaseItem.PROPERTY_INSTALLSCRIPT );
		item.INSTALLPATH = props.getPropertyValue( BaseItem.PROPERTY_INSTALLPATH );
		item.INSTALLLINK = props.getPropertyValue( BaseItem.PROPERTY_INSTALLLINK );
		item.CHARSET = props.getPropertyValue( BaseItem.PROPERTY_CHARSET );
		if( !item.isValid() )
			item.OFFLINE = true;
		
		int baseId = getBaseItemIdByName( c , item.NAME );
		insertItem( c , baseId , item );
		DBSettings.savedb( loader , props , baseId , false , item.CV ); 
		
		return( item );
	}

	public static int getBaseGroupIdByName( DBConnection c , String name ) throws Exception {
		return( DBNames.getNameIndex( c , DBVersions.CORE_ID , name , DBEnumObjectType.BASE_GROUP ) );
	}
	
	public static int getBaseItemIdByName( DBConnection c , String name ) throws Exception {
		return( DBNames.getNameIndex( c , DBVersions.CORE_ID , name , DBEnumObjectType.BASE_ITEM ) );
	}
	
	public static void exportxml( EngineLoader loader , EngineBase base , Document doc , Element root ) throws Exception {
		for( String id : base.getCategories() ) {
			BaseCategory category = base.findCategory( id );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_CATEGORY );
			exportxmlCategory( loader , category , doc , node );
		}
	}

	public static void loaddb( EngineLoader loader , EngineBase base ) throws Exception {
	}
	
	public static void exportxmlItem( EngineLoader loader , BaseItem item , Document doc , Element root ) throws Exception {
		DBSettings.exportxml( loader , doc , root , item.parameters , false );
	}
	
	public static void exportxmlGroup( EngineLoader loader , BaseGroup group , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , group.NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , group.DESC );
		
		for( BaseItem item : group.getItems() ) {
			Element element = Common.xmlCreateElement( doc , root , ELEMENT_ITEM );
			exportxmlItem( loader , item , doc , element );
		}
	}

	public static void exportxmlCategory( EngineLoader loader , BaseCategory category , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , PROPERTY_TYPE , Common.getEnumLower( category.TYPE ) );
		Common.xmlSetElementAttr( doc , root , "id" , category.LABEL );
		Common.xmlSetElementAttr( doc , root , "name" , category.NAME );
		
		for( BaseGroup group : category.getGroups() ) {
			Element element = Common.xmlCreateElement( doc , root , ELEMENT_GROUP );
			exportxmlGroup( loader , group , doc , element );
		}
	}

	public static void insertItem( DBConnection c , int baseId , BaseItem item ) throws Exception {
		item.ID = baseId;
		item.CV = c.getNextCoreVersion();
		if( !c.update( DBQueries.MODIFY_BASE_ADDITEM19 , new String[] {
				EngineDB.getInteger( item.ID ) , 
				EngineDB.getInteger( item.group.ID ) , 
				EngineDB.getString( item.NAME ) , 
				EngineDB.getString( item.DESC ) ,
				EngineDB.getEnum( item.BASESRC_TYPE ) ,
				EngineDB.getEnum( item.BASESRCFORMAT_TYPE ) , 
				EngineDB.getEnum( item.OS_TYPE ) ,
				EngineDB.getEnum( item.SERVERACCESS_TYPE ) ,
				EngineDB.getString( item.BASENAME ) ,
				EngineDB.getString( item.BASEVERSION ) ,
				EngineDB.getString( item.SRCDIR ) ,
				EngineDB.getString( item.SRCFILE ) ,
				EngineDB.getString( item.SRCFILEDIR ) ,
				EngineDB.getString( item.INSTALLSCRIPT ) ,
				EngineDB.getString( item.INSTALLPATH ) ,
				EngineDB.getString( item.INSTALLLINK ) ,
				EngineDB.getString( item.CHARSET ) ,
				EngineDB.getBoolean( item.OFFLINE ) ,
				EngineDB.getInteger( item.CV ) 
				} ) )
			Common.exitUnexpected();
	}

	public static void insertGroup( DBConnection c , int groupId , BaseGroup group ) throws Exception {
		group.ID = groupId;
		group.CV = c.getNextCoreVersion();
		if( !c.update( DBQueries.MODIFY_BASE_ADDGROUP6 , new String[] {
				EngineDB.getInteger( group.ID ) ,
				EngineDB.getEnum( group.category.TYPE ) ,
				EngineDB.getString( group.NAME ) , 
				EngineDB.getString( group.DESC ) ,
				EngineDB.getBoolean( group.OFFLINE ) ,
				EngineDB.getInteger( group.CV ) 
				} ) )
			Common.exitUnexpected();
	}

	public static BaseGroup createGroup( EngineTransaction transaction , EngineBase base , DBEnumBaseCategoryType type , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( base.findGroup( name ) != null )
			transaction.exitUnexpectedState();
		
		BaseCategory category = base.getCategory( type );
		BaseGroup group = new BaseGroup( category );
		int groupId = DBNames.getNameIndex( c , type.code() , name , DBEnumObjectType.BASE_GROUP );
		group.NAME = name;
		group.DESC = desc;
		insertGroup( c , groupId , group );
		
		base.addGroup( group );
		return( group );
	}
	
	public static void deleteGroup( EngineTransaction transaction , EngineBase base , BaseGroup group ) throws Exception {
	}
	
	public static void modifyGroup( EngineTransaction transaction , EngineBase base , BaseGroup group , String name , String desc ) throws Exception {
	}
	
	public static BaseItem createItem( EngineTransaction transaction , EngineBase base , BaseGroup group , String name , String desc ) throws Exception {
		return( null );
	}
	
	public static void modifyItem( EngineTransaction transaction , EngineBase base , BaseItem item , String name , String desc ) throws Exception {
	}
	
	public static void deleteItem( EngineTransaction transaction , EngineBase base , BaseItem item ) throws Exception {
	}
	
}
