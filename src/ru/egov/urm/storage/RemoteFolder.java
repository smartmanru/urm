package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;

public class RemoteFolder extends Folder {
	
	public Account account;
	
	public RemoteFolder( Artefactory artefactory , Account account , String folderPath ) {
		super( artefactory , folderPath , true , account.isWindows() );
		this.account = account;
	}

	public ShellExecutor getSession( ActionBase action ) throws Exception {
		ShellExecutor session = action.getShell( account );
		return( session );
	}
	
	public void copyVFileFromLocal( ActionBase action , LocalFolder localFolder , String FNAME , String FOLDER , String BASENAME , String EXT ) throws Exception {
		String srcDir = localFolder.folderPath;
		String dstDir = Common.getPath( folderPath , FOLDER );

		ShellExecutor session = getSession( action );
		if( !session.checkDirExists( action , dstDir ) )
			action.exit( "target directory " + dstDir + " does not exist" );
			
		if( !BASENAME.isEmpty() )
			deleteVOld( action , session , FOLDER , BASENAME , EXT );
		
		String finalName = Common.getPath( srcDir , FNAME );
		
		action.session.copyFileLocalToTarget( action , account , finalName , dstDir );
		action.session.copyFileLocalToTarget( action , account , finalName + ".md5" , dstDir );
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
		
		action.session.copyDirLocalToTarget( action , account , srcDir , dstParentDir );
	}
	
	public void copyDirContentFromLocal( ActionBase action , LocalFolder sourceFolder , String dstParentFolder ) throws Exception {
		String srcDir = sourceFolder.folderPath; 
		String dstParentDir = Common.getPath( folderPath , dstParentFolder );
		
		action.session.copyDirContentLocalToTarget( action , account , srcDir , dstParentDir );
	}
	
	public RemoteFolder getSubFolder( ActionBase action , String subFolder ) throws Exception {
		String PATH = Common.getPath( folderPath , subFolder );
		return( new RemoteFolder( artefactory , account , PATH ) );
	}

	public RemoteFolder getParentFolder( ActionBase action ) throws Exception {
		String BASEDIR = Common.getDirName( folderPath );
		return( new RemoteFolder( artefactory , account , BASEDIR ) );
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
		
		action.session.copyFileTargetToLocalDir( action , account , Common.getPath( srcPath , file ) , dstPath );
		return( Common.getPath( dstPath , file ) );
	}

	public void copyFilesToLocal( ActionBase action , LocalFolder localFolder , String files , String FOLDER ) throws Exception {
		String srcPath = Common.getPath( folderPath , FOLDER );
		String dstPath = Common.getPath( localFolder.folderPath , FOLDER );
		
		action.session.copyFilesTargetToLocal( action , account , Common.getPath( srcPath , files ) , dstPath );
	}

	public void moveFilesFromLocal( ActionBase action , LocalFolder localFolder , String files , String FOLDER ) throws Exception {
		String dstPath = Common.getPath( folderPath , FOLDER );
		String srcPath = Common.getPath( localFolder.folderPath , FOLDER );
		
		action.session.moveFilesTargetFromLocal( action , account , srcPath , files , dstPath );
	}

	public void copyFilesFromLocal( ActionBase action , LocalFolder localFolder , String files ) throws Exception {
		copyFilesFromLocal( action , localFolder , files , "" );
	}
	
	public void copyFilesFromLocal( ActionBase action , LocalFolder localFolder , String files , String FOLDER ) throws Exception {
		String dstPath = Common.getPath( folderPath , FOLDER );
		String srcPath = Common.getPath( localFolder.folderPath , FOLDER );
		
		action.session.copyFilesTargetFromLocal( action , account , srcPath , files , dstPath );
	}

	public String copyFileToLocalRename( ActionBase action , LocalFolder localFolder , String file , String newName ) throws Exception {
		String srcPath = Common.getPath( folderPath , file );
		String dstPath = Common.getPath( localFolder.folderPath , newName );
		
		action.session.copyFileTargetToLocalFile( action , account , srcPath , dstPath );
		return( dstPath );
	}

	public void copyDirContentToLocal( ActionBase action , LocalFolder localFolder , String FOLDER ) throws Exception {
		String srcPath = Common.getPath( folderPath , FOLDER );
		action.session.copyDirContentTargetToLocal( action , account , srcPath , localFolder.folderPath );
	}
	
	public void copyDirToLocal( ActionBase action , LocalFolder localFolder ) throws Exception {
		action.session.copyDirTargetToLocal( action , account , folderPath , localFolder.folderPath );
	}
	
	public void copyFileFromLocal( ActionBase action , String filePath , String FOLDER ) throws Exception {
		String dstDir = Common.getPath( folderPath , FOLDER );
		action.session.copyFileLocalToTarget( action , account , filePath , dstDir );
	}
	
	public void copyFileFromLocal( ActionBase action , String filePath ) throws Exception {
		action.session.copyFileLocalToTarget( action , account , filePath , folderPath );
	}

	public void copyFileFromLocalRename( ActionBase action , String filePath , String newName ) throws Exception {
		action.session.copyFileLocalToTargetRename( action , account , filePath , folderPath , newName );
	}

	public void copyFile( ActionBase action , String fileSrc , String fileDst ) throws Exception {
		action.session.copyDirFileToFile( action , account , folderPath , fileSrc , fileDst );
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

}
