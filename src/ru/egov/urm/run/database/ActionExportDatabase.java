package ru.egov.urm.run.database;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.DistRepository;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.MetadataStorage;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;
import ru.egov.urm.storage.UrmStorage;

public class ActionExportDatabase extends ActionBase {

	MetaEnvServer server;
	String SPECFILE;
	String CMD;
	String SCHEMA;

	String DATASET;
	String TABLESETFILE;
	String DUMPDIR;
	String REMOTE_SETDBENV;
	String DATABASE_DATAPUMPDIR;
	boolean STANDBY;
	
	Map<String,MetaDatabaseSchema> serverSchemas;
	Map<String,Map<String,String>> tableSet;

	RemoteFolder distDataFolder;
	RemoteFolder distLogFolder;
	RemoteFolder exportScriptsFolder;
	RemoteFolder exportLogFolder;
	RemoteFolder exportDataFolder;
	DatabaseClient client;

	public ActionExportDatabase( ActionBase action , String stream , MetaEnvServer server , String CMD , String SCHEMA ) {
		super( action , stream );
		this.server = server;
		this.SPECFILE = "export-" + meta.env.ID + "-" + meta.dc.NAME + "-" + server.NAME + ".conf";
		this.CMD = CMD;
		this.SCHEMA = SCHEMA;
	}

	@Override protected boolean executeSimple() throws Exception {
		loadExportSettings();
		
		client = new DatabaseClient();
		MetaEnvServerNode node;
		if( STANDBY )
			node = server.getStandbyNode( this );
		else
			node = server.getActiveNode( this );
		if( !client.checkConnect( this , server , node ) )
			exit( "unable to connect to administrative db" );
		
		distDataFolder = prepareDestination();
		distLogFolder = distDataFolder.getSubFolder( this , "log" );
		distLogFolder.ensureExists( this );
		
		makeTargetScripts();
		makeTargetConfig();
		runAll();
		
		log( "export has been finished, dumps are copied to " + distDataFolder.folderPath );
		
		return( true );
	}

	private void loadExportSettings() throws Exception {
		MetadataStorage ms = artefactory.getMetadataStorage( this ); 
		String specPath = ms.getDatapumpFile( this , SPECFILE );
		
		log( "reading export specification file " + specPath + " ..." );
		Properties props = ConfReader.readPropertyFile( this , specPath );
		
		DATASET = props.getProperty( "CONFIG_DATASET" );
		TABLESETFILE = props.getProperty( "CONFIG_TABLESETFILE" );
		DUMPDIR = props.getProperty( "CONFIG_DATADIR" , "" );
		REMOTE_SETDBENV = props.getProperty( "CONFIG_REMOTE_SETDBENV" , "" );
		DATABASE_DATAPUMPDIR = props.getProperty( "CONFIG_DATABASE_DATAPUMPDIR" , "" );
		STANDBY = Common.getBooleanValue( props.getProperty( "CONFIG_STANDBY" ) ); 

		serverSchemas = server.getSchemaSet( this );
		if( CMD.equals( "data" ) && !SCHEMA.isEmpty() )
			if( !serverSchemas.containsKey( SCHEMA ) )
				exit( "schema " + SCHEMA + " is not part of server datasets" );

		// load tableset
		tableSet = ms.readDatapumpFile( this , TABLESETFILE , SCHEMA );
	}

	private RemoteFolder prepareDestination() throws Exception {
		DistRepository repository = artefactory.getDistRepository( this );
		RemoteFolder folder = repository.getDataFolder( this , DATASET );
		folder.ensureExists( this );
		
		if( !folder.isEmpty( this ) ) {
			if( SCHEMA.isEmpty() && !CMD.equals( "data" ) ) {
				RemoteFolder backup = repository.getDataFolder( this , DATASET + "-backup" );
				log( "storage folder is not empty, backup to " + backup.folderPath + " ..." );
				
				backup.ensureExists( this );
				backup.removeAll( this );
				folder.moveAll( this , backup.folderPath );
			}
			else {
				log( "storage folder is not empty, will overwrite dumps if any ..." );
			}
		}
		else {
			if( CMD.equals( "data" ) )
				exit( "storage folder is empty, data files will not be usable without metadata" );
		}
		
		return( folder );
	}
	
	private void makeTargetScripts() throws Exception {
		// copy scripts
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder urmScripts = urm.getDatapumpScripts( this , server.DBMSTYPE );
		RedistStorage storage = artefactory.getRedistStorage( "database" , client.getDatabaseAccount( this ) );
		RemoteFolder redist = storage.getRedistTmpFolder( this );
		
		RemoteFolder exportFolder = redist.getSubFolder( this , "export" );
		exportScriptsFolder = exportFolder.getSubFolder( this , "scripts" );
		
		// ensure not running currently 
		if( exportScriptsFolder.checkFileExists( this , "run.sh" ) ) {
			String value = checkStatus( exportScriptsFolder );
			if( value.equals( "RUNNING" ) )
				exit( "unable to start because export is already running" );
		}
		
		log( "copy execution part to " + redist.folderPath + " ..." );
		exportFolder.recreateThis( this );
		exportScriptsFolder.ensureExists( this );
		exportScriptsFolder.copyDirContentFromLocal( this , urmScripts , "" );
		
		exportLogFolder = exportFolder.getSubFolder( this , "log" );
		exportLogFolder.ensureExists( this );
		exportDataFolder = exportFolder.getSubFolder( this , "data" );
		exportDataFolder.ensureExists( this );
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
		conf.add( "CONF_STANDBY=" + Common.getBooleanValue( STANDBY ) );
		Common.createFileFromStringList( confFile , conf );
		exportScriptsFolder.copyFileFromLocal( this , confFile );
		
		MetadataStorage ms = artefactory.getMetadataStorage( this );
		String tablesFilePath = work.getFilePath( this , MetadataStorage.tablesFileName );
		ms.saveDatapumpSet( this , tableSet , server , tablesFilePath );
		exportScriptsFolder.copyFileFromLocal( this , tablesFilePath );
	}

	private void runAll() throws Exception {
		if( !STANDBY ) {
			MetadataStorage ms = artefactory.getMetadataStorage( this );
			ms.loadDatapumpSet( this , tableSet , server , STANDBY , true );
		}
		
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
	
	public String checkStatus( RemoteFolder folder ) throws Exception {
		ShellExecutor shell = folder.getSession( this );
		String value = shell.customGetValue( this , folder.folderPath , "./run.sh export status" );
		return( value );
	}
	
	private void runTarget( String cmd , String SN ) throws Exception {
		// skip data for missing schema
		if( cmd.equals( "data" ) ) {
			if( !tableSet.containsKey( SN ) ) {
				log( "skip export data schema=" + SN + " due to empty tableset" );
				return;
			}
		}
		
		LocalFolder workFolder = artefactory.getWorkFolder( this );
		
		// initiate execution
		log( "start export cmd=" + cmd + " schemaset=" + SN + " ..." );
		ShellExecutor shell = exportScriptsFolder.getSession( this );
		shell.customCheckStatus( this , exportScriptsFolder.folderPath , "./run.sh export start " + cmd + " " + Common.getQuoted( SN ) );
		
		// check execution is started
		Common.sleep( this , 1000 );
		String value = checkStatus( exportScriptsFolder );
		if( value.equals( "RUNNING" ) == false && value.equals( "FINISHED" ) == false ) {
			log( "export has not been started (status=" + value + "), save logs ..." );
			
			String logFileName = cmd + "-" + SN + "run.sh.log";
			exportScriptsFolder.copyFileToLocalRename( this , workFolder , "run.sh.log" , logFileName );
			distLogFolder.copyFileFromLocal( this , workFolder.getFilePath( this , logFileName ) );
			copyDataAndLogs( false , cmd , SN );
			exit( "unable to start export process, see logs" );
		}
		
		// wait for completion - unlimited
		if( value.equals( "RUNNING" ) )
			log( "wait export to complete ..." );
		while( value.equals( "RUNNING" ) ) {
			Common.sleep( this , context.CTX_COMMANDTIMEOUT );
			value = checkStatus( exportScriptsFolder );
		}
		
		// copy top log
		String logFileName = cmd + "-" + SN + "-run.sh.log";
		exportScriptsFolder.copyFileToLocalRename( this , workFolder , "run.sh.log" , logFileName );
		distLogFolder.copyFileFromLocal( this , workFolder.getFilePath( this , logFileName ) );
		
		// check final status
		if( !value.equals( "FINISHED" ) ) {
			log( "export finished with errors, save logs ..." );
			copyDataAndLogs( false , cmd , SN );
			exit( "export process completed with errors, see logs" );
		}
		
		log( "export successfully finished, copy data and logs ..." );
		copyDataAndLogs( true , cmd , SN );
	}
	
	private void copyDataAndLogs( boolean succeeded , String cmd , String SN ) throws Exception {
		// copy logs
		if( cmd.equals( "meta" ) ) {
			String logMetaFiles = "meta-*.log";
			copyFiles( logMetaFiles , exportLogFolder , distLogFolder );
		}
		else if( cmd.equals( "data" ) ) {
			String logDataFiles = "data-" + SN + "-*.log";
			copyFiles( logDataFiles , exportLogFolder , distLogFolder );
		}
		
		// copy data
		if( succeeded ) {
			if( cmd.equals( "meta" ) ) {
				String dataFiles = "meta-*.dump";
				copyFiles( dataFiles , exportDataFolder , distDataFolder );
			}
			if( cmd.equals( "data" ) ) {
				String dataFiles = "data-" + SN + "-*.dump";
				copyFiles( dataFiles , exportDataFolder , distDataFolder );
			}
		}
	}

	private void copyFiles( String files , RemoteFolder exportFolder , RemoteFolder distFolder ) throws Exception {
		log( "copy files: " + files + " ..." );
		
		LocalFolder workDataFolder = artefactory.getWorkFolder( this , "data" );
		workDataFolder.recreateThis( this );
		exportFolder.copyFilesToLocal( this , workDataFolder , files );
		String[] copied = workDataFolder.findFiles( this , files );
		
		if( copied.length == 0 )
			exit( "unable to find files: " + files );
		
		// copy to target
		distFolder.moveFilesFromLocal( this , workDataFolder , files );
		
		// cleanup source
		exportFolder.removeFiles( this , files );
	}

}
