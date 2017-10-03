package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.product.MetaSourceProject;

public class ActionBuild extends ActionBase {
	
	ActionScope scope;
	public LocalFolder OUTDIR;
	public String OUTFILE;
	public String TAG;
	public Dist dist;

	public String BUILDSTATUS;
	
	public ActionBuild( ActionBase action , String stream , LocalFolder OUTDIR , String OUTFILE , String TAG , Dist dist ) {
		super( action , stream , "Generic build, tag=" + TAG );
		this.OUTDIR = OUTDIR;
		this.OUTFILE = OUTFILE;
		this.TAG = TAG;
		this.dist = dist;
	}

	@Override 
	protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		// run in order of build
		debug( "build set=" + set.NAME + " ..." );
		for( MetaSourceProject project : set.pset.getOrderedList() ) {
			ActionScopeTarget target = set.findSourceTarget( this , project );
			if( target == null ) {
				trace( "skip non-set target=" + project.NAME );
				continue;
			}
			
			if( !Common.checkListItem( targets , target ) ) {
				trace( "skip nonbuild target=" + set.NAME );
				continue;
			}
				
			debug( "build project=" + project.NAME );
			if( !super.runCustomTarget( state , target ) ) {
				if( !super.continueRun() ) {
					error( "cancel build due to errors" );
					return( SCOPESTATE.RunFail );
				}
			}
		}

		return( SCOPESTATE.RunSuccess );
	}
	
	@Override
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		MetaSourceProject project = target.sourceProject;
		
		String version = target.getProjectBuildVersion( this );
		
		// execute
		Builder builder = Builder.createBuilder( this , project , TAG , version );
		info( "ActionBuild: CATEGORY=" + Common.getEnumLower( target.CATEGORY ) + ", PROJECT=" + project.NAME + 
				", REPOSITORY=" + project.REPOSITORY + ", TAG=" + TAG + ", VERSION=" + version + ", BUILDER=" + builder.builder.NAME );

		// in separate shell
		LocalFolder BUILDDIR = OUTDIR.getSubFolder( this , project.set.NAME );
		String fileName = builder.project.NAME + "-build.log";
		String logFile = BUILDDIR.getFilePath( this , fileName );
		ActionPatch action = new ActionPatch( this , null , builder , BUILDDIR , fileName , shell );

		BUILDDIR.ensureExists( this );
		
		action.startRedirect( "PROJECT BUILD LOG:" , logFile );
		info( "build: BUILDER=" + builder.builder.NAME + ", BUILDMODE=" + context.getBuildModeName() + ", PROJECT=" + builder.project.NAME + 
				", REPOSITORY=" + builder.project.REPOSITORY + ", VCS=" + builder.project.getVCS( this ) + ", VCSPATH=" + builder.project.REPOPATH + 
				", TAG=" + builder.TAG + ", VERSION=" + builder.APPVERSION );

		BUILDSTATUS = "SUCCESSFUL";
		boolean res = true;
		if( !action.runProductBuild( project.meta.name , SecurityAction.ACTION_CODEBASE , context.buildMode , false ) ) {
			BUILDSTATUS = "FAILED";
			res = false;
			super.fail1( _Error.ProjectBuildError1 , "Errors while building project=" + project.NAME , project.NAME );
		}
		
		// check status
		info( "ActionBuild: build finished for CATEGORY=" + Common.getEnumLower( target.CATEGORY ) + ", TAG=" + TAG + ", VERSION=" + version + ", BUILDSTATUS=" + BUILDSTATUS );
		if( !res )
			return( SCOPESTATE.RunFail );
		return( SCOPESTATE.RunSuccess );
	}
	
}
