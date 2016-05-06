package ru.egov.urm.action.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.Folder;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;
import ru.egov.urm.storage.UrmStorage;

public class DatabaseSpecific {

	VarDBMSTYPE dbmsType;
	MetaEnvServer server;
	MetaEnvServerNode node;

	Map<String,String> ctxFiles = new HashMap<String,String>();
	String lastValue = null;
	boolean applysystemscriptCopied = false;
	
	protected DatabaseSpecific() {
	}
	
	public DatabaseSpecific( VarDBMSTYPE dbmsType ) {
		this.dbmsType = dbmsType; 
	}

	public DatabaseSpecific( MetaEnvServer server , MetaEnvServerNode node ) {
		this.server = server;
		this.node = node;
		this.dbmsType = server.dbType; 
	}

	public String getAdmUser( ActionBase action ) throws Exception {
		return( server.admSchema.DBUSER );
	}
	
	public String getAdmSchema( ActionBase action ) throws Exception {
		return( server.admSchema.DBNAME );
	}
	
	public boolean checkConnect( ActionBase action , String dbschema , String user , String password ) throws Exception {
		String ctxScript = getContextScript( action , dbschema , user , password );
		int status = runScriptCmd( action , ctxScript , "checkconnect" , "" );
		if( status != 0 )
			return( false );
		return( true );
	}
	
	public boolean applySystemScript( ActionBase action , ShellExecutor shell , String file , String fileLog ) throws Exception {
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder scripts = urm.getSqlScripts( action , server );
		
		Folder execFolder = scripts;
		String applyName = "applysystemscript" + ( ( shell.isLinux() )? ".sh" : ".cmd" );
		
		if( !applysystemscriptCopied ) {
			if( !action.isLocal() ) {
				RedistStorage redist = action.artefactory.getRedistStorage( action , server , node );
				RemoteFolder folder = redist.getRedistTmpFolder( action );
				execFolder = folder;
				
				folder.copyFileFromLocal( action , scripts , applyName );
				applysystemscriptCopied = true;
			}
		}
		
		String cmd = null;
		if( shell.isLinux() )
			cmd = "./" + applyName + " " + file + " " + fileLog;
		else
			cmd = applyName + " " + file + " " + fileLog;
		
		int status = shell.customGetStatus( action , execFolder.folderPath , cmd );
		if( status != 0 ) {
			action.log( "errors, status=" + status + " (see logs)" );
			return( false );
		}
		
		String err = "";
		if( shell.isLinux() ) {
			err = shell.customGetValue( action , "cat " + fileLog + " | grep ^ERROR: | head -1" );
		}
		else {
			String[] lines = shell.customGetLines( action , "type " + fileLog + " | findstr ^ERROR:" );
			if( lines.length > 0 )
				err = lines[0];
		}
		
		if( err.isEmpty() )
			return( true );
		
		action.log( "error: " + err + " (see logs)" );
		return( false );
	}
	
	public String readCellValue( ActionBase action , String dbschema , String user , String password , String table , String column , String condition ) throws Exception {
		String ctxScript = getContextScript( action , dbschema , user , password );
		if( !runScriptCmdGetValueCheckStatus( action , ctxScript , "readcellvalue" , table + " " + column + " " + Common.getQuoted( condition ) ) )
			action.exit( "unexpected error" );
		
		return( lastValue );
	}

	public void readTableData( ActionBase action , String dbschema , String user , String password , String table , String condition , String[] columns , List<String[]> rows ) throws Exception {
//		String query = "select ";
//		boolean first = true;
//		for( String column : columns ) {
//			if( !first )
//				query += ", ";
//			first = false;
//			query += "'c=' || " + column;
//		}
//		query += " from " + table + " where " + condition;
//		
//		String[] data = action.session.customGetLines( action , "export PGPASSWORD='" + password + "'; " + 
//				"(echo " + Common.getQuoted( query + ";" ) +  
//				" ) | psql -A -q -t -d " + schema + " -h " + dbmsAddrHost + " -U " + user );
//
//		for( String value : data ) {
//			String[] values = Common.split( value , "\\|" );
//			if( values.length != columns.length )
//				action.exit( "unexpected table row output: " + value + " (" + values.length + ", " + columns.length + ")" );
//			
//			String[] row = new String[ columns.length ];
//			int pos = 0;
//			for( String s : values ) {
//				if( !s.startsWith( "c=" ) )
//					action.exit( "unexpected table row output: " + value );
//				
//				row[ pos ] = s.substring( 2 );
//				pos++;
//			}
//			
//			rows.add( row );
//		}
	}
	
	public void createTableData( ActionBase action , String dbschema , String user , String password , String table , String[] columns , String columntypes[] , List<String[]> rows ) throws Exception {
//		List<String> lines = new LinkedList<String>();
//		lines.add( "DROP TABLE IF EXISTS " + table + ";" );
//		
//		String ct = "create table " + table + " ( ";
//		if( columns.length != columntypes.length )
//			action.exit( "invalid column names and types" );
//		
//		for( int k = 0; k < columns.length; k++ ) {
//			if( k > 0 )
//				ct += ", ";
//			String type = columntypes[ k ];
//			ct += columns[k] + " " + type + "";
//		}
//		ct += " );";
//		lines.add( ct );
//				
//		writeTableDataInternal( action , schema , user , password , table , columns , rows , lines );
	}
	
	private void writeTableDataInternal( ActionBase action , String dbschema , String user , String password , String table , String[] columns , List<String[]> rows , List<String> lines ) throws Exception {
//		lines.add( "begin;" );
//		for( String[] values : rows ) {
//			String query = getInsertRowString( action , table , columns , values );
//			lines.add( query );
//		}
//		lines.add( "commit;" );
//		
//		LocalFolder work = action.artefactory.getWorkFolder( action );
//		String scriptFile = work.getFilePath( action , "run.sql" );
//		Common.createFileFromStringList( scriptFile , lines );
//
//		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
//				"cat " + scriptFile +  
//				" | psql -d " + schema + " -h " + dbmsAddrHost + " -U " + user + " 2>&1" );
//		
//		if( value.indexOf( "ERROR:" ) >= 0 )
//			action.exit( "unexpected error: " + value );
	}

	private String getInsertRowString( ActionBase action , String table , String[] columns , String[] values ) throws Exception {
//		if( values.length != columns.length )
//			action.exit( "number of values should be equal to number of columns" );
//			
//		String query = "insert into " + table + " (";
//		boolean first = true;
//		for( String column : columns ) {
//			if( !first )
//				query += ", ";
//			
//			first = false;
//			query += column; 
//		}
//		query += " ) values (";
//		
//		first = true;
//		for( String value : values ) {
//			if( !first )
//				query += ", ";
//			
//			first = false;
//			if( value.equals( "TIMESTAMP" ) )
//				query += "now()";
//			else
//				query += value;
//		}
//		query += " );";
//		
//		return( query );
		return( null );
	}
	
	public void writeTableData( ActionBase action , String dbschema , String user , String password , String table , String[] columns , List<String[]> rows ) throws Exception {
//		List<String> lines = new LinkedList<String>();
//		writeTableDataInternal( action , schema , user , password , table , columns , rows , lines );
	}
	
	public void insertRow( ActionBase action , String dbschema , String user , String password , String table , String[] columns , String[] values ) throws Exception {
//		String query = getInsertRowString( action , table , columns , values );
//		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
//				"(echo " + Common.getQuoted( query ) +  
//				" ) | psql -d " + schema + " -h " + dbmsAddrHost + " -U " + user );
//		
//		if( value.indexOf( "ERROR:" ) >= 0 )
//			action.exit( "unexpected error: " + value );
	}
	
	public void updateRow( ActionBase action , String dbschema , String user , String password , String table , String[] columns , String[] values , String condition ) throws Exception {
//		if( values.length != columns.length )
//			action.exit( "number of values should be equal to number of columns" );
//			
//		String query = "update " + table + " set ";
//		for( int pos = 0; pos < columns.length; pos++ ) {
//			if( pos > 0 )
//				query += ", ";
//			
//			query += columns[ pos ] + " = ";
//			
//			String value = values[ pos ];
//			if( value.equals( "TIMESTAMP" ) )
//				query += "now()";
//			else
//				query += value;
//		}
//		query += " where " + condition;
//		
//		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
//				"(echo " + Common.getQuoted( query ) +  
//				" ) | psql -d " + schema + " -h " + dbmsAddrHost + " -U " + user );
//		
//		if( value.indexOf( "ERROR:" ) >= 0 )
//			action.exit( "unexpected error: " + value );
	}
	
	public boolean applyScript( ActionBase action , String dbschema , String user , String password , String scriptFile , String outFile ) throws Exception {
//		action.session.customCheckStatus( action , "export PGPASSWORD='" + password + "'; " + 
//				"cat " + scriptFile + " | psql -d " + schema + " -h " + dbmsAddrHost + " -U " + user + " > " + outFile + " 2>&1" );
//		
//		String err = action.session.customGetValue( action , "cat " + outFile + " | grep ^ERROR: | head -1" );
//		if( err.isEmpty() )
//			return( true );
//		
//		action.log( "error: " + err + " (see logs at " + outFile + ")" );
		return( false );
	}
	
	public boolean validateScriptContent( ActionBase action , LocalFolder dir , String script ) throws Exception {
		return( true );
	}
	
	public String getComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile ) throws Exception {
		return( "" );
	}
	
	public void grepComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	public void addComment( ActionBase action , String comment , LocalFolder dstDir , String outfile ) throws Exception {
	}

	private String getContextScript( ActionBase action , String dbschema , String user , String password ) throws Exception {
		String key = server.NAME + "." + dbschema + "." + user;
		String ctxFile = ctxFiles.get( key );
		if( ctxFile != null )
			return( ctxFile );

		LocalFolder work = action.artefactory.getWorkFolder( action );
		List<String> lines = new LinkedList<String>();
		String name = null;
		String DBHOST = ( action.isLocal() )? "localhost" : server.DBMSADDR;
		if( action.isLinux() ) {
			lines.add( "export URMDB_USER=" + user );
			lines.add( "export URMDB_PWD=" + password );
			lines.add( "export URMDB_DBHOST=" + DBHOST );
			lines.add( "export URMDB_DBNAME=" + dbschema );
			name = "urmdb." + key + ".sh"; 
		}
		else
		if( action.isWindows() ) {
			lines.add( "set URMDB_USER=" + user );
			lines.add( "set URMDB_PWD=" + password );
			lines.add( "set URMDB_DBHOST=" + DBHOST );
			lines.add( "set URMDB_DBNAME=" + dbschema );
			name = "urmdb." + key + ".cmd"; 
		}
		else
			action.exitUnexpectedState();
		
		String file = work.getFilePath( action , name );
		Common.createFileFromStringList( file , lines );
		return( file );
	}

	private int runScriptCmd( ActionBase action , String ctxFile , String cmd , String params ) throws Exception {
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder scripts = urm.getSqlScripts( action , server );
		
		String ctxCmd = null;
		if( action.isLinux() )
			ctxCmd = "( . " + ctxFile + "; ./" + cmd + ".sh " + params + " )";
		else
		if( action.isWindows() )
			ctxCmd = "call " + Common.getWinPath( ctxFile ) + " && call " + cmd + ".cmd " + params;
		else
			action.exitUnexpectedState();
		
		int status = action.session.customGetStatus( action , scripts.folderPath , ctxCmd );
		if( status != 0 )
			return( status );
		
		String errors = action.session.getErrors( action );
		if( !errors.isEmpty() )
			return( -1 );
		
		return( 0 );
	}

	private boolean runScriptCmdGetValueCheckStatus( ActionBase action , String ctxFile , String cmd , String params ) throws Exception {
		int status = runScriptCmd( action , ctxFile , cmd , params );
		if( status != 0 )
			return( false );
		
		lastValue = action.session.getValue( action );
		if( lastValue.isEmpty() )
			return( true );
		
		lastValue = Common.getPartAfterFirst( lastValue , "value=" );
		return( true );
	}
	
}
