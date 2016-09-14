package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.engine.ServerProjectBuilder;
import org.urm.engine.meta.MetaProductBuildSettings;
import org.urm.engine.meta.MetaSourceProject;
import org.urm.engine.meta.Meta.VarCATEGORY;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;

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

	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		boolean run = false;
		
		// run in order of build
		debug( "build set=" + set.NAME + " ..." );
		for( MetaSourceProject project : set.pset.getOriginalList( this ) ) {
			ActionScopeTarget target = set.findSourceTarget( this , project );
			if( target == null ) {
				trace( "skip non-set target=" + project.PROJECT );
				continue;
			}
			
			if( !Common.checkListItem( targets , target ) ) {
				trace( "skip nonbuild target=" + set.NAME );
				continue;
			}
				
			debug( "build project=" + project.PROJECT );
			if( !executeTarget( target ) ) {
				error( "cancel build due to errors" );
				return( false );
			}
			
			run = true;
		}

		if( run )
			shell.customCheckErrorsDebug( this , "grep " + Common.getQuoted( "[INFO|ERROR]] BUILD" ) + " " + OUTDIR.folderPath + "/" + set.NAME + "/*.log >> " + OUTFILE );
		return( true );
	}
	
	private boolean executeTarget( ActionScopeTarget scopeProject ) throws Exception {
		String BUILD_OPTIONS = null;
		if( scopeProject.CATEGORY == VarCATEGORY.BUILD ) {
			MetaProductBuildSettings build = getBuildSettings();
			BUILD_OPTIONS = build.CONFIG_BUILDER_OPTIONS;
		}
		else
			exitUnexpectedCategory( scopeProject.CATEGORY );

		String version = scopeProject.getProjectBuildVersion( this );
		
		// execute
		MetaSourceProject project = scopeProject.sourceProject;
		info( "ActionBuild: CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", PROJECT=" + project.PROJECT + 
				", REPOSITORY=" + project.REPOSITORY + ", TAG=" + TAG + ", VERSION=" + version + ", MODULEOPTIONS=" + BUILD_OPTIONS );

		// in separate shell
		Builder builder = createBuilder( project , TAG , BUILD_OPTIONS , version );
		LocalFolder BUILDDIR = OUTDIR.getSubFolder( this , project.set.NAME );
		ActionPatch action = new ActionPatch( actionInit , null , builder , BUILDDIR );
		builder.createShell( action );

		BUILDSTATUS = "SUCCESSFUL"; 
		if( !action.runSimple() ) {
			BUILDSTATUS = "FAILED";
			super.setFailed();
		}

		// check status
		info( "ActionBuild: build finished for CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) + ", TAG=" + TAG + ", VERSION=" + version + ", BUILDSTATUS=" + BUILDSTATUS );
		return( true );
	}
	
	private Builder createBuilder( MetaSourceProject project , String TAG , String BUILD_OPTIONS , String VERSION ) throws Exception {
		Builder builder = null;
		
		String BUILDER = project.getBuilder( this );
		BuildStorage storage = artefactory.getEmptyBuildStorage( this , project );
		if( BUILDER.equals( ServerProjectBuilder.BUILDER_TYPE_MAVEN ) ) {
			builder = new BuilderLinuxMaven( BUILDER , project , storage , TAG , BUILD_OPTIONS , VERSION );
		}
		else if( BUILDER.equals( ServerProjectBuilder.BUILDER_TYPE_GRADLE ) ) {
			builder = new BuilderLinuxGradle( BUILDER , project , storage , TAG , BUILD_OPTIONS , VERSION );
		}
		else if( BUILDER.equals( ServerProjectBuilder.BUILDER_TYPE_DOTNET ) ) {
			builder = new BuilderWindowsDotnet( BUILDER , project , storage , TAG , BUILD_OPTIONS , VERSION );
		}
		else
			exit1( _Error.UnknownBuilderType1 , "unknown builder=" + BUILDER , BUILDER );
		
		return( builder );
	}

}
