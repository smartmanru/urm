package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.product.MetaSourceProject;

public class ProjectVersionControl {

	ActionBase action;
	Artefactory artefactory;
	
	public ProjectVersionControl( ActionBase action ) {
		this.action = action;
		this.artefactory = action.artefactory;
	}

	private GenericVCS getVCS( MetaSourceProject project ) throws Exception {
		return( getVCS( project , null ) );
	}
	
	private GenericVCS getVCS( MetaSourceProject project , ProjectBuilder builder ) throws Exception {
		MirrorRepository repo = project.getMirror( action );
		return( GenericVCS.getVCS( action , project.meta , repo.RESOURCE_ID , false , builder ) );
	}

	public String checkDefaultBranch( GenericVCS vcs , String BRANCH ) {
		if( BRANCH.equals( "master" ) || BRANCH.equals( "trunk" ) )
			return( vcs.getMainBranch() );
		return( BRANCH );
	}

	public String[] listBranches( MetaSourceProject project ) throws Exception {
		GenericVCS vcs = getVCS( project );
		return( vcs.getBranches( project ) );
	}
	
	public String[] listTags( MetaSourceProject project ) throws Exception {
		GenericVCS vcs = getVCS( project );
		return( vcs.getTags( project ) );
	}
	
	public boolean checkout( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH ) {
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
		return( res );
	}
	
	public boolean commit( MetaSourceProject project , String BRANCH , LocalFolder PATCHFOLDER , String MESSAGE ) {
		try {
			action.info( "commit PATCHPATH=" + PATCHFOLDER.folderPath + ", PROJECT=" + project.NAME + ", MESSAGE=" + MESSAGE + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.commit( project , BRANCH , PATCHFOLDER , MESSAGE ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean copyBranchToBranch( MetaSourceProject project , String branchFrom , String branchTo , boolean deleteOld ) {
		try {
			action.info( "copyBranchToNewBranch PROJECT=" + project.NAME + ", branchFrom=" + branchFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			branchFrom = checkDefaultBranch( vcs , branchFrom );
			branchTo = checkDefaultBranch( vcs , branchTo );
			return( vcs.copyBranchToBranch( project , branchFrom , branchTo , deleteOld ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean renameBranchToBranch( MetaSourceProject project , String branchFrom , String branchTo , boolean deleteOld ) {
		try {
			action.info( "renameBranchToNewBranch PROJECT=" + project.NAME + ", branchFrom=" + branchFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			branchFrom = checkDefaultBranch( vcs , branchFrom );
			branchTo = checkDefaultBranch( vcs , branchTo );
			return( vcs.renameBranchToBranch( project , branchFrom , branchTo , deleteOld ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean copyTagToTag( MetaSourceProject project , String tagFrom , String tagTo , boolean deleteOld ) {
		try {
			action.info( "copyTagToTag PROJECT=" + project.NAME + ", tagFrom=" + tagFrom + ", tagTo=" + tagTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.copyTagToTag( project , tagFrom , tagTo , deleteOld ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean renameTagToTag( MetaSourceProject project , String tagFrom , String tagTo , boolean deleteOld ) {
		try {
			action.info( "renameTagToTag PROJECT=" + project.NAME + ", tagFrom=" + tagFrom + ", tagTo=" + tagTo + " ..." );
			GenericVCS vcs = getVCS( project );
			return( vcs.renameTagToTag( project , tagFrom , tagTo , deleteOld ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

	public boolean copyTagToBranch( MetaSourceProject project , String tagFrom , String branchTo , boolean deleteOld ) {
		try {
			action.info( "copyTagToNewBranch PROJECT=" + project.NAME + ", tagFrom=" + tagFrom + ", branchTo=" + branchTo + " ..." );
			GenericVCS vcs = getVCS( project );
			branchTo = checkDefaultBranch( vcs , branchTo );
			return( vcs.copyTagToBranch( project , tagFrom , branchTo , deleteOld ) );
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

	public boolean export( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH , String TAG , String SINGLEFILE , ProjectBuilder builder ) {
		boolean res = false;
		try {
			action.info( "export PROJECT=" + project.NAME + ", BRANCH=" + BRANCH + ", TAG=" + TAG + ", singlefile=" + SINGLEFILE + " ..." );
			GenericVCS vcs = getVCS( project , builder );
			BRANCH = checkDefaultBranch( vcs , BRANCH );
			res = vcs.export( project , PATCHFOLDER , BRANCH , TAG , SINGLEFILE );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( res );
	}

	public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String branchDate , boolean deleteOld ) {
		try {
			action.info( "setTag PROJECT=" + project.NAME + ", BRANCH=" + BRANCH + ", TAG=" + TAG + ", branchDate=" + branchDate + " ..." );
			GenericVCS vcs = getVCS( project );
			BRANCH = checkDefaultBranch( vcs , BRANCH );
			return( vcs.setTag( project , BRANCH , TAG , branchDate , deleteOld ) );
		}
		catch( Throwable e ) {
			action.handle( e );
		}
		return( false );
	}

}

