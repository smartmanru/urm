package org.urm.action.monitor;

import java.awt.Color;
import java.awt.image.BufferedImage;

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
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaMonitoringTarget;

public class MonitorTargetInfo {

	public Meta meta;
	public MetaMonitoringTarget target;
	public MonitoringStorage storage;
	
	public long timeMajor;
	public boolean statusMajor;
	public boolean statusMinor;
	
	RrdDb rrdDb;
	boolean rrdDbFail;
	String F_RRDFILE;
	
	public MonitorTargetInfo( MetaMonitoringTarget target , MonitoringStorage storage ) {
		this.target = target;
		this.meta = target.meta;
		this.storage = storage;
		rrdDbFail = false;
	}
	
	public void setLastMajor( boolean statusMajor , long timeMajor ) throws Exception {
		this.statusMajor = statusMajor;
		this.timeMajor = timeMajor; 
	}
	
	public void setLastMinor( boolean statusMinor ) throws Exception {
		this.statusMinor = statusMinor;
	}

	public void stop( ActionBase action ) {
		try {
			if( rrdDb != null && rrdDbFail == false ) {
				rrdDb.close();
				rrdDb = null;
			}
		}
		catch( Throwable e ) {
			action.log( "MonitorTargetInfo" , e );
		}
	}
	
	public void createRrdFile( ActionBase action , String fname ) throws Exception {
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
		LocalFolder folder = action.getLocalFolder( Common.getDirName( fname ) );
		folder.ensureExists( action );
		String rrdFile = action.getLocalPath( fname );
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
		
		rrdDb = new RrdDb( rrdFile );
	}

	public boolean openRrdFile( ActionBase action , MonitoringStorage storage ) throws Exception {
		if( rrdDbFail )
			return( false );
		
		if( rrdDb == null ) {
			LocalFolder dataFolder = storage.getDataFolder( action , target );
			F_RRDFILE = dataFolder.getFilePath( action , storage.getRrdFile( target ) );
			
			rrdDbFail = true;
			if( !action.shell.checkFileExists( action , F_RRDFILE ) )
				createRrdFile( action , F_RRDFILE );
			else
				rrdDb = new RrdDb( F_RRDFILE );
			rrdDbFail = false;
		}
		
		return( true );
	}
	
	public void addCheckEnvData( ActionBase action , long timeMillis , boolean status ) throws Exception {
		action.info( "addCheckEnvData: product=" + target.meta.name + ", env=" + target.ENV + ", sg=" + target.SG + 
				", timeMillis=" + timeMillis + ", succeeded:" + Common.getBooleanValue( status ) );
		setLastMajor( status , timeMillis );
	}

	public void addCheckMinorsData( ActionBase action , boolean status ) throws Exception {
		action.info( "addCheckMinorsData: product=" + target.meta.name + ", env=" + target.ENV + ", sg=" + target.SG + 
				", succeeded:" + Common.getBooleanValue( status ) );
		setLastMinor( status );
	}

	public void addHistoryGraph( ActionBase action ) throws Exception {
		// add to totals, update reports
		addRrdRecord( action );
		createHistoryGraph( action );
		updateReport( action );
	}
	
	private RrdGraph createHistoryGraph( ActionBase action ) throws Exception {
		if( rrdDbFail )
			return( null );
		
		LocalFolder dataFolder = storage.getDataFolder( action , target );
		String relativeDataFile = storage.getRrdFile( target );
		dataFolder.ensureFolderExists( action , Common.getDirName( relativeDataFile ) );
		String rrdfile = action.getLocalPath( dataFolder.getFilePath( action , relativeDataFile ) );
		
		LocalFolder reportsFolder = storage.getReportsFolder( action , target );
		String relativeReportFile = storage.getHistoryImageFile( target );
		reportsFolder.ensureFolderExists( action , Common.getDirName( relativeReportFile ) );
		String F_CREATEFILE = action.getLocalPath( reportsFolder.getFilePath( action , relativeReportFile ) );
		
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
			" -t " + Common.getQuoted( target.ENV + ", sg=" + target.SG + " checkenv.sh execution time (0 if not running)" ) +
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
		gDef.setMaxValue( target.maxTimeMajor );
		gDef.setRigid( true );
		gDef.setWidth( 1024 );
		gDef.setHeight( 200 );
		gDef.setVerticalLabel( "Milliseconds" );
		gDef.setAltYGrid( true );
		gDef.setTitle( target.ENV + ", sg=" + target.SG + " check segment execution time (0 if not running)" );
		gDef.setColor( RrdGraphDef.COLOR_GRID , Color.decode( "0xC0C0C0" ) );
		gDef.setColor( RrdGraphDef.COLOR_BACK , Color.decode( "0xE4E4E4" ) );
		gDef.setTimeAxis( RrdGraphDef.MINUTE , 30 , RrdGraphDef.HOUR , 1 , RrdGraphDef.HOUR , 1 , 0 , "%H" );
		gDef.datasource( "linec" , rrdfile , "checkenv-time" , ConsolFun.MIN );
		gDef.datasource( "linea" , rrdfile , "checkenv-time" , ConsolFun.AVERAGE );
		gDef.datasource( "lineb" , rrdfile , "checkenv-time" , ConsolFun.MAX );
		gDef.line( "linea" , Color.decode( "0x0000FF" ) , "Avg" );
		gDef.line( "lineb" , Color.decode( "0xFF0000" ) , "Max" );
		gDef.line( "linec" , Color.decode( "0x00FF00" ) , "Min" );
		gDef.setImageFormat( "png" );
		gDef.setFilename( F_CREATEFILE );
		RrdGraph graph = new RrdGraph( gDef );
		BufferedImage bi = new BufferedImage( 1024 , 200 , BufferedImage.TYPE_INT_RGB );
		graph.render( bi.getGraphics() );
		
		return( graph );
	}

	private void updateReport( ActionBase action ) throws Exception {
		LocalFolder resourceFolder = storage.getResourceFolder( action );
		if( !resourceFolder.checkExists( action ) ) {
			action.trace( "ignore create report due to missing resource folder: " + resourceFolder.folderPath );
			return;
		}
		
		// calculate status
		boolean F_STATUS = ( statusMajor && statusMinor )? true : false;
		
		// form report
		String F_IMAGEFILE;
		String F_IMAGETEXT;
		if( F_STATUS ) {
			F_IMAGEFILE = storage.getRunningImageBasename();
			F_IMAGETEXT = "Environment " + target.ENV + " , sg=" + target.SG + " is up and running";
		}
		else {
			F_IMAGEFILE = storage.getFailedImageBasename();
			F_IMAGETEXT = "Environment " + target.ENV + " , sg=" + target.SG + " is not working";
		}

		LocalFolder reportsFolder = storage.getReportsFolder( action , target );
		String F_REPFILE = reportsFolder.getFilePath( action , storage.getStatusReportFile( target ) );
		
		String F_RESFILE = resourceFolder.getFilePath( action , storage.getStatusReportTemplateFile() );
		String F_RESCONTEXT = storage.getMonitoringUrl();
		
		String template = action.readFile( F_RESFILE );
		template = Common.replace( template , "@IMAGE@" , F_RESCONTEXT + "/" + F_IMAGEFILE );  
		template = Common.replace( template , "@TEXT@" , F_IMAGETEXT );  
		
		Common.createFileFromString( action.execrc , F_REPFILE , template );
	}

	private void addRrdRecord( ActionBase action ) throws Exception {
		if( !openRrdFile( action , storage ) ) {
			action.trace( "unable to open RRD database file: " + F_RRDFILE );
			return;
		}

		String F_RRDFILE_LOG = F_RRDFILE + ".log"; 
		
		int F_STATUSTOTAL = 100;
		int F_ENVTOTAL = 100;
		if( !statusMajor )
			F_ENVTOTAL = 1;
		if( statusMajor == false || statusMinor == false )
			F_STATUSTOTAL = 1;
		
		String X_VALUES = F_STATUSTOTAL + ":" + F_ENVTOTAL + ":" + timeMajor;
		long F_TS = System.currentTimeMillis();
		long X_TS = F_TS / 1000;

		/*
		 * OLD COMMAND SYNTAX 
		if( !action.shell.checkFileExists( action , F_RRDFILE ) )
			createRrdFile( F_RRDFILE );
		 */

		action.trace( "add record to RRD database file: " + F_RRDFILE + " (" + X_TS + ":" + X_VALUES + ")" );
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
		sample.setValue( "checkenv-time" , timeMajor );
		sample.update();
	}
	
}
