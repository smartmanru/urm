package org.urm.meta.env;

import org.urm.db.core.DBEnums.*;

public class MetaMonitoringItem {

	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_URL = "url";
	public static String PROPERTY_WSDATA = "wsdata";
	public static String PROPERTY_WSCHECK = "wscheck";
	
	public ProductEnvs envs;
	public MetaMonitoringTarget target;
	
	public int ID;
	public DBEnumMonItemType MONITEM_TYPE;
	public String DESC;
	public String URL;
	public String WSDATA;
	public String WSCHECK;
	public int EV;

	public MetaMonitoringItem( ProductEnvs envs , MetaMonitoringTarget target ) {
		this.envs = envs; 
		this.target = target;
	}

	public MetaMonitoringItem copy( ProductEnvs renvs , MetaMonitoringTarget rtarget ) {
		MetaMonitoringItem r = new MetaMonitoringItem( renvs , rtarget );
		
		r.ID = ID;
		r.MONITEM_TYPE = MONITEM_TYPE;
		r.DESC = DESC;
		r.URL = URL;
		r.WSDATA = WSDATA;
		r.WSCHECK = WSCHECK;
		r.EV = EV;
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
