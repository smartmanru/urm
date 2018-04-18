package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.common.Common;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.loader.EngineObject;
import org.urm.db.core.DBEnums.*;

public class BaseCategory extends EngineObject {

	public static String PROPERTY_TYPE = "type";
	
	public DBEnumBaseCategoryType BASECATEGORY_TYPE;
	public String LABEL;
	public String NAME;

	public EngineBase base;
	Map<String,BaseGroup> groupMap;

	public BaseCategory( EngineBase base ) {
		super( null );
		this.base = base;
		groupMap = new HashMap<String,BaseGroup>();
	}
	
	public BaseCategory( EngineBase base , DBEnumBaseCategoryType type , String NAME ) {
		super( base );
		this.base = base;
		this.BASECATEGORY_TYPE = type;
		this.NAME = NAME;
		this.LABEL = type.name().toLowerCase();
		groupMap = new HashMap<String,BaseGroup>();
	}
	
	@Override
	public String getName() {
		return( LABEL );
	}
	
	public BaseCategory copy( EngineBase rn , EngineEntities entities , ObjectProperties parent ) throws Exception {
		BaseCategory r = new BaseCategory( rn , BASECATEGORY_TYPE , NAME );
		
		for( BaseGroup group : groupMap.values() ) {
			BaseGroup rgroup = group.copy( r , entities , parent );
			r.addGroup( rgroup );
		}
		return( r );
	}
	
	public void addGroup( BaseGroup group ) {
		groupMap.put( group.NAME , group );
	}

	public String[] getGroupNames() {
		return( Common.getSortedKeys( groupMap ) );
	}

	public BaseGroup[] getGroups() {
		return( groupMap.values().toArray( new BaseGroup[0] ) );
	}

	public BaseGroup findGroup( String name ) {
		return( groupMap.get( name ) );
	}
	
	public void createGroup( BaseGroup group ) {
		addGroup( group );
	}
	
	public void removeGroup( BaseGroup group ) {
		groupMap.remove( group.NAME );
	}
	
	public void modifyGroup( BaseGroup group ) {
		String oldId = null;
		for( Entry<String,BaseGroup> entry : groupMap.entrySet() ) {
			if( entry.getValue() == group )
				oldId = entry.getKey();
		}
		groupMap.remove( oldId );
		addGroup( group );
	}

}
