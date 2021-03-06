package org.urm.engine.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;

public class LocalFolder extends Folder {

	public LocalFolder( String folderPath , boolean windows ) {
		super( folderPath , false , windows );
	}

	@Override 
	public ShellExecutor getSession( ActionBase action ) throws Exception {
		return( action.shell );
	}
	
	@Override
	public boolean checkExists( ActionBase action ) throws Exception {
		String path = super.getLocalPath( action );
		File file = new File( path );
		return( file.isDirectory() );
	}

	@Override
	public void ensureExists( ActionBase action ) throws Exception {
		String path = super.getLocalPath( action );
		File file = new File( path );
		if( !file.isDirectory() )
			file.mkdirs();
	}
	
	@Override
	public boolean checkFileExists( ActionBase action , String filename ) throws Exception {
		String path = super.getLocalFilePath( action , filename );
		File file = new File( path );
		return( file.isFile() );
	}
	
	@Override
	public LocalFolder getSubFolder( ActionBase action , String folder ) throws Exception {
		if( folder.isEmpty() || folder.equals( "/" ) || folder.equals( "." ) )
			return( this );
		String newPath = ( folder.startsWith( "/" ) )? folderPath + folder : folderPath + "/" + folder;
		return( new LocalFolder( newPath , windows ) );
	}
	
	@Override
	public LocalFolder getParentFolder( ActionBase action ) throws Exception {
		String BASEDIR = Common.getDirName( folderPath );
		return( new LocalFolder( BASEDIR , windows ) );
	}
	
	@Override
	public String readFile( ActionBase action , String FILENAME ) throws Exception {
		String filePath = getFilePath( action , FILENAME );
		return( action.readFile( filePath ) );
	}

	@Override
	public void copyFileRename( ActionBase action , String srcFile , String newName ) throws Exception {
		Files.copy( Paths.get( super.getLocalFilePath( action , srcFile ) ) , Paths.get( super.getLocalFilePath( action , newName ) ) , StandardCopyOption.REPLACE_EXISTING );
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
		String srcFilePath = action.getLocalPath( Common.getPath( folderPath , srcFolder , fileName ) );
		String dstFilePath = action.getLocalPath( Common.getPath( dstFolder.folderPath , newName ) );
		Files.copy( new File( srcFilePath ).toPath() , new File( dstFilePath ).toPath() , StandardCopyOption.REPLACE_EXISTING );
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

	public boolean equals( LocalFolder folder ) {
		if( folderPath.equals( folder.folderPath ) )
			return( true );
		return( false );
	}

	public String[] listFilesSorted() throws Exception {
		String path = ( windows )? Common.getWinPath( folderPath ) : Common.getLinuxPath( folderPath );
		File folder = new File( path );
		return( Common.getSortedList( folder.list() ) );
	}
	
}
