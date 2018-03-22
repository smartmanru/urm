package org.urm.meta.release;

import java.util.Date;

public class ReleaseDist {

	public static String PROPERTY_HASH = "hash";
	public static String PROPERTY_DATE = "date";
	public static String PROPERTY_VARIANT = "variant";

	public Release release;
	
	public int ID;
	public String DATA_HASH;
	public Date DIST_DATE;
	public String DIST_VARIANT;

	public ReleaseDistTarget findTarget( ReleaseTarget rt ) {
		return( null );
	}
	
	public String getReleaseDir() {
		if( DIST_VARIANT.isEmpty() )
			return( release.RELEASEVER );
		return( release.RELEASEVER + "-" + DIST_VARIANT );
	}

}
