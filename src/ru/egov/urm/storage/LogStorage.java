package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.meta.Metadata;

public class LogStorage {

	Artefactory artefactory;
	Metadata meta;
	public LocalFolder logFolder;
	
	public LogStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}

	public void prepareReleaseBuildLogFolder( ActionBase action , String release ) throws Exception {
		logFolder = artefactory.getProductFolder( action , "master/makedistr/" + action.context.getBuildModeName() + "/" + release );
		logFolder.ensureExists( action );
	}

	public void prepareTagBuildLogFolder( ActionBase action , String TAG ) throws Exception {
		logFolder = artefactory.getProductFolder( action , "master/makedistr/" + action.context.getBuildModeName() + "/tag-" + TAG );
		logFolder.ensureExists( action );
	}

	public void prepareDatabaseLogFolder( ActionBase action , String release ) throws Exception {
		String dir = action.meta.product.CONFIG_SQL_LOGDIR + "/" + action.context.env.ID + "/" + release + "-" + Common.getNameTimeStamp();  
		logFolder = artefactory.getAnyFolder( action , dir );
		logFolder.ensureExists( action );
	}

	public LocalFolder getDatabaseLogReleaseCopyFolder( ActionBase action ) throws Exception {
		return( logFolder.getSubFolder( action , "dist" ) );
	}
	
	public LocalFolder getDatabaseLogReleaseCopyFolder( ActionBase action , MetaReleaseDelivery releaseDelivery ) throws Exception {
		return( logFolder.getSubFolder( action , "dist-" + releaseDelivery.distDelivery.NAME ) );
	}
	
	public LocalFolder getDatabaseLogReleaseCopyFolder( ActionBase action , MetaEnvServer server , MetaReleaseDelivery releaseDelivery ) throws Exception {
		return( logFolder.getSubFolder( action , "dist-" + server.dc.NAME + "-" + server.NAME + "-" + releaseDelivery.distDelivery.NAME ) );
	}
	
	public LocalFolder getDatabaseLogExecuteFolder( ActionBase action , MetaEnvServer server ) throws Exception {
		return( logFolder.getSubFolder( action , "run-" + server.dc.NAME + "-" + server.NAME ) );
	}
	
	public LocalFolder getDatabaseLogExecuteFolder( ActionBase action , MetaEnvServer server , MetaReleaseDelivery releaseDelivery ) throws Exception {
		return( logFolder.getSubFolder( action , "run-" + server.dc.NAME + "-" + server.NAME + "-" + releaseDelivery.distDelivery.NAME ) );
	}
	
}
