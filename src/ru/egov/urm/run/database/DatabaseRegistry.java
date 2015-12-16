package ru.egov.urm.run.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaRelease;
import ru.egov.urm.run.ActionBase;

public class DatabaseRegistry {

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
		this.server = client.server;
		this.release = release; 
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
		releaseStatus = client.readCellValue( action , server.admSchema , TABLE_RELEASES , "rel_status" , 
				"release = " + Common.getSQLQuoted( release.RELEASEVER ) ); 
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
		return( releaseStatus.equals( "F" ) );
	}
	
	public void startApplyRelease( ActionBase action ) throws Exception {
		// check current release state
		if( releaseStatus.isEmpty() ) {
			client.insertRow( action , server.admSchema , TABLE_RELEASES ,
				new String[] { "release" , "rel_p1" , "rel_p2" , "rel_p3" , "rel_p4" , "begin_apply_time" , "rel_status" } , 
				new String[] { Common.getSQLQuoted( full ) , Common.getSQLQuoted( major1 ) , Common.getSQLQuoted( major2 ) , Common.getSQLQuoted( minor1 ) , Common.getSQLQuoted( minor2 ) , "TIMESTAMP" , Common.getSQLQuoted( "S" ) } );
		}
		else
		if( isReleaseStarted( action ) ) {
			client.updateRow( action , server.admSchema , TABLE_RELEASES ,
					new String[] { "begin_apply_time" } , new String[] { "TIMESTAMP" } ,
					"release = " + Common.getSQLQuoted( release.RELEASEVER ) ); 
		}
		else
		if( isReleaseFinished( action ) ) {
			if( !action.options.OPT_FORCE )
				action.exit( "release is completely done, use force to reapply" );
			
			releaseStatus = "S";
			client.updateRow( action , server.admSchema , TABLE_RELEASES ,
					new String[] { "rel_status" , "begin_apply_time" } , new String[] { releaseStatus , "TIMESTAMP" } ,
					"release = " + Common.getSQLQuoted( release.RELEASEVER ) ); 
		}
		else
			action.exitUnexpectedState();
	}
	
	public void readDeliveryState( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		Map<String,String> data = new HashMap<String,String>(); 
		
		// check connect to admin schema
		String[] columns = { "key" , "script_status" };
		List<String[]> rows = client.readTableData( action , server.admSchema , TABLE_SCRIPTS  , 
				"release = " + Common.getSQLQuoted( release.RELEASEVER ) + " and " +
				"delivery = " + Common.getSQLQuoted( delivery.NAME ) , columns ); 
		
		for( String[] row : rows )
			data.put( row[0] , row[2] );
		
		deliveryState.put( delivery.NAME , data );
	}
	
	public void runBeforeScript( ActionBase action , MetaDistrDelivery delivery , String fileName ) throws Exception {
	}
	
}
