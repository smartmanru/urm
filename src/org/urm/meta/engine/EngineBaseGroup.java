package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineBaseGroup extends EngineObject {

	public String ID;
	public String DESC;

	public EngineBaseCategory category;
	Map<String,EngineBaseItem> itemMap;

	public EngineBaseGroup( EngineBaseCategory category ) {
		super( category );
		this.category = category;
		itemMap = new HashMap<String,EngineBaseItem>();
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public EngineBaseGroup copy( EngineBaseCategory rn ) throws Exception {
		EngineBaseGroup r = new EngineBaseGroup( rn );
		
		for( EngineBaseItem item : itemMap.values() ) {
			EngineBaseItem ritem = item.copy( r );
			r.addItem( ritem );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "item" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			EngineBaseItem item = new EngineBaseItem( this );
			item.load( node );
			addItem( item );
		}
	}

	public void addItem( EngineBaseItem item ) {
		itemMap.put( item.ID , item );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		
		for( EngineBaseItem item : itemMap.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "item" );
			item.save( doc , element );
		}
	}

	public String[] getItemNames() {
		return( Common.getSortedKeys( itemMap ) );
	}

	public EngineBaseItem[] getItems() {
		return( itemMap.values().toArray( new EngineBaseItem[0] ) );
	}

	public EngineBaseItem findItem( String ID ) {
		return( itemMap.get( ID ) );
	}
	
	public void createGroup( EngineTransaction transaction , String ID , String DESC ) throws Exception {
		this.ID = ID;
		this.DESC = DESC;
	}
	
	public void modifyGroup( EngineTransaction transaction , String ID , String DESC ) throws Exception {
		this.ID = ID;
		this.DESC = DESC;
	}
	
	public void createItem( EngineTransaction transaction , EngineBaseItem item ) throws Exception {
		addItem( item );
	}
	
	public void deleteItem( EngineTransaction transaction , EngineBaseItem item ) throws Exception {
		itemMap.remove( item );
	}
	
	public void modifyItem( EngineTransaction transaction , EngineBaseItem item ) {
		String oldId = null;
		for( Entry<String,EngineBaseItem> entry : itemMap.entrySet() ) {
			if( entry.getValue() == item )
				oldId = entry.getKey();
		}
		itemMap.remove( oldId );
		addItem( item );
	}

	public void deleteItem( EngineTransaction transaction ) throws Exception {
		super.deleteObject();
	}

}
