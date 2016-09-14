package org.urm.action.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.database.DatabaseRegistryRelease.RELEASE_STATE;
import org.urm.common.Common;
import org.urm.common.action.CommandOptions.SQLMODE;
import org.urm.engine.dist.Release;
import org.urm.engine.meta.MetaDistrDelivery;
import org.urm.engine.meta.MetaEnvServer;

public class DatabaseRegistry {

	// script file name: A<alignedid>-T<type>-I<instance>-{ZZ|RR}-<index>-<schema>-<any>.sql
	
	MetaEnvServer server;
	String RELEASEVER;
	
	DatabaseClient client;
	
	String major1;
	String major2;
	String minor1;
	String minor2;
	
	String releaseStatus;
	Map<String,Map<String,String>> deliveryState;
	
	static String TABLE_RELEASES = "adm_releases"; 
	static String TABLE_SCRIPTS = "adm_scripts"; 

	static String RELEASE_STATUS_STARTED = "S";
	static String RELEASE_STATUS_APPLIED = "A";
	static String SCRIPT_STATUS_STARTED = "S";
	static String SCRIPT_STATUS_APPLIED = "A";
	
	private DatabaseRegistry( DatabaseClient client ) {
		this.client = client;
		this.server = client.specific.server;
		deliveryState = new HashMap<String,Map<String,String>>();
	}

	public static DatabaseRegistry getRegistry( ActionBase action , DatabaseClient client ) throws Exception {
		DatabaseRegistry registry = new DatabaseRegistry( client );
		return( registry );
	}
	
	private void parseReleaseNumber( ActionBase action , String RELEASEVER ) throws Exception {
		this.RELEASEVER = RELEASEVER;
		
		String[] parts = Common.splitDotted( RELEASEVER );
		if( parts.length != 4 )
			action.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );

		major1 = parts[0];
		major2 = parts[1];
		minor1 = parts[2]; 
		minor2 = parts[3]; 
	}

	private void readReleaseStatus( ActionBase action ) throws Exception {
		releaseStatus = client.readCellValue( action , server.admSchema , TABLE_RELEASES , "zrel_status" , 
				"zrelease = " + Common.getSQLQuoted( RELEASEVER ) ); 
	}

	public int getScriptCount( ActionBase action ) throws Exception {
		int n = 0;
		for( Map<String,String> map : deliveryState.values() )
			n += map.size();
		return( n );
	}
	
	public void readIncompleteScripts( ActionBase action ) throws Exception {
		List<String[]> rows = client.readTableData( action , server.admSchema , TABLE_SCRIPTS , 
				"zrelease = " + Common.getSQLQuoted( RELEASEVER ) + 
				" and zscript_status = " + Common.getSQLQuoted( RELEASE_STATUS_STARTED ) , 
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
	
	public String[] getStateDeliveries( ActionBase action ) throws Exception {
		return( Common.getSortedKeys( deliveryState ) );
	}
	
	public Map<String,String> getStateData( ActionBase action , String delivery ) throws Exception {
		return( deliveryState.get( delivery ) );
	}
	
	public boolean isReleaseUnknown( ActionBase action ) throws Exception {
		return( releaseStatus == null || releaseStatus.isEmpty() );
	}
	
	public boolean isReleaseStarted( ActionBase action ) throws Exception {
		return( releaseStatus.equals( RELEASE_STATUS_STARTED ) );
	}
	
	public boolean isReleaseFinished( ActionBase action ) throws Exception {
		return( releaseStatus.equals( RELEASE_STATUS_APPLIED ) );
	}

	public void setActiveRelease( ActionBase action , String version ) throws Exception {
		parseReleaseNumber( action , version );
		readReleaseStatus( action );
	}
	
	public boolean startApplyRelease( ActionBase action , Release release ) throws Exception {
		parseReleaseNumber( action , release.RELEASEVER );
		
		// check last release if not forced
		if( !action.context.CTX_FORCE ) {
			if( !checkLastRelease( action , release ) )
				return( false );
		}
		
		// check current release state
		readReleaseStatus( action );
		if( isReleaseUnknown( action ) ) {
			client.insertRow( action , server.admSchema , TABLE_RELEASES ,
				new String[] { "zrelease" , "zrel_p1" , "zrel_p2" , "zrel_p3" , "zrel_p4" , "zbegin_apply_time" , "zrel_status" } , 
				new String[] { Common.getSQLQuoted( RELEASEVER ) , Common.getSQLQuoted( major1 ) , Common.getSQLQuoted( major2 ) , Common.getSQLQuoted( minor1 ) , Common.getSQLQuoted( minor2 ) , "TIMESTAMP" , Common.getSQLQuoted( RELEASE_STATUS_STARTED ) } , 
				true );
		}
		else
		if( isReleaseStarted( action ) ) {
			client.updateRow( action , server.admSchema , TABLE_RELEASES ,
					new String[] { "zend_apply_time" } , 
					new String[] { "NULL" } ,
					"zrelease = " + Common.getSQLQuoted( RELEASEVER ) ,
					true ); 
		}
		else
		if( isReleaseFinished( action ) ) {
			if( !action.context.CTX_FORCE ) {
				action.error( "release is completely done, use -force to reapply" );
				return( false );
			}
			
			releaseStatus = RELEASE_STATUS_STARTED;
			client.updateRow( action , server.admSchema , TABLE_RELEASES ,
					new String[] { "zrel_status" , "zend_apply_time" } , 
					new String[] { Common.getSQLQuoted( releaseStatus ) , "NULL" } ,
					"zrelease = " + Common.getSQLQuoted( RELEASEVER ) ,
					true ); 
		}
		else
			action.exit1( _Error.UnexpectedReleaseStatus1 , "unexpected release status=" + releaseStatus , releaseStatus );
		
		return( true );
	}

	public boolean checkLastRelease( ActionBase action , Release release ) throws Exception {
		DatabaseRegistryRelease last = getLastReleaseInfo( action );
		
		// no last
		if( last.state == RELEASE_STATE.UNKNOWN ) {
			action.debug( "last release - no information, ignore checks" );
			return( true );
		}
		
		// last is current
		if( last.version.equals( release.RELEASEVER ) ) {
			action.debug( "last release reapply" );
			return( true );
		}
		
		// check last is completed
		if( last.state != RELEASE_STATE.FINISHED ) {
			action.error( "last release is not finilized, please complete (state=" + Common.getEnumLower( last.state ) + ")" );
			return( false );
		}
		
		// check compatibility
		if( !release.isCompatible( action , last.version ) ) {
			action.error( "last release=" + last.version + " is not compatible with current, compatibility list={" + release.PROPERTY_COMPATIBILITY + "}" );
			return( false );
		}
	
		return( true );
	}
	
	public void finishApplyRelease( ActionBase action ) throws Exception {
		releaseStatus = RELEASE_STATUS_APPLIED;
		client.updateRow( action , server.admSchema , TABLE_RELEASES ,
				new String[] { "zrel_status" , "zend_apply_time" } , 
				new String[] { Common.getSQLQuoted( releaseStatus ) , "TIMESTAMP" } ,
				"zrelease = " + Common.getSQLQuoted( RELEASEVER ) ,
				true ); 
	}
	
	public void finishReleaseState( ActionBase action ) throws Exception {
		readIncompleteScripts( action );
		if( deliveryState.isEmpty() )
			finishApplyRelease( action );
		else
			action.debug( "there are incomplete scripts" );
	}
	
	public void readDeliveryState( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		Map<String,String> data = new HashMap<String,String>(); 
		
		// check connect to admin schema
		String[] columns = { "zkey" , "zscript_status" };
		List<String[]> rows = client.readTableData( action , server.admSchema , TABLE_SCRIPTS  , 
				"zrelease = " + Common.getSQLQuoted( RELEASEVER ) + " and " +
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
			
			action.info( "script " + file + " is new. Skipped" );
			return( false );
		}
		
		if( status.equals( "S" ) ) {
			if( action.context.CTX_DBMODE == SQLMODE.ANYWAY ||
				action.context.CTX_DBMODE == SQLMODE.CORRECT )
				return( true );
			
			action.info( "script " + file + " is already applied with errors. Skipped" );
			return( false );
		}
		
		if( status.equals( RELEASE_STATUS_APPLIED ) ) {
			if( action.context.CTX_DBMODE == SQLMODE.ANYWAY )
				return( true );
			
			action.info( "script " + file + " is already successfully applied. Skipped" );
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
					new String[] { Common.getSQLQuoted( RELEASEVER ) , Common.getSQLQuoted( delivery.NAME ) , Common.getSQLQuoted( key ) , Common.getSQLQuoted( schema ) , Common.getSQLQuoted( file ) , "TIMESTAMP" , Common.getSQLQuoted( SCRIPT_STATUS_STARTED ) } ,
					true );
		}
		else {
			res = client.updateRow( action , server.admSchema , TABLE_SCRIPTS ,
					new String[] { "zschema" , "zfilename" , "zbegin_apply_time" , "zend_apply_time" , "zscript_status" } , 
					new String[] { Common.getSQLQuoted( schema ) , Common.getSQLQuoted( file ) , "TIMESTAMP" , "NULL" , Common.getSQLQuoted( SCRIPT_STATUS_STARTED ) } ,
					"zrelease = " + Common.getSQLQuoted( RELEASEVER ) + " and " +
							"zdelivery = " + Common.getSQLQuoted( delivery.NAME ) + " and " +
							"zkey = " + Common.getSQLQuoted( key ) ,
							true ); 
		}
		
		if( !res )
			action.ifexit( _Error.UnableRegisterExecution1 , "unable to register script execution: " + file , new String[] { file } );
	}

	public void correctScript( ActionBase action , String delivery , String key ) throws Exception {
		Map<String,String> data = deliveryState.get( delivery );
		if( data == null )
			action.exitUnexpectedState();
		String status = data.get( key );
		if( status == null )
			action.exitUnexpectedState();
		
		finishApplyScript( action , delivery , key , SCRIPT_STATUS_APPLIED );
	}
	
	public void finishApplyScript( ActionBase action , MetaDistrDelivery delivery , String file ) throws Exception {
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( action , file );
		String key = dsf.getDistKey();
		finishApplyScript( action , delivery.NAME , key , SCRIPT_STATUS_APPLIED );
	}
		
	private void finishApplyScript( ActionBase action , String delivery , String key , String status ) throws Exception {
		boolean res = client.updateRow( action , server.admSchema , TABLE_SCRIPTS ,
				new String[] { "zend_apply_time" , "zscript_status" } , 
				new String[] { "TIMESTAMP" , Common.getSQLQuoted( status ) } ,
				"zrelease = " + Common.getSQLQuoted( RELEASEVER ) + " and " +
						"zdelivery = " + Common.getSQLQuoted( delivery ) + " and " + 
						"zkey = " + Common.getSQLQuoted( key ) ,
				true );
		if( !res ) {
			String msg = "unable to register script execution: " + key ;
			if( action.context.CTX_FORCE )
				action.error( msg + ", ignored." );
			else
				action.exit1( _Error.UnableRegisterExecution1 , msg , key );
		}
	}

	public RELEASE_STATE getReleaseState( ActionBase action , String value ) throws Exception {
		if( value == null || value.isEmpty() )
			return( RELEASE_STATE.UNKNOWN );
		if( value.equals( "S" ) )
			return( RELEASE_STATE.STARTED );
		if( value.equals( "A" ) )
			return( RELEASE_STATE.FINISHED );
		action.exitUnexpectedState();
		return( null );
	}
	
	public DatabaseRegistryRelease getLastReleaseInfo( ActionBase action ) throws Exception {
		return( getReleaseInfo( action , null ) );
	}

	public DatabaseRegistryRelease getReleaseInfo( ActionBase action ) throws Exception {
		return( getReleaseInfo( action , RELEASEVER ) );
	}
	
	public DatabaseRegistryRelease getReleaseInfo( ActionBase action , String version ) throws Exception {
		String query = null;
		if( version == null || version.isEmpty() ) 
			query = "select 'c=' || zrelease || '|c=' || zrel_status from " + TABLE_RELEASES +
				" where zrelease = ( select max( zrelease ) from " + TABLE_RELEASES + " )";
		else
			query = "select 'c=' || zrelease || '|c=' || zrel_status from " + TABLE_RELEASES +
				" where zrelease = '" + version + "'";
		
		List<String[]> rows = client.readSelectData( action , server.admSchema , query );
		
		DatabaseRegistryRelease release = new DatabaseRegistryRelease();  
		if( rows.isEmpty() ) {
			release.version = "";
			release.state = RELEASE_STATE.UNKNOWN;
			return( release );
		}
		
		String[] row = rows.get( 0 );
		if( rows.size() != 1 && row.length != 2 )
			action.exitUnexpectedState();
		
		release.version = row[0];
		release.state = getReleaseState( action , row[1] );
		return( release );
	}

	public void dropRelease( ActionBase action ) throws Exception {
		client.deleteRows( action , server.admSchema , TABLE_SCRIPTS , 
				"zrelease = " + Common.getSQLQuoted( RELEASEVER ) , false );
		client.deleteRows( action , server.admSchema , TABLE_RELEASES , 
				"zrelease = " + Common.getSQLQuoted( RELEASEVER ) , true );
	}

	public void dropReleaseDelivery( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		client.deleteRows( action , server.admSchema , TABLE_SCRIPTS , 
				"zdelivery = " + Common.getSQLQuoted( delivery.NAME ) , true );
	}
	
	public void dropReleaseDeliveryItems( ActionBase action , MetaDistrDelivery delivery , String[] keys ) throws Exception {
		String list = "";
		for( String key : keys )
			list = Common.addToList( list , Common.getSQLQuoted( key ) , " , " );
		
		client.deleteRows( action , server.admSchema , TABLE_SCRIPTS , 
				"zdelivery = " + Common.getSQLQuoted( delivery.NAME ) + " and " +
				"zkey in ( " + list + " )" , 
				true );
	}
	
}
