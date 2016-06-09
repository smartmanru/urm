package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaDistrComponentWS {

	Metadata meta;
	MetaDistrComponent comp;
	
	public String URL;
	boolean OBSOLETE;
	
	public MetaDistrComponentWS( Metadata meta , MetaDistrComponent comp ) {
		this.meta = meta;
		this.comp = comp;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( node , "url" );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
	}
}
