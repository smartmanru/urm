package ru.egov.urm.meta;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;

public class MetaMonitoringTarget {

	Metadata meta;
	public String NAME;
	
	public String HOME;
	public String PRODUCT;
	public String ENVFILE;
	public String DC;
	public long MAXTIME;
	public String ENV;

	private List<MetaMonitoringItem> listUrls;
	private List<MetaMonitoringItem> listWS;

	public MetaMonitoringTarget( Metadata meta ) {
		this.meta = meta;
	}

	public List<MetaMonitoringItem> getUrlsList( ActionBase action ) throws Exception {
		return( listUrls );
	}
	
	public List<MetaMonitoringItem> getWSList( ActionBase action ) throws Exception {
		return( listWS );
	}
	
	public void loadEnv( ActionBase action , Node node ) throws Exception {
		HOME = ConfReader.getRequiredAttrValue( action , node , "home" );
		PRODUCT = ConfReader.getRequiredAttrValue( action , node , "product" );
		ENVFILE = ConfReader.getRequiredAttrValue( action , node , "env" );
		DC = ConfReader.getRequiredAttrValue( action , node , "dc" );
		MAXTIME = ConfReader.getIntegerAttrValue( action , node , "maxtime" , 300000 );
		
		String basename = Common.getBaseName( ENVFILE );
		ENV = Common.cutExtension( basename );
		NAME = PRODUCT + "::" + ENV;
		
		loadCheckUrls( action , node );
		loadCheckWS( action , node );
	}

	private void loadCheckUrls( ActionBase action , Node node ) throws Exception {
		listUrls = new LinkedList<MetaMonitoringItem>();
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "checkurl" );
		if( items == null )
			return;
		
		for( Node checkNode : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( this );
			item.loadUrl( action , checkNode );
			listUrls.add( item );
		}
	}

	private void loadCheckWS( ActionBase action , Node node ) throws Exception {
		listWS = new LinkedList<MetaMonitoringItem>();
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "checkws" );
		if( items == null )
			return;
		
		for( Node checkNode : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( this );
			item.loadUrl( action , checkNode );
			listWS.add( item );
		}
	}

}
