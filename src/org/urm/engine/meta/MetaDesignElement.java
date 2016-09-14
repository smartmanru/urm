package org.urm.engine.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.meta.Meta.VarNAMETYPE;
import org.urm.engine.meta.MetaDesign.VarELEMENTTYPE;
import org.w3c.dom.Node;

public class MetaDesignElement {
	
	protected Meta meta;
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
	
	public MetaDesignElement( Meta meta , MetaDesign design , MetaDesignElement group ) {
		this.meta = meta;
		this.design = design;
		this.group = group;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		links = new HashMap<String,MetaDesignLink>();
		childs = new HashMap<String,MetaDesignElement>();
		
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		TYPE = ConfReader.getRequiredAttrValue( node , "type" );
		elementType = meta.getDesignElementType( action , TYPE );
		FUNCTION = ConfReader.getAttrValue( node , "function" );

		if( isGroup() ) {
			if( group != null )
				action.exit1( _Error.NestedGroupsDisallowed1 , "nested groups are diasallowed, item=" + NAME , NAME );
			GROUPCOLOR = ConfReader.getAttrValue( node , "color" );
			GROUPFILLCOLOR = ConfReader.getAttrValue( node , "fillcolor" );
		}
		
		// subgraph
		Node[] items = ConfReader.xmlGetChildren( node , "element" );
		if( items != null ) {
			if( !isGroup() )
				action.exit1( _Error.NonGroupItemChilds1 , "non-group item has childs, item=" + NAME , NAME );
			
			for( Node elementNode : items ) {
				MetaDesignElement child = new MetaDesignElement( meta , design , this );
				child.load( action , elementNode );
				childs.put( child.NAME , child );
				design.addSubGraphItem( action , this , child );
			}
		}
		
		// links
		items = ConfReader.xmlGetChildren( node , "link" );
		if( items != null ) {
			for( Node elementNode : items ) {
				MetaDesignLink link = new MetaDesignLink( meta , design , this );
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
			action.exit1( _Error.UnknownDesignLink1 , "unknown design link=" + ID , ID );
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
		action.exit0( _Error.UnableGetGroupItem0 , "unable to get group item" );
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
