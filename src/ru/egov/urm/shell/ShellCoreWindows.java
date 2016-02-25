package ru.egov.urm.shell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
		localSession.setWindowsHelper();
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
			
		execLine += " " + executor.account.HOSTLOGIN + " " + Common.getQuoted( "cmd /c chcp 65001 & " + Common.replace( cmd , "\\" , "\\\\" ) );
		action.trace( executor.name + " execute: " + cmd );
		
		localSession.runCommand( action , execLine , debug );
		cmdout.addAll( localSession.cmdout );
		cmderr.addAll( localSession.cmderr );
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
			
		execLine += " " + executor.account.HOSTLOGIN + " " + Common.getQuoted( "cmd /c chcp 65001 & " + Common.replace( cmd , "\\" , "\\\\" ) );
		action.trace( executor.name + " execute: " + cmd );
		
		int status = localSession.runCommandGetStatus( action , execLine , debug );
		cmdout.addAll( localSession.cmdout );
		cmderr.addAll( localSession.cmderr );
		
		return( status );
	}

	@Override public void runCommandCritical( ActionBase action , String cmd ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		String dirWin = Common.getWinPath( action , dir );
		return( "if exist " + dirWin + " ( cd " + dirWin + " & " + cmd + " )" );
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
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "check/create directory error" );
	}

	@Override public void cmdCreateFileFromString( ActionBase action , String path , String value ) throws Exception {
		String pathWin = Common.getWinPath( action , path );
		runCommand( action , "echo " + value + " > " + pathWin , true );
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
		if( value.equals( "ok" ) )
			return( true );
		
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "check directory error" );
		return( false );
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

	@Override public void cmdRemoveDirContent( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( action , dir );
		runCommand( action , "if exist " + wdir + " ( rmdir /S /Q " + wdir + " & md " + wdir + " )" , false );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "remove directory content error" );
	}
	
	@Override public void cmdRemoveDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( action , dir );
		runCommand( action , "if exist " + wdir + " rmdir /S /Q " + wdir , false );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "remove directory error" );
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
		String delimiter = "URM_DELIMITER";
		List<String> res = runCommandCheckGetOutputDebug( action , rootPath , 
				"chdir & dir /ad /s /b & echo " + delimiter + " & dir /a-d /b /s" );
		
		if( res.isEmpty() )
			action.exit( "directory " + rootPath + " does not exist" );
		
		String pwd = res.get( 0 );
		int skipStart = pwd.length() + 1;
		
		List<String> copyTo = dirs;
		boolean ok = false;
		for( int k = 1; k < res.size(); k++ ) {
			String s = res.get( k );
			if( s.startsWith( delimiter ) ) {
				copyTo = files;
				ok = true;
				continue;
			}

			if( s.startsWith( pwd ) )
				s = s.substring( skipStart );
			else
				action.exit( "unexpected line=" + s );
			
			s = s.replace( "\\" , "/" );
			copyTo.add( s );
		}
		
		if( !ok )
			action.exit( "unable to read directory " + rootPath );
	}

	@Override public void cmdGetTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public String cmdGetMD5( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( action , filePath );
		runCommand( action , "certutil -hashfile " + fileWin + " MD5" , true );
		if( cmdout.size() != 3 )
			action.exit( "unable to get md5sum of " + filePath );
		
		return( Common.replace( cmdout.get( 1 ) , " " , "" ) );
	}

	@Override public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , "type " + filePath );
		return( value );
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
	
	@Override public Map<String,List<String>> cmdGetFilesContent( ActionBase action , String dir , String fileMask ) throws Exception {
		String useMarker = "##";
		String cmd = "for %x in (" + fileMask + ") do @echo %x & type %x & echo " + useMarker;
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommand( action , cmdDir , true );
		
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		int pos = 0;
		List<String> data = null;
		for( String s : cmdout ) {
			if( pos == 0 ) {
				data = new LinkedList<String>();
				map.put( s , data );
				pos = 1;
				continue;
			}

			if( s.equals( useMarker ) ) {
				pos = 0;
				continue;
			}
				
			if( s.endsWith( useMarker ) ) {
				data.add( s.substring( 0 , s.length() - useMarker.length() ) );
				pos = 0;
				continue;
			}
			
			data.add( s );
		}
		
		if( pos != 0 )
			action.exit( "error reading files in dir=" + dir );
		
		return( map );
	}
	
}
