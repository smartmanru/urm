package org.urm.meta.release;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;

public class ReleaseDist {

	public static String PROPERTY_HASH = "hash";
	public static String PROPERTY_DATE = "date";
	public static String PROPERTY_VARIANT = "variant";

	public Release release;
	
	public int ID;
	public String DIST_VARIANT;
	public Date DIST_DATE;
	public String DATA_HASH;
	public int RV;

	private Map<Integer,ReleaseDistItem> itemMap;
	
	public ReleaseDist( Release release ) {
		this.release = release;
		itemMap = new HashMap<Integer,ReleaseDistItem>();
	}
	
	public ReleaseDist copy( Release rrelease ) {
		ReleaseDist r = new ReleaseDist( rrelease );
		
		r.ID = ID;
		r.DATA_HASH = DATA_HASH;
		r.DIST_DATE = DIST_DATE;
		r.DIST_VARIANT = DIST_VARIANT;
		
		for( ReleaseDistItem item : itemMap.values() ) {
			ReleaseDistItem ritem = item.copy( rrelease , r );
			r.addItem( ritem );
		}
		
		return( r );
	}

	public void create( String DIST_VARIANT ) {
		create( DIST_VARIANT , Common.getDateCurrentDay() , "" );
	}
	
	public void create( String DIST_VARIANT , Date DIST_DATE , String DATA_HASH ) {
		this.DIST_VARIANT = DIST_VARIANT;
		this.DIST_DATE = DIST_DATE;
		this.DATA_HASH = DATA_HASH;
	}
	
	public void addItem( ReleaseDistItem item ) {
		itemMap.put( item.ID ,  item );
	}
	
	public ReleaseDistItem findTarget( ReleaseDistTarget target ) {
		for( ReleaseDistItem item : itemMap.values() ) {
			if( item.DELIVERYTARGET_ID == target.ID )
				return( item );
		}
		return( null );
	}
	
	public String getReleaseDir() {
		if( DIST_VARIANT.isEmpty() )
			return( release.RELEASEVER );
		return( release.RELEASEVER + "-" + DIST_VARIANT );
	}
	
	public void clear() {
		itemMap.clear();
	}

}
