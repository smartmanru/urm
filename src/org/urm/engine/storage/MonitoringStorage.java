package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.system.AppProductMonitoringTarget;

public class MonitoringStorage {

	public Artefactory artefactory;
	public Meta meta;
	LocalFolder workFolder;
	
	public MonitoringStorage( Artefactory artefactory , Meta meta , LocalFolder workFolder ) {
		this.artefactory = artefactory;
		this.meta = meta;
		this.workFolder = workFolder;
	}
	
	public LocalFolder getDataFolder( ActionBase action , AppProductMonitoringTarget target ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		MetaEnvSegment sg = target.findSegment();
		if( sg == null )
			Common.exitUnexpected();
		
		String path = Common.getPath( core.MONITORING_DIR_DATA , sg.env.NAME );
		return( artefactory.getAnyFolder( action , path ) );
	}
	
	public LocalFolder getReportsFolder( ActionBase action , AppProductMonitoringTarget target ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		String path = core.MONITORING_DIR_REPORTS;
		return( artefactory.getAnyFolder( action , path ) );
	}

	public LocalFolder getResourceFolder( ActionBase action ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		String path = core.MONITORING_DIR_RES;
		return( artefactory.getAnyFolder( action , path ) );
	}
	
	public LocalFolder getLogsFolder( ActionBase action , AppProductMonitoringTarget target ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		MetaEnvSegment sg = target.findSegment();
		String path = Common.getPath( core.MONITORING_DIR_LOGS , sg.env.NAME );
		return( artefactory.getAnyFolder( action , path ) );
	}

	public String getMonitoringUrl() {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		return( core.MONITORING_RESOURCE_URL );
	}
	
	public String getHistoryImageFile( AppProductMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.findSegment();
		String file = "history." + sg.env.NAME + "." + sg.NAME + ".png";
		return( file );
	}
	
	public String getRrdFile( AppProductMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.findSegment();
		String file = "env." + sg.NAME + ".rrd";
		return( file );
	}
	
	public String getCheckEnvFile( AppProductMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.findSegment();
		String file = "checkenv." + sg.NAME + ".log";
		return( file );
	}
	
	public String getCheckEnvRunningFile( AppProductMonitoringTarget target ) throws Exception {
		String name = getCheckEnvFile( target );
		return( name + ".running" );
	}

	public String getRunningImageBasename() {
		return( "running.jpg" );
	}
	
	public String getFailedImageBasename() {
		return( "stopped.jpg" );
	}
	
	public String getStatusReportFile( AppProductMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.findSegment();
		String basename = "overall." + sg.env.NAME + "." + sg.NAME + ".html";
		return( basename );
	}
	
	public String getStatusReportTemplateFile() {
		return( "imageonly.html" );
	}

}
