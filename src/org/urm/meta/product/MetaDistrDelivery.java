package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumChangeType;

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
	public DBEnumChangeType CHANGETYPE;
	
	private Map<String,MetaDistrBinaryItem> mapBinaryItems;
	private Map<Integer,MetaDistrBinaryItem> mapBinaryItemsById;
	private Map<String,MetaDistrConfItem> mapConfComps;
	private Map<Integer,MetaDistrConfItem> mapConfCompsById;
	private Map<Integer,DBEnumChangeType> mapDatabaseSchema;
	private Map<Integer,DBEnumChangeType> mapDocuments;
	
	public MetaDistrDelivery( Meta meta , MetaDistr dist , MetaDatabase db , MetaDocs docs ) {
		this.meta = meta;
		this.dist = dist;
		this.db = db;
		this.docs = docs;
		
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapBinaryItemsById = new HashMap<Integer,MetaDistrBinaryItem>();
		mapConfComps = new HashMap<String,MetaDistrConfItem>();
		mapConfCompsById = new HashMap<Integer,MetaDistrConfItem>();
		mapDatabaseSchema = new HashMap<Integer,DBEnumChangeType>();
		mapDocuments = new HashMap<Integer,DBEnumChangeType>();

		ID = -1;
		PV = -1;
		SCHEMA_ANY = false;
		DOC_ANY = false;
	}

	public MetaDistrDelivery copy( Meta meta , MetaDistr distr , MetaDatabase rdb , MetaDocs rdocs , boolean all ) throws Exception {
		MetaDistrDelivery r = new MetaDistrDelivery( meta , distr , rdb , rdocs );
		r.ID = ID;
		r.UNIT_ID = UNIT_ID; 
		r.NAME = NAME;
		r.FOLDER = FOLDER;
		r.DESC = DESC;
		r.SCHEMA_ANY = SCHEMA_ANY;
		r.DOC_ANY = DOC_ANY;
		r.PV = PV;
		r.CHANGETYPE = CHANGETYPE;
		
		if( all ) {
			for( MetaDistrBinaryItem item : mapBinaryItems.values() ) {
				MetaDistrBinaryItem ritem = item.copy( meta , r );
				r.addBinaryItem( ritem );
			}
				
			for( MetaDistrConfItem item : mapConfComps.values() ) {
				MetaDistrConfItem ritem = item.copy( meta , r );
				r.addConfItem( ritem );
			}
				
			for( int id : mapDatabaseSchema.keySet() ) {
				MetaDatabaseSchema ritem = rdb.getSchema( id );
				DBEnumChangeType changeType = mapDatabaseSchema.get( id );
				r.addSchema( ritem , changeType );
			}
				
			for( int id : mapDocuments.keySet() ) {
				MetaProductDoc ritem = rdocs.getDoc( id );
				DBEnumChangeType changeType = mapDocuments.get( id );
				r.addDocument( ritem , changeType );
			}
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
		if( SCHEMA_ANY ) {
			MetaDatabase database = meta.getDatabase();
			if( !database.isEmpty() )
				return( false );
		}
		
		if( DOC_ANY ) {
			MetaDocs docs = meta.getDocs();
			if( !docs.isEmpty() )
				return( false );
		}
		
		if( mapBinaryItems.isEmpty() && mapConfComps.isEmpty() && mapDatabaseSchema.isEmpty() && mapDocuments.isEmpty() )
			return( true );
		return( false );
	}
	
	public MetaDistrBinaryItem findBinaryItem( String name ) {
		return( mapBinaryItems.get( name ) );
	}
	
	public MetaDistrBinaryItem getBinaryItem( String name ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItems.get( name );
		if( item == null )
			Common.exit1( _Error.UnknownDeliveryBinaryItem1 , "unknown delivery binary item=" + name , name );
		return( item );
	}
	
	public MetaDistrBinaryItem getBinaryItem( int id ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItemsById.get( id );
		if( item == null )
			Common.exit1( _Error.UnknownDeliveryBinaryItem1 , "unknown delivery binary item=" + id , "" + id );
		return( item );
	}
	
	public MetaDistrConfItem findConfItem( String name ) {
		return( mapConfComps.get( name ) );
	}
	
	public MetaDistrConfItem getConfItem( String name ) throws Exception {
		MetaDistrConfItem item = mapConfComps.get( name );
		if( item == null )
			Common.exit1( _Error.UnknownDeliveryConfigurationItem1 , "unknown delivery configuration item=" + name , name );
		return( item );
	}

	public MetaDistrConfItem getConfItem( int id ) throws Exception {
		MetaDistrConfItem item = mapConfCompsById.get( id );
		if( item == null )
			Common.exit1( _Error.UnknownDeliveryConfigurationItem1 , "unknown delivery configuration item=" + id , "" + id );
		return( item );
	}

	public MetaDatabaseSchema findSchema( String name ) {
		MetaDatabaseSchema schema = db.findSchema( name );
		if( schema == null )
			return( null );
		
		if( SCHEMA_ANY || mapDatabaseSchema.containsKey( schema.ID ) )
			return( schema );
			
		return( null );
	}
	
	public MetaDatabaseSchema getSchema( String name ) throws Exception {
		MetaDatabaseSchema schema = findSchema( name );
		if( schema == null )
			Common.exit1( _Error.UnknownDeliverySchema1 , "unknown delivery schema=" + name , name );
		return( schema );
	}

	public MetaProductDoc findDoc( String name ) {
		MetaProductDoc doc = docs.findDoc( name );
		if( doc == null )
			return( null );
		
		if( DOC_ANY || mapDocuments.containsKey( doc.ID ) )
			return( doc );
			
		return( null );
	}
	
	public MetaProductDoc getDoc( String name ) throws Exception {
		MetaProductDoc doc = findDoc( name );
		if( doc == null )
			Common.exit1( _Error.UnknownDeliveryDoc1 , "unknown delivery doc=" + name , name );
		return( doc );
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
		
		Map<String,MetaDatabaseSchema> set = new HashMap<String,MetaDatabaseSchema>();
		for( int id : mapDatabaseSchema.keySet() ) {
			MetaDatabaseSchema schema = db.findSchema( id );
			set.put( schema.NAME , schema );
		}
		return( Common.getSortedKeys( set ) );
	}

	public Integer[] getSchemaIds() {
		return( mapDatabaseSchema.keySet().toArray( new Integer[0] ) );
	}
	
	public MetaDatabaseSchema[] getDatabaseSchemes() {
		if( SCHEMA_ANY )
			return( db.getSchemaList() );
			
		List<MetaDatabaseSchema> list = new LinkedList<MetaDatabaseSchema>();
		for( String name : getDatabaseSchemaNames() ) {
			MetaDatabaseSchema schema = db.findSchema( name );
			list.add( schema );
		}
			
		return( list.toArray( new MetaDatabaseSchema[0] ) );
	}

	public String[] getDocNames() {
		if( DOC_ANY )
			return( docs.getDocNames() );
		
		Map<String,MetaProductDoc> set = new HashMap<String,MetaProductDoc>();
		for( int id : mapDocuments.keySet() ) {
			MetaProductDoc doc = docs.findDoc( id );
			set.put( doc.NAME , doc );
		}
		return( Common.getSortedKeys( set ) );
	}
	
	public MetaProductDoc[] getDocs() {
		if( DOC_ANY )
			return( docs.getDocList() );
			
		List<MetaProductDoc> list = new LinkedList<MetaProductDoc>();
		for( String name : getDocNames() ) {
			MetaProductDoc doc = docs.findDoc( name );
			list.add( doc );
		}
			
		return( list.toArray( new MetaProductDoc[0] ) );
	}

	public Integer[] getDocIds() {
		return( mapDocuments.keySet().toArray( new Integer[0] ) );
	}
	
	public boolean hasBinaryItems() {
		if( mapBinaryItems.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean hasConfItems() {
		if( mapConfComps.isEmpty() )
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

	public boolean hasDocItems() {
		if( DOC_ANY ) {
			if( !docs.isEmpty() )
				return( true );
			return( false );
		}
			
		if( mapDocuments.isEmpty() )
			return( false );
		return( true );
	}

	public void addBinaryItem( MetaDistrBinaryItem item ) {
		mapBinaryItems.put( item.NAME , item );
		mapBinaryItemsById.put( item.ID , item );
	}
	
	public void updateBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		Common.changeMapKey( mapBinaryItems , item , item.NAME );
	}
	
	public void moveItemToThis( MetaDistrBinaryItem item ) throws Exception {
		if( item.delivery == this )
			return;
			
		item.delivery.removeBinaryItemOnly( item );
		addBinaryItem( item );
		item.setDelivery( this );
	}

	public void removeBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		for( MetaDistrComponent comp : dist.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findBinaryItem( item.NAME );
			if( compItem != null )
				comp.removeCompItem( compItem );
		}
		
		removeBinaryItemOnly( item );
	}
	
	private void removeBinaryItemOnly( MetaDistrBinaryItem item ) throws Exception {
		mapBinaryItems.remove( item.NAME );
		mapBinaryItemsById.remove( item.ID );
	}
	
	public void addConfItem( MetaDistrConfItem item ) throws Exception {
		mapConfComps.put( item.NAME , item );
		mapConfCompsById.put( item.ID , item );
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
		mapConfCompsById.remove( item.ID );
	}
	
	public void removeSchema( MetaDatabaseSchema schema ) throws Exception {
		mapDatabaseSchema.remove( schema.ID );
	}

	public void setDatabaseAll( boolean all ) throws Exception {
		SCHEMA_ANY = all;
		if( all )
			mapDatabaseSchema.clear();
	}

	public void addSchema( MetaDatabaseSchema schema , DBEnumChangeType changeType ) throws Exception {
		SCHEMA_ANY = false;
		mapDatabaseSchema.put( schema.ID , changeType );
	}
	
	public DBEnumChangeType getSchemaChangeType( MetaDatabaseSchema schema ) {
		return( mapDatabaseSchema.get( schema.ID ) );
	}
	
	public DBEnumChangeType getSchemaChangeType( int id ) {
		return( mapDatabaseSchema.get( id ) );
	}
	
	public void removeDoc( MetaProductDoc doc ) throws Exception {
		mapDocuments.remove( doc.ID );
	}

	public void setDocAll( boolean all ) {
		DOC_ANY = all;
		if( all )
			mapDocuments.clear();
	}
	
	public void addDocument( MetaProductDoc doc , DBEnumChangeType changeType ) throws Exception {
		DOC_ANY = false;
		mapDocuments.put( doc.ID , changeType );
	}
	
	public DBEnumChangeType getDocChangeType( MetaProductDoc doc ) {
		return( mapDocuments.get( doc.ID ) );
	}
	
	public DBEnumChangeType getDocChangeType( int id ) {
		return( mapDocuments.get( id ) );
	}
	
	public void clearUnit() throws Exception {
		UNIT_ID = null;
	}

}
