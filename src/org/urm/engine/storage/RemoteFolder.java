package org.urm.engine.storage;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;

public class RemoteFolder extends Folder {
	
	public Account account;
	
	public RemoteFolder( Account account , String folderPath ) {
		super( folderPath , true , account.isWindows() );
		this.account = account;
	}

	public ShellExecutor getSession( ActionBase action ) throws Exception {
		ShellExecutor session = action.getShell( account );
		return( session );
	}
	
	public void copyVFileFromLocal( ActionBase action , LocalFolder localFolder , String SNAME , String DFOLDER , String DNAME , String DBASENAME , String DEXT ) throws Exception {
		String srcDir = localFolder.folderPath;
		String dstDir = Common.getPath( folderPath , DFOLDER );

		ShellExecutor session = getSession( action );
		if( !session.checkDirExists( action , dstDir ) )
			action.exit1( _Error.MissingTargetDirectory1 , "target directory " + dstDir + " does not exist" , dstDir );
			
		if( !DBASENAME.isEmpty() )
			deleteVOld( action , session , DFOLDER , DBASENAME , DEXT );
		
		String srcPath = Common.getPath( srcDir , SNAME );
		
		action.shell.copyFileLocalToTargetRename( action , account , srcPath , dstDir , DNAME );
		action.shell.copyFileLocalToTargetRename( action , account , srcPath + ".md5" , dstDir , DNAME );
	}

	public void deleteVOld( ActionBase action , ShellExecutor session , String FOLDER , String BASENAME , String EXT ) throws Exception {
		String dstDir = Common.getPath( folderPath , FOLDER );
		session.removeFiles( action , dstDir , 
				BASENAME + "-[0-9]*" + EXT + " " + 
				BASENAME + "-[0-9]*" + EXT + ".md5 " + 
				BASENAME + EXT + " " +
				BASENAME + EXT + ".md5" );
	}

	public void deleteVFile( ActionBase action , String FOLDER , String BASENAME , String EXT ) throws Exception {
		ShellExecutor session = getSession( action );
		deleteVOld( action , session , FOLDER , BASENAME , EXT );
	}
	
	public void copyDirFromLocal( ActionBase action , LocalFolder sourceFolder , String dstParentFolder ) throws Exception {
		String srcDir = sourceFolder.folderPath; 
		String dstParentDir = Common.getPath( folderPath , dstParentFolder );
		
		action.shell.copyDirLocalToTarget( action , account , srcDir , dstParentDir );
	}
	
	public void copyDirContentFromLocal( ActionBase action , LocalFolder sourceFolder , String dstParentFolder ) throws Exception {
		String srcDir = sourceFolder.folderPath; 
		String dstParentDir = Common.getPath( folderPath , dstParentFolder );
		
		action.shell.copyDirContentLocalToTarget( action , account , srcDir , dstParentDir );
	}
	
	public RemoteFolder getSubFolder( ActionBase action , String subFolder ) throws Exception {
		String PATH = Common.getPath( folderPath , subFolder );
		return( new RemoteFolder( account , PATH ) );
	}

	public RemoteFolder getParentFolder( ActionBase action ) throws Exception {
		String BASEDIR = Common.getDirName( folderPath );
		return( new RemoteFolder( account , BASEDIR ) );
	}
	
	public String copyFileToLocal( ActionBase action , LocalFolder localFolder , String file ) throws Exception {
		return( copyFileToLocal( action , localFolder , file , "" ) );
	}
	
	public void copyFilesToLocal( ActionBase action , LocalFolder localFolder , String files ) throws Exception {
		copyFilesToLocal( action , localFolder , files , "" );
	}
	
	public void moveFilesFromLocal( ActionBase action , LocalFolder localFolder , String files ) throws Exception {
		moveFilesFromLocal( action , localFolder , files , "" );
	}
	
	public String copyFileToLocal( ActionBase action , LocalFolder localFolder , String file , String FOLDER ) throws Exception {
		String srcPath = Common.getPath( folderPath , FOLDER );
		String dstPath = Common.getPath( localFolder.folderPath , FOLDER );
		
		action.shell.copyFileTargetToLocalDir( action , account , Common.getPath( srcPath , file ) , dstPath );
		return( Common.getPath( dstPath , file ) );
	}

	public void copyFilesToLocal( ActionBase action , LocalFolder localFolder , String files , String FOLDER ) throws Exception {
		String srcPath = Common.getPath( folderPath , FOLDER );
		String dstPath = Common.getPath( localFolder.folderPath , FOLDER );
		
		action.shell.copyFilesTargetToLocal( action , account , Common.getPath( srcPath , files ) , dstPath );
	}

	public void moveFilesFromLocal( ActionBase action , LocalFolder localFolder , String files , String FOLDER ) throws Exception {
		String dstPath = Common.getPath( folderPath , FOLDER );
		String srcPath = Common.getPath( localFolder.folderPath , FOLDER );
		
		action.shell.moveFilesTargetFromLocal( action , account , srcPath , files , dstPath );
	}

	public void copyFilesFromLocal( ActionBase action , LocalFolder localFolder , String files ) throws Exception {
		copyFilesFromLocal( action , localFolder , files , "" );
	}
	
	public void copyFilesFromLocal( ActionBase action , LocalFolder localFolder , String files , String FOLDER ) throws Exception {
		String dstPath = Common.getPath( folderPath , FOLDER );
		String srcPath = Common.getPath( localFolder.folderPath , FOLDER );
		
		action.shell.copyFilesTargetFromLocal( action , account , srcPath , files , dstPath );
	}

	public String copyFileToLocalRename( ActionBase action , LocalFolder localFolder , String file , String newName ) throws Exception {
		String srcPath = Common.getPath( folderPath , file );
		String dstPath = Common.getPath( localFolder.folderPath , newName );
		
		action.shell.copyFileTargetToLocalFile( action , account , srcPath , dstPath );
		return( dstPath );
	}

	public void copyDirContentToLocal( ActionBase action , LocalFolder localFolder , String FOLDER ) throws Exception {
		String srcPath = Common.getPath( folderPath , FOLDER );
		action.shell.copyDirContentTargetToLocal( action , account , srcPath , localFolder.folderPath );
	}
	
	public void copyDirToLocal( ActionBase action , LocalFolder localFolder ) throws Exception {
		action.shell.copyDirTargetToLocal( action , account , folderPath , localFolder.folderPath );
	}
	
	public void copyFileFromLocal( ActionBase action , String filePath , String FOLDER ) throws Exception {
		String dstDir = Common.getPath( folderPath , FOLDER );
		action.shell.copyFileLocalToTarget( action , account , filePath , dstDir );
	}
	
	public void copyFileFromLocal( ActionBase action , LocalFolder folder , String fileName ) throws Exception {
		String filePath = folder.getFilePath( action , fileName );
		action.shell.copyFileLocalToTarget( action , account , filePath , folderPath );
	}

	public String copyFileFromLocal( ActionBase action , String filePath ) throws Exception {
		action.shell.copyFileLocalToTarget( action , account , filePath , folderPath );
		return( Common.getPath( folderPath , Common.getBaseName( filePath ) ) );
	}

	public void copyFileFromLocalRename( ActionBase action , String filePath , String newName ) throws Exception {
		action.shell.copyFileLocalToTargetRename( action , account , filePath , folderPath , newName );
	}

	public void copyFile( ActionBase action , String fileSrc , String fileDst ) throws Exception {
		action.shell.copyDirFileToFile( action , account , folderPath , fileSrc , fileDst );
	}
	
	public void copyFile( ActionBase action , Folder srcFolder , String fileSrc , String newName ) throws Exception {
		ShellExecutor session = getSession( action );
		session.copyFile( action , srcFolder.getFilePath( action , fileSrc ) , getFilePath( action , newName ) );
	}
	
	public boolean isRemote( ActionBase action ) throws Exception {
		if( account.local )
			return( false );
		return( true );
	}

	public String readFile( ActionBase action , String file ) throws Exception {
		if( !account.local )
			action.exitNotImplemented();
		
		return( action.shell.getFileContentAsString( action , getFilePath( action , file ) ) );
	}

	public Date getFileChangeTime( ActionBase action , String file ) throws Exception {
		if( account.local )
			return( super.getFileChangeTime( action ,  file ) );
		
		String path = getFilePath( action , file );
		ShellExecutor shell = action.getShell( account );
		return( shell.getFileChangeTime( action , path ) );
	}
	
}
