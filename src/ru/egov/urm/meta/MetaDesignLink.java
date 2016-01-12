package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaDesign.VarLINKTYPE;
import ru.egov.urm.run.ActionBase;

public class MetaDesignLink {

	MetaDesign design;
	MetaDesignElement element;
	
	String TARGET;
	MetaDesignElement target;
	public String TYPE;
	public VarLINKTYPE linkType;
	public String TEXT;
	
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
	
}
