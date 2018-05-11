package org.urm.action.monitor;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Util;
import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.product.MetaMonitoringTarget;

public class MonitorTargetInfo {

	MetaMonitoringTarget target;
	
	public long timeMajor;
	public boolean statusMajor;
	public boolean statusMinor;
	
	RrdDb rrdDb;
	boolean rrdDbFail;
	String F_RRDFILE;
	
	public MonitorTargetInfo( MetaMonitoringTarget target ) {
		this.target = target;
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
	
}
