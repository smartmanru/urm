package ru.egov.urm.run.database;

import java.util.HashMap;
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
	
	Map<String,MetaDatabaseSchema> serverSchemas;
	Map<String,Map<String,String>> tableSet;

	RemoteFolder distDataFolder;
	RemoteFolder distLogFolder;
	RemoteFolder exportScriptsFolder;
	RemoteFolder exportLogFolder;
	RemoteFolder exportDataFolder;
	String tablesetPath;
	DatabaseClient client;
	
	static String tablesFileName = "tableset.txt"; 
	
	public ActionExportDatabase( ActionBase action , String stream , MetaEnvServer server , String CMD , String SCHEMA ) {
		super( action , stream );
		this.server = server;
		this.SPECFILE = "export-" + meta.env.ID + "-" + meta.dc.NAME + "-" + server.NAME + ".conf";
		this.CMD = CMD;
		this.SCHEMA = SCHEMA;
	}

	@Override protected boolean executeSimple() throws Exception {
		loadExportSettings();
		
		client = new DatabaseClient( server ); 
		client.checkConnect( this );
		
		exportDataFolder = prepareDestination();
		exportLogFolder = exportDataFolder.getSubFolder( this , "log" );
		makeTargetScripts();
		makeTargetConfig();
		runAll();
		
		return( true );
	}

	private void loadExportSettings() throws Exception {
		MetadataStorage ms = artefactory.getMetadataStorage( this ); 
		String specPath = ms.getDatapumpFile( this , SPECFILE );
		
		log( "reading export specification file " + specPath + " ..." );
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
		tablesetPath = ms.getDatapumpFile( this , TABLESETFILE );
		log( "reading export table set file " + tablesetPath + " ..." );
		tableSet = new HashMap<String,Map<String,String>>();
		for( String line : ConfReader.readFileLines( this , tablesetPath ) ) {
			if( line.isEmpty() || line.startsWith( "#" ) )
				continue;
			
			String[] opts = Common.split( line , "/" );
			if( opts.length != 2 )
				exit( "invalid table set line=" + line );
			String SN = opts[0]; 
			String table = opts[1]; 
			if( SN.isEmpty() || table.isEmpty() )
				exit( "invalid table set line=" + line );

			if( CMD.equals( "data" ) && !SCHEMA.isEmpty() )
				if( !SN.equals( SCHEMA ) )
					continue;
			
			Map<String,String> tables = tableSet.get( SN );
			if( tables == null ) {
				meta.distr.database.getSchema( this , SN );
				tables = new HashMap<String,String>();
				tableSet.put( SN , tables );
			}
			
			tables.put( table , SN );
		}
	}

	private RemoteFolder prepareDestination() throws Exception {
		DistRepository repository = artefactory.getDistRepository( this );
		RemoteFolder folder = repository.getDataFolder( this , DATASET );
		folder.ensureExists( this );
		
		if( !folder.isEmpty( this ) ) {
			RemoteFolder backup = repository.getDataFolder( this , DATASET + "-backup" );
			log( "data folder is not empty, backup to " + backup.folderPath + " ..." );
			
			backup.ensureExists( this );
			backup.removeAll( this );
			folder.moveAll( this , backup.folderPath );
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
		Common.createFileFromStringList( confFile , conf );
		exportScriptsFolder.copyFileFromLocal( this , confFile );
	}

	private void runAll() throws Exception {
		// load table set into database
		String[] columns = { "xschema" , "xtable" };
		String[] columntypes = { "varchar(30)" , "varchar(30)" };
		List<String[]> data = new LinkedList<String[]>();
		List<String> conf = new LinkedList<String>();
		
		for( String SN : tableSet.keySet() ) {
			MetaDatabaseSchema schema = serverSchemas.get( SN );
			Map<String,String> tables = tableSet.get( SN );
			
			if( tables.containsKey( "*" ) ) {
				data.add( new String[] { schema.DBNAME , "*" } );
				conf.add( schema.DBNAME + "/*" );
			}
			else {
				for( String s : tables.keySet() ) {
					data.add( new String[] { schema.DBNAME , s } );
					conf.add( schema.DBNAME + "/" + s );
				}
			}
		}
		
		client.createTableData( this , server.admSchema , "urm_export" , columns , columntypes , data );  
		Common.createFileFromStringList( tablesFileName , conf );
		exportScriptsFolder.copyFileFromLocal( this , tablesFileName );
		
		if( CMD.equals( "all" ) || CMD.equals( "meta" ) )
			runTarget( "meta" , "" );
		
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
		String value = shell.customGetValue( this , folder.folderPath , "./run.sh status" );
		if( value.equals( "RUNNING" ) )
			exit( "unable to start because export is already running" );
		return( value );
	}
	
	private void runTarget( String cmd , String SN ) throws Exception {
		String EXECUTESCHEMA = "";
		if( cmd.equals( "meta" ) ) {
			for( MetaDatabaseSchema schema : serverSchemas.values() )
				EXECUTESCHEMA = Common.addItemToUniqueSpacedList( EXECUTESCHEMA , schema.DBNAME ); 
		}
		else
		if( cmd.equals( "data" ) ) {
			MetaDatabaseSchema schema = serverSchemas.get( SN );
			EXECUTESCHEMA = schema.DBNAME;
		}
		
		// initiate execution
		log( "start export cmd=" + cmd + " schemaset=" + EXECUTESCHEMA + " ..." );
		ShellExecutor shell = exportScriptsFolder.getSession( this );
		shell.customCheckStatus( this , exportScriptsFolder.folderPath , "./run.sh start " + cmd + " " + Common.getQuoted( EXECUTESCHEMA ) );
		
		// check execution is started
		Common.sleep( this , 1000 );
		String value = checkStatus( exportScriptsFolder );
		if( !value.equals( "RUNNING" ) ) {
			log( "export has not started, save logs ..." );
			copyDataAndLogs( false , cmd , SN , EXECUTESCHEMA );
			exit( "unable to start export process, see logs" );
		}
		
		// wait for completion - unlimited
		log( "wait export to complete ..." );
		while( value.equals( "RUNNING" ) )
			value = checkStatus( exportScriptsFolder );
		
		// check final status
		if( !value.equals( "FINISHED" ) ) {
			log( "export finished with errors, save logs ..." );
			copyDataAndLogs( false , cmd , SN , EXECUTESCHEMA );
			exit( "export process completed with errors, see logs" );
		}
		
		log( "export successfully finished, copy data and logs ..." );
		copyDataAndLogs( true , cmd , SN , EXECUTESCHEMA );
	}
	
	private void copyDataAndLogs( boolean copyData , String cmd , String SN , String EXECUTESCHEMA ) throws Exception {
		// copy logs
		String logFiles = "meta-*.log";
		String dataFiles = "data-" + EXECUTESCHEMA + "-*.log";
		copyFiles( logFiles , exportLogFolder , distLogFolder );
		
		// copy data
		if( copyData )
			copyFiles( dataFiles , exportDataFolder , distDataFolder );
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
	}

}
