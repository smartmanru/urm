package ru.egov.urm.storage;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.shell.ShellExecutor;

public class LocalFolder extends Folder {

	public LocalFolder( Artefactory artefactory , String folderPath ) {
		super( artefactory , folderPath , false , artefactory.context.account.isWindows() );
	}

	@Override public ShellExecutor getSession( ActionBase action ) throws Exception {
		return( action.session );
	}
	
	public LocalFolder getSubFolder( ActionBase action , String folder ) throws Exception {
		if( folder.isEmpty() || folder.equals( "/" ) )
			return( this );
		return( new LocalFolder( artefactory , folderPath + "/" + folder ) );
	}
	
	public LocalFolder getParentFolder( ActionBase action ) throws Exception {
		String BASEDIR = Common.getDirName( folderPath );
		return( new LocalFolder( artefactory , BASEDIR ) );
	}
	
	public String readFile( ActionBase action , String FILENAME ) throws Exception {
		String filePath = getFilePath( action , FILENAME );
		return( ConfReader.readFile( action , filePath ) );
	}
	
	public List<String> readFileLines( ActionBase action , String FILENAME ) throws Exception {
		String filePath = getFilePath( action , FILENAME );
		return( ConfReader.readFileLines( action , filePath ) );
	}

	public void copyFiles( ActionBase action , String fileNames , LocalFolder dstFolder ) throws Exception {
		copyFiles( action , "" , fileNames , dstFolder );
	}
	
	public void copyFiles( ActionBase action , String srcFolder , String fileNames , LocalFolder dstFolder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.copyFiles( action , Common.getPath( folderPath , srcFolder ) , fileNames , dstFolder.folderPath );
	}
	
	public void copyFile( ActionBase action , String srcFolder , String fileName , LocalFolder dstFolder , String newName ) throws Exception {
		String srcFilePath = Common.getPath( folderPath , srcFolder , fileName );
		String dstFilePath = Common.getPath( dstFolder.folderPath , newName );
		ShellExecutor session = getSession( action ); 
		session.copyFile( action , srcFilePath , dstFilePath );
	}

	public void copyFolder( ActionBase action , String srcFolder , LocalFolder dstFolder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.copyDirDirect( action , Common.getPath( folderPath , srcFolder ) , dstFolder.folderPath );
	}

	public void copyDirContent( ActionBase action , LocalFolder srcFolder ) throws Exception {
		copyDirContentToFolder( action , srcFolder , "" );
	}
	
	public void copyDirContentToFolder( ActionBase action , LocalFolder srcFolder , String dstFolder ) throws Exception {
		String srcDir = srcFolder.folderPath;
		String dstDir = Common.getPath( folderPath , dstFolder ); 
		ShellExecutor session = getSession( action ); 
		session.copyDirContent( action , srcDir , dstDir );
	}

	public void removeVcsFiles( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action );
		session.customCheckStatus( action , folderPath , "rm -rf `find . -name \".svn\" -o -name \".git\"`" );
	}

}
