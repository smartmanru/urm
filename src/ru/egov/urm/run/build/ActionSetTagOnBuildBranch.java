package ru.egov.urm.run.build;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.vcs.ProjectVersionControl;

public class ActionSetTagOnBuildBranch extends ActionBase {

	String TAG;
	
	public ActionSetTagOnBuildBranch( ActionBase action , String stream , String TAG ) {
		super( action , stream );
		this.TAG = TAG;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		String BUILDBRANCH = "";
		String BUILDTAG = "";
		
		MetaSourceProject sourceProject = scopeProject.sourceProject;
		if( !context.CTX_BRANCH.isEmpty() )
			BUILDBRANCH = context.CTX_BRANCH;
		else {
			if( scopeProject.releaseTarget != null ) {
				BUILDBRANCH = scopeProject.releaseTarget.BUILDBRANCH;
				BUILDTAG = scopeProject.releaseTarget.BUILDTAG;
			}
		}

		if( !BUILDTAG.isEmpty() ) {
			setTagFromTag( BUILDTAG , TAG , scopeProject.CATEGORY , sourceProject );
			return( true );
		}
		
		if( BUILDBRANCH.isEmpty() ) {
			BUILDBRANCH = sourceProject.getDefaultBranch( this );
			if( BUILDBRANCH.isEmpty() )
				exit( "unable to find default branch for CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", project=" + sourceProject.PROJECT );
		}
		
		setTagFromBranch( BUILDBRANCH , TAG , scopeProject.CATEGORY , sourceProject );
		return( true );
	}

	private void setTagFromTag( String SRCTAG , String TAG , VarCATEGORY CATEGORY , MetaSourceProject sourceProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this , false );
		vcs.copyTagToTag( sourceProject , SRCTAG , TAG );
	}
	
	private void setTagFromBranch( String BRANCH , String TAG , VarCATEGORY CATEGORY , MetaSourceProject sourceProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this , false );
		vcs.setTag( sourceProject , BRANCH , TAG , context.CTX_DATE );
	}
	
}
