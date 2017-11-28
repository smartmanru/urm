package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
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
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public BaseGroup copy( BaseCategory rn , ObjectProperties rparametersEngine ) throws Exception {
		BaseGroup r = new BaseGroup( rn );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		r.CV = CV;
		
		for( BaseItem item : itemMap.values() ) {
			ObjectProperties ritem_parameters = item.parameters.copy( rparametersEngine );
			BaseItem ritem = item.copy( r , ritem_parameters );
			r.addItem( ritem );
		}
		return( r );
	}
	
	public void addItem( BaseItem item ) {
		itemMap.put( item.NAME , item );
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
	
	public void createGroup( String ID , String DESC ) throws Exception {
		OFFLINE = false;
		modifyGroup( ID , DESC );
	}
	
	public void modifyGroup( String ID , String DESC ) throws Exception {
		this.NAME = ID;
		this.DESC = DESC;
	}
	
	public void createItem( BaseItem item ) throws Exception {
		addItem( item );
	}
	
	public void deleteItem( BaseItem item ) throws Exception {
		itemMap.remove( item );
	}
	
	public void modifyItem( BaseItem item ) throws Exception {
		Common.changeMapKey( itemMap , item , item.NAME );
	}

	public void deleteItem() throws Exception {
		super.deleteObject();
	}

}
