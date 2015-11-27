package ru.egov.urm.run.build;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.vcs.ProjectVersionControl;

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

		ProjectVersionControl vcs = new ProjectVersionControl( this );
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
