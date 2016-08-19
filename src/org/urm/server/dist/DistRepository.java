package org.urm.server.dist;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.ServerContext;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.Meta;
import org.urm.server.meta.MetaVersion;
import org.urm.server.shell.Account;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.Artefactory;
import org.urm.server.storage.RemoteFolder;

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
				action.exit( "DISTPATH is not defined in product configuration" );
				
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
				distPath = sc.DISTR_PATH;
			}
			
			if( distPath.isEmpty() )
				action.exit( "DISTPATH is not defined in server configuration" );
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
			action.exit( "release does not exist at " + distFolder.folderPath );
		
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
			action.ifexit( "release already exists at " + RELEASEPATH );

		storage.create( action , RELEASEDIR );
		return( storage );
	}

	public String getReleaseProdDir( ActionBase action ) throws Exception {
		return( getReleasePathByLabel( action , "prod" ) );
	}

	public String normalizeReleaseVer( ActionBase action , String RELEASEVER ) throws Exception {
		String[] items = Common.splitDotted( RELEASEVER );
		if( items.length < 2 && items.length > 4 )
			action.exit( "invalid release version=" + RELEASEVER );
		
		String value = "";
		for( int k = 0; k < 4; k++ ) {
			if( k > 0 )
				value += ".";
			if( k >= items.length )
				value += "0";
			else {
				if( !items[k].matches( "[0-9]+" ) )
					action.exit( "invalid release version=" + RELEASEVER );
				if( items[k].length() > 3 )
					action.exit( "invalid release version=" + RELEASEVER );
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
				action.exit( "getReleaseVerByLabel: unable to find prod distributive" );
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

		String RELEASEVER = "";
		if( RELEASELABEL.equals( "last" ) ) {
			RELEASEVER = meta.product.CONFIG_VERSION_LAST_FULL;
			if( RELEASEVER.isEmpty() )
				action.exit( "CONFIG_VERSION_LAST_FULL is not set in product.conf" );

			return( RELEASEVER );
		}
		
		if( RELEASELABEL.equals( "next" ) ) {
			RELEASEVER = meta.product.CONFIG_VERSION_NEXT_FULL;
			if( RELEASEVER.isEmpty() )
				action.exit( "CONFIG_VERSION_NEXT_FULL is not set in product.conf" );

			return( RELEASEVER );
		}
		
		if( RELEASELABEL.indexOf( "-" ) < 0 ) {
			RELEASEVER = normalizeReleaseVer( action , RELEASELABEL );
			return( RELEASEVER );
		}

		action.exit( "unexpected release label=" + RELEASELABEL );
		return( null );
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

	public MetaVersion getVersion( ActionBase action ) {
		MetaVersion version = new MetaVersion( action.meta );
		return( version );
	}
	
}
