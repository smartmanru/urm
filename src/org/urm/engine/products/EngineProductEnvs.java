package org.urm.engine.products;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.loader.MatchItem;
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

	public MetaEnv findEnv( int id ) {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ProductEnvs envs = storage.getEnviroments();
			MetaEnv env = envs.findMetaEnv( id );
			if( env != null )
				return( env );
		}
		return( null ); 
	}

	public MetaEnvSegment getSegment( int id ) throws Exception {
		MetaEnvSegment sg = findSegment( id );
		if( sg == null )
			Common.exitUnexpected();
		return( sg );
	}
	
	public MetaEnvSegment findSegment( int id ) {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ProductEnvs envs = storage.getEnviroments();
			MetaEnvSegment sg = envs.findMetaEnvSegment( id );
			if( sg != null )
				return( sg );
		}
		return( null ); 
	}

	public MetaEnvServer getServer( int id ) throws Exception {
		MetaEnvServer server = findServer( id );
		if( server == null )
			Common.exitUnexpected();
		return( server );
	}
	
	public MetaEnvServer findServer( int id ) {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			ProductEnvs envs = storage.getEnviroments();
			MetaEnvServer server = envs.findMetaEnvServer( id );
			if( server != null )
				return( server );
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

	public MetaEnv getEnv( int id ) throws Exception {
		MetaEnv env = findEnv( id );
		if( env == null )
			Common.exitUnexpected();
		return( env );
	}
	
	public MetaEnv getEnv( String name ) throws Exception {
		MetaEnv env = findEnv( name );
		if( env == null )
			Common.exitUnexpected();
		return( env );
	}
	
	public MetaEnv getEnv( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		
		if( item.MATCHED ) {
			MetaEnv env = findEnv( item.FKID );
			if( env == null )
				Common.exit1( _Error.UnknownEnvironment1 , "Unknown environment=" + item.FKID , "" + item.FKID );
			return( env );
		}
		
		MetaEnv env = findEnv( item.FKNAME );
		if( env == null )
			Common.exit1( _Error.UnknownEnvironment1 , "Unknown environment=" + item.FKNAME , "" + item.FKNAME );
		return( env );
	}
	
}
