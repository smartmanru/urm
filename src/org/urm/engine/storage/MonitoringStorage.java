package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.system.MetaMonitoringTarget;

public class MonitoringStorage {

	public Artefactory artefactory;
	public Meta meta;
	LocalFolder workFolder;
	
	public MonitoringStorage( Artefactory artefactory , Meta meta , LocalFolder workFolder ) {
		this.artefactory = artefactory;
		this.meta = meta;
		this.workFolder = workFolder;
	}
	
	public LocalFolder getDataFolder( ActionBase action , MetaMonitoringTarget target ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		MetaEnvSegment sg = target.getSegment();
		if( sg == null )
			Common.exitUnexpected();
		
		String path = Common.getPath( core.MONITORING_DIR_DATA , sg.env.NAME );
		return( artefactory.getAnyFolder( action , path ) );
	}
	
	public LocalFolder getReportsFolder( ActionBase action , MetaMonitoringTarget target ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		String path = core.MONITORING_DIR_REPORTS;
		return( artefactory.getAnyFolder( action , path ) );
	}

	public LocalFolder getResourceFolder( ActionBase action ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		String path = core.MONITORING_DIR_RES;
		return( artefactory.getAnyFolder( action , path ) );
	}
	
	public LocalFolder getLogsFolder( ActionBase action , MetaMonitoringTarget target ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		MetaEnvSegment sg = target.getSegment();
		String path = Common.getPath( core.MONITORING_DIR_LOGS , sg.env.NAME );
		return( artefactory.getAnyFolder( action , path ) );
	}

	public String getMonitoringUrl() {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		return( core.MONITORING_RESOURCE_URL );
	}
	
	public String getHistoryImageFile( MetaMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment();
		String file = "history." + sg.env.NAME + "." + sg.NAME + ".png";
		return( file );
	}
	
	public String getRrdFile( MetaMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment();
		String file = "env." + sg.NAME + ".rrd";
		return( file );
	}
	
	public String getCheckEnvFile( MetaMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment();
		String file = "checkenv." + sg.NAME + ".log";
		return( file );
	}
	
	public String getCheckEnvRunningFile( MetaMonitoringTarget target ) throws Exception {
		String name = getCheckEnvFile( target );
		return( name + ".running" );
	}

	public String getRunningImageBasename() {
		return( "running.jpg" );
	}
	
	public String getFailedImageBasename() {
		return( "stopped.jpg" );
	}
	
	public String getStatusReportFile( MetaMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment();
		String basename = "overall." + sg.env.NAME + "." + sg.NAME + ".html";
		return( basename );
	}
	
	public String getStatusReportTemplateFile() {
		return( "imageonly.html" );
	}

}
