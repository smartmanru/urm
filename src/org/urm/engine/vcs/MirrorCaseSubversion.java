package org.urm.engine.vcs;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerMirrorRepository;

public class MirrorCaseSubversion extends MirrorCase {

	SubversionVCS vcsSubversion;
	
	public MirrorCaseSubversion( SubversionVCS vcs , ServerMirrorRepository mirror ) {
		super( vcs , mirror );
		this.vcsSubversion = vcs;
	}
	
	@Override
	public LocalFolder getRepositoryFolder() throws Exception {
		LocalFolder res = super.getResourceFolder();
		String folder = Common.getPath( mirror.RESOURCE_ROOT , mirror.RESOURCE_REPO + ".svn.branches" );
		return( res.getSubFolder( action , folder ) );
	}
	
	@Override
	public LocalFolder getBranchFolder() throws Exception {
		LocalFolder repo = getRepositoryFolder();
		String folder;
		if( mirror.BRANCH.isEmpty() )
			folder = SubversionVCS.MASTERBRANCH;
		else
			folder = mirror.BRANCH;
		
		return( repo.getSubFolder( action , folder ) );
	}
	
	@Override
	public LocalFolder getComponentFolder() throws Exception {
		LocalFolder branch = getBranchFolder();
		return( branch.getSubFolder( action , mirror.RESOURCE_DATA ) );
	}
	
	@Override
	public void createEmptyMirrorOnServer() throws Exception {
		useRepositoryMirror();
		useBranchMirror();
		
		LocalFolder comp = getComponentFolder();
		if( comp.checkExists( action ) ) {
			if( !checkCompEmpty() ) {
				String OSPATH = shell.getLocalPath( mirror.RESOURCE_DATA );
				action.exit1( _Error.MirrorDirectoryNotEmpty1 , "Target mirror folder is not empty - " + OSPATH , OSPATH );
			}
		}
		
		comp.ensureExists( action );
		vcs.addDirToCommit( mirror , getBranchFolder() , mirror.RESOURCE_DATA );
		pushComponentChanges();
	}

	@Override
	public void useMirror() throws Exception {
		useRepositoryMirror();
		useBranchMirror();
		
		LocalFolder comp = getComponentFolder();
		if( !comp.checkExists( action ) ) {
			String OSPATH = shell.getLocalPath( mirror.RESOURCE_DATA );
			action.exit1( _Error.MissingRepoMirrorDirectory1 , "Missing mirror repository directory: " + OSPATH , OSPATH );
		}
	}
	
	@Override
	public void dropMirror( boolean dropOnServer ) throws Exception {
	}

	@Override
	public void refreshRepository() throws Exception {
	}
	
	@Override
	public void refreshBranch( boolean refreshRepository ) throws Exception {
		LocalFolder branch = getBranchFolder();
		shell.customCheckStatus( action , branch.folderPath , "svn update " + vcsSubversion.SVNAUTH );
	}
	
	@Override
	public void refreshComponent( boolean refreshRepository ) throws Exception {
		if( refreshRepository )
			refreshBranch( true );
		else {
			LocalFolder comp = getComponentFolder();
			shell.customCheckStatus( action , comp.folderPath , "svn update " + vcsSubversion.SVNAUTH );
		}
	}
	
	@Override
	public void pushComponentChanges() throws Exception {
		String msg = "push to origin";
		LocalFolder comp = getComponentFolder();
		shell.customCheckStatus( action , comp.folderPath , "svn commit -m " + Common.getQuoted( msg ) + " " + vcsSubversion.SVNAUTH );
	}

	@Override 
	public boolean checkValidBranch() throws Exception {
		String branch = getBranch();
		return( checkValidBranch( branch ) );
	}
	
	@Override 
	public String getSpecialDirectory() {
		return( "\\.svn" );
	}
	
	public boolean checkCompEmpty() throws Exception {
		LocalFolder compFolder = getComponentFolder();
		List<String> dirs = new LinkedList<String>();
		List<String> files = new LinkedList<String>();
		compFolder.getTopDirsAndFiles( action , dirs , files );
		
		if( !files.isEmpty() )
			return( false );
		
		if( !dirs.isEmpty() ) {
			if( !( dirs.size() == 1 && dirs.get(0).equals( ".svn" ) ) )
				return( false );
		}
		
		return( true );
	}
	
	public boolean checkValidBranch( String branch ) throws Exception {
		String path = branch;
		if( !path.equals( SubversionVCS.MASTERBRANCH ) )
			path = Common.getPath( "branches" , branch );
		
		return( vcsSubversion.isValidRepositoryMasterRootPath( mirror , path ) );
	}
	
	private void useRepositoryMirror() throws Exception {
		LocalFolder repo = getRepositoryFolder();
		if( repo.checkExists( action ) ) {
			refreshRepository();
			return;
		}
		
		if( !vcs.isValidRepositoryMasterRootPath( mirror , "/" ) )
			action.exit0( _Error.UnableCheckRepositoryPath0 , "Unable to find repository" );

		repo.ensureExists( action );
	}
	
	private void useBranchMirror() throws Exception {
		LocalFolder branch = getBranchFolder();
		if( branch.checkExists( action ) ) {
			refreshBranch( false );
			return;
		}

		if( !checkValidBranch() ) {
			String BRANCH = getBranch();
			action.exit1( _Error.MissingRepoBranch1 , "Missing repository branch=" + BRANCH , BRANCH );
		}
		
		String remotePath = vcsSubversion.getRepositoryRootPath( mirror );
		String OSPATH = branch.getLocalPath( action );
		int status = shell.customGetStatus( action , "svn co --non-interactive " + vcsSubversion.SVNAUTH + " " + remotePath + " " + OSPATH );
		if( status != 0 )
			action.exit1( _Error.UnableCheckOut1 , "Having problem to check out " + remotePath , remotePath );
	}
	
	private String getBranch() {
		String branch = mirror.BRANCH;
		if( branch == null || branch.isEmpty() || branch.equals( GitVCS.MASTERBRANCH ) )
			branch = SubversionVCS.MASTERBRANCH;
		return( branch );
	}

}
