package org.urm.server.action.monitor;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.MetaMonitoringTarget;
import org.urm.meta.Metadata;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.MonitoringStorage;

public class MonitorInfo {

	ActionBase action;
	MonitoringStorage storage;
	Metadata meta;
	Map<String,MonitorTargetInfo> targets;

	public MonitorInfo( ActionBase action , MonitoringStorage storage ) {
		this.action = action;
		this.storage = storage;
		this.meta = action.meta;
		targets = new HashMap<String,MonitorTargetInfo>();
	}

	public MonitorTargetInfo getTargetInfo( MetaMonitoringTarget target ) throws Exception {
		MonitorTargetInfo info;
		synchronized( this ) {
			info = targets.get( target.NAME );
			if( info == null ) {
				info = new MonitorTargetInfo( target );
				targets.put( target.NAME , info );
			}
		};
		return( info );
	}
	
	public void addCheckEnvData( MetaMonitoringTarget target , long timeMillis , boolean status ) throws Exception {
		action.info( "addCheckEnvData: product=" + target.PRODUCT + ", env=" + target.ENV + ", dc=" + target.DC + 
				", timeMillis=" + timeMillis + ", succeeded:" + Common.getBooleanValue( status ) );
		MonitorTargetInfo info = getTargetInfo( target );
		info.setLastMajor( status , timeMillis );
	}

	public void addCheckMinorsData( MetaMonitoringTarget target , boolean status ) throws Exception {
		action.info( "addCheckMinorsData: product=" + target.PRODUCT + ", env=" + target.ENV + ", dc=" + target.DC + 
				", succeeded:" + Common.getBooleanValue( status ) );
		MonitorTargetInfo info = getTargetInfo( target );
		info.setLastMinor( status );
		
		// add to totals, update reports
		addRrdRecord( info );
		createHistoryGraph( info );
		updateReport( info );
	}

	private void createHistoryGraph( MonitorTargetInfo info ) throws Exception {
		MetaMonitoringTarget target = info.target;
		
		// form graph
		String DELAYS_GRAPH_SCALE = "-l 0 -u " + target.MAXTIME + " -r";
		String NOW = "now";

		String scale = DELAYS_GRAPH_SCALE;
		String geometry = "-w 1024 -h 200 -i";

		String now = NOW;
		String max_color = "#FF0000";
		String min_color = "#0000FF";
		String avg_color = "#00FF00";
		String color = "--color GRID#C0C0C0";

		String rrdfile = storage.getRrdFile( target );
		String F_CREATEFILE = storage.getHistoryImageFile( target );
		action.session.custom( action , "rrdtool graph " + F_CREATEFILE +
			" " + scale + " -v " + Common.getQuoted( "secs" ) + 
			" -t " + Common.getQuoted( target.ENV + ", dc=" + target.DC + " checkenv.sh execution time (0 if not running)" ) +
			" " + geometry + " " + color + 
			" --color BACK#E4E4E4" +
			" --end " + now +
			" --start end-1d" +
			" --x-grid MINUTE:1:HOUR:1:HOUR:1:0:%H" + 
			" DEF:linec=" + rrdfile + ":checkenv-time:MIN:step=60 LINE1:linec" + min_color + ":" + Common.getQuoted( "Min" ) +
			" DEF:linea=" + rrdfile + ":checkenv-time:AVERAGE:step=60 LINE1:linea" + avg_color + ":" + Common.getQuoted( "Avg" ) +
			" DEF:lineb=" + rrdfile + ":checkenv-time:MAX:step=60 LINE1:lineb" + max_color + ":" + Common.getQuoted( "Max" ) ); 
	}

	private void updateReport( MonitorTargetInfo info ) throws Exception {
		// calculate status
		boolean F_STATUS = ( info.statusMajor && info.statusMinor )? true : false;
		
		// form report
		String F_IMAGEFILE;
		String F_IMAGETEXT;
		if( F_STATUS ) {
			F_IMAGEFILE = storage.getRunningImageBasename();
			F_IMAGETEXT = "Environment " + info.target.ENV + " , dc=" + info.target.DC + " is up and running";
		}
		else {
			F_IMAGEFILE = storage.getFailedImageBasename();
			F_IMAGETEXT = "Environment " + info.target.ENV + " , dc=" + info.target.DC + " is not working";
		}

		String F_REPFILE = storage.getStatusReportFile( info.target );
		String F_RESFILE = storage.getStatusReportTemplateFile();
		String F_RESCONTEXT = storage.getMonitoringUrl();
		
		String template = ConfReader.readFile( action , F_RESFILE );
		template = Common.replace( template , "@IMAGE@" , F_RESCONTEXT + "/" + F_IMAGEFILE );  
		template = Common.replace( template , "@TEXT@" , F_IMAGETEXT );  
		
		Common.createFileFromString( F_REPFILE , template );
	}

	private void createRrdFile( String fname ) throws Exception {
		action.session.custom( action , "rrdtool create " + fname + 
			" --start 20150101" +
			" --step 60" +
			" DS:total:GAUGE:1000:0:U" +
			" DS:checkenv:GAUGE:1000:0:U" +
			" DS:checkenv-time:GAUGE:1000000:0:U" +
			" RRA:AVERAGE:0.5:5:1000" +
			" RRA:MAX:0.5:5:1000" +
			" RRA:MIN:0.5:5:1000" );
	}

	private void addRrdRecord( MonitorTargetInfo info ) throws Exception {
		String F_RRDFILE = storage.getRrdFile( info.target );
		String F_RRDFILE_LOG = F_RRDFILE + ".log"; 
		
		String F_STATUSTOTAL = "100";
		String F_ENVTOTAL = "100";
		if( !info.statusMajor )
			F_ENVTOTAL = "1";
		if( info.statusMajor == false || info.statusMinor == false )
			F_STATUSTOTAL = "1";
		
		String X_VALUES = F_STATUSTOTAL + ":" + F_ENVTOTAL + ":" + info.timeMajor;
		long F_TS = System.currentTimeMillis();
		long X_TS = F_TS / 1000;

		if( !action.session.checkFileExists( action , F_RRDFILE ) )
			createRrdFile( F_RRDFILE );
		
		action.session.appendFileWithString( action , F_RRDFILE_LOG , 
				"rrdtool update: " + Common.getTimeStamp( F_TS ) + "=" + X_TS + ":" + X_VALUES );
		action.session.custom( action , "rrdtool update " + F_RRDFILE + " " + X_TS + ":" + X_VALUES );
	}
	
	class MonitorTargetInfo {
		MetaMonitoringTarget target;
		
		public long timeMajor;
		public boolean statusMajor;
		public boolean statusMinor;
		
		public MonitorTargetInfo( MetaMonitoringTarget target ) {
			this.target = target;
		}
		
		public void setLastMajor( boolean statusMajor , long timeMajor ) throws Exception {
			this.statusMajor = statusMajor;
			this.timeMajor = timeMajor; 
		}
		
		public void setLastMinor( boolean statusMinor ) throws Exception {
			this.statusMinor = statusMinor;
		}
	}

}
