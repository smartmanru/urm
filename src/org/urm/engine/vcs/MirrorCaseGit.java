package org.urm.engine.vcs;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.storage.Folder;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.MirrorRepository;

public class MirrorCaseGit extends MirrorCase {

	GitVCS vcsGit;
	
	public MirrorCaseGit( GitVCS vcs , MirrorRepository mirror , String BRANCH ) {
		super( vcs , mirror , BRANCH );
		this.vcsGit = vcs;
	}

	@Override
	public String getResourceRepositoryPath() {
		String path = Common.getPath( mirror.RESOURCE_ROOT , mirror.RESOURCE_REPO + ".git.bare" );
		return( path );
	}
	
	@Override
	public String getResourceBranchPath() {
		String path = Common.getPath( mirror.RESOURCE_ROOT , mirror.RESOURCE_REPO + ".git.branches" );
		String branch = getBranch();
		return( Common.getPath( path , branch ) );
	}
	
	@Override
	public String getResourceComponentPath() {
		String branch = getResourceBranchPath();
		return( Common.getPath( branch , mirror.RESOURCE_DATA ) );
	}
	
	@Override
	public void createEmptyMirrorOnServer() throws Exception {
		useRepositoryMirror();
		useBranchMirror();
		
		LocalFolder comp = getComponentFolder();
		if( shell.checkDirExists( action , comp.folderPath ) ) {
			if( !checkCompEmpty() )
				action.exit1( _Error.MirrorDirectoryNotEmpty1 , "Target mirror folder is not empty - " + mirror.RESOURCE_DATA , mirror.RESOURCE_DATA );
			return;
		}
		
		shell.ensureDirExists( action , comp.folderPath );
		LocalFolder branch = getBranchFolder();
		vcs.addDirToCommit( mirror , branch , mirror.RESOURCE_DATA );
		vcs.commitMasterFolder( mirror , branch , mirror.RESOURCE_DATA , "add mirror component" );
		pushMirror();
	}
	
	@Override
	public void useMirror() throws Exception {
		useRepositoryMirror();
		if( BRANCH.isEmpty() )
			return;
		
		useBranchMirror();
		
		LocalFolder compFolder = getComponentFolder();
		if( !shell.checkDirExists( action , compFolder.folderPath ) ) {
			String OSPATH = shell.getLocalPath( compFolder.folderPath );
			action.exit2( _Error.MissingRepoMirrorDirectory1 , "Missing mirror repository directory: " , OSPATH , OSPATH );
		}
	}

	@Override
	public void refreshMirror() throws Exception {
		refreshComponent( true );
	}
	
	@Override
	public void pushMirror() throws Exception {
		LocalFolder comp = getComponentFolder();
		pushOrigin( comp.folderPath );
		LocalFolder repo = getRepositoryFolder();
		pushOrigin( repo.folderPath );
	}
	
	@Override
	public void dropMirror( boolean dropOnServer ) throws Exception {
	}
	
	@Override 
	public LocalFolder getMirrorFolder() throws Exception {
		return( getComponentFolder() );
	}
	
	@Override 
	public String getSpecialDirectory() {
		return( "\\.git" );
	}

	@Override
	public void syncFolderToVcs( String mirrorSubFolder , LocalFolder folder ) throws Exception {
		LocalFolder cf = getMirrorFolder();
		LocalFolder mf = cf.getSubFolder( action , mirrorSubFolder );
		
		super.syncFolderToVcsContent( mirrorSubFolder , folder );
		addModified( mf );
		
		vcs.commitMasterFolder( mirror , mf , "" , "sync from source" );
	}
	
	@Override
	public void syncVcsToFolder( String mirrorFolder , LocalFolder folder ) throws Exception {
		super.syncVcsToFolderContent( mirrorFolder , folder );
	}
	
	public void pushRepository() throws Exception {
		LocalFolder repo = getRepositoryFolder();
		pushOrigin( repo.folderPath );
	}
	
	public boolean checkValidBranch() throws Exception {
		String branch = getBranch();
		return( checkValidBranch( branch ) );
	}

	public void refreshComponent( boolean refreshRepository ) throws Exception {
		if( refreshRepository )
			refreshBranch( true );
		else {
			useMirror();
			LocalFolder compFolder = getComponentFolder();
			fetchOrigin( compFolder.folderPath );
		}
	}
	
	public void refreshRepository() throws Exception {
		useMirror();
	}
	
	public void refreshRepositoryInternal() throws Exception {
		LocalFolder repoFolder = getRepositoryFolder();
		fetchOrigin( repoFolder.folderPath );
	}
	
	public void refreshBranch( boolean refreshRepository ) throws Exception {
		if( refreshRepository )
			refreshRepository();
		else
			useMirror();
		refreshBranchInternal();
	}

	public void refreshBranchInternal() throws Exception {
		LocalFolder branchFolder = getBranchFolder();
		fetchOrigin( branchFolder.folderPath );
	}
	
	public boolean checkCompEmpty() throws Exception {
		LocalFolder compFolder = getComponentFolder();
		List<String> dirs = new LinkedList<String>();
		List<String> files = new LinkedList<String>();
		compFolder.getTopDirsAndFiles( action , dirs , files );
		if( !dirs.isEmpty() ) {
			if( !( dirs.size() == 1 && dirs.get(0).equals( ".git" ) ) )
				return( false );
		}
		
		if( files.isEmpty() || ( files.size() == 1 && files.get(0).equals( "README.md" ) ) )
			return( true );
		return( false );
	}

	public boolean checkValidBranch( String BRANCH ) throws Exception {
		String OSPATH = getBareOSPath();
		String GITBRANCH = vcsGit.getGitBranchName( BRANCH );
		String STATUS = shell.customGetValue( action , "git -C " + OSPATH + " branch --list " + GITBRANCH );
		
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getBareOSPath() throws Exception {
		LocalFolder folder = getRepositoryFolder();
		return( shell.getOSPath( action , folder.folderPath ) );
	}

	public void createLocalFromBranch( LocalFolder checkoutFolder , String BRANCH ) throws Exception {
		String OSPATH = getBareOSPath();
		String OSPATHPROJECT = checkoutFolder.getLocalPath( action );
		
		if( !checkValidBranch( BRANCH ) )
			action.exit2( _Error.GitUnableClone2 , "Unable to clone from " + OSPATH + " to " + OSPATHPROJECT , OSPATH , OSPATHPROJECT );
		
		String GITBRANCH = vcsGit.getGitBranchName( BRANCH );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " clone -q " + OSPATH + " --shared -b " + GITBRANCH + " " + OSPATHPROJECT );
		if( status != 0 )
			action.exit2( _Error.GitUnableClone2 , "Unable to clone from " + OSPATH + " to " + OSPATHPROJECT , OSPATH , OSPATHPROJECT );
		
		setAccess( OSPATHPROJECT );
	}
	
	private void setAccess( String OSPATH ) throws Exception {
		AuthResource res = vcs.res;
		String user = "";
		if( !res.ac.isAnonymous() )
			user = res.ac.getUser( action );
		
		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.name " + Common.getQuoted( user ) );
		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.email " + Common.getQuoted( "ignore@mail.com" ) );
	}
	
	public void addModified( LocalFolder checkoutFolder ) throws Exception {
		shell.customCheckErrorsDebug( action , checkoutFolder.folderPath , "git add -u" );
	}

	public void pushOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " push origin" );
		if( status != 0 )
			action.exit1( _Error.UnablePushOrigin1 , "Unable to push origin, path=" + OSPATH , OSPATH );
	}
	
	public boolean exportFromPath( LocalFolder exportFolder , String GITBRANCHTAG , String SUBPATH , String FILENAME ) throws Exception {
		refreshRepository();
		
		Folder BASEDIR = exportFolder.getParentFolder( action );
		String BASENAME = exportFolder.getBaseName( action );
		
		if( FILENAME.isEmpty() ) {
			if( !shell.checkDirExists( action , BASEDIR.folderPath ) )
				action.exit1( _Error.MissingLocalDirectory1 , "Local directory " + shell.getLocalPath( BASEDIR.folderPath ) + " does not exist" , shell.getLocalPath( BASEDIR.folderPath ) );
			if( shell.checkDirExists( action , exportFolder.folderPath ) )
				action.exit1( _Error.LocalDirectoryShouldNotExist1 , "Local directory " + shell.getLocalPath( exportFolder.folderPath ) + " should not exist" , shell.getLocalPath( exportFolder.folderPath ) );
			
			// export file or subdir
			shell.ensureDirExists( action , exportFolder.folderPath );
			if( SUBPATH.isEmpty() || SUBPATH.equals( "/" ) ) {
				if( shell.isWindows() ) {
					String WINPATH = getBareOSPath();
					String WINPATHPROJECT = shell.getLocalPath( exportFolder.folderPath );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + GITBRANCHTAG + " " + 
							" . | ( cd /D " + WINPATHPROJECT + " & tar x --exclude pax_global_header)" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + getBareOSPath() + " archive " + GITBRANCHTAG + " " + 
						" . | ( cd " + exportFolder.folderPath + "; tar x )" );
				}
			}
			else {
				int COMPS = Common.getDirCount( SUBPATH ) + 1;
				String STRIPOPTION = "--strip-components=" + COMPS;
				
				if( shell.isWindows() ) {
					String WINPATH = getBareOSPath();
					String WINPATHPROJECT = shell.getLocalPath( exportFolder.folderPath );
					String WINPATHSUB = Common.getWinPath( SUBPATH );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + GITBRANCHTAG + " " + 
							WINPATHSUB + " | ( cd /D " + WINPATHPROJECT + " & tar x " + WINPATHSUB + " " + STRIPOPTION + " )" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + getBareOSPath() + " archive " + GITBRANCHTAG + " " + 
							SUBPATH + " | ( cd " + exportFolder.folderPath + "; tar x --exclude pax_global_header " + SUBPATH + " " + STRIPOPTION + " )" );
				}
			}
		}
		else {
			if( !shell.checkDirExists( action , exportFolder.folderPath ) )
				action.exit1( _Error.MissingLocalDirectory1 , "Local directory " + shell.getLocalPath( exportFolder.folderPath ) + " does not exist" , shell.getLocalPath( exportFolder.folderPath ) );
			
			// export file or subdir
			int COMPS = Common.getDirCount( SUBPATH );
			String STRIPOPTION = "--strip-components=" + COMPS;

			String srcFile = BASEDIR.getFilePath( action , FILENAME );
			if( ( !FILENAME.equals( BASENAME ) ) && shell.checkFileExists( action , BASEDIR.folderPath , FILENAME ) )
				action.exit1( _Error.LocalFileOrDirectoryShouldNotExist1 , "Local file or directory " + shell.getLocalPath( srcFile ) + " already exists" , shell.getLocalPath( srcFile ) );

			String FILEPATH = Common.getPath( SUBPATH , FILENAME );
			
			if( shell.isWindows() ) {
				String WINPATH = getBareOSPath();
				String WINPATHBASE = Common.getWinPath( BASEDIR.folderPath );
				String WINPATHFILE = Common.getWinPath( FILEPATH );
				shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + GITBRANCHTAG + " " + 
						WINPATHFILE + " | ( cd /D " + WINPATHBASE + " & tar x --exclude pax_global_header " + WINPATHFILE + " " + STRIPOPTION + " )" );
			}
			else {
				shell.customCheckStatus( action , "git -C " + getBareOSPath() + " archive " + GITBRANCHTAG + " " + 
						FILEPATH + " | ( cd " + BASEDIR.folderPath + "; tar x " + FILEPATH + " " + STRIPOPTION + " )" );
			}
			if( !FILENAME.equals( BASENAME ) ) {
				String src = BASEDIR.getFilePath( action , FILENAME );
				String dst = BASEDIR.getFilePath( action , BASENAME );
				shell.move( action , src , dst );
			}
		}

		return( true );
	}

	private String getBranch() {
		String branch = BRANCH;
		if( branch == null || branch.isEmpty() || branch.equals( SubversionVCS.MASTERBRANCH ) )
			branch = GitVCS.MASTERBRANCH;
		return( branch );
	}

	private void useRepositoryMirror() throws Exception {
		LocalFolder repo = getRepositoryFolder();
		
		String OSPATH = shell.getOSPath( action , repo.folderPath );
		if( shell.checkDirExists( action , OSPATH ) ) {
			refreshRepositoryInternal();
			return;
		}

		String remotePath = mirror.RESOURCE_REPO;
		if( !mirror.RESOURCE_ROOT.isEmpty() )
			remotePath = Common.getPath( mirror.RESOURCE_ROOT , remotePath );

		int status = 0;
		AuthResource res = vcs.res;
		String url = res.BASEURL;
		int timeout = action.setTimeoutUnlimited();
		try {
			String urlAuth = vcsGit.getRepositoryAuthUrl();
			
			shell.ensureDirExists( action , OSPATH );
			
			String urlAuthFull = Common.getPath( urlAuth , remotePath );
			String cmd = "git clone -q " + urlAuthFull + " --mirror";
			cmd += " " + OSPATH;
			
			status = shell.customGetStatus( action , cmd );
			if( status == 0 ) {
				setAccess( OSPATH );
				
				// only for push
				if( shell.isLocal() ) {
					String file = Common.getPath( OSPATH , "packed-refs" );
					List<String> lines = ConfReader.readFileLines( action.execrc , file );
					List<String> nopulls = new LinkedList<String>();
					for( String line : lines ) {
						if( !line.contains( "refs/pull/" ) )
							nopulls.add( line );
					}
					Common.createFileFromStringList( action.execrc , file , nopulls );
					
					shell.customCheckStatus( action , "git -C " + OSPATH + " config --unset-all remote.origin.fetch" );
					shell.customCheckStatus( action , "git -C " + OSPATH + " config --add remote.origin.fetch +refs/heads/*:refs/heads/*" );
					shell.customCheckStatus( action , "git -C " + OSPATH + " config --add remote.origin.fetch +refs/tags/*:refs/tags/*" );
					shell.customCheckStatus( action , "git -C " + OSPATH + " config --add remote.origin.fetch +refs/change/*:refs/change/*" );
				}
			}
		}
		catch( Throwable e ) {
			action.log( "mirror repository" , e );
			status = -1;
		}
		
		action.setTimeout( timeout );
		if( status != 0 ) {
			shell.removeDir( action , repo.folderPath );
			String urlShow = Common.getPath( url , remotePath );
			action.exit2( _Error.UnableCloneRepository2 , "Unable to clone repository " + urlShow + " to " + OSPATH , urlShow , OSPATH );
		}
	}

	private void useBranchMirror() throws Exception {
		LocalFolder branch = getBranchFolder();
		if( shell.checkDirExists( action , branch.folderPath ) ) {
			refreshBranchInternal();
			return;
		}

		if( !checkValidBranch() ) {
			String BRANCH = getBranch();
			action.exit1( _Error.MissingRepoBranch1 , "Missing repository branch=" + BRANCH , BRANCH );
		}
		
		try {
			LocalFolder parent = branch.getParentFolder( action );
			shell.ensureDirExists( action , parent.folderPath );
			createLocalFromBranch( branch , getBranch() );
		}
		catch( Throwable e ) {
			shell.removeDir( action , branch.folderPath );
			action.log( "mirror repository" , e );
			throw e;
		}
	}

	private void fetchOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " fetch -q origin" );
		if( status != 0 )
			action.exit1( _Error.UnableFetchOrigin1 , "Unable to fetch origin, path=" + OSPATH , OSPATH );
	}

}
