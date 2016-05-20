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
import java.util.Map;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.CommandOutput;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.meta.Metadata.VarSESSIONTYPE;
import ru.egov.urm.storage.Folder;

abstract class ShellCore {

	public boolean local;
	public VarOSTYPE osType;
	public VarSESSIONTYPE sessionType;
	public Folder tmpFolder;
	protected ShellExecutor executor;
	
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

	public String cmdCurrent;
	public boolean running = false;
	public boolean initialized = false;
	
	static String finishMarker = "URM.MARKER";
	static String EXECUTE_LOG = "execute.log";
	static String UPLOAD_LOG = "upload.log";

	abstract protected String getExportCmd( ActionBase action ) throws Exception;
	abstract protected void getProcessAttributes( ActionBase action ) throws Exception;
	abstract public void runCommand( ActionBase action , String cmd , int logLevel ) throws Exception;
	abstract public int runCommandGetStatus( ActionBase action , String cmd , int logLevel ) throws Exception;
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
	abstract public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception;
	abstract public void cmdExtractTar( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception;
	abstract public String cmdLs( ActionBase action , String path ) throws Exception;
	abstract public void cmdCreateZipFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception;
	abstract public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception;
	abstract public void cmdCreateTarFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception;
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
	abstract public String cmdGetTarContentMD5( ActionBase action , String filePath ) throws Exception;
	abstract public String cmdGetArchivePartMD5( ActionBase action , String filePath , String archivePartPath , String EXT ) throws Exception;
	abstract public String cmdGetFilesMD5( ActionBase action , String dir , String includeList , String excludeList ) throws Exception;
	abstract public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception;
	abstract public String[] cmdGrepFile( ActionBase action , String filePath , String mask ) throws Exception;
	abstract public void cmdReplaceFileLine( ActionBase action , String filePath , String mask , String newLine ) throws Exception;
	abstract public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception;
	abstract public void cmdAppendUploadLog( ActionBase action , String src , String dst ) throws Exception;
	abstract public void cmdCreatePublicDir( ActionBase action , String dir ) throws Exception;
	abstract public String[] cmdGetFileLines( ActionBase action , String filePath ) throws Exception;
	abstract public Map<String,List<String>> cmdGetFilesContent( ActionBase action , String dir , String fileMask ) throws Exception;
	
	public static ShellCore createShellCore( ActionBase action , ShellExecutor executor , VarOSTYPE osType , boolean local ) throws Exception {
		ShellCore core = null;
		
		if( osType == VarOSTYPE.LINUX ) {
			VarSESSIONTYPE sessionType = ( local )? VarSESSIONTYPE.UNIXLOCAL : VarSESSIONTYPE.UNIXREMOTE;
			core = new ShellCoreUnix( executor , sessionType , executor.tmpFolder , local );
		}
		else
		if( osType == VarOSTYPE.WINDOWS ) {
			VarSESSIONTYPE sessionType = null;
			if( action.context.account.isWindows() ) {
				if( !local )
					action.exitUnexpectedState();
				sessionType = VarSESSIONTYPE.WINDOWSLOCAL;
			}
			else {
				if( local )
					action.exitUnexpectedState();
				sessionType = VarSESSIONTYPE.WINDOWSFROMUNIX;
			}
				
			core = new ShellCoreWindows( executor , sessionType , executor.tmpFolder , local );
		}
		else
			action.exitUnexpectedState();
		
		return( core );
	}
	
	protected ShellCore( ShellExecutor executor , VarOSTYPE osType , VarSESSIONTYPE sessionType , Folder tmpFolder , boolean local ) {
		this.local = local;
		this.executor = executor;
		this.osType = osType;
		this.sessionType = sessionType;
		this.tmpFolder = tmpFolder;
		
		cmdout = new LinkedList<String>();
		cmderr = new LinkedList<String>();
		running = false;
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

	public void setRootPath( String rootPath ) {
		this.rootPath = rootPath;
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

	public void runCommandCritical( ActionBase action , String cmd ) throws Exception {
		if( action.isExecute() ) {
			action.debug( executor.name + ": execute " + cmd );
			cmdAppendExecuteLog( action , "run: " + cmd );
			runCommand( action , cmd , CommandOutput.LOGLEVEL_INFO );
		}
		else {
			action.debug( executor.name + ": showonly " + cmd );
		}
	}
	
	public String runCommandCheckNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandCheck( action , cmd , CommandOutput.LOGLEVEL_INFO ) ); 
	}

	public String runCommandCheckNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , CommandOutput.LOGLEVEL_INFO ) ); 
	}

	public String runCommandCheckDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandCheck( action , cmd , CommandOutput.LOGLEVEL_TRACE ) ); 
	}

	public String runCommandCheckDebugIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmdIfDir( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , CommandOutput.LOGLEVEL_TRACE ) ); 
	}

	public void runCommand( ActionBase action , String dir , String cmd , int logLevel ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommand( action , cmdDir , logLevel ); 
	}

	public String runCommandCheckDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , CommandOutput.LOGLEVEL_TRACE ) ); 
	}

	public String[] runCommandGetLines( ActionBase action , String dir , String cmd , int logLevel ) throws Exception {
		runCommand( action , dir , cmd , logLevel );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );
		
		return( cmdout.toArray( new String[0] ) );
	}
	
	public String[] runCommandGetLines( ActionBase action , String cmd , int logLevel ) throws Exception {
		runCommand( action , cmd , logLevel );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );
		
		return( cmdout.toArray( new String[0] ) );
	}
	
	public String runCommandCheck( ActionBase action , String cmd , int logLevel ) throws Exception {
		runCommand( action , cmd , logLevel );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );

		String out = getOut();
		return( out );
	}

	public int runCommandGetStatusNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetStatus( action , cmd , CommandOutput.LOGLEVEL_INFO ) );
	}
	
	public int runCommandGetStatusDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetStatus( action , cmd , CommandOutput.LOGLEVEL_TRACE ) );
	}
	
	public void runCommandCheckStatusNormal( ActionBase action , String cmd ) throws Exception {
		runCommandCheckStatus( action , cmd , CommandOutput.LOGLEVEL_INFO );
	}
	
	public void runCommandCheckStatusNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , CommandOutput.LOGLEVEL_INFO );
	}
	
	public void runCommandCheckStatusDebug( ActionBase action , String cmd ) throws Exception {
		runCommandCheckStatus( action , cmd , CommandOutput.LOGLEVEL_TRACE );
	}
	
	public void runCommandCheckStatusDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		if( !cmdout.isEmpty() ) {
			if( cmdout.get( 0 ).startsWith( "invalid directory:" ) )
				exitError( action , "invalid directory, error executing command: " + cmd + ", " + cmdout.get( 0 ) );
		}
	}
	
	public void runCommandCheckStatus( ActionBase action , String dir , String cmd , int logLevel ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , logLevel );
		if( !cmdout.isEmpty() ) {
			if( cmdout.get( 0 ).startsWith( "invalid directory:" ) )
				exitError( action , "invalid directory, error executing command: " + cmd + ", " + cmdout.get( 0 ) );
		}
	}
	
	public void runCommandCheckStatus( ActionBase action , String cmd , int logLevel ) throws Exception {
		int status = runCommandGetStatus( action , cmd , logLevel );
		String err = getErr();
		if( status != 0 || !err.isEmpty() )
			exitError( action , "error executing command: " + cmd + ", status=" + status + ", stderr: " + err );
	}

	public String runCommandGetValueCheckNormal( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetValueCheck( action , cmd , CommandOutput.LOGLEVEL_INFO ) );
	}
	
	public String runCommandGetValueCheckNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		String value = runCommandGetValueCheck( action , cmdDir , CommandOutput.LOGLEVEL_INFO );
		if( value.startsWith( "invalid directory" ) )
			action.exit( value );
		return( value );
	}
	
	public String runCommandGetValueCheckDebug( ActionBase action , String cmd ) throws Exception {
		return( runCommandGetValueCheck( action , cmd , CommandOutput.LOGLEVEL_TRACE ) );
	}
	
	public String runCommandGetValueCheckDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		String value = runCommandGetValueCheck( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		if( value.startsWith( "invalid directory" ) )
			action.exit( value );
		return( value );
	}
	
	public String runCommandGetValueCheck( ActionBase action , String cmd , int logLevel ) throws Exception {
		String value = runCommandCheck( action , cmd , logLevel );
		value = value.trim();
		return( value );
	}

	public String runCommandGetValueNoCheck( ActionBase action , String cmd , int logLevel ) throws Exception {
		runCommand( action , cmd , logLevel );
		String value = getOut();
		return( value );
	}

	public List<String> runCommandCheckGetOutputDebug( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		List<String> out = runCommandCheckGetOutputDebug( action , cmdDir );
		if( !out.isEmpty() )
			if( out.get( 0 ).startsWith( "invalid directory" ) )
				action.exit( out.get( 0 ) );
		return( out );
	}
	
	public List<String> runCommandCheckGetOutputDebug( ActionBase action , String cmd ) throws Exception {
		runCommand( action , cmd , CommandOutput.LOGLEVEL_TRACE );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , "error running command (" + cmd + ")" + " - " + err );

		return( cmdout );
	}
	
	protected String readBuffer( ActionBase action , BufferedReader textreader , String buffer , char lineTerm ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "readBuffer start reading ... " );
		
		String s = "";
		if( !textreader.ready() ) {
			char[] c = new char[1];
			if( textreader.read( c , 0 , 1 ) != 1 )
				return( null );
				
			s += c[0];
			buffer += c[0];
		}

		char buf[] = new char[ 100 ];
		String nextBuffer = buffer;
		while( textreader.ready() ) {
			int len = textreader.read( buf , 0 , 100 );
			
			if( len > 0 ) {
				boolean lineFound = false;
				for( int k = 0; k < len; k++ ) {
					s += buf[ k ];
					
					if( buf[ k ] == '\r' )
						continue;
					if( buf[ k ] == lineTerm )
						lineFound = true; 

					nextBuffer += buf[ k ];
				}
				
				if( lineFound )
					break;
			}
		}
		
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "readBuffer part=" + s.replaceAll("\\p{C}", "?") );
		
		return( nextBuffer );
	}

	protected void skipUpTo( ActionBase action , BufferedReader textreader , char endChar ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			System.out.print( "TRACEINTERNAL: skipUpTo part=" );

		char[] c = new char[1];
		while( true ) {
			if( textreader.read( c , 0 , 1 ) != 1 )
				action.exit( "unable to read" );
			
			if( action.context.CTX_TRACEINTERNAL ) {
				String s = "" + c[0];
				System.out.print( s.replaceAll("\\p{C}", "?") );
			}
			
			if( c[0] == endChar )
				break;
		}
		
		if( action.context.CTX_TRACEINTERNAL )
			System.out.print( "\n" );
	}		
	
}
