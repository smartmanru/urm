package org.urm.server.vcs;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.Folder;

public class GitProjectRepo {
	
	public ActionBase action;
	public ShellExecutor shell;
	public MetaSourceProject project;
	public GitMirrorStorage storage;
	public Folder PATCHFOLDER;
	public String MIRRORPATH;
	
	GitProjectRepo( ActionBase action , ShellExecutor shell , MetaSourceProject project , GitMirrorStorage storage , Folder PATCHFOLDER ) {
		this.action = action;
		this.shell = shell;
		this.project = project;
		this.storage = storage;
		this.PATCHFOLDER = PATCHFOLDER;
		this.MIRRORPATH = storage.mirrorFolder.folderPath;
	}

	public void refreshMirror() throws Exception {
		storage.vcs.fetchOrigin( MIRRORPATH );
	}

	public void createLocalFromBranch( String BRANCH ) throws Exception {
		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		String OSPATHPROJECT = shell.getOSPath( action , PATCHFOLDER.folderPath );
		shell.customCheckStatus( action , "git -C " + OSPATH + " clone " + OSPATH + " --shared -b " + BRANCH + " " + OSPATHPROJECT );
	}

	public void addModified() throws Exception {
		if( shell.isWindows() )
			action.exitNotImplemented();
			
		shell.customCheckErrorsDebug( action , PATCHFOLDER.folderPath , 
			"F_LIST=`git diff --name-only`; " +
			"if [ " + Common.getQuoted( "$F_LIST" ) + " != " + Common.getQuoted( "" ) + " ]; then git add $F_LIST; fi; " +
			"git commit -m " + Common.getQuoted( action.meta.product.CONFIG_ADM_TRACKER + "-0000: set version" ) + "; " +
			"git push origin 2>&1; if [ $? != 0 ]; then echo error on push origin >&2; fi" );
	}

	public void pushMirror() throws Exception {
		storage.vcs.pushOrigin( MIRRORPATH );
	}

	public boolean checkBranchExists( String BRANCH ) throws Exception {
		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		String STATUS = shell.customGetValue( action , "git -C " + OSPATH + " branch --list " + BRANCH );
		
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	public void copyMirrorBranchFromBranch( String BRANCH_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch " + BRANCH_TO + " refs/heads/" + BRANCH_FROM );
	}

	public void dropMirrorBranch( String BRANCH ) throws Exception {
		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch -D " + BRANCH );
	}

	public boolean checkTagExists( String TAG ) throws Exception {
		String STATUS;
		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		STATUS = shell.customGetValue( action , "git -C " + OSPATH + " tag -l " + TAG );
		
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	public void dropMirrorTag( String TAG ) throws Exception {
		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		shell.customCheckStatus( action , "git -C " + OSPATH + " tag -d " + TAG );
	}

	public void copyMirrorTagFromTag( String TAG_FROM , String TAG_TO , String MESSAGE ) throws Exception {
		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		shell.customCheckStatus( action , "git -C " + OSPATH + " tag -a -f -m " + Common.getQuoted( MESSAGE ) + " " + TAG_TO + " refs/tags/" + TAG_FROM );
	}

	public void copyMirrorBranchFromTag( String TAG_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch " + BRANCH_TO + " refs/tags/" + TAG_FROM );
	}

	public boolean exportFromBranch( Folder PATCHFOLDER , String BRANCH , String FILEPATH , String FILENAME ) throws Exception {
		boolean res = exportFromPath( PATCHFOLDER , BRANCH , FILEPATH , FILENAME );
		return( res );
	}

	public boolean exportFromTag( Folder PATCHFOLDER , String TAG , String FILEPATH , String FILENAME ) throws Exception {
		boolean res = exportFromPath( PATCHFOLDER , TAG , FILEPATH , FILENAME );
		return( res );
	}

	public boolean exportFromPath( Folder PATCHFOLDER , String BRANCHTAG , String SUBPATH , String FILENAME ) throws Exception {
		Folder BASEDIR = PATCHFOLDER.getParentFolder( action );
		String BASENAME = PATCHFOLDER.getBaseName( action );
		
		if( FILENAME.isEmpty() ) {
			if( !BASEDIR.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + BASEDIR + " does not exist" );
			if( PATCHFOLDER.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + PATCHFOLDER.folderPath + " should not exist" );
			
			// export file or subdir
			PATCHFOLDER.ensureExists( action );
			if( SUBPATH.isEmpty() ) {
				if( shell.isWindows() ) {
					String WINPATH = Common.getWinPath( MIRRORPATH );
					String WINPATHPROJECT = Common.getWinPath( PATCHFOLDER.folderPath );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							" . | ( cd /D " + WINPATHPROJECT + " & tar x --exclude pax_global_header)" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + MIRRORPATH + " archive " + BRANCHTAG + " " + 
						" . | ( cd " + PATCHFOLDER.folderPath + "; tar x )" );
				}
			}
			else {
				int COMPS = Common.getDirCount( SUBPATH ) + 1;
				String STRIPOPTION = "--strip-components=" + COMPS;
				
				if( shell.isWindows() ) {
					String WINPATH = Common.getWinPath( MIRRORPATH );
					String WINPATHPROJECT = Common.getWinPath( PATCHFOLDER.folderPath );
					String WINPATHSUB = Common.getWinPath( SUBPATH );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							WINPATHSUB + " | ( cd /D " + WINPATHPROJECT + " & tar x " + WINPATHSUB + " " + STRIPOPTION + " )" );
				}
				else {
					shell.customCheckStatus( action , "git -C " + MIRRORPATH + " archive " + BRANCHTAG + " " + 
							SUBPATH + " | ( cd " + PATCHFOLDER.folderPath + "; tar x --exclude pax_global_header " + SUBPATH + " " + STRIPOPTION + " )" );
				}
			}
		}
		else {
			if( !PATCHFOLDER.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + PATCHFOLDER.folderPath + " does not exist" );
			
			// export file or subdir
			int COMPS = Common.getDirCount( SUBPATH );
			String STRIPOPTION = "--strip-components=" + COMPS;

			String srcFile = BASEDIR.getFilePath( action , FILENAME );
			if( ( !FILENAME.equals( BASENAME ) ) && BASEDIR.checkFileExists( action , FILENAME ) )
				action.exit( "exportFromPath: local file or directory " + srcFile + " already exists" );

			String FILEPATH = Common.getPath( SUBPATH , FILENAME );
			
			if( shell.isWindows() ) {
				String WINPATH = Common.getWinPath( MIRRORPATH );
				String WINPATHBASE = Common.getWinPath( BASEDIR.folderPath );
				String WINPATHFILE = Common.getWinPath( FILEPATH );
				shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
						WINPATHFILE + " | ( cd /D " + WINPATHBASE + " & tar x --exclude pax_global_header " + WINPATHFILE + " " + STRIPOPTION + " )" );
			}
			else {
				shell.customCheckStatus( action , "git -C " + MIRRORPATH + " archive " + BRANCHTAG + " " + 
						FILEPATH + " | ( cd " + BASEDIR.folderPath + "; tar x " + FILEPATH + " " + STRIPOPTION + " )" );
			}
			if( !FILENAME.equals( BASENAME ) )
				BASEDIR.moveFileToFolder( action , FILENAME , BASENAME );
		}

		return( true );
	}

	public void setMirrorTag( String BRANCH , String TAG , String MESSAGE , String TAGDATE ) throws Exception {
		// get revision by date
		String REVMARK = "";
		if( !TAGDATE.isEmpty() ) {
			if( shell.isWindows() ) {
				String WINPATH = Common.getWinPath( MIRRORPATH );
				REVMARK = shell.customGetValue( action , "git -C " + WINPATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + 
						" refs/heads/" + BRANCH );
				REVMARK = Common.getListItem( REVMARK , " " , 0 );
			}
			else {
				REVMARK = shell.customGetValue( action , "git -C " + MIRRORPATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + " refs/heads/" + 
						BRANCH + " | tr -d " + Common.getQuoted( " " ) + " -f1" );
			}
			if( REVMARK.isEmpty() )
				action.exit( "setMirrorTag: unable to find branch revision on given date" );
		}

		String OSPATH = shell.getOSPath( action , MIRRORPATH );
		shell.customCheckStatus( action , "git -C " + OSPATH + " tag " + TAG + " -a -f -m " + Common.getQuoted( "$P_MESSAGE" ) + " refs/heads/" + BRANCH + " " + REVMARK );
	}

	public String getMirrorTagStatus( String TAG ) throws Exception {
		if( !checkTagExists( TAG ) )
			return( "" );

		String REPOVERSION;
		if( shell.isWindows() ) {
			String WINPATH = Common.getWinPath( MIRRORPATH );
			String[] lines = shell.customGetLines( action , "git -C " + WINPATH + " show --format=raw " + TAG );
			String[] grep = Common.grep( lines , "commit " );
			REPOVERSION = ( grep.length > 0 )? grep[ 0 ] : "";
			REPOVERSION = Common.getListItem( REPOVERSION , " " , 1 );
		}
		else {
			REPOVERSION = shell.customGetValue( action , "git -C " + MIRRORPATH + " show --format=raw " + TAG + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		}
		
		return( REPOVERSION ); 
	}

	public String getMirrorBranchStatus( String BRANCH ) throws Exception {
		if( !checkBranchExists( BRANCH ) )
			return( "" );

		String REPOVERSION;
		if( shell.isWindows() ) {
			String WINPATH = Common.getWinPath( MIRRORPATH );
			String[] lines = shell.customGetLines( action , "git -C " + WINPATH + " show --format=raw " + BRANCH );
			String[] grep = Common.grep( lines , "commit " );
			REPOVERSION = ( grep.length > 0 )? grep[ 0 ] : "";
			REPOVERSION = Common.getListItem( REPOVERSION , " " , 1 );
		}
		else {
			REPOVERSION = shell.customGetValue( action , "git -C " + MIRRORPATH + " show --format=raw " + BRANCH + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		}
		return( REPOVERSION ); 
	}

}
