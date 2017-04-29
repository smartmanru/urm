package org.urm.engine.vcs;

import java.net.URLEncoder;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerMirrorRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProject;

public class GitVCS extends GenericVCS {

	static String MASTERBRANCH = "master";

	public GitVCS( ActionBase action , Meta meta , ServerAuthResource res , ShellExecutor shell ) {
		super( action , meta , res , shell );
	}

	@Override
	public MirrorCase getMirror( ServerMirrorRepository mirror ) throws Exception {
		return( new MirrorCaseGit( this , mirror , "" ) );
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
	
	@Override 
	public boolean checkout( MetaSourceProject project , LocalFolder PATCHFOLDER , String BRANCH ) throws Exception {
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();

		String REPOVERSION = "(branch head)";

		action.info( "git: checkout sources from " + repo.getBareOSPath() + " (branch=" + BRANCH + ", revision=" + REPOVERSION + ") to " + PATCHFOLDER.folderPath + " ..." );
		repo.createLocalFromBranch( PATCHFOLDER , BRANCH );
		
		return( true );
	}

	@Override 
	public boolean commit( MetaSourceProject project , LocalFolder PATCHFOLDER , String MESSAGE ) throws Exception {
		if( !PATCHFOLDER.checkExists( action ) ) {
			action.error( "directory " + PATCHFOLDER.folderPath + " does not exist " );
			return( false );
		}

		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		// automatically add modified and push
		repo.addModified( PATCHFOLDER );
		repo.pushMirror();
		
		return( true );
	}

	@Override 
	public boolean copyBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		BRANCH1 = getBranchName( BRANCH1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		if( !repo.checkBranchExists( BRANCH1 ) ) {
			action.error( project.NAME + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( repo.checkBranchExists( BRANCH2 ) ) {
			action.error( project.NAME + ": skip copy branch to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorBranchFromBranch( BRANCH1 , BRANCH2 , product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + BRANCH1 );
		repo.pushMirror();
		return( true );
	}

	@Override 
	public boolean renameBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		BRANCH1 = getBranchName( BRANCH1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		if( !repo.checkBranchExists( BRANCH1 ) ) {
			action.error( project.NAME + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( repo.checkBranchExists( BRANCH2 ) ) {
			action.error( project.NAME + ": cannot rename branch to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorBranchFromBranch( BRANCH1 , BRANCH2 , product.CONFIG_ADM_TRACKER + "-0000: rename branch " + BRANCH1 + " to " + BRANCH2 );
		repo.dropMirrorBranch( BRANCH1 );
		repo.pushMirror();
		return( true );
	}

	@Override 
	public boolean copyTagToNewTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( project.NAME + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkTagExists( TAG2 ) ) {
			action.error( project.NAME + ": tag " + TAG2 + " already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorTagFromTag( TAG1 , TAG2 , product.CONFIG_ADM_TRACKER + "-0000: create tag from " + TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override 
	public boolean copyTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( project.NAME + ": tag " + TAG1 + " does not exist" );
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

	@Override 
	public boolean renameTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( project.NAME + ": tag " + TAG1 + " does not exist" );
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

	@Override 
	public boolean copyTagToNewBranch( MetaSourceProject project , String TAG1 , String BRANCH2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( repo + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkBranchExists( BRANCH2 ) ) {
			action.error( project.NAME + ": cannot copy branch to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		repo.copyMirrorBranchFromTag( TAG1 , BRANCH2 , product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override 
	public boolean dropTag( MetaSourceProject project , String TAG ) throws Exception {
		TAG = getTagName( TAG );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG ) ) {
			action.error( project.NAME + ": tag " + TAG + " does not exist" );
			return( false );
		}
		
		// drop tag
		repo.dropMirrorTag( TAG );
		repo.pushMirror();
		return( true );
	}
	
	@Override 
	public boolean dropBranch( MetaSourceProject project , String BRANCH ) throws Exception {
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		if( !repo.checkBranchExists( BRANCH ) ) {
			action.error( project.NAME + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		// drop branch
		repo.dropMirrorBranch( BRANCH );
		repo.pushMirror();
		return( true );
	}

	@Override 
	public boolean export( MetaSourceProject project , LocalFolder PATCHFOLDER , String BRANCH , String TAG , String FILENAME ) throws Exception {
		TAG = getTagName( TAG );
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();
		
		boolean res;
		String FILEPATH = project.CODEPATH;
		String FILEBASE = "";
		if( !FILENAME.isEmpty() ) {
			FILEPATH = Common.getPath( FILEPATH , Common.getDirName( FILENAME ) );
			FILEBASE = Common.getBaseName( FILENAME );
		}
		if( !TAG.isEmpty() )
			res = repo.exportFromTag( PATCHFOLDER , TAG , FILEPATH , FILEBASE );
		else
			res = repo.exportFromBranch( PATCHFOLDER , BRANCH , FILEPATH , FILEBASE );
		
		return( res );
	}

	@Override 
	public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String BRANCHDATE ) throws Exception {
		TAG = getTagName( TAG );
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project );
		repo.refreshMirror();

		String CO_BRANCH = BRANCH;
		if( CO_BRANCH.startsWith( "branches/" ) )
			CO_BRANCH = CO_BRANCH.substring( "branches/".length() );

		if( !repo.checkBranchExists( BRANCH ) ) {
			action.error( project.NAME + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		MetaProductSettings product = meta.getProductSettings( action );
		repo.setMirrorTag( CO_BRANCH , TAG , product.CONFIG_ADM_TRACKER + "-0000: create tag" , BRANCHDATE );
		repo.pushMirror();
		return( true );
	}

	@Override 
	public boolean isValidRepositoryTagPath( ServerMirrorRepository mirror , String TAG , String path ) throws Exception {
		TAG = getTagName( TAG );
		action.exitNotImplemented();
		return( false );
	}
	
	@Override 
	public boolean isValidRepositoryMasterRootPath( ServerMirrorRepository mirror , String path ) throws Exception {
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		
		int status;
		String OSPATH = mc.getBareOSPath();
		String OSPATHDIR = shell.getOSPath( action , path );
		status = shell.customGetStatus( action , "git -C " + OSPATH + " cat-file -e master:" + OSPATHDIR );
		
		if( status == 0 )
			return( true );
		
		return( false );
	}

	@Override 
	public boolean isValidRepositoryMasterPath( ServerMirrorRepository mirror , String path ) throws Exception {
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		
		int status;
		String OSPATH = mc.getBareOSPath();
		String OSPATHDIR = shell.getOSPath( action , Common.getPath( mirror.RESOURCE_DATA , path ) );
		status = shell.customGetStatus( action , "git -C " + OSPATH + " cat-file -e master:" + OSPATHDIR );
		
		if( status == 0 )
			return( true );
		
		return( false );
	}

	@Override 
	public boolean exportRepositoryTagPath( ServerMirrorRepository mirror , LocalFolder PATCHFOLDER , String TAG , String ITEMPATH , String name ) throws Exception {
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		
		TAG = getTagName( TAG );
		boolean res = mc.exportFromPath( PATCHFOLDER , TAG , name , ITEMPATH );
		return( res );
	}
	
	@Override 
	public boolean exportRepositoryMasterPath( ServerMirrorRepository mirror , LocalFolder PATCHFOLDER , String ITEMPATH , String name ) throws Exception {
		if( !isValidRepositoryMasterPath( mirror , ITEMPATH ) )
			return( false );
			
		if( !PATCHFOLDER.checkExists( action ) )
			action.exit1( _Error.MissingLocalDirectory1 , "local directory " + PATCHFOLDER.folderPath + " does not exist" , PATCHFOLDER.folderPath );

		String baseName = Common.getBaseName( ITEMPATH );
		if( PATCHFOLDER.checkPathExists( action , baseName ) ) {
			String path = PATCHFOLDER.getFilePath( action , baseName );
			action.exit1( _Error.LocalDirectoryShouldNotExist1 , "local directory " + path + " should not exist" , path );
		}
		
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		
		String OSPATH = mc.getBareOSPath();
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

	@Override 
	public String getInfoMasterPath( ServerMirrorRepository mirror , String ITEMPATH ) throws Exception {
		String CO_PATH = "git:" + mirror.NAME + ":" + ITEMPATH;
		return( CO_PATH );
	}
	
	@Override 
	public boolean createMasterFolder( ServerMirrorRepository mirror , String ITEMPATH , String commitMessage ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override 
	public boolean moveMasterFiles( ServerMirrorRepository mirror , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override 
	public String[] listMasterItems( ServerMirrorRepository mirror , String masterFolder ) throws Exception {
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		
		String checkPath = masterFolder;
		if( masterFolder == null || masterFolder.equals( "/" ) )
			checkPath = "";
		
		String s;
		String OSPATH = mc.getBareOSPath();
		if( shell.isWindows() ) {
			s = shell.customGetValue( action , "git -C " + OSPATH + " ls-tree master " + checkPath + " --name-only" );
			s = Common.replace( s , "\\n" , " \"" );
		}
		else {
			s = shell.customGetValue( action , "git -C " + OSPATH + " ls-tree master " + checkPath + " --name-only | tr \"\\n\" \" \"" );
		}
		return( Common.splitSpaced( s ) );
	}

	@Override 
	public void deleteMasterFolder( ServerMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception {
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		action.exitNotImplemented();
	}

	@Override 
	public void checkoutMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder ) throws Exception {
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		action.exitNotImplemented();
	}
	
	@Override 
	public void importMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception {
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		action.exitNotImplemented();
	}
	
	@Override 
	public void ensureMasterFolderExists( ServerMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception {
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.refreshRepository();
		action.exitNotImplemented();
	}
	
	@Override 
	public boolean commitMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception {
		String folder = PATCHPATH.getFilePath( action , masterFolder );
		int status = shell.customGetStatus( action , folder , "git commit -m " + Common.getQuoted( commitMessage ) );
		if( status != 0 )
			return( false );
		
		MirrorCaseGit mc = getMasterMirrorCase( mirror );
		mc.pushMirror();
		return( true );
	}
	
	@Override 
	public void addFileToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		String path = PATCHPATH.getFilePath( action , folder );
		String filePath = file;
		if( PATCHPATH.windows )
			filePath = Common.getWinPath( filePath );
		shell.customCheckStatus( action , path , "git add " + filePath );
	}
	
	@Override 
	public void deleteFileToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		String path = PATCHPATH.getFilePath( action , folder );
		String filePath = file;
		if( PATCHPATH.windows )
			filePath = Common.getWinPath( filePath );
		shell.customCheckStatus( action , path , "git rm " + filePath );
	}
	
	@Override 
	public void addDirToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception {
		String path = PATCHPATH.getFilePath( action , folder );
		if( PATCHPATH.windows )
			path = Common.getWinPath( path );
		shell.customCheckStatus( action , path , "git add " + path );
	}
	
	@Override 
	public void deleteDirToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception {
		String path = PATCHPATH.getFilePath( action , folder );
		if( PATCHPATH.windows )
			path = Common.getWinPath( path );
		shell.customCheckStatus( action , path , "git rm -rf " + path );
	}

	@Override 
	public void createMasterTag( ServerMirrorRepository mirror , String masterFolder , String TAG , String commitMessage ) throws Exception {
		TAG = getTagName( TAG );
		action.exitNotImplemented();
	}

	@Override
	public boolean verifyRepository( String repo , String pathToRepo ) {
		try {
			String url = getRepositoryAuthUrl();
			if( !pathToRepo.isEmpty() )
				url += "/" + pathToRepo;
			url += "/" + repo;
			int status = shell.customGetStatus( action , "git ls-remote -h " + url + " master" );
			if( status == 0 )
				return( true );
		}
		catch( Throwable e ) {
			action.log( "verify repository" , e );
		}
		return( false );
	}
	
	// implementation
	public String getRepositoryAuthUrl() throws Exception {
		String url = res.BASEURL;
		
		String urlAuth = url;
		String user = "";
		if( !res.ac.isAnonymous() ) {
			user = res.ac.getUser( action );
			String userEncoded = URLEncoder.encode( user , "UTF-8" );
			String password = URLEncoder.encode( res.ac.getPassword( action ) , "UTF-8" );
			urlAuth = Common.getPartBeforeFirst( url , "//" ) + "//" + userEncoded + ":" + password + "@" + Common.getPartAfterFirst( url , "//" );
		}
		
		return( urlAuth );
	}
	
	private GitProjectRepo getRepo( MetaSourceProject project ) throws Exception {
		ServerMirrorRepository mirror = action.getProjectMirror( project );
		String BRANCH = project.getDefaultBranch( action );
		GitProjectRepo repo = new GitProjectRepo( this , mirror , project , BRANCH );
		return( repo );
	}

	private MirrorCaseGit getMasterMirrorCase( ServerMirrorRepository mirror ) throws Exception {
		MirrorCaseGit mc = new MirrorCaseGit( this , mirror , "" );
		return( mc );
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
