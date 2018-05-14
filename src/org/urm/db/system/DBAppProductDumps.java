package org.urm.db.system;

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
import org.urm.engine.products.EngineProductEnvs;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.loader._Error;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.system.AppProduct;
import org.urm.meta.system.AppProductDumps;
import org.urm.meta.system.ProductDump;
import org.urm.meta.system.ProductDumpMask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBAppProductDumps {

	public static String ELEMENT_DUMP = "dump";
	public static String ELEMENT_TABLESET = "data";
	public static String ELEMENT_DUMPMASK = "tables";

	public static void importxmlAll( EngineLoader loader , AppProduct product , Node root ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_DUMP );
		if( items == null )
			return;
		
		for( Node node : items ) {
			ActionBase action = loader.getAction();
			try {
				// dump settings
				DBAppProductDumps.importxmlDump( loader , product , node );
			}
			catch( Throwable e ) {
				loader.log( "import dump metadata" , e );
				loader.setLoadFailed( action , _Error.UnableLoadProductDumps1 , e , "unable to import dump metadata, product=" + product.NAME , product.NAME );
			}
		}
	}
	
	public static ProductDump importxmlDump( EngineLoader loader , AppProduct product , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction(); 
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProductDump;
		
		AppProductDumps dumps = product.dumps;
		ProductDump dump = new ProductDump( dumps );
		dump.create( 
				entity.importxmlStringProperty( root , ProductDump.PROPERTY_NAME ) , 
				entity.importxmlStringProperty( root , ProductDump.PROPERTY_DESC ) , 
				entity.importxmlBooleanProperty( root , ProductDump.PROPERTY_EXPORT , true ) 
				);

		EngineProductEnvs envs = product.findEnvs();
		String envName = entity.importxmlStringProperty( root , ProductDump.PROPERTY_ENV );
		MetaEnv env = envs.getEnv( envName );
		
		String sgName = entity.importxmlStringProperty( root , ProductDump.PROPERTY_SEGMENT );
		MetaEnvSegment sg = env.getSegment( sgName );
		
		String serverName = entity.importxmlStringProperty( root , ProductDump.PROPERTY_SERVER );
		MetaEnvServer server = sg.getServer( serverName );
		
		dump.setTarget( 
				server , 
				entity.importxmlBooleanProperty( root , ProductDump.PROPERTY_STANDBY , true ) ,
				entity.importxmlStringProperty( root , ProductDump.PROPERTY_SETDBENV ) 
				);
		
		dump.setFiles( 
				entity.importxmlStringProperty( root , ProductDump.PROPERTY_DATASET ) ,
				entity.importxmlBooleanProperty( root , ProductDump.PROPERTY_OWNTABLESET , false ) ,
				entity.importxmlStringProperty( root , ProductDump.PROPERTY_DUMPDIR ) ,
				entity.importxmlStringProperty( root , ProductDump.PROPERTY_DATAPUMPDIR ) ,
				entity.importxmlBooleanProperty( root , ProductDump.PROPERTY_NFS , false ) , 
				entity.importxmlStringProperty( root , ProductDump.PROPERTY_POSTREFRESH ) 
				);
		
		ScheduleProperties props = new ScheduleProperties();
		props.setScheduleData( action , entity.importxmlStringProperty( root , ProductDump.PROPERTY_SCHEDULE ) );
		dump.setSchedule( props );
		
		dump.setOffline( entity.importxmlBooleanProperty( root , ProductDump.PROPERTY_OFFLINE , true ) );
		
		modifyDump( c , product , dump , true );
		
		if( dump.OWNTABLESET ) {
			Node data = ConfReader.xmlGetFirstChild( root , ELEMENT_TABLESET );
			if( data != null ) {
				Node[] items = ConfReader.xmlGetChildren( data , ELEMENT_DUMPMASK );
				if( items != null ) {
					for( Node node : items )
						importxmlDumpMask( loader , product , dump , node );
				}
			}
		}
		
		return( dump );
	}
	
	private static void importxmlDumpMask( EngineLoader loader , AppProduct product , ProductDump dump , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProductDumpMask;
		
		ProductDumpMask mask = new ProductDumpMask( dump );
		MetaEnvServer server = dump.getServer();
		MetaDatabase db = server.meta.getDatabase();
		
		String schemaName = entity.importxmlStringAttr( root , ProductDumpMask.PROPERTY_SCHEMA );
		MetaDatabaseSchema schema = db.getSchema( schemaName );
		mask.create( 
				schema , 
				entity.importxmlBooleanAttr( root , ProductDumpMask.PROPERTY_INCLUDE , true ) , 
				entity.importxmlStringAttr( root , ProductDumpMask.PROPERTY_MASK ) 
				);
		
		modifyDumpMask( c , product , dump , mask , true );
		
		dump.addTableMask( mask );
	}
	
	private static void modifyDump( DBConnection c , AppProduct product , ProductDump dump , boolean insert ) throws Exception {
		if( insert )
			dump.ID = DBNames.getNameIndex( c , product.ID , dump.NAME , DBEnumParamEntityType.APPPRODUCT_DUMP );
		else
			DBNames.updateName( c , product.ID , dump.NAME , dump.ID , DBEnumParamEntityType.APPPRODUCT_DUMP );
		
		dump.EV = c.getNextSystemVersion( product.system );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppProductDump , dump.ID , dump.EV , new String[] {
				EngineDB.getInteger( product.ID ) , 
				EngineDB.getString( dump.NAME ) ,
				EngineDB.getString( dump.DESC ) ,
				EngineDB.getMatchId( dump.DB ) ,
				EngineDB.getString( dump.DB_FKENV ) ,
				EngineDB.getString( dump.DB_FKSG ) ,
				EngineDB.getString( dump.DB_FKSERVER ) ,
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
	
	private static void modifyDumpMask( DBConnection c , AppProduct product , ProductDump dump , ProductDumpMask mask , boolean insert ) throws Exception {
		if( insert )
			mask.ID = c.getNextSequenceValue();
		
		mask.EV = c.getNextSystemVersion( product.system );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppProductDumpMask , dump.ID , dump.EV , new String[] {
				EngineDB.getObject( product.ID ) , 
				EngineDB.getObject( dump.ID ) , 
				EngineDB.getBoolean( mask.INCLUDE ) ,
				EngineDB.getMatchId( mask.SCHEMA ) ,
				EngineDB.getMatchName( mask.SCHEMA ) ,
				EngineDB.getString( mask.TABLEMASK )
				} , insert );
	}
	
	public static void exportxmlAll( EngineLoader loader , AppProduct product , Document doc , Element root ) throws Exception {
		AppProductDumps dumps = product.dumps;
		
		for( String name : dumps.getExportDumpNames() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DUMP );
			ProductDump dump = dumps.findExportDump( name );
			exportxmlDump( loader , product , dump , doc , node );
		}
		
		for( String name : dumps.getImportDumpNames() ) {
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DUMP );
			ProductDump dump = dumps.findImportDump( name );
			exportxmlDump( loader , product , dump , doc , node );
		}
	}
	
	public static void exportxmlDump( EngineLoader loader , AppProduct product , ProductDump dump , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProductDump;
		
		MetaEnvServer server = dump.getServer();
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( dump.NAME ) ,
				entity.exportxmlString( dump.DESC ) ,
				entity.exportxmlString( server.sg.env.NAME ) ,
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
			MetaDatabase db = server.meta.getDatabase();
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_TABLESET );
			for( ProductDumpMask mask : dump.getTables() ) {
				Element nodeMask = Common.xmlCreateElement( doc , node , ELEMENT_DUMPMASK );
				exportxmlDumpMask( loader , product , db , dump , mask , doc , nodeMask );
			}
		}
	}

	public static void exportxmlDumpMask( EngineLoader loader , AppProduct product , MetaDatabase db , ProductDump dump , ProductDumpMask mask , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProductDumpMask;
		
		MetaDatabaseSchema schema = db.getSchema( mask.SCHEMA );
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlBoolean( mask.INCLUDE ) ,
				entity.exportxmlString( schema.NAME ) , 
				entity.exportxmlString( mask.TABLEMASK )
		} , false );
	}		
	
	public static void loaddbAll( EngineLoader loader , AppProduct product ) throws Exception {
		loaddbDumps( loader , product );
		loaddbDumpMasks( loader , product );
	}
		
	public static void loaddbDumps( EngineLoader loader , AppProduct product ) throws Exception {
		DBConnection c = loader.getConnection();
		ActionBase action = loader.getAction();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppProductDump;

		AppProductDumps dumps = product.dumps;
		EngineProductEnvs envs = product.findEnvs();
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		try {
			while( rs.next() ) {
				ProductDump dump = new ProductDump( dumps );
				dump.ID = entity.loaddbId( rs );
				dump.EV = entity.loaddbVersion( rs );
				dump.create( 
						entity.loaddbString( rs , ProductDump.PROPERTY_NAME ) , 
						entity.loaddbString( rs , ProductDump.PROPERTY_DESC ) ,
						entity.loaddbBoolean( rs , ProductDump.PROPERTY_EXPORT )
						);
				
				int serverId = entity.loaddbObject( rs , DBSystemData.FIELD_DUMP_SERVER_ID );
				MetaEnvServer server = envs.getServer( serverId );
				dump.setTarget( 
						server , 
						entity.loaddbBoolean( rs , ProductDump.PROPERTY_STANDBY ) ,
						entity.loaddbString( rs , ProductDump.PROPERTY_SETDBENV )
						);
				
				dump.setFiles( 
						entity.loaddbString( rs , ProductDump.PROPERTY_DATASET ) ,
						entity.loaddbBoolean( rs , ProductDump.PROPERTY_OWNTABLESET ) ,
						entity.loaddbString( rs , ProductDump.PROPERTY_DUMPDIR ) ,
						entity.loaddbString( rs , ProductDump.PROPERTY_DATAPUMPDIR ) ,
						entity.loaddbBoolean( rs , ProductDump.PROPERTY_NFS ) , 
						entity.loaddbString( rs , ProductDump.PROPERTY_POSTREFRESH ) 
						);
				
				ScheduleProperties props = new ScheduleProperties();
				props.setScheduleData( action , entity.loaddbString( rs , ProductDump.PROPERTY_SCHEDULE ) );
				dump.setSchedule( props );
				
				dump.setOffline( entity.loaddbBoolean( rs , ProductDump.PROPERTY_OFFLINE ) );
				
				dumps.addDump( dump );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void loaddbDumpMasks( EngineLoader loader , AppProduct product ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppProductDumpMask;

		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		try {
			while( rs.next() ) {
				int dumpId = entity.loaddbObject( rs , DBSystemData.FIELD_DUMPMASK_DUMP_ID );
				ProductDump dump = product.getDump( dumpId );
				MetaEnvServer server = dump.getServer();
				MetaDatabase db = server.meta.getDatabase();
				
				MatchItem SCHEMA = entity.loaddbMatchItem( rs , DBSystemData.FIELD_DUMPMASK_SCHEMA_ID , ProductDumpMask.PROPERTY_SCHEMA );
				MetaDatabaseSchema schema = db.getSchema( SCHEMA );
				
				ProductDumpMask mask = new ProductDumpMask( dump );
				mask.ID = entity.loaddbId( rs );
				mask.EV = entity.loaddbVersion( rs );
				mask.create( 
						schema , 
						entity.loaddbBoolean( rs , ProductDumpMask.PROPERTY_INCLUDE ) , 
						entity.loaddbString( rs , ProductDumpMask.PROPERTY_MASK )
						);
				
				dump.addTableMask( mask );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static ProductDump createDump( TransactionBase transaction , AppProduct product , MetaEnvServer server , boolean export , String name , String desc , String dataset ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		AppProductDumps dumps = product.dumps;
		ProductDump dump = new ProductDump( dumps );
		dump.create( name , desc , export );
		dump.setTarget( server , false , "" );
		boolean owntables = ( export )? true : false;
		dump.setFiles( dataset , owntables , "" , "" , false , "" );
		dump.setOffline( true );
		
		modifyDump( c , product , dump , true );
		dumps.addDump( dump );
		
		return( dump );
	}

	public static void modifyDumpPrimary( TransactionBase transaction , AppProduct product , ProductDump dump , String name , String desc , MetaEnvServer server , String dataset ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		dump.modify( name , desc );
		dump.setTargetServer( server );
		dump.setFilesDataset( dataset );

		modifyDump( c , product , dump , false );
	}
	
	public static void modifyDumpExecution( TransactionBase transaction , AppProduct product , ProductDump dump , boolean standby , String setdbenv , boolean ownTables , String dumpdir , String datapumpdir , boolean nfs , String postRefresh ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		dump.setTargetDetails( standby , setdbenv );
		dump.setFilesDetails( ownTables , dumpdir , datapumpdir , nfs , postRefresh );
		
		modifyDump( c , product , dump , false );
	}
	
	public static void deleteDump( TransactionBase transaction , AppProduct product , ProductDump dump ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();

		AppProductDumps dumps = product.dumps;
		
		int version = c.getNextSystemVersion( product.system );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDumpMask , DBQueries.FILTER_DUMP_ID1 , new String[] { EngineDB.getObject( dump.ID ) } );
		DBEngineEntities.deleteAppObject( c , entities.entityAppProductDump , dump.ID , version );
		
		dumps.removeDump( dump );
	}

	public static ProductDumpMask createDumpMask( TransactionBase transaction , AppProduct product , ProductDump dump , MetaDatabaseSchema schema , boolean include , String tables ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ProductDumpMask mask = new ProductDumpMask( dump );
		mask.create( schema , include , tables );
		modifyDumpMask( c , product , dump , mask , true );
		
		dump.addTableMask( mask );
		
		return( mask );
	}
	
	public static void modifyDumpMask( TransactionBase transaction , AppProduct product , ProductDump dump , ProductDumpMask mask , MetaDatabaseSchema schema , boolean include , String tables ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		mask.modify( schema , include , tables );
		modifyDumpMask( c , product , dump , mask , false );
	}

	public static void deleteDumpMask( TransactionBase transaction , AppProduct product , ProductDump dump , ProductDumpMask mask ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		int version = c.getNextSystemVersion( product.system );
		DBEngineEntities.deleteAppObject( c , entities.entityAppProductDumpMask , mask.ID , version );
		
		dump.removeTableMask( mask );
	}
	
	public static void setDumpOffline( TransactionBase transaction , AppProduct product , ProductDump dump , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		dump.setOffline( offline );
		modifyDump( c , product , dump , false );
	}

	public static void setDumpSchedule( TransactionBase transaction , AppProduct product , ProductDump dump , ScheduleProperties schedule ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		dump.setSchedule( schedule );
		modifyDump( c , product , dump , false );
	}

}
