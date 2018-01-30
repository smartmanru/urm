package org.urm.meta.env;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.meta.MatchItem;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoringTarget {

	public Meta meta;
	public MetaMonitoring mon;
	
	public int ID;
	public MatchItem ENVSG;
	public boolean MAJOR_ENABLED;
	public int MAJOR_MAXTIME;
	public boolean MINOR_ENABLED;
	public int MINOR_MAXTIME;
	public int EV;

	public ScheduleProperties majorSchedule;
	public ScheduleProperties minorSchedule;
	
	private List<MetaMonitoringItem> listUrls;
	private List<MetaMonitoringItem> listWS;

	public MetaMonitoringTarget( Meta meta , MetaMonitoring mon ) {
		this.meta = meta;
		this.mon = mon;
		
		ID = -1;
		EV = -1;
		listUrls = new LinkedList<MetaMonitoringItem>();
		listWS = new LinkedList<MetaMonitoringItem>();
	}

	public MetaMonitoringTarget copy( Meta rmeta , MetaMonitoring rmon ) {
		MetaMonitoringTarget r = new MetaMonitoringTarget( rmeta , rmon );
		r.ID = ID;
		r.ENVSG = MatchItem.copy( ENVSG );
		r.MAJOR_ENABLED = MAJOR_ENABLED;
		r.MAJOR_MAXTIME = MAJOR_MAXTIME;
		r.MINOR_ENABLED = MINOR_ENABLED;
		r.MINOR_MAXTIME = MINOR_MAXTIME;
		r.majorSchedule = majorSchedule.copy();
		r.minorSchedule = minorSchedule.copy();
		r.EV = EV;
		
		for( MetaMonitoringItem item : listUrls ) {
			MetaMonitoringItem ritem = item.copy( meta , r );
			r.addUrl( ritem );
		}
		
		for( MetaMonitoringItem item : listWS ) {
			MetaMonitoringItem ritem = item.copy( meta , r );
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
	
	public void createTarget( EngineTransaction transaction , MetaEnvSegment sg ) throws Exception {
		this.ENVSG = new MatchItem( null , meta.name + "::" + sg.env.NAME + "::" + sg.NAME );
		majorSchedule = new ScheduleProperties();
		minorSchedule = new ScheduleProperties();
	}

	public void modifyTarget( EngineTransaction transaction , boolean major , boolean enabled , ScheduleProperties schedule , int maxTime ) throws Exception {
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

	public MetaEnvSegment findSegment() {
		MetaEnvs envs = meta.getEnviroments();
		MetaEnv env = envs.findEnv( getMatchEnvName() );
		if( env == null )
			return( null );
		return( env.findSegment( getMatchSgName() ) );
	}
	
	public String getMatchEnvName() {
		return( Common.getListItem( ENVSG.FKNAME , "::" , 1 ) );
	}
	
	public String getMatchSgName() {
		return( Common.getListItem( ENVSG.FKNAME , "::" , 2 ) );
	}
	
	public String getName() throws Exception {
		return( ENVSG.FKNAME );
	}
	
	public void loadTarget( ActionBase action , Node node ) throws Exception {
		String ENV = ConfReader.getRequiredAttrValue( node , "env" );
		String SG = ConfReader.getRequiredAttrValue( node , "segment" );
		this.ENVSG = new MatchItem( null , meta.name + "::" + ENV + "::" + SG );
		
		MAJOR_ENABLED = ConfReader.getBooleanAttrValue( node , "major.enabled" , false );
		MAJOR_MAXTIME = ConfReader.getIntegerAttrValue( node , "major.maxtime" , 300000 );
		majorSchedule = new ScheduleProperties();
		majorSchedule.setScheduleData( action , ConfReader.getAttrValue( node , "major.schedule" ) );

		MINOR_ENABLED = ConfReader.getBooleanAttrValue( node , "minor.enabled" , false );
		MINOR_MAXTIME = ConfReader.getIntegerAttrValue( node , "minor.maxtime" , 300000 );
		minorSchedule = new ScheduleProperties();
		minorSchedule.setScheduleData( action , ConfReader.getAttrValue( node , "minor.schedule" ) );
		
		loadCheckUrls( action , node );
		loadCheckWS( action , node );
	}

	private void loadCheckUrls( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "checkurl" );
		if( items == null )
			return;
		
		for( Node checkNode : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( meta , this );
			item.loadUrl( action , checkNode );
			listUrls.add( item );
		}
	}

	private void loadCheckWS( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "checkws" );
		if( items == null )
			return;
		
		for( Node checkNode : items ) {
			MetaMonitoringItem item = new MetaMonitoringItem( meta , this );
			item.loadUrl( action , checkNode );
			listWS.add( item );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		MetaEnvSegment sg = findSegment();
		if( sg == null )
			Common.exitUnexpected();
		
		Common.xmlSetElementAttr( doc , root , "env" , sg.env.NAME );
		Common.xmlSetElementAttr( doc , root , "segment" , sg.NAME );
		Common.xmlSetElementAttr( doc , root , "major.enabled" , Common.getBooleanValue( MAJOR_ENABLED ) );
		Common.xmlSetElementAttr( doc , root , "major.maxtime" , "" + MAJOR_MAXTIME );
		if( majorSchedule != null )
			Common.xmlSetElementAttr( doc , root , "major.schedule" , majorSchedule.getScheduleData() );
		Common.xmlSetElementAttr( doc , root , "minor.enabled" , Common.getBooleanValue( MINOR_ENABLED ) );
		Common.xmlSetElementAttr( doc , root , "minor.maxtime" , "" + MINOR_MAXTIME );
		if( minorSchedule != null )
			Common.xmlSetElementAttr( doc , root , "minor.schedule" , minorSchedule.getScheduleData() );
		
		for( MetaMonitoringItem item : listUrls ) {
			Element element = Common.xmlCreateElement( doc , root , "checkurl" );
			item.save( action , doc , element );
		}
		
		for( MetaMonitoringItem item : listWS ) {
			Element element = Common.xmlCreateElement( doc , root , "checkws" );
			item.save( action , doc , element );
		}
	}
	
}
