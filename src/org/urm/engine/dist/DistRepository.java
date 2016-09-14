package org.urm.engine.dist;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerContext;
import org.urm.engine.action.ActionBase;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.meta.MetaProductBuildSettings;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.RemoteFolder;

public class DistRepository {

	Artefactory artefactory;
	private RemoteFolder repoFolder;
	Meta meta;

	static String RELEASEHISTORYFILE = "history.txt";
	
	private DistRepository( Artefactory artefactory ) {
		this.artefactory = artefactory; 
		this.meta = artefactory.meta;
	}
	
	public static DistRepository getDistRepository( ActionBase action , Artefactory artefactory ) throws Exception {
		DistRepository repo = new DistRepository( artefactory ); 
		
		String distPath = action.context.CTX_DISTPATH;
		
		Account account = action.getLocalAccount();
		if( action.session.standalone ) {
			if( distPath.isEmpty() ) {
				if( action.context.env != null )
					distPath = action.context.env.DISTR_PATH;
			}
			
			if( distPath.isEmpty() )
				action.exit0( _Error.DistPathNotDefined0 , "DISTPATH is not defined in product configuration" );
				
			if( action.context.env != null ) {
				if( !action.isLocalRun() )
					account = Account.getAccount( action , action.context.env.DISTR_HOSTLOGIN , VarOSTYPE.LINUX );
			}
			else {
				if( !action.isLocalRun() )
					account = Account.getAccount( action , action.meta.product.CONFIG_DISTR_HOSTLOGIN , VarOSTYPE.LINUX );
			}
		}
		else {
			if( distPath.isEmpty() ) {
				ServerContext sc = action.actionInit.getServerContext();
				distPath = sc.DIST_ROOT;
			}
			
			if( distPath.isEmpty() )
				action.exit0( _Error.DistPathNotDefined0 , "DISTPATH is not defined in server configuration" );
		}
		
		repo.repoFolder = new RemoteFolder( account , distPath );
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
			action.info( "save data backup to " + backupFolder.folderPath + " ..." );
			backupFolder.removeThis( action );
			backupFolder.ensureExists( action );
			dataFolder.moveAll( action , backupFolder.folderPath );
		}

		// move all from new to data
		action.info( "move new data to " + dataFolder.folderPath + " ..." );
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
		Dist storage = new Dist( meta , this );
		storage.setFolder( distFolder , prod );
		
		// check release directory exists
		if( !distFolder.checkExists( action ) )
			action.exit1( _Error.MissingRelease1 , "release does not exist at " + distFolder.folderPath , distFolder.folderPath );
		
		storage.load( action );
		return( storage );
	}

	public Dist createDist( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEPATH = getReleasePathByLabel( action , RELEASELABEL );
		String RELEASEDIR = Common.getBaseName( RELEASEPATH );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , RELEASEPATH );
		Dist storage = new Dist( meta , this );
		storage.setFolder( distFolder , false );
		
		// check release directory exists
		if( distFolder.checkExists( action ) )
			action.ifexit( _Error.ReleaseAlreadyExists1 , "release already exists at " + RELEASEPATH , new String[] { RELEASEPATH } );

		storage.create( action , RELEASEDIR );
		return( storage );
	}

	public String getReleaseProdDir( ActionBase action ) throws Exception {
		return( getReleasePathByLabel( action , "prod" ) );
	}

	public String normalizeReleaseVer( ActionBase action , String RELEASEVER ) throws Exception {
		String[] items = Common.splitDotted( RELEASEVER );
		if( items.length < 2 && items.length > 4 )
			action.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
		
		String value = "";
		for( int k = 0; k < 4; k++ ) {
			if( k > 0 )
				value += ".";
			if( k >= items.length )
				value += "0";
			else {
				if( !items[k].matches( "[0-9]+" ) )
					action.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
				if( items[k].length() > 3 )
					action.exit1( _Error.InvalidReleaseVersion1 , "invalid release version=" + RELEASEVER , RELEASEVER );
				value += items[k];
			}
		}
		
		return( value );
	}
	
	private String getReleaseDirByVer( ActionBase action , String RELEASEVER ) throws Exception {
		RELEASEVER = normalizeReleaseVer( action , RELEASEVER );
		String[] items = Common.splitDotted( RELEASEVER );
		if( items[3].equals( "0" ) ) {
			if( items[2].equals( "0" ) )
				return( items[0] + "." + items[1] );
			return( items[0] + "." + items[1] + "." + items[2] );
		}
		return( RELEASEVER );
	}
	
	public String getReleaseVerByDir( ActionBase action , String RELEASEDIR ) throws Exception {
		String RELEASEVER = Common.getPartBeforeFirst( RELEASEDIR , "-" );
		RELEASEVER = normalizeReleaseVer( action , RELEASEVER );
		return( RELEASEVER );
	}
	
	private String getReleasePathByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		if( RELEASELABEL.equals( "default" ) && !action.context.CTX_DISTPATH.isEmpty() )
			return( "." );
		
		String RELEASEPATH = "";
		String RELEASEVER = "";
		String RELEASEDIR = "";
		
		if( RELEASELABEL.equals( "prod" ) ) {
			RELEASEPATH = "prod";
			RELEASEVER = "(prod)";

			// check content
			if( !repoFolder.checkFolderExists( action , RELEASEPATH ) )
				action.exit0( _Error.UnableFindProdDistributive0 , "getReleaseVerByLabel: unable to find prod distributive" );
		}
		else
		if( RELEASELABEL.indexOf( "-" ) > 0 ) {
			RELEASEDIR = RELEASELABEL;
			RELEASEVER = getReleaseVerByDir( action , RELEASEDIR );
			RELEASEPATH = "releases/" + RELEASEDIR;
		}
		else {
			RELEASEVER = getReleaseVerByLabel( action , RELEASELABEL );
			RELEASEDIR = getReleaseDirByVer( action , RELEASEVER );
			RELEASEPATH = "releases/" + RELEASEDIR;
		}
		
		action.debug( "found release directory=" + RELEASEPATH + " by label=" + RELEASELABEL + "( RELEASEVER=" + RELEASEVER + ")" );
		return( RELEASEPATH );
	}
	
	public String getReleaseVerByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );

		MetaProductBuildSettings build = action.getBuildSettings();
		
		String RELEASEVER = "";
		if( RELEASELABEL.equals( "last" ) ) {
			RELEASEVER = build.CONFIG_RELEASE_LASTMINOR;
			if( RELEASEVER.isEmpty() )
				action.exit0( _Error.LastMinorVersionNotSet0 , "Last minor release version is not set in product settings" );

			return( RELEASEVER );
		}
		
		if( RELEASELABEL.equals( "next" ) ) {
			RELEASEVER = build.CONFIG_RELEASE_NEXTMINOR;
			if( RELEASEVER.isEmpty() )
				action.exit0( _Error.NextMinorVersionNotSet0 , "Next minor release version is not set in product settings" );

			return( RELEASEVER );
		}
		
		if( RELEASELABEL.indexOf( "-" ) < 0 ) {
			RELEASEVER = normalizeReleaseVer( action , RELEASELABEL );
			return( RELEASEVER );
		}

		action.exit1( _Error.UnexpectedReleaseLabel1 , "unexpected release label=" + RELEASELABEL , RELEASELABEL );
		return( null );
	}
	
	public void createProd( ActionBase action , String RELEASEVER ) throws Exception {
		String PRODPATH = getReleasePathByLabel( action , "prod" );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , PRODPATH );
		if( !distFolder.checkExists( action ) )
			action.exit1( _Error.MissingProdFolder1 , "prod folder does not exists at " + distFolder.folderPath , distFolder.folderPath );
		
		if( action.context.CTX_FORCE ) {
			distFolder.removeFiles( action , "history.txt state.txt" );
		}
		else {
			if( distFolder.checkFileExists( action , RELEASEHISTORYFILE ) )
				action.exit1( _Error.ProdFolderAlreadyInitialized1 , "prod folder is probably already initialized, delete history.txt manually to recreate" , distFolder.folderPath );
		}
		
		Dist storage = new Dist( meta , this );
		storage.setFolder( distFolder , true );
		distFolder.createFileFromString( action , RELEASEHISTORYFILE , getHistoryRecord( action , RELEASEVER , "add" ) );
		storage.createProd( action , RELEASEVER );
		
		storage.finish( action );
	}

	private String getHistoryRecord( ActionBase action , String RELEASEVER , String operation ) throws Exception {
		String s = Common.getNameTimeStamp() + ":" + operation + ":" + RELEASEVER;
		return( s );
	}

}
