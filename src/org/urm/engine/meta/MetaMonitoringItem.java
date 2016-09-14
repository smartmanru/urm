package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class MetaMonitoringItem {

	protected Meta meta;
	MetaMonitoringTarget target;
	
	public String NAME;

	public boolean monitorUrl = false;
	public boolean monitorWS = false;
	
	public String URL;
	public String WSDATA;
	public String WSCHECK;
	
	public MetaMonitoringItem( Meta meta , MetaMonitoringTarget target ) {
		this.meta = meta; 
		this.target = target;
	}

	public void loadUrl( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( node , "url" );
		NAME = URL;
		monitorUrl = true;
	}

	private String getNodeSubTree( ActionBase action , Node node , String name ) throws Exception {
		Node parent = ConfReader.xmlGetFirstChild( node , name );
		if( parent == null )
			return( null );
		
		Node content = parent.getFirstChild();
		if( content == null )
			return( null );
		
		return( ConfReader.getNodeSubTree( content ) );
	}
	
	public void loadWS( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( node , "url" );
		WSDATA = getNodeSubTree( action , node , "wsdata" );
		WSCHECK = getNodeSubTree( action , node , "wscheck" );
		NAME = URL;
		monitorWS = true;
	}
	
}