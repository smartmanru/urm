package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.ProductMeta;

public class MetaDocs {

	public Meta meta;
	
	private Map<String,MetaProductDoc> mapDocs;
	
	public MetaDocs( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setDocs( this );
		mapDocs = new HashMap<String,MetaProductDoc>();
	}
	
	public MetaDocs copy( Meta meta ) throws Exception {
		MetaDocs r = new MetaDocs( meta.getStorage() , meta );
		
		for( MetaProductDoc doc : mapDocs.values() ) {
			MetaProductDoc rdoc = doc.copy( meta , r );
			r.mapDocs.put( rdoc.NAME , rdoc );
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

	public void addDoc( MetaProductDoc doc ) {
		mapDocs.put( doc.NAME , doc );
	}
	
	public void updateDoc( MetaProductDoc doc ) throws Exception {
		Common.changeMapKey( mapDocs , doc , doc.NAME );
	}
	
	public void removeDoc( MetaProductDoc doc ) {
		mapDocs.remove( doc.NAME );
	}
	
}
