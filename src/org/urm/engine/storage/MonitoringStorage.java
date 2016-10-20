package org.urm.engine.storage;

import org.urm.common.Common;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;

public class MonitoringStorage {

	public Artefactory artefactory;
	public Meta meta;
	LocalFolder workFolder;
	MetaMonitoring mon;
	
	public MonitoringStorage( Artefactory artefactory , LocalFolder workFolder , MetaMonitoring mon ) {
		this.artefactory = artefactory;
		this.workFolder = workFolder;
		this.mon = mon;
		this.meta = mon.meta;
	}
	
	public String getHistoryImageFile( MetaMonitoringTarget target ) throws Exception {
		String path = mon.DIR_REPORTS;
		String file = "history." + target.ENV + "." + target.DC + ".png";
		return( Common.getPath( path , file ) );
	}
	
	public String getRrdFile( MetaMonitoringTarget target ) throws Exception {
		String path = Common.getPath( mon.DIR_DATA , target.ENV );
		String file = "env." + target.DC + ".rrd";
		return( Common.getPath( path , file ) );
	}
	
	public String getCheckEnvFile( MetaMonitoringTarget target ) throws Exception {
		String path = Common.getPath( mon.DIR_DATA , target.ENV );
		String file = "checkenv." + target.DC + ".log";
		return( Common.getPath( path , file ) );
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
		String basename = "overall." + target.ENV + "." + target.DC + ".html";
		return( Common.getPath( mon.DIR_REPORTS , basename ) );
	}
	
	public String getStatusReportTemplateFile() {
		return( Common.getPath( mon.DIR_RES , "imageonly.html" ) );
	}

	public String getMonitoringUrl() {
		return( mon.RESOURCE_URL );
	}
}
