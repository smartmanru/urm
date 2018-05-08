package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.product.MetaSourceProject;

public class GitProjectRepo {
	
	public MetaSourceProject project;
	private MirrorCaseGit mc;
	
	protected ShellExecutor shell;
	protected ActionBase action;
	
	public GitProjectRepo( GitVCS vcs , MirrorRepository mirror , MetaSourceProject project , String BRANCH ) {
		mc = new MirrorCaseGit( vcs , mirror , BRANCH );
		this.project = project;
		this.shell = vcs.shell;
		this.action = vcs.action;
	}

	public MirrorRepository getRepository() {
		return( mc.mirror );
	}
	
	public void refreshRepository() throws Exception {
		mc.refreshRepository();
	}
	
	public void refreshMirror() throws Exception {
		mc.refreshMirror();
	}
	
	public void pushRepository() throws Exception {
		mc.pushRepository();
	}
	
	public void pushMirror() throws Exception {
		mc.pushMirror();
	}
	
	public String getBareOSPath() throws Exception {
		return( mc.getBareOSPath() );
	}
	
	public void createLocalFromBranch( LocalFolder checkoutFolder , String BRANCH ) throws Exception {
		mc.createLocalFromBranch( checkoutFolder , BRANCH );
	}
	
	public void addModified( LocalFolder checkoutFolder ) throws Exception {
		mc.addModified( checkoutFolder );
	}
	
	public String[] getBranches() throws Exception {
		return( mc.getBranches() );
	}
	
	public String[] getTags() throws Exception {
		return( mc.getTags() );
	}
	
	public boolean checkBranchExists( String BRANCH ) throws Exception {
		return( mc.checkValidBranch( BRANCH ) );
	}
	
	public void copyMirrorBranchFromBranch( String BRANCH_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		String OSPATH = mc.getBareOSPath();
		String GITBRANCH_FROM = mc.vcsGit.getGitBranchName( BRANCH_FROM );
		String GITBRANCH_TO = mc.vcsGit.getGitBranchName( BRANCH_TO );
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch " + GITBRANCH_TO + " refs/heads/" + GITBRANCH_FROM );
	}

	public void dropMirrorBranch( String BRANCH ) throws Exception {
		String OSPATH = mc.getBareOSPath();
		String GITBRANCH = mc.vcsGit.getGitBranchName( BRANCH );
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch -D " + GITBRANCH );
	}

	public boolean checkTagExists( String TAG ) throws Exception {
		String STATUS;
		String OSPATH = mc.getBareOSPath();
		String GITTAG = mc.vcsGit.getGitTagName( TAG );
		STATUS = shell.customGetValue( action , "git -C " + OSPATH + " tag -l " + GITTAG );
		
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	public void dropMirrorTag( String TAG ) throws Exception {
		String OSPATH = mc.getBareOSPath();
		String GITTAG = mc.vcsGit.getGitTagName( TAG );
		shell.customCheckStatus( action , "git -C " + OSPATH + " tag -d " + GITTAG );
	}

	public void copyMirrorTagFromTag( String TAG_FROM , String TAG_TO , String MESSAGE ) throws Exception {
		String OSPATH = mc.getBareOSPath();
		String GITTAG_FROM = mc.vcsGit.getGitTagName( TAG_FROM );
		String GITTAG_TO = mc.vcsGit.getGitTagName( TAG_TO );
		shell.customCheckStatus( action , "git -C " + OSPATH + " tag -a -f -m " + Common.getQuoted( MESSAGE ) + " " + GITTAG_TO + " refs/tags/" + GITTAG_FROM );
	}

	public void copyMirrorBranchFromTag( String TAG_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		String OSPATH = mc.getBareOSPath();
		String GITTAG_FROM = mc.vcsGit.getGitTagName( TAG_FROM );
		String GITBRANCH_TO = mc.vcsGit.getGitBranchName( BRANCH_TO );
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch " + GITBRANCH_TO + " refs/tags/" + GITTAG_FROM );
	}

	public boolean exportFromBranch( LocalFolder PATCHFOLDER , String BRANCH , String FILEPATH , String FILENAME ) throws Exception {
		String GITBRANCH = mc.vcsGit.getGitBranchName( BRANCH );
		boolean res = mc.exportFromPath( PATCHFOLDER , GITBRANCH , FILEPATH , FILENAME );
		return( res );
	}

	public boolean exportFromTag( LocalFolder PATCHFOLDER , String TAG , String FILEPATH , String FILENAME ) throws Exception {
		String GITTAG = mc.vcsGit.getGitTagName( TAG );
		boolean res = mc.exportFromPath( PATCHFOLDER , GITTAG , FILEPATH , FILENAME );
		return( res );
	}

	public void setMirrorTag( String BRANCH , String TAG , String MESSAGE , String TAGDATE ) throws Exception {
		String GITBRANCH = mc.vcsGit.getGitBranchName( BRANCH );
		String GITTAG = mc.vcsGit.getGitTagName( TAG );
		
		// get revision by date
		String REVMARK = "";
		String OSPATH = mc.getBareOSPath();
		if( !TAGDATE.isEmpty() ) {
			if( shell.isWindows() ) {
				REVMARK = shell.customGetValue( action , "git -C " + OSPATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + 
						" refs/heads/" + GITBRANCH );
				REVMARK = Common.getListItem( REVMARK , " " , 0 );
			}
			else {
				REVMARK = shell.customGetValue( action , "git -C " + OSPATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + " refs/heads/" + 
						GITBRANCH + " | tr -d " + Common.getQuoted( " " ) + " -f1" );
			}
			if( REVMARK.isEmpty() )
				action.exit0( _Error.MissingBranchDateRevision0 , "setMirrorTag: unable to find branch revision on given date" );
		}

		shell.customCheckStatus( action , "git -C " + OSPATH + " tag " + GITTAG + " -a -f -m " + Common.getQuoted( "$P_MESSAGE" ) + " refs/heads/" + GITBRANCH + " " + REVMARK );
	}

	public String getMirrorTagStatus( String TAG ) throws Exception {
		if( !checkTagExists( TAG ) )
			return( "" );

		String GITTAG = mc.vcsGit.getGitTagName( TAG );
		String REPOVERSION;
		String OSPATH = mc.getBareOSPath();
		if( shell.isWindows() ) {
			String[] lines = shell.customGetLines( action , "git -C " + OSPATH + " show --format=raw " + GITTAG );
			String[] grep = Common.grep( lines , "commit " );
			REPOVERSION = ( grep.length > 0 )? grep[ 0 ] : "";
			REPOVERSION = Common.getListItem( REPOVERSION , " " , 1 );
		}
		else {
			REPOVERSION = shell.customGetValue( action , "git -C " + OSPATH + " show --format=raw " + GITTAG + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		}
		
		return( REPOVERSION ); 
	}

	public String getMirrorBranchStatus( String BRANCH ) throws Exception {
		if( !checkBranchExists( BRANCH ) )
			return( "" );

		String GITBRANCH = mc.vcsGit.getGitBranchName( BRANCH );
		String REPOVERSION;
		String OSPATH = mc.getBareOSPath();
		if( shell.isWindows() ) {
			String[] lines = shell.customGetLines( action , "git -C " + OSPATH + " show --format=raw " + GITBRANCH );
			String[] grep = Common.grep( lines , "commit " );
			REPOVERSION = ( grep.length > 0 )? grep[ 0 ] : "";
			REPOVERSION = Common.getListItem( REPOVERSION , " " , 1 );
		}
		else {
			REPOVERSION = shell.customGetValue( action , "git -C " + OSPATH + " show --format=raw " + GITBRANCH + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		}
		return( REPOVERSION ); 
	}

}
