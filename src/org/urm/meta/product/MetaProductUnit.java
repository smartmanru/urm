package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.Types.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductUnit {

	public Meta meta;
	public MetaUnits units;

	public String NAME;
	public String DESC;

	public MetaProductUnit( Meta meta , MetaUnits units ) {
		this.meta = meta;
		this.units = units;
	}
	
	public void createUnit( EngineTransaction transaction , String NAME , String DESC ) throws Exception {
		this.NAME = NAME;
		this.DESC = DESC;
	}

	public void setData( EngineTransaction transaction , String desc ) throws Exception {
		this.DESC = desc;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUM );
		DESC = ConfReader.getAttrValue( node , "desc" );
	}

	public MetaProductUnit copy( ActionBase action , Meta meta , MetaUnits units ) throws Exception {
		MetaProductUnit r = new MetaProductUnit( meta , units );
		
		r.NAME = NAME;
		r.DESC = DESC;
		return( r );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
	}
	
}
