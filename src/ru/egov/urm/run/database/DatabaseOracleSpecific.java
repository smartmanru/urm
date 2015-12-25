package ru.egov.urm.run.database;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;

public class DatabaseOracleSpecific extends DatabaseSpecific {

	public VarPROCESSMODE getProcessStatus( ActionBase action , String hostLogin , String instance ) throws Exception {
		ShellExecutor shell = action.getShell( hostLogin );
		String value = shell.customGetValue( action , "echo " + 
				Common.getQuoted( "select 'status=' || status from gv\\$instance where instance_name = '" + instance + "';" ) + 
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

	@Override public boolean checkConnect( ActionBase action , MetaEnvServer server , String user , String password ) throws Exception {
		String dbmsAddr = server.DBMSADDR;
		String value = action.session.customGetValue( action ,  
				"(echo " + Common.getQuoted( "'value=ok' as x from dual\\;" ) +  
				" ) | sqlplus " + user + "/" + password + "@" + dbmsAddr );
		if( value.indexOf( "connect_ok" ) >= 0 )
			return( true );
		return( false );
	}

	@Override public boolean applySystemScript( ActionBase action , MetaEnvServer server , ShellExecutor shell , String file , String fileLog ) throws Exception {
		shell.customCheckStatus( action , "sqlplus / " + Common.getQuoted( "as sysdba" ) + " < " + file + " > " + fileLog );
		return( true );
	}

	@Override public String readCellValue( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String column , String condition ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}
	
	@Override public void readTableData( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String condition , String[] columns , List<String[]> rows ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void createTableData( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , String columntypes[] , List<String[]> rows ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void writeTableData( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , List<String[]> rows ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void insertRow( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , String[] values ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void updateRow( ActionBase action , MetaEnvServer server , String schema , String user , String password , String table , String[] columns , String[] values , String condition ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public boolean applyScript( ActionBase action , MetaEnvServer server , String schema , String user , String password , String scriptFile , String outFile ) throws Exception {
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
