package org.urm.engine.vcs;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.engine.storage.Folder;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerMirrorRepository;
import org.urm.meta.product.MetaProductSettings;

public class MirrorCaseGit extends MirrorCase {

	GitVCS vcsGit;
	
	public MirrorCaseGit( GitVCS vcs , ServerMirrorRepository mirror ) {
		super( vcs , mirror );
		this.vcsGit = vcs;
	}

	@Override
	public LocalFolder getRepositoryFolder() throws Exception {
		LocalFolder res = super.getResourceFolder();
		String folder = Common.getPath( mirror.RESOURCE_ROOT , mirror.RESOURCE_REPO + ".git.bare" );
		return( res.getSubFolder( action , folder ) );
	}
	
	@Override
	public LocalFolder getBranchFolder() throws Exception {
		LocalFolder res = super.getResourceFolder();
		String folder = Common.getPath( mirror.RESOURCE_ROOT , mirror.RESOURCE_REPO + ".git.branches" );
		if( mirror.BRANCH.isEmpty() )
			folder = Common.getPath( folder , GitVCS.MASTERBRANCH );
		else
			folder = Common.getPath( folder , mirror.BRANCH );
		
		return( res.getSubFolder( action , folder ) );
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
			if( !checkCompEmpty() )
				action.exit1( _Error.MirrorDirectoryNotEmpty1 , "Target mirror folder is not empty - " + mirror.RESOURCE_DATA , mirror.RESOURCE_DATA );
			return;
		}
		
		comp.ensureExists( action );
		LocalFolder branch = getBranchFolder();
		vcs.addDirToCommit( mirror , branch , mirror.RESOURCE_DATA );
		vcs.commitMasterFolder( mirror , branch , mirror.RESOURCE_DATA , "add mirror component" );
		pushComponentChanges();
	}
	
	@Override
	public void useMirror() throws Exception {
		useRepositoryMirror();
		useBranchMirror();
		
		LocalFolder compFolder = getComponentFolder();
		if( !compFolder.checkExists( action ) ) {
			String OSPATH = shell.getLocalPath( compFolder.folderPath );
			action.exit2( _Error.MissingRepoMirrorDirectory1 , "Missing mirror repository directory: " , OSPATH , OSPATH );
		}
	}

	@Override
	public void dropMirror( boolean dropOnServer ) throws Exception {
	}
	
	@Override
	public void refreshRepository() throws Exception {
		LocalFolder repoFolder = getRepositoryFolder();
		fetchOrigin( repoFolder.folderPath );
	}
	
	@Override
	public void refreshBranch( boolean refreshRepository ) throws Exception {
		if( refreshRepository )
			refreshRepository();
		
		LocalFolder branchFolder = getBranchFolder();
		fetchOrigin( branchFolder.folderPath );
	}
	
	@Override
	public void refreshComponent( boolean refreshRepository ) throws Exception {
		if( refreshRepository )
			refreshBranch( true );
		else {
			LocalFolder compFolder = getComponentFolder();
			fetchOrigin( compFolder.folderPath );
		}
	}
	
	@Override
	public void pushComponentChanges() throws Exception {
		LocalFolder comp = getComponentFolder();
		pushOrigin( comp.folderPath );
		LocalFolder repo = getRepositoryFolder();
		pushOrigin( repo.folderPath );
	}
	
	@Override 
	public boolean checkValidBranch() throws Exception {
		String branch = getBranch();
		return( checkValidBranch( branch ) );
	}

	@Override 
	public String getSpecialDirectory() {
		return( "\\.git" );
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

	public boolean checkValidBranch( String branch ) throws Exception {
		String OSPATH = getBareOSPath();
		String STATUS = shell.customGetValue( action , "git -C " + OSPATH + " branch --list " + branch );
		
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
			
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " clone " + OSPATH + " --shared -b " + BRANCH + " " + OSPATHPROJECT );
		if( status != 0 )
			action.exit2( _Error.GitUnableClone2 , "Unable to clone from " + OSPATH + " to " + OSPATHPROJECT , OSPATH , OSPATHPROJECT );
		
		setAccess( OSPATHPROJECT );
	}
	
	private void setAccess( String OSPATH ) throws Exception {
		ServerAuthResource res = vcs.res;
		String user = "";
		if( !res.ac.isAnonymous() )
			user = res.ac.getUser( action );
		
		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.name " + Common.getQuoted( user ) );
		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.email " + Common.getQuoted( "ignore@mail.com" ) );
	}
	
	public void addModified( LocalFolder checkoutFolder ) throws Exception {
		if( shell.isWindows() )
			action.exitNotImplemented();
			
		MetaProductSettings product = vcs.meta.getProductSettings( action );
		shell.customCheckErrorsDebug( action , checkoutFolder.folderPath , 
			"F_LIST=`git diff --name-only`; " +
			"if [ " + Common.getQuoted( "$F_LIST" ) + " != " + Common.getQuoted( "" ) + " ]; then git add $F_LIST; fi; " +
			"git commit -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: set version" ) + "; " +
			"git push origin 2>&1; if [ $? != 0 ]; then echo error on push origin >&2; fi" );
	}

	public void pushOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " push origin" );
		if( status != 0 )
			action.exit1( _Error.UnablePushOrigin1 , "Unable to push origin, path=" + OSPATH , OSPATH );
	}
	
	public boolean exportFromPath( LocalFolder exportFolder , String BRANCHTAG , String SUBPATH , String FILENAME ) throws Exception {
		refreshRepository();
		
		Folder BASEDIR = exportFolder.getParentFolder( action );
		String BASENAME = exportFolder.getBaseName( action );
		
		if( FILENAME.isEmpty() ) {
			if( !BASEDIR.checkExists( action ) )
				action.exit1( _Error.MissingLocalDirectory1 , "Local directory " + BASEDIR.folderPath + " does not exist" , BASEDIR.folderPath );
			if( exportFolder.checkExists( action ) )
				action.exit1( _Error.LocalDirectoryShouldNotExist1 , "Local directory " + exportFolder.folderPath + " should not exist" , exportFolder.folderPath );
			
			// export file or subdir
			exportFolder.ensureExists( action );
			if( SUBPATH.isEmpty() ) {
				if( shell.isWindows() ) {
					String WINPATH = getBareOSPath();
					String WINPATHPROJECT = exportFolder.getLocalPath( action );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							" . | ( cd /D " + WINPATHPROJECT + " & tar x --exclude pax_global_header)" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + getBareOSPath() + " archive " + BRANCHTAG + " " + 
						" . | ( cd " + exportFolder.folderPath + "; tar x )" );
				}
			}
			else {
				int COMPS = Common.getDirCount( SUBPATH ) + 1;
				String STRIPOPTION = "--strip-components=" + COMPS;
				
				if( shell.isWindows() ) {
					String WINPATH = getBareOSPath();
					String WINPATHPROJECT = exportFolder.getLocalPath( action );
					String WINPATHSUB = Common.getWinPath( SUBPATH );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							WINPATHSUB + " | ( cd /D " + WINPATHPROJECT + " & tar x " + WINPATHSUB + " " + STRIPOPTION + " )" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + getBareOSPath() + " archive " + BRANCHTAG + " " + 
							SUBPATH + " | ( cd " + exportFolder.folderPath + "; tar x --exclude pax_global_header " + SUBPATH + " " + STRIPOPTION + " )" );
				}
			}
		}
		else {
			if( !exportFolder.checkExists( action ) )
				action.exit1( _Error.MissingLocalDirectory1 , "Local directory " + exportFolder.folderPath + " does not exist" , exportFolder.folderPath );
			
			// export file or subdir
			int COMPS = Common.getDirCount( SUBPATH );
			String STRIPOPTION = "--strip-components=" + COMPS;

			String srcFile = BASEDIR.getFilePath( action , FILENAME );
			if( ( !FILENAME.equals( BASENAME ) ) && BASEDIR.checkFileExists( action , FILENAME ) )
				action.exit1( _Error.LocalFileOrDirectoryShouldNotExist1 , "Local file or directory " + srcFile + " already exists" , srcFile );

			String FILEPATH = Common.getPath( SUBPATH , FILENAME );
			
			if( shell.isWindows() ) {
				String WINPATH = getBareOSPath();
				String WINPATHBASE = Common.getWinPath( BASEDIR.folderPath );
				String WINPATHFILE = Common.getWinPath( FILEPATH );
				shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
						WINPATHFILE + " | ( cd /D " + WINPATHBASE + " & tar x --exclude pax_global_header " + WINPATHFILE + " " + STRIPOPTION + " )" );
			}
			else {
				shell.customCheckStatus( action , "git -C " + getBareOSPath() + " archive " + BRANCHTAG + " " + 
						FILEPATH + " | ( cd " + BASEDIR.folderPath + "; tar x " + FILEPATH + " " + STRIPOPTION + " )" );
			}
			if( !FILENAME.equals( BASENAME ) )
				BASEDIR.moveFileToFolder( action , FILENAME , BASENAME );
		}

		return( true );
	}

	private String getBranch() {
		String branch = mirror.BRANCH;
		if( branch == null || branch.isEmpty() || branch.equals( SubversionVCS.MASTERBRANCH ) )
			branch = GitVCS.MASTERBRANCH;
		return( branch );
	}

	private void useRepositoryMirror() throws Exception {
		LocalFolder repo = getRepositoryFolder(); 
		if( repo.checkExists( action ) ) {
			refreshRepository();
			return;
		}

		String remotePath = mirror.RESOURCE_REPO;
		if( !mirror.RESOURCE_ROOT.isEmpty() )
			remotePath = Common.getPath( mirror.RESOURCE_ROOT , remotePath );

		int status = 0;
		ServerAuthResource res = vcs.res;
		String url = res.BASEURL;
		String OSPATH = "";
		int timeout = action.setTimeoutUnlimited();
		try {
			String urlAuth = vcsGit.getRepositoryAuthUrl();
			
			repo.ensureExists( action );
			
			OSPATH = shell.getOSPath( action , repo.folderPath );
			String cmd = "git clone " + urlAuth + "/" + remotePath + " --mirror";
			cmd += " " + OSPATH;
			
			status = shell.customGetStatus( action , cmd );
			setAccess( OSPATH );
		}
		catch( Throwable e ) {
			action.log( "mirror repository" , e );
			status = -1;
		}
		
		action.setTimeout( timeout );
		if( status != 0 ) {
			repo.removeThis( action );
			action.exit2( _Error.UnableCloneRepository2 , "Unable to clone repository " + url + " to " + OSPATH , url , OSPATH );
		}
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
		
		try {
			branch.getParentFolder( action ).ensureExists( action );
			createLocalFromBranch( branch , getBranch() );
		}
		catch( Throwable e ) {
			branch.removeThis( action );
			action.log( "mirror repository" , e );
			throw e;
		}
	}

	private void fetchOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " fetch origin" );
		if( status != 0 )
			action.exit1( _Error.UnableFetchOrigin1 , "Unable to fetch origin, path=" + OSPATH , OSPATH );
	}

}
