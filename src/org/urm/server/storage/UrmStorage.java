package org.urm.server.storage;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.Metadata;
import org.urm.server.meta.Metadata.VarDBMSTYPE;

public class UrmStorage {

	public Artefactory artefactory;
	public Metadata meta;

	public static String PRODUCTS_FOLDER = "products";
	
	public UrmStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}

	public boolean isServerMode( ActionBase action ) throws Exception {
		LocalFolder folder = getInstallFolder( action );
		if( folder.checkFolderExists( action , PRODUCTS_FOLDER ) )
			return( true );
		
		return( false );
	}
	
	private String getSpecificFolder( ActionBase action , VarDBMSTYPE dbtype , VarOSTYPE ostype ) throws Exception {
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
		String folderPath = getSpecificFolder( action , server.dbType , server.osType );
		
		LocalFolder folder = getProductFolder( action , Common.getPath( parentPath , folderPath ) );
		if( !folder.checkExists( action ) )
			action.exit( "database is not supported: dbtype=" + Common.getEnumLower( server.dbType ) + ", ostype=" + Common.getEnumLower( server.osType ) );
		
		return( folder );
	}
	
	public LocalFolder getInitScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "master/database/init" ) ); 
	}
	
	public LocalFolder getSqlScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "master/database/sql" ) ); 
	}
	
	public LocalFolder getDatapumpScripts( ActionBase action , MetaEnvServer server ) throws Exception {
		return( getDatabaseFolder( action , server , "master/database/datapump" ) ); 
	}
	
	public LocalFolder getProductFolder( ActionBase action ) throws Exception {
		return( getProductFolder( action , "" ) );
	}

	public LocalFolder getProductFolder( ActionBase action , String dirname ) throws Exception {
		String dir = Common.getPath( action.context.session.productPath , dirname );
		return( artefactory.getAnyFolder( action , dir ) );
	}

	public String getMetadataPath( ActionBase action , String dirname ) throws Exception {
		String dir = Common.getPath( action.context.session.etcPath , dirname );
		return( dir );
	}

	public LocalFolder getMetadataFolder( ActionBase action , String dirname ) throws Exception {
		String dir = getMetadataPath( action , dirname );
		return( artefactory.getAnyFolder( action , dir ) );
	}

	public LocalFolder getInstallFolder( ActionBase action ) throws Exception {
		return( artefactory.getAnyFolder( action , action.context.session.execrc.installPath ) );
	}

	public LocalFolder getServerProductsFolder( ActionBase action ) throws Exception {
		LocalFolder folder = getInstallFolder( action );
		return( folder.getSubFolder( action , PRODUCTS_FOLDER ) );
	}
	
}
