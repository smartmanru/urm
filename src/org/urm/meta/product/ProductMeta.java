package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.engine.EngineSession;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.env.ProductEnvs;

public class ProductMeta extends EngineObject {

	public Engine engine;
	public EngineProducts products;
	public AppProduct product;
	public String name;
	
	public Meta meta;
	public Integer ID;
	public int PV;
	public boolean MATCHED;
	
	private MetaProductVersion version;
	private MetaProductSettings settings;
	private MetaProductPolicy policy;
	private MetaUnits units;
	private MetaDatabase database;
	private MetaSources sources;
	private MetaDocs docs;
	private MetaDistr distr;
	private ProductEnvs envs;

	private DistRepository repo;
	
	private Map<EngineSession,Meta> sessionMeta;
	private boolean primary;
	
	public ProductMeta( EngineProducts products , AppProduct product ) {
		super( null );
		this.products = products;
		this.engine = products.engine;
		this.product = product;
		this.name = product.NAME;
		
		meta = new Meta( this , null );
		ID = null;
		PV = -1;
		MATCHED = false;
		engine.trace( "new product storage meta object, id=" + meta.objectId + ", storage=" + objectId );
		
		sessionMeta = new HashMap<EngineSession,Meta>();
		primary = false;
	}

	@Override
	public String getName() {
		return( name );
	}
	
	public synchronized ProductMeta copy( ActionBase action , EngineProducts rproducts , AppProduct rproduct , ObjectProperties opsParent ) throws Exception {
		ProductMeta r = new ProductMeta( products , rproduct );
		
		r.ID = ID;
		r.PV = PV;
		r.MATCHED = MATCHED;
		
		r.version = version.copy( r.meta );
		
		r.settings = settings.copy( r.meta , opsParent );
		r.policy = policy.copy( r.meta );
		r.units = units.copy( r.meta );
		r.database = database.copy( r.meta );
		r.sources = sources.copy( r.meta );
		r.docs = docs.copy( r.meta );
		r.distr = distr.copy( r.meta );
		
		r.envs = envs.copy( action , r.meta );
		r.envs.copyResolveExternals();
		
		r.repo = repo.copy( action , r.meta );
		return( r );
	}

	public void setMatched( boolean matched ) {
		this.MATCHED = matched;
	}
	
	public void setContext( ProductContext context ) {
		this.ID = context.ID;
		this.PV = context.PV;
	}
	
	public boolean isExists() {
		if( ID != null )
			return( true );
		return( false );
	}
	
	public void setPrimary( boolean primary ) {
		this.primary = primary;
	}
	
	public synchronized void addSessionMeta( Meta meta ) {
		sessionMeta.put( meta.session , meta );
	}
	
	public synchronized void releaseSessionMeta( Meta meta ) {
		sessionMeta.remove( meta.session );
	}

	public synchronized Meta findSessionMeta( EngineSession session ) {
		return( sessionMeta.get( session ) );
	}

	public synchronized boolean isReferencedBySessions() {
		if( sessionMeta.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean isPrimary() {
		return( primary );
	}
	
	public DistRepository getDistRepository() {
		return( repo );
	}
	
	public void setVersion( MetaProductVersion version ) throws Exception {
		this.version = version;
	}

	public void setSettings( MetaProductSettings settings ) throws Exception {
		this.settings = settings;
	}

	public void setPolicy( MetaProductPolicy policy ) throws Exception {
		this.policy = policy;
	}

	public void setUnits( MetaUnits units ) throws Exception {
		this.units = units;
	}

	public void setDatabase( MetaDatabase database ) throws Exception {
		this.database = database;
	}

	public void setSources( MetaSources sources ) throws Exception {
		this.sources = sources;
	}

	public void setDocs( MetaDocs docs ) throws Exception {
		this.docs = docs;
	}

	public void setDistr( MetaDistr distr ) throws Exception {
		this.distr = distr;
	}

	public void setEnvs( ProductEnvs envs ) throws Exception {
		this.envs = envs;
	}

	public void setReleases( DistRepository repo ) throws Exception {
		this.repo = repo;
	}
	
	public MetaProductVersion getVersion() {
		return( version );
	}
	
	public MetaProductSettings getSettings() {
		return( settings );
	}
	
	public MetaProductPolicy getPolicy() {
		return( policy );
	}
	
	public MetaUnits getUnits() {
		return( units );
	}
	
	public MetaDatabase getDatabase() {
		return( database );
	}
	
	public MetaDocs getDocs() {
		return( docs );
	}
	
	public MetaDistr getDistr() {
		return( distr );
	}
	
	public MetaSources getSources() {
		return( sources );
	}
	
	public ProductEnvs getEnviroments() {
		return( envs );
	}

	public boolean isMatched() {
		return( MATCHED );
	}
	
}
