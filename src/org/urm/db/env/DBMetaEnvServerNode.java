package org.urm.db.env;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
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
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Node;

public class DBMetaEnvServerNode {

	public static MetaEnvServerNode importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvServer server , Node root ) throws Exception {
		MetaEnvServerNode node = new MetaEnvServerNode( storage.meta , server );
		
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		EngineInfrastructure infra = loader.getInfrastructure();
		
		// identify
		PropertyEntity entity = entities.entityAppNodePrimary;
		int pos = entity.importxmlIntAttr( root , MetaEnvServerNode.PROPERTY_POS );
		node.ID = c.getNextSequenceValue();

		loader.trace( "import meta env server node object, pos=" + pos );

		// create settings
		ObjectProperties ops = entities.createMetaEnvServerNodeProps( env.getProperties() );
		node.createSettings( ops );
		
		// primary match
		String hostLogin = entity.importxmlStringAttr( root , MetaEnvServerNode.PROPERTY_HOSTLOGIN );
		hostLogin = matcher.matchEnvBefore( env , hostLogin , node.ID , entities.entityAppNodePrimary , MetaEnvServerNode.PROPERTY_HOSTLOGIN , null );
		MatchItem ACCOUNT = infra.matchAccountByHostlogin( hostLogin );
		matcher.matchEnvDone( ACCOUNT );
		
		// primary
		node.setNodePrimary(
				pos ,
				DBEnumNodeType.getValue( entity.importxmlEnumAttr( root , MetaEnvServerNode.PROPERTY_NODETYPE ) , true ) ,
				ACCOUNT ,
				entity.importxmlStringAttr( root , MetaEnvServerNode.PROPERTY_DEPLOYGROUP ) ,
				entity.importxmlBooleanAttr( root , MetaEnvServerNode.PROPERTY_OFFLINE , false ) ,
				entity.importxmlStringAttr( root , MetaEnvServerNode.PROPERTY_DBINSTANCE ) ,
				entity.importxmlBooleanAttr( root , MetaEnvServerNode.PROPERTY_DBSTANDBY , false ) 
				);
		modifyNode( c , storage , env , node , true );
		
		// custom
		DBSettings.importxml( loader , root , ops , node.ID , storage.ID , false , true , env.EV );
		
 		return( node );
	}

	private static void modifyNode( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvServerNode node , boolean insert ) throws Exception {
		node.EV = c.getNextEnvironmentVersion( env );
		
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppNodePrimary , node.ID , node.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getObject( node.server.ID ) ,
				EngineDB.getInteger( node.POS ) ,
				EngineDB.getEnum( node.NODE_TYPE ) ,
				EngineDB.getMatchId( node.getAccountMatchItem() ) ,
				EngineDB.getMatchName( node.getAccountMatchItem() ) ,
				EngineDB.getString( node.DEPLOYGROUP ) ,
				EngineDB.getBoolean( node.OFFLINE ) ,
				EngineDB.getString( node.DBINSTANCE ) ,
				EngineDB.getBoolean( node.DBSTANDBY )
				} , insert );
	}
	
	public static MetaEnvServerNode createNode( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServer server , int pos , DBEnumNodeType nodeType , HostAccount account ) throws Exception {
		Common.exitUnexpected();
		return( null );
	}
	
	public static void modifyNode( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServerNode node , int pos , DBEnumNodeType nodeType , HostAccount account ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void deleteNode( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServerNode node ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void setOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServerNode node , boolean offline ) throws Exception {
		Common.exitUnexpected();
	}
	
	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvServerNode node ) throws Exception {
		Common.exitUnexpected();
	}
	
}
