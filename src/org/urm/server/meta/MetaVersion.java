package org.urm.server.meta;

import org.urm.common.PropertySet;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaVersion {

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
	
	public MetaVersion( Meta meta ) {
		loaded = false;
		loadFailed = false;
		
		majorFirstNumber = 0;
		majorSecondNumber = 0;
		majorNextFirstNumber = 0;
		majorNextSecondNumber = 0;
		lastProdTag = 0;
		nextProdTag = 0;
	}
	
	public void setLoadFailed() {
		loadFailed = true;
	}

	public void create( ActionBase action , ServerRegistry registry ) {
		majorFirstNumber = 1;
		majorSecondNumber = 0;
		majorNextFirstNumber = 1;
		majorNextSecondNumber = 1;
		lastProdTag = 0;
		nextProdTag = 1;
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		properties.loadRawFromNodeElements( root );
		scatterSystemProperties( action );
		properties.finishRawProperties();
	}

	private void scatterSystemProperties( ActionBase action ) throws Exception {
		majorFirstNumber = properties.getSystemRequiredIntProperty( PROPERTY_MAJOR_FIRST );
		majorSecondNumber = properties.getSystemRequiredIntProperty( PROPERTY_MAJOR_LAST );
		majorNextFirstNumber = properties.getSystemRequiredIntProperty( PROPERTY_NEXT_MAJOR_FIRST );
		majorNextSecondNumber = properties.getSystemRequiredIntProperty( PROPERTY_NEXT_MAJOR_LAST );
		lastProdTag = properties.getSystemRequiredIntProperty( PROPERTY_PROD_LASTTAG );
		nextProdTag = properties.getSystemRequiredIntProperty( PROPERTY_PROD_NEXTTAG );
		
		if( ( majorFirstNumber > majorNextFirstNumber ) || 
			( majorFirstNumber == majorNextFirstNumber && majorSecondNumber >= majorNextSecondNumber ) ||
			( lastProdTag >= nextProdTag ) )
			action.exit( "inconsistent version attributes" );
	}
	
}
