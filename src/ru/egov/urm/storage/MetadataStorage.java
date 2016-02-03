package ru.egov.urm.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.database.DatabaseClient;

public class MetadataStorage {

	public static String tablesFileName = "tableset.txt";
	public static String xdocDir = "etc/xdoc";
	
	public Artefactory artefactory;
	public Metadata meta;
	
	public MetadataStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}
	
	public String getDistrFile( ActionBase action ) throws Exception {
		 return( action.context.productHome + "/etc/distr.xml" );
	}

	public String[] getDesignFiles( ActionBase action ) throws Exception {
		LocalFolder folder = artefactory.getAnyFolder( action , Common.getPath( action.context.productHome , xdocDir ) );
		if( !folder.checkExists( action ) )
			return( new String[0] );
		
		FileSet files = folder.getFileSet( action );
		return( files.fileList.toArray( new String[0] ) );
	}
	
	public String getDesignFile( ActionBase action , String fileName ) throws Exception {
		LocalFolder folder = artefactory.getAnyFolder( action , Common.getPath( action.context.productHome , xdocDir ) );
		return( folder.getFilePath( action , fileName ) );
	}
	
	public String getLastProdTagFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/last-prod-tag.txt" );
	}
	
	public String getProductConfFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/product.conf" );
	}
	
	public String getSourceConfFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/source.xml" );
	}

	public String getMonitoringFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/monitoring.xml" );
	}
	
	public String getOrgInfoFile( ActionBase action ) throws Exception {
		return( action.context.productHome + "/etc/orginfo.txt" );
	}

	public String getEnvFile( ActionBase action , String envFile ) throws Exception {
		 return( action.context.productHome + "/etc/env/" + envFile );
	}
	
	public String[] getEnvFiles( ActionBase action ) throws Exception {
		LocalFolder folder = artefactory.getAnyFolder( action , action.context.productHome + "/etc/env/" );
		String[] files = folder.findFiles( action , "*.xml" );
		return( files );
	}
	
	public String getDatapumpFile( ActionBase action , String specFile ) throws Exception {
		 return( action.context.productHome + "/etc/datapump/" + specFile );
	}

	public HashMap<String,Map<String,String>> readDatapumpFile( ActionBase action , String specFile , String schema ) throws Exception {
		String tablesetPath = getDatapumpFile( action , specFile );
		action.log( "reading export table set file " + tablesetPath + " ..." );
		
		HashMap<String,Map<String,String>> tableSet = new HashMap<String,Map<String,String>>();
		for( String line : ConfReader.readFileLines( action , tablesetPath ) ) {
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
				meta.distr.database.getSchema( action , SN );
				tables = new HashMap<String,String>();
				tableSet.put( SN , tables );
			}
			
			tables.put( table , SN );
		}
		
		return( tableSet );
	}
	
	public void loadDatapumpSet( ActionBase action , Map<String,Map<String,String>> tableSet , MetaEnvServer server , boolean standby , boolean export ) throws Exception {
		String table = ( export )? "urm_export" : "urm_import";  
		action.log( "create table " + table + " in administrative database ..." );
		
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
