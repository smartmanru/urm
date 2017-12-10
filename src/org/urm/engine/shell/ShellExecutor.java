package org.urm.engine.shell;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.action.CommandOutput;
import org.urm.engine.storage.Folder;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.meta.Types.VarSESSIONTYPE;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvServer;

public abstract class ShellExecutor extends Shell {

	public String rootPath;
	public Folder tmpFolder;
	public boolean dedicated;
	private boolean fatal; 
	
	public long tsLastStarted;
	public long tsLastFinished;
	
	private ShellCore coreHidden;
	
	@Override
	abstract public boolean start( ActionBase action ) throws Exception;

	// construction and administration
	protected ShellExecutor( int id , String name , EngineShellPool pool , Account account , String rootPath , Folder tmpFolder , boolean dedicated ) {
		super( id , name , pool , account );
		this.rootPath = rootPath;
		this.tmpFolder = tmpFolder;
		this.dedicated = dedicated;
		this.fatal = false;
	}
	
	public boolean isWindowsFromUnix() {
		if( coreHidden.sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX )
			return( true );
		return( false );
	}
	
	public boolean isUnixFromWindows() {
		if( coreHidden.sessionType == VarSESSIONTYPE.UNIXFROMWINDOWS )
			return( true );
		return( false );
	}
	
	public boolean isUnixRemote() {
		if( coreHidden.sessionType == VarSESSIONTYPE.UNIXREMOTE )
			return( true );
		return( false );
	}
	
	public static ShellExecutor getLocalShellExecutor( ActionBase action , int id , String name , EngineShellPool pool , String rootPath , Folder tmpFolder , boolean dedicated ) throws Exception {
		ShellExecutor executor = new LocalShellExecutor( id , name , pool , rootPath , tmpFolder , dedicated );
		executor.coreHidden = ShellCore.createShellCore( action, executor , action.context.account.osType , true );
		return( executor );
	}

	public static ShellExecutor getRemoteShellExecutor( ActionBase action , int id , String name , EngineShellPool pool , Account account , AuthResource auth , boolean dedicated ) throws Exception {
		RedistStorage storage = action.artefactory.getRedistStorage( action , account );
		Folder tmpFolder = storage.getRedistTmpFolder( action );

		ShellExecutor executor = new RemoteShellExecutor( id , name , pool , account , tmpFolder , auth , dedicated );
		executor.coreHidden = ShellCore.createShellCore( action, executor , account.osType , false );
		return( executor );
	}

	public boolean isRunning() {
		return( coreHidden.running );
	}
	
	public boolean isLocal() {
		return( account.isLocal() );
	}
	
	private ShellCore opstart( ActionBase action ) throws Exception {
		if( !coreHidden.running ) {
			if( fatal ) {
				String accName = super.account.getHostLogin();
				action.exit1( _Error.ShellUnavailable1 , "fatal state of shell to account=" + accName , accName );
			}
			
			restart( action );
		}
		
		tsLastStarted = System.currentTimeMillis();
		tsLastFinished = 0;
		
		return( coreHidden );
	}
	
	private void opstop() {
		tsLastFinished = System.currentTimeMillis();
	}
	
	public void exitError( ActionBase action , int errorCode , String error , String[] params ) throws Exception {
		action.exit( errorCode , name + ": " + error , params );
	}
	
	public void restart( ActionBase action ) throws Exception {
		if( fatal ) {
			String accName = super.account.getHostLogin();
			action.exit1( _Error.ShellUnavailable1 , "fatal state of shell to account=" + accName , accName );
		}
		
		coreHidden.kill( action );
		coreHidden = ShellCore.createShellCore( action , this , coreHidden.osType , coreHidden.local );
		
		start( action );
		if( !coreHidden.initialized ) {
			fatal = true; 
			action.exit1( _Error.ShellInitFailed1 , "shell=" + name + " failed on init stage" , name );
		}
	}
	
	protected boolean createProcess( ActionBase action , ShellProcess process , AuthResource auth ) throws Exception {
		if( isLocal() )
			action.debug( "start shell=" + name + " at rootPath=" + rootPath + " (" + Common.getEnumLower( account.osType ) + ")" );
		else
			action.debug( "start shell=" + name + " (" + Common.getEnumLower( account.osType ) + ")" );
		return( coreHidden.createProcess( action , process , rootPath , auth ) );
	}

	@Override
	public void kill( ActionBase action ) throws Exception {
		action.debug( "kill shell=" + name + " at rootPath=" + rootPath );
		coreHidden.kill( action );
	}

	public void release( ActionBase action ) {
		action.trace( "release shell=" + name );
		pool.releaseExecutorShell( action , this );
	}
	
	// information
	public String getHomePath() {
		return( coreHidden.homePath );
	}

	// operations
	public synchronized String createDir( ActionBase action , String home , String dir ) throws Exception {
		try {
			opstart( action );
			String path = Common.getPath( home , dir );
			ensureDirExists( action , path );
			return( path );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void ensureDirExists( ActionBase action , String path ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( isLocal() ) {
				path = action.getLocalPath( path );
				if( !Files.isDirectory( Paths.get( path ) , LinkOption.NOFOLLOW_LINKS ) ) {
					File file = new File( path );
					if( !file.mkdirs() )
						action.exit1( _Error.UnableCreateDirectory1 , "Unable to create local directory: " + path , path );
					action.trace( "local directory created: " + path );
				}
				return;
			}
			core.cmdEnsureDirExists( action , path );
		}
		finally {
			opstop();
		}
	}

	public synchronized void createFileFromString( ActionBase action , String path , String value ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCreateFileFromString( action , path , value );
		}
		finally {
			opstop();
		}
	}

	public synchronized void appendFileWithString( ActionBase action , String path , String value ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( isLocal() ) {
				path = action.getLocalPath( path );
				List<String> list = new LinkedList<String>();
				list.add( value );
				Files.write( Paths.get( path ) , list , Charset.forName( "UTF-8" ) , StandardOpenOption.CREATE , StandardOpenOption.APPEND ); 
			}
			else
				core.cmdAppendFileWithString( action , path , value );
		}
		finally {
			opstop();
		}
	}

	public synchronized void appendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdAppendFileWithFile( action , pathDst , pathSrc );
		}
		finally {
			opstop();
		}
	}

	public synchronized boolean checkDirExists( ActionBase action , String path ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( path.isEmpty() )
				return( false );
			
			if( isLocal() ) {
				path = action.getLocalPath( path );
				return( Files.isDirectory( Paths.get( path ) , LinkOption.NOFOLLOW_LINKS ) );
			}
			
			return( core.cmdCheckDirExists( action , path ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized Map<String,List<String>> getFilesContent( ActionBase action , String dir , String fileMask ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetFilesContent( action , dir , fileMask ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized boolean isFileEmpty( ActionBase action , String path ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdIsFileEmpty( action , path ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized boolean checkFileExists( ActionBase action , String dir , String path ) throws Exception {
		return( checkFileExists( action , Common.getPath( dir , path ) ) );
	}
	
	public synchronized boolean checkFileExists( ActionBase action , String path ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( path.isEmpty() )
				return( false );
	
			if( isLocal() ) {
				path = action.getLocalPath( path );
				return( Files.isRegularFile( Paths.get( path ) , LinkOption.NOFOLLOW_LINKS ) );
			}
			return( core.cmdCheckFileExists( action , path ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized boolean checkPathExists( ActionBase action , String path ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( path.isEmpty() )
				return( false );
	
			if( isLocal() ) {
				path = action.getLocalPath( path );
				return( Files.exists( Paths.get( path ) , LinkOption.NOFOLLOW_LINKS ) );
			}
			return( core.cmdCheckPathExists( action , path ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized String findOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdFindOneTopWithGrep( action , path , mask , grepMask ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized String findOneTop( ActionBase action , String path , String mask ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdFindOneTop( action , path , mask ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized void createMD5( ActionBase action , String filepath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCreateMD5( action , filepath );
		}
		finally {
			opstop();
		}
	}

	public synchronized void removeDirContent( ActionBase action , String dirpath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdRemoveDirContent( action , dirpath );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void removeDir( ActionBase action , String dirpath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdRemoveDir( action , dirpath );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void recreateDir( ActionBase action , String dirpath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdRecreateDir( action , dirpath );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void removeFiles( ActionBase action , String dir , String files ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdRemoveFiles(action , dir , files );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void removeFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdRemoveFilesWithExclude( action , dir , files , exclude );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void unzip( ActionBase action , String runDir , String zipFile , String folder ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdUnzipPart( action , runDir , zipFile , folder , "" );
		}
		finally {
			opstop();
		}
	}

	public synchronized void unzipPart( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdUnzipPart( action , unzipDir , zipFile , targetDir , zipPart );
		}
		finally {
			opstop();
		}
	}

	public synchronized void move( ActionBase action , String source , String target ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdMove( action , source , target );
		}
		finally {
			opstop();
		}
	}

	public synchronized void extractTarGz( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdExtractTarGz( action , tarFile , targetFolder , part );
		}
		finally {
			opstop();
		}
	}

	public synchronized void extractTar( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdExtractTar( action , tarFile , targetFolder , part );
		}
		finally {
			opstop();
		}
	}

	public synchronized void extractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdExtractTarGz( action , tarFile , targetFolder , "" );
		}
		finally {
			opstop();
		}
	}

	public synchronized void extractTar( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdExtractTar( action , tarFile , targetFolder , "" );
		}
		finally {
			opstop();
		}
	}

	public synchronized String ls( ActionBase action , String path ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdLs( action , path ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized void createZipFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCreateZipFromDirContent( action , tarFile , dir , content , exclude );
		}
		finally {
			opstop();
		}
	}

	public synchronized void createTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCreateTarGzFromDirContent( action , tarFile , dir , content , exclude );
		}
		finally {
			opstop();
		}
	}

	public synchronized void createTarFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCreateTarFromDirContent( action , tarFile , dir , content , exclude );
		}
		finally {
			opstop();
		}
	}

	public synchronized String getFileInfo( ActionBase action , String dir , String dirFile ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetFileInfo( action , dir , dirFile ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized void custom( ActionBase action , String cmd ) throws Exception {
		custom( action , cmd , CommandOutput.LOGLEVEL_INFO );
	}
	
	public synchronized void custom( ActionBase action , String cmd , int logLevel ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommand( action , cmd , logLevel );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void custom( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommand( action , dir , cmd , CommandOutput.LOGLEVEL_INFO );
		}
		finally {
			opstop();
		}
	}

	public synchronized void checkErrors( ActionBase action ) throws Exception {
		try {
			ShellCore core = opstart( action );
			String err = core.getErr();
			if( !err.isEmpty() )
				action.exit2( _Error.ErrorExecutingCmd2 , "error executing CMD=" + core.cmdCurrent + ": " + err , core.cmdCurrent , err );
		}
		finally {
			opstop();
		}
	}

	public synchronized void customCheckErrorsDebug( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommand( action , dir , cmd , CommandOutput.LOGLEVEL_TRACE );
			String err = core.getErr();
			if( !err.isEmpty() )
				action.exit2( _Error.ErrorExecutingCmd2 , "error executing CMD=" + cmd + ": " + err , core.cmdCurrent , err );
		}
		finally {
			opstop();
		}
	}

	public synchronized void customCritical( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			String cmdDir = core.getDirCmd( action , dir , cmd );
			core.runCommandCritical( action , cmdDir );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void customCritical( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommandCritical( action , cmd );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized int customGetStatus( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.runCommandGetStatusDebug( action , cmd ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized int customGetStatus( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			String cmdDir = core.getDirCmd( action , dir , cmd );
			return( core.runCommandGetStatusDebug( action , cmdDir ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized int customGetStatusCheckErrors( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			String cmdDir = core.getDirCmd( action , dir , cmd );
			int status = core.runCommandGetStatusDebug( action , cmdDir );
			if( status != 0 )
				return( status );
			
			String errors = core.getErr();
			if( !errors.isEmpty() )
				return( -1 );
			
			return( 0 );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized int customGetStatusNormal( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.runCommandGetStatusNormal( action , cmd ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized int customGetStatusNormal( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			String cmdDir = core.getDirCmd( action , dir , cmd );
			return( core.runCommandGetStatusNormal( action , cmdDir ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void customCheckStatus( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommandCheckStatusDebug( action , cmd );
		}
		finally {
			opstop();
		}
	}

	public synchronized void customCheckStatus( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommandCheckStatusDebug( action , dir , cmd );
		}
		finally {
			opstop();
		}
	}

	public synchronized void customCheckErrorsDebug( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommandCheckDebug( action , cmd );
		}
		finally {
			opstop();
		}
	}

	public synchronized void customCheckErrorsNormal( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommandCheckNormal( action , cmd );
		}
		finally {
			opstop();
		}
	}

	public synchronized String customGetValueNoCheck( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.runCommandGetValueNoCheck( action , cmd , CommandOutput.LOGLEVEL_TRACE ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized String customGetValue( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.runCommandGetValueCheckDebug( action , cmd ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized String[] customGetLines( ActionBase action , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.runCommandGetLines( action , cmd , CommandOutput.LOGLEVEL_TRACE ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized String[] customGetLines( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.runCommandGetLines( action , dir , cmd , CommandOutput.LOGLEVEL_TRACE ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized String customGetValue( ActionBase action , String dir , String cmd ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.runCommandGetValueCheckDebug( action , dir , cmd ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized String[] findFiles( ActionBase action , String dir , String mask ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdFindFiles( action , dir , mask ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized String getFirstFile( ActionBase action , String dir ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetFirstFile( action , dir ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void createJarFromFolder( ActionBase action , String runDir , String jarFile , String folder ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCreateJarFromFolder( action , runDir , jarFile , folder );
		}
		finally {
			opstop();
		}
	}

	public synchronized void export( ActionBase action , String var , String value ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdSetShellVariable( action , var , value );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void mvnCheckStatus( ActionBase action , String runDir , String MAVEN_CMD ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.runCommandCheckStatusNormal( action , runDir , MAVEN_CMD );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void gitAddPomFiles( ActionBase action , String runDir ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdGitAddPomFiles( action , runDir );
		}
		finally {
			opstop();
		}
	}

	public synchronized void copyFiles( ActionBase action , String dirFrom , String files , String dirTo ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCopyFiles( action , dirFrom , files , dirTo );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void copyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCopyFile( action, fileFrom , fileTo );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void copyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCopyFile( action , fileFrom , targetDir , finalName , FOLDER );
		}
		finally {
			opstop();
		}
	}

	public synchronized void copyDirContent( ActionBase action , String srcDir , String dstDir ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCopyDirContent( action , srcDir , dstDir );
		}
		finally {
			opstop();
		}
	}

	public synchronized void copyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCopyDirDirect( action , dirFrom , dirTo );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void copyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCopyDirToBase( action , dirFrom , baseDstDir );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void scpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			int timeout = action.setTimeoutUnlimited();
			if( account.isNativeScp() )
				ShellProcess.scpFilesRemoteToLocal( action , srcPath , account , dstPath );
			else
				core.cmdScpFilesRemoteToLocal( action , srcPath , account , dstPath );
			action.setTimeout( timeout );
		}
		finally {
			opstop();
		}
	}

	public synchronized void scpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( account.isNativeScp() )
				ShellProcess.scpDirContentRemoteToLocal( action , srcPath , account , dstPath );
			else
				core.cmdScpDirContentRemoteToLocal( action , srcPath , account , dstPath );
		}
		finally {
			opstop();
		}
	}

	public synchronized void scpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( account.isNativeScp() )
				ShellProcess.scpFilesLocalToRemote( action , srcPath , account , dstPath );
			else
				core.cmdScpFilesLocalToRemote( action , srcPath , account , dstPath );
		}
		finally {
			opstop();
		}
	}

	public synchronized void scpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( account.isNativeScp() )
				ShellProcess.scpDirLocalToRemote( action , srcDirPath , account , baseDstDir );
			else
				core.cmdScpDirLocalToRemote( action , srcDirPath , account , baseDstDir );
		}
		finally {
			opstop();
		}
	}

	public synchronized void scpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( account.isNativeScp() )
				ShellProcess.scpDirContentLocalToRemote( action , srcDirPath , account , dstDir );
			else
				core.cmdScpDirContentLocalToRemote( action , srcDirPath , account , dstDir );
		}
		finally {
			opstop();
		}
	}

	public synchronized void scpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( account.isNativeScp() )
				ShellProcess.scpDirRemoteToLocal( action , srcPath , account , dstPath );
			else
				core.cmdScpDirRemoteToLocal( action , srcPath , account , dstPath );
		}
		finally {
			opstop();
		}
	}

	public synchronized void copyFileTargetToLocalDir( ActionBase action , Account account , String srcFilePath , String dstDir ) throws Exception {
		if( account.local )
			copyFile( action , srcFilePath , dstDir );
		else
			scpFilesRemoteToLocal( action , srcFilePath , account , Common.ensureDir( dstDir ) );
	}

	public synchronized void copyFileTargetToLocalFile( ActionBase action , Account account , String srcFilePath , String dstFilePath ) throws Exception {
		if( account.local )
			copyFile( action , srcFilePath , dstFilePath );
		else
			scpFilesRemoteToLocal( action , srcFilePath , account , dstFilePath );
	}

	public synchronized void copyFilesTargetToLocal( ActionBase action , Account account , String srcFiles , String dstDir ) throws Exception {
		if( account.local )
			copyFiles( action , Common.getDirName( srcFiles ) , Common.getBaseName( srcFiles ) , dstDir );
		else
			scpFilesRemoteToLocal( action , srcFiles , account , Common.ensureDir( dstDir ) );
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
		else
			scpFilesLocalToRemote( action , Common.getPath( srcDir , srcFiles ) , account , Common.ensureDir( dstDir ) );
	}

	public synchronized void copyDirContentTargetToLocal( ActionBase action , Account account , String srcDir , String dstDir ) throws Exception {
		if( account.local )
			copyDirContent( action , srcDir , dstDir );
		else
			scpDirContentRemoteToLocal( action , srcDir , account , Common.ensureDir( dstDir ) );
	}
	
	public synchronized void copyDirTargetToLocal( ActionBase action , Account account , String srcDir , String dstBaseDir ) throws Exception {
		if( account.local )
			copyDirToBase( action , srcDir , dstBaseDir );
		else
			scpDirRemoteToLocal( action , srcDir , account , Common.ensureDir( dstBaseDir ) );
	}

	public synchronized void copyFileLocalToTarget( ActionBase action , Account account , String srcFilePath , String dstDir ) throws Exception {
		if( account.local )
			copyFile( action , srcFilePath , dstDir , "" , "" );
		else
			scpFilesLocalToRemote( action , srcFilePath , account , Common.ensureDir( dstDir ) );
	}

	public synchronized void copyFileLocalToTargetRename( ActionBase action , Account account , String srcFilePath , String dstDir , String newName ) throws Exception {
		if( account.local )
			copyFile( action , srcFilePath , dstDir , newName , "" );
		else
			scpFilesLocalToRemote( action , srcFilePath , account , Common.getPath( dstDir , newName ) );
	}

	public synchronized void copyDirLocalToTarget( ActionBase action , Account account , String srcDirPath , String baseDstDir ) throws Exception {
		if( account.local )
			copyDirToBase( action , srcDirPath , baseDstDir );
		else
			scpDirLocalToRemote( action , srcDirPath , account , Common.ensureDir( baseDstDir ) );
	}

	public synchronized void copyDirContentLocalToTarget( ActionBase action , Account account , String srcDirPath , String dstDir ) throws Exception {
		if( account.local )
			copyDirContent( action , srcDirPath , dstDir );
		else
			scpDirContentLocalToRemote( action , srcDirPath , account , Common.ensureDir( dstDir ) );
	}

	public synchronized void copyDirFileToFile( ActionBase action , Account account , String dirPath , String fileSrc , String fileDst ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCopyDirFileToFile( action , account , dirPath , fileSrc , fileDst );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized String[] getFolders( ActionBase action , String rootPath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetFolders( action , rootPath ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void getDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files , String excludeRegExp ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdGetDirsAndFiles( action , rootPath , dirs , files , excludeRegExp );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void getTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( !isLocal() ) {
				core.cmdGetTopDirsAndFiles( action , rootPath , dirs , files );
				return;
			}

			rootPath = action.getLocalPath( rootPath );
			File folder = new File( rootPath );
			if( folder.exists() == false )
				return;
			
			if( !folder.isDirectory() )
				action.exit1( _Error.NotDirectoryPath1 , "not a directory path=" + rootPath , rootPath );
			
			for( final File fileEntry : folder.listFiles() ) {
		        if( fileEntry.isDirectory() )
		        	dirs.add( fileEntry.getName() );
		        else
		        	files.add( fileEntry.getName() );
		    }
		}
		finally {
			opstop();
		}
	}
	
	public synchronized String getMD5( ActionBase action , String filePath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetMD5( action , filePath ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized String getTarContentMD5( ActionBase action , String filePath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetTarContentMD5( action , filePath ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized String getArchivePartMD5( ActionBase action , String filePath , String archivePartPath , String EXT ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetArchivePartMD5( action , filePath , archivePartPath , EXT ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized String getFilesMD5( ActionBase action , String dir , String includeList , String excludeList ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetFilesMD5( action , dir , includeList , excludeList ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized String getFileContentAsString( ActionBase action , String filePath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( account.local )
				return( action.readFile( filePath ) );
			
			return( core.cmdGetFileContentAsString( action , filePath ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized String[] grepFile( ActionBase action , String filePath , String mask ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGrepFile( action , filePath , mask ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void replaceFileLine( ActionBase action , String filePath , String mask , String newLine ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdReplaceFileLine( action , filePath , mask , newLine );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized void appendExecuteLog( ActionBase action , String msg ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdAppendExecuteLog( action , msg ); 
		}
		finally {
			opstop();
		}
	}

	public synchronized void appendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdAppendUploadLog( action , src , dst );
		}
		finally {
			opstop();
		}
	}

	public synchronized void createPublicDir( ActionBase action , String dir ) throws Exception {
		try {
			ShellCore core = opstart( action );
			core.cmdCreatePublicDir( action , dir );
		}
		finally {
			opstop();
		}
	}

	public synchronized String[] getFileLines( ActionBase action , String filePath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( account.local )
				return( action.readFileLines( filePath ).toArray( new String[0] ) );
			
			return( core.cmdGetFileLines( action , filePath ) );
		}
		finally {
			opstop();
		}
	}
	
	public synchronized Date getFileChangeTime( ActionBase action , String path ) throws Exception {
		try {
			ShellCore core = opstart( action );
			return( core.cmdGetFileChangeTime( action , path ) );
		}
		finally {
			opstop();
		}
	}

	public synchronized void downloadUnix( ActionBase action , String URL , String TARGETNAME , String auth ) throws Exception {
		try {
			ShellCore core = opstart( action );
			if( !isLinux() )
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
				action.exit1( _Error.UnableDownload1 , URL + ": unable to download" , URL );
		}
		finally {
			opstop();
		}
	}

	public synchronized void prepareDirForLinux( ActionBase action , String dirPath ) throws Exception {
		try {
			ShellCore core = opstart( action );
			String[] exts = Meta.getConfigurableExtensions( action );
			
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
		finally {
			opstop();
		}
	}

	public boolean isSystemctlService( ActionBase action , MetaEnvServer server ) throws Exception {
		if( !server.isLinux() )
			return( false );

		if( !server.isService() )
			return( false );

		ShellCoreUnix coreUnix = ( ShellCoreUnix )coreHidden;
		if( coreUnix.osType.equals( "CentOS" ) && coreUnix.osTypeVersion.startsWith( "6." ) )
			return( false );
		
		if( coreUnix.osType.equals( "RedHat" ) && coreUnix.osTypeVersion.startsWith( "6." ) )
			return( false );

		return( true );
	}
	
	public String getSystemPath( ActionBase action , MetaEnvServer server ) throws Exception {
		if( !server.isLinux() )
			return( "" );
				
		if( server.isService() ) {
			if( isSystemctlService( action , server ) )
				return( "/usr/lib/systemd/system/" );
			return( "/etc/init.d" );
		}
		
		if( server.isGeneric() )
			return( Common.getPath( server.ROOTPATH , server.BINPATH ) );
			
		String value = Common.getEnumLower( server.getServerAccessType() );
		action.exit1( _Error.AccessTypeNotSupported1 , "Access type (" + value + ") is not supported for opertation" , value );
		return( null );
	}
	
	public String getSystemFiles( ActionBase action , MetaEnvServer server ) throws Exception {
		if( isLinux() && server.isService() ) {
			if( isSystemctlService( action , server ) )
				return( server.SYSNAME + ".*" );
			return( server.SYSNAME );
		}
		
		if( server.isGeneric() ) {
			if( isLinux() )
				return( "server.*.sh" );
			return( "server.*.cmd" );
		}
		
		String value = Common.getEnumLower( server.getServerAccessType() );
		action.exit1( _Error.AccessTypeNotSupported1 , "Access type (" + value + ") is not supported for opertation" , value );
		return( null );
	}

	public String getArtefactPath( ActionBase action , Meta meta , String FOLDER ) throws Exception {
		LocalFolder folder = action.artefactory.getArtefactFolder( action , account.osType , meta , FOLDER );
		return( folder.folderPath );
	}
	
	public String findVersionedFile( ActionBase action , String folderPath , String basename , String ext ) throws Exception {
		boolean addDotSlash = ( isWindows() )? false : true;
		String filePath = findOneTopWithGrep( action , folderPath , "*" + basename + "*" + ext , Common.getGrepMask( action , basename , addDotSlash , ext ) );

		// ensure correct file
		if( filePath.isEmpty() ) {
			action.trace( "findBinarySourceItemFile: file " + basename + ext + " not found in " + folderPath );
			return( "" );
		}

		return( Common.getBaseName( filePath ) );
	}

}
