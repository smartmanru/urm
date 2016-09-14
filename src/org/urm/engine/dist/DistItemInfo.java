package org.urm.engine.dist;

import org.urm.engine.meta.MetaDistrBinaryItem;
import org.urm.engine.meta.MetaDistrConfItem;

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