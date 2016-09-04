package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDatabase extends PropertyController {

	protected Meta meta;

	public MetaDatabaseAdministration admin;
	public Map<String,MetaDatabaseSchema> mapSchema;
	public Map<String,MetaDatabaseDatagroup> mapDatagroup;
	
	public String ALIGNEDMAPPING;
	
	public MetaDatabase( Meta meta ) {
		super( "database" );
		
		this.meta = meta;
		meta.setDatabase( this );
		admin = new MetaDatabaseAdministration( meta , this );
		mapSchema = new HashMap<String,MetaDatabaseSchema>();
		mapDatagroup = new HashMap<String,MetaDatabaseDatagroup>();
	}

	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	public MetaDatabase copy( ActionBase action , Meta meta ) throws Exception {
		MetaDatabase r = new MetaDatabase( meta );
		r.initCopyStarted( this , meta.product.getProperties() );
		
		return( r );
	}
	
	public void create( ActionBase action , ServerRegistry registry ) throws Exception {
		if( !initCreateStarted( meta.product.getProperties() ) )
			return;

		initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( !initCreateStarted( meta.product.getProperties() ) )
			return;

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

	public void saveAdministration( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlCreateElement( doc , root , "administration" );
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

	public void saveSchemaSet( ActionBase action , Document doc , Element root ) throws Exception {
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

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;

		properties.saveAsElements( doc , root );
		saveAdministration( action , doc , root );
		saveSchemaSet( action , doc , root );
	}
	
}
