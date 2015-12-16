package ru.egov.urm.run.database;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
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
		String user = server.admSchema.DBUSER;
		String pwd = getUserPassword( action , user );
		return( specific.checkConnect( action , server , user , pwd ) );
	}
	
	public String getUserPassword( ActionBase action , String user ) throws Exception {
		String dbmsAddr = server.DBMSADDR;
		
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

	public boolean applyManualSet( ActionBase action , LocalFolder files ) throws Exception {
		if( specific == null )
			action.exit( "need to check connectivity first" );
			
		// copy folder to remote
		String hostLogin = getDatabaseAccount( action );
		RedistStorage storage = action.artefactory.getRedistStorage( "database" , hostLogin );
		RemoteFolder folder = storage.getRedistTmpFolder( action );
		folder.recreateThis( action );
		
		FileSet set = files.getFileSet( action );
		if( set.isEmpty() ) {
			action.log( "nothing to apply" );
			return( true );
		}

		folder.copyDirContentFromLocal( action , files , "" );
		ShellExecutor shell = action.getShell( hostLogin );
		
		RemoteFolder logFolder = folder.getSubFolder( action , "out" );
		logFolder.ensureExists( action );
		
		boolean res = true;
		for( String file : Common.getSortedKeys( set.files ) ) {
			if( !applyManualScript( action , shell , folder , file , logFolder ) ) {
				res = false;
				if( !action.options.OPT_FORCE ) {
					action.log( "error executing manual script, cancel set execution" );
					break;
				}
			}
		}

		logFolder.copyDirToLocal( action , files );
		return( res );
	}
	
	private boolean applyManualScript( ActionBase action , ShellExecutor shell , RemoteFolder folder , String file , RemoteFolder logFolder ) throws Exception {
		action.log( server.NAME + ": apply " + file + " ..." );
		
		String fileRun = folder.getFilePath( action , Common.getBaseName( file ) );
		String fileLog = logFolder.getFilePath( action , Common.getBaseName( file ) + ".out" );
		
		action.executeLogLive( shell , "apply manual script: " + file );
		if( action.context.SHOWONLY )
			return( true );
		
		return( specific.applySystemScript( action , server , shell , fileRun , fileLog ) );
	}

	public String getDatabaseAccount( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : server.getNodes( action ) )
			if( !node.OFFLINE )
				return( node.HOSTLOGIN );
		action.exit( "server " + server.NAME + " has no online nodes defined" );
		return( null );
	}

	public String readCellValue( ActionBase action , MetaDatabaseSchema schema , String table , String column , String ansiCondition ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		return( specific.readCellValue( action , server , schema.DBNAME , schema.DBUSER , password , table , column , ansiCondition ) );
	}

	public List<String[]> readTableData( ActionBase action , MetaDatabaseSchema schema , String table , String ansiCondition , String[] columns ) throws Exception {
		List<String[]> list = new LinkedList<String[]>();
		String password = getUserPassword( action , schema.DBUSER );
		specific.readTableData( action , server , schema.DBNAME , schema.DBUSER , password , table , ansiCondition , columns , list );
		return( list );
	}

	public void insertRow( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , String[] values ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		specific.insertRow( action , server , schema.DBNAME , schema.DBUSER , password , table , columns , values );
	}
	
	public void updateRow( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , String[] values , String ansiCondition ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		specific.updateRow( action , server , schema.DBNAME , schema.DBUSER , password , table , columns , values , ansiCondition );
	}
	
}
