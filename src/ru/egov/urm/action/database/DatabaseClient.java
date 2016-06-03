package ru.egov.urm.action.database;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDatabaseSchema;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;

public class DatabaseClient {

	public DatabaseSpecific specific;
	
	public DatabaseClient() {
	}

	public boolean checkConnect( ActionBase action , MetaEnvServer server ) throws Exception {
		MetaEnvServerNode node = server.getActiveNode( action );
		return( checkConnect( action , server , node ) );
	}
	
	public boolean checkConnect( ActionBase action , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		specific = new DatabaseSpecific( server , node );
		
		// check connect to admin schema
		String schema = specific.getAdmSchema( action );
		String user = specific.getAdmUser( action );
		String pwd = getUserPassword( action , user );
		try { 
			action.debug( "check connect to database server=" + server.NAME + ", node=" + node.POS + " ..." );
			return( specific.checkConnect( action , schema , user , pwd ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		
		action.error( "unable to connect to database server=" + server.NAME );
		return( false );
	}
	
	public String getUserPassword( ActionBase action , String user ) throws Exception {
		String serverId = specific.server.getFullId( action );
		
		String S_DB_USE_SCHEMA_PASSWORD = "";
		if( !action.context.CTX_DBPASSWORD.isEmpty() )
			S_DB_USE_SCHEMA_PASSWORD = action.context.CTX_DBPASSWORD;
		else
		if( !action.context.CTX_DBAUTH )
			S_DB_USE_SCHEMA_PASSWORD = user;
		else
		if( !specific.server.dc.env.DB_AUTHFILE.isEmpty() ) {
			String F_FNAME = specific.server.dc.env.DB_AUTHFILE;
			if( !action.session.checkFileExists( action , F_FNAME ) )
				action.exit( "getSchemaPassword: password file " + F_FNAME + " does not exist" );

			// get password
			S_DB_USE_SCHEMA_PASSWORD = action.session.customGetValue( action , 
					"cat " + F_FNAME + " | grep " + Common.getQuoted( "^" + serverId + "." + user + "=" ) +
					" | cut -d \"=\" -f2 | tr -d \"\n\r\"" );
			
			if( S_DB_USE_SCHEMA_PASSWORD.isEmpty() )
				action.exit( "getSchemaPassword: unable to find password for dbms=" + serverId + ", schema=" + user + 
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
		Account account = action.getNodeAccount( specific.node );
		RedistStorage storage = action.artefactory.getRedistStorage( action , account );
		RemoteFolder folder = storage.getRedistTmpFolder( action , "database" );
		
		FileSet set = files.getFileSet( action );
		if( set.isEmpty() ) {
			action.info( "nothing to apply" );
			return( true );
		}

		folder.copyDirContentFromLocal( action , files , "" );
		ShellExecutor shell = action.getShell( account );
		
		RemoteFolder logFolder = folder.getSubFolder( action , "out" );
		logFolder.ensureExists( action );
		
		boolean res = true;
		for( String file : Common.getSortedKeys( set.files ) ) {
			if( !applyManualScript( action , shell , folder , file , logFolder ) ) {
				res = false;
				if( !action.context.CTX_FORCE ) {
					action.error( "error executing manual script, cancel set execution" );
					break;
				}
			}
		}

		if( action.isExecute() )
			logFolder.copyDirToLocal( action , files );
		return( res );
	}
	
	private boolean applyManualScript( ActionBase action , ShellExecutor shell , RemoteFolder folder , String file , RemoteFolder logFolder ) throws Exception {
		action.info( specific.server.NAME + " " + action.getMode() + ": apply " + file + " ..." );
		
		String fileRun = folder.getFilePath( action , Common.getBaseName( file ) );
		String fileLog = logFolder.getFilePath( action , Common.getBaseName( file ) + ".out" );
		
		if( !action.isExecute() )
			return( true );
		
		action.executeLogLive( shell , "apply manual script: " + file );
		if( !action.isExecute() )
			return( true );
		
		return( specific.applySystemScript( action , shell , fileRun , fileLog ) );
	}

	public Account getDatabaseAccount( ActionBase action ) throws Exception {
		return( action.getNodeAccount( specific.node ) );
	}

	public String readCellValue( ActionBase action , MetaDatabaseSchema schema , String table , String column , String ansiCondition ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		return( specific.readCellValue( action , schema.DBNAME , schema.DBUSER , password , table , column , ansiCondition ) );
	}

	public List<String[]> readTableData( ActionBase action , MetaDatabaseSchema schema , String table , String ansiCondition , String[] columns ) throws Exception {
		List<String[]> list = new LinkedList<String[]>();
		String password = getUserPassword( action , schema.DBUSER );
		specific.readTableData( action , schema.DBNAME , schema.DBUSER , password , table , ansiCondition , columns , list );
		return( list );
	}

	public List<String[]> readSelectData( ActionBase action , MetaDatabaseSchema schema , String select ) throws Exception {
		List<String[]> list = new LinkedList<String[]>();
		String password = getUserPassword( action , schema.DBUSER );
		specific.readSelectData( action , schema.DBNAME , schema.DBUSER , password , select , list );
		return( list );
	}

	public void createTableData( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , String[] columntypes , List<String[]> data ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		
		if( !action.isExecute() )
			return;
		
		specific.createTableData( action , schema.DBNAME , schema.DBUSER , password , table , columns , columntypes , data );
	}

	public void writeTableData( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , List<String[]> data , boolean commit ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		if( !action.isExecute() )
			return;
		
		specific.writeTableData( action , schema.DBNAME , schema.DBUSER , password , table , columns , data , commit );
	}

	public boolean insertRow( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , String[] values , boolean commit ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		if( !action.isExecute() )
			return( true );
		
		return( specific.insertRow( action , schema.DBNAME , schema.DBUSER , password , table , columns , values , commit ) );
	}
	
	public boolean updateRow( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , String[] values , String ansiCondition , boolean commit ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		if( !action.isExecute() )
			return( true );
		
		return( specific.updateRow( action , schema.DBNAME , schema.DBUSER , password , table , columns , values , ansiCondition , commit ) );
	}

	public boolean deleteRows( ActionBase action , MetaDatabaseSchema schema , String table , String ansiCondition , boolean commit ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		if( !action.isExecute() )
			return( true );
		
		return( specific.deleteRows( action , schema.DBNAME , schema.DBUSER , password , table , ansiCondition , commit ) );
	}
	
	public boolean applyScript( ActionBase action , MetaDatabaseSchema schema , LocalFolder scriptFolder , String scriptFile , LocalFolder outFolder , String outFile ) throws Exception {
		String password = getUserPassword( action , schema.DBUSER );
		String file = scriptFolder.getFilePath( action , scriptFile );
		String log = outFolder.getFilePath( action , outFile );
		if( !action.isExecute() )
			return( true );
		
		return( specific.applyScript( action , schema.DBNAME , schema.DBUSER , password , file , log ) );
	}
	
	public boolean applyAdmScript( ActionBase action , LocalFolder scriptFolder , String scriptFile , LocalFolder outFolder , String outFile ) throws Exception {
		String DBUSER = specific.getAdmUser( action );
		String DBSCHEMA = specific.getAdmSchema( action );
		String password = getUserPassword( action , DBUSER );
		String file = scriptFolder.getFilePath( action , scriptFile );
		String log = outFolder.getFilePath( action , outFile );
		if( !action.isExecute() )
			return( true );
		
		return( specific.applyScript( action , DBSCHEMA , DBUSER , password , file , log ) );
	}
	
}
