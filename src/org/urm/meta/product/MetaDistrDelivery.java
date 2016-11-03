package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrDelivery {

	public Meta meta;
	public MetaDistr dist;
	
	public String NAME;
	public String FOLDER;
	public String DESC;
	public String SCHEMASET;

	Map<String,MetaDistrBinaryItem> mapBinaryItems;
	Map<String,MetaDistrConfItem> mapConfComps;
	Map<String,MetaDatabaseSchema> mapDatabaseSchema;
	
	public MetaDistrDelivery( Meta meta , MetaDistr dist ) {
		this.meta = meta;
		this.dist = dist;
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		mapConfComps = new HashMap<String,MetaDistrConfItem>();
		mapDatabaseSchema = new HashMap<String,MetaDatabaseSchema>();
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
		
		loadBinaryItems( action , node );
		loadConfigurationComponents( action , node );
		loadDatabaseItems( action , node );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "folder" , FOLDER );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		
		for( MetaDistrBinaryItem item : mapBinaryItems.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "distitem" );
			item.save( action , doc , itemElement );
		}
			
		for( MetaDistrConfItem item : mapConfComps.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "confitem" );
			item.save( action , doc , itemElement );
		}
			
		for( MetaDatabaseSchema item : mapDatabaseSchema.values() ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "database" );
			Common.xmlSetElementAttr( doc , itemElement , "schema" , item.SCHEMA );
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
		
		SCHEMASET = Common.getList( Common.getSortedKeys( mapDatabaseSchema ) , " " );
	}

	public MetaDistrDelivery copy( ActionBase action , Meta meta , MetaDistr distr ) throws Exception {
		MetaDistrDelivery r = new MetaDistrDelivery( meta , distr );
		r.NAME = NAME;
		r.FOLDER = FOLDER;
		r.DESC = DESC;
		r.SCHEMASET = SCHEMASET;
		
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
	
	public MetaDistrBinaryItem getBinaryItem( ActionBase action , String NAME ) throws Exception {
		MetaDistrBinaryItem item = mapBinaryItems.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliveryBinaryItem1 , "unknown delivery binary item=" + NAME , NAME );
		return( item );
	}
	
	public MetaDistrConfItem getConfItem( ActionBase action , String NAME ) throws Exception {
		MetaDistrConfItem item = mapConfComps.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliveryConfigurationItem1 , "unknown delivery configuration item=" + NAME , NAME );
		return( item );
	}

	public MetaDatabaseSchema findSchema( String NAME ) {
		return( mapDatabaseSchema.get( NAME ) );
	}
	
	public MetaDatabaseSchema getSchema( ActionBase action , String NAME ) throws Exception {
		MetaDatabaseSchema item = mapDatabaseSchema.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliverySchema1 , "unknown delivery schema=" + NAME , NAME );
		return( item );
	}
	
	public Map<String,MetaDistrBinaryItem> getBinaryItems( ActionBase action ) throws Exception {
		return( mapBinaryItems );
	}
	
	public Map<String,MetaDistrConfItem> getConfigurationItems( ActionBase action ) throws Exception {
		return( mapConfComps );
	}

	public Map<String,MetaDatabaseSchema> getDatabaseSchemes( ActionBase action ) throws Exception {
		return( mapDatabaseSchema );
	}

	public boolean hasDatabaseItems( ActionBase action ) throws Exception {
		if( mapDatabaseSchema.isEmpty() )
			return( false );
		return( true );
	}

	public void deleteAllItems( ServerTransaction transaction ) throws Exception {
		for( MetaDistrBinaryItem item : mapBinaryItems.values() ) {
			for( MetaDistrComponent comp : dist.getComponents() ) {
				MetaDistrComponentItem compItem = comp.findBinaryItem( item.KEY );
				if( compItem != null )
					comp.removeCompItem( transaction , compItem );
			}
		}
		for( MetaDistrConfItem item : mapConfComps.values() ) {
			for( MetaDistrComponent comp : dist.getComponents() ) {
				MetaDistrComponentItem compItem = comp.findConfItem( item.KEY );
				if( compItem != null )
					comp.removeCompItem( transaction , compItem );
			}
		}
		for( MetaDatabaseSchema item : mapDatabaseSchema.values() ) {
			for( MetaDistrComponent comp : dist.getComponents() ) {
				MetaDistrComponentItem compItem = comp.findSchemaItem( item.SCHEMA );
				if( compItem != null )
					comp.removeCompItem( transaction , compItem );
			}
		}
	}
	
}
