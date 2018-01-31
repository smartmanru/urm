package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;

public class MetaDistrComponent {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	
	public Meta meta;
	public MetaDistr dist;

	public int ID;
	public String NAME;
	public String DESC;
	public int PV;

	private Map<Integer,MetaDistrComponentItem> mapBinaryItems;
	private Map<Integer,MetaDistrComponentItem> mapConfItems;
	private Map<Integer,MetaDistrComponentItem> mapSchemaItems;
	private Map<Integer,MetaDistrComponentItem> mapWS;
	
	public MetaDistrComponent( Meta meta , MetaDistr dist ) {
		this.meta = meta;
		this.dist = dist;
		
		mapBinaryItems = new HashMap<Integer,MetaDistrComponentItem>();
		mapConfItems = new HashMap<Integer,MetaDistrComponentItem>();
		mapSchemaItems = new HashMap<Integer,MetaDistrComponentItem>();
		mapWS = new HashMap<Integer,MetaDistrComponentItem>();
	}

	public MetaDistrComponent copy( Meta meta , MetaDistr distr ) throws Exception {
		MetaDistrComponent r = new MetaDistrComponent( meta , distr );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.PV = PV;
		
		for( MetaDistrComponentItem item : mapBinaryItems.values() ) {
			MetaDistrComponentItem ritem = item.copy( meta , r );
			r.addBinaryItem( ritem );
		}
		
		for( MetaDistrComponentItem item : mapConfItems.values() ) {
			MetaDistrComponentItem ritem = item.copy( meta , r );
			r.addConfItem( ritem );
		}
		
		for( MetaDistrComponentItem item : mapSchemaItems.values() ) {
			MetaDistrComponentItem ritem = item.copy( meta , r );
			r.addSchemaItem( ritem );
		}

		for( MetaDistrComponentItem item : mapWS.values() ) {
			MetaDistrComponentItem ritem = item.copy( meta , r );
			r.addWebService( ritem );
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
	
	public void addBinaryItem( MetaDistrComponentItem item ) {
		mapBinaryItems.put( item.ID , item );
	}
	
	public void addConfItem( MetaDistrComponentItem item ) {
		mapConfItems.put( item.ID , item );
	}
	
	public void addSchemaItem( MetaDistrComponentItem item ) {
		mapSchemaItems.put( item.ID , item );
	}
	
	public void addWebService( MetaDistrComponentItem item ) {
		mapWS.put( item.ID , item );
	}
	
	public boolean hasWebServices() {
		if( mapWS.isEmpty() )
			return( false );
		return( true );
	}

	public MetaDistrComponentItem[] getWebServices() {
		return( mapWS.values().toArray( new MetaDistrComponentItem[0] ) );
	}

	public boolean hasBinaryItems() {
		if( mapBinaryItems.isEmpty() )
			return( false );
		return( true );
	}

	public boolean hasConfItems() {
		if( mapConfItems.isEmpty() )
			return( false );
		return( true );
	}

	public String[] getBinaryItemNames() {
		Map<String,MetaDistrComponentItem> map = new HashMap<String,MetaDistrComponentItem>();
		for( MetaDistrComponentItem item : mapBinaryItems.values() )
			map.put( item.binaryItem.NAME , item );
		return( Common.getSortedKeys( map ) );
	}
	
	public MetaDistrComponentItem[] getBinaryItems() {
		return( mapBinaryItems.values().toArray( new MetaDistrComponentItem[0] ) );
	}
	
	public String[] getConfItemNames() {
		Map<String,MetaDistrComponentItem> map = new HashMap<String,MetaDistrComponentItem>();
		for( MetaDistrComponentItem item : mapConfItems.values() )
			map.put( item.confItem.NAME , item );
		return( Common.getSortedKeys( map ) );
	}
	
	public MetaDistrComponentItem[] getConfItems() {
		return( mapConfItems.values().toArray( new MetaDistrComponentItem[0] ) );
	}
	
	public String[] getSchemaItemNames() {
		Map<String,MetaDistrComponentItem> map = new HashMap<String,MetaDistrComponentItem>();
		for( MetaDistrComponentItem item : mapSchemaItems.values() )
			map.put( item.schema.NAME , item );
		return( Common.getSortedKeys( map ) );
	}
	
	public MetaDistrComponentItem[] getSchemaItems() {
		return( mapSchemaItems.values().toArray( new MetaDistrComponentItem[0] ) );
	}

	public MetaDistrComponentItem findBinaryItem( String name ) {
		for( MetaDistrComponentItem item : mapBinaryItems.values() ) {
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
		for( MetaDistrComponentItem item : mapConfItems.values() ) {
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
	
	public MetaDistrComponentItem findSchemaItem( String name ) {
		for( MetaDistrComponentItem item : mapSchemaItems.values() ) {
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
		for( MetaDistrComponentItem item : mapWS.values() ) {
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
			mapBinaryItems.remove( item.binaryItem.NAME );
		else
		if( item.confItem != null )
			mapConfItems.remove( item.confItem.NAME );
		else
		if( item.schema != null )
			mapSchemaItems.remove( item.schema.NAME );
		else
		if( !item.WSDL_REQUEST.isEmpty() )
			mapWS.remove( item.ID );
	}

}
