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
	
	private Map<String,Release> mapReleasesNormal;
	private Map<String,Release> mapReleasesMaster;
	private Map<Integer,Release> mapReleasesById;
	
	public ReleaseRepository( Meta meta , ProductReleases releases ) {
		this.meta = meta;
		this.releases = releases;
		mapReleasesNormal = new HashMap<String,Release>();
		mapReleasesById = new HashMap<Integer,Release>();
		mapReleasesMaster = new HashMap<String,Release>();
	}

	public void createRepository( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}
	
	public Release findRelease( String RELEASEVER ) {
		try {
			String version = VersionInfo.normalizeReleaseVer( RELEASEVER );
			return( mapReleasesNormal.get( version ) );
		}
		catch( Throwable e ) {
			return( null );
		}
	}

	public Release findMaster( String name ) {
		return( mapReleasesMaster.get( name ) );
	}
	
	public synchronized String[] getActiveVersions() {
		return( VersionInfo.orderVersions( Common.getSortedKeys( mapReleasesNormal ) ) );
	}

	public Release getNextRelease( String RELEASEVER ) {
		try {
			String[] versions = getActiveVersions();
			String version = VersionInfo.normalizeReleaseVer( RELEASEVER );
			int index = Common.getIndexOf( versions , version );
			if( index >= 0 && index < versions.length - 1 )
				return( mapReleasesNormal.get( versions[ index + 1 ] ) );
		}		
		catch( Throwable e ) {
		}
		return( null );	
	}

	public synchronized void addRelease( Release release ) {
		if( release.MASTER )
			mapReleasesMaster.put( release.NAME , release );
		else
			mapReleasesNormal.put( release.RELEASEVER , release );
		mapReleasesById.put( release.ID , release );
	}

	public Release getRelease( int id ) throws Exception {
		return( mapReleasesById.get( id ) );
	}
	
}
