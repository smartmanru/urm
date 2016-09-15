package org.urm.engine.vcs;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.engine.ServerAuthResource;
import org.urm.engine.ServerMirrorRepository;
import org.urm.engine.storage.Folder;
import org.urm.engine.storage.LocalFolder;

public class GitMirrorStorage extends MirrorStorage {

	GitVCS vcs;
	
	String REPONAME;
	String REPOROOT;
	
	LocalFolder bareFolder;
	
	public GitMirrorStorage( GitVCS vcs , ServerMirrorRepository mirror , LocalFolder customRepoFolder ) {
		super( vcs , mirror , customRepoFolder );
		this.vcs = vcs;
	}
	
	@Override
	public void create( boolean newStorage , boolean check ) throws Exception {
		super.create( newStorage , check );
		
		REPONAME = mirror.RESOURCE_REPO;
		REPOROOT = mirror.RESOURCE_ROOT;
		LocalFolder basex = super.getBaseFolder();
		bareFolder = basex.getSubFolder( action , mirror.NAME + ".git.bare" );
	}
	
	@Override
	public boolean isEmpty() throws Exception {
		List<String> dirs = new LinkedList<String>();
		List<String> files = new LinkedList<String>();
		
		LocalFolder commitFolder = super.getCommitFolder();
		if( !commitFolder.checkExists( action ) )
			return( true );
		
		commitFolder.getTopDirsAndFiles( action , dirs , files );
		if( !files.isEmpty() )
			return( false );
		if( dirs.isEmpty() )
			return( true );
		if( dirs.size() == 1 && dirs.get(0).equals( ".git" ) )
			return( true );
		return( false );
	}
	
	public String getBranch() {
		String branch = mirror.BRANCH;
		if( branch == null || branch.isEmpty() || branch.equals( "trunk" ) )
			branch = "master";
		return( branch );
	}
	
	public void createLocalMirror() throws Exception {
		create( true , true );

		if( bareFolder.checkExists( action ) ) {
			String bareOSpath = getBareOSPath();
			action.exit1( _Error.BareDirectoryAlreadyExists1 , "Bare folder already exists - " + bareOSpath , bareOSpath );
		}
		
		// create bare repository if not exists
		String repoPath = REPONAME;
		if( REPOROOT != null && !REPOROOT.isEmpty() )
			repoPath = REPOROOT + "/" + repoPath;
		
		cloneRemoteToBare( repoPath );
		if( !vcs.checkTargetEmpty( mirror ) )
			action.exit1( _Error.MirrorDirectoryNotEmpty1 , "Target mirror folder is not empty - " + mirror.RESOURCE_DATA , mirror.RESOURCE_DATA );

		int status = shell.customGetStatus( action , bareFolder.folderPath , "git log -n 1 --oneline -m " + getBranch() );
		if( status == 0 )
			createLocalFromBranch( getBranch() );
		else
			initLocalFromBranch( getBranch() );
	}
	
	public void useProjectMirror( boolean check ) throws Exception {
		create( false , check );
		
		if( check && !bareFolder.checkExists( action ) ) {
			String bareOSpath = getBareOSPath();
			action.exit1( _Error.MissingBareDirectory1 , "Bare folder not exists at " + bareOSpath , bareOSpath );
		}
	}

	public void removeLocalMirror() throws Exception {
		super.remove();
		bareFolder.removeThis( action );
	}
	
	public void initLocalFromBranch( String BRANCH ) throws Exception {
		String OSPATH = getBareOSPath();
		String OSPATHPROJECT = super.getRepoOSPath();
		
		LocalFolder repoFolder = super.getRepoFolder(); 
		repoFolder.ensureExists( action );
		int status = shell.customGetStatus( action , repoFolder.folderPath , "git init" );
		
		ServerAuthResource res = vcs.res;
		String user = "";
		if( !res.ac.isAnonymous() )
			user = res.ac.getUser( action );
		String branch = getBranch();
		
		shell.customCheckStatus( action , repoFolder.folderPath , "git config user.name " + Common.getQuoted( user ) );
		shell.customCheckStatus( action , repoFolder.folderPath , "git config user.email " + Common.getQuoted( "ignore@mail.com" ) );
		
		repoFolder.createFileFromString( action , "README.md" , "# URM REPOSITORY" );
		vcs.addFileToCommit( mirror , repoFolder , "" , "README.md" );
		if( status == 0 )
			status = shell.customGetStatus( action , repoFolder.folderPath , "git commit -m " + Common.getQuoted( "First commit" ) );
		if( status == 0 )
			status = shell.customGetStatus( action , repoFolder.folderPath , "git remote add origin " + OSPATH );
		if( status == 0 )
			status = shell.customGetStatus( action , repoFolder.folderPath , "git push -u origin " + branch );
		if( status != 0 )
			action.exit2( _Error.GitUnableClone2 , "Unable to clone from " + OSPATH + " to " + OSPATHPROJECT , OSPATH , OSPATHPROJECT );
		
		pushBare();
	}
	
	public void createLocalFromBranch( String BRANCH ) throws Exception {
		String OSPATH = getBareOSPath();
		String OSPATHPROJECT = super.getCommitOSPath();
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " clone " + OSPATH + " --shared -b " + BRANCH + " " + OSPATHPROJECT );
		if( status != 0 )
			action.exit2( _Error.GitUnableClone2 , "Unable to clone from " + OSPATH + " to " + OSPATHPROJECT , OSPATH , OSPATHPROJECT );
	}
	
	public void refreshMirror() throws Exception {
		refreshBare();
		LocalFolder repoFolder = super.getRepoFolder();
		fetchOrigin( repoFolder.folderPath );
	}

	public void refreshBare() throws Exception {
		fetchOrigin( bareFolder.folderPath );
	}

	public void addModified() throws Exception {
		if( shell.isWindows() )
			action.exitNotImplemented();
			
		LocalFolder commitFolder = super.getCommitFolder();
		shell.customCheckErrorsDebug( action , commitFolder.folderPath , 
			"F_LIST=`git diff --name-only`; " +
			"if [ " + Common.getQuoted( "$F_LIST" ) + " != " + Common.getQuoted( "" ) + " ]; then git add $F_LIST; fi; " +
			"git commit -m " + Common.getQuoted( action.meta.product.CONFIG_ADM_TRACKER + "-0000: set version" ) + "; " +
			"git push origin 2>&1; if [ $? != 0 ]; then echo error on push origin >&2; fi" );
	}

	public void pushMirror() throws Exception {
		LocalFolder repoFolder = super.getRepoFolder();
		pushOrigin( repoFolder.folderPath );
		pushOrigin( bareFolder.folderPath );
	}

	public void pushBare() throws Exception {
		pushOrigin( bareFolder.folderPath );
	}

	public String getBareOSPath() throws Exception {
		return( shell.getOSPath( action , bareFolder.folderPath ) );
	}
	
	private void cloneRemoteToBare( String remotePath ) throws Exception {
		ServerAuthResource res = vcs.res;
		String url = res.BASEURL;
		
		String urlAuth = url;
		String user = "";
		if( !res.ac.isAnonymous() ) {
			user = res.ac.getUser( action );
			String userEncoded = URLEncoder.encode( user , "UTF-8" );
			String password = URLEncoder.encode( res.ac.getPassword( action ) , "UTF-8" );
			urlAuth = Common.getPartBeforeFirst( url , "//" ) + "//" + userEncoded + ":" + password + "@" + Common.getPartAfterFirst( url , "//" );
		}
		
		bareFolder.ensureExists( action );
		
		String OSPATH = getBareOSPath();
		String cmd = "git clone " + urlAuth + "/" + remotePath + " --mirror";
		cmd += " " + OSPATH;
		
		int timeout = action.setTimeoutUnlimited();
		int status = shell.customGetStatus( action , cmd );
		action.setTimeout( timeout );
		
		if( status != 0 )
			action.exit2( _Error.UnableCloneRepository2 , "Unable to clone repository " + url + " to " + OSPATH , url , OSPATH );

		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.name " + Common.getQuoted( user ) );
		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.email " + Common.getQuoted( "ignore@mail.com" ) );
	}
	
	public boolean exportFromPath( String BRANCHTAG , String SUBPATH , String FILENAME ) throws Exception {
		LocalFolder commitFolder = super.getCommitFolder();
		Folder BASEDIR = commitFolder.getParentFolder( action );
		String BASENAME = commitFolder.getBaseName( action );
		
		if( FILENAME.isEmpty() ) {
			if( !BASEDIR.checkExists( action ) )
				action.exit1( _Error.MissingLocalDirectory1 , "Local directory " + BASEDIR.folderPath + " does not exist" , BASEDIR.folderPath );
			if( commitFolder.checkExists( action ) )
				action.exit1( _Error.LocalDirectoryShouldNotExist1 , "Local directory " + commitFolder.folderPath + " should not exist" , commitFolder.folderPath );
			
			// export file or subdir
			commitFolder.ensureExists( action );
			if( SUBPATH.isEmpty() ) {
				if( shell.isWindows() ) {
					String WINPATH = getBareOSPath();
					String WINPATHPROJECT = getCommitOSPath();
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							" . | ( cd /D " + WINPATHPROJECT + " & tar x --exclude pax_global_header)" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + getBareOSPath() + " archive " + BRANCHTAG + " " + 
						" . | ( cd " + getCommitOSPath() + "; tar x )" );
				}
			}
			else {
				int COMPS = Common.getDirCount( SUBPATH ) + 1;
				String STRIPOPTION = "--strip-components=" + COMPS;
				
				if( shell.isWindows() ) {
					String WINPATH = getBareOSPath();
					String WINPATHPROJECT = getCommitOSPath();
					String WINPATHSUB = Common.getWinPath( SUBPATH );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							WINPATHSUB + " | ( cd /D " + WINPATHPROJECT + " & tar x " + WINPATHSUB + " " + STRIPOPTION + " )" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + getBareOSPath() + " archive " + BRANCHTAG + " " + 
							SUBPATH + " | ( cd " + getCommitOSPath() + "; tar x --exclude pax_global_header " + SUBPATH + " " + STRIPOPTION + " )" );
				}
			}
		}
		else {
			if( !commitFolder.checkExists( action ) )
				action.exit1( _Error.MissingLocalDirectory1 , "Local directory " + commitFolder.folderPath + " does not exist" , commitFolder.folderPath );
			
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

	public void pushOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " push origin" );
		if( status != 0 )
			action.exit1( _Error.UnablePushOrigin1 , "Unable to push origin, path=" + OSPATH , OSPATH );
	}

	public void fetchOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " fetch origin" );
		if( status != 0 )
			action.exit1( _Error.UnableFetchOrigin1 , "Unable to fetch origin, path=" + OSPATH , OSPATH );
	}

}
