package ru.egov.urm.run.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.DistRepository;
import ru.egov.urm.storage.MetadataStorage;
import ru.egov.urm.storage.RemoteFolder;

public class ActionExportDatabase extends ActionBase {

	String SPECFILE;
	String CMD;
	String SCHEMA;

	String DATASET;
	String SCHEMALIST;
	String SCHMAPPING;
	String TABLESETFILE;
	String DUMPDIR;
	String REMOTE_SETDBENV;
	String DATABASE_DATAPUMPDIR;
	
	Map<String,Map<String,String>> tableSet;
	
	public ActionExportDatabase( ActionBase action , String stream , String SPECFILE , String CMD , String SCHEMA ) {
		super( action , stream );
		this.SPECFILE = SPECFILE;
		this.CMD = CMD;
		this.SCHEMA = SCHEMA;
	}

	@Override protected boolean executeSimple() throws Exception {
		loadExportSettings();
		
		RemoteFolder dataFolder = prepareDestination();
		makeTargetScripts();
		makeTargetConfig();
		runTarget();
		copyDataAndLogs( dataFolder );
		
		return( true );
	}

	private void loadExportSettings() throws Exception {
		MetadataStorage ms = artefactory.getMetadataStorage( this ); 
		String specPath = ms.getDatapumpFile( this , SPECFILE );
		
		log( "reading export specification file " + specPath + " ..." );
		Properties props = ConfReader.readPropertyFile( this , specPath );
		
		DATASET = props.getProperty( "CONFIG_DATASET" );
		SCHEMALIST = props.getProperty( "CONFIG_SCHEMALIST" );
		SCHMAPPING = props.getProperty( "CONFIG_SCHMAPPING" );
		TABLESETFILE = props.getProperty( "CONFIG_TABLESETFILE" );
		DUMPDIR = props.getProperty( "CONFIG_LOADDIR" );
		REMOTE_SETDBENV = props.getProperty( "CONFIG_REMOTE_SETDBENV" );
		DATABASE_DATAPUMPDIR = props.getProperty( "CONFIG_DATABASE_DATAPUMPDIR" );
		
		// load tableset
		String tablesetPath = ms.getDatapumpFile( this , TABLESETFILE );
		log( "reading export table set file " + tablesetPath + " ..." );
		tableSet = new HashMap<String,Map<String,String>>();
		for( String line : ConfReader.readFileLines( this , tablesetPath ) ) {
			if( line.isEmpty() || line.startsWith( "#" ) )
				continue;
			
			String[] opts = Common.split( line , "/" );
			if( opts.length != 2 )
				exit( "invalid table set line=" + line );
			String schema = opts[0]; 
			String table = opts[1]; 
			if( schema.isEmpty() || table.isEmpty() )
				exit( "invalid table set line=" + line );
			
			Map<String,String> tables = tableSet.get( schema );
			if( tables == null ) {
				tables = new HashMap<String,String>();
				tableSet.put( schema , tables );
			}
			
			tables.put( table , schema );
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
	}
	
	private void makeTargetConfig() throws Exception {
	}
	
	private void runTarget() throws Exception {
	}
	
	private void copyDataAndLogs( RemoteFolder dataFolder ) throws Exception {
	}
	
}
