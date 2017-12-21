package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.Types.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductDoc {

	public Meta meta;
	public MetaDocs docs;

	public String NAME;
	public String DESC;
	public String EXT;
	public boolean UNITBOUND;

	public MetaProductDoc( Meta meta , MetaDocs docs ) {
		this.meta = meta;
		this.docs = docs;
	}
	
	public void createDoc( EngineTransaction transaction , String NAME , String EXT , String DESC , boolean UNITBOUND ) throws Exception {
		this.NAME = NAME;
		this.EXT = EXT;
		this.DESC = DESC;
		this.UNITBOUND = UNITBOUND;
	}

	public void setData( EngineTransaction transaction , String EXT , String DESC , boolean UNITBOUND ) throws Exception {
		this.EXT = EXT;
		this.DESC = DESC;
		this.UNITBOUND = UNITBOUND;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUM );
		EXT = ConfReader.getAttrValue( node , "ext" );
		DESC = ConfReader.getAttrValue( node , "desc" );
		UNITBOUND = ConfReader.getBooleanAttrValue( node , "unitbound" , false );
	}

	public MetaProductDoc copy( ActionBase action , Meta meta , MetaDocs docs ) throws Exception {
		MetaProductDoc r = new MetaProductDoc( meta , docs );
		
		r.NAME = NAME;
		r.EXT = EXT;
		r.DESC = DESC;
		r.UNITBOUND = UNITBOUND;
		return( r );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "ext" , EXT );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementBooleanAttr( doc , root , "unitbound" , UNITBOUND );
	}
	
}
