package org.urm.action.monitor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.product.MetaMonitoringTarget;

public class MonitorInfo {

	ActionBase action;
	MonitoringStorage storage;
	Map<String,MonitorTargetInfo> targets;

	RrdDb rrdDb;
	boolean rrdDbFail;
	
	public MonitorInfo( ActionBase action , MonitoringStorage storage ) {
		this.action = action;
		this.storage = storage;
		targets = new HashMap<String,MonitorTargetInfo>();
		rrdDbFail = false;
	}

	public void stop() throws Exception {
		if( rrdDb != null && rrdDbFail == false )
			rrdDb.close();
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
		action.info( "addCheckEnvData: product=" + target.meta.name + ", env=" + target.ENV + ", dc=" + target.DC + 
				", timeMillis=" + timeMillis + ", succeeded:" + Common.getBooleanValue( status ) );
		MonitorTargetInfo info = getTargetInfo( target );
		info.setLastMajor( status , timeMillis );
	}

	public void addCheckMinorsData( MetaMonitoringTarget target , boolean status ) throws Exception {
		action.info( "addCheckMinorsData: product=" + target.meta.name + ", env=" + target.ENV + ", dc=" + target.DC + 
				", succeeded:" + Common.getBooleanValue( status ) );
		MonitorTargetInfo info = getTargetInfo( target );
		info.setLastMinor( status );
		
		// add to totals, update reports
		addRrdRecord( info );
		createHistoryGraph( info );
		updateReport( info );
	}

	private RrdGraph createHistoryGraph( MonitorTargetInfo info ) throws Exception {
		if( rrdDbFail )
			return( null );
		
		MetaMonitoringTarget target = info.target;
		
		LocalFolder dataFolder = storage.getDataFolder( action , info.target );  
		String rrdfile = dataFolder.getFilePath( action , storage.getRrdFile( target ) );
		
		LocalFolder reportsFolder = storage.getReportsFolder( action , target );
		String F_CREATEFILE = reportsFolder.getFilePath( action , storage.getHistoryImageFile( target ) );
		
		/*
		 * OLD COMMAND SYNTAX
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

		action.shell.custom( action , "rrdtool graph " + F_CREATEFILE +
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
		 */
		
		RrdGraphDef gDef = new RrdGraphDef();
		long endTime = Util.getTime();
		long startTime = endTime - 86400;
		gDef.setTimeSpan( startTime , endTime );
		gDef.setMinValue( 0 );
		gDef.setMaxValue( target.MAXTIME );
		gDef.setRigid( true );
		gDef.setWidth( 1024 );
		gDef.setHeight( 200 );
		gDef.setVerticalLabel( "secs" );
		gDef.setTitle( target.ENV + ", dc=" + target.DC + " check datacenter execution time (0 if not running)" );
		gDef.setColor( RrdGraphDef.COLOR_GRID , Color.decode( "0xC0C0C0" ) );
		gDef.setColor( RrdGraphDef.COLOR_BACK , Color.decode( "0xE4E4E4" ) );
		gDef.setTimeAxis( RrdGraphDef.MINUTE , 1 , RrdGraphDef.HOUR , 1 , RrdGraphDef.HOUR , 1 , 0 , "%H" );
		gDef.datasource( "linec" , rrdfile , "checkenv-time" , ConsolFun.MIN );
		gDef.datasource( "linea" , rrdfile , "checkenv-time" , ConsolFun.AVERAGE );
		gDef.datasource( "lineb" , rrdfile , "checkenv-time" , ConsolFun.MAX );
		gDef.line( "linec" , Color.decode( "0xFF0000" ) , "Min" );
		gDef.line( "linea" , Color.decode( "0x00FF00" ) , "Avg" );
		gDef.line( "lineb" , Color.decode( "0xFF0000" ) , "Max" );
		gDef.setImageFormat( "png" );
		gDef.setFilename( F_CREATEFILE );
		RrdGraph graph = new RrdGraph( gDef );
		return( graph );
	}

	private void updateReport( MonitorTargetInfo info ) throws Exception {
		LocalFolder resourceFolder = storage.getResourceFolder( action );
		if( resourceFolder.checkExists( action ) ) {
			action.trace( "ignore create report due to missing resource folder: " + resourceFolder.folderPath );
			return;
		}
		
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

		LocalFolder reportsFolder = storage.getReportsFolder( action , info.target );
		String F_REPFILE = reportsFolder.getFilePath( action , storage.getStatusReportFile( info.target ) );
		
		String F_RESFILE = resourceFolder.getFilePath( action , storage.getStatusReportTemplateFile() );
		String F_RESCONTEXT = storage.getMonitoringUrl();
		
		String template = action.readFile( F_RESFILE );
		template = Common.replace( template , "@IMAGE@" , F_RESCONTEXT + "/" + F_IMAGEFILE );  
		template = Common.replace( template , "@TEXT@" , F_IMAGETEXT );  
		
		Common.createFileFromString( F_REPFILE , template );
	}

	private void createRrdFile( String fname ) throws Exception {
		/*
		 * OLD COMMAND SYNTAX
		action.shell.customCheckStatus( action , "rrdtool create " + fname + 
			" --start 20150101" +
			" --step 60" +
			" DS:total:GAUGE:1000:0:U" +
			" DS:checkenv:GAUGE:1000:0:U" +
			" DS:checkenv-time:GAUGE:1000000:0:U" +
			" RRA:AVERAGE:0.5:5:1000" +
			" RRA:MAX:0.5:5:1000" +
			" RRA:MIN:0.5:5:1000" );
		*/
		
		// new - from https://oldwww.jrobin.org/api/jrobinandrrdtoolcompared.html
		String rrdFile = fname;
		long start = Util.getTime();
		int step = 60;
		
		RrdDef rrdDef = new RrdDef( rrdFile , start - 1 , step );
		rrdDef.addDatasource( "total" , DsType.GAUGE , 1000 , 0 , Double.NaN );
		rrdDef.addDatasource( "checkenv" , DsType.GAUGE , 1000 , 0 , Double.NaN );
		rrdDef.addDatasource( "checkenv-time" , DsType.GAUGE , 1000000 , 0 , Double.NaN );
		rrdDef.addArchive( ConsolFun.AVERAGE , 0.5 , 5 , 1000 );
		rrdDef.addArchive( ConsolFun.MAX , 0.5 , 5 , 1000 );
		rrdDef.addArchive( ConsolFun.MIN , 0.5 , 5 , 1000 );
		
		rrdDb = new RrdDb( rrdDef );
		rrdDb.close();
		
		rrdDb = new RrdDb( fname );
	}

	private void addRrdRecord( MonitorTargetInfo info ) throws Exception {
		if( rrdDbFail )
			return;
			
		LocalFolder dataFolder = storage.getDataFolder( action , info.target );
		
		String F_RRDFILE = dataFolder.getFilePath( action , storage.getRrdFile( info.target ) );
		String F_RRDFILE_LOG = F_RRDFILE + ".log"; 
		
		int F_STATUSTOTAL = 100;
		int F_ENVTOTAL = 100;
		if( !info.statusMajor )
			F_ENVTOTAL = 1;
		if( info.statusMajor == false || info.statusMinor == false )
			F_STATUSTOTAL = 1;
		
		String X_VALUES = F_STATUSTOTAL + ":" + F_ENVTOTAL + ":" + info.timeMajor;
		long F_TS = System.currentTimeMillis();
		long X_TS = F_TS / 1000;

		/*
		 * OLD COMMAND SYNTAX 
		if( !action.shell.checkFileExists( action , F_RRDFILE ) )
			createRrdFile( F_RRDFILE );
		 */

		if( rrdDb == null ) {
			rrdDbFail = true;
			if( !action.shell.checkFileExists( action , F_RRDFILE ) )
				createRrdFile( F_RRDFILE );
			else
				rrdDb = new RrdDb( F_RRDFILE );
			rrdDbFail = false;
		}
		
		action.shell.appendFileWithString( action , F_RRDFILE_LOG , 
				"rrdtool update: " + Common.getTimeStamp( F_TS ) + "=" + X_TS + ":" + X_VALUES );
		
		/*
		 * OLD COMMAND SYNTAX
		action.shell.custom( action , "rrdtool update " + F_RRDFILE + " " + X_TS + ":" + X_VALUES );
		 */
		
		long t = Util.getTime();
		Sample sample = rrdDb.createSample( t );
		sample.setValue( "total" , F_STATUSTOTAL );
		sample.setValue( "checkenv" , F_ENVTOTAL );
		sample.setValue( "checkenv-time" , info.timeMajor );
		sample.update();
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
