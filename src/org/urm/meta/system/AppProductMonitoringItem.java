package org.urm.meta.system;

import org.urm.db.core.DBEnums.*;

public class AppProductMonitoringItem {

	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_URL = "url";
	public static String PROPERTY_WSDATA = "wsdata";
	public static String PROPERTY_WSCHECK = "wscheck";
	
	public AppProduct product;
	public AppProductMonitoringTarget target;
	
	public int ID;
	public DBEnumMonItemType MONITEM_TYPE;
	public String DESC;
	public String URL;
	public String WSDATA;
	public String WSCHECK;
	public int SV;

	public AppProductMonitoringItem( AppProduct product , AppProductMonitoringTarget target ) {
		this.product = product; 
		this.target = target;
	}

	public AppProductMonitoringItem copy( AppProduct rproduct , AppProductMonitoringTarget rtarget ) {
		AppProductMonitoringItem r = new AppProductMonitoringItem( rproduct , rtarget );
		
		r.ID = ID;
		r.MONITEM_TYPE = MONITEM_TYPE;
		r.DESC = DESC;
		r.URL = URL;
		r.WSDATA = WSDATA;
		r.WSCHECK = WSCHECK;
		r.SV = SV;
		return( r );
	}		

	public void create( DBEnumMonItemType type , String url , String desc , String wsdata , String wscheck ) {
		this.MONITEM_TYPE = type;
		this.URL = url;
		this.WSDATA = wsdata;
		this.WSCHECK = wscheck;
		this.DESC = desc;
	}
	
	public void modifyPage( String url , String desc ) {
		this.URL = url;
		this.DESC = desc;
		this.WSDATA = "";
		this.WSCHECK = "";
	}
	
	public void modifyWebService( String url , String desc , String wsdata , String wscheck ) {
		this.URL = url;
		this.DESC = desc;
		this.WSDATA = wsdata;
		this.WSCHECK = wscheck;
	}
	
}
