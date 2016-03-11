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
	
	private String prepareExecute( ActionBase action , String cmd , boolean debug ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
			
		cmdCurrent = cmd;
		cmdout.clear();
		cmderr.clear();
		
		String execLine = "ssh";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			execLine += " -i " + keyFile;

		String cmdWin = Common.replace( cmd , "\\" , "\\\\" );
		execLine += " " + executor.account.HOSTLOGIN + " " + Common.getQuoted( "cmd /c chcp 65001 & cmd /c \"" + cmdWin + "\"" );
		action.trace( executor.name + " execute: " + cmd );
		return( execLine );
	}
	
	private void getOutput( ActionBase action ) throws Exception {
		cmdout.addAll( localSession.cmdout );
		cmderr.addAll( localSession.cmderr );
	}
	
	@Override public void runCommand( ActionBase action , String cmd , boolean debug ) throws Exception {
		String execLine = prepareExecute( action , cmd , debug );
		
		localSession.runCommand( action , execLine , debug );
		getOutput( action );
	}

	@Override public int runCommandGetStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		String execLine = prepareExecute( action , cmd , debug );
		
		int status = localSession.runCommandGetStatus( action , execLine , debug );
		getOutput( action );
		
		return( status );
	}

	@Override public void runCommandCritical( ActionBase action , String cmd ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		String dirWin = Common.getWinPath( action , dir );
		return( "if exist " + dirWin + " ( cd " + dirWin + " && " + cmd + " ) else echo invalid directory: " + dir );
	}
	
	@Override public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		String dirWin = Common.getWinPath( action , dir );
		return( "if exist " + dirWin + " ( cd " + dirWin + " && " + cmd + " )" );
	}

	@Override protected void killProcess( ActionBase action ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdEnsureDirExists( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( action , dir );
		runCommand( action , "if not exist " + wdir + " md " + wdir , true );
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
		String wpath = Common.getWinPath( action , path );
		String value = this.runCommandGetValueCheckDebug( action , "if exist " + wpath + "/ ( echo dir ) else if exist " + wpath + " echo file" );
		if( value.equals( "file" ) )
			return( true );
		return( false );
	}

	@Override public boolean cmdCheckPathExists( ActionBase action , String path ) throws Exception {
		String wpath = Common.getWinPath( action , path );
		String value = this.runCommandGetValueCheckDebug( action , "if exist " + wpath + " echo ok" );
		if( value.equals( "ok" ) )
			return( true );
		return( false );
	}

	@Override public String cmdFindOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception {
		String cmdDir = getDirCmdIfDir( action , path , "dir /b " + mask );
		String[] values = this.runCommandGetLines( action , cmdDir , true );
		if( values.length == 0 || values[0].equals( "File Not Found" ) )
			return( "" );

		action.trace( "filter out files using mask=" + grepMask + " ..." );
		String[] list = Common.grep( values , grepMask );
		if( list.length == 0 )
			return( "" );
			
		if( list.length > 1 )
			action.exit( "too many files found in path=" + path + ", mask=" + Common.getQuoted( mask ) + " (" + Common.getList( list ) + ")" );
		
		return( list[0] );
	}
	
	@Override public String cmdFindOneTop( ActionBase action , String path , String mask ) throws Exception {
		String cmdDir = getDirCmdIfDir( action , path , "dir /b " + mask );
		String[] list = this.runCommandGetLines( action , cmdDir , true );
		if( list.length == 0 || list[0].equals( "File Not Found" ) )
			return( "" );
		
		if( list.length > 1 )
			action.exit( "too many files found in path=" + path + ", mask=" + Common.getQuoted( mask ) + " (" + Common.getList( list ) + ")" );
		
		return( list[0] );
	}

	@Override public void cmdCreateMD5( ActionBase action , String filepath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdRemoveDirContent( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( action , dir );
		runCommand( action , "if exist " + wdir + " ( rmdir /S /Q " + wdir + " && md " + wdir + " )" , true );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "remove directory content error" );
	}
	
	@Override public void cmdRemoveDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( action , dir );
		runCommand( action , "if exist " + wdir + " rmdir /S /Q " + wdir , true );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "remove directory error" );
	}
	
	@Override public void cmdRecreateDir( ActionBase action , String dirpath ) throws Exception {
		action.exitNotImplemented();
	}

	public String getRegularMaskList( ActionBase action , String maskList ) throws Exception {
		String reg = "";
		for( String mask : Common.splitSpaced( maskList ) ) {
			if( !reg.isEmpty() )
				reg += " ";
			mask = Common.replace( mask , "." , "\\." );
			mask = Common.replace( mask , "*" , ".*" );
			reg += "^mask$";
		}
		return( reg );
	}

	@Override public void cmdRemoveFiles( ActionBase action , String dir , String files ) throws Exception {
		String filesRegular = getRegularMaskList( action , files );
		String cmdDir = getDirCmdIfDir( action , dir , 
				"( for /f %x in ('dir /b /ad | findstr /R " + 
				Common.getQuoted( filesRegular ) + "') do rmdir /Q /S %x ) && " +
				"( for /f %x in ('dir /b /a-d | findstr /R " + 
				Common.getQuoted( filesRegular ) + "') do del /Q %x )" );
		runCommandCheckStatus( action , cmdDir , true );
	}

	@Override public void cmdRemoveFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		if( exclude.isEmpty() ) {
			cmdRemoveFiles( action , dir , files );
			return;
		}
		
		String filesRegular = getRegularMaskList( action , files );
		String excludeRegular = getRegularMaskList( action , exclude );
		String cmdDir = getDirCmdIfDir( action , dir , 
				"( for /f %x in ('dir /b /ad | findstr /R " + 
				Common.getQuoted( filesRegular ) + " | findstr /V " +
				Common.getQuoted( excludeRegular ) + "') do rmdir /Q /S %x ) && " +
				"( for /f %x in ('dir /b /a-d | findstr /R " + 
				Common.getQuoted( filesRegular ) + " | findstr /V " +
				Common.getQuoted( excludeRegular ) + "') do del /Q %x )" );
		runCommandCheckStatus( action , cmdDir , true );
	}

	@Override public void cmdUnzipPart( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdMove( ActionBase action , String source , String target ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		String wtarFile = Common.getWinPath( action , tarFile );
		runCommandCheckStatusDebug( action , targetFolder , "7z x -y -bd " + wtarFile );
	}
	
	@Override public void cmdExtractTar( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		String wtarFile = Common.getWinPath( action , tarFile );
		runCommandCheckStatusDebug( action , targetFolder , "7z x -y -bd " + wtarFile );
	}
	
	@Override public String cmdLs( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public void cmdCreateZipFromDirContent( ActionBase action , String zipFile , String dir , String content ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCreateTarFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		String wtarFile = Common.getWinPath( action , tarFile );
		String contentArgs = "";
		for( String item : Common.split( content , " " ) )
			contentArgs += " -i!" + Common.getWinPath( action , item );
		
		String excludeArgs = "";
		for( String item : Common.split( exclude , " " ) )
			excludeArgs += " -x!" + Common.getWinPath( action , item );
		
		runCommandCheckStatusDebug( action , dir , "7z a -ttar -r -bd " + wtarFile + " " + contentArgs + " " + excludeArgs );
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
		action.debug( "copy " + fileFrom + " to " + fileTo + " ..." );
		String wfileFrom = Common.getWinPath( action , fileFrom );
		String wfileTo = Common.getWinPath( action , fileTo );
		runCommandCheckStatus( action , "copy /Y " + wfileFrom + " " + wfileTo , true );
	}
	
	@Override public void cmdCopyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception {
		String finalDir = Common.getPath( targetDir , FOLDER );
		String baseName = Common.getBaseName( fileFrom );
		String finalFile;
		if( !finalName.isEmpty() )
			finalFile = finalDir + "/" + finalName;
		else
			finalFile = finalDir + "/" + baseName;

		cmdCopyFile( action , fileFrom , finalFile );
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
				"chdir && dir /ad /s /b && echo " + delimiter + " && dir /a-d /b /s" );
		
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
		String delimiter = "URM_DELIMITER";
		String cmd = "dir /ad /b && echo " + delimiter + " && dir /a-d /b"; 
		String dirCmd = getDirCmd( action , rootPath , cmd );
		runCommand( action , dirCmd , true );
		
		List<String> list = dirs; 
		for( String s : cmdout ) {
			if( s.startsWith( delimiter ) ) {
				list = files;
				continue;
			}
			
			list.add( s );
		}
		
		if( files.size() == 1 && files.get( 0 ).equals( "File Not Found" ) )
			files.clear();
	}

	@Override public String cmdGetMD5( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( action , filePath );
		runCommand( action , "certutil -hashfile " + fileWin + " MD5" , true );
		if( cmdout.size() != 3 )
			action.exit( "unable to get md5sum of " + filePath );
		
		return( Common.replace( cmdout.get( 1 ) , " " , "" ) );
	}

	@Override public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( action , filePath );
		String value = runCommandGetValueCheckDebug( action , "type " + fileWin );
		return( value );
	}

	@Override public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception {
		String executeLog = Common.getWinPath( action , Common.getPath( executor.rootPath , "execute.log" ) );
		String ts = Common.getLogTimeStamp();
		runCommand( action , "echo " + Common.getQuoted( ts + ": " + msg ) + " >> " + executeLog , true );
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
		String cmd = "for %x in (" + fileMask + ") do @echo %x && type %x && echo " + useMarker;
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommand( action , cmdDir , true );
		
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		int pos = 0;
		List<String> data = null;
		for( String s : cmdout ) {
			s = s.trim();
			action.trace( "cmdout=[" + s + "]" );
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
