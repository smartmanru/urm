package org.urm.db.upgrade;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.EngineLoader;

public abstract class DBUpgradeSet {

	public int versionFrom;
	public int versionTo;
	abstract public void upgrade( EngineLoader loader ) throws Exception;
	
	public DBUpgradeSet( int versionFrom , int versionTo ) {
		this.versionFrom = versionFrom;
		this.versionTo = versionTo;
	}

	public void applyScripts( EngineLoader loader , int version ) throws Exception {
		ActionBase action = loader.getAction();
		UrmStorage storage = action.artefactory.getUrmStorage();
		LocalFolder folder = storage.getServerInstallFolder( action );
		LocalFolder versionFolder = folder.getSubFolder( action , "database/upgrade/" + version );
		if( !versionFolder.checkExists( action ) )
			Common.exitUnexpected();
		
		String[] files = versionFolder.listFilesSorted();
		for( String filename : files )
			applyScript( loader , version , filename );
	}
	
	public void applyScript( EngineLoader loader , int version , String filename ) throws Exception {
		ActionBase action = loader.getAction();
		UrmStorage storage = action.artefactory.getUrmStorage();
		LocalFolder folder = storage.getServerInstallFolder( action );
		LocalFolder versionFolder = folder.getSubFolder( action , "database/upgrade/" + version );
		if( !versionFolder.checkFileExists( action , filename ) )
			Common.exitUnexpected();
		
		DBConnection c = loader.getConnection();
		String data = versionFolder.readFile( action , filename );
		String[] ops = Common.split( data , ";" );
		
		for( String op : ops ) {
			op = Common.trim( op , '\n' );
			if( op.isEmpty() )
				continue;
			
			if( !c.modify( op ) )
				Common.exitUnexpected();
		}
	}
	
}
