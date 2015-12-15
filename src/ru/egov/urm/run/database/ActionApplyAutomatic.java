package ru.egov.urm.run.database;

import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.CommandOptions.SQLTYPE;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.LogStorage;

public class ActionApplyAutomatic extends ActionBase {

	DistStorage dist;
	MetaReleaseDelivery delivery;
	String indexScope;
	
	public ActionApplyAutomatic( ActionBase action , String stream , DistStorage dist , MetaReleaseDelivery delivery , String indexScope ) {
		super( action , stream );
		this.dist = dist;
		this.delivery = delivery;
		this.indexScope = indexScope;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		DatabaseClient client = new DatabaseClient( server );
		if( !client.checkConnect( this ) )
			exit( "unable to connect to server=" + server.NAME );
		
		log( "apply changes to database=" + server.NAME );

		Map<String,MetaDatabaseSchema> schemaSet = server.getSchemaSet( this );
		boolean done = false;
		for( MetaReleaseDelivery releaseDelivery : dist.info.getDeliveries( this ).values() ) {
			if( delivery == null || delivery == releaseDelivery )
				if( applyDelivery( server , releaseDelivery , schemaSet ) )
					done = true;
		}

		if( done )
			log( "apply done." );
		else
			log( "nothing to apply." );
		
		return( true );
	}

	private boolean applyDelivery( MetaEnvServer server , MetaReleaseDelivery releaseDelivery , Map<String,MetaDatabaseSchema> schemaSet ) throws Exception {
		LogStorage logs = artefactory.getDatabaseLogStorage( this , dist.info.RELEASEVER );
		LocalFolder logReleaseCopy = logs.getDatabaseLogReleaseCopyFolder( this , releaseDelivery );
		LocalFolder logReleaseExecute = logs.getDatabaseLogExecuteFolder( this , server , releaseDelivery );
		
		if( !createRunSet( server , releaseDelivery , logReleaseCopy , schemaSet ) )
			return( false );
		
		executeRunSet( server , releaseDelivery , logReleaseCopy , logReleaseExecute );
		return( true );
	}

	private boolean createRunSet( MetaEnvServer server , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseCopy , Map<String,MetaDatabaseSchema> schemaSet ) throws Exception {
		String distFolder = dist.getDeliveryDatabaseScriptFolder( this , delivery.distDelivery );
		FileSet files = dist.getFiles( this );
		FileSet deliveryFiles = files.getDirByPath( this , distFolder );
		if( deliveryFiles == null ) {
			trace( "script directory " + distFolder + " is not found" );
			return( false );
		}

		// copy scripts
		boolean copy = false;
		LocalFolder scriptFolder = logReleaseCopy.getSubFolder( this , "scripts" );
		for( String file : deliveryFiles.files.keySet() ) {
			if( checkApplicable( server , file , schemaSet ) ) {
				dist.copyDistToFolder( this , scriptFolder , distFolder , file );
				copy = true;
			}
		}
		
		return( copy );
	}

	private boolean checkApplicable( MetaEnvServer server , String file , Map<String,MetaDatabaseSchema> schemaSet ) throws Exception {
		// check schema
		String[] parts = Common.split( file , "-" );
		MetaDatabaseSchema schema = meta.distr.database.getSchema( this , parts[5] );
		if( !schemaSet.containsKey( schema ) ) {
			trace( "script " + file + " is filtered by schema" );
			return( false );
		}
		
		if( !options.OPT_DBALIGNED.isEmpty() ) {
			String alignedid = meta.distr.database.alignedGetIDByBame( this , options.OPT_DBALIGNED );
			if( !alignedid.equals( parts[0].substring( 1 ) ) ) {
				trace( "script " + file + " is filtered by alignedid" );
				return( false );
			}
		}
		
		if( options.OPT_DBTYPE != SQLTYPE.UNKNOWN && options.OPT_DBTYPE != SQLTYPE.SQL ) {
			trace( "script " + file + " is filtered by type" );
			return( false );
		}
		
		if( indexScope == null || indexScope.isEmpty() ) {
			String mask = Common.replace( indexScope , "*" , ".*" );
			if( !file.matches( mask ) ) {
				trace( "script " + file + " is filtered by index mask (" + mask + ")" );
				return( false );
			}
		}

		return( true );
	}
	
	private void executeRunSet( MetaEnvServer server , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseCopy , LocalFolder logReleaseExecute ) throws Exception {
	}
	
}
