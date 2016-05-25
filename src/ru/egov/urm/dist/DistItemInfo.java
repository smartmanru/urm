package ru.egov.urm.dist;

import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;

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
