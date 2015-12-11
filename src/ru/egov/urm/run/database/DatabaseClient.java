package ru.egov.urm.run.database;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDatabase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;

public class DatabaseClient {

	MetaEnvServer server;
	DatabaseSpecific specific;
	
	public DatabaseClient( MetaEnvServer server ) {
		this.server = server;
	}

	public boolean checkConnect( ActionBase action ) throws Exception {
		specific = DatabaseSpecific.getSpecificHandler( action , server.DBMSTYPE );
		
		// check connect to admin schema
		MetaDatabase db = action.meta.distr.database;
		String user = db.getAdmUser( action );
		String pwd = getUserPassword( action , server.DBMSADDR , user );
		return( specific.checkConnect( action , server , user , pwd ) );
	}
	
	public String getUserPassword( ActionBase action , String dbmsAddr , String user ) throws Exception {
		String S_DB_USE_SCHEMA_PASSWORD = "";
		if( !action.context.DB_AUTH )
			S_DB_USE_SCHEMA_PASSWORD = user;
		else
		if( action.options.OPT_DBPASSWORD.isEmpty() )
			S_DB_USE_SCHEMA_PASSWORD = action.options.OPT_DBPASSWORD;
		else
		if( !action.meta.env.DB_AUTHFILE.isEmpty() ) {
			String F_FNAME = action.meta.env.DB_AUTHFILE;
			if( !action.session.checkFileExists( action , F_FNAME ) )
				action.exit( "getSchemaPassword: password file " + F_FNAME + " does not exist" );

			// get password
			S_DB_USE_SCHEMA_PASSWORD = action.session.customGetValue( action , 
					"cat " + F_FNAME + " | grep " + Common.getQuoted( "^" + dbmsAddr + "." + user + "=" ) +
					" | cut -d \"=\" -f2 | tr -d \"\n\r\"" );
			
			if( S_DB_USE_SCHEMA_PASSWORD.isEmpty() )
				action.exit( "getSchemaPassword: unable to find password for dbms=" + dbmsAddr + ", schema=" + user + 
						" in " + F_FNAME );
		}
		else
			action.exit( "getSchemaPassword: unable to derive auth type" );
		
		return( S_DB_USE_SCHEMA_PASSWORD );
	}

	public void applyManualScript( ActionBase action , String file ) throws Exception {
		if( specific == null )
			action.exit( "need to check connectivity first" );
			
		action.log( server.NAME + ": apply " + file + " ..." );
		
		// copy file to remote
		String hostLogin = getDatabaseAccount( action );
		RedistStorage storage = action.artefactory.getRedistStorage( hostLogin );
		RemoteFolder folder = storage.getRedistTmpFolder( action );
		folder.copyFileFromLocal( action , file );
		
		String fileRun = folder.getFilePath( action , Common.getBaseName( file ) );
		String fileLog = fileRun + ".out";
		
		ShellExecutor shell = action.getShell( hostLogin );
		specific.applySystemScript( action , server , shell , fileRun , fileLog );
	}

	public String getDatabaseAccount( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : server.getNodes( action ) )
			if( !node.OFFLINE )
				return( node.HOSTLOGIN );
		action.exit( "server " + server.NAME + " has no online nodes defined" );
		return( null );
	}
	
}
