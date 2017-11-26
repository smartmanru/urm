package org.urm.meta.product;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.schedule.ScheduleProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoringTarget {

	public Meta meta;
	public MetaMonitoring monitoring;
	
	public String NAME;
	
	public String ENV;
	public String SG;
	
	public boolean enabledMajor;
	public ScheduleProperties scheduleMajor;
	public int maxTimeMajor;
	public boolean enabledMinor;
	public ScheduleProperties scheduleMinor;
	public int maxTimeMinor;

	private List<MetaMonitoringItem> listUrls;
	private List<MetaMonitoringItem> listWS;

	public MetaMonitoringTarget( Meta meta , MetaMonitoring monitoring ) {
		this.meta = meta;
		this.monitoring = monitoring;
		listUrls = new LinkedList<MetaMonitoringItem>();
		listWS = new LinkedList<MetaMonitoringItem>();
	}

	public List<MetaMonitoringItem> getUrlsList( ActionBase action ) throws Exception {
		return( listUrls );
	}
	
	public List<MetaMonitoringItem> getWSList( ActionBase action ) throws Exception {
		return( listWS );
	}
	
	public MetaMonitoringTarget copy( ActionBase action , Meta meta , MetaMonitoring monitoring ) {
		MetaMonitoringTarget r = new MetaMonitoringTarget( meta , monitoring );
		r.NAME = NAME;
		r.ENV = ENV;
		r.SG = SG;
		r.enabledMajor = enabledMajor;
		r.scheduleMajor = scheduleMajor;
		r.maxTimeMajor = maxTimeMajor;
		r.enabledMinor = enabledMinor;
		r.scheduleMinor = scheduleMinor;
		r.maxTimeMinor = maxTimeMinor;
		
		for( MetaMonitoringItem item : listUrls ) {
			MetaMonitoringItem ritem = item.copy( action , meta , r );
			r.listUrls.add( ritem );
		}
		
		for( MetaMonitoringItem item : listWS ) {
			MetaMonitoringItem ritem = item.copy( action , meta , r );
			r.listWS.add( ritem );
		}
		
		return( r );
	}
	
	public void loadTarget( ActionBase action , Node node ) throws Exception {
		ENV = ConfReader.getRequiredAttrValue( node , "env" );
		SG = ConfReader.getRequiredAttrValue( node , "segment" );
		setName( action );
		
		enabledMajor = ConfReader.getBooleanAttrValue( node , "major.enabled" , false );
		maxTimeMajor = ConfReader.getIntegerAttrValue( node , "major.maxtime" , 300000 );
		scheduleMajor = new ScheduleProperties();
		scheduleMajor.setScheduleData( action , ConfReader.getAttrValue( node , "major.schedule" ) );

		enabledMinor = ConfReader.getBooleanAttrValue( node , "minor.enabled" , false );
		maxTimeMinor = ConfReader.getIntegerAttrValue( node , "minor.maxtime" , 300000 );
		scheduleMinor = new ScheduleProperties();
		scheduleMinor.setScheduleData( action , ConfReader.getAttrValue( node , "minor.schedule" ) );
		
		loadCheckUrls( action , node );
		loadCheckWS( action , node );
	}

	private void setName( ActionBase action ) throws Exception {
		NAME = meta.name + "::" + ENV + "::" + SG;
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
		Common.xmlSetElementAttr( doc , root , "env" , ENV );
		Common.xmlSetElementAttr( doc , root , "segment" , SG );
		Common.xmlSetElementAttr( doc , root , "major.enabled" , Common.getBooleanValue( enabledMajor ) );
		Common.xmlSetElementAttr( doc , root , "major.maxtime" , "" + maxTimeMajor );
		Common.xmlSetElementAttr( doc , root , "major.schedule" , scheduleMajor.getScheduleData() );
		Common.xmlSetElementAttr( doc , root , "minor.enabled" , Common.getBooleanValue( enabledMinor ) );
		Common.xmlSetElementAttr( doc , root , "minor.maxtime" , "" + maxTimeMinor );
		Common.xmlSetElementAttr( doc , root , "minor.schedule" , scheduleMinor.getScheduleData() );
		
		for( MetaMonitoringItem item : listUrls ) {
			Element element = Common.xmlCreateElement( doc , root , "checkurl" );
			item.save( action , doc , element );
		}
		
		for( MetaMonitoringItem item : listWS ) {
			Element element = Common.xmlCreateElement( doc , root , "checkws" );
			item.save( action , doc , element );
		}
	}
	
	public void createTarget( EngineTransaction transaction , MetaEnvSegment sg ) throws Exception {
		this.ENV = sg.env.NAME;
		this.SG = sg.NAME;
		setName( transaction.getAction() );
		scheduleMajor = new ScheduleProperties();
		scheduleMinor = new ScheduleProperties();
	}

	public void modifyTarget( EngineTransaction transaction , boolean major , boolean enabled , ScheduleProperties schedule , int maxTime ) throws Exception {
		if( major ) {
			this.enabledMajor = enabled;
			this.maxTimeMajor = maxTime;
			this.scheduleMajor = schedule;
		}
		else {
			this.enabledMinor = enabled;
			this.maxTimeMinor = maxTime;
			this.scheduleMinor = schedule;
		}
	}

	public MetaEnvSegment getSegment( ActionBase action ) throws Exception {
		MetaEnv env = meta.getEnv( action , ENV );
		return( env.getSG( action , SG ) );
	}
	
}
