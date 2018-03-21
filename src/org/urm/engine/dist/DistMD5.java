package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.release.ReleaseScopeTarget;
import org.urm.meta.release.ReleaseScopeItem;

public class DistMD5 {

	public String distItem;
	public String distFile;
	public String md5value;

	public static String getDistItemRecord( ActionBase action , Dist dist , MetaDistrBinaryItem item ) throws Exception {
		String deliveryFile = dist.getBinaryDistItemFile( action , item );
		String md5value = dist.getDistItemMD5( action , item , deliveryFile );
		String releaseFile = Common.getPath( item.delivery.FOLDER , deliveryFile );
		return( item.NAME + ":" + releaseFile + ":" + md5value );
	}
	
	public static String getManualItemRecord( ActionBase action , Dist dist , ReleaseScopeTarget manualItem ) throws Exception {
		return( getDistItemRecord( action , dist , manualItem.distManualItem ) );
	}

	public static String getProjectItemRecord( ActionBase action , Dist dist , ReleaseScopeItem projectItem ) throws Exception {
		return( getDistItemRecord( action , dist , projectItem.distItem ) );
	}
	
}
