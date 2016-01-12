package ru.egov.urm.meta;

import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaDesign.VarELEMENTTYPE;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;
import ru.egov.urm.run.ActionBase;

public class MetaDesignElement {
	
	MetaDesign design;
	public Map<String,MetaDesignLink> links;
	
	public String NAME;
	public String TYPE;
	public VarELEMENTTYPE elementType;
	public String FUNCTION;
	
	public MetaDesignElement( MetaDesign design ) {
		this.design = design;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		NAME = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		TYPE = ConfReader.getRequiredAttrValue( action , node , "type" );
		elementType = design.getElementType( action , TYPE );
		FUNCTION = ConfReader.getAttrValue( action , node , "function" );
		
		// read links
		Node[] items = ConfReader.xmlGetChildren( action , node , "link" );
		if( items == null )
			return;
		
		for( Node elementNode : items ) {
			MetaDesignLink link = new MetaDesignLink( design , this );
			link.load( action , elementNode );
			links.put( link.TARGET , link );
		}
	}

	public void resolve( ActionBase action ) throws Exception {
		for( MetaDesignLink link : links.values() )
			link.resolve( action );
	}
	
}
