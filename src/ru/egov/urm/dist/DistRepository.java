package ru.egov.urm.dist;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.Artefactory;
import ru.egov.urm.storage.RemoteFolder;

public class DistRepository {

	Artefactory artefactory;
	private RemoteFolder repoFolder;
	Metadata meta;

	static String RELEASEHISTORYFILE = "history.txt";
	
	private DistRepository( Artefactory artefactory ) {
		this.artefactory = artefactory; 
		this.meta = artefactory.meta;
	}
	
	public static DistRepository getDistRepository( ActionBase action , Artefactory artefactory ) throws Exception {
		DistRepository repo = new DistRepository( artefactory ); 
		
		String distPath = action.context.CTX_DISTPATH;
		if( distPath.isEmpty() ) {
			if( action.context.env == null )
				action.exit( "DISTPATH is not defined in product configuration" );
			distPath = action.context.env.DISTR_PATH;
		}
		
		Account account = action.session.account;
		if( action.context.env != null ) {
			if( !action.isLocal() )
				account = Account.getAccount( action , action.context.env.DISTR_HOSTLOGIN , VarOSTYPE.LINUX );
		}
		else {
			if( !action.isLocal() )
				account = Account.getAccount( action , action.meta.product.CONFIG_DISTR_HOSTLOGIN , VarOSTYPE.LINUX );
		}
		
		repo.repoFolder = new RemoteFolder( artefactory , account , distPath );
		ShellExecutor shell = action.getShell( account );
		shell.tmpFolder.recreateThis( action );
				
		return( repo );
	}

	public RemoteFolder getDataSetFolder( ActionBase action , String dataSet ) throws Exception {
		return( repoFolder.getSubFolder( action , "data/" + dataSet ) );
	}
	
	public RemoteFolder getDataFolder( ActionBase action , String dataSet ) throws Exception {
		RemoteFolder folder = getDataSetFolder( action , dataSet );
		return( folder.getSubFolder( action , "dump" ) );
	}
	
	public RemoteFolder getDataNewFolder( ActionBase action , String dataSet ) throws Exception {
		RemoteFolder folder = getDataSetFolder( action , dataSet );
		return( folder.getSubFolder( action , "dump-new" ) );
	}
	
	public RemoteFolder getDataBackupFolder( ActionBase action , String dataSet ) throws Exception {
		RemoteFolder folder = getDataSetFolder( action , dataSet );
		return( folder.getSubFolder( action , "dump-backup" ) );
	}
	
	public RemoteFolder getExportLogFolder( ActionBase action , String dataSet ) throws Exception {
		RemoteFolder folder = getDataSetFolder( action , dataSet );
		return( folder.getSubFolder( action , "log-export" ) );
	}
	
	public void copyNewToPrimary( ActionBase action , String dataSet , boolean full ) throws Exception {
		RemoteFolder dataFolder = getDataFolder( action , dataSet );
		RemoteFolder backupFolder = getDataBackupFolder( action , dataSet );
		RemoteFolder newFolder = getDataNewFolder( action , dataSet );
		
		// move data to backup, if not partial
		dataFolder.ensureExists( action );
		if( full && !dataFolder.isEmpty( action ) ) {
			action.log( "save data backup to " + backupFolder.folderPath + " ..." );
			backupFolder.removeThis( action );
			backupFolder.ensureExists( action );
			dataFolder.moveAll( action , backupFolder.folderPath );
		}

		// move all from new to data
		action.log( "move new data to " + dataFolder.folderPath + " ..." );
		newFolder.moveAll( action , dataFolder.folderPath );
		newFolder.removeThis( action );
	}
	
	public RemoteFolder getImportLogFolder( ActionBase action , String dataSet , MetaEnvServer server ) throws Exception {
		String location = server.dc.env.ID + "-" + server.dc.NAME + "-" + server.NAME;
		return( repoFolder.getSubFolder( action , "data/" + dataSet + "/log-import-" + location ) );
	}
	
	public Dist getDistByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEPATH = getReleasePathByLabel( action , RELEASELABEL );
		boolean prod = RELEASELABEL.equals( "prod" );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , RELEASEPATH );
		Dist storage = new Dist( artefactory , distFolder , prod );
		
		// check release directory exists
		if( !distFolder.checkExists( action ) )
			action.exit( "release does not exist at " + distFolder.folderPath );
		
		storage.load( action );
		return( storage );
	}

	public Dist createDist( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEPATH = getReleasePathByLabel( action , RELEASELABEL );
		String RELEASEVER = Common.getBaseName( RELEASEPATH );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , RELEASEPATH );
		Dist storage = new Dist( artefactory , distFolder , false );
		
		// check release directory exists
		if( distFolder.checkExists( action ) ) {
			if( !action.context.CTX_FORCE )
				action.exit( "release already exists at " + RELEASEPATH );
		}

		storage.create( action , RELEASEVER );
		return( storage );
	}

	public String getReleaseProdDir( ActionBase action ) throws Exception {
		return( getReleasePathByLabel( action , "prod" ) );
	}
	
	private String getReleasePathByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		if( RELEASELABEL.equals( "default" ) && !action.context.CTX_DISTPATH.isEmpty() )
			return( "." );
		
		String RELEASEPATH = "";
		if( RELEASELABEL.equals( "last" ) ) {
			RELEASEPATH = meta.product.CONFIG_VERSION_LAST_FULL;
			if( RELEASEPATH.isEmpty() )
				action.exit( "CONFIG_VERSION_LAST_FULL is not set in product.conf" );
			
			RELEASEPATH = "releases/" + RELEASEPATH;
		}
		else if( RELEASELABEL.equals( "next" ) ) {
			RELEASEPATH = meta.product.CONFIG_VERSION_NEXT_FULL;
			if( RELEASEPATH.isEmpty() )
				action.exit( "CONFIG_VERSION_NEXT_FULL is not set in product.conf" );

			RELEASEPATH = "releases/" + RELEASEPATH;
		}
		else if( RELEASELABEL.equals( "prod" ) ) {
			RELEASEPATH = "prod";

			// check content
			if( !repoFolder.checkFolderExists( action , RELEASEPATH ) )
				action.exit( "getReleaseVerByLabel: unable to find prod distributive" );
		}
		else
			RELEASEPATH = "releases/" + RELEASELABEL;
		
		if( !RELEASELABEL.equals( RELEASEPATH ))
			action.debug( "found release directory=" + RELEASEPATH + " by label=" + RELEASELABEL );
		
		return( RELEASEPATH );
	}
	
	public void createProd( ActionBase action , String RELEASEVER ) throws Exception {
		String PRODPATH = getReleasePathByLabel( action , "prod" );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , PRODPATH );
		if( !distFolder.checkExists( action ) )
			action.exit( "prod folder does not exists at " + distFolder.folderPath );
		
		if( action.context.CTX_FORCE ) {
			distFolder.removeFiles( action , "history.txt state.txt" );
		}
		else {
			if( distFolder.checkFileExists( action , RELEASEHISTORYFILE ) )
				action.exit( "prod folder is probably already initialized, delete history.txt manually to recreate" );
		}
		
		Dist storage = new Dist( artefactory , distFolder , true );
		distFolder.createFileFromString( action , RELEASEHISTORYFILE , getHistoryRecord( action , RELEASEVER , "add" ) );
		storage.createProd( action , RELEASEVER );
		
		storage.finish( action );
	}

	private String getHistoryRecord( ActionBase action , String RELEASEVER , String operation ) throws Exception {
		String s = Common.getNameTimeStamp() + ":" + operation + ":" + RELEASEVER;
		return( s );
	}
	
}
