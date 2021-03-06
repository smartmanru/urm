package org.urm.engine.shell;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.action.CommandOutput;
import org.urm.engine.security.AuthResource;
import org.urm.engine.storage.Folder;
import org.urm.meta.loader.Types.*;

abstract public class ShellCore {

	public boolean local;
	public DBEnumOSType osType;
	public EnumSessionType sessionType;
	public Folder tmpFolder;
	protected ShellExecutor executor;
	
	List<String> cmdout; 
	List<String> cmderr;
	public String homePath;
	
	public String cmdCurrent;
	public boolean running = false;
	public boolean initialized = false;
	
	static String EXECUTE_LOG = "execute.log";
	static String UPLOAD_LOG = "upload.log";

	abstract protected boolean getProcessAttributes( ActionBase action ) throws Exception;
	abstract public void runCommand( ActionBase action , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception;
	abstract public int runCommandGetStatus( ActionBase action , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception;
	abstract public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception;
	abstract public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception;

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
	abstract public void cmdGetDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files , String excludeRegExp ) throws Exception;
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
	abstract public Date cmdGetFileChangeTime( ActionBase action , String filePath ) throws Exception;
	abstract public long cmdGetFileSize( ActionBase action , String filePath ) throws Exception;
	abstract public Map<String,List<String>> cmdGetFilesContent( ActionBase action , String dir , String fileMask ) throws Exception;
	
	public static ShellCore createShellCore( ActionBase action , ShellExecutor executor , DBEnumOSType osType , boolean local ) throws Exception {
		ShellCore core = null;
		
		EnumSessionType sessionType = null;
		if( osType.isLinux() ) {
			if( action.isLocalWindows() ) {
				if( local )
					action.exitUnexpectedState();
				sessionType = EnumSessionType.UNIXFROMWINDOWS;
			}
			else
				sessionType = ( local )? EnumSessionType.UNIXLOCAL : EnumSessionType.UNIXREMOTE;
			
			core = new ShellCoreUnix( executor , sessionType , executor.tmpFolder , local );
		}
		else
		if( osType.isWindows() ) {
			if( action.isLocalWindows() ) {
				if( !local )
					action.exitUnexpectedState();
				sessionType = EnumSessionType.WINDOWSLOCAL;
			}
			else {
				if( local )
					action.exitUnexpectedState();
				sessionType = EnumSessionType.WINDOWSFROMUNIX;
			}
				
			core = new ShellCoreWindows( executor , sessionType , executor.tmpFolder , local );
		}
		else
			action.exitUnexpectedState();
		
		return( core );
	}
	
	protected ShellCore( ShellExecutor executor , DBEnumOSType osType , EnumSessionType sessionType , Folder tmpFolder , boolean local ) {
		this.local = local;
		this.executor = executor;
		this.osType = osType;
		this.sessionType = sessionType;
		this.tmpFolder = tmpFolder;
		
		cmdout = new LinkedList<String>();
		cmderr = new LinkedList<String>();
		running = false;
	}

	public boolean createProcess( ActionBase action , ShellProcess process , String rootPath , AuthResource auth ) throws Exception {
		executor.startProcess( action , process , rootPath , true , auth );
		running = true;
		
		// additional process setup
		if( !getProcessAttributes( action ) )
			return( false );
		
		initialized = true;
		return( true );
	}

	public void kill( ActionBase action ) throws Exception {
		if( !initialized )
			return;
		
		executor.killProcess( action );
		running = false;
		initialized = false;
	}

	protected void exitError( ActionBase action , int errorCode , String error , String[] params ) throws Exception {
		 executor.exitError( action , errorCode , error , params );
	}
	
	public void addInput( ActionBase action , String input , boolean windowsHelper ) throws Exception {
		if( windowsHelper )
			executor.addInput( action , input.getBytes( "Cp1251" ) , false );
		else
			executor.addInput( action , input , false );
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

	public void runCommandCritical( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		if( action.isExecute() ) {
			action.debug( executor.name + ": critical execute " + cmd );
			cmdAppendExecuteLog( action , "run: " + cmd );
			runCommand( action , cmd , CommandOutput.LOGLEVEL_INFO , commandTimeoutMillis );
		}
		else {
			action.debug( executor.name + ": critical showonly " + cmd );
		}
	}
	
	public String runCommandCheckNormal( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		return( runCommandCheck( action , cmd , CommandOutput.LOGLEVEL_INFO , commandTimeoutMillis ) ); 
	}

	public String runCommandCheckNormal( ActionBase action , String dir , String cmd , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , CommandOutput.LOGLEVEL_INFO , commandTimeoutMillis ) ); 
	}

	public String runCommandCheckDebug( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		return( runCommandCheck( action , cmd , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis ) ); 
	}

	public String runCommandCheckDebugIfDir( ActionBase action , String dir , String cmd , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmdIfDir( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis ) ); 
	}

	public void runCommand( ActionBase action , String dir , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommand( action , cmdDir , logLevel , commandTimeoutMillis ); 
	}

	public String runCommandCheckDebug( ActionBase action , String dir , String cmd , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		return( runCommandCheck( action , cmdDir , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis ) ); 
	}

	public String[] runCommandGetLines( ActionBase action , String dir , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception {
		runCommand( action , dir , cmd , logLevel , commandTimeoutMillis );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , _Error.ErrorExecutingCmd2 , "error running command (" + cmd + ")" + " - " + err , new String[] { cmd , err } );
		
		return( cmdout.toArray( new String[0] ) );
	}
	
	public String[] runCommandGetLines( ActionBase action , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception {
		runCommand( action , cmd , logLevel , commandTimeoutMillis );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , _Error.ErrorExecutingCmd2 , "error running command (" + cmd + ")" + " - " + err , new String[] { cmd , err } );
		
		return( cmdout.toArray( new String[0] ) );
	}
	
	public String runCommandCheck( ActionBase action , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception {
		runCommand( action , cmd , logLevel , commandTimeoutMillis );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , _Error.ErrorExecutingCmd2 , "error running command (" + cmd + ")" + " - " + err , new String[] { cmd , err } );

		String out = getOut();
		return( out );
	}

	public int runCommandGetStatusNormal( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		return( runCommandGetStatus( action , cmd , CommandOutput.LOGLEVEL_INFO , commandTimeoutMillis ) );
	}
	
	public int runCommandGetStatusDebug( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		return( runCommandGetStatus( action , cmd , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis ) );
	}
	
	public void runCommandCheckStatusNormal( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		runCommandCheckStatus( action , cmd , CommandOutput.LOGLEVEL_INFO , commandTimeoutMillis );
	}
	
	public void runCommandCheckStatusNormal( ActionBase action , String dir , String cmd , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , CommandOutput.LOGLEVEL_INFO , commandTimeoutMillis );
	}
	
	public void runCommandCheckStatusDebug( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		runCommandCheckStatus( action , cmd , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis );
	}
	
	public void runCommandCheckStatusDebug( ActionBase action , String dir , String cmd , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis );
		if( !cmdout.isEmpty() ) {
			if( cmdout.get( 0 ).startsWith( "invalid directory:" ) ) {
				String err = cmdout.get( 0 );
				exitError( action , _Error.ErrorExecutingCmd2 , "error running command (" + cmd + ")" + " - " + err , new String[] { cmd , err } );
			}
		}
	}
	
	public void runCommandCheckStatus( ActionBase action , String dir , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommandCheckStatus( action , cmdDir , logLevel , commandTimeoutMillis );
		if( !cmdout.isEmpty() ) {
			if( cmdout.get( 0 ).startsWith( "invalid directory:" ) ) {
				String err = cmdout.get( 0 );
				exitError( action , _Error.ErrorExecutingCmd2 , "error running command (" + cmd + ")" + " - " + err , new String[] { cmd , err } );
			}
		}
	}
	
	public void runCommandCheckStatus( ActionBase action , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception {
		int status = runCommandGetStatus( action , cmd , logLevel , commandTimeoutMillis );
		String err = getErr();
		if( status != 0 || !err.isEmpty() )
			exitError( action , _Error.ErrorExecutingCmd3 , "error executing command: " + cmd + ", status=" + status + ", stderr: " + err , new String[] { cmd , "" + status , err } );
	}

	public String runCommandGetValueCheckNormal( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		return( runCommandGetValueCheck( action , cmd , CommandOutput.LOGLEVEL_INFO , commandTimeoutMillis ) );
	}
	
	public String runCommandGetValueCheckNormal( ActionBase action , String dir , String cmd , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		String value = runCommandGetValueCheck( action , cmdDir , CommandOutput.LOGLEVEL_INFO , commandTimeoutMillis );
		if( value.startsWith( "invalid directory" ) )
			action.exit1( _Error.InvalidDirectory1 , "invalid directory " + cmdDir , cmdDir );
		return( value );
	}
	
	public String runCommandGetValueCheckDebug( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		return( runCommandGetValueCheck( action , cmd , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis ) );
	}
	
	public String runCommandGetValueCheckDebug( ActionBase action , String dir , String cmd , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		String value = runCommandGetValueCheck( action , cmdDir , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis );
		if( value.startsWith( "invalid directory" ) )
			action.exit1( _Error.InvalidDirectory1 , "invalid directory " + cmdDir , cmdDir );
		return( value );
	}
	
	public String runCommandGetValueCheck( ActionBase action , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception {
		String value = runCommandCheck( action , cmd , logLevel , commandTimeoutMillis );
		value = value.trim();
		return( value );
	}

	public String runCommandGetValueNoCheck( ActionBase action , String cmd , int logLevel , int commandTimeoutMillis ) throws Exception {
		runCommand( action , cmd , logLevel , commandTimeoutMillis );
		String value = getOut();
		return( value );
	}

	public List<String> runCommandCheckGetOutputDebug( ActionBase action , String dir , String cmd , int commandTimeoutMillis ) throws Exception {
		String cmdDir = getDirCmd( action , dir , cmd );
		List<String> out = runCommandCheckGetOutputDebug( action , cmdDir , commandTimeoutMillis );
		if( !out.isEmpty() ) {
			if( out.get( 0 ).startsWith( "invalid directory" ) )
				action.exit1( _Error.InvalidDirectory1 , "invalid directory " + cmdDir , cmdDir );
		}
		return( out );
	}
	
	public List<String> runCommandCheckGetOutputDebug( ActionBase action , String cmd , int commandTimeoutMillis ) throws Exception {
		runCommand( action , cmd , CommandOutput.LOGLEVEL_TRACE , commandTimeoutMillis );
		String err = getErr();
		
		if( !err.isEmpty() )
			exitError( action , _Error.ErrorExecutingCmd2 , "error running command (" + cmd + ")" + " - " + err , new String[] { cmd , err } );

		return( cmdout );
	}
	
}
