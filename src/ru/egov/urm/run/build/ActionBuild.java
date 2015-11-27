package ru.egov.urm.run.build;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;

public class ActionBuild extends ActionBase {
	
	ActionScope scope;
	LocalFolder OUTDIR;
	String OUTFILE;
	String TAG;

	String BUILDSTATUS;
	
	public ActionBuild( ActionBase action , String stream , LocalFolder OUTDIR , String OUTFILE , String TAG ) {
		super( action , stream );
		this.OUTDIR = OUTDIR;
		this.TAG = TAG;
	}

	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		boolean run = false;
		
		// run in order of build
		for( MetaSourceProject project : set.pset.getOriginalList( this ) ) {
			ActionScopeTarget target = set.findSourceTarget( this , project );
			if( !Common.checkListItem( targets , target ) )
				continue;
				
			if( target == null )
				continue;
			
			if( !executeTarget( target ) )
				return( false );
			
			run = true;
		}

		if( run )
			session.customCheckErrorsDebug( this , "grep " + Common.getQuoted( "[INFO|ERROR]] BUILD" ) + " " + OUTDIR.folderPath + "/" + set.NAME + "/*.log >> " + OUTFILE );
		return( true );
	}
	
	private boolean executeTarget( ActionScopeTarget scopeProject ) throws Exception {
		String BUILD_OPTIONS = null;
		if( scopeProject.CATEGORY == VarCATEGORY.BUILD )
			BUILD_OPTIONS = meta.product.CONFIG_MODULE_BUILD_OPTIONS_CORE;
		else
			exit( "ActionBuild: unexpected CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) );

		String version = scopeProject.getProjectBuildVersion( this );
		
		// execute
		log( "ActionBuild: CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", PROJECT=" + scopeProject.sourceProject.PROJECT + 
				", REPOSITORY=" + scopeProject.sourceProject.REPOSITORY + ", TAG=" + TAG + ", VERSION=" + version + ", MODULEOPTIONS=" + BUILD_OPTIONS );

		// in separate shell
		ActionPatch action = new ActionPatch( this , null , OUTDIR , TAG , BUILD_OPTIONS , version ); 
		ShellExecutor bs = context.pool.createDedicatedLocalShell( action );

		BUILDSTATUS = "SUCCESSFUL"; 
		if( !action.runSingleTarget( scopeProject ) ) {
			BUILDSTATUS = "FAILED";
			super.setFailed();
		}

		bs.kill( this );
	
		// check status
		log( "ActionBuild: build finished for CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", TAG=" + TAG + ", VERSION=" + version + ", BUILDSTATUS=" + BUILDSTATUS );
		return( true );
	}
}
