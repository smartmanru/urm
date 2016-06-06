package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaDesign.VarLINKTYPE;
import org.w3c.dom.Node;

public class MetaDesignLink {

	MetaDesign design;
	MetaDesignElement element;
	
	public String TARGET;
	public MetaDesignElement target;
	public String TYPE;
	public String TEXT;
	private VarLINKTYPE linkType;
	
	public MetaDesignLink( MetaDesign design , MetaDesignElement element ) {
		this.design = design;
		this.element = element;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		TARGET = ConfReader.getRequiredAttrValue( action , node , "target" );
		TYPE = ConfReader.getRequiredAttrValue( action , node , "type" );
		linkType = design.getLinkType( action , TYPE );
		TEXT = ConfReader.getAttrValue( action , node , "text" );
	}

	public void resolve( ActionBase action ) throws Exception {
		target = design.getElement( action , TARGET );
	}

	public boolean isGenericType() {
		return( linkType == VarLINKTYPE.GENERIC );		
	}

	public boolean isMsgType() {
		return( linkType == VarLINKTYPE.MSG );		
	}
	
}
