package ru.egov.urm.vcs;

import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.Artefactory;
import ru.egov.urm.storage.LocalFolder;

public class ProjectVersionControl {

	ActionBase action;
	Artefactory artefactory;
	
	public ProjectVersionControl( ActionBase action ) {
		this.action = action;
		this.artefactory = action.artefactory;
	}
	
	private GenericVCS getVCS( MetaSourceProject project ) throws Exception {
		return( artefactory.getVCS( action , project.getVCS( action ) ) );
	}
	
	public boolean checkout( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH ) {
		try {
			action.log( "checkout PATCHPATH=" + PATCHFOLDER.folderPath + ", PROJECT=" + project.PROJECT + ", BRANCH=" + BRANCH + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.checkout( PATCHFOLDER , project , BRANCH ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}
	
	public boolean commit( LocalFolder PATCHFOLDER , MetaSourceProject project , String MESSAGE ) {
		try {
			action.log( "commit PATCHPATH=" + PATCHFOLDER.folderPath + ", PROJECT=" + project.PROJECT + ", MESSAGE=" + MESSAGE + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.commit( PATCHFOLDER , project , MESSAGE ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean copyBranchToNewBranch( MetaSourceProject project , String branchFrom , String branchTo ) {
		try {
			action.log( "copyBranchToNewBranch PROJECT=" + project.PROJECT + ", branchFrom=" + branchFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.copyBranchToNewBranch( project , branchFrom , branchTo ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean renameBranchToNewBranch( MetaSourceProject project , String branchFrom , String branchTo ) {
		try {
			action.log( "renameBranchToNewBranch PROJECT=" + project.PROJECT + ", branchFrom=" + branchFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.renameBranchToNewBranch( project , branchFrom , branchTo ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean copyTagToNewTag( MetaSourceProject project , String tagFrom , String tagTo ) {
		try {
			action.log( "copyTagToNewTag PROJECT=" + project.PROJECT + ", tagFrom=" + tagFrom + ", tagTo=" + tagTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.copyTagToNewTag( project , tagFrom , tagTo ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean copyTagToTag( MetaSourceProject project , String tagFrom , String tagTo ) {
		try {
			action.log( "copyTagToTag PROJECT=" + project.PROJECT + ", tagFrom=" + tagFrom + ", tagTo=" + tagTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.copyTagToTag( project , tagFrom , tagTo ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean renameTagToTag( MetaSourceProject project , String tagFrom , String tagTo ) {
		try {
			action.log( "renameTagToTag PROJECT=" + project.PROJECT + ", tagFrom=" + tagFrom + ", tagTo=" + tagTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.renameTagToTag( project , tagFrom , tagTo ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean copyTagToNewBranch( MetaSourceProject project , String tagFrom , String branchTo ) {
		try {
			action.log( "copyTagToNewBranch PROJECT=" + project.PROJECT + ", tagFrom=" + tagFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.copyTagToNewBranch( project , tagFrom , branchTo ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean dropTag( MetaSourceProject project , String TAG ) {
		try {
			action.log( "dropTag PROJECT=" + project.PROJECT + ", TAG=" + TAG + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.dropTag( project , TAG ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean dropBranch( MetaSourceProject project , String BRANCH ) {
		try {
			action.log( "dropBranch PROJECT=" + project.PROJECT + ", BRANCH=" + BRANCH + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.dropBranch( project , BRANCH ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean export( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH , String TAG , String SINGLEFILE ) {
		try {
			action.log( "export PROJECT=" + project.PROJECT + ", BRANCH=" + BRANCH + ", TAG=" + TAG + ", singlefile=" + SINGLEFILE + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.export( PATCHFOLDER , project , BRANCH , TAG , SINGLEFILE ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

	public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String branchDate ) {
		try {
			action.log( "setTag PROJECT=" + project.PROJECT + ", BRANCH=" + BRANCH + ", TAG=" + TAG + ", branchDate=" + branchDate + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.setTag( project , BRANCH , TAG , branchDate ) );
		}
		catch( Throwable e ) {
			action.log( e );
		}
		return( false );
	}

}

