package org.urm.server.action.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.Metadata.VarDBMSTYPE;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.Folder;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.RedistStorage;
import org.urm.server.storage.RemoteFolder;
import org.urm.server.storage.UrmStorage;

public class DatabaseSpecific {

	VarDBMSTYPE dbmsType;
	MetaEnvServer server;
	MetaEnvServerNode node;

	Map<String,String> ctxFiles = new HashMap<String,String>();
	String lastValue = null;
	boolean applysystemscriptCopied = false;
	LocalFolder work;
	
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
		String applyName = "applysystemscript";
		applyName += ( ( shell.isLinux() )? ".sh" : ".cmd" );
		
		if( !applysystemscriptCopied ) {
			if( !action.isLocalAccount() ) {
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
			action.error( "errors, status=" + status + " (see log at " + fileLog + ")" );
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
		
		action.error( "error: " + err + " (see log at " + fileLog + ")" );
		return( false );
	}
	
	public boolean applyScript( ActionBase action , String dbschema , String user , String password , String scriptFile , String outFile ) throws Exception {
		String ctxScript = getContextScript( action , dbschema , user , password );
		
		String file = scriptFile;
		String fileLog = outFile;
		if( action.isWindows() ) {
			file = Common.getWinPath( file );
			fileLog = Common.getWinPath( fileLog );
		}
		
		int status = runScriptCmd( action , ctxScript , "applyscript" , file + " " + fileLog );
		if( status != 0 ) {
			action.error( "error: (see log at " + fileLog + ")" );
			return( false );
		}
		
		List<String> data = action.readFileLines( fileLog , action.meta.product.charset );
		String[] lines = data.toArray( new String[0] );
		String[] errors = Common.grep( lines , "^ERROR" );
		if( errors.length > 0 ) {
			action.error( "error: " + " (" + errors[0] + " ...)" );
			return( false );
		}
		
		return( true );
	}
	
	public String[] queryLines( ActionBase action , String dbschema , String user , String password , String query ) throws Exception {
		String ctxScript = getContextScript( action , dbschema , user , password );
		
		String file = work.getFilePath( action , "query.sql" );
		String fileLog = file + ".out";
		if( action.isWindows() ) {
			file = Common.getWinPath( file );
			fileLog = Common.getWinPath( fileLog );
		}
		
		Common.createFileFromString( file , query );
		
		int status = runScriptCmd( action , ctxScript , "queryscript" , file + " " + fileLog );
		if( status != 0 )
			action.exit( "error: (see logs)" );

		List<String> data = action.readFileLines( fileLog , action.meta.product.charset );
		String[] lines = data.toArray( new String[0] );
		for( int k = 0; k < lines.length; k++ )
			lines[ k ] = lines[ k ].trim();
		
		String[] errors = Common.grep( lines , "^ERROR" );
		if( errors.length > 0 )
			action.exit( "error: " + " (" + errors[0] + " ...)" );
		
		return( lines );
	}

	public String getTableName( ActionBase action , String dbschema , String table ) throws Exception {
		if( server.dbType == VarDBMSTYPE.ORACLE )
			return( dbschema + "." + table );
		if( server.dbType == VarDBMSTYPE.FIREBIRD ||
			server.dbType == VarDBMSTYPE.POSTGRESQL )
			return( table );
		
		action.exitUnexpectedState();
		return( null );
	}
	
	public String readCellValue( ActionBase action , String dbschema , String user , String password , String table , String column , String condition ) throws Exception {
		String query = "select 'value=' || " + column + " as x from " + getTableName( action , dbschema , table ) + " where " + condition + ";";
		String[] lines = queryLines( action , dbschema , user , password , query );
		lines = Common.grep( lines , "^value=" );
		if( lines.length == 0 )
			return( "" );
		
		if( lines.length != 1 )
			action.exit( "unexpected output: " + Common.getList( lines ) );
		
		return( Common.getPartAfterFirst( lines[0] , "value=" ) );
	}

	public void readTableData( ActionBase action , String dbschema , String user , String password , String table , String condition , String[] columns , List<String[]> rows ) throws Exception {
		String query = "select ";
		boolean first = true;
		for( String column : columns ) {
			if( !first )
				query += " || '|c=' || ";
			else
				query += "'c=' || ";
			first = false;
			query += column;
		}
		query += " from " + getTableName( action , dbschema , table ) + " where " + condition + ";";
		String[] lines = queryLines( action , dbschema , user , password , query );
		
		for( String value : lines ) {
			value = value.trim();
			if( value.isEmpty() )
				continue;
			if( !value.startsWith( "c=" ) )
				continue;
			
			String[] values = Common.split( value , "\\|" );
			if( values.length != columns.length )
				action.exit( "unexpected table row output: " + value + " (" + values.length + ", " + columns.length + ")" );
			
			String[] row = new String[ columns.length ];
			int pos = 0;
			for( String s : values ) {
				if( !s.startsWith( "c=" ) )
					continue;
				
				row[ pos ] = s.substring( 2 ).trim();
				pos++;
			}
			
			rows.add( row );
		}
	}

	public void readSelectData( ActionBase action , String dbschema , String user , String password , String select , List<String[]> rows ) throws Exception {
		String[] lines = queryLines( action , dbschema , user , password , select + ";" );
		
		for( String value : lines ) {
			value = value.trim();
			if( value.isEmpty() )
				continue;
			if( !value.startsWith( "c=" ) )
				continue;
			
			String[] values = Common.split( value , "\\|" );
			String[] row = new String[ values.length ];
			int pos = 0;
			for( String s : values ) {
				if( !s.startsWith( "c=" ) )
					continue;
				
				row[ pos ] = s.substring( 2 ).trim();
				pos++;
			}
			
			rows.add( row );
		}
	}

	public boolean dropTable( ActionBase action , String dbschema , String user , String password , String table ) throws Exception {
		String query = "drop table " + getTableName( action , dbschema , table ) + ";";
		String scriptFile = work.getFilePath( action , "control.sql" );
		String outFile = scriptFile + ".out";
		Common.createFileFromString( scriptFile , query );

		return( applyScript( action , dbschema , user , password , scriptFile , outFile ) );
	}
	
	public void createTableData( ActionBase action , String dbschema , String user , String password , String table , String[] columns , String columntypes[] , List<String[]> rows ) throws Exception {
		dropTable( action , dbschema , user , password , table );
		
		List<String> lines = new LinkedList<String>();
		String ct = "create table " + getTableName( action , dbschema , table ) + " ( ";
		if( columns.length != columntypes.length )
			action.exit( "invalid column names and types" );
		
		for( int k = 0; k < columns.length; k++ ) {
			if( k > 0 )
				ct += ", ";
			String type = columntypes[ k ];
			ct += columns[k] + " " + type + "";
		}
		ct += " );";
		lines.add( ct );
				
		writeTableDataInternal( action , dbschema , user , password , table , columns , rows , lines , true );
	}
	
	private boolean writeTableDataInternal( ActionBase action , String dbschema , String user , String password , String table , String[] columns , List<String[]> rows , List<String> lines , boolean commit ) throws Exception {
		beginTransaction( action , lines );
		for( String[] values : rows ) {
			String query = getInsertRowString( action , dbschema , table , columns , values );
			lines.add( query );
		}
		endTransaction( action , lines );
		
		String scriptFile = work.getFilePath( action , "run.sql" );
		String outFile = scriptFile + ".out";
		Common.createFileFromStringList( scriptFile , lines );

		return( applyScript( action , dbschema , user , password , scriptFile , outFile ) );
	}

	private void beginTransaction( ActionBase action , List<String> lines ) throws Exception {
		if( server.dbType == VarDBMSTYPE.POSTGRESQL )
			lines.add( "begin;" );
	}
	
	private void endTransaction( ActionBase action , List<String> lines ) throws Exception {
		lines.add( "commit;" );
	}
	
	private String getInsertRowString( ActionBase action , String dbschema , String table , String[] columns , String[] values ) throws Exception {
		if( values.length != columns.length )
			action.exit( "number of values should be equal to number of columns" );
			
		String query = "insert into " + getTableName( action , dbschema , table ) + " (";
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
				query += getTimestampValue( action );
			else
				query += value;
		}
		query += " );";
		
		return( query );
	}
	
	public void writeTableData( ActionBase action , String dbschema , String user , String password , String table , String[] columns , List<String[]> rows , boolean commit ) throws Exception {
		List<String> lines = new LinkedList<String>();
		writeTableDataInternal( action , dbschema , user , password , table , columns , rows , lines , commit );
	}
	
	public boolean insertRow( ActionBase action , String dbschema , String user , String password , String table , String[] columns , String[] values , boolean commit ) throws Exception {
		String query = getInsertRowString( action , dbschema , table , columns , values );
		String scriptFile = work.getFilePath( action , "control.sql" );
		String outFile = scriptFile + ".out";
		Common.createFileFromString( scriptFile , query );

		return( applyScript( action , dbschema , user , password , scriptFile , outFile ) );
	}
	
	public boolean updateRow( ActionBase action , String dbschema , String user , String password , String table , String[] columns , String[] values , String condition , boolean commit ) throws Exception {
		if( values.length != columns.length )
			action.exit( "number of values should be equal to number of columns" );
			
		// ANSI query
		String query = "update " + getTableName( action , dbschema , table ) + " set ";
		for( int pos = 0; pos < columns.length; pos++ ) {
			if( pos > 0 )
				query += ", ";
			
			query += columns[ pos ] + " = ";
			
			String value = values[ pos ];
			if( value.equals( "TIMESTAMP" ) )
				query += getTimestampValue( action );
			else
				query += value;
		}
		query += " where " + condition + ";";

		String scriptFile = work.getFilePath( action , "control.sql" );
		String outFile = scriptFile + ".out";
		Common.createFileFromString( scriptFile , query );
		
		return( applyScript( action , dbschema , user , password , scriptFile , outFile ) );
	}
	
	public boolean deleteRows( ActionBase action , String dbschema , String user , String password , String table , String condition , boolean commit ) throws Exception {
		// ANSI query
		String query = "delete from " + getTableName( action , dbschema , table );
		query += " where " + condition + ";";

		String scriptFile = work.getFilePath( action , "control.sql" );
		String outFile = scriptFile + ".out";
		Common.createFileFromString( scriptFile , query );
		
		return( applyScript( action , dbschema , user , password , scriptFile , outFile ) );
	}
	
	private String getTimestampValue( ActionBase action ) throws Exception {
		if( server.dbType == VarDBMSTYPE.POSTGRESQL )
			return( "now()" );
		else
		if( server.dbType == VarDBMSTYPE.ORACLE )
			return( "SYSDATE" );
		else
		if( server.dbType == VarDBMSTYPE.FIREBIRD )
			return( "CURRENT_TIMESTAMP" );
		
		action.exitUnexpectedState();
		return( null );
	}
	
	private String getContextScript( ActionBase action , String dbschema , String user , String password ) throws Exception {
		String key = server.NAME + "." + dbschema + "." + user;
		String ctxFile = ctxFiles.get( key );
		if( ctxFile != null )
			return( ctxFile );

		work = action.artefactory.getWorkFolder( action );
		List<String> lines = new LinkedList<String>();
		String name = null;
		String DBHOST = ( action.isLocalAccount() )? "localhost" : server.DBMSADDR;
		if( action.isLinux() ) {
			lines.add( "export URMDB_USER=" + user );
			lines.add( "export URMDB_PWD=" + password );
			lines.add( "export URMDB_DBHOST=" + DBHOST );
			lines.add( "export URMDB_DBNAME=" + dbschema );
			lines.add( "export URMDB_CHARSET=" + action.meta.product.charset.name() );
			name = "urmdb." + key + ".sh"; 
		}
		else
		if( action.isWindows() ) {
			lines.add( "set URMDB_USER=" + user );
			lines.add( "set URMDB_PWD=" + password );
			lines.add( "set URMDB_DBHOST=" + DBHOST );
			lines.add( "set URMDB_DBNAME=" + dbschema );
			lines.add( "set URMDB_CHARSET=" + action.meta.product.charset.name() );
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

}
