package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;

public class MetaDistrDelivery {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_UNIT_NAME = "unit";
	public static String PROPERTY_FOLDER = "folder";
	public static String PROPERTY_SCHEMA_ANY = "schema_any";
	public static String PROPERTY_DOC_ANY = "doc_any";
	
	public Meta meta;
	public MetaDistr dist;
	public MetaDatabase db;
	public MetaDocs docs;

	public int ID;
	public Integer UNIT_ID;
	public String NAME;
	public String DESC;
	public String FOLDER;
	public boolean SCHEMA_ANY;
	public boolean DOC_ANY;
	public int PV;
	
	private Map<String,MetaDistrBinaryItem> mapBinaryItems;
	private Map<String,MetaDistrConfItem> mapConfComps;
	private Map<String,MetaDatabaseSchema> mapDatabaseSchema;
	private Map<String,MetaProductDoc> mapDocuments;
	
	public MetaDistrDelivery( Meta meta , MetaDistr dist , MetaDatabase db , MetaDocs docs ) {
		this.meta = meta;
		this.dist = dist;
		this.db = db;
		this.docs = docs;
		
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapConfComps = new HashMap<String,MetaDistrConfItem>();
		mapDatabaseSchema = new HashMap<String,MetaDatabaseSchema>();
		mapDocuments = new HashMap<String,MetaProductDoc>();

		ID = -1;
		PV = -1;
		SCHEMA_ANY = false;
		DOC_ANY = false;
	}

	public MetaDistrDelivery copy( Meta meta , MetaDistr distr , MetaDatabase rdb , MetaDocs rdocs ) throws Exception {
		MetaDistrDelivery r = new MetaDistrDelivery( meta , distr , rdb , rdocs );
		r.ID = ID;
		r.UNIT_ID = UNIT_ID; 
		r.NAME = NAME;
		r.FOLDER = FOLDER;
		r.DESC = DESC;
		r.SCHEMA_ANY = SCHEMA_ANY;
		r.DOC_ANY = DOC_ANY;
		r.PV = PV;
		
		for( MetaDistrBinaryItem item : mapBinaryItems.values() ) {
			MetaDistrBinaryItem ritem = item.copy( meta , r );
			r.mapBinaryItems.put( ritem.NAME , ritem );
		}
			
		for( MetaDistrConfItem item : mapConfComps.values() ) {
			MetaDistrConfItem ritem = item.copy( meta , r );
			r.mapConfComps.put( ritem.NAME , ritem );
		}
			
		for( MetaDatabaseSchema item : mapDatabaseSchema.values() ) {
			MetaDatabaseSchema ritem = rdb.getSchema( item.NAME );
			r.mapDatabaseSchema.put( ritem.NAME , ritem );
		}
			
		for( MetaProductDoc item : mapDocuments.values() ) {
			MetaProductDoc ritem = rdocs.getDoc( item.NAME );
			r.mapDocuments.put( ritem.NAME , ritem );
		}
			
		return( r );
	}

	public void createDelivery( Integer unitId , String name , String desc , String folder ) {
		modifyDelivery( unitId , name , desc , folder );
	}
	
	public void modifyDelivery( Integer unitId , String name , String desc , String folder ) {
		this.UNIT_ID = unitId;
		this.NAME = name;
		this.DESC = desc;
		this.FOLDER = folder;
	}
	
	public boolean isEmpty() {
		if( mapBinaryItems.isEmpty() && mapConfComps.isEmpty() && mapDatabaseSchema.isEmpty() && mapDocuments.isEmpty() )
			return( true );
		return( false );
	}
	
	public MetaDistrBinaryItem findBinaryItem( String NAME ) {
		return( mapBinaryItems.get( NAME ) );
	}
	
	public MetaDistrBinaryItem getBinaryItem( String NAME ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItems.get( NAME );
		if( item == null )
			Common.exit1( _Error.UnknownDeliveryBinaryItem1 , "unknown delivery binary item=" + NAME , NAME );
		return( item );
	}
	
	public MetaDistrConfItem findConfItem( String NAME ) {
		return( mapConfComps.get( NAME ) );
	}
	
	public MetaDistrConfItem getConfItem( String NAME ) throws Exception {
		MetaDistrConfItem item = mapConfComps.get( NAME );
		if( item == null )
			Common.exit1( _Error.UnknownDeliveryConfigurationItem1 , "unknown delivery configuration item=" + NAME , NAME );
		return( item );
	}

	public MetaDatabaseSchema findSchema( String NAME ) {
		if( SCHEMA_ANY )
			return( db.findSchema( NAME ) );
			
		return( mapDatabaseSchema.get( NAME ) );
	}
	
	public MetaDatabaseSchema getSchema( String NAME ) throws Exception {
		if( SCHEMA_ANY )
			return( db.getSchema( NAME ) );
			
		MetaDatabaseSchema item = mapDatabaseSchema.get( NAME );
		if( item == null )
			Common.exit1( _Error.UnknownDeliverySchema1 , "unknown delivery schema=" + NAME , NAME );
		return( item );
	}

	public MetaProductDoc findDoc( String NAME ) {
		if( DOC_ANY )
			return( docs.findDoc( NAME ) );
			
		return( mapDocuments.get( NAME ) );
	}
	
	public MetaProductDoc getDoc( String NAME ) throws Exception {
		if( DOC_ANY )
			return( docs.getDoc( NAME ) );
			
		MetaProductDoc item = mapDocuments.get( NAME );
		if( item == null )
			Common.exit1( _Error.UnknownDeliveryDoc1 , "unknown delivery doc=" + NAME , NAME );
		return( item );
	}

	public String[] getBinaryItemNames() {
		return( Common.getSortedKeys( mapBinaryItems ) );
	}
	
	public MetaDistrBinaryItem[] getBinaryItems() {
		return( mapBinaryItems.values().toArray( new MetaDistrBinaryItem[0] ) );
	}
	
	public String[] getConfItemNames() {
		return( Common.getSortedKeys( mapConfComps ) );
	}
	
	public MetaDistrConfItem[] getConfItems() {
		return( mapConfComps.values().toArray( new MetaDistrConfItem[0] ) );
	}

	public String[] getDatabaseSchemaNames() {
		if( SCHEMA_ANY )
			return( db.getSchemaNames() );
		
		return( Common.getSortedKeys( mapDatabaseSchema ) );
	}
	
	public MetaDatabaseSchema[] getDatabaseSchemes() {
		if( SCHEMA_ANY )
			return( db.getSchemaList() );
			
		return( mapDatabaseSchema.values().toArray( new MetaDatabaseSchema[0] ) );
	}

	public String[] getDocNames() {
		if( DOC_ANY )
			return( docs.getDocNames() );
		
		return( Common.getSortedKeys( mapDocuments ) );
	}
	
	public MetaProductDoc[] getDocs() {
		if( SCHEMA_ANY )
			return( docs.getDocList() );
			
		return( mapDocuments.values().toArray( new MetaProductDoc[0] ) );
	}

	public boolean hasBinaryItems() {
		if( mapBinaryItems.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean hasDatabaseItems() {
		if( SCHEMA_ANY ) {
			if( !db.isEmpty() )
				return( true );
			return( false );
		}
			
		if( mapDatabaseSchema.isEmpty() )
			return( false );
		return( true );
	}

	public void addBinaryItem( MetaDistrBinaryItem item ) {
		mapBinaryItems.put( item.NAME , item );
	}
	
	public void updateBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		Common.changeMapKey( mapBinaryItems , item , item.NAME );
	}
	
	public void moveItemToThis( MetaDistrBinaryItem item ) throws Exception {
		if( item.delivery == this )
			return;
			
		item.delivery.mapBinaryItems.remove( item.NAME );
		mapBinaryItems.put( item.NAME , item );
		item.setDelivery( this );
	}

	public void removeBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		for( MetaDistrComponent comp : dist.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findBinaryItem( item.NAME );
			if( compItem != null )
				comp.removeCompItem( compItem );
		}
		
		mapBinaryItems.remove( item.NAME );
	}
	
	public void addConfItem( MetaDistrConfItem item ) throws Exception {
		mapConfComps.put( item.NAME , item );
	}
	
	public void updateConfItem( MetaDistrConfItem item ) throws Exception {
		Common.changeMapKey( mapConfComps , item , item.NAME );
	}

	public void removeConfItem( MetaDistrConfItem item ) throws Exception {
		for( MetaDistrComponent comp : dist.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findConfItem( item.NAME );
			if( compItem != null )
				comp.removeCompItem( compItem );
		}
		
		mapConfComps.remove( item.NAME );
	}
	
	public void removeSchema( MetaDatabaseSchema schema ) throws Exception {
		mapDatabaseSchema.remove( schema.NAME );
	}

	public void setDatabaseAll( boolean all ) throws Exception {
		SCHEMA_ANY = all;
		if( all )
			mapDatabaseSchema.clear();
	}

	public void addSchema( MetaDatabaseSchema schema ) throws Exception {
		SCHEMA_ANY = false;
		mapDatabaseSchema.put( schema.NAME , schema );
	}
	
	public void setDatabaseSet( MetaDatabaseSchema[] set ) throws Exception {
		SCHEMA_ANY = false;
			
		mapDatabaseSchema.clear();
		for( MetaDatabaseSchema schema : set )
			mapDatabaseSchema.put( schema.NAME , schema );
	}
	
	public void removeDoc( MetaProductDoc doc ) throws Exception {
		mapDocuments.remove( doc.NAME );
	}

	public void setDocAll( boolean all ) {
		DOC_ANY = all;
		if( all )
			mapDocuments.clear();
	}
	
	public void addDocument( MetaProductDoc doc ) throws Exception {
		DOC_ANY = false;
		mapDocuments.put( doc.NAME , doc );
	}
	
	public void setDocSet( MetaProductDoc[] set ) throws Exception {
		DOC_ANY = false;
			
		mapDocuments.clear();
		for( MetaProductDoc doc : set )
			mapDocuments.put( doc.NAME , doc );
	}
	
	public void clearUnit() throws Exception {
		UNIT_ID = null;
	}

}
