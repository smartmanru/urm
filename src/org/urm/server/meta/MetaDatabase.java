package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaDatabase {

	protected Meta meta;

	public Map<String,MetaDatabaseSchema> mapSchema = new HashMap<String,MetaDatabaseSchema>();
	public Map<String,MetaDatabaseDatagroup> mapDatagroup = new HashMap<String,MetaDatabaseDatagroup>();
	boolean loaded = false;
	
	public String ALIGNEDMAPPING;
	
	public MetaDatabase( Meta meta ) {
		this.meta = meta;
	}

	public void createInitial( ActionBase action , ServerRegistry registry ) throws Exception {
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		if( !loadAdministration( action , root ) )
			return;
		
		loadSchemaSet( action , root );
	}

	public boolean loadAdministration( ActionBase action , Node node ) throws Exception {
		Node administration = ConfReader.xmlGetFirstChild( node , "administration" );
		if( administration == null ) {
			action.debug( "database administration is missing, ignore database information." );
			return( false );
		}
		
		return( true );
	}

	public void loadSchemaSet( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "schema" );
		if( items == null )
			return;
		
		for( Node schemaNode : items ) {
			MetaDatabaseSchema item = new MetaDatabaseSchema( meta , this );
			item.load( action , schemaNode );
			mapSchema.put( item.SCHEMA , item );
		}
		
		items = ConfReader.xmlGetChildren( node , "datagroup" );
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
