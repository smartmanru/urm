package ru.egov.urm.run.database;

import ru.egov.urm.conf.ConfBuilder;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.LogStorage;

public class ActionApplyManual extends ActionBase {

	DistStorage release;
	MetaEnvServer server;
	
	public ActionApplyManual( ActionBase action , String stream , DistStorage release , MetaEnvServer server ) {
		super( action , stream );
		this.release = release;
		this.server = server;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		log( "apply manual database items ..." );
		LogStorage logs = artefactory.getDatabaseLogStorage( this , release.info.RELEASEVER );
		log( "log to " + logs.logFolder.folderPath );
		
		LocalFolder logReleaseCopy = logs.getDatabaseLogReleaseCopyFolder( this );
		LocalFolder logReleaseExecute = logs.getDatabaseLogExecuteFolder( this , server );
		logReleaseCopy.ensureExists( this );
		logReleaseExecute.ensureExists( this );
		
		DatabaseClient client = new DatabaseClient( server );
		if( !client.checkConnect( this ) )
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
		builder.parseConfigParameters( logReleaseExecute , file , server );
	}
	
}
