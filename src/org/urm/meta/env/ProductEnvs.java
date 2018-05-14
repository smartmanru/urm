package org.urm.meta.env;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductEnvs;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.Meta;
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
		mon = new MetaMonitoring( this );
	}
	
	public ProductEnvs copy( Meta rmeta ) throws Exception {
		ProductEnvs r = new ProductEnvs( rmeta.getStorage() , rmeta );
		
		MetaProductSettings rsettings = rmeta.getProductSettings();
		for( MetaEnv env : mapEnvs.values() ) {
			MetaEnv renv = env.copy( rmeta.getStorage() , rmeta , r , rsettings.getParameters() );
			r.addEnv( renv );
		}

		for( MetaEnv renv : r.getEnvs() )
			renv.refreshProperties();
		
		r.mon = mon.copy( r );
		
		return( r );
	}

	public void copyResolveExternals() throws Exception {
		// resolve properties
		for( MetaEnv env : getEnvs() )
			env.copyResolveExternals();
	}

	public MetaEnv getProductEnv( MatchItem item ) throws Exception {
		EngineProduct ep = meta.getEngineProduct();
		EngineProductEnvs envs = ep.getEnvs();
		return( envs.getEnv( item ) );
	}
	
	public String getProductEnvName( MatchItem item ) throws Exception {
		if( item == null )
			return( "" );
		
		MetaEnv env = getProductEnv( item );
		return( env.NAME );
	}
	
	public MetaEnv getProductEnv( int id ) throws Exception {
		EngineProduct ep = meta.getEngineProduct();
		EngineProductEnvs envs = ep.getEnvs();
		return( envs.getEnv( id ) );
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
	
	public void deleteEnv( TransactionBase transaction , MetaEnv env ) throws Exception {
		mapEnvs.remove( env.NAME );
		mapEnvsById.remove( env.ID );
		env.deleteObject();
	}

	public MetaEnv findMetaEnv( int id ) {
		return( mapEnvsById.get( id ) );
	}

	public MetaEnv findMetaEnv( String name ) {
		return( mapEnvs.get( name ) );
	}

    public MetaEnv findMetaEnv( MetaEnv env ) {
    	if( env == null )
    		return( null );
    	return( findMetaEnv( env.NAME ) );
    }
    
	public MetaEnv findMetaEnv( MatchItem env ) {
		if( env == null )
			return( null );
		if( env.MATCHED )
			return( findMetaEnv( env.FKID ) );
		return( findMetaEnv( env.FKNAME ) );
	}
	
	public MetaEnvSegment findMetaEnvSegment( int id ) {
		for( MetaEnv env : mapEnvsById.values() ) {
			MetaEnvSegment sg = env.findSegment( id );
			if( sg != null )
				return( sg );
		}
		return( null );
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

	public MetaEnvServer findMetaEnvServer( int id ) {
		for( MetaEnv env : mapEnvsById.values() ) {
			MetaEnvServer server = env.findServer( id );
			if( server != null )
				return( server );
		}
		return( null );
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

	public boolean isEmpty() {
		return( mapEnvsById.isEmpty() );
	}
	
}
