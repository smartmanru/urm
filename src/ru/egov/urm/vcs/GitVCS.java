package ru.egov.urm.vcs;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.Folder;
import ru.egov.urm.storage.GitMirrorStorage;
import ru.egov.urm.storage.LocalFolder;

public class GitVCS extends GenericVCS {

	static String MASTERBRANCH = "master";

	boolean build;
	
	class GitRepo {
		public MetaSourceProject project;
		public GitMirrorStorage storage;
		public Folder PATCHFOLDER;
		public String MIRRORPATH;
		
		GitRepo( MetaSourceProject project , GitMirrorStorage storage , Folder PATCHFOLDER ) {
			this.project = project;
			this.storage = storage;
			this.PATCHFOLDER = PATCHFOLDER;
			this.MIRRORPATH = storage.mirrorFolder.folderPath;
		}
	}
	
	public GitVCS( ActionBase action , boolean build ) {
		super( action );
		this.build = build;
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
		GitRepo repo = getRepo( project , PATCHFOLDER );
		refreshMirror( repo );

		String REPOVERSION = "(branch head)";

		action.log( "git: checkout sources from " + repo.MIRRORPATH + " (branch=" + BRANCH + ", revision=" + REPOVERSION + ") to " + PATCHFOLDER.folderPath + " ..." );
		createLocalFromBranch( repo , BRANCH );
		
		return( true );
	}

	@Override public boolean commit( LocalFolder PATCHFOLDER , MetaSourceProject project , String MESSAGE ) throws Exception {
		if( !PATCHFOLDER.checkExists( action ) ) {
			action.log( "directory " + PATCHFOLDER.folderPath + " does not exist " );
			return( false );
		}

		GitRepo repo = getRepo( project , PATCHFOLDER );
		refreshMirror( repo );
		
		// automatically add modified and push
		addModified( repo );
		pushMirror( repo );
		
		return( true );
	}

	@Override public boolean copyBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		BRANCH1 = getBranchName( BRANCH1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		if( !checkBranchExists( repo , BRANCH1 ) ) {
			action.log( repo.MIRRORPATH + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( checkBranchExists( repo , BRANCH2 ) ) {
			action.log( "skip copy branch to branch - target branch already exists" );
			return( false );
		}

		copyMirrorBranchFromBranch( repo , BRANCH1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + BRANCH1 );
		pushMirror( repo );
		return( true );
	}

	@Override public boolean renameBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		BRANCH1 = getBranchName( BRANCH1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		if( !checkBranchExists( repo , BRANCH1 ) ) {
			action.log( repo.MIRRORPATH + ": branch " + BRANCH1 + " does not exist" );
			return( false );
		}

		if( checkBranchExists( repo , BRANCH2 ) ) {
			action.log( "skip rename branch to branch - target branch already exists" );
			return( false );
		}

		copyMirrorBranchFromBranch( repo , BRANCH1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: rename branch " + BRANCH1 + " to " + BRANCH2 );
		dropMirrorBranch( repo , BRANCH1 );
		pushMirror( repo );
		return( true );
	}

	@Override public boolean copyTagToNewTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		if( !checkTagExists( repo , TAG1 ) ) {
			action.log( repo.MIRRORPATH + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( checkTagExists( repo , TAG2 ) ) {
			action.log( repo.MIRRORPATH + ": tag " + TAG2 + " exists" );
			return( false );
		}

		copyMirrorTagFromTag( repo , TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag from " + TAG1 );
		pushMirror( repo );
		return( true );
	}

	@Override public boolean copyTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		if( !checkTagExists( repo , TAG1 ) ) {
			action.log( repo.MIRRORPATH + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( checkTagExists( repo , TAG2 ) ) {
			// drop tag
			dropMirrorTag( repo , TAG2 );
			pushMirror( repo );
		}

		copyMirrorTagFromTag( repo , TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag " + TAG2 + " from " + TAG1 );
		pushMirror( repo );
		return( true );
	}

	@Override public boolean renameTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		TAG2 = getTagName( TAG2 );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		if( !checkTagExists( repo , TAG1 ) ) {
			action.log( repo + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( checkTagExists( repo , TAG2 ) ) {
			// drop tag
			dropMirrorTag( repo , TAG2 );
			pushMirror( repo );
		}

		copyMirrorTagFromTag( repo , TAG1 , TAG2 , meta.product.CONFIG_ADM_TRACKER + "-0000: rename tag " + TAG1 + " to " + TAG2 );
		dropMirrorTag( repo , TAG1 );
		pushMirror( repo );
		return( true );
	}

	@Override public boolean copyTagToNewBranch( MetaSourceProject project , String TAG1 , String BRANCH2 ) throws Exception {
		TAG1 = getTagName( TAG1 );
		BRANCH2 = getBranchName( BRANCH2 );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		if( !checkTagExists( repo , TAG1 ) ) {
			action.log( repo + ": tag " + TAG1 + " does not exist" );
			return( false );
		}

		if( checkBranchExists( repo , BRANCH2 ) ) {
			action.log( "skip copy branch to branch - target branch already exists" );
			return( false );
		}

		copyMirrorBranchFromTag( repo , TAG1 , BRANCH2 , meta.product.CONFIG_ADM_TRACKER + "-0000: create branch " + BRANCH2 + " from " + TAG1 );
		pushMirror( repo );
		return( true );
	}

	@Override public boolean dropTag( MetaSourceProject project , String TAG ) throws Exception {
		TAG = getTagName( TAG );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		if( !checkTagExists( repo , TAG ) ) {
			action.log( repo.MIRRORPATH + ": tag " + TAG + " does not exist" );
			return( false );
		}
		
		// drop tag
		dropMirrorTag( repo , TAG );
		pushMirror( repo );
		return( true );
	}
	
	@Override public boolean dropBranch( MetaSourceProject project , String BRANCH ) throws Exception {
		BRANCH = getBranchName( BRANCH );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		if( !checkBranchExists( repo , BRANCH ) ) {
			action.log( repo.MIRRORPATH + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		// drop branch
		dropMirrorBranch( repo , BRANCH );
		pushMirror( repo );
		return( true );
	}

	@Override public boolean export( Folder PATCHFOLDER , MetaSourceProject project , String BRANCH , String TAG , String FILENAME ) throws Exception {
		TAG = getTagName( TAG );
		BRANCH = getBranchName( BRANCH );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );
		
		boolean res;
		String FILEPATH = project.CODEPATH;
		String FILEBASE = "";
		if( !FILENAME.isEmpty() ) {
			FILEPATH = Common.getPath( FILEPATH , Common.getDirName( FILENAME ) );
			FILEBASE = Common.getBaseName( FILENAME );
		}
		if( !TAG.isEmpty() )
			res = exportFromTag( repo , PATCHFOLDER , TAG , FILEPATH , FILEBASE );
		else
			res = exportFromBranch( repo , PATCHFOLDER , BRANCH , FILEPATH , FILEBASE );
		
		return( res );
	}

	@Override public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String BRANCHDATE ) throws Exception {
		TAG = getTagName( TAG );
		BRANCH = getBranchName( BRANCH );
		GitRepo repo = getRepo( project , null );
		refreshMirror( repo );

		String CO_BRANCH = BRANCH;
		if( CO_BRANCH.startsWith( "branches/" ) )
			CO_BRANCH = CO_BRANCH.substring( "branches/".length() );

		if( !checkBranchExists( repo , BRANCH ) ) {
			action.log( repo.MIRRORPATH + ": branch " + BRANCH + " does not exist" );
			return( false );
		}
		
		setMirrorTag( repo , CO_BRANCH , TAG , meta.product.CONFIG_ADM_TRACKER + "-0000: create tag" , BRANCHDATE );
		pushMirror( repo );
		return( true );
	}
	
	// implementation
	private GitRepo getRepo( MetaSourceProject project , Folder PATCHFOLDER ) throws Exception {
		GitMirrorStorage storage = action.artefactory.getGitMirrorStorage( action , project , build );
		GitRepo repo = new GitRepo( project , storage , PATCHFOLDER );
		return( repo );
	}

	private GitRepo getRepo( String REPOSITORY ) throws Exception {
		GitMirrorStorage storage = action.artefactory.getGitMirrorStorage( action , REPOSITORY );
		GitRepo repo = new GitRepo( null , storage , null );
		return( repo );
	}

	private void refreshMirror( GitRepo repo ) throws Exception {
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " fetch origin" );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " fetch origin" );
		}
	}

	private void createLocalFromBranch( GitRepo repo , String BRANCH ) throws Exception {
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			String WINPATHPROJECT = Common.getWinPath( action , repo.PATCHFOLDER.folderPath );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " clone " + WINPATH + " --shared -b " + BRANCH + " " + WINPATHPROJECT );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " clone " + repo.MIRRORPATH + " --shared -b " + BRANCH + " " + repo.PATCHFOLDER.folderPath );
		}
	}

	private boolean exportFromPath( GitRepo repo , Folder PATCHFOLDER , String BRANCHTAG , String SUBPATH , String FILENAME ) throws Exception {
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
				if( repo.storage.winBuild ) {
					String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
					String WINPATHPROJECT = Common.getWinPath( action , PATCHFOLDER.folderPath );
					ShellExecutor shell = action.getShell( repo.storage.account );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							" . | ( cd " + WINPATHPROJECT + " & tar x --exclude pax_global_header)" );
				}
				else {
					session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " archive " + BRANCHTAG + " " + 
						" . | ( cd " + PATCHFOLDER.folderPath + "; tar x )" );
				}
			}
			else {
				int COMPS = Common.getDirCount( SUBPATH ) + 1;
				String STRIPOPTION = "--strip-components=" + COMPS;
				
				if( repo.storage.winBuild ) {
					String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
					String WINPATHPROJECT = Common.getWinPath( action , PATCHFOLDER.folderPath );
					String WINPATHSUB = Common.getWinPath( action , SUBPATH );
					ShellExecutor shell = action.getShell( repo.storage.account );
					shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
							WINPATHSUB + " | ( cd " + WINPATHPROJECT + " & tar x " + WINPATHSUB + " " + STRIPOPTION + " )" );
				}
				else {
					session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " archive " + BRANCHTAG + " " + 
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
			
			if( repo.storage.winBuild ) {
				String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
				String WINPATHBASE = Common.getWinPath( action , BASEDIR.folderPath );
				String WINPATHFILE = Common.getWinPath( action , FILEPATH );
				ShellExecutor shell = action.getShell( repo.storage.account );
				shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + BRANCHTAG + " " + 
						WINPATHFILE + " | ( cd " + WINPATHBASE + " & tar x --exclude pax_global_header " + WINPATHFILE + " " + STRIPOPTION + " )" );
			}
			else {
				session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " archive " + BRANCHTAG + " " + 
						FILEPATH + " | ( cd " + BASEDIR.folderPath + "; tar x " + FILEPATH + " " + STRIPOPTION + " )" );
			}
			if( !FILENAME.equals( BASENAME ) )
				BASEDIR.moveFileToFolder( action , FILENAME , BASENAME );
		}

		return( true );
	}

	private boolean exportFromBranch( GitRepo repo , Folder PATCHFOLDER , String BRANCH , String FILEPATH , String FILENAME ) throws Exception {
		boolean res = exportFromPath( repo , PATCHFOLDER , BRANCH , FILEPATH , FILENAME );
		return( res );
	}

	private boolean exportFromTag( GitRepo repo , Folder PATCHFOLDER , String TAG , String FILEPATH , String FILENAME ) throws Exception {
		boolean res = exportFromPath( repo , PATCHFOLDER , TAG , FILEPATH , FILENAME );
		return( res );
	}

	private void setMirrorTag( GitRepo repo , String BRANCH , String TAG , String MESSAGE , String TAGDATE ) throws Exception {
		// get revision by date
		String REVMARK = "";
		if( !TAGDATE.isEmpty() ) {
			if( repo.storage.winBuild ) {
				String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
				ShellExecutor shell = action.getShell( repo.storage.account );
				REVMARK = shell.customGetValue( action , "git -C " + WINPATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + 
						" refs/heads/" + BRANCH );
				REVMARK = Common.getListItem( REVMARK , " " , 0 );
			}
			else {
				REVMARK = session.customGetValue( action , "git -C " + repo.MIRRORPATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + " refs/heads/" + 
						BRANCH + " | tr -d " + Common.getQuoted( " " ) + " -f1" );
			}
			if( REVMARK.isEmpty() )
				action.exit( "setMirrorTag: unable to find branch revision on given date" );
		}

		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " tag " + TAG + " -a -f -m " + Common.getQuoted( "$P_MESSAGE" ) + " refs/heads/" + BRANCH + " " + REVMARK );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " tag " + TAG + " -a -f -m " + Common.getQuoted( "$P_MESSAGE" ) + " refs/heads/" + BRANCH + " " + REVMARK );
		}
	}

	private void dropMirrorTag( GitRepo repo , String TAG ) throws Exception {
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " tag -d " + TAG );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " tag -d " + TAG );
		}
	}

	private void dropMirrorBranch( GitRepo repo , String BRANCH ) throws Exception {
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " branch -D " + BRANCH );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " branch -D " + BRANCH );
		}
	}

	private boolean checkTagExists( GitRepo repo , String TAG ) throws Exception {
		String STATUS;
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			STATUS = shell.customGetValue( action , "git -C " + WINPATH + " tag -l " + TAG );
		}
		else {
			STATUS = session.customGetValue( action , "git -C " + repo.MIRRORPATH + " tag -l " + TAG );
		}
		
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	private boolean checkBranchExists( GitRepo repo , String BRANCH ) throws Exception {
		String STATUS;
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			STATUS = shell.customGetValue( action , "git -C " + WINPATH + " branch --list " + BRANCH );
		}
		else {
			STATUS = session.customGetValue( action , "git -C " + repo.MIRRORPATH + " branch --list " + BRANCH );
		}
		
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	@SuppressWarnings("unused")
	private String getMirrorTagStatus( GitRepo repo , String TAG ) throws Exception {
		if( !checkTagExists( repo , TAG ) )
			return( "" );

		String REPOVERSION;
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			String[] lines = shell.customGetLines( action , "git -C " + WINPATH + " show --format=raw " + TAG );
			String[] grep = Common.grep( lines , "commit " );
			REPOVERSION = ( grep.length > 0 )? grep[ 0 ] : "";
			REPOVERSION = Common.getListItem( REPOVERSION , " " , 1 );
		}
		else {
			REPOVERSION = session.customGetValue( action , "git -C " + repo.MIRRORPATH + " show --format=raw " + TAG + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		}
		
		return( REPOVERSION ); 
	}

	@SuppressWarnings("unused")
	private String getMirrorBranchStatus( GitRepo repo , String BRANCH ) throws Exception {
		if( !checkBranchExists( repo , BRANCH ) )
			return( "" );

		String REPOVERSION;
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			String[] lines = shell.customGetLines( action , "git -C " + WINPATH + " show --format=raw " + BRANCH );
			String[] grep = Common.grep( lines , "commit " );
			REPOVERSION = ( grep.length > 0 )? grep[ 0 ] : "";
			REPOVERSION = Common.getListItem( REPOVERSION , " " , 1 );
		}
		else {
			REPOVERSION = session.customGetValue( action , "git -C " + repo.MIRRORPATH + " show --format=raw " + BRANCH + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		}
		return( REPOVERSION ); 
	}

	private void copyMirrorTagFromTag( GitRepo repo , String TAG_FROM , String TAG_TO , String MESSAGE ) throws Exception {
		// drop if exists
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " tag -a -f -m " + Common.getQuoted( MESSAGE ) + " " + TAG_TO + " refs/tags/" + TAG_FROM );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " tag -a -f -m " + Common.getQuoted( MESSAGE ) + " " + TAG_TO + " refs/tags/" + TAG_FROM );
		}
	}

	private void copyMirrorBranchFromTag( GitRepo repo , String TAG_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " branch " + BRANCH_TO + " refs/tags/" + TAG_FROM );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " branch " + BRANCH_TO + " refs/tags/" + TAG_FROM );
		}
	}

	private void copyMirrorBranchFromBranch( GitRepo repo , String BRANCH_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " branch " + BRANCH_TO + " refs/heads/" + BRANCH_FROM );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " branch " + BRANCH_TO + " refs/heads/" + BRANCH_FROM );
		}
	}

	private void pushMirror( GitRepo repo ) throws Exception {
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " push origin" );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " push origin" );
		}
	}

	private void addModified( GitRepo repo ) throws Exception {
		if( repo.storage.winBuild )
			action.exitNotImplemented();
			
		session.customCheckErrorsDebug( action , "( cd " + repo.PATCHFOLDER.folderPath + "; " +
			"F_LIST=`git diff --name-only`; " +
			"if [ " + Common.getQuoted( "$F_LIST" ) + " != " + Common.getQuoted( "" ) + " ]; then git add $F_LIST; fi; " +
			"git commit -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: set version" ) + "; " +
			"git push origin 2>&1; if [ $? != 0 ]; then echo error on push origin >&2; fi )" );
	}

	@Override public boolean isValidRepositoryTagPath( String repository , String TAG , String path ) throws Exception {
		TAG = getTagName( TAG );
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public boolean isValidRepositoryMasterPath( String repository , String path ) throws Exception {
		GitRepo repo = getRepo( repository );
		refreshMirror( repo );
		
		int status;
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			String WINPATHDIR = Common.getWinPath( action , path );
			ShellExecutor shell = action.getShell( repo.storage.account );
			status = shell.customGetStatus( action , "git -C " + WINPATH + " cat-file -e master:" + WINPATHDIR );
		}
		else {
			status = session.customGetStatus( action , "git -C " + repo.MIRRORPATH + " cat-file -e master:" + path );
		}
		
		if( status == 0 )
			return( true );
		
		return( false );
	}

	@Override public boolean exportRepositoryTagPath( LocalFolder PATCHFOLDER , String repository , String TAG , String ITEMPATH , String name ) throws Exception {
		GitRepo repo = getRepo( repository );
		refreshMirror( repo );
		
		TAG = getTagName( TAG );
		boolean res = exportFromPath( repo , PATCHFOLDER.getSubFolder( action , name ) , TAG , ITEMPATH , "" );
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
		
		GitRepo repo = getRepo( repository );
		refreshMirror( repo );
		
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			String WINPATHDIR = Common.getWinPath( action , ITEMPATH );
			String WINPATHPATCH = Common.getWinPath( action , PATCHFOLDER.folderPath );
			ShellExecutor shell = action.getShell( repo.storage.account );
			shell.customCheckStatus( action , "git -C " + WINPATH + " archive " + WINPATHDIR + " . | ( cd " + WINPATHPATCH + " & tar x --exclude pax_global_header)" );
		}
		else {
			session.customCheckStatus( action , "git -C " + repo.MIRRORPATH + " archive " + ITEMPATH + " . | ( cd " + PATCHFOLDER.folderPath + "; tar x )" );
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
	
	@Override public String listMasterItems( String repository , String masterFolder ) throws Exception {
		GitRepo repo = getRepo( repository );
		refreshMirror( repo );
		
		String s;
		if( repo.storage.winBuild ) {
			String WINPATH = Common.getWinPath( action , repo.MIRRORPATH );
			ShellExecutor shell = action.getShell( repo.storage.account );
			s = shell.customGetValue( action , "git -C " + WINPATH + " ls-tree master --name-only" );
			s = Common.replace( s , "\\n" , " \"" );
		}
		else {
			s = session.customGetValue( action , "git -C " + repo.MIRRORPATH + " ls-tree master --name-only | tr \"\\n\" \" \"" );
		}
		return( s );
	}

	@Override public void deleteMasterFolder( String repository , String masterFolder , String commitMessage ) throws Exception {
		GitRepo repo = getRepo( repository );
		refreshMirror( repo );
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
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public void addFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		action.exitNotImplemented();
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
	
}
