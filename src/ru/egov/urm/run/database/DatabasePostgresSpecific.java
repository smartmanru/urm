package ru.egov.urm.run.database;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;

public class DatabasePostgresSpecific extends DatabaseSpecific {

	public VarPROCESSMODE getProcessStatus( ActionBase action , String hostLogin , String instance ) throws Exception {
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
		shell.customCheckStatus( action , "psql " + " < " + file + " > " + fileLog );
		return( true );
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
	
	public void uddiBegin( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	public void uddiEnd( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	public void uddiAddEndpoint( ActionBase action , String UDDI_KEY , String UDDI_UAT , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	public void smevAttrBegin( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	public void smevAttrEnd( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	public void smevAttrAddValue( ActionBase action , String UDDI_ATTR_ID , String UDDI_ATTR_NAME , String UDDI_ATTR_CODE , String UDDI_ATTR_REGION , String UDDI_ATTR_ACCESSPOINT , 
			LocalFolder dstDir , String outfile ) throws Exception {
	}

}
