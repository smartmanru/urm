package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.ProductMeta;

public class MetaDocs {

	public Meta meta;
	
	private Map<String,MetaProductDoc> mapDocs;
	private Map<Integer,MetaProductDoc> mapDocsById;
	private Map<String,MetaDesignDiagram> diagrams;
	
	public MetaDocs( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setDocs( this );
		
		mapDocs = new HashMap<String,MetaProductDoc>();
		mapDocsById = new HashMap<Integer,MetaProductDoc>();
		diagrams = new HashMap<String,MetaDesignDiagram>();
	}
	
	public MetaDocs copy( Meta meta ) throws Exception {
		MetaDocs r = new MetaDocs( meta.getStorage() , meta );
		
		for( MetaProductDoc doc : mapDocs.values() ) {
			MetaProductDoc rdoc = doc.copy( meta , r );
			r.addDoc( rdoc );
		}
		
		for( String name : diagrams.keySet() ) {
			MetaDesignDiagram design = diagrams.get( name );
			MetaDesignDiagram rd = design.copy( r.meta );
			r.diagrams.put( name , rd );
		}
		
		return( r );
	}
	
	public boolean isEmpty() {
		return( mapDocs.isEmpty() );
	}
	
	public String[] getDocNames() {
		return( Common.getSortedKeys( mapDocs ) );
	}

	public MetaProductDoc[] getDocList() {
		return( mapDocs.values().toArray( new MetaProductDoc[0] ) );
	}

	public MetaProductDoc findDoc( String name ) {
		return( mapDocs.get( name ) );
	}
	
	public MetaProductDoc getDoc( String name ) throws Exception {
		MetaProductDoc doc = mapDocs.get( name );
		if( doc == null )
			Common.exit1( _Error.UnknownDoc1 , "unknown doc=" + name , name );
		return( doc );
	}

	public MetaProductDoc getDoc( int id ) throws Exception {
		MetaProductDoc doc = mapDocsById.get( id );
		if( doc == null )
			Common.exit1( _Error.UnknownDoc1 , "unknown doc=" + id , "" + id );
		return( doc );
	}

	public void addDoc( MetaProductDoc doc ) {
		mapDocs.put( doc.NAME , doc );
		mapDocsById.put( doc.ID , doc );
	}
	
	public void updateDoc( MetaProductDoc doc ) throws Exception {
		Common.changeMapKey( mapDocs , doc , doc.NAME );
	}
	
	public void removeDoc( MetaProductDoc doc ) {
		mapDocs.remove( doc.NAME );
		mapDocsById.remove( doc.ID );
	}
	
	public void addDiagram( MetaDesignDiagram diagram ) throws Exception {
		diagrams.put( diagram.NAME , diagram );
	}

	public String[] getDiagramNames() {
		return( Common.getSortedKeys( diagrams ) );
	}
	
	public MetaDesignDiagram findDiagram( String diagramName ) {
		for( MetaDesignDiagram diagram : diagrams.values() ) {
			if( diagram.NAME.equals( diagramName ) )
				return( diagram );
		}
		return( null );
	}

}
