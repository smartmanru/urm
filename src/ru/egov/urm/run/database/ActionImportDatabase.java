package ru.egov.urm.run.database;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.DistRepository;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.MetadataStorage;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;
import ru.egov.urm.storage.UrmStorage;

public class ActionImportDatabase extends ActionBase {

	MetaEnvServer server;
	String SPECFILE;
	String CMD;
	String SCHEMA;

	RemoteFolder distDataFolder;
	DatabaseClient client;

	String DATASET;
	String TABLESETFILE;
	String DUMPDIR;
	String REMOTE_SETDBENV;
	String DATABASE_DATAPUMPDIR;

	Map<String,MetaDatabaseSchema> serverSchemas;
	Map<String,Map<String,String>> tableSet;
	LocalFolder workFolder;
	RemoteFolder importScriptsFolder;
	RemoteFolder importLogFolder;
	RemoteFolder importDataFolder;
	
	public ActionImportDatabase( ActionBase action , String stream , MetaEnvServer server , String CMD , String SCHEMA ) {
		super( action , stream );
		this.server = server;
		this.SPECFILE = "import-" + meta.env.ID + "-" + meta.dc.NAME + "-" + server.NAME + ".conf";
		this.CMD = CMD;
		this.SCHEMA = SCHEMA;
	}

	@Override protected boolean executeSimple() throws Exception {
		workFolder = artefactory.getWorkFolder( this );
		loadImportSettings();
		
		client = new DatabaseClient( server ); 
		if( !client.checkConnect( this ) )
			exit( "unable to connect to administrative db" );
		
		distDataFolder = checkSource();
		makeTargetScripts();
		makeTargetConfig();
		runAll();
		
		log( "import has been finished, dumps are loaded from " + distDataFolder.folderPath );
		
		return( true );
	}
	
	private void loadImportSettings() throws Exception {
		MetadataStorage ms = artefactory.getMetadataStorage( this ); 
		String specPath = ms.getDatapumpFile( this , SPECFILE );
		
		log( "reading import specification file " + specPath + " ..." );
		Properties props = ConfReader.readPropertyFile( this , specPath );
		
		DATASET = props.getProperty( "CONFIG_DATASET" );
		TABLESETFILE = props.getProperty( "CONFIG_TABLESETFILE" );
		DUMPDIR = props.getProperty( "CONFIG_LOADDIR" );
		REMOTE_SETDBENV = props.getProperty( "CONFIG_REMOTE_SETDBENV" );
		DATABASE_DATAPUMPDIR = props.getProperty( "CONFIG_DATABASE_DATAPUMPDIR" );

		serverSchemas = server.getSchemaSet( this );
		if( !SCHEMA.isEmpty() )
			if( !serverSchemas.containsKey( SCHEMA ) )
				exit( "schema " + SCHEMA + " is not part of server datasets" );

		// load tableset
		tableSet = ms.readDatapumpFile( this , TABLESETFILE , SCHEMA );
	}

	private RemoteFolder checkSource() throws Exception {
		DistRepository repository = artefactory.getDistRepository( this );
		RemoteFolder folder = repository.getDataFolder( this , DATASET );
		if( !folder.checkExists( this ) )
			exit( "data folder does not exist: " + folder.folderPath );
		
		// check required dump files are available
		FileSet files = folder.getFileSet( this );
		if( !CMD.equals( "data" ) )
			if( files.getFilesMatched( this , "meta-.*\\.dump" ).length == 0 )
				exit( "no metadata dump files to load, check dump directory: " + folder.folderPath );
			
		if( !CMD.equals( "meta" ) ) {
			for( String schema : serverSchemas.keySet() ) {
				if( SCHEMA.isEmpty() || SCHEMA.equals( schema ) ) {
					if( files.getFilesMatched( this , "data-" + schema + ".*\\.dump" ).length == 0 )
						exit( "no data dump files for schema=" + schema + " to load, check dump directory: " + folder.folderPath );
				}
			}
		}

		return( folder );
	}

	private void makeTargetScripts() throws Exception {
		// copy scripts
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder urmScripts = urm.getDatapumpScripts( this , server.DBMSTYPE );
		RedistStorage storage = artefactory.getRedistStorage( "database" , client.getDatabaseAccount( this ) );
		RemoteFolder redist = storage.getRedistTmpFolder( this );
		
		RemoteFolder importFolder = redist.getSubFolder( this , "import" );
		importScriptsFolder = importFolder.getSubFolder( this , "scripts" );
		
		// ensure not running currently 
		if( importScriptsFolder.checkFileExists( this , "run.sh" ) ) {
			String value = checkStatus( importScriptsFolder );
			if( value.equals( "RUNNING" ) )
				exit( "unable to start because import is already running" );
		}
		
		log( "copy execution part to " + redist.folderPath + " ..." );
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
		Common.createFileFromStringList( confFile , conf );
		importScriptsFolder.copyFileFromLocal( this , confFile );
		
		MetadataStorage ms = artefactory.getMetadataStorage( this );
		String tablesFilePath = workFolder.getFilePath( this , MetadataStorage.tablesFileName );
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
			MetadataStorage ms = artefactory.getMetadataStorage( this );
			ms.loadDatapumpSet( this , tableSet , server , false );
			
			if( CMD.equals( "data" ) && !SCHEMA.isEmpty() )
				runTarget( "data" , SCHEMA );
			else {
				for( String s : server.getSchemaSet( this ).keySet() )
					runTarget( "data" , s );
			}
		}
	}
	
	private void runTarget( String cmd , String SN ) throws Exception {
		// initiate execution
		log( "start import cmd=" + cmd + " schemaset=" + SN + " ..." );
		
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
		Common.sleep( this , 1000 );
		String value = checkStatus( importScriptsFolder );
		if( value.equals( "RUNNING" ) == false && value.equals( "FINISHED" ) == false ) {
			log( "import has not started (status=" + value + "), save logs ..." );
			copyLogs( false , cmd , SN );
			exit( "unable to start import process, see logs at " + workFolder.folderPath );
		}
		
		// wait for completion - unlimited
		if( value.equals( "RUNNING" ) )
			log( "wait import to complete ..." );
		while( value.equals( "RUNNING" ) ) {
			Common.sleep( this , options.OPT_COMMANDTIMEOUT );
			value = checkStatus( importScriptsFolder );
		}
		
		// check final status
		if( !value.equals( "FINISHED" ) ) {
			log( "import finished with errors, save logs ..." );
			copyLogs( false , cmd , SN );
			exit( "import process completed with errors, see logs" );
		}
		
		log( "import successfully finished, copy logs ..." );
		copyLogs( true , cmd , SN );
		
		// cleanup target
		importDataFolder.removeFiles( this , dataFiles );
	}

	private void copyLogs( boolean succeeded , String cmd , String SN ) throws Exception {
		// copy logs
		if( cmd.equals( "meta" ) ) {
			String logMetaFiles = "meta-*.log";
			copyFiles( logMetaFiles , importLogFolder , workFolder );
		}
		else if( cmd.equals( "data" ) ) {
			String logDataFiles = "data-" + SN + "-*.log";
			copyFiles( logDataFiles , importLogFolder , workFolder );
		}
	}

	private void copyFiles( String files , RemoteFolder importFolder , LocalFolder workFolder ) throws Exception {
		log( "copy files: " + files + " ..." );
		
		importFolder.copyFilesToLocal( this , workFolder , files );
		String[] copied = workFolder.findFiles( this , files );
		
		if( copied.length == 0 )
			exit( "unable to find files: " + files );
		
		// cleanup source
		importFolder.removeFiles( this , files );
	}
	
	private void uploadFiles( String files , RemoteFolder distFolder , RemoteFolder importFolder ) throws Exception {
		log( "copy files: " + files + " ..." );

		String[] copied = distFolder.findFiles( this , files );
		if( copied.length == 0 )
			exit( "unable to find files: " + files );
		
		LocalFolder workDataFolder;
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
	}

}
