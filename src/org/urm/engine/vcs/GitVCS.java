package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaProductSettings;
import org.urm.engine.meta.MetaSourceProject;
import org.urm.engine.registry.ServerAuthResource;
import org.urm.engine.registry.ServerMirrorRepository;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;

public class GitVCS extends GenericVCS {

	static String MASTERBRANCH = "master";

	public GitVCS( ActionBase action , Meta meta , ServerAuthResource res , ShellExecutor shell ) {
		super( action , meta , res , shell );
	}

	@Override public String getMainBranch() {
		return( MASTERBRANCH );
	}
	
	@Override
	public boolean ignoreDir( String name ) {
		if( name.equals( ".git" ) )
			return( true );
		return( false );
	}
	
	@Override
	public boolean ignoreFile( String name ) {
		return( false );
	}
	
	@Override public boolean checkout( MetaSourceProject project , LocalFolder PATCHFOLDER , String BRANCH ) throws Exception {
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project , PATCHFOLDER );
		repo.refreshMirror();

		String REPOVERSION = "(branch head)";

		action.info( "git: checkout sources from " + repo.getBareOSPath() + " (branch=" + BRANCH + ", revision=" + REPOVERSION + ") to " + PATCHFOLDER.folderPath + " ..." );
		repo.createLocalFromBranch( BRANCH );
		
		return( true );
	}

	@Override public boolean commit( MetaSourceProject project , LocalFolder PATCHFOLDER , String MESSAGE ) throws Exception {
		if( !PATCHFOLDER.checkExists( action ) ) {
			action.error( "directory " + PATCHFOLDER.folderPath + " does not exist " );
			return( false );
		}

		GitProjectRepo repo = getRepo( project , PATCHFOLDER );
		repo.refreshMirror();
		
		// automatically add modified and push
		repo.addModified();
		repo.pushMirror();
		
		return( true );
	}

	@Override public boolean copyBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		BRANCH1 = getBranchName( BRANCH1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkBranchExists( BRANCH1 ) ) {
			action.error( project.PROJECT + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( repo.checkBranchExists( BRANCH2 ) ) {
			action.error( project.PROJECT + ": skip copy branch to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorBranchFromBranch( BRANCH1 , BRANCH2 , product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + BRANCH1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean renameBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		BRANCH1 = getBranchName( BRANCH1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkBranchExists( BRANCH1 ) ) {
			action.error( project.PROJECT + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( repo.checkBranchExists( BRANCH2 ) ) {
			action.error( project.PROJECT + ": cannot rename branch to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorBranchFromBranch( BRANCH1 , BRANCH2 , product.CONFIG_ADM_TRACKER + "-0000: rename branch " + BRANCH1 + " to " + BRANCH2 );
		repo.dropMirrorBranch( BRANCH1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean copyTagToNewTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( project.PROJECT + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkTagExists( TAG2 ) ) {
			action.error( project.PROJECT + ": tag " + TAG2 + " already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorTagFromTag( TAG1 , TAG2 , product.CONFIG_ADM_TRACKER + "-0000: create tag from " + TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean copyTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( project.PROJECT + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkTagExists( TAG2 ) ) {
			// drop tag
			repo.dropMirrorTag( TAG2 );
			repo.pushMirror();
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorTagFromTag( TAG1 , TAG2 , product.CONFIG_ADM_TRACKER + "-0000: create tag " + TAG2 + " from " + TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean renameTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( project.PROJECT + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkTagExists( TAG2 ) ) {
			// drop tag
			repo.dropMirrorTag( TAG2 );
			repo.pushMirror();
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorTagFromTag( TAG1 , TAG2 , product.CONFIG_ADM_TRACKER + "-0000: rename tag " + TAG1 + " to " + TAG2 );
		repo.dropMirrorTag( TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean copyTagToNewBranch( MetaSourceProject project , String TAG1 , String BRANCH2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( repo + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkBranchExists( BRANCH2 ) ) {
			action.error( project.PROJECT + ": cannot copy branch to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorBranchFromTag( TAG1 , BRANCH2 , product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean dropTag( MetaSourceProject project , String TAG ) throws Exception {
		TAG = getTagName( TAG );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG ) ) {
			action.error( project.PROJECT + ": tag " + TAG + " does not exist" );
			return( false );
		}
		
		// drop tag
		repo.dropMirrorTag( TAG );
		repo.pushMirror();
		return( true );
	}
	
	@Override public boolean dropBranch( MetaSourceProject project , String BRANCH ) throws Exception {
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkBranchExists( BRANCH ) ) {
			action.error( project.PROJECT + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		// drop branch
		repo.dropMirrorBranch( BRANCH );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean export( MetaSourceProject project , LocalFolder PATCHFOLDER , String BRANCH , String TAG , String FILENAME ) throws Exception {
		TAG = getTagName( TAG );
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		boolean res;
		String FILEPATH = project.CODEPATH;
		String FILEBASE = "";
		if( !FILENAME.isEmpty() ) {
			FILEPATH = Common.getPath( FILEPATH , Common.getDirName( FILENAME ) );
			FILEBASE = Common.getBaseName( FILENAME );
		}
		if( !TAG.isEmpty() )
			res = repo.exportFromTag( TAG , FILEPATH , FILEBASE );
		else
			res = repo.exportFromBranch( BRANCH , FILEPATH , FILEBASE );
		
		return( res );
	}

	@Override public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String BRANCHDATE ) throws Exception {
		TAG = getTagName( TAG );
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();

		String CO_BRANCH = BRANCH;
		if( CO_BRANCH.startsWith( "branches/" ) )
			CO_BRANCH = CO_BRANCH.substring( "branches/".length() );

		if( !repo.checkBranchExists( BRANCH ) ) {
			action.error( project.PROJECT + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		MetaProductSettings product = meta.getProductSettings( action );
		repo.setMirrorTag( CO_BRANCH , TAG , product.CONFIG_ADM_TRACKER + "-0000: create tag" , BRANCHDATE );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean isValidRepositoryTagPath( ServerMirrorRepository mirror , String TAG , String path ) throws Exception {
		TAG = getTagName( TAG );
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public boolean isValidRepositoryMasterPath( ServerMirrorRepository mirror , String path ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , null );
		storage.refreshMirror();
		
		int status;
		String OSPATH = storage.getBareOSPath();
		String OSPATHDIR = shell.getOSPath( action , path );
		status = shell.customGetStatus( action , "git -C " + OSPATH + " cat-file -e master:" + OSPATHDIR );
		
		if( status == 0 )
			return( true );
		
		return( false );
	}

	@Override public boolean exportRepositoryTagPath( ServerMirrorRepository mirror , LocalFolder PATCHFOLDER , String TAG , String ITEMPATH , String name ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , PATCHFOLDER );
		storage.refreshMirror();
		
		TAG = getTagName( TAG );
		boolean res = storage.exportFromPath( TAG , name , ITEMPATH );
		return( res );
	}
	
	@Override public boolean exportRepositoryMasterPath( ServerMirrorRepository mirror , LocalFolder PATCHFOLDER , String ITEMPATH , String name ) throws Exception {
		if( !isValidRepositoryMasterPath( mirror , ITEMPATH ) )
			return( false );
			
		if( !PATCHFOLDER.checkExists( action ) )
			action.exit1( _Error.MissingLocalDirectory1 , "local directory " + PATCHFOLDER.folderPath + " does not exist" , PATCHFOLDER.folderPath );

		String baseName = Common.getBaseName( ITEMPATH );
		if( PATCHFOLDER.checkPathExists( action , baseName ) ) {
			String path = PATCHFOLDER.getFilePath( action , baseName );
			action.exit1( _Error.LocalDirectoryShouldNotExist1 , "local directory " + path + " should not exist" , path );
		}
		
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , PATCHFOLDER );
		storage.refreshMirror();
		
		String OSPATH = storage.getBareOSPath();
		if( shell.isWindows() ) {
			String WINPATHDIR = Common.getWinPath( ITEMPATH );
			String WINPATHPATCH = Common.getWinPath( PATCHFOLDER.folderPath );
			shell.customCheckStatus( action , "git -C " + OSPATH + " archive " + WINPATHDIR + " . | ( cd /D " + WINPATHPATCH + " & tar x --exclude pax_global_header)" );
		}
		else {
			shell.customCheckStatus( action , "git -C " + OSPATH + " archive " + ITEMPATH + " . | ( cd " + PATCHFOLDER.folderPath + "; tar x )" );
		}
		
		if( name.isEmpty() == false && name.equals( baseName ) == false )
			PATCHFOLDER.renameFile( action , baseName , name );
		
		return( false );
	}

	@Override public String getInfoMasterPath( ServerMirrorRepository mirror , String ITEMPATH ) throws Exception {
		String CO_PATH = "git:" + mirror.NAME + ":" + ITEMPATH;
		return( CO_PATH );
	}
	
	@Override public boolean createMasterFolder( ServerMirrorRepository mirror , String ITEMPATH , String commitMessage ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public boolean moveMasterFiles( ServerMirrorRepository mirror , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public String[] listMasterItems( ServerMirrorRepository mirror , String masterFolder ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , null );
		storage.refreshBare();
		
		String s;
		String OSPATH = storage.getBareOSPath();
		if( shell.isWindows() ) {
			s = shell.customGetValue( action , "git -C " + OSPATH + " ls-tree master --name-only" );
			s = Common.replace( s , "\\n" , " \"" );
		}
		else {
			s = shell.customGetValue( action , "git -C " + OSPATH + " ls-tree master --name-only | tr \"\\n\" \" \"" );
		}
		return( Common.splitSpaced( s ) );
	}

	@Override public void deleteMasterFolder( ServerMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , null );
		storage.refreshMirror();
		action.exitNotImplemented();
	}

	@Override public void checkoutMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , PATCHPATH );
		storage.refreshMirror();
		action.exitNotImplemented();
	}
	
	@Override public void importMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , PATCHPATH );
		storage.refreshMirror();
		action.exitNotImplemented();
	}
	
	@Override public void ensureMasterFolderExists( ServerMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , null );
		storage.refreshMirror();
		action.exitNotImplemented();
	}
	
	@Override public boolean commitMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception {
		String folder = PATCHPATH.getFilePath( action , masterFolder );
		int status = shell.customGetStatus( action , folder , "git commit -m " + Common.getQuoted( commitMessage ) );
		if( status != 0 )
			return( false );
		
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , PATCHPATH );
		LocalFolder storageFolder = storage.getCommitFolder();
		if( !PATCHPATH.equals( storageFolder ) )
			storage.pushOrigin( PATCHPATH.folderPath );
		
		storage.pushOrigin( storageFolder.folderPath );
		return( true );
	}
	
	@Override public void addFileToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		String path = PATCHPATH.getFilePath( action , folder );
		String filePath = file;
		if( PATCHPATH.windows )
			filePath = Common.getWinPath( filePath );
		shell.customCheckStatus( action , path , "git add " + filePath );
	}
	
	@Override public void deleteFileToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void addDirToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void deleteDirToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void createMasterTag( ServerMirrorRepository mirror , String masterFolder , String TAG , String commitMessage ) throws Exception {
		TAG = getTagName( TAG );
		action.exitNotImplemented();
	}

	@Override
	public boolean checkMirrorEmpty( ServerMirrorRepository mirror ) throws Exception {
		String[] items = listMasterItems( mirror , mirror.RESOURCE_DATA );
		if( items.length == 0 || ( items.length == 1 && items[0].equals( "README.md" ) ) )
			return( true );
		return( false );
	}

	@Override
	public MirrorStorage createInitialMirror( ServerMirrorRepository mirror ) throws Exception {
		GitMirrorStorage storage = new GitMirrorStorage( this , mirror , null );
		storage.createLocalMirror();
		return( storage );
	}

	@Override
	public MirrorStorage createServerMirror( ServerMirrorRepository mirror ) throws Exception {
		GitMirrorStorage storage = new GitMirrorStorage( this , mirror , null );
		storage.createServerMirror();
		return( storage );
	}

	@Override
	public void dropMirror( ServerMirrorRepository mirror ) throws Exception {
		GitMirrorStorage storage = new GitMirrorStorage( this , mirror , null );
		storage.useProjectMirror( false );
		storage.removeLocalMirror();
	}
	
	@Override
	public void pushMirror( ServerMirrorRepository mirror ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , null );
		storage.pushMirror();
	}
	
	@Override
	public void refreshMirror( ServerMirrorRepository mirror ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , null );
		storage.refreshMirror();
	}

	@Override
	public MirrorStorage getMirror( ServerMirrorRepository mirror ) throws Exception {
		GitMirrorStorage storage = getMasterMirrorStorage( mirror , null );
		return( storage );
	}
	
	// implementation
	private GitProjectRepo getRepo( MetaSourceProject project , LocalFolder PATCHFOLDER ) throws Exception {
		ServerMirrorRepository mirror = action.getProjectMirror( project );
		GitProjectRepo repo = new GitProjectRepo( this , mirror , project , PATCHFOLDER );
		repo.useProjectMirror( true );
		return( repo );
	}

	private GitMirrorStorage getMasterMirrorStorage( ServerMirrorRepository mirror , LocalFolder PATCHFOLDER ) throws Exception {
		GitMirrorStorage storage = new GitMirrorStorage( this , mirror , PATCHFOLDER );
		storage.useProjectMirror( false );
		return( storage );
	}
	
	private String getBranchName( String BRANCH ) {
		if( BRANCH.isEmpty() )
			return( "" );
		
		if( BRANCH.equals( MASTERBRANCH ) )
			return( MASTERBRANCH );
		return( "branch-" + BRANCH );
	}
	
	private String getTagName( String TAG ) {
		if( TAG.isEmpty() )
			return( "" );
		
		return( "tag-" + TAG );
	}
	
}
