package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.PropertySet;
import org.urm.engine.ServerTransaction;
import org.urm.engine.action.ActionInit;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.engine.vcs.GenericVCS;
import org.urm.engine.vcs.MirrorCase;
import org.urm.meta.ServerObject;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMirrorRepository extends ServerObject {

	public ServerMirrors mirrors;
	
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

	@Override
	public String getName() {
		return( NAME );
	}
	
	public String getFolderName() {
		return( NAME );
	}
	
	public boolean isActive() {
		if( RESOURCE.isEmpty() )
			return( false );
		return( true );
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
		properties.loadFromNodeElements( root , false );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}

	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root , false );
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
		
		MirrorCase mc = vcs.getMirror( this );
		if( push )
			mc.createEmptyMirrorOnServer();
		else
			mc.useMirror();
		
		for( String mirrorFolder : map.keySet() ) {
			LocalFolder folder = map.get( mirrorFolder );
			folder.ensureExists( action );
			if( push )
				mc.syncFolderToVcs( mirrorFolder , folder );
			else
				mc.syncVcsToFolder( mirrorFolder , folder );
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
		TYPE = TYPE_PROJECT;
		PRODUCT = project.meta.name;
		PROJECT = project.NAME;
		RESOURCE = project.RESOURCE;
		RESOURCE_REPO = project.REPOSITORY;
		RESOURCE_ROOT = project.REPOPATH;
		RESOURCE_DATA = project.CODEPATH;
		BRANCH = project.BRANCH;
		createProperties();
	}
	
	public void dropMirror( ServerTransaction transaction , boolean dropOnServer ) throws Exception {
		if( RESOURCE.isEmpty() )
			return;
		
		dropMirrorInternal( transaction , dropOnServer );
		
		RESOURCE = "";
		RESOURCE_REPO = "";
		RESOURCE_ROOT = "";
		RESOURCE_DATA = "";
		BRANCH = "";
		createProperties();
	}
	
	private void dropMirrorInternal( ServerTransaction transaction , boolean dropOnServer ) throws Exception {
		if( isProject() && dropOnServer )
			transaction.exitUnexpectedState();
		
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , null , RESOURCE , "" , true );
		MirrorCase mc = vcs.getMirror( this );
		mc.dropMirror( dropOnServer );
	}

	public void pushMirror( ServerTransaction transaction ) throws Exception {
		pushMirrorInternal( transaction );
	}

	private void pushMirrorInternal( ServerTransaction transaction ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , null , RESOURCE );
		MirrorCase mc = vcs.getMirror( this );
		mc.refreshComponent( true );
		
		ActionInit action = transaction.getAction();
		
		Map<String,LocalFolder> map = getFolderMap( action );
		for( String mirrorFolder : map.keySet() ) {
			LocalFolder folder = map.get( mirrorFolder );
			folder.ensureExists( action );
			mc.syncFolderToVcs( mirrorFolder , folder );
		}
		
		mc.pushComponentChanges();
	}
	
	public void refreshMirror( ServerTransaction transaction ) throws Exception {
		refreshMirrorInternal( transaction );
	}

	private void refreshMirrorInternal( ServerTransaction transaction ) throws Exception {
		ActionInit action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , null , RESOURCE );
		MirrorCase mc = vcs.getMirror( this );
		mc.refreshComponent( true );
		
		Map<String,LocalFolder> map = getFolderMap( action );
		for( String mirrorFolder : map.keySet() ) {
			LocalFolder folder = map.get( mirrorFolder );
			folder.ensureExists( action );
			mc.syncVcsToFolder( mirrorFolder , folder );
		}
	}
	
}
