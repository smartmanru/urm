package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.shell.Account;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.ServerContext;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaProductSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DistRepository {

	Meta meta;
	RemoteFolder repoFolder;

	public Map<String,DistRepositoryItem> distMap; 
	
	static String RELEASEREPOSITORYFILE = "releases.xml";
	static String RELEASEHISTORYFILE = "history.txt";
	
	private DistRepository( Meta meta ) {
		this.meta = meta;
		distMap = new HashMap<String,DistRepositoryItem>();
	}
	
	public static DistRepository loadDistRepository( ActionBase action , Meta meta ) throws Exception {
		DistRepository repo = new DistRepository( meta );
		repo.open( action );
		return( repo );
	}

	public static DistRepository createInitialRepository( ActionBase action , Meta meta ) throws Exception {
		DistRepository repo = new DistRepository( meta );
		repo.create( action );
		return( repo );
	}

	private void open( ActionBase action ) throws Exception {
		repoFolder = getDistFolder( action );
		
		if( !repoFolder.checkExists( action ) ) {
			String path = repoFolder.getLocalPath( action );
			action.exit1( _Error.MissingReleaseRepository1 , "missing release repository at " + path , path );
		}
		
		readRepositoryFile( action );
	}
	
	private void create( ActionBase action ) throws Exception {
		repoFolder = getDistFolder( action );
		if( repoFolder.checkExists( action ) ) {
			String path = repoFolder.getLocalPath( action );
			action.exit1( _Error.ReleaseRepositoryExists1 , "unable to create release repository, already exists at " + path , path );
		}
		
		RemoteFolder parent = repoFolder.getParentFolder( action );
		if( !parent.checkExists( action ) ) {
			String path = parent.getLocalPath( action );
			action.exit1( _Error.MissingReleaseRepositoryParent1 , "unable to create release repository, missing parent path=" + path , path );
		}
			
		repoFolder.recreateThis( action );
		readRepositoryFile( action );
	}

	private void readRepositoryFile( ActionBase action ) throws Exception {
		distMap.clear();
		
		if( !repoFolder.checkFileExists( action , RELEASEREPOSITORYFILE ) ) {
			createRepositoryFile( action );
			return;
		}
			
		String repoFile = repoFolder.getFilePath( action , RELEASEREPOSITORYFILE );
		Document doc = action.readXmlFile( repoFile );
		Node root = doc.getDocumentElement();
		
		Node[] items = ConfReader.xmlGetChildren( root , "release" );
		if( items == null )
			return;
		
		for( Node releaseNode : items ) {
			DistRepositoryItem item = new DistRepositoryItem( this );
			item.load( action , releaseNode );
			distMap.put( item.VERSION , item );
		}
	}

	private void createRepositoryFile( ActionBase action ) throws Exception {
		String tmpFile = action.getWorkFilePath( RELEASEREPOSITORYFILE );
		Document doc = Common.xmlCreateDoc( "repository" );
		Element root = doc.getDocumentElement();
		Common.xmlSetElementAttr( doc , root , "created" , Common.getNameTimeStamp() );
		
		Common.xmlSaveDoc( doc , tmpFile );
		repoFolder.copyFileFromLocal( action , tmpFile );
		action.debug( "repository registry created at " + repoFolder.getLocalFilePath( action , RELEASEREPOSITORYFILE ) );
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
		String location = server.sg.env.ID + "-" + server.sg.NAME + "-" + server.NAME;
		return( repoFolder.getSubFolder( action , "data/" + dataSet + "/log-import-" + location ) );
	}
	
	public Dist getDistByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		DistLabelInfo info = getLabelInfo( action , RELEASELABEL );
		String RELEASEPATH = info.RELEASEPATH;
		boolean prod = info.prod;
		
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
		
		DistLabelInfo info = getLabelInfo( action , RELEASELABEL );
		String RELEASEPATH = info.RELEASEPATH;
		String RELEASEDIR = info.RELEASEDIR;
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , RELEASEPATH );
		Dist storage = new Dist( meta , this );
		storage.setFolder( distFolder , false );
		
		// check release directory exists
		if( distFolder.checkExists( action ) )
			action.ifexit( _Error.ReleaseAlreadyExists1 , "release already exists at " + RELEASEPATH , new String[] { RELEASEPATH } );

		storage.create( action , RELEASEDIR );
		return( storage );
	}

	private RemoteFolder getDistFolder( ActionBase action ) throws Exception {
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
					account = Account.getAccount( action , "" , action.context.env.DISTR_HOSTLOGIN , VarOSTYPE.LINUX );
			}
			else {
				if( !action.isLocalRun() ) {
					MetaProductSettings product = meta.getProductSettings( action );
					account = Account.getAccount( action , "" , product.CONFIG_DISTR_HOSTLOGIN , VarOSTYPE.LINUX );
				}
			}
		}
		else {
			if( distPath.isEmpty() ) {
				ServerContext sc = action.getServerContext();
				distPath = sc.DIST_ROOT;
				distPath = Common.getPath( distPath , meta.name );
			}
			
			if( distPath.isEmpty() )
				action.exit0( _Error.DistPathNotDefined0 , "DISTPATH is not defined in server configuration" );
		}
		
		RemoteFolder folder = new RemoteFolder( account , distPath );
		return( folder );
	}
	
	private DistLabelInfo getLabelInfo( ActionBase action , String RELEASELABEL ) throws Exception {
		DistLabelInfo info = new DistLabelInfo( this );
		info.createLabelInfo( action , RELEASELABEL );
		return( info );
	}
	
	public String getReleaseVerByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		DistLabelInfo info = getLabelInfo( action , RELEASELABEL );
		return( info.RELEASEVER );
	}
	
	public void createProd( ActionBase action , String RELEASEVER ) throws Exception {
		DistLabelInfo info = getLabelInfo( action , "prod" );
		String PRODPATH = info.RELEASEPATH;
		
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
