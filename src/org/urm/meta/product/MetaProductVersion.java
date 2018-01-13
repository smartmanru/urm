package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.engine.TransactionBase;
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
			( lastProdTag >= nextProdTag ) )
			return( false );
		
		return( true );
	}
	
	public void scatterProperties( ActionBase action ) throws Exception {
	}
	
	public MetaProductVersion copy( ActionBase action , Meta meta ) throws Exception {
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
	
	public void createVersion( TransactionBase transaction , int majorFirstNumber , int majorSecondNumber , int majorNextFirstNumber , int majorNextSecondNumber , int lastProdTag , int nextProdTag ) throws Exception {
		updateVersion( transaction , majorFirstNumber , majorSecondNumber , majorNextFirstNumber , majorNextSecondNumber , lastProdTag , nextProdTag );
	}

	public void updateVersion( TransactionBase transaction , int majorLastFirstNumber , int majorLastSecondNumber , int majorNextFirstNumber , int majorNextSecondNumber , int lastProdTag , int nextProdTag ) throws Exception {
		this.majorLastFirstNumber = majorLastFirstNumber;
		this.majorLastSecondNumber = majorLastSecondNumber;
		this.majorNextFirstNumber = majorNextFirstNumber;
		this.majorNextSecondNumber = majorNextSecondNumber;
		this.lastProdTag = lastProdTag;
		this.nextProdTag = nextProdTag;
		this.lastUrgentTag = 0;
		this.nextUrgentTag = 0;
		
		if( !isValid() )
			transaction.exit0( _Error.InconsistentVersionAttributes0 , "Inconsistent version attributes" );
	}
	
}
