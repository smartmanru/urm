package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.vcs.ProjectVersionControl;

public class ActionDropCodebase extends ActionBase {

	boolean branchVAR1;
	String VAR1;
	
	public ActionDropCodebase( ActionBase action , String stream , boolean branchVAR1 , String VAR1 ) {
		super( action , stream );
		this.branchVAR1 = branchVAR1;
		this.VAR1 = VAR1;
	}

	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this , false );
		if( branchVAR1 == true )
			vcs.dropBranch( scopeProject.sourceProject , VAR1 );
		else
		if( branchVAR1 == false )
			vcs.dropTag( scopeProject.sourceProject , VAR1 );
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
