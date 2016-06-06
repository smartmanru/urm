package org.urm.dist;

import org.urm.common.Common;
import org.urm.meta.MetaDistrBinaryItem;
import org.urm.server.action.ActionBase;

public class DistMD5 {

	public String distItem;
	public String distFile;
	public String md5value;

	public static String getDistItemRecord( ActionBase action , Dist dist , MetaDistrBinaryItem item ) throws Exception {
		String deliveryFile = dist.getBinaryDistItemFile( action , item );
		String md5value = dist.getDistItemMD5( action , item , deliveryFile );
		String releaseFile = Common.getPath( item.delivery.FOLDER , deliveryFile );
		return( item.KEY + ":" + releaseFile + ":" + md5value );
	}
	
	public static String getManualItemRecord( ActionBase action , Dist dist , ReleaseTarget manualItem ) throws Exception {
		return( getDistItemRecord( action , dist , manualItem.distManualItem ) );
	}

	public static String getProjectItemRecord( ActionBase action , Dist dist , ReleaseTargetItem projectItem ) throws Exception {
		return( getDistItemRecord( action , dist , projectItem.distItem ) );
	}
	
}
