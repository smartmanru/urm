package org.urm.meta.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.PropertySet;
import org.urm.engine.ServerTransaction;
import org.urm.engine.action.ActionInit;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.engine.vcs.GenericVCS;
import org.urm.engine.vcs.MirrorStorage;
import org.urm.meta.ServerObject;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMirrorRepository extends ServerObject {

	ServerMirrors mirrors;
	
	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public String TYPE;
	public String PRODUCT;
	public String PROJECT;
	public String RESOURCE;
	public String RESOURCE_REPO;
	public String RESOURCE_ROOT;
	public String RESOURCE_DATA;
	public String BRANCH;
	
	public static String TYPE_SERVER = "server";
	public static String TYPE_PROJECT = "project";
	public static String TYPE_PRODUCT_META = "product.meta";
	public static String TYPE_PRODUCT_DATA = "product.data";

	public ServerMirrorRepository( ServerMirrors mirrors ) {
		super( mirrors );
		this.mirrors = mirrors;
		
		loaded = false;
		loadFailed = false;
	}

	public String getFolderName() {
		return( NAME );
	}
	
	public boolean isServer() {
		return( TYPE.equals( TYPE_SERVER ) );
	}
	
	public boolean isProject() {
		return( TYPE.equals( TYPE_PROJECT ) );
	}
	
	public boolean isProductMeta() {
		return( TYPE.equals( TYPE_PRODUCT_META ) );
	}
	
	public boolean isProductData() {
		return( TYPE.equals( TYPE_PRODUCT_DATA ) );
	}
	
	public ServerMirrorRepository copy( ServerMirrors mirror ) throws Exception {
		ServerMirrorRepository r = new ServerMirrorRepository( mirror );
		r.properties = properties.copy( null );
		r.scatterSystemProperties();
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		properties = new PropertySet( "mirror" , null );
		properties.loadFromNodeElements( root );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}

	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}

	private void scatterSystemProperties() throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		TYPE = properties.getSystemRequiredStringProperty( "type" );
		PRODUCT = properties.getSystemStringProperty( "product" );
		PROJECT = properties.getSystemStringProperty( "project" );
		RESOURCE = properties.getSystemStringProperty( "resource" );
		RESOURCE_REPO = properties.getSystemStringProperty( "repository" );
		RESOURCE_ROOT = properties.getSystemStringProperty( "rootpath" );
		RESOURCE_DATA = properties.getSystemStringProperty( "datapath" );
		BRANCH = properties.getSystemStringProperty( "branch" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "mirror" , null );
		properties.setOriginalStringProperty( "name" , NAME );
		properties.setOriginalStringProperty( "type" , TYPE );
		properties.setOriginalStringProperty( "product" , PRODUCT );
		properties.setOriginalStringProperty( "project" , PROJECT );
		properties.setOriginalStringProperty( "resource" , RESOURCE );
		properties.setOriginalStringProperty( "repository" , RESOURCE_REPO );
		properties.setOriginalStringProperty( "rootpath" , RESOURCE_ROOT );
		properties.setOriginalStringProperty( "datapath" , RESOURCE_DATA );
		properties.setOriginalStringProperty( "branch" , BRANCH );
	}

	public String getResource( ActionBase action ) throws Exception {
		if( RESOURCE.isEmpty() )
			action.exit1( _Error.MissingResourceName1 , "Missing resource source in mirror=" + NAME , NAME );
		return( RESOURCE );
	}
	
	public void createMirrorRepository( ServerTransaction transaction , String resource , String reponame , String reporoot , String dataroot , String repobranch , boolean push ) throws Exception {
		RESOURCE = resource;
		RESOURCE_REPO = reponame;
		RESOURCE_ROOT = ( reporoot.isEmpty() )? "/" : reporoot;
		RESOURCE_DATA = ( dataroot.isEmpty() )? "/" : dataroot;
		BRANCH = "";
		
		try {
			if( isServer() )
				createServerMirror( transaction , push );
			else
			if( isProductMeta() )
				createProductMetaMirror( transaction , push );
			else
			if( isProductData() )
				createProductDataMirror( transaction , push );
		}
		catch( Throwable e ) {
			RESOURCE = "";
			RESOURCE_REPO = "";
			RESOURCE_ROOT = "";
			RESOURCE_DATA = "";
			BRANCH = "";
			transaction.handle0( e , _Error.UnablePublishRepository0 , "Unable to create mirror repository" );
		}
		
		createProperties();
	}
	
	private void createServerMirror( ServerTransaction transaction , boolean push ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		createMirrorInternal( transaction , push );
	}

	private void createProductMetaMirror( ServerTransaction transaction , boolean push ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		createMirrorInternal( transaction , push );
	}

	private void createProductDataMirror( ServerTransaction transaction , boolean push ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		createMirrorInternal( transaction , push );
	}

	private Map<String,LocalFolder> getFolderMap( ActionInit action ) throws Exception {
		Map<String,LocalFolder> map = new HashMap<String,LocalFolder>();
		if( TYPE.equals( TYPE_SERVER ) ) {
			LocalFolder serverSettings = action.getServerSettingsFolder();
			map.put( "." , serverSettings );
		}
		else
		if( TYPE.equals( TYPE_PRODUCT_META ) ) {
			LocalFolder productSettings = action.getActiveProductHomeFolder( PRODUCT );
			map.put( "etc" , productSettings.getSubFolder( action , "etc" ) );
			map.put( "master" , productSettings.getSubFolder( action , "master" ) );
		}
		else
		if( TYPE.equals( TYPE_PRODUCT_DATA ) ) {
			Meta meta = action.getActiveProductMetadata( PRODUCT );
			MetaProductSettings settings = meta.getProductSettings( action );
			LocalFolder home = action.getServerHomeFolder();
			addFolderMapItem( action , map , SourceStorage.DATA_LIVE , home , settings.CONFIG_SOURCE_CFG_LIVEROOTDIR );
			addFolderMapItem( action , map , SourceStorage.DATA_TEMPLATES , home , settings.CONFIG_SOURCE_CFG_ROOTDIR );
			addFolderMapItem( action , map , SourceStorage.DATA_POSTREFRESH , home , settings.CONFIG_SOURCE_SQL_POSTREFRESH );
			addFolderMapItem( action , map , SourceStorage.DATA_CHANGES , home , settings.CONFIG_SOURCE_RELEASEROOTDIR );
		}
		
		return( map );
	}

	private void addFolderMapItem( ActionInit action , Map<String,LocalFolder> map , String key , LocalFolder home , String subPath ) throws Exception {
		if( subPath.isEmpty() )
			action.exit1( _Error.MissingDataPath1 , "Missing product data path parameter: " + key , key );
		map.put( key , home.getSubFolder( action , subPath ) );
	}
	
	private void createMirrorInternal( ServerTransaction transaction , boolean push ) throws Exception {
		ActionInit action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( action , null , RESOURCE );
		
		Map<String,LocalFolder> map = getFolderMap( action );
		
		MirrorStorage storage;
		if( push )
			storage = vcs.createInitialMirror( this );
		else
			storage = vcs.createServerMirror( this );
		
		for( String mirrorFolder : map.keySet() ) {
			LocalFolder folder = map.get( mirrorFolder );
			folder.ensureExists( action );
			if( push )
				syncFolderToVcs( action , vcs , storage , mirrorFolder , folder );
			else
				syncVcsToFolder( action , vcs , storage , mirrorFolder , folder );
		}
	}
	
	void createProductMeta( ServerTransaction transaction , ServerProduct product , String name ) throws Exception {
		NAME = name;
		TYPE = TYPE_PRODUCT_META;
		PRODUCT = product.NAME;
		PROJECT = "";
		RESOURCE = "";
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		RESOURCE_DATA = "";
		BRANCH = "";
		createProperties();
	}
	
	void createProductData( ServerTransaction transaction , ServerProduct product , String name ) throws Exception {
		NAME = name;
		TYPE = TYPE_PRODUCT_DATA;
		PRODUCT = product.NAME;
		PROJECT = "";
		RESOURCE = "";
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		RESOURCE_DATA = "";
		BRANCH = "";
		createProperties();
	}
	
	void createProjectSource( ServerTransaction transaction , MetaSourceProject project , String name ) throws Exception {
		NAME = name;
		TYPE = TYPE_PRODUCT_DATA;
		PRODUCT = project.meta.name;
		PROJECT = project.PROJECT;
		RESOURCE = "";
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		RESOURCE_DATA = "";
		BRANCH = "";
		createProperties();
	}
	
	public void dropMirror( ServerTransaction transaction ) throws Exception {
		if( RESOURCE.isEmpty() )
			return;
		
		if( isServer() )
			dropMirrorInternal( transaction );
		else
		if( isProductMeta() )
			dropMirrorInternal( transaction );
		else
		if( isProductData() )
			dropMirrorInternal( transaction );
		
		RESOURCE = "";
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		RESOURCE_DATA = "";
		BRANCH = "";
		createProperties();
	}
	
	private void dropMirrorInternal( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , null , RESOURCE , false , true );
		vcs.dropMirror( this );
	}

	public void pushMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			pushMirrorInternal( transaction );
		else
		if( isProductMeta() )
			pushMirrorInternal( transaction );
		else
		if( isProductData() )
			pushMirrorInternal( transaction );
	}

	private void pushMirrorInternal( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , null , RESOURCE );
		vcs.refreshMirror( this );
		MirrorStorage storage = vcs.getMirror( this );
		ActionInit action = transaction.getAction();
		
		Map<String,LocalFolder> map = getFolderMap( action );
		for( String mirrorFolder : map.keySet() ) {
			LocalFolder folder = map.get( mirrorFolder );
			folder.ensureExists( action );
			syncFolderToVcs( action , vcs , storage , mirrorFolder , folder );
		}
		
		vcs.pushMirror( this );
	}
	
	public void refreshMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			refreshServerMirror( transaction );
		else
		if( isProductMeta() )
			refreshProductMetaMirror( transaction );
		else
		if( isProductData() )
			refreshProductDataMirror( transaction );
	}

	private void refreshServerMirror( ServerTransaction transaction ) throws Exception {
		refreshMirrorInternal( transaction );
	}
	
	private void refreshProductMetaMirror( ServerTransaction transaction ) throws Exception {
		refreshMirrorInternal( transaction );
	}

	private void refreshProductDataMirror( ServerTransaction transaction ) throws Exception {
		refreshMirrorInternal( transaction );
	}

	private void refreshMirrorInternal( ServerTransaction transaction ) throws Exception {
		ActionInit action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , null , RESOURCE );
		vcs.refreshMirror( this );
		
		MirrorStorage storage = vcs.getMirror( this );
		Map<String,LocalFolder> map = getFolderMap( action );
		for( String mirrorFolder : map.keySet() ) {
			LocalFolder folder = map.get( mirrorFolder );
			folder.ensureExists( action );
			syncVcsToFolder( action , vcs , storage , mirrorFolder , folder );
		}
	}
	
	private void syncFolderToVcs( ActionBase action , GenericVCS vcs , MirrorStorage storage , String mirrorFolder , LocalFolder folder ) throws Exception {
		LocalFolder cf = storage.getCommitFolder();
		LocalFolder mf = cf.getSubFolder( action , mirrorFolder );
		LocalFolder sf = folder;
		if( !mf.checkExists( action ) ) {
			mf.ensureExists( action );
			mf.copyDirContent( action , sf );
			vcs.addDirToCommit( this , mf , "." );
		}
		else {
			FileSet mset = mf.getFileSet( action );
			FileSet sset = sf.getFileSet( action );
			syncFolderToVcs( action , vcs , storage , mf , sf , mset , sset );
		}
		
		vcs.commitMasterFolder( this , mf , "" , "sync from source" );
		vcs.pushMirror( this );
	}

	private void syncFolderToVcs( ActionBase action , GenericVCS vcs , MirrorStorage storage , LocalFolder mfolder , LocalFolder sfolder , FileSet mset , FileSet sset ) throws Exception {
		// add to mirror and change
		for( FileSet sd : sset.dirs.values() ) {
			if( vcs.ignoreDir( sd.dirName ) )
				continue;
			
			FileSet md = mset.dirs.get( sd.dirName );
			if( md == null ) {
				sfolder.copyFolder( action , sd.dirPath , mfolder.getSubFolder( action , sd.dirPath ) );
				vcs.addDirToCommit( this , mfolder , sd.dirPath );
			}
			else
				syncFolderToVcs( action , vcs , storage , mfolder , sfolder , md , sd );
		}

		// delete from mirror
		for( FileSet md : mset.dirs.values() ) {
			if( vcs.ignoreDir( md.dirName ) )
				continue;
			
			FileSet sd = sset.dirs.get( md.dirName );
			if( sd == null )
				vcs.deleteDirToCommit( this , mfolder , md.dirPath );
		}
		
		// add files to mirror and change
		LocalFolder dstFolder = mfolder.getSubFolder( action , mset.dirPath );
		for( String sf : sset.files.keySet() ) {
			if( vcs.ignoreFile( sf ) )
				continue;
			
			sfolder.copyFile( action , sset.dirPath , sf , dstFolder , sf );
			if( mset.files.get( sf ) == null )
				vcs.addFileToCommit( this , mfolder , mset.dirPath , sf );
		}

		// delete from mirror
		for( String mf : mset.files.keySet() ) {
			if( vcs.ignoreFile( mf ) )
				continue;
			
			if( sset.files.get( mf ) == null )
				vcs.deleteFileToCommit( this , mfolder , mset.dirPath , mf );
		}
	}
	
	private void syncVcsToFolder( ActionBase action , GenericVCS vcs , MirrorStorage storage , String mirrorFolder , LocalFolder folder ) throws Exception {
		LocalFolder cf = storage.getCommitFolder();
		LocalFolder mf = cf.getSubFolder( action , mirrorFolder );
		if( !mf.checkExists( action ) ) {
			folder.removeThis( action );
			folder.ensureExists( action );
			return;
		}

		folder.ensureExists( action );
		
		LocalFolder sf = folder;
		FileSet mset = mf.getFileSet( action );
		FileSet sset = sf.getFileSet( action );
		syncVcsToFolder( action , vcs , storage , mf , sf , mset , sset );
		
		if( action.isLocalLinux() )
			addLinuxExecution( action , vcs , folder , mset );
	}

	private void addLinuxExecution( ActionBase action , GenericVCS vcs , LocalFolder folder , FileSet set ) throws Exception {
		for( String f : set.files.keySet() ) {
			if( f.endsWith( ".sh" ) ) {
				File ff = new File( folder.getFilePath( action , f ) );
				ff.setExecutable( true );
			}
		}
		for( FileSet md : set.dirs.values() ) {
			if( vcs.ignoreDir( md.dirName ) )
				continue;
			addLinuxExecution( action , vcs , folder.getSubFolder( action , md.dirName ) , md );
		}
	}
	
	private void syncVcsToFolder( ActionBase action , GenericVCS vcs , MirrorStorage storage , LocalFolder mfolder , LocalFolder sfolder , FileSet mset , FileSet sset ) throws Exception {
		// add to source and change
		for( FileSet md : mset.dirs.values() ) {
			if( vcs.ignoreDir( md.dirName ) )
				continue;
			
			FileSet sd = sset.dirs.get( md.dirName );
			if( sd == null )
				mfolder.copyFolder( action , md.dirPath , sfolder.getSubFolder( action , md.dirPath ) );
			else
				syncVcsToFolder( action , vcs , storage , mfolder , sfolder , md , sd );
		}

		// delete from source
		for( FileSet sd : sset.dirs.values() ) {
			if( vcs.ignoreDir( sd.dirName ) )
				continue;
			
			FileSet md = mset.dirs.get( sd.dirName );
			if( md == null )
				sfolder.removeFolder( action , sd.dirPath );
		}
		
		// add files to source and change
		LocalFolder dstFolder = sfolder.getSubFolder( action , sset.dirPath );
		for( String mf : mset.files.keySet() ) {
			if( vcs.ignoreFile( mf ) )
				continue;
			
			mfolder.copyFile( action , mset.dirPath , mf , dstFolder , mf );
		}

		// delete from mirror
		for( String sf : sset.files.keySet() ) {
			if( vcs.ignoreFile( sf ) )
				continue;
			
			if( mset.files.get( sf ) == null )
				dstFolder.removeFolderFile( action , "" , sf );
		}
	}
	
}
