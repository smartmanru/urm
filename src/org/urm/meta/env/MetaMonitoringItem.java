package org.urm.meta.env;

import org.urm.db.core.DBEnums.*;
import org.urm.meta.product.Meta;

public class MetaMonitoringItem {

	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_URL = "url";
	public static String PROPERTY_WSDATA = "wsdata";
	public static String PROPERTY_WSCHECK = "wscheck";
	
	public Meta meta;
	public MetaMonitoringTarget target;
	
	public int ID;
	public DBEnumMonItemType MONITEM_TYPE;
	public String DESC;
	public String URL;
	public String WSDATA;
	public String WSCHECK;
	public int EV;

	public MetaMonitoringItem( Meta meta , MetaMonitoringTarget target ) {
		this.meta = meta; 
		this.target = target;
	}

	public MetaMonitoringItem copy( Meta rmeta , MetaMonitoringTarget rtarget ) {
		MetaMonitoringItem r = new MetaMonitoringItem( rmeta , rtarget );
		r.ID = ID;
		r.MONITEM_TYPE = MONITEM_TYPE;
		r.DESC = DESC;
		r.URL = URL;
		r.WSDATA = WSDATA;
		r.WSCHECK = WSCHECK;
		r.EV = EV;
		return( r );
	}		

	public void create( String url , String wsdata , String wscheck , String desc ) {
		this.URL = url;
		this.WSDATA = wsdata;
		this.WSCHECK = wscheck;
		this.DESC = desc;
	}
	
}
