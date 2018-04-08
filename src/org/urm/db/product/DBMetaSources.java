package org.urm.db.product;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.core.DBNames;
import org.urm.db.engine.DBEngineEntities;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.engine.data.EngineBuilders;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineMatcher;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaProductUnit;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaUnits;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaSources {

	public static String ELEMENT_SET = "projectset";
	public static String ELEMENT_PROJECT = "project";
	public static String ELEMENT_ITEM = "srcitem";

	public static void createdb( EngineLoader loader , ProductMeta storage ) throws Exception {
		MetaSources sources = new MetaSources( storage , storage.meta );
		storage.setSources( sources );
	}
	
	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		MetaSources sources = new MetaSources( storage , storage.meta );
		storage.setSources( sources );
	
		Node[] sets = ConfReader.xmlGetChildren( root , ELEMENT_SET );
		if( sets != null ) {
			for( Node node : sets ) {
				MetaSourceProjectSet projectset = importxmlProjectSet( loader , storage , sources , node );
				sources.addProjectSet( projectset );
			}
		}
	}

	public static MetaSourceProjectSet importxmlProjectSet( EngineLoader loader , ProductMeta storage , MetaSources sources , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceSet;
		
		MetaSourceProjectSet set = new MetaSourceProjectSet( storage.meta , sources );
		set.createProjectSet( 
				entity.importxmlStringAttr( root , MetaSourceProjectSet.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaSourceProjectSet.PROPERTY_DESC )
				);
		modifyProjectSet( c , storage , set , true , DBEnumChangeType.CREATED );
		
		Node[] projects = ConfReader.xmlGetChildren( root , ELEMENT_PROJECT );
		if( projects != null ) {
			for( Node node : projects ) {
				MetaSourceProject project = importxmlProject( loader , storage , sources , set , node );
				sources.addProject( set , project );
			}
		}
		
		return( set );
	}
	
	private static void modifyProjectSet( DBConnection c , ProductMeta storage , MetaSourceProjectSet set , boolean insert , DBEnumChangeType type ) throws Exception {
		if( insert )
			set.ID = DBNames.getNameIndex( c , storage.ID , set.NAME , DBEnumParamEntityType.PRODUCT_SOURCESET );
		else
			DBNames.updateName( c , storage.ID , set.NAME , set.ID , DBEnumParamEntityType.PRODUCT_SOURCESET );
		
		set.PV = c.getNextProductVersion( storage );
		set.CHANGETYPE = type;
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaSourceSet , set.ID , set.PV , new String[] {
				EngineDB.getInteger( storage.ID ) , 
				EngineDB.getString( set.NAME ) ,
				EngineDB.getString( set.DESC )
				} , insert , type );
	}
	
	public static MetaSourceProject importxmlProject( EngineLoader loader , ProductMeta storage , MetaSources sources , MetaSourceProjectSet set , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceProject;
		EngineMatcher matcher = loader.getMatcher();
		
		MetaSourceProject project = new MetaSourceProject( storage.meta , set );
		MetaUnits units = storage.getUnits();
		Integer unitId = units.getUnitId( entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_UNIT ) );
				
		project.createProject( 
				entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_DESC ) ,
				entity.importxmlIntAttr( root , MetaSourceProject.PROPERTY_PROJECTPOS ) ,
				unitId ,
				entity.importxmlBooleanAttr( root , MetaSourceProject.PROPERTY_PROD , false )
				);
		
		EngineMirrors mirrors = loader.getMirrors();
		String mirrorName = mirrors.getProjectRepositoryMirroName( project );
		MirrorRepository repo = mirrors.findRepository( mirrorName );
		
		String mirrorRes = entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_MIRRORRES );
		String mirrorRepo = entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_MIRRORREPO );
		String mirrorPath = entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_MIRRORPATH );
		String mirrorData = entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_MIRRORDATA );
		
		// mirror is not matched if attributes are not the same
		if( repo != null ) {
			AuthResource res = repo.getResource( loader.getAction() );
			if( mirrorRes.equals( res.NAME ) == false || 
				repo.RESOURCE_REPO.equals( mirrorRes ) == false ||
				repo.RESOURCE_ROOT.equals( mirrorPath ) == false ||
				repo.RESOURCE_DATA.equals( mirrorData ) == false )
				repo = null;
		}
		
		MatchItem matchMirror = ( repo == null )? new MatchItem( mirrorName ) : new MatchItem( repo.ID );
		if( repo != null ) {
			mirrorRes = "";
			mirrorRepo = "";
			mirrorPath = "";
			mirrorData = "";
		}
		
		project.setSource( 
				DBEnumProjectType.getValue( entity.importxmlEnumAttr( root , MetaSourceProject.PROPERTY_PROJECTTYPE ) , true ) ,
				entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_TRACKER ) ,
				matchMirror , mirrorRes , mirrorRepo , mirrorPath , mirrorData );
		
		EngineBuilders builders = loader.getBuilders();
		String value = matcher.matchProductBefore( storage , entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_BUILDER_NAME ) , project.ID , entity , MetaSourceProject.PROPERTY_BUILDER_NAME , null );
		MatchItem matchBuilder = builders.getBuilderMatchItem( null , value );
		matcher.matchProductDone( matchBuilder );
		project.setBuild( matchBuilder , 
				entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_BUILDER_OPTIONS ) ,
				entity.importxmlStringAttr( root , MetaSourceProject.PROPERTY_BRANCH )
				);
				
		project.setCustom(
				entity.importxmlBooleanAttr( root , MetaSourceProject.PROPERTY_CUSTOM_BUILD , false ) ,
				entity.importxmlBooleanAttr( root , MetaSourceProject.PROPERTY_CUSTOM_GET , false )
				);
		
		modifyProject( c , storage , project , true , DBEnumChangeType.CREATED );
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_ITEM );
		if( items != null ) {
			for( Node node : items ) {
				MetaSourceProjectItem item = importxmlProjectItem( loader , storage , sources , project , node );
				sources.addProjectItem( project , item );
			}
		}
		
		return( project );
	}
	
	public static void modifyProject( DBConnection c , ProductMeta storage , MetaSourceProject project , boolean insert , DBEnumChangeType type ) throws Exception {
		if( insert )
			project.ID = DBNames.getNameIndex( c , storage.ID , project.NAME , DBEnumParamEntityType.PRODUCT_SOURCEPROJECT );
		else
			DBNames.updateName( c , storage.ID , project.NAME , project.ID , DBEnumParamEntityType.PRODUCT_SOURCEPROJECT );
		
		project.PV = c.getNextProductVersion( storage );
		project.CHANGETYPE = type;
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaSourceProject , project.ID , project.PV , new String[] {
				EngineDB.getInteger( storage.ID ) , 
				EngineDB.getInteger( project.set.ID ) ,
				EngineDB.getString( project.NAME ) ,
				EngineDB.getString( project.DESC ) ,
				EngineDB.getInteger( project.PROJECT_POS ) ,
				EngineDB.getEnum( project.PROJECT_TYPE ) ,
				EngineDB.getBoolean( project.CODEBASE_PROD ) ,
				EngineDB.getObject( project.UNIT_ID ) ,
				EngineDB.getString( project.TRACKER ) ,
				EngineDB.getString( project.BRANCH ) ,
				EngineDB.getMatchId( project.BUILDER ) ,
				EngineDB.getMatchName( project.BUILDER ) ,
				EngineDB.getString( project.BUILDER_ADDOPTIONS ) ,
				EngineDB.getMatchId( project.MIRROR ) ,
				EngineDB.getString( project.MIRROR_RESOURCE ) ,
				EngineDB.getString( project.MIRROR_REPOSITORY ) ,
				EngineDB.getString( project.MIRROR_REPOPATH ) ,
				EngineDB.getString( project.MIRROR_CODEPATH ) ,
				EngineDB.getBoolean( project.CUSTOMBUILD ) ,
				EngineDB.getBoolean( project.CUSTOMGET )
				} , insert , type );
	}
	
	public static MetaSourceProjectItem importxmlProjectItem( EngineLoader loader , ProductMeta storage , MetaSources sources , MetaSourceProject project , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceItem;
		
		MetaSourceProjectItem item = new MetaSourceProjectItem( storage.meta , project );
		item.createItem( 
				entity.importxmlStringAttr( root , MetaSourceProjectItem.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaSourceProjectItem.PROPERTY_DESC )
				);
		item.setSourceData( 
				DBEnumSourceItemType.getValue( entity.importxmlEnumAttr( root , MetaSourceProjectItem.PROPERTY_SRCTYPE ) , true ) ,
				entity.importxmlStringAttr( root , MetaSourceProjectItem.PROPERTY_BASENAME ) ,
				entity.importxmlStringAttr( root , MetaSourceProjectItem.PROPERTY_EXT ) ,
				entity.importxmlStringAttr( root , MetaSourceProjectItem.PROPERTY_STATICEXT ) ,
				entity.importxmlStringAttr( root , MetaSourceProjectItem.PROPERTY_PATH ) ,
				entity.importxmlStringAttr( root , MetaSourceProjectItem.PROPERTY_VERSION ) ,
				entity.importxmlBooleanAttr( root , MetaSourceProjectItem.PROPERTY_NODIST , false )
				);
				
		modifyProjectItem( c , storage , item , true , DBEnumChangeType.CREATED );
		
		return( item );
	}
	
	private static void modifyProjectItem( DBConnection c , ProductMeta storage , MetaSourceProjectItem item , boolean insert , DBEnumChangeType type ) throws Exception {
		if( insert )
			item.ID = DBNames.getNameIndex( c , item.project.ID , item.NAME , DBEnumParamEntityType.PRODUCT_SOURCEITEM );
		else
			DBNames.updateName( c , item.project.ID , item.NAME , item.ID , DBEnumParamEntityType.PRODUCT_SOURCEITEM );
		
		item.PV = c.getNextProductVersion( storage );
		item.CHANGETYPE = type;
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaSourceItem , item.ID , item.PV , new String[] {
				EngineDB.getInteger( storage.ID ) , 
				EngineDB.getInteger( item.project.ID ) , 
				EngineDB.getString( item.NAME ) ,
				EngineDB.getString( item.DESC ) ,
				EngineDB.getEnum( item.SOURCEITEM_TYPE ) ,
				EngineDB.getString( item.BASENAME ) ,
				EngineDB.getString( item.EXT ) ,
				EngineDB.getString( item.STATICEXT ) ,
				EngineDB.getString( item.PATH ) ,
				EngineDB.getString( item.FIXED_VERSION ) ,
				EngineDB.getBoolean( item.INTERNAL )
				} , insert , type );
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		MetaSources sources = storage.getSources();
		
		for( String name : sources.getSetNames() ) {
			MetaSourceProjectSet set = sources.findProjectSet( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_SET );
			exportxmlSet( loader , storage , set , doc , node );
		}
	}

	public static void exportxmlSet( EngineLoader loader , ProductMeta storage , MetaSourceProjectSet set , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceSet;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( set.NAME ) ,
				entity.exportxmlString( set.DESC )
		} , true );
		
		for( MetaSourceProject project : set.getOrderedList() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_PROJECT );
			exportxmlProject( loader , storage , project , doc , node );
		}
	}
	
	public static void exportxmlProject( EngineLoader loader , ProductMeta storage , MetaSourceProject project , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceProject;
		
		MetaUnits units = storage.getUnits();
		String unitName = units.getUnitName( project.UNIT_ID );
		
		EngineBuilders builders = loader.getBuilders();
		String builderName = builders.getBuilderName( project.BUILDER );

		String mirrorRes = project.MIRROR_RESOURCE;
		String mirrorRepo = project.MIRROR_REPOSITORY;
		String mirrorPath = project.MIRROR_REPOPATH;
		String mirrorData = project.MIRROR_CODEPATH;
		
		if( project.MIRROR.MATCHED ) {
			EngineMirrors mirrors = loader.getMirrors();
			MirrorRepository repo = mirrors.getRepository( project.MIRROR.FKID );
			
			AuthResource res = repo.getResource( loader.getAction() );
			mirrorRes = res.NAME;
			mirrorRepo = repo.RESOURCE_REPO;
			mirrorPath = repo.RESOURCE_ROOT;
			mirrorData = repo.RESOURCE_DATA;
		}
		
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( project.NAME ) ,
				entity.exportxmlString( project.DESC ) ,
				entity.exportxmlInt( project.PROJECT_POS ) ,
				entity.exportxmlEnum( project.PROJECT_TYPE ) ,
				entity.exportxmlBoolean( project.CODEBASE_PROD ) ,
				entity.exportxmlString( unitName ) ,
				entity.exportxmlString( project.TRACKER ) ,
				entity.exportxmlString( project.BRANCH ) ,
				entity.exportxmlString( builderName ) ,
				entity.exportxmlString( project.BUILDER_ADDOPTIONS ) ,
				entity.exportxmlString( mirrorRes ) ,
				entity.exportxmlString( mirrorRepo ) ,
				entity.exportxmlString( mirrorPath ) ,
				entity.exportxmlString( mirrorData ) ,
				entity.exportxmlBoolean( project.CUSTOMBUILD ) ,
				entity.exportxmlBoolean( project.CUSTOMGET )
 		} , true );
		
		for( String name : project.getItemNames() ) {
			MetaSourceProjectItem item = project.findItem( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_ITEM );
			exportxmlProjectItem( loader , storage , item , doc , node );
		}
	}

	public static void exportxmlProjectItem( EngineLoader loader , ProductMeta storage , MetaSourceProjectItem item , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceItem;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( item.NAME ) ,
				entity.exportxmlString( item.DESC ) ,
				entity.exportxmlEnum( item.SOURCEITEM_TYPE ) ,
				entity.exportxmlString( item.BASENAME ) ,
				entity.exportxmlString( item.EXT ) ,
				entity.exportxmlString( item.STATICEXT ) ,
				entity.exportxmlString( item.PATH ) ,
				entity.exportxmlString( item.FIXED_VERSION ) ,
				entity.exportxmlBoolean( item.INTERNAL )
		} , true );
	}
	
	public static void loaddb( EngineLoader loader , ProductMeta storage ) throws Exception {
		MetaSources sources = new MetaSources( storage , storage.meta );
		storage.setSources( sources );
		
		loaddbSets( loader , storage , sources );
		loaddbProjects( loader , storage , sources );
		loaddbProjectItems( loader , storage , sources );
	}
	
	public static void loaddbSets( EngineLoader loader , ProductMeta storage , MetaSources sources ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceSet;

		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID1 , new String[] { "" + storage.ID } );
		try {
			while( rs.next() ) {
				MetaSourceProjectSet set = new MetaSourceProjectSet( storage.meta , sources );
				set.ID = entity.loaddbId( rs );
				set.PV = entity.loaddbVersion( rs );
				set.createProjectSet( 
						entity.loaddbString( rs , MetaSourceProjectSet.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaSourceProjectSet.PROPERTY_DESC )
						);
				sources.addProjectSet( set );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbProjects( EngineLoader loader , ProductMeta storage , MetaSources sources ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineBuilders builders = loader.getBuilders();
		EngineMatcher matcher = loader.getMatcher();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceProject;

		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID1 , new String[] { "" + storage.ID } );
		try {
			while( rs.next() ) {
				int setId = entity.loaddbInt( rs , DBProductData.FIELD_SOURCEPROJECT_SET_ID );
				MetaSourceProjectSet set = sources.getProjectSet( setId );
				MetaSourceProject project = new MetaSourceProject( storage.meta , set );
				project.ID = entity.loaddbId( rs );
				project.PV = entity.loaddbVersion( rs );

				// create
				project.createProject( 
						entity.loaddbString( rs , MetaSourceProject.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaSourceProject.PROPERTY_DESC ) ,
						entity.loaddbInt( rs , MetaSourceProject.PROPERTY_PROJECTPOS ) ,
						entity.loaddbObject( rs , DBProductData.FIELD_SOURCEPROJECT_UNIT_ID ) ,
						entity.loaddbBoolean( rs , MetaSourceProject.PROPERTY_PROD )
						);
				
				// mirror
				Integer mirrorId = entity.loaddbObject( rs , DBProductData.FIELD_SOURCEPROJECT_MIRROR_ID );
				MatchItem matchMirror = null;
				
				String mirrorRes = null;
				String mirrorRepo = null;
				String mirrorPath = null;
				String mirrorData = null;
				
				if( mirrorId != null )
					matchMirror = new MatchItem( mirrorId );
				else {
					EngineMirrors mirrors = loader.getMirrors();
					String mirrorName = mirrors.getProjectRepositoryMirroName( project );
					matchMirror = new MatchItem( mirrorName );
					mirrorRes = entity.loaddbString( rs , MetaSourceProject.PROPERTY_MIRRORRES );
					mirrorRepo = entity.loaddbString( rs , MetaSourceProject.PROPERTY_MIRRORREPO );
					mirrorPath = entity.loaddbString( rs , MetaSourceProject.PROPERTY_MIRRORPATH );
					mirrorData = entity.loaddbString( rs , MetaSourceProject.PROPERTY_MIRRORDATA );
				}
				
				// source
				project.setSource(
						DBEnumProjectType.getValue( entity.loaddbEnum( rs , MetaSourceProject.PROPERTY_PROJECTTYPE ) , true ) ,
						entity.loaddbString( rs , MetaSourceProject.PROPERTY_TRACKER ) ,
						matchMirror , mirrorRes , mirrorRepo , mirrorPath , mirrorData );

				// build
				MatchItem builder = builders.getBuilderMatchItem(
						entity.loaddbObject( rs , DBProductData.FIELD_SOURCEPROJECT_BUILDER_ID ) ,
						entity.loaddbString( rs , MetaSourceProject.PROPERTY_BUILDER_NAME ) );
				matcher.matchProductDone( builder , storage , project.ID , entity , MetaSourceProject.PROPERTY_BUILDER_NAME , null );
				project.setBuild(
						builder , 
						entity.loaddbString( rs , MetaSourceProject.PROPERTY_BUILDER_OPTIONS ) , 
						entity.loaddbString( rs , MetaSourceProject.PROPERTY_BRANCH ) );
				
				// custom
				project.setCustom(
						entity.loaddbBoolean( rs , MetaSourceProject.PROPERTY_CUSTOM_BUILD ) , 
						entity.loaddbBoolean( rs , MetaSourceProject.PROPERTY_CUSTOM_GET ) );
				
				sources.addProject( set , project );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbProjectItems( EngineLoader loader , ProductMeta storage , MetaSources sources ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceItem;

		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID1 , new String[] { "" + storage.ID } );
		try {
			while( rs.next() ) {
				int projectId = entity.loaddbInt( rs , DBProductData.FIELD_SOURCEITEM_PROJECT_ID );
				MetaSourceProject project = sources.getProject( projectId );
				MetaSourceProjectItem item = new MetaSourceProjectItem( storage.meta , project );
				item.ID = entity.loaddbId( rs );
				item.PV = entity.loaddbVersion( rs );
				item.createItem( 
						entity.loaddbString( rs , MetaSourceProjectSet.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaSourceProjectSet.PROPERTY_DESC )
						);
				
				item.setSourceData(
						DBEnumSourceItemType.getValue( entity.loaddbEnum( rs , MetaSourceProjectItem.PROPERTY_SRCTYPE ) , true ) ,
						entity.loaddbString( rs , MetaSourceProjectItem.PROPERTY_BASENAME ) ,
						entity.loaddbString( rs , MetaSourceProjectItem.PROPERTY_EXT ) ,
						entity.loaddbString( rs , MetaSourceProjectItem.PROPERTY_STATICEXT ) ,
						entity.loaddbString( rs , MetaSourceProjectItem.PROPERTY_PATH ) ,
						entity.loaddbString( rs , MetaSourceProjectItem.PROPERTY_VERSION ) ,
						entity.loaddbBoolean( rs , MetaSourceProjectItem.PROPERTY_NODIST )
						);
				sources.addProjectItem( project , item );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static MetaSourceProjectSet createProjectSet( EngineTransaction transaction , ProductMeta storage , MetaSources sources , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( sources.findProjectSet( name ) != null )
			transaction.exitUnexpectedState();
		
		MetaSourceProjectSet set = new MetaSourceProjectSet( storage.meta , sources );
		set.createProjectSet( name , desc );
		modifyProjectSet( c , storage , set , true , DBEnumChangeType.CREATED );
		
		sources.addProjectSet( set );
		return( set );
	}
	
	public static MetaSourceProject createProject( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProjectSet set , 
			String name , String desc , int pos , Integer unit , boolean prod , 
			DBEnumProjectType type , String tracker , Integer repoRes , String repoName , String repoPath , String codePath ,
			Integer builder , String addOptions , String branch ,
			boolean customBuild , boolean customGet ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( sources.findProject( name ) != null )
			transaction.exitUnexpectedState();
		
		MetaSourceProject project = new MetaSourceProject( storage.meta , set );
		project.createProject( name , desc , pos , unit , prod );
		
		EngineMirrors mirrors = transaction.changeMirrors();
		MirrorRepository mirror = DBEngineMirrors.createProjectMirror( transaction , mirrors , project , repoRes , repoName , repoPath , codePath );
		
		project.setSource( type , tracker , new MatchItem( mirror.ID ) , null , "" , "" , "" );
		project.setBuild( new MatchItem( builder ) , addOptions , branch );
		project.setCustom( customBuild , customGet );
		modifyProject( c , storage , project , true , DBEnumChangeType.CREATED );
		
		sources.addProject( set , project );
		
		return( project );
	}

	public static void modifyProject( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProject project , 
			String name , String desc , int pos , Integer unit , boolean prod , 
			DBEnumProjectType type , String tracker , Integer repoRes , String repoName , String repoPath , String codePath ,
			Integer builder , String addOptions , String branch ,
			boolean customBuild , boolean customGet ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaSourceProject projectRename = sources.findProject( name );
		if( projectRename != null && projectRename != project )
			transaction.exitUnexpectedState();
		
		project.modifyProject( name , desc , pos , unit , prod );

		EngineMirrors mirrors = transaction.getMirrors();
		MirrorRepository mirror = mirrors.getRepository( project.getMirrorId() );
		
		project.setSource( type , tracker , new MatchItem( mirror.ID ) , null , "" , "" , "" );
		project.setBuild( new MatchItem( builder ) , addOptions , branch );
		project.setCustom( customBuild , customGet );
		modifyProject( c , storage , project , false , DBEnumChangeType.UPDATED );

		if( Common.equalsIntegers( mirror.RESOURCE_ID , repoRes ) == false || 
				mirror.RESOURCE_REPO.equals( repoName ) == false || 
				mirror.RESOURCE_ROOT.equals( repoPath ) == false || 
				mirror.RESOURCE_DATA.equals( codePath ) == false ) {
			mirrors = transaction.changeMirrors();
			DBEngineMirrors.changeProjectMirror( transaction , mirrors , project , repoRes , repoName , repoPath , codePath );
		}
		
		sources.updateProject( project );
	}

	public static void changeProjectSet( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProject project , MetaSourceProjectSet setNew , int posNew ) throws Exception {
		DBConnection c = transaction.getConnection();

		if( posNew < 1 )
			posNew = 1;
		if( posNew > setNew.getProjects().length )
			posNew = setNew.getProjects().length;
		
		MetaSourceProjectSet setOld = project.set;
		setOld.removeProject( project );
		if( !c.modify( DBQueries.MODIFY_SOURCE_SHIFTPOS_ONDELETEPROJECT3 , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getInteger( setOld.ID ) ,
				EngineDB.getInteger( project.PROJECT_POS )
				}))
			transaction.exitUnexpectedState();
		
		project.changeProjectSet( setNew , posNew );
		setNew.addProject( project );
		
		if( !c.modify( DBQueries.MODIFY_SOURCE_CHANGEPROJECTSET2 , new String[] {
				EngineDB.getInteger( project.ID ) ,
				EngineDB.getInteger( setNew.ID )
				}))
			transaction.exitUnexpectedState();
		
		if( !c.modify( DBQueries.MODIFY_SOURCE_SHIFTPOS_ONINSERTPROJECT3 , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getInteger( setNew.ID ) ,
				EngineDB.getInteger( posNew )
				}))
			transaction.exitUnexpectedState();
		
		if( setOld.isEmpty() ) {
			EngineEntities entities = c.getEntities();
			PropertyEntity entity = entities.entityAppMetaSourceSet;
			
			int version = c.getNextProductVersion( storage );
			DBEngineEntities.deleteAppObject( c , entity , setOld.ID , version );
			sources.removeProjectSet( setOld );
		}
	}
	
	public static void changeProjectOrder( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProject project , int posNew ) throws Exception {
		DBConnection c = transaction.getConnection();

		MetaSourceProjectSet set = project.set;
		if( posNew < 1 )
			posNew = 1;
		if( posNew > set.getProjects().length )
			posNew = set.getProjects().length;
		
		set.removeProject( project );
		if( !c.modify( DBQueries.MODIFY_SOURCE_SHIFTPOS_ONDELETEPROJECT3 , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getInteger( set.ID ) ,
				EngineDB.getInteger( project.PROJECT_POS )
				}))
			transaction.exitUnexpectedState();
		
		set.changeProjectOrder( project , posNew );
		
		if( !c.modify( DBQueries.MODIFY_SOURCE_CHANGEPROJECTORDER2 , new String[] {
				EngineDB.getInteger( project.ID ) ,
				EngineDB.getInteger( posNew )
				}))
			transaction.exitUnexpectedState();
		
		if( !c.modify( DBQueries.MODIFY_SOURCE_SHIFTPOS_ONINSERTPROJECT3 , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getInteger( set.ID ) ,
				EngineDB.getInteger( posNew )
				}))
			transaction.exitUnexpectedState();
	}

	public static void modifySetOrder( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProjectSet set , String[] namesOrdered ) throws Exception {
		DBConnection c = transaction.getConnection();

		MetaSourceProject[] projects = set.getProjects();
		if( projects.length != namesOrdered.length )
			transaction.exitUnexpectedState();
		
		for( MetaSourceProject project : projects ) {
			int pos = Common.findItem( project.NAME , namesOrdered );
			if( pos < 0 )
				transaction.exitUnexpectedState();
			
			project.changeOrder( pos + 1 );
			if( !c.modify( DBQueries.MODIFY_SOURCE_CHANGEPROJECTORDER2 , new String[] {
					EngineDB.getInteger( project.ID ) ,
					EngineDB.getInteger( project.PROJECT_POS )
					}))
				transaction.exitUnexpectedState();
		}
		
		set.reorderProjects();
	}
	
	public static MetaSourceProjectItem createProjectItem( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProject project , 
			String name , String desc ,  
			DBEnumSourceItemType srcType , String basename , String ext , String staticext , String path , String version , boolean internal ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( project.findItem( name ) != null )
			transaction.exitUnexpectedState();
		
		MetaSourceProjectItem item = new MetaSourceProjectItem( storage.meta , project );
		item.createItem( name , desc );
		item.setSourceData( srcType , basename , ext , staticext , path , version , internal );
		
		modifyProjectItem( c , storage , item , true , DBEnumChangeType.CREATED );
		sources.addProjectItem( project , item );
		
		return( item );
	}

	public static void modifyProjectItem( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProjectItem item , 
			String name , String desc ,  
			DBEnumSourceItemType srcType , String basename , String ext , String staticext , String path , String version , boolean internal ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaSourceProject project = item.project;
		MetaSourceProjectItem itemRenamed = project.findItem( name );
		if( itemRenamed != null && itemRenamed != item )
			transaction.exitUnexpectedState();
		
		item.modifyItem( name , desc );
		item.setSourceData( srcType , basename , ext , staticext , path , version , internal );
		
		modifyProjectItem( c , storage , item , false , DBEnumChangeType.UPDATED );
		sources.updateProjectItem( item );
	}

	public static void deleteProject( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProject project , boolean leaveManual ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceItem;

		MetaDistr distr = storage.getDistr();
		for( MetaSourceProjectItem item : project.getItems() ) {
			MetaDistrBinaryItem distItem = item.distItem;
			if( leaveManual )
				DBMetaDistr.changeBinaryItemProjectToManual( transaction , storage , distr , distItem );
			else
				DBMetaDistr.deleteBinaryItem( transaction , storage , distr , distItem );
		}
		
		if( !c.modify( DBQueries.MODIFY_SOURCE_DELETEPROJECTITEMS1 , new String[] {
				EngineDB.getInteger( project.ID )
				}))
			transaction.exitUnexpectedState();
		
		int version = c.getCurrentProductVersion( storage );
		DBEngineEntities.deleteAppObject( c , entity , project.ID , version );
		sources.removeProject( project.set , project );
		
		transaction.changeMirrors();
		EngineMirrors mirrors = transaction.getTransactionMirrors();
		DBEngineMirrors.deleteProjectMirror( transaction , mirrors , project );
	}

	public static void deleteProjectItem( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaSourceProjectItem item ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceItem;
		
		int version = c.getNextProductVersion( storage );
		DBEngineEntities.deleteAppObject( c , entity , item.ID , version );
		sources.removeProjectItem( item.project , item );
	}

	public static void deleteUnit( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaProductUnit unit ) throws Exception {
		for( String name : sources.getProjectNames() ) {
			MetaSourceProject project = sources.findProject( name );
			if( Common.equalsIntegers( project.UNIT_ID , unit.ID ) )
				project.clearUnit();
		}
	}

}
