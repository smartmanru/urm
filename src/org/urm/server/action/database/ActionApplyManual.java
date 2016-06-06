package org.urm.server.action.database;

import org.urm.dist.Dist;
import org.urm.meta.MetaEnvServer;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionScopeTargetItem;
import org.urm.server.action.conf.ConfBuilder;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.LogStorage;

public class ActionApplyManual extends ActionBase {

	Dist release;
	MetaEnvServer server;
	
	public ActionApplyManual( ActionBase action , String stream , Dist release , MetaEnvServer server ) {
		super( action , stream );
		this.release = release;
		this.server = server;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		info( "apply manual database items ..." );
		LogStorage logs = artefactory.getDatabaseLogStorage( this , release.release.RELEASEVER );
		info( "log to " + logs.logFolder.folderPath );
		
		LocalFolder logReleaseCopy = logs.getDatabaseLogReleaseCopyFolder( this );
		LocalFolder logReleaseExecute = logs.getDatabaseLogExecuteFolder( this , server );
		logReleaseCopy.ensureExists( this );
		logReleaseExecute.ensureExists( this );
		
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit( "unable to connect to server=" + server.NAME );
		
		if( target.itemFull ) {
			String[] manualFiles = release.getManualDatabaseFiles( this );
			for( String file : manualFiles )
				prepareManual( client , logReleaseCopy , logReleaseExecute , file );
		}
		else {
			for( ActionScopeTargetItem item : target.getItems( this ) ) {
				String file = release.findManualDatabaseItemFile( this , item.NAME );
				if( file == null )
					exit( "unable to find manual file index=" + item.NAME );
				
				prepareManual( client , logReleaseCopy , logReleaseExecute , file );
			}
		}

		client.applyManualSet( this , logReleaseExecute );
		return( true );
	}

	private void prepareManual( DatabaseClient client , LocalFolder logReleaseCopy , LocalFolder logReleaseExecute , String file ) throws Exception {
		// copy file from distributive
		release.copyDistDatabaseManualFileToFolder( this , logReleaseCopy , file );
		logReleaseCopy.copyFiles( this , file , logReleaseExecute );
		
		// configure
		ConfBuilder builder = new ConfBuilder( this );
		builder.configureFile( logReleaseExecute , file , server , null , meta.product.charset );
	}
	
}
