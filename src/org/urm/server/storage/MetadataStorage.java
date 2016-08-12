package org.urm.server.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.database.DatabaseClient;
import org.urm.server.meta.MetaDatabaseSchema;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.Meta;

public class MetadataStorage {

	public static String tablesFileName = "tableset.txt";
	public static String xdocDir = "xdoc";
	
	public Artefactory artefactory;
	public Meta meta;
	
	public MetadataStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}
	
	public String getDistrFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getMetadataPath( action , UrmStorage.DISTR_SETTINGS_FILE ) );
	}

	public String getDatabaseFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getMetadataPath( action , UrmStorage.DATABASE_SETTINGS_FILE ) );
	}

	public String[] getDesignFiles( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getMetadataFolder( action , xdocDir );
		if( !folder.checkExists( action ) )
			return( new String[0] );
		
		FileSet files = folder.getFileSet( action );
		return( files.fileList.toArray( new String[0] ) );
	}
	
	public String getDesignFile( ActionBase action , String fileName ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		String dir = urm.getMetadataPath( action , xdocDir );
		return( Common.getPath( dir , fileName ) );
	}
	
	public String getLastProdTagFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getMetadataPath( action , "last-prod-tag.txt" ) );
	}
	
	public String getProductConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getMetadataPath( action , UrmStorage.PRODUCT_SETTINGS_FILE ) );
	}
	
	public String getSourceConfFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getMetadataPath( action , UrmStorage.SOURCE_SETTINGS_FILE ) );
	}

	public String getMonitoringFile( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		return( urm.getMetadataPath( action , UrmStorage.MONITORING_SETTINGS_FILE ) );
	}
	
	public String getEnvFile( ActionBase action , String envFile ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		String dir = urm.getMetadataPath( action , "env" );
		return( Common.getPath( dir , envFile ) );
	}
	
	public String[] getEnvFiles( ActionBase action ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder folder = urm.getMetadataFolder( action , "env" );
		String[] files = folder.findFiles( action , "*.xml" );
		return( files );
	}
	
	public String getDatapumpFile( ActionBase action , String specFile ) throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		String dir = urm.getMetadataPath( action , "datapump" );
		return( Common.getPath( dir , specFile ) );
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
				action.exit( "invalid table set line=" + line );
			String SN = opts[0]; 
			String table = opts[1]; 
			if( SN.isEmpty() || table.isEmpty() )
				action.exit( "invalid table set line=" + line );

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
			action.exit( "unable to connect to administrative db" );
		
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
	
}
