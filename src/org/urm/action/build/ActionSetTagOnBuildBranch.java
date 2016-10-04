package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.Meta.VarCATEGORY;

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
				exit1( _Error.NoProjectDefaultBranch1 , "unable to find default branch for CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", project=" + sourceProject.PROJECT , sourceProject.PROJECT );
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
