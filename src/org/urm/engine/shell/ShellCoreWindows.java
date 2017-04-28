package org.urm.engine.shell;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.action.CommandOutput;
import org.urm.engine.storage.Folder;
import org.urm.meta.Types.*;
import org.urm.meta.engine.ServerAuthResource;

public class ShellCoreWindows extends ShellCore {

	ShellCoreUnix localSession;

	String cmdAnd;

	public ShellCoreWindows( ShellExecutor executor , VarSESSIONTYPE sessionType , Folder tmpFolder , boolean local ) {
		super( executor , VarOSTYPE.WINDOWS , sessionType , tmpFolder , local );
		cmdAnd = "&&";
	}

	@Override 
	public boolean createProcess( ActionBase action , ShellProcess process , String rootPath , ServerAuthResource auth ) throws Exception {
		if( rootPath == null )
			action.exitUnexpectedState();
		
		if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX ) {
			localSession = new ShellCoreUnix( executor , VarSESSIONTYPE.UNIXLOCAL , tmpFolder , true );
			localSession.setWindowsHelper();
			running = true;
			if( !localSession.createProcess( action , process , action.context.CTX_REDISTWIN_PATH , auth ) )
				return( false );
			
			initialized = true;
			return( true );
		}
		
		return( super.createProcess( action , process , rootPath , auth ) );
	}
	
	@Override 
	public void kill( ActionBase action ) throws Exception {
		if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX )
			localSession.kill( action );
		
		super.kill( action );
	}
	
	@Override 
	protected boolean getProcessAttributes( ActionBase action ) throws Exception {
		super.homePath = action.context.session.installPath;
		
		runCommand( action , "echo off && chcp 1251" , CommandOutput.LOGLEVEL_TRACE );
		return( true );
	}
	
	@Override 
	public void runCommand( ActionBase action , String cmd , int logLevel ) throws Exception {
		runCommand( action , cmd , logLevel , false );
	}
	
	@Override 
	public int runCommandGetStatus( ActionBase action , String cmd , int logLevel ) throws Exception {
		if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX ) {
			String execLine = prepareExecuteWindowsFromLinux( action , cmd , logLevel );
			int status = localSession.runCommandGetStatus( action , execLine , logLevel );
			getOutput( action );
			return( status );
		}
		else {
			runCommand( action , cmd , logLevel , true );
			if( cmdout.size() > 0 ) {
				String last = cmdout.get( cmdout.size() - 1 );
				if( last.startsWith( "status=" ) ) {
					int status = Integer.parseInt( Common.getPartAfterFirst( last , "status=" ) );
					return( status );
				}
			}
		}
		
		action.exit1( _Error.ErrorExecutingCmd1 , "error executing cmd=(" + cmd + ")" , cmd );
		return( 0 );
	}

	@Override 
	public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		String dirWin = Common.getWinPath( dir );
		String rootPathWin = Common.getWinPath( executor.rootPath );
		return( "if exist " + dirWin + " ( cd /D " + dirWin + " " + cmdAnd + " ( " + cmd + " ) " + cmdAnd + " " + "cd /D " + Common.getQuoted( rootPathWin ) + " ) else echo invalid directory: " + dirWin );
	}
	
	@Override 
	public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		String dirWin = Common.getWinPath( dir );
		String rootPathWin = Common.getWinPath( executor.rootPath );
		return( "if exist " + dirWin + " ( cd /D " + dirWin + " " + cmdAnd + " ( " + cmd + " ) " + cmdAnd + " " + "cd /D " + Common.getQuoted( rootPathWin ) + " )" );
	}

	@Override 
	public void cmdEnsureDirExists( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "if not exist " + wdir + " md " + wdir , CommandOutput.LOGLEVEL_TRACE );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit1( _Error.CheckCreateDirectoryError1 , "check/create directory error" , wdir );
	}

	@Override 
	public void cmdCreateFileFromString( ActionBase action , String path , String value ) throws Exception {
		String pathWin = Common.getWinPath( path );
		if( value.isEmpty() )
			runCommand( action , "type NUL > " + pathWin , CommandOutput.LOGLEVEL_TRACE );
		else
			runCommand( action , "echo " + value + " > " + pathWin , CommandOutput.LOGLEVEL_TRACE );
	}

	@Override 
	public void cmdAppendFileWithString( ActionBase action , String path , String value ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdAppendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override 
	public boolean cmdCheckDirExists( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		String value = this.runCommandGetValueCheck( action , "if exist " + wdir + " echo ok" , CommandOutput.LOGLEVEL_TRACE );
		if( value.equals( "ok" ) )
			return( true );
		
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit1( _Error.CheckDirectoryError1 , "check directory error" , wdir );
		return( false );
	}

	@Override 
	public boolean cmdIsFileEmpty( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}

	@Override 
	public boolean cmdCheckFileExists( ActionBase action , String path ) throws Exception {
		String wpath = Common.getWinPath( path );
		String value = this.runCommandGetValueCheckDebug( action , "if exist " + wpath + "\\ ( echo dir ) else if exist " + 
				wpath + " echo file" );
		if( value.equals( "file" ) )
			return( true );
		return( false );
	}

	@Override 
	public boolean cmdCheckPathExists( ActionBase action , String path ) throws Exception {
		String wpath = Common.getWinPath( path );
		String value = this.runCommandGetValueCheckDebug( action , "if exist " + wpath + " echo ok" );
		if( value.equals( "ok" ) )
			return( true );
		return( false );
	}

	@Override 
	public String cmdFindOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception {
		String cmdDir = getDirCmdIfDir( action , path , "dir /b " + mask );
		String[] values = this.runCommandGetLines( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		if( values.length == 0 || values[0].equals( "File Not Found" ) )
			return( "" );

		action.trace( "filter out files using mask=" + grepMask + " ..." );
		String[] list = Common.grep( values , grepMask );
		if( list.length == 0 )
			return( "" );
			
		if( list.length > 1 ) {
			String xlist = Common.getList( list );
			action.exit3( _Error.TooManyFilesInPath3 , "too many files found in path=" + path + ", mask=" + Common.getQuoted( mask ) + " (" + xlist + ")" , path , mask , xlist );
		}
		
		return( list[0] );
	}
	
	@Override 
	public String cmdFindOneTop( ActionBase action , String path , String mask ) throws Exception {
		String cmdDir = getDirCmdIfDir( action , path , "dir /b " + mask );
		String[] list = this.runCommandGetLines( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		if( list.length == 0 || list[0].equals( "File Not Found" ) )
			return( "" );
		
		if( list.length > 1 ) {
			String xlist = Common.getList( list );
			action.exit3( _Error.TooManyFilesInPath3 , "too many files found in path=" + path + ", mask=" + Common.getQuoted( mask ) + " (" + xlist + ")" , path , mask , xlist );
		}
		
		return( list[0] );
	}

	@Override 
	public void cmdCreateMD5( ActionBase action , String filePath ) throws Exception {
		String value = cmdGetMD5( action , filePath );
		String filePathMD5 = filePath + ".md5";
		cmdCreateFileFromString( action , filePathMD5 , value );
	}

	@Override 
	public void cmdRemoveDirContent( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "if exist " + wdir + " ( rmdir /S /Q " + wdir + " " + cmdAnd + " md " + wdir + " )" , CommandOutput.LOGLEVEL_TRACE );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit1( _Error.RemoveDirectoryContentError1 , "remove directory content error" , wdir );
	}
	
	@Override 
	public void cmdRemoveDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "if exist " + wdir + " rmdir /S /Q " + wdir , CommandOutput.LOGLEVEL_TRACE );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit1( _Error.RemoveDirectoryError1 , "remove directory error" , wdir );
	}
	
	@Override 
	public void cmdRecreateDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "( if exist " + wdir + " rmdir /S /Q " + wdir + " ) " + cmdAnd + " md " + wdir , CommandOutput.LOGLEVEL_TRACE );
	}

	@Override 
	public void cmdCreatePublicDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "md " + wdir , CommandOutput.LOGLEVEL_TRACE );
	}

	@Override 
	public String[] cmdGrepFile( ActionBase action , String filePath , String mask ) throws Exception {
		String wpath = Common.getWinPath( filePath );
		return( runCommandGetLines( action , "type " + wpath + " | findstr /C:" + Common.getQuoted( mask ) , CommandOutput.LOGLEVEL_TRACE ) );
	}

	@Override 
	public void cmdReplaceFileLine( ActionBase action , String filePath , String mask , String newLine ) throws Exception {
		String filePathWin = Common.getWinPath( filePath );
		String filePathTmp = filePathWin + ".new";
		String cmd = "findstr /V " + mask + " " + filePathWin + " > " + filePathTmp + 
				" " + cmdAnd + " del /Q " + filePathWin + 
				" " + cmdAnd + " rename " + filePathTmp + " " + Common.getBaseName( filePath );
		
		if( !newLine.isEmpty() )
			cmd += " " + cmdAnd + " echo " + newLine + " >> " + filePathWin;
		runCommandCheckDebug( action , cmd );
	}
	
	@Override 
	public void cmdRemoveFiles( ActionBase action , String dir , String files ) throws Exception {
		String filesRegular = getRegularMaskList( action , files );
		String cmdDir = getDirCmdIfDir( action , dir , 
				"for /f %x in ('dir /b /ad ^| findstr /R " + 
				Common.getQuoted( filesRegular ) + "') do @rmdir /Q /S %x" );
		runCommand( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		checkOut( action , _Error.ErrorsDeleteDirs1 , "errors on delete dirs" , new String[] { filesRegular } );
						
		cmdDir = getDirCmdIfDir( action , dir , 
				"for /f %x in ('dir /b /a-d ^| findstr /R " + 
				Common.getQuoted( filesRegular ) + "') do @del /Q %x" );
		runCommand( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		checkOut( action , _Error.ErrorsDeleteFiles1 , "errors on delete files" , new String[] { filesRegular } );
	}

	@Override 
	public void cmdRemoveFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		if( exclude.isEmpty() ) {
			cmdRemoveFiles( action , dir , files );
			return;
		}
		
		String filesRegular = getRegularMaskList( action , files );
		String excludeRegular = getRegularMaskList( action , exclude );
		String cmdDir = getDirCmdIfDir( action , dir , 
				"for /f %x in ('dir /b /ad ^| findstr /R " + 
				Common.getQuoted( filesRegular ) + " ^| findstr /V " +
				Common.getQuoted( excludeRegular ) + "') do rmdir /Q /S %x" );
		runCommand( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		checkOut( action , _Error.ErrorsDeleteDirs2 , "errors on delete dirs" , new String[] { filesRegular , excludeRegular } );
		
		cmdDir = getDirCmdIfDir( action , dir , 
				"for /f %x in ('dir /b /a-d ^| findstr /R " + 
				Common.getQuoted( filesRegular ) + " ^| findstr /V " +
				Common.getQuoted( excludeRegular ) + "') do del /Q %x" );
		runCommand( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		checkOut( action , _Error.ErrorsDeleteFiles2 , "errors on delete files" , new String[] { filesRegular , excludeRegular } );
	}

	@Override 
	public void cmdUnzipPart( ActionBase action , String unzipDir , String zipFile , String targetFolder , String part ) throws Exception {
		String extractPart = ( part == null || part.isEmpty() )? "" : part;
		
		String wtarFile = Common.getWinPath( zipFile );
		String cmd = "7z x -tzip -y -bd " + wtarFile + " " + extractPart;
		if( !extractPart.isEmpty() )
			if( !extractPart.equals( targetFolder ) )
				cmd += " " + cmdAnd + " rmdir /S /Q " + targetFolder + " " + cmdAnd + " rename " + extractPart + " " + targetFolder;
		String wtargetParent = Common.getWinPath( unzipDir );
		
		int timeout = action.setTimeoutUnlimited();
		runCommandCheckStatusDebug( action , wtargetParent , cmd );
		action.setTimeout( timeout );
	}

	@Override 
	public void cmdMove( ActionBase action , String source , String target ) throws Exception {
		String wsource = Common.getWinPath( source );
		String wtarget = Common.getWinPath( target );
		runCommandCheckStatus( action , "move /Y " + wsource + " " + wtarget , CommandOutput.LOGLEVEL_TRACE );
	}

	@Override 
	public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		cmdExtractAny( action , tarFile , targetFolder , part , "tar" );
	}
	
	@Override 
	public void cmdExtractTar( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		cmdExtractAny( action , tarFile , targetFolder , part , "tar" );
	}
	
	@Override 
	public String cmdLs( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override 
	public void cmdCreateZipFromDirContent( ActionBase action , String zipFile , String dir , String content , String exclude ) throws Exception {
		cmdCreateAnyFromDirContent( action , zipFile , dir , content , exclude , "zip" );
	}
	
	@Override 
	public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdCreateTarFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		cmdCreateAnyFromDirContent( action , tarFile , dir , content , exclude , "tar" );
	}

	@Override 
	public String cmdGetFileInfo( ActionBase action , String dir , String dirFile ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override 
	public void cmdCreateJarFromFolder( ActionBase action , String runDir , String jarFile , String folder ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override 
	public void cmdSetShellVariable( ActionBase action , String var , String value ) throws Exception {
		runCommandCheckNormal( action , "set \"" + var + "=" + value + "\"" );
	}

	@Override 
	public void cmdGitAddPomFiles( ActionBase action , String runDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdCopyFiles( ActionBase action , String dirFrom , String files , String dirTo ) throws Exception {
		action.debug( "copy " + files + " from " + dirFrom + " to " + dirTo + " ..." );
		String wfilesFrom = Common.getWinPath( files );
		String wdirTo = Common.getWinPath( dirTo ) + "\\";
		runCommandCheckStatus( action , dirFrom , "for %x in ( " + wfilesFrom + " ) do xcopy /Y /Q \"%~x\" " + wdirTo , CommandOutput.LOGLEVEL_TRACE );
	}

	@Override 
	public void cmdCopyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception {
		action.debug( "copy " + fileFrom + " to " + fileTo + " ..." );
		String wfileFrom = Common.getWinPath( fileFrom );
		String wfileTo = Common.getWinPath( fileTo );
		runCommandCheckStatus( action , "copy /Y " + wfileFrom + " " + wfileTo , CommandOutput.LOGLEVEL_TRACE );
	}
	
	@Override 
	public void cmdCopyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception {
		String finalDir = Common.getPath( targetDir , FOLDER );
		String baseName = Common.getBaseName( fileFrom );
		String finalFile;
		if( !finalName.isEmpty() )
			finalFile = finalDir + "/" + finalName;
		else
			finalFile = finalDir + "/" + baseName;

		cmdCopyFile( action , fileFrom , finalFile );
	}

	@Override 
	public void cmdCopyDirContent( ActionBase action , String srcDir , String dstDir ) throws Exception {
		action.debug( "copy content from " + srcDir + " to " + dstDir + " ..." );
		String wdirFrom = Common.getWinPath( srcDir );
		String wdirTo = Common.getWinPath( dstDir );
		runCommandCheckDebug( action , "xcopy /Q /Y /E " + wdirFrom + "\\* " + wdirTo + "\\" );
	}
	
	@Override 
	public void cmdCopyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception {
		action.debug( "copy dir " + dirFrom + " to " + dirTo + " ..." );
		String wdirFrom = Common.getWinPath( dirFrom );
		String wdirTo = Common.getWinPath( dirTo );
		cmdRemoveDir( action , dirTo );
		cmdEnsureDirExists( action , dirTo );
		runCommandCheckDebug( action , "xcopy /Q /Y /E " + wdirFrom + "\\* " + wdirTo + "\\" );
	}
	
	@Override 
	public void cmdCopyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception {
		String baseName = Common.getBaseName( dirFrom );
		String dirTo = baseDstDir + "/" + baseName;
		cmdRemoveDir( action , dirTo );
		cmdEnsureDirExists( action , dirTo );
		
		action.debug( "copy " + dirFrom + " to " + dirTo + " ..." );
		String wdirFrom = Common.getWinPath( dirFrom );
		String wdirTo = Common.getWinPath( dirTo );
		runCommandCheckDebug( action , "xcopy /Q /Y /E " + wdirFrom + " " + wdirTo  );
	}
	
	@Override 
	public void cmdScpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdScpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdScpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdScpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdScpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdScpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override 
	public void cmdCopyDirFileToFile( ActionBase action , Account account , String dirPath , String fileSrc , String fileDst ) throws Exception {
		String wfileSrc = Common.getWinPath( fileSrc );
		String wfileDst = Common.getWinPath( fileDst );
		String cmdDir = getDirCmdIfDir( action , dirPath , "copy /Y " + wfileSrc + " " + wfileDst );
		runCommandCheckDebug( action , cmdDir );
	}

	@Override 
	public void cmdGetDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files , String excludeRegExp ) throws Exception {
		String excludeOption = ( excludeRegExp == null || excludeRegExp.isEmpty() )? "" : " | findstr /V /R \"" + excludeRegExp + "\"";
		List<String> resDirs = runCommandCheckGetOutputDebug( action , rootPath , 
				"chdir " + cmdAnd + " dir /ad /s /b" + excludeOption + " 2>nul" );
		
		if( resDirs.isEmpty() )
			action.exit1( _Error.MissingDirectory1 , "directory " + rootPath + " does not exist" , rootPath );
		
		String pwd = resDirs.get( 0 );
		int skipStart = pwd.length() + 1;
		
		for( int k = 1; k < resDirs.size(); k++ ) {
			String s = resDirs.get( k );
			if( s.startsWith( pwd ) )
				s = s.substring( skipStart );
			else
				action.exit1( _Error.UnexpectedDirContentLine1 , "unexpected line=" + s , s );
			
			s = s.replace( "\\" , "/" );
			dirs.add( s );
		}
		
		List<String> resFiles = runCommandCheckGetOutputDebug( action , rootPath , 
				"dir /a-d /s /b" + excludeOption + " 2>nul" );
		for( int k = 0; k < resFiles.size(); k++ ) {
			String s = resFiles.get( k );
			if( s.startsWith( pwd ) )
				s = s.substring( skipStart );
			else
				action.exit1( _Error.UnexpectedDirContentLine1 , "unexpected line=" + s , s );
			
			s = s.replace( "\\" , "/" );
			files.add( s );
		}
	}

	@Override 
	public void cmdGetTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		String delimiter = "URM_DELIMITER";
		String cmd = "dir /ad /b " + cmdAnd + " echo " + delimiter + " " + cmdAnd + " dir /a-d /b 2>nul"; 
		String dirCmd = getDirCmd( action , rootPath , cmd );
		runCommand( action , dirCmd , CommandOutput.LOGLEVEL_TRACE );
		
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

	@Override 
	public String cmdGetMD5( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( filePath );
		runCommand( action , "certutil -hashfile " + Common.getQuoted( fileWin ) + " MD5" , CommandOutput.LOGLEVEL_TRACE );
		if( cmdout.size() != 3 )
			action.exit1( _Error.UnableGetMd5Sum1 , "unable to get md5sum of " + filePath , filePath );
		
		return( Common.replace( cmdout.get( 1 ) , " " , "" ) );
	}

	@Override 
	public String cmdGetArchivePartMD5( ActionBase action , String filePath , String archivePartPath , String EXT ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}

	@Override 
	public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( filePath );
		String value = runCommandGetValueCheckDebug( action , "type " + fileWin );
		return( value );
	}

	@Override 
	public String[] cmdGetFileLines( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( filePath );
		return( this.runCommandGetLines( action , "type  " + fileWin , CommandOutput.LOGLEVEL_TRACE ) );
	}
	
	@Override
	public Date cmdGetFileChangeTime( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( filePath );
		String[] lines = this.runCommandGetLines( action , "wmic datafile where name=\"" + Common.replace( fileWin , "\\" , "\\\\" ) + "\" get lastmodified " , CommandOutput.LOGLEVEL_TRACE );
		if( lines.length != 2 || !lines[0].equals( "LastModified" ) )
			return( null );
		
		String value = lines[1].substring( 0 , 14 );
		SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMddhhmmss" );
		Date date = formatter.parse( value );
		return( date );
	}
	
	@Override 
	public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception {
		String executeLog = Common.getWinPath( Common.getPath( executor.rootPath , EXECUTE_LOG ) );
		String ts = Common.getLogTimeStamp();
		runCommand( action , "echo " + Common.getQuoted( ts + ": " + msg ) + " >> " + Common.getQuoted( executeLog ) , CommandOutput.LOGLEVEL_TRACE );
	}

	@Override 
	public void cmdAppendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		String executeLog = Common.getWinPath( Common.getPath( executor.rootPath , Common.getQuoted( UPLOAD_LOG ) ) );
		String ts = Common.getLogTimeStamp();
		String msg = "upload " + dst + " from " + src;
		runCommand( action , "echo " + Common.getQuoted( ts + ": " + msg ) + " >> " + executeLog , CommandOutput.LOGLEVEL_TRACE );
	}

	@Override 
	public String[] cmdGetFolders( ActionBase action , String rootPath ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}

	@Override 
	public String cmdGetFirstFile( ActionBase action , String dir ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override 
	public String[] cmdFindFiles( ActionBase action , String dir , String mask ) throws Exception {
		String filesRegular = getRegularMaskList( action , mask );
		String cmdDir = getDirCmdIfDir( action , dir , 
				"dir /b | findstr /R " + Common.getQuoted( filesRegular ) );
		return( runCommandGetLines( action , cmdDir , CommandOutput.LOGLEVEL_TRACE ) );
	}

	@Override 
	public String cmdGetTarContentMD5( ActionBase action , String filePath ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override 
	public String cmdGetFilesMD5( ActionBase action , String dir , String includeList , String excludeList ) throws Exception {
		String tmpFile = action.getTmpFilePath( "cmdGetFilesMD5" );
		String filesRegular = getRegularMaskList( action , includeList );

		String cmd = "findstr /R " + Common.getQuoted( filesRegular );
		if( !excludeList.isEmpty() ) {
			String excludeRegular = getRegularMaskList( action , excludeList );
			cmd += " ^| findstr /V " + Common.getQuoted( excludeRegular );
		}
		
		String wtmpFile = Common.getWinPath( tmpFile );
		String cmdDir = getDirCmdIfDir( action , dir , 
				"( for /f %x in ('dir /S /b /a-d /ON ^| " + cmd + "') do certutil -hashfile %x MD5 | findstr /V " + 
				Common.getQuoted( "MD5 CertUtil" ) + " ) > " + wtmpFile );
		int timeout = action.setTimeoutUnlimited();
		executor.customCheckErrorsDebug( action , cmdDir );
		action.setTimeout( timeout );
		
		String[] lines = executor.getFileLines( action , tmpFile );
		for( int k = 0; k < lines.length; k++ )
			lines[ k ] = Common.replace( lines[ k ] , " " , "" );
		
		String workFile = action.getTmpFilePath( "cmdGetFilesMD5.md5" );
		Common.createFileFromStringList( action.execrc , workFile , lines );
		ShellExecutor local = action.getLocalShell();
		String value = local.getMD5( action , workFile );
		
		return( value );
	}
	
	@Override 
	public Map<String,List<String>> cmdGetFilesContent( ActionBase action , String dir , String fileMask ) throws Exception {
		String useMarker = "##";
		String cmd = "for %x in (" + fileMask + ") do echo %x " + cmdAnd + " type %x " + cmdAnd + " echo " + useMarker;
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommand( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		
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
			action.exit1( _Error.ErrorReadingFiles1 , "error reading files in dir=" + dir , dir );
		
		return( map );
	}
	
	private void cmdExtractAny( ActionBase action , String tarFile , String targetFolder , String part , String type ) throws Exception {
		String extractPart = ( part == null || part.isEmpty() )? "" : part;
		String targetParent = ( part == null || part.isEmpty() )? targetFolder : Common.getDirName( targetFolder );
		String targetDir = Common.getBaseName( targetFolder );
		
		String wtarFile = Common.getWinPath( tarFile );
		String cmd = "7z x -t" + type + " -y -bd " + wtarFile + " " + extractPart;
		if( !extractPart.isEmpty() )
			if( !extractPart.equals( targetDir ) )
				cmd += " " + cmdAnd + " rmdir /S /Q " + targetDir + " " + cmdAnd + " rename " + extractPart + " " + targetDir;
		String wtargetParent = Common.getWinPath( targetParent );
		
		int timeout = action.setTimeoutUnlimited();
		runCommandCheckStatusDebug( action , wtargetParent , cmd );
		action.setTimeout( timeout );
	}
	
	private void cmdCreateAnyFromDirContent( ActionBase action , String anyFile , String dir , String content , String exclude , String type ) throws Exception {
		String wtarFile = Common.getWinPath( anyFile );
		String contentArgs = "";
		for( String item : Common.split( content , " " ) )
			contentArgs += " -i!" + Common.getWinPath( item );
		
		String excludeArgs = "";
		for( String item : Common.split( exclude , " " ) )
			excludeArgs += " -x!" + Common.getWinPath( item );
		
		int timeout = action.setTimeoutUnlimited();
		runCommandCheckStatusDebug( action , dir , "7z a -t" + type + " -r -bd " + wtarFile + " " + contentArgs + " " + excludeArgs );
		action.setTimeout( timeout );
	}
	
	private String prepareExecuteWindowsFromLinux( ActionBase action , String cmd , int logLevel ) throws Exception {
		if( !running )
			exitError( action , _Error.RunCommandClosedSession1 , "attempt to run command in closed session: " + cmd , new String[] { cmd } );
			
		cmdCurrent = cmd;
		cmdout.clear();
		cmderr.clear();

		return( executor.process.prepareExecuteWindowsFromLinux( action , cmd ) );
	}
	
	private void getOutput( ActionBase action ) throws Exception {
		cmdout.addAll( localSession.cmdout );
		cmderr.addAll( localSession.cmderr );
	}

	private void runCommand( ActionBase action , String cmd , int logLevel , boolean addErrorLevel ) throws Exception {
		if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX ) {
			String execLine = prepareExecuteWindowsFromLinux( action , cmd , logLevel );
			localSession.runCommand( action , execLine , logLevel );
			getOutput( action );
		}
		else {
			cmdCurrent = cmd;

			cmdout.clear();
			cmderr.clear();
			
			try {
				executor.addInput( action , cmd , false );
				
				if( addErrorLevel )
					executor.addInput( action , "echo status=%errorlevel%" , true );
				
				executor.addInput( action , "echo " + ShellOutputWaiter.FINISH_MARKER + " >&2" , true );
				executor.addInput( action , "echo " + ShellOutputWaiter.FINISH_MARKER , true );
			}
			catch( Throwable e ) {
				if( action.context.CTX_TRACEINTERNAL )
					e.printStackTrace();
			}
			
			executor.waitCommandFinished( action , logLevel , cmdout , cmderr , false );
		}
	}

	private String getRegularMaskList( ActionBase action , String maskList ) throws Exception {
		String reg = "";
		for( String mask : Common.splitSpaced( maskList ) ) {
			if( !reg.isEmpty() )
				reg += " ";
			mask = Common.replace( mask , "." , "\\." );
			mask = Common.replace( mask , "*" , ".*" );
			
			reg += "^" + mask;
			if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX )
				reg += "\\$";
			else
				reg += "$";
		}
		return( reg );
	}

	private void checkOut( ActionBase action , int errorCode , String s , String[] params ) throws Exception {
		if( cmdout.isEmpty() )
			return;
		
		if( cmdout.size() == 1 && cmdout.get( 0 ).equals( "File Not Found" ) )
			return;
		
		action.exit( errorCode , s , params );
	}
	
}
