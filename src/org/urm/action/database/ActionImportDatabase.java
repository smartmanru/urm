package org.urm.action.database;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.urm.action.ActionBase;
import org.urm.action.conf.ConfBuilder;
import org.urm.common.Common;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaProductSettings;

public class ActionImportDatabase extends ActionBase {

	MetaEnvServer server;
	String SPECFILE;
	String CMD;
	String SCHEMA;

	RemoteFolder distDataFolder;
	RemoteFolder distLogFolder;
	DatabaseClient client;

	String DATASET;
	String TABLESETFILE;
	String DUMPDIR;
	String REMOTE_SETDBENV;
	String DATABASE_DATAPUMPDIR;
	String POSTREFRESH;
	boolean NFS;

	Map<String,MetaDatabaseSchema> serverSchemas;
	Map<String,Map<String,String>> tableSet;
	LocalFolder workFolder;
	RemoteFolder importScriptsFolder;
	RemoteFolder importLogFolder;
	RemoteFolder importDataFolder;
	
	public ActionImportDatabase( ActionBase action , String stream , MetaEnvServer server , String CMD , String SCHEMA ) {
		super( action , stream , "Import database, server=" + server.NAME );
		this.server = server;
		this.SPECFILE = "import-" + context.env.ID + "-" + server.sg.NAME + "-" + server.NAME + ".conf";
		this.CMD = CMD;
		this.SCHEMA = SCHEMA;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		workFolder = artefactory.getWorkFolder( this );
		loadImportSettings();
		
		client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit0( _Error.UnableConnectAdmin0 , "unable to connect to administrative db" );
		
		checkSource();
		makeTargetScripts();
		makeTargetConfig();
		runAll();
		
		info( "import has been finished, dumps are loaded from " + distDataFolder.folderPath );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	private void loadImportSettings() throws Exception {
		MetadataStorage ms = artefactory.getMetadataStorage( this , server.meta ); 
		String specPath = ms.getDatapumpFile( this , SPECFILE );
		
		info( "reading import specification file " + specPath + " ..." );
		Properties props = readPropertyFile( specPath );
		
		DATASET = props.getProperty( "CONFIG_DATASET" , "" );
		TABLESETFILE = props.getProperty( "CONFIG_TABLESETFILE" , "" );
		DUMPDIR = props.getProperty( "CONFIG_LOADDIR" , "" );
		REMOTE_SETDBENV = props.getProperty( "CONFIG_REMOTE_SETDBENV" , "" );
		DATABASE_DATAPUMPDIR = props.getProperty( "CONFIG_DATABASE_DATAPUMPDIR" , "" );
		POSTREFRESH = props.getProperty( "CONFIG_POSTREFRESH" , "" );
		NFS = Common.getBooleanValue( props.getProperty( "CONFIG_NFS" ) ); 

		serverSchemas = server.getSchemaSet( this );
		if( !SCHEMA.isEmpty() )
			if( !serverSchemas.containsKey( SCHEMA ) )
				exit1( _Error.UnknownServerSchema1 , "schema " + SCHEMA + " is not part of server datasets" , SCHEMA );

		// load tableset
		tableSet = ms.readDatapumpFile( this , TABLESETFILE , SCHEMA );
	}

	private void checkSource() throws Exception {
		DistRepository repository = artefactory.getDistRepository( this , server.meta );
		distDataFolder = repository.getDataFolder( this , DATASET );
		if( !distDataFolder.checkExists( this ) )
			exit1( _Error.MissingDataFolder1 , "data folder does not exist: " + distDataFolder.folderPath , distDataFolder.folderPath );
		
		// check required dump files are available
		FileSet files = distDataFolder.getFileSet( this );
		if( !CMD.equals( "data" ) )
			if( files.getFilesMatched( this , "meta-.*\\.dump" ).length == 0 )
				exit1( _Error.NoMetadataDumpFiles1 , "no metadata dump files to load, check dump directory: " + distDataFolder.folderPath , distDataFolder.folderPath );
			
		if( !CMD.equals( "meta" ) ) {
			for( String schema : serverSchemas.keySet() ) {
				// skip data for missing schema
				if( !tableSet.containsKey( schema ) ) {
					trace( "skip check data schema=" + schema + " due to empty tableset" );
					continue;
				}

				// check data files
				if( SCHEMA.isEmpty() || SCHEMA.equals( schema ) ) {
					if( files.getFilesMatched( this , "data-" + schema + ".*\\.dump" ).length == 0 )
						exit2( _Error.NoSchemaDataDumpFiles2 , "no data dump files for schema=" + schema + " to load, check dump directory: " + distDataFolder.folderPath , schema , distDataFolder.folderPath );
				}
			}
		}

		distLogFolder = repository.getImportLogFolder( this , DATASET , server );
		distLogFolder.ensureExists( this );
	}

	private void makeTargetScripts() throws Exception {
		// copy scripts
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder urmScripts = urm.getDatabaseDatapumpScripts( this , server );
		RedistStorage storage = artefactory.getRedistStorage( this , client.getDatabaseAccount( this ) );
		RemoteFolder redist = storage.getRedistTmpFolder( this , "database" );
		
		RemoteFolder importFolder = redist.getSubFolder( this , "import" );
		importScriptsFolder = importFolder.getSubFolder( this , "scripts" );
		
		// ensure not running currently 
		if( importScriptsFolder.checkFileExists( this , "run.sh" ) ) {
			String value = checkStatus( importScriptsFolder );
			if( value.equals( "RUNNING" ) )
				exit0( _Error.ImportAlreadyRunning0 , "unable to start because import is already running" );
		}
		
		info( "copy execution part to " + redist.folderPath + " ..." );
		importFolder.recreateThis( this );
		importScriptsFolder.ensureExists( this );
		importScriptsFolder.copyDirContentFromLocal( this , urmScripts , "" );
		
		importLogFolder = importFolder.getSubFolder( this , "log" );
		importLogFolder.ensureExists( this );
		importDataFolder = importFolder.getSubFolder( this , "data" );
		importDataFolder.ensureExists( this );
	}
	
	public String checkStatus( RemoteFolder folder ) throws Exception {
		ShellExecutor shell = folder.getSession( this );
		String value = shell.customGetValue( this , folder.folderPath , "./run.sh import status" );
		return( value );
	}
	
	private void makeTargetConfig() throws Exception {
		// create configuration to run scripts
		String confFile = workFolder.getFilePath( this , "run.conf" );
		
		List<String> conf = new LinkedList<String>();
		String EXECUTEMAPPING = "";
		for( MetaDatabaseSchema schema : serverSchemas.values() )
			EXECUTEMAPPING = Common.addItemToUniqueSpacedList( EXECUTEMAPPING , schema.SCHEMA + "=" + schema.DBNAME );
		
		conf.add( "CONF_MAPPING=" + Common.getQuoted( EXECUTEMAPPING ) );
		if( NFS ) {
			conf.add( "CONF_NFS=" + Common.getBooleanValue( NFS ) );
			conf.add( "CONF_NFSDATA=" + distDataFolder.folderPath );
			conf.add( "CONF_NFSLOG=" + distLogFolder.folderPath );
		}
		
		Common.createFileFromStringList( execrc , confFile , conf );
		importScriptsFolder.copyFileFromLocal( this , confFile );
		
		MetadataStorage ms = artefactory.getMetadataStorage( this , server.meta );
		String tablesFilePath = workFolder.getFilePath( this , UrmStorage.TABLES_FILE_NAME );
		ms.saveDatapumpSet( this , tableSet , server , tablesFilePath );
		importScriptsFolder.copyFileFromLocal( this , tablesFilePath );
	}
	
	private void runAll() throws Exception {
		if( CMD.equals( "all" ) || CMD.equals( "meta" ) ) {
			if( SCHEMA.isEmpty() )
				runTarget( "meta" , "all" );
			else
				runTarget( "meta" , SCHEMA );
		}
		
		if( CMD.equals( "all" ) || CMD.equals( "data" ) ) {
			MetadataStorage ms = artefactory.getMetadataStorage( this , server.meta );
			ms.loadDatapumpSet( this , tableSet , server , false , false );
			
			if( CMD.equals( "data" ) && !SCHEMA.isEmpty() )
				runTarget( "data" , SCHEMA );
			else {
				for( String s : server.getSchemaSet( this ).keySet() )
					runTarget( "data" , s );
			}
		}
		
		applyPostRefresh();
	}
	
	private void runTarget( String cmd , String SN ) throws Exception {
		// skip data for missing schema
		if( cmd.equals( "data" ) ) {
			if( !tableSet.containsKey( SN ) ) {
				info( "skip import data schema=" + SN + " due to empty tableset" );
				return;
			}
		}
		
		// initiate execution
		info( "start import cmd=" + cmd + " schemaset=" + SN + " ..." );
		
		// copy dump files
		String dataFiles = null;
		if( cmd.equals( "meta" ) ) {
			dataFiles = "meta-*.dump";
			uploadFiles( dataFiles , distDataFolder , importDataFolder  );
		}
		if( cmd.equals( "data" ) ) {
			dataFiles = "data-" + SN + "-*.dump";
			uploadFiles( dataFiles , distDataFolder , importDataFolder );
		}
		
		ShellExecutor shell = importScriptsFolder.getSession( this );
		shell.customCheckStatus( this , importScriptsFolder.folderPath , "./run.sh import start " + cmd + " " + Common.getQuoted( SN ) );
		
		// check execution is started
		Common.sleep( 1000 );
		String value = checkStatus( importScriptsFolder );
		if( value.equals( "RUNNING" ) == false && value.equals( "FINISHED" ) == false ) {
			info( "import has not been started (status=" + value + "), save logs ..." );
			
			importScriptsFolder.copyFileToLocalRename( this , workFolder , "run.sh.log" , cmd + "-" + SN + "run.sh.log" );
			copyLogs( false , cmd , SN );
			exit1( _Error.UnableStartImport1 , "unable to start import process, see logs at " + workFolder.folderPath , workFolder.folderPath );
		}
		
		// wait for completion - unlimited
		if( value.equals( "RUNNING" ) )
			info( "wait import to complete ..." );
		while( value.equals( "RUNNING" ) ) {
			Common.sleep( context.CTX_TIMEOUT );
			value = checkStatus( importScriptsFolder );
		}
		
		// copy top log
		importScriptsFolder.copyFileToLocalRename( this , workFolder , "run.sh.log" , cmd + "-" + SN + "run.sh.log" );
		
		// check final status
		if( !value.equals( "FINISHED" ) ) {
			info( "import finished with errors, save logs ..." );
			copyLogs( false , cmd , SN );
			exit0( _Error.ImportProcessErrors0 , "import process completed with errors, see logs" );
		}
		
		info( "import successfully finished, copy logs ..." );
		copyLogs( true , cmd , SN );
		
		// cleanup target
		importDataFolder.removeFiles( this , dataFiles );
	}

	private void copyLogs( boolean succeeded , String cmd , String SN ) throws Exception {
		if( NFS ) {
			debug( "skip download log files, use NFS" );
			return;
		}
		
		// copy logs
		if( cmd.equals( "meta" ) ) {
			String logMetaFiles = "meta-*.log";
			copyLogFiles( logMetaFiles );
		}
		else if( cmd.equals( "data" ) ) {
			String logDataFiles = "data-" + SN + "-*.log";
			copyLogFiles( logDataFiles );
		}
	}

	private void copyLogFiles( String files ) throws Exception {
		info( "copy files: " + files + " ..." );
		
		importLogFolder.copyFilesToLocal( this , workFolder , files );
		String[] copied = workFolder.findFiles( this , files );
		
		if( copied.length == 0 )
			exit1( _Error.UnableFindFiles1 , "unable to find files: " + files , files );
		
		distLogFolder.copyFilesFromLocal( this , workFolder , files );
	}
	
	private void uploadFiles( String files , RemoteFolder distFolder , RemoteFolder importFolder ) throws Exception {
		if( NFS ) {
			debug( "skip upload data files, use NFS" );
			return;
		}
		
		info( "upload files: " + files + " ..." );

		String[] copied = distFolder.findFiles( this , files );
		if( copied.length == 0 )
			exit1( _Error.UnableFindFiles1 , "unable to find files: " + files , files );
		
		LocalFolder workDataFolder;
		int timeout = setTimeoutUnlimited();
		if( distFolder.isRemote( this ) ) {
			workDataFolder = artefactory.getWorkFolder( this , "data" );
			workDataFolder.recreateThis( this );
			distFolder.copyFilesToLocal( this , workDataFolder , files );
			
			importFolder.moveFilesFromLocal( this , workDataFolder , files );
		}
		else {
			workDataFolder = artefactory.getAnyFolder( this , distFolder.folderPath );
			importFolder.copyFilesFromLocal( this , workDataFolder , files );
		}
		setTimeout( timeout );
}

	private void applyPostRefresh() throws Exception {
		if( POSTREFRESH.isEmpty() ) {
			info( "no post-refresh defined. Skipped." );
			return;
		}
		
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit1( _Error.ConnectFailed1 , "unable to connect to server=" + server.NAME , server.NAME );
		
		LocalFolder post = workFolder.getSubFolder( this , "post-refresh" );
		post.ensureExists( this );
		for( String name : Common.splitSpaced( POSTREFRESH ) )
			applyPostRefreshFolder( client , post , name );
		
		if( isFailed() )
			error( "post-refresh applied with errors" );
		else
			info( "post-refresh successfully applied" );
	}
	
	private void applyPostRefreshFolder( DatabaseClient client , LocalFolder post , String name ) throws Exception {
		info( "apply post-refresh " + name + " ..." );
	
		// export
		LocalFolder folder = post.getSubFolder( this , name );
		
		SourceStorage storage = artefactory.getSourceStorage( this , server.meta );
		storage.exportPostRefresh( this , name , post );
		
		// configure
		ConfBuilder builder = new ConfBuilder( this , server.meta );
		MetaProductSettings settings = server.meta.getProductSettings( this );
		builder.configureFolder( this , folder , server , null , settings.charset );
		
		// apply
		if( !client.applyManualSet( this , folder ) )
			super.fail1( _Error.PostRefreshApplyFailed1 , "Post-refresh apply failed, name=" + name , name );
	}
	
}
