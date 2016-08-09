package org.urm.server.action.build;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScope;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.meta.Metadata.VarCATEGORY;
import org.urm.server.storage.BuildStorage;
import org.urm.server.storage.LocalFolder;

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
		if( scopeProject.CATEGORY == VarCATEGORY.BUILD )
			BUILD_OPTIONS = meta.product.CONFIG_MODULE_BUILD_OPTIONS_CORE;
		else
			exit( "ActionBuild: unexpected CATEGORY=" + Common.getEnumLower( scopeProject.CATEGORY ) );

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
		if( BUILDER.equals( "maven" ) ) {
			builder = new BuilderLinuxMaven( BUILDER , project , storage , TAG , BUILD_OPTIONS , VERSION );
		}
		else if( BUILDER.equals( "gradle" ) ) {
			builder = new BuilderLinuxGradle( BUILDER , project , storage , TAG , BUILD_OPTIONS , VERSION );
		}
		else if( BUILDER.equals( "dotnet" ) ) {
			builder = new BuilderWindowsDotnet( BUILDER , project , storage , TAG , BUILD_OPTIONS , VERSION );
		}
		else
			exit( "unknown builder=" + BUILDER );
		
		return( builder );
	}

}
