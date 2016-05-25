package ru.egov.urm.dist;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.MetaReleaseTargetItem;

public class DistMD5 {

	public String distItem;
	public String distFile;
	public String md5value;
	
	public static String getManualItemRecord( ActionBase action , Dist dist , MetaReleaseTarget manualItem ) throws Exception {
		String md5value = dist.getDistItemMD5( action , manualItem.distManualItem , manualItem.DISTFILE );
		return( manualItem.distManualItem.KEY + ":" + manualItem.DISTFILE + ":" + md5value );
	}

	public static String getProjectItemRecord( ActionBase action , Dist dist , MetaReleaseTargetItem projectItem ) throws Exception {
		String md5value = dist.getDistItemMD5( action , projectItem.distItem , projectItem.DISTFILE );
		return( projectItem.distItem.KEY + ":" + projectItem.DISTFILE + ":" + md5value );
	}
	
}
