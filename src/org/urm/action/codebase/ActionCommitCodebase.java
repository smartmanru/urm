package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.vcs.ProjectVersionControl;

public class ActionCommitCodebase extends ActionBase {

	LocalFolder CODIR;
	String MESSAGE; 
	
	public ActionCommitCodebase( ActionBase action , String stream , LocalFolder CODIR , String MESSAGE ) {
		super( action , stream , "Codebase commit, dir=" + action.getLocalPath( CODIR.folderPath ) );
		this.CODIR = CODIR;
		this.MESSAGE = MESSAGE;
	}

	@Override 
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget scopeProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this );
		LocalFolder COPATH = CODIR.getSubFolder( this , scopeProject.sourceProject.NAME );
		String BRANCH = scopeProject.sourceProject.getDefaultBranch( this );
		if( !vcs.commit( scopeProject.sourceProject , BRANCH , COPATH , MESSAGE ) )
			super.ifexit1( _Error.UnableCommitProjectCodebase1 , "Unable to commit codebase project=" + scopeProject.sourceProject.NAME , scopeProject.sourceProject.NAME );
		return( SCOPESTATE.RunSuccess );
	}
	
}
