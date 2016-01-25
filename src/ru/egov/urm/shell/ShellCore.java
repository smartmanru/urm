package ru.egov.urm.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;

abstract class ShellCore {

	public VarOSTYPE OSTYPE;
	ShellExecutor executor;
	int commandTimeoutDefault;
	
	List<String> cmdout; 
	List<String> cmderr;
	public String rootPath;
	public String homePath;
	public String processId;
	
	Process process = null;
	OutputStream stdin;
	InputStream stderr;
	InputStream stdout;
	BufferedReader reader;
	Writer writer;
	BufferedReader errreader;

	String cmdCurrent;
	public boolean running = false;
	public boolean initialized = false;
	
	static String finishMarker = "URM.MARKER";  

	int commandTimeout;

	abstract protected String getExportCmd( ActionBase action ) throws Exception;
	abstract protected void getProcessAttributes( ActionBase action ) throws Exception;
	abstract public void runCommand( ActionBase action , String cmd , boolean debug ) throws Exception;
	abstract public int runCommandGetStatus( ActionBase action , String cmd , boolean debug ) throws Exception;
	abstract public void runCommandCritical( ActionBase action , String cmd ) throws Exception;
	abstract public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception;
	abstract public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception;
	abstract protected void killProcess( ActionBase action ) throws Exception;

	abstract public void cmdEnsureDirExists( ActionBase action , String dir ) throws Exception;
	abstract public void cmdCreateFileFromString( ActionBase action , String path , String value ) throws Exception;
	abstract public void cmdAppendFileWithString( ActionBase action , String path , String value ) throws Exception;
	abstract public void cmdAppendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception;
	abstract public boolean cmdCheckDirExists( ActionBase action , String path ) throws Exception;
	abstract public boolean cmdIsFileEmpty( ActionBase action , String path ) throws Exception;
	abstract public boolean cmdCheckFileExists( ActionBase action , String path ) throws Exception;
	abstract public boolean cmdCheckPathExists( ActionBase action , String path ) throws Exception;
	abstract public String cmdFindOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception;
	abstract public String cmdFindOneTop( ActionBase action , String path , String mask ) throws Exception;
	abstract public void cmdCreateMD5( ActionBase action , String filepath ) throws Exception;
	abstract public void cmdRemoveDirContent( ActionBase action , String dirpath ) throws Exception;
	abstract public void cmdRemoveDir( ActionBase action , String dirpath ) throws Exception;
	abstract public void cmdRecreateDir( ActionBase action , String dirpath ) throws Exception;
	abstract public void cmdRemoveFiles( ActionBase action , String dir , String files ) throws Exception;
	abstract public void cmdRemoveFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception;
	abstract public void cmdUnzipPart( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception;
	abstract public void cmdMove( ActionBase action , String source , String target ) throws Exception;
	abstract public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception;
	abstract public String cmdLs( ActionBase action , String path ) throws Exception;
	abstract public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception;
	abstract public String cmdGetFileInfo( ActionBase action , String dir , String dirFile ) throws Exception;
	abstract public void cmdCreateJarFromFolder( ActionBase action , String runDir , String jarFile , String folder ) throws Exception;
	abstract public void cmdSetShellVariable( ActionBase action , String var , String value ) throws Exception;
	abstract public void cmdGitAddPomFiles( ActionBase action , String runDir ) throws Exception;
	abstract public void cmdCd( ActionBase action , String dir ) throws Exception;
	abstract public void cmdCopyFiles( ActionBase action , String dirFrom , String files , String dirTo ) throws Exception;
	abstract public void cmdCopyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception;
	abstract public void cmdCopyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception;
	abstract public void cmdCopyDirFileToFile( ActionBase action , Account account , String dirPath , String fileSrc , String fileDst ) throws Exception;
	abstract public void cmdCopyDirContent( ActionBase action , String srcDir , String dstDir ) throws Exception;
	abstract public void cmdCopyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception;
	abstract public void cmdCopyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception;
	abstract public void cmdScpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception;
	abstract public void cmdScpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception;
	abstract public void cmdScpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception;
	abstract public void cmdScpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception;
	abstract public void cmdScpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception;
	abstract public void cmdScpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception;
	abstract public void cmdGetDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception;
	abstract public void cmdGetTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception;
	abstract public String[] cmdGetFolders( ActionBase action , String rootPath ) throws Exception;
	abstract public String cmdGetFirstFile( ActionBase action , String dir ) throws Exception;
	abstract public String[] cmdFindFiles( ActionBase action , String dir , String mask ) throws Exception;
	abstract public String cmdGetMD5( ActionBase action , String filePath ) throws Exception;
	abstract public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception;
	abstract public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception;
	abstract public void cmdAppendUploadLog( ActionBase action , String src , String dst ) throws Exception;
	
	public static ShellCore createShellCore( ActionBase action , ShellExecutor executor , int timeoutDefault , VarOSTYPE osType ) throws Exception {
		ShellCore core = null;
		
		if( osType == VarOSTYPE.UNIX )
			core = new ShellCoreUnix( executor , timeoutDefault , osType );
		else
			action.exitUnexpectedState();
		
		core.OSTYPE = osType;
		
		return( core );
	}
	
	protected ShellCore( ShellExecutor executor , int timeoutDefault , VarOSTYPE osType ) {
		this.executor = executor;
		this.commandTimeoutDefault = timeoutDefault;
		this.OSTYPE = osType;
		
		cmdout = new LinkedList<String>();
		cmderr = new LinkedList<String>();
		running = false;
		
		commandTimeout = commandTimeoutDefault;
	}

	public void createProcess( ActionBase action , ProcessBuilder builder , String rootPath ) throws Exception {
		this.rootPath = rootPath;
		
		builder.directory( new File( rootPath ) );
		process = builder.start();
		
		stdin = process.getOutputStream();
		writer = new OutputStreamWriter( stdin );
		
		stderr = process.getErrorStream();
		stdout = process.getInputStream();
		
		reader = new BufferedReader( new InputStreamReader( stdout ) );
		errreader = new BufferedReader( new InputStreamReader( stderr ) );

		running = true;
		
		// get process ID
		getProcessAttributes( action );
		
		// run predefined exports
		String cmd = getExportCmd( action );
		if( !cmd.isEmpty() )
			runCommandCheckDebug( action , cmd );
		
		initialized = true;
	}
	
	public void kill( ActionBase action ) throws Exception {
		if( process != null ) {
			if( executor != executor.pool.master && processId != null && !processId.isEmpty() )
				killProcess( action );
				
			process.destroy();
			
			process = null;
			stdin = null;
			writer = null;
			
			stderr = null;
			stdout = null;
			
			reader = null;
			errreader = null;
		}
		
		running = false;
		initialized = false;
	}

	public void setTimeout( ActionBase action , int timeout ) throws Exception {
		commandTimeout = timeout;
	}
	
	protected void exitError( ActionBase action , String error ) throws Exception {
		 executor.exitError( action , error );
	}
	
	public String getOut() {
		return( getListValue( cmdout ) );
	}
	
	public String getErr() {
		return( getListValue( cmderr ) );
	}
	
	private String getListValue( List<String> lst ) {
		String res;
		
		if( lst.size() <= 0 )
			return( "" );
		
		res = lst.get( 0 );
		for( int k = 1; k < lst.size(); k++ ) {
			res += "\n" + lst.get( k );
		}
		
		return( res );
	}
	
	public String runCommandCheckNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandCheck( action , cmd , false ) ); 
	}

	public String runCommandCheckNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , false ) ); 
	}

	public String runCommandCheckDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandCheck( action , cmd , true ) ); 
	}

	public String runCommandCheckDebugIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmdIfDir( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , true ) ); 
	}

	public void runCommand( ActionBase action , String dir , String cmd , boolean debug ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommand( action , cmdDir , debug ); 
	}

	public String runCommandCheckDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , true ) ); 
	}

	public String[] runCommandGetLines( ActionBase action , String dir , String cmd , boolean debug ) throws Exception {
		runCommand( action , dir , cmd , debug );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );
		
		return( cmdout.toArray( new String[0] ) );
	}
	
	public String[] runCommandGetLines( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd , debug );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );
		
		return( cmdout.toArray( new String[0] ) );
	}
	
	public String runCommandCheck( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd , debug );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );

		String out = getOut();
		return( out );
	}

	public int runCommandGetStatusNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetStatus( action , cmd , false ) );
	}
	
	public int runCommandGetStatusDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetStatus( action , cmd , true ) );
	}
	
	public void runCommandCheckStatusNormal( ActionBase action , String cmd ) throws Exception {
		runCommandCheckStatus( action , cmd , false );
	}
	
	public void runCommandCheckStatusNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , false );
	}
	
	public void runCommandCheckStatusDebug( ActionBase action , String cmd ) throws Exception {
		runCommandCheckStatus( action , cmd , true );
	}
	
	public void runCommandCheckStatusDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , true );
	}
	
	public void runCommandCheckStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		int status = runCommandGetStatus( action , cmd , debug );
		if( status != 0 )
			exitError( action , "error executing command: " + cmd + ", status=" + status + ", stderr: " + getErr() );
	}

	public String runCommandGetValueCheckNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetValueCheck( action , cmd , false ) );
	}
	
	public String runCommandGetValueCheckNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandGetValueCheck( action , cmdDir , false ) );
	}
	
	public String runCommandGetValueCheckDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetValueCheck( action , cmd , true ) );
	}
	
	public String runCommandGetValueCheckDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandGetValueCheck( action , cmdDir , true ) );
	}
	
	public String runCommandGetValueCheck( ActionBase action , String cmd , boolean debug ) throws Exception {
		String value = runCommandCheck( action , cmd , debug );
		value = value.trim();
		return( value );
	}

	public String runCommandGetValueNoCheck( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd , debug );
		String value = getOut();
		return( value );
	}

	public List<String> runCommandCheckGetOutputDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheckGetOutputDebug( action , cmdDir ) );
	}
	
	public List<String> runCommandCheckGetOutputDebug( ActionBase action , String cmd ) throws Exception {
		runCommand( action , cmd , true );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );

		return( cmdout );
	}
	
	class CommandReader extends WaiterCommand {
		boolean debug;
		
		public CommandReader( boolean debug ) {
			this.debug = debug;
		}
		
		public void run( ActionBase action ) throws Exception {
			readStreamToMarker( action , reader , cmdout , "" );
			readStreamToMarker( action , errreader , cmderr , "stderr:" );
		}
		
		private void outStreamLine( ActionBase action , String line ) throws Exception {
			if( debug )
				action.trace( line );
			else
				action.log( line );
		}
		
		private void readStreamToMarker( ActionBase action , BufferedReader textreader , List<String> text , String prompt ) throws Exception {
			String line;
			boolean first = true;
			while ( true ) {
				line = textreader.readLine();
				if( line == null ) {
					Thread.yield();
					continue;
				}
				
				int index = line.indexOf( finishMarker );
				if( index >= 0 ) {
					line = line.substring( 0 , index );
					if( index > 0 ) {
						text.add( line );
						if( first && !prompt.isEmpty() ) {
							outStreamLine( action , prompt );
							first = false;
						}
						outStreamLine( action , line );
					}
				}
				else {
					text.add( line );
					if( first && !prompt.isEmpty() ) {
						outStreamLine( action , prompt );
						first = false;
					}
					outStreamLine( action , line );
				}
				
				if( index >= 0 )
					break;
			}
		}
	}
	
}
