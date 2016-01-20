package ru.egov.urm.vcs;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.GitMirrorStorage;
import ru.egov.urm.storage.LocalFolder;

public class GitVCS extends GenericVCS {

	String MIRRORPATH;
	
	public GitVCS( ActionBase action , String MIRRORPATH ) {
		super( action );
		this.MIRRORPATH = MIRRORPATH;
	}

	public String getMainBranch() {
		return( "master" );
	}
	
	public boolean checkout( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );

		String REPOVERSION = "(branch head)";

		action.log( "git: checkout sources from " + CO_PATH + " (branch=" + BRANCH + ", revision=" + REPOVERSION + ") to " + PATCHFOLDER.folderPath + " ..." );
		createLocalFromBranch( CO_PATH , PATCHFOLDER , BRANCH );
		
		return( true );
	}

	public boolean commit( LocalFolder PATCHFOLDER , MetaSourceProject project , String MESSAGE ) throws Exception {
		if( !PATCHFOLDER.checkExists( action ) ) {
			action.log( "directory " + PATCHFOLDER.folderPath + " does not exist " );
			return( false );
		}

		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		// automatically add modified and push
		addModified( PATCHFOLDER );
		pushMirror( CO_PATH );
		
		return( true );
	}

	public boolean copyBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		if( !checkBranchExists( CO_PATH , BRANCH1 ) ) {
			action.log( CO_PATH + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( checkBranchExists( CO_PATH , BRANCH2 ) ) {
			action.log( "skip copy branch to branch - target branch already exists" );
			return( false );
		}

		copyMirrorBranchFromBranch( CO_PATH , BRANCH1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + BRANCH1 );
		pushMirror( CO_PATH );
		return( true );
	}

	public boolean renameBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		if( !checkBranchExists( CO_PATH , BRANCH1 ) ) {
			action.log( CO_PATH + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( checkBranchExists( CO_PATH , BRANCH2 ) ) {
			action.log( "skip rename branch to branch - target branch already exists" );
			return( false );
		}

		copyMirrorBranchFromBranch( CO_PATH , BRANCH1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: rename branch " + BRANCH1 + " to " + BRANCH2 );
		dropMirrorBranch( CO_PATH , BRANCH1 );
		pushMirror( CO_PATH );
		return( true );
	}

	public boolean copyTagToNewTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		if( !checkTagExists( CO_PATH , TAG1 ) ) {
			action.log( CO_PATH + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( checkTagExists( CO_PATH , TAG2 ) ) {
			action.log( CO_PATH + ": tag " + TAG2 + " exists" );
			return( false );
		}

		copyMirrorTagFromTag( CO_PATH , TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag from " + TAG1 );
		pushMirror( CO_PATH );
		return( true );
	}

	public boolean copyTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		if( !checkTagExists( CO_PATH , TAG1 ) ) {
			action.log( CO_PATH + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( checkTagExists( CO_PATH , TAG2 ) ) {
			// drop tag
			dropMirrorTag( CO_PATH , TAG2 );
			pushMirror( CO_PATH );
		}

		copyMirrorTagFromTag( CO_PATH , TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag " + TAG2 + " from " + TAG1 );
		pushMirror( CO_PATH );
		return( true );
	}

	public boolean renameTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		if( !checkTagExists( CO_PATH , TAG1 ) ) {
			action.log( CO_PATH + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( checkTagExists( CO_PATH , TAG2 ) ) {
			// drop tag
			dropMirrorTag( CO_PATH , TAG2 );
			pushMirror( CO_PATH );
		}

		copyMirrorTagFromTag( CO_PATH , TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: rename tag " + TAG1 + " to " + TAG2 );
		dropMirrorTag( CO_PATH , TAG1 );
		pushMirror( CO_PATH );
		return( true );
	}

	public boolean copyTagToNewBranch( MetaSourceProject project , String TAG1 , String BRANCH2 ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		if( !checkTagExists( CO_PATH , TAG1 ) ) {
			action.log( CO_PATH + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( checkBranchExists( CO_PATH , BRANCH2 ) ) {
			action.log( "skip copy branch to branch - target branch already exists" );
			return( false );
		}

		copyMirrorBranchFromTag( CO_PATH , TAG1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + TAG1 );
		pushMirror( CO_PATH );
		return( true );
	}

	public boolean dropTag( MetaSourceProject project , String TAG ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		if( !checkTagExists( CO_PATH , TAG ) ) {
			action.log( CO_PATH + ": tag " + TAG + " does not exist" );
			return( false );
		}
		
		// drop tag
		dropMirrorTag( CO_PATH , TAG );
		pushMirror( CO_PATH );
		return( true );
	}
	
	public boolean dropBranch( MetaSourceProject project , String BRANCH ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		if( !checkBranchExists( CO_PATH , BRANCH ) ) {
			action.log( CO_PATH + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		// drop branch
		dropMirrorBranch( CO_PATH , BRANCH );
		pushMirror( CO_PATH );
		return( true );
	}

	public boolean export( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH , String TAG , String FILENAME ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );
		
		boolean res;
		if( !TAG.isEmpty() )
			res = exportFromTag( CO_PATH , PATCHFOLDER , TAG , FILENAME );
		else
			res = exportFromBranch( CO_PATH , PATCHFOLDER , BRANCH , FILENAME );
		
		return( res );
	}

	public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String BRANCHDATE ) throws Exception {
		String CO_PATH = getRepoPath( project );
		refreshMirror( CO_PATH );

		String CO_BRANCH = BRANCH;
		if( CO_BRANCH.startsWith( "branches/" ) )
			CO_BRANCH = CO_BRANCH.substring( "branches/".length() );

		if( !checkBranchExists( CO_PATH , BRANCH ) ) {
			action.log( CO_PATH + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		setMirrorTag( CO_PATH , CO_BRANCH , TAG , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag" , BRANCHDATE );
		pushMirror( CO_PATH );
		return( true );
	}
	
	// implementation
	private String getRepoPath( MetaSourceProject project ) throws Exception {
		GitMirrorStorage storage = action.artefactory.getGitMirrorStorage( action , MIRRORPATH , project );
		return( storage.mirrorFolder.folderPath );
	}

	private String getRepoPath( String REPOSITORY ) throws Exception {
		GitMirrorStorage storage = action.artefactory.getGitMirrorStorage( action , MIRRORPATH , REPOSITORY );
		return( storage.mirrorFolder.folderPath );
	}

	private void refreshMirror( String CO_PATH ) throws Exception {
		session.customCheckStatus( action , "git -C " + CO_PATH + " fetch origin" );
	}

	private void createLocalFromBranch( String CO_PATH , LocalFolder PATCHFOLDER , String BRANCH ) throws Exception {
		session.customCheckStatus( action , "git -C " + CO_PATH + " clone " + CO_PATH + " --shared -b branch-" + BRANCH + " " + PATCHFOLDER.folderPath );
	}

	private boolean exportFromPath( String CO_PATH , LocalFolder PATCHFOLDER , String SUBPATH , String FILENAME ) throws Exception {
		LocalFolder BASEDIR = PATCHFOLDER.getParentFolder( action );
		String BASENAME = PATCHFOLDER.getBaseName( action );
		
		if( FILENAME.isEmpty() ) {
			if( !BASEDIR.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + BASEDIR + " does not exist" );
			if( PATCHFOLDER.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + PATCHFOLDER.folderPath + " should not exist" );
			
			PATCHFOLDER.ensureExists( action );
			session.customCheckStatus( action , "git -C " + CO_PATH + " archive " + SUBPATH + " . | ( cd " + PATCHFOLDER.folderPath + "; tar x )" );
		}
		else {
			if( !PATCHFOLDER.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + PATCHFOLDER.folderPath + " does not exist" );
			
			// export file or subdir
			int COMPS = Common.getDirCount( FILENAME );
			String STRIPOPTION = "--strip-components=" + COMPS;

			String FILEBASENAME = Common.getBaseName( FILENAME );
			String srcFile = BASEDIR.getFilePath( action , FILEBASENAME );
			if( ( !FILEBASENAME.equals( BASENAME ) ) && BASEDIR.checkFileExists( action , FILEBASENAME ) )
				action.exit( "exportFromPath: local file or directory " + srcFile + " already exists" );

			session.customCheckStatus( action , "git -C " + CO_PATH + " archive " + SUBPATH + " " + FILENAME + " | ( cd " + BASEDIR + "; tar x " + STRIPOPTION + " )" );
			if( !FILEBASENAME.equals( BASENAME ) )
				BASEDIR.moveFileToFolder( action , FILEBASENAME , BASENAME );
		}

		return( true );
	}

	private boolean exportFromBranch( String CO_PATH , LocalFolder PATCHFOLDER , String BRANCH , String FILENAME ) throws Exception {
		boolean res = exportFromPath( CO_PATH , PATCHFOLDER , "branch-" + BRANCH , FILENAME );
		return( res );
	}

	private boolean exportFromTag( String CO_PATH , LocalFolder PATCHFOLDER , String TAG , String FILENAME ) throws Exception {
		boolean res = exportFromPath( CO_PATH , PATCHFOLDER , "tag-" + TAG , FILENAME );
		return( res );
	}

	private void setMirrorTag( String CO_PATH , String BRANCH , String TAG , String MESSAGE , String TAGDATE ) throws Exception {
		// get revision by date
		String REVMARK = "";
		if( !TAGDATE.isEmpty() ) {
			REVMARK = session.customGetValue( action , "git -C " + CO_PATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + " refs/heads/branch-" + 
					BRANCH + " | tr -d " + Common.getQuoted( " " ) + " -f1" );
			if( REVMARK.isEmpty() )
				action.exit( "setMirrorTag: unable to find branch revision on given date" );
		}

		session.customCheckStatus( action , "git -C " + CO_PATH + " tag tag-" + TAG + " -a -f -m " + Common.getQuoted( "$P_MESSAGE" ) + " refs/heads/branch-" + BRANCH + " " + REVMARK );
	}

	private void dropMirrorTag( String CO_PATH , String TAG ) throws Exception {
		session.customCheckStatus( action , "git -C " + CO_PATH + " tag -d tag-" + TAG );
	}

	private void dropMirrorBranch( String CO_PATH , String BRANCH ) throws Exception {
		session.customCheckStatus( action , "git -C " + CO_PATH + " branch -D branch-" + BRANCH );
	}

	private boolean checkTagExists( String CO_PATH , String TAG ) throws Exception {
		String STATUS = session.customGetValue( action , "git -C " + CO_PATH + " tag -l tag-" + TAG );
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	private boolean checkBranchExists( String CO_PATH , String BRANCH ) throws Exception {
		String STATUS = session.customGetValue( action , "git -C " + CO_PATH + " branch --list branch-" + BRANCH );
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getMirrorTagStatus( String CO_PATH , String TAG ) throws Exception {
		if( !checkTagExists( CO_PATH , TAG ) )
			return( "" );

		String REPOVERSION = session.customGetValue( action , "git -C " + CO_PATH + " show --format=raw tag-" + TAG + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		return( REPOVERSION ); 
	}

	@SuppressWarnings("unused")
	private String getMirrorBranchStatus( String CO_PATH , String BRANCH ) throws Exception {
		if( !checkBranchExists( CO_PATH , BRANCH ) )
			return( "" );

		String REPOVERSION = session.customGetValue( action , "git -C " + CO_PATH + " show --format=raw branch-" + BRANCH + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		return( REPOVERSION ); 
	}

	private void copyMirrorTagFromTag( String CO_PATH , String TAG_FROM , String TAG_TO , String MESSAGE ) throws Exception {
		// drop if exists
		session.customCheckStatus( action , "git -C " + CO_PATH + " tag -a -f -m " + Common.getQuoted( MESSAGE ) + " tag-" + TAG_TO + " refs/tags/tag-" + TAG_FROM );
	}

	private void copyMirrorBranchFromTag( String CO_PATH , String TAG_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		session.customCheckStatus( action , "git -C " + CO_PATH + " branch branch-" + BRANCH_TO + " refs/tags/tag-" + TAG_FROM );
	}

	private void copyMirrorBranchFromBranch( String CO_PATH , String BRANCH_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		session.customCheckStatus( action , "git -C " + CO_PATH + " branch branch-" + BRANCH_TO + " refs/heads/branch-" + BRANCH_FROM );
	}

	private void pushMirror( String CO_PATH ) throws Exception {
		session.customCheckStatus( action , "git -C " + CO_PATH + " push origin" );
	}

	@SuppressWarnings("unused")
	private void refreshLocal( String CO_PATH ) throws Exception {
		session.customCheckStatus( action , "git -C " + CO_PATH + " fetch origin" );
	}

	@SuppressWarnings("unused")
	private void pushLocal( String LOCAL_PATH ) throws Exception {
		session.customCheckStatus( action , "git -C " + LOCAL_PATH + " push origin" );
	}
	
	private void addModified( LocalFolder PATCHFOLDER ) throws Exception {
		session.customCheckErrorsDebug( action , "( cd " + PATCHFOLDER.folderPath + "; " +
			"F_LIST=`git diff --name-only`; " +
			"if [ " + Common.getQuoted( "$F_LIST" ) + " != " + Common.getQuoted( "" ) + " ]; then git add $F_LIST; fi; " +
			"git commit -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: set version" ) + "; " +
			"git push origin 2>&1; if [ $? != 0 ]; then echo error on push origin >&2; fi )" );
	}

	public boolean isValidRepositoryTagPath( String repository , String TAG , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	public boolean isValidRepositoryMasterPath( String repository , String path ) throws Exception {
		String CO_PATH = getRepoPath( repository );
		refreshMirror( CO_PATH );
		
		int status = session.customGetStatus( action , "git -C " + CO_PATH + " cat-file -e master:" + path );
		if( status == 0 )
			return( true );
		
		return( false );
	}

	public boolean exportRepositoryTagPath( LocalFolder PATCHFOLDER , String repository , String TAG , String ITEMPATH , String name ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	public boolean exportRepositoryMasterPath( LocalFolder PATCHFOLDER , String repository , String ITEMPATH , String name ) throws Exception {
		if( !isValidRepositoryMasterPath( repository , ITEMPATH ) )
			return( false );
			
		if( !PATCHFOLDER.checkExists( action ) )
			action.exit( "exportRepositoryMasterPath: local directory " + PATCHFOLDER.folderPath + " does not exist" );

		String baseName = Common.getBaseName( ITEMPATH );
		if( PATCHFOLDER.checkPathExists( action , baseName ) )
			action.exit( "exportRepositoryMasterPath: local directory " + PATCHFOLDER.getFilePath( action , baseName ) + " should not exist" );
		
		String CO_PATH = getRepoPath( repository );
		refreshMirror( CO_PATH );
		
		session.customCheckStatus( action , "git -C " + CO_PATH + " archive " + ITEMPATH + " . | ( cd " + PATCHFOLDER.folderPath + "; tar x )" );
		if( name.isEmpty() == false && name.equals( baseName ) == false )
			PATCHFOLDER.renameFile( action , baseName , name );
		
		return( false );
	}

	public String getInfoMasterPath( String repository , String ITEMPATH ) throws Exception {
		String CO_PATH = "git:" + repository + ":" + ITEMPATH;
		return( CO_PATH );
	}
	
	public boolean createMasterFolder( String repository , String ITEMPATH , String commitMessage ) throws Exception {
		action.exit( "not implemented" );
		return( false );
	}
	
	public boolean moveMasterFiles( String repository , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception {
		action.exit( "not implemented" );
		return( false );
	}
	
	public String listMasterItems( String repository , String masterFolder ) throws Exception {
		String CO_PATH = getRepoPath( repository );
		refreshMirror( CO_PATH );
		String s = action.session.customGetValue( action , "git -C " + CO_PATH + " ls-tree master --name-only | tr \"\\n\" \" \"" );
		return( s );
	}

	public void deleteMasterFolder( String repository , String masterFolder , String commitMessage ) throws Exception {
		String CO_PATH = getRepoPath( repository );
		refreshMirror( CO_PATH );
		action.exitNotImplemented();
	}

	public void checkoutMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void importMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder , String commitMessage ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void ensureMasterFolderExists( String repository , String masterFolder , String commitMessage ) throws Exception {
		action.exitNotImplemented();
	}
	
	public boolean commitMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder , String commitMessage ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	public void addFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void deleteFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void addDirToCommit( LocalFolder PATCHPATH , String folder ) throws Exception {
		action.exitNotImplemented();
	}
	
	public void deleteDirToCommit( LocalFolder PATCHPATH , String folder ) throws Exception {
		action.exitNotImplemented();
	}

	public void createMasterTag( String repository , String masterFolder , String TAG , String commitMessage ) throws Exception {
		action.exitNotImplemented();
	}
	
}
