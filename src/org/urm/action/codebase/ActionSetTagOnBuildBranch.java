package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.product.MetaSourceProject;

public class ActionSetTagOnBuildBranch extends ActionBase {

	String TAG;
	boolean force;
	
	public ActionSetTagOnBuildBranch( ActionBase action , String stream , String TAG , boolean force ) {
		super( action , stream , "Codebase set build tag=" + TAG );
		this.TAG = TAG;
		this.force = force;
	}

	@Override 
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget scopeProject ) throws Exception {
		String BUILDBRANCH = "";
		String BUILDTAG = "";
		
		MetaSourceProject sourceProject = scopeProject.sourceProject;
		if( !context.CTX_BRANCH.isEmpty() )
			BUILDBRANCH = context.CTX_BRANCH;
		else {
			if( scopeProject.releaseBuildScopeProject != null ) {
				BUILDBRANCH = scopeProject.getProjectBuildBranch( this );
				BUILDTAG = scopeProject.getProjectBuildTag( this );
			}
		}

		if( !BUILDTAG.isEmpty() ) {
			setTagFromTag( BUILDTAG , TAG , scopeProject.CATEGORY , sourceProject );
			return( SCOPESTATE.RunSuccess );
		}
		
		if( BUILDBRANCH.isEmpty() ) {
			BUILDBRANCH = sourceProject.getDefaultBranch( this );
			if( BUILDBRANCH.isEmpty() )
				exit1( _Error.NoProjectDefaultBranch1 , "unable to find default branch for CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", project=" + sourceProject.NAME , sourceProject.NAME );
		}
		
		setTagFromBranch( BUILDBRANCH , TAG , scopeProject.CATEGORY , sourceProject );
		return( SCOPESTATE.RunSuccess );
	}

	private void setTagFromTag( String SRCTAG , String TAG , DBEnumScopeCategoryType CATEGORY , MetaSourceProject sourceProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this );
		if( !vcs.copyTagToTag( sourceProject , SRCTAG , TAG , force ) )
			super.ifexit3( _Error.ProjectCopyTagError3 , "unable to copy tag=" + SRCTAG + " to tag=" + TAG + " for proect=" + sourceProject.NAME , SRCTAG , TAG , sourceProject.NAME );
	}
	
	private void setTagFromBranch( String BRANCH , String TAG , DBEnumScopeCategoryType CATEGORY , MetaSourceProject sourceProject ) throws Exception {
		ProjectVersionControl vcs = new ProjectVersionControl( this );
		if( !vcs.setTag( sourceProject , BRANCH , TAG , context.CTX_DATE , force ) )
			super.ifexit2( _Error.ProjectSetTagError2 , "unable to set tag=" + TAG + " on proect=" + sourceProject.NAME , TAG , sourceProject.NAME );
	}
	
}
