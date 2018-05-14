package org.urm.meta.system;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.products.EngineProductEnvs;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.loader.MatchItem;

public class AppProductMonitoringTarget {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_ENV = "env";
	public static String PROPERTY_SEGMENT = "segment";
	public static String PROPERTY_MAJOR_ENABLED = "major.enabled";
	public static String PROPERTY_MAJOR_SCHEDULE = "major.schedule";
	public static String PROPERTY_MAJOR_MAXTIME = "major.maxtime";
	public static String PROPERTY_MINOR_ENABLED = "minor.enabled";
	public static String PROPERTY_MINOR_SCHEDULE = "minor.schedule";
	public static String PROPERTY_MINOR_MAXTIME = "minor.maxtime";

	public AppProduct product;
	public AppProductMonitoring mon;
	
	public int ID;
	public MatchItem SEGMENT;
	public String FKENV;
	public String FKSG;
	public boolean MAJOR_ENABLED;
	public int MAJOR_MAXTIME;
	public boolean MINOR_ENABLED;
	public int MINOR_MAXTIME;
	public int SV;

	public ScheduleProperties majorSchedule;
	public ScheduleProperties minorSchedule;

	private Map<Integer,AppProductMonitoringItem> mapItems;
	private List<AppProductMonitoringItem> listUrls;
	private List<AppProductMonitoringItem> listWS;

	public AppProductMonitoringTarget( AppProduct product , AppProductMonitoring mon ) {
		this.mon = mon;
		
		ID = -1;
		SV = -1;
		
		listUrls = new LinkedList<AppProductMonitoringItem>();
		listWS = new LinkedList<AppProductMonitoringItem>();
		mapItems = new HashMap<Integer,AppProductMonitoringItem>();
		
		majorSchedule = new ScheduleProperties();
		minorSchedule = new ScheduleProperties();
	}

	public AppProductMonitoringTarget copy( AppProduct rproduct , AppProductMonitoring rmon ) {
		AppProductMonitoringTarget r = new AppProductMonitoringTarget( rproduct , rmon );
		
		r.ID = ID;
		r.SEGMENT = MatchItem.copy( SEGMENT );
		r.FKENV = FKENV;
		r.FKSG = FKSG;
		r.MAJOR_ENABLED = MAJOR_ENABLED;
		r.MAJOR_MAXTIME = MAJOR_MAXTIME;
		r.MINOR_ENABLED = MINOR_ENABLED;
		r.MINOR_MAXTIME = MINOR_MAXTIME;
		r.majorSchedule = majorSchedule.copy();
		r.minorSchedule = minorSchedule.copy();
		r.SV = SV;
		
		for( AppProductMonitoringItem item : listUrls ) {
			AppProductMonitoringItem ritem = item.copy( rproduct , r );
			r.addUrl( ritem );
		}
		
		for( AppProductMonitoringItem item : listWS ) {
			AppProductMonitoringItem ritem = item.copy( rproduct , r );
			r.addWS( ritem );
		}
		
		return( r );
	}
	
	public void addItem( AppProductMonitoringItem item ) {
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKURL )
			addUrl( item );
		else
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKWS )
			addWS( item );
	}
	
	public void addUrl( AppProductMonitoringItem item ) {
		listUrls.add( item );
		mapItems.put( item.ID , item );
	}
	
	public void addWS( AppProductMonitoringItem item ) {
		listWS.add( item );
		mapItems.put( item.ID , item );
	}
	
	public AppProductMonitoringItem[] getUrlsList() {
		return( listUrls.toArray( new AppProductMonitoringItem[0] ) );
	}
	
	public AppProductMonitoringItem[] getWSList() {
		return( listWS.toArray( new AppProductMonitoringItem[0] ) );
	}

	public void createTarget( Integer sgId , String envName , String sgName ) throws Exception {
		SEGMENT = MatchItem.create( sgId );
		this.FKENV = envName;
		this.FKSG = sgName;
	}
	
	public void createTarget( MetaEnvSegment sg ) throws Exception {
		SEGMENT = MatchItem.create( sg.ID );
		FKENV = "";
		FKSG = "";
	}

	public void removeItem( AppProductMonitoringItem item ) {
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKURL )
			listUrls.remove( item );
		else
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKWS )
			listWS.remove( item );
		mapItems.remove( item.ID );
	}

	public void modifyTarget( boolean major , boolean enabled , ScheduleProperties schedule , int maxTime ) throws Exception {
		if( major ) {
			this.MAJOR_ENABLED = enabled;
			this.MAJOR_MAXTIME = maxTime;
			this.majorSchedule = schedule;
		}
		else {
			this.MINOR_ENABLED = enabled;
			this.MINOR_MAXTIME = maxTime;
			this.minorSchedule = schedule;
		}
	}

	public MetaEnv findEnv() {
		if( SEGMENT.MATCHED ) {
			MetaEnvSegment sg = findSegment();
			if( sg == null )
				return( null );
			
			return( sg.env );
		}
		EngineProductEnvs envs = product.findEnvs();
		return( envs.findEnv( FKENV ) );
	}
	
	public MetaEnvSegment findSegment() {
		EngineProductEnvs envs = product.findEnvs();
		if( SEGMENT.MATCHED )
			return( envs.findSegment( SEGMENT.FKID ) );

		MetaEnv env = findEnv();
		return( env.findSegment( FKSG ) );
	}

	public AppProductMonitoringItem getItem( int id ) throws Exception {
		AppProductMonitoringItem item = mapItems.get( id );
		if( item == null )
			Common.exitUnexpected();
		return( item );
	}
	
}
