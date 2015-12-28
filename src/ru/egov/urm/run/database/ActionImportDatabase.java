package ru.egov.urm.run.database;

import java.util.Map;
import java.util.Properties;

import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.DistRepository;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.MetadataStorage;
import ru.egov.urm.storage.RemoteFolder;

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
	}
	
	private void makeTargetConfig() throws Exception {
	}
	
	private void runAll() throws Exception {
	}
	
}
