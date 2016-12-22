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
		boolean run = false;
		
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
			
			run = true;
		}

		if( run )
			shell.customCheckErrorsDebug( this , "grep " + Common.getQuoted( "[INFO|ERROR]] BUILD" ) + " " + OUTDIR.folderPath + "/" + set.NAME + "/*.log >> " + OUTFILE );
		return( SCOPESTATE.RunSuccess );
	}
	
	private boolean executeTarget( ActionScopeTarget scopeProject ) throws Exception {
		String version = scopeProject.getProjectBuildVersion( this );
		
		// execute
		MetaSourceProject project = scopeProject.sourceProject;
		info( "ActionBuild: CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", PROJECT=" + project.NAME + 
				", REPOSITORY=" + project.REPOSITORY + ", TAG=" + TAG + ", VERSION=" + version );

		// in separate shell
		Builder builder = createBuilder( project , TAG , version );
		LocalFolder BUILDDIR = OUTDIR.getSubFolder( this , project.set.NAME );
		ActionPatch action = new ActionPatch( this , null , builder , BUILDDIR );
		builder.createShell( action );

		BUILDSTATUS = "SUCCESSFUL"; 
		if( !action.runProductBuild( project.meta.name , SecurityAction.ACTION_BUILD , context.buildMode , false ) ) {
			BUILDSTATUS = "FAILED";
			super.fail1( _Error.ProjectBuildError1 , "Errors while build project=" + project.NAME , project.NAME );
		}

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
		if( builder.isMaven() )
			projectBuilder = new BuilderLinuxMaven( builder , project , storage , TAG , VERSION );
		else
		if( builder.isGradle() )
			projectBuilder = new BuilderLinuxGradle( builder , project , storage , TAG , VERSION );
		else
		if( builder.isWinBuild() )
			projectBuilder = new BuilderWindowsDotnet( builder , project , storage , TAG , VERSION );
		else
			exit1( _Error.UnknownBuilderType1 , "unknown builder=" + BUILDER , BUILDER );
		
		return( projectBuilder );
	}

}
