package ru.egov.urm.run.build;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.vcs.ProjectVersionControl;

public class ActionDropCodebase extends ActionBase {

	boolean branchVAR1;
	String VAR1;
	
	public ActionDropCodebase( ActionBase action , String stream , boolean branchVAR1 , String VAR1 ) {
		super( action , stream );
		this.branchVAR1 = branchVAR1;
		this.VAR1 = VAR1;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this , false );
		if( branchVAR1 == true )
			vcs.dropBranch( scopeProject.sourceProject , VAR1 );
		else
		if( branchVAR1 == false )
			vcs.dropTag( scopeProject.sourceProject , VAR1 );
		
		return( true );
	}
	
}
