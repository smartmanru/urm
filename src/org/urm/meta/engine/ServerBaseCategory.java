package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerObject;
import org.urm.meta.engine.ServerBase.CATEGORY_TYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerBaseCategory extends ServerObject {

	public CATEGORY_TYPE type;
	public String ID;

	public ServerBase base;
	Map<String,ServerBaseGroup> groupMap;

	public ServerBaseCategory( ServerBase base ) {
		super( null );
		this.base = base;
	}
	
	public ServerBaseCategory( ServerBase base , CATEGORY_TYPE type , String ID ) {
		super( null );
		this.base = base;
		this.type = type;
		this.ID = ID;
		groupMap = new HashMap<String,ServerBaseGroup>();
	}
	
	public ServerBaseCategory copy( ServerBase rn ) throws Exception {
		ServerBaseCategory r = new ServerBaseCategory( rn );
		
		for( ServerBaseGroup group : groupMap.values() ) {
			ServerBaseGroup rgroup = group.copy( r );
			r.addGroup( rgroup );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		type = CATEGORY_TYPE.valueOf( ConfReader.getAttrValue( root , "type" ).toUpperCase() );
		ID = ConfReader.getAttrValue( root , "id" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "group" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerBaseGroup group = new ServerBaseGroup( this );
			group.load( node );
			addGroup( group );
		}
	}

	public void addGroup( ServerBaseGroup group ) {
		groupMap.put( group.ID , group );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( type ) );
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		
		for( ServerBaseGroup group : groupMap.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "account" );
			group.save( doc , element );
		}
	}

	public String[] getGroups() {
		return( Common.getSortedKeys( groupMap ) );
	}

	public ServerBaseGroup findGroup( String ID ) {
		return( groupMap.get( ID ) );
	}
	
	public void createGroup( ServerTransaction transaction , ServerBaseGroup group ) throws Exception {
		addGroup( group );
	}
	
	public void deleteGroup( ServerTransaction transaction , ServerBaseGroup group ) throws Exception {
		groupMap.remove( group );
	}
	
	public void modifyGroup( ServerTransaction transaction , ServerBaseGroup group ) {
		String oldId = null;
		for( Entry<String,ServerBaseGroup> entry : groupMap.entrySet() ) {
			if( entry.getValue() == group )
				oldId = entry.getKey();
		}
		groupMap.remove( oldId );
		addGroup( group );
	}

	public void deleteHost( ServerTransaction transaction ) throws Exception {
		super.deleteObject();
	}

}
