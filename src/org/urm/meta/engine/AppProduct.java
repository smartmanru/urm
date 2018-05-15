package org.urm.meta.engine;

import org.urm.common.Common;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductEnvs;
import org.urm.engine.products.EngineProductReleases;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.meta.loader.EngineObject;

public class AppProduct extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_PATH = "path";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_MONITORING_ENABLED = "monitoring.enabled";
	public static String PROPERTY_LAST_MAJOR_FIRST = "major.first";
	public static String PROPERTY_LAST_MAJOR_SECOND = "major.last";
	public static String PROPERTY_NEXT_MAJOR_FIRST = "next.major.first";
	public static String PROPERTY_NEXT_MAJOR_SECOND = "next.major.last";
	public static String PROPERTY_LAST_MINOR_FIRST = "prod.lasttag";
	public static String PROPERTY_LAST_MINOR_SECOND = "prod.lasturgent";
	public static String PROPERTY_NEXT_MINOR_FIRST = "prod.nexttag";
	public static String PROPERTY_NEXT_MINOR_SECOND = "prod.nexturgent";
	
	public EngineDirectory directory;
	public AppSystem system;

	public int ID;
	public String NAME;
	public String DESC;
	public String PATH;
	public boolean OFFLINE;
	public boolean MONITORING_ENABLED;
	public int LAST_MAJOR1;
	public int LAST_MAJOR2;
	public int LAST_MINOR1;
	public int LAST_MINOR2;
	public int NEXT_MAJOR1;
	public int NEXT_MAJOR2;
	public int NEXT_MINOR1;
	public int NEXT_MINOR2;

	public int SV;
	
	public AppProductPolicy policy;
	private boolean matched;
	
	public AppProduct( EngineDirectory directory , AppSystem system ) {
		super( directory );
		this.directory = directory;
		this.system = system;
		this.ID = -1;
		this.SV = 0;
		
		policy = new AppProductPolicy( directory , this );
		matched = false;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}

	public void setPolicy( AppProductPolicy policy ) {
		this.policy = policy;
	}
	
	public AppProduct copy( EngineDirectory nr , AppSystem rs ) {
		AppProduct r = new AppProduct( nr , rs );
		
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.PATH = PATH;
		r.OFFLINE = OFFLINE;
		r.MONITORING_ENABLED = MONITORING_ENABLED;
		r.LAST_MAJOR1 = LAST_MAJOR1;
		r.LAST_MAJOR2 = LAST_MAJOR2;
		r.NEXT_MAJOR1 = NEXT_MAJOR1;
		r.NEXT_MAJOR2 = NEXT_MAJOR2;
		r.LAST_MINOR1 = LAST_MINOR1;
		r.NEXT_MINOR1 = NEXT_MINOR1;
		r.LAST_MINOR2 = LAST_MINOR2;
		r.NEXT_MINOR2 = NEXT_MINOR2;
		r.SV = SV;
		
		r.policy = policy.copy( nr ,  r );
		
		return( r );
	}
	
	public AppProductPolicy getPolicy() {
		return( policy );
	}
	
	public boolean isValid() {
		if( ( LAST_MAJOR1 > NEXT_MAJOR1 ) || 
			( LAST_MAJOR1 == NEXT_MAJOR1 && LAST_MAJOR2 >= NEXT_MAJOR2 ) ||
			( LAST_MINOR1 >= NEXT_MINOR1 ) ||
			( LAST_MINOR2 >= NEXT_MINOR2 ) )
			return( false );
		
		return( true );
	}
	
	public void setMatched( boolean matched ) {
		this.matched = matched;
	}
	
	public boolean isMatched() {
		return( matched );
	}
	
	public void createProduct( String name , String desc , String path ) throws Exception {
		modifyProduct( name , desc , path );
		OFFLINE = true;
		MONITORING_ENABLED = false;
		
		this.LAST_MAJOR1 = 1;
		this.LAST_MAJOR2 = 0;
		this.LAST_MINOR1 = 0;
		this.LAST_MINOR2 = 0;
		this.NEXT_MAJOR1 = 1;
		this.NEXT_MAJOR2 = 1;
		this.NEXT_MINOR1 = 1;
		this.NEXT_MINOR2 = 1;
	}

	public void modifyProduct( String name , String desc , String path ) throws Exception {
		this.NAME = name;
		this.DESC = Common.nonull( desc );
		this.PATH = path;
	}
	
	public boolean isOffline() {
		return( OFFLINE );
	}

	public boolean isBroken() {
		return( !isMatched() );
	}
	
	public void setOffline( boolean offline ) throws Exception {
		this.OFFLINE = offline;
	}

	public void setMonitoringEnabled( boolean enabled ) throws Exception {
		this.MONITORING_ENABLED = enabled;
	}

	public void setVersions( int majorLastFirstNumber , int majorLastSecondNumber , int lastProdTag , int lastUrgentTag , int majorNextFirstNumber , int majorNextSecondNumber , int nextProdTag , int nextUrgentTag ) throws Exception {
		this.LAST_MAJOR1 = majorLastFirstNumber;
		this.LAST_MAJOR2 = majorLastSecondNumber;
		this.NEXT_MAJOR1 = majorNextFirstNumber;
		this.NEXT_MAJOR2 = majorNextSecondNumber;
		this.LAST_MINOR1 = lastProdTag;
		this.NEXT_MINOR1 = nextProdTag;
		this.LAST_MINOR2 = lastUrgentTag;
		this.NEXT_MINOR2 = nextUrgentTag;
		
		if( !isValid() )
			Common.exit0( _Error.InconsistentVersionAttributes0 , "Inconsistent version attributes" );
	}

	public EngineProduct getEngineProduct() throws Exception {
		EngineProduct ep = directory.findEngineProduct( this );
		if( ep == null )
			Common.exitUnexpected();
		return( ep );
	}
	
	public EngineProduct findEngineProduct() {
		return( directory.findEngineProduct( this ) );
	}
	
	public EngineProductReleases findReleases() {
		EngineProduct ep = findEngineProduct();
		if( ep == null )
			return( null );
		return( ep.getReleases() ); 
	}
	
	public EngineProductEnvs findEnvs() {
		EngineProduct ep = findEngineProduct();
		if( ep == null )
			return( null );
		return( ep.getEnvs() ); 
	}
	
	public EngineProductRevisions findRevisions() {
		EngineProduct ep = findEngineProduct();
		if( ep == null )
			return( null );
		return( ep.getRevisions() ); 
	}
	
}
