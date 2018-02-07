package org.urm.db.env;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumResourceType;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.DBEnumEnvType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineMatcher;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnv {

	public static String ELEMENT_SEGMENT = "segment";
	
	public static MetaEnv importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		MetaEnv env = new MetaEnv( storage , storage.meta );
		
		loader.trace( "import meta env object, id=" + env.objectId );

		importxmlMain( loader , storage , env , root );
		importxmlSegments( loader , storage , env , root );
		importxmlResolve( loader , storage , env , root );
		
		envs.addEnv( env );
		return( env );
	}
	
	private static void importxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		EngineResources resources = loader.getResources();
		EngineInfrastructure infra = loader.getInfrastructure();
		
		// identify
		PropertyEntity entity = entities.entityAppEnvPrimary;
		String NAME = entity.importxmlStringAttr( root , MetaEnv.PROPERTY_NAME );
		env.ID = DBNames.getNameIndex( c , storage.ID , NAME , DBEnumObjectType.ENVIRONMENT );

		// create settings
		MetaProductSettings settings = storage.getSettings();
		ObjectProperties ops = entities.createMetaEnvProps( settings.ops );
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
			DISTR_ACCOUNT = infra.matchAccount( envKey );
			matcher.matchEnvDone( DISTR_ACCOUNT );
			DISTR_PATH = entity.importxmlStringAttr( root , MetaEnv.PROPERTY_DISTR_PATH );
		}
		
		// primary
		env.setEnvPrimary(
				NAME ,
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
		DBSettings.importxml( loader , root , ops , env.ID , env.ID , false , true , env.EV );
		
		// extra
		DBSettings.importxml( loader , root , ops , env.ID , env.ID , true , false , env.EV , DBEnumParamEntityType.ENV_EXTRA );
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
	
	private static void importxmlResolve( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , MetaEnv env , Document doc , Element root ) throws Exception {
	}
	
	private static void modifyEnv( DBConnection c , ProductMeta storage , MetaEnv env , boolean insert ) throws Exception {
		if( !insert )
			DBNames.updateName( c , storage.ID , env.NAME , env.ID , DBEnumObjectType.ENVIRONMENT );
		
		env.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppEnvPrimary , env.ID , env.EV , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getString( null ) ,
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
	
	public static void setMatched( EngineLoader loader , MetaEnv env , boolean matched ) throws Exception {
		DBConnection c = loader.getConnection();
		if( !c.modify( DBQueries.MODIFY_META_SETSTATUS2 , new String[] { 
				EngineDB.getInteger( env.ID ) ,
				EngineDB.getBoolean( matched )
				} ) )
			Common.exitUnexpected();
	}

	public static void deleteDatabaseSchema( EngineTransaction transaction , ProductMeta storage , MetaDatabaseSchema schema ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		envs.removeDatabaseSchemaFromEnvironments( schema );
		Common.exitUnexpected();
	}

	public static void deleteBinaryItem( EngineTransaction transaction , ProductMeta storage , MetaDistrBinaryItem item ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		envs.removeBinaryItemFromEnvironments( item );
		Common.exitUnexpected();
	}
	
	public static void deleteConfItem( EngineTransaction transaction , ProductMeta storage , MetaDistrConfItem item ) throws Exception {
		ProductEnvs envs = storage.getEnviroments();
		envs.removeConfItemFromEnvironments( item );
		Common.exitUnexpected();
	}

	public static MetaEnv createEnv( EngineTransaction transaction , ProductMeta storage , String name , DBEnumEnvType envType ) throws Exception {
		Common.exitUnexpected();
		return( null );
	}

	public static void deleteEnv( EngineTransaction transaction , ProductMeta storage , MetaEnv env ) throws Exception {
		Common.exitUnexpected();
	}

	public static void setEnvOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , boolean offline ) throws Exception {
		Common.exitUnexpected();
	}

	public static void setEnvBaseline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , Integer envBaselineId ) throws Exception {
		Common.exitUnexpected();
	}

	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void updateExtraProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env ) throws Exception {
		Common.exitUnexpected();
	}
	
}
