package org.urm.server.meta;

import org.urm.common.PropertySet;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductVersion {

	private boolean loaded;
	public boolean loadFailed;

	public int majorFirstNumber;
	public int majorSecondNumber;
	public int majorNextFirstNumber;
	public int majorNextSecondNumber;
	public int lastProdTag;
	public int nextProdTag;

	PropertySet properties;

	public static String PROPERTY_MAJOR_FIRST = "major.first";
	public static String PROPERTY_MAJOR_LAST = "major.last";
	public static String PROPERTY_NEXT_MAJOR_FIRST = "next.major.first";
	public static String PROPERTY_NEXT_MAJOR_LAST = "next.major.last";
	public static String PROPERTY_PROD_LASTTAG = "major.prod.lasttag";
	public static String PROPERTY_PROD_NEXTTAG = "major.prod.nexttag";
	
	public MetaProductVersion( Meta meta ) {
		loaded = false;
		loadFailed = false;
		
		majorFirstNumber = 0;
		majorSecondNumber = 0;
		majorNextFirstNumber = 0;
		majorNextSecondNumber = 0;
		lastProdTag = 0;
		nextProdTag = 0;
		
		properties = new PropertySet( "version" , null );
	}
	
	public MetaProductVersion copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductVersion r = new MetaProductVersion( meta );
		r.loaded = true;
		r.properties = properties.copy( null );
		try {
			r.scatterVariables( action );
			r.loadFailed = false;
		}
		catch( Throwable e ) {
			r.loadFailed = true;
		}
		
		return( r );
	}
	
	public void createFailed() {
		loaded = true;
		loadFailed = true;
	}

	public void create( ActionBase action , ServerRegistry registry ) throws Exception {
		loaded = true;
		majorFirstNumber = 1;
		majorSecondNumber = 0;
		majorNextFirstNumber = 1;
		majorNextSecondNumber = 1;
		lastProdTag = 0;
		nextProdTag = 1;
		gatherVariables( action );
		
		loadFailed = false;
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		properties.loadRawFromNodeElements( root );
		scatterVariables( action );
		properties.finishRawProperties();
		loadFailed = false;
	}

	public void save( ActionBase action , Element root ) throws Exception {
		if( !loaded )
			return;

		properties.saveAsElements( root.getOwnerDocument() , root );
	}

	public boolean isValid() {
		if( loadFailed )
			return( false );
		
		if( ( majorFirstNumber > majorNextFirstNumber ) || 
			( majorFirstNumber == majorNextFirstNumber && majorSecondNumber >= majorNextSecondNumber ) ||
			( lastProdTag >= nextProdTag ) )
			return( false );
		
		return( true );
	}
	
	private void scatterVariables( ActionBase action ) throws Exception {
		majorFirstNumber = properties.getSystemRequiredIntProperty( PROPERTY_MAJOR_FIRST );
		majorSecondNumber = properties.getSystemRequiredIntProperty( PROPERTY_MAJOR_LAST );
		majorNextFirstNumber = properties.getSystemRequiredIntProperty( PROPERTY_NEXT_MAJOR_FIRST );
		majorNextSecondNumber = properties.getSystemRequiredIntProperty( PROPERTY_NEXT_MAJOR_LAST );
		lastProdTag = properties.getSystemRequiredIntProperty( PROPERTY_PROD_LASTTAG );
		nextProdTag = properties.getSystemRequiredIntProperty( PROPERTY_PROD_NEXTTAG );
		
		if( !isValid() )
			action.exit( "inconsistent version attributes" );
	}
	
	public void gatherVariables( ActionBase action ) throws Exception {
		properties.setNumberProperty( PROPERTY_MAJOR_FIRST , majorFirstNumber );
		properties.setNumberProperty( PROPERTY_MAJOR_LAST , majorSecondNumber );
		properties.setNumberProperty( PROPERTY_NEXT_MAJOR_FIRST , majorNextFirstNumber );
		properties.setNumberProperty( PROPERTY_NEXT_MAJOR_LAST , majorNextSecondNumber );
		properties.setNumberProperty( PROPERTY_PROD_LASTTAG , lastProdTag );
		properties.setNumberProperty( PROPERTY_PROD_NEXTTAG , nextProdTag );
		properties.finishRawProperties();
		loaded = true;
	}

}
