package org.urm.action.conf;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.action.ActionBase;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.meta.MetaDistrConfItem;
import org.urm.engine.storage.FileSet;

public class ConfDiffSet {
	
	FileSet releaseSet;
	FileSet prodSet;
	String dirPrefix;
	boolean confComps;
	
	List<ConfDiffItem> diffs;

	Map<String,ConfDiffItem> dirNew;
	Map<String,ConfDiffItem> dirOld;
	Map<String,ConfDiffItem> fileNew;
	Map<String,ConfDiffItem> fileOld;
	Map<String,ConfDiffItem> fileModified;
	
	Map<String,MetaDistrConfItem> fileMatched;
	Map<String,MetaDistrConfItem> topMatched;
	
	public ConfDiffSet( FileSet releaseSet , FileSet prodSet , String dirPrefix , boolean confComps ) {
		this.releaseSet = releaseSet;
		this.prodSet = prodSet;
		this.dirPrefix = dirPrefix;
		this.confComps = confComps;
	}
	
	public boolean isDifferent( ActionBase action ) throws Exception {
		return( diffs.size() > 0 );
	}
	
	public void calculate( ActionBase action , Release release ) throws Exception {
		diffs = new LinkedList<ConfDiffItem>();
		dirNew = new HashMap<String,ConfDiffItem>();
		dirOld = new HashMap<String,ConfDiffItem>();
		fileNew = new HashMap<String,ConfDiffItem>();
		fileOld = new HashMap<String,ConfDiffItem>();
		fileModified = new HashMap<String,ConfDiffItem>();
		
		fileMatched = new HashMap<String,MetaDistrConfItem>();  
		topMatched = new HashMap<String,MetaDistrConfItem>(); 
		
		getFileSetDiffs( action , release );
		getFileContentDiffs( action );
	}

	private void getFileSetDiffs( ActionBase action , Release release ) throws Exception {
		compareDirSet( action , release );
		compareFileSet( action , release );
	}

	private Map<String,String> getMap( ActionBase action , List<String> items ) throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		for( String s : items ) {
			if( dirPrefix != null ) {
				if( !s.startsWith( dirPrefix ) )
					continue;
			}
			
			map.put( s , "ok" );
		}
		return( map );
	}

	private void compareDirSet( ActionBase action , Release release ) throws Exception {
		Map<String,String> dirRel = getMap( action , releaseSet.dirList );
		Map<String,String> dirProd = getMap( action , prodSet.dirList );
		
		for( String key : releaseSet.dirList ) {
			if( dirPrefix != null ) {
				if( !key.startsWith( dirPrefix ) )
					continue;
			}
			
			String itemName = Common.getTopDir( key );
			String topName = itemName;
			if( dirPrefix != null )
				topName = Common.getPartAfterFirst( topName , dirPrefix );

			MetaDistrConfItem comp = null;
			if( confComps )
				comp = action.meta.distr.getConfItem( action , topName );
			
			if( !topMatched.containsKey( topName ) )
				topMatched.put( topName , comp );
			
			if( !dirProd.containsKey( key ) ) {
				// if parent directory is new then do not add new diff, but add new dir
				if( Common.hasDirPart( key ) ) {
					String dir = Common.getDirName( key );
					ConfDiffItem xdiff = dirNew.get( dir );
					if( xdiff != null ) {
						dirNew.put( key , xdiff );
						continue;
					}
				}
				
				ConfDiffItem diff = ConfDiffItem.createNewDirDiff( key );
				diffs.add( diff );
				dirNew.put( key , diff );
			}
		}

		for( String key : prodSet.dirList ) {
			if( dirPrefix != null ) {
				if( !key.startsWith( dirPrefix ) )
					continue;
			}
			
			// ignore check for partial component
			if( confComps && release != null ) {
				String compName = Common.getTopDir( key );
				if( dirPrefix != null )
					compName = Common.getPartAfterFirst( compName , dirPrefix );
				
				ReleaseTarget comp = release.getConfComponent( action , compName );
				if( !comp.ALL )
					continue;
			}
			
			if( !dirRel.containsKey( key ) ) {
				// if parent directory is old then do not add new diff, but add old dir
				if( Common.hasDirPart( key ) ) {
					String dir = Common.getDirName( key );
					ConfDiffItem xdiff = dirOld.get( dir );
					if( xdiff != null ) {
						dirOld.put( key , xdiff );
						continue;
					}
				}
				
				ConfDiffItem diff = ConfDiffItem.createOldDirDiff( key );
				diffs.add( diff );
				dirOld.put( key , diff );
			}
		}
	}

	private void compareFileSet( ActionBase action , Release release ) throws Exception {
		Map<String,String> fileRel = getMap( action , releaseSet.fileList );
		Map<String,String> fileProd = getMap( action , prodSet.fileList );
		
		for( String key : fileRel.keySet() ) {
			if( !fileProd.containsKey( key ) ) {
				String dir = Common.getDirName( key );
				// if parent directory is new then do not add new diff, but add new file
				ConfDiffItem xdiff = dirNew.get( dir );
				if( xdiff != null ) {
					fileNew.put( key , xdiff );
					continue;
				}
				
				ConfDiffItem diff = ConfDiffItem.createNewFileDiff( key );
				diffs.add( diff );
				dirNew.put( key , diff );
			}
			else {
				MetaDistrConfItem comp = null;
				String topName = Common.getTopDir( key );
				if( dirPrefix != null )
					topName = Common.getPartAfterFirst( topName , dirPrefix );
				
				if( confComps )
					comp = action.meta.distr.getConfItem( action , topName );
				fileMatched.put( key , comp );
			}
		}

		for( String key : fileProd.keySet() ) {
			if( confComps && release != null ) {
				// ignore check for partial component
				String compName = Common.getTopDir( key );
				if( dirPrefix != null )
					compName = Common.getPartAfterFirst( compName , dirPrefix );
				
				ReleaseTarget comp = release.getConfComponent( action , compName );
				if( !comp.ALL )
					continue;
			}
			
			if( !fileRel.containsKey( key ) ) {
				String dir = Common.getDirName( key );
				ConfDiffItem xdiff = dirOld.get( dir );
				if( xdiff != null ) {
					fileOld.put( key , xdiff );
					continue;
				}
				
				ConfDiffItem diff = ConfDiffItem.createOldFileDiff( key );
				diffs.add( diff );
				dirNew.put( key , diff );
			}
		}
	}

	private void getFileContentDiffs( ActionBase action ) throws Exception {
		for( String file : fileMatched.keySet() ) {
			ConfFileDiff fileDiff = new ConfFileDiff( file , releaseSet.rootFolder , prodSet.rootFolder );
			
			action.trace( "get diff - " + file + " ..." );
			if( fileDiff.getDiff( action ) ) {
				action.debug( "added diff - " + file );
				ConfDiffItem diff = ConfDiffItem.createFileChangeDiff( file , fileDiff );
				diffs.add( diff );
				fileModified.put( file , diff );
			}
		}
	}

	public void save( ActionBase action , String filePath ) throws Exception {
		BufferedWriter writer = Common.openFile( filePath );
		if( diffs.isEmpty() ) {
			writer.close();
			return;
		}
		
		writer.write( "CONFIGURATION DIFFERENCES:\n" );
		writer.write( "=========================\n" );
		
		for( int k = 0; k < diffs.size(); k++ ) {
			ConfDiffItem diff = diffs.get( k );
			diff.write( action , k , writer );
		}
		
		writer.write( "=========================\n" );
		writer.write( "TOTAL DIFFERENCES: " + diffs.size() + "\n" );
		
		writer.close();
	}

}
