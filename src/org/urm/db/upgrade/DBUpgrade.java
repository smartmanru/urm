package org.urm.db.upgrade;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action._Error;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.loader.EngineLoader;

public class DBUpgrade {

	private static DBUpgradeSet[] getUpgrades() {
		return( 
			new DBUpgradeSet[] {
				} );
	};
	
	public static void upgrade( EngineLoader loader , int appVersion , int dbVersion ) throws Exception {
		if( appVersion <= dbVersion )
			Common.exitUnexpected();
		
		int indexFrom = -1;
		int indexTo = -1;
		
		DBUpgradeSet[] upgrades = getUpgrades();
		for( int k = 0; k < upgrades.length; k++ ) {
			DBUpgradeSet u = upgrades[ k ];
			if( u.versionFrom == dbVersion )
				indexFrom = k;
			if( u.versionTo == appVersion )
				indexTo = k;
		}
		
		if( indexFrom < 0 || indexTo < 0 )
			Common.exit2( _Error.UnableToUpgrade2 , "Unable to upgrade database from " + dbVersion + " to " + appVersion , "" + dbVersion , "" + appVersion );
		
		DBConnection c = loader.getConnection(); 
		for( int k = indexFrom; k <= indexTo; k++ ) {
			DBUpgradeSet u = upgrades[ k ];
			if( !upgradeSet( loader , u , dbVersion ) )
				Common.exitUnexpected();
			
			dbVersion = c.getCurrentAppVersion();
		}
		loader.trace( "upgrade database successfully finished" );
	}
	
	private static boolean upgradeSet( EngineLoader loader , DBUpgradeSet u , int dbVersion ) throws Exception {
		DBConnection c = loader.getConnection(); 
		loader.trace( "upgrade database from version=" + u.versionFrom + " to version=" + u.versionTo + " ..." );
		
		try {
			if( dbVersion != u.versionFrom )
				Common.exitUnexpected();
				
			u.upgrade( loader );
			c.setAppVersion( u.versionTo );
			c.save( true );
			return( true );
		}
		catch( Throwable e ) {
			loader.log( "unable to upgrade" , e );
			c.save( false );
			return( false );
		}
	}

	public static void applyScripts( EngineLoader loader , String installFolder ) throws Exception {
		ActionBase action = loader.getAction();
		UrmStorage storage = action.artefactory.getUrmStorage();
		LocalFolder folder = storage.getServerInstallFolder( action );
		LocalFolder scriptsFolder = folder.getSubFolder( action , installFolder );
		if( !scriptsFolder.checkExists( action ) )
			Common.exitUnexpected();
		
		String[] files = scriptsFolder.listFilesSorted();
		for( String filename : files )
			applyScript( loader , scriptsFolder , filename );
	}
	
	public static void applyScript( EngineLoader loader , LocalFolder scriptsFolder , String filename ) throws Exception {
		ActionBase action = loader.getAction();
		if( !scriptsFolder.checkFileExists( action , filename ) )
			Common.exitUnexpected();
		
		DBConnection c = loader.getConnection();
		List<String> lines = scriptsFolder.readFileLines( action , filename );
		
		// remove comments
		String data = stripComments( lines );
		String[] ops = Common.split( data , ";" );
		
		for( String op : ops ) {
			op = Common.trim( op , '\n' );
			if( op.isEmpty() )
				continue;
			
			if( !c.modify( op , 0 ) )
				Common.exitUnexpected();
		}
	}

	private static String stripComments( List<String> lines ) throws Exception {
		List<String> run = new LinkedList<String>();
		int size = lines.size();
		
		boolean commentMode = false;
		for( int k = 0; k < size; k++ ) {
			String s = lines.get( k );
			String out = "";
			
			// process single line
			while( true ) {
				if( !commentMode ) {
					int index1 = s.indexOf( "--" );
					int index2 = s.indexOf( "/*" );
					
					if( index2 >= 0 ) {
						if( index1 >= 0 && index2 > index1 ) {
							out += s.substring( 0 , index1 );
							break;
						}
						
						out += s.substring( 0 , index2 );
						s = s.substring( index2 );
						commentMode = true;
						continue;
					}
					else {
						if( index1 >= 0 )
							out += s.substring( 0 , index1 );
						else
							out += s;
						break;
					}
				}
			
				if( commentMode ) {
					int index = s.indexOf( "*/" );
					if( index >= 0 ) {
						s = s.substring( index + 2 );
						commentMode = false;
						continue;
					}
					
					break;
				}
			}
			
			if( !out.isEmpty() )
				run.add( out );
		}
		
		return( Common.getListLines( run.toArray( new String[0] ) ) );
	}
	
}
