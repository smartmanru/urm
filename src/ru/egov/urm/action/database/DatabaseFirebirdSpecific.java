package ru.egov.urm.action.database;

import java.util.List;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarPROCESSMODE;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;

public class DatabaseFirebirdSpecific extends DatabaseSpecific {

	public DatabaseFirebirdSpecific() {
		super();
	}
	
	public VarPROCESSMODE getProcessStatus( ActionBase action ) throws Exception {
		return( VarPROCESSMODE.ERRORS );
	}

	@Override public boolean checkConnect( ActionBase action , String user , String password ) throws Exception {
		return( false );
	}

	@Override public boolean applySystemScript( ActionBase action , ShellExecutor shell , String file , String fileLog ) throws Exception {
		return( false );
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
