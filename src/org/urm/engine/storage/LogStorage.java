package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.release.ReleaseDelivery;

public class LogStorage {

	Artefactory artefactory;
	Meta meta;
	public LocalFolder logFolder;
	
	public LogStorage( Artefactory artefactory , Meta meta ) {
		this.artefactory = artefactory;
		this.meta = meta;
	}

	public void prepareReleaseBuildLogFolder( ActionBase action , String release ) throws Exception {
		logFolder = artefactory.getWorkFolder( action , "build/" + action.context.getBuildModeName() + "/" + release );
		logFolder.ensureExists( action );
	}

	public void prepareTagBuildLogFolder( ActionBase action , String TAG ) throws Exception {
		logFolder = artefactory.getWorkFolder( action , "build/" + action.context.getBuildModeName() + "/tag-" + TAG );
		logFolder.ensureExists( action );
	}

	public void prepareDatabaseLogFolder( ActionBase action , String release ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		String dir = build.CONFIG_LOGPATH + "/db/" + action.context.env.NAME + "/" + release + "-" + Common.getNameTimeStamp();  
		logFolder = artefactory.getAnyFolder( action , dir );
		logFolder.ensureExists( action );
	}

	public LocalFolder getDatabaseLogReleaseCopyFolder( ActionBase action ) throws Exception {
		return( logFolder.getSubFolder( action , "dist" ) );
	}
	
	public LocalFolder getDatabaseLogReleaseCopyFolder( ActionBase action , ReleaseDelivery releaseDelivery ) throws Exception {
		return( logFolder.getSubFolder( action , "dist-" + releaseDelivery.distDelivery.NAME ) );
	}
	
	public LocalFolder getDatabaseLogReleaseCopyFolder( ActionBase action , MetaEnvServer server , ReleaseDelivery releaseDelivery , String version ) throws Exception {
		return( logFolder.getSubFolder( action , "dist-" + server.sg.NAME + "-" + server.NAME + "-" + releaseDelivery.distDelivery.NAME + "-" + version ) );
	}
	
	public LocalFolder getDatabaseLogExecuteFolder( ActionBase action , MetaEnvServer server ) throws Exception {
		return( logFolder.getSubFolder( action , "run-" + server.sg.NAME + "-" + server.NAME ) );
	}
	
	public LocalFolder getDatabaseLogExecuteFolder( ActionBase action , MetaEnvServer server , ReleaseDelivery releaseDelivery , String version ) throws Exception {
		return( logFolder.getSubFolder( action , "run-" + server.sg.NAME + "-" + server.NAME + "-" + releaseDelivery.distDelivery.NAME  + "-" + version ) );
	}
	
}
