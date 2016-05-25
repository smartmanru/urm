package ru.egov.urm.dist;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.MetaReleaseTargetItem;

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
	
	public static String getManualItemRecord( ActionBase action , Dist dist , MetaReleaseTarget manualItem ) throws Exception {
		return( getDistItemRecord( action , dist , manualItem.distManualItem ) );
	}

	public static String getProjectItemRecord( ActionBase action , Dist dist , MetaReleaseTargetItem projectItem ) throws Exception {
		return( getDistItemRecord( action , dist , projectItem.distItem ) );
	}
	
}
