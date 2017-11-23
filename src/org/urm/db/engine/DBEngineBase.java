package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.DBEnumBaseCategoryType;
import org.urm.db.core.DBSettings;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
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

	public static void importxml( EngineLoader loader , EngineBase base , Node root ) throws Exception {
		base.addCategory( new BaseCategory( base , DBEnumBaseCategoryType.HOST , "Host-Bound" ) );
		base.addCategory( new BaseCategory( base , DBEnumBaseCategoryType.ACCOUNT , "Account-Bound" ) );
		base.addCategory( new BaseCategory( base , DBEnumBaseCategoryType.APP , "Application-Bound" ) );
		
		Node[] list = ConfReader.xmlGetChildren( root , "category" );
		if( list != null ) {
			for( Node node : list ) {
				BaseCategory category = importxmlCategory( loader , base , node );
				base.addCategory( category );
			}
		}
	}
	
	private static BaseCategory importxmlCategory( EngineLoader loader , EngineBase base , Node root ) throws Exception {
		DBEnumBaseCategoryType type = DBEnumBaseCategoryType.getValue( ConfReader.getAttrValue( root , "type" ).toUpperCase() , true );
		BaseCategory category = base.findCategory( type );
		if( category == null )
			Common.exitUnexpected();
		
		Node[] list = ConfReader.xmlGetChildren( root , "group" );
		if( list != null ) {
			for( Node node : list ) {
				BaseGroup group = importxmlGroup( loader , base , category , node );
				category.addGroup( group );
			}
		}
		
		return( category );
	}

	private static BaseGroup importxmlGroup( EngineLoader loader , EngineBase base , BaseCategory category , Node root ) throws Exception {
		BaseGroup group = new BaseGroup( category );
		group.NAME = ConfReader.getAttrValue( root , "id" );
		group.DESC = ConfReader.getAttrValue( root , "desc" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "item" );
		if( list != null ) {
			for( Node node : list ) {
				BaseItem item = importxmlItem( loader , base , group , node );
				group.addItem( item );
				base.addItem( item );
			}
		}
		
		return( group );
	}
	
	private static BaseItem importxmlItem( EngineLoader loader , EngineBase base , BaseGroup group , Node root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		EngineSettings settings = loader.getData().getEngineSettings();
		ObjectProperties properties = entities.createEngineMonitoringProps( settings.getEngineProperties() );
		BaseItem item = new BaseItem( group , properties );
		item.ID = ConfReader.getAttrValue( root , "id" );
		item.DESC = ConfReader.getAttrValue( root , "desc" );
		return( item );
	}

	public static void exportxml( EngineLoader loader , EngineBase base , Document doc , Element root ) throws Exception {
		for( String id : base.getCategories() ) {
			BaseCategory category = base.findCategory( id );
			Element node = Common.xmlCreateElement( doc , root , "category" );
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
			Element element = Common.xmlCreateElement( doc , root , "item" );
			exportxmlItem( loader , item , doc , element );
		}
	}

	public static void exportxmlCategory( EngineLoader loader , BaseCategory category , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( category.TYPE ) );
		Common.xmlSetElementAttr( doc , root , "id" , category.LABEL );
		Common.xmlSetElementAttr( doc , root , "name" , category.NAME );
		
		for( BaseGroup group : category.getGroups() ) {
			Element element = Common.xmlCreateElement( doc , root , "group" );
			exportxmlGroup( loader , group , doc , element );
		}
	}

}
