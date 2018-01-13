package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaUnits {

	protected Meta meta;
	
	public Map<String,MetaProductUnit> mapUnits;
	
	public MetaUnits( ProductMeta storage , MetaProductSettings settings , Meta meta ) {
		this.meta = meta;
		meta.setUnits( this );
		mapUnits = new HashMap<String,MetaProductUnit>();
	}
	
	public MetaUnits copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings product = meta.getProductSettings();
		MetaUnits r = new MetaUnits( meta.getStorage() , product , meta );
		
		for( MetaProductUnit unit : mapUnits.values() ) {
			MetaProductUnit runit = unit.copy( action , meta , r );
			r.mapUnits.put( runit.NAME , runit );
		}
		return( r );
	}
	
	public void createUnits( TransactionBase transaction ) throws Exception {
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( root != null )
			loadUnitSet( action , root );
	}

	private void loadUnitSet( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "unit" );
		if( items == null )
			return;
		
		for( Node unitNode : items ) {
			MetaProductUnit item = new MetaProductUnit( meta , this );
			item.load( action , unitNode );
			mapUnits.put( item.NAME , item );
		}
	}

	private void saveUnitSet( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaProductUnit unit : mapUnits.values() ) {
			Element unitElement = Common.xmlCreateElement( doc , root , "unit" );
			unit.save( action , doc , unitElement );
		}
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
	
	public MetaProductUnit getUnit( ActionBase action , String name ) throws Exception {
		MetaProductUnit unit = mapUnits.get( name );
		if( unit == null )
			action.exit1( _Error.UnknownUnit1 , "unknown unit=" + name , name );
		return( unit );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		//super.saveAsElements( doc , root , false );
		saveUnitSet( action , doc , root );
	}

	public void createUnit( EngineTransaction transaction , MetaProductUnit unit ) throws Exception {
		mapUnits.put( unit.NAME , unit );
	}
	
	public void modifyUnit( EngineTransaction transaction , MetaProductUnit unit ) throws Exception {
	}
	
	public void deleteUnit( EngineTransaction transaction , MetaProductUnit unit ) throws Exception {
		MetaDistr distr = unit.meta.getDistr();
		distr.deleteUnit( transaction , unit );
		MetaSource sources = unit.meta.getSources();
		sources.deleteUnit( transaction , unit );
		mapUnits.remove( unit.NAME );
	}
	
}
