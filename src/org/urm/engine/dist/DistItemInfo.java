package org.urm.engine.dist;

import java.util.Date;

import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaProductDoc;

public class DistItemInfo {

	MetaDistrBinaryItem distBinaryItem;
	MetaDistrConfItem distConfItem;
	MetaProductDoc distDocItem;

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

	public DistItemInfo( MetaProductDoc distDocItem ) {
		this.distDocItem = distDocItem;
	}

}
