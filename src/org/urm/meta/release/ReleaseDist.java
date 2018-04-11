package org.urm.meta.release;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.dist.VersionInfo;

public class ReleaseDist {

	public static String PROPERTY_VARIANT = "variant";

	public Release release;
	
	public int ID;
	public String DIST_VARIANT;
	public Date DIST_DATE;
	public String META_HASH;
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
		r.META_HASH = META_HASH;
		r.DATA_HASH = DATA_HASH;
		r.DIST_DATE = DIST_DATE;
		r.DIST_VARIANT = DIST_VARIANT;
		
		for( ReleaseDistItem item : itemMap.values() ) {
			ReleaseDistItem ritem = item.copy( rrelease , r );
			r.addDistItem( ritem );
		}
		
		return( r );
	}

	public void create( String DIST_VARIANT ) {
		create( DIST_VARIANT , Common.getDateCurrentDay() , "unknown" , "empty" );
	}
	
	public void create( String DIST_VARIANT , Date DIST_DATE , String META_HASH , String DATA_HASH ) {
		this.DIST_VARIANT = DIST_VARIANT;
		this.DIST_DATE = DIST_DATE;
		this.META_HASH = META_HASH;
		this.DATA_HASH = DATA_HASH;
	}

	public void setHash( String META_HASH , String DATA_HASH ) {
		this.META_HASH = META_HASH;
		this.DATA_HASH = DATA_HASH;
	}
	
	public boolean isDefault() {
		if( DIST_VARIANT.isEmpty() )
			return( true );
		return( false );
	}
	
	public void addDistItem( ReleaseDistItem item ) {
		itemMap.put( item.ID ,  item );
	}
	
	public ReleaseDistItem findTarget( ReleaseDistTarget target ) {
		for( ReleaseDistItem item : itemMap.values() ) {
			if( item.DISTTARGET_ID == target.ID )
				return( item );
		}
		return( null );
	}
	
	public String getReleaseDir() {
		String shortVersion = VersionInfo.getReleaseShortVersion( release.RELEASEVER );
		if( DIST_VARIANT.isEmpty() )
			return( shortVersion );
		return( shortVersion + "-" + DIST_VARIANT );
	}
	
	public void clear() {
		itemMap.clear();
	}

	public ReleaseDistItem[] getDistItems() {
		return( itemMap.values().toArray( new ReleaseDistItem[0] ) );
	}
	
}
