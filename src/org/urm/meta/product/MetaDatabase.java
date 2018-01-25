package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.ProductMeta;

public class MetaDatabase {

	public Meta meta;

	public MetaDatabaseAdministration admin;
	public Map<String,MetaDatabaseSchema> mapSchema;
	public Map<Integer,MetaDatabaseSchema> mapSchemaById;
	public Map<String,MetaDump> mapExport;
	public Map<String,MetaDump> mapImport;
	
	public String ALIGNEDMAPPING;
	
	public MetaDatabase( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setDatabase( this );
		admin = new MetaDatabaseAdministration( meta , this );
		mapSchema = new HashMap<String,MetaDatabaseSchema>();
		mapSchemaById = new HashMap<Integer,MetaDatabaseSchema>();
		mapExport = new HashMap<String,MetaDump>();
		mapImport = new HashMap<String,MetaDump>();
	}

	public MetaDatabase copy( Meta rmeta ) throws Exception {
		MetaDatabase r = new MetaDatabase( rmeta.getStorage() , rmeta );
		
		r.admin = admin.copy( rmeta , r );
		for( MetaDatabaseSchema schema : mapSchema.values() ) {
			MetaDatabaseSchema rschema = schema.copy( rmeta , r );
			r.addSchema( rschema );
		}
		
		return( r );
	}
	
	public void addSchema( MetaDatabaseSchema schema ) {
		mapSchema.put( schema.NAME , schema );
		mapSchemaById.put( schema.ID , schema );
	}
	
	public void updateSchema( MetaDatabaseSchema schema ) throws Exception {
		Common.changeMapKey( mapSchema , schema , schema.NAME );
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
	
	public MetaDatabaseSchema getSchema( String name ) throws Exception {
		MetaDatabaseSchema schema = mapSchema.get( name );
		if( schema == null )
			Common.exit1( _Error.UnknownSchema1 , "unknown schema=" + name , name );
		return( schema );
	}

	public MetaDatabaseSchema getSchema( int id ) throws Exception {
		MetaDatabaseSchema schema = mapSchemaById.get( id );
		if( schema == null )
			Common.exit1( _Error.UnknownSchema1 , "unknown schema=" + id , "" + id );
		return( schema );
	}

	public boolean checkAligned( String id ) {
		return( true );
	}

	public void removeSchema( MetaDatabaseSchema schema ) throws Exception {
		mapSchema.remove( schema.NAME );
		mapSchemaById.remove( schema.ID );
	}

	public void addDump( MetaDump dump ) {
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
	
	public void removeDump( MetaDump dump ) {
		if( dump.EXPORT )
			mapExport.remove( dump.NAME );
		else
			mapImport.remove( dump.NAME );
	}
	
}
