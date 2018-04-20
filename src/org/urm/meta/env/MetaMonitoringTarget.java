package org.urm.meta.env;

import java.util.LinkedList;
import java.util.List;

import org.urm.db.core.DBEnums.DBEnumMonItemType;
import org.urm.engine.schedule.ScheduleProperties;

public class MetaMonitoringTarget {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_MAJOR_ENABLED = "major.enabled";
	public static String PROPERTY_MAJOR_SCHEDULE = "major.schedule";
	public static String PROPERTY_MAJOR_MAXTIME = "major.maxtime";
	public static String PROPERTY_MINOR_ENABLED = "minor.enabled";
	public static String PROPERTY_MINOR_SCHEDULE = "minor.schedule";
	public static String PROPERTY_MINOR_MAXTIME = "minor.maxtime";

	public ProductEnvs envs;
	public MetaMonitoring mon;
	
	public int ID;
	public int ENV_ID;
	public int SEGMENT_ID;
	public boolean MAJOR_ENABLED;
	public int MAJOR_MAXTIME;
	public boolean MINOR_ENABLED;
	public int MINOR_MAXTIME;
	public int EV;

	public ScheduleProperties majorSchedule;
	public ScheduleProperties minorSchedule;
	
	private List<MetaMonitoringItem> listUrls;
	private List<MetaMonitoringItem> listWS;

	public MetaMonitoringTarget( ProductEnvs envs , MetaMonitoring mon ) {
		this.envs = envs;
		this.mon = mon;
		
		ID = -1;
		EV = -1;
		listUrls = new LinkedList<MetaMonitoringItem>();
		listWS = new LinkedList<MetaMonitoringItem>();
	}

	public MetaMonitoringTarget copy( ProductEnvs renvs , MetaMonitoring rmon ) {
		MetaMonitoringTarget r = new MetaMonitoringTarget( renvs , rmon );
		
		r.ID = ID;
		r.ENV_ID = ENV_ID;
		r.SEGMENT_ID = SEGMENT_ID;
		r.MAJOR_ENABLED = MAJOR_ENABLED;
		r.MAJOR_MAXTIME = MAJOR_MAXTIME;
		r.MINOR_ENABLED = MINOR_ENABLED;
		r.MINOR_MAXTIME = MINOR_MAXTIME;
		r.majorSchedule = majorSchedule.copy();
		r.minorSchedule = minorSchedule.copy();
		r.EV = EV;
		
		for( MetaMonitoringItem item : listUrls ) {
			MetaMonitoringItem ritem = item.copy( renvs , r );
			r.addUrl( ritem );
		}
		
		for( MetaMonitoringItem item : listWS ) {
			MetaMonitoringItem ritem = item.copy( renvs , r );
			r.addWS( ritem );
		}
		
		return( r );
	}
	
	public void addUrl( MetaMonitoringItem item ) {
		listUrls.add( item );
	}
	
	public void addWS( MetaMonitoringItem item ) {
		listWS.add( item );
	}
	
	public MetaMonitoringItem[] getUrlsList() {
		return( listUrls.toArray( new MetaMonitoringItem[0] ) );
	}
	
	public MetaMonitoringItem[] getWSList() {
		return( listWS.toArray( new MetaMonitoringItem[0] ) );
	}
	
	public void createTarget( MetaEnvSegment sg ) throws Exception {
		this.ENV_ID = sg.env.ID;
		this.SEGMENT_ID = sg.ID;
		majorSchedule = new ScheduleProperties();
		minorSchedule = new ScheduleProperties();
	}

	public void removeItem( MetaMonitoringItem item ) {
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKURL )
			listUrls.remove( item );
		else
		if( item.MONITEM_TYPE == DBEnumMonItemType.CHECKWS )
			listWS.remove( item );
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

	public MetaEnv getEnv() {
		return( envs.findMetaEnv( ENV_ID ) );
	}
	
	public MetaEnvSegment getSegment() {
		MetaEnv env = envs.findMetaEnv( ENV_ID );
		return( env.findSegment( SEGMENT_ID ) );
	}
	
}
