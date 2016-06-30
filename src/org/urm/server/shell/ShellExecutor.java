package org.urm.server.shell;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.action.ActionBase;
import org.urm.server.action.CommandOutput;
import org.urm.server.storage.Folder;
import org.urm.server.storage.RedistStorage;

public abstract class ShellExecutor {

	public String name;
	public ShellExecutorPool pool;
	public Account account;
	public String rootPath;
	public Folder tmpFolder;
	
	protected ShellCore core;
	
	abstract public void start( ActionBase action ) throws Exception;

	// construction and administration
	protected ShellExecutor( String name , ShellExecutorPool pool , Account account , String rootPath , Folder tmpFolder ) {
		this.name = name;
		this.pool = pool;
		this.account = account;
		this.rootPath = rootPath;
		this.tmpFolder = tmpFolder;
	}
	
	public static ShellExecutor getLocalShellExecutor( ActionBase action , String name , ShellExecutorPool pool , String rootPath , Folder tmpFolder ) throws Exception {
		ShellExecutor executor = new LocalShellExecutor( name , pool , rootPath , tmpFolder );
		executor.core = ShellCore.createShellCore( action, executor , action.context.account.osType , true );
		return( executor );
	}

	public static ShellExecutor getRemoteShellExecutor( ActionBase action , String name , ShellExecutorPool pool , Account account , String rootPath ) throws Exception {
		RedistStorage storage = action.artefactory.getRedistStorage( action , account );
		Folder tmpFolder = storage.getRedistTmpFolder( action );

		ShellExecutor executor = new RemoteShellExecutor( name , pool , account , rootPath , tmpFolder );
		executor.core = ShellCore.createShellCore( action, executor , account.osType , false );
		return( executor );
	}

	public void exitError( ActionBase action , String error ) throws Exception {
		action.exit( name + ": " + error );
	}
	
	public void restart( ActionBase action ) throws Exception {
		boolean initialized = core.initialized; 
		
		core.kill( action );
		if( !initialized )
			action.exit( "shell=" + name + " failed on init stage" );
		
		core = ShellCore.createShellCore( action , this , core.osType , core.local );
		start( action );
	}
	
	protected void createProcess( ActionBase action , ProcessBuilder builder , String rootPath ) throws Exception {
		action.debug( "start shell=" + name + " at rootPath=" + rootPath );
		core.createProcess( action , builder , rootPath );
	}
	
	public void kill( ActionBase action ) throws Exception {
		action.debug( "kill shell=" + name + " at rootPath=" + rootPath );
		core.kill( action );
	}

	// information
	public String getProcessId() {
		return( core.processId );
	}

	public String getHomePath() {
		return( core.homePath );
	}

	public synchronized String getOSPath( ActionBase action , String path ) throws Exception {
		if( account.isWindows() )
			return( Common.getWinPath( path ) );
		return( path );
	}
	
	public boolean isWindows() {
		return( account.isWindows() );
	}

	public boolean isLinux() {
		return( account.isLinux() );
	}

	public String getOSDevNull() {
		if( isLinux() )
			return( "/dev/null" );
		if( isWindows() )
			return( "nul" );
		return( null );
	}
	
	// operations
	public synchronized String createDir( ActionBase action , String home , String dir ) throws Exception {
		String path = Common.getPath( home , dir );
		ensureDirExists( action , path );
		return( path );
	}
	
	public synchronized void ensureDirExists( ActionBase action , String dirpath ) throws Exception {
		core.cmdEnsureDirExists( action , dirpath );
	}

	public synchronized void createFileFromString( ActionBase action , String path , String value ) throws Exception {
		core.cmdCreateFileFromString( action , path , value );
	}

	public synchronized void appendFileWithString( ActionBase action , String path , String value ) throws Exception {
		core.cmdAppendFileWithString( action , path , value );
	}

	public synchronized void appendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception {
		core.cmdAppendFileWithFile( action , pathDst , pathSrc );
	}

	public synchronized boolean checkDirExists( ActionBase action , String path ) throws Exception {
		if( path.isEmpty() )
			return( false );
		
		if( action.isLocalAccount() )
			return( Files.isDirectory( Paths.get( path ) , LinkOption.NOFOLLOW_LINKS ) );
		
		return( core.cmdCheckDirExists( action , path ) );
	}

	public synchronized Map<String,List<String>> getFilesContent( ActionBase action , String dir , String fileMask ) throws Exception {
		return( core.cmdGetFilesContent( action , dir , fileMask ) );
	}
	
	public synchronized boolean isFileEmpty( ActionBase action , String path ) throws Exception {
		return( core.cmdIsFileEmpty( action , path ) );
	}

	public synchronized boolean checkFileExists( ActionBase action , String dir , String path ) throws Exception {
		return( checkFileExists( action , Common.getPath( dir , path ) ) );
	}
	
	public synchronized boolean checkFileExists( ActionBase action , String path ) throws Exception {
		if( path.isEmpty() )
			return( false );

		return( core.cmdCheckFileExists( action , path ) );
	}

	public synchronized boolean checkPathExists( ActionBase action , String path ) throws Exception {
		if( path.isEmpty() )
			return( false );

		return( core.cmdCheckPathExists( action , path ) );
	}

	public synchronized String findOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception {
		return( core.cmdFindOneTopWithGrep( action , path , mask , grepMask ) );
	}
	
	public synchronized String findOneTop( ActionBase action , String path , String mask ) throws Exception {
		return( core.cmdFindOneTop( action , path , mask ) );
	}

	public synchronized void createMD5( ActionBase action , String filepath ) throws Exception {
		core.cmdCreateMD5( action , filepath );
	}

	public synchronized void removeDirContent( ActionBase action , String dirpath ) throws Exception {
		core.cmdRemoveDirContent( action , dirpath );
	}
	
	public synchronized void removeDir( ActionBase action , String dirpath ) throws Exception {
		core.cmdRemoveDir( action , dirpath );
	}
	
	public synchronized void recreateDir( ActionBase action , String dirpath ) throws Exception {
		core.cmdRecreateDir( action , dirpath );
	}
	
	public synchronized void removeFiles( ActionBase action , String dir , String files ) throws Exception {
		core.cmdRemoveFiles(action , dir , files );
	}
	
	public synchronized void removeFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		core.cmdRemoveFilesWithExclude( action , dir , files , exclude );
	}
	
	public synchronized void unzip( ActionBase action , String runDir , String zipFile , String folder ) throws Exception {
		core.cmdUnzipPart( action , runDir , zipFile , folder , "" );
	}

	public synchronized void unzipPart( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception {
		core.cmdUnzipPart( action , unzipDir , zipFile , targetDir , zipPart );
	}

	public synchronized void move( ActionBase action , String source , String target ) throws Exception {
		core.cmdMove( action , source , target );
	}

	public synchronized void extractTarGz( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		core.cmdExtractTarGz( action , tarFile , targetFolder , part );
	}

	public synchronized void extractTar( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		core.cmdExtractTar( action , tarFile , targetFolder , part );
	}

	public synchronized void extractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		core.cmdExtractTarGz( action , tarFile , targetFolder , "" );
	}

	public synchronized void extractTar( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		core.cmdExtractTar( action , tarFile , targetFolder , "" );
	}

	public synchronized String ls( ActionBase action , String path ) throws Exception {
		return( core.cmdLs( action , path ) );
	}

	public synchronized void createZipFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		core.cmdCreateZipFromDirContent( action , tarFile , dir , content , exclude );
	}

	public synchronized void createTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		core.cmdCreateTarGzFromDirContent( action , tarFile , dir , content , exclude );
	}

	public synchronized void createTarFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		core.cmdCreateTarFromDirContent( action , tarFile , dir , content , exclude );
	}

	public synchronized String getFileInfo( ActionBase action , String dir , String dirFile ) throws Exception {
		return( core.cmdGetFileInfo( action , dir , dirFile ) );
	}

	public synchronized void custom( ActionBase action , String cmd ) throws Exception {
		custom( action , cmd , CommandOutput.LOGLEVEL_INFO );
	}
	
	public synchronized void custom( ActionBase action , String cmd , int logLevel ) throws Exception {
		core.runCommand( action , cmd , logLevel );
	}
	
	public synchronized void custom( ActionBase action , String dir , String cmd ) throws Exception {
		core.runCommand( action , dir , cmd , CommandOutput.LOGLEVEL_INFO );
	}

	public synchronized void checkErrors( ActionBase action ) throws Exception {
		String err = core.getErr();
		if( !err.isEmpty() )
			action.exit( "error executing CMD=" + core.cmdCurrent + ": " + err );
	}

	public synchronized void customCheckErrorsDebug( ActionBase action , String dir , String cmd ) throws Exception {
		core.runCommand( action , dir , cmd , CommandOutput.LOGLEVEL_TRACE );
		String err = core.getErr();
		if( !err.isEmpty() )
			action.exit( "error executing CMD=" + cmd + ": " + err );
	}

	public synchronized void customCritical( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = core.getDirCmd( action , dir , cmd );
		core.runCommandCritical( action , cmdDir );
	}
	
	public synchronized void customCritical( ActionBase action , String cmd ) throws Exception {
		core.runCommandCritical( action , cmd );
	}
	
	public synchronized int customGetStatus( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetStatusDebug( action , cmd ) );
	}
	
	public synchronized int customGetStatus( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = core.getDirCmd( action , dir , cmd );
		return( core.runCommandGetStatusDebug( action , cmdDir ) );
	}

	public synchronized int customGetStatusCheckErrors( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = core.getDirCmd( action , dir , cmd );
		int status = core.runCommandGetStatusDebug( action , cmdDir );
		if( status != 0 )
			return( status );
		
		String errors = core.getErr();
		if( !errors.isEmpty() )
			return( -1 );
		
		return( 0 );
	}
	
	public synchronized int customGetStatusNormal( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetStatusNormal( action , cmd ) );
	}
	
	public synchronized int customGetStatusNormal( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = core.getDirCmd( action , dir , cmd );
		return( core.runCommandGetStatusNormal( action , cmdDir ) );
	}
	
	public synchronized void customCheckStatus( ActionBase action , String cmd ) throws Exception {
		core.runCommandCheckStatusDebug( action , cmd );
	}

	public synchronized void customCheckStatus( ActionBase action , String dir , String cmd ) throws Exception {
		core.runCommandCheckStatusDebug( action , dir , cmd );
	}

	public synchronized void customCheckErrorsDebug( ActionBase action , String cmd ) throws Exception {
		core.runCommandCheckDebug( action , cmd );
	}

	public synchronized void customCheckErrorsNormal( ActionBase action , String cmd ) throws Exception {
		core.runCommandCheckNormal( action , cmd );
	}

	public synchronized String customGetValueNoCheck( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetValueNoCheck( action , cmd , CommandOutput.LOGLEVEL_TRACE ) );
	}

	public synchronized String customGetValue( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetValueCheckDebug( action , cmd ) );
	}

	public synchronized String[] customGetLines( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetLines( action , cmd , CommandOutput.LOGLEVEL_TRACE ) );
	}

	public synchronized String[] customGetLines( ActionBase action , String dir , String cmd ) throws Exception {
		return( core.runCommandGetLines( action , dir , cmd , CommandOutput.LOGLEVEL_TRACE ) );
	}

	public synchronized String customGetValue( ActionBase action , String dir , String cmd ) throws Exception {
		return( core.runCommandGetValueCheckDebug( action , dir , cmd ) );
	}

	public synchronized String[] findFiles( ActionBase action , String dir , String mask ) throws Exception {
		return( core.cmdFindFiles( action , dir , mask ) );
	}
	
	public synchronized String getFirstFile( ActionBase action , String dir ) throws Exception {
		return( core.cmdGetFirstFile( action , dir ) );
	}
	
	public synchronized void createJarFromFolder( ActionBase action , String runDir , String jarFile , String folder ) throws Exception {
		core.cmdCreateJarFromFolder( action , runDir , jarFile , folder );
	}

	public synchronized void export( ActionBase action , String var , String value ) throws Exception {
		core.cmdSetShellVariable( action , var , value );
	}
	
	public synchronized void mvnCheckStatus( ActionBase action , String runDir , String MAVEN_CMD ) throws Exception {
		core.runCommandCheckStatusNormal( action , runDir , MAVEN_CMD );
	}
	
	public synchronized void gitAddPomFiles( ActionBase action , String runDir ) throws Exception {
		core.cmdGitAddPomFiles( action , runDir );
	}

	public synchronized void cd( ActionBase action , String dir ) throws Exception {
		core.cmdCd( action , dir );
	}

	public synchronized void copyFiles( ActionBase action , String dirFrom , String files , String dirTo ) throws Exception {
		core.cmdCopyFiles( action , dirFrom , files , dirTo );
	}
	
	public synchronized void copyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception {
		core.cmdCopyFile( action, fileFrom , fileTo );
	}
	
	public synchronized void copyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception {
		core.cmdCopyFile( action , fileFrom , targetDir , finalName , FOLDER );
	}

	public synchronized void copyDirContent( ActionBase action , String srcDir , String dstDir ) throws Exception {
		core.cmdCopyDirContent( action , srcDir , dstDir );
	}

	public synchronized void copyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception {
		core.cmdCopyDirDirect( action , dirFrom , dirTo );
	}
	
	public synchronized void copyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception {
		core.cmdCopyDirToBase( action , dirFrom , baseDstDir );
	}
	
	public synchronized void scpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		int timeout = action.setTimeoutUnlimited();
		core.cmdScpFilesRemoteToLocal( action , srcPath , account , dstPath );
		action.setTimeout( timeout );
	}

	public synchronized void scpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		core.cmdScpDirContentRemoteToLocal( action , srcPath , account , dstPath );
	}

	public synchronized void scpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		core.cmdScpFilesLocalToRemote( action , srcPath , account , dstPath );
	}

	public synchronized void scpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		core.cmdScpDirLocalToRemote( action , srcDirPath , account , baseDstDir );
	}

	public synchronized void scpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		core.cmdScpDirContentLocalToRemote( action , srcDirPath , account , dstDir );
	}

	public synchronized void scpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		core.cmdScpDirRemoteToLocal( action , srcPath , account , dstPath );
	}

	public synchronized void copyFileTargetToLocalDir( ActionBase action , Account account , String srcFilePath , String dstDir ) throws Exception {
		if( account.local )
			copyFile( action , srcFilePath , dstDir );
		else {
			scpFilesRemoteToLocal( action , srcFilePath , account , Common.ensureDir( dstDir ) );
		}
	}

	public synchronized void copyFileTargetToLocalFile( ActionBase action , Account account , String srcFilePath , String dstFilePath ) throws Exception {
		if( account.local )
			copyFile( action , srcFilePath , dstFilePath );
		else {
			scpFilesRemoteToLocal( action , srcFilePath , account , dstFilePath );
		}
	}

	public synchronized void copyFilesTargetToLocal( ActionBase action , Account account , String srcFiles , String dstDir ) throws Exception {
		if( account.local )
			copyFiles( action , Common.getDirName( srcFiles ) , Common.getBaseName( srcFiles ) , dstDir );
		else {
			scpFilesRemoteToLocal( action , srcFiles , account , Common.ensureDir( dstDir ) );
		}
	}

	public synchronized void moveFilesTargetFromLocal( ActionBase action , Account account , String srcDir , String srcFiles , String dstDir ) throws Exception {
		if( account.local )
			move( action , Common.getPath( srcDir , srcFiles ) , dstDir );
		else {
			scpFilesLocalToRemote( action , Common.getPath( srcDir , srcFiles ) , account , Common.ensureDir( dstDir ) );
			removeFiles( action , srcDir , srcFiles );
		}
	}

	public synchronized void copyFilesTargetFromLocal( ActionBase action , Account account , String srcDir , String srcFiles , String dstDir ) throws Exception {
		if( account.local )
			this.copyFiles( action , srcDir , srcFiles , dstDir );
		else {
			scpFilesLocalToRemote( action , Common.getPath( srcDir , srcFiles ) , account , Common.ensureDir( dstDir ) );
		}
	}

	public synchronized void copyDirContentTargetToLocal( ActionBase action , Account account , String srcDir , String dstDir ) throws Exception {
		if( account.local )
			copyDirContent( action , srcDir , dstDir );
		else {
			scpDirContentRemoteToLocal( action , srcDir , account , Common.ensureDir( dstDir ) );
		}
	}
	
	public synchronized void copyDirTargetToLocal( ActionBase action , Account account , String srcDir , String dstBaseDir ) throws Exception {
		if( account.local )
			copyDirToBase( action , srcDir , dstBaseDir );
		else {
			scpDirRemoteToLocal( action , srcDir , account , Common.ensureDir( dstBaseDir ) );
		}
	}

	public synchronized void copyFileLocalToTarget( ActionBase action , Account account , String srcFilePath , String dstDir ) throws Exception {
		if( account.local )
			copyFile( action , srcFilePath , dstDir , "" , "" );
		else {
			scpFilesLocalToRemote( action , srcFilePath , account , Common.ensureDir( dstDir ) );
		}
	}

	public synchronized void copyFileLocalToTargetRename( ActionBase action , Account account , String srcFilePath , String dstDir , String newName ) throws Exception {
		if( account.local )
			copyFile( action , srcFilePath , dstDir , newName , "" );
		else {
			scpFilesLocalToRemote( action , srcFilePath , account , Common.getPath( dstDir , newName ) );
		}
	}

	public synchronized void copyDirFileToFile( ActionBase action , Account account , String dirPath , String fileSrc , String fileDst ) throws Exception {
		core.cmdCopyDirFileToFile( action , account , dirPath , fileSrc , fileDst );
	}
	
	public synchronized void copyDirLocalToTarget( ActionBase action , Account account , String srcDirPath , String baseDstDir ) throws Exception {
		if( account.local )
			copyDirToBase( action , srcDirPath , baseDstDir );
		else {
			scpDirLocalToRemote( action , srcDirPath , account , Common.ensureDir( baseDstDir ) );
		}
	}

	public synchronized void copyDirContentLocalToTarget( ActionBase action , Account account , String srcDirPath , String dstDir ) throws Exception {
		if( account.local )
			this.copyDirContent( action , srcDirPath , dstDir );
		else {
			this.scpDirContentLocalToRemote( action , srcDirPath , account , Common.ensureDir( dstDir ) );
		}
	}

	public synchronized String[] getFolders( ActionBase action , String rootPath ) throws Exception {
		return( core.cmdGetFolders( action , rootPath ) );
	}
	
	public synchronized void getDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		core.cmdGetDirsAndFiles( action , rootPath , dirs , files );
	}
	
	public synchronized void getTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		if( !action.isLocalAccount() ) {
			core.cmdGetTopDirsAndFiles( action , rootPath , dirs , files );
			return;
		}
		
		File folder = new File( rootPath );
		if( folder.exists() == false )
			return;
		
		if( !folder.isDirectory() )
			action.exit( "not a directory path=" + rootPath );
		
		for( final File fileEntry : folder.listFiles() ) {
	        if( fileEntry.isDirectory() )
	        	dirs.add( fileEntry.getName() );
	        else
	        	files.add( fileEntry.getName() );
	    }
	}
	
	public synchronized String getMD5( ActionBase action , String filePath ) throws Exception {
		return( core.cmdGetMD5( action , filePath ) );
	}
	
	public synchronized String getTarContentMD5( ActionBase action , String filePath ) throws Exception {
		return( core.cmdGetTarContentMD5( action , filePath ) );
	}
	
	public synchronized String getArchivePartMD5( ActionBase action , String filePath , String archivePartPath , String EXT ) throws Exception {
		return( core.cmdGetArchivePartMD5( action , filePath , archivePartPath , EXT ) );
	}
	
	public synchronized String getFilesMD5( ActionBase action , String dir , String includeList , String excludeList ) throws Exception {
		return( core.cmdGetFilesMD5( action , dir , includeList , excludeList ) );
	}
	
	public synchronized String getFileContentAsString( ActionBase action , String filePath ) throws Exception {
		if( account.local )
			return( action.readFile( filePath ) );
		
		return( core.cmdGetFileContentAsString( action , filePath ) );
	}

	public synchronized String[] grepFile( ActionBase action , String filePath , String mask ) throws Exception {
		return( core.cmdGrepFile( action , filePath , mask ) );
	}
	
	public synchronized void replaceFileLine( ActionBase action , String filePath , String mask , String newLine ) throws Exception {
		core.cmdReplaceFileLine( action , filePath , mask , newLine );
	}
	
	public synchronized void appendExecuteLog( ActionBase action , String msg ) throws Exception {
		core.cmdAppendExecuteLog( action , msg ); 
	}

	public synchronized void appendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		core.cmdAppendUploadLog( action , src , dst );
	}

	public synchronized void createPublicDir( ActionBase action , String dir ) throws Exception {
		core.cmdCreatePublicDir( action , dir );
	}

	public synchronized String[] getFileLines( ActionBase action , String filePath ) throws Exception {
		if( account.local )
			return( action.readFileLines( filePath ).toArray( new String[0] ) );
		
		return( core.cmdGetFileLines( action , filePath ) );
	}
	
	public synchronized void downloadUnix( ActionBase action , String URL , String TARGETNAME , String auth ) throws Exception {
		if( core.osType != VarOSTYPE.LINUX )
			action.exitUnexpectedState();
		
		String TARGETDIRNAME;
		String TARGETFINALNAME;
		String FBASENAME;
		if( TARGETNAME.isEmpty() ) {
			FBASENAME = Common.getBaseName( URL );
			TARGETFINALNAME = FBASENAME;
		}
		else {
			FBASENAME = Common.getBaseName( TARGETNAME );
			TARGETDIRNAME = Common.getDirName( TARGETNAME );
			core.runCommandCheckDebug( action , "mkdir -p " + TARGETDIRNAME );
	
			TARGETFINALNAME = TARGETNAME;
		}
	
		action.debug( FBASENAME + ": wget " + URL + " ..." );

		// delete old if partial download
		core.runCommandCheckDebug( action , "rm -rf " + TARGETFINALNAME + " " + TARGETFINALNAME + ".md5" );
		String cmd = "wget -q " + Common.getQuoted( URL ) + " -O " + TARGETFINALNAME;
		if( auth != null && !auth.isEmpty() )
			cmd += " " + auth;
		int status = core.runCommandGetStatusDebug( action , cmd );
	
		if( status == 0 && checkFileExists( action , TARGETFINALNAME ) )
			createMD5( action , TARGETFINALNAME );
		else
			action.exit( URL + ": unable to download" );
	}

	public synchronized void prepareDirForLinux( ActionBase action , String dirPath ) throws Exception {
		String[] exts = action.meta.getConfigurableExtensions( action );
		
		// create find mask
		String mask = "";
		for( int k = 0; k < exts.length; k++ ) {
			if( k > 0 )
				mask += " -o ";
			mask += "-name " + Common.getQuoted( "*." + exts[ k ] );
		}
		
		if( !mask.isEmpty() )
			mask = " -a \\( " + mask + " \\) ";
		core.runCommandCheckDebug( action , dirPath , 
				"x=`find . -type f " + mask + "`" +
				"; if [ " + Common.getQuoted( "$x" ) + " != " + Common.getQuoted( "" ) + " ]; then sed -i " + Common.getQuoted( "s/\\r//" ) + " $x; fi" +
				"; x=`find . -name " + Common.getQuoted( "*.sh" ) + "`" +
				"; if [ " + Common.getQuoted( "$x" ) + " != " + Common.getQuoted( "" ) + " ]; then chmod 744 $x; fi" );
	}

}
