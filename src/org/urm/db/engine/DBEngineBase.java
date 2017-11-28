package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
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

	public static String TABLE_BASEGROUP = "urm_base_group";
	public static String TABLE_BASEITEM = "urm_base_item";
	public static String ELEMENT_CATEGORY = "category";
	public static String ELEMENT_GROUP = "group";
	public static String ELEMENT_ITEM = "item";
	public static String XMLPROP_GROUP_NAME = "id";
	public static String XMLPROP_ITEM_NAME = "id";
	public static String FIELD_GROUP_ID = "group_id";
	public static String FIELD_GROUP_CATEGORY = "basecategory_type";
	public static String FIELD_GROUP_DESC = "xdesc";
	public static String FIELD_ITEM_ID = "item_id";
	public static String FIELD_ITEM_DESC = "xdesc";
	
	public static PropertyEntity upgradeEntityBaseGroup( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BASE_GROUP , DBEnumParamEntityType.BASEGROUP , DBEnumObjectVersionType.CORE , TABLE_BASEGROUP , FIELD_GROUP_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaStringXmlOnly( BaseGroup.PROPERTY_TYPE , "Type" , true , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_GROUP_CATEGORY , "Category" , true , null ) ,
				EntityVar.metaStringVar( BaseGroup.PROPERTY_NAME , BaseGroup.PROPERTY_NAME , XMLPROP_GROUP_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( BaseGroup.PROPERTY_DESC , FIELD_GROUP_DESC , BaseGroup.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( BaseGroup.PROPERTY_OFFLINE , "Offline" , false , true )
		} ) );
	}

	public static PropertyEntity upgradeEntityBaseItem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BASE_ITEM , DBEnumParamEntityType.BASEITEM , DBEnumObjectVersionType.CORE , TABLE_BASEITEM , FIELD_ITEM_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaIntegerDatabaseOnly( FIELD_GROUP_ID , "Name" , true , null ) ,
				EntityVar.metaStringVar( BaseItem.PROPERTY_NAME , BaseItem.PROPERTY_NAME , XMLPROP_ITEM_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( BaseItem.PROPERTY_DESC , FIELD_ITEM_DESC , BaseItem.PROPERTY_DESC , "Description" , false , null ) ,
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

	public static PropertyEntity loaddbEntityBaseGroup( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BASE_GROUP , DBEnumParamEntityType.BASEGROUP , DBEnumObjectVersionType.CORE , TABLE_BASEGROUP , FIELD_GROUP_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static PropertyEntity loaddbEntityBaseItem( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BASE_ITEM , DBEnumParamEntityType.BASEITEM , DBEnumObjectVersionType.CORE , TABLE_BASEITEM , FIELD_ITEM_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static void importxml( EngineLoader loader , EngineBase base , Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_CATEGORY );
		if( list != null ) {
			for( Node node : list )
				importxmlCategory( loader , base , node );
		}
	}
	
	private static BaseCategory importxmlCategory( EngineLoader loader , EngineBase base , Node root ) throws Exception {
		DBEnumBaseCategoryType type = DBEnumBaseCategoryType.getValue( ConfReader.getAttrValue( root , BaseCategory.PROPERTY_TYPE ) , true );
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
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppBaseGroup;
		
		BaseGroup group = new BaseGroup( category );
		group.NAME = entity.getAttrValue( root , BaseGroup.PROPERTY_NAME );
		group.DESC = entity.getAttrValue( root , BaseGroup.PROPERTY_DESC );
		group.OFFLINE = entity.getBooleanAttrValue( root , BaseGroup.PROPERTY_OFFLINE , true );
		int groupId = getBaseGroupIdByName( c , group.NAME );
		insertGroup( c , groupId , group );
		
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
		DBSettings.importxmlLoad( loader , root , props );
		
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
		DBSettings.importxmlSave( loader , props , baseId , DBVersions.CORE_ID , false , item.CV ); 
		
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
		EngineEntities entities = loader.getEntities();
		DBEngineEntities.exportxmlAppObject( doc , root , entities.entityAppBaseGroup , new String[] {
				group.NAME ,
				group.DESC
		});
		
		for( BaseItem item : group.getItems() ) {
			Element element = Common.xmlCreateElement( doc , root , ELEMENT_ITEM );
			exportxmlItem( loader , item , doc , element );
		}
	}

	public static void exportxmlCategory( EngineLoader loader , BaseCategory category , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , BaseCategory.PROPERTY_TYPE , Common.getEnumLower( category.TYPE ) );
		
		for( BaseGroup group : category.getGroups() ) {
			Element element = Common.xmlCreateElement( doc , root , ELEMENT_GROUP );
			exportxmlGroup( loader , group , doc , element );
		}
	}

	public static void insertItem( DBConnection c , int baseId , BaseItem item ) throws Exception {
		item.ID = baseId;
		item.CV = c.getNextCoreVersion();
		DBSettings.savedbAppValues( c , item.ID , item.parameters , item.CV , new String[] {
				EngineDB.getInteger( item.group.ID )
		});
	}

	public static void insertGroup( DBConnection c , int groupId , BaseGroup group ) throws Exception {
		group.ID = groupId;
		group.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.insertAppObject( c , entities.entityAppBaseGroup , group.ID , group.CV , new String[] {
				EngineDB.getEnum( group.category.TYPE ) ,
				EngineDB.getString( group.NAME ) , 
				EngineDB.getString( group.DESC ) ,
				EngineDB.getBoolean( group.OFFLINE )
				} );
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
