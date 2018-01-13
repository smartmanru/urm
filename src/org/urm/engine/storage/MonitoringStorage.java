package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;
import org.urm.meta.product.MetaProductCoreSettings;

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
	
	public LocalFolder getDataFolder( ActionBase action , MetaMonitoringTarget target ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		String path = Common.getPath( core.MONITORING_DIR_DATA , target.ENV );
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
		String path = Common.getPath( core.MONITORING_DIR_LOGS , target.ENV );
		return( artefactory.getAnyFolder( action , path ) );
	}

	public String getMonitoringUrl() {
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		return( core.MONITORING_RESOURCE_URL );
	}
	
	public String getHistoryImageFile( MetaMonitoringTarget target ) throws Exception {
		String file = "history." + target.ENV + "." + target.SG + ".png";
		return( file );
	}
	
	public String getRrdFile( MetaMonitoringTarget target ) throws Exception {
		String file = "env." + target.SG + ".rrd";
		return( file );
	}
	
	public String getCheckEnvFile( MetaMonitoringTarget target ) throws Exception {
		String file = "checkenv." + target.SG + ".log";
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
		String basename = "overall." + target.ENV + "." + target.SG + ".html";
		return( basename );
	}
	
	public String getStatusReportTemplateFile() {
		return( "imageonly.html" );
	}

}
