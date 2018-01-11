package org.urm.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.engine.EngineSession;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDesignDiagram;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaUnits;

public class ProductMeta extends EngineObject {

	public Engine engine;
	public EngineProducts products;
	public String name;
	
	public Meta meta;
	
	private MetaProductVersion version;
	private MetaProductSettings settings;
	private MetaUnits units;
	private MetaDatabase database;
	private MetaSource sources;
	private MetaDocs docs;
	private MetaDistr distr;
	private MetaMonitoring mon;

	private DistRepository repo;
	
	private Map<String,MetaEnv> envs;
	private Map<String,MetaDesignDiagram> diagrams;
	
	private Map<EngineSession,Meta> sessionMeta;
	private boolean primary;
	
	public ProductMeta( EngineProducts products , String name ) {
		super( null );
		this.products = products;
		this.engine = products.engine;
		this.name = name;
		
		meta = new Meta( this , null );
		engine.trace( "new product storage meta object, id=" + meta.objectId + ", storage=" + objectId );
		diagrams = new HashMap<String,MetaDesignDiagram>();
		envs = new HashMap<String,MetaEnv>();
		
		sessionMeta = new HashMap<EngineSession,Meta>();
		primary = false;
	}

	@Override
	public String getName() {
		return( name );
	}
	
	public synchronized ProductMeta copy( ActionBase action ) throws Exception {
		ProductMeta r = new ProductMeta( products , name );
		
		if( version != null )
			r.version = version.copy( action , r.meta );
		if( settings != null )
			r.settings = settings.copy( action , r.meta );
		if( units != null )
			r.units = units.copy( action , r.meta );
		if( database != null )
			r.database = database.copy( action , r.meta );
		if( sources != null )
			r.sources = sources.copy( action , r.meta );
		if( docs != null )
			r.docs = docs.copy( action , r.meta );
		if( distr != null )
			r.distr = distr.copy( action , r.meta , r.database , r.docs );
		if( mon != null )
			r.mon = mon.copy( action , r.meta );
		
		for( String envKey : envs.keySet() ) {
			MetaEnv env = envs.get( envKey );
			MetaEnv re = env.copy( action , r.meta );
			r.envs.put( envKey , re );
		}
		
		for( String name : diagrams.keySet() ) {
			MetaDesignDiagram design = diagrams.get( name );
			MetaDesignDiagram rd = design.copy( action , r.meta );
			r.diagrams.put( name , rd );
		}
		
		r.repo = repo.copy( action , r.meta );
		return( r );
	}

	public DistRepository getDistRepository( ActionBase action ) {
		return( repo );
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
	
	public synchronized void createInitial( TransactionBase transaction , EngineSettings settings , AppProduct product ) throws Exception {
		createInitialVersion( transaction );
		createInitialProduct( transaction , settings , product );
		createInitialUnits( transaction );
		createInitialDatabase( transaction );
		createInitialSources( transaction );
		createInitialDocs( transaction );
		createInitialDistr( transaction );
		createInitialMonitoring( transaction );
	}

	private void createInitialVersion( TransactionBase transaction ) throws Exception {
		version = new MetaProductVersion( this , meta );
		version.createVersion( transaction , 1 , 0 , 1 , 1 , 1 , 2 );
		meta.setVersion( version );
	}
	
	private void createInitialProduct( TransactionBase transaction , EngineSettings engineSettings , AppProduct product ) throws Exception {
		ObjectProperties systemProps = product.system.getParameters();
		settings = new MetaProductSettings( this , meta , systemProps.getProperties() );
		
		ProductContext productContext = new ProductContext( meta );
		productContext.create( transaction.action , version );
		
		settings.createSettings( transaction , engineSettings , productContext );
		meta.setProduct( settings );
	}
	
	private void createInitialUnits( TransactionBase transaction ) throws Exception {
		units = new MetaUnits( this , settings , meta );
		units.createUnits( transaction );
		meta.setUnits( units );
	}
	
	private void createInitialDatabase( TransactionBase transaction ) throws Exception {
		database = new MetaDatabase( this , settings , meta );
		database.createDatabase( transaction );
		meta.setDatabase( database );
	}
	
	private void createInitialDocs( TransactionBase transaction ) throws Exception {
		docs = new MetaDocs( this , settings , meta );
		docs.createDocs( transaction );
		meta.setDocs( docs );
	}
	
	private void createInitialDistr( TransactionBase transaction ) throws Exception {
		distr = new MetaDistr( this , settings , meta );
		distr.createDistr( transaction );
		meta.setDistr( distr );
	}
	
	private void createInitialSources( TransactionBase transaction ) throws Exception {
		sources = new MetaSource( this , settings , meta );
		sources.createSources( transaction );
		meta.setSources( sources );
	}
	
	private void createInitialMonitoring( TransactionBase transaction ) throws Exception {
		mon = new MetaMonitoring( this , settings , meta );
		mon.createMonitoring( transaction );
	}

	public void createInitialRepository( TransactionBase transaction , boolean forceClear ) throws Exception {
		repo = DistRepository.createInitialRepository( transaction.action , meta , forceClear );
	}

	public DistRepository getDistRepository() {
		return( repo );
	}
	
	public void setVersion( MetaProductVersion version ) throws Exception {
		if( this.version != null )
			this.version.deleteObject();
		this.version = version;
	}

	public void setSettings( MetaProductSettings settings ) throws Exception {
		if( this.settings != null )
			this.settings.deleteObject();
		this.settings = settings;
	}

	public void setUnits( MetaUnits units ) throws Exception {
		if( this.units != null )
			this.units.deleteObject();
		this.units = units;
	}

	public void setDatabase( MetaDatabase database ) throws Exception {
		if( this.database != null )
			this.database.deleteObject();
		this.database = database;
	}

	public void setSources( MetaSource sources ) throws Exception {
		if( this.sources != null )
			this.sources.deleteObject();
		this.sources = sources;
	}

	public void setDocs( MetaDocs docs ) throws Exception {
		if( this.docs != null )
			this.docs.deleteObject();
		this.docs = docs;
	}

	public void setDistr( MetaDistr distr ) throws Exception {
		if( this.distr != null )
			this.distr.deleteObject();
		this.distr = distr;
	}

	public void setMonitoring( MetaMonitoring mon ) throws Exception {
		if( this.mon != null )
			this.mon.deleteObject();
		this.mon = mon;
	}

	public void addEnv( MetaEnv env ) throws Exception {
		envs.put( env.NAME , env );
	}

	public void addDiagram( MetaDesignDiagram diagram ) throws Exception {
		diagrams.put( diagram.NAME , diagram );
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
	
	public MetaSource getSources() {
		return( sources );
	}
	
	public MetaMonitoring getMonitoring() {
		return( mon );
	}
	
	public String[] getEnvironmentNames() {
		List<String> names = new LinkedList<String>();
		for( MetaEnv env : envs.values() )
			names.add( env.NAME );
		Collections.sort( names );
		return( names.toArray( new String[0] ) );
	}

	public MetaEnv findEnvironment( String envId ) {
		for( MetaEnv env : envs.values() ) {
			if( env.NAME.equals( envId ) )
				return( env );
		}
		return( null );
	}

	public String[] getDiagramNames() {
		return( Common.getSortedKeys( diagrams ) );
	}
	
	public MetaDesignDiagram findDiagram( String diagramName ) {
		for( MetaDesignDiagram diagram : diagrams.values() ) {
			if( diagram.NAME.equals( diagramName ) )
				return( diagram );
		}
		return( null );
	}

	public MetaEnv[] getEnvironments() {
		return( envs.values().toArray( new MetaEnv[0] ) );
	}
	
	public void deleteEnv( EngineTransaction transaction , MetaEnv env ) throws Exception {
		String envFile = env.NAME + ".xml";
		envs.remove( envFile );
		ActionBase action = transaction.getAction();
		MetadataStorage storage = action.artefactory.getMetadataStorage( action , env.meta );
		storage.deleteEnvConfFile( action , envFile );
		env.deleteObject();
	}

	public void deleteHostAccount( EngineTransaction transaction , HostAccount account ) throws Exception {
		for( MetaEnv env : envs.values() )
			env.deleteHostAccount( transaction , account );
	}

}
