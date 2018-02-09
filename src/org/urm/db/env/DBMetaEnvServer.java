package org.urm.db.env;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineMatcher;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Node;

public class DBMetaEnvServer {

	public static String ELEMENT_NODE = "node";
	public static String ELEMENT_PLATFORM = "platform";
	public static String ELEMENT_DEPLOY = "deploy";

	public static MetaEnvServer importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		MetaEnvServer server = new MetaEnvServer( storage.meta , sg );
		
		loader.trace( "import meta env segment object, name=" + env.NAME );

		importxmlMain( loader , storage , env , server , root );
		importxmlNodes( loader , storage , env , server , root );
		importxmlBase( loader , storage , env , server , root );
		
 		return( server );
	}

	public static void importxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		MetaDatabase database = storage.getDatabase();
		EngineBase base = loader.getBase();
		
		// identify
		PropertyEntity entity = entities.entityAppServerPrimary;
		String NAME = entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_NAME );
		server.ID = DBNames.getNameIndex( c , server.sg.ID , NAME , DBEnumObjectType.ENVIRONMENT_SERVER );

		loader.trace( "import meta env server object, name=" + NAME );
		
		// create settings
		ObjectProperties ops = entities.createMetaEnvServerProps( server.sg.getProperties() );
		server.createSettings( ops );
		
		// primary match (baseline match is postponed)
		MatchItem BASELINE = MatchItem.create( entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_BASELINE ) );
		
		String admSchema = entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_ADMSCHEMA );
		admSchema = matcher.matchEnvBefore( env , admSchema , server.ID , entities.entityAppServerPrimary , MetaEnvServer.PROPERTY_ADMSCHEMA , null );
		MatchItem ADMSCHEMA = database.matchSchema( admSchema );
		matcher.matchEnvDone( ADMSCHEMA );
		
		String baseItem = entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_BASEITEM );
		baseItem = matcher.matchEnvBefore( env , baseItem , server.ID , entities.entityAppServerPrimary , MetaEnvServer.PROPERTY_BASEITEM , null );
		MatchItem BASEITEM = base.matchBaseItem( baseItem );
		matcher.matchEnvDone( BASEITEM );
		
		// primary
		server.setServerPrimary(
				NAME ,
				entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_DESC ) ,
				DBEnumServerRunType.getValue( entity.importxmlEnumAttr( root , MetaEnvServer.PROPERTY_SERVERRUNTYPE ) , true ) ,
				DBEnumServerAccessType.getValue( entity.importxmlEnumAttr( root , MetaEnvServer.PROPERTY_SERVERACCESSTYPE ) , true ) ,
				DBEnumOSType.getValue( entity.importxmlEnumAttr( root , MetaEnvServer.PROPERTY_OSTYPE ) , true ) ,
				BASELINE ,
				entity.importxmlBooleanAttr( root , MetaEnv.PROPERTY_OFFLINE , false ) ,
				DBEnumDbmsType.getValue( entity.importxmlEnumAttr( root , MetaEnvServer.PROPERTY_DBMSTYPE ) , false ) ,
				ADMSCHEMA ,
				BASEITEM );
		modifyServer( c , storage , env , server , true );
		
		// custom
		DBSettings.importxml( loader , root , ops , server.ID , storage.ID , false , true , env.EV );
		
		// extra
		DBSettings.importxmlApp( loader , root , ops , server.ID , server.EV , DBEnumParamEntityType.ENV_SERVER_EXTRA );
		server.scatterExtraProperties();
	}
	
	public static void importxmlNodes( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
	}
	
	public static void importxmlBase( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
	}
	
	private static void modifyServer( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvServer server , boolean insert ) throws Exception {
		if( !insert )
			DBNames.updateName( c , server.sg.ID , server.NAME , server.ID , DBEnumObjectType.ENVIRONMENT_SERVER );
		
		server.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppServerPrimary , server.ID , server.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getObject( server.sg.ID ) ,
				EngineDB.getString( server.NAME ) ,
				EngineDB.getString( server.DESC ) ,
				EngineDB.getEnum( server.SERVERRUN_TYPE ) ,
				EngineDB.getEnum( server.SERVERACCESS_TYPE ) ,
				EngineDB.getEnum( server.OS_TYPE ) ,
				EngineDB.getString( server.SYSNAME ) ,
				EngineDB.getMatchId( server.getBaselineMatchItem() ) ,
				EngineDB.getMatchName( server.getBaselineMatchItem() ) ,
				EngineDB.getBoolean( server.OFFLINE ) ,
				EngineDB.getEnum( server.DBMS_TYPE ) ,
				EngineDB.getMatchId( server.getAdmSchemaMatchItem() ) ,
				EngineDB.getMatchName( server.getAdmSchemaMatchItem() ) ,
				EngineDB.getMatchId( server.getBaseItemMatchItem() ) ,
				EngineDB.getMatchName( server.getBaseItemMatchItem() )
				} , insert );
	}
	
	public static MetaEnvServer createServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , String name , String desc , DBEnumOSType osType , DBEnumServerRunType runType , DBEnumServerAccessType accessType , String sysname , DBEnumDbmsType dbmsType , Integer admSchema ) throws Exception {
		Common.exitUnexpected();
		return( null );
	}
	
	public static void modifyServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , String name , String desc , DBEnumOSType osType , DBEnumServerRunType runType , DBEnumServerAccessType accessType , String sysname , DBEnumDbmsType dbmsType , Integer admSchema ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void deleteServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setServerBaseline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , Integer baselineId ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setServerBaseItem( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , Integer baseItemId ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setServerOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , boolean offline ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setDeployments( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , MetaEnvServerDeployment[] deployments ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void updateExtraProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		Common.exitUnexpected();
	}
	
}
