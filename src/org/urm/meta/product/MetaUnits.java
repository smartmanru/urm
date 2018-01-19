package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.ProductMeta;

public class MetaUnits {

	public Meta meta;
	
	private Map<String,MetaProductUnit> mapUnits;
	
	public MetaUnits( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setUnits( this );
		mapUnits = new HashMap<String,MetaProductUnit>();
	}
	
	public MetaUnits copy( Meta meta ) throws Exception {
		MetaUnits r = new MetaUnits( meta.getStorage() , meta );
		
		for( MetaProductUnit unit : mapUnits.values() ) {
			MetaProductUnit runit = unit.copy( meta , r );
			r.addUnit( runit );
		}
		return( r );
	}
	
	public void addUnit( MetaProductUnit unit ) {
		mapUnits.put( unit.NAME , unit );
	}

	public void removeUnit( MetaProductUnit unit ) {
		mapUnits.remove( unit.NAME );
	}

	public void updateUnit( MetaProductUnit unit ) throws Exception {
		Common.changeMapKey( mapUnits , unit , unit.NAME );
	}
	
	public boolean isEmpty() {
		return( mapUnits.isEmpty() );
	}
	
	public String[] getUnitNames() {
		return( Common.getSortedKeys( mapUnits ) );
	}

	public MetaProductUnit[] getUnitList() {
		return( mapUnits.values().toArray( new MetaProductUnit[0] ) );
	}

	public MetaProductUnit findUnit( String name ) {
		return( mapUnits.get( name ) );
	}
	
	public MetaProductUnit getUnit( String name ) throws Exception {
		MetaProductUnit unit = mapUnits.get( name );
		if( unit == null )
			Common.exit1( _Error.UnknownUnit1 , "unknown unit=" + name , name );
		return( unit );
	}

}
