package org.urm.meta.system;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.engine._Error;

public class AppProductDumps {

	public AppProduct product;
	public Map<Integer,ProductDump> mapExportById;
	public Map<Integer,ProductDump> mapImportById;

	public AppProductDumps( AppProduct product ) {
		this.product = product;
		
		mapExportById = new HashMap<Integer,ProductDump>();
		mapImportById = new HashMap<Integer,ProductDump>();
	}

	public AppProductDumps copy( AppProduct rproduct ) throws Exception {
		AppProductDumps r = new AppProductDumps( rproduct );
		
		for( ProductDump dump : mapExportById.values() ) {
			ProductDump rdump = dump.copy( r );
			r.addDump( rdump );
		}
		
		for( ProductDump dump : mapImportById.values() ) {
			ProductDump rdump = dump.copy( r );
			r.addDump( rdump );
		}
		return( r );
	}

	public String[] getExportDumpNames() {
		List<String> list = new LinkedList<String>();
		for( ProductDump dump : mapExportById.values() )
			list.add( dump.NAME );
		return( Common.getSortedList( list ) );
	}

	public String[] getImportDumpNames() {
		List<String> list = new LinkedList<String>();
		for( ProductDump dump : mapImportById.values() )
			list.add( dump.NAME );
		return( Common.getSortedList( list ) );
	}

	public ProductDump findExportDump( String name ) {
		for( ProductDump dump : mapExportById.values() ) {
			if( name.equals( dump.NAME ) )
				return( dump );
		}
		return( null );
	}
	
	public ProductDump findImportDump( String name ) {
		for( ProductDump dump : mapImportById.values() ) {
			if( name.equals( dump.NAME ) )
				return( dump );
		}
		return( null );
	}
	
	public void addDump( ProductDump dump ) {
		if( dump.MODEEXPORT )
			mapExportById.put( dump.ID , dump );
		else
			mapImportById.put( dump.ID , dump );
	}
	
	public void removeDump( ProductDump dump ) {
		if( dump.MODEEXPORT )
			mapExportById.remove( dump.ID );
		else
			mapImportById.remove( dump.ID );
	}

	public ProductDump findDump( int id ) {
		ProductDump dump = mapExportById.get( id );
		if( dump != null )
			return( dump );
		
		dump = mapImportById.get( id );
		if( dump != null )
			return( dump );
		
		return( null );
	}
	
	public ProductDump getDump( int id ) throws Exception {
		ProductDump dump = findDump( id );
		if( dump == null )
			Common.exit1( _Error.UnknownDump1 , "unknown dump=" + id , "" + id );
		return( dump );
	}

}
