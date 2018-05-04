package org.urm.engine.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.database.DatabaseClient;
import org.urm.common.Common;
import org.urm.db.EngineDB;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.env.MetaDumpMask;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.product.MetaDatabaseSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductStorage {

	public static String XML_APPVERSION = "appversion";
	
	public Artefactory artefactory;
	public AppProduct product;
	
	public ProductStorage( Artefactory artefactory , AppProduct product ) {
		this.artefactory = artefactory;
		this.product = product;
	}

	public LocalFolder getHomeFolder( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getProductHome( action , product ) );
	}
	
	public LocalFolder getMetaFolder( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getProductCoreMetadataFolder( action , product ) );
	}
	
	public String getCoreConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action , product );
		return( folder.getFilePath( action , UrmStorage.CORE_SETTINGS_FILE ) );
	}
	
	public String getUnitsFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action , product );
		return( folder.getFilePath( action , UrmStorage.UNITS_FILE ) );
	}
	
	public String getDocumentationFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action , product );
		return( folder.getFilePath( action , UrmStorage.DOCUMENTATION_FILE ) );
	}
	
	public String getDatabaseConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action , product );
		return( folder.getFilePath( action , UrmStorage.DATABASE_SETTINGS_FILE ) );
	}
	
	public String getDistrConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action , product );
		return( folder.getFilePath( action , UrmStorage.DISTR_SETTINGS_FILE ) );
	}

	public String getSourcesConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action , product );
		return( folder.getFilePath( action , UrmStorage.SOURCE_SETTINGS_FILE ) );
	}

	public String getMonitoringConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action , product );
		return( folder.getFilePath( action , UrmStorage.MONITORING_SETTINGS_FILE ) );
	}
	
	public LocalFolder getEnvConfFolder( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getProductEnvMetadataFolder( action , product ) );
	}
	
	public String getEnvConfFile( ActionBase action , String envName ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductEnvMetadataFolder( action , product );
		return( folder.getFilePath( action , envName + ".xml" ) );
	}
	
	public String getDesignFile( ActionBase action , String fileName ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductXDocMetadataFolder( action , product );
		return( folder.getFilePath( action , fileName ) );
	}
	
	public String[] getEnvFiles( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductEnvMetadataFolder( action , product );
		if( !folder.checkExists( action ) )
			return( new String[0] );
		
		String[] files = folder.findFiles( action , "*.xml" );
		return( files );
	}
	
	public String getEnvFilePath( ActionBase action , String file ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductEnvMetadataFolder( action , product );
		return( folder.getFilePath( action , file ) );
	}
	
	public String[] getDesignFiles( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductXDocMetadataFolder( action , product );
		if( !folder.checkExists( action ) )
			return( new String[0] );
		
		FileSet files = folder.getFileSet( action );
		return( files.fileList.toArray( new String[0] ) );
	}
	
	public void createdbDatapumpSet( ActionBase action , Map<String,List<MetaDumpMask>> tableSet , MetaEnvServer server , boolean standby , boolean export ) throws Exception {
		String table = ( export )? "urm_export" : "urm_import";  
		action.info( "create table " + table + " in administrative database ..." );
		
		// load table set into database
		String[] columns = { "xschema" , "xtable" , "xinclude" };
		String[] columntypes = { "varchar(30)" , "varchar(255)" , "char(1)" };
		List<String[]> data = new LinkedList<String[]>();
		
		Map<String,MetaDatabaseSchema> serverSchemas = new HashMap<String,MetaDatabaseSchema>();
		for( MetaDatabaseSchema schema : server.getSchemaSet() )
			serverSchemas.put( schema.NAME , schema );
			
		for( String SN : tableSet.keySet() ) {
			MetaDatabaseSchema schema = serverSchemas.get( SN );
			List<MetaDumpMask> tables = tableSet.get( SN );
			
			String dbschema = server.getSchemaDBName( schema );
			for( MetaDumpMask s : tables ) {
				String bv = ( s.INCLUDE )? "Y" : "N";
				data.add( new String[] { Common.getSQLQuoted( dbschema ) , Common.getSQLQuoted( s.TABLEMASK ) , Common.getSQLQuoted( bv ) } );
			}
		}

		DatabaseClient client = new DatabaseClient(); 
		if( !client.checkConnect( action , server ) )
			action.exit0( _Error.UnableConnectAdminDatabase0 , "unable to connect to administrative db" );
		
		client.createTableData( action , server.getAdmSchema() , table , columns , columntypes , data );  
	}

	public void saveDatapumpSet( ActionBase action , Map<String,List<MetaDumpMask>> tableSet , MetaEnvServer server , String filePath ) throws Exception {
		Map<String,MetaDatabaseSchema> serverSchemas = new HashMap<String,MetaDatabaseSchema>();
		for( MetaDatabaseSchema schema : server.getSchemaSet() )
			serverSchemas.put( schema.NAME , schema );
		
		List<String> conf = new LinkedList<String>();
		
		for( String SN : tableSet.keySet() ) {
			MetaDatabaseSchema schema = serverSchemas.get( SN );
			List<MetaDumpMask> tables = tableSet.get( SN );

			String dbschema = server.getSchemaDBName( schema );
			for( MetaDumpMask s : tables ) {
				String bv = ( s.INCLUDE )? "include" : "exclude";
				conf.add( dbschema + "/" + s.TABLEMASK + "/" + bv );
			}
		}
		
		Common.createFileFromStringList( action.execrc , filePath , conf );
	}

	public void saveFile( ActionBase action , Document doc , String file ) throws Exception {
		String dir = Common.getDirName( file );
		LocalFolder folder = action.getLocalFolder( dir );
		folder.ensureExists( action );
		Common.xmlSaveDoc( doc , file );
	}
	
	public static void saveDoc( Document doc , String path ) throws Exception {
		Element root = doc.getDocumentElement();
		Common.xmlSetElementAttr( doc , root , XML_APPVERSION , "" + EngineDB.APP_VERSION );
		Common.xmlSaveDoc( doc , path );
	}
	
	public void saveEnvConfFile( ActionBase action , Document doc , String envFile ) throws Exception {
		LocalFolder folder = getEnvConfFolder( action );
		folder.ensureExists( action );
		String filePath = getEnvConfFile( action , envFile );
		saveFile( action , doc , filePath );
	}
	
	public void deleteEnvConfFile( ActionBase action , String envFile ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductEnvMetadataFolder( action , product );
		folder.removeFiles( action , envFile );
	}
	
}
