package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.engine.ServerBuilders;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaSourceProject;

public class ActionBuild extends ActionBase {
	
	ActionScope scope;
	LocalFolder OUTDIR;
	String OUTFILE;
	String TAG;

	String BUILDSTATUS;
	
	public ActionBuild( ActionBase action , String stream , LocalFolder OUTDIR , String OUTFILE , String TAG ) {
		super( action , stream );
		this.OUTDIR = OUTDIR;
		this.OUTFILE = OUTFILE;
		this.TAG = TAG;
	}

	@Override protected SCOPESTATE executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
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
			if( !executeTarget( target ) ) {
				error( "cancel build due to errors" );
				return( SCOPESTATE.RunFail );
			}
		}

		return( SCOPESTATE.RunSuccess );
	}
	
	private boolean executeTarget( ActionScopeTarget scopeProject ) throws Exception {
		String version = scopeProject.getProjectBuildVersion( this );
		
		// execute
		MetaSourceProject project = scopeProject.sourceProject;
		Builder builder = createBuilder( project , TAG , version );
		info( "ActionBuild: CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", PROJECT=" + project.NAME + 
				", REPOSITORY=" + project.REPOSITORY + ", TAG=" + TAG + ", VERSION=" + version + ", BUILDER=" + builder.builder.NAME );

		// in separate shell
		LocalFolder BUILDDIR = OUTDIR.getSubFolder( this , project.set.NAME );
		ActionPatch action = new ActionPatch( this , null , builder , BUILDDIR );
		builder.createShell( action );

		BUILDDIR.ensureExists( this );
		
		String logFile = BUILDDIR.getFilePath( this , builder.project.NAME + "-build.log" );
		super.startRedirect( "PROJECT BUILD LOG:" , logFile );
		info( "build: BUILDER=" + builder.builder.NAME + ", BUILDMODE=" + context.getBuildModeName() + ", PROJECT=" + builder.project.NAME + 
				", REPOSITORY=" + builder.project.REPOSITORY + ", VCS=" + builder.project.getVCS( this ) + ", VCSPATH=" + builder.project.REPOPATH + 
				", TAG=" + builder.TAG + ", VERSION=" + builder.APPVERSION );

		BUILDSTATUS = "SUCCESSFUL"; 
		if( !action.runProductBuild( project.meta.name , SecurityAction.ACTION_BUILD , context.buildMode , false ) ) {
			BUILDSTATUS = "FAILED";
			super.fail1( _Error.ProjectBuildError1 , "Errors while building project=" + project.NAME , project.NAME );
		}
		super.stopRedirect();
		
		// check status
		info( "ActionBuild: build finished for CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", TAG=" + TAG + ", VERSION=" + version + ", BUILDSTATUS=" + BUILDSTATUS );
		return( true );
	}
	
	private Builder createBuilder( MetaSourceProject project , String TAG , String VERSION ) throws Exception {
		String BUILDER = project.getBuilder( this );
		
		ServerBuilders builders = super.getBuilders();
		ServerProjectBuilder builder = builders.getBuilder( BUILDER );
		
		Builder projectBuilder = null;
		
		BuildStorage storage = artefactory.getEmptyBuildStorage( this , project );
		if( builder.isAnt() )
			projectBuilder = new BuilderAntMethod( builder , project , storage , TAG , VERSION );
		else
		if( builder.isMaven() )
			projectBuilder = new BuilderMavenMethod( builder , project , storage , TAG , VERSION );
		else
		if( builder.isGradle() )
			projectBuilder = new BuilderGradleMethod( builder , project , storage , TAG , VERSION );
		else
		if( builder.isWinBuild() )
			projectBuilder = new BuilderWinbuildMethod( builder , project , storage , TAG , VERSION );
		else {
			String method = Common.getEnumLower( builder.builderMethod );
			exit2( _Error.UnknownBuilderMethod2 , "unknown builder method=" + method + " (builder=" + BUILDER + ")" , method , BUILDER );
		}
		
		return( projectBuilder );
	}

}
