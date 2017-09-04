package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.PropertyController;
import org.urm.engine.TransactionBase;
import org.urm.meta.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductVersion extends PropertyController {

	public Meta meta;
	
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
	
	public MetaProductVersion( ProductMeta storage , Meta meta ) {
		super( storage , null , "version" );
		
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
	public String getName() {
		return( "meta-version" );
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
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		majorFirstNumber = super.getIntPropertyRequired( action , PROPERTY_MAJOR_FIRST );
		majorSecondNumber = super.getIntPropertyRequired( action , PROPERTY_MAJOR_LAST );
		majorNextFirstNumber = super.getIntPropertyRequired( action , PROPERTY_NEXT_MAJOR_FIRST );
		majorNextSecondNumber = super.getIntPropertyRequired( action , PROPERTY_NEXT_MAJOR_LAST );
		lastProdTag = super.getIntPropertyRequired( action , PROPERTY_PROD_LASTTAG );
		nextProdTag = super.getIntPropertyRequired( action , PROPERTY_PROD_NEXTTAG );
		
		if( !isValid() )
			action.exit0( _Error.InconsistentVersionAttributes0 , "inconsistent version attributes" );
	}
	
	public MetaProductVersion copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductVersion r = new MetaProductVersion( meta.getStorage( action ) , meta );
		r.initCopyStarted( this , null );
		r.scatterProperties( action );
		r.initFinished();
		
		return( r );
	}
	
	public void createVersion( TransactionBase transaction , int majorFirstNumber , int majorSecondNumber , int majorNextFirstNumber , int majorNextSecondNumber , int lastProdTag , int nextProdTag ) throws Exception {
		if( !super.initCreateStarted( null ) )
			return;

		this.majorFirstNumber = majorFirstNumber;
		this.majorSecondNumber = majorSecondNumber;
		this.majorNextFirstNumber = majorNextFirstNumber;
		this.majorNextSecondNumber = majorNextSecondNumber;
		this.lastProdTag = lastProdTag;
		this.nextProdTag = nextProdTag;
		
		if( !isValid() )
			transaction.exit0( _Error.InconsistentVersionAttributes0 , "Inconsistent version attributes" );
		
		setProperties( transaction.action );
		super.initFinished();
	}

	public void updateVersion( TransactionBase transaction , int majorFirstNumber , int majorSecondNumber , int majorNextFirstNumber , int majorNextSecondNumber , int lastProdTag , int nextProdTag ) throws Exception {
		this.majorFirstNumber = majorFirstNumber;
		this.majorSecondNumber = majorSecondNumber;
		this.majorNextFirstNumber = majorNextFirstNumber;
		this.majorNextSecondNumber = majorNextSecondNumber;
		this.lastProdTag = lastProdTag;
		this.nextProdTag = nextProdTag;
		
		if( !isValid() )
			transaction.exit0( _Error.InconsistentVersionAttributes0 , "Inconsistent version attributes" );
		
		setProperties( transaction.action );
		super.recalculateProperties();
	}
	
	public void setProperties( ActionBase action ) throws Exception {
		super.setNumberProperty( PROPERTY_MAJOR_FIRST , majorFirstNumber );
		super.setNumberProperty( PROPERTY_MAJOR_LAST , majorSecondNumber );
		super.setNumberProperty( PROPERTY_NEXT_MAJOR_FIRST , majorNextFirstNumber );
		super.setNumberProperty( PROPERTY_NEXT_MAJOR_LAST , majorNextSecondNumber );
		super.setNumberProperty( PROPERTY_PROD_LASTTAG , lastProdTag );
		super.setNumberProperty( PROPERTY_PROD_NEXTTAG , nextProdTag );
		super.finishProperties( action );
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( !super.initCreateStarted( null ) )
			return;

		super.loadFromNodeElements( action , root , false );
		scatterProperties( action );
		
		super.initFinished();
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveAsElements( doc , root , false );
	}

}
