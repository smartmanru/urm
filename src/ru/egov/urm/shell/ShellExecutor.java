package ru.egov.urm.shell;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;

public abstract class ShellExecutor {

	public String name;
	public ShellExecutorPool pool;
	public String rootPath;
	
	protected ShellCore core;
	
	abstract public void start( ActionBase action ) throws Exception;
	
	public ShellExecutor( String name , ShellExecutorPool pool , String rootPath ) {
		this.name = name;
		this.pool = pool;
		this.rootPath = rootPath;

		core = new ShellCore( this , pool.timeoutDefault );
	}

	public void exitError( ActionBase action , String error ) throws Exception {
		action.exit( name + ": " + error );
	}
	
	public void restart( ActionBase action ) throws Exception {
		boolean initialized = core.initialized; 
		
		core.kill( action );
		if( !initialized )
			action.exit( "session=" + name + " failed on init stage" );
		
		core = new ShellCore( this , action.options.OPT_COMMANDTIMEOUT );
		start( action );
	}
	
	protected void createProcess( ActionBase action , ProcessBuilder builder , String rootPath ) throws Exception {
		action.debug( "start session=" + name + " at rootPath=" + rootPath );
		core.createProcess( action , builder , rootPath );
	}
	
	public void kill( ActionBase action ) throws Exception {
		core.kill( action );
	}

	public String getProcessId() {
		return( core.processId );
	}

	public String getHomePath() {
		return( core.homePath );
	}
	
	public String createDir( ActionBase action , String home , String dir ) throws Exception {
		core.runCommandCheckDebug( action , "mkdir -p " + home + "/" + dir );
		return( home + "/" + dir );
	}
	
	public void createFileFromString( ActionBase action , String path , String value ) throws Exception {
		core.runCommandCheckDebug( action , "echo " + Common.getQuoted( value ) + " > " + path );
	}

	public void appendFileWithString( ActionBase action , String path , String value ) throws Exception {
		core.runCommandCheckDebug( action , "echo " + value + " >> " + path );
	}

	public void appendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception {
		core.runCommandCheckDebug( action , "cat " + pathSrc + " >> " + pathDst );
	}

	public boolean checkDirExists( ActionBase action , String path ) throws Exception {
		if( path.isEmpty() )
			return( false );
		
		String ok = core.runCommandGetValueCheckDebug( action , "if [ -d " + path + " ]; then echo ok; fi" );
		return( ok.equals( "ok" ) );
	}

	public boolean isFileEmpty( ActionBase action , String path ) throws Exception {
		String ok = core.runCommandGetValueCheckDebug( action , "if [ -f " + path + " ]; then wc -w " + path + "; fi" );
		return( ok.startsWith( "0" ) );
	}

	public boolean checkFileExists( ActionBase action , String dir , String path ) throws Exception {
		return( checkFileExists( action , Common.getPath( dir , path ) ) );
	}
	
	public boolean checkFileExists( ActionBase action , String path ) throws Exception {
		if( path.isEmpty() )
			return( false );
		
		String ok = core.runCommandGetValueCheckDebug( action , "if [ -f " + path + " ]; then echo ok; fi" );
		return( ok.equals( "ok" ) );
	}

	public boolean checkPathExists( ActionBase action , String path ) throws Exception {
		if( path.isEmpty() )
			return( false );
		
		String ok = core.runCommandGetValueCheckDebug( action , "if [ -f " + path + " ] || [ -d " + path + " ]; then echo ok; fi" );
		return( ok.equals( "ok" ) );
	}

	public void download( ActionBase action , String URL , String TARGETNAME , String auth ) throws Exception {
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

	public String findOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception {
		String value = core.runCommandGetValueCheckDebug( action , path , "find . -maxdepth 1 -name " + Common.getQuoted( mask ) + 
				" | egrep " + Common.getQuoted( grepMask ) + " | tr '\\n' ' '" );
		value = value.trim();
		
		if( value.isEmpty() )
			return( "" );
		
		if( value.indexOf( ' ' ) > 0 )
			action.exit( "too many files found in path=" + path + ", mask=" + Common.getQuoted( mask ) + " (" + value + ")" );
		
		value = Common.getPartAfterFirst( value , "./" );
		return( value );
	}
	
	public String findOneTop( ActionBase action , String path , String mask ) throws Exception {
		String value = core.runCommandGetValueCheckDebug( action , path , "find . -maxdepth 1 -name " + Common.getQuoted( mask ) + " | tr '\\n' ' '" );
		value = value.trim();
		
		if( value.isEmpty() )
			return( "" );
		
		if( value.indexOf( ' ' ) > 0 )
			action.exit( "too many files found in path=" + path + ", mask=" + Common.getQuoted( mask ) + " (" + value + ")" );
		
		value = Common.getPartAfterFirst( value , "./" );
		return( value );
	}

	public void ensureDirExists( ActionBase action , String dirpath ) throws Exception {
		core.runCommandCheckDebug( action , "mkdir -p " + dirpath );
	}

	public void createMD5( ActionBase action , String filepath ) throws Exception {
		core.runCommandCheckDebug( action , "md5sum " + filepath + " | cut -d " + Common.getQuoted( " " ) + " -f1 > " + filepath + ".md5" );
	}

	public void removeDirContent( ActionBase action , String dirpath ) throws Exception {
		core.runCommandCheckDebug( action , "rm -rf " + dirpath + "/*" );
	}
	
	public void removeDir( ActionBase action , String dirpath ) throws Exception {
		core.runCommandCheckDebug( action , "rm -rf " + dirpath );
	}
	
	public void recreateDir( ActionBase action , String dirpath ) throws Exception {
		core.runCommandCheckDebug( action , "rm -rf " + dirpath + "; mkdir -p " + dirpath );
	}
	
	public void removeFiles( ActionBase action , String dir , String files ) throws Exception {
		core.runCommandCheckDebugIfDir( action , dir , "rm -rf " + files );
	}
	
	public void removeFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		String includeOptions = "";
		for( String s : Common.splitSpaced( files ) ) {
			if( !includeOptions.isEmpty() )
				includeOptions += " -o";
			includeOptions += " -name " + Common.getQuoted( s );
		}
		String excludeOptions = "";
		if( !exclude.isEmpty() ) {
			for( String s : Common.splitSpaced( exclude ) )
				excludeOptions += " ! -name " + Common.getQuoted( s );
		}
		
		String find = "find . -maxdepth 1 \\( " + includeOptions + " ! -name \".\" " + excludeOptions;
		core.runCommandCheckDebugIfDir( action , dir , find + " \\) -exec rm -rf {} \\;" );
	}
	
	public void unzip( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception {
		core.runCommandCheckDebug( action , unzipDir , "unzip " + zipFile + " " + zipPart + " -d " + targetDir + " > /dev/null" );
	}

	public void move( ActionBase action , String source , String target ) throws Exception {
		core.runCommandCheckDebug( action , "mv " + source + " " + target );
	}

	public void extractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		core.runCommandCheckDebug( action , targetFolder , "tar --no-same-owner --overwrite -zxmf " + tarFile + " > /dev/null" );
	}

	public String ls( ActionBase action , String path ) throws Exception {
		String value = core.runCommandGetValueCheckDebug( action , path , "ls" );
		return( value );
	}

	public void createTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		String excludeOption = "";
		if( !exclude.isEmpty() ) {
			for( String pattern : Common.splitSpaced( exclude ) )
				excludeOption += "--exclude=" + Common.getQuoted( pattern ) + " ";
		}
		core.runCommandCheckDebug( action , dir , "tar " + excludeOption + "-zcf " + tarFile + " " + content + " > /dev/null 2> /dev/null" );
	}

	public String getFileInfo( ActionBase action , String dir , String dirFile ) throws Exception {
		String value = core.runCommandGetValueCheckDebug( action , dir , "ls -l " + dirFile );
		
		if( value.isEmpty() )
			action.exit( "cannot find file=" + dirFile + " in directory " + dir );
		
		return( value );
	}

	public void custom( ActionBase action , String cmd ) throws Exception {
		core.runCommand( action , cmd , false );
	}
	
	public void custom( ActionBase action , String dir , String cmd ) throws Exception {
		core.runCommand( action , cmd , dir , false );
	}

	public void customDeployment( ActionBase action , String dir , String cmd ) throws Exception {
		String cmdDir = core.getDirCmd( action , dir , cmd );
		customDeployment( action , cmdDir );
	}
	
	public void customDeployment( ActionBase action , String cmd ) throws Exception {
		if( action.context.SHOWONLY ) {
			action.debug( name + ": showonly " + cmd );
		}
		else {
			action.debug( name + ": execute " + cmd );
			core.runCommand( action , "echo `date` " + Common.getQuoted( "(SSH_CLIENT=\\$SSH_CLIENT): " + cmd ) + " >> ~/execute.log" , false );
			core.runCommand( action , cmd , false );
		}
	}
	
	public int customGetStatus( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetStatusDebug( action , cmd ) );
	}
	
	public int customGetStatusNormal( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetStatusNormal( action , cmd ) );
	}
	
	public void customCheckStatus( ActionBase action , String cmd ) throws Exception {
		core.runCommandCheckStatusDebug( action , cmd );
	}

	public void customCheckStatus( ActionBase action , String dir , String cmd ) throws Exception {
		core.runCommandCheckStatusDebug( action , dir , cmd );
	}

	public void customCheckErrorsDebug( ActionBase action , String cmd ) throws Exception {
		core.runCommandCheckDebug( action , cmd );
	}

	public void customCheckErrorsNormal( ActionBase action , String cmd ) throws Exception {
		core.runCommandCheckNormal( action , cmd );
	}

	public String customGetValueNoCheck( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetValueNoCheck( action , cmd , true ) );
	}

	public String customGetValue( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetValueCheckDebug( action , cmd ) );
	}

	public String[] customGetLines( ActionBase action , String cmd ) throws Exception {
		return( core.runCommandGetLines( action , cmd , true ) );
	}

	public String[] customGetLines( ActionBase action , String dir , String cmd ) throws Exception {
		return( core.runCommandGetLines( action , dir , cmd , true ) );
	}

	public String customGetValue( ActionBase action , String dir , String cmd ) throws Exception {
		return( core.runCommandGetValueCheckDebug( action , dir , cmd ) );
	}

	public void unzipToFolder( ActionBase action , String runDir , String zipFile , String folder ) throws Exception {
		core.runCommandCheckDebug( action , runDir , "unzip " + zipFile + " -d " + folder + " > /dev/null" );
	}

	public void createJarFromFolder( ActionBase action , String runDir , String jarFile , String folder ) throws Exception {
		core.runCommandCheckDebug( action , runDir , "jar cfvM " + jarFile + " -C " + folder + "/ . > /dev/null" );
	}

	public void export( ActionBase action , String var , String value ) throws Exception {
		core.runCommandCheckDebug( action , "export " + var + "=" + value );
	}
	
	public void mvnCheckStatus( ActionBase action , String runDir , String MAVEN_CMD ) throws Exception {
		core.runCommandCheckStatusNormal( action , runDir , MAVEN_CMD );
	}
	
	public void gitAddPomFiles( ActionBase action , String runDir ) throws Exception {
		core.runCommandCheckDebug( action , runDir ,  
				"for pom in `find . -name " + Common.getQuoted( "pom.xml" ) + "`; do\n" +
				"	git add $pom\n" +
				"done" );
	}

	public void cd( ActionBase action , String dir ) throws Exception {
		core.runCommandCheckDebug( action , "cd " + dir );
	}

	public void copyFiles( ActionBase action , String dirFrom , String files , String dirTo ) throws Exception {
		action.debug( "copy " + files + " from " + dirFrom + " to " + dirTo + " ..." );
		core.runCommandCheckDebug( action , dirFrom , "cp -p " + files + " " + dirTo );
	}
	
	public void copyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception {
		action.debug( "copy " + fileFrom + " to " + fileTo + " ..." );
		core.runCommandCheckDebug( action , "cp -p " + fileFrom + " " + fileTo );
	}
	
	public void copyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception {
		String finalDir = Common.getPath( targetDir , FOLDER );
		String baseName = Common.getBaseName( fileFrom );
		String finalFile;
		if( !finalName.isEmpty() )
			finalFile = finalDir + "/" + finalName;
		else
			finalFile = finalDir + "/" + baseName;
			
		action.debug( "copy " + fileFrom + " to " + finalFile + " ..." );
		core.runCommandCheckDebug( action , "cp -p " + fileFrom + " " + finalFile );
	}

	public void copyDirContent( ActionBase action , String srcDir , String dstDir ) throws Exception {
		action.debug( "copy content from " + srcDir + " to " + dstDir + " ..." );
		core.runCommandCheckDebug( action , "if [ \"`ls -A " + srcDir + "`\" != \"\" ]; then cp -R -p " + srcDir + "/* " + dstDir + "/; fi" );
	}

	public void copyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception {
		action.debug( "copy dir " + dirFrom + " to " + dirTo + " ..." );
		core.runCommandCheckDebug( action , "mkdir -p `dirname " + dirTo + "`; rm -rf " + dirTo + "; cp -R -p " + dirFrom + "/* " + dirTo );
	}
	
	public void copyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception {
		String baseName = Common.getBaseName( dirFrom );
		removeDir( action , baseDstDir + "/" + baseName );
		ensureDirExists( action , baseDstDir );
		
		action.debug( "copy " + dirFrom + " to " + baseDstDir + " ..." );
		core.runCommandCheckDebug( action , "cp -R -p " + dirFrom + " " + baseDstDir + "/" );
	}
	
	public void scpFilesRemoteToLocal( ActionBase action , String srcPath , String hostLogin , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		setTimeoutUnlimited( action );
		core.runCommandCheckDebug( action , "scp -q -B -p " + keyOption + hostLogin + ":" + srcPath + " " + dstPath );
	}

	public void scpDirContentRemoteToLocal( ActionBase action , String srcPath , String hostLogin , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		setTimeoutUnlimited( action );
		core.runCommandCheckDebug( action , "scp -q -B -p " + keyOption + hostLogin + ":" + srcPath + "/* " + dstPath );
	}

	public void scpFilesLocalToRemote( ActionBase action , String srcPath , String hostLogin , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		setTimeoutUnlimited( action );
		core.runCommandCheckDebug( action , "scp -q -B -p " + keyOption + srcPath + " " + hostLogin + ":" + dstPath );
	}

	public void scpDirLocalToRemote( ActionBase action , String srcDirPath , String hostLogin , String baseDstDir ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		String baseName = Common.getBaseName( srcDirPath );
		ShellExecutor session = action.getShell( hostLogin );
		session.removeDir( action , baseDstDir + "/" + baseName );
		session.ensureDirExists( action , baseDstDir );
		
		setTimeoutUnlimited( action );
		core.runCommandCheckDebug( action , "scp -r -q -B -p " + keyOption + srcDirPath + " " + hostLogin + ":" + baseDstDir );
	}

	public void scpDirContentLocalToRemote( ActionBase action , String srcDirPath , String hostLogin , String dstDir ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		ShellExecutor session = action.getShell( hostLogin );
		session.ensureDirExists( action , dstDir );
		
		setTimeoutUnlimited( action );
		core.runCommandCheckDebug( action , "scp -r -q -B -p " + keyOption + srcDirPath + "/* " + hostLogin + ":" + dstDir );
	}

	public void scpDirRemoteToLocal( ActionBase action , String srcPath , String hostLogin , String dstPath ) throws Exception {
		String keyOption = "";
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			keyOption = "-i " + keyFile + " ";
		
		setTimeoutUnlimited( action );
		core.runCommandCheckDebug( action , "scp -r -q -B -p " + keyOption + hostLogin + ":" + srcPath + " " + dstPath );
	}

	public void copyFileTargetToLocal( ActionBase action , String hostLogin , String srcFilePath , String dstDir ) throws Exception {
		if( action.isLocal( hostLogin ) )
			copyFile( action , srcFilePath , dstDir );
		else {
			scpFilesRemoteToLocal( action , srcFilePath , hostLogin , dstDir + "/" );
		}
	}

	public void copyFilesTargetToLocal( ActionBase action , String hostLogin , String srcFiles , String dstDir ) throws Exception {
		if( action.isLocal( hostLogin ) )
			copyFiles( action , Common.getDirName( srcFiles ) , Common.getBaseName( srcFiles ) , dstDir );
		else {
			scpFilesRemoteToLocal( action , srcFiles , hostLogin , dstDir + "/" );
		}
	}

	public void moveFilesTargetFromLocal( ActionBase action , String hostLogin , String srcFiles , String dstDir ) throws Exception {
		if( action.isLocal( hostLogin ) )
			move( action , srcFiles , dstDir );
		else {
			scpFilesLocalToRemote( action , srcFiles , hostLogin , dstDir + "/" );
			removeFiles( action , Common.getDirName( srcFiles ) , Common.getBaseName( srcFiles ) );
		}
	}

	public void copyDirContentTargetToLocal( ActionBase action , String hostLogin , String srcDir , String dstDir ) throws Exception {
		if( action.isLocal( hostLogin ) )
			copyDirContent( action , srcDir , dstDir );
		else {
			scpDirContentRemoteToLocal( action , srcDir , hostLogin , dstDir + "/" );
		}
	}
	
	public void copyDirTargetToLocal( ActionBase action , String hostLogin , String srcDir , String dstBaseDir ) throws Exception {
		if( action.isLocal( hostLogin ) )
			copyDirToBase( action , srcDir , dstBaseDir );
		else {
			scpDirRemoteToLocal( action , srcDir , hostLogin , dstBaseDir + "/" );
		}
	}

	public void copyFileLocalToTarget( ActionBase action , String hostLogin , String srcFilePath , String dstDir ) throws Exception {
		if( action.isLocal( hostLogin ) )
			copyFile( action , srcFilePath , dstDir , "" , "" );
		else {
			scpFilesLocalToRemote( action , srcFilePath , hostLogin , dstDir + "/" );
		}
	}

	public void copyFileLocalToTargetRename( ActionBase action , String hostLogin , String srcFilePath , String dstDir , String newName ) throws Exception {
		if( action.isLocal( hostLogin ) )
			copyFile( action , srcFilePath , dstDir , newName , "" );
		else {
			scpFilesLocalToRemote( action , srcFilePath , hostLogin , dstDir + "/" + newName );
		}
	}

	public void copyDirFileToFile( ActionBase action , String hostLogin , String dirPath , String fileSrc , String fileDst ) throws Exception {
		ShellExecutor session = action.getShell( hostLogin );
		session.custom( action , dirPath , "cp " + fileSrc + " " + fileDst );
	}
	
	public void copyDirLocalToTarget( ActionBase action , String hostLogin , String srcDirPath , String baseDstDir ) throws Exception {
		if( action.isLocal( hostLogin ) )
			copyDirToBase( action , srcDirPath , baseDstDir );
		else {
			scpDirLocalToRemote( action , srcDirPath , hostLogin , baseDstDir + "/" );
		}
	}

	public void copyDirContentLocalToTarget( ActionBase action , String hostLogin , String srcDirPath , String dstDir ) throws Exception {
		if( action.isLocal( hostLogin ) )
			this.copyDirContent( action , srcDirPath , dstDir );
		else {
			this.scpDirContentLocalToRemote( action , srcDirPath , hostLogin , dstDir + "/" );
		}
	}

	public void getDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		String delimiter = "URM_DELIMITER";
		List<String> res = core.runCommandCheckGetOutputDebug( action , rootPath , 
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
	
	public void getTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		String delimiter = "URM_DELIMITER";
		List<String> res = core.runCommandCheckGetOutputDebug( action , rootPath , 
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
	
	public String getMD5( ActionBase action , String filePath ) throws Exception {
		String value = core.runCommandGetValueCheckDebug( action , "md5sum " + filePath + " | cut -d " + Common.getQuoted( " " ) + " -f1" );
		return( value );
	}

	public void prepareDirForLinux( ActionBase action , String dirPath ) throws Exception {
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
	
	public String getFileContentAsString( ActionBase action , String filePath ) throws Exception {
		String value = core.runCommandGetValueCheckDebug( action , "cat " + filePath );
		return( value );
	}

	public void appendExecuteLog( ActionBase action , String msg ) throws Exception {
		appendFileWithString( action , "~/execute.log" , Common.getQuoted( "`date` (SSH_CLIENT=$SSH_CLIENT): " + msg ) ); 
	}

	public void appendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		String msg = "upload " + dst + " from " + src;
		appendFileWithString( action , "~/upload.log" , Common.getQuoted( "`date` (SSH_CLIENT=$SSH_CLIENT): " + msg ) ); 
	}
	
	public void setTimeout( ActionBase action , int timeout ) throws Exception {
		core.setTimeout( action ,  timeout );
	}

	public void setTimeoutUnlimited( ActionBase action ) throws Exception {
		core.setTimeout( action ,  0 );
	}
	
}
