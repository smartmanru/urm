package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaDistrComponentItem {

	protected Meta meta;
	MetaDistrComponent comp;
	
	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	public boolean OBSOLETE;
	public String DEPLOYNAME;

	public MetaDistrComponentItem( Meta meta , MetaDistrComponent comp ) {
		this.meta = meta;
		this.comp = comp;
	}

	public void loadBinary( ActionBase action , Node node ) throws Exception {
		String NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		binaryItem = comp.dist.getBinaryItem( action , NAME );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
		DEPLOYNAME = ConfReader.getAttrValue( node , "deployname" );
	}

	public void loadConf( ActionBase action , Node node ) throws Exception {
		String NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		confItem = comp.dist.getConfItem( action , NAME );
		OBSOLETE = ConfReader.getBooleanAttrValue( node , "obsolete" , false );
	}

}
