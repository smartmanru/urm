package org.urm.engine.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.database.DatabaseClient;
import org.urm.common.Common;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaDatabaseSchema;
import org.urm.engine.meta.MetaEnvServer;
import org.w3c.dom.Document;

public class MetadataStorage {

	public Artefactory artefactory;
	public Meta meta;
	
	public MetadataStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}

	public LocalFolder getFolder( ActionBase action ) throws Exception {
		return( new LocalFolder( action.context.session.etcPath , action.isLocalWindows() ) );
	}
	
	public String getVersionConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action );
		return( folder.getFilePath( action , UrmStorage.VERSION_SETTINGS_FILE ) );
	}
	
	public String getProductConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action );
		return( folder.getFilePath( action , UrmStorage.PRODUCT_SETTINGS_FILE ) );
	}
	
	public String getDatabaseConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action );
		return( folder.getFilePath( action , UrmStorage.DATABASE_SETTINGS_FILE ) );
	}
	
	public String getDistrConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action );
		return( folder.getFilePath( action , UrmStorage.DISTR_SETTINGS_FILE ) );
	}

	public String getSourcesConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action );
		return( folder.getFilePath( action , UrmStorage.SOURCE_SETTINGS_FILE ) );
	}

	public String getMonitoringConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductCoreMetadataFolder( action );
		return( folder.getFilePath( action , UrmStorage.MONITORING_SETTINGS_FILE ) );
	}
	
	public String getEnvFile( ActionBase action , String envFile ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductEnvMetadataFolder( action );
		return( folder.getFilePath( action , envFile ) );
	}
	
	public String getDesignFile( ActionBase action , String fileName ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductXDocMetadataFolder( action );
		return( folder.getFilePath( action , fileName ) );
	}
	
	public String[] getEnvFiles( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductEnvMetadataFolder( action );
		String[] files = folder.findFiles( action , "*.xml" );
		return( files );
	}
	
	public String[] getDesignFiles( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductXDocMetadataFolder( action );
		if( !folder.checkExists( action ) )
			return( new String[0] );
		
		FileSet files = folder.getFileSet( action );
		return( files.fileList.toArray( new String[0] ) );
	}
	
	public String getDatapumpFile( ActionBase action , String specFile ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductDatapumpMetadataFolder( action );
		return( folder.getFilePath( action , specFile ) );
	}

	public HashMap<String,Map<String,String>> readDatapumpFile( ActionBase action , String specFile , String schema ) throws Exception {
		String tablesetPath = getDatapumpFile( action , specFile );
		action.info( "reading export table set file " + tablesetPath + " ..." );
		
		HashMap<String,Map<String,String>> tableSet = new HashMap<String,Map<String,String>>();
		for( String line : action.readFileLines( tablesetPath ) ) {
			if( line.isEmpty() || line.startsWith( "#" ) )
				continue;
			
			String[] opts = Common.split( line , "/" );
			if( opts.length != 2 )
				action.exit1( _Error.InvalidTableSetLine1 , "invalid table set line=" + line , line );
			String SN = opts[0]; 
			String table = opts[1]; 
			if( SN.isEmpty() || table.isEmpty() )
				action.exit1( _Error.InvalidTableSetLine1 , "invalid table set line=" + line , line );

			if( !schema.isEmpty() )
				if( !SN.equals( schema ) )
					continue;
			
			Map<String,String> tables = tableSet.get( SN );
			if( tables == null ) {
				meta.database.getSchema( action , SN );
				tables = new HashMap<String,String>();
				tableSet.put( SN , tables );
			}
			
			tables.put( table , SN );
		}
		
		return( tableSet );
	}
	
	public void loadDatapumpSet( ActionBase action , Map<String,Map<String,String>> tableSet , MetaEnvServer server , boolean standby , boolean export ) throws Exception {
		String table = ( export )? "urm_export" : "urm_import";  
		action.info( "create table " + table + " in administrative database ..." );
		
		// load table set into database
		String[] columns = { "xschema" , "xtable" };
		String[] columntypes = { "varchar(30)" , "varchar(30)" };
		List<String[]> data = new LinkedList<String[]>();
		
		Map<String,MetaDatabaseSchema> serverSchemas = server.getSchemaSet( action );
		for( String SN : tableSet.keySet() ) {
			MetaDatabaseSchema schema = serverSchemas.get( SN );
			Map<String,String> tables = tableSet.get( SN );
			
			if( tables.containsKey( "*" ) ) {
				data.add( new String[] { Common.getSQLQuoted( schema.DBNAME ) , Common.getSQLQuoted( "*" ) } );
			}
			else {
				for( String s : tables.keySet() ) {
					data.add( new String[] { Common.getSQLQuoted( schema.DBNAME ) , Common.getSQLQuoted( s ) } );
				}
			}
		}

		DatabaseClient client = new DatabaseClient(); 
		if( !client.checkConnect( action , server ) )
			action.exit0( _Error.UnableConnectAdminDatabase0 , "unable to connect to administrative db" );
		
		client.createTableData( action , server.admSchema , table , columns , columntypes , data );  
	}

	public void saveDatapumpSet( ActionBase action , Map<String,Map<String,String>> tableSet , MetaEnvServer server , String filePath ) throws Exception {
		Map<String,MetaDatabaseSchema> serverSchemas = server.getSchemaSet( action );
		List<String> conf = new LinkedList<String>();
		
		for( String SN : tableSet.keySet() ) {
			MetaDatabaseSchema schema = serverSchemas.get( SN );
			Map<String,String> tables = tableSet.get( SN );
			
			if( tables.containsKey( "*" ) ) {
				conf.add( schema.DBNAME + "/*" );
			}
			else {
				for( String s : tables.keySet() ) {
					conf.add( schema.DBNAME + "/" + s );
				}
			}
		}
		
		Common.createFileFromStringList( filePath , conf );
	}

	public void saveFile( ActionBase action , Document doc , String file ) throws Exception {
		String dir = Common.getDirName( file );
		LocalFolder folder = action.getLocalFolder( dir );
		folder.ensureExists( action );
		Common.xmlSaveDoc( doc , file );
	}
	
	public void saveVersionConfFile( ActionBase action , Document doc ) throws Exception {
		String filePath = getVersionConfFile( action );
		saveFile( action , doc , filePath );
	}
	
	public void saveProductConfFile( ActionBase action , Document doc ) throws Exception {
		String filePath = getProductConfFile( action );
		saveFile( action , doc , filePath );
	}
	
	public void saveDatabaseConfFile( ActionBase action , Document doc ) throws Exception {
		String filePath = getDatabaseConfFile( action );
		saveFile( action , doc , filePath );
	}
	
	public void saveDistrConfFile( ActionBase action , Document doc ) throws Exception {
		String filePath = getDistrConfFile( action );
		saveFile( action , doc , filePath );
	}
	
	public void saveSourcesConfFile( ActionBase action , Document doc ) throws Exception {
		String filePath = getSourcesConfFile( action );
		saveFile( action , doc , filePath );
	}
	
	public void saveMonitoringConfFile( ActionBase action , Document doc ) throws Exception {
		String filePath = getMonitoringConfFile( action );
		saveFile( action , doc , filePath );
	}
	
}