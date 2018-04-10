package org.urm.engine.vcs;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.MirrorRepository;

public class MirrorCaseSubversion extends MirrorCase {

	SubversionVCS vcsSubversion;
	
	public MirrorCaseSubversion( SubversionVCS vcs , MirrorRepository mirror , String BRANCH ) {
		super( vcs , mirror , BRANCH );
		this.vcsSubversion = vcs;
	}
	
	@Override
	public String getResourceRepositoryPath() {
		String path = Common.getPath( mirror.RESOURCE_ROOT , mirror.RESOURCE_REPO + ".svn.branches" );
		return( path );
	}
	
	@Override
	public String getResourceBranchPath() {
		String comp = getResourceComponentPath();
		String branch = getBranch();
		return( Common.getPath( comp , branch ) );
	}
	
	@Override
	public String getResourceComponentPath() {
		String repo = getResourceRepositoryPath();
		return( Common.getPath( repo , mirror.RESOURCE_DATA ) );
	}
	
	@Override
	public void createEmptyMirrorOnServer() throws Exception {
		useRepositoryMirror();
		
		LocalFolder comp = getComponentFolder();
		if( !comp.checkExists( action ) ) {
			comp.ensureExists( action );
			vcsSubversion.createRepositoryFolders( mirror );
		}

		LocalFolder branch = getBranchFolder();
		if( branch.checkExists( action ) ) {
			refreshMirror();
			if( !checkBranchEmpty() ) {
				String OSPATH = shell.getLocalPath( mirror.RESOURCE_DATA );
				action.exit1( _Error.MirrorDirectoryNotEmpty1 , "Target mirror folder is not empty - " + OSPATH , OSPATH );
			}
			return;
		}

		boolean res = false; 
		try {
			String branchName = getBranch();
			String branchPath = getComponentBranchPath( branchName );
			if( checkValidServerBranch( branchName ) ) {
				vcsSubversion.checkoutMasterRootFolder( mirror , branch , branchPath );
				if( !checkBranchEmpty() ) {
					String OSPATH = shell.getLocalPath( mirror.RESOURCE_DATA );
					action.exit1( _Error.MirrorDirectoryNotEmpty1 , "Target mirror folder is not empty - " + OSPATH , OSPATH );
				}
			}
			else {
				vcsSubversion.createMasterFolder( mirror , branchPath , "create component branch" );
				vcsSubversion.checkoutMasterRootFolder( mirror , branch , branchPath );
			}
			res = true;
		}
		finally {
			if( !res )
				branch.removeThis( action );
		}
	}

	@Override
	public void useMirror() throws Exception {
		useRepositoryMirror();
		
		LocalFolder branch = getBranchFolder();
		if( branch.checkExists( action ) ) {
			refreshMirror();
			return;
		}
		
		String branchName = getBranch();
		if( !checkValidServerBranch( branchName ) )
			action.exit1( _Error.MissingRepoBranch1 , "Missing repository branch=" + branchName , branchName );
		
		String path = getComponentBranchPath( branchName );
		vcsSubversion.checkoutMasterRootFolder( mirror , branch , path );
	}
	
	@Override
	public void refreshMirror() throws Exception {
		LocalFolder branch = getBranchFolder();
		try {
			action.setTimeout( 10 * 60 * 1000 );
			shell.customCheckStatus( action , branch.folderPath , "svn update " + vcsSubversion.SVNAUTH );
		}
		finally {
			action.setTimeoutDefault();
		}
	}
	
	@Override
	public void pushMirror() throws Exception {
		String msg = "push to origin";
		LocalFolder branch = getBranchFolder();
		try {
			action.setTimeout( 10 * 60 * 1000 );
			shell.customCheckStatus( action , branch.folderPath , "svn commit -m " + Common.getQuoted( msg ) + " " + vcsSubversion.SVNAUTH );
		}
		finally {
			action.setTimeoutDefault();
		}
	}

	@Override
	public void dropMirror( boolean dropOnServer ) throws Exception {
	}

	@Override 
	public LocalFolder getMirrorFolder() throws Exception {
		return( getBranchFolder() );
	}
	
	@Override 
	public String getSpecialDirectory() {
		return( "\\.svn" );
	}
	
	@Override
	public void syncFolderToVcs( String mirrorSubFolder , LocalFolder folder ) throws Exception {
		LocalFolder cf = getMirrorFolder();
		LocalFolder mf = cf.getSubFolder( action , mirrorSubFolder );
		
		super.syncFolderToVcsContent( mirrorSubFolder , folder );
		vcs.commitMasterFolder( mirror , mf , "" , "sync from source" );
	}
	
	@Override
	public void syncVcsToFolder( String mirrorFolder , LocalFolder folder ) throws Exception {
		super.syncVcsToFolderContent( mirrorFolder , folder );
	}
	
	public boolean checkBranchEmpty() throws Exception {
		LocalFolder branchFolder = getBranchFolder();
		List<String> dirs = new LinkedList<String>();
		List<String> files = new LinkedList<String>();
		branchFolder.getTopDirsAndFiles( action , dirs , files );
		
		if( !files.isEmpty() )
			return( false );
		
		if( !dirs.isEmpty() ) {
			if( !( dirs.size() == 1 && dirs.get(0).equals( ".svn" ) ) )
				return( false );
		}
		
		return( true );
	}
	
	private String getComponentBranchPath( String branch ) {
		String path = branch;
		if( !branch.equals( SubversionVCS.MASTERBRANCH ) )
			path = Common.getPath( "branches" , branch );
		return( path );
	}
	
	public boolean checkValidServerBranch( String branch ) throws Exception {
		String path = getComponentBranchPath( branch );
		return( vcsSubversion.isValidRepositoryMasterRootPath( mirror , path ) );
	}
	
	private void useRepositoryMirror() throws Exception {
		LocalFolder repo = getRepositoryFolder();
		if( repo.checkExists( action ) )
			return;
		
		if( !vcsSubversion.isValidRepositoryMasterRootPath( mirror , "/" ) )
			action.exit0( _Error.UnableCheckRepositoryPath0 , "Unable to find repository" );

		repo.ensureExists( action );
	}
	
	private String getBranch() {
		String branch = BRANCH;
		if( branch == null || branch.isEmpty() || branch.equals( GitVCS.MASTERBRANCH ) )
			branch = SubversionVCS.MASTERBRANCH;
		return( branch );
	}

}
