package org.urm.meta.env;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.EngineTransaction;
import org.urm.engine.storage.ProductStorage;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.ProductMeta;

public class ProductEnvs {

	public Meta meta;
	
	private Map<String,MetaEnv> mapEnvs;
	private Map<Integer,MetaEnv> mapEnvsById;
	private MetaMonitoring mon;
	
	public ProductEnvs( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		
		mapEnvs = new HashMap<String,MetaEnv>();
		mapEnvsById = new HashMap<Integer,MetaEnv>();
		mon = new MetaMonitoring( storage , meta );
	}
	
	public ProductEnvs copy( ActionBase action , Meta rmeta ) throws Exception {
		ProductEnvs r = new ProductEnvs( rmeta.getStorage() , rmeta );
		
		MetaProductSettings rsettings = rmeta.getProductSettings();
		for( MetaEnv env : mapEnvs.values() ) {
			MetaEnv renv = env.copy( rmeta.getStorage() , rmeta , rsettings.getProperties() );
			r.addEnv( renv );
		}
		
		return( r );
	}

	public MetaMonitoring getMonitoring() {
		return( mon );
	}
	
	public void addEnv( MetaEnv env ) {
		mapEnvs.put( env.NAME , env );
		mapEnvsById.put( env.ID , env );
	}
	
	public String[] getEnvNames() {
		List<String> names = new LinkedList<String>();
		for( MetaEnv env : mapEnvs.values() )
			names.add( env.NAME );
		Collections.sort( names );
		return( names.toArray( new String[0] ) );
	}

	public MetaEnv[] getEnvs() {
		return( mapEnvs.values().toArray( new MetaEnv[0] ) );
	}
	
	public void deleteEnv( EngineTransaction transaction , MetaEnv env ) throws Exception {
		String envFile = env.NAME + ".xml";
		mapEnvs.remove( env.NAME );
		
		ActionBase action = transaction.getAction();
		ProductStorage storage = action.artefactory.getMetadataStorage( action , env.meta );
		storage.deleteEnvConfFile( action , envFile );
		env.deleteObject();
	}

	public void deleteHostAccount( EngineTransaction transaction , HostAccount account ) throws Exception {
		for( MetaEnv env : mapEnvs.values() )
			env.deleteHostAccount( transaction , account );
	}

	public void deleteBinaryItemFromEnvironments( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		for( MetaEnv env : getEnvs() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteBinaryItem( transaction , item );
	}

	public void deleteConfItemFromEnvironments( EngineTransaction transaction , MetaDistrConfItem item ) throws Exception {
		for( MetaEnv env : getEnvs() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteConfItem( transaction , item );
	}

	public void deleteComponentFromEnvironments( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
		for( MetaEnv env : getEnvs() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteComponent( transaction , item );
	}

	public void deleteDatabaseSchemaFromEnvironments( EngineTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnv env : getEnvs() )
			for( MetaEnvSegment sg : env.getSegments() )
				for( MetaEnvServer server : sg.getServers() )
					server.reflectDeleteSchema( transaction , schema );
	}

	public MetaEnv findMetaEnv( int id ) {
		return( mapEnvsById.get( id ) );
	}

	public MetaEnv getMetaEnv( int id ) throws Exception {
		MetaEnv env = mapEnvsById.get( id );
		if( env == null )
			Common.exitUnexpected();
		return( env );
	}

	public MetaEnv findMetaEnv( String name ) {
		return( mapEnvs.get( name ) );
	}

	public MetaEnv getMetaEnv( String name ) throws Exception {
		MetaEnv env = mapEnvs.get( name );
		if( env == null )
			Common.exitUnexpected();
		return( env );
	}

    public MetaEnv findMetaEnv( MetaEnv env ) {
    	if( env == null )
    		return( null );
    	return( findMetaEnv( env.NAME ) );
    }
    
	public MetaEnv findMetaEnv( MatchItem env ) throws Exception {
		if( env == null )
			return( null );
		if( env.MATCHED )
			return( findMetaEnv( env.FKID ) );
		return( findMetaEnv( env.FKNAME ) );
	}
	
	public MetaEnv getMetaEnv( MatchItem env ) throws Exception {
		if( env == null )
			return( null );
		if( env.MATCHED )
			return( getMetaEnv( env.FKID ) );
		return( getMetaEnv( env.FKNAME ) );
	}
	
    public MetaEnvSegment findMetaEnvSegment( MetaEnvSegment sg ) {
    	if( sg == null )
    		return( null );
    	MetaEnv env = findMetaEnv( sg.env );
    	if( env == null )
    		return( null );
    	return( env.findSegment( sg.NAME ) );
    }
    
    public MetaEnvServer findMetaEnvServer( MetaEnvServer server ) {
    	if( server == null )
    		return( null );
    	MetaEnvSegment sg = findMetaEnvSegment( server.sg );
    	if( sg == null )
    		return( null );
    	return( sg.findServer( server.NAME ) );
    }

    public MetaEnvServerNode getMetaEnvServerNode( MetaEnvServerNode node ) {
    	if( node == null )
    		return( null );
    	MetaEnvServer server = findMetaEnvServer( node.server );
    	if( server == null )
    		return( null );
    	return( server.findNode( node.POS ) );
    }

	public void getApplicationReferences( HostAccount account , List<AccountReference> refs ) {
		for( MetaEnv env : getEnvs() )
			env.getApplicationReferences( account , refs );
	}

}