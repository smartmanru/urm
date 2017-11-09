package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ProductMonitoringItem {

	ProductMonitoringTarget target;
	
	public String NAME;

	public boolean monitorUrl = false;
	public boolean monitorWS = false;
	
	public String URL;
	public String WSDATA;
	public String WSCHECK;

	public boolean monitorStatus = false;
	
	public ProductMonitoringItem( ProductMonitoringTarget target ) {
		this.target = target;
	}

	public void loadUrl( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( node , "url" );
		NAME = URL;
		monitorUrl = true;
	}

	public void setUrlItem( ActionBase action , String URL ) throws Exception {
		monitorUrl = true;
		this.URL = URL;
		this.NAME = URL;
	}
	
	public void setMonitorStatus( boolean status ) {
		this.monitorStatus = status;
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

	public ProductMonitoringItem copy( ActionBase action , ProductMonitoringTarget target ) {
		ProductMonitoringItem r = new ProductMonitoringItem( target );
		r.URL = URL;
		r.WSDATA = WSDATA;
		r.WSCHECK = WSCHECK;
		r.NAME = NAME;
		r.monitorWS = monitorWS;
		return( r );
	}		
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( monitorUrl )
			Common.xmlSetElementAttr( doc , root , "url" , URL );
		else {
			Common.xmlSetElementAttr( doc , root , "wsdata" , WSDATA );
			Common.xmlSetElementAttr( doc , root , "wscheck" , WSCHECK );
		}
	}
	
}
