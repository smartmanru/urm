package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.product.MetaSourceProject;

public class ProjectVersionControl {

	ActionBase action;
	Artefactory artefactory;
	
	public ProjectVersionControl( ActionBase action ) {
		this.action = action;
		this.artefactory = action.artefactory;
	}
	
	private GenericVCS getVCS( MetaSourceProject project ) throws Exception {
		return( GenericVCS.getVCS( action , project.meta , project.getVCS( action ) ) );
	}

	public String checkDefaultBranch( GenericVCS vcs , String BRANCH ) {
		if( BRANCH.equals( "master" ) || BRANCH.equals( "trunk" ) )
			return( vcs.getMainBranch() );
		return( BRANCH );
	}
	
	public boolean checkout( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH ) {
		int timeout = action.setTimeoutUnlimited();
		boolean res = false;
		try {
			action.info( "checkout PATCHPATH=" + PATCHFOLDER.folderPath + ", PROJECT=" + project.NAME + ", BRANCH=" + BRANCH + " ..." );
			GenericVCS vcs = getVCS( project );
			BRANCH = checkDefaultBranch( vcs , BRANCH );
			res = vcs.checkout( project , PATCHFOLDER , BRANCH );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		action.setTimeout( timeout );
		return( res );
	}
	
	public boolean commit( LocalFolder PATCHFOLDER , MetaSourceProject project , String MESSAGE ) {
		try {
			action.info( "commit PATCHPATH=" + PATCHFOLDER.folderPath + ", PROJECT=" + project.NAME + ", MESSAGE=" + MESSAGE + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.commit( project , PATCHFOLDER , MESSAGE ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean copyBranchToNewBranch( MetaSourceProject project , String branchFrom , String branchTo ) {
		try {
			action.info( "copyBranchToNewBranch PROJECT=" + project.NAME + ", branchFrom=" + branchFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			branchFrom = checkDefaultBranch( vcs , branchFrom );
			branchTo = checkDefaultBranch( vcs , branchTo );
			return( vcs.copyBranchToNewBranch( project , branchFrom , branchTo ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean renameBranchToNewBranch( MetaSourceProject project , String branchFrom , String branchTo ) {
		try {
			action.info( "renameBranchToNewBranch PROJECT=" + project.NAME + ", branchFrom=" + branchFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			branchFrom = checkDefaultBranch( vcs , branchFrom );
			branchTo = checkDefaultBranch( vcs , branchTo );
			return( vcs.renameBranchToNewBranch( project , branchFrom , branchTo ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean copyTagToNewTag( MetaSourceProject project , String tagFrom , String tagTo ) {
		try {
			action.info( "copyTagToNewTag PROJECT=" + project.NAME + ", tagFrom=" + tagFrom + ", tagTo=" + tagTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.copyTagToNewTag( project , tagFrom , tagTo ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean copyTagToTag( MetaSourceProject project , String tagFrom , String tagTo ) {
		try {
			action.info( "copyTagToTag PROJECT=" + project.NAME + ", tagFrom=" + tagFrom + ", tagTo=" + tagTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.copyTagToTag( project , tagFrom , tagTo ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean renameTagToTag( MetaSourceProject project , String tagFrom , String tagTo ) {
		try {
			action.info( "renameTagToTag PROJECT=" + project.NAME + ", tagFrom=" + tagFrom + ", tagTo=" + tagTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.renameTagToTag( project , tagFrom , tagTo ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean copyTagToNewBranch( MetaSourceProject project , String tagFrom , String branchTo ) {
		try {
			action.info( "copyTagToNewBranch PROJECT=" + project.NAME + ", tagFrom=" + tagFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			branchTo = checkDefaultBranch( vcs , branchTo );
			return( vcs.copyTagToNewBranch( project , tagFrom , branchTo ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean dropTag( MetaSourceProject project , String TAG ) {
		try {
			action.info( "dropTag PROJECT=" + project.NAME + ", TAG=" + TAG + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.dropTag( project , TAG ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean dropBranch( MetaSourceProject project , String BRANCH ) {
		try {
			action.info( "dropBranch PROJECT=" + project.NAME + ", BRANCH=" + BRANCH + " ..." );
			GenericVCS vcs = getVCS( project );
			BRANCH = checkDefaultBranch( vcs , BRANCH );
			return( vcs.dropBranch( project , BRANCH ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean export( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH , String TAG , String SINGLEFILE ) {
		int timeout = action.setTimeoutUnlimited();
		boolean res = false;
		try {
			action.info( "export PROJECT=" + project.NAME + ", BRANCH=" + BRANCH + ", TAG=" + TAG + ", singlefile=" + SINGLEFILE + " ..." );
			GenericVCS vcs = getVCS( project );
			BRANCH = checkDefaultBranch( vcs , BRANCH );
			res = vcs.export( project , PATCHFOLDER , BRANCH , TAG , SINGLEFILE );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		action.setTimeout( timeout );
		return( res );
	}

	public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String branchDate ) {
		try {
			action.info( "setTag PROJECT=" + project.NAME + ", BRANCH=" + BRANCH + ", TAG=" + TAG + ", branchDate=" + branchDate + " ..." );
			GenericVCS vcs = getVCS( project );
			BRANCH = checkDefaultBranch( vcs , BRANCH );
			return( vcs.setTag( project , BRANCH , TAG , branchDate ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

}

