package org.urm.db.env;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvStartGroup;
import org.urm.meta.env.MetaEnvStartInfo;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvStartInfo {

	public static String ELEMENT_STARTORDER = "startorder";
	public static String ELEMENT_STARTGROUP = "startgroup";
	
	public static void importxmlStartOrder( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Node root ) throws Exception {
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
		
		int index = startInfo.getLastStartGroupPos();
		group.createGroup(
				entity.importxmlStringAttr( root , MetaEnvStartGroup.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaEnvStartGroup.PROPERTY_DESC ) ,
				index + 1 );
		modifyStartGroup( c , storage , env , group , true );
		String servers = entity.importxmlStringAttr( root , MetaEnvStartGroup.PROPERTY_SERVERS );
		
		for( String name : Common.splitSpaced( servers ) ) {
			MetaEnvServer server = sg.getServer( name );
			group.addServer( server );
			addStartGroupServer( c , storage , env , group , server );
		}
		
		return( group );
	}
	
	private static void modifyStartGroup( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvStartGroup group , boolean insert ) throws Exception {
		if( insert )
			group.ID = DBNames.getNameIndex( c , group.startInfo.sg.ID , group.NAME , DBEnumParamEntityType.ENV_SEGMENT_STARTGROUP );
		else
			DBNames.updateName( c , group.ID , group.NAME , group.ID , DBEnumParamEntityType.ENV_SEGMENT_STARTGROUP );
		
		group.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppSegmentStartGroup , group.ID , group.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getObject( group.startInfo.sg.ID ) ,
				EngineDB.getString( group.NAME ) ,
				EngineDB.getString( group.DESC ) ,
				EngineDB.getInteger( group.POS )
				} , insert );
	}
	
	private static void addStartGroupServer( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvStartGroup group , MetaEnvServer server ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextEnvironmentVersion( env );
		DBEngineEntities.modifyAppEntity( c , entities.entityAppSegmentStartGroupServer , version , new String[] { 
				EngineDB.getInteger( group.ID ) , 
				EngineDB.getInteger( server.ID ) ,
				EngineDB.getInteger( env.ID )
				} , true );
	}
	
	public static void exportxmlStartOrder( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvSegment sg , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppSegmentStartGroup;
		
		Element nodeStartOrder = Common.xmlCreateElement( doc , root , ELEMENT_STARTORDER );
		
		MetaEnvStartInfo startInfo = sg.getStartInfo();
		for( MetaEnvStartGroup startGroup : startInfo.getForwardGroupList() ) {
			Element nodeStartGroup = Common.xmlCreateElement( doc , nodeStartOrder , ELEMENT_STARTGROUP );
			
			String servers = Common.getListSpaced( startGroup.getServerNames() );
			DBEngineEntities.exportxmlAppObject( doc , nodeStartGroup , entity , new String[] {
					entity.exportxmlString( startGroup.NAME ) ,
					entity.exportxmlString( sg.DESC ) ,
					servers
			} , true );
		}
	}
	
	public static MetaEnvStartGroup createStartGroup( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvStartInfo startInfo , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaEnvStartGroup group = new MetaEnvStartGroup( env.meta , startInfo );
		int index = startInfo.getLastStartGroupPos();
		group.createGroup( name , desc , index + 1 );
		modifyStartGroup( c , storage , env , group , true );
		
		startInfo.addGroup( group );
		return( group );
	}
	
	public static void deleteStartGroup( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvStartInfo startInfo , MetaEnvStartGroup group ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		if( !c.modify( DBQueries.MODIFY_ENV_SHIFTSTARTGROUPS2 , new String[] { EngineDB.getInteger( group.ID ) , EngineDB.getInteger( group.POS ) } ) )
			Common.exitUnexpected();
		
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentStartGroupServer , DBQueries.FILTER_ENV_STARTGROUP1 , new String[] { EngineDB.getInteger( group.ID ) } );
		
		int version = c.getNextProductVersion( storage );
		DBEngineEntities.deleteAppObject( c , entities.entityAppSegmentStartGroup , group.ID , version );
		
		startInfo.removeGroup( group );
	}
	
	public static void moveStartGroup( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvStartInfo startInfo , MetaEnvStartGroup group , int pos ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		int index = startInfo.getLastStartGroupPos();
		if( pos > index )
			Common.exitUnexpected();

		if( pos == group.POS )
			return;
		
		int minPos = ( pos > group.POS )? group.POS : pos;
		int maxPos = ( pos > group.POS )? pos : group.POS;
		startInfo.moveGroup( group , pos );
		
		for( MetaEnvStartGroup groupChange : startInfo.getGroups() ) {
			if( groupChange.POS >= minPos && groupChange.POS <= maxPos )
				modifyStartGroup( c , storage , env , groupChange , false );
		}
	}

	public static void addStartGroupServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvStartInfo startInfo , MetaEnvStartGroup group , MetaEnvServer server ) throws Exception {
		DBConnection c = transaction.getConnection();

		if( group.findServer( server ) != null )
			Common.exitUnexpected();
		
		group.addServer( server );
		addStartGroupServer( c , storage , env , group , server );
	}
	
	public static void deleteStartGroupServer( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvStartInfo startInfo , MetaEnvStartGroup group , MetaEnvServer server ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();

		if( group.findServer( server ) == null )
			Common.exitUnexpected();
		
		group.removeServer( server );
		DBEngineEntities.dropAppObjects( c , entities.entityAppSegmentStartGroupServer , DBQueries.FILTER_ENV_STARTGROUPSERVER2 , new String[] { 
				EngineDB.getInteger( group.ID ) ,
				EngineDB.getInteger( server.ID ) } );
	}
	
}
