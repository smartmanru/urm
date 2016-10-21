package org.urm.meta.product;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoringTarget {

	public Meta meta;
	MetaMonitoring monitoring;
	
	public String NAME;
	
	public String HOME;
	public String ENV;
	public String DC;
	public long MAXTIME;

	private List<MetaMonitoringItem> listUrls;
	private List<MetaMonitoringItem> listWS;

	public MetaMonitoringTarget( Meta meta , MetaMonitoring monitoring ) {
		this.meta = meta;
		this.monitoring = monitoring;
		listUrls = new LinkedList<MetaMonitoringItem>();
		listWS = new LinkedList<MetaMonitoringItem>();
	}

	public List<MetaMonitoringItem> getUrlsList( ActionBase action ) throws Exception {
		return( listUrls );
	}
	
	public List<MetaMonitoringItem> getWSList( ActionBase action ) throws Exception {
		return( listWS );
	}
	
	public MetaMonitoringTarget copy( ActionBase action , Meta meta , MetaMonitoring monitoring ) {
		MetaMonitoringTarget r = new MetaMonitoringTarget( meta , monitoring );
		r.NAME = NAME;
		r.HOME = HOME;
		r.ENV = ENV;
		r.DC = DC;
		r.MAXTIME = MAXTIME;
		
		r.listUrls.addAll( listUrls );
		r.listWS.addAll( listWS );
		return( r );
	}
	
	public void loadEnv( ActionBase action , Node node ) throws Exception {
		HOME = ConfReader.getRequiredAttrValue( node , "home" );
		ENV = ConfReader.getRequiredAttrValue( node , "env" );
		DC = ConfReader.getRequiredAttrValue( node , "dc" );
		MAXTIME = ConfReader.getIntegerAttrValue( node , "maxtime" , 300000 );
		
		NAME = meta.name + "::" + ENV;
		
		loadCheckUrls( action , node );
		loadCheckWS( action , node );
	}

	private void loadCheckUrls( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "checkurl" );
		if( items == null )
			return;
		
		for( Node checkNode : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( meta , this );
			item.loadUrl( action , checkNode );
			listUrls.add( item );
		}
	}

	private void loadCheckWS( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "checkws" );
		if( items == null )
			return;
		
		for( Node checkNode : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( meta , this );
			item.loadUrl( action , checkNode );
			listWS.add( item );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "home" , HOME );
		Common.xmlSetElementAttr( doc , root , "env" , ENV );
		Common.xmlSetElementAttr( doc , root , "dc" , DC );
		Common.xmlSetElementAttr( doc , root , "maxtime" , "" + MAXTIME );
		
		for( MetaMonitoringItem item : listUrls ) {
			Element element = Common.xmlCreateElement( doc , root , "checkurl" );
			item.save( action , doc , element );
		}
		
		for( MetaMonitoringItem item : listWS ) {
			Element element = Common.xmlCreateElement( doc , root , "checkws" );
			item.save( action , doc , element );
		}
	}
	
}
