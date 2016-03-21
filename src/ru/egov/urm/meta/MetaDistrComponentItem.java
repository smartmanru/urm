package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;

public class MetaDistrComponentItem {

	Metadata meta;
	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	public boolean OBSOLETE;
	public String DEPLOYNAME;

	public MetaDistrComponentItem( Metadata meta ) {
		this.meta = meta;
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
