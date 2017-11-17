package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.db.DBEnums.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineBase extends EngineObject {

	public EngineData data;
	
	private Map<String,BaseCategory> mapCategory;
	private Map<String,BaseItem> mapItem;
	
	public EngineBase( EngineData data ) {
		super( null );
		this.data = data;
		mapCategory = new HashMap<String,BaseCategory>(); 
		mapItem = new HashMap<String,BaseItem>(); 
	}
	
	@Override
	public String getName() {
		return( "server-base" );
	}
	
	public void load( String baseFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , baseFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "category" );
		if( list != null ) {
			for( Node node : list ) {
				BaseCategory category = new BaseCategory( this );
				category.load( node );
				addCategory( category );
				
				for( BaseGroup group : category.groupMap.values() ) {
					for( BaseItem item : group.itemMap.values() )
						addItem( item );
				}
			}
		}
		
		if( findCategory( DBEnumBaseCategoryType.HOST ) == null )
			addCategory( new BaseCategory( this , DBEnumBaseCategoryType.HOST , "Host-Bound" ) );
		if( findCategory( DBEnumBaseCategoryType.ACCOUNT ) == null )
			addCategory( new BaseCategory( this , DBEnumBaseCategoryType.ACCOUNT , "Account-Bound" ) );
		if( findCategory( DBEnumBaseCategoryType.APP ) == null )
			addCategory( new BaseCategory( this , DBEnumBaseCategoryType.APP , "Application-Bound" ) );
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "base" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( mapCategory ) ) {
			BaseCategory category = mapCategory.get( id );
			Element node = Common.xmlCreateElement( doc , root , "category" );
			category.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addCategory( BaseCategory category ) {
		mapCategory.put( category.ID , category );
	}

	public void addItem( BaseItem item ) {
		mapItem.put( item.ID , item );
	}

	public void createItem( EngineTransaction transaction , BaseItem item ) throws Exception {
		if( mapItem.get( item.ID ) != null )
			transaction.exit1( _Error.DuplicateBaseItem1 , "duplicate base item=" + item.ID , item.ID );
		
		addItem( item );
	}
	
	public BaseCategory findCategory( DBEnumBaseCategoryType type ) {
		return( findCategory( Common.getEnumLower( type ) ) );
	}

	public BaseCategory findCategory( String id ) {
		return( mapCategory.get( id ) );
	}

	public BaseGroup findGroup( DBEnumBaseCategoryType type , String groupName ) {
		BaseCategory ct = findCategory( type );
		if( ct != null )
			return( ct.findGroup( groupName ) );
		return( null );
	}
	
	public BaseItem findBase( String id ) {
		return( mapItem.get( id ) );
	}

	public String[] getCategories() {
		return( Common.getSortedKeys( mapCategory ) );
	}

}
