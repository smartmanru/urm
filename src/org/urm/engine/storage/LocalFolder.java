package org.urm.engine.storage;

import java.util.List;

import org.urm.common.Common;
import org.urm.engine.action.ActionBase;
import org.urm.engine.shell.ShellExecutor;

public class LocalFolder extends Folder {

	public LocalFolder( String folderPath , boolean windows ) {
		super( folderPath , false , windows );
	}

	@Override public ShellExecutor getSession( ActionBase action ) throws Exception {
		return( action.shell );
	}
	
	public LocalFolder getSubFolder( ActionBase action , String folder ) throws Exception {
		if( folder.isEmpty() || folder.equals( "/" ) )
			return( this );
		return( new LocalFolder( folderPath + "/" + folder , windows ) );
	}
	
	public LocalFolder getParentFolder( ActionBase action ) throws Exception {
		String BASEDIR = Common.getDirName( folderPath );
		return( new LocalFolder( BASEDIR , windows ) );
	}
	
	public String readFile( ActionBase action , String FILENAME ) throws Exception {
		String filePath = getFilePath( action , FILENAME );
		return( action.readFile( filePath ) );
	}
	
	public List<String> readFileLines( ActionBase action , String FILENAME ) throws Exception {
		String filePath = getFilePath( action , FILENAME );
		return( action.readFileLines( filePath ) );
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

	public boolean equals( LocalFolder folder ) {
		if( folderPath.equals( folder.folderPath ) )
			return( true );
		return( false );
	}
	
}
