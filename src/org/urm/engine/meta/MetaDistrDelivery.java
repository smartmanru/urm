package org.urm.engine.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.action.ActionBase;
import org.urm.engine.meta.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaDistrDelivery {

	protected Meta meta;
	MetaDistr dist;
	
	public String NAME;
	public String FOLDER;
	public String SCHEMASET;

	Map<String,MetaDistrBinaryItem> mapBinaryItems;
	Map<String,MetaDistrConfItem> mapConfComps;
	Map<String,MetaDatabaseSchema> mapDatabaseSchema;
	Map<String,MetaDatabaseDatagroup> mapDatabaseDatagroup;
	
	public MetaDistrDelivery( Meta meta , MetaDistr dist ) {
		this.meta = meta;
		this.dist = dist;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		FOLDER = ConfReader.getAttrValue( node , "folder" , NAME );
		
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
	
	public MetaDatabaseDatagroup getDatagroup( ActionBase action , String NAME ) throws Exception {
		MetaDatabaseDatagroup item = mapDatabaseDatagroup.get( NAME );
		if( item == null )
			action.exit1( _Error.UnknownDeliveryDatagroup1 , "unknown delivery datagroup=" + NAME , NAME );
		return( item );
	}
	
	public Map<String,MetaDistrBinaryItem> getBinaryItems( ActionBase action ) throws Exception {
		return( mapBinaryItems );
	}
	
	public Map<String,MetaDistrConfItem> getConfigurationItems( ActionBase action ) throws Exception {
		return( mapConfComps );
	}

	public Map<String,MetaDatabaseDatagroup> getDatabaseDatagroups( ActionBase action ) throws Exception {
		return( mapDatabaseDatagroup );
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
		mapDatabaseDatagroup = new HashMap<String,MetaDatabaseDatagroup>();
		
		Node[] items = ConfReader.xmlGetChildren( node , "datagroup" );
		if( items == null )
			return;
		
		MetaDatabase database = meta.database;
		for( Node item : items ) {
			String datagroupName = ConfReader.getAttrValue( item , "name" );
			MetaDatabaseDatagroup datagroup = database.getDatagroup( action , datagroupName );
			mapDatabaseDatagroup.put( datagroupName , datagroup );
			
			for( MetaDatabaseSchema schema : datagroup.getSchemes( action ).values() ) {
				if( !mapDatabaseSchema.containsKey( schema.SCHEMA ) )
					mapDatabaseSchema.put( schema.SCHEMA , schema );
			}
		}
		
		SCHEMASET = Common.getList( Common.getSortedKeys( mapDatabaseSchema ) , " " );
	}

}
