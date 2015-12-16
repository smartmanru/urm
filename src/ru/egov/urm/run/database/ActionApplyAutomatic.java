package ru.egov.urm.run.database;

import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.conf.ConfBuilder;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.CommandOptions.SQLTYPE;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.LogStorage;

public class ActionApplyAutomatic extends ActionBase {

	DistStorage dist;
	MetaReleaseDelivery optDelivery;
	String indexScope;
	LogStorage logs;

	public ActionApplyAutomatic( ActionBase action , String stream , DistStorage dist , MetaReleaseDelivery optDelivery , String indexScope ) {
		super( action , stream );
		this.dist = dist;
		this.optDelivery = optDelivery;
		this.indexScope = indexScope;
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		logs = artefactory.getDatabaseLogStorage( this , dist.info.RELEASEVER );
		log( "log to " + logs.logFolder.folderPath );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		DatabaseClient client = new DatabaseClient( server );
		if( !client.checkConnect( this ) )
			exit( "unable to connect to server=" + server.NAME );

		log( "apply changes to database=" + server.NAME + " ..." );
		DatabaseRegistry registry = DatabaseRegistry.getRegistry( this , client , dist.info );
		
		if( applyDatabase( server , client , registry ) )
			log( "apply done." );
		else
			log( "nothing to apply." );
		
		return( true );
	}

	private boolean applyDatabase( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry ) throws Exception {
		registry.startApplyRelease( this );
		
		Map<String,MetaDatabaseSchema> schemaSet = server.getSchemaSet( this );
		boolean done = false;
		for( MetaReleaseDelivery releaseDelivery : dist.info.getDeliveries( this ).values() ) {
			if( optDelivery == null || optDelivery == releaseDelivery )
				if( applyDelivery( server , client , registry , releaseDelivery , schemaSet , logs ) )
					done = true;
		}
		
		return( done );
	}
	
	private boolean applyDelivery( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , MetaReleaseDelivery releaseDelivery , Map<String,MetaDatabaseSchema> schemaSet , LogStorage logs ) throws Exception {
		LocalFolder logReleaseCopy = logs.getDatabaseLogReleaseCopyFolder( this , server , releaseDelivery );
		LocalFolder logReleaseExecute = logs.getDatabaseLogExecuteFolder( this , server , releaseDelivery );
		
		if( !createRunSet( server , releaseDelivery , logReleaseCopy , logReleaseExecute , schemaSet ) )
			return( false );
		
		executeRunSet( server , client , registry , releaseDelivery , logReleaseExecute );
		return( true );
	}

	private boolean createRunSet( MetaEnvServer server , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseCopy , LocalFolder logReleaseExecute , Map<String,MetaDatabaseSchema> schemaSet ) throws Exception {
		String distFolder = dist.getDeliveryDatabaseScriptFolder( this , releaseDelivery.distDelivery );
		FileSet files = dist.getFiles( this );
		FileSet deliveryFiles = files.getDirByPath( this , distFolder );
		if( deliveryFiles == null ) {
			trace( "script directory " + distFolder + " is not found. Skipped." );
			return( false );
		}

		// copy scripts
		boolean copy = false;
		LocalFolder scriptFolder = logReleaseCopy.getSubFolder( this , "scripts" );
		scriptFolder.ensureExists( this );
		logReleaseExecute.ensureExists( this );
		
		for( String file : deliveryFiles.files.keySet() ) {
			if( checkApplicable( server , file , schemaSet ) ) {
				prepareFile( server , scriptFolder , logReleaseExecute , distFolder , file );
				copy = true;
			}
		}

		// copy dataload
		
		return( copy );
	}

	private void prepareFile( MetaEnvServer server , LocalFolder scriptFolder , LocalFolder logReleaseExecute , String distFolder , String file ) throws Exception {
		String[] parts = Common.splitDashed( file );
		dist.copyDistToFolder( this , scriptFolder , distFolder , file );
		scriptFolder.copyFiles( this , file , logReleaseExecute );
		
		ConfBuilder builder = new ConfBuilder( this );
		builder.parseConfigParameters( logReleaseExecute , file , server );
		
		if( !parts[3].equals( "RR" ) )
			return;

		// regional
		String regions = session.customGetValue( this , logReleaseExecute.folderPath , "grep \"^-- REGIONS \" " + file );
		if( regions.isEmpty() )
			exit( "region set not found in regional script=" + file );

		// replicate regional file
		String schema = parts[5]; 
			
		regions = " " + regions + " ";
		for( String region : Common.split( server.REGIONS , " " ) ) {
			if( regions.indexOf( region ) >= 0 ) {
				parts[3] = region;
				parts[5] = Common.replace( schema , "RR" , region );
				String newName = Common.getList( parts , "-" );
				
				session.customCheckStatus( this , logReleaseExecute.folderPath , "sed " + Common.getQuoted( "s/@region@/" + region + "/g" ) + 
						" " + file + " > " + newName ); 
			}
		}
		
		logReleaseExecute.removeFiles( this , file );
	}

	private boolean checkApplicable( MetaEnvServer server , String file , Map<String,MetaDatabaseSchema> schemaSet ) throws Exception {
		// check schema
		String[] parts = Common.splitDashed( file );
		MetaDatabaseSchema schema = meta.distr.database.getSchema( this , parts[5] );
		if( !schemaSet.containsKey( schema.SCHEMA ) ) {
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
		
		if( indexScope != null && !indexScope.isEmpty() ) {
			String mask = Common.replace( indexScope , "*" , ".*" );
			if( !file.matches( mask ) ) {
				trace( "script " + file + " is filtered by index mask (" + mask + ")" );
				return( false );
			}
		}

		return( true );
	}

	private void executeRunSet( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseExecute ) throws Exception {
		registry.readDeliveryState( this , releaseDelivery.distDelivery );

		FileSet files = logReleaseExecute.getFileSet( this );
		for( String file : Common.getSortedKeys( files.files ) )
			executeRunSetScript( server , client , registry , releaseDelivery , logReleaseExecute , file );
	}

	private void executeRunSetScript( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseExecute , String file ) throws Exception {
		if( !registry.checkNeedApply( this , releaseDelivery.distDelivery , file ) )
			return;
		
		trace( "start apply script " + file );
		registry.startApplyScript( this , releaseDelivery.distDelivery , file );
		
		String log = file + ".out";
		String schemaName = DatabaseRegistry.getSchema( this , file );
		MetaDatabaseSchema schema = meta.distr.database.getSchema( this , schemaName );
		if( !client.applyScript( this , schema , logReleaseExecute , file , log ) ) {
			exit( "error applying script " + file + ", see logs." );
			if( !options.OPT_FORCE )
				exit( "cancel apply script set due to errors." );
			
			return;
		}

		registry.finishApplyScript( this , releaseDelivery.distDelivery , file );
		log( "scipt " + file + " has been successfully applid to " + server.NAME );
	}
	
}
