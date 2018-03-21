package org.urm.meta.release;

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
}
