package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.meta.Metadata.VarOSTYPE;

public class UrmStorage {

	public Artefactory artefactory;
	public Metadata meta;
	
	public UrmStorage( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
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
		if( ostype == VarOSTYPE.UNIX )
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
		
		LocalFolder folder = artefactory.getProductFolder( action , Common.getPath( parentPath , folderPath ) );
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
	
}
