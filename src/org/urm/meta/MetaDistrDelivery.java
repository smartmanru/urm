package org.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Metadata.VarNAMETYPE;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaDistrDelivery {

	Metadata meta;
	MetaDistr dist;
	
	public String NAME;
	public String FOLDER;
	public String SCHEMASET;

	Map<String,MetaDistrBinaryItem> mapBinaryItems;
	Map<String,MetaDistrConfItem> mapConfComps;
	Map<String,MetaDatabaseSchema> mapDatabaseSchema;
	Map<String,MetaDatabaseDatagroup> mapDatabaseDatagroup;
	
	public MetaDistrDelivery( Metadata meta , MetaDistr dist ) {
		this.meta = meta;
		this.dist = dist;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		NAME = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		FOLDER = ConfReader.getAttrValue( action , node , "folder" , NAME );
		
		loadBinaryItems( action , node );
		loadConfigurationComponents( action , node );
		loadDatabaseItems( action , node );
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
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "distitem" );
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
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "confitem" );
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
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "datagroup" );
		if( items == null )
			return;
		
		MetaDatabase database = meta.database;
		for( Node item : items ) {
			String datagroupName = ConfReader.getAttrValue( action , item , "name" );
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
