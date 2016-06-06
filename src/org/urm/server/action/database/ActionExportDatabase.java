package org.urm.server.action.database;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.dist.DistRepository;
import org.urm.server.meta.MetaDatabaseSchema;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.MetadataStorage;
import org.urm.server.storage.RedistStorage;
import org.urm.server.storage.RemoteFolder;
import org.urm.server.storage.UrmStorage;

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
	boolean NFS;
	
	Map<String,MetaDatabaseSchema> serverSchemas;
	Map<String,Map<String,String>> tableSet;

	DistRepository repository;
	RemoteFolder distDataFolder;
	RemoteFolder distLogFolder;
	RemoteFolder exportScriptsFolder;
	RemoteFolder exportLogFolder;
	RemoteFolder exportDataFolder;
	DatabaseClient client;

	public ActionExportDatabase( ActionBase action , String stream , MetaEnvServer server , String CMD , String SCHEMA ) {
		super( action , stream );
		this.server = server;
		this.SPECFILE = "export-" + context.env.ID + "-" + server.dc.NAME + "-" + server.NAME + ".conf";
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
		
		prepareDestination();
		makeTargetScripts();
		makeTargetConfig();
		runAll();
		
		return( true );
	}

	private void loadExportSettings() throws Exception {
		MetadataStorage ms = artefactory.getMetadataStorage( this ); 
		String specPath = ms.getDatapumpFile( this , SPECFILE );
		
		info( "reading export specification file " + specPath + " ..." );
		Properties props = ConfReader.readPropertyFile( this , specPath );
		
		DATASET = props.getProperty( "CONFIG_DATASET" );
		TABLESETFILE = props.getProperty( "CONFIG_TABLESETFILE" );
		DUMPDIR = props.getProperty( "CONFIG_DATADIR" , "" );
		REMOTE_SETDBENV = props.getProperty( "CONFIG_REMOTE_SETDBENV" , "" );
		DATABASE_DATAPUMPDIR = props.getProperty( "CONFIG_DATABASE_DATAPUMPDIR" , "" );
		STANDBY = Common.getBooleanValue( props.getProperty( "CONFIG_STANDBY" ) ); 
		NFS = Common.getBooleanValue( props.getProperty( "CONFIG_NFS" ) ); 

		serverSchemas = server.getSchemaSet( this );
		if( CMD.equals( "data" ) && !SCHEMA.isEmpty() )
			if( !serverSchemas.containsKey( SCHEMA ) )
				exit( "schema " + SCHEMA + " is not part of server datasets" );

		// load tableset
		tableSet = ms.readDatapumpFile( this , TABLESETFILE , SCHEMA );
	}

	private void prepareDestination() throws Exception {
		repository = artefactory.getDistRepository( this );
		distDataFolder = repository.getDataNewFolder( this , DATASET );
		distDataFolder.ensureExists( this );
		distLogFolder = repository.getExportLogFolder( this , DATASET );
		distLogFolder.ensureExists( this );
	}
	
	private void makeTargetScripts() throws Exception {
		// copy scripts
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder urmScripts = urm.getDatapumpScripts( this , server );
		RedistStorage storage = artefactory.getRedistStorage( this , client.getDatabaseAccount( this ) );
		RemoteFolder redist = storage.getRedistTmpFolder( this , "database" );
		
		RemoteFolder exportFolder = redist.getSubFolder( this , "export" );
		exportScriptsFolder = exportFolder.getSubFolder( this , "scripts" );
		
		// ensure not running currently 
		if( exportScriptsFolder.checkFileExists( this , "run.sh" ) ) {
			String value = checkStatus( exportScriptsFolder );
			if( value.equals( "RUNNING" ) )
				exit( "unable to start because export is already running" );
		}
		
		info( "copy execution part to " + redist.folderPath + " ..." );
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
		if( NFS ) {
			conf.add( "CONF_NFS=" + Common.getBooleanValue( NFS ) );
			conf.add( "CONF_NFSDATA=" + distDataFolder.folderPath );
			conf.add( "CONF_NFSLOG=" + distLogFolder.folderPath );
		}
		
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
		
		boolean full = ( CMD.equals( "all" ) )? true : false;
		if( full || CMD.equals( "meta" ) )
			runTarget( "meta" , "all" );
		
		if( full || CMD.equals( "data" ) ) {
			if( CMD.equals( "data" ) && !SCHEMA.isEmpty() )
				runTarget( "data" , SCHEMA );
			else {
				for( String s : server.getSchemaSet( this ).keySet() )
					runTarget( "data" , s );
			}
		}

		info( "export has been finished, dumps are copied to " + distDataFolder.folderPath );
		
		// complete
		repository.copyNewToPrimary( this , DATASET , full );
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
				info( "skip export data schema=" + SN + " due to empty tableset" );
				return;
			}
		}
		
		LocalFolder workFolder = artefactory.getWorkFolder( this );
		
		// initiate execution
		info( "start export cmd=" + cmd + " schemaset=" + SN + " ..." );
		ShellExecutor shell = exportScriptsFolder.getSession( this );
		shell.customCheckStatus( this , exportScriptsFolder.folderPath , "./run.sh export start " + cmd + " " + Common.getQuoted( SN ) );
		
		// check execution is started
		Common.sleep( this , 1000 );
		String value = checkStatus( exportScriptsFolder );
		if( value.equals( "RUNNING" ) == false && value.equals( "FINISHED" ) == false ) {
			info( "export has not been started (status=" + value + "), save logs ..." );
			
			String logFileName = cmd + "-" + SN + "run.sh.log";
			exportScriptsFolder.copyFileToLocalRename( this , workFolder , "run.sh.log" , logFileName );
			distLogFolder.copyFileFromLocal( this , workFolder.getFilePath( this , logFileName ) );
			copyDataAndLogs( false , cmd , SN );
			exit( "unable to start export process, see logs" );
		}
		
		// wait for completion - unlimited
		if( value.equals( "RUNNING" ) )
			info( "wait export to complete ..." );
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
			info( "export finished with errors, save logs ..." );
			copyDataAndLogs( false , cmd , SN );
			exit( "export process completed with errors, see logs" );
		}
		
		info( "export successfully finished, copy data and logs ..." );
		copyDataAndLogs( true , cmd , SN );
	}
	
	private void copyDataAndLogs( boolean succeeded , String cmd , String SN ) throws Exception {
		if( NFS ) {
			debug( "skip download data and log files, use NFS" );
			return;
		}
		
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
		info( "copy files: " + files + " ..." );
		
		LocalFolder workDataFolder = artefactory.getWorkFolder( this , "data" );
		workDataFolder.recreateThis( this );
		
		int timeout = setTimeoutUnlimited();
		exportFolder.copyFilesToLocal( this , workDataFolder , files );
		setTimeout( timeout );
		
		String[] copied = workDataFolder.findFiles( this , files );
		
		if( copied.length == 0 )
			exit( "unable to find files: " + files );
		
		// copy to target
		timeout = setTimeoutUnlimited();
		distFolder.moveFilesFromLocal( this , workDataFolder , files );
		
		// cleanup source
		exportFolder.removeFiles( this , files );
		setTimeout( timeout );
	}

}
