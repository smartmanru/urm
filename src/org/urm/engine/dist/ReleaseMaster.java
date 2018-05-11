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
	
	public ReleaseMaster copy( ActionBase action , Release rr ) throws Exception {
		ReleaseMaster rMaster = new ReleaseMaster( rr.meta , rr );
		
		for( ReleaseMasterHistory hItem : mapHistory.values() ) {
			ReleaseMasterHistory rhItem = hItem.copy( action , rMaster );
			rMaster.addHistoryItem( rhItem );
		}
		
		for( ReleaseMasterItem item : mapItem.values() ) {
			ReleaseMasterItem ritem = item.copy( action , rMaster );
			rMaster.addMasterItem( ritem );
		}
		return( rMaster );
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( root == null )
			return;
		
		loadHistory( action , ConfReader.xmlGetFirstChild( root , Release.ELEMENT_HISTORY ) );
		loadItems( action , ConfReader.xmlGetFirstChild( root , Release.ELEMENT_FILES ) );
	}

	public void loadHistory( ActionBase action , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , Release.ELEMENT_RELEASE );
		if( items == null )
			return;
		
		for( Node node : items ) {
			ReleaseMasterHistory rh = new ReleaseMasterHistory( meta , this );
			rh.load( action , node );
			addHistoryItem( rh );
		}
	}
	
	private void addHistoryItem( ReleaseMasterHistory rh ) {
		mapHistory.put( rh.RELEASE , rh );
	}
	
	public void loadItems( ActionBase action , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , Release.ELEMENT_DISTITEM );
		if( items == null )
			return;
		
		for( Node node : items ) {
			ReleaseMasterItem item = new ReleaseMasterItem( meta , this );
			item.load( action , node );
			addMasterItem( item );
		}
	}
	
	private void addMasterItem( ReleaseMasterItem item ) {
		mapItem.put( item.KEY , item );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element history = Common.xmlCreateElement( doc , root , Release.ELEMENT_HISTORY );
		for( ReleaseMasterHistory release : mapHistory.values() ) {
			Element node = Common.xmlCreateElement( doc , history , Release.ELEMENT_RELEASE );
			release.save( action , doc , node );
		}
		
		Element files = Common.xmlCreateElement( doc , root , Release.ELEMENT_FILES );
		for( ReleaseMasterItem item : mapItem.values() ) {
			Element node = Common.xmlCreateElement( doc , files , Release.ELEMENT_DISTITEM );
			item.save( action , doc , node );
		}
	}

	public void addMasterItem( ActionBase action , Release src , MetaDistrBinaryItem distItem , DistItemInfo info ) throws Exception {
		ReleaseMasterItem item = mapItem.get( distItem.NAME );
		if( item == null ) {
			item = new ReleaseMasterItem( meta , this );
			mapItem.put( distItem.NAME , item );
		}
		
		if( src != null )
			item.setRelease( action , src , distItem , info );
		else
			item.setManual( action , distItem , info );
	}

	public void removeMasterItem( String key ) {
		mapItem.remove( key );
	}
	
	public ReleaseMasterItem[] getMasterItems() {
		return( mapItem.values().toArray( new ReleaseMasterItem[0] ) );
	}
	
	public ReleaseMasterItem findMasterItem( MetaDistrBinaryItem distItem ) {
		return( mapItem.get( distItem.NAME ) );
	}
	
	public void addMasterHistory( ActionBase action , String RELEASEVER ) throws Exception {
		ReleaseMasterHistory mh = new ReleaseMasterHistory( meta , this );
		mh.create( action , RELEASEVER );
		mapHistory.put( mh.RELEASE , mh );
	}
	
}
