package org.urm.db.engine;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumBaseCategoryType;
import org.urm.db.core.DBEnums.DBEnumBaseSrcFormatType;
import org.urm.db.core.DBEnums.DBEnumBaseSrcType;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumParamRoleType;
import org.urm.db.core.DBEnums.DBEnumServerAccessType;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.engine.BaseCategory;
import org.urm.meta.engine.BaseGroup;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.loader.EngineLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineBase {

	public static String TABLE_BASEGROUP = "urm_base_group";
	public static String TABLE_BASEITEM = "urm_base_item";
	public static String TABLE_BASEITEMDATA = "urm_base_data";
	public static String ELEMENT_CATEGORY = "category";
	public static String ELEMENT_GROUP = "group";
	public static String ELEMENT_ITEM = "item";
	public static String ELEMENT_DEPITEM = "dependency";
	public static String XMLPROP_ITEM_DEPNAME = "name";
	public static String FIELD_GROUP_ID = "group_id";
	public static String FIELD_GROUP_CATEGORY = "basecategory_type";
	public static String FIELD_GROUP_DESC = "xdesc";
	public static String FIELD_ITEM_ID = "baseitem_id";
	public static String FIELD_ITEM_GROUP_ID = "group_id";
	public static String FIELD_ITEM_DESC = "xdesc";
	public static String FIELD_ITEMDATA_ID = "baseitem_id";
	
	public static PropertyEntity makeEntityBaseGroup( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BASE_GROUP , DBEnumParamEntityType.BASEGROUP , DBEnumObjectVersionType.CORE , TABLE_BASEGROUP , FIELD_GROUP_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaStringXmlOnly( BaseGroup.PROPERTY_TYPE , "Type" , true , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_GROUP_CATEGORY , "Category" , true , null ) ,
				EntityVar.metaStringVar( BaseGroup.PROPERTY_NAME , BaseGroup.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( BaseGroup.PROPERTY_DESC , FIELD_GROUP_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( BaseGroup.PROPERTY_OFFLINE , "Offline" , false , true )
		} ) );
	}

	public static PropertyEntity makeEntityBaseItem( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BASE_ITEM , DBEnumParamEntityType.BASEITEM , DBEnumObjectVersionType.CORE , TABLE_BASEITEM , FIELD_ITEM_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaIntegerDatabaseOnly( FIELD_ITEM_GROUP_ID , "Name" , true , null ) ,
				EntityVar.metaStringVar( BaseItem.PROPERTY_NAME , BaseItem.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( BaseItem.PROPERTY_DESC , FIELD_ITEM_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( BaseItem.PROPERTY_OFFLINE , "Offline" , false , true )
		} ) );
	}

	public static PropertyEntity makeEntityBaseItemData( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BASE_ITEMDATA , DBEnumParamEntityType.BASEITEMDATA , DBEnumObjectVersionType.CORE , TABLE_BASEITEMDATA , FIELD_ITEMDATA_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaBoolean( BaseItem.PROPERTY_ADMIN , "Administrative" , false , false ) ,
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
		} ) );
	}

	public static void importxml( EngineLoader loader , EngineBase base , Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_CATEGORY );
		if( list != null ) {
			for( Node node : list )
				importxmlCategory( loader , base , node );
		}
		
		for( BaseItem item : base.getItems() )
			importxmlItemResolve( loader , base , item );
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
		group.createGroup( 
				entity.importxmlStringAttr( root , BaseGroup.PROPERTY_NAME ) , 
				entity.importxmlStringAttr( root , BaseGroup.PROPERTY_DESC ) 
				);
		group.setOffline( entity.importxmlBooleanAttr( root , BaseGroup.PROPERTY_OFFLINE , true ) );
		modifyGroup( c , group , true );
		
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
		EngineSettings settings = loader.getSettings();
		
		ObjectProperties ops = entities.createBaseItemProps( settings.getEngineProperties() ); 
		BaseItem item = new BaseItem( group , ops );
		DBSettings.importxmlLoadCustomEntity( loader , root , ops );
		DBSettings.importxmlLoad( loader , root , ops , true , false );
		item.scatterPropertiesPrimary();
		item.scatterPropertiesData();
		
		if( !item.isValid() )
			item.setOffline( true );
		
		int version = c.getNextCoreVersion();
		modifyItem( c , item , true );
		if( item.isImplemented() )
			modifyItemData( c , item , true );
		ops.setOwnerId( item.ID );
		
		DBSettings.savedbEntityCustom( c , ops , version );
		DBSettings.importxmlSave( loader , ops , item.ID , DBVersions.CORE_ID , true , false , item.CV ); 
		
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_DEPITEM );
		if( list != null ) {
			for( Node node : list ) {
				String name = ConfReader.getAttrValue( node , XMLPROP_ITEM_DEPNAME );
				item.addDepDraft( name );
			}
		}
		
		return( item );
	}

	private static void importxmlItemResolve( EngineLoader loader , EngineBase base , BaseItem item ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		
		int version = item.CV;
		for( String name : item.getDepItemDraftNames() ) {
			BaseItem dep = base.getItem( name );
			addItemDependency( c , entities , item , dep , version );
		}
		
		item.clearDrafts();
	}
	
	public static void exportxml( EngineLoader loader , EngineBase base , Document doc , Element root ) throws Exception {
		for( String id : base.getCategoryNames() ) {
			BaseCategory category = base.findCategory( id );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_CATEGORY );
			exportxmlCategory( loader , category , doc , node );
		}
	}

	public static void exportxmlCategory( EngineLoader loader , BaseCategory category , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , BaseCategory.PROPERTY_TYPE , Common.getEnumLower( category.BASECATEGORY_TYPE ) );
		
		for( String name : category.getGroupNames() ) {
			BaseGroup group = category.findGroup( name );
			Element element = Common.xmlCreateElement( doc , root , ELEMENT_GROUP );
			exportxmlGroup( loader , group , doc , element );
		}
	}

	public static void exportxmlGroup( EngineLoader loader , BaseGroup group , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppBaseGroup;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlEnum( group.category.BASECATEGORY_TYPE ) ,
				entity.exportxmlString( group.NAME ) ,
				entity.exportxmlString( group.DESC ) ,
				entity.exportxmlBoolean( group.OFFLINE )
		} , true );
		
		for( String name : group.getItemNames() ) {
			BaseItem item = group.findItem( name );
			Element element = Common.xmlCreateElement( doc , root , ELEMENT_ITEM );
			exportxmlItem( loader , item , doc , element );
		}
	}

	public static void exportxmlItem( EngineLoader loader , BaseItem item , Document doc , Element root ) throws Exception {
		DBSettings.exportxml( loader , doc , root , item.ops , false );
		
		for( String name : item.getDepItemNames() ) {
			BaseItem dep = item.findDepItem( name );
			Element element = Common.xmlCreateElement( doc , root , ELEMENT_DEPITEM );
			exportxmlDepItem( loader , item , dep , doc , element );
		}
	}

	public static void exportxmlDepItem( EngineLoader loader , BaseItem item , BaseItem dep , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , XMLPROP_ITEM_DEPNAME , dep.NAME );
	}
	
	public static void loaddb( EngineLoader loader , EngineBase base ) throws Exception {
		DBConnection c = loader.getConnection();
		
		loaddbGroups( loader , base );
		loaddbItems( loader , base );
		loaddbItemsData( loader , base );
		loaddbItemDeps( loader , base );

		// load base item custom meta
		for( BaseItem item : base.getItems() ) {
			ObjectProperties p = item.getParameters();
			ObjectMeta meta = p.getMeta();
			PropertyEntity custom = meta.getCustomEntity();
			DBSettings.loaddbEntity( c , custom , item.ID , false );
		}
	}

	public static void loaddbGroups( EngineLoader loader , EngineBase base ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppBaseGroup;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				DBEnumBaseCategoryType type = DBEnumBaseCategoryType.getValue( entity.loaddbEnum( rs , FIELD_GROUP_CATEGORY ) , true );
				BaseCategory category = base.getCategory( type );
				
				BaseGroup group = new BaseGroup( category );
				group.ID = entity.loaddbId( rs );
				group.CV = entity.loaddbVersion( rs );
				group.createGroup( 
						entity.loaddbString( rs , BaseGroup.PROPERTY_NAME ) , 
						entity.loaddbString( rs , BaseGroup.PROPERTY_DESC ) );
				group.setOffline( entity.loaddbBoolean( rs , BaseGroup.PROPERTY_OFFLINE ) );
						
				base.addGroup( group );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void loaddbItems( EngineLoader loader , EngineBase base ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineSettings settings = loader.getSettings();
		ObjectProperties pe = settings.getEngineProperties();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppBaseItem;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				int groupId = entity.loaddbInt( rs , FIELD_ITEM_GROUP_ID );
				BaseGroup group = base.getGroup( groupId );
		
				ObjectProperties p = entities.createBaseItemProps( pe );
				DBEngineEntities.loaddbAppObject( rs , p , entity );
				
				BaseItem item = new BaseItem( group , p );
				item.ID = entity.loaddbId( rs );
				item.CV = entity.loaddbVersion( rs );
				item.scatterPropertiesPrimary();
				item.scatterPropertiesData();

				p.setOwnerId( item.ID );
				base.addItem( item );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbItemsData( EngineLoader loader , EngineBase base ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppBaseItemData;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				int itemId = entity.loaddbId( rs );
				BaseItem item = base.getItem( itemId );
		
				ObjectProperties p = item.getParameters();
				DBEngineEntities.loaddbAppObject( rs , p , entity );
				item.scatterPropertiesData();
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbItemDeps( EngineLoader loader , EngineBase base ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		
		ResultSet rs = c.query( DBQueries.QUERY_BASE_ITEMDEPS0 );
		try {
			while( rs.next() ) {
				int itemId = rs.getInt( 1 );
				int depId = rs.getInt( 2 );
				
				BaseItem item = base.getItem( itemId );
				BaseItem dep = base.getItem( depId );
				item.addDepItem( entities , dep );
			}
		}
		finally {
			c.closeQuery();
		}
		
		loaddbDependencyValues( loader , base );
	}
	
	private static void loaddbDependencyValues( EngineLoader loader , EngineBase base ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ResultSet rs = c.query( DBQueries.QUERY_PARAM_GETPARAMROLEVALUES1 , new String[] { 
				EngineDB.getEnum( DBEnumParamRoleType.BASEITEM_DEPENDENCY ) } );

		try {
			while( rs.next() ) {
				int objectId = rs.getInt( 1 );
				int paramObjectId = rs.getInt( 2 );
				int param = rs.getInt( 4 );
				String exprValue = rs.getString( 5 );
				
				BaseItem item = base.getItem( objectId );
				ObjectProperties ops = item.getDependencySettings( paramObjectId );
				
				EntityVar var = ops.getVar( param );
				ops.setProperty( var , exprValue );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void modifyItem( DBConnection c , BaseItem item , boolean insert ) throws Exception {
		if( insert )
			item.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , item.NAME , DBEnumParamEntityType.BASEITEM );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , item.NAME , item.ID , DBEnumParamEntityType.BASEITEM );
		
		item.CV = c.getNextCoreVersion();
		DBSettings.modifyAppValues( c , item.ID , item.ops , DBEnumParamEntityType.BASEITEM , item.CV , new String[] {
				EngineDB.getInteger( item.group.ID )
		} , insert );
	}

	private static void modifyItemData( DBConnection c , BaseItem item , boolean insert ) throws Exception {
		item.CV = c.getNextCoreVersion();
		DBSettings.modifyAppValues( c , item.ID , item.ops , DBEnumParamEntityType.BASEITEMDATA , item.CV , null , insert );
		item.implemented = true;
	}

	private static void modifyGroup( DBConnection c , BaseGroup group , boolean insert ) throws Exception {
		if( insert )
			group.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , group.NAME , DBEnumParamEntityType.BASEGROUP );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , group.NAME , group.ID , DBEnumParamEntityType.BASEGROUP );
		
		group.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppBaseGroup , group.ID , group.CV , new String[] {
				EngineDB.getEnum( group.category.BASECATEGORY_TYPE ) ,
				EngineDB.getString( group.NAME ) , 
				EngineDB.getString( group.DESC ) ,
				EngineDB.getBoolean( group.OFFLINE )
				} , insert );
	}

	private static void addItemDependency( DBConnection c , EngineEntities entities , BaseItem item , BaseItem dep , int version ) throws Exception {
		if( !c.modify( DBQueries.MODIFY_BASE_ADDDEPITEM3 , new String[] { 
				EngineDB.getInteger( item.ID ) , 
				EngineDB.getInteger( dep.ID ) ,
				EngineDB.getInteger( version )
				} ) )
			Common.exitUnexpected();
		
		item.addDepItem( entities , dep );
	}
	
	public static BaseGroup createGroup( EngineTransaction transaction , EngineBase base , DBEnumBaseCategoryType type , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( base.findGroup( name ) != null )
			transaction.exitUnexpectedState();
		
		BaseCategory category = base.getCategory( type );
		BaseGroup group = new BaseGroup( category );
		group.createGroup( name , desc );
		modifyGroup( c , group , true );
		
		base.addGroup( group );
		return( group );
	}
	
	public static void deleteGroup( EngineTransaction transaction , EngineBase base , BaseGroup group ) throws Exception {
		if( !group.isEmpty() )
			transaction.exit0( _Error.GroupNotEmpty0 , "Group is not empty, unable to delete" );
		
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.deleteAppObject( c , entities.entityAppBaseGroup , group.ID , c.getNextCoreVersion() );
		base.removeGroup( group );
		group.deleteObject();
	}
	
	public static void modifyGroup( EngineTransaction transaction , EngineBase base , BaseGroup group , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		group.modifyGroup( name , desc );
		modifyGroup( c , group , false );
		base.updateGroup( group );
	}
	
	public static void setGroupOffline( EngineTransaction transaction , EngineBase base , BaseGroup group , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		group.setOffline( offline );
		if( offline == true ) {
			for( BaseItem item : group.getItems() ) {
				if( !item.OFFLINE ) {
					transaction.exit0( _Error.GroupHasActive0 , "Group contains active items" );
				}
			}
		}
		
		modifyGroup( c , group , false );
	}
	
	public static BaseItem createItem( EngineTransaction transaction , EngineBase base , BaseGroup group , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		EngineSettings settings = transaction.getSettings();
		ObjectProperties pe = settings.getEngineProperties();
		
		if( base.findItem( name ) != null )
			transaction.exitUnexpectedState();
		
		ObjectProperties ops = entities.createBaseItemProps( pe );
		BaseItem item = new BaseItem( group , ops );
		item.createBaseItem( name , desc );
		modifyItem( c , item , true );
		ops.setOwnerId( item.ID );
		
		DBSettings.savedbEntityCustom( c , ops , item.CV );
		
		base.addItem( item );
		return( item );
	}
	
	public static void modifyItem( EngineTransaction transaction , EngineBase base , BaseItem item , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		item.modifyBaseItem( name , desc );
		modifyItem( c , item , false );
		base.updateItem( item );
	}

	public static void modifyItemData( EngineTransaction transaction , BaseItem item , boolean admin , String name , String version , DBEnumOSType ostype , DBEnumServerAccessType accessType , DBEnumBaseSrcType srcType , DBEnumBaseSrcFormatType srcFormat , String SRCDIR , String SRCFILE , String SRCFILEDIR , String INSTALLSCRIPT , String INSTALLPATH , String INSTALLLINK ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		boolean insert = ( item.isImplemented() )? false : true;
		item.modifyData( admin , name , version , ostype , accessType , srcType , srcFormat , SRCDIR , SRCFILE , SRCFILEDIR , INSTALLSCRIPT , INSTALLPATH , INSTALLLINK );
		if( !item.isValidImplementation() )
			transaction.exit0( _Error.ItemInvalidData0 , "Unable to set item data, configuration is invalid" );
		
		modifyItemData( c , item , insert );
	}
	
	public static void setItemOffline( EngineTransaction transaction , EngineBase base , BaseItem item , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( offline == false ) {
			if( !item.isValid() )
				transaction.exit0( _Error.ItemIsInvalid0 , "Unable to set item online, configuration is invalid" );
			if( item.group.OFFLINE )
				transaction.exit0( _Error.GroupIsOffline0 , "Unable to set item online, group is offline" );
		}
		
		item.setOffline( offline );
		modifyItem( c , item , false );
	}
	
	public static void deleteItem( EngineTransaction transaction , EngineBase base , BaseItem item ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		EngineEntities entities = c.getEntities();
		DBSettings.dropObjectSettings( c , item.ID );
		DBEngineEntities.deleteAppObject( c , entities.entityAppBaseItemData , item.ID , c.getNextCoreVersion() );
		DBEngineEntities.deleteAppObject( c , entities.entityAppBaseItem , item.ID , c.getNextCoreVersion() );
		base.removeItem( item );
		item.deleteObject();
	}
	
	public static void addItemDependency( EngineTransaction transaction , EngineBase base , BaseItem item , BaseItem dep ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = transaction.getEntities();
		
		int version = c.getNextCoreVersion();
		addItemDependency( c , entities , item , dep , version );
	}
	
	public static void deleteItemDependency( EngineTransaction transaction , EngineBase base , BaseItem item , BaseItem dep ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( !c.modify( DBQueries.MODIFY_BASE_DELETEDEPITEM2 , new String[] { 
				EngineDB.getInteger( item.ID ) , 
				EngineDB.getInteger( dep.ID )
				} ) )
			Common.exitUnexpected();
		item.deleteDepItem( dep );
	}
	
	public static void updateCustomProperties( EngineTransaction transaction , BaseItem item ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ObjectProperties ops = item.getParameters();
		int version = c.getNextCoreVersion();
		DBSettings.savedbPropertyValues( transaction , ops , false , true , version );
		ops.recalculateChildProperties();
	}
	
	public static void updateCustomDependencyProperties( EngineTransaction transaction , BaseItem item , BaseItem depitem ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ObjectProperties ops = item.getDependencySettings( depitem.ID );
		int version = c.getNextCoreVersion();
		DBSettings.savedbPropertyValues( transaction , ops , false , true , version );
		ops.recalculateChildProperties();
	}
	
}
