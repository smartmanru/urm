package org.urm.meta.product;

import org.urm.engine.products.EngineProduct;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaMonitoring;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.release.ReleaseRepository;

public class ProductMeta extends EngineObject {

	public EngineProduct ep;
	
	public Meta meta;
	public Integer ID;
	public String NAME;
	public boolean DRAFT;
	public String REVISION;
	public int PV;
	public boolean MATCHED;
	
	private MetaProductSettings settings;
	private MetaUnits units;
	private MetaDatabase database;
	private MetaSources sources;
	private MetaDocs docs;
	private MetaDistr distr;
	
	private ProductEnvs envs;
	private ReleaseRepository releases;

	private boolean primary;
	
	public ProductMeta( EngineProduct ep ) {
		super( null );
		this.ep = ep;
		this.NAME = ep.productName;
		
		meta = new Meta( ep , this , null );
		ID = null;
		PV = -1;
		MATCHED = false;
		DRAFT = true;
		
		primary = false;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public synchronized ProductMeta copy( ObjectProperties opsParent ) throws Exception {
		ProductMeta r = new ProductMeta( ep );
		
		r.ID = ID;
		r.NAME = NAME;
		r.DRAFT = DRAFT;
		r.REVISION = REVISION;
		r.PV = PV;
		r.MATCHED = MATCHED;
		
		r.settings = settings.copy( r.meta , opsParent );
		r.units = units.copy( r.meta );
		r.database = database.copy( r.meta );
		r.sources = sources.copy( r.meta );
		r.docs = docs.copy( r.meta );
		r.distr = distr.copy( r.meta );
		
		r.envs = envs.copy( r.meta );
		r.envs.copyResolveExternals();
		r.releases = releases.copy( r.meta );
		
		return( r );
	}

	public void create( String revision , boolean draft , boolean matched ) {
		this.REVISION = revision;
		this.DRAFT = draft;
		this.MATCHED = matched;
	}
	
	public EngineProduct getEngineProduct() {
		return( ep );
	}
	
	public AppProduct getProduct() throws Exception {
		return( ep.getProduct() );
	}
	
	public AppProduct findProduct() {
		return( ep.findProduct() );
	}
	
	public void setMatched( boolean matched ) {
		this.MATCHED = matched;
	}
	
	public void setRevision( String revision ) {
		this.REVISION = revision;
	}
	
	public boolean isExists() {
		if( ID != null )
			return( true );
		return( false );
	}
	
	public void setPrimary( boolean primary ) {
		this.primary = primary;
	}
	
	public boolean isPrimary() {
		return( primary );
	}
	
	public boolean isDraft() {
		return( DRAFT );
	}
	
	public ReleaseRepository getReleaseRepository() {
		return( releases );
	}
	
	public void setReleaseRepository( ReleaseRepository repo ) {
		releases = repo;
	}
	
	public void setSettings( MetaProductSettings settings ) throws Exception {
		this.settings = settings;
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

	public MetaProductSettings getSettings() {
		return( settings );
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

	public MetaMonitoring getMonitoring() {
		return( envs.getMonitoring() );
	}
	
	public boolean isMatched() {
		return( MATCHED );
	}

	public void deleteEnvObjects() {
		if( envs == null )
			return;
		
		for( MetaEnv env : envs.getEnvs() )
			env.deleteObject();
	}
	
}
