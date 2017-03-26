package org.urm.engine.dist;

import java.util.Date;

import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;

public class DistItemInfo {

	MetaDistrBinaryItem distBinaryItem;
	MetaDistrConfItem distConfItem;

	public boolean found;
	public String subPath;
	public String fileName;
	public String md5value;
	public Date timestamp; 
	
	public DistItemInfo( MetaDistrBinaryItem distBinaryItem ) {
		this.distBinaryItem = distBinaryItem;
	}

	public DistItemInfo( MetaDistrConfItem distConfItem ) {
		this.distConfItem = distConfItem;
	}

}
