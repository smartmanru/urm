package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.vcs.ProjectVersionControl;

public class ActionCommitCodebase extends ActionBase {

	LocalFolder CODIR;
	String MESSAGE; 
	
	public ActionCommitCodebase( ActionBase action , String stream , LocalFolder CODIR , String MESSAGE ) {
		super( action , stream );
		this.CODIR = CODIR;
		this.MESSAGE = MESSAGE;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this , false );
		LocalFolder COPATH = CODIR.getSubFolder( this , scopeProject.sourceProject.PROJECT );
		vcs.commit( COPATH , scopeProject.sourceProject , MESSAGE );
		return( true );
	}
	
}
