package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDesign.VarELEMENTTYPE;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;

public class MetaDesignElement {
	
	MetaDesign design;
	MetaDesignElement group;
	public Map<String,MetaDesignLink> links;
	public Map<String,MetaDesignElement> childs;
	
	public String NAME;
	public String TYPE;
	public String GROUPCOLOR;
	public String GROUPFILLCOLOR;
	public String FUNCTION;
	private VarELEMENTTYPE elementType;
	
	public MetaDesignElement( MetaDesign design , MetaDesignElement group ) {
		this.design = design;
		this.group = group;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		links = new HashMap<String,MetaDesignLink>();
		childs = new HashMap<String,MetaDesignElement>();
		
		NAME = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		TYPE = ConfReader.getRequiredAttrValue( action , node , "type" );
		elementType = design.getElementType( action , TYPE );
		FUNCTION = ConfReader.getAttrValue( action , node , "function" );

		if( isGroup() ) {
			if( group != null )
				action.exit( "nested groups are diasallowed, item=" + NAME );
			GROUPCOLOR = ConfReader.getAttrValue( action , node , "color" );
			GROUPFILLCOLOR = ConfReader.getAttrValue( action , node , "fillcolor" );
		}
		
		// subgraph
		Node[] items = ConfReader.xmlGetChildren( action , node , "element" );
		if( items != null ) {
			if( !isGroup() )
				action.exit( "non-group item has childs, item=" + NAME );
			
			for( Node elementNode : items ) {
				MetaDesignElement child = new MetaDesignElement( design , this );
				child.load( action , elementNode );
				childs.put( child.NAME , child );
				design.addSubGraphItem( action , this , child );
			}
		}
		
		// links
		items = ConfReader.xmlGetChildren( action , node , "link" );
		if( items != null ) {
			for( Node elementNode : items ) {
				MetaDesignLink link = new MetaDesignLink( design , this );
				link.load( action , elementNode );
				links.put( link.TARGET , link );
			}
		}
	}

	public void resolve( ActionBase action ) throws Exception {
		for( MetaDesignLink link : links.values() )
			link.resolve( action );
	}

	public MetaDesignLink getLink( ActionBase action , String ID ) throws Exception {
		MetaDesignLink link = links.get( ID );
		if( ID == null )
			action.exit( "unknown link=" + ID );
		return( link );
	}

	public String getName( ActionBase action ) throws Exception {
		if( !isGroup() )
			return( Common.getQuoted( NAME ) );
		return( "cluster_" + Common.replace( NAME , "." , "_" ) );
	}
	
	public String getLinkName( ActionBase action ) throws Exception {
		if( !isGroup() )
			return( getName( action ) );
		for( String s : childs.keySet() )
			return( Common.getQuoted( s ) );
		action.exit( "unable to get group item" );
		return( null );
	}

	public boolean isGroup() {
		if( elementType == VarELEMENTTYPE.GROUP )
			return( true );
		return( false );
	}
	
	public boolean isGenericType() {
		return( elementType == VarELEMENTTYPE.GENERIC );
	}
	
	public boolean isExternalType() {
		return( elementType == VarELEMENTTYPE.EXTERNAL );
	}
	
	public boolean isLibraryType() {
		return( elementType == VarELEMENTTYPE.LIBRARY );
	}
	
	public boolean isAppServerType() {
		return( elementType == VarELEMENTTYPE.SERVER );
	}
	
	public boolean isDatabaseServerType() {
		return( elementType == VarELEMENTTYPE.DATABASE );
	}
	
	public boolean isServerType() {
		if( elementType == VarELEMENTTYPE.DATABASE || 
			elementType == VarELEMENTTYPE.SERVER )
			return( true );
		return( false );
	}
}
