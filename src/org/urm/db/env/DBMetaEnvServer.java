package org.urm.db.env;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineMatcher;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvServer {

	public static String ELEMENT_NODE = "node";
	public static String ELEMENT_DEPLOY = "deploy";
	public static String ELEMENT_DEPENDENCIES = "dependencies";
	public static String ELEMENT_DEPSERVER = "server";
	public static String ELEMENT_BASE = "base";
	public static String ATTR_DEPSERVER_NAME = "name";
	public static String ATTR_DEPSERVER_TYPE = "type";

	public static MetaEnvServer importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		MetaEnvServer server = new MetaEnvServer( storage.meta , sg );
		
		importxmlMain( loader , storage , env , server , root );
		importxmlNodes( loader , storage , env , server , root );
		importxmlBase( loader , storage , env , server , root );
		importxmlDeployments( loader , storage , env , server , root );
		
 		return( server );
	}

	public static void importxmlDependencies( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppServerPrimary;
		
		String NAME = entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_NAME );
		MetaEnvServer server = sg.getServer( NAME );
		
		Node deps = ConfReader.xmlGetFirstChild( root , ELEMENT_DEPENDENCIES );
		if( deps == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_DEPSERVER );
		if( items == null )
			return;
		
		for( Node node : items ) {
			String depName = ConfReader.getAttrValue( node , ATTR_DEPSERVER_NAME );
			DBEnumServerDependencyType type = DBEnumServerDependencyType.getValue( ConfReader.getAttrValue( node , ATTR_DEPSERVER_TYPE ) , true );
			
			MetaEnvServer depServer = sg.getServer( depName );
			server.addDependencyServer( depServer , type );
			addDependencyServer( c , storage , env , server , depServer , type );
		}
	}
	
	public static void matchBaseline( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , MetaEnvSegment baselineSegment ) throws Exception {
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		DBConnection c = loader.getConnection();
		
		MatchItem BASELINE = server.getBaselineMatchItem();
		if( BASELINE != null ) {
			String value = matcher.matchEnvBefore( env , BASELINE.FKNAME , server.ID , entities.entityAppServerPrimary , MetaEnvServer.PROPERTY_BASELINE , null );
			MetaEnvServer baseline = baselineSegment.findServer( value );
			if( baseline != null ) {
				BASELINE.match( baseline.ID );
				modifyServerMatch( c , storage , env , server );
			}
			matcher.matchEnvDone( BASELINE );
		}
	}
	
	private static void importxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		MetaDatabase database = storage.getDatabase();
		EngineBase base = loader.getBase();
		
		// identify
		PropertyEntity entity = entities.entityAppServerPrimary;
		String NAME = entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_NAME );
		server.ID = DBNames.getNameIndex( c , server.sg.ID , NAME , DBEnumParamEntityType.ENV_SERVER_PRIMARY );

		loader.trace( "import meta env server object, name=" + NAME );
		
		// create settings
		ObjectProperties ops = entities.createMetaEnvServerProps( server.ID , server.sg.getProperties() );
		server.createSettings( ops );
		
		// primary match (baseline match is postponed)
		MatchItem BASELINE = MatchItem.create( entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_BASELINE ) );
		
		String admSchema = entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_ADMSCHEMA );
		admSchema = matcher.matchEnvBefore( env , admSchema , server.ID , entities.entityAppServerPrimary , MetaEnvServer.PROPERTY_ADMSCHEMA , null );
		MatchItem ADMSCHEMA = database.getSchemaMatchItem( null , admSchema );
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
				entity.importxmlStringAttr( root , MetaEnvServer.PROPERTY_SYSNAME ) ,
				BASELINE ,
				entity.importxmlBooleanAttr( root , MetaEnv.PROPERTY_OFFLINE , false ) ,
				DBEnumDbmsType.getValue( entity.importxmlEnumAttr( root , MetaEnvServer.PROPERTY_DBMSTYPE ) , false ) ,
				ADMSCHEMA ,
				BASEITEM );
		modifyServer( c , storage , env , server , true );
		
		// custom
		DBSettings.importxml( loader , root , ops , false , true , env.EV );
		
		// extra
		DBSettings.importxmlApp( loader , root , ops , server.EV , DBEnumParamEntityType.ENV_SERVER_EXTRA );
		server.scatterExtraProperties();
	}
	
	private static void importxmlNodes( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_NODE );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaEnvServerNode sn = DBMetaEnvServerNode.importxml( loader , storage , env , server , node );
			server.addNode( sn );
		}
	}
	
	private static void importxmlBase( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		
		ObjectProperties base = entities.createMetaEnvServerBaseProps( server.getProperties() );
		server.createBaseSettings( base );
		
		MatchItem BASEITEM = server.getBaseItemMatchItem();
		if( BASEITEM == null )
			return;
		
		BaseItem baseItem = server.getBaseItem();
		
		loader.trace( "import base item properties, name=" + baseItem.NAME );
		
		Node baseNode = ConfReader.xmlGetFirstChild( root , ELEMENT_BASE );
		if( baseNode == null )
			Common.exitUnexpected();
		
		// base item and base custom
		DBSettings.importxml( loader , root , base , false , true , env.EV );
		base.recalculateProperties();
	}
	
	private static void importxmlDeployments( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_DEPLOY );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaEnvServerDeployment deployment = DBMetaEnvServerDeployment.importxml( loader , storage , env , server , node );
			server.addDeployment( deployment );
		}
	}
	
	private static void modifyServer( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvServer server , boolean insert ) throws Exception {
		if( !insert )
			DBNames.updateName( c , server.sg.ID , server.NAME , server.ID , DBEnumParamEntityType.ENV_SERVER_PRIMARY );
		
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
	
	private static void modifyServerMatch( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		MatchItem item = server.getBaselineMatchItem();
		if( !item.MATCHED )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVSERVER_MATCHBASELINE2 , new String[] { EngineDB.getInteger( server.ID ) , EngineDB.getInteger( item.FKID ) } ) )
			Common.exitUnexpected();
	}
	
	private static void addDependencyServer( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvServer server , MetaEnvServer depServer , DBEnumServerDependencyType type ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextEnvironmentVersion( env );
		DBEngineEntities.modifyAppEntity( c , entities.entityAppServerDependency , version , new String[] { 
				EngineDB.getInteger( server.ID ) , 
				EngineDB.getInteger( depServer.ID ) ,
				EngineDB.getInteger( env.ID ) ,
				EngineDB.getEnum( type )
				} , true );
	}
	
	public static void loaddb( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppServerPrimary;
		MetaDatabase database = storage.getDatabase();
		EngineBase base = loader.getBase();
		EngineMatcher matcher = loader.getMatcher();
		
		// load segments
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				MetaEnvSegment sg = env.getSegment( entity.loaddbObject( rs , DBEnvData.FIELD_SERVER_SEGMENT_ID ) );
				MetaEnvServer server = new MetaEnvServer( storage.meta , sg );
				server.ID = entity.loaddbId( rs );
				server.EV = entity.loaddbVersion( rs );

				ObjectProperties ops = entities.createMetaEnvServerProps( server.ID , sg.getProperties() );
				server.createSettings( ops );
				ObjectProperties opsBase = entities.createMetaEnvServerBaseProps( ops );
				server.createBaseSettings( opsBase );
				
				// match baseline later
				MatchItem BASELINE = entity.loaddbMatchItem( rs , DBEnvData.FIELD_SERVER_BASELINE_ID , MetaEnvServer.PROPERTY_BASELINE );
				
				// set primary 
				MatchItem ADMSCHEMA = entity.loaddbMatchItem( rs , DBEnvData.FIELD_SERVER_ADMSCHEMA_ID , MetaEnvServer.PROPERTY_ADMSCHEMA );
				database.matchSchema( ADMSCHEMA );
				matcher.matchEnvDone( ADMSCHEMA , env , server.ID , entity , MetaEnvServer.PROPERTY_ADMSCHEMA , null );
				
				MatchItem BASEITEM = entity.loaddbMatchItem( rs , DBEnvData.FIELD_SERVER_BASEITEM_ID , MetaEnvServer.PROPERTY_BASEITEM );
				base.matchBaseItem( BASEITEM );
				matcher.matchEnvDone( BASEITEM , env , server.ID , entity , MetaEnvServer.PROPERTY_BASEITEM , null );
				
				server.setServerPrimary(
						entity.loaddbString( rs , MetaEnvServer.PROPERTY_NAME ) ,
						entity.loaddbString( rs , MetaEnvServer.PROPERTY_DESC ) ,
						DBEnumServerRunType.getValue( entity.loaddbEnum( rs , MetaEnvServer.PROPERTY_SERVERRUNTYPE ) , true ) ,
						DBEnumServerAccessType.getValue( entity.loaddbEnum( rs , MetaEnvServer.PROPERTY_SERVERACCESSTYPE ) , true ) ,
						DBEnumOSType.getValue( entity.loaddbEnum( rs , MetaEnvServer.PROPERTY_OSTYPE ) , true ) ,
						entity.loaddbString( rs , MetaEnvServer.PROPERTY_SYSNAME ) ,
						BASELINE ,
						entity.loaddbBoolean( rs , MetaEnvServer.PROPERTY_OFFLINE ) ,
						DBEnumDbmsType.getValue( entity.loaddbEnum( rs , MetaEnvServer.PROPERTY_DBMSTYPE ) , false ) ,
						ADMSCHEMA ,
						BASEITEM
						);
				
				sg.addServer( server );
			}
		}
		finally {
			c.closeQuery();
		}
		
		// properties
		for( MetaEnvSegment sg : env.getSegments() ) {
			for( MetaEnvServer server : sg.getServers() ) {
				ObjectProperties ops = server.getProperties();
				DBSettings.loaddbValues( loader , ops );
				server.scatterExtraProperties();
				
				ObjectProperties opsBase = server.getBaseProperties();
				DBSettings.loaddbValues( loader , opsBase );
			}
		}
		
		DBMetaEnvServerDeployment.loaddb( loader , storage , env );
	}

	public static void exportxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Document doc , Element root ) throws Exception {
		exportxmlMain( loader , storage , env , server , doc , root );
		exportxmlNodes( loader , storage , env , server , doc , root );
		exportxmlBase( loader , storage , env , server , doc , root );
		exportxmlDeployments( loader , storage , env , server , doc , root );
		exportxmlDependencies( loader , storage , env , server , doc , root );
	}

	private static void exportxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Document doc , Element root ) throws Exception {
		ObjectProperties ops = server.getProperties();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppServerPrimary;
		EngineBase base = loader.getBase();
		MetaDatabase database = storage.getDatabase();
		
		// primary
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( server.NAME ) ,
				entity.exportxmlString( server.DESC ) ,
				entity.exportxmlEnum( server.SERVERRUN_TYPE ) ,
				entity.exportxmlEnum( server.SERVERACCESS_TYPE ) ,
				entity.exportxmlEnum( server.OS_TYPE ) ,
				entity.exportxmlString( server.SYSNAME ) ,
				entity.exportxmlString( server.sg.getServerName( server.getBaselineMatchItem() ) ) ,
				entity.exportxmlBoolean( server.OFFLINE ) ,
				entity.exportxmlEnum( server.DBMS_TYPE ) ,
				entity.exportxmlString( database.getSchemaName( server.getAdmSchemaMatchItem() ) ) ,
				entity.exportxmlString( base.getItemName( server.getBaseItemMatchItem() ) )
		} , true );
		
		// custom settings
		DBSettings.exportxmlCustomEntity( loader , doc , root , ops );
		
		// extra settings
		DBSettings.exportxml( loader , doc , root , ops , true , false , true , DBEnumParamEntityType.ENV_SERVER_EXTRA );
	}
	
	private static void exportxmlNodes( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Document doc , Element root ) throws Exception {
		for( MetaEnvServerNode sn : server.getNodes() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_NODE );
			DBMetaEnvServerNode.exportxml( loader , storage , env , sn , doc , node );
		}
	}
	
	private static void exportxmlBase( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Document doc , Element root ) throws Exception {
		if( server.hasBaseItem() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_BASE );
			ObjectProperties base = server.getBaseProperties();
			DBSettings.exportxmlCustomEntity( loader , doc , node , base );
		}
	}
	
	private static void exportxmlDeployments( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Document doc , Element root ) throws Exception {
		for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DEPLOY );
			DBMetaEnvServerDeployment.exportxml( loader , storage , env , deployment , doc , node );
		}
	}
	
	private static void exportxmlDependencies( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Document doc , Element root ) throws Exception {
		if( !server.hasDependencies() )
			return;
		
		Element deps = Common.xmlCreateElement( doc , root , ELEMENT_DEPENDENCIES );
		
		MetaEnvServer dep = null;
		dep = server.getNlbServer();
		if( dep != null ) {
			Element node = Common.xmlCreateElement( doc , deps , ELEMENT_DEPSERVER );
			Common.xmlSetElementAttr( doc , node , ATTR_DEPSERVER_NAME , dep.NAME );
			Common.xmlSetElementAttr( doc , node , ATTR_DEPSERVER_TYPE , DBEnumServerDependencyType.NLB.name().toLowerCase() );
		}
		
		dep = server.getProxyServer();
		if( dep != null ) {
			Element node = Common.xmlCreateElement( doc , deps , ELEMENT_DEPSERVER );
			Common.xmlSetElementAttr( doc , node , ATTR_DEPSERVER_NAME , dep.NAME );
			Common.xmlSetElementAttr( doc , node , ATTR_DEPSERVER_TYPE , DBEnumServerDependencyType.PROXY.name().toLowerCase() );
		}
		
		dep = server.getStaticServer();
		if( dep != null ) {
			Element node = Common.xmlCreateElement( doc , deps , ELEMENT_DEPSERVER );
			Common.xmlSetElementAttr( doc , node , ATTR_DEPSERVER_NAME , dep.NAME );
			Common.xmlSetElementAttr( doc , node , ATTR_DEPSERVER_TYPE , DBEnumServerDependencyType.STATIC.name().toLowerCase() );
		}
		
		for( MetaEnvServer sub : server.getSubordinateServers() ) {
			Element node = Common.xmlCreateElement( doc , deps , ELEMENT_DEPSERVER );
			Common.xmlSetElementAttr( doc , node , ATTR_DEPSERVER_NAME , sub.NAME );
			Common.xmlSetElementAttr( doc , node , ATTR_DEPSERVER_TYPE , DBEnumServerDependencyType.SUBORDINATE.name().toLowerCase() );
		}
	}
	
	public static MetaEnvServer createServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , String name , String desc , 
			DBEnumOSType osType , DBEnumServerRunType runType , DBEnumServerAccessType accessType , String sysname , DBEnumDbmsType dbmsType , Integer admSchema ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = transaction.getEntities();
		
		// identify
		MetaEnvServer server = new MetaEnvServer( storage.meta , sg );
		server.ID = DBNames.getNameIndex( c , server.sg.ID , name , DBEnumParamEntityType.ENV_SERVER_PRIMARY );

		transaction.trace( "create meta env server, name=" + name );
		
		// create settings
		ObjectProperties ops = entities.createMetaEnvServerProps( server.ID , server.sg.getProperties() );
		server.createSettings( ops );
		ObjectProperties opsBase = entities.createMetaEnvServerBaseProps( ops );
		server.createBaseSettings( opsBase );
		
		// primary
		server.setServerPrimary( name , desc , runType , accessType , osType , sysname , null , true , dbmsType , MatchItem.create( admSchema ) , null );
 		modifyServer( c , storage , env , server , true );
		server.scatterExtraProperties();
		sg.addServer( server );
		return( server );
	}
	
	public static void modifyServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , String name , String desc , DBEnumOSType osType , DBEnumServerRunType runType , DBEnumServerAccessType accessType , String sysname , DBEnumDbmsType dbmsType , Integer admSchema ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		server.modifyServer( name , desc , runType , accessType , osType , dbmsType , MatchItem.create( admSchema ) );
 		modifyServer( c , storage , env , server , false );
 		server.sg.updateServer( server );
	}
	
	public static void deleteServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		DBEngineEntities.deleteAppObject( c , entities.entityAppServerPrimary , server.ID , c.getNextEnvironmentVersion( env ) );
		server.sg.removeServer( server );
	}
	
	public static void setServerBaseline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , Integer baselineId ) throws Exception {
		DBConnection c = transaction.getConnection();
		server.setBaseline( MatchItem.create( baselineId ) );
		
		modifyServer( c , storage , env , server , false );
	}
	
	public static void setServerBaseItem( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , Integer baseItemId ) throws Exception {
		DBConnection c = transaction.getConnection();
		server.setBaseItem( MatchItem.create( baseItemId ) );
		
		modifyServer( c , storage , env , server , false );
	}
	
	public static void setServerOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		server.setOffline( offline );
		
		modifyServer( c , storage , env , server , false );
	}
	
	public static void setDeployments( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , MetaEnvServerDeployment[] deployments ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( !c.modify( DBQueries.MODIFY_ENV_CASCADESERVER_ALLDEPLOYMENTS1 , new String[] { EngineDB.getInteger( server.ID ) } ) )
			Common.exitUnexpected();
		
		server.clearDeployments();
		for( MetaEnvServerDeployment deployment : deployments ) {
			deployment = deployment.copy( storage.meta , server );
			server.addDeployment( deployment );
			DBMetaEnvServerDeployment.modifyDeployment( c , storage , env , server , deployment , true );
		}
	}

	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		DBConnection c = transaction.getConnection();
		ObjectProperties ops = server.getProperties();
		int version = c.getNextEnvironmentVersion( env );
		DBSettings.savedbPropertyValues( transaction , ops , false , true , version );
		ops.recalculateChildProperties();
	}
	
	public static void updateExtraProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server ) throws Exception {
		DBConnection c = transaction.getConnection();
		ObjectProperties ops = server.getProperties();
		int version = c.getNextEnvironmentVersion( env );
		DBSettings.savedbPropertyValues( transaction , ops , true , false , version , DBEnumParamEntityType.ENV_SERVER_EXTRA );
		ops.recalculateChildProperties();
	}
	
}
