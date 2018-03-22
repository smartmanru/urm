package org.urm.meta.release;

import java.util.LinkedList;
import java.util.List;

import org.urm.meta.product.Meta;

public class ReleaseRepository {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_PRODUCT = "product";
	
	public int ID;
	public String NAME;
	public String DESC;
	
	public Meta meta;
	ProductReleases releases;
	
	public ReleaseRepository( Meta meta , ProductReleases releases ) {
		this.meta = meta;
		this.releases = releases;
	}

	public void createRepository( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}
	
	public Release findRelease( String RELEASEVER ) {
		return( null );
	}
	
	public synchronized String[] getActiveVersions() {
		List<String> list = new LinkedList<String>();
		return( list.toArray( new String[0] ) );
	}

	public Release getNextRelease( String RELEASEVER ) {
		return( null );	
	}
	
}
