package org.urm.meta.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.engine.DataService;
import org.urm.engine.data.EngineLifecycles;

public class AppProductPolicy {

	public static String PROPERTY_RELEASELC_MAJOR = "minor";
	public static String PROPERTY_RELEASELC_MINOR = "major";
	public static String PROPERTY_RELEASELC_URGENTANY = "urgentany";
	public static String PROPERTY_RELEASELC_URGENTS = "urgentset";

	public AppProduct product;
	
	private Integer LC_MAJOR;
	private Integer LC_MINOR;
	public boolean LC_URGENT_All;
	private Integer[] LC_URGENT_LIST;
	public int SV;
	
	public AppProductPolicy( AppProduct product ) {
		this.product = product;
		LC_URGENT_All = false;
		LC_URGENT_LIST = new Integer[0];
	}

	public AppProductPolicy copy( AppProduct rproduct ) {
		AppProductPolicy r = new AppProductPolicy( rproduct );
		
		// stored
		r.LC_MAJOR = LC_MAJOR;
		r.LC_MINOR = LC_MINOR;
		r.LC_URGENT_All = LC_URGENT_All;
		r.LC_URGENT_LIST = LC_URGENT_LIST.clone();
		r.SV = SV;
		
		return( r );
	}

	public boolean isAnyUrgent() {
		return( LC_URGENT_All );
	}
	
	public boolean isNoUrgents() {
		if( LC_URGENT_All || LC_URGENT_LIST.length > 0 )
			return( false );
		return( true );
	}
	
	public void setAttrs( boolean urgentsAll ) {
		LC_URGENT_All = urgentsAll;
	}
	
	public void setLifecycles( ReleaseLifecycle major , ReleaseLifecycle minor , ReleaseLifecycle[] urgents ) throws Exception {
		LC_MAJOR = null;
		if( major != null ) {
			if( !major.isMajor() )
				Common.exitUnexpected();
			LC_MAJOR = major.ID;
		}
		
		LC_MINOR = null;
		if( minor != null ) {
			if( !minor.isMinor() )
				Common.exitUnexpected();
			LC_MINOR = minor.ID;
		}
		
		LC_URGENT_LIST = new Integer[ urgents.length ];
		for( int k = 0; k < urgents.length; k++ ) {
			if( !urgents[ k ].isUrgent() )
				Common.exitUnexpected();
			
			LC_URGENT_LIST[ k ] = urgents[ k ].ID;
		}
	}

	public String getMajorName() throws Exception {
		if( LC_MAJOR == null )
			return( "" );
		ReleaseLifecycle lc = findLifecycle( LC_MAJOR );
		return( lc.NAME );
	}

	public String getMinorName() throws Exception {
		if( LC_MINOR == null )
			return( "" );
		ReleaseLifecycle lc = findLifecycle( LC_MINOR );
		return( lc.NAME );
	}

	public Integer getMajorId() {
		return( LC_MAJOR );
	}
	
	public Integer getMinorId() {
		return( LC_MINOR );
	}
	
	public Integer[] getUrgentIds() {
		return( LC_URGENT_LIST );
	}
	
	private ReleaseLifecycle findLifecycle( int id ) {
		DataService data = product.directory.engine.getData();
		EngineLifecycles lifecycles = data.getReleaseLifecycles();
		return( lifecycles.findLifecycle( id ) );
	}
	
	public boolean checkUrgentIncluded( ReleaseLifecycle lc ) {
		if( lc.isUrgent() && LC_URGENT_All )
			return( true );
		for( Integer item : LC_URGENT_LIST ) {
			if( item == lc.ID )
				return( true );
		}
		return( false );
	}
	
	public String[] getUrgentNames() {
		String[] names = new String[ LC_URGENT_LIST.length ];
		for( int k = 0; k < names.length; k++ ) {
			ReleaseLifecycle lc = findLifecycle( LC_URGENT_LIST[ k ] );
			names[ k ] = lc.NAME;
		}
		return( Common.getSortedList( names ) );
	}
	
	public ReleaseLifecycle findLifecycle( DBEnumLifecycleType lctype ) {
		if( lctype == DBEnumLifecycleType.MAJOR ) {
			if( LC_MAJOR == null )
				return( null );
			return( findLifecycle( LC_MAJOR ) );
		}
		if( lctype == DBEnumLifecycleType.MINOR ) {
			if( LC_MINOR == null )
				return( null );
			return( findLifecycle( LC_MINOR ) );
		}
		return( null );
	}

	public boolean hasMajor() {
		if( LC_MAJOR != null )
			return( true );
		return( false );
	}

	public boolean hasMinor() {
		if( LC_MINOR != null )
			return( true );
		return( false );
	}

	public ReleaseLifecycle getLifecycle( ActionBase action , ReleaseLifecycle lc , DBEnumLifecycleType type ) throws Exception {
		if( type == DBEnumLifecycleType.MAJOR ) {
			Integer expected = LC_MAJOR;
			if( expected == null ) {
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					if( expected != lc.ID )
						action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" + lc.NAME , lc.NAME );
					return( lc );
				}
				
				EngineLifecycles lifecycles = action.getEngineLifecycles();
				return( lifecycles.getLifecycle( expected ) );
			}
		}
		else
		if( type == DBEnumLifecycleType.MINOR ) {
			Integer expected = LC_MINOR;
			if( expected == null ) {
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					if( expected != lc.ID )
						action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" + lc.NAME , lc.NAME );
					return( lc );
				}
				
				EngineLifecycles lifecycles = action.getEngineLifecycles();
				return( lifecycles.getLifecycle( expected ) );
			}
		}
		else
		if( type == DBEnumLifecycleType.URGENT ) {
			Integer[] expected = LC_URGENT_LIST;
			if( expected.length == 0 ) {
				if( !LC_URGENT_All )
					return( null );
					
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					for( int k = 0; k < expected.length; k++ ) {
						if( expected[ k ] == lc.ID )
							return( lc );
					}
					action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" + lc.NAME , lc.NAME );
				}
				
				action.exit0( _Error.MissingReleasecycleType0 , "Missing release cycle type" );
			}
		}
		
		return( null );
	}

	public String[] getAvailableLifecycles( EngineLifecycles lifecycles , DBEnumLifecycleType type ) {
		List<String> names = new LinkedList<String>();
		for( String name : lifecycles.getLifecycleNames() ) {
			ReleaseLifecycle lc = lifecycles.findLifecycle( name );
			if( !lc.ENABLED )
				continue;
			
			if( lc.isMajor() ) {
				if( type == null || type == DBEnumLifecycleType.MAJOR ) {
					Integer majorId = getMajorId();
					if( majorId != null ) {
						if( majorId != lc.ID )
							continue;
					}
					names.add( name );
				}
			}
			else
			if( lc.isMinor() ) {
				if( type == null || type == DBEnumLifecycleType.MINOR ) {
					// all or policy
					Integer minorId = getMinorId();
					if( minorId != null ) {
						if( minorId != lc.ID )
							continue;
					}
					names.add( name );
				}
			}
			else
			if( lc.isUrgent() ) {
				if( type == null || type == DBEnumLifecycleType.URGENT ) {
					// all or policy
					if( !checkUrgentIncluded( lc ) )
						continue;
					
					names.add( name );
				}
			}
		}
		
		return( names.toArray( new String[0] ) );
	}

	public boolean checkLifecycleRequired( DBEnumLifecycleType type ) {
		if( type == DBEnumLifecycleType.MAJOR && LC_MAJOR != null )
			return( true );
		if( type == DBEnumLifecycleType.MINOR && LC_MINOR != null )
			return( true );
		if( type == DBEnumLifecycleType.URGENT && !LC_URGENT_All )
			return( true );
		return( false );
	}
	
}
