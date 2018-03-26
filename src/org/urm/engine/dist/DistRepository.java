package org.urm.engine.dist;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.data.EngineContext;
import org.urm.engine.shell.Account;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;

public class DistRepository {

	static String RELEASEREPOSITORYFILE = "releases.xml";
	static String RELEASEHISTORYFILE = "history.txt";
	
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
	
	public Meta meta;
	RemoteFolder repoFolder;

	private Map<String,DistRepositoryItem> itemMap; 
	private Map<String,DistRepositoryItem> runMap; 
	
	private DistRepository( Meta meta ) {
		this.meta = meta;
		itemMap = new HashMap<String,DistRepositoryItem>();
		runMap = new HashMap<String,DistRepositoryItem>();
	}
	
	public static DistRepository loadDistRepository( ActionBase action , Meta meta , boolean importxml ) throws Exception {
		DistRepository repo = new DistRepository( meta );
		repo.open( action , importxml );
		return( repo );
	}

	public static DistRepository createInitialRepository( ActionBase action , Meta meta , boolean forceClear ) throws Exception {
		DistRepository repo = new DistRepository( meta );
		repo.create( action , forceClear );
		return( repo );
	}

	private void open( ActionBase action , boolean importxml ) throws Exception {
		repoFolder = getDistFolder( action );
		
		if( !repoFolder.checkExists( action ) ) {
			if( importxml ) {
				create( action , false );
				return;
			}
			
			String path = repoFolder.getLocalPath( action );
			action.exit1( _Error.MissingReleaseRepository1 , "missing release repository at " + path , path );
		}
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
				action.debug( "found existing release repository, path=" + path );
			}
		}
		
		RemoteFolder parent = repoFolder.getParentFolder( action );
		if( !parent.checkExists( action ) ) {
			String path = parent.getLocalPath( action );
			action.exit1( _Error.MissingReleaseRepositoryParent1 , "unable to create release repository, missing parent path=" + path , path );
		}
			
		repoFolder.ensureExists( action );
		action.info( "distributive repository has been created, path=" + action.getLocalPath( repoFolder.folderPath ) );
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
		String location = server.sg.env.NAME + "-" + server.sg.NAME + "-" + server.NAME;
		return( repoFolder.getSubFolder( action , "data/" + dataSet + "/log-import-" + location ) );
	}
	
	public synchronized Dist getDistByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , RELEASELABEL );
		Dist dist = findDist( info );
		if( dist == null )
			Common.exitUnexpected();
		
		return( dist );
	}
	
	private DistRepositoryItem createDistItem( ActionBase action , ReleaseLabelInfo info , Dist dist ) throws Exception {
		DistRepositoryItem item = new DistRepositoryItem( this );
		item.createItem( action , info.RELEASEDIR , info.RELEASEPATH );
		if( dist != null )
			item.setDist( dist );
		addItem( item );
		return( item );
	}

	public DistRepositoryItem createRepositoryItem( ActionBase action , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , RELEASELABEL );
		DistRepositoryItem item = new DistRepositoryItem( this );
		item.createItem( action , info.RELEASEDIR , info.RELEASEPATH );
		item.createItemFolder( action );
		return( item );
	}

	public synchronized void dropDist( ActionBase action , Dist dist , boolean force ) throws Exception {
		DistRepositoryItem item = findRunItem( dist );
		if( item == null )
			Common.exitUnexpected();
			
		if( force )
			dist.forceDrop( action );
		else
			dist.dropRelease( action );
		removeItem( item );
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
				if( !action.isLocalRun() ) {
					MetaEnv env = action.context.env;
					HostAccount hostAccount = env.getDistrAccount();
					if( hostAccount != null )
						account = hostAccount.getAccount();
				}
			}
		}
		else {
			if( distPath.isEmpty() ) {
				EngineContext sc = action.getServerContext();
				distPath = sc.DIST_ROOT;
				distPath = Common.getPath( distPath , meta.name );
			}
			
			if( distPath.isEmpty() )
				action.exit0( _Error.DistPathNotDefined0 , "DISTPATH is not defined in server configuration" );
		}
		
		RemoteFolder folder = new RemoteFolder( account , distPath );
		return( folder );
	}
	
	public RemoteFolder getDistFolder( ActionBase action , DistRepositoryItem item ) throws Exception {
		RemoteFolder repoFolder = getDistFolder( action );
		RemoteFolder distFolder = repoFolder.getSubFolder( action , item.DISTPATH );
		return( distFolder );
	}
	
	public ReleaseLabelInfo getLabelInfo( ActionBase action , String RELEASELABEL ) throws Exception {
		return( ReleaseLabelInfo.getLabelInfo( action , meta , RELEASELABEL ) );
	}
	
	public String getReleaseVerByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , RELEASELABEL );
		return( info.RELEASEVER );
	}
	
	public synchronized Dist createMasterInitial( ActionBase action , String RELEASEVER , ReleaseDist releaseDist ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , ReleaseLabelInfo.LABEL_MASTER );
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
		DistRepositoryItem item = new DistRepositoryItem( this );
		item.createItem( action , RELEASEVER , info.RELEASEPATH );
		Dist dist = item.createDistMaster( action , distFolder , releaseDist );
		createDistItem( action , info , dist );
		return( dist );
	}

	public synchronized Dist createMasterCopy( ActionBase action , String RELEASEDIR , ReleaseDist releaseDist ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , ReleaseLabelInfo.LABEL_MASTER );
		Dist src = this.getDistByLabel( action , RELEASEDIR );
		if( !src.isCompleted() )
			action.exit1( _Error.NotCompletedSource1 , "Unable to use incomplete source release " + src.RELEASEDIR , src.RELEASEDIR );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
		DistRepositoryItem item = new DistRepositoryItem( this );
		item.createItem( action , RELEASEDIR , info.RELEASEPATH );
		Dist dist = item.createDistMaster( action , distFolder , releaseDist );
		createDistItem( action , info , dist );
		dist.createMasterFiles( action , src );
		dist.finish( action );
		
		return( dist );
	}

	public DistRepositoryItem findItem( String RELEASEDIR ) {
		return( itemMap.get( RELEASEDIR ) );
	}
	
	public DistRepositoryItem findRunItem( Dist dist ) {
		return( runMap.get( dist.RELEASEDIR ) );
	}
	
	public Dist findDist( ReleaseLabelInfo info ) {
		return( findDist( info.RELEASEDIR ) );
	}
	
	public synchronized Dist findDist( String releaseDir ) {
		DistRepositoryItem item = runMap.get( releaseDir );
		if( item == null )
			return( null );
		return( item.dist );
	}

	public synchronized void addItem( DistRepositoryItem item ) {
		itemMap.put( item.RELEASEDIR , item );
		if( item.dist != null )
			runMap.put( item.RELEASEDIR , item );
	}
	
	private synchronized DistRepositoryItem findRunItem( String releaseDir ) {
		return( runMap.get( releaseDir ) );
	}

	public synchronized void removeItem( DistRepositoryItem item ) {
		itemMap.remove( item.RELEASEDIR );
		runMap.remove( item.RELEASEDIR );
	}

	public synchronized DistRepositoryItem addDistAction( ActionBase action , boolean success , Dist dist , DistOperation op , String msg ) throws Exception {
		DistRepositoryItem item = null;
		if( op == DistOperation.CREATE ) {
			if( success == false )
				return( null );
			
			item = findRunItem( dist.RELEASEDIR );
		}
		else {
			item = findRunItem( dist.RELEASEDIR );
			if( item == null )
				return( null );
		}
		
		item.addAction( action , success , op , msg );
		
		if( op == DistOperation.DROP ) {
			if( success )
				removeItem( item );
		}
		else
		if( op == DistOperation.ARCHIVE ) {
			if( success ) {
				removeItem( item );
				item.archiveItem( action );
			}
		}
		
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
		DistRepositoryItem item = findRunItem( dist );
		if( item == null )
			Common.exitUnexpected();
		
		String folderOld = ReleaseLabelInfo.getReleaseFolder( dist.RELEASEDIR );
		String folderNew = ReleaseLabelInfo.getArchivedReleaseFolder( dist.RELEASEDIR );
		String folderArchive = ReleaseLabelInfo.getArchiveFolder();
		repoFolder.ensureFolderExists( action , folderArchive );
		repoFolder.moveFolderToFolder( action , folderOld , folderNew );
		removeItem( item );
	}

	public synchronized Dist reloadDist( ActionBase action , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , RELEASELABEL );
		
		DistRepositoryItem item = findRunItem( info.RELEASEDIR );
		if( item == null )
			return( null );
			
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.RELEASEPATH );
		item.readDist( action , item , distFolder , item.dist.releaseDist );
		return( item.dist );
	}

	public synchronized String[] getActiveVersions() {
		List<String> list = new LinkedList<String>();
		for( String releasedir : runMap.keySet() ) {
			if( releasedir.equals( Dist.MASTER_DIR ) )
				continue;
			list.add( releasedir );
		}
		return( list.toArray( new String[0] ) );
	}
	
	public synchronized Dist getNextDist( ActionBase action , VersionInfo info ) throws Exception {
		String[] versions = getActiveVersions();
		String[] ordered = VersionInfo.orderVersions( versions );
		
		String name = info.getReleaseName();
		for( int k = 0; k < ordered.length; k++ ) {
			if( name.equals( ordered[k] ) ) {
				if( k >= ordered.length - 1 )
					break;
				DistRepositoryItem item = itemMap.get( ordered[k+1] );
				return( item.dist );
			}
		}
		return( null );
	}

	public Dist copyDist( ActionBase action , Dist dist , String newName , ReleaseDist newReleaseDist ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , newReleaseDist.getReleaseDir() );
		DistRepositoryItem newItem = new DistRepositoryItem( this );
		newItem.createItem( action , info.RELEASEDIR , info.RELEASEPATH );
		return( dist.copyDist( action , newName , newItem , newReleaseDist ) );
	}
	
	public void replaceDist( ActionBase action , Dist dist , Dist distNew ) throws Exception {
		DistRepositoryItem item = findRunItem( dist );
		if( item == null )
			Common.exitUnexpected();
		
		removeItem( item );
		String releasedir = dist.RELEASEDIR;
		
		dist.moveDist( action , dist.RELEASEDIR + "-old" );
		distNew.moveDist( action , releasedir );
		item.setDist( distNew );
		
		addItem( item );
	}

	public Dist findMasterDist() {
		DistRepositoryItem item = findItem( Dist.MASTER_DIR );
		if( item == null )
			return( null );
		return( item.dist );
	}

	public Dist createDistNormal( ActionBase action , DistRepositoryItem item , ReleaseDist releaseDist ) throws Exception {
		RemoteFolder distFolder = getDistFolder( action , item );
		Dist dist = new Dist( meta , item , releaseDist , distFolder );
		dist.createNormal( action );
		item.setDist( dist );
		return( dist );
	}

	public Dist findDefaultDist( Release release ) {
		ReleaseDist dist = release.getDefaultReleaseDist();
		return( findDist( dist.getReleaseDir() ) );
	}
	
}
