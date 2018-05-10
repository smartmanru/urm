package org.urm.db.env;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.DBEnumEnvType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineResources;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.loader.EngineMatcher;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnv {

	public static String ELEMENT_SEGMENT = "segment";
	public static String ATTR_VERSION = "envversion";
	
	public static MetaEnv importxml( EngineLoader loader , EngineProduct ep , ProductMeta storage , Node root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppEnvPrimary;
		String name = entity.importxmlStringAttr( root , MetaEnv.PROPERTY_NAME );
		
		if( ep.findEnv( name ) != null ) {
			loader.trace( "skip import existing environment, name=" + name );
			return( null );
		}
		
		ProductEnvs envs = storage.getEnviroments();
		MetaEnv env = new MetaEnv( storage , storage.meta , envs );
		
		importxmlMain( loader , storage , name , env , root );
		importxmlSegments( loader , storage , env , root );
		DBMetaDump.importxmlAll( loader , storage , env , root );
		
		envs.addEnv( env );
		return( env );
	}

	public static void matchBaseline( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		ProductEnvs envs = storage.getEnviroments();
		DBConnection c = loader.getConnection();
		
		MatchItem BASELINE = env.getBaselineMatchItem();
		if( BASELINE != null ) {
			String value = matcher.matchEnvBefore( env , BASELINE.FKNAME , env.ID , entities.entityAppEnvPrimary , MetaEnv.PROPERTY_BASELINE , null );
			MetaEnv baseline = envs.findMetaEnv( value );
			if( baseline != null ) {
				BASELINE.match( baseline.ID );
				modifyEnvMatch( c , storage , env );
			}
			matcher.matchEnvDone( BASELINE );
			
			if( baseline != null ) {
				for( MetaEnvSegment sg : env.getSegments() )
					DBMetaEnvSegment.matchBaseline( loader , storage , env , sg , baseline );
			}
		}
	}
	
	private static void importxmlMain( EngineLoader loader , ProductMeta storage , String name , MetaEnv env , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		EngineResources resources = loader.getResources();
		EngineInfrastructure infra = loader.getInfrastructure();
		
		// identify
		String version = ConfReader.getAttrValue( root , ATTR_VERSION );
		PropertyEntity entity = entities.entityAppEnvPrimary;
		env.ID = DBNames.getNameIndex( c , storage.ID , name , DBEnumParamEntityType.ENV_PRIMARY );

		loader.trace( "import meta env object, object=" + env.objectId + ", id=" + env.ID + ", name=" + name + ", source version=" + version );

		TransactionBase transaction = loader.getTransaction();
		if( !transaction.requestImportEnv( env ) )
			Common.exitUnexpected();
		
		// create settings
		MetaProductSettings settings = storage.getSettings();
		ObjectProperties ops = entities.createMetaEnvProps( env.ID , settings.ops );
		env.createSettings( ops );

		// primary match (baseline match is postponed)
		MatchItem BASELINE = MatchItem.create( entity.importxmlStringAttr( root , MetaEnv.PROPERTY_BASELINE ) );
		
		String envKey = entity.importxmlStringAttr( root , MetaEnv.PROPERTY_ENVKEY );
		envKey = matcher.matchEnvBefore( env , envKey , env.ID , entities.entityAppEnvPrimary , MetaEnv.PROPERTY_ENVKEY , null );
		MatchItem ENVKEY = resources.matchResource( envKey , DBEnumResourceType.SSH );
		matcher.matchEnvDone( ENVKEY );
		
		boolean DISTR_REMOTE = entity.importxmlBooleanAttr( root , MetaEnv.PROPERTY_DISTR_REMOTE , false );
		MatchItem DISTR_ACCOUNT = null;
		String DISTR_PATH = "";
		
		if( DISTR_REMOTE ) {
			String account = entity.importxmlStringAttr( root , MetaEnv.PROPERTY_DISTR_HOSTLOGIN );
			account = matcher.matchEnvBefore( env , account , env.ID , entities.entityAppEnvPrimary , MetaEnv.PROPERTY_DISTR_HOSTLOGIN , null );
			DISTR_ACCOUNT = infra.matchAccountByHostlogin( envKey );
			matcher.matchEnvDone( DISTR_ACCOUNT );
			DISTR_PATH = entity.importxmlStringAttr( root , MetaEnv.PROPERTY_DISTR_PATH );
		}
		
		// primary
		env.setEnvPrimary(
				name ,
				entity.importxmlStringAttr( root , MetaEnv.PROPERTY_DESC ) ,
				DBEnumEnvType.getValue( entity.importxmlEnumAttr( root , MetaEnv.PROPERTY_ENVTYPE ) , true ) ,
				BASELINE ,
				entity.importxmlBooleanAttr( root , MetaEnv.PROPERTY_OFFLINE , false ) ,
				ENVKEY ,
				DISTR_REMOTE ,
				DISTR_ACCOUNT , 
				DISTR_PATH );
		modifyEnv( c , storage , env , true );
		
		// custom
		DBSettings.importxml( loader , root , ops , false , true , env.EV );
		
		// extra
		DBSettings.importxmlApp( loader , root , ops , env.EV , DBEnumParamEntityType.ENV_EXTRA );
		env.scatterExtraProperties();
	}

	private static void importxmlSegments( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_SEGMENT );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaEnvSegment sg = DBMetaEnvSegment.importxml( loader , storage , env , node );
			env.addSegment( sg );
		}
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , MetaEnv env , Document doc , Element root ) throws Exception {
		exportxmlMain( loader , storage , env , doc , root );
		exportxmlSegments( loader , storage , env , doc , root );
		DBMetaDump.exportxmlAll( loader , storage , env , doc , root );
	}

	private static void exportxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , Document doc , Element root ) throws Exception {
		DBConnection c = loader.getConnection();
		ObjectProperties ops = env.getProperties();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppEnvPrimary;
		ProductEnvs envs = storage.getEnviroments();
		EngineResources resources = loader.getResources();
		EngineInfrastructure infra = loader.getInfrastructure();
		
		int version = c.getCurrentEnvironmentVersion( env );
		Common.xmlSetElementAttr( doc , root , ATTR_VERSION , "" + version );
		
		// primary
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( env.NAME ) ,
				entity.exportxmlString( env.DESC ) ,
				entity.exportxmlEnum( env.ENV_TYPE ) ,
				entity.exportxmlString( envs.getProductEnvName( env.getBaselineMatchItem() ) ) ,
				entity.exportxmlBoolean( env.OFFLINE ) ,
				entity.exportxmlString( resources.getResourceName( env.getEnvKeyMatchItem() ) ) ,
				entity.exportxmlBoolean( env.DISTR_REMOTE ) ,
				entity.exportxmlString( infra.getHostAccountName( env.getDistrAccountMatchItem() ) ) ,
				entity.exportxmlString( env.DISTR_PATH )
		} , true );
		
		// custom settings
		DBSettings.exportxmlCustomEntity( loader , doc , root , ops );
		
		// core settings
		DBSettings.exportxml( loader , doc , root , ops , true , false , true , DBEnumParamEntityType.ENV_EXTRA );
	}
	
	private static void exportxmlSegments( EngineLoader loader , ProductMeta storage , MetaEnv env , Document doc , Element root ) throws Exception {
		for( String name : env.getSegmentNames() ) {
			MetaEnvSegment sg = env.findSegment( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_SEGMENT );
			DBMetaEnvSegment.exportxml( loader , storage , env , sg , doc , node );
		}
	}
	
	private static void modifyEnvMatch( DBConnection c , ProductMeta storage , MetaEnv env ) throws Exception {
		MatchItem item = env.getBaselineMatchItem();
		if( !item.MATCHED )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENV_MATCHBASELINE2 , new String[] { EngineDB.getInteger( env.ID ) , EngineDB.getInteger( item.FKID ) } ) )
			Common.exitUnexpected();
	}
	
	private static void modifyEnv( DBConnection c , ProductMeta storage , MetaEnv env , boolean insert ) throws Exception {
		if( !insert )
			DBNames.updateName( c , storage.ID , env.NAME , env.ID , DBEnumParamEntityType.ENV_PRIMARY );
		
		env.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppEnvPrimary , env.ID , env.EV , new String[] {
				EngineDB.getObject( storage.ID ) ,
				EngineDB.getObject( env.TRANSITION_META_ID ) ,
				EngineDB.getBoolean( env.MATCHED ) ,
				EngineDB.getString( env.NAME ) ,
				EngineDB.getString( env.DESC ) ,
				EngineDB.getEnum( env.ENV_TYPE ) ,
				EngineDB.getMatchId( env.getBaselineMatchItem() ) ,
				EngineDB.getMatchName( env.getBaselineMatchItem() ) ,
				EngineDB.getBoolean( env.OFFLINE ) ,
				EngineDB.getMatchId( env.getEnvKeyMatchItem() ) ,
				EngineDB.getMatchName( env.getEnvKeyMatchItem() ) ,
				EngineDB.getBoolean( env.DISTR_REMOTE ) ,
				EngineDB.getMatchId( env.getDistrAccountMatchItem() ) ,
				EngineDB.getMatchName( env.getDistrAccountMatchItem() ) ,
				EngineDB.getString( env.DISTR_PATH )
				} , insert );
	}
	
	public static void loaddbProductEnvs( EngineLoader loader , ProductMeta storage , ProductEnvs envs ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		PropertyEntity entity = entities.entityAppEnvPrimary;
		EngineResources resources = loader.getResources();
		EngineInfrastructure infra = loader.getInfrastructure();
		MetaProductSettings settings = storage.getSettings();

		List<MetaEnv> list = new LinkedList<MetaEnv>();
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID1 , new String[] { 
				EngineDB.getInteger( storage.ID ) 
				} );
		try {
			while( rs.next() ) {
				MetaEnv env = new MetaEnv( storage , storage.meta , envs );
				env.ID = entity.loaddbId( rs );
				env.EV = entity.loaddbVersion( rs );
				
				ObjectProperties ops = entities.createMetaEnvProps( env.ID , settings.ops );
				env.createSettings( ops );

				// match baseline later
				MatchItem BASELINE = entity.loaddbMatchItem( rs , DBEnvData.FIELD_ENV_BASELINE_ID , MetaEnv.PROPERTY_BASELINE );
				
				// set primary 
				MatchItem ENVKEY = entity.loaddbMatchItem( rs , DBEnvData.FIELD_ENV_ENVKEY_ID , MetaEnv.PROPERTY_ENVKEY );
				resources.matchResource( ENVKEY , DBEnumResourceType.SSH );
				matcher.matchEnvDone( ENVKEY , env , env.ID , entity , MetaEnv.PROPERTY_ENVKEY , null );
				
				MatchItem DISTACCOUNT = entity.loaddbMatchItem( rs , DBEnvData.FIELD_ENV_REMOTE_ACCOUNT_ID , MetaEnv.PROPERTY_DISTR_HOSTLOGIN );
				infra.matchAccount( DISTACCOUNT );
				matcher.matchEnvDone( DISTACCOUNT , env , env.ID , entity , MetaEnv.PROPERTY_DISTR_HOSTLOGIN , null );
				
				env.setEnvPrimary(
						entity.loaddbString( rs , MetaEnv.PROPERTY_NAME ) ,
						entity.loaddbString( rs , MetaEnv.PROPERTY_DESC ) ,
						DBEnumEnvType.getValue( entity.loaddbEnum( rs , MetaEnv.PROPERTY_ENVTYPE ) , true ) ,
						BASELINE ,
						entity.loaddbBoolean( rs , MetaEnv.PROPERTY_OFFLINE ) ,
						ENVKEY ,
						entity.loaddbBoolean( rs , MetaEnv.PROPERTY_DISTR_REMOTE ) ,
						DISTACCOUNT ,
						entity.loaddbString( rs , MetaEnv.PROPERTY_DISTR_PATH )
						);
				
				env.setTransition( entity.loaddbObject( rs , DBEnvData.FIELD_ENV_TRANSITION_META_ID ) );
				
				list.add( env );
			}
		}
		finally {
			c.closeQuery();
		}
		
		// load env data by env
		List<MetaEnv> ready = new LinkedList<MetaEnv>();
		for( MetaEnv env : list ) {
			try {
				loader.trace( "load env=" + env.NAME + " ..." );
				loaddbEnvData( loader , storage , env );
				ready.add( env );
			}
			catch( Throwable e ) {
				loader.log( "unable to load environment=" + env.NAME , e );
				env.deleteObject();
			}
		}
		
		// match baselines
		for( MetaEnv env : ready ) {
			matchBaseline( loader , storage , env );
			if( env.checkMatched() ) {
				loader.trace( "successfully matched env=" + env.NAME );
				env.refreshProperties();
			}
			else
				loader.trace( "match failed env=" + env.NAME );
			envs.addEnv( env );
		}
	}

	public static void loaddbEnvData( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ObjectProperties ops = env.getProperties();
		DBSettings.loaddbCustomEntity( c , ops , true );
		ops.createCustom();
		DBSettings.loaddbValues( loader , ops );
		env.scatterExtraProperties();
		
		DBMetaEnvSegment.loaddb( loader , storage , env );
		DBMetaDump.loaddbAll( loader , storage , env );
	}
	
	public static void setMatched( EngineLoader loader , MetaEnv env , boolean matched ) throws Exception {
		DBConnection c = loader.getConnection();
		if( !c.modify( DBQueries.MODIFY_ENV_SETSTATUS2 , new String[] { 
				EngineDB.getInteger( env.ID ) ,
				EngineDB.getBoolean( matched )
				} ) )
			Common.exitUnexpected();
	}

	public static void deleteDatabaseSchema( EngineTransaction transaction , ProductMeta storage , MetaDatabaseSchema schema ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		for( MetaEnv env : envs.getEnvs() ) {
			for( MetaEnvSegment sg : env.getSegments() ) {
				for( MetaEnvServer server : sg.getServers() ) {
					MetaEnvServerDeployment deployment = server.findDatabaseSchemaDeployment( schema );
					if( deployment != null )
						DBMetaEnvServerDeployment.deleteDeployment( transaction , storage , env , server , deployment );
				}
			}
		}
	}

	public static void deleteBinaryItem( EngineTransaction transaction , ProductMeta storage , MetaDistrBinaryItem item ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		for( MetaEnv env : envs.getEnvs() ) {
			for( MetaEnvSegment sg : env.getSegments() ) {
				for( MetaEnvServer server : sg.getServers() ) {
					MetaEnvServerDeployment deployment = server.findBinaryItemDeployment( item );
					if( deployment != null )
						DBMetaEnvServerDeployment.deleteDeployment( transaction , storage , env , server , deployment );
				}
			}
		}
	}
	
	public static void deleteConfItem( EngineTransaction transaction , ProductMeta storage , MetaDistrConfItem item ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		for( MetaEnv env : envs.getEnvs() ) {
			for( MetaEnvSegment sg : env.getSegments() ) {
				for( MetaEnvServer server : sg.getServers() ) {
					MetaEnvServerDeployment deployment = server.findConfItemDeployment( item );
					if( deployment != null )
						DBMetaEnvServerDeployment.deleteDeployment( transaction , storage , env , server , deployment );
				}
			}
		}
	}

	public static void deleteComponent( EngineTransaction transaction , ProductMeta storage , MetaDistrComponent comp ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		for( MetaEnv env : envs.getEnvs() ) {
			for( MetaEnvSegment sg : env.getSegments() ) {
				for( MetaEnvServer server : sg.getServers() ) {
					MetaEnvServerDeployment deployment = server.findComponentDeployment( comp );
					if( deployment != null )
						DBMetaEnvServerDeployment.deleteDeployment( transaction , storage , env , server , deployment );
				}
			}
		}
	}
	
	public static MetaEnv createEnv( EngineTransaction transaction , ProductMeta storage , String name , String desc , DBEnumEnvType envType ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = transaction.getEntities();
		ProductEnvs envs = storage.getEnviroments();
		
		MetaEnv env = new MetaEnv( storage , storage.meta , envs );
		env.ID = DBNames.getNameIndex( c , storage.ID , name , DBEnumParamEntityType.ENV_PRIMARY );
		
		transaction.trace( "create meta env, object=" + env.objectId + ", name=" + name + ", id=" + env.ID );

		// create settings
		MetaProductSettings settings = storage.getSettings();
		ObjectProperties ops = entities.createMetaEnvProps( env.ID , settings.getParameters() );
		env.createSettings( ops );
		
		env.setEnvPrimary( name , desc , envType , null , true , null , false , null , "" );
		env.refreshPrimaryProperties();
		env.scatterExtraProperties();
		modifyEnv( c , storage , env , true );

		// create empty custom entity
		DBSettings.savedbEntityCustom( c , ops , env.EV );
		
		envs.addEnv( env );
		return( env );
	}

	public static void deleteEnv( EngineTransaction transaction , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = transaction.getConnection();
		ProductEnvs envs = storage.getEnviroments();
		
		DBEnvData.dropEnvData( c , env );
		envs.deleteEnv( transaction , env );
	}

	public static void setEnvOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		env.setOffline( offline );
		
		modifyEnv( c , storage , env , false );
	}

	public static void setEnvBaseline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , Integer envBaselineId ) throws Exception {
		DBConnection c = transaction.getConnection();
		env.setBaseline( MatchItem.create( envBaselineId ) );
		env.refreshPrimaryProperties();
		
		modifyEnv( c , storage , env , false );
	}

	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = transaction.getConnection();
		ObjectProperties ops = env.getProperties();
		int version = c.getNextEnvironmentVersion( env );
		DBSettings.savedbPropertyValues( transaction , ops , false , true , version );
		ops.recalculateChildProperties();
	}
	
	public static void updateExtraProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = transaction.getConnection();
		ObjectProperties ops = env.getProperties();
		int version = c.getNextEnvironmentVersion( env );
		DBSettings.savedbPropertyValues( transaction , ops , true , false , version , DBEnumParamEntityType.ENV_EXTRA );
		ops.recalculateChildProperties();
	}
	
}
