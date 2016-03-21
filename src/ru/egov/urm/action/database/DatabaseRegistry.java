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

	public static String getKey( ActionBase action , String file ) throws Exception {
		int index = Common.getIndexOf( file , "-" , 4 );
		if( index < 0 )
			action.exit( "invalid file name " + file );
		return( file.substring( 0 , index ) );
	}
	
	public static String getSchema( ActionBase action , String file ) throws Exception {
		String s = Common.cutItem( file , "-" , 5 );
		if( s.isEmpty() )
			action.exit( "invalid file name=" + file );
		return( s );
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
				"release = " + Common.getSQLQuoted( full ) ); 
	}

	public int getScriptCount( ActionBase action ) throws Exception {
		int n = 0;
		for( Map<String,String> map : deliveryState.values() )
			n += map.size();
		return( n );
	}
	
	public void readIncompleteScripts( ActionBase action ) throws Exception {
		List<String[]> rows = client.readTableData( action , server.admSchema , TABLE_SCRIPTS , 
				"release = " + Common.getSQLQuoted( full ) + " and script_status = 'S'" , 
				new String[] { "delivery" , "filename" } );
		
		deliveryState.clear();
		for( String[] row : rows ) {
			String delivery = row[0];
			Map<String,String> files = deliveryState.get( delivery );
			if( files == null ) {
				files = new HashMap<String,String>();
				deliveryState.put( delivery , files );
			}
			
			String file = row[1];
			String key = getKey( action , file );
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
				new String[] { "release" , "rel_p1" , "rel_p2" , "rel_p3" , "rel_p4" , "begin_apply_time" , "rel_status" } , 
				new String[] { Common.getSQLQuoted( full ) , Common.getSQLQuoted( major1 ) , Common.getSQLQuoted( major2 ) , Common.getSQLQuoted( minor1 ) , Common.getSQLQuoted( minor2 ) , "TIMESTAMP" , Common.getSQLQuoted( "S" ) } );
		}
		else
		if( isReleaseStarted( action ) ) {
			client.updateRow( action , server.admSchema , TABLE_RELEASES ,
					new String[] { "end_apply_time" } , 
					new String[] { "NULL" } ,
					"release = " + Common.getSQLQuoted( full ) ); 
		}
		else
		if( isReleaseFinished( action ) ) {
			if( !action.context.CTX_FORCE )
				action.exit( "release is completely done, use force to reapply" );
			
			releaseStatus = "S";
			client.updateRow( action , server.admSchema , TABLE_RELEASES ,
					new String[] { "rel_status" , "end_apply_time" } , 
					new String[] { Common.getSQLQuoted( releaseStatus ) , "NULL" } ,
					"release = " + Common.getSQLQuoted( full ) ); 
		}
		else
			action.exitUnexpectedState();
	}

	public void finishApplyRelease( ActionBase action ) throws Exception {
		releaseStatus = "A";
		client.updateRow( action , server.admSchema , TABLE_RELEASES ,
				new String[] { "rel_status" , "end_apply_time" } , 
				new String[] { Common.getSQLQuoted( releaseStatus ) , "TIMESTAMP" } ,
				"release = " + Common.getSQLQuoted( full ) ); 
	}
	
	public void readDeliveryState( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		Map<String,String> data = new HashMap<String,String>(); 
		
		// check connect to admin schema
		String[] columns = { "key" , "script_status" };
		List<String[]> rows = client.readTableData( action , server.admSchema , TABLE_SCRIPTS  , 
				"release = " + Common.getSQLQuoted( full ) + " and " +
				"delivery = " + Common.getSQLQuoted( delivery.NAME ) , columns ); 
		
		for( String[] row : rows )
			data.put( row[0] , row[1] );
		
		deliveryState.put( delivery.NAME , data );
	}
	
	public boolean checkNeedApply( ActionBase action , MetaDistrDelivery delivery , String file ) throws Exception {
		Map<String,String> data = deliveryState.get( delivery.NAME );
		String key = getKey( action , file );
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
		String key = getKey( action , file );
		String status = data.get( key );
		
		String schema = getSchema( action , file );
		if( status == null ) {
			client.insertRow( action , server.admSchema , TABLE_SCRIPTS ,
					new String[] { "release" , "delivery" , "key" , "schema" , "filename" , "begin_apply_time" , "script_status" } , 
					new String[] { Common.getSQLQuoted( full ) , Common.getSQLQuoted( delivery.NAME ) , Common.getSQLQuoted( key ) , Common.getSQLQuoted( schema ) , Common.getSQLQuoted( file ) , "TIMESTAMP" , Common.getSQLQuoted( "S" ) } );
		}
		else {
			client.updateRow( action , server.admSchema , TABLE_SCRIPTS ,
					new String[] { "schema" , "filename" , "begin_apply_time" , "end_apply_time" , "script_status" } , 
					new String[] { Common.getSQLQuoted( schema ) , Common.getSQLQuoted( file ) , "TIMESTAMP" , "NULL" , Common.getSQLQuoted( "S" ) } ,
					"release = " + Common.getSQLQuoted( full ) + " and " +
							"delivery = " + Common.getSQLQuoted( delivery.NAME ) + " and " +
							"key = " + Common.getSQLQuoted( key ) ); 
		}
	}
	
	public void finishApplyScript( ActionBase action , MetaDistrDelivery delivery , String file ) throws Exception {
		String key = getKey( action , file );
		
		client.updateRow( action , server.admSchema , TABLE_SCRIPTS ,
				new String[] { "end_apply_time" , "script_status" } , 
				new String[] { "TIMESTAMP" , Common.getSQLQuoted( "A" ) } ,
				"release = " + Common.getSQLQuoted( full ) + " and " +
						"delivery = " + Common.getSQLQuoted( delivery.NAME ) + " and " + 
						"key = " + Common.getSQLQuoted( key ) ); 
	}
	
}
