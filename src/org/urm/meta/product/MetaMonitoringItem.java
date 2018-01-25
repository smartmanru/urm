package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoringItem {

	public Meta meta;
	public MetaMonitoringTarget target;
	
	public int ID;
	public DBEnumMonItemType MONITEM_TYPE;
	public String URL;
	public String WSDATA;
	public String WSCHECK;
	public int PV;

	public MetaMonitoringItem( Meta meta , MetaMonitoringTarget target ) {
		this.meta = meta; 
		this.target = target;
	}

	public MetaMonitoringItem copy( Meta rmeta , MetaMonitoringTarget rtarget ) {
		MetaMonitoringItem r = new MetaMonitoringItem( rmeta , rtarget );
		r.ID = ID;
		r.MONITEM_TYPE = MONITEM_TYPE;
		r.URL = URL;
		r.WSDATA = WSDATA;
		r.WSCHECK = WSCHECK;
		r.PV = PV;
		return( r );
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

	public void loadUrl( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( node , "url" );
		WSDATA = "";
		WSCHECK = "";
	}

	public void loadWS( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( node , "url" );
		WSDATA = getNodeSubTree( action , node , "wsdata" );
		WSCHECK = getNodeSubTree( action , node , "wscheck" );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( MONITEM_TYPE == DBEnumMonItemType.CHECKURL )
			Common.xmlSetElementAttr( doc , root , "url" , URL );
		else
		if( MONITEM_TYPE == DBEnumMonItemType.CHECKWS ) {
			Common.xmlSetElementAttr( doc , root , "wsdata" , WSDATA );
			Common.xmlSetElementAttr( doc , root , "wscheck" , WSCHECK );
		}
	}
	
}
