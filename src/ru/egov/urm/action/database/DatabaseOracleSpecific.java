package ru.egov.urm.action.database;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;

public class DatabaseOracleSpecific extends DatabaseSpecific {

	public DatabaseOracleSpecific() {
		super();
	}
	
	public VarPROCESSMODE getProcessStatus( ActionBase action ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		String value = shell.customGetValue( action , "echo " + 
				Common.getQuoted( "select 'status=' || status from gv\\$instance where instance_name = '" + node.INSTANCE + "';" ) + 
				" | sqlplus -S / " + Common.getQuoted( "as sysdba" ) );
		
		if( value.indexOf( "ORACLE not available" ) >= 0 )
			return( VarPROCESSMODE.STOPPED );
		if( value.indexOf( "command not found" ) >= 0 )
			return( VarPROCESSMODE.UNKNOWN );
		if( value.indexOf( "status=OPEN" ) >= 0 )
			return( VarPROCESSMODE.STARTED );
		if( value.indexOf( "status=STARTED" ) >= 0 )
			return( VarPROCESSMODE.STARTING );
		
		return( VarPROCESSMODE.ERRORS );
	}

	@Override public boolean checkConnect( ActionBase action , String user , String password ) throws Exception {
		String dbmsAddr = action.getNodeAccount( node ).HOST;
		String value = action.session.customGetValue( action ,  
				"(echo " + Common.getQuoted( "'value=ok' as x from dual\\;" ) +  
				" ) | sqlplus " + user + "/" + password + "@" + dbmsAddr );
		if( value.indexOf( "connect_ok" ) >= 0 )
			return( true );
		return( false );
	}

	@Override public boolean applySystemScript( ActionBase action , ShellExecutor shell , String file , String fileLog ) throws Exception {
		shell.customCheckStatus( action , "sqlplus / " + Common.getQuoted( "as sysdba" ) + " < " + file + " > " + fileLog );
		return( true );
	}

	@Override public String readCellValue( ActionBase action , String schema , String user , String password , String table , String column , String condition ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}
	
	@Override public void readTableData( ActionBase action , String schema , String user , String password , String table , String condition , String[] columns , List<String[]> rows ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void createTableData( ActionBase action , String schema , String user , String password , String table , String[] columns , String columntypes[] , List<String[]> rows ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void writeTableData( ActionBase action , String schema , String user , String password , String table , String[] columns , List<String[]> rows ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void insertRow( ActionBase action , String schema , String user , String password , String table , String[] columns , String[] values ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void updateRow( ActionBase action , String schema , String user , String password , String table , String[] columns , String[] values , String condition ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public boolean applyScript( ActionBase action , String schema , String user , String password , String scriptFile , String outFile ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public boolean validateScriptContent( ActionBase action , LocalFolder dir , String P_SCRIPT ) throws Exception {
		String cmd = "sed '/^[ ]*$/d' " + P_SCRIPT + " | tail -2 | tr -d " + Common.getQuoted( "\\n\\r" ) + " | tr -d " + Common.getQuoted( " " ) + " | tr '[a-z]' '[A-Z]' | grep -ce " + Common.getQuoted( "END;\\$" );
		String value = action.session.customGetValue( action , dir.folderPath , cmd );
		if( !value.equals( "0" ) ) {
			action.log( "no trailing slash on BEGIN-END block, sqlplus may hang - " + P_SCRIPT + " (END;)" );
			return( false );
		}
		return( true );
	}
	
	@Override public String getComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile ) throws Exception {
		return( "" );
	}
	
	@Override public void grepComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	@Override public void addComment( ActionBase action , String comment , LocalFolder dstDir , String outfile ) throws Exception {
	}

}
