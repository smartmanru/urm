package org.urm.engine.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaProductDoc;

public class FileSet {

	public Folder rootFolder;
	
	// path of this dir relative to root folder
	public String dirPath;
	public String dirName;
	// map basename to dir
	private Map<String,FileSet> dirs = new HashMap<String,FileSet>();
	// map basename to path relative to rootFolder
	private Map<String,String> files = new HashMap<String,String>();
	// find content
	public List<String> dirList;
	public List<String> fileList;
	
	public FileSet( LocalFolder rootFolder ) {
		this.rootFolder = rootFolder;
		this.dirPath = "";
		this.dirName = "";
	}
	
	public FileSet( Folder rootFolder , List<String> dirList , List<String> fileList ) {
		this.rootFolder = rootFolder;
		this.dirList = dirList;
		this.fileList = fileList;
		this.dirPath = "";
		this.dirName = "";
	}
	
	public FileSet( Folder rootFolder , String dirPath ) {
		this.rootFolder = rootFolder;
		this.dirPath = dirPath;
		this.dirName = Common.getBaseName( dirPath );
	}
	
	public void addDir( FileSet dir ) {
		dirs.put( Common.getBaseName( dir.dirPath ) , dir );
	}

	public void addFile( String baseName ) throws Exception {
		files.put( baseName , Common.getPath( dirPath , baseName ) );
	}
	
	public String[] getAllDirNames() {
		return( Common.getSortedKeys( dirs ) );
	}
	
	public FileSet[] getAllDirs() {
		List<FileSet> sorted = new LinkedList<FileSet>();
		for( String dir : Common.getSortedKeys( dirs ) )
			sorted.add( dirs.get( dir ) );
		return( sorted.toArray( new FileSet[0] ) );
	}
	
	public FileSet findDirByName( String name ) {
		return( dirs.get( name ) );
	}
	
	public boolean findFileByName( String name ) {
		return( files.containsKey( name ) );
	}
	
	public String findFileMatched( String regexp ) {
		for( String s : files.keySet() ) {
			if( s.matches( regexp ) )
				return( files.get( s ) );
		}
		for( FileSet dir : dirs.values() ) {
			String f = dir.findFileMatched( regexp );
			if( f != null )
				return( f );
		}
		return( null );
	}
	
	public String[] getFilesMatched( String regexp ) {
		List<String> files = new LinkedList<String>();
		getFilesMatchedInternal( files , regexp );
		return( Common.getSortedList( files ) );
	}
	
	private void getFilesMatchedInternal( List<String> list , String regexp ) {
		for( String s : files.keySet() ) {
			if( s.matches( regexp ) )
				list.add( files.get( s ) );
		}
		for( FileSet dir : dirs.values() )
			dir.getFilesMatchedInternal( list , regexp );
	}
	
	public String[] getAllFiles() {
		return( Common.getSortedKeys( files ) );
	}
	
	public String getFilePath( String name ) {
		return( files.get( name ) );
	}
	
	public FileSet createDir( String dir ) throws Exception {
		FileSet parent = this;
		if( Common.hasDirPart( dir ) ) {
			String parentDir = Common.getDirName( dir );
			parent = createDir( parentDir );
		}

		String baseDir = Common.getBaseName( dir );
		FileSet resBase = parent.dirs.get( baseDir );
		if( resBase != null )
			return( resBase );
		
		FileSet child = new FileSet( rootFolder , dir );
		parent.addDir( child );
		return( child );
	}

	public boolean isEmpty() {
		if( dirs.isEmpty() && files.isEmpty() )
			return( true );
		return( false );
	}
	
	public void makeStructure( ActionBase action ) throws Exception {
		for( int k = 0; k < dirList.size(); k++ ) {
			String dir = dirList.get( k );
			createDir( dir );
		}
		
		for( int k = 0; k < fileList.size(); k++ ) {
			String file = fileList.get( k );
			FileSet set = this;
			if( Common.hasDirPart( file ) ) {
				String dir = Common.getDirName( file );
				set = createDir( dir );
			}
			
			set.addFile( Common.getBaseName( file ) );
		}
	}
	
	public FileSet getDirByPath( ActionBase action , String path ) throws Exception {
		if( path.equals( "." ) || path.isEmpty() )
			return( this );
		
		FileSet res = this;
		String findPath = path;
		while( !findPath.isEmpty() ) {
			String topDir = Common.getTopDir( findPath );
			res = res.dirs.get( topDir );
			if( res == null )
				return( null );
			
			findPath = Common.getSubdir( findPath );
		}

		return( res );
	}

	public void removeAll( ActionBase action ) throws Exception {
		dirs.clear();
		files.clear();
		dirList.clear();
		fileList.clear();
	}

	public String findDistItem( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
		return( findDistItem( action , distItem , "" ) );
	}
	
	public String findDistItem( ActionBase action , MetaProductDoc doc ) throws Exception {
		return( findDistItem( action , doc , "" ) );
	}
	
	public String findDistItem( ActionBase action , MetaDistrBinaryItem distItem , String subPath ) throws Exception {
		FileSet delivery = getDirByPath( action , subPath );
		if( delivery == null ) {
			action.trace( "missing delivery folder=" + subPath + " for distItem=" + distItem.NAME );
			return( "" );
		}
		
		String[] patterns = distItem.getVersionPatterns();
		for( String baseName : delivery.files.keySet() ) {
			for( String pattern : patterns ) {
				if( baseName.matches( pattern ) )
					return( baseName );
			}
		}
		
		action.trace( "missing distItem=" + distItem.NAME + " (path=" + subPath + ", search using " + Common.getList( patterns) + ")" );
		return( "" );
	}

	public String findDistItem( ActionBase action , MetaProductDoc doc , String subPath ) throws Exception {
		FileSet delivery = getDirByPath( action , subPath );
		if( delivery == null ) {
			action.trace( "missing delivery folder=" + subPath + " for doc=" + doc.NAME );
			return( "" );
		}

		String docname = doc.NAME + doc.EXT;
		for( String baseName : delivery.files.keySet() ) {
			if( baseName.equals( docname ) )
				return( baseName );
		}
		
		action.trace( "missing doc=" + doc.NAME + " (path=" + subPath + ")" );
		return( "" );
	}

	public String findDistItem( ActionBase action , MetaDistrConfItem distItem , String subPath ) throws Exception {
		FileSet delivery = getDirByPath( action , subPath );
		if( delivery == null ) {
			action.trace( "missing delivery folder=" + subPath + " for distItem=" + distItem.NAME );
			return( "" );
		}

		FileSet comp = delivery.getDirByPath( action , distItem.NAME );
		if( comp != null )
			return( distItem.NAME );
		
		action.trace( "missing distItem=" + distItem.NAME + " (path=" + subPath + ")" );
		return( "" );
	}

	public boolean hasFilesEndingWith( String ending ) throws Exception {
		for( String s : files.keySet() ) {
			if( s.endsWith( ending ) )
				return( true );
		}
		return( false );
	}
	
	public String[] getAllDirs( ActionBase action ) throws Exception {
		List<String> pathList = new LinkedList<String>();
		pathList.add( "." );
		getAllDirsPrefix( action , "" , pathList );
		return( pathList.toArray( new String[0] ) );
	}

	public void getAllDirsPrefix( ActionBase action , String prefix , List<String> pathList ) throws Exception {
		for( FileSet dir : dirs.values() ) {
			pathList.add( prefix + dir.dirName );
			dir.getAllDirsPrefix( action , prefix + dir.dirName + "/" , pathList );
		}
	}

	public String[] getFilesMatched( ActionBase action , String regex ) throws Exception {
		List<String> fileList = new LinkedList<String>();
		for( String file : files.keySet() )
			if( file.matches( regex ) )
				fileList.add( file );
		return( fileList.toArray( new String[0] ) );
	}
	
	public boolean hasFiles() throws Exception {
		if( !files.isEmpty() )
			return( true );
		
		for( FileSet dir : dirs.values() )
			if( dir.hasFiles() )
				return( true );
		
		return( false );
	}
	
}
