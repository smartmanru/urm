package ru.egov.urm.storage;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;

public abstract class Folder {

	Artefactory artefactory;
	public Metadata meta;
	public String folderPath;
	public String folderName;
	public boolean remote;
	public boolean windows;
	
	public abstract ShellExecutor getSession( ActionBase action ) throws Exception; 

	protected Folder( Artefactory artefactory , String folderPath , boolean remote , boolean windows ) {
		this.artefactory = artefactory;
		this.folderPath = folderPath;
		this.meta = artefactory.meta;
		this.remote = remote;
		this.windows = windows;
		
		folderName = Common.getBaseName( folderPath );
	}

	public String getBaseName( ActionBase action ) throws Exception {
		return( Common.getBaseName( folderPath ) );
	}
	
	public void ensureExists( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.ensureDirExists( action , folderPath );
	}
	
	public void ensureFolderExists( ActionBase action , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.ensureDirExists( action , Common.getPath( folderPath , folder ) );
	}
	
	public void md5file( ActionBase action , String subPath ) throws Exception {
		String fname = Common.getPath( folderPath , subPath );
		ShellExecutor session = getSession( action ); 
		session.createMD5( action , fname );
	}

	public String md5value( ActionBase action , String filePath ) throws Exception {
		String fname = Common.getPath( folderPath , filePath );
		ShellExecutor session = getSession( action ); 
		return( session.getMD5( action , fname ) );
	}

	public String getFilesMD5( ActionBase action ) throws Exception {
		return( getFilesMD5( action , "*" , "" ) );
	}
	
	public String getFilesMD5( ActionBase action , String includeList , String excludeList ) throws Exception {
		ShellExecutor session = getSession( action ); 
		return( session.getFilesMD5( action , folderPath , includeList , excludeList ) );
	}
	
	public void removeAll( ActionBase action ) throws Exception {
		if( folderPath.isEmpty() || folderPath.equals( "/" ) )
			action.exit( "attempt to delete files at root" );
		
		ShellExecutor session = getSession( action ); 
		session.removeDirContent( action , folderPath );
	}

	public void moveAll( ActionBase action , String targetPath ) throws Exception {
		if( folderPath.isEmpty() || folderPath.equals( "/" ) )
			action.exit( "attempt to delete files at root" );
		
		ShellExecutor session = getSession( action ); 
		session.move( action , folderPath + "/*" , targetPath + "/" );
	}

	public boolean checkFileExists( ActionBase action , String file ) throws Exception {
		ShellExecutor session = getSession( action ); 
		return( session.checkFileExists( action , Common.getPath( folderPath , file ) ) );
	}

	public boolean isFileEmpty( ActionBase action , String file ) throws Exception {
		ShellExecutor session = getSession( action ); 
		return( session.isFileEmpty( action , Common.getPath( folderPath , file ) ) );
	}
	
	public boolean checkExists( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action ); 
		return( session.checkDirExists( action , folderPath ) );
	}

	public boolean checkPathExists( ActionBase action , String path ) throws Exception {
		ShellExecutor session = getSession( action ); 
		return( session.checkPathExists( action , Common.getPath( folderPath , path ) ) );
	}
	
	public boolean checkFolderExists( ActionBase action , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		return( session.checkDirExists( action , Common.getPath( folderPath , folder ) ) );
	}

	public void copyFile( ActionBase action , String srcFile ) throws Exception {
		ShellExecutor session = getSession( action );
		session.copyFile( action , srcFile , Common.ensureDir( folderPath ) );
	}
	
	public void copyFileRename( ActionBase action , String srcFile , String newName ) throws Exception {
		ShellExecutor session = getSession( action );
		session.copyFile( action , srcFile , Common.getPath( folderPath , newName ) );
	}
	
	public void copyFile( ActionBase action , String folder1 , String folder2 , String newName , String targetSubFolder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		if( targetSubFolder.isEmpty() )
			session.copyFile( action , Common.getPath( folderPath , folder1 ) , Common.getPath( folderPath , folder2 ) , newName , "" );
		else
			session.copyFile( action , Common.getPath( folderPath , folder1 ) , Common.getPath( folderPath ,targetSubFolder , folder2 ) , newName , "" );
	}

	public String getFilePath( ActionBase action , String file ) {
		return( folderPath + "/" + file ); 
	}
	
	public String getFolderPath( ActionBase action , String FOLDER ) {
		if( !FOLDER.isEmpty() )
			return( Common.getPath( folderPath , FOLDER ) );
		
		return( folderPath );
	}
	
	public void removeContent( ActionBase action ) throws Exception {
		removeFolderContent( action , "" );
	}
	
	public void removeFolderContent( ActionBase action , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.removeDirContent( action , Common.getPath( folderPath , folder ) );
	}

	public void removeFolder( ActionBase action , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.removeDir( action , Common.getPath( folderPath , folder ) );
	}

	public void removeThis( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.removeDir( action , folderPath );
	}

	public void removeFiles( ActionBase action , String files ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.removeFiles( action , folderPath , files );
	}
	
	public void removeFilesWithExclude( ActionBase action , String files , String exclude ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.removeFilesWithExclude( action , folderPath , files , exclude );
	}
	
	public void removeFolderFile( ActionBase action , String folder , String file ) throws Exception {
		String finalDir = folderPath;
		if( !folder.isEmpty() )
			finalDir = Common.getPath( finalDir , folder );
			
		ShellExecutor session = getSession( action ); 
		session.removeFiles( action , finalDir , file );
	}
	
	public void recreateThis( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.recreateDir( action , folderPath );
	}

	public void recreateFolder( ActionBase action , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.recreateDir( action , Common.getPath( folderPath , folder ) );
	}

	public void moveFileToThis( ActionBase action , String file ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.move( action , Common.getPath( folderPath , file ) , folderPath );
	}
	
	public void moveFileToFolder( ActionBase action , String file , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.move( action , Common.getPath( folderPath , file ) , Common.getPath( folderPath , folder ) );
	}
	
	public void extractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.extractTarGz( action , tarFile , Common.getPath( folderPath , targetFolder ) );
	}
	
	public void extractTar( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.extractTar( action , tarFile , Common.getPath( folderPath , targetFolder ) );
	}
	
	public String getFolderContent( ActionBase action , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		return( session.ls( action , Common.getPath( folderPath , folder ) ) );
	}
	
	public List<String> getTopDirs( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action ); 
		List<String> dirs = new LinkedList<String>();  
		List<String> files = new LinkedList<String>();  
		session.getTopDirsAndFiles( action , folderPath , dirs , files );
		return( dirs );
	}
	
	public List<String> getTopFiles( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action ); 
		List<String> dirs = new LinkedList<String>();  
		List<String> files = new LinkedList<String>();  
		session.getTopDirsAndFiles( action , folderPath , dirs , files );
		return( files );
	}
	
	public void getTopDirsAndFiles( ActionBase action , List<String> dirs , List<String> files ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.getTopDirsAndFiles( action , folderPath , dirs , files );
	}

	public void createZipFromContent( ActionBase action , String zipFilePath , String content ) throws Exception {
		createZipFromFolderContent( action , zipFilePath , "" , content );
	}
	
	public void createZipFromFolderContent( ActionBase action , String zipFilePath , String folder , String content ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.createZipFromDirContent( action , zipFilePath , Common.getPath( folderPath , folder ) , content );
	}
	
	public void createTarGzFromFolderContent( ActionBase action , String tarFilePath , String folder , String content , String exclude ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.createTarGzFromDirContent( action , tarFilePath , Common.getPath( folderPath , folder ) , content , exclude );
	}
	
	public void createTarGzFromContent( ActionBase action , String tarFilePath , String content , String exclude ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.createTarGzFromDirContent( action , tarFilePath , folderPath , content , exclude );
	}
	
	public void createTarFromFolderContent( ActionBase action , String tarFilePath , String folder , String content , String exclude ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.createTarFromDirContent( action , tarFilePath , Common.getPath( folderPath , folder ) , content , exclude );
	}
	
	public void createTarFromContent( ActionBase action , String tarFilePath , String content , String exclude ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.createTarFromDirContent( action , tarFilePath , folderPath , content , exclude );
	}
	
	public void createFileFromString( ActionBase action , String filepath , String value ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.createFileFromString( action , Common.getPath( folderPath , filepath ) , value );
	}
	
	public void appendFileWithString( ActionBase action , String filepath , String value ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.appendFileWithString( action , Common.getPath( folderPath , filepath ) , value );
	}

	public void appendFileWithFile( ActionBase action , String fileDst , String fileSrc ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.appendFileWithFile( action , Common.getPath( folderPath , fileDst ) , Common.getPath( folderPath , fileSrc ) );
	}
	
	public String getFileInfo( ActionBase action , String file ) throws Exception {
		ShellExecutor session = getSession( action ); 
		return( session.getFileInfo( action , folderPath , file ) );
	}
	
	public void unzipToFolder( ActionBase action , String zipFile , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.unzip( action , folderPath , zipFile , folder );
	}
	
	public void unzipSingleFile( ActionBase action , String zipFile , String zipPart , String newName ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.unzipPart( action , folderPath , zipFile , zipPart , "" );
		if( !newName.equals( zipPart ) )
			renameFile( action , zipPart , newName );
	}
	
	public void renameThis( ActionBase action , String newName ) throws Exception {
		ShellExecutor session = getSession( action );
		String parentPath = Common.getDirName( folderPath );
		session.move( action , folderPath , Common.getPath( parentPath , newName ) );
	}
	
	public void renameFolder( ActionBase action , String folder , String newName ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.move( action , Common.getPath( folderPath , folder ) , Common.getPath( folderPath , newName ) );
	}
	
	public void renameFile( ActionBase action , String file , String newName ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.move( action , Common.getPath( folderPath , file ) , Common.getPath( folderPath , newName ) );
	}
	
	public void createJarFromFolder( ActionBase action , String jar , String folder ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.createJarFromFolder( action , folderPath , jar , folder );
	}
	
	public void download( ActionBase action , String DOWNLOAD_URL_REQUEST , String DOWNLOAD_FILENAME , String auth ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.downloadUnix( action , DOWNLOAD_URL_REQUEST , Common.getPath( folderPath , DOWNLOAD_FILENAME ) , auth );
	}
	
	public FileSet getFileSet( ActionBase action ) throws Exception {
		List<String> dirs = new LinkedList<String>(); 
		List<String> files = new LinkedList<String>(); 
		ShellExecutor session = getSession( action ); 
		session.getDirsAndFiles( action , folderPath , dirs , files );

		FileSet root = new FileSet( this , dirs , files );
		root.makeStructure( action );
		return( root );
	}
	
	public void prepareFolderForLinux( ActionBase action , String FOLDER ) throws Exception {
		ShellExecutor session = getSession( action ); 
		session.prepareDirForLinux( action , Common.getPath( folderPath , FOLDER ) );
	}

	public String findOneTop( ActionBase action , String mask ) throws Exception {
		ShellExecutor session = getSession( action );
		return( session.findOneTop( action , folderPath , mask ) );
	}
	
	public String findOneTopWithGrep( ActionBase action , String mask , String grepMask ) throws Exception {
		ShellExecutor session = getSession( action );
		return( session.findOneTopWithGrep( action , folderPath , mask , grepMask ) );
	}
	
	public String getFileContentAsString( ActionBase action , String path ) throws Exception {
		String fullPath = getFilePath( action , path );
		ShellExecutor session = getSession( action ); 
		String content = session.getFileContentAsString( action , fullPath );
		return( content );
	}

	public String[] getFolders( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action );
		return( session.getFolders( action , folderPath ) );
	}

	public String findBinaryDistItemFile( ActionBase action , MetaDistrBinaryItem item , String specificDeployBaseName ) throws Exception {
		String deployBasename = specificDeployBaseName;
		if( deployBasename.isEmpty() )
			deployBasename = item.DEPLOYBASENAME;
		
		boolean addDotSlash = ( windows )? false : true;
		String filePath = findOneTopWithGrep( action , "*" + deployBasename + "*" + item.EXT , item.getGrepMask( action , deployBasename , addDotSlash ) );

		// ensure correct file
		if( filePath.isEmpty() ) {
			action.trace( "findBinaryDistItem: file " + deployBasename + item.EXT + " not found in " + folderPath );
			return( "" );
		}

		return( Common.getBaseName( filePath ) );
	}

	public boolean isEmpty( ActionBase action ) throws Exception {
		ShellExecutor session = getSession( action );
		String file = session.getFirstFile( action , folderPath );
		if( file.isEmpty() )
			return( true );
		
		return( false );
	}

	public String[] findFiles( ActionBase action , String mask ) throws Exception {
		ShellExecutor session = getSession( action );
		return( session.findFiles( action , folderPath , mask ) );
	}

	public String getFileMD5( ActionBase action , String file ) throws Exception {
		ShellExecutor session = getSession( action );
		String filePath = getFilePath( action , file );
		return( session.getMD5( action , filePath ) );
	}

	public Folder getParentFolder( ActionBase action ) throws Exception {
		if( remote )
			return( (( RemoteFolder )this).getParentFolder( action ) );
		return( (( LocalFolder )this).getParentFolder( action ) );
	}
	
	public Folder getSubFolder( ActionBase action , String subFolder ) throws Exception {
		if( remote )
			return( (( RemoteFolder )this).getSubFolder( action , subFolder ) );
		return( (( LocalFolder )this).getSubFolder( action , subFolder ) );
	}

}
