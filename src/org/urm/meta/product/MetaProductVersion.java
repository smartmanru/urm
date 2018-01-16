package org.urm.meta.product;

import org.urm.common.Common;
import org.urm.meta.ProductMeta;

public class MetaProductVersion {

	public Meta meta;
	
	public int majorLastFirstNumber;
	public int majorLastSecondNumber;
	public int majorNextFirstNumber;
	public int majorNextSecondNumber;
	public int lastProdTag;
	public int nextProdTag;
	public int lastUrgentTag;
	public int nextUrgentTag;

	public MetaProductVersion( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setVersion( this );
		
		majorLastFirstNumber = 0;
		majorLastSecondNumber = 0;
		majorNextFirstNumber = 0;
		majorNextSecondNumber = 0;
		lastProdTag = 0;
		nextProdTag = 0;
		lastUrgentTag = 0;
		nextUrgentTag = 0;
	}
	
	public boolean isValid() {
		if( ( majorLastFirstNumber > majorNextFirstNumber ) || 
			( majorLastFirstNumber == majorNextFirstNumber && majorLastSecondNumber >= majorNextSecondNumber ) ||
			( lastProdTag >= nextProdTag ) ||
			( lastUrgentTag >= nextUrgentTag ) )
			return( false );
		
		return( true );
	}
	
	public MetaProductVersion copy( Meta meta ) {
		MetaProductVersion r = new MetaProductVersion( meta.getStorage() , meta );
		r.majorLastFirstNumber = majorLastFirstNumber;
		r.majorLastSecondNumber = majorLastSecondNumber;
		r.majorNextFirstNumber = majorNextFirstNumber;
		r.majorNextSecondNumber = majorNextSecondNumber;
		r.lastProdTag = lastProdTag;
		r.nextProdTag = nextProdTag;
		r.lastUrgentTag = lastUrgentTag;
		r.nextUrgentTag = nextUrgentTag;
		
		return( r );
	}
	
	public void createVersion( int majorFirstNumber , int majorSecondNumber , int lastProdTag , int lastUrgentTag , int majorNextFirstNumber , int majorNextSecondNumber , int nextProdTag , int nextUrgentTag ) throws Exception {
		updateVersion( majorFirstNumber , majorSecondNumber , lastProdTag , nextUrgentTag , majorNextFirstNumber , majorNextSecondNumber , nextProdTag , lastUrgentTag );
	}

	public void updateVersion( int majorLastFirstNumber , int majorLastSecondNumber , int lastProdTag , int nextUrgentTag , int majorNextFirstNumber , int majorNextSecondNumber , int nextProdTag , int lastUrgentTag ) throws Exception {
		this.majorLastFirstNumber = majorLastFirstNumber;
		this.majorLastSecondNumber = majorLastSecondNumber;
		this.majorNextFirstNumber = majorNextFirstNumber;
		this.majorNextSecondNumber = majorNextSecondNumber;
		this.lastProdTag = lastProdTag;
		this.nextProdTag = nextProdTag;
		this.lastUrgentTag = lastUrgentTag;
		this.nextUrgentTag = nextUrgentTag;
		
		if( !isValid() )
			Common.exit0( _Error.InconsistentVersionAttributes0 , "Inconsistent version attributes" );
	}
	
}
