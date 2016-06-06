package org.urm.meta;

import org.urm.common.ConfReader;
import org.urm.meta.Metadata.VarNAMETYPE;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaDistrComponentItem {

	Metadata meta;
	MetaDistrComponent comp;
	
	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	public boolean OBSOLETE;
	public String DEPLOYNAME;

	public MetaDistrComponentItem( Metadata meta , MetaDistrComponent comp ) {
		this.meta = meta;
		this.comp = comp;
	}

	public void loadBinary( ActionBase action , Node node ) throws Exception {
		String NAME = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		binaryItem = meta.distr.getBinaryItem( action , NAME );
		OBSOLETE = ConfReader.getBooleanAttrValue( action , node , "obsolete" , false );
		DEPLOYNAME = ConfReader.getAttrValue( action , node , "deployname" );
	}

	public void loadConf( ActionBase action , Node node ) throws Exception {
		String NAME = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		confItem = meta.distr.getConfItem( action , NAME );
		OBSOLETE = ConfReader.getBooleanAttrValue( action , node , "obsolete" , false );
	}

}
