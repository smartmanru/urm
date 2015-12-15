package ru.egov.urm.run.database;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.LogStorage;

public class ActionApplyAutomatic extends ActionBase {

	DistStorage release;
	MetaReleaseDelivery delivery;
	String indexScope;
	
	public ActionApplyAutomatic( ActionBase action , String stream , DistStorage release , MetaReleaseDelivery delivery , String indexScope ) {
		super( action , stream );
		this.release = release;
		this.delivery = delivery;
		this.indexScope = indexScope;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		DatabaseClient client = new DatabaseClient( server );
		if( !client.checkConnect( this ) )
			exit( "unable to connect to server=" + server.NAME );
		
		log( "apply changes to database=" + server.NAME );

		for( MetaReleaseDelivery releaseDelivery : release.info.getDeliveries( this ).values() ) {
			if( delivery == null || delivery == releaseDelivery )
				applyDelivery( server , releaseDelivery );
		}

		log( "apply done." );
		
		return( true );
	}

	private void applyDelivery( MetaEnvServer server , MetaReleaseDelivery releaseDelivery ) throws Exception {
		LogStorage logs = artefactory.getDatabaseLogStorage( this , release.info.RELEASEVER );
		LocalFolder logReleaseCopy = logs.getDatabaseLogReleaseCopyFolder( this , releaseDelivery );
		LocalFolder logReleaseExecute = logs.getDatabaseLogExecuteFolder( this , server , releaseDelivery );
		
		createRunSet( server , releaseDelivery , logReleaseCopy );
		executeRunSet( server , releaseDelivery , logReleaseCopy , logReleaseExecute );
	}

	private void createRunSet( MetaEnvServer server , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseCopy ) throws Exception {
	}
	
	private void executeRunSet( MetaEnvServer server , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseCopy , LocalFolder logReleaseExecute ) throws Exception {
	}
	
}
