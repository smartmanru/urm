package ru.egov.urm.action.database;

import java.util.List;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;

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

	public static MetaEnvServerNode getDatabaseNode( ActionBase action , MetaEnvServer server ) throws Exception {
		for( MetaEnvServerNode node : server.getNodes( action ) )
			if( !node.OFFLINE )
				return( node );
		action.exit( "server " + server.NAME + " has no online nodes defined" );
		return( null );
	}

	public String getAdmUser( ActionBase action ) throws Exception {
		return( server.admSchema.DBUSER );
	}
	
	public String getAdmSchema( ActionBase action ) throws Exception {
		return( server.admSchema.DBNAME );
	}
	
	public VarPROCESSMODE getProcessStatus( ActionBase action ) throws Exception {
	}
	
	public boolean checkConnect( ActionBase action , String user , String password ) throws Exception {
	}
	
	public boolean applySystemScript( ActionBase action , ShellExecutor shell , String file , String fileLog ) throws Exception {
	}
	
	public String readCellValue( ActionBase action , String schema , String user , String password , String table , String column , String condition ) throws Exception {
	}

	public void readTableData( ActionBase action , String schema , String user , String password , String table , String condition , String[] columns , List<String[]> rows ) throws Exception {
	}
	
	public void createTableData( ActionBase action , String schema , String user , String password , String table , String[] columns , String columntypes[] , List<String[]> rows ) throws Exception {
	}
	
	public void writeTableData( ActionBase action , String schema , String user , String password , String table , String[] columns , List<String[]> rows ) throws Exception {
	}
	
	public void insertRow( ActionBase action , String schema , String user , String password , String table , String[] columns , String[] values ) throws Exception {
	}
	
	public void updateRow( ActionBase action , String schema , String user , String password , String table , String[] columns , String[] values , String condition ) throws Exception {
	}
	
	public boolean applyScript( ActionBase action , String schema , String user , String password , String scriptFile , String outFile ) throws Exception {
	}
	
	public boolean validateScriptContent( ActionBase action , LocalFolder dir , String script ) throws Exception {
	}
	
	public String getComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile ) throws Exception {
	}
	
	public void grepComments( ActionBase action , String grep , LocalFolder srcDir , String srcFile , LocalFolder dstDir , String outfile ) throws Exception {
	}
	
	public void addComment( ActionBase action , String comment , LocalFolder dstDir , String outfile ) throws Exception {
	}

}
