package org.urm.db.env;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineMatcher;
import org.urm.meta.MatchItem;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvServerDeployment {

	public static String ATTR_BINARY = "distitem";
	public static String ATTR_CONF = "confitem";
	public static String ATTR_SCHEMA = "schema";
	
	public static MetaEnvServerDeployment importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		
		MetaEnvServerDeployment deployment = new MetaEnvServerDeployment( storage.meta , server );
		importxmlData( loader , storage , env , deployment , root );
		
		modifyDeployment( c , storage , env , deployment , true );
		
		return( deployment );
	}
	
	private static void importxmlData( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServerDeployment deployment , Node root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppServerDeployment;
		EngineMatcher matcher = loader.getMatcher();
		MetaDistr distr = storage.getDistr();
		MetaDatabase database = storage.getDatabase();
		
		DBEnumDeployModeType deployMode = DBEnumDeployModeType.getValue( entity.importxmlEnumAttr( root , MetaEnvServerDeployment.PROPERTY_DEPLOYMODE ) , false );
		String deployPath = entity.importxmlStringAttr( root , MetaEnvServerDeployment.PROPERTY_DEPLOYPATH );
		DBEnumNodeType nodeType = DBEnumNodeType.getValue( entity.importxmlEnumAttr( root , MetaEnvServerDeployment.PROPERTY_NODETYPE ) , false );
		
		String compName = entity.importxmlStringAttr( root , MetaEnvServerDeployment.PROPERTY_COMPONENT );
		if( !compName.isEmpty() ) {
			compName = matcher.matchEnvBefore( env , compName , deployment.ID , entity , MetaEnvServerDeployment.PROPERTY_COMPONENT , null );
			MatchItem COMP = distr.matchComponent( compName );
			matcher.matchEnvDone( COMP );
			
			deployment.create( DBEnumServerDeploymentType.COMP , COMP , null , null , null , deployMode , deployPath , "" , "" , nodeType );
			return;
		}
		
		String binaryName = entity.importxmlStringAttr( root , MetaEnvServerDeployment.PROPERTY_DISTITEM );
		if( !binaryName.isEmpty() ) {
			binaryName = matcher.matchEnvBefore( env , binaryName , deployment.ID , entity , MetaEnvServerDeployment.PROPERTY_DISTITEM , null );
			MatchItem BINARYITEM = distr.matchBinaryItem( binaryName );
			matcher.matchEnvDone( BINARYITEM );
			
			deployment.create( DBEnumServerDeploymentType.BINARY , null , BINARYITEM , null , null , deployMode , deployPath , "" , "" , nodeType );
			return;
		}
		
		String confName = entity.importxmlStringAttr( root , MetaEnvServerDeployment.PROPERTY_CONFITEM );
		if( !confName.isEmpty() ) {
			confName = matcher.matchEnvBefore( env , confName , deployment.ID , entity , MetaEnvServerDeployment.PROPERTY_CONFITEM , null );
			MatchItem CONFITEM = distr.matchConfItem( confName );
			matcher.matchEnvDone( CONFITEM );
			
			deployment.create( DBEnumServerDeploymentType.CONF , null , null , CONFITEM , null , deployMode , deployPath , "" , "" , nodeType );
			return;
		}

		String dbname = entity.importxmlStringAttr( root , MetaEnvServerDeployment.PROPERTY_DBNAME );
		String dbuser = entity.importxmlStringAttr( root , MetaEnvServerDeployment.PROPERTY_DBUSER );
		
		String schemaName = entity.importxmlStringAttr( root , MetaEnvServerDeployment.PROPERTY_SCHEMA );
		if( !schemaName.isEmpty() ) {
			schemaName = matcher.matchEnvBefore( env , schemaName , deployment.ID , entity , MetaEnvServerDeployment.PROPERTY_SCHEMA , null );
			MatchItem SCHEMA = database.matchSchema( schemaName );
			matcher.matchEnvDone( SCHEMA );
			
			deployment.create( DBEnumServerDeploymentType.SCHEMA , null , null , null , SCHEMA , deployMode , "" , dbname , dbuser , DBEnumNodeType.UNKNOWN );
			return;
		}

		Common.exitUnexpected();
	}
	
	private static void modifyDeployment( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvServerDeployment deployment , boolean insert ) throws Exception {
		if( insert )
			deployment.ID = c.getNextSequenceValue();
		
		deployment.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppServerDeployment , deployment.ID , deployment.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getObject( deployment.server.ID ) ,
				EngineDB.getEnum( deployment.SERVERDEPLOYMENT_TYPE ) ,
				EngineDB.getMatchId( deployment.getCompMatchItem() ) ,
				EngineDB.getMatchName( deployment.getCompMatchItem() ) ,
				EngineDB.getMatchId( deployment.getBinaryItemMatchItem() ) ,
				EngineDB.getMatchName( deployment.getBinaryItemMatchItem() ) ,
				EngineDB.getMatchId( deployment.getConfItemMatchItem() ) ,
				EngineDB.getMatchName( deployment.getConfItemMatchItem() ) ,
				EngineDB.getMatchId( deployment.getSchemaMatchItem() ) ,
				EngineDB.getMatchName( deployment.getSchemaMatchItem() ) ,
				EngineDB.getEnum( deployment.DEPLOYMODE_TYPE ) ,
				EngineDB.getString( deployment.DEPLOYPATH ) ,
				EngineDB.getString( deployment.DBNAME ) ,
				EngineDB.getString( deployment.DBUSER ) ,
				EngineDB.getEnum( deployment.NODE_TYPE )
				} , insert );
	}
	
	public static void loaddb( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppServerDeployment;
		MetaDatabase database = storage.getDatabase();
		MetaDistr distr = storage.getDistr();
		EngineMatcher matcher = loader.getMatcher();
		
		// load segments
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				MetaEnvServer server = env.getServer( entity.loaddbObject( rs , DBEnvData.FIELD_DEPLOYMENT_SERVER_ID ) );
				MetaEnvServerDeployment deployment = new MetaEnvServerDeployment( storage.meta , server );
				deployment.ID = entity.loaddbId( rs );
				deployment.EV = entity.loaddbVersion( rs );

				// set primary 
				MatchItem COMP = entity.loaddbMatchItem( rs , DBEnvData.FIELD_DEPLOYMENT_COMP_ID , MetaEnvServerDeployment.PROPERTY_COMPONENT );
				distr.matchComponent( COMP );
				matcher.matchEnvDone( COMP , env , deployment.ID , entity , MetaEnvServerDeployment.PROPERTY_COMPONENT , null );
				
				MatchItem BINARYITEM = entity.loaddbMatchItem( rs , DBEnvData.FIELD_DEPLOYMENT_BINARY_ID , MetaEnvServerDeployment.PROPERTY_DISTITEM );
				distr.matchBinaryItem( BINARYITEM );
				matcher.matchEnvDone( BINARYITEM , env , deployment.ID , entity , MetaEnvServerDeployment.PROPERTY_DISTITEM , null );
				
				MatchItem CONFITEM = entity.loaddbMatchItem( rs , DBEnvData.FIELD_DEPLOYMENT_CONF_ID , MetaEnvServerDeployment.PROPERTY_CONFITEM );
				distr.matchConfItem( CONFITEM );
				matcher.matchEnvDone( CONFITEM , env , deployment.ID , entity , MetaEnvServerDeployment.PROPERTY_CONFITEM , null );
				
				MatchItem SCHEMA = entity.loaddbMatchItem( rs , DBEnvData.FIELD_DEPLOYMENT_SCHEMA_ID , MetaEnvServerDeployment.PROPERTY_SCHEMA );
				database.matchSchema( SCHEMA );
				matcher.matchEnvDone( SCHEMA , env , deployment.ID , entity , MetaEnvServerDeployment.PROPERTY_SCHEMA , null );
				
				deployment.create(
						DBEnumServerDeploymentType.getValue( entity.loaddbEnum( rs , DBEnvData.FIELD_DEPLOYMENT_TYPE ) , true ) ,
						COMP ,
						BINARYITEM ,
						CONFITEM ,
						SCHEMA ,
						DBEnumDeployModeType.getValue( entity.loaddbEnum( rs , MetaEnvServerDeployment.PROPERTY_DEPLOYMODE ) , false ) ,
						entity.loaddbString( rs , MetaEnvServerDeployment.PROPERTY_DEPLOYPATH ) ,
						entity.loaddbString( rs , MetaEnvServerDeployment.PROPERTY_DBNAME ) ,
						entity.loaddbString( rs , MetaEnvServerDeployment.PROPERTY_DBUSER ) ,
						DBEnumNodeType.getValue( entity.loaddbEnum( rs , MetaEnvServerDeployment.PROPERTY_NODETYPE ) , false )
						);
				
				server.addDeployment( deployment );
			}
		}
		finally {
			c.closeQuery();
		}
		
		// properties
		for( MetaEnvSegment sg : env.getSegments() ) {
			for( MetaEnvServer server : sg.getServers() ) {
				ObjectProperties ops = server.getProperties();
				DBSettings.loaddbValues( loader , server.ID , ops );
				server.scatterExtraProperties();
			}
		}
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServerDeployment deployment , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppServerDeployment;
		MetaDistr distr = storage.getDistr();
		MetaDatabase database = storage.getDatabase();
		
		// primary
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( distr.getComponentName( deployment.getCompMatchItem() ) ) ,
				entity.exportxmlString( distr.getBinaryItemName( deployment.getBinaryItemMatchItem() ) ) ,
				entity.exportxmlString( distr.getConfItemName( deployment.getConfItemMatchItem() ) ) ,
				entity.exportxmlString( database.getSchemaName( deployment.getSchemaMatchItem() ) ) ,
				entity.exportxmlEnum( deployment.DEPLOYMODE_TYPE ) ,
				entity.exportxmlString( deployment.DEPLOYPATH ) ,
				entity.exportxmlString( deployment.DBNAME ) ,
				entity.exportxmlString( deployment.DBUSER ) ,
				entity.exportxmlEnum( deployment.NODE_TYPE )
		} , true );
	}
	
}
