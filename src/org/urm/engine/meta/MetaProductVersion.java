package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.urm.common.PropertyController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductVersion extends PropertyController {

	Meta meta;
	
	public int majorFirstNumber;
	public int majorSecondNumber;
	public int majorNextFirstNumber;
	public int majorNextSecondNumber;
	public int lastProdTag;
	public int nextProdTag;

	public static String PROPERTY_MAJOR_FIRST = "major.first";
	public static String PROPERTY_MAJOR_LAST = "major.last";
	public static String PROPERTY_NEXT_MAJOR_FIRST = "next.major.first";
	public static String PROPERTY_NEXT_MAJOR_LAST = "next.major.last";
	public static String PROPERTY_PROD_LASTTAG = "prod.lasttag";
	public static String PROPERTY_PROD_NEXTTAG = "prod.nexttag";
	
	public MetaProductVersion( Meta meta ) {
		super( "version" );
		
		this.meta = meta;
		meta.setVersion( this );
		
		majorFirstNumber = 0;
		majorSecondNumber = 0;
		majorNextFirstNumber = 0;
		majorNextSecondNumber = 0;
		lastProdTag = 0;
		nextProdTag = 0;
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		
		if( ( majorFirstNumber > majorNextFirstNumber ) || 
			( majorFirstNumber == majorNextFirstNumber && majorSecondNumber >= majorNextSecondNumber ) ||
			( lastProdTag >= nextProdTag ) )
			return( false );
		
		return( true );
	}
	
	public MetaProductVersion copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductVersion r = new MetaProductVersion( meta );
		r.initCopyStarted( this , null );
		r.scatterVariables( action );
		r.initFinished();
		
		return( r );
	}
	
	public void create( ActionBase action ) throws Exception {
		if( !super.initCreateStarted( null ) )
			return;

		majorFirstNumber = 1;
		majorSecondNumber = 0;
		majorNextFirstNumber = 1;
		majorNextSecondNumber = 1;
		lastProdTag = 0;
		nextProdTag = 1;
		
		gatherVariables( action );
		super.finishProperties( action );
		super.initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( !super.initCreateStarted( null ) )
			return;

		properties.loadFromNodeElements( root );
		scatterVariables( action );
		
		super.initFinished();
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;

		properties.saveAsElements( doc , root );
	}

	private void scatterVariables( ActionBase action ) throws Exception {
		majorFirstNumber = super.getIntPropertyRequired( action , PROPERTY_MAJOR_FIRST );
		majorSecondNumber = super.getIntPropertyRequired( action , PROPERTY_MAJOR_LAST );
		majorNextFirstNumber = super.getIntPropertyRequired( action , PROPERTY_NEXT_MAJOR_FIRST );
		majorNextSecondNumber = super.getIntPropertyRequired( action , PROPERTY_NEXT_MAJOR_LAST );
		lastProdTag = super.getIntPropertyRequired( action , PROPERTY_PROD_LASTTAG );
		nextProdTag = super.getIntPropertyRequired( action , PROPERTY_PROD_NEXTTAG );
		
		if( !isValid() )
			action.exit0( _Error.InconsistentVersionAttributes0 , "inconsistent version attributes" );
	}
	
	public void gatherVariables( ActionBase action ) throws Exception {
		if( !isValid() )
			action.exit0( _Error.InconsistentVersionAttributes0 , "inconsistent version attributes" );
	
		properties.setOriginalNumberProperty( PROPERTY_MAJOR_FIRST , majorFirstNumber );
		properties.setOriginalNumberProperty( PROPERTY_MAJOR_LAST , majorSecondNumber );
		properties.setOriginalNumberProperty( PROPERTY_NEXT_MAJOR_FIRST , majorNextFirstNumber );
		properties.setOriginalNumberProperty( PROPERTY_NEXT_MAJOR_LAST , majorNextSecondNumber );
		properties.setOriginalNumberProperty( PROPERTY_PROD_LASTTAG , lastProdTag );
		properties.setOriginalNumberProperty( PROPERTY_PROD_NEXTTAG , nextProdTag );
		properties.finishRawProperties();
	}
	
}
