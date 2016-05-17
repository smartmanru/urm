package ru.egov.urm.action.database;

import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScope;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.CommandOptions.SQLTYPE;
import ru.egov.urm.action.conf.ConfBuilder;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.LogStorage;

public class ActionApplyAutomatic extends ActionBase {

	DistStorage dist;
	MetaReleaseDelivery optDelivery;
	String indexScope;
	LogStorage logs;
	
	boolean applyFailed;

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
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit( "unable to connect to server=" + server.NAME );

		log( "apply changes to database=" + server.NAME + " ..." );
		DatabaseRegistry registry = DatabaseRegistry.getRegistry( this , client , dist.info );
		
		applyFailed = false;
		if( applyDatabase( server , client , registry ) )
			log( "apply done." );
		else
			log( "nothing to apply." );
		
		if( applyFailed )
			super.setFailed();
		
		return( true );
	}

	private boolean applyDatabase( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry ) throws Exception {
		if( !registry.startApplyRelease( this ) )
			return( false );
		
		Map<String,MetaDatabaseSchema> schemaSet = server.getSchemaSet( this );
		boolean done = false;
		for( MetaReleaseDelivery releaseDelivery : dist.info.getDeliveries( this ).values() ) {
			if( optDelivery == null || optDelivery == releaseDelivery )
				if( applyDelivery( server , client , registry , releaseDelivery , schemaSet , logs ) )
					done = true;
		}
		
		// check release is finished
		if( !applyFailed ) {
			registry.readIncompleteScripts( this );
			int n = registry.getScriptCount( this );
			if( n > 0 )
				log( "release is not finalized, total " + n + " incomplete scripts" );
			else {
				registry.finishApplyRelease( this );
				log( "release is finalized." );
			}
		}
		
		return( done );
	}
	
	private boolean applyDelivery( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , MetaReleaseDelivery releaseDelivery , Map<String,MetaDatabaseSchema> schemaSet , LogStorage logs ) throws Exception {
		LocalFolder logReleaseCopy = logs.getDatabaseLogReleaseCopyFolder( this , server , releaseDelivery );
		LocalFolder logReleaseExecute = logs.getDatabaseLogExecuteFolder( this , server , releaseDelivery );
		
		if( !createRunSet( server , releaseDelivery , logReleaseCopy , logReleaseExecute , schemaSet ) )
			return( false );
		
		if( !executeRunSet( server , client , registry , releaseDelivery , logReleaseExecute ) )
			applyFailed = true;
		
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
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( this , file );
		dist.copyDistToFolder( this , scriptFolder , distFolder , file );
		scriptFolder.copyFiles( this , file , logReleaseExecute );
		
		ConfBuilder builder = new ConfBuilder( this );
		builder.configureFile( logReleaseExecute , file , server , null , meta.product.charset );
		
		if( !dsf.REGIONALINDEX.equals( "RR" ) )
			return;

		// regional
		String[] lines = session.grepFile( this , logReleaseExecute.getFilePath( this , file ) , "^-- REGIONS " );
		String regions = ( lines.length == 1 )? lines[0] : "";
		if( regions.isEmpty() )
			exit( "region set not found in regional script=" + file );

		// replicate regional file
		String schema = dsf.SRCSCHEMA; 
			
		regions = " " + regions + " ";
		for( String region : Common.split( server.REGIONS , " " ) ) {
			if( regions.indexOf( region ) >= 0 ) {
				dsf.REGIONALINDEX = region;
				dsf.SRCSCHEMA = Common.replace( schema , "RR" , region );
				String newName = dsf.getDistFile();
				
				session.customCheckStatus( this , logReleaseExecute.folderPath , "sed " + Common.getQuoted( "s/@region@/" + region + "/g" ) + 
						" " + file + " > " + newName ); 
			}
		}
		
		logReleaseExecute.removeFiles( this , file );
	}

	private boolean checkApplicable( MetaEnvServer server , String file , Map<String,MetaDatabaseSchema> schemaSet ) throws Exception {
		// check schema
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( this , file );
		
		MetaDatabaseSchema schema = meta.distr.database.getSchema( this , dsf.SRCSCHEMA );
		if( !schemaSet.containsKey( schema.SCHEMA ) ) {
			trace( "script " + file + " is filtered by schema" );
			return( false );
		}
		
		if( !context.CTX_DBALIGNED.isEmpty() ) {
			String alignedid = context.CTX_DBALIGNED;
			if( !alignedid.equals( dsf.PREFIXALIGNED ) ) {
				trace( "script " + file + " is filtered by alignedid" );
				return( false );
			}
		}
		
		if( context.CTX_DBTYPE != SQLTYPE.UNKNOWN && context.CTX_DBTYPE != SQLTYPE.SQL ) {
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

	private boolean executeRunSet( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseExecute ) throws Exception {
		registry.readDeliveryState( this , releaseDelivery.distDelivery );

		FileSet files = logReleaseExecute.getFileSet( this );
		boolean ok = true;
		for( String file : Common.getSortedKeys( files.files ) ) {
			if( !executeRunSetScript( server , client , registry , releaseDelivery , logReleaseExecute , file ) )
				ok = false;
		}
		
		return( ok );
	}

	private boolean executeRunSetScript( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , MetaReleaseDelivery releaseDelivery , LocalFolder logReleaseExecute , String file ) throws Exception {
		if( !registry.checkNeedApply( this , releaseDelivery.distDelivery , file ) )
			return( true );
		
		trace( "start apply script " + file + " (" + getMode() + " ..." );
		if( context.CTX_SHOWONLY )
			return( true );
		
		registry.startApplyScript( this , releaseDelivery.distDelivery , file );
		
		String log = file + ".out";
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( this , file );
		String schemaName = dsf.SRCSCHEMA;
		MetaDatabaseSchema schema = meta.distr.database.getSchema( this , schemaName );
		if( !client.applyScript( this , schema , logReleaseExecute , file , logReleaseExecute , log ) ) {
			exit( "error applying script " + file + ", see logs." );
			if( !context.CTX_FORCE )
				exit( "cancel apply script set due to errors." );
			
			return( false );
		}

		registry.finishApplyScript( this , releaseDelivery.distDelivery , file );
		log( "script " + file + " has been successfully applied to " + server.NAME );
		return( true );
	}
	
}
