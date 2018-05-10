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
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvStartGroup;
import org.urm.meta.env.MetaEnvStartInfo;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.loader.EngineMatcher;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvSegment {

	public static String ELEMENT_DEPLOYMENT = "deployment";
	public static String ELEMENT_SERVER = "server";
	
	public static MetaEnvSegment importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		MetaEnvSegment sg = new MetaEnvSegment( storage.meta , env );
		
		importxmlMain( loader , storage , env , sg , root );
		importxmlServers( loader , storage , env , sg , root );
		DBMetaEnvStartInfo.importxmlStartOrder( loader , storage , env , sg , root );
		importxmlServerDependencies( loader , storage , env , sg , root );
		
 		return( sg );
	}

	public static void matchBaseline( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , MetaEnv baselineEnv ) throws Exception {
		DBConnection c = loader.getConnection();
		
		MatchItem BASELINE = sg.getBaselineMatchItem();
		if( BASELINE != null ) {
			MetaEnvSegment baseline = baselineEnv.findSegment( BASELINE );
			if( baseline != null ) {
				BASELINE.match( baseline.ID );
				modifySegmentMatch( c , storage , env , sg );
			}
			
			if( baseline != null ) {
				for( MetaEnvServer server : sg.getServers() )
					DBMetaEnvServer.matchBaseline( loader , storage , env , server , baseline );
			}
		}
	}
	
	private static void importxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		EngineInfrastructure infra = loader.getInfrastructure();
		
		// identify
		PropertyEntity entity = entities.entityAppSegmentPrimary;
		String NAME = entity.importxmlStringAttr( root , MetaEnvSegment.PROPERTY_NAME );
		sg.ID = DBNames.getNameIndex( c , env.ID , NAME , DBEnumParamEntityType.ENV_SEGMENT_PRIMARY );

		loader.trace( "import meta env segment object, name=" + NAME );

		// create settings
		ObjectProperties ops = entities.createMetaEnvSegmentProps( sg.ID , env.getProperties() );
		sg.createSettings( ops );
		
		// primary match (baseline match is postponed)
		MatchItem BASELINE = MatchItem.create( entity.importxmlStringAttr( root , MetaEnvSegment.PROPERTY_BASELINE ) );
		
		String datacenterName = entity.importxmlStringAttr( root , MetaEnvSegment.PROPERTY_DC );
		datacenterName = matcher.matchEnvBefore( env , datacenterName , sg.ID , entities.entityAppSegmentPrimary , MetaEnvSegment.PROPERTY_DC , null );
		MatchItem DC = infra.matchDatacenter( datacenterName );
		matcher.matchEnvDone( DC );
		
		// primary
		sg.setSegmentPrimary(
				NAME ,
				entity.importxmlStringAttr( root , MetaEnvSegment.PROPERTY_DESC ) ,
				BASELINE ,
				entity.importxmlBooleanAttr( root , MetaEnvSegment.PROPERTY_OFFLINE , false ) ,
				DC );
		modifySegment( c , storage , env , sg , true );
		
		// custom
		DBSettings.importxml( loader , root , ops , false , true , env.EV );
	}
	
	private static void importxmlServers( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_SERVER );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaEnvServer server = DBMetaEnvServer.importxml( loader , storage , env , sg , node );
			sg.addServer( server );
		}
	}
	
	private static void importxmlServerDependencies( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_SERVER );
		if( items == null )
			return;
		
		for( Node node : items )
			DBMetaEnvServer.importxmlDependencies( loader , storage , env , sg , node );
	}
	
	private static void modifySegment( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , boolean insert ) throws Exception {
		if( !insert )
			DBNames.updateName( c , env.ID , sg.NAME , sg.ID , DBEnumParamEntityType.ENV_SEGMENT_PRIMARY );
		
		sg.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppSegmentPrimary , sg.ID , sg.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getString( sg.NAME ) ,
				EngineDB.getString( sg.DESC ) ,
				EngineDB.getMatchId( sg.getBaselineMatchItem() ) ,
				EngineDB.getMatchName( sg.getBaselineMatchItem() ) ,
				EngineDB.getBoolean( sg.OFFLINE ) ,
				EngineDB.getMatchId( sg.getDatacenterMatchItem() ) ,
				EngineDB.getMatchName( sg.getDatacenterMatchItem() )
				} , insert );
	}
	
	public static void loaddb( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentPrimary;
		EngineInfrastructure infra = loader.getInfrastructure();
		EngineMatcher matcher = loader.getMatcher();
		
		// load segments
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				MetaEnvSegment sg = new MetaEnvSegment( storage.meta , env );
				sg.ID = entity.loaddbId( rs );
				sg.EV = entity.loaddbVersion( rs );

				ObjectProperties ops = entities.createMetaEnvSegmentProps( sg.ID , env.getProperties() );
				sg.createSettings( ops );
				
				// match baseline later
				MatchItem BASELINE = entity.loaddbMatchItem( rs , DBEnvData.FIELD_SEGMENT_BASELINE_ID , MetaEnvSegment.PROPERTY_BASELINE );
				
				// set primary 
				MatchItem DATACENTER = entity.loaddbMatchItem( rs , DBEnvData.FIELD_SEGMENT_DATACENTER_ID , MetaEnvSegment.PROPERTY_DC );
				infra.matchDatacenter( DATACENTER );
				matcher.matchEnvDone( DATACENTER , env , sg.ID , entity , MetaEnvSegment.PROPERTY_DC , null );
				
				sg.setSegmentPrimary(
						entity.loaddbString( rs , MetaEnvSegment.PROPERTY_NAME ) ,
						entity.loaddbString( rs , MetaEnvSegment.PROPERTY_DESC ) ,
						BASELINE ,
						entity.loaddbBoolean( rs , MetaEnvSegment.PROPERTY_OFFLINE ) ,
						DATACENTER
						);
				
				env.addSegment( sg );
			}
		}
		finally {
			c.closeQuery();
		}
		
		// properties
		for( MetaEnvSegment sg : env.getSegments() ) {
			ObjectProperties ops = sg.getProperties();
			DBSettings.loaddbValues( loader , ops );
		}
		
		DBMetaEnvServer.loaddb( loader , storage , env );
		DBMetaEnvServerNode.loaddb( loader , storage , env );
		
		loaddbStartGroups( loader , storage , env );
		loaddbStartGroupItems( loader , storage , env );
		loaddbServerDeps( loader , storage , env );
	}	
	
	private static void loaddbStartGroups( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentStartGroup;
		
		// load start groups
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				MetaEnvSegment sg = env.getSegment( entity.loaddbObject( rs , DBEnvData.FIELD_STARTGROUP_SEGMENT_ID ) );
				MetaEnvStartInfo startInfo = sg.getStartInfo();
				MetaEnvStartGroup startGroup = new MetaEnvStartGroup( storage.meta , startInfo );
				
				startGroup.ID = entity.loaddbId( rs );
				startGroup.EV = entity.loaddbVersion( rs );
				startGroup.createGroup(
						entity.loaddbString( rs , MetaEnvStartGroup.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaEnvStartGroup.PROPERTY_DESC ) ,
						entity.loaddbInt( rs , DBEnvData.FIELD_STARTGROUP_POS )
						);
				
				startInfo.addGroup( startGroup );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	private static void loaddbStartGroupItems( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentStartGroupServer;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				int startGroupId = rs.getInt( 1 );
				int serverId = rs.getInt( 2 );
				
				MetaEnvStartGroup startGroup = env.getStartGroup( startGroupId );
				MetaEnvServer server = startGroup.startInfo.sg.getServer( serverId );
				startGroup.addServer( server );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	private static void loaddbServerDeps( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppServerDependency;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				int serverId = rs.getInt( 1 );
				int serverDepId = rs.getInt( 2 );
				DBEnumServerDependencyType type = DBEnumServerDependencyType.getValue( rs.getInt( 4 ) , true );
				
				MetaEnvServer server = env.getServer( serverId );
				MetaEnvServer depServer = server.sg.getServer( serverDepId );
				server.addDependencyServer( depServer , type );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Document doc , Element root ) throws Exception {
		exportxmlMain( loader , storage , env , sg , doc , root );
		exportxmlServers( loader , storage , env , sg , doc , root );
		DBMetaEnvStartInfo.exportxmlStartOrder( loader , storage , env , sg , doc , root );
	}	

	private static void exportxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Document doc , Element root ) throws Exception {
		ObjectProperties ops = sg.getProperties();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentPrimary;
		EngineInfrastructure infra = loader.getInfrastructure();
		
		// primary
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( sg.NAME ) ,
				entity.exportxmlString( sg.DESC ) ,
				entity.exportxmlString( env.getSegmentName( sg.getBaselineMatchItem() ) ) ,
				entity.exportxmlBoolean( sg.OFFLINE ) ,
				entity.exportxmlString( infra.getDatacenterName( sg.getDatacenterMatchItem() ) )
		} , true );
		
		// custom settings
		DBSettings.exportxmlCustomEntity( loader , doc , root , ops );
	}
	
	private static void exportxmlServers( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Document doc , Element root ) throws Exception {
		for( String name : sg.getServerNames() ) {
			MetaEnvServer server = sg.findServer( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_SERVER );
			DBMetaEnvServer.exportxml( loader , storage , env , server , doc , node );
		}
	}
	
	private static void modifySegmentMatch( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		MatchItem item = sg.getBaselineMatchItem();
		if( !item.MATCHED )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVSG_MATCHBASELINE2 , new String[] { EngineDB.getInteger( sg.ID ) , EngineDB.getInteger( item.FKID ) } ) )
			Common.exitUnexpected();
	}
	
	public static MetaEnvSegment createSegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , String name , String desc , Integer dcId ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = transaction.getEntities();
		
		MetaEnvSegment sg = new MetaEnvSegment( storage.meta , env );
		sg.ID = DBNames.getNameIndex( c , env.ID , name , DBEnumParamEntityType.ENV_SEGMENT_PRIMARY );
		
		transaction.trace( "create meta env segment, object=" + sg.objectId + ", name=" + name + ", id=" + sg.ID );

		// create settings
		ObjectProperties ops = entities.createMetaEnvSegmentProps( sg.ID , env.getProperties() );
		sg.createSettings( ops );
		
		sg.setSegmentPrimary( name , desc , null , true , MatchItem.create( dcId ) );
		sg.refreshPrimaryProperties();
		modifySegment( c , storage , env , sg , true );
		
		env.addSegment( sg );
		return( sg );
	}

	public static void modifySegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , String name , String desc , Integer dcId ) throws Exception {
		DBConnection c = transaction.getConnection();
		sg.modifySegment( name , desc , MatchItem.create( dcId ) );
		
		modifySegment( c , storage , env , sg , false );
		env.updateSegment( sg );
	}

	public static void setSegmentBaseline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Integer sgId ) throws Exception {
		DBConnection c = transaction.getConnection();
		sg.setBaseline( MatchItem.create( sgId ) );
		
		modifySegment( c , storage , env , sg , false );
	}

	public static void setSegmentOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		sg.setOffline( offline );
		
		modifySegment( c , storage , env , sg , false );
	}

	public static void deleteSegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		DBEngineEntities.deleteAppObject( c , entities.entityAppSegmentPrimary , sg.ID , c.getNextEnvironmentVersion( env ) );
		env.removeSegment( sg );
	}

	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ObjectProperties ops = sg.getProperties();
		int version = c.getNextEnvironmentVersion( env );
		DBSettings.savedbPropertyValues( transaction , ops , false , true , version );
		ops.recalculateChildProperties();
	}
	
}
