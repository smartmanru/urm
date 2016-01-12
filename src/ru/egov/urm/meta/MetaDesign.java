package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.MetadataStorage;

public class MetaDesign {

	public enum VarELEMENTTYPE {
		UNKNOWN ,
		EXTERNAL ,
		GENERIC ,
		SERVER ,
		DATABASE
	};
	
	public enum VarLINKTYPE {
		UNKNOWN ,
		GENERIC ,
		MSG
	};
	
	boolean loaded = false;

	Metadata meta;
	public Map<String,MetaDesignElement> elements;
	
	public MetaDesign( Metadata meta ) {
		this.meta = meta;
	}
	
	public void load( ActionBase action , MetadataStorage storage ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		// read xml
		String file = storage.getDesignFile( action );
		
		action.debug( "read distributive definition file " + file + "..." );
		Document doc = ConfReader.readXmlFile( action , file );
		loadElements( action , doc.getDocumentElement() );
	}

	public void loadElements( ActionBase action , Node node ) throws Exception {
		elements = new HashMap<String,MetaDesignElement>();

		Node[] items = ConfReader.xmlGetChildren( action , node , "element" );
		if( items == null )
			return;
		
		for( Node elementNode : items ) {
			MetaDesignElement element = new MetaDesignElement( this );
			element.load( action , elementNode );
			elements.put( element.NAME , element );
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
		if( ID == null )
			action.exit( "unknown element=" + ID );
		return( element );
	}
	
}
