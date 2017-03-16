package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.engine.ServerTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.ServerProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDatabase extends PropertyController {

	protected Meta meta;

	public MetaDatabaseAdministration admin;
	public Map<String,MetaDatabaseSchema> mapSchema;
	
	public String ALIGNEDMAPPING;
	
	public MetaDatabase( ServerProductMeta storage , Meta meta ) {
		super( storage , "database" );
		
		this.meta = meta;
		meta.setDatabase( this );
		admin = new MetaDatabaseAdministration( meta , this );
		mapSchema = new HashMap<String,MetaDatabaseSchema>();
	}

	@Override
	public String getName() {
		return( "meta-database" );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
	}
	
	public MetaDatabase copy( ActionBase action , Meta meta ) throws Exception {
		MetaDatabase r = new MetaDatabase( meta.getStorage( action ) , meta );
		MetaProductSettings product = meta.getProductSettings( action );
		r.initCopyStarted( this , product.getProperties() );
		
		r.admin = admin.copy( action , meta , r );
		for( MetaDatabaseSchema schema : mapSchema.values() ) {
			MetaDatabaseSchema rschema = schema.copy( action , meta , r );
			r.mapSchema.put( rschema.SCHEMA , rschema );
		}
		r.initFinished();
		return( r );
	}
	
	public void createDatabase( TransactionBase transaction ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( transaction.action );
		if( !initCreateStarted( product.getProperties() ) )
			return;

		initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		if( !initCreateStarted( product.getProperties() ) )
			return;

		if( !loadAdministration( action , root ) )
			return;
		
		loadSchemaSet( action , root );
		initFinished();
	}

	private boolean loadAdministration( ActionBase action , Node node ) throws Exception {
		Node administration = ConfReader.xmlGetFirstChild( node , "administration" );
		if( administration == null ) {
			action.debug( "database administration is missing, ignore database information." );
			return( false );
		}
		
		return( true );
	}

	private void saveAdministration( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlCreateElement( doc , root , "administration" );
	}
	
	private void loadSchemaSet( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "schema" );
		if( items == null )
			return;
		
		for( Node schemaNode : items ) {
			MetaDatabaseSchema item = new MetaDatabaseSchema( meta , this );
			item.load( action , schemaNode );
			mapSchema.put( item.SCHEMA , item );
		}
	}

	private void saveSchemaSet( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaDatabaseSchema schema : mapSchema.values() ) {
			Element schemaElement = Common.xmlCreateElement( doc , root , "schema" );
			schema.save( action , doc , schemaElement );
		}
	}

	public String[] getSchemaSet() {
		return( Common.getSortedKeys( mapSchema ) );
	}
	
	public MetaDatabaseSchema getSchema( ActionBase action , String name ) throws Exception {
		MetaDatabaseSchema schema = mapSchema.get( name );
		if( schema == null )
			action.exit1( _Error.UnknownSchema1 , "unknown schema=" + name , name );
		return( schema );
	}

	public boolean checkAligned( ActionBase action , String id ) throws Exception {
		return( true );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveAsElements( doc , root , false );
		saveAdministration( action , doc , root );
		saveSchemaSet( action , doc , root );
	}

	public void createDatabaseSchema( ServerTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		mapSchema.put( schema.SCHEMA , schema );
	}
	
	public void modifyDatabaseSchema( ServerTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
	}
	
	public void deleteDatabaseSchema( ServerTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		meta.deleteDatabaseSchemaFromEnvironments( transaction , schema );
		MetaDistr distr = schema.meta.getDistr( transaction.getAction() );
		distr.deleteDatabaseSchema( transaction , schema );
		mapSchema.remove( schema.SCHEMA );
	}
	
}
