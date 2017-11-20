package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineObject;
import org.urm.db.core.DBEnums.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BaseCategory extends EngineObject {

	public DBEnumBaseCategoryType type;
	public String ID;
	public String NAME;

	public EngineBase base;
	Map<String,BaseGroup> groupMap;

	public BaseCategory( EngineBase base ) {
		super( null );
		this.base = base;
		groupMap = new HashMap<String,BaseGroup>();
	}
	
	public BaseCategory( EngineBase base , DBEnumBaseCategoryType type , String NAME ) {
		super( null );
		this.base = base;
		this.type = type;
		this.NAME = NAME;
		this.ID = type.name().toLowerCase();
		groupMap = new HashMap<String,BaseGroup>();
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public BaseCategory copy( EngineBase rn ) throws Exception {
		BaseCategory r = new BaseCategory( rn , type , NAME );
		
		for( BaseGroup group : groupMap.values() ) {
			BaseGroup rgroup = group.copy( r );
			r.addGroup( rgroup );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		type = DBEnumBaseCategoryType.valueOf( ConfReader.getAttrValue( root , "type" ).toUpperCase() );
		ID = ConfReader.getAttrValue( root , "id" );
		NAME = ConfReader.getAttrValue( root , "name" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "group" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			BaseGroup group = new BaseGroup( this );
			group.load( node );
			addGroup( group );
		}
	}

	public void addGroup( BaseGroup group ) {
		groupMap.put( group.ID , group );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( type ) );
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		
		for( BaseGroup group : groupMap.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "group" );
			group.save( doc , element );
		}
	}

	public String[] getGroupNames() {
		return( Common.getSortedKeys( groupMap ) );
	}

	public BaseGroup[] getGroups() {
		return( groupMap.values().toArray( new BaseGroup[0] ) );
	}

	public BaseGroup findGroup( String ID ) {
		return( groupMap.get( ID ) );
	}
	
	public void createGroup( EngineTransaction transaction , BaseGroup group ) throws Exception {
		addGroup( group );
	}
	
	public void deleteGroup( EngineTransaction transaction , BaseGroup group ) throws Exception {
		groupMap.remove( group );
	}
	
	public void modifyGroup( EngineTransaction transaction , BaseGroup group ) {
		String oldId = null;
		for( Entry<String,BaseGroup> entry : groupMap.entrySet() ) {
			if( entry.getValue() == group )
				oldId = entry.getKey();
		}
		groupMap.remove( oldId );
		addGroup( group );
	}

	public void deleteHost( EngineTransaction transaction ) throws Exception {
		super.deleteObject();
	}

}
