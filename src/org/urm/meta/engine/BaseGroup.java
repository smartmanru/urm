package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.common.Common;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;

public class BaseGroup extends EngineObject {

	public static String PROPERTY_NAME;
	public static String PROPERTY_DESC;
	
	public int ID;
	public String NAME;
	public String DESC;
	public int CV;

	public BaseCategory category;
	Map<String,BaseItem> itemMap;

	public ObjectProperties parameters;
	
	public BaseGroup( BaseCategory category ) {
		super( category );
		this.category = category;
		itemMap = new HashMap<String,BaseItem>();
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public BaseGroup copy( BaseCategory rn , ObjectProperties parent ) throws Exception {
		BaseGroup r = new BaseGroup( rn );
		
		for( BaseItem item : itemMap.values() ) {
			ObjectProperties rparameters = item.parameters.copy( parent );
			BaseItem ritem = item.copy( r , rparameters );
			r.addItem( ritem );
		}
		return( r );
	}
	
	public void addItem( BaseItem item ) {
		itemMap.put( item.ID , item );
	}

	public String[] getItemNames() {
		return( Common.getSortedKeys( itemMap ) );
	}

	public BaseItem[] getItems() {
		return( itemMap.values().toArray( new BaseItem[0] ) );
	}

	public BaseItem findItem( String ID ) {
		return( itemMap.get( ID ) );
	}
	
	public void createGroup( EngineTransaction transaction , String ID , String DESC ) throws Exception {
		this.NAME = ID;
		this.DESC = DESC;
	}
	
	public void modifyGroup( EngineTransaction transaction , String ID , String DESC ) throws Exception {
		this.NAME = ID;
		this.DESC = DESC;
	}
	
	public void createItem( EngineTransaction transaction , BaseItem item ) throws Exception {
		addItem( item );
	}
	
	public void deleteItem( EngineTransaction transaction , BaseItem item ) throws Exception {
		itemMap.remove( item );
	}
	
	public void modifyItem( EngineTransaction transaction , BaseItem item ) {
		String oldId = null;
		for( Entry<String,BaseItem> entry : itemMap.entrySet() ) {
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
