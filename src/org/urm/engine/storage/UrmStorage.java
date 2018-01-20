package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.MetaEnvServer;

public class UrmStorage {

	public Artefactory artefactory;

	public static String INSTALL_FOLDER = "install";
	public static String PRODUCTS_FOLDER = "products";
	public static String SERVER_SETTINGS_FILE = "server.xml";
	public static String VERSION_SETTINGS_FILE = "version.xml";
	public static String CORE_SETTINGS_FILE = "product.xml";
	public static String POLICY_SETTINGS_FILE = "policy.xml";
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
	}

	public boolean isServerMode( ActionBase action ) throws Exception {
		LocalFolder folder = getServerFolder( action );
		if( folder.checkFileExists( action , Common.getPath( ETC_PATH , SERVER_SETTINGS_FILE ) ) )
			return( true );
		
		return( false );
	}
	
	public boolean isStandaloneMode( ActionBase action ) throws Exception {
		LocalFolder folder = getServerFolder( action );
		if( folder.checkFileExists( action , Common.getPath( ETC_PATH , CORE_SETTINGS_FILE ) ) )
			return( true );
		
		return( false );
	}
	
	private String getDatabaseSpecificFolder( ActionBase action , DBEnumDbmsType dbtype , DBEnumOSType ostype , boolean remoteRun ) throws Exception {
		String dbFolder = "";
		if( dbtype == DBEnumDbmsType.ORACLE )
			dbFolder = "oracle";
		else
		if( dbtype == DBEnumDbmsType.POSTGRESQL )
			dbFolder = "postgres";
		else
		if( dbtype == DBEnumDbmsType.FIREBIRD )
			dbFolder = "firebird";
		else
			action.exitUnexpectedState();
		
		if( !remoteRun )
			ostype = DBEnumOSType.getValue( action.execrc.osType );
		
		String osFolder = "";
		if( ostype.isLinux() )
			osFolder = "linux";
		else
		if( ostype.isWindows() )
			osFolder = "windows";
		else
			action.exitUnexpectedState();
		
		String folder = Common.getPath( dbFolder , osFolder );
		return( folder );
	}

	private LocalFolder getDatabaseFolder( ActionBase action , MetaEnvServer server , String parentPath , boolean remoteRun ) throws Exception {
		String folderPath = getDatabaseSpecificFolder( action , server.dbType , server.osType , remoteRun );
		
		LocalFolder folder = getInstallFolder( action , Common.getPath( parentPath , folderPath ) );
		if( !folder.checkExists( action ) ) {
			String dbtype = Common.getEnumLower( server.dbType );
			String ostype = Common.getEnumLower( action.execrc.osType );
			action.exit2( _Error.DatabaseNotSupported2 , "database is not supported: dbtype=" + dbtype + ", ostype=" + ostype , dbtype , ostype );
		}
		
		return( folder );
	}
	
	public LocalFolder getDatabaseInitScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "database/init" , false ) ); 
	}
	
	public LocalFolder getDatabaseSqlScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "database/sql" , false ) ); 
	}
	
	public LocalFolder getDatabaseDatapumpScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "database/datapump" , true ) ); 
	}
	
	public LocalFolder getProductHome( ActionBase action , String productName ) throws Exception {
		if( isStandaloneMode( action ) ) {
			String dir = action.execrc.installPath;
			return( artefactory.getAnyFolder( action , dir ) );
		}
		
		AppProduct product = action.findProduct( productName );
		if( product == null )
			action.exitUnexpectedState();
		
		LocalFolder products = getServerProductsFolder( action );
		return( products.getSubFolder( action , product.PATH ) );
	}

	public LocalFolder getInstallFolder( ActionBase action , String dirname ) throws Exception {
		String dir = Common.getPath( action.context.session.execrc.installPath , dirname );
		return( artefactory.getAnyFolder( action , dir ) );
	}

	public LocalFolder getProductFolder( ActionBase action , String productName , String dirname ) throws Exception {
		LocalFolder folder = getProductHome( action , productName );
		return( folder.getSubFolder( action , dirname ) );
	}

	public LocalFolder getProductCoreMetadataFolder( ActionBase action , String productName ) throws Exception {
		return( getProductFolder( action , productName , ETC_PATH ) );
	}

	public LocalFolder getProductEnvMetadataFolder( ActionBase action , String productName ) throws Exception {
		return( getProductFolder( action , productName , Common.getPath( ETC_PATH , ENV_DIR ) ) );
	}

	public LocalFolder getProductDatapumpMetadataFolder( ActionBase action , String productName ) throws Exception {
		return( getProductFolder( action , productName , Common.getPath( ETC_PATH , DATAPUMP_DIR ) ) );
	}

	public LocalFolder getProductXDocMetadataFolder( ActionBase action , String productName ) throws Exception {
		return( getProductFolder( action , productName , Common.getPath( ETC_PATH , XDOC_DIR ) ) );
	}

	public LocalFolder getServerSettingsFolder( ActionBase action ) throws Exception {
		return( getInstallFolder( action , ETC_PATH ) );
	}
	
	public LocalFolder getServerFolder( ActionBase action ) throws Exception {
		return( artefactory.getAnyFolder( action , action.context.session.execrc.installPath ) );
	}

	public LocalFolder getServerInstallFolder( ActionBase action ) throws Exception {
		LocalFolder folder = getServerFolder( action );
		return( folder.getSubFolder( action , INSTALL_FOLDER ) );
	}
	
	public LocalFolder getServerProductsFolder( ActionBase action ) throws Exception {
		LocalFolder folder = getServerFolder( action );
		return( folder.getSubFolder( action , PRODUCTS_FOLDER ) );
	}
	
}
