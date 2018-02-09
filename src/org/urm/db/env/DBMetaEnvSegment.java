package org.urm.db.env;

import org.urm.common.Common;
import org.urm.common.ConfReader;
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
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvStartInfo;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Node;

public class DBMetaEnvSegment {

	public static String ELEMENT_DEPLOYMENT = "deployment";
	public static String ELEMENT_STARTORDER = "startorder";
	public static String ELEMENT_SERVER = "server";
	
	public static MetaEnvSegment importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		MetaEnvSegment sg = new MetaEnvSegment( storage.meta , env );
		
		importxmlMain( loader , storage , env , sg , root );
		importxmlServers( loader , storage , env , sg , root );
		importxmlStartOrder( loader , storage , env , sg , root );
		importxmlDeployment( loader , storage , env , sg , root );
		
 		return( sg );
	}

	public static void importxmlMain( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
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
	
	public static void importxmlServers( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_SERVER );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaEnvServer server = DBMetaEnvServer.importxml( loader , storage , env , sg , node );
			sg.addServer( server );
		}
	}
	
	public static void importxmlStartOrder( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
	}
	
	public static void importxmlDeployment( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
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
