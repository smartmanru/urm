package org.urm.action.conf;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.engine.dist.ReleaseDistScope;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.engine.storage.FileSet;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.release.Release;

public class ConfDiffSet {
	
	Meta meta;
	FileSet releaseSet;
	FileSet masterSet;
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
	
	public ConfDiffSet( Meta meta , FileSet releaseSet , FileSet masterSet , String dirPrefix , boolean confComps ) {
		this.meta = meta;
		this.releaseSet = releaseSet;
		this.masterSet = masterSet;
		this.dirPrefix = dirPrefix;
		this.confComps = confComps;
	}
	
	public boolean isDifferent() {
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
		
		ReleaseDistScope scope = null;
		if( release != null )
			scope = ReleaseDistScope.createScope( release );
		
		getFileSetDiffs( action , scope );
		getFileContentDiffs( action );
	}

	private void getFileSetDiffs( ActionBase action , ReleaseDistScope scope ) throws Exception {
		compareDirSet( action , scope );
		compareFileSet( action , scope );
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

	private void compareDirSet( ActionBase action , ReleaseDistScope scope ) throws Exception {
		Map<String,String> dirRel = getMap( action , releaseSet.dirList );
		Map<String,String> dirMaster = getMap( action , masterSet.dirList );
		
		ReleaseDistScopeSet set = null;
		if( scope != null )
			set = scope.findCategorySet( DBEnumScopeCategoryType.CONFIG );
		
		MetaDistr distr = meta.getDistr();
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
				comp = distr.getConfItem( topName );
			
			if( !topMatched.containsKey( topName ) )
				topMatched.put( topName , comp );
			
			if( !dirMaster.containsKey( key ) ) {
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

		for( String key : masterSet.dirList ) {
			if( dirPrefix != null ) {
				if( !key.startsWith( dirPrefix ) )
					continue;
			}
			
			// ignore check for partial component
			if( confComps && scope != null ) {
				String compName = Common.getTopDir( key );
				if( dirPrefix != null )
					compName = Common.getPartAfterFirst( compName , dirPrefix );
				
				ReleaseDistScopeDeliveryItem comp = set.findDeliveryItem( compName );
				if( comp.partial )
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

	private void compareFileSet( ActionBase action , ReleaseDistScope scope ) throws Exception {
		Map<String,String> fileRel = getMap( action , releaseSet.fileList );
		Map<String,String> fileProd = getMap( action , masterSet.fileList );
		
		ReleaseDistScopeSet set = null;
		if( scope != null )
			set = scope.findCategorySet( DBEnumScopeCategoryType.CONFIG );
		
		MetaDistr distr = meta.getDistr();
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
					comp = distr.getConfItem( topName );
				fileMatched.put( key , comp );
			}
		}

		for( String key : fileProd.keySet() ) {
			if( confComps && scope != null ) {
				// ignore check for partial component
				String compName = Common.getTopDir( key );
				if( dirPrefix != null )
					compName = Common.getPartAfterFirst( compName , dirPrefix );
				
				ReleaseDistScopeDeliveryItem comp = set.findDeliveryItem( compName );
				if( comp.partial )
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
			ConfFileDiff fileDiff = new ConfFileDiff( file , releaseSet.rootFolder , masterSet.rootFolder );
			
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
