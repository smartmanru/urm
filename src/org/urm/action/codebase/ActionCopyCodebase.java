package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.vcs.ProjectVersionControl;

public class ActionCopyCodebase extends ActionBase {

	boolean branchVAR1;
	String VAR1;
	boolean branchVAR2;
	String VAR2;
	boolean force;
	
	public ActionCopyCodebase( ActionBase action , String stream , boolean branchVAR1 , String VAR1 , boolean branchVAR2 , String VAR2 , boolean force ) {
		super( action , stream , "Codebase copy src=" + VAR1 + ", dst=" + VAR2 );
		this.branchVAR1 = branchVAR1;
		this.VAR1 = VAR1;
		this.branchVAR2 = branchVAR2;
		this.VAR2 = VAR2;
		this.force = force;
	}

	@Override 
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget scopeProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this );
		boolean res = false;
		if( branchVAR1 == true && branchVAR2 == true && force == false )
			res = vcs.copyBranchToNewBranch( scopeProject.sourceProject , VAR1 , VAR2 );
		else
		if( branchVAR1 == true && branchVAR2 == false && force == true )
			res = vcs.setTag( scopeProject.sourceProject , VAR1 , VAR2 , "" );
		else
		if( branchVAR1 == false && branchVAR2 == true && force == false )
			res = vcs.copyTagToNewBranch( scopeProject.sourceProject , VAR1 , VAR2 );
		else
		if( branchVAR1 == false && branchVAR2 == false && force == false )
			res = vcs.copyTagToNewTag( scopeProject.sourceProject , VAR1 , VAR2 );
		else
		if( branchVAR1 == false && branchVAR2 == false && force == true )
			res = vcs.copyTagToTag( scopeProject.sourceProject , VAR1 , VAR2 );
		else
			exitNotImplemented();
		
		if( !res )
			super.exit1( _Error.UnableChangeProjectCodebase1 , "Unable to change source codebase of project=" + scopeProject.sourceProject.NAME , scopeProject.sourceProject.NAME );
			
		return( SCOPESTATE.RunSuccess );
	}
	
}
