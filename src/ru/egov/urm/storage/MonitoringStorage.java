package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaMonitoring;
import ru.egov.urm.meta.MetaMonitoringTarget;
import ru.egov.urm.meta.Metadata;

public class MonitoringStorage {

	public Artefactory artefactory;
	public Metadata meta;
	LocalFolder workFolder;
	MetaMonitoring mon;
	
	public MonitoringStorage( Artefactory artefactory , LocalFolder workFolder , MetaMonitoring mon ) {
		this.artefactory = artefactory;
		this.workFolder = workFolder;
		this.mon = mon;
		this.meta = artefactory.meta;
	}
	
	public String getHistoryImageFile( MetaMonitoringTarget target ) throws Exception {
		String path = Common.getPath( mon.DIR_REPORTS , target.PRODUCT );
		String file = "history." + target.ENV + "." + target.DC + ".png";
		return( Common.getPath( path , file ) );
	}
	
	public String getRrdFile( MetaMonitoringTarget target ) throws Exception {
		String path = Common.getPath( mon.DIR_DATA , target.PRODUCT , target.ENV );
		String file = "env." + target.DC + ".rrd";
		return( Common.getPath( path , file ) );
	}
	
	public String getCheckEnvFile( MetaMonitoringTarget target ) throws Exception {
		String path = Common.getPath( mon.DIR_DATA , target.PRODUCT , target.ENV );
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
		return( mon.RESOURCE_CONTEXT );
	}
}
