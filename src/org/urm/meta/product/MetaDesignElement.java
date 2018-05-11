package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDesignElement {
	
	protected Meta meta;
	MetaDesignDiagram design;
	MetaDesignElement group;
	public Map<String,MetaDesignLink> links;
	public Map<String,MetaDesignElement> childs;
	
	public String NAME;
	public String GROUPCOLOR;
	public String GROUPFILLCOLOR;
	public String FUNCTION;
	private EnumElementType elementType;
	
	public MetaDesignElement( Meta meta , MetaDesignDiagram design , MetaDesignElement group ) {
		this.meta = meta;
		this.design = design;
		this.group = group;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		links = new HashMap<String,MetaDesignLink>();
		childs = new HashMap<String,MetaDesignElement>();
		
		NAME = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		String TYPE = ConfReader.getRequiredAttrValue( node , "type" );
		elementType = Types.getDesignElementType( TYPE , false );
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
				addChild( child );
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

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( elementType ) );
		Common.xmlSetElementAttr( doc , root , "function" , FUNCTION );

		if( isGroup() ) {
			Common.xmlSetElementAttr( doc , root , "color" , GROUPCOLOR );
			Common.xmlSetElementAttr( doc , root , "fillcolor" , GROUPFILLCOLOR );
			
			for( MetaDesignElement child : childs.values() ) {
				Element childElement = Common.xmlCreateElement( doc , root , "element" );
				child.save( action , doc , childElement );
			}
		}
		
		for( MetaDesignLink link : links.values() ) {
			Element linkElement = Common.xmlCreateElement( doc , root , "link" );
			link.save( action , doc , linkElement );
		}		
	}
	
	private void addChild( MetaDesignElement child ) throws Exception {
		childs.put( child.NAME , child );
		design.addSubGraphItem( this , child );
	}
	
	public MetaDesignElement copy( Meta meta , MetaDesignDiagram design ) throws Exception {
		return( copy( meta , design , null ) );
	}
	
	public MetaDesignElement copy( Meta meta , MetaDesignDiagram design , MetaDesignElement group ) throws Exception {
		MetaDesignElement r = new MetaDesignElement( meta , design , group );
		
		r.NAME = NAME;
		r.elementType = elementType;
		r.FUNCTION = FUNCTION;

		if( isGroup() ) {
			r.GROUPCOLOR = GROUPCOLOR;
			r.GROUPFILLCOLOR = GROUPFILLCOLOR;
			
			for( MetaDesignElement child : childs.values() ) {
				MetaDesignElement rchild = child.copy( meta , design , r );
				r.addChild( rchild );
			}
		}
		
		for( MetaDesignLink link : links.values() ) {
			MetaDesignLink rlink = link.copy( meta , r );
			r.links.put( rlink.TARGET , rlink );
		}
		
		return( r );
	}		
	
	public void resolve() throws Exception {
		for( MetaDesignLink link : links.values() )
			link.resolve();
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
		if( elementType == EnumElementType.GROUP )
			return( true );
		return( false );
	}
	
	public boolean isGenericType() {
		return( elementType == EnumElementType.GENERIC );
	}
	
	public boolean isExternalType() {
		return( elementType == EnumElementType.EXTERNAL );
	}
	
	public boolean isLibraryType() {
		return( elementType == EnumElementType.LIBRARY );
	}
	
	public boolean isAppServerType() {
		return( elementType == EnumElementType.SERVER );
	}
	
	public boolean isDatabaseServerType() {
		return( elementType == EnumElementType.DATABASE );
	}
	
	public boolean isServerType() {
		if( elementType == EnumElementType.DATABASE || 
			elementType == EnumElementType.SERVER )
			return( true );
		return( false );
	}
	
}
