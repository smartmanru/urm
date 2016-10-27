package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaDistrDelivery {

	protected Meta meta;
	MetaDistr dist;
	
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
	}

	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		FOLDER = ConfReader.getAttrValue( node , "folder" , NAME );
		DESC = ConfReader.getAttrValue( node , "desc" );
		
		loadBinaryItems( action , node );
		loadConfigurationComponents( action , node );
		loadDatabaseItems( action , node );
	}

	public MetaDistrDelivery copy( ActionBase action , Meta meta , MetaDistr distr ) throws Exception {
		MetaDistrDelivery r = new MetaDistrDelivery( meta , distr );
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
	
	public void loadBinaryItems( ActionBase action , Node node ) throws Exception {
		mapBinaryItems = new HashMap<String,MetaDistrBinaryItem>();
		
		Node[] items = ConfReader.xmlGetChildren( node , "distitem" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			MetaDistrBinaryItem item = new MetaDistrBinaryItem( meta , this );
			item.load( action , itemNode );
			mapBinaryItems.put( item.KEY , item );
		}
	}
	
	public void loadConfigurationComponents( ActionBase action , Node node ) throws Exception {
		mapConfComps = new HashMap<String,MetaDistrConfItem>();
		
		Node[] items = ConfReader.xmlGetChildren( node , "confitem" );
		if( items == null )
			return;
		
		for( Node compNode : items ) {
			MetaDistrConfItem item = new MetaDistrConfItem( meta , this );
			item.load( action , compNode );
			mapConfComps.put( item.KEY , item );
		}
	}
	
	public void loadDatabaseItems( ActionBase action , Node node ) throws Exception {
		mapDatabaseSchema = new HashMap<String,MetaDatabaseSchema>();
		
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

}
