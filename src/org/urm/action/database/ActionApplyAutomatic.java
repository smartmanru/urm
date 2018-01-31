package org.urm.action.database;

import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeTarget;
import org.urm.action.conf.ConfBuilder;
import org.urm.common.Common;
import org.urm.common.action.CommandOptions.SQLTYPE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.LogStorage;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;

public class ActionApplyAutomatic extends ActionBase {

	Dist dist;
	ReleaseDelivery optDelivery;
	String indexScope;
	LogStorage logs;
	
	boolean applyFailed;

	public ActionApplyAutomatic( ActionBase action , String stream , Dist dist , ReleaseDelivery optDelivery , String indexScope ) {
		super( action , stream , "Apply database changes, release=" + dist.RELEASEDIR );
		this.dist = dist;
		this.optDelivery = optDelivery;
		this.indexScope = indexScope;
	}

	@Override protected void runBefore( ScopeState state , ActionScope scope ) throws Exception {
		logs = artefactory.getDatabaseLogStorage( this , scope.meta , dist.release.RELEASEVER );
		info( "log to " + logs.logFolder.folderPath );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit1( _Error.ConnectFailed1 , "unable to connect to server=" + server.NAME , server.NAME );

		info( "apply changes to database=" + server.NAME + " ..." );
		
		applyFailed = false;
		if( applyDatabase( server , client ) )
			info( "apply done." );
		
		if( applyFailed )
			super.fail0( _Error.ApplyFailed0 , "Database apply changes failed" );
		
		return( SCOPESTATE.RunSuccess );
	}

	private boolean applyDatabase( MetaEnvServer server , DatabaseClient client ) throws Exception {
		boolean done = false;
		String[] versions = dist.release.getApplyVersions( this );
		for( String version : versions )
			if( applyDatabaseVersion( server , client , logs , version ) )
				done = true;
		
		return( done );
	}

	private boolean applyDatabaseVersion( MetaEnvServer server , DatabaseClient client , LogStorage logs , String version ) throws Exception {
		info( version + " " + getMode() + ": apply database changes ..." );
		
		DatabaseRegistry registry = DatabaseRegistry.getRegistry( this , client );
		if( !registry.startApplyRelease( this , dist.release ) )
			return( false );

		boolean done = false;
		Map<String,MetaDatabaseSchema> schemaSet = server.getSchemaSet( this );
		for( ReleaseDelivery releaseDelivery : dist.release.getDeliveries() ) {
			if( optDelivery == null || optDelivery == releaseDelivery )
				if( applyDelivery( server , client , registry , version , releaseDelivery , schemaSet , logs ) )
					done = true;
		}
		
		// check release is finished
		int n = 0;
		if( isExecute() && done ) {
			registry.readIncompleteScripts( this );
			n = registry.getScriptCount( this );
		}
		
		if( n > 0 )
			info( version + " " + getMode() + ": release is not finalized, total " + n + " incomplete script(s)" );
		else {
			registry.finishApplyRelease( this );
			info( version + " " + getMode() + ": release is finalized." );
		}
		
		return( done );
	}
	
	private boolean applyDelivery( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , String version , ReleaseDelivery releaseDelivery , Map<String,MetaDatabaseSchema> schemaSet , LogStorage logs ) throws Exception {
		LocalFolder logReleaseCopy = logs.getDatabaseLogReleaseCopyFolder( this , server , releaseDelivery , version );
		LocalFolder logReleaseExecute = logs.getDatabaseLogExecuteFolder( this , server , releaseDelivery , version );
		
		if( !createRunSet( server , releaseDelivery , logReleaseCopy , logReleaseExecute , schemaSet , version ) )
			return( false );
		
		if( !executeRunSet( server , client , registry , releaseDelivery , logReleaseExecute , version ) )
			applyFailed = true;
		
		return( true );
	}

	private boolean createRunSet( MetaEnvServer server , ReleaseDelivery releaseDelivery , LocalFolder logReleaseCopy , LocalFolder logReleaseExecute , Map<String,MetaDatabaseSchema> schemaSet , String version ) throws Exception {
		String distFolder = dist.getDeliveryDatabaseScriptFolder( this , releaseDelivery.distDelivery , version );
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
		
		for( String file : deliveryFiles.getAllFiles() ) {
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
		
		ConfBuilder builder = new ConfBuilder( this , server.meta );
		MetaProductSettings settings = server.meta.getProductSettings();
		MetaProductCoreSettings core = settings.getCoreSettings();
		builder.configureFile( logReleaseExecute , file , server , null , core.charset );
		
		if( !dsf.REGIONALINDEX.equals( "RR" ) )
			return;

		// regional
		String[] lines = shell.grepFile( this , logReleaseExecute.getFilePath( this , file ) , "^-- REGIONS " );
		String regions = ( lines.length == 1 )? lines[0] : "";
		if( regions.isEmpty() )
			exit1( _Error.NoRegionSet1 , "region set not found in regional script=" + file , file );

		// replicate regional file
		String schema = dsf.SRCSCHEMA; 
			
		regions = " " + regions + " ";
		for( String region : Common.split( server.REGIONS , " " ) ) {
			if( regions.indexOf( region ) >= 0 ) {
				dsf.REGIONALINDEX = region;
				dsf.SRCSCHEMA = Common.replace( schema , "RR" , region );
				String newName = dsf.getDistFile();
				
				shell.customCheckStatus( this , logReleaseExecute.folderPath , "sed " + Common.getQuoted( "s/@region@/" + region + "/g" ) + 
						" " + file + " > " + newName ); 
			}
		}
		
		logReleaseExecute.removeFiles( this , file );
	}

	private boolean checkApplicable( MetaEnvServer server , String file , Map<String,MetaDatabaseSchema> schemaSet ) throws Exception {
		// check schema
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( this , file );
		
		MetaDatabase database = server.meta.getDatabase();
		MetaDatabaseSchema schema = database.getSchema( dsf.SRCSCHEMA );
		if( !schemaSet.containsKey( schema.NAME ) ) {
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

	private boolean executeRunSet( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , ReleaseDelivery releaseDelivery , LocalFolder logReleaseExecute , String version ) throws Exception {
		registry.readDeliveryState( this , releaseDelivery.distDelivery );

		FileSet files = logReleaseExecute.getFileSet( this );
		boolean ok = true;
		for( String file : files.getAllFiles() ) {
			if( !executeRunSetScript( server , client , registry , releaseDelivery , logReleaseExecute , file ) )
				ok = false;
		}
		
		return( ok );
	}

	private boolean executeRunSetScript( MetaEnvServer server , DatabaseClient client , DatabaseRegistry registry , ReleaseDelivery releaseDelivery , LocalFolder logReleaseExecute , String file ) throws Exception {
		if( !registry.checkNeedApply( this , releaseDelivery.distDelivery , file ) )
			return( true );
		
		info( "apply script " + file + " (" + getMode() + ") ..." );
		if( context.CTX_SHOWONLY )
			return( true );
		
		registry.startApplyScript( this , releaseDelivery.distDelivery , file );
		
		String log = file + ".out";
		DatabaseScriptFile dsf = new DatabaseScriptFile();
		dsf.setDistFile( this , file );
		String schemaName = dsf.SRCSCHEMA;
		MetaDatabase database = server.meta.getDatabase();
		MetaDatabaseSchema schema = database.getSchema( schemaName );
		if( !client.applyScript( this , schema , logReleaseExecute , file , logReleaseExecute , log ) ) {
			ifexit( _Error.ErrorApplyingScript1 , "error applying script " + file + ", see logs" , new String[] { file } );
			return( false );
		}

		registry.finishApplyScript( this , releaseDelivery.distDelivery , file );
		info( "script " + file + " has been successfully applied to " + server.NAME );
		return( true );
	}
	
}
