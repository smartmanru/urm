package ru.egov.urm.run.database;

import java.util.List;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;

abstract public class DatabaseSpecific {

	MetaEnvServer server;
	MetaEnvServerNode node;
	
	protected DatabaseSpecific() {
	}
	
	public static DatabaseSpecific getSpecificHandler( ActionBase action , VarDBMSTYPE dbmsType , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		DatabaseSpecific specific = null;
		if( dbmsType == VarDBMSTYPE.ORACLE )
			specific = new DatabaseOracleSpecific();
		else
		if( dbmsType == VarDBMSTYPE.POSTGRESQL )
			specific = new DatabasePostgresSpecific();
		else
			action.exit( "unexpected database type=" + dbmsType.name() );
		
		if( server == null )
			return( specific );
		
		specific.server = server;
		if( node != null )
			specific.node = node;
		else {
			for( MetaEnvServerNode xnode : server.getNodes( action ) ) {
				if( !xnode.OFFLINE ) {
					specific.node = xnode;
					break;
				}
			}
			
			if( specific.node == null )
				action.exit( "server " + server.NAME + " has no online nodes defined" );
		}
		
		return( specific );
	}

	public static MetaEnvServerNode getDatabaseNode( ActionBase action , MetaEnvServer server ) throws Exception {
		for( MetaEnvServerNode node : server.getNodes( action ) )
			if( !node.OFFLINE )
				return( node );
		action.exit( "server " + server.NAME + " has no online nodes defined" );
		return( null );
	}
	
	abstract public VarPROCESSMODE getProcessStatus( ActionBase action ) throws Exception;
	abstract public boolean checkConnect( ActionBase action , String user , String password ) throws Exception;
	abstract public boolean applySystemScript( ActionBase action , ShellExecutor shell , String file , String fileLog ) throws Exception;
	
	abstract public String readCellValue( ActionBase action , String schema , String user , String password , String table , String column , String condition ) throws Exception;
	abstract public void readTableData( ActionBase action , String schema , String user , String password , String table , String condition , String[] columns , List<String[]> rows ) throws Exception;
	abstract public void createTableData( ActionBase action , String schema , String user , String password , String table , String[] columns , String columntypes[] , List<String[]> rows ) throws Exception;
	abstract public void writeTableData( ActionBase action , String schema , String user , String password , String table , String[] columns , List<String[]> rows ) throws Exception;
	abstract public void insertRow( ActionBase action , String schema , String user , String password , String table , String[] columns , String[] values ) throws Exception;
	abstract public void updateRow( ActionBase action , String schema , String user , String password , String table , String[] columns , String[] values , String condition ) throws Exception;
	abstract public boolean applyScript( ActionBase action , String schema , String user , String password , String scriptFile , String outFile ) throws Exception;
	
	abstract public boolean validateScriptContent( ActionBase action , LocalFolder dir , String script ) throws Exception;
	abstract public String getComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile ) throws Exception;
	abstract public void grepComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void addComment( ActionBase action , String comment , LocalFolder dstDir , String outfile ) throws Exception;
	
	abstract public void uddiBegin( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void uddiEnd( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void uddiAddEndpoint( ActionBase action , String UDDI_KEY , String UDDI_UAT , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void smevAttrBegin( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void smevAttrEnd( ActionBase action , LocalFolder dstDir , String outfile ) throws Exception;
	abstract public void smevAttrAddValue( ActionBase action , String UDDI_ATTR_ID , String UDDI_ATTR_NAME , String UDDI_ATTR_CODE , String UDDI_ATTR_REGION , String UDDI_ATTR_ACCESSPOINT , 
			LocalFolder dstDir , String outfile ) throws Exception;
}
