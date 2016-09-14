package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class MetaDistrComponentWS {

	protected Meta meta;
	MetaDistrComponent comp;
	
	public String URL;
	boolean OBSOLETE;
	
	public MetaDistrComponentWS( Meta meta , MetaDistrComponent comp ) {
		this.meta = meta;
		this.comp = comp;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		URL = ConfReader.getRequiredAttrValue( node , "url" );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
	}
}