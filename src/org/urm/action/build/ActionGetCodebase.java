package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.storage.LocalFolder;
import org.urm.vcs.ProjectVersionControl;

public class ActionGetCodebase extends ActionBase {

	LocalFolder CODIR;
	boolean checkout;
	boolean force;
	String SINGLEFILE;
	
	public ActionGetCodebase( ActionBase action , String stream , LocalFolder CODIR , boolean checkout , boolean force , String SINGLEFILE ) {
		super( action , stream );
		this.CODIR = CODIR;
		this.checkout = checkout;
		this.force = force;
		this.SINGLEFILE = SINGLEFILE;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		String BRANCH = scopeProject.getProjectBuildBranch( this );
		String TAG = scopeProject.getProjectBuildTag( this );

		LocalFolder COPATH = CODIR.getSubFolder( this , scopeProject.sourceProject.PROJECT );
		
		if( force == false )
			if( COPATH.checkExists( this ) )
				exit( "downloadProject: target path=" + COPATH + " already exists" );
			
		COPATH.removeThis( this );
		CODIR.ensureExists( this );

		ProjectVersionControl vcs = new ProjectVersionControl( this , false );
		if( checkout ) {
			if( !SINGLEFILE.isEmpty() )
				exit( "downloadProject: unable to checkout single file" );
			vcs.checkout( COPATH , scopeProject.sourceProject , BRANCH );
		}
		else
			vcs.export( COPATH , scopeProject.sourceProject , BRANCH , TAG , SINGLEFILE );
		return( true );
	}

}
