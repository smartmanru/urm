package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.engine.ProductMonitoring;
import org.urm.meta.engine.ProductMonitoringTarget;

public class MonitoringStorage {

	public Artefactory artefactory;
	LocalFolder workFolder;
	ProductMonitoring mon;
	
	public MonitoringStorage( Artefactory artefactory , LocalFolder workFolder , ProductMonitoring mon ) {
		this.artefactory = artefactory;
		this.workFolder = workFolder;
		this.mon = mon;
	}
	
	public LocalFolder getDataFolder( ActionBase action , ProductMonitoringTarget target ) throws Exception {
		String path = Common.getPath( mon.DIR_DATA , target.ENV );
		return( artefactory.getAnyFolder( action , path ) );
	}
	
	public LocalFolder getReportsFolder( ActionBase action , ProductMonitoringTarget target ) throws Exception {
		String path = mon.DIR_REPORTS;
		return( artefactory.getAnyFolder( action , path ) );
	}

	public LocalFolder getResourceFolder( ActionBase action ) throws Exception {
		String path = mon.DIR_RES;
		return( artefactory.getAnyFolder( action , path ) );
	}
	
	public LocalFolder getLogsFolder( ActionBase action , ProductMonitoringTarget target ) throws Exception {
		String path = Common.getPath( mon.DIR_LOGS , target.ENV );
		return( artefactory.getAnyFolder( action , path ) );
	}
	
	public String getHistoryImageFile( ProductMonitoringTarget target ) throws Exception {
		String file = "history." + target.ENV + "." + target.SG + ".png";
		return( file );
	}
	
	public String getRrdFile( ProductMonitoringTarget target ) throws Exception {
		String file = "env." + target.SG + ".rrd";
		return( file );
	}
	
	public String getCheckEnvFile( ProductMonitoringTarget target ) throws Exception {
		String file = "checkenv." + target.SG + ".log";
		return( file );
	}
	
	public String getCheckEnvRunningFile( ProductMonitoringTarget target ) throws Exception {
		String name = getCheckEnvFile( target );
		return( name + ".running" );
	}

	public String getRunningImageBasename() {
		return( "running.jpg" );
	}
	
	public String getFailedImageBasename() {
		return( "stopped.jpg" );
	}
	
	public String getStatusReportFile( ProductMonitoringTarget target ) throws Exception {
		String basename = "overall." + target.ENV + "." + target.SG + ".html";
		return( basename );
	}
	
	public String getStatusReportTemplateFile() {
		return( "imageonly.html" );
	}

	public String getMonitoringUrl() {
		return( mon.RESOURCE_URL );
	}
}
