package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.PropertySet;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.vcs.GenericVCS;
import org.urm.engine.vcs.MirrorStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMirrorRepository {

	ServerMirror mirror;
	
	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public String TYPE;
	public String RESOURCE;
	public String RESOURCE_REPO;
	public String RESOURCE_ROOT;
	public String RESOURCE_DATA;
	public String BRANCH;
	
	public static String TYPE_SERVER = "server";
	public static String TYPE_PROJECT = "project";
	public static String TYPE_PRODUCT = "product";

	public ServerMirrorRepository( ServerMirror mirror ) {
		this.mirror = mirror;
		
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
	
	public boolean isProduct() {
		return( TYPE.equals( TYPE_PRODUCT ) );
	}
	
	public ServerMirrorRepository copy( ServerMirror mirror ) throws Exception {
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
		properties.loadRawFromNodeElements( root );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}

	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}
	
	private void scatterSystemProperties() throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		TYPE = properties.getSystemRequiredStringProperty( "type" );
		RESOURCE = properties.getSystemStringProperty( "resource" , "" );
		RESOURCE_REPO = properties.getSystemStringProperty( "repository" , "" );
		RESOURCE_ROOT = properties.getSystemStringProperty( "rootpath" , "" );
		RESOURCE_DATA = properties.getSystemStringProperty( "datapath" , "" );
		BRANCH = properties.getSystemStringProperty( "branch" , "" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "mirror" , null );
		properties.setStringProperty( "name" , NAME );
		properties.setStringProperty( "type" , TYPE );
		properties.setStringProperty( "resource" , RESOURCE );
		properties.setStringProperty( "repository" , RESOURCE_REPO );
		properties.setStringProperty( "rootpath" , RESOURCE_ROOT );
		properties.setStringProperty( "datapath" , RESOURCE_DATA );
		properties.setStringProperty( "branch" , BRANCH );
	}

	public void createMirrorRepository( ServerTransaction transaction , String resource , String reponame , String reporoot , String dataroot , String repobranch , boolean push ) throws Exception {
		RESOURCE = resource;
		RESOURCE_REPO = reponame;
		RESOURCE_ROOT = ( reporoot.isEmpty() )? "/" : reporoot;
		RESOURCE_DATA = ( dataroot.isEmpty() )? "/" : dataroot;
		BRANCH = "";
		
		try {
			if( isServer() )
				createMirrorServer( transaction , push );
		}
		catch( Throwable e ) {
			RESOURCE = "";
			RESOURCE_REPO = "";
			RESOURCE_ROOT = "";
			RESOURCE_DATA = "";
			BRANCH = "";
			transaction.handle0( e , _Error.UnablePublishRepository0 , "Unable to publish repository" );
		}
		
		createProperties();
		mirror.registry.loader.saveMirrors( transaction );
	}
	
	public void createMirrorServer( ServerTransaction transaction , boolean push ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		ActionBase action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( action , RESOURCE , false );
		ServerLoader loader = mirror.engine.getLoader();
		LocalFolder serverSettings = loader.getServerSettingsFolder( action );
		
		if( push ) {
			MirrorStorage storage = vcs.createInitialMirror( this );
			syncFromFolder( action , vcs , storage , serverSettings );
		}
		else {
			MirrorStorage storage = vcs.createServerMirror( this );
			syncToFolder( action , vcs , storage , serverSettings );
		}
	}
	
	public void dropMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			dropServerMirror( transaction );
		
		RESOURCE = "";
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		RESOURCE_DATA = "";
		BRANCH = "";
		createProperties();
		mirror.registry.loader.saveMirrors( transaction );
	}
	
	public void dropServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		vcs.dropMirror( this );
	}

	public void pushMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			pushServerMirror( transaction );
	}

	public void pushServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		vcs.refreshMirror( this );
		MirrorStorage storage = vcs.getMirror( this );
		
		ServerLoader loader = mirror.engine.getLoader();
		ActionBase action = transaction.getAction();
		LocalFolder serverSettings = loader.getServerSettingsFolder( action );
		
		syncFromFolder( action , vcs , storage , serverSettings );
		vcs.pushMirror( this );
	}
	
	public void refreshMirror( ServerTransaction transaction ) throws Exception {
		if( isServer() )
			refreshServerMirror( transaction );
	}

	public void refreshServerMirror( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , RESOURCE , false );
		vcs.refreshMirror( this );
		MirrorStorage storage = vcs.getMirror( this );

		ServerLoader loader = mirror.engine.getLoader();
		ActionBase action = transaction.getAction();
		LocalFolder serverSettings = loader.getServerSettingsFolder( action );
		syncToFolder( action , vcs , storage , serverSettings );
	}
	
	private void syncFromFolder( ActionBase action , GenericVCS vcs , MirrorStorage storage , LocalFolder folder ) throws Exception {
		LocalFolder mf = storage.getCommitFolder();
		LocalFolder sf = folder;
		FileSet mset = mf.getFileSet( action );
		FileSet sset = sf.getFileSet( action );
		syncFromFolder( action , vcs , storage , mf , sf , mset , sset );
		vcs.commitMasterFolder( this , mf , "" , "sync from source" );
		vcs.pushMirror( this );
	}

	private void syncFromFolder( ActionBase action , GenericVCS vcs , MirrorStorage storage , LocalFolder mfolder , LocalFolder sfolder , FileSet mset , FileSet sset ) throws Exception {
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
				syncFromFolder( action , vcs , storage , mfolder , sfolder , md , sd );
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
	
	public void syncToFolder( ActionBase action , GenericVCS vcs , MirrorStorage storage , LocalFolder folder ) throws Exception {
		LocalFolder mf = storage.getCommitFolder();
		LocalFolder sf = folder;
		FileSet mset = mf.getFileSet( action );
		FileSet sset = sf.getFileSet( action );
		syncToFolder( action , vcs , storage , mf , sf , mset , sset );
	}
	
	private void syncToFolder( ActionBase action , GenericVCS vcs , MirrorStorage storage , LocalFolder mfolder , LocalFolder sfolder , FileSet mset , FileSet sset ) throws Exception {
		// add to source and change
		for( FileSet md : mset.dirs.values() ) {
			if( vcs.ignoreDir( md.dirName ) )
				continue;
			
			FileSet sd = sset.dirs.get( md.dirName );
			if( sd == null )
				mfolder.copyFolder( action , md.dirPath , sfolder.getSubFolder( action , md.dirPath ) );
			else
				syncToFolder( action , vcs , storage , mfolder , sfolder , md , sd );
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
