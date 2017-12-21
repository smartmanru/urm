package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.PropertyController;
import org.urm.meta.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDocs extends PropertyController {

	protected Meta meta;
	
	public Map<String,MetaProductDoc> mapDocs;
	
	public MetaDocs( ProductMeta storage , MetaProductSettings settings , Meta meta ) {
		super( storage , settings , "docs" );
		
		this.meta = meta;
		meta.setDocs( this );
		mapDocs = new HashMap<String,MetaProductDoc>();
	}
	
	@Override
	public String getName() {
		return( "meta-docs" );
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
	
	public MetaDocs copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		MetaDocs r = new MetaDocs( meta.getStorage() , product , meta );
		r.initCopyStarted( this , product.getProperties() );
		
		for( MetaProductDoc doc : mapDocs.values() ) {
			MetaProductDoc rdoc = doc.copy( action , meta , r );
			r.mapDocs.put( rdoc.NAME , rdoc );
		}
		r.initFinished();
		return( r );
	}
	
	public void createDocs( TransactionBase transaction ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( transaction.action );
		if( !initCreateStarted( product.getProperties() ) )
			return;

		initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		if( !initCreateStarted( product.getProperties() ) )
			return;

		if( root != null )
			loadDocSet( action , root );
		initFinished();
	}

	private void loadDocSet( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "document" );
		if( items == null )
			return;
		
		for( Node docNode : items ) {
			MetaProductDoc item = new MetaProductDoc( meta , this );
			item.load( action , docNode );
			mapDocs.put( item.NAME , item );
		}
	}

	private void saveDocSet( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaProductDoc pdoc : mapDocs.values() ) {
			Element docElement = Common.xmlCreateElement( doc , root , "document" );
			pdoc.save( action , doc , docElement );
		}
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
	
	public MetaProductDoc getDoc( ActionBase action , String name ) throws Exception {
		MetaProductDoc doc = mapDocs.get( name );
		if( doc == null )
			action.exit1( _Error.UnknownDoc1 , "unknown doc=" + name , name );
		return( doc );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveAsElements( doc , root , false );
		saveDocSet( action , doc , root );
	}

	public void createDoc( EngineTransaction transaction , MetaProductDoc doc ) throws Exception {
		mapDocs.put( doc.NAME , doc );
	}
	
	public void modifyDoc( EngineTransaction transaction , MetaProductDoc doc ) throws Exception {
	}
	
	public void deleteDoc( EngineTransaction transaction , MetaProductDoc doc ) throws Exception {
		MetaDistr distr = doc.meta.getDistr( transaction.getAction() );
		distr.deleteDocument( transaction , doc );
		mapDocs.remove( doc.NAME );
	}
	
}
