package ru.egov.urm.shell;

import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.meta.Metadata.VarSESSIONTYPE;
import ru.egov.urm.storage.Folder;

public class ShellCoreWindows extends ShellCore {

	ShellCoreUnix localSession;

	String cmdAnd;

	public interface Kernel32 extends W32API {
	    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, DEFAULT_OPTIONS);
	    /* http://msdn.microsoft.com/en-us/library/ms683179(VS.85).aspx */
	    HANDLE GetCurrentProcess();
	    /* http://msdn.microsoft.com/en-us/library/ms683215.aspx */
	    int GetProcessId(HANDLE Process);
	}
	
	public interface W32Errors {
		
	    int NO_ERROR               = 0;
	    int ERROR_INVALID_FUNCTION = 1;
	    int ERROR_FILE_NOT_FOUND   = 2;
	    int ERROR_PATH_NOT_FOUND   = 3;

	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	public interface W32API extends StdCallLibrary, W32Errors {
	    
	    /** Standard options to use the unicode version of a w32 API. */
		Map UNICODE_OPTIONS = new HashMap() {
	        {
	            put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
	            put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
	        }
	    };
	    
	    /** Standard options to use the ASCII/MBCS version of a w32 API. */
	    Map ASCII_OPTIONS = new HashMap() {
	        {
	            put(OPTION_TYPE_MAPPER, W32APITypeMapper.ASCII);
	            put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.ASCII);
	        }
	    };

	    Map DEFAULT_OPTIONS = Boolean.getBoolean("w32.ascii") ? ASCII_OPTIONS : UNICODE_OPTIONS;
	    
	    public class HANDLE extends PointerType {
	    	@Override
	        public Object fromNative(Object nativeValue, FromNativeContext context) {
	            Object o = super.fromNative(nativeValue, context);
	            if (INVALID_HANDLE_VALUE.equals(o))
	                return INVALID_HANDLE_VALUE;
	            return o;
	        }
	    }

	    /** Constant value representing an invalid HANDLE. */
	    HANDLE INVALID_HANDLE_VALUE = new HANDLE() { 
	        { super.setPointer(Pointer.createConstant(-1)); }
	        @Override
	        public void setPointer(Pointer p) { 
	            throw new UnsupportedOperationException("Immutable reference");
	        }
	    };
	}	
	
	public ShellCoreWindows( ShellExecutor executor , VarSESSIONTYPE sessionType , Folder tmpFolder , boolean local ) {
		super( executor , VarOSTYPE.WINDOWS , sessionType , tmpFolder , local );
		cmdAnd = "&&";
	}

	@Override public void createProcess( ActionBase action , ProcessBuilder builder , String rootPath ) throws Exception {
		if( rootPath == null )
			action.exitUnexpectedState();
		
		if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX ) {
			super.setRootPath( rootPath );
			localSession = new ShellCoreUnix( executor , VarSESSIONTYPE.UNIXLOCAL , tmpFolder , true );
			localSession.setWindowsHelper();
			running = true;
			localSession.createProcess( action , builder , action.context.CTX_REDISTPATH );
			initialized = true;
			return;
		}
		
		super.createProcess( action , builder , rootPath );
	}
	
	@Override public void kill( ActionBase action ) throws Exception {
		if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX )
			localSession.kill( action );
		
		super.kill( action );
	}
	
	@Override protected String getExportCmd( ActionBase action ) throws Exception {
		return( "" );
	}

	@Override protected void getProcessAttributes( ActionBase action ) throws Exception {
		super.homePath = action.context.productHome;
		
		try {
			  Field f = super.process.getClass().getDeclaredField( "handle" );
			  f.setAccessible( true );				
			  long handle = f.getLong( super.process );
			    
			  Kernel32 kernel = Kernel32.INSTANCE;
			  W32API.HANDLE winHandle = new W32API.HANDLE();
			  winHandle.setPointer( Pointer.createConstant( handle ) );
			  super.processId = "" + kernel.GetProcessId( winHandle );
		} catch (Throwable e) {				
		}
		
		action.debug( "process started: name=" + super.executor.name + ", id=" + super.processId );
		runCommand( action , "echo off" , true );
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
		cmdWin = Common.replace( cmdWin , "\\\\$" , "\\$" );
		execLine += " " + executor.account.HOSTLOGIN + " " + Common.getQuoted( "cmd /c chcp 65001 & cmd /c \"echo off & " + cmdWin + "\"" );
		action.trace( executor.name + " execute: " + cmd );
		return( execLine );
	}
	
	private void getOutput( ActionBase action ) throws Exception {
		cmdout.addAll( localSession.cmdout );
		cmderr.addAll( localSession.cmderr );
	}

	@Override public void runCommand( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd , debug , false );
	}
	
	private void runCommand( ActionBase action , String cmd , boolean debug , boolean addErrorLevel ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
		
		if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX ) {
			String execLine = prepareExecute( action , cmd , debug );
			localSession.runCommand( action , execLine , debug );
			getOutput( action );
		}
		else {
			cmdCurrent = cmd;

			cmdout.clear();
			cmderr.clear();
			
			action.trace( executor.name + " execute: " + cmd );
			String execLine = cmd + "\r\n";
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( execLine );
			writer.write( execLine );
			
			if( addErrorLevel ) {
				execLine = "echo status=%errorlevel%";
				action.trace( execLine );
				writer.write( execLine + "\r\n" );
			}
			
			execLine = "echo " + finishMarker + " >&2\r\n";
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( execLine );
			writer.write( execLine );
			
			execLine = "echo " + finishMarker + "\r\n";
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( execLine );
			writer.write( execLine );
			
			try {
				writer.flush();
			}
			catch( Throwable e ) {
				if( action.context.CTX_TRACEINTERNAL )
					e.printStackTrace();
			}
			
			ShellWaiter waiter = new ShellWaiter( executor , new CommandReaderWindows( debug ) );
			boolean res = waiter.wait( action , action.commandTimeout );
			
			if( !res )
				exitError( action , "command has been killed" );
		}
	}

	@Override public int runCommandGetStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		if( sessionType == VarSESSIONTYPE.WINDOWSFROMUNIX ) {
			String execLine = prepareExecute( action , cmd , debug );
			int status = localSession.runCommandGetStatus( action , execLine , debug );
			getOutput( action );
			return( status );
		}
		else {
			runCommand( action , cmd , debug , true );
			if( cmdout.size() > 0 ) {
				String last = cmdout.get( cmdout.size() - 1 );
				if( last.startsWith( "status=" ) ) {
					int status = Integer.parseInt( Common.getPartAfterFirst( last , "status=" ) );
					return( status );
				}
			}
		}
		
		action.exit( "error executing cmd=(" + cmd + ")" );
		return( 0 );
	}

	@Override public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		String dirWin = Common.getWinPath( dir );
		String rootPathWin = Common.getWinPath( rootPath );
		return( "if exist " + dirWin + " ( cd " + dirWin + " " + cmdAnd + " ( " + cmd + " ) " + cmdAnd + " " + "cd " + rootPathWin + " ) else echo invalid directory: " + dirWin );
	}
	
	@Override public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		String dirWin = Common.getWinPath( dir );
		String rootPathWin = Common.getWinPath( rootPath );
		return( "if exist " + dirWin + " ( cd " + dirWin + " " + cmdAnd + " ( " + cmd + " ) " + cmdAnd + " " + "cd " + rootPathWin + " )" );
	}

	@Override protected void killProcess( ActionBase action ) throws Exception {
		executor.pool.master.custom( action , "taskkill /pid " + processId + " /f" , true );	
	}

	@Override public void cmdEnsureDirExists( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "if not exist " + wdir + " md " + wdir , true );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "check/create directory error" );
	}

	@Override public void cmdCreateFileFromString( ActionBase action , String path , String value ) throws Exception {
		String pathWin = Common.getWinPath( path );
		runCommand( action , "echo " + value + " > " + pathWin , true );
	}

	@Override public void cmdAppendFileWithString( ActionBase action , String path , String value ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdAppendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public boolean cmdCheckDirExists( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
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
		String wpath = Common.getWinPath( path );
		String value = this.runCommandGetValueCheckDebug( action , "if exist " + wpath + "\\ ( echo dir ) else if exist " + 
				wpath + " echo file" );
		if( value.equals( "file" ) )
			return( true );
		return( false );
	}

	@Override public boolean cmdCheckPathExists( ActionBase action , String path ) throws Exception {
		String wpath = Common.getWinPath( path );
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
		String wdir = Common.getWinPath( dir );
		runCommand( action , "if exist " + wdir + " ( rmdir /S /Q " + wdir + " " + cmdAnd + " md " + wdir + " )" , true );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "remove directory content error" );
	}
	
	@Override public void cmdRemoveDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "if exist " + wdir + " rmdir /S /Q " + wdir , true );
		if( cmdout.isEmpty() == false || cmderr.isEmpty() == false )
			action.exit( "remove directory error" );
	}
	
	@Override public void cmdRecreateDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "( if exist " + wdir + " rmdir /S /Q " + wdir + " ) " + cmdAnd + " md " + wdir , true );
	}

	@Override public void cmdCreatePublicDir( ActionBase action , String dir ) throws Exception {
		String wdir = Common.getWinPath( dir );
		runCommand( action , "md " + wdir , true );
	}

	@Override public String[] cmdGrepFile( ActionBase action , String filePath , String mask ) throws Exception {
		String wpath = Common.getWinPath( filePath );
		return( runCommandGetLines( action , "type " + wpath + " | findstr /C:" + Common.getQuoted( mask ) , true ) );
	}

	@Override public void cmdReplaceFileLine( ActionBase action , String filePath , String mask , String newLine ) throws Exception {
		String filePathWin = Common.getWinPath( filePath );
		String filePathTmp = filePathWin + ".new";
		String cmd = "findstr /V " + mask + " " + filePathWin + " > " + filePathTmp + 
				" " + cmdAnd + " del /Q " + filePathWin + 
				" " + cmdAnd + " rename " + filePathTmp + " " + Common.getBaseName( filePath );
		
		if( !newLine.isEmpty() )
			cmd += " " + cmdAnd + " echo " + newLine + " >> " + filePathWin;
		runCommandCheckDebug( action , cmd );
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

	private void checkOut( ActionBase action , String s ) throws Exception {
		if( cmdout.isEmpty() )
			return;
		
		if( cmdout.size() == 1 && cmdout.get( 0 ).equals( "File Not Found" ) )
			return;
		
		action.exit( "errors on delete dirs" );
	}
	
	@Override public void cmdRemoveFiles( ActionBase action , String dir , String files ) throws Exception {
		String filesRegular = getRegularMaskList( action , files );
		String cmdDir = getDirCmdIfDir( action , dir , 
				"for /f %x in ('dir /b /ad ^| findstr /R " + 
				Common.getQuoted( filesRegular ) + "') do @rmdir /Q /S %x" );
		runCommand( action , cmdDir , true );
		checkOut( action , "errors on delete dirs" );
						
		cmdDir = getDirCmdIfDir( action , dir , 
				"for /f %x in ('dir /b /a-d ^| findstr /R " + 
				Common.getQuoted( filesRegular ) + "') do @del /Q %x" );
		runCommand( action , cmdDir , true );
		checkOut( action , "errors on delete files" );
	}

	@Override public void cmdRemoveFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
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
		runCommand( action , cmdDir , true );
		checkOut( action , "errors on delete dirs" );
		
		cmdDir = getDirCmdIfDir( action , dir , 
				"for /f %x in ('dir /b /a-d ^| findstr /R " + 
				Common.getQuoted( filesRegular ) + " ^| findstr /V " +
				Common.getQuoted( excludeRegular ) + "') do del /Q %x" );
		runCommand( action , cmdDir , true );
		checkOut( action , "errors on delete files" );
	}

	@Override public void cmdUnzipPart( ActionBase action , String unzipDir , String zipFile , String targetFolder , String part ) throws Exception {
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

	@Override public void cmdMove( ActionBase action , String source , String target ) throws Exception {
		String wsource = Common.getWinPath( source );
		String wtarget = Common.getWinPath( target );
		runCommandCheckStatus( action , "move /Y " + wsource + " " + wtarget , true );
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
	
	@Override public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		cmdExtractAny( action , tarFile , targetFolder , part , "tar" );
	}
	
	@Override public void cmdExtractTar( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		cmdExtractAny( action , tarFile , targetFolder , part , "tar" );
	}
	
	@Override public String cmdLs( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( "" );
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
	
	@Override public void cmdCreateZipFromDirContent( ActionBase action , String zipFile , String dir , String content , String exclude ) throws Exception {
		cmdCreateAnyFromDirContent( action , zipFile , dir , content , exclude , "zip" );
	}
	
	@Override public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCreateTarFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		cmdCreateAnyFromDirContent( action , tarFile , dir , content , exclude , "tar" );
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
		action.debug( "copy " + files + " from " + dirFrom + " to " + dirTo + " ..." );
		String wfilesFrom = Common.getWinPath( files );
		String wdirTo = Common.getWinPath( dirTo ) + "\\";
		runCommandCheckStatus( action , dirFrom , "for %x in ( " + wfilesFrom + " ) do xcopy /Y /Q \"%~x\" " + wdirTo , true );
	}

	@Override public void cmdCopyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception {
		action.debug( "copy " + fileFrom + " to " + fileTo + " ..." );
		String wfileFrom = Common.getWinPath( fileFrom );
		String wfileTo = Common.getWinPath( fileTo );
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
		action.debug( "copy content from " + srcDir + " to " + dstDir + " ..." );
		String wdirFrom = Common.getWinPath( srcDir );
		String wdirTo = Common.getWinPath( dstDir );
		runCommandCheckDebug( action , "xcopy /Q /Y /E " + wdirFrom + "\\* " + wdirTo + "\\" );
	}
	
	@Override public void cmdCopyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception {
		action.debug( "copy dir " + dirFrom + " to " + dirTo + " ..." );
		String wdirFrom = Common.getWinPath( dirFrom );
		String wdirTo = Common.getWinPath( dirTo );
		cmdRemoveDir( action , dirTo );
		cmdEnsureDirExists( action , dirTo );
		runCommandCheckDebug( action , "xcopy /Q /Y /E " + wdirFrom + "\\* " + wdirTo + "\\" );
	}
	
	@Override public void cmdCopyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception {
		String baseName = Common.getBaseName( dirFrom );
		String dirTo = baseDstDir + "/" + baseName;
		cmdRemoveDir( action , dirTo );
		cmdEnsureDirExists( action , dirTo );
		
		action.debug( "copy " + dirFrom + " to " + dirTo + " ..." );
		String wdirFrom = Common.getWinPath( dirFrom );
		String wdirTo = Common.getWinPath( dirTo );
		runCommandCheckDebug( action , "xcopy /Q /Y /E " + wdirFrom + " " + wdirTo  );
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
				"chdir " + cmdAnd + " dir /ad /s /b " + cmdAnd + " echo " + delimiter + " " + cmdAnd + " dir /a-d /b /s 2>nul" );
		
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
		String cmd = "dir /ad /b " + cmdAnd + " echo " + delimiter + " " + cmdAnd + " dir /a-d /b 2>nul"; 
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
		String fileWin = Common.getWinPath( filePath );
		runCommand( action , "certutil -hashfile " + fileWin + " MD5" , true );
		if( cmdout.size() != 3 )
			action.exit( "unable to get md5sum of " + filePath );
		
		return( Common.replace( cmdout.get( 1 ) , " " , "" ) );
	}

	@Override public String cmdGetArchivePartMD5( ActionBase action , String filePath , String archivePartPath , String EXT ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}

	@Override public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( filePath );
		String value = runCommandGetValueCheckDebug( action , "type " + fileWin );
		return( value );
	}

	@Override public String[] cmdGetFileLines( ActionBase action , String filePath ) throws Exception {
		String fileWin = Common.getWinPath( filePath );
		return( this.runCommandGetLines( action , "type  " + fileWin , true ) );
	}
	
	@Override public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception {
		String executeLog = Common.getWinPath( Common.getPath( executor.rootPath , EXECUTE_LOG ) );
		String ts = Common.getLogTimeStamp();
		runCommand( action , "echo " + Common.getQuoted( ts + ": " + msg ) + " >> " + executeLog , true );
	}

	@Override public void cmdAppendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		String executeLog = Common.getWinPath( Common.getPath( executor.rootPath , UPLOAD_LOG ) );
		String ts = Common.getLogTimeStamp();
		String msg = "upload " + dst + " from " + src;
		runCommand( action , "echo " + Common.getQuoted( ts + ": " + msg ) + " >> " + executeLog , true );
	}

	@Override public String[] cmdGetFolders( ActionBase action , String rootPath ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}

	@Override public String cmdGetFirstFile( ActionBase action , String dir ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public String[] cmdFindFiles( ActionBase action , String dir , String mask ) throws Exception {
		String filesRegular = getRegularMaskList( action , mask );
		String cmdDir = getDirCmdIfDir( action , dir , 
				"dir /b | findstr /R " + Common.getQuoted( filesRegular ) );
		return( runCommandGetLines( action , cmdDir , true ) );
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
		String cmd = "for %x in (" + fileMask + ") do echo %x " + cmdAnd + " type %x " + cmdAnd + " echo " + useMarker;
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
	
	/*##################################################*/
	/*##################################################*/
	
	class CommandReaderWindows extends WaiterCommand {
		boolean debug;
		
		public CommandReaderWindows( boolean debug ) {
			this.debug = debug;
		}
		
		public void run( ActionBase action ) throws Exception {
			readStreamToMarker( action , reader , cmdout , "" );
			readStreamToMarker( action , errreader , cmderr , "stderr:" );
		}
		
		private void outStreamLine( ActionBase action , String line , List<String> text ) throws Exception {
			text.add( line );
			if( debug )
				action.trace( line );
			else
				action.log( line );
		}

		private void readStreamToMarker( ActionBase action , BufferedReader textreader , List<String> text , String prompt ) throws Exception {
			String line;
			boolean first = true;
			
			String buffer = "";
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "readStreamToMarker - start reading ..." );
			
			while ( true ) {
				int index = buffer.indexOf( '\n' );
				if( index < 0 ) {
					String newBuffer = readBuffer( action , textreader , buffer , '\n' );
					if( newBuffer != null )
						buffer = newBuffer;
					continue;
				}
				
				line = buffer.substring( 0 , index );
				buffer = buffer.substring( index + 1 );
				
				if( action.context.CTX_TRACEINTERNAL )
					action.trace( "readStreamToMarker - line=" + line.replaceAll("\\p{C}", "?") );
				
				index = line.indexOf( finishMarker );
				if( index >= 0 ) {
					line = line.substring( 0 , index );
					if( index > 0 ) {
						if( first && !prompt.isEmpty() ) {
							outStreamLine( action , prompt , text );
							first = false;
						}
						outStreamLine( action , line , text );
					}
				}
				else {
					if( first && !prompt.isEmpty() ) {
						outStreamLine( action , prompt , text );
						first = false;
					}
					outStreamLine( action , line , text );
				}
				
				if( index >= 0 )
					break;
			}
		}
	}
	
}
