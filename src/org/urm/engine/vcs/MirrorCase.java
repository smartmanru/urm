package org.urm.engine.vcs;

import java.io.File;

import org.urm.action.ActionBase;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.MetaProductSettings;

public abstract class MirrorCase {

	GenericVCS vcs;
	MirrorRepository mirror;
	String BRANCH;
	
	public Account account;
	
	protected ShellExecutor shell;
	protected ActionBase action;

	abstract public String getResourceRepositoryPath() throws Exception;
	abstract public String getResourceBranchPath() throws Exception;
	abstract public String getResourceComponentPath() throws Exception;
	
	abstract public void createEmptyMirrorOnServer() throws Exception;
	abstract public void useMirror() throws Exception;
	abstract public void refreshMirror() throws Exception;
	abstract public void pushMirror() throws Exception;
	abstract public void dropMirror( boolean dropOnServer ) throws Exception;
	
	abstract public void syncFolderToVcs( String mirrorSubFolder , LocalFolder folder ) throws Exception;
	abstract public void syncVcsToFolder( String mirrorFolder , LocalFolder folder ) throws Exception;
	abstract public LocalFolder getMirrorFolder() throws Exception;
	abstract public String getSpecialDirectory();
	
	public MirrorCase( GenericVCS vcs ) {
		this.vcs = vcs;
		
		shell = vcs.shell;
		action = vcs.action;
	}

	public MirrorCase( GenericVCS vcs , MirrorRepository mirror , String BRANCH ) {
		this.vcs = vcs;
		this.mirror = mirror;
		this.BRANCH = BRANCH;
		
		shell = vcs.shell;
		action = vcs.action;
	}

	public LocalFolder getBaseFolder() throws Exception {
		String mirrorPath;
		if( vcs.meta == null ) {
			EngineSettings settings = action.getServerSettings();
			mirrorPath = settings.context.WORK_MIRRORPATH;
		}
		else {
			MetaProductSettings product = vcs.meta.getProductSettings( action );
			mirrorPath = product.CONFIG_MIRRORPATH;
		}
		
		if( mirrorPath.isEmpty() )
			action.exit0( _Error.MissingMirrorPathParameter0 , "Missing configuration parameter: mirror path" );
		
		return( action.getLocalFolder( mirrorPath ) );
	}

	public String getBaseResourcePath() {
		return( vcs.res.NAME );
	}

	public LocalFolder getResourceFolder() throws Exception {
		LocalFolder base = getBaseFolder();
		return( base.getSubFolder( action , getBaseResourcePath() ) );
	}

	protected LocalFolder getRepositoryFolder() throws Exception {
		LocalFolder res = getResourceFolder();
		return( res.getSubFolder( action , getResourceRepositoryPath() ) );
	}
	
	protected LocalFolder getBranchFolder() throws Exception {
		LocalFolder res = getResourceFolder();
		return( res.getSubFolder( action , getResourceBranchPath() ) );
	}
	
	protected LocalFolder getComponentFolder() throws Exception {
		LocalFolder res = getResourceFolder();
		return( res.getSubFolder( action , getResourceComponentPath() ) );
	}
	
	public void removeResourceFolder() throws Exception {
		LocalFolder res = getResourceFolder();
		if( shell.checkDirExists( action , res.folderPath ) )
			res.removeThis( action );
	}
	
	protected void syncFolderToVcsContent( String mirrorSubFolder , LocalFolder folder ) throws Exception {
		LocalFolder cf = getMirrorFolder();
		LocalFolder mf = cf.getSubFolder( action , mirrorSubFolder );
		LocalFolder sf = folder;
		
		if( !mf.checkExists( action ) ) {
			mf.ensureExists( action );
			mf.copyDirContent( action , sf );
			vcs.addDirToCommit( mirror , mf , "." );
		}
		else {
			FileSet mset = mf.getFileSet( action , getPattern() );
			FileSet sset = sf.getFileSet( action );
			syncFolderToVcs( mf , sf , mset , sset );
		}
	}

	protected void syncVcsToFolderContent( String mirrorFolder , LocalFolder folder ) throws Exception {
		LocalFolder cf = getMirrorFolder();
		LocalFolder mf = cf.getSubFolder( action , mirrorFolder );
		if( !mf.checkExists( action ) ) {
			folder.removeThis( action );
			folder.ensureExists( action );
			return;
		}

		folder.ensureExists( action );
		
		LocalFolder sf = folder;
		FileSet mset = mf.getFileSet( action , getPattern() );
		FileSet sset = sf.getFileSet( action );
		syncVcsToFolder( mf , sf , mset , sset );
		
		if( action.isLocalLinux() )
			addLinuxExecution( folder , mset );
	}

	public String getPattern() {
		String dir = getSpecialDirectory();
		if( shell.isLinux() )
			return( "/" + dir + "/" );
		return( "\\\\" + dir + "\\\\.*" );
	}
	
	private void syncFolderToVcs( LocalFolder mfolder , LocalFolder sfolder , FileSet mset , FileSet sset ) throws Exception {
		// add to mirror and change
		for( FileSet sd : sset.getAllDirs() ) {
			if( vcs.ignoreDir( sd.dirName ) )
				continue;
			
			FileSet md = mset.findDirByName( sd.dirName );
			if( md == null ) {
				sfolder.copyFolder( action , sd.dirPath , mfolder.getSubFolder( action , sd.dirPath ) );
				vcs.addDirToCommit( mirror , mfolder , sd.dirPath );
			}
			else
				syncFolderToVcs( mfolder , sfolder , md , sd );
		}

		// delete from mirror
		for( FileSet md : mset.getAllDirs() ) {
			if( vcs.ignoreDir( md.dirName ) )
				continue;
			
			FileSet sd = sset.findDirByName( md.dirName );
			if( sd == null )
				vcs.deleteDirToCommit( mirror , mfolder , md.dirPath );
		}
		
		// add files to mirror and change
		LocalFolder dstFolder = mfolder.getSubFolder( action , mset.dirPath );
		for( String sf : sset.getAllFiles() ) {
			if( vcs.ignoreFile( sf ) )
				continue;
			
			sfolder.copyFile( action , sset.dirPath , sf , dstFolder , sf );
			if( !mset.findFileByName( sf ) )
				vcs.addFileToCommit( mirror , mfolder , mset.dirPath , sf );
		}

		// delete from mirror
		for( String mf : mset.getAllFiles() ) {
			if( vcs.ignoreFile( mf ) )
				continue;
			
			if( !sset.findFileByName( mf ) )
				vcs.deleteFileToCommit( mirror , mfolder , mset.dirPath , mf );
		}
	}
	
	private void addLinuxExecution( LocalFolder folder , FileSet set ) throws Exception {
		for( String f : set.getAllFiles() ) {
			if( f.endsWith( ".sh" ) ) {
				File ff = new File( folder.getFilePath( action , f ) );
				ff.setExecutable( true );
			}
		}
		for( FileSet md : set.getAllDirs() ) {
			if( vcs.ignoreDir( md.dirName ) )
				continue;
			addLinuxExecution( folder.getSubFolder( action , md.dirName ) , md );
		}
	}
	
	private void syncVcsToFolder( LocalFolder mfolder , LocalFolder sfolder , FileSet mset , FileSet sset ) throws Exception {
		// add to source and change
		for( FileSet md : mset.getAllDirs() ) {
			if( vcs.ignoreDir( md.dirName ) )
				continue;
			
			FileSet sd = sset.findDirByName( md.dirName );
			if( sd == null )
				mfolder.copyFolder( action , md.dirPath , sfolder.getSubFolder( action , md.dirPath ) );
			else
				syncVcsToFolder( mfolder , sfolder , md , sd );
		}

		// delete from source
		for( FileSet sd : sset.getAllDirs() ) {
			if( vcs.ignoreDir( sd.dirName ) )
				continue;
			
			FileSet md = mset.findDirByName( sd.dirName );
			if( md == null )
				sfolder.removeFolder( action , sd.dirPath );
		}
		
		// add files to source and change
		LocalFolder dstFolder = sfolder.getSubFolder( action , sset.dirPath );
		for( String mf : mset.getAllFiles() ) {
			if( vcs.ignoreFile( mf ) )
				continue;
			
			mfolder.copyFile( action , mset.dirPath , mf , dstFolder , mf );
		}

		// delete from mirror
		for( String sf : sset.getAllFiles() ) {
			if( vcs.ignoreFile( sf ) )
				continue;
			
			if( !mset.findFileByName( sf ) )
				dstFolder.removeFolderFile( action , "" , sf );
		}
	}
	
}
