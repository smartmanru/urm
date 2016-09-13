package org.urm.server.vcs;

import java.net.URLEncoder;
import java.util.List;

import org.urm.common.Common;
import org.urm.server.ServerAuthResource;
import org.urm.server.ServerMirrorRepository;
import org.urm.server.storage.Folder;
import org.urm.server.storage.LocalFolder;

public class GitMirrorStorage extends MirrorStorage {

	boolean bare;
	GitVCS vcs;
	
	String REPONAME;
	String REPOROOT;
	LocalFolder commitFolder;
	
	public GitMirrorStorage( GitVCS vcs , ServerMirrorRepository mirror , boolean bare , LocalFolder commitFolder ) {
		super( vcs , mirror );
		this.vcs = vcs;
		this.bare = bare;
		this.commitFolder = commitFolder;
	}
	
	public void createLocalMirror() throws Exception {
		super.create( true , true );
		REPONAME = mirror.RESOURCE_REPO;
		REPOROOT = mirror.RESOURCE_ROOT;
		
		String repoPath = REPONAME;
		if( REPOROOT != null && !REPOROOT.isEmpty() )
			repoPath = REPOROOT + "/" + repoPath;
		
		cloneRemoteToLocal( repoPath );
	}

	public String getCommitOSPath() throws Exception {
		return( shell.getOSPath( action , commitFolder.folderPath ) );
	}
	
	public boolean isEmpty() throws Exception {
		List<String> dirs = mirrorFolder.getTopDirs( action );
		List<String> files = mirrorFolder.getTopFiles( action );
		
		if( dirs.size() == 1 && dirs.get(0).equals( ".git" ) ) {
			if( files.isEmpty() || ( files.size() == 1 && files.get(0).equals( "README.md" ) ) )
				return( true );
		}
		
		return( false );
	}
	
	public void createReadMe() throws Exception {
		if( mirrorFolder.checkFileExists( action , "README.md" ) )
			return;
				
		mirrorFolder.createFileFromString( action , "README.md" , "# URM REPOSITORY" );
		vcs.addFileToCommit( mirror , mirrorFolder , "" , "README.md" );
		if( !vcs.commitMasterFolder( mirror , mirrorFolder , "" , "first commit" ) )
			action.exit0( _Error.UnableÑommit0 , "unable to commit" );
	}
	
	public void refreshMirror() throws Exception {
		fetchOrigin( mirrorFolder.folderPath );
	}

	public void addModified() throws Exception {
		if( shell.isWindows() )
			action.exitNotImplemented();
			
		shell.customCheckErrorsDebug( action , commitFolder.folderPath , 
			"F_LIST=`git diff --name-only`; " +
			"if [ " + Common.getQuoted( "$F_LIST" ) + " != " + Common.getQuoted( "" ) + " ]; then git add $F_LIST; fi; " +
			"git commit -m " + Common.getQuoted( action.meta.product.CONFIG_ADM_TRACKER + "-0000: set version" ) + "; " +
			"git push origin 2>&1; if [ $? != 0 ]; then echo error on push origin >&2; fi" );
	}

	public void pushMirror() throws Exception {
		pushOrigin( mirrorFolder.folderPath );
	}

	public void cloneRemoteToLocal( String remotePath ) throws Exception {
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
		
		mirrorFolder.ensureExists( action );
		
		String OSPATH = getMirrorOSPath();
		String cmd = "git clone " + urlAuth + "/" + remotePath;
		if( bare )
			cmd += " --mirror";
		cmd += " " + OSPATH;
		
		int status = shell.customGetStatus( action , cmd );
		if( status != 0 )
			action.exit2( _Error.UnableCloneRepository2 , "unable to clone repository " + url + " to " + OSPATH , url , OSPATH );

		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.name " + Common.getQuoted( user ) );
		shell.customCheckStatus( action , "git -C " + OSPATH + " config user.email " + Common.getQuoted( "ignore@mail.com" ) );
	}
	
	public boolean exportFromPath( String BRANCHTAG , String SUBPATH , String FILENAME ) throws Exception {
		Folder BASEDIR = commitFolder.getParentFolder( action );
		String BASENAME = commitFolder.getBaseName( action );
		
		if( FILENAME.isEmpty() ) {
			if( !BASEDIR.checkExists( action ) )
				action.exit1( _Error.MissingLocalDirectory1 , "exportFromPath: local directory " + BASEDIR.folderPath + " does not exist" , BASEDIR.folderPath );
			if( commitFolder.checkExists( action ) )
				action.exit1( _Error.LocalDirectoryShouldNotExist1 , "exportFromPath: local directory " + commitFolder.folderPath + " should not exist" , commitFolder.folderPath );
			
			// export file or subdir
			commitFolder.ensureExists( action );
			if( SUBPATH.isEmpty() ) {
				if( shell.isWindows() ) {
					String WINPATH = getMirrorOSPath();
					String WINPATHPROJECT = getCommitOSPath();
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							" . | ( cd /D " + WINPATHPROJECT + " & tar x --exclude pax_global_header)" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + getMirrorOSPath() + " archive " + BRANCHTAG + " " + 
						" . | ( cd " + getCommitOSPath() + "; tar x )" );
				}
			}
			else {
				int COMPS = Common.getDirCount( SUBPATH ) + 1;
				String STRIPOPTION = "--strip-components=" + COMPS;
				
				if( shell.isWindows() ) {
					String WINPATH = getMirrorOSPath();
					String WINPATHPROJECT = getCommitOSPath();
					String WINPATHSUB = Common.getWinPath( SUBPATH );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							WINPATHSUB + " | ( cd /D " + WINPATHPROJECT + " & tar x " + WINPATHSUB + " " + STRIPOPTION + " )" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + getMirrorOSPath() + " archive " + BRANCHTAG + " " + 
							SUBPATH + " | ( cd " + getCommitOSPath() + "; tar x --exclude pax_global_header " + SUBPATH + " " + STRIPOPTION + " )" );
				}
			}
		}
		else {
			if( !commitFolder.checkExists( action ) )
				action.exit1( _Error.MissingLocalDirectory1 , "exportFromPath: local directory " + commitFolder.folderPath + " does not exist" , commitFolder.folderPath );
			
			// export file or subdir
			int COMPS = Common.getDirCount( SUBPATH );
			String STRIPOPTION = "--strip-components=" + COMPS;

			String srcFile = BASEDIR.getFilePath( action , FILENAME );
			if( ( !FILENAME.equals( BASENAME ) ) && BASEDIR.checkFileExists( action , FILENAME ) )
				action.exit1( _Error.LocalFileOrDirectoryShouldNotExist1 , "exportFromPath: local file or directory " + srcFile + " already exists" , srcFile );

			String FILEPATH = Common.getPath( SUBPATH , FILENAME );
			
			if( shell.isWindows() ) {
				String WINPATH = getMirrorOSPath();
				String WINPATHBASE = Common.getWinPath( BASEDIR.folderPath );
				String WINPATHFILE = Common.getWinPath( FILEPATH );
				shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
						WINPATHFILE + " | ( cd /D " + WINPATHBASE + " & tar x --exclude pax_global_header " + WINPATHFILE + " " + STRIPOPTION + " )" );
			}
			else {
				shell.customCheckStatus( action , "git -C " + getMirrorOSPath() + " archive " + BRANCHTAG + " " + 
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
			action.exit1( _Error.UnablePushOrigin1 , "unable to push origin, path=" + OSPATH , OSPATH );
	}

	public void fetchOrigin( String path ) throws Exception {
		String OSPATH = shell.getOSPath( action , path );
		int status = shell.customGetStatus( action , "git -C " + OSPATH + " fetch origin" );
		if( status != 0 )
			action.exit1( _Error.UnableFetchOrigin1 , "unable to fetch origin, path=" + OSPATH , OSPATH );
	}

}
