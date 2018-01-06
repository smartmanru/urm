package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDistrComponentWS {

	public Meta meta;
	public MetaDistrComponent comp;
	
	public String NAME;
	public String URL;
	boolean OBSOLETE;
	
	public MetaDistrComponentWS( Meta meta , MetaDistrComponent comp ) {
		this.meta = meta;
		this.comp = comp;
	}

	public void createComponentService( EngineTransaction transaction ) throws Exception {
		NAME = "";
		URL = "";
		OBSOLETE = false;
	}
	
	public void setServiceData( EngineTransaction transaction , String NAME , String URL ) throws Exception {
		this.NAME = NAME;
		this.URL = URL;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = ConfReader.getRequiredAttrValue( node , "name" );
		URL = ConfReader.getRequiredAttrValue( node , "url" );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
	}
	
	public MetaDistrComponentWS copy( ActionBase action , Meta meta , MetaDistrComponent comp ) throws Exception {
		MetaDistrComponentWS r = new MetaDistrComponentWS( meta , comp );
		r.NAME = NAME;
		r.URL = URL;
		r.OBSOLETE = OBSOLETE;
		return( r );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "url" , URL );
		Common.xmlSetElementAttr( doc , root , "obsolete" , Common.getBooleanValue( OBSOLETE ) );
	}

	public String getURL( String ACCESSPOINT ) {
		return( ACCESSPOINT + "/" + URL + "?wsdl" );
	}
	
}
