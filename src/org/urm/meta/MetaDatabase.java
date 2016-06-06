package org.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaDatabase {

	Metadata meta;

	public Map<String,MetaDatabaseSchema> mapSchema = new HashMap<String,MetaDatabaseSchema>();
	public Map<String,MetaDatabaseDatagroup> mapDatagroup = new HashMap<String,MetaDatabaseDatagroup>();
	
	public String ALIGNEDMAPPING;
	
	public MetaDatabase( Metadata meta ) {
		this.meta = meta;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		if( !loadAdministration( action , node ) )
			return;
		
		loadSchemaSet( action , node );
	}

	public boolean loadAdministration( ActionBase action , Node node ) throws Exception {
		Node administration = ConfReader.xmlGetFirstChild( action , node , "administration" );
		if( administration == null ) {
			action.debug( "database administration is missing, ignore database information." );
			return( false );
		}
		
		return( true );
	}

	public void loadSchemaSet( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( action , node , "schema" );
		if( items == null )
			return;
		
		for( Node schemaNode : items ) {
			MetaDatabaseSchema item = new MetaDatabaseSchema( meta , this );
			item.load( action , schemaNode );
			mapSchema.put( item.SCHEMA , item );
		}
		
		items = ConfReader.xmlGetChildren( action , node , "datagroup" );
		if( items == null )
			return;
		
		for( Node dgNode : items ) {
			MetaDatabaseDatagroup item = new MetaDatabaseDatagroup( meta , this );
			item.load( action , dgNode );
			mapDatagroup.put( item.NAME , item );
		}
	}
	
	public MetaDatabaseSchema getSchema( ActionBase action , String name ) throws Exception {
		MetaDatabaseSchema schema = mapSchema.get( name );
		if( schema == null )
			action.exit( "unknown schema=" + name );
		return( schema );
	}

	public MetaDatabaseDatagroup getDatagroup( ActionBase action , String name ) throws Exception {
		MetaDatabaseDatagroup datagroup = mapDatagroup.get( name );
		if( datagroup == null )
			action.exit( "unknown datagroup=" + name );
		return( datagroup );
	}
	
	public boolean checkAligned( ActionBase action , String id ) throws Exception {
		return( true );
	}
	
}
