package org.urm.engine.vcs;

import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerMirrorRepository;
import org.urm.meta.product.MetaSourceProject;

public class GitProjectRepo extends GitMirrorStorage {
	
	public MetaSourceProject project;
	
	public GitProjectRepo( GitVCS vcs , ServerMirrorRepository mirror , MetaSourceProject project , LocalFolder PATCHFOLDER ) {
		super( vcs , mirror , PATCHFOLDER );
		this.project = project;
	}

	public boolean checkBranchExists( String BRANCH ) throws Exception {
		String OSPATH = super.getBareOSPath();
		String STATUS = shell.customGetValue( action , "git -C " + OSPATH + " branch --list " + BRANCH );
		
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	public void copyMirrorBranchFromBranch( String BRANCH_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		String OSPATH = super.getBareOSPath();
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch " + BRANCH_TO + " refs/heads/" + BRANCH_FROM );
	}

	public void dropMirrorBranch( String BRANCH ) throws Exception {
		String OSPATH = super.getBareOSPath();
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch -D " + BRANCH );
	}

	public boolean checkTagExists( String TAG ) throws Exception {
		String STATUS;
		String OSPATH = super.getBareOSPath();
		STATUS = shell.customGetValue( action , "git -C " + OSPATH + " tag -l " + TAG );
		
		if( STATUS.isEmpty() )
			return( false );
		return( true );
	}
	
	public void dropMirrorTag( String TAG ) throws Exception {
		String OSPATH = super.getBareOSPath();
		shell.customCheckStatus( action , "git -C " + OSPATH + " tag -d " + TAG );
	}

	public void copyMirrorTagFromTag( String TAG_FROM , String TAG_TO , String MESSAGE ) throws Exception {
		String OSPATH = super.getBareOSPath();
		shell.customCheckStatus( action , "git -C " + OSPATH + " tag -a -f -m " + Common.getQuoted( MESSAGE ) + " " + TAG_TO + " refs/tags/" + TAG_FROM );
	}

	public void copyMirrorBranchFromTag( String TAG_FROM , String BRANCH_TO , String MESSAGE ) throws Exception {
		String OSPATH = super.getBareOSPath();
		shell.customCheckStatus( action , "git -C " + OSPATH + " branch " + BRANCH_TO + " refs/tags/" + TAG_FROM );
	}

	public boolean exportFromBranch( String BRANCH , String FILEPATH , String FILENAME ) throws Exception {
		boolean res = exportFromPath( BRANCH , FILEPATH , FILENAME );
		return( res );
	}

	public boolean exportFromTag( String TAG , String FILEPATH , String FILENAME ) throws Exception {
		boolean res = exportFromPath( TAG , FILEPATH , FILENAME );
		return( res );
	}

	public void setMirrorTag( String BRANCH , String TAG , String MESSAGE , String TAGDATE ) throws Exception {
		// get revision by date
		String REVMARK = "";
		String OSPATH = super.getBareOSPath();
		if( !TAGDATE.isEmpty() ) {
			if( shell.isWindows() ) {
				REVMARK = shell.customGetValue( action , "git -C " + OSPATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + 
						" refs/heads/" + BRANCH );
				REVMARK = Common.getListItem( REVMARK , " " , 0 );
			}
			else {
				REVMARK = shell.customGetValue( action , "git -C " + OSPATH + " log --format=oneline -n 1 --before=" + Common.getQuoted( TAGDATE ) + " refs/heads/" + 
						BRANCH + " | tr -d " + Common.getQuoted( " " ) + " -f1" );
			}
			if( REVMARK.isEmpty() )
				action.exit0( _Error.MissingBranchDateRevision0 , "setMirrorTag: unable to find branch revision on given date" );
		}

		shell.customCheckStatus( action , "git -C " + OSPATH + " tag " + TAG + " -a -f -m " + Common.getQuoted( "$P_MESSAGE" ) + " refs/heads/" + BRANCH + " " + REVMARK );
	}

	public String getMirrorTagStatus( String TAG ) throws Exception {
		if( !checkTagExists( TAG ) )
			return( "" );

		String REPOVERSION;
		String OSPATH = super.getBareOSPath();
		if( shell.isWindows() ) {
			String[] lines = shell.customGetLines( action , "git -C " + OSPATH + " show --format=raw " + TAG );
			String[] grep = Common.grep( lines , "commit " );
			REPOVERSION = ( grep.length > 0 )? grep[ 0 ] : "";
			REPOVERSION = Common.getListItem( REPOVERSION , " " , 1 );
		}
		else {
			REPOVERSION = shell.customGetValue( action , "git -C " + OSPATH + " show --format=raw " + TAG + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		}
		
		return( REPOVERSION ); 
	}

	public String getMirrorBranchStatus( String BRANCH ) throws Exception {
		if( !checkBranchExists( BRANCH ) )
			return( "" );

		String REPOVERSION;
		String OSPATH = super.getBareOSPath();
		if( shell.isWindows() ) {
			String[] lines = shell.customGetLines( action , "git -C " + OSPATH + " show --format=raw " + BRANCH );
			String[] grep = Common.grep( lines , "commit " );
			REPOVERSION = ( grep.length > 0 )? grep[ 0 ] : "";
			REPOVERSION = Common.getListItem( REPOVERSION , " " , 1 );
		}
		else {
			REPOVERSION = shell.customGetValue( action , "git -C " + OSPATH + " show --format=raw " + BRANCH + " | grep " + Common.getQuoted( "commit " ) + 
				" | head -1 | cut -d " + Common.getQuoted( " " ) + " -f2" );
		}
		return( REPOVERSION ); 
	}

}
