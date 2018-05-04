package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.loader.MatchItem;

public class MetaDatabase {

	public Meta meta;

	public MetaDatabaseAdministration admin;
	public Map<String,MetaDatabaseSchema> mapSchema;
	public Map<Integer,MetaDatabaseSchema> mapSchemaById;
	
	public String ALIGNEDMAPPING;
	
	public MetaDatabase( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setDatabase( this );
		admin = new MetaDatabaseAdministration( meta , this );
		mapSchema = new HashMap<String,MetaDatabaseSchema>();
		mapSchemaById = new HashMap<Integer,MetaDatabaseSchema>();
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

	public MetaDatabaseSchema findSchema( int id ) {
		return( mapSchemaById.get( id ) );
	}

	public MetaDatabaseSchema findSchema( MatchItem item ) {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( mapSchemaById.get( item.FKID ) );
		return( mapSchema.get( item.FKNAME ) );
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

	public MetaDatabaseSchema getSchema( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getSchema( item.FKID ) );
		return( getSchema( item.FKNAME ) );
	}
	
	public String getSchemaName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		MetaDatabaseSchema schema = getSchema( item.FKID );
		return( schema.NAME );
	}
	
	public boolean checkAligned( String id ) {
		return( true );
	}

	public void removeSchema( MetaDatabaseSchema schema ) throws Exception {
		mapSchema.remove( schema.NAME );
		mapSchemaById.remove( schema.ID );
	}

	public MatchItem getSchemaMatchItem( Integer id , String name ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		MetaDatabaseSchema schema = ( id == null )? findSchema( name ) : getSchema( id );
		MatchItem match = ( schema == null )? new MatchItem( name ) : new MatchItem( schema.ID );
		return( match );
	}

	public boolean matchSchema( MatchItem item ) throws Exception {
		if( item == null )
			return( true );
		
		MetaDatabaseSchema schema = null;
		if( item.MATCHED ) {
			schema = getSchema( item.FKID );
			return( true );
		}
		
		schema = findSchema( item.FKNAME );
		if( schema != null ) {
			item.match( schema.ID );
			return( true );
		}
		return( false );
	}
	
}
