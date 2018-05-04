package org.urm.engine.products;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.env.MetaDump;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.product.ProductMeta;

public class EngineProductEnvs {

	public Engine engine;
	public EngineProduct ep;

	public EngineProductEnvs( EngineProduct ep ) {
		this.engine = ep.engine;
		this.ep = ep;
	}
	
	public String[] getEnvNames() {
		EngineProductRevisions revisions = ep.getRevisions();
		
		String[] list = new String[0];
		for( ProductMeta storage : revisions.getRevisions() ) {
			ProductEnvs envs = storage.getEnviroments();
			list = Common.addArrays( list , envs.getEnvNames() );
		}
		return( Common.getSortedList( list ) );
	}
	
	public MetaEnv findEnv( String name ) {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ProductEnvs envs = storage.getEnviroments();
			MetaEnv env = envs.findMetaEnv( name );
			if( env != null )
				return( env );
		}
		return( null ); 
	}

	public MetaEnv[] getEnvs() {
		String[] names = getEnvNames();
		MetaEnv[] envs = new MetaEnv[ names.length ];
		for( int k = 0; k < names.length; k++ )
			envs[ k ] = findEnv( names[ k ] );
		return( envs );
	}

	public String[] getExportDumpNames() {
		String[] list = new String[0];
		for( MetaEnv env : getEnvs() )
			list = Common.addArrays( list , env.getExportDumpNames() );
		return( Common.getSortedList( list ) );
	}

	public MetaDump findExportDump( String name ) {
		for( MetaEnv env : getEnvs() ) {
			MetaDump dump = env.findExportDump( name );
			if( dump != null )
				return( dump );
		}
		return( null );
	}
	
	public String[] getImportDumpNames() {
		String[] list = new String[0];
		for( MetaEnv env : getEnvs() )
			list = Common.addArrays( list , env.getImportDumpNames() );
		return( Common.getSortedList( list ) );
	}

	public MetaDump findImportDump( String name ) {
		for( MetaEnv env : getEnvs() ) {
			MetaDump dump = env.findImportDump( name );
			if( dump != null )
				return( dump );
		}
		return( null );
	}
	
}
