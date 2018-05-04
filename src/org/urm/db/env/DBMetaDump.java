package org.urm.db.env;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.env.MetaDump;
import org.urm.meta.env.MetaDumpMask;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaDump {

	public static String ELEMENT_DUMP = "dump";
	public static String ELEMENT_TABLESET = "data";
	public static String ELEMENT_DUMPMASK = "tables";
	
	public static void importxmlAll( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_DUMP );
		if( items == null )
			return;
		
		for( Node node : items )
			importxmlDump( loader , storage , env , node );
	}
	
	public static void importxmlDump( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction(); 
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDump;
		
		MetaDump dump = new MetaDump( storage.meta , env );
		dump.create( 
				entity.importxmlStringProperty( root , MetaDump.PROPERTY_NAME ) , 
				entity.importxmlStringProperty( root , MetaDump.PROPERTY_DESC ) , 
				entity.importxmlBooleanProperty( root , MetaDump.PROPERTY_EXPORT , true ) 
				);
		
		String sgName = entity.importxmlStringProperty( root , MetaDump.PROPERTY_SEGMENT );
		MetaEnvSegment sg = env.getSegment( sgName );
		
		String serverName = entity.importxmlStringProperty( root , MetaDump.PROPERTY_SERVER );
		MetaEnvServer server = sg.getServer( serverName );
		
		dump.setTarget( 
				server , 
				entity.importxmlBooleanProperty( root , MetaDump.PROPERTY_STANDBY , true ) ,
				entity.importxmlStringProperty( root , MetaDump.PROPERTY_SETDBENV ) 
				);
		
		dump.setFiles( 
				entity.importxmlStringProperty( root , MetaDump.PROPERTY_DATASET ) ,
				entity.importxmlBooleanProperty( root , MetaDump.PROPERTY_OWNTABLESET , false ) ,
				entity.importxmlStringProperty( root , MetaDump.PROPERTY_DUMPDIR ) ,
				entity.importxmlStringProperty( root , MetaDump.PROPERTY_DATAPUMPDIR ) ,
				entity.importxmlBooleanProperty( root , MetaDump.PROPERTY_NFS , false ) , 
				entity.importxmlStringProperty( root , MetaDump.PROPERTY_POSTREFRESH ) 
				);
		
		ScheduleProperties props = new ScheduleProperties();
		props.setScheduleData( action , entity.importxmlStringProperty( root , MetaDump.PROPERTY_SCHEDULE ) );
		dump.setSchedule( props );
		
		dump.setOffline( entity.importxmlBooleanProperty( root , MetaDump.PROPERTY_OFFLINE , true ) );
		
		modifyDump( c , env , dump , true );
		
		if( dump.OWNTABLESET ) {
			Node data = ConfReader.xmlGetFirstChild( root , ELEMENT_TABLESET );
			if( data == null )
				return;
			
			Node[] items = ConfReader.xmlGetChildren( data , ELEMENT_DUMPMASK );
			if( items == null )
				return;
			
			for( Node node : items )
				importxmlDumpMask( loader , storage , env , dump , node );
		}
	}
	
	public static void importxmlDumpMask( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaDump dump , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDumpMask;
		
		MetaDumpMask mask = new MetaDumpMask( storage.meta , dump );
		MetaDatabase db = storage.getDatabase();
		
		String schemaName = entity.importxmlStringAttr( root , MetaDumpMask.PROPERTY_SCHEMA );
		MetaDatabaseSchema schema = db.getSchema( schemaName );
		mask.create( 
				schema , 
				entity.importxmlBooleanAttr( root , MetaDumpMask.PROPERTY_INCLUDE , true ) , 
				entity.importxmlStringAttr( root , MetaDumpMask.PROPERTY_MASK ) 
				);
		
		modifyDumpMask( c , env , dump , mask , true );
		
		dump.addTableMask( mask );
	}
	
	private static void modifyDump( DBConnection c , MetaEnv env , MetaDump dump , boolean insert ) throws Exception {
		if( insert )
			dump.ID = DBNames.getNameIndex( c , env.ID , dump.NAME , DBEnumParamEntityType.ENV_DUMP );
		else
			DBNames.updateName( c , env.ID , dump.NAME , dump.ID , DBEnumParamEntityType.ENV_DUMP );
		
		dump.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppDump , dump.ID , dump.EV , new String[] {
				EngineDB.getInteger( env.ID ) , 
				EngineDB.getString( dump.NAME ) ,
				EngineDB.getString( dump.DESC ) ,
				EngineDB.getObject( dump.SERVER ) ,
				EngineDB.getBoolean( dump.MODEEXPORT ) ,
				EngineDB.getString( dump.DATASET ) ,
				EngineDB.getBoolean( dump.OWNTABLESET ) ,
				EngineDB.getString( dump.DUMPDIR ) ,
				EngineDB.getString( dump.REMOTE_SETDBENV ) ,
				EngineDB.getString( dump.DATABASE_DATAPUMPDIR ) ,
				EngineDB.getString( dump.POSTREFRESH ) ,
				EngineDB.getString( dump.schedule.getScheduleData() ) ,
				EngineDB.getBoolean( dump.USESTANDBY ) ,
				EngineDB.getBoolean( dump.USENFS ) ,
				EngineDB.getBoolean( dump.OFFLINE )
				} , insert );
	}
	
	private static void modifyDumpMask( DBConnection c , MetaEnv env , MetaDump dump , MetaDumpMask mask , boolean insert ) throws Exception {
		if( insert )
			mask.ID = c.getNextSequenceValue();
		
		mask.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppDumpMask , dump.ID , dump.EV , new String[] {
				EngineDB.getInteger( env.ID ) ,
				EngineDB.getInteger( dump.ID ) , 
				EngineDB.getBoolean( mask.INCLUDE ) ,
				EngineDB.getMatchId( mask.SCHEMA ) ,
				EngineDB.getMatchName( mask.SCHEMA ) ,
				EngineDB.getString( mask.TABLEMASK )
				} , insert );
	}
	
	public static void exportxmlAll( EngineLoader loader , ProductMeta storage , MetaEnv env , Document doc , Element root ) throws Exception {
		for( String name : env.getExportDumpNames() ) {
			MetaDump dump = env.findExportDump( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DUMP );
			exportxmlDump( loader , storage , env , dump , doc , node );
		}
		
		for( String name : env.getImportDumpNames() ) {
			MetaDump dump = env.findImportDump( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DUMP );
			exportxmlDump( loader , storage , env , dump , doc , node );
		}
	}

	public static void exportxmlDump( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaDump dump , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDump;
		
		MetaEnvServer server = env.getServer( dump.SERVER );
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( dump.NAME ) ,
				entity.exportxmlString( dump.DESC ) ,
				entity.exportxmlString( server.sg.NAME ) ,
				entity.exportxmlString( server.NAME ) ,
				entity.exportxmlBoolean( dump.MODEEXPORT ) ,
				entity.exportxmlString( dump.DATASET ) ,
				entity.exportxmlBoolean( dump.OWNTABLESET ) ,
				entity.exportxmlString( dump.DUMPDIR ) ,
				entity.exportxmlString( dump.REMOTE_SETDBENV ) ,
				entity.exportxmlString( dump.DATABASE_DATAPUMPDIR ) ,
				entity.exportxmlString( dump.POSTREFRESH ) ,
				entity.exportxmlString( dump.schedule.getScheduleData() ) ,
				entity.exportxmlBoolean( dump.USESTANDBY ) ,
				entity.exportxmlBoolean( dump.USENFS ) ,
				entity.exportxmlBoolean( dump.OFFLINE )
		} , false );
		
		if( dump.OWNTABLESET ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_TABLESET );
			for( MetaDumpMask mask : dump.getTables() ) {
				Element nodeMask = Common.xmlCreateElement( doc , node , ELEMENT_DUMPMASK );
				exportxmlDumpMask( loader , storage , env , dump , mask , doc , nodeMask );
			}
		}
	}

	public static void exportxmlDumpMask( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaDump dump , MetaDumpMask mask , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDumpMask;
		
		MetaDatabase db = storage.getDatabase();
		MetaDatabaseSchema schema = db.getSchema( mask.SCHEMA );
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlBoolean( mask.INCLUDE ) ,
				entity.exportxmlString( schema.NAME ) , 
				entity.exportxmlString( mask.TABLEMASK )
		} , false );
	}		
	
	public static void loaddbAll( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		loaddbDumps( loader , storage , env );
		loaddbDumpMasks( loader , storage , env );
	}
		
	public static void loaddbDumps( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppDump;

		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				MetaDump dump = new MetaDump( storage.meta , env );
				dump.ID = entity.loaddbId( rs );
				dump.EV = entity.loaddbVersion( rs );
				dump.create( 
						entity.loaddbString( rs , MetaDump.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaDump.PROPERTY_DESC ) ,
						entity.loaddbBoolean( rs , MetaDump.PROPERTY_EXPORT )
						);
				
				int serverId = entity.loaddbObject( rs , DBEnvData.FIELD_DUMP_SERVER_ID );
				MetaEnvServer server = env.getServer( serverId );
				dump.setTarget( 
						server , 
						entity.loaddbBoolean( rs , MetaDump.PROPERTY_STANDBY ) ,
						entity.loaddbString( rs , MetaDump.PROPERTY_SETDBENV )
						);
				
				dump.setFiles( 
						entity.loaddbString( rs , MetaDump.PROPERTY_DATASET ) ,
						entity.loaddbBoolean( rs , MetaDump.PROPERTY_OWNTABLESET ) ,
						entity.loaddbString( rs , MetaDump.PROPERTY_DUMPDIR ) ,
						entity.loaddbString( rs , MetaDump.PROPERTY_DATAPUMPDIR ) ,
						entity.loaddbBoolean( rs , MetaDump.PROPERTY_NFS ) , 
						entity.loaddbString( rs , MetaDump.PROPERTY_POSTREFRESH ) 
						);
				
				ScheduleProperties props = new ScheduleProperties();
				props.setScheduleData( action , entity.loaddbString( rs , MetaDump.PROPERTY_SCHEDULE ) );
				dump.setSchedule( props );
				
				dump.setOffline( entity.loaddbBoolean( rs , MetaDump.PROPERTY_OFFLINE ) );
				
				env.addDump( dump );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void loaddbDumpMasks( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppDumpMask;
		MetaDatabase db = storage.getDatabase();

		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				int dumpId = entity.loaddbObject( rs , DBEnvData.FIELD_DUMPMASK_DUMP_ID );
				MetaDump dump = env.getDump( dumpId );
				
				MatchItem SCHEMA = entity.loaddbMatchItem( rs , DBEnvData.FIELD_DUMPMASK_SCHEMA_ID , MetaDumpMask.PROPERTY_SCHEMA );
				MetaDatabaseSchema schema = db.getSchema( SCHEMA );
				
				MetaDumpMask mask = new MetaDumpMask( storage.meta , dump );
				mask.ID = entity.loaddbId( rs );
				mask.EV = entity.loaddbVersion( rs );
				mask.create( 
						schema , 
						entity.loaddbBoolean( rs , MetaDumpMask.PROPERTY_INCLUDE ) , 
						entity.loaddbString( rs , MetaDumpMask.PROPERTY_MASK )
						);
				
				dump.addTableMask( mask );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static MetaDump createDump( TransactionBase transaction , MetaEnvServer server , boolean export , String name , String desc , String dataset ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaEnv env = server.sg.env;
		MetaDump dump = new MetaDump( env.meta , env );
		dump.create( name , desc , export );
		dump.setTarget( server , false , "" );
		boolean owntables = ( export )? true : false;
		dump.setFiles( dataset , owntables , "" , "" , false , "" );
		dump.setOffline( true );
		
		modifyDump( c , env , dump , true );
		env.addDump( dump );
		
		return( dump );
	}

	public static void modifyDumpPrimary( TransactionBase transaction , MetaDump dump , String name , String desc , MetaEnvServer server , String dataset ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaEnvServer serverOld = dump.findServer();
		
		dump.modify( name , desc );
		dump.setTargetServer( server );
		dump.setFilesDataset( dataset );

		modifyDump( c , dump.env , dump , false );
		
		if( serverOld.sg.env.ID != server.sg.env.ID ) {
			serverOld.sg.env.removeDump( dump );
			server.sg.env.addDump( dump );
		}
	}
	
	public static void modifyDumpExecution( TransactionBase transaction , MetaDump dump , boolean standby , String setdbenv , boolean ownTables , String dumpdir , String datapumpdir , boolean nfs , String postRefresh ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		dump.setTargetDetails( standby , setdbenv );
		dump.setFilesDetails( ownTables , dumpdir , datapumpdir , nfs , postRefresh );
		
		MetaEnv env = dump.env;
		modifyDump( c , env , dump , false );
	}
	
	public static void deleteDump( TransactionBase transaction , MetaEnv env , MetaDump dump ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();

		int version = c.getNextEnvironmentVersion( env );
		DBEngineEntities.dropAppObjects( c , entities.entityAppDumpMask , DBQueries.FILTER_DUMP_ID1 , new String[] { EngineDB.getObject( dump.ID ) } );
		DBEngineEntities.deleteAppObject( c , entities.entityAppDump , dump.ID , version );
		
		env.removeDump( dump );
	}

	public static MetaDumpMask createDumpMask( TransactionBase transaction , MetaEnv env , MetaDump dump , MetaDatabaseSchema schema , boolean include , String tables ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaDumpMask mask = new MetaDumpMask( env.meta , dump );
		mask.create( schema , include , tables );
		modifyDump( c , env , dump , true );
		
		dump.addTableMask( mask );
		
		return( mask );
	}
	
	public static void modifyDumpMask( TransactionBase transaction , MetaEnv env , MetaDump dump , MetaDumpMask mask , MetaDatabaseSchema schema , boolean include , String tables ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		mask.modify( schema , include , tables );
		modifyDump( c , env , dump , false );
	}

	public static void deleteDumpMask( TransactionBase transaction , MetaEnv env , MetaDump dump , MetaDumpMask mask ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		int version = c.getNextEnvironmentVersion( env );
		DBEngineEntities.deleteAppObject( c , entities.entityAppDumpMask , mask.ID , version );
		
		dump.removeTableMask( mask );
	}
	
	public static void setDumpOffline( TransactionBase transaction , MetaEnv env , MetaDump dump , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		dump.setOffline( offline );
		modifyDump( c , env , dump , false );
	}

	public static void setDumpSchedule( TransactionBase transaction , MetaEnv env , MetaDump dump , ScheduleProperties schedule ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		dump.setSchedule( schedule );
		modifyDump( c , env , dump , false );
	}

}
