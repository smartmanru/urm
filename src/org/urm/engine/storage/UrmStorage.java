package org.urm.engine.storage;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.action.ActionBase;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.meta.Meta.VarDBMSTYPE;

public class UrmStorage {

	public Artefactory artefactory;
	public Meta meta;

	public static String PRODUCTS_FOLDER = "products";
	public static String SERVER_SETTINGS_FILE = "server.xml";
	public static String VERSION_SETTINGS_FILE = "version.xml";
	public static String PRODUCT_SETTINGS_FILE = "product.xml";
	public static String SOURCE_SETTINGS_FILE = "source.xml";
	public static String DISTR_SETTINGS_FILE = "distr.xml";
	public static String DATABASE_SETTINGS_FILE = "database.xml";
	public static String MONITORING_SETTINGS_FILE = "monitoring.xml";
	public static String ETC_PATH = "etc";
	public static String TABLES_FILE_NAME = "tableset.txt";
	public static String XDOC_DIR = "xdoc";
	public static String ENV_DIR = "env";
	public static String DATAPUMP_DIR = "datapump";
	public static String MASTER_PATH = "master";
	
	public UrmStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}

	public boolean isServerMode( ActionBase action ) throws Exception {
		LocalFolder folder = getInstallFolder( action );
		if( folder.checkFileExists( action , Common.getPath( ETC_PATH , SERVER_SETTINGS_FILE ) ) )
			return( true );
		
		return( false );
	}
	
	public boolean isStandaloneMode( ActionBase action ) throws Exception {
		LocalFolder folder = getInstallFolder( action );
		if( folder.checkFileExists( action , Common.getPath( ETC_PATH , PRODUCT_SETTINGS_FILE ) ) )
			return( true );
		
		return( false );
	}
	
	private String getDatabaseSpecificFolder( ActionBase action , VarDBMSTYPE dbtype , VarOSTYPE ostype ) throws Exception {
		String dbFolder = "";
		if( dbtype == VarDBMSTYPE.ORACLE )
			dbFolder = "oracle";
		else
		if( dbtype == VarDBMSTYPE.POSTGRESQL )
			dbFolder = "postgres";
		else
		if( dbtype == VarDBMSTYPE.FIREBIRD )
			dbFolder = "firebird";
		else
			action.exitUnexpectedState();
		
		String osFolder = "";
		if( ostype == VarOSTYPE.LINUX )
			osFolder = "linux";
		else
		if( ostype == VarOSTYPE.WINDOWS )
			osFolder = "windows";
		else
			action.exitUnexpectedState();
		
		String folder = Common.getPath( dbFolder , osFolder );
		return( folder );
	}

	private LocalFolder getDatabaseFolder( ActionBase action , MetaEnvServer server , String parentPath ) throws Exception {
		String folderPath = getDatabaseSpecificFolder( action , server.dbType , server.osType );
		
		LocalFolder folder = getInstallFolder( action , Common.getPath( parentPath , folderPath ) );
		if( !folder.checkExists( action ) ) {
			String dbtype = Common.getEnumLower( server.dbType );
			String ostype = Common.getEnumLower( server.osType );
			action.exit2( _Error.DatabaseNotSupported2 , "database is not supported: dbtype=" + dbtype + ", ostype=" + ostype , dbtype , ostype );
		}
		
		return( folder );
	}
	
	public LocalFolder getDatabaseInitScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "init" ) ); 
	}
	
	public LocalFolder getDatabaseSqlScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "sql" ) ); 
	}
	
	public LocalFolder getDatabaseDatapumpScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "datapump" ) ); 
	}
	
	public LocalFolder getProductFolder( ActionBase action ) throws Exception {
		return( getProductFolder( action , "" ) );
	}

	public LocalFolder getInstallFolder( ActionBase action , String dirname ) throws Exception {
		String dir = Common.getPath( action.context.session.execrc.installPath , dirname );
		return( artefactory.getAnyFolder( action , dir ) );
	}

	public LocalFolder getProductFolder( ActionBase action , String dirname ) throws Exception {
		String dir = Common.getPath( action.context.session.productPath , dirname );
		return( artefactory.getAnyFolder( action , dir ) );
	}

	public LocalFolder getProductCoreMetadataFolder( ActionBase action ) throws Exception {
		return( getProductFolder( action , ETC_PATH ) );
	}

	public LocalFolder getProductEnvMetadataFolder( ActionBase action ) throws Exception {
		return( getProductFolder( action , Common.getPath( ETC_PATH , ENV_DIR ) ) );
	}

	public LocalFolder getProductDatapumpMetadataFolder( ActionBase action ) throws Exception {
		return( getProductFolder( action , Common.getPath( ETC_PATH , DATAPUMP_DIR ) ) );
	}

	public LocalFolder getProductXDocMetadataFolder( ActionBase action ) throws Exception {
		return( getProductFolder( action , Common.getPath( ETC_PATH , XDOC_DIR ) ) );
	}

	public LocalFolder getServerSettingsFolder( ActionBase action ) throws Exception {
		return( getInstallFolder( action , ETC_PATH ) );
	}
	
	public LocalFolder getInstallFolder( ActionBase action ) throws Exception {
		return( artefactory.getAnyFolder( action , action.context.session.execrc.installPath ) );
	}

	public LocalFolder getServerProductsFolder( ActionBase action ) throws Exception {
		LocalFolder folder = getInstallFolder( action );
		return( folder.getSubFolder( action , PRODUCTS_FOLDER ) );
	}
	
}
