package org.urm.server.vcs;

import java.net.URLEncoder;

import org.urm.common.Common;
import org.urm.server.ServerAuthResource;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.Folder;
import org.urm.server.storage.LocalFolder;

public class GitVCS extends GenericVCS {

	static String MASTERBRANCH = "master";

	public GitVCS( ActionBase action , ServerAuthResource res , ShellExecutor shell ) {
		super( action , res , shell );
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
	
	@Override public String getMainBranch() {
		return( MASTERBRANCH );
	}
	
	@Override public boolean checkout( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH ) throws Exception {
		BRANCH = getBranchName( BRANCH );
		GitProjectRepo repo = getRepo( project , PATCHFOLDER );
		repo.refreshMirror();

		String REPOVERSION = "(branch head)";

		action.info( "git: checkout sources from " + repo.MIRRORPATH + " (branch=" + BRANCH + ", revision=" + REPOVERSION + ") to " + PATCHFOLDER.folderPath + " ..." );
		repo.createLocalFromBranch( BRANCH );
		
		return( true );
	}

	@Override public boolean commit( LocalFolder PATCHFOLDER , MetaSourceProject project , String MESSAGE ) throws Exception {
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
			action.error( repo.MIRRORPATH + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( repo.checkBranchExists( BRANCH2 ) ) {
			action.error( "skip copy branch to branch - target branch already exists" );
			return( false );
		}

		repo.copyMirrorBranchFromBranch( BRANCH1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + BRANCH1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean renameBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		BRANCH1 = getBranchName( BRANCH1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkBranchExists( BRANCH1 ) ) {
			action.error( repo.MIRRORPATH + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( repo.checkBranchExists( BRANCH2 ) ) {
			action.error( "cannot rename branch to branch - target branch already exists" );
			return( false );
		}

		repo.copyMirrorBranchFromBranch( BRANCH1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: rename branch " + BRANCH1 + " to " + BRANCH2 );
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
			action.error( repo.MIRRORPATH + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkTagExists( TAG2 ) ) {
			action.error( repo.MIRRORPATH + ": tag " + TAG2 + " already exists" );
			return( false );
		}

		repo.copyMirrorTagFromTag( TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag from " + TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean copyTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( repo.MIRRORPATH + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkTagExists( TAG2 ) ) {
			// drop tag
			repo.dropMirrorTag( TAG2 );
			repo.pushMirror();
		}

		repo.copyMirrorTagFromTag( TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag " + TAG2 + " from " + TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean renameTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG1 ) ) {
			action.error( repo + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( repo.checkTagExists( TAG2 ) ) {
			// drop tag
			repo.dropMirrorTag( TAG2 );
			repo.pushMirror();
		}

		repo.copyMirrorTagFromTag( TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: rename tag " + TAG1 + " to " + TAG2 );
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
			action.error( "cannot copy branch to branch - target branch already exists" );
			return( false );
		}

		repo.copyMirrorBranchFromTag( TAG1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + TAG1 );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean dropTag( MetaSourceProject project , String TAG ) throws Exception {
		TAG = getTagName( TAG );
		GitProjectRepo repo = getRepo( project , null );
		repo.refreshMirror();
		
		if( !repo.checkTagExists( TAG ) ) {
			action.error( repo.MIRRORPATH + ": tag " + TAG + " does not exist" );
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
			action.error( repo.MIRRORPATH + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		// drop branch
		repo.dropMirrorBranch( BRANCH );
		repo.pushMirror();
		return( true );
	}

	@Override public boolean export( Folder PATCHFOLDER , MetaSourceProject project , String BRANCH , String TAG , String FILENAME ) throws Exception {
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
			res = repo.exportFromTag( PATCHFOLDER , TAG , FILEPATH , FILEBASE );
		else
			res = repo.exportFromBranch( PATCHFOLDER , BRANCH , FILEPATH , FILEBASE );
		
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
			action.error( repo.MIRRORPATH + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		repo.setMirrorTag( CO_BRANCH , TAG , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag" , BRANCHDATE );
		repo.pushMirror();
		return( true );
	}

	public GitMirrorStorage getStorage( String REPOSITORY , boolean bare , boolean newStorage ) throws Exception {
		String REPOFOLDER = getStorageFolder( REPOSITORY , bare );
		GitMirrorStorage storage = new GitMirrorStorage( this , REPOFOLDER , bare );
		storage.create( action , newStorage );
		return( storage );
	}
	
	public GitMirrorStorage createStorage( String REPOSITORY , String reponame , String reporoot , boolean bare ) throws Exception {
		GitMirrorStorage storage = getStorage( REPOSITORY , bare , true );
		storage.createLocalMirror( action , REPOSITORY , reponame , reporoot , bare );
		return( storage );
	}
	
	public void removeStorage( String REPOSITORY , boolean bare ) throws Exception {
		String REPOFOLDER = getStorageFolder( REPOSITORY , bare );
		GitMirrorStorage storage = new GitMirrorStorage( this , REPOFOLDER , bare );
		storage.remove( action );
	}
	
	public void pushStorage( String REPOSITORY , boolean bare ) throws Exception {
		GitMirrorStorage storage = getStorage( REPOSITORY , bare , false );
		pushOrigin( storage.getStorageFolder( action ).folderPath );
	}
	
	public void refreshStorage( String REPOSITORY , boolean bare ) throws Exception {
		GitMirrorStorage storage = getStorage( REPOSITORY , bare , false );
		fetchOrigin( storage.getStorageFolder( action ).folderPath );
	}
	
	// implementation
	private String getStorageFolder( String REPOSITORY , boolean bare ) {
		String REPOFOLDER = res.NAME + "-" + REPOSITORY + ".git";
		if( bare )
			REPOFOLDER += ".mirror";
		return( REPOFOLDER );
	}
	
	private GitProjectRepo getRepo( MetaSourceProject project , Folder PATCHFOLDER ) throws Exception {
		GitMirrorStorage storage = getStorage( project.REPOSITORY , true , false );
		GitProjectRepo repo = new GitProjectRepo( action , shell , project , storage , PATCHFOLDER );
		return( repo );
	}

	private GitProjectRepo getRepo( String REPOSITORY ) throws Exception {
		GitMirrorStorage storage = getStorage( REPOSITORY , true , false );
		GitProjectRepo repo = new GitProjectRepo( action , shell , null , storage , null );
		return( repo );
	}

	@Override public boolean isValidRepositoryTagPath( String repository , String TAG , String path ) throws Exception {
		TAG = getTagName( TAG );
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public boolean isValidRepositoryMasterPath( String repository , String path ) throws Exception {
		GitProjectRepo repo = getRepo( repository );
		repo.refreshMirror();
		
		int status;
		String OSPATH = shell.getOSPath( action , repo.MIRRORPATH );
		String OSPATHDIR = shell.getOSPath( action , path );
		status = shell.customGetStatus( action , "git -C " + OSPATH + " cat-file -e master:" + OSPATHDIR );
		
		if( status == 0 )
			return( true );
		
		return( false );
	}

	@Override public boolean exportRepositoryTagPath( LocalFolder PATCHFOLDER , String repository , String TAG , String ITEMPATH , String name ) throws Exception {
		GitProjectRepo repo = getRepo( repository );
		repo.refreshMirror();
		
		TAG = getTagName( TAG );
		boolean res = repo.exportFromPath( PATCHFOLDER.getSubFolder( action , name ) , TAG , ITEMPATH , "" );
		return( res );
	}
	
	@Override public boolean exportRepositoryMasterPath( LocalFolder PATCHFOLDER , String repository , String ITEMPATH , String name ) throws Exception {
		if( !isValidRepositoryMasterPath( repository , ITEMPATH ) )
			return( false );
			
		if( !PATCHFOLDER.checkExists( action ) )
			action.exit( "exportRepositoryMasterPath: local directory " + PATCHFOLDER.folderPath + " does not exist" );

		String baseName = Common.getBaseName( ITEMPATH );
		if( PATCHFOLDER.checkPathExists( action , baseName ) )
			action.exit( "exportRepositoryMasterPath: local directory " + PATCHFOLDER.getFilePath( action , baseName ) + " should not exist" );
		
		GitProjectRepo repo = getRepo( repository );
		repo.refreshMirror();
		
		if( shell.isWindows() ) {
			String WINPATH = Common.getWinPath( repo.MIRRORPATH );
			String WINPATHDIR = Common.getWinPath( ITEMPATH );
			String WINPATHPATCH = Common.getWinPath( PATCHFOLDER.folderPath );
			shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + WINPATHDIR + " . | ( cd /D " + WINPATHPATCH + " & tar x --exclude pax_global_header)" );
		}
		else {
			shell.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " archive " + ITEMPATH + " . | ( cd " + PATCHFOLDER.folderPath + "; tar x )" );
		}
		
		if( name.isEmpty() == false && name.equals( baseName ) == false )
			PATCHFOLDER.renameFile( action , baseName , name );
		
		return( false );
	}

	@Override public String getInfoMasterPath( String repository , String ITEMPATH ) throws Exception {
		String CO_PATH = "git:" + repository + ":" + ITEMPATH;
		return( CO_PATH );
	}
	
	@Override public boolean createMasterFolder( String repository , String ITEMPATH , String commitMessage ) throws Exception {
		action.exit( "not implemented" );
		return( false );
	}
	
	@Override public boolean moveMasterFiles( String repository , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception {
		action.exit( "not implemented" );
		return( false );
	}
	
	@Override public String[] listMasterItems( String repository , String masterFolder ) throws Exception {
		GitProjectRepo repo = getRepo( repository );
		repo.refreshMirror();
		
		String s;
		if( shell.isWindows() ) {
			String WINPATH = Common.getWinPath( repo.MIRRORPATH );
			s = shell.customGetValue( action , "git -C " + WINPATH + " ls-tree master --name-only" );
			s = Common.replace( s , "\\n" , " \"" );
		}
		else {
			s = shell.customGetValue( action , "git -C " + repo.MIRRORPATH + " ls-tree master --name-only | tr \"\\n\" \" \"" );
		}
		return( Common.splitSpaced( s ) );
	}

	@Override public void deleteMasterFolder( String repository , String masterFolder , String commitMessage ) throws Exception {
		GitProjectRepo repo = getRepo( repository );
		repo.refreshMirror();
		action.exitNotImplemented();
	}

	@Override public void checkoutMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void importMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder , String commitMessage ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void ensureMasterFolderExists( String repository , String masterFolder , String commitMessage ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public boolean commitMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder , String commitMessage ) throws Exception {
		String folder = PATCHPATH.getFilePath( action , masterFolder );
		int status = shell.customGetStatus( action , folder , "git commit -m " + Common.getQuoted( commitMessage ) );
		if( status != 0 )
			return( false );
		
		GitMirrorStorage storage = getStorage( repository , false , false );
		LocalFolder storageFolder = storage.getStorageFolder( action );
		if( !PATCHPATH.equals( storageFolder ) )
			pushOrigin( PATCHPATH.folderPath );
		
		pushOrigin( storageFolder.folderPath );
		return( true );
	}
	
	@Override public void addFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		String path = PATCHPATH.getFilePath( action , folder );
		String filePath = file;
		if( PATCHPATH.windows )
			filePath = Common.getWinPath( filePath );
		shell.customCheckStatus( action , path , "git add " + filePath );
	}
	
	@Override public void deleteFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void addDirToCommit( LocalFolder PATCHPATH , String folder ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void deleteDirToCommit( LocalFolder PATCHPATH , String folder ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void createMasterTag( String repository , String masterFolder , String TAG , String commitMessage ) throws Exception {
		TAG = getTagName( TAG );
		action.exitNotImplemented();
	}

	public void cloneRemoteToLocal( String remotePath , String repository , Folder projectFolder , boolean bare ) throws Exception {
		String url = res.BASEURL;
		
		String urlAuth = url;
		String user = "";
		if( !res.ac.isAnonymous() ) {
			user = res.ac.getUser( action );
			String userEncoded = URLEncoder.encode( user , "UTF-8" );
			String password = URLEncoder.encode( res.ac.getPassword( action ) , "UTF-8" );
			urlAuth = Common.getPartBeforeFirst( url , "//" ) + "//" + userEncoded + ":" + password + "@" + Common.getPartAfterFirst( url , "//" );
		}
		
		projectFolder.ensureExists( action );
		
		String cmd = "git clone " + urlAuth + "/" + remotePath;
		if( bare )
			cmd += " --mirror";
		cmd += " " + projectFolder.getOSPath();
		
		int status = shell.customGetStatus( action , cmd );
		if( status != 0 )
			action.exit( "unable to clone repository " + url + " to " + projectFolder.getOSPath() );

		GitMirrorStorage storage = getStorage( repository , bare , false );
		LocalFolder storageFolder = storage.getStorageFolder( action );
		String OSPATH = shell.getOSPath( action , storageFolder.folderPath );
		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.name " + Common.getQuoted( user ) );
		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.email " + Common.getQuoted( "ignore@mail.com" ) );
	}
	
	public void pushOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " push origin" );
		if( status != 0 )
			action.exit( "unable to push origin, path=" + OSPATH );
	}

	public void fetchOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " fetch origin" );
		if( status != 0 )
			action.exit( "unable to fetch origin, path=" + OSPATH );
	}

}
