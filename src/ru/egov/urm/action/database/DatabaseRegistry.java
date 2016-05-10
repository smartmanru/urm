package ru.egov.urm.action.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.CommandOptions.SQLMODE;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaRelease;

public class DatabaseRegistry {

	// script file name: A<alignedid>-T<type>-I<instance>-{ZZ|RR}-<index>-<schema>-<any>.sql
	
	MetaEnvServer server;
	MetaRelease release;
	DatabaseClient client;
	
	String major1;
	String major2;
	String minor1;
	String minor2;
	String full;
	
	String releaseStatus;
	Map<String,Map<String,String>> deliveryState;
	
	static String TABLE_RELEASES = "adm_releases"; 
	static String TABLE_SCRIPTS = "adm_scripts"; 
	
	private DatabaseRegistry( DatabaseClient client , MetaRelease release ) {
		this.client = client;
		this.server = client.specific.server;
		this.release = release;
		deliveryState = new HashMap<String,Map<String,String>>();
	}

	public static DatabaseRegistry getRegistry( ActionBase action , DatabaseClient client , MetaRelease release ) throws Exception {
		DatabaseRegistry registry = new DatabaseRegistry( client , release );
		registry.readReleaseState( action );
		return( registry );
	}
	
	private void parseReleaseNumber( ActionBase action ) throws Exception {
		String[] parts = Common.splitDotted( release.RELEASEVER );
		if( parts.length < 2 || parts.length > 4 )
			action.exit( "invalid release version=" + release.RELEASEVER );

		major1 = parts[0];
		major2 = parts[1];
		minor1 = ( parts.length >= 3 )? parts[2] : "0"; 
		minor2 = ( parts.length == 4 )? parts[3] : "0"; 

		full = major1 + "." + major2 + "." + minor1 + "." + minor2;
	}

	private void readReleaseStatus( ActionBase action ) throws Exception {
		releaseStatus = client.readCellValue( action , server.admSchema , TABLE_RELEASES , "zrel_status" , 
				"zrelease = " + Common.getSQLQuoted( full ) ); 
	}

	public int getScriptCount( ActionBase action ) throws Exception {
		int n = 0;
		for( Map<String,String> map : deliveryState.values() )
			n += map.size();
		return( n );
	}
	
	public void readIncompleteScripts( ActionBase action ) throws Exception {
		List<String[]> rows = client.readTableData( action , server.admSchema , TABLE_SCRIPTS , 
				"zrelease = " + Common.getSQLQuoted( full ) + " and zscript_status = 'S'" , 
				new String[] { "zdelivery" , "zfilename" } );
		
		deliveryState.clear();
		for( String[] row : rows ) {
			String delivery = row[0];
			Map<String,String> files = deliveryState.get( delivery );
			if( files == null ) {
				files = new HashMap<String,String>();
				deliveryState.put( delivery , files );
			}
			
			String file = row[1];
			DatabaseScriptFile dsf = new DatabaseScriptFile();
			dsf.setDistFile( action , file );
			String key = dsf.getDistKey();
			files.put( key , file );
		}
	}
	
	private void readReleaseState( ActionBase action ) throws Exception {
		parseReleaseNumber( action );
		readReleaseStatus( action );
	}

	public boolean isReleaseUnknown( ActionBase action ) throws Exception {
		return( releaseStatus == null || releaseStatus.isEmpty() );
	}
	
	public boolean isReleaseStarted( ActionBase action ) throws Exception {
		return( releaseStatus.equals( "S" ) );
	}
	
	public boolean isReleaseFinished( ActionBase action ) throws Exception {
		return( releaseStatus.equals( "A" ) );
	}
	
	public void startApplyRelease( ActionBase action ) throws Exception {
		// check current release state
		if( isReleaseUnknown( action ) ) {
			client.insertRow( action , server.admSchema , TABLE_RELEASES ,
				new String[] { "zrelease" , "zrel_p1" , "zrel_p2" , "zrel_p3" , "zrel_p4" , "zbegin_apply_time" , "zrel_status" } , 
				new String[] { Common.getSQLQuoted( full ) , Common.getSQLQuoted( major1 ) , Common.getSQLQuoted( major2 ) , Common.getSQLQuoted( minor1 ) , Common.getSQLQuoted( minor2 ) , "TIMESTAMP" , Common.getSQLQuoted( "S" ) } );
		}
		else
		if( isReleaseStarted( action ) ) {
			client.updateRow( action , server.admSchema , TABLE_RELEASES ,
					new String[] { "zend_apply_time" } , 
					new String[] { "NULL" } ,
					"zrelease = " + Common.getSQLQuoted( full ) ); 
		}
		else
		if( isReleaseFinished( action ) ) {
			if( !action.context.CTX_FORCE )
				action.exit( "release is completely done, use force to reapply" );
			
			releaseStatus = "S";
			client.updateRow( action , server.admSchema , TABLE_RELEASES ,
					new String[] { "zrel_status" , "zend_apply_time" } , 
					new String[] { Common.getSQLQuoted( releaseStatus ) , "NULL" } ,
					"zrelease = " + Common.getSQLQuoted( full ) ); 
		}
		else
			action.exit( "unexpected release status=" + releaseStatus );
	}

	public void finishApplyRelease( ActionBase action ) throws Exception {
		releaseStatus = "A";
		client.updateRow( action , server.admSchema , TABLE_RELEASES ,
				new String[] { "zrel_status" , "zend_apply_time" } , 
				new String[] { Common.getSQLQuoted( releaseStatus ) , "TIMESTAMP" } ,
				"zrelease = " + Common.getSQLQuoted( full ) ); 
	}
	
	public void readDeliveryState( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		Map<String,String> data = new HashMap<String,String>(); 
		
		// check connect to admin schema
		String[] columns = { "zkey" , "zscript_status" };
		List<String[]> rows = client.readTableData( action , server.admSchema , TABLE_SCRIPTS  , 
				"zrelease = " + Common.getSQLQuoted( full ) + " and " +
				"zdelivery = " + Common.getSQLQuoted( delivery.NAME ) , columns ); 
		
		for( String[] row : rows )
			data.put( row[0] , row[1] );
		
		deliveryState.put( delivery.NAME , data );
	}
	
	public boolean checkNeedApply( ActionBase action , MetaDistrDelivery delivery , String file ) throws Exception {
		Map<String,String> data = deliveryState.get( delivery.NAME );
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( action , file );
		String key = dsf.getDistKey();
		String status = data.get( key );
		
		if( status == null ) {
			if( action.context.CTX_DBMODE == SQLMODE.ANYWAY ||
				action.context.CTX_DBMODE == SQLMODE.APPLY )
				return( true );
			
			action.log( "script " + file + " is new. Skipped" );
			return( false );
		}
		
		if( status.equals( "S" ) ) {
			if( action.context.CTX_DBMODE == SQLMODE.ANYWAY ||
				action.context.CTX_DBMODE == SQLMODE.CORRECT )
				return( true );
			
			action.log( "script " + file + " is already applied with errors. Skipped" );
			return( false );
		}
		
		if( status.equals( "A" ) ) {
			if( action.context.CTX_DBMODE == SQLMODE.ANYWAY )
				return( true );
			
			action.log( "script " + file + " is already successfully applied. Skipped" );
			return( false );
		}
		
		action.exitUnexpectedState();
		return( false );
	}

	public void startApplyScript( ActionBase action , MetaDistrDelivery delivery , String file ) throws Exception {
		Map<String,String> data = deliveryState.get( delivery.NAME );
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( action , file );
		String key = dsf.getDistKey();
		String status = data.get( key );
		
		String schema = dsf.SRCSCHEMA;
		boolean res = false;
		if( status == null ) {
			res = client.insertRow( action , server.admSchema , TABLE_SCRIPTS ,
					new String[] { "zrelease" , "zdelivery" , "zkey" , "zschema" , "zfilename" , "zbegin_apply_time" , "zscript_status" } , 
					new String[] { Common.getSQLQuoted( full ) , Common.getSQLQuoted( delivery.NAME ) , Common.getSQLQuoted( key ) , Common.getSQLQuoted( schema ) , Common.getSQLQuoted( file ) , "TIMESTAMP" , Common.getSQLQuoted( "S" ) } );
		}
		else {
			res = client.updateRow( action , server.admSchema , TABLE_SCRIPTS ,
					new String[] { "zschema" , "zfilename" , "zbegin_apply_time" , "zend_apply_time" , "zscript_status" } , 
					new String[] { Common.getSQLQuoted( schema ) , Common.getSQLQuoted( file ) , "TIMESTAMP" , "NULL" , Common.getSQLQuoted( "S" ) } ,
					"release = " + Common.getSQLQuoted( full ) + " and " +
							"zdelivery = " + Common.getSQLQuoted( delivery.NAME ) + " and " +
							"zkey = " + Common.getSQLQuoted( key ) ); 
		}
		
		if( !res ) {
			String msg = "unable to register script execution: " + file ;
			if( action.context.CTX_FORCE )
				action.log( msg + ", ignored." );
			else
				action.exit( msg );
		}
	}
	
	public void finishApplyScript( ActionBase action , MetaDistrDelivery delivery , String file ) throws Exception {
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( action , file );
		String key = dsf.getDistKey();
		
		boolean res = client.updateRow( action , server.admSchema , TABLE_SCRIPTS ,
				new String[] { "zend_apply_time" , "zscript_status" } , 
				new String[] { "TIMESTAMP" , Common.getSQLQuoted( "A" ) } ,
				"release = " + Common.getSQLQuoted( full ) + " and " +
						"zdelivery = " + Common.getSQLQuoted( delivery.NAME ) + " and " + 
						"zkey = " + Common.getSQLQuoted( key ) );
		if( !res ) {
			String msg = "unable to register script execution: " + file ;
			if( action.context.CTX_FORCE )
				action.log( msg + ", ignored." );
			else
				action.exit( msg );
		}
	}
	
}
