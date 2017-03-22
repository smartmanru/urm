package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseMaster {

	public Meta meta;
	public Release release;
	
	Map<String,ReleaseMasterHistory> mapHistory;
	Map<String,ReleaseMasterItem> mapItem;
	
	public ReleaseMaster( Meta meta , Release release ) {
		this.meta = meta;
		this.release = release;
		mapHistory = new HashMap<String,ReleaseMasterHistory>();
		mapItem = new HashMap<String,ReleaseMasterItem>();
	}

	public void create( ActionBase action ) throws Exception {
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( root == null )
			return;
		
		loadHistory( action , ConfReader.xmlGetFirstChild( root , "history" ) );
		loadItems( action , ConfReader.xmlGetFirstChild( root , "files" ) );
	}

	public void loadHistory( ActionBase action , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , "release" );
		if( items == null )
			return;
		
		for( Node node : items ) {
			ReleaseMasterHistory rh = new ReleaseMasterHistory( meta , this );
			rh.load( action , node );
			mapHistory.put( rh.RELEASE , rh );
		}
	}
	
	public void loadItems( ActionBase action , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , "distitem" );
		if( items == null )
			return;
		
		for( Node node : items ) {
			ReleaseMasterItem item = new ReleaseMasterItem( meta , this );
			item.load( action , node );
			mapItem.put( item.KEY , item );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element history = Common.xmlCreateElement( doc , root , "history" );
		for( ReleaseMasterHistory release : mapHistory.values() ) {
			Element node = Common.xmlCreateElement( doc , history , "release" );
			release.save( action , doc , node );
		}
		
		Element files = Common.xmlCreateElement( doc , root , "files" );
		for( ReleaseMasterItem item : mapItem.values() ) {
			Element node = Common.xmlCreateElement( doc , files , "distitem" );
			item.save( action , doc , node );
		}
	}

	public void addMasterItem( ActionBase action , MetaDistrBinaryItem distItem , DistItemInfo info ) throws Exception {
		ReleaseMasterItem item = mapItem.get( distItem.KEY );
		if( item == null ) {
			item = new ReleaseMasterItem( meta , this );
			mapItem.put( distItem.KEY , item );
		}
		
		item.setItem( action , release , distItem , info );
	}

	public ReleaseMasterItem findMasterItem( MetaDistrBinaryItem distItem ) {
		return( mapItem.get( distItem.KEY ) );
	}
	
}
