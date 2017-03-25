package org.urm.engine.dist;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.shell.Account;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.ServerContext;
import org.urm.meta.engine.ServerReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaProductSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DistRepository {

	public enum DistOperation {
		CREATE ,
		DROP ,
		FINISH ,
		REOPEN ,
		COMPLETE ,
		PHASE ,
		MODIFY ,
		BUILD ,
		PUT ,
		ARCHIVE ,
		STATUS
	};
	
	Meta meta;
	RemoteFolder repoFolder;

	public Map<String,Dist> distMap; 
	public Map<String,DistRepositoryItem> runMap; 
	
	static String RELEASEREPOSITORYFILE = "releases.xml";
	static String RELEASEHISTORYFILE = "history.txt";
	
	private DistRepository( Meta meta ) {
		this.meta = meta;
		distMap = new HashMap<String,Dist>();
		runMap = new HashMap<String,DistRepositoryItem>();
	}
	
	public static DistRepository loadDistRepository( ActionBase action , Meta meta ) throws Exception {
		DistRepository repo = new DistRepository( meta );
		repo.open( action );
		return( repo );
	}

	public static DistRepository createInitialRepository( ActionBase action , Meta meta , boolean forceClear ) throws Exception {
		DistRepository repo = new DistRepository( meta );
		repo.create( action , forceClear );
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
	
	private void create( ActionBase action , boolean forceClear ) throws Exception {
		repoFolder = getDistFolder( action );
		if( repoFolder.checkExists( action ) ) {
			if( forceClear ) {
				action.error( "remove existing distributive repository at " + repoFolder.getLocalPath( action ) + " ..." );
				repoFolder.removeThis( action );
			}
			else {
				String path = repoFolder.getLocalPath( action );
				action.exit1( _Error.ReleaseRepositoryExists1 , "unable to create release repository, already exists at " + path , path );
			}
		}
		
		RemoteFolder parent = repoFolder.getParentFolder( action );
		if( !parent.checkExists( action ) ) {
			String path = parent.getLocalPath( action );
			action.exit1( _Error.MissingReleaseRepositoryParent1 , "unable to create release repository, missing parent path=" + path , path );
		}
			
		repoFolder.recreateThis( action );
		readRepositoryFile( action );
	}

	private synchronized void readRepositoryFile( ActionBase action ) throws Exception {
		distMap.clear();
		runMap.clear();
		
		if( !repoFolder.checkFileExists( action , RELEASEREPOSITORYFILE ) ) {
			createInitialRepositoryFile( action );
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
			DistLabelInfo info = getLabelInfo( action , item.RELEASEDIR );
			RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
			item.read( action , distFolder );
			addRunItem( item );
			addDist( item.dist );
		}
	}

	private void saveRepositoryFile( ActionBase action ) throws Exception {
		Document doc = createRepositoryFile( action );
		saveRepositoryFile( action , doc );
	}

	private Document createRepositoryFile( ActionBase action ) throws Exception {
		Document doc = createEmptyRepositoryFile( action );
		Element root = doc.getDocumentElement();
		for( String itemKey : Common.getSortedKeys( runMap ) ) {
			DistRepositoryItem item = runMap.get( itemKey );
			Element distElement = Common.xmlCreateElement( doc , root , "release" );
			item.save( action , doc , distElement );
		}
		return( doc );
	}
	
	private void createInitialRepositoryFile( ActionBase action ) throws Exception {
		Document doc = createEmptyRepositoryFile( action );
		saveRepositoryFile( action , doc );
	}

	private Document createEmptyRepositoryFile( ActionBase action ) throws Exception {
		Document doc = Common.xmlCreateDoc( "repository" );
		Element root = doc.getDocumentElement();
		Common.xmlSetElementAttr( doc , root , "created" , Common.getNameTimeStamp() );
		return( doc );
	}

	private void saveRepositoryFile( ActionBase action , Document doc ) throws Exception {
		String tmpFile = action.getWorkFilePath( RELEASEREPOSITORYFILE );
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
	
	public synchronized Dist getDistByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		DistLabelInfo info = getLabelInfo( action , RELEASELABEL );
		Dist dist = findDist( action , info );
		if( dist != null )
			return( dist );

		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
		dist = DistRepositoryItem.read( action , this , distFolder );
		addDist( dist );
		
		return( dist );
	}

	public synchronized Dist createDist( ActionBase action , String RELEASELABEL , Date releaseDate , ServerReleaseLifecycle lc ) throws Exception {
		DistLabelInfo info = getLabelInfo( action , RELEASELABEL );
		Dist dist = findDist( action , info );
		if( dist != null ) {
			String path = dist.getDistPath( action );
			action.exit( _Error.ReleaseAlreadyExists1 , "release already exists at " + path , new String[] { path } );
		}
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
		dist = DistRepositoryItem.createDist( action , this , distFolder , releaseDate , lc );
		addDist( dist );
		
		return( dist );
	}

	public synchronized void dropDist( ActionBase action , Dist dist , boolean force ) throws Exception {
		if( force )
			dist.forceDrop( action );
		else
			dist.dropRelease( action );
		removeDist( dist );
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
					account = Account.getDatacenterAccount( action , "" , action.context.env.DISTR_HOSTLOGIN , VarOSTYPE.LINUX );
			}
			else {
				if( !action.isLocalRun() ) {
					MetaProductSettings product = meta.getProductSettings( action );
					account = Account.getDatacenterAccount( action , "" , product.CONFIG_DISTR_HOSTLOGIN , VarOSTYPE.LINUX );
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
	
	public DistLabelInfo getLabelInfo( ActionBase action , String RELEASELABEL ) throws Exception {
		DistLabelInfo info = new DistLabelInfo( this );
		info.createLabelInfo( action , RELEASELABEL );
		return( info );
	}
	
	public String getReleaseVerByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		DistLabelInfo info = getLabelInfo( action , RELEASELABEL );
		return( info.RELEASEVER );
	}
	
	public synchronized Dist createProdInitial( ActionBase action , String RELEASEVER ) throws Exception {
		DistLabelInfo info = getLabelInfo( action , Dist.MASTER_LABEL );
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
		Dist dist = DistRepositoryItem.createProdDist( action , this , distFolder , RELEASEVER );
		addDist( dist );
		return( dist );
	}

	public synchronized Dist createProdCopy( ActionBase action , String RELEASEDIR ) throws Exception {
		DistLabelInfo info = getLabelInfo( action , Dist.MASTER_LABEL );
		Dist src = this.getDistByLabel( action , RELEASEDIR );
		if( !src.isCompleted() )
			action.exit1( _Error.NotCompletedSource1 , "Unable to use incomplete source release " + src.RELEASEDIR , src.RELEASEDIR );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
		Dist dist = DistRepositoryItem.createProdDist( action , this , distFolder , src.release.RELEASEVER );
		addDist( dist );
		dist.createMasterFiles( action , src );
		dist.finish( action );
		return( dist );
	}

	public DistRepositoryItem findRunItem( ActionBase action , Dist dist ) throws Exception {
		return( runMap.get( dist.RELEASEDIR ) );
	}
	
	private Dist findDist( ActionBase action , DistLabelInfo info ) throws Exception {
		return( findDist( info.RELEASEDIR ) );
	}
	
	private synchronized Dist findDist( String releaseDir ) {
		return( distMap.get( releaseDir ) );
	}

	private synchronized void addDist( Dist dist ) {
		distMap.put( dist.RELEASEDIR , dist );
	}

	private synchronized void removeDist( Dist dist ) {
		distMap.remove( dist.RELEASEDIR );
	}

	private synchronized void addRunItem( DistRepositoryItem item ) {
		runMap.put( item.dist.RELEASEDIR , item );
	}
	
	private synchronized DistRepositoryItem findRunItem( String releaseDir ) {
		return( runMap.get( releaseDir ) );
	}

	private synchronized void removeRunItem( DistRepositoryItem item ) {
		runMap.remove( item.dist.RELEASEDIR );
	}

	public synchronized DistRepositoryItem addDistAction( ActionBase action , boolean success , Dist dist , DistOperation op , String msg ) throws Exception {
		DistRepositoryItem item = null;
		boolean save = false;
		if( op == DistOperation.CREATE ) {
			if( success == false )
				return( null );
			
			item = new DistRepositoryItem( this );
			item.createItem( action , dist );
			addRunItem( item );
			save = true;
		}
		else {
			item = findRunItem( dist.RELEASEDIR );
			if( item == null )
				return( null );
		}
		
		item.addAction( action , success , op , msg );
		
		if( op == DistOperation.DROP ) {
			if( success ) {
				removeRunItem( item );
				save = true;
			}
		}
		else
		if( op == DistOperation.ARCHIVE ) {
			if( success ) {
				save = true;
				removeRunItem( item );
				item.archiveItem( action );
			}
		}
		
		if( save )
			saveRepositoryFile( action );
		return( item );
	}

	public synchronized DistRepositoryItem[] getRunItems() {
		int count = runMap.size();
		DistRepositoryItem[] items = new DistRepositoryItem[ count ];
		int k = 0;
		for( String key : Common.getSortedKeys( runMap ) )
			items[ k++ ] = runMap.get( key );
		return( items );
	}
	
	public synchronized void archiveDist( ActionBase action , Dist dist ) throws Exception {
		String folderOld = DistLabelInfo.getReleaseFolder( action , dist );
		String folderNew = DistLabelInfo.getArchivedReleaseFolder( action , dist );
		String folderArchive = DistLabelInfo.getArchiveFolder( action );
		repoFolder.ensureFolderExists( action , folderArchive );
		repoFolder.moveFolderToFolder( action , folderOld , folderNew );
	}

	public synchronized Dist reloadDist( ActionBase action , String RELEASELABEL ) throws Exception {
		DistLabelInfo info = getLabelInfo( action , RELEASELABEL );
		
		DistRepositoryItem item = findRunItem( info.RELEASEDIR );
		Dist dist = findDist( info.RELEASEDIR );
		if( dist != null )
			removeDist( dist );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
		item.read( action , distFolder );
		addDist( item.dist );
		return( item.dist );
	}

	public synchronized Dist getNextDist( ActionBase action , VersionInfo info ) throws Exception {
		String[] versions = distMap.keySet().toArray( new String[0] );
		String[] ordered = VersionInfo.orderVersions( versions );
		
		String name = info.getReleaseName();
		for( int k = 0; k < ordered.length; k++ ) {
			if( name.equals( ordered[k] ) ) {
				if( k >= ordered.length - 1 )
					break;
				return( distMap.get( ordered[k+1] ) );
			}
		}
		return( null );
	}

	public Dist copyDist( ActionBase action , Dist dist , String newName ) throws Exception {
		return( dist.copyDist( action , newName ) );
	}
	
	public void replaceDist( ActionBase action , Dist dist , Dist distNew ) throws Exception {
		removeDist( dist );
		String releasedir = dist.RELEASEDIR;
		DistRepositoryItem item = findRunItem( releasedir );
		
		dist.moveDist( action , dist.RELEASEDIR + "-old" );
		distNew.moveDist( action , releasedir );
		item.createItem( action , distNew );
		
		addDist( distNew );
	}
	
}
