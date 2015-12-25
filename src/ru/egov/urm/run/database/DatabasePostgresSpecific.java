package ru.egov.urm.run.database;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;

public class DatabasePostgresSpecific extends DatabaseSpecific {

	@Override public VarPROCESSMODE getProcessStatus( ActionBase action , String hostLogin , String instance ) throws Exception {
		ShellExecutor shell = action.getShell( hostLogin );
		String value = shell.customGetValue( action , "(echo " + Common.getQuoted( "select 'value=ok' as x;" ) + 
				" ) | psql -d " + instance );
		
		if( value.indexOf( "value=ok" ) >= 0 )
			return( VarPROCESSMODE.STARTED );
		
		return( VarPROCESSMODE.ERRORS );
	}

	@Override public boolean checkConnect( ActionBase action , MetaEnvServer server , String user , String password ) throws Exception {
		String dbmsAddrDB = server.admSchema.DBNAME;
		String dbmsAddrHost = server.DBMSADDR;
		
		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
				"(echo " + Common.getQuoted( "select 'value=ok' as x;" ) +  
				" ) | psql -d " + dbmsAddrDB + " -h " + dbmsAddrHost + " -U " + user );
		if( value.indexOf( "value=ok" ) >= 0 )
			return( true );
		return( false );
	}

	@Override public boolean applySystemScript( ActionBase action , MetaEnvServer server , ShellExecutor shell , String file , String fileLog ) throws Exception {
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

	@Override public String readCellValue( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String column , String condition ) throws Exception {
		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
				"(echo " + Common.getQuoted( "select 'value=' || " + column + " as x from " + table + 
						" where " + condition + ";" ) +  
				" ) | psql -A -q -t -d " + schema + " -h " + server.DBMSADDR + " -U " + user );
		
		if( value.indexOf( "ERROR:" ) >= 0 )
			action.exit( "unexpected error: " + value );
		
		if( value.indexOf( "value=" ) < 0 )
			return( null );

		return( Common.getPartAfterFirst( value , "value=" ) );
	}
	
	@Override public void readTableData( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String condition , String[] columns , List<String[]> rows ) throws Exception {
		String query = "select ";
		boolean first = true;
		for( String column : columns ) {
			if( !first )
				query += ", ";
			first = false;
			query += "'c=' || " + column;
		}
		query += " from " + table + " where " + condition;
		
		String[] data = action.session.customGetLines( action , "export PGPASSWORD='" + password + "'; " + 
				"(echo " + Common.getQuoted( query + ";" ) +  
				" ) | psql -A -q -t -d " + schema + " -h " + server.DBMSADDR + " -U " + user );

		for( String value : data ) {
			String[] values = Common.split( value , "\\|" );
			if( values.length != columns.length )
				action.exit( "unexpected table row output: " + value + " (" + values.length + ", " + columns.length + ")" );
			
			String[] row = new String[ columns.length ];
			int pos = 0;
			for( String s : values ) {
				if( !s.startsWith( "c=" ) )
					action.exit( "unexpected table row output: " + value );
				
				row[ pos ] = s.substring( 2 );
				pos++;
			}
			
			rows.add( row );
		}
	}

	@Override public void createTableData( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , String columntypes[] , List<String[]> rows ) throws Exception {
		List<String> lines = new LinkedList<String>();
		lines.add( "DROP TABLE IF EXISTS " + table );
		String ct = "create table " + table + " ( ";
		if( columns.length != columntypes.length )
			action.exit( "invalid column names and types" );
		
		for( int k = 0; k < columns.length; k++ ) {
			String type = columntypes[ k ];
			ct += columns[k] + " " + type + "; ";
		}
		ct += " );";
		lines.add( ct );
				
		writeTableDataInternal( action , server , schema , user , password , table , columns , rows , lines );
	}

	@Override public void writeTableData( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , List<String[]> rows ) throws Exception {
		List<String> lines = new LinkedList<String>();
		writeTableDataInternal( action , server , schema , user , password , table , columns , rows , lines );
	}
	
	public void writeTableDataInternal( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , List<String[]> rows , List<String> lines ) throws Exception {
		for( String[] values : rows ) {
			String query = getInsertRowString( action , table , columns , values );
			lines.add( query );
		}
		lines.add( "commit;" );
		
		LocalFolder work = action.artefactory.getWorkFolder( action );
		String scriptFile = work.getFilePath( action , "run.sql" );
		Common.createFileFromStringList( scriptFile , lines );

		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
				"cat " + scriptFile +  
				" | psql -d " + schema + " -h " + server.DBMSADDR + " -U " + user );
		
		if( value.indexOf( "ERROR:" ) >= 0 )
			action.exit( "unexpected error: " + value );
	}

	private String getInsertRowString( ActionBase action , String table , String[] columns , String[] values ) throws Exception {
		if( values.length != columns.length )
			action.exit( "number of values should be equal to number of columns" );
			
		String query = "insert into " + table + " (";
		boolean first = true;
		for( String column : columns ) {
			if( !first )
				query += ", ";
			
			first = false;
			query += column; 
		}
		query += " ) values (";
		
		first = true;
		for( String value : values ) {
			if( !first )
				query += ", ";
			
			first = false;
			if( value.equals( "TIMESTAMP" ) )
				query += "now()";
			else
				query += value;
		}
		query += " );";
		
		return( query );
	}
	
	@Override public void insertRow( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , String[] values ) throws Exception {
		String query = getInsertRowString( action , table , columns , values );
		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
				"(echo " + Common.getQuoted( query ) +  
				" ) | psql -d " + schema + " -h " + server.DBMSADDR + " -U " + user );
		
		if( value.indexOf( "ERROR:" ) >= 0 )
			action.exit( "unexpected error: " + value );
	}
	
	@Override public void updateRow( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , String[] values , String condition ) throws Exception {
		if( values.length != columns.length )
			action.exit( "number of values should be equal to number of columns" );
			
		String query = "update " + table + " set ";
		for( int pos = 0; pos < columns.length; pos++ ) {
			if( pos > 0 )
				query += ", ";
			
			query += columns[ pos ] + " = ";
			
			String value = values[ pos ];
			if( value.equals( "TIMESTAMP" ) )
				query += "now()";
			else
				query += value;
		}
		query += " where " + condition;
		
		String value = action.session.customGetValue( action , "export PGPASSWORD='" + password + "'; " + 
				"(echo " + Common.getQuoted( query ) +  
				" ) | psql -d " + schema + " -h " + server.DBMSADDR + " -U " + user );
		
		if( value.indexOf( "ERROR:" ) >= 0 )
			action.exit( "unexpected error: " + value );
	}

	@Override public boolean applyScript( ActionBase action , MetaEnvServer server , String schema , String user , String password , String scriptFile , String outFile ) throws Exception {
		action.session.customCheckStatus( action , "export PGPASSWORD='" + password + "'; " + 
				"cat " + scriptFile + " | psql -d " + schema + " -h " + server.DBMSADDR + " -U " + user + " > " + outFile + " 2>&1" );
		
		String err = action.session.customGetValue( action , "cat " + outFile + " | grep ^ERROR: | head -1" );
		if( err.isEmpty() )
			return( true );
		
		action.log( "error: " + err + " (see logs at + " + outFile + ")" );
		return( false );
	}
	
	@Override public boolean validateScriptContent( ActionBase action , LocalFolder dir , String script ) throws Exception {
		return( true );
	}
	
	@Override public String getComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile ) throws Exception {
		return( "" );
	}
	
	@Override public void grepComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	@Override public void addComment( ActionBase action , String comment , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	@Override public void uddiBegin( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	@Override public void uddiEnd( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	@Override public void uddiAddEndpoint( ActionBase action , String UDDI_KEY , String UDDI_UAT , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	@Override public void smevAttrBegin( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	@Override public void smevAttrEnd( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	@Override public void smevAttrAddValue( ActionBase action , String UDDI_ATTR_ID , String UDDI_ATTR_NAME , String UDDI_ATTR_CODE , String UDDI_ATTR_REGION , String UDDI_ATTR_ACCESSPOINT , 
			LocalFolder dstDir , String outfile ) throws Exception {
	}

}
