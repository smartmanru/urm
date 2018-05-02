package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumChangeType;
import org.urm.db.core.DBEnums.DBEnumCompItemType;

public class MetaDistrComponent {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	
	public Meta meta;
	public MetaDistr dist;

	public int ID;
	public String NAME;
	public String DESC;
	public int PV;
	public DBEnumChangeType CHANGETYPE;

	private Map<Integer,MetaDistrComponentItem> mapBinaryItemsById;
	private Map<Integer,MetaDistrComponentItem> mapConfItemsById;
	private Map<Integer,MetaDistrComponentItem> mapSchemaItemsById;
	private Map<Integer,MetaDistrComponentItem> mapWSByItemId;
	
	public MetaDistrComponent( Meta meta , MetaDistr dist ) {
		this.meta = meta;
		this.dist = dist;
		
		mapBinaryItemsById = new HashMap<Integer,MetaDistrComponentItem>();
		mapConfItemsById = new HashMap<Integer,MetaDistrComponentItem>();
		mapSchemaItemsById = new HashMap<Integer,MetaDistrComponentItem>();
		mapWSByItemId = new HashMap<Integer,MetaDistrComponentItem>();
	}

	public MetaDistrComponent copy( Meta rmeta , MetaDistr rdistr , boolean all ) throws Exception {
		MetaDistrComponent r = new MetaDistrComponent( rmeta , rdistr );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.PV = PV;
		r.CHANGETYPE = CHANGETYPE;
		
		if( all ) {
			for( MetaDistrComponentItem item : mapBinaryItemsById.values() ) {
				MetaDistrComponentItem ritem = item.copy( rmeta , r );
				r.addBinaryItem( ritem );
			}
			
			for( MetaDistrComponentItem item : mapConfItemsById.values() ) {
				MetaDistrComponentItem ritem = item.copy( rmeta , r );
				r.addConfItem( ritem );
			}
			
			for( MetaDistrComponentItem item : mapSchemaItemsById.values() ) {
				MetaDistrComponentItem ritem = item.copy( rmeta , r );
				r.addSchemaItem( ritem );
			}
	
			for( MetaDistrComponentItem item : mapWSByItemId.values() ) {
				MetaDistrComponentItem ritem = item.copy( rmeta , r );
				r.addWebService( ritem );
			}
		}
		
		return( r );
	}
	
	public void createComponent( String name , String desc ) throws Exception {
		modifyComponent( name , desc );
	}
	
	public void modifyComponent( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
	}

	public void addItem( MetaDistrComponentItem item ) throws Exception {
		if( item.COMPITEM_TYPE == DBEnumCompItemType.BINARY )
			addBinaryItem( item );
		else
		if( item.COMPITEM_TYPE == DBEnumCompItemType.CONF )
			addConfItem( item );
		else
		if( item.COMPITEM_TYPE == DBEnumCompItemType.SCHEMA )
			addSchemaItem( item );
		else
		if( item.COMPITEM_TYPE == DBEnumCompItemType.WSDL )
			addWebService( item );
		else
			Common.exitUnexpected();
	}
	
	public void addBinaryItem( MetaDistrComponentItem item ) {
		mapBinaryItemsById.put( item.ID , item );
	}
	
	public void addConfItem( MetaDistrComponentItem item ) {
		mapConfItemsById.put( item.ID , item );
	}
	
	public void addSchemaItem( MetaDistrComponentItem item ) {
		mapSchemaItemsById.put( item.ID , item );
	}
	
	public void addWebService( MetaDistrComponentItem item ) {
		mapWSByItemId.put( item.ID , item );
	}
	
	public boolean hasWebServices() {
		if( mapWSByItemId.isEmpty() )
			return( false );
		return( true );
	}

	public MetaDistrComponentItem[] getWebServices() {
		return( mapWSByItemId.values().toArray( new MetaDistrComponentItem[0] ) );
	}

	public boolean hasBinaryItems() {
		if( mapBinaryItemsById.isEmpty() )
			return( false );
		return( true );
	}

	public boolean hasConfItems() {
		if( mapConfItemsById.isEmpty() )
			return( false );
		return( true );
	}

	public String[] getBinaryItemNames() {
		Map<String,MetaDistrComponentItem> map = new HashMap<String,MetaDistrComponentItem>();
		for( MetaDistrComponentItem item : mapBinaryItemsById.values() )
			map.put( item.binaryItem.NAME , item );
		return( Common.getSortedKeys( map ) );
	}
	
	public MetaDistrComponentItem[] getBinaryItems() {
		return( mapBinaryItemsById.values().toArray( new MetaDistrComponentItem[0] ) );
	}
	
	public String[] getConfItemNames() {
		Map<String,MetaDistrComponentItem> map = new HashMap<String,MetaDistrComponentItem>();
		for( MetaDistrComponentItem item : mapConfItemsById.values() )
			map.put( item.confItem.NAME , item );
		return( Common.getSortedKeys( map ) );
	}
	
	public MetaDistrComponentItem[] getConfItems() {
		return( mapConfItemsById.values().toArray( new MetaDistrComponentItem[0] ) );
	}
	
	public String[] getSchemaItemNames() {
		Map<String,MetaDistrComponentItem> map = new HashMap<String,MetaDistrComponentItem>();
		for( MetaDistrComponentItem item : mapSchemaItemsById.values() )
			map.put( item.schema.NAME , item );
		return( Common.getSortedKeys( map ) );
	}
	
	public MetaDistrComponentItem[] getSchemaItems() {
		return( mapSchemaItemsById.values().toArray( new MetaDistrComponentItem[0] ) );
	}

	public MetaDistrComponentItem findBinaryItem( String name ) {
		for( MetaDistrComponentItem item : mapBinaryItemsById.values() ) {
			if( item.binaryItem.NAME.equals( name ) )
				return( item );
		}
		return( null );
	}
	
	public MetaDistrComponentItem getBinaryItem( String name ) throws Exception {
		MetaDistrComponentItem item = findBinaryItem( name );
		if( item == null )
			Common.exit1( _Error.UnknownCompBinaryItem1 , "Unknown component binary item=" + name , name );
		return( item );
	}
	
	public MetaDistrComponentItem findConfItem( String name ) {
		for( MetaDistrComponentItem item : mapConfItemsById.values() ) {
			if( item.confItem.NAME.equals( name ) )
				return( item );
		}
		return( null );
	}
	
	public MetaDistrComponentItem getConfItem( String name ) throws Exception {
		MetaDistrComponentItem item = findConfItem( name );
		if( item == null )
			Common.exit1( _Error.UnknownCompConfItem1 , "Unknown component configuration item=" + name , name );
		return( item );
	}

	public MetaDistrComponentItem findSchemaItem( int id ) {
		return( mapSchemaItemsById.get( id ) );
	}
	
	public MetaDistrComponentItem findSchemaItem( String name ) {
		for( MetaDistrComponentItem item : mapSchemaItemsById.values() ) {
			if( item.schema.NAME.equals( name ) )
				return( item );
		}
		return( null );
	}
	
	public MetaDistrComponentItem getSchemaItem( String name ) throws Exception {
		MetaDistrComponentItem item = findSchemaItem( name );
		if( item == null )
			Common.exit1( _Error.UnknownCompSchemaItem1 , "Unknown component databce schema item=" + name , name );
		return( item );
	}

	public MetaDistrComponentItem findWebService( String name ) {
		for( MetaDistrComponentItem item : mapWSByItemId.values() ) {
			if( item.WSDL_REQUEST.equals( name ) )
				return( item );
		}
			
		return( null );
	}
	
	public MetaDistrComponentItem getWebService( String name ) throws Exception {
		MetaDistrComponentItem service = findWebService( name );
		if( service == null )
			Common.exit1( _Error.UnknownCompWebService1 , "Unknown component web service=" + name , name );
		return( service );
	}
	
	public void removeCompItem( MetaDistrComponentItem item ) throws Exception {
		if( item.binaryItem != null )
			mapBinaryItemsById.remove( item.binaryItem.ID );
		else
		if( item.confItem != null )
			mapConfItemsById.remove( item.confItem.ID );
		else
		if( item.schema != null )
			mapSchemaItemsById.remove( item.schema.ID );
		else
		if( !item.WSDL_REQUEST.isEmpty() )
			mapWSByItemId.remove( item.ID );
	}

}
