package org.urm.server.dist;

import org.urm.server.meta.MetaDistrBinaryItem;
import org.urm.server.meta.MetaDistrConfItem;

public class DistItemInfo {

	MetaDistrBinaryItem distBinaryItem;
	MetaDistrConfItem distConfItem;

	public boolean found;
	public String subPath;
	public String fileName;
	public String md5value;
	
	public DistItemInfo( MetaDistrBinaryItem distBinaryItem ) {
		this.distBinaryItem = distBinaryItem;
	}

	public DistItemInfo( MetaDistrConfItem distConfItem ) {
		this.distConfItem = distConfItem;
	}

}
