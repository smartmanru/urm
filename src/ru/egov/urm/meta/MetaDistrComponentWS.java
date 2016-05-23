package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.action.ActionBase;

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
		URL = ConfReader.getRequiredAttrValue( action , node , "url" );
		OBSOLETE = ConfReader.getBooleanAttrValue( action , node , "obsolete" , false );
	}
}
