package org.urm.server.action.build;

import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.storage.LocalFolder;
import org.urm.server.vcs.ProjectVersionControl;

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
