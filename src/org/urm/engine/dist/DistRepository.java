package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.data.EngineContext;
import org.urm.engine.products.EngineProductReleases;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.shell.Account;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class DistRepository {

	static String RELEASEREPOSITORYFILE = "releases.xml";

	static String REPO_FOLDER_DATA = "data";
	static String REPO_FOLDER_DATA_DUMP = "dump";
	static String REPO_FOLDER_DATA_DUMPNEW = "dump-new";
	static String REPO_FOLDER_DATA_DUMPBACKUP = "dump-backup";
	static String REPO_FOLDER_RELEASES_MASTER = "master";
	static String REPO_FOLDER_RELEASES_NORMAL = "releases";
	static String REPO_FOLDER_RELEASES_ARCHIVE = "archive";
	
	public EngineProductReleases releases;
	
	private RemoteFolder repoFolder;
	private Map<String,DistRepositoryItem> normalMap; 
	private Map<String,DistRepositoryItem> masterMap; 
	
	private boolean modifyState;
	
	private DistRepository( EngineProductReleases releases ) {
		this.releases = releases;
		normalMap = new HashMap<String,DistRepositoryItem>();
		masterMap = new HashMap<String,DistRepositoryItem>();
		modifyState = false;
	}
	
	public DistRepository copy( EngineProductReleases rreleases ) throws Exception {
		DistRepository r = new DistRepository( rreleases );
		r.repoFolder = repoFolder;
		for( DistRepositoryItem item : normalMap.values() ) {
			ReleaseDist rreleaseDist = rreleases.findReleaseDist( item.dist );
			DistRepositoryItem ritem = item.copy( r , rreleaseDist );
			r.addNormalItem( ritem );
		}
		for( DistRepositoryItem item : masterMap.values() ) {
			ReleaseDist rreleaseDist = rreleases.findReleaseDist( item.dist );
			DistRepositoryItem ritem = item.copy( r , rreleaseDist );
			r.addMasterItem( ritem );
		}
		return( r );
	}
	
	public synchronized void modify( boolean done ) throws Exception {
		if( !done ) {
			if( modifyState )
				Common.exitUnexpected();
			modifyState = true;
		}
		else {
			if( !modifyState )
				Common.exitUnexpected();
			modifyState = false;
		}
	}
	
	public static DistRepository loadDistRepository( ActionBase action , EngineProductReleases releases , boolean importxml ) throws Exception {
		DistRepository repo = new DistRepository( releases );
		repo.open( action , importxml );
		return( repo );
	}

	public static DistRepository createInitialRepository( ActionBase action , EngineProductReleases releases , boolean forceClear ) throws Exception {
		DistRepository repo = new DistRepository( releases );
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
		
		// read repository
		RemoteFolder normalFolder = repoFolder.getSubFolder( action , REPO_FOLDER_RELEASES_NORMAL );
		String[] folders = normalFolder.getTopDirs( action );
		
		for( String folder : folders ) {
			try {
				VersionInfo versionInfo = VersionInfo.getReleaseDirInfo( folder );
				Release release = releases.findRelease( versionInfo.getFullVersion() );
				if( release == null )
					continue;
				
				ReleaseDist releaseDist = release.findDistVariant( versionInfo.variant );
				DistRepositoryItem item = new DistRepositoryItem( this );
				ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( action , release.getMeta() , folder );
				item.createItem( action , info );
				item.read( action , normalFolder.getSubFolder( action , folder ) , releaseDist );
				addNormalItem( item );
			}
			catch( Throwable e ) {
				action.log( "unable to read release" , e );
			}
		}
		
		// master if any
		Release release = releases.findDefaultMaster();
		if( release == null )
			return;
		
		RemoteFolder masterFolder = repoFolder.getSubFolder( action , REPO_FOLDER_RELEASES_MASTER );
		if( !repoFolder.checkExists( action ) ) {
			action.error( "missing master release folder at " + masterFolder.getLocalPath( action ) );
			return;
		}
			
		ReleaseDist releaseDist = release.getDefaultReleaseDist();
		if( releaseDist == null )
			Common.exitUnexpected();
		
		DistRepositoryItem item = new DistRepositoryItem( this );
		ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( action , release.getMeta() , ReleaseLabelInfo.LABEL_MASTER );
		item.createItem( action , info );
		Dist dist = item.read( action , masterFolder , releaseDist );
		if( dist != null )
			addMasterItem( item );
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
		return( repoFolder.getSubFolder( action , REPO_FOLDER_DATA + "/" + dataSet ) );
	}
	
	public RemoteFolder getDataFolder( ActionBase action , String dataSet ) throws Exception {
		RemoteFolder folder = getDataSetFolder( action , dataSet );
		return( folder.getSubFolder( action , REPO_FOLDER_DATA_DUMP ) );
	}
	
	public RemoteFolder getDataNewFolder( ActionBase action , String dataSet ) throws Exception {
		RemoteFolder folder = getDataSetFolder( action , dataSet );
		return( folder.getSubFolder( action , REPO_FOLDER_DATA_DUMPNEW ) );
	}
	
	public RemoteFolder getDataBackupFolder( ActionBase action , String dataSet ) throws Exception {
		RemoteFolder folder = getDataSetFolder( action , dataSet );
		return( folder.getSubFolder( action , REPO_FOLDER_DATA_DUMPBACKUP ) );
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
	
	public synchronized Dist getDistByLabel( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , meta , RELEASELABEL );
		Dist dist = findDist( info );
		if( dist == null )
			Common.exitUnexpected();
		
		return( dist );
	}
	
	private DistRepositoryItem createDistItem( ActionBase action , ReleaseLabelInfo info , Dist dist ) throws Exception {
		DistRepositoryItem item = new DistRepositoryItem( this );
		item.createItem( action , info );
		if( dist != null )
			item.setDist( dist );
		if( dist.isMaster() )
			addMasterItem( item );
		else
			addNormalItem( item );
		return( item );
	}

	public DistRepositoryItem createRepositoryMasterItem( EngineMethod method , ActionBase action , Meta meta ) throws Exception {
		ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( action , meta , ReleaseLabelInfo.LABEL_MASTER );
		return( createRepositoryItem( method , action , info ) );
	}
	
	public DistRepositoryItem createRepositoryItem( EngineMethod method , ActionBase action , ReleaseLabelInfo info ) throws Exception {
		DistRepositoryItem item = new DistRepositoryItem( this );
		item.createItem( action , info );
		item.createItemFolder( action );
		
		method.createDistItem( this , item );
		if( info.master )
			addMasterItem( item );
		else
			addNormalItem( item );
		return( item );
	}

	public DistRepositoryItem attachRepositoryItem( EngineMethod method , ActionBase action , ReleaseLabelInfo info , ReleaseDist releaseDist ) throws Exception {
		DistRepositoryItem item = new DistRepositoryItem( this );
		item.createItem( action , info );
		RemoteFolder distFolder = getDistFolder( action , item );
		Dist dist = item.read( action , distFolder , releaseDist );
		if( dist == null ) {
			String path = distFolder.getLocalPath( method.action );
			Common.exit1( _Error.MissingDistributive1 , "missing distributive at " + path , path );
		}
		
		method.createDistItem( this , item );
		if( releaseDist.release.isMaster() )
			addMasterItem( item );
		else
			addNormalItem( item );
		return( item );
	}

	public synchronized void dropDist( EngineMethod method , ActionBase action , DistRepositoryItem item , boolean force ) throws Exception {
		method.checkUpdateDistItem( item );
		
		Dist dist = item.dist;
		if( dist == null ) {
			action.error( "distributive is missing, ignored" );
			return;
		}
		
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
				EngineContext sc = action.getEngineContext();
				distPath = sc.DIST_ROOT;
				distPath = Common.getPath( distPath , releases.ep.productName );
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
	
	public ReleaseLabelInfo getLabelInfo( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( action , meta , RELEASELABEL );
		if( info.local )
			info.setRepositoryPath( "." );
		else
		if( info.master )
			info.setRepositoryPath( REPO_FOLDER_RELEASES_MASTER );
		else
		if( info.archived )
			info.setRepositoryPath( REPO_FOLDER_RELEASES_ARCHIVE + "/" + info.RELEASEDIR );
		else
			info.setRepositoryPath( REPO_FOLDER_RELEASES_NORMAL + "/" + info.RELEASEDIR );
		return( info );
	}
	
	public String getReleaseVerByLabel( ActionBase action , Meta meta , String RELEASELABEL ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , meta , RELEASELABEL );
		return( info.RELEASEVER );
	}
	
	public synchronized Dist createMasterInitial( ActionBase action , Release release , ReleaseDist releaseDist ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , release.getMeta() , ReleaseLabelInfo.LABEL_MASTER );
		
		DistRepositoryItem item = new DistRepositoryItem( this );
		item.createItem( action , info );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.DISTPATH );
		Dist dist = item.createDistMaster( action , distFolder , releaseDist );
		createDistItem( action , info , dist );
		return( dist );
	}

	public synchronized Dist createMasterCopy( EngineMethod method , ActionBase action , Dist src , Release release , ReleaseDist releaseDist ) throws Exception {
		if( !src.isCompleted() )
			action.exit1( _Error.NotCompletedSource1 , "Unable to use incomplete source release " + src.RELEASEDIR , src.RELEASEDIR );

		Meta meta = release.getMeta();
		ReleaseLabelInfo info = getLabelInfo( action , meta , ReleaseLabelInfo.LABEL_MASTER );
		DistRepositoryItem item = createRepositoryMasterItem( method , action , meta );
		item.createItem( action , info );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , info.DISTPATH );
		Dist dist = item.createDistMaster( action , distFolder , releaseDist );
		createDistItem( action , info , dist );
		
		return( dist );
	}

	public DistRepositoryItem findNormalItem( String RELEASEDIR ) {
		return( normalMap.get( RELEASEDIR ) );
	}
	
	public DistRepositoryItem findMasterItem( String NAME ) {
		return( masterMap.get( NAME ) );
	}
	
	public DistRepositoryItem findItem( Dist dist ) {
		if( dist.isMaster() )
			return( findMasterItem( dist.release.NAME ) );
		return( findNormalItem( dist.RELEASEDIR ) );
	}
	
	public Dist findDist( ReleaseLabelInfo info ) {
		return( findDist( info.RELEASEDIR ) );
	}
	
	public synchronized Dist findDist( String releaseDir ) {
		DistRepositoryItem item = findNormalItem( releaseDir );
		if( item == null )
			return( null );
		return( item.dist );
	}

	public void addNormalItem( DistRepositoryItem item ) throws Exception {
		if( item.dist != null && item.dist.isMaster() )
			Common.exitUnexpected();
		
		normalMap.put( item.RELEASEDIR , item );
	}

	public void addMasterItem( DistRepositoryItem item ) {
		masterMap.put( item.dist.release.NAME , item );
	}

	public void replaceItem( DistRepositoryItem itemOld , DistRepositoryItem item ) throws Exception {
		removeItem( itemOld );
		if( item.dist.isMaster() )
			addMasterItem( item );
		else
			addNormalItem( item );
	}
	
	public synchronized void removeItem( DistRepositoryItem item ) {
		if( item.dist.isMaster() )
			masterMap.remove( item.dist.release.NAME );
		else
			normalMap.remove( item.dist.RELEASEDIR );
	}

	public synchronized DistRepositoryItem[] getNormalItems() {
		int count = normalMap.size();
		DistRepositoryItem[] items = new DistRepositoryItem[ count ];
		int k = 0;
		for( String key : Common.getSortedKeys( normalMap ) )
			items[ k++ ] = normalMap.get( key );
		return( items );
	}
	
	public synchronized void archiveDist( ActionBase action , Dist dist ) throws Exception {
		DistRepositoryItem item = findItem( dist );
		if( item == null )
			Common.exitUnexpected();
		
		String folderOld = getNormalReleaseFolder( dist.RELEASEDIR );
		String folderNew = getArchivedReleaseFolder( dist.RELEASEDIR );
		String folderArchive = getArchiveFolder();
		repoFolder.ensureFolderExists( action , folderArchive );
		repoFolder.moveFolderToFolder( action , folderOld , folderNew );
		removeItem( item );
	}

	public synchronized Dist reloadDist( ActionBase action , DistRepositoryItem item ) throws Exception {
		RemoteFolder distFolder = getDistFolder( action , item );
		item.read( action , distFolder , item.dist.releaseDist );
		return( item.dist );
	}

	public synchronized String[] getActiveVersions() {
		return( VersionInfo.orderVersions( Common.getSortedKeys( normalMap ) ) );
	}
	
	public synchronized Dist getNextDist( ActionBase action , VersionInfo info ) throws Exception {
		String[] versions = getActiveVersions();
		String[] ordered = VersionInfo.orderVersions( versions );
		
		String name = info.getReleaseName();
		for( int k = 0; k < ordered.length; k++ ) {
			if( name.equals( ordered[k] ) ) {
				if( k >= ordered.length - 1 )
					break;
				DistRepositoryItem item = normalMap.get( ordered[k+1] );
				return( item.dist );
			}
		}
		return( null );
	}

	public Dist copyDist( ActionBase action , Dist dist , String newName , ReleaseDist newReleaseDist ) throws Exception {
		ReleaseLabelInfo info = getLabelInfo( action , newReleaseDist.release.getMeta() , newReleaseDist.getReleaseDir() );
		DistRepositoryItem newItem = new DistRepositoryItem( this );
		newItem.createItem( action , info );
		return( dist.copyDist( action , newName , newItem , newReleaseDist ) );
	}
	
	public void replaceDist( ActionBase action , Dist dist , Dist distNew ) throws Exception {
		DistRepositoryItem item = findItem( dist );
		if( item == null )
			Common.exitUnexpected();
		
		removeItem( item );
		String releasedir = dist.RELEASEDIR;
		
		dist.moveDist( action , dist.RELEASEDIR + "-old" );
		distNew.moveDist( action , releasedir );
		item.setDist( distNew );

		if( dist.isMaster() )
			addMasterItem( item );
		else
			addNormalItem( item );
	}

	public Dist findDefaultMasterDist() {
		DistRepositoryItem item = findMasterItem( ReleaseRepository.MASTER_NAME_PRIMARY );
		if( item == null )
			return( null );
		return( item.dist );
	}

	public Dist createDistNormal( EngineMethod method , ActionBase action , DistRepositoryItem item , ReleaseDist releaseDist ) throws Exception {
		method.checkUpdateDistItem( item );
		
		RemoteFolder distFolder = getDistFolder( action , item );
		Dist dist = new Dist( releaseDist.release.getMeta() , item , releaseDist , distFolder );
		dist.createNormal( action );
		item.setDist( dist );
		return( dist );
	}

	public Dist findDefaultDist( Release release ) {
		ReleaseDist dist = release.getDefaultReleaseDist();
		return( findDist( dist.getReleaseDir() ) );
	}
	
	public DistRepositoryItem findDefaultItem( Release release ) {
		ReleaseDist dist = release.getDefaultReleaseDist();
		if( release.isMaster() )
			return( findMasterItem( release.NAME ) );
		return( findNormalItem( dist.getReleaseDir() ) );
	}
	
	public static String getNormalReleaseFolder( String RELEASEDIR ) throws Exception {
		return( REPO_FOLDER_RELEASES_NORMAL + "/" + RELEASEDIR );
	}
	
	public static String getArchivedReleaseFolder( String RELEASEDIR ) {
		return( REPO_FOLDER_RELEASES_ARCHIVE + "/" + RELEASEDIR );
	}
	
	public static String getArchiveFolder() {
		return( REPO_FOLDER_RELEASES_ARCHIVE );
	}
	
}
