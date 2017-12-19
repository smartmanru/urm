package org.urm.db.upgrade;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.meta.EngineLoader;

public class DBUpgrade {

	private static DBUpgradeSet[] getUpgrades() {
		return( 
			new DBUpgradeSet[] {
				new DBUpgradeSet_102() ,
				new DBUpgradeSet_103() 
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
		
		if( indexFrom < 0 )
			Common.exitUnexpected();
		if( indexTo < 0 )
			Common.exitUnexpected();
		
		for( int k = indexFrom; k <= indexTo; k++ ) {
			DBUpgradeSet u = upgrades[ k ];
			if( !upgradeSet( loader , u ) )
				Common.exitUnexpected();
		}
		loader.trace( "upgrade database successfully finished" );
	}
	
	private static boolean upgradeSet( EngineLoader loader , DBUpgradeSet u ) throws Exception {
		DBConnection c = loader.getConnection(); 
		loader.trace( "upgrade database from version=" + u.versionFrom + " to version=" + u.versionTo + " ..." );
		
		try {
			int versionFrom = c.getCurrentAppVersion();
			if( versionFrom != u.versionFrom )
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
	
}
