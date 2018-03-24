package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.product.MetaDistrBinaryItem;

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
	
	public static String getScopeDeliveryItemRecord( ActionBase action , Dist dist , ReleaseDistScopeDeliveryItem scopeItem ) throws Exception {
		return( getDistItemRecord( action , dist , scopeItem.binary ) );
	}
	
}
