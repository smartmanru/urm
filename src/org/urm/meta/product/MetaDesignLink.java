package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.loader.Types;
import org.urm.meta.loader.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDesignLink {

	protected Meta meta;
	MetaDesignDiagram design;
	MetaDesignElement element;
	
	public String TARGET;
	public MetaDesignElement target;
	public String TEXT;
	private EnumLinkType linkType;
	
	public MetaDesignLink( Meta meta , MetaDesignDiagram design , MetaDesignElement element ) {
		this.meta = meta;
		this.design = design;
		this.element = element;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		TARGET = ConfReader.getRequiredAttrValue( node , "target" );
		String TYPE = ConfReader.getRequiredAttrValue( node , "type" );
		linkType = Types.getDesignLinkType( TYPE , false );
		TEXT = ConfReader.getAttrValue( node , "text" );
	}

	public MetaDesignLink copy( Meta meta , MetaDesignElement element ) throws Exception {
		MetaDesignLink r = new MetaDesignLink( meta , element.design , element );
		r.TARGET = TARGET;
		r.linkType = linkType;
		r.TEXT = TEXT;
		return( r );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( target != null )
			Common.xmlSetElementAttr( doc , root , "target" , target.NAME );
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( linkType ) );
		Common.xmlSetElementAttr( doc , root , "text" , TEXT );
	}
	
	public void resolve() throws Exception {
		target = design.getElement( TARGET );
	}

	public boolean isGenericType() {
		return( linkType == EnumLinkType.GENERIC );		
	}

	public boolean isMsgType() {
		return( linkType == EnumLinkType.MSG );		
	}
	
}
