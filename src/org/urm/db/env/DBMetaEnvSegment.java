package org.urm.db.env;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
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
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvStartGroup;
import org.urm.meta.env.MetaEnvStartInfo;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Node;

public class DBMetaEnvSegment {

	public static String ELEMENT_DEPLOYMENT = "deployment";
	public static String ELEMENT_STARTORDER = "startorder";
	public static String ELEMENT_STARTGROUP = "startgroup";
	public static String ELEMENT_SERVER = "server";
	
	public static MetaEnvSegment importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		MetaEnvSegment sg = new MetaEnvSegment( storage.meta , env );
		
		importxmlMain( loader , storage , env , sg , root );
		importxmlServers( loader , storage , env , sg , root );
		importxmlStartOrder( loader , storage , env , sg , root );
		importxmlServerDependencies( loader , storage , env , sg , root );
		
 		return( sg );
	}

	public static void importxmlMatchBaseline( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , MetaEnv baselineEnv ) throws Exception {
		EngineEntities entities = loader.getEntities();
		EngineMatcher matcher = loader.getMatcher();
		DBConnection c = loader.getConnection();
		
		MatchItem BASELINE = sg.getBaselineMatchItem();
		if( BASELINE != null ) {
			String value = matcher.matchEnvBefore( env , BASELINE.FKNAME , sg.ID , entities.entityAppSegmentPrimary , MetaEnvSegment.PROPERTY_BASELINE , null );
			MetaEnvSegment baseline = baselineEnv.findSegment( value );
			if( baseline != null ) {
				BASELINE.match( baseline.ID );
				modifySegmentMatch( c , storage , env , sg );
			}
			matcher.matchEnvDone( BASELINE );
			
			if( baseline != null ) {
				for( MetaEnvServer server : sg.getServers() )
					DBMetaEnvServer.importxmlMatchBaseline( loader , storage , env , server , baseline );
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
		sg.ID = DBNames.getNameIndex( c , env.ID , NAME , DBEnumObjectType.ENVIRONMENT_SEGMENT );

		loader.trace( "import meta env segment object, name=" + NAME );

		// create settings
		ObjectProperties ops = entities.createMetaEnvSegmentProps( env.getProperties() );
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
		DBSettings.importxml( loader , root , ops , sg.ID , storage.ID , false , true , env.EV );
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
	
	private static void importxmlStartOrder( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		MetaEnvStartInfo startInfo = sg.getStartInfo();
		
		Node startorder = ConfReader.xmlGetFirstChild( root , ELEMENT_STARTORDER );
		if( startorder == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( startorder , ELEMENT_STARTGROUP );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaEnvStartGroup group = importxmlStartGroup( loader , storage , env , sg , node );
			startInfo.addGroup( group );
		}
	}
	
	private static MetaEnvStartGroup importxmlStartGroup( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentStartGroup;
		DBConnection c = loader.getConnection();
		
		MetaEnvStartInfo startInfo = sg.getStartInfo();
		MetaEnvStartGroup group = new MetaEnvStartGroup( storage.meta , startInfo );
		
		group.createGroup(
				entity.importxmlStringAttr( root , MetaEnvStartGroup.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaEnvStartGroup.PROPERTY_DESC ) );
		modifyStartGroup( c , storage , env , group , true );
		
		String servers = entity.importxmlStringAttr( root , MetaEnvStartGroup.PROPERTY_SERVERS );
		for( String name : Common.splitSpaced( servers ) ) {
			MetaEnvServer server = sg.getServer( name );
			group.addServer( server );
			addStartGroupServer( c , storage , env , group , server );
		}
		
		return( group );
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
			DBNames.updateName( c , env.ID , sg.NAME , sg.ID , DBEnumObjectType.ENVIRONMENT_SEGMENT );
		
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
	
	private static void modifyStartGroup( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvStartGroup group , boolean insert ) throws Exception {
		if( insert )
			group.ID = DBNames.getNameIndex( c , group.startInfo.sg.ID , group.NAME , DBEnumObjectType.ENVIRONMENT_STARTGROUP );
		else
			DBNames.updateName( c , group.ID , group.NAME , group.ID , DBEnumObjectType.ENVIRONMENT_STARTGROUP );
		
		group.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppSegmentStartGroup , group.ID , group.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getObject( group.startInfo.sg.ID ) ,
				EngineDB.getString( group.NAME ) ,
				EngineDB.getString( group.DESC )
				} , insert );
	}
	
	private static void addStartGroupServer( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvStartGroup group , MetaEnvServer server ) throws Exception {
		int version = c.getNextEnvironmentVersion( env );
		if( !c.modify( DBQueries.MODIFY_ENVSG_ADDSTARTGROUPSERVER4 , new String[] { 
				EngineDB.getInteger( group.ID ) , 
				EngineDB.getInteger( server.ID ) ,
				EngineDB.getInteger( env.ID ) ,
				EngineDB.getInteger( version ) 
				} ) )
			Common.exitUnexpected();
	}
	
	private static void modifySegmentMatch( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		MatchItem item = sg.getBaselineMatchItem();
		if( !item.MATCHED )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_ENVSG_MATCHBASELINE2 , new String[] { EngineDB.getInteger( sg.ID ) , EngineDB.getInteger( item.FKID ) } ) )
			Common.exitUnexpected();
	}
	
	public static MetaEnvSegment createSegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , String name , String desc , Integer dcId ) throws Exception {
		Common.exitUnexpected();
		return( null );
	}

	public static void modifySegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , String name , String desc , Integer dcId ) throws Exception {
		Common.exitUnexpected();
	}

	public static void setSegmentBaseline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Integer sgId ) throws Exception {
		Common.exitUnexpected();
	}

	public static void setSegmentOffline( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , boolean offline ) throws Exception {
		Common.exitUnexpected();
	}

	public static void deleteSegment( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		Common.exitUnexpected();
	}

	public static void setStartInfo( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , MetaEnvStartInfo startInfo ) throws Exception {
		Common.exitUnexpected();
	}

	public static void updateCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		Common.exitUnexpected();
	}
	
}
