package ru.egov.urm.shell;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.Folder;

public class ShellCoreWindows extends ShellCore {

	ShellCoreUnix localSession;
	
	public ShellCoreWindows( ShellExecutor executor , VarOSTYPE osType , Folder tmpFolder ) {
		super( executor , osType , tmpFolder );
	}

	@Override public void createProcess( ActionBase action , ProcessBuilder builder , String rootPath ) throws Exception {
		localSession = new ShellCoreUnix( executor , executor.account.OSTYPE , tmpFolder );
		running = true;
		localSession.createProcess( action , builder , rootPath );
		initialized = true;
	}
	
	@Override public void kill( ActionBase action ) throws Exception {
		localSession.kill( action );
		super.kill( action );
	}
	
	@Override protected String getExportCmd( ActionBase action ) throws Exception {
		return( "" );
	}

	@Override protected void getProcessAttributes( ActionBase action ) throws Exception {
	}
	
	@Override public void runCommand( ActionBase action , String cmd , boolean debug ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
			
		cmdCurrent = cmd;
		cmdout.clear();
		cmderr.clear();
		
		String execLine = "ssh";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			execLine += " -i " + keyFile;
			
		execLine += " " + executor.account.HOSTLOGIN + " " + Common.getQuoted( "cmd /c chcp 65001 & " + cmd );
		action.trace( executor.name + " execute: " + cmd );
		
		localSession.runCommand( action , execLine , debug );
		if( localSession.cmdout.size() > 0 && localSession.cmdout.get( 0 ).equals( "Active code page: 65001" ) ) {
			for( int k = 1; k < localSession.cmdout.size(); k++ )
				cmdout.add( localSession.cmdout.get( k ) );
			cmderr.addAll( localSession.cmderr );
		}
		else
			action.exit( "unable to change codepage to utf-8" );
	}

	@Override public int runCommandGetStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
			
		cmdCurrent = cmd;
		cmdout.clear();
		cmderr.clear();
		
		String execLine = "ssh";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			execLine += " -i " + keyFile;
			
		execLine += " " + executor.account.HOSTLOGIN + " " + Common.getQuoted( "cmd /c chcp 65001 & " + cmd );
		action.trace( executor.name + " execute: " + cmd );
		
		int status = localSession.runCommandGetStatus( action , execLine , debug );
		if( localSession.cmdout.size() > 0 && localSession.cmdout.get( 0 ).equals( "Active code page: 65001" ) ) {
			for( int k = 1; k < localSession.cmdout.size(); k++ )
				cmdout.add( localSession.cmdout.get( k ) );
			cmderr.addAll( localSession.cmderr );
		}
		else
			action.exit( "unable to change codepage to utf-8" );
		
		return( status );
	}

	@Override public void runCommandCritical( ActionBase action , String cmd ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		return( "cd " + Common.getWinPath( action , dir ) + " & " + cmd );
	}
	
	@Override public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override protected void killProcess( ActionBase action ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdEnsureDirExists( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( action , dir );
		runCommand( action , "if not exist " + wdir + " md " + wdir , false );
	}

	@Override public void cmdCreateFileFromString( ActionBase action , String path , String value ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdAppendFileWithString( ActionBase action , String path , String value ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdAppendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public boolean cmdCheckDirExists( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( action , dir );
		String value = this.runCommandGetValueCheck( action , "if exist " + wdir + " echo ok" , true );
		return( value.equals( "ok" ) );
	}

	@Override public boolean cmdIsFileEmpty( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}

	@Override public boolean cmdCheckFileExists( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}

	@Override public boolean cmdCheckPathExists( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}

	@Override public String cmdFindOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public String cmdFindOneTop( ActionBase action , String path , String mask ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public void cmdCreateMD5( ActionBase action , String filepath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdRemoveDirContent( ActionBase action , String dirpath ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdRemoveDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( action , dir );
		runCommand( action , "if exist " + wdir + " rmdir /S /Q " + wdir , false );
	}
	
	@Override public void cmdRecreateDir( ActionBase action , String dirpath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdRemoveFiles( ActionBase action , String dir , String files ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdRemoveFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdUnzipPart( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdMove( ActionBase action , String source , String target ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public String cmdLs( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public String cmdGetFileInfo( ActionBase action , String dir , String dirFile ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public void cmdCreateJarFromFolder( ActionBase action , String runDir , String jarFile , String folder ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdSetShellVariable( ActionBase action , String var , String value ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdGitAddPomFiles( ActionBase action , String runDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCd( ActionBase action , String dir ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCopyFiles( ActionBase action , String dirFrom , String files , String dirTo ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCopyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCopyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCopyDirContent( ActionBase action , String srcDir , String dstDir ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCopyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCopyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdScpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCopyDirFileToFile( ActionBase action , Account account , String dirPath , String fileSrc , String fileDst ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdGetDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdGetTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public String cmdGetMD5( ActionBase action , String filePath ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception {
		String executeLog = Common.getWinPath( action , Common.getPath( executor.rootPath , "execute.log" ) );
		String ts = Common.getLogTimeStamp();
		runCommand( action , "echo " + Common.getQuoted( ts + ": " + msg ) + " >> " + executeLog , false );
	}

	@Override public void cmdAppendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public String[] cmdGetFolders( ActionBase action , String rootPath ) throws Exception {
		return( null );
	}

	@Override public String cmdGetFirstFile( ActionBase action , String dir ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public String[] cmdFindFiles( ActionBase action , String dir , String mask ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}

	@Override public String cmdGetTarContentMD5( ActionBase action , String filePath ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public String cmdGetFilesMD5( ActionBase action , String dir , String includeList , String excludeList ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}
	
}
