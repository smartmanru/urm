package ru.egov.urm.action.database;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.UrmStorage;

public class DatabaseSpecific {

	VarDBMSTYPE dbmsType;
	MetaEnvServer server;
	MetaEnvServerNode node;

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
	
	public boolean checkConnect( ActionBase action , String schema , String user , String password ) throws Exception {
		String ctxScript = getContextScript( action , schema , user , password );
		int status = runScriptCmd( action , ctxScript , "checkconnect" , "" );
		if( status != 0 )
			return( false );
		
		return( false );
	}
	
	public boolean applySystemScript( ActionBase action , ShellExecutor shell , String file , String fileLog ) throws Exception {
		int status = shell.customGetStatus( action , "psql -a -e " + " < " + file + " > " + fileLog + " 2>&1" );
		if( status != 0 ) {
			action.log( "errors, status=" + status + " (see logs)" );
			return( false );
		}
		
		String err = shell.customGetValue( action , "cat " + fileLog + " | grep ^ERROR: | head -1" );
		if( err.isEmpty() )
			return( true );
		
		action.log( "error: " + err + " (see logs)" );
		return( false );
	}
	
	public String readCellValue( ActionBase action , String schema , String user , String password , String table , String column , String condition ) throws Exception {
//		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
//				"(echo " + Common.getQuoted( "select 'value=' || " + column + " as x from " + table + 
//						" where " + condition + ";" ) +  
//				" ) | psql -A -q -t -d " + schema + " -h " + dbmsAddrHost + " -U " + user );
//		
//		if( value.indexOf( "ERROR:" ) >= 0 )
//			action.exit( "unexpected error: " + value );
//		
//		if( value.indexOf( "value=" ) < 0 )
//			return( null );
//
//		return( Common.getPartAfterFirst( value , "value=" ) );
		return( null );
	}

	public void readTableData( ActionBase action , String schema , String user , String password , String table , String condition , String[] columns , List<String[]> rows ) throws Exception {
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
	
	public void createTableData( ActionBase action , String schema , String user , String password , String table , String[] columns , String columntypes[] , List<String[]> rows ) throws Exception {
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
	
	private void writeTableDataInternal( ActionBase action , String schema , String user , String password , String table , String[] columns , List<String[]> rows , List<String> lines ) throws Exception {
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
	
	public void writeTableData( ActionBase action , String schema , String user , String password , String table , String[] columns , List<String[]> rows ) throws Exception {
//		List<String> lines = new LinkedList<String>();
//		writeTableDataInternal( action , schema , user , password , table , columns , rows , lines );
	}
	
	public void insertRow( ActionBase action , String schema , String user , String password , String table , String[] columns , String[] values ) throws Exception {
//		String query = getInsertRowString( action , table , columns , values );
//		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
//				"(echo " + Common.getQuoted( query ) +  
//				" ) | psql -d " + schema + " -h " + dbmsAddrHost + " -U " + user );
//		
//		if( value.indexOf( "ERROR:" ) >= 0 )
//			action.exit( "unexpected error: " + value );
	}
	
	public void updateRow( ActionBase action , String schema , String user , String password , String table , String[] columns , String[] values , String condition ) throws Exception {
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
	
	public boolean applyScript( ActionBase action , String schema , String user , String password , String scriptFile , String outFile ) throws Exception {
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

	private String getContextScript( ActionBase action , String schema , String user , String password ) throws Exception {
//		String dbmsAddrDB = getAdmSchema( action );
//		String dbmsAddrHost = node.getHost( action );
		return( "" );
	}

	private int runScriptCmd( ActionBase action , String ctxFile , String cmd , String params ) throws Exception {
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder scripts = urm.getSqlScripts( action , server );
		
		String ctxCmd = null;
		if( action.isLinux() )
			ctxCmd = "( . " + ctxFile + "; ./" + cmd + ".sh " + params + " )";
		else
		if( action.isWindows() )
			ctxCmd = "call " + ctxFile + " && call " + cmd + ".cmd " + params;
		else
			action.exitUnexpectedState();
		
		int status = action.session.customGetStatus( action , scripts.folderPath , ctxCmd );
		return( status );
	}
	
}
