package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;

public class BaseGroup extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_OFFLINE = "offline";
	
	public int ID;
	public String NAME;
	public String DESC;
	public boolean OFFLINE;
	public int CV;

	public BaseCategory category;
	Map<String,BaseItem> itemMap;

	public BaseGroup( BaseCategory category ) {
		super( category );
		this.category = category;
		itemMap = new HashMap<String,BaseItem>();
		ID = -1;
		CV = 0;
		OFFLINE = false;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public BaseGroup copy( BaseCategory rn , EngineEntities entities , ObjectProperties rparametersEngine ) throws Exception {
		BaseGroup r = new BaseGroup( rn );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		r.CV = CV;
		
		for( BaseItem item : itemMap.values() ) {
			ObjectProperties ritem_parameters = item.ops.copy( rparametersEngine );
			BaseItem ritem = item.copy( r , entities , ritem_parameters );
			r.addItem( ritem );
		}
		return( r );
	}
	
	public boolean isEmpty() {
		if( itemMap.isEmpty() )
			return( true );
		return( false );
	}
	
	public void addItem( BaseItem item ) {
		itemMap.put( item.NAME , item );
	}

	public void removeItem( BaseItem item ) {
		itemMap.remove( item.NAME );
	}

	public String[] getItemNames() {
		return( Common.getSortedKeys( itemMap ) );
	}

	public BaseItem[] getItems() {
		return( itemMap.values().toArray( new BaseItem[0] ) );
	}

	public BaseItem findItem( String name ) {
		return( itemMap.get( name ) );
	}
	
	public void createGroup( String name , String desc ) throws Exception {
		OFFLINE = false;
		modifyGroup( name , desc );
	}
	
	public void modifyGroup( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = Common.nonull( desc );
	}
	
	public void setOffline( boolean offline ) {
		this.OFFLINE = offline;
	}
	
	public void createItem( BaseItem item ) throws Exception {
		addItem( item );
	}
	
	public void deleteItem( BaseItem item ) throws Exception {
		itemMap.remove( item.NAME );
	}
	
	public void modifyItem( BaseItem item ) throws Exception {
		Common.changeMapKey( itemMap , item , item.NAME );
	}

}
