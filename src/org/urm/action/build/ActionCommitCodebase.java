package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
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

	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget scopeProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this );
		LocalFolder COPATH = CODIR.getSubFolder( this , scopeProject.sourceProject.NAME );
		String BRANCH = scopeProject.sourceProject.getDefaultBranch( this );
		vcs.commit( scopeProject.sourceProject , BRANCH , COPATH , MESSAGE );
		return( SCOPESTATE.RunSuccess );
	}
	
}
