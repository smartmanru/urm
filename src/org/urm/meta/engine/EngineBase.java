package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.urm.db.DBEnumTypes.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineBase extends EngineObject {

	public EngineLoader loader;
	
	private Map<String,EngineBaseCategory> mapCategory;
	private Map<String,EngineBaseItem> mapItem;
	
	public EngineBase( EngineLoader loader ) {
		super( null );
		this.loader = loader;
		mapCategory = new HashMap<String,EngineBaseCategory>(); 
		mapItem = new HashMap<String,EngineBaseItem>(); 
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
				EngineBaseCategory category = new EngineBaseCategory( this );
				category.load( node );
				addCategory( category );
				
				for( EngineBaseGroup group : category.groupMap.values() ) {
					for( EngineBaseItem item : group.itemMap.values() )
						addItem( item );
				}
			}
		}
		
		if( findCategory( DBEnumBaseCategoryType.HOST ) == null )
			addCategory( new EngineBaseCategory( this , DBEnumBaseCategoryType.HOST , "Host-Bound" ) );
		if( findCategory( DBEnumBaseCategoryType.ACCOUNT ) == null )
			addCategory( new EngineBaseCategory( this , DBEnumBaseCategoryType.ACCOUNT , "Account-Bound" ) );
		if( findCategory( DBEnumBaseCategoryType.APP ) == null )
			addCategory( new EngineBaseCategory( this , DBEnumBaseCategoryType.APP , "Application-Bound" ) );
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "base" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( mapCategory ) ) {
			EngineBaseCategory category = mapCategory.get( id );
			Element node = Common.xmlCreateElement( doc , root , "category" );
			category.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addCategory( EngineBaseCategory category ) {
		mapCategory.put( category.ID , category );
	}

	public void addItem( EngineBaseItem item ) {
		mapItem.put( item.ID , item );
	}

	public void createItem( EngineTransaction transaction , EngineBaseItem item ) throws Exception {
		if( mapItem.get( item.ID ) != null )
			transaction.exit1( _Error.DuplicateBaseItem1 , "duplicate base item=" + item.ID , item.ID );
		
		addItem( item );
	}
	
	public EngineBaseCategory findCategory( DBEnumBaseCategoryType type ) {
		return( findCategory( Common.getEnumLower( type ) ) );
	}

	public EngineBaseCategory findCategory( String id ) {
		return( mapCategory.get( id ) );
	}

	public EngineBaseGroup findGroup( DBEnumBaseCategoryType type , String groupName ) {
		EngineBaseCategory ct = findCategory( type );
		if( ct != null )
			return( ct.findGroup( groupName ) );
		return( null );
	}
	
	public EngineBaseItem findBase( String id ) {
		return( mapItem.get( id ) );
	}

	public String[] getCategories() {
		return( Common.getSortedKeys( mapCategory ) );
	}

}
