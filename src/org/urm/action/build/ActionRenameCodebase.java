package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.vcs.ProjectVersionControl;

public class ActionRenameCodebase extends ActionBase {

	boolean branchVAR1;
	String VAR1;
	boolean branchVAR2;
	String VAR2;
	boolean force;
	
	public ActionRenameCodebase( ActionBase action , String stream , boolean branchVAR1 , String VAR1 , boolean branchVAR2 , String VAR2 , boolean force ) {
		super( action , stream );
		this.branchVAR1 = branchVAR1;
		this.VAR1 = VAR1;
		this.branchVAR2 = branchVAR2;
		this.VAR2 = VAR2;
	}

	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this );
		if( branchVAR1 == true && branchVAR2 == true && force == false )
			vcs.renameBranchToNewBranch( scopeProject.sourceProject , VAR1 , VAR2 );
		else
		if( branchVAR1 == false && branchVAR2 == false && force == true )
			vcs.renameTagToTag( scopeProject.sourceProject , VAR1 , VAR2 );
		else
			exitNotImplemented();
		return( SCOPESTATE.RunSuccess );
	}
}
