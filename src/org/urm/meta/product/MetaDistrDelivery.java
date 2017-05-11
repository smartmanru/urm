package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrDelivery {

	public Meta meta;
	public MetaDistr dist;
	public MetaDatabase db;
	
	public String NAME;
	public String FOLDER;
	public String DESC;
	public boolean allSchemas;

	private Map<String,MetaDistrBinaryItem> mapBinaryItems;
	private Map<String,MetaDistrConfItem> mapConfComps;
	private Map<String,MetaDatabaseSchema> mapDatabaseSchema;
	
	public MetaDistrDelivery( Meta meta , MetaDistr dist , MetaDatabase db ) {
		this.meta = meta;
		this.dist = dist;
		this.db = db;
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapConfComps = new HashMap<String,MetaDistrConfItem>();
		mapDatabaseSchema = new HashMap<String,MetaDatabaseSchema>();
		allSchemas = false;
	}

	public void createDelivery( ServerTransaction transaction , String NAME , String FOLDER , String DESC ) {
		this.NAME = NAME;
		this.FOLDER = FOLDER;
		this.DESC = DESC;
	}
	
	public void modifyDelivery( ServerTransaction transaction , String NAME , String FOLDER , String DESC ) {
		this.NAME = NAME;
		this.FOLDER = FOLDER;
		this.DESC = DESC;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		FOLDER = ConfReader.getAttrValue( node , "folder" , NAME );
		DESC = ConfReader.getAttrValue( node , "desc" );
		allSchemas = ConfReader.getBooleanAttrValue( node , "dball" , false );
		
		loadBinaryItems( action , node );
		loadConfigurationComponents( action , node );
		if( !allSchemas )
			loadDatabaseItems( action , node );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "folder" , FOLDER );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "dball" , Common.getBooleanValue( allSchemas ) );
		
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
				Common.xmlSetElementAttr( doc , itemElement , "schema" , item.SCHEMA );
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
		
		MetaDatabase database = meta.getDatabase( action );
		for( Node item : items ) {
			String schemaName = ConfReader.getAttrValue( item , "schema" );
			MetaDatabaseSchema schema = database.getSchema( action , schemaName );
			mapDatabaseSchema.put( schemaName , schema );
		}
	}

	public MetaDistrDelivery copy( ActionBase action , Meta meta , MetaDistr distr , MetaDatabase db ) throws Exception {
		MetaDistrDelivery r = new MetaDistrDelivery( meta , distr , db );
		r.NAME = NAME;
		r.FOLDER = FOLDER;
		r.DESC = DESC;
		r.allSchemas = allSchemas;
		
		for( MetaDistrBinaryItem item : mapBinaryItems.values() ) {
			MetaDistrBinaryItem ritem = item.copy( action , meta , r );
			r.mapBinaryItems.put( ritem.KEY , ritem );
		}
			
		for( MetaDistrConfItem item : mapConfComps.values() ) {
			MetaDistrConfItem ritem = item.copy( action , meta , r );
			r.mapConfComps.put( ritem.KEY , ritem );
		}
			
		MetaDatabase rdatabase = meta.getDatabase( action ); 
		for( MetaDatabaseSchema item : mapDatabaseSchema.values() ) {
			MetaDatabaseSchema ritem = rdatabase.getSchema( action , item.SCHEMA );
			r.mapDatabaseSchema.put( ritem.SCHEMA , ritem );
		}
			
		return( r );
	}

	public boolean isEmpty() {
		if( mapBinaryItems.isEmpty() && mapConfComps.isEmpty() && mapDatabaseSchema.isEmpty() )
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
			return( db.getSchema( action , NAME ) );
			
		MetaDatabaseSchema item = mapDatabaseSchema.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliverySchema1 , "unknown delivery schema=" + NAME , NAME );
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

	public boolean hasBinaryItems() {
		if( mapBinaryItems.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean hasDatabaseItems() {
		if( allSchemas )
			return( db.isEmpty() );
			
		if( mapDatabaseSchema.isEmpty() )
			return( false );
		return( true );
	}

	public void deleteAllItems( ServerTransaction transaction ) throws Exception {
		for( MetaDistrBinaryItem item : mapBinaryItems.values() )
			deleteBinaryItemInternal( transaction , item );
		for( MetaDistrConfItem item : mapConfComps.values() )
			deleteConfItemInternal( transaction , item );
	}

	public void createBinaryItem( ServerTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		mapBinaryItems.put( item.KEY , item );
	}
	
	public void modifyBinaryItem( ServerTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
	}
	
	public void moveItemToThis( ServerTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		if( item.delivery == this )
			return;
			
		item.delivery.mapBinaryItems.remove( item.KEY );
		mapBinaryItems.put( item.KEY , item );
		item.setDelivery( transaction , this );
	}

	public void deleteBinaryItem( ServerTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		deleteBinaryItemInternal( transaction , item );
		mapBinaryItems.remove( item.KEY );
	}

	private void deleteBinaryItemInternal( ServerTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		for( MetaDistrComponent comp : dist.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findBinaryItem( item.KEY );
			if( compItem != null )
				comp.removeCompItem( transaction , compItem );
		}
		meta.deleteBinaryItemFromEnvironments( transaction , item );
	}
	
	public void createConfItem( ServerTransaction transaction , MetaDistrConfItem item ) throws Exception {
		mapConfComps.put( item.KEY , item );
	}
	
	public void modifyConfItem( ServerTransaction transaction , MetaDistrConfItem item ) throws Exception {
	}

	public void deleteConfItem( ServerTransaction transaction , MetaDistrConfItem item ) throws Exception {
		deleteConfItemInternal( transaction , item );
		mapConfComps.remove( item.KEY );
	}
	
	private void deleteConfItemInternal( ServerTransaction transaction , MetaDistrConfItem item ) throws Exception {
		for( MetaDistrComponent comp : dist.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findConfItem( item.KEY );
			if( compItem != null )
				comp.removeCompItem( transaction , compItem );
		}
		meta.deleteConfItemFromEnvironments( transaction , item );
	}

	public void deleteSchema( ServerTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		if( allSchemas )
			return;
		
		mapDatabaseSchema.remove( schema.SCHEMA );
	}

	public void setDatabaseAll( ServerTransaction transaction ) throws Exception {
		allSchemas = true;
		mapDatabaseSchema.clear();
	}
	
	public void setDatabaseSet( ServerTransaction transaction , MetaDatabaseSchema[] set ) throws Exception {
		allSchemas = false;
			
		mapDatabaseSchema.clear();
		for( MetaDatabaseSchema schema : set )
			mapDatabaseSchema.put( schema.SCHEMA , schema );
	}
	
}
