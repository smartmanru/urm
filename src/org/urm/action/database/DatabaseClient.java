package org.urm.action.database;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class DatabaseClient {

	public DatabaseSpecific specific;
	
	public DatabaseClient() {
	}

	public boolean checkConnect( ActionBase action , MetaEnvServer server ) throws Exception {
		MetaEnvServerNode node = server.getMasterNode( action );
		return( checkConnect( action , server , node ) );
	}
	
	public boolean checkConnect( ActionBase action , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		specific = new DatabaseSpecific( server , node );
		
		// check connect to admin schema if any
		String schema = specific.getAdmSchema( action );
		if( schema.isEmpty() )
			return( true );
		
		String user = specific.getAdmUser( action );
		String pwd = getUserPassword( action , user );
		try { 
			action.debug( "check connect to database server=" + server.NAME + ", node=" + node.POS + " ..." );
			return( specific.checkConnect( action , schema , user , pwd ) );
		}
		catch( Throwable e ) {
			action.handle( e );
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
		if( !specific.server.sg.env.DB_AUTHFILE.isEmpty() ) {
			String F_FNAME = specific.server.sg.env.DB_AUTHFILE;
			if( !action.shell.checkFileExists( action , F_FNAME ) )
				action.exit1( _Error.PasswordFileNotExist1 , "getSchemaPassword: password file " + F_FNAME + " does not exist" , F_FNAME );

			// get password
			S_DB_USE_SCHEMA_PASSWORD = action.shell.customGetValue( action , 
					"cat " + F_FNAME + " | grep " + Common.getQuoted( "^" + serverId + "." + user + "=" ) +
					" | cut -d \"=\" -f2 | tr -d \"\n\r\"" );
			
			if( S_DB_USE_SCHEMA_PASSWORD.isEmpty() )
				action.exit3( _Error.UnableFindPassword3 , "getSchemaPassword: unable to find password for dbms=" + serverId + ", schema=" + user + 
						" in " + F_FNAME , serverId , user , F_FNAME );
		}
		else
			action.exit0( _Error.UnableDeriveAuthType0 , "getSchemaPassword: unable to derive auth type" );
		
		return( S_DB_USE_SCHEMA_PASSWORD );
	}

	public boolean applyManualSet( ActionBase action , LocalFolder files ) throws Exception {
		if( specific == null )
			action.exit0( _Error.NeedCheckConnectivity0 , "need to check connectivity first" );
			
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
		for( String file : set.getAllFiles() ) {
			if( !applyManualScript( action , shell , folder , file , logFolder ) ) {
				res = false;
				if( !action.isForced() ) {
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
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		return( specific.readCellValue( action , specific.getSchemaDBName( schema ) , dbuser , password , table , column , ansiCondition ) );
	}

	public List<String[]> readTableData( ActionBase action , MetaDatabaseSchema schema , String table , String ansiCondition , String[] columns ) throws Exception {
		List<String[]> list = new LinkedList<String[]>();
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		specific.readTableData( action , specific.getSchemaDBName( schema ) , dbuser , password , table , ansiCondition , columns , list );
		return( list );
	}

	public List<String[]> readSelectData( ActionBase action , MetaDatabaseSchema schema , String select ) throws Exception {
		List<String[]> list = new LinkedList<String[]>();
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		specific.readSelectData( action , specific.getSchemaDBName( schema ) , dbuser , password , select , list );
		return( list );
	}

	public void createTableData( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , String[] columntypes , List<String[]> data ) throws Exception {
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		
		if( !action.isExecute() )
			return;
		
		specific.createTableData( action , specific.getSchemaDBName( schema ) , specific.getSchemaDBUser( schema ) , password , table , columns , columntypes , data );
	}

	public void writeTableData( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , List<String[]> data , boolean commit ) throws Exception {
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		if( !action.isExecute() )
			return;
		
		specific.writeTableData( action , specific.getSchemaDBName( schema ) , dbuser , password , table , columns , data , commit );
	}

	public boolean insertRow( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , String[] values , boolean commit ) throws Exception {
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		if( !action.isExecute() )
			return( true );
		
		return( specific.insertRow( action , specific.getSchemaDBName( schema ) , dbuser , password , table , columns , values , commit ) );
	}
	
	public boolean updateRow( ActionBase action , MetaDatabaseSchema schema , String table , String[] columns , String[] values , String ansiCondition , boolean commit ) throws Exception {
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		if( !action.isExecute() )
			return( true );
		
		return( specific.updateRow( action , specific.getSchemaDBName( schema ) , dbuser , password , table , columns , values , ansiCondition , commit ) );
	}

	public boolean deleteRows( ActionBase action , MetaDatabaseSchema schema , String table , String ansiCondition , boolean commit ) throws Exception {
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		if( !action.isExecute() )
			return( true );
		
		return( specific.deleteRows( action , specific.getSchemaDBName( schema ) , dbuser , password , table , ansiCondition , commit ) );
	}
	
	public boolean applyScript( ActionBase action , MetaDatabaseSchema schema , LocalFolder scriptFolder , String scriptFile , LocalFolder outFolder , String outFile ) throws Exception {
		String dbuser = specific.getSchemaDBUser( schema );
		String password = getUserPassword( action , dbuser );
		String file = scriptFolder.getFilePath( action , scriptFile );
		String log = outFolder.getFilePath( action , outFile );
		if( !action.isExecute() )
			return( true );
		
		return( specific.applyScript( action , specific.getSchemaDBName( schema ) , dbuser , password , file , log ) );
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
