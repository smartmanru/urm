package org.urm.engine.meta;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class MetaMonitoringTarget {

	protected Meta meta;
	MetaMonitoring monitoring;
	
	public String NAME;
	
	public String HOME;
	public String PRODUCT;
	public String ENVFILE;
	public String DC;
	public long MAXTIME;
	public String ENV;

	private List<MetaMonitoringItem> listUrls;
	private List<MetaMonitoringItem> listWS;

	public MetaMonitoringTarget( Meta meta , MetaMonitoring monitoring ) {
		this.meta = meta;
		this.monitoring = monitoring;
	}

	public List<MetaMonitoringItem> getUrlsList( ActionBase action ) throws Exception {
		return( listUrls );
	}
	
	public List<MetaMonitoringItem> getWSList( ActionBase action ) throws Exception {
		return( listWS );
	}
	
	public void loadEnv( ActionBase action , Node node ) throws Exception {
		HOME = ConfReader.getRequiredAttrValue( node , "home" );
		PRODUCT = ConfReader.getRequiredAttrValue( node , "product" );
		ENVFILE = ConfReader.getRequiredAttrValue( node , "env" );
		DC = ConfReader.getRequiredAttrValue( node , "dc" );
		MAXTIME = ConfReader.getIntegerAttrValue( node , "maxtime" , 300000 );
		
		String basename = Common.getBaseName( ENVFILE );
		ENV = Common.cutExtension( basename );
		NAME = PRODUCT + "::" + ENV;
		
		loadCheckUrls( action , node );
		loadCheckWS( action , node );
	}

	private void loadCheckUrls( ActionBase action , Node node ) throws Exception {
		listUrls = new LinkedList<MetaMonitoringItem>();
		
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
		listWS = new LinkedList<MetaMonitoringItem>();
		
		Node[] items = ConfReader.xmlGetChildren( node , "checkws" );
		if( items == null )
			return;
		
		for( Node checkNode : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( meta , this );
			item.loadUrl( action , checkNode );
			listWS.add( item );
		}
	}

}
