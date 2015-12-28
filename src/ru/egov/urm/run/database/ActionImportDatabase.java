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
	RemoteFolder importScriptsFolder;
	RemoteFolder importLogFolder;
	RemoteFolder importDataFolder;
	
	public ActionImportDatabase( ActionBase action , String stream , MetaEnvServer server , String CMD , String SCHEMA ) {
		super( action , stream );
		this.server = server;
		this.SPECFILE = "import-" + server.NAME + ".conf";
		this.CMD = CMD;
		this.SCHEMA = SCHEMA;
	}

	@Override protected boolean executeSimple() throws Exception {
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
		if( CMD.equals( "data" ) && !SCHEMA.isEmpty() )
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
		LocalFolder work = artefactory.getWorkFolder( this );
		String confFile = work.getFilePath( this , "run.conf" );
		
		List<String> conf = new LinkedList<String>();
		String EXECUTEMAPPING = "";
		for( MetaDatabaseSchema schema : serverSchemas.values() )
			EXECUTEMAPPING = Common.addItemToUniqueSpacedList( EXECUTEMAPPING , schema.SCHEMA + "=" + schema.DBNAME );
		
		conf.add( "CONF_MAPPING=" + Common.getQuoted( EXECUTEMAPPING ) );
		Common.createFileFromStringList( confFile , conf );
		importScriptsFolder.copyFileFromLocal( this , confFile );
		
		MetadataStorage ms = artefactory.getMetadataStorage( this );
		String tablesFilePath = work.getFilePath( this , MetadataStorage.tablesFileName );
		ms.saveDatapumpSet( this , tableSet , server , tablesFilePath );
		importScriptsFolder.copyFileFromLocal( this , tablesFilePath );
	}
	
	private void runAll() throws Exception {
		MetadataStorage ms = artefactory.getMetadataStorage( this );
		ms.loadDatapumpSet( this , tableSet , server );
		
		if( CMD.equals( "all" ) || CMD.equals( "meta" ) )
			runTarget( "meta" , "all" );
		
		if( CMD.equals( "all" ) || CMD.equals( "data" ) ) {
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
		ShellExecutor shell = importScriptsFolder.getSession( this );
		shell.customCheckStatus( this , importScriptsFolder.folderPath , "./run.sh export start " + cmd + " " + Common.getQuoted( SN ) );

		// check execution is started
		Common.sleep( this , 1000 );
		String value = checkStatus( importScriptsFolder );
		if( value.equals( "RUNNING" ) == false && value.equals( "FINISHED" ) == false ) {
			log( "import has not started (status=" + value + "), save logs ..." );
			copyLogs( false , cmd , SN );
			exit( "unable to start import process, see logs" );
		}
		
		// wait for completion - unlimited
		if( value.equals( "RUNNING" ) )
			log( "wait export to complete ..." );
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
		
		log( "import successfully finished, copy data and logs ..." );
		copyLogs( true , cmd , SN );
	}

	private void copyLogs( boolean succeeded , String cmd , String SN ) throws Exception {
		LocalFolder work = artefactory.getWorkFolder( this );
		
		// copy logs
		if( cmd.equals( "meta" ) ) {
			String logMetaFiles = "meta-*.log";
			copyFiles( logMetaFiles , importLogFolder , work );
		}
		else if( cmd.equals( "data" ) ) {
			String logDataFiles = "data-" + SN + "-*.log";
			copyFiles( logDataFiles , importLogFolder , work );
		}
	}

	private void copyFiles( String files , RemoteFolder exportFolder , LocalFolder workFolder ) throws Exception {
		log( "copy files: " + files + " ..." );
		
		exportFolder.copyFilesToLocal( this , workFolder , files );
		String[] copied = workFolder.findFiles( this , files );
		
		if( copied.length == 0 )
			exit( "unable to find files: " + files );
		
		// cleanup source
		exportFolder.removeFiles( this , files );
	}
	
}
