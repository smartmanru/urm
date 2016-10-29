package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.ServerObject;
import org.urm.meta.ServerProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaDesign extends ServerObject {

	public enum VarELEMENTTYPE {
		UNKNOWN ,
		EXTERNAL ,
		GENERIC ,
		SERVER ,
		DATABASE ,
		LIBRARY ,
		GROUP
	};
	
	public enum VarLINKTYPE {
		UNKNOWN ,
		GENERIC ,
		MSG
	};
	
	private boolean loaded;
	public boolean loadFailed;

	protected Meta meta;
	public Map<String,MetaDesignElement> childs;
	public Map<String,MetaDesignElement> groups;
	public Map<String,MetaDesignElement> elements;
	public boolean fullProd;
	
	public MetaDesign( ServerProductMeta storage , Meta meta ) {
		super( storage );
		this.meta = meta;
		loaded = false;
		loadFailed = false;
		elements = new HashMap<String,MetaDesignElement>();
		childs = new HashMap<String,MetaDesignElement>();
		groups = new HashMap<String,MetaDesignElement>();
	}
	
	public MetaDesign copy( ActionBase action , Meta meta ) throws Exception {
		MetaDesign r = new MetaDesign( meta.getStorage( action ) , meta );
		r.fullProd = fullProd;
		for( MetaDesignElement element : elements.values() ) {
			MetaDesignElement relement = element.copy( action , meta , r );
			r.elements.put( relement.NAME , relement );
			if( relement.isGroup() )
				r.groups.put( relement.NAME , relement );
			else
				r.childs.put( relement.NAME , relement );
		}
		for( MetaDesignElement element : r.elements.values() )
			element.resolve( action );
		return( r );
	}
	
	public boolean isLoadFailed() {
		return( false );
	}
	
	public void setLoadFailed() {
		loadFailed = true;
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		fullProd = ConfReader.getBooleanAttrValue( root , "fullprod" , false );
		loadElements( action , root );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "fullprod" , Common.getBooleanValue( fullProd ) );
		saveElements( action , doc , root );
	}

	public void loadElements( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "element" );
		if( items == null )
			return;
		
		for( Node elementNode : items ) {
			MetaDesignElement element = new MetaDesignElement( meta , this , null );
			element.load( action , elementNode );
			elements.put( element.NAME , element );
			if( element.isGroup() )
				groups.put( element.NAME , element );
			else
				childs.put( element.NAME , element );
		}
		
		for( MetaDesignElement element : elements.values() )
			element.resolve( action );
	}

	public void saveElements( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaDesignElement element : elements.values() ) {
			Element item = Common.xmlCreateElement( doc , root , "element" );
			element.save( action , doc , item );
		}
	}
	
	public MetaDesignElement getElement( ActionBase action , String ID ) throws Exception {
		MetaDesignElement element = elements.get( ID );
		if( element == null )
			action.exit1( _Error.UnknownDesignElement1 , "unknown design element=" + ID , ID );
		return( element );
	}

	public void addSubGraphItem( ActionBase action , MetaDesignElement item , MetaDesignElement child ) throws Exception {
		elements.put( child.NAME , child );
	}
	
}
