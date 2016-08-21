package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaDesign {

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
	
	public MetaDesign( Meta meta ) {
		this.meta = meta;
		loaded = false;
		loadFailed = false;
	}
	
	public MetaDesign copy( ActionBase action , Meta meta ) throws Exception {
		return( null );
	}
	
	public void setLoadFailed() {
		loadFailed = true;
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		loadAttributes( action , root );
		loadElements( action , root );
	}

	public void loadAttributes( ActionBase action , Node node ) throws Exception {
		fullProd = ConfReader.getBooleanAttrValue( node , "fullprod" , false );
	}
	
	public void loadElements( ActionBase action , Node node ) throws Exception {
		elements = new HashMap<String,MetaDesignElement>();
		childs = new HashMap<String,MetaDesignElement>();
		groups = new HashMap<String,MetaDesignElement>();

		Node[] items = ConfReader.xmlGetChildren( node , "element" );
		if( items == null )
			return;
		
		for( Node elementNode : items ) {
			MetaDesignElement element = new MetaDesignElement( this , null );
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

	public VarELEMENTTYPE getElementType( ActionBase action , String ID ) throws Exception {
		VarELEMENTTYPE value = null;		
		
		try {
			value = VarELEMENTTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid element type=" + ID );
		}
		
		if( value == null )
			action.exit( "unknown element type=" + ID );
		
		return( value );
	}

	public VarLINKTYPE getLinkType( ActionBase action , String ID ) throws Exception {
		VarLINKTYPE value = null;		
		
		try {
			value = VarLINKTYPE.valueOf( Common.xmlToEnumValue( ID ) );
		}
		catch( IllegalArgumentException e ) {
			action.exit( "invalid link type=" + ID );
		}
		
		if( value == null )
			action.exit( "unknown link type=" + ID );
		
		return( value );
	}

	public MetaDesignElement getElement( ActionBase action , String ID ) throws Exception {
		MetaDesignElement element = elements.get( ID );
		if( element == null )
			action.exit( "unknown element=" + ID );
		return( element );
	}

	public void addSubGraphItem( ActionBase action , MetaDesignElement item , MetaDesignElement child ) throws Exception {
		elements.put( child.NAME , child );
	}
	
}
