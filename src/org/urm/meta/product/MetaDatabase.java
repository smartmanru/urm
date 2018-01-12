package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDatabase {

	public Meta meta;

	public MetaDatabaseAdministration admin;
	public Map<String,MetaDatabaseSchema> mapSchema;
	public Map<String,MetaDump> mapExport;
	public Map<String,MetaDump> mapImport;
	
	public String ALIGNEDMAPPING;
	
	public MetaDatabase( ProductMeta storage , MetaProductSettings settings , Meta meta ) {
		this.meta = meta;
		meta.setDatabase( this );
		admin = new MetaDatabaseAdministration( meta , this );
		mapSchema = new HashMap<String,MetaDatabaseSchema>();
		mapExport = new HashMap<String,MetaDump>();
		mapImport = new HashMap<String,MetaDump>();
	}

	public MetaDatabase copy( ActionBase action , Meta rmeta ) throws Exception {
		MetaProductSettings product = rmeta.getProductSettings();
		MetaDatabase r = new MetaDatabase( rmeta.getStorage() , product , rmeta );
		
		r.admin = admin.copy( action , rmeta , r );
		for( MetaDatabaseSchema schema : mapSchema.values() ) {
			MetaDatabaseSchema rschema = schema.copy( action , rmeta , r );
			r.mapSchema.put( rschema.SCHEMA , rschema );
		}
		
		for( MetaDump dump : mapExport.values() ) {
			MetaDump rdump = dump.copy( action , rmeta , r );
			r.mapExport.put( rdump.NAME , rdump );
		}
		for( MetaDump dump : mapImport.values() ) {
			MetaDump rdump = dump.copy( action , rmeta , r );
			r.mapImport.put( rdump.NAME , rdump );
		}
		
		return( r );
	}
	
	public void createDatabase( TransactionBase transaction ) throws Exception {
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( !loadAdministration( action , root ) )
			return;
		
		loadSchemaSet( action , root );
		Node dumps = ConfReader.xmlGetFirstChild( root , "dumps" );
		if( dumps != null )
			loadDumpSet( action , dumps );
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

	private void loadDumpSet( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "dump" );
		if( items == null )
			return;
		
		for( Node exportNode : items ) {
			MetaDump dump = new MetaDump( meta , this );
			dump.load( action , exportNode );
			if( dump.EXPORT )
				mapExport.put( dump.NAME , dump );
			else
				mapImport.put( dump.NAME , dump );
		}
	}

	private void saveSchemaSet( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaDatabaseSchema schema : mapSchema.values() ) {
			Element schemaElement = Common.xmlCreateElement( doc , root , "schema" );
			schema.save( action , doc , schemaElement );
		}
	}

	private void saveDumpSet( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaDump dump : mapExport.values() ) {
			Element dumpElement = Common.xmlCreateElement( doc , root , "dump" );
			dump.save( action , doc , dumpElement );
		}
		for( MetaDump dump : mapImport.values() ) {
			Element dumpElement = Common.xmlCreateElement( doc , root , "dump" );
			dump.save( action , doc , dumpElement );
		}
	}

	public boolean isEmpty() {
		return( mapSchema.isEmpty() );
	}
	
	public String[] getSchemaNames() {
		return( Common.getSortedKeys( mapSchema ) );
	}

	public MetaDatabaseSchema[] getSchemaList() {
		return( mapSchema.values().toArray( new MetaDatabaseSchema[0] ) );
	}

	public MetaDatabaseSchema findSchema( String name ) {
		return( mapSchema.get( name ) );
	}

	public String[] getExportDumpNames() {
		return( Common.getSortedKeys( mapExport ) );
	}

	public String[] getImportDumpNames() {
		return( Common.getSortedKeys( mapImport ) );
	}

	public MetaDump findExportDump( String name ) {
		return( mapExport.get( name ) );
	}
	
	public MetaDump findImportDump( String name ) {
		return( mapImport.get( name ) );
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
		saveAdministration( action , doc , root );
		saveSchemaSet( action , doc , root );
		
		Element dumpElement = Common.xmlCreateElement( doc , root , "dumps" );
		saveDumpSet( action , doc , dumpElement );
	}

	public void createDatabaseSchema( EngineTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		mapSchema.put( schema.SCHEMA , schema );
	}
	
	public void modifyDatabaseSchema( EngineTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
	}
	
	public void deleteDatabaseSchema( EngineTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		meta.deleteDatabaseSchemaFromEnvironments( transaction , schema );
		MetaDistr distr = schema.meta.getDistr();
		distr.deleteDatabaseSchema( transaction , schema );
		mapSchema.remove( schema.SCHEMA );
	}

	public void createDump( EngineTransaction transaction , MetaDump dump ) throws Exception {
		if( dump.EXPORT )
			mapExport.put( dump.NAME , dump );
		else
			mapImport.put( dump.NAME , dump );
	}
	
	public void updateDump( MetaDump dump ) throws Exception {
		if( dump.EXPORT )
			Common.changeMapKey( mapExport , dump , dump.NAME );
		else
			Common.changeMapKey( mapImport , dump , dump.NAME );
	}
	
	public void deleteDump( EngineTransaction transaction , MetaDump dump ) throws Exception {
		if( dump.EXPORT )
			mapExport.remove( dump.NAME );
		else
			mapImport.remove( dump.NAME );
	}
	
}
