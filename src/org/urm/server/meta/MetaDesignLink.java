package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaDesign.VarLINKTYPE;
import org.w3c.dom.Node;

public class MetaDesignLink {

	protected Meta meta;
	MetaDesign design;
	MetaDesignElement element;
	
	public String TARGET;
	public MetaDesignElement target;
	public String TYPE;
	public String TEXT;
	private VarLINKTYPE linkType;
	
	public MetaDesignLink( Meta meta , MetaDesign design , MetaDesignElement element ) {
		this.meta = meta;
		this.design = design;
		this.element = element;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		TARGET = ConfReader.getRequiredAttrValue( node , "target" );
		TYPE = ConfReader.getRequiredAttrValue( node , "type" );
		linkType = meta.getDesignLinkType( action , TYPE );
		TEXT = ConfReader.getAttrValue( node , "text" );
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
