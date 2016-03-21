package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.action.ActionBase;

public class MetaMonitoringItem {

	MetaMonitoringTarget target;
	
	public String NAME;

	public boolean monitorUrl = false;
	public boolean monitorWS = false;
	
	public String URL;
	public String WSDATA;
	public String WSCHECK;
	
	public MetaMonitoringItem( MetaMonitoringTarget target ) {
		this.target = target;
	}

	public void loadUrl( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( action , node , "url" );
		NAME = URL;
		monitorUrl = true;
	}

	private String getNodeSubTree( ActionBase action , Node node , String name ) throws Exception {
		Node parent = ConfReader.xmlGetFirstChild( action , node , name );
		if( parent == null )
			return( null );
		
		Node content = parent.getFirstChild();
		if( content == null )
			return( null );
		
		return( ConfReader.getNodeSubTree( action , content ) );
	}
	
	public void loadWS( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( action , node , "url" );
		WSDATA = getNodeSubTree( action , node , "wsdata" );
		WSCHECK = getNodeSubTree( action , node , "wscheck" );
		NAME = URL;
		monitorWS = true;
	}
	
}
