package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerBase extends ServerObject {

	public enum CATEGORY_TYPE {
		HOST ,
		ACCOUNT ,
		APP
	};
	
	public ServerLoader loader;
	
	private Map<String,ServerBaseCategory> mapCategory;
	private Map<String,ServerBaseItem> mapItem;
	
	public ServerBase( ServerLoader loader ) {
		super( null );
		this.loader = loader;
		mapCategory = new HashMap<String,ServerBaseCategory>(); 
		mapItem = new HashMap<String,ServerBaseItem>(); 
	}
	
	public void load( String baseFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , baseFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "category" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerBaseCategory category = new ServerBaseCategory( this );
			category.load( node );
			addCategory( category );
			
			for( ServerBaseGroup group : category.groupMap.values() ) {
				for( ServerBaseItem item : group.itemMap.values() )
					addItem( item );
			}
		}
		
		if( findCategory( CATEGORY_TYPE.HOST ) == null )
			addCategory( new ServerBaseCategory( this , CATEGORY_TYPE.HOST , "Host-Based" ) );
		if( findCategory( CATEGORY_TYPE.ACCOUNT ) == null )
			addCategory( new ServerBaseCategory( this , CATEGORY_TYPE.ACCOUNT , "Account-Based" ) );
		if( findCategory( CATEGORY_TYPE.APP ) == null )
			addCategory( new ServerBaseCategory( this , CATEGORY_TYPE.APP , "Application-Based" ) );
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "base" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( mapCategory ) ) {
			ServerBaseCategory category = mapCategory.get( id );
			Element node = Common.xmlCreateElement( doc , root , "category" );
			category.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addCategory( ServerBaseCategory category ) {
		mapCategory.put( category.ID , category );
	}

	public void addItem( ServerBaseItem item ) {
		mapItem.put( item.ID , item );
	}

	public ServerBaseCategory findCategory( CATEGORY_TYPE type ) {
		return( mapCategory.get( Common.getEnumLower( type ) ) );
	}

	public ServerBaseCategory findCategory( String id ) {
		return( mapCategory.get( id ) );
	}

	public ServerBaseItem findBase( String id ) {
		return( mapItem.get( id ) );
	}

	public String[] getCategories() {
		return( Common.getSortedKeys( mapCategory ) );
	}

}
