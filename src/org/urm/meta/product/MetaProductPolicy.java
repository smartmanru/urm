package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.ReleaseLifecycle;

public class MetaProductPolicy {

	public static String PROPERTY_RELEASELC_MAJOR = "minor";
	public static String PROPERTY_RELEASELC_MINOR = "major";
	public static String PROPERTY_RELEASELC_URGENTANY = "urgentany";
	public static String PROPERTY_RELEASELC_URGENTS = "urgentset";

	public Meta meta;
	
	public MatchItem LC_MAJOR;
	public MatchItem LC_MINOR;
	public boolean LCUrgentAll;
	public MatchItem[] LC_URGENT_LIST;
	public int PV;
	
	public MetaProductPolicy( ProductMeta storage , Meta meta ) {
		LCUrgentAll = false;
		LC_URGENT_LIST = new MatchItem[0];
	}

	public MetaProductPolicy copy( Meta rmeta ) throws Exception {
		MetaProductPolicy r = new MetaProductPolicy( rmeta.getStorage() , rmeta );
		
		// stored
		r.LC_MAJOR = MatchItem.copy( LC_MAJOR );
		r.LC_MINOR = LC_MINOR;
		r.LCUrgentAll = LCUrgentAll;
		r.LC_URGENT_LIST = new MatchItem[ LC_URGENT_LIST.length ];
		for( int k = 0; k < LC_URGENT_LIST.length; k++ )
			r.LC_URGENT_LIST[ k ] = MatchItem.copy( LC_URGENT_LIST[ k ] );
		
		return( r );
	}

	public void setAttrs( boolean urgentsAll ) {
		LCUrgentAll = urgentsAll;
	}
	
	public void setLifecycles( MatchItem major , MatchItem minor , MatchItem[] urgents ) throws Exception {
		LC_MAJOR = major;
		LC_MINOR = minor;
		LC_URGENT_LIST = urgents.clone();
	}

	public String getMajorName( ActionBase action ) throws Exception {
		return( getLifecycleName( action , LC_MAJOR ) );
	}

	public String getMinorName( ActionBase action ) throws Exception {
		return( getLifecycleName( action , LC_MINOR ) );
	}

	public String[] getUrgentNames( ActionBase action ) throws Exception {
		String[] names = new String[ LC_URGENT_LIST.length ];
		for( int k = 0; k < names.length; k++ )
			names[ k ] = getLifecycleName( action , LC_URGENT_LIST[ k ] );
		return( Common.getSortedList( names ) );
	}
	
	public String getLifecycleName( ActionBase action , MatchItem item ) throws Exception {
		EngineLifecycles lifecycles = action.getServerReleaseLifecycles();
		ReleaseLifecycle lc = lifecycles.getLifecycle( item.FKID );
		return( lc.NAME );
	}

}
