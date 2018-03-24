package org.urm.meta.release;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.dist.VersionInfo;
import org.urm.meta.product.Meta;

public class ReleaseRepository {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_PRODUCT = "product";
	
	public Meta meta;
	public ProductReleases releases;
	
	public int ID;
	public String NAME;
	public String DESC;
	
	private Map<String,Release> mapReleases;
	
	public ReleaseRepository( Meta meta , ProductReleases releases ) {
		this.meta = meta;
		this.releases = releases;
		mapReleases = new HashMap<String,Release>();
	}

	public void createRepository( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}
	
	public Release findRelease( String RELEASEVER ) {
		try {
			String version = VersionInfo.normalizeReleaseVer( RELEASEVER );
			return( mapReleases.get( version ) );
		}
		catch( Throwable e ) {
			return( null );
		}
	}
	
	public synchronized String[] getActiveVersions() {
		return( VersionInfo.orderVersions( Common.getSortedKeys( mapReleases ) ) );
	}

	public Release getNextRelease( String RELEASEVER ) {
		try {
			String[] versions = getActiveVersions();
			String version = VersionInfo.normalizeReleaseVer( RELEASEVER );
			int index = Common.getIndexOf( versions , version );
			if( index >= 0 && index < versions.length - 1 )
				return( mapReleases.get( versions[ index + 1 ] ) );
		}		
		catch( Throwable e ) {
		}
		return( null );	
	}

	public void addRelease( Release release ) {
		mapReleases.put( release.RELEASEVER , release );
	}
	
}
