package org.urm.server.shell;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.action.ActionBase;
import org.urm.server.action.CommandOutput;
import org.urm.server.meta.Metadata.VarSESSIONTYPE;
import org.urm.server.storage.Folder;

public class ShellCoreUnix extends ShellCore {

	boolean windowsHelper = false;
	
	public ShellCoreUnix( ShellExecutor executor , VarSESSIONTYPE sessionType , Folder tmpFolder , boolean local ) {
		super( executor , VarOSTYPE.LINUX , sessionType , tmpFolder , local );
	}

	public void setWindowsHelper() {
		windowsHelper = true;
	}

	public void setProcessName( String name ) {
		ShellCoreJNI osapi = getOSAPI();
		osapi.setProcessName( name );
	}
	
	@Override protected String getExportCmd( ActionBase action ) throws Exception {
		if( action.meta == null || action.meta.product == null )
			return( "" );
		
		Map<String,String> exports = action.meta.product.getExportProperties( action );
		String cmd = "";
		for( String key : exports.keySet() ) {
			if( !cmd.isEmpty() )
				cmd += "; ";
			cmd += "export " + key + "=" + exports.get( key );
		}
		return( cmd );
	}

	@Override protected void getProcessAttributes( ActionBase action ) throws Exception {
		this.runCommand( action , "echo check and skip banner ... " , CommandOutput.LOGLEVEL_TRACE );
		processId = runCommandGetValueCheckDebug( action , "echo $$" );
		homePath = runCommandGetValueCheckDebug( action , "echo $HOME" );
	}
	
	@Override public void runCommand( ActionBase action , String cmd , int logLevel ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
			
		cmdCurrent = cmd;

		cmdout.clear();
		cmderr.clear();
		
		String execLine = cmd + "; echo " + finishMarker + " >&2; echo " + finishMarker + "\n";
		action.trace( executor.name + " execute: " + cmd );
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "write cmd line=" + execLine );

		try {
			addInput( action , execLine , windowsHelper );
		}
		catch( Throwable e ) {
			if( action.context.CTX_TRACEINTERNAL )
				e.printStackTrace();
		}
		
		ShellWaiter waiter = new ShellWaiter( executor , new CommandReaderUnix( logLevel ) );
		if( windowsHelper )
			waiter.setWindowsHelper();
		boolean res = waiter.wait( action , action.commandTimeout );
		
		if( !res )
			exitError( action , "command has been killed" );
	}

	@Override public int runCommandGetStatus( ActionBase action , String cmd , int logLevel ) throws Exception {
		runCommand( action , cmd + "; echo COMMAND_STATUS=$?" , logLevel );
		
		if( cmdout.size() > 0 ) {
			String last = cmdout.get( cmdout.size() - 1 );
			if( last.startsWith( "COMMAND_STATUS=" ) ) {
				String ss = last.substring( "COMMAND_STATUS=".length() );
				int value = Integer.parseInt( ss );
				cmdout.remove( cmdout.size() - 1 );
				return( value );
			}
		}
				
		exitError( action , "unable to obtain command status" );
		return( -1 );
	}

	@Override public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		return( "( if [ -d " + Common.getQuoted( dir ) + " ]; then cd " + dir + "; " + cmd + "; else echo invalid directory: " + dir + " >&2; fi )" );
	}
	
	@Override public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		return( "( if [ -d " + Common.getQuoted( dir ) + " ]; then cd " + dir + "; " + cmd + "; fi )" );
	}

	@Override protected void killProcess( ActionBase action ) throws Exception {
		executor.pool.master.custom( action , "pkill -9 -P " + processId , CommandOutput.LOGLEVEL_TRACE );
	}

	@Override public void cmdEnsureDirExists( ActionBase action , String dir ) throws Exception {
		runCommandCheckDebug( action , "mkdir -p " + dir );
	}

	@Override public void cmdCreateFileFromString( ActionBase action , String path , String value ) throws Exception {
		if( value.isEmpty() )
			runCommandCheckDebug( action , "touch " + path );
		else
			runCommandCheckDebug( action , "echo " + Common.getQuoted( value ) + " > " + path );
	}

	@Override public void cmdAppendFileWithString( ActionBase action , String path , String value ) throws Exception {
		runCommandCheckDebug( action , "echo " + value + " >> " + path );
	}

	@Override public void cmdAppendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception {
		runCommandCheckDebug( action , "cat " + pathSrc + " >> " + pathDst );
	}
	
	@Override public boolean cmdCheckDirExists( ActionBase action , String path ) throws Exception {
		String ok = runCommandGetValueCheckDebug( action , "if [ -d " + path + " ]; then echo ok; fi" );
		return( ok.equals( "ok" ) );
	}

	@Override public boolean cmdIsFileEmpty( ActionBase action , String path ) throws Exception {
		String ok = runCommandGetValueCheckDebug( action , "if [ -f " + path + " ]; then wc -w " + path + "; fi" );
		return( ok.startsWith( "0" ) );
	}

	@Override public boolean cmdCheckFileExists( ActionBase action , String path ) throws Exception {
		String ok = runCommandGetValueCheckDebug( action , "if [ -f " + path + " ]; then echo ok; fi" );
		return( ok.equals( "ok" ) );
	}

	@Override public boolean cmdCheckPathExists( ActionBase action , String path ) throws Exception {
		String ok = runCommandGetValueCheckDebug( action , "if [ -f " + path + " ] || [ -d " + path + " ]; then echo ok; fi" );
		return( ok.equals( "ok" ) );
	}

	@Override public String cmdFindOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , path , "find . -maxdepth 1 -name " + Common.getQuoted( mask ) + 
				" | egrep " + Common.getQuoted( grepMask ) + " | tr '\\n' ' '" );
		value = value.trim();
		
		if( value.isEmpty() )
			return( "" );
		
		if( value.indexOf( ' ' ) > 0 )
			action.exit( "too many files found in path=" + path + ", mask=" + Common.getQuoted( mask ) + " (" + value + ")" );
		
		value = Common.getPartAfterFirst( value , "./" );
		return( value );
	}
	
	@Override public String[] cmdGrepFile( ActionBase action , String filePath , String mask ) throws Exception {
		return( runCommandGetLines( action , "grep " + mask + " " + filePath , CommandOutput.LOGLEVEL_TRACE ) );
	}
	
	@Override public void cmdReplaceFileLine( ActionBase action , String filePath , String mask , String newLine ) throws Exception {
		String filePathTmp = filePath + ".new";
		String cmd = "grep -v " + mask + " " + filePath + " > " + filePathTmp + 
				"; mv " + filePathTmp + " " + filePath;
		if( !newLine.isEmpty() )
			cmd += "; echo " + Common.getQuoted( newLine ) + " >> " + filePath;
		runCommandCheckDebug( action , cmd );
	}
	
	@Override public String cmdFindOneTop( ActionBase action , String path , String mask ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , path , "find . -maxdepth 1 -name " + Common.getQuoted( mask ) + " | tr '\\n' ' '" );
		value = value.trim();
		
		if( value.isEmpty() )
			return( "" );
		
		if( value.indexOf( ' ' ) > 0 )
			action.exit( "too many files found in path=" + path + ", mask=" + Common.getQuoted( mask ) + " (" + value + ")" );
		
		value = Common.getPartAfterFirst( value , "./" );
		return( value );
	}

	@Override public void cmdCreateMD5( ActionBase action , String filepath ) throws Exception {
		runCommandCheckDebug( action , "md5sum " + filepath + " | cut -d " + Common.getQuoted( " " ) + " -f1 > " + filepath + ".md5" );
	}

	@Override public void cmdRemoveDirContent( ActionBase action , String dirpath ) throws Exception {
		runCommandCheckDebug( action , "rm -rf " + dirpath + "/*" );
	}
	
	@Override public void cmdRemoveDir( ActionBase action , String dirpath ) throws Exception {
		runCommandCheckDebug( action , "rm -rf " + dirpath );
	}
	
	@Override public void cmdRecreateDir( ActionBase action , String dirpath ) throws Exception {
		runCommandCheckDebug( action , "rm -rf " + dirpath + "; mkdir -p " + dirpath );
	}

	@Override public void cmdRemoveFiles( ActionBase action , String dir , String files ) throws Exception {
		runCommandCheckDebugIfDir( action , dir , "rm -rf " + files );
	}

	private String getFindCommandIncludeExclude( String files , String exclude , boolean filesOnly ) throws Exception {
		String includeOptions = "";
		for( String s : Common.splitSpaced( files ) ) {
			if( !includeOptions.isEmpty() )
				includeOptions += " -o";
			if( filesOnly )
				includeOptions += " -wholename " + Common.getQuoted( "./" + s + "/*" ) + " -o -wholename " + Common.getQuoted( "./" + s );
			else
				includeOptions += " -name " + Common.getQuoted( s );
		}
		String excludeOptions = "";
		if( !exclude.isEmpty() ) {
			for( String s : Common.splitSpaced( exclude ) ) {
				if( filesOnly )
					excludeOptions += " ! -wholename " + Common.getQuoted( "./" + s + "/*" ) + " ! -wholename " + Common.getQuoted( "./" + s );
				else
					excludeOptions += " ! -name " + Common.getQuoted( s );
			}
		}
		
		String filesOption = "-follow";
		if( filesOnly )
			filesOption += " -type f";
		else
			filesOption += " -maxdepth 1";
			
		String find = "find . " + filesOption + " \\( " + includeOptions + " \\) ! -name \".\" " + excludeOptions;
		return( find );
	}
	
	@Override public void cmdRemoveFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		String find = getFindCommandIncludeExclude( files , exclude , false );
		runCommandCheckDebugIfDir( action , dir , find + " -exec rm -rf {} \\;" );
	}

	@Override public void cmdUnzipPart( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception {
		String dirOption = "";
		String filesDir = unzipDir;
		if( !targetDir.isEmpty() ) {
			dirOption = " -d " + targetDir;
			filesDir = targetDir;
		}
		
		if( zipPart.isEmpty() || zipPart.equals( "*" ) ) {
			cmdRemoveFiles( action , filesDir , "*" );
			runCommandCheckDebug( action , unzipDir , "unzip" + dirOption + " " + zipFile + " > /dev/null" );
		}
		else { 
			cmdRemoveFiles( action , filesDir , zipPart );
			runCommandCheckDebug( action , unzipDir , "unzip" + dirOption + " " + zipFile + " " + zipPart + " > /dev/null" );
		}
	}

	@Override public void cmdMove( ActionBase action , String source , String target ) throws Exception {
		runCommandCheckDebug( action , "mv " + source + " " + target );
	}

	@Override public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		String extractPart = ( part == null || part.isEmpty() )? "" : part;
		String targetParent = ( part == null || part.isEmpty() )? targetFolder : Common.getDirName( targetFolder );
		String targetDir = Common.getBaseName( targetFolder );
		
		String cmd = "tar --no-same-owner --overwrite -zxmf " + tarFile + " " + extractPart + " > /dev/null";
		if( !extractPart.isEmpty() )
			if( !extractPart.equals( targetDir ) )
				cmd += "; rm -rf " + targetDir + "; mv " + extractPart + " " + targetDir;
		runCommandCheckDebug( action , targetParent , cmd ); 
	}
	
	@Override public void cmdExtractTar( ActionBase action , String tarFile , String targetFolder , String part ) throws Exception {
		String extractPart = ( part == null || part.isEmpty() )? "" : part;
		String targetParent = ( part == null || part.isEmpty() )? targetFolder : Common.getDirName( targetFolder );
		String targetDir = Common.getBaseName( targetFolder );
		
		String cmd = "tar --no-same-owner --overwrite -xmf " + tarFile + " " + extractPart + " > /dev/null";
		if( !extractPart.isEmpty() )
			if( !extractPart.equals( targetDir ) )
				cmd += "; rm -rf " + targetDir + "; mv " + extractPart + " " + targetDir;
		runCommandCheckDebug( action , targetParent , cmd ); 
	}
	
	@Override public String cmdLs( ActionBase action , String path ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , path , "ls" );
		return( value );
	}

	@Override public void cmdCreateZipFromDirContent( ActionBase action , String zipFile , String dir , String content , String exclude ) throws Exception {
		String excludeOptions = "";
		if( !exclude.isEmpty() ) {
			for( String s : Common.splitSpaced( exclude ) )
				excludeOptions = " -x " + Common.getQuoted( s ) + " -x " + Common.getQuoted( s + "/*" );
		}
		runCommandCheckDebug( action , dir , "zip " + zipFile + " " + content + excludeOptions + " > /dev/null 2> /dev/null" );
	}
	
	@Override public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		String find = this.getFindCommandIncludeExclude( content , exclude , true );
		String listFile = tmpFolder.getFilePath( action , "fileList.txt" );
		runCommandCheckDebug( action , dir , find + " > " + listFile + "; tar -zcf " + tarFile + " --files-from=" + listFile + " > /dev/null 2> /dev/null" );
	}

	@Override public void cmdCreateTarFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		String find = this.getFindCommandIncludeExclude( content , exclude , true );
		String listFile = tmpFolder.getFilePath( action , "fileList.txt" );
		runCommandCheckDebug( action , dir , find + " > " + listFile + "; tar -cf " + tarFile + " --files-from=" + listFile + " > /dev/null 2> /dev/null" );
	}

	@Override public String cmdGetFileInfo( ActionBase action , String dir , String dirFile ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , dir , "ls -l " + dirFile );
		
		if( value.isEmpty() )
			action.exit( "cannot find file=" + dirFile + " in directory " + dir );
		
		return( value );
	}

	@Override public void cmdCreateJarFromFolder( ActionBase action , String runDir , String jarFile , String folder ) throws Exception {
		runCommandCheckDebug( action , runDir , "jar cfvM " + jarFile + " -C " + folder + "/ . > /dev/null" );
	}
	
	@Override public void cmdSetShellVariable( ActionBase action , String var , String value ) throws Exception {
		runCommandCheckDebug( action , "export " + var + "=" + value );
	}

	@Override public void cmdGitAddPomFiles( ActionBase action , String runDir ) throws Exception {
		runCommandCheckDebug( action , runDir ,  
				"for pom in `find . -name " + Common.getQuoted( "pom.xml" ) + "`; do\n" +
				"	git add $pom\n" +
				"done" );
	}

	@Override public void cmdCd( ActionBase action , String dir ) throws Exception {
		runCommandCheckDebug( action , "cd " + dir );
	}
	
	@Override public void cmdCopyFiles( ActionBase action , String dirFrom , String files , String dirTo ) throws Exception {
		action.debug( "copy files (" + files + ") from " + dirFrom + " to " + dirTo + " ..." );
		runCommandCheckDebug( action , dirFrom , "cp -p -t " + dirTo + " " + files );
	}

	@Override public void cmdCopyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception {
		action.debug( "copy " + fileFrom + " to " + fileTo + " ..." );
		runCommandCheckDebug( action , "cp -p " + fileFrom + " " + fileTo );
	}
	
	@Override public void cmdCopyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception {
		String finalDir = Common.getPath( targetDir , FOLDER );
		String baseName = Common.getBaseName( fileFrom );
		String finalFile;
		if( !finalName.isEmpty() )
			finalFile = finalDir + "/" + finalName;
		else
			finalFile = finalDir + "/" + baseName;
			
		action.debug( "copy " + fileFrom + " to " + finalFile + " ..." );
		runCommandCheckDebug( action , "cp -p " + fileFrom + " " + finalFile );
	}

	@Override public void cmdCopyDirContent( ActionBase action , String srcDir , String dstDir ) throws Exception {
		action.debug( "copy content from " + srcDir + " to " + dstDir + " ..." );
		runCommandCheckDebug( action , "if [ \"`ls -A " + srcDir + "`\" != \"\" ]; then cp -R -p " + srcDir + "/* " + dstDir + "/; fi" );
	}
	
	@Override public void cmdCopyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception {
		action.debug( "copy dir " + dirFrom + " to " + dirTo + " ..." );
		runCommandCheckDebug( action , "mkdir -p `dirname " + dirTo + "`; rm -rf " + dirTo + "; cp -R -p " + dirFrom + " " + dirTo );
	}
	
	@Override public void cmdCopyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception {
		String baseName = Common.getBaseName( dirFrom );
		cmdRemoveDir( action , baseDstDir + "/" + baseName );
		cmdEnsureDirExists( action , baseDstDir );
		
		action.debug( "copy " + dirFrom + " to " + baseDstDir + " ..." );
		runCommandCheckDebug( action , "cp -R -p " + dirFrom + " " + baseDstDir + "/" );
	}
	
	@Override public void cmdScpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		runCommandCheckDebug( action , "scp -q -B " + keyOption + account.HOSTLOGIN + ":" + srcPath + " " + dstPath );
	}

	@Override public void cmdScpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		int timeout = action.setTimeoutUnlimited();
		runCommandCheckDebug( action , "scp -q -B " + keyOption + account.HOSTLOGIN + ":" + srcPath + "/* " + dstPath );
		action.setTimeout( timeout );
	}

	@Override public void cmdScpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		int timeout = action.setTimeoutUnlimited();
		String preserveOption = "-p ";
		if( account.isWindows() )
			preserveOption = "";
		
		runCommandCheckDebug( action , "scp -q -B " + preserveOption + keyOption + srcPath + " " + account.HOSTLOGIN + ":" + dstPath );
		action.setTimeout( timeout );
	}

	@Override public void cmdScpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		String baseName = Common.getBaseName( srcDirPath );
		ShellExecutor session = action.getShell( account );
		session.removeDir( action , baseDstDir + "/" + baseName );
		session.ensureDirExists( action , baseDstDir );
		
		int timeout = action.setTimeoutUnlimited();
		String preserveOption = "-p ";
		if( account.isWindows() )
			preserveOption = "";
		
		runCommandCheckDebug( action , "scp -r -q -B " + preserveOption + keyOption + srcDirPath + " " + account.HOSTLOGIN + ":" + baseDstDir );
		action.setTimeout( timeout );
	}

	@Override public void cmdScpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		ShellExecutor session = action.getShell( account );
		session.ensureDirExists( action , dstDir );
		
		int timeout = action.setTimeoutUnlimited();
		String preserveOption = "-p ";
		if( account.isWindows() )
			preserveOption = "";
		
		runCommandCheckDebug( action , "scp -r -q -B " + preserveOption + keyOption + srcDirPath + "/* " + account.HOSTLOGIN + ":" + dstDir );
		action.setTimeout( timeout );
	}

	@Override public void cmdScpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		int timeout = action.setTimeoutUnlimited();
		runCommandCheckDebug( action , "scp -r -q -B " + keyOption + account.HOSTLOGIN + ":" + srcPath + " " + dstPath );
		action.setTimeout( timeout );
	}

	@Override public void cmdCopyDirFileToFile( ActionBase action , Account account , String dirPath , String fileSrc , String fileDst ) throws Exception {
		ShellExecutor session = action.getShell( account );
		session.custom( action , dirPath , "cp " + fileSrc + " " + fileDst );
	}

	@Override public void cmdGetDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		String delimiter = "URM_DELIMITER";
		List<String> res = runCommandCheckGetOutputDebug( action , rootPath , 
				"find . -type d | sort; echo " + delimiter + "; find . -type f | sort" );
		
		if( res.isEmpty() )
			action.exit( "directory " + rootPath + " does not exist" );
		
		List<String> copyTo = dirs;
		boolean ok = false;
		for( int k = 0; k < res.size(); k++ ) {
			String s = res.get( k );
			if( s.equals( "." ) )
				continue;
			
			if( s.equals( delimiter ) ) {
				copyTo = files;
				ok = true;
				continue;
			}
			
			if( s.startsWith( "./") )
				s = s.substring( 2 );
			
			copyTo.add( s );
		}
		
		if( !ok )
			action.exit( "unable to read directory " + rootPath );
	}

	@Override public void cmdGetTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		String delimiter = "URM_DELIMITER";
		List<String> res = runCommandCheckGetOutputDebug( action , rootPath , 
				"find . -maxdepth 1 -type d | sort; echo " + delimiter + "; find . -maxdepth 1 -type f | sort" );
		
		if( res.isEmpty() )
			action.exit( "directory " + rootPath + " does not exist" );
		
		List<String> copyTo = dirs;
		boolean ok = false;
		for( int k = 0; k < res.size(); k++ ) {
			String s = res.get( k );
			if( s.equals( "." ) )
				continue;
			
			if( s.equals( delimiter ) ) {
				copyTo = files;
				ok = true;
				continue;
			}
			
			if( s.startsWith( "./") )
				s = s.substring( 2 );
			
			copyTo.add( s );
		}
		
		if( !ok )
			action.exit( "unable to read directory " + rootPath );
	}

	@Override public String cmdGetMD5( ActionBase action , String filePath ) throws Exception {
		String fileCheck = filePath;
		if( fileCheck.contains( " " ) )
			fileCheck = Common.getQuoted( fileCheck );
		String value = runCommandGetValueCheckDebug( action , "md5sum " + fileCheck + " | cut -d " + Common.getQuoted( " " ) + " -f1" );
		return( value );
	}

	@Override public String cmdGetArchivePartMD5( ActionBase action , String filePath , String archivePartPath , String EXT ) throws Exception {
		String extractCmd = "";
		if( EXT.equals( ".zip" ) ) {
			extractCmd = "unzip -p " + filePath + " " + archivePartPath;
			extractCmd += " | md5sum | cut -d " + Common.getQuoted( " " ) + " -f1";
			return( this.runCommandCheckDebug( action , extractCmd ) );
		}
		
			// extract
		Folder tmp = action.getTmpFolder( "cmdGetArchivePartMD5" );
		tmp.ensureExists( action );
		
		if( EXT.equals( ".tar" ) )
			tmp.extractTarPart( action , filePath , archivePartPath );
		else
		if( EXT.equals( ".tgz" ) || EXT.equals( ".tar.gz" ) )
			tmp.extractTarGzPart( action , filePath , archivePartPath );
		else
			action.exitUnexpectedState();

		// ordered cat and md5sum
		tmp.createFileFromString( action , "placeholder" , "" );
		extractCmd = "cat `find . -type f | sort`"; 
		extractCmd += " | md5sum | cut -d " + Common.getQuoted( " " ) + " -f1";
		
		String value = runCommandCheckDebug( action , tmp.folderPath , extractCmd );
		if( !action.isDebug() )
			tmp.removeThis( action );
		
		return( value );
	}
	
	@Override public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , "cat " + filePath );
		return( value );
	}

	@Override public String[] cmdGetFileLines( ActionBase action , String filePath ) throws Exception {
		return( this.runCommandGetLines( action , "cat " + filePath , CommandOutput.LOGLEVEL_TRACE ) );
	}
	
	@Override public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception {
		cmdAppendFileWithString( action , "~/" + EXECUTE_LOG , Common.getQuoted( "`date` (SSH_CLIENT=$SSH_CLIENT): " + msg ) ); 
	}

	@Override public void cmdAppendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		String msg = "upload " + dst + " from " + src;
		cmdAppendFileWithString( action , "~/" + UPLOAD_LOG , Common.getQuoted( "`date` (SSH_CLIENT=$SSH_CLIENT): " + msg ) ); 
	}

	@Override public void cmdCreatePublicDir( ActionBase action , String dir ) throws Exception {
		runCommandCheckDebug( action , "mkdir -p " + dir + "; chmod 777 " + dir );
	}
	
	@Override public String[] cmdGetFolders( ActionBase action , String rootPath ) throws Exception {
		String list = runCommandGetValueCheckDebug( action , rootPath , "find . -type d | grep -v \"^.$\"" );
		return( Common.split( list , "\n" ) );
	}

	@Override public String cmdGetFirstFile( ActionBase action , String dir ) throws Exception {
		String file = runCommandGetValueCheckDebug( action , dir , "ls | head -1" );
		return( file );
	}
	
	@Override public String[] cmdFindFiles( ActionBase action , String dir , String mask ) throws Exception {
		String[] list = runCommandGetLines( action , dir , "find . -type f -name " + Common.getQuoted( mask ) , CommandOutput.LOGLEVEL_TRACE );
		List<String> items = new LinkedList<String>();
		for( String item : list ) {
			if( item.equals( "." ) || item.equals( ".." ) )
				continue;
			
			if( item.startsWith( "./" ) )
				item = item.substring( 2 );
			items.add( item );
		}
			
		return( items.toArray( new String[0] ) );
	}

	@Override public String cmdGetTarContentMD5( ActionBase action , String filePath ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , "tar -xOzf " + filePath + " | md5sum | cut -d \" \" -f1" );
		return( value );
	}

	@Override public String cmdGetFilesMD5( ActionBase action , String dir , String includeList , String excludeList ) throws Exception {
		String find = getFindCommandIncludeExclude( includeList , excludeList , true );
		String list = runCommandGetValueCheckDebug( action , dir , find + " | sort -s" );
		if( list.isEmpty() )
			return( "(nofiles)" );
		
		String cmd = "cat " + Common.fileLinesToList( list ) + " | md5sum | cut -d \" \" -f1";
		String value = runCommandGetValueCheckDebug( action , dir , cmd );
		return( value );
	}

	@Override public Map<String,List<String>> cmdGetFilesContent( ActionBase action , String dir , String fileMask ) throws Exception {
		String delimiter = "URM_DELIMITER";
		String cmd = "for x in $(find . -maxdepth 1 -name " + Common.getQuoted( fileMask ) + "); do echo $x; cat $x; echo " + delimiter + "; done";
		String cmdDir = getDirCmd( action , dir , cmd );
		runCommand( action , cmdDir , CommandOutput.LOGLEVEL_TRACE );
		
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		int pos = 0;
		List<String> data = null;
		for( String s : cmdout ) {
			if( pos == 0 ) {
				if( s.startsWith( "./" ) )
					s = s.substring( 2 );
				
				data = new LinkedList<String>();
				map.put( s , data );
				pos = 1;
				continue;
			}

			if( s.equals( delimiter ) ) {
				pos = 0;
				continue;
			}

			if( s.endsWith( delimiter ) ) {
				data.add( s.substring( 0 , data.size() - delimiter.length() ) );
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
	
	class CommandReaderUnix extends WaiterCommand {
		int logLevel;
		
		public CommandReaderUnix( int logLevel ) {
			this.logLevel = logLevel;
		}
		
		public void run( ActionBase action ) throws Exception {
			readStreamToMarker( action , reader , cmdout , "" );
			readStreamToMarker( action , errreader , cmderr , "stderr:" );
		}
		
		private void outStreamLine( ActionBase action , String line , List<String> text ) throws Exception {
			if( windowsHelper && line.equals( "Active code page: 65001" ) ) {
				if( action.context.CTX_TRACE ) 
					action.logExact( line , CommandOutput.LOGLEVEL_TRACE );
				return;
			}
			
			text.add( line );
			action.logExact( line , logLevel );
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
