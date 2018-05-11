package org.urm.db.engine;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumMirrorType;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.action.ActionInit;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.engine.vcs.GenericVCS;
import org.urm.engine.vcs.MirrorCase;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine._Error;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBEngineMirrors {

	public static String ELEMENT_MIRROR = "repository";
	public static String TABLE_MIRROR = "urm_mirror";
	public static String FIELD_MIRROR_ID = "mirror_id";
	public static String FIELD_MIRROR_DESC = "xdesc";
	public static String FIELD_MIRROR_TYPE = "mirror_type";
	public static String FIELD_MIRROR_RESOURCE_ID = "resource_id";
	public static String FIELD_MIRROR_RESOURCE_REPO = "resource_repo";
	public static String FIELD_MIRROR_RESOURCE_ROOT = "resource_root";
	public static String FIELD_MIRROR_RESOURCE_DATA = "resource_data";
	
	public static PropertyEntity makeEntityMirror( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.MIRROR , DBEnumParamEntityType.MIRROR , DBEnumObjectVersionType.CORE , TABLE_MIRROR , FIELD_MIRROR_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( MirrorRepository.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MirrorRepository.PROPERTY_DESC , FIELD_MIRROR_DESC , MirrorRepository.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MirrorRepository.PROPERTY_MIRROR_TYPE , FIELD_MIRROR_TYPE , MirrorRepository.PROPERTY_MIRROR_TYPE , "Function type" , true , DBEnumMirrorType.UNKNOWN ) ,
				EntityVar.metaObjectVar( MirrorRepository.PROPERTY_RESOURCE , FIELD_MIRROR_RESOURCE_ID , MirrorRepository.PROPERTY_RESOURCE , "Mirror resource" , DBEnumObjectType.RESOURCE , false ) ,
				EntityVar.metaStringVar( MirrorRepository.PROPERTY_RESOURCE_REPO , FIELD_MIRROR_RESOURCE_REPO , MirrorRepository.PROPERTY_RESOURCE_REPO , "Resource repository" , false , null ) ,
				EntityVar.metaStringVar( MirrorRepository.PROPERTY_RESOURCE_ROOT , FIELD_MIRROR_RESOURCE_ROOT , MirrorRepository.PROPERTY_RESOURCE_ROOT , "Repository root" , false , null ) ,
				EntityVar.metaStringVar( MirrorRepository.PROPERTY_RESOURCE_DATA , FIELD_MIRROR_RESOURCE_DATA , MirrorRepository.PROPERTY_RESOURCE_DATA , "Repository data path" , false , null )
		} ) );
	}

	public static void importxml( EngineLoader loader , EngineMirrors mirrors , Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_MIRROR );
		if( list != null ) {
			for( Node node : list ) {
				MirrorRepository repo = importxmlRepository( loader , mirrors , node );
				mirrors.addRepository( repo );
			}
		}
	}
	
	private static MirrorRepository importxmlRepository( EngineLoader loader , EngineMirrors mirrors , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMirror;
		
		MirrorRepository repo = new MirrorRepository( mirrors );
		repo.createRepository(
				entity.importxmlStringProperty( root , MirrorRepository.PROPERTY_NAME ) ,
				entity.importxmlStringProperty( root , MirrorRepository.PROPERTY_DESC ) ,
				DBEnumMirrorType.getValue( entity.importxmlEnumProperty( root , MirrorRepository.PROPERTY_MIRROR_TYPE ) , true ) );
		repo.setMirror(
				entity.importxmlObjectProperty( loader , root , MirrorRepository.PROPERTY_RESOURCE ) ,
				entity.importxmlStringProperty( root , MirrorRepository.PROPERTY_RESOURCE_REPO ) ,
				entity.importxmlStringProperty( root , MirrorRepository.PROPERTY_RESOURCE_ROOT ) ,
				entity.importxmlStringProperty( root , MirrorRepository.PROPERTY_RESOURCE_DATA )
				);
		modifyRepository( c , repo , true );
		
		return( repo );
	}
	
	public static void modifyRepository( DBConnection c , MirrorRepository repo , boolean insert ) throws Exception {
		if( insert )
			repo.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , repo.NAME , DBEnumParamEntityType.MIRROR );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , repo.NAME , repo.ID , DBEnumParamEntityType.MIRROR );
		
		repo.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMirror , repo.ID , repo.CV , new String[] {
				EngineDB.getString( repo.NAME ) , 
				EngineDB.getString( repo.DESC ) ,
				EngineDB.getEnum( repo.MIRROR_TYPE ) ,
				EngineDB.getObject( repo.RESOURCE_ID ) ,
				EngineDB.getString( repo.RESOURCE_REPO ) ,
				EngineDB.getString( repo.RESOURCE_ROOT ) ,
				EngineDB.getString( repo.RESOURCE_DATA )
				} , insert );
	}

	public static void exportxml( EngineLoader loader , EngineMirrors mirrors , Document doc , Element root ) throws Exception {
		for( String name : mirrors.getRepositoryNames() ) {
			MirrorRepository repo = mirrors.findRepository( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_MIRROR );
			exportxmlRepository( loader , repo , doc , node );
		}
	}
	
	public static void exportxmlRepository( EngineLoader loader , MirrorRepository repo , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMirror;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( repo.NAME ) ,
				entity.exportxmlString( repo.DESC ) ,
				entity.exportxmlEnum( repo.MIRROR_TYPE ) ,
				entity.exportxmlObject( loader , MirrorRepository.PROPERTY_RESOURCE , repo.RESOURCE_ID ) ,
				entity.exportxmlString( repo.RESOURCE_REPO ) ,
				entity.exportxmlString( repo.RESOURCE_ROOT ) ,
				entity.exportxmlString( repo.RESOURCE_DATA )
		} , false );
	}

	public static void loaddb( EngineLoader loader , EngineMirrors mirrors ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMirror;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				MirrorRepository repo = new MirrorRepository( mirrors );
				repo.ID = entity.loaddbId( rs );
				repo.CV = entity.loaddbVersion( rs );
				repo.createRepository( 
						entity.loaddbString( rs , MirrorRepository.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MirrorRepository.PROPERTY_DESC ) ,
						DBEnumMirrorType.getValue( entity.loaddbEnum( rs , MirrorRepository.PROPERTY_MIRROR_TYPE ) , true ) );
				repo.setMirror(
						entity.loaddbObject( rs , MirrorRepository.PROPERTY_RESOURCE ) ,
						entity.loaddbString( rs , MirrorRepository.PROPERTY_RESOURCE_REPO ) ,
						entity.loaddbString( rs , MirrorRepository.PROPERTY_RESOURCE_ROOT ) ,
						entity.loaddbString( rs , MirrorRepository.PROPERTY_RESOURCE_DATA )
						);
				mirrors.addRepository( repo );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void createProductMirrors( EngineTransaction transaction , EngineMirrors mirrors , AppProduct product ) throws Exception {
		// meta
		MirrorRepository meta = mirrors.findProductMetaRepository( product.NAME );
		if( meta == null ) {
			String name = "product-" + product.NAME + "-meta";
			meta = createRepository( transaction , mirrors , name , "standard meta repository" , DBEnumMirrorType.PRODUCT_META );
			mirrors.addRepository( meta );
		}
		
		meta.setProduct( product.ID );
 		
 		// data
		MirrorRepository data = mirrors.findProductDataRepository( product.NAME );
		if( data == null ) {
			String name = "product-" + product.NAME + "-data";
			data = createRepository( transaction , mirrors , name , "standard data repository" , DBEnumMirrorType.PRODUCT_DATA );
			mirrors.addRepository( data );
		}
		
		data.setProduct( product.ID );
	}

	private static MirrorRepository createRepository( EngineTransaction transaction , EngineMirrors mirrors , String name , String desc , DBEnumMirrorType type ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MirrorRepository repo = new MirrorRepository( mirrors );
		repo.createRepository( name , desc , type );
		modifyRepository( c , repo , true );
		
		return( repo );
	}
	
	private static void setMirrorRepository( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo , Integer resourceId , String reponame , String reporoot , String dataroot ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		repo.setMirror( resourceId , reponame , reporoot , dataroot );
		modifyRepository( c , repo , false );
	} 
	
	public static MirrorRepository createProjectMirror( EngineTransaction transaction , EngineMirrors mirrors , MetaSourceProject project , Integer repoRes , String repoName , String repoPath , String codePath ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MirrorRepository repo = new MirrorRepository( mirrors );
		String name = "project-" + project.meta.name + "-" + project.NAME;
		repo.createRepository( name , null , DBEnumMirrorType.PROJECT );
		repo.setMirror( repoRes , repoName , repoPath , codePath );
		modifyRepository( c , repo , true );
		mirrors.addRepository( repo );
		return( repo );
	}
	
	public static void createDetachedMirror( EngineTransaction transaction , EngineMirrors mirrors , DBEnumMirrorType type , String product , String project ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MirrorRepository repo = new MirrorRepository( mirrors );
		String name = "";
		if( type == DBEnumMirrorType.PRODUCT_META )
			name = "product-" + product + "-meta";
		else
		if( type == DBEnumMirrorType.PRODUCT_DATA )
			name = "product-" + product + "-data";
		else
		if( type == DBEnumMirrorType.PROJECT )
			name = "project-" + product + "-" + project;
		repo.createRepository( name , null , type );
		modifyRepository( c , repo , true );
		mirrors.addRepository( repo );
	}
	
	public static void deleteProductResources( EngineTransaction transaction , EngineMirrors mirrors , AppProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		List<MirrorRepository> repos = new LinkedList<MirrorRepository>();
		for( String name : mirrors.getRepositoryNames() ) {
			MirrorRepository repo = mirrors.findRepository( name );
			if( repo.productId == null )
				continue;
			
			if( product.ID == repo.productId )
				repos.add( repo );
		}
		
		for( MirrorRepository repo : repos ) {
			dropMirrorWorkspace( transaction , mirrors , repo , vcsDeleteFlag );
			dropRepository( transaction , mirrors , repo );
		}
	}

	public static void changeProjectMirror( EngineTransaction transaction , EngineMirrors mirrors , MetaSourceProject project , Integer repoRes , String repoName , String repoPath , String codePath ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MirrorRepository repo = mirrors.findProjectRepository( project );
		dropMirrorWorkspace( transaction , mirrors , repo , false );
		
		repo.setMirror( repoRes , repoName , repoPath , codePath );
		modifyRepository( c , repo , false );
	}
	
	public static void deleteProjectMirror( EngineTransaction transaction , EngineMirrors mirrors , MetaSourceProject project ) throws Exception {
		MirrorRepository repoOld = mirrors.findProjectRepository( project );
		if( repoOld != null ) {
			dropMirrorWorkspace( transaction , mirrors , repoOld , false );
			dropRepository( transaction , mirrors , repoOld );
		}
	}

	public static void dropResourceMirrors( EngineTransaction transaction , EngineMirrors mirrors , AuthResource res ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ActionBase action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( action , null , res.ID );
		MirrorCase mc = vcs.getMirror( null );
		mc.removeResourceFolder();

		for( String name : mirrors.getRepositoryNames() ) {
			MirrorRepository repo = mirrors.findRepository( name );
			if( repo.RESOURCE_ID != null && repo.RESOURCE_ID == res.ID ) {
				repo.clearMirror();
				modifyRepository( c , repo , false );
			}
		}
	}

	private static void createServerMirror( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo , boolean push ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		createMirrorInternal( transaction , mirrors , repo , push );
	}

	private static void createProductMetaMirror( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo , boolean push ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		createMirrorInternal( transaction , mirrors , repo , push );
	}

	private static void createProductDataMirror( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo , boolean push ) throws Exception {
		// reject already published
		// server: test target, remove mirror work/repo, create mirror work/repo, publish target
		createMirrorInternal( transaction , mirrors , repo , push );
	}

	private static void createMirrorInternal( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo , boolean push ) throws Exception {
		ActionInit action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( action , null , repo.RESOURCE_ID );
		
		Map<String,LocalFolder> map = getFolderMap( transaction , mirrors , repo );
		
		MirrorCase mc = vcs.getMirror( repo );
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
		
		if( push )
			mc.pushMirror();
	}
	
	public static void createRepository( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo , Integer resourceId , String reponame , String reporoot , String dataroot , boolean push ) throws Exception {
		setMirrorRepository( transaction , mirrors , repo , resourceId , reponame , reporoot , dataroot );
		
		try {
			if( repo.isServer() )
				createServerMirror( transaction , mirrors , repo , push );
			else
			if( repo.isProductMeta() )
				createProductMetaMirror( transaction , mirrors , repo , push );
			else
			if( repo.isProductData() )
				createProductDataMirror( transaction , mirrors , repo , push );
		}
		catch( Throwable e ) {
			repo.clearMirror();
			transaction.handle0( e , _Error.UnablePublishRepository0 , "Unable to create mirror repository" );
		}
	}

	private static Map<String,LocalFolder> getFolderMap( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo ) throws Exception {
		EngineDirectory directory = transaction.getDirectory();
		
		ActionInit action = transaction.getAction();
		if( repo.isServer() )
			return( getFolderMap( action , mirrors , repo , null ) );
			
		AppProduct product = directory.getProduct( repo.productId );
		EngineProductRevisions revisions = product.findRevisions();
		ProductMeta storage = revisions.getDraftRevision();
		return( getFolderMap( action , mirrors , repo , storage ) );
	}
	
	private static Map<String,LocalFolder> getFolderMap( ActionInit action , EngineMirrors mirrors , MirrorRepository repo , ProductMeta storage ) throws Exception {
		EngineLoader loader = action.engine.createLoader( action );
		
		Map<String,LocalFolder> map = new HashMap<String,LocalFolder>();
		if( repo.MIRROR_TYPE == DBEnumMirrorType.SERVER ) {
			LocalFolder serverSettings = loader.getEngineSettingsFolder();
			map.put( "." , serverSettings );
		}
		else
		if( repo.MIRROR_TYPE == DBEnumMirrorType.PRODUCT_META ) {
			AppProduct product = action.getProduct( repo.productId );
			LocalFolder productSettings = loader.getProductHomeFolder( product.NAME );
			map.put( "etc" , productSettings.getSubFolder( action , "etc" ) );
			map.put( "master" , productSettings.getSubFolder( action , "master" ) );
		}
		else
		if( repo.MIRROR_TYPE == DBEnumMirrorType.PRODUCT_DATA ) {
			MetaProductSettings settings = storage.getSettings();
			MetaProductCoreSettings core = settings.getCoreSettings();
			LocalFolder home = loader.getEngineHomeFolder();
			addFolderMapItem( action , map , SourceStorage.DATA_LIVE , home , core.CONFIG_SOURCE_CFG_LIVEROOTDIR );
			addFolderMapItem( action , map , SourceStorage.DATA_TEMPLATES , home , core.CONFIG_SOURCE_CFG_ROOTDIR );
			addFolderMapItem( action , map , SourceStorage.DATA_POSTREFRESH , home , core.CONFIG_SOURCE_SQL_POSTREFRESH );
			addFolderMapItem( action , map , SourceStorage.DATA_CHANGES , home , core.CONFIG_SOURCE_RELEASEROOTDIR );
		}
		
		return( map );
	}

	private static void addFolderMapItem( ActionInit action , Map<String,LocalFolder> map , String key , LocalFolder home , String subPath ) throws Exception {
		if( subPath.isEmpty() )
			action.exit1( _Error.MissingDataPath1 , "Missing product data path parameter: " + key , key );
		map.put( key , home.getSubFolder( action , subPath ) );
	}

	public static void dropMirrorWorkspace( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo , boolean dropOnServer ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( !repo.isActive() )
			return;
		
		dropMirrorInternal( transaction , mirrors , repo , dropOnServer );
		repo.clearMirror();
		modifyRepository( c , repo , false );
	}
	
	public static void dropDetachedMirror( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo ) throws Exception {
		if( repo.isActive() )
			dropMirrorInternal( transaction , mirrors , repo , false );
		
		dropRepository( transaction , mirrors , repo );
	}
	
	private static void dropMirrorInternal( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo , boolean dropOnServer ) throws Exception {
		if( repo.isProject() && dropOnServer )
			transaction.exitUnexpectedState();
		
		// silently ignore if missing
		ActionBase action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( action , null , repo.RESOURCE_ID , true , null );
		MirrorCase mc = vcs.getMirror( repo );
		mc.dropMirror( dropOnServer );
	}

	public static void pushMirror( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo ) throws Exception {
		pushMirrorInternal( transaction , mirrors , repo );
	}

	private static void pushMirrorInternal( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo ) throws Exception {
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , null , repo.RESOURCE_ID );
		MirrorCase mc = vcs.getMirror( repo );
		mc.useMirror();
		
		ActionInit action = transaction.getAction();
		
		Map<String,LocalFolder> map = getFolderMap( transaction , mirrors , repo );
		for( String mirrorFolder : map.keySet() ) {
			LocalFolder folder = map.get( mirrorFolder );
			folder.ensureExists( action );
			mc.syncFolderToVcs( mirrorFolder , folder );
		}
		
		mc.pushMirror();
	}
	
	public static void refreshMirror( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo ) throws Exception {
		refreshMirrorInternal( transaction , mirrors , repo );
	}

	private static void refreshMirrorInternal( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo ) throws Exception {
		ActionInit action = transaction.getAction();
		GenericVCS vcs = GenericVCS.getVCS( transaction.getAction() , null , repo.RESOURCE_ID );
		MirrorCase mc = vcs.getMirror( repo );
		mc.useMirror();
		
		Map<String,LocalFolder> map = getFolderMap( transaction , mirrors , repo );
		for( String mirrorFolder : map.keySet() ) {
			LocalFolder folder = map.get( mirrorFolder );
			folder.ensureExists( action );
			mc.syncVcsToFolder( mirrorFolder , folder );
		}
	}

	private static void dropRepository( EngineTransaction transaction , EngineMirrors mirrors , MirrorRepository repo ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.deleteAppObject( c , entities.entityAppMirror , repo.ID , c.getNextCoreVersion() );
		
		mirrors.removeRepository( repo );
		repo.deleteObject();
	}
	
}
