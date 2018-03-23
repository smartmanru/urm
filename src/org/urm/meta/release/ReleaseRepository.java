package org.urm.meta.release;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.engine.dist.VersionInfo;
import org.urm.meta.product.Meta;

public class ReleaseRepository {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_PRODUCT = "product";
	
	public int ID;
	public String NAME;
	public String DESC;
	
	public Meta meta;
	public ProductReleases releases;
	
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
		List<String> list = new LinkedList<String>();
		return( list.toArray( new String[0] ) );
	}

	public Release getNextRelease( String RELEASEVER ) {
		return( null );	
	}

	public void addRelease( Release release ) {
		mapReleases.put( release.RELEASEVER , release );
	}
	
}
