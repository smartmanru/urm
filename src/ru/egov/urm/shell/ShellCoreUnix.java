package ru.egov.urm.shell;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;

public class ShellCoreUnix extends ShellCore {

	public ShellCoreUnix( ShellExecutor executor , int commandTimeoutDefault , VarOSTYPE osType ) {
		super( executor , commandTimeoutDefault , osType );
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
		processId = runCommandGetValueCheckDebug( action , "echo $$" );
		homePath = runCommandGetValueCheckDebug( action , "echo $HOME" );
	}
	
	@Override public void runCommand( ActionBase action , String cmd , boolean debug ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
			
		cmdCurrent = cmd;

		cmdout.clear();
		cmderr.clear();
		
		String execLine = cmd + "; echo " + finishMarker + " >&2; echo " + finishMarker + "\n";
		action.trace( executor.name + " execute: " + cmd );
		writer.write( execLine );
		try {
			writer.flush();
		}
		catch( Throwable e ) {
		}
		
		ShellWaiter waiter = new ShellWaiter( executor , new CommandReader( debug ) );
		boolean res = waiter.wait( action , commandTimeout );
		commandTimeout = commandTimeoutDefault;
		
		if( !res )
			exitError( action , "command has been killed" );
	}

	@Override public int runCommandGetStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd + "; echo COMMAND_STATUS=$?" , debug );
		
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

	@Override public void runCommandCritical( ActionBase action , String cmd ) throws Exception {
		if( action.context.SHOWONLY ) {
			action.debug( executor.name + ": showonly " + cmd );
		}
		else {
			action.debug( executor.name + ": execute " + cmd );
			runCommand( action , "echo `date` " + Common.getQuoted( "(SSH_CLIENT=\\$SSH_CLIENT): " + cmd ) + " >> ~/execute.log" , false );
			runCommand( action , cmd , false );
		}
	}
	
	@Override public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		return( "( if [ -d " + Common.getQuoted( dir ) + " ]; then cd " + dir + "; " + cmd + "; else echo invalid directory: " + dir + " >&2; fi )" );
	}
	
	@Override public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		return( "( if [ -d " + Common.getQuoted( dir ) + " ]; then cd " + dir + "; " + cmd + "; fi )" );
	}

	@Override protected void killProcess( ActionBase action ) throws Exception {
		executor.pool.master.custom( action , "pkill -9 -P " + processId );
	}

	@Override public void cmdEnsureDirExists( ActionBase action , String dir ) throws Exception {
		runCommandCheckDebug( action , "mkdir -p " + dir );
	}

	@Override public void cmdCreateFileFromString( ActionBase action , String path , String value ) throws Exception {
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
		
		String filesOption = "";
		if( filesOnly )
			filesOption = " -type f";
		else
			filesOption = " -maxdepth 1";
			
		String find = "find ." + filesOption + " \\( " + includeOptions + " ! -name \".\" " + excludeOptions + " \\)";
		return( find );
	}
	
	@Override public void cmdRemoveFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		String find = getFindCommandIncludeExclude( files , exclude , false );
		runCommandCheckDebugIfDir( action , dir , find + " -exec rm -rf {} \\;" );
	}

	@Override public void cmdUnzipPart( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception {
		if( zipPart == null || zipPart.equals( "*" ) )
			runCommandCheckDebug( action , unzipDir , "unzip -d " + targetDir + " " + zipFile + " > /dev/null" );
		else
			runCommandCheckDebug( action , unzipDir , "unzip -d " + targetDir + " " + zipFile + " " + zipPart + " > /dev/null" );
	}

	@Override public void cmdMove( ActionBase action , String source , String target ) throws Exception {
		runCommandCheckDebug( action , "mv " + source + " " + target );
	}

	@Override public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		runCommandCheckDebug( action , targetFolder , "tar --no-same-owner --overwrite -zxmf " + tarFile + " > /dev/null" );
	}
	
	@Override public String cmdLs( ActionBase action , String path ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , path , "ls" );
		return( value );
	}
	
	@Override public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		String excludeOption = "";
		if( !exclude.isEmpty() ) {
			for( String pattern : Common.splitSpaced( exclude ) )
				excludeOption += "--exclude=" + Common.getQuoted( pattern ) + " ";
		}
		runCommandCheckDebug( action , dir , "tar " + excludeOption + "-zcf " + tarFile + " " + content + " > /dev/null 2> /dev/null" );
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
		action.debug( "copy " + files + " from " + dirFrom + " to " + dirTo + " ..." );
		runCommandCheckDebug( action , dirFrom , "cp -p " + files + " " + dirTo );
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
		runCommandCheckDebug( action , "mkdir -p `dirname " + dirTo + "`; rm -rf " + dirTo + "; cp -R -p " + dirFrom + "/* " + dirTo );
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
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		runCommandCheckDebug( action , "scp -q -B -p " + keyOption + account.HOSTLOGIN + ":" + srcPath + " " + dstPath );
	}

	@Override public void cmdScpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		setTimeout( action , 0 );
		runCommandCheckDebug( action , "scp -q -B -p " + keyOption + account.HOSTLOGIN + ":" + srcPath + "/* " + dstPath );
	}

	@Override public void cmdScpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		setTimeout( action , 0 );
		runCommandCheckDebug( action , "scp -q -B -p " + keyOption + srcPath + " " + account.HOSTLOGIN + ":" + dstPath );
	}

	@Override public void cmdScpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		String baseName = Common.getBaseName( srcDirPath );
		ShellExecutor session = action.getShell( account );
		session.removeDir( action , baseDstDir + "/" + baseName );
		session.ensureDirExists( action , baseDstDir );
		
		setTimeout( action , 0 );
		runCommandCheckDebug( action , "scp -r -q -B -p " + keyOption + srcDirPath + " " + account.HOSTLOGIN + ":" + baseDstDir );
	}

	@Override public void cmdScpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		ShellExecutor session = action.getShell( account );
		session.ensureDirExists( action , dstDir );
		
		setTimeout( action , 0 );
		runCommandCheckDebug( action , "scp -r -q -B -p " + keyOption + srcDirPath + "/* " + account.HOSTLOGIN + ":" + dstDir );
	}

	@Override public void cmdScpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		setTimeout( action , 0 );
		runCommandCheckDebug( action , "scp -r -q -B -p " + keyOption + account.HOSTLOGIN + ":" + srcPath + " " + dstPath );
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
		String value = runCommandGetValueCheckDebug( action , "md5sum " + filePath + " | cut -d " + Common.getQuoted( " " ) + " -f1" );
		return( value );
	}

	@Override public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception {
		String value = runCommandGetValueCheckDebug( action , "cat " + filePath );
		return( value );
	}

	@Override public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception {
		cmdAppendFileWithString( action , "~/execute.log" , Common.getQuoted( "`date` (SSH_CLIENT=$SSH_CLIENT): " + msg ) ); 
	}

	@Override public void cmdAppendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		String msg = "upload " + dst + " from " + src;
		cmdAppendFileWithString( action , "~/upload.log" , Common.getQuoted( "`date` (SSH_CLIENT=$SSH_CLIENT): " + msg ) ); 
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
		String[] list = runCommandGetLines( action , dir , "find . -type f -name " + Common.getQuoted( mask ) , true );
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
		String value = runCommandGetValueCheckDebug( action , "tar -xOzf " + filePath + " | md5sum" );
		return( value );
	}

	@Override public String cmdGetFilesMD5( ActionBase action , String dir , String includeList , String excludeList ) throws Exception {
		String find = getFindCommandIncludeExclude( includeList , excludeList , true );
		String list = runCommandGetValueCheckDebug( action , dir , find + " | sort -s" );
		if( list.isEmpty() )
			return( "(nofiles)" );
		
		String cmd = "cat " + list + " | md5sum";
		String value = runCommandGetValueCheckDebug( action , dir , cmd );
		return( value );
	}
}
