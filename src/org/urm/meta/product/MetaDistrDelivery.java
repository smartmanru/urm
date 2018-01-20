package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrDelivery {

	public Meta meta;
	public MetaDistr dist;
	public MetaDatabase db;
	public MetaDocs docs;
	
	public String NAME;
	public String FOLDER;
	public String DESC;
	public boolean allSchemas;
	public String UNIT;
	public boolean allDocs;

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
		allSchemas = false;
		allDocs = false;
	}

	public void createDelivery( EngineTransaction transaction , String NAME , String FOLDER , String DESC , String UNIT ) {
		this.NAME = NAME;
		this.FOLDER = FOLDER;
		this.DESC = DESC;
		this.UNIT = UNIT;
	}
	
	public void modifyDelivery( EngineTransaction transaction , String NAME , String FOLDER , String DESC , String UNIT ) {
		this.NAME = NAME;
		this.FOLDER = FOLDER;
		this.DESC = DESC;
		this.UNIT = UNIT;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		FOLDER = ConfReader.getAttrValue( node , "folder" , NAME );
		DESC = ConfReader.getAttrValue( node , "desc" );
		allSchemas = ConfReader.getBooleanAttrValue( node , "dball" , false );
		UNIT = ConfReader.getAttrValue( node , "unit" );
		allDocs = ConfReader.getBooleanAttrValue( node , "docall" , false );
		
		loadBinaryItems( action , node );
		loadConfigurationComponents( action , node );
		if( !allSchemas )
			loadDatabaseItems( action , node );
		if( !allDocs )
			loadProductDocuments( action , node );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "folder" , FOLDER );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "dball" , Common.getBooleanValue( allSchemas ) );
		Common.xmlSetElementAttr( doc , root , "unit" , UNIT );
		Common.xmlSetElementAttr( doc , root , "docall" , Common.getBooleanValue( allDocs ) );
		
		for( MetaDistrBinaryItem item : mapBinaryItems.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "distitem" );
			item.save( action , doc , itemElement );
		}
			
		for( MetaDistrConfItem item : mapConfComps.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "confitem" );
			item.save( action , doc , itemElement );
		}

		if( !allSchemas ) {
			for( MetaDatabaseSchema item : mapDatabaseSchema.values() ) {
				Element itemElement = Common.xmlCreateElement( doc , root , "database" );
				Common.xmlSetElementAttr( doc , itemElement , "schema" , item.NAME );
			}
		}
		
		if( !allDocs ) {
			for( MetaProductDoc item : mapDocuments.values() ) {
				Element itemElement = Common.xmlCreateElement( doc , root , "document" );
				Common.xmlSetElementAttr( doc , itemElement , "name" , item.NAME );
			}
		}
	}
	
	private void loadBinaryItems( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "distitem" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			MetaDistrBinaryItem item = new MetaDistrBinaryItem( meta , this );
			item.load( action , itemNode );
			mapBinaryItems.put( item.KEY , item );
		}
	}

	private void loadConfigurationComponents( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "confitem" );
		if( items == null )
			return;
		
		for( Node compNode : items ) {
			MetaDistrConfItem item = new MetaDistrConfItem( meta , this );
			item.load( action , compNode );
			mapConfComps.put( item.KEY , item );
		}
	}
	
	private void loadDatabaseItems( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "database" );
		if( items == null )
			return;
		
		for( Node item : items ) {
			String schemaName = ConfReader.getAttrValue( item , "schema" );
			MetaDatabaseSchema schema = db.getSchema( schemaName );
			mapDatabaseSchema.put( schemaName , schema );
		}
	}

	private void loadProductDocuments( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "document" );
		if( items == null )
			return;
		
		for( Node item : items ) {
			String docName = ConfReader.getAttrValue( item , "name" );
			MetaProductDoc doc = docs.getDoc( action , docName );
			mapDocuments.put( docName , doc );
		}
	}

	public MetaDistrDelivery copy( ActionBase action , Meta meta , MetaDistr distr , MetaDatabase rdb , MetaDocs rdocs ) throws Exception {
		MetaDistrDelivery r = new MetaDistrDelivery( meta , distr , rdb , rdocs );
		r.NAME = NAME;
		r.FOLDER = FOLDER;
		r.DESC = DESC;
		r.allSchemas = allSchemas;
		r.UNIT = UNIT;
		r.allDocs = allDocs;
		
		for( MetaDistrBinaryItem item : mapBinaryItems.values() ) {
			MetaDistrBinaryItem ritem = item.copy( action , meta , r );
			r.mapBinaryItems.put( ritem.KEY , ritem );
		}
			
		for( MetaDistrConfItem item : mapConfComps.values() ) {
			MetaDistrConfItem ritem = item.copy( action , meta , r );
			r.mapConfComps.put( ritem.KEY , ritem );
		}
			
		for( MetaDatabaseSchema item : mapDatabaseSchema.values() ) {
			MetaDatabaseSchema ritem = rdb.getSchema( item.NAME );
			r.mapDatabaseSchema.put( ritem.NAME , ritem );
		}
			
		for( MetaProductDoc item : mapDocuments.values() ) {
			MetaProductDoc ritem = rdocs.getDoc( action , item.NAME );
			r.mapDocuments.put( ritem.NAME , ritem );
		}
			
		return( r );
	}

	public boolean isEmpty() {
		if( mapBinaryItems.isEmpty() && mapConfComps.isEmpty() && mapDatabaseSchema.isEmpty() && mapDocuments.isEmpty() )
			return( true );
		return( false );
	}
	
	public MetaDistrBinaryItem findBinaryItem( String NAME ) {
		return( mapBinaryItems.get( NAME ) );
	}
	
	public MetaDistrBinaryItem getBinaryItem( ActionBase action , String NAME ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItems.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliveryBinaryItem1 , "unknown delivery binary item=" + NAME , NAME );
		return( item );
	}
	
	public MetaDistrConfItem findConfItem( String NAME ) {
		return( mapConfComps.get( NAME ) );
	}
	
	public MetaDistrConfItem getConfItem( ActionBase action , String NAME ) throws Exception {
		MetaDistrConfItem item = mapConfComps.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliveryConfigurationItem1 , "unknown delivery configuration item=" + NAME , NAME );
		return( item );
	}

	public MetaDatabaseSchema findSchema( String NAME ) {
		if( allSchemas )
			return( db.findSchema( NAME ) );
			
		return( mapDatabaseSchema.get( NAME ) );
	}
	
	public MetaDatabaseSchema getSchema( ActionBase action , String NAME ) throws Exception {
		if( allSchemas )
			return( db.getSchema( NAME ) );
			
		MetaDatabaseSchema item = mapDatabaseSchema.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliverySchema1 , "unknown delivery schema=" + NAME , NAME );
		return( item );
	}

	public MetaProductDoc findDoc( String NAME ) {
		if( allDocs )
			return( docs.findDoc( NAME ) );
			
		return( mapDocuments.get( NAME ) );
	}
	
	public MetaProductDoc getDoc( ActionBase action , String NAME ) throws Exception {
		if( allDocs )
			return( docs.getDoc( action , NAME ) );
			
		MetaProductDoc item = mapDocuments.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliveryDoc1 , "unknown delivery doc=" + NAME , NAME );
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
		if( allSchemas )
			return( db.getSchemaNames() );
		
		return( Common.getSortedKeys( mapDatabaseSchema ) );
	}
	
	public MetaDatabaseSchema[] getDatabaseSchemes() {
		if( allSchemas )
			return( db.getSchemaList() );
			
		return( mapDatabaseSchema.values().toArray( new MetaDatabaseSchema[0] ) );
	}

	public String[] getDocNames() {
		if( allDocs )
			return( docs.getDocNames() );
		
		return( Common.getSortedKeys( mapDocuments ) );
	}
	
	public MetaProductDoc[] getDocs() {
		if( allSchemas )
			return( docs.getDocList() );
			
		return( mapDocuments.values().toArray( new MetaProductDoc[0] ) );
	}

	public boolean hasBinaryItems() {
		if( mapBinaryItems.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean hasDatabaseItems() {
		if( allSchemas ) {
			if( !db.isEmpty() )
				return( true );
			return( false );
		}
			
		if( mapDatabaseSchema.isEmpty() )
			return( false );
		return( true );
	}

	public void deleteAllItems( EngineTransaction transaction ) throws Exception {
		for( MetaDistrBinaryItem item : mapBinaryItems.values() )
			deleteBinaryItemInternal( transaction , item );
		for( MetaDistrConfItem item : mapConfComps.values() )
			deleteConfItemInternal( transaction , item );
	}

	public void createBinaryItem( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		mapBinaryItems.put( item.KEY , item );
	}
	
	public void modifyBinaryItem( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
	}
	
	public void moveItemToThis( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		if( item.delivery == this )
			return;
			
		item.delivery.mapBinaryItems.remove( item.KEY );
		mapBinaryItems.put( item.KEY , item );
		item.setDelivery( transaction , this );
	}

	public void deleteBinaryItem( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		deleteBinaryItemInternal( transaction , item );
		mapBinaryItems.remove( item.KEY );
	}

	private void deleteBinaryItemInternal( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		for( MetaDistrComponent comp : dist.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findBinaryItem( item.KEY );
			if( compItem != null )
				comp.removeCompItem( transaction , compItem );
		}
		meta.getStorage().deleteBinaryItemFromEnvironments( transaction , item );
	}
	
	public void createConfItem( EngineTransaction transaction , MetaDistrConfItem item ) throws Exception {
		mapConfComps.put( item.KEY , item );
	}
	
	public void modifyConfItem( EngineTransaction transaction , MetaDistrConfItem item ) throws Exception {
	}

	public void deleteConfItem( EngineTransaction transaction , MetaDistrConfItem item ) throws Exception {
		deleteConfItemInternal( transaction , item );
		mapConfComps.remove( item.KEY );
	}
	
	private void deleteConfItemInternal( EngineTransaction transaction , MetaDistrConfItem item ) throws Exception {
		for( MetaDistrComponent comp : dist.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findConfItem( item.KEY );
			if( compItem != null )
				comp.removeCompItem( transaction , compItem );
		}
		meta.getStorage().deleteConfItemFromEnvironments( transaction , item );
	}

	public void deleteSchema( EngineTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		if( allSchemas )
			return;
		
		mapDatabaseSchema.remove( schema.NAME );
	}

	public void setDatabaseAll( EngineTransaction transaction ) throws Exception {
		allSchemas = true;
		mapDatabaseSchema.clear();
	}
	
	public void setDatabaseSet( EngineTransaction transaction , MetaDatabaseSchema[] set ) throws Exception {
		allSchemas = false;
			
		mapDatabaseSchema.clear();
		for( MetaDatabaseSchema schema : set )
			mapDatabaseSchema.put( schema.NAME , schema );
	}
	
	public void deleteDoc( EngineTransaction transaction , MetaProductDoc doc ) throws Exception {
		if( allDocs )
			return;
		
		mapDocuments.remove( doc.NAME );
	}

	public void setDocAll( EngineTransaction transaction ) throws Exception {
		allDocs = true;
		mapDocuments.clear();
	}
	
	public void setDocSet( EngineTransaction transaction , MetaProductDoc[] set ) throws Exception {
		allDocs = false;
			
		mapDocuments.clear();
		for( MetaProductDoc doc : set )
			mapDocuments.put( doc.NAME , doc );
	}
	
	public void clearUnit( EngineTransaction transaction ) throws Exception {
		UNIT = "";
	}

}
