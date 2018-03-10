package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;

public class ActionSetVersion extends ActionBase {

	String BUILDVERSION;
	
	public ActionSetVersion( ActionBase action , String stream , String BUILDVERSION ) {
		super( action , stream , "Set codebase versioin=" + BUILDVERSION );
		this.BUILDVERSION = BUILDVERSION;
	}

	@Override 
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget scopeProject ) throws Exception {
		// ignore if builder is not maven
		if( !scopeProject.sourceProject.getBuilder( this ).isMaven() ) {
			info( "project=" + scopeProject.sourceProject.NAME + " is not built by maven. Skipped." );
			return( SCOPESTATE.NotRun );
		}
		
		// checkout
		BuildStorage PATCHPATH = artefactory.getEmptyBuildStorage( this , scopeProject.sourceProject );
		String BRANCH = scopeProject.getProjectBuildBranch( this );
		
		MirrorRepository mirror = scopeProject.sourceProject.getMirror( this );
		info( "setVersionProject: PROJECT=" + scopeProject.sourceProject.NAME + ", MIRROR=" + mirror.NAME + 
				", BRANCH=" + BRANCH + ", VERSION=" + BUILDVERSION + ", PATCHPATH=" + PATCHPATH.buildFolder.folderPath + " ..." );

		ProjectVersionControl vcs = new ProjectVersionControl( this );
		
		if( !vcs.checkout( PATCHPATH.buildFolder , scopeProject.sourceProject , BRANCH ) )
			exit1( _Error.UnableCheckout1 , "setVersionProject: error calling checkout" , scopeProject.sourceProject.NAME );

		// set version
		createDedicatedShell( "build"  );
		updateVersion( scopeProject , PATCHPATH.buildFolder );
		
		MetaProductSettings product = scopeProject.meta.getProductSettings();
		MetaProductCoreSettings core =  product.getCoreSettings();
		
		vcs.commit( scopeProject.sourceProject , BRANCH , PATCHPATH.buildFolder , core.CONFIG_ADM_TRACKER + "-0000: set version " + BUILDVERSION );
		return( SCOPESTATE.RunSuccess );
	}

	private void updateVersion( ActionScopeTarget scopeProject , LocalFolder PATCHPATH ) throws Exception {
		LocalFolder CODEPATH = PATCHPATH;
		ProjectBuilder builder = scopeProject.sourceProject.getBuilder( this );
		String JAVA_HOME = builder .JAVA_JDKHOMEPATH;
		
		shell.export( this , "JAVA_HOME" , JAVA_HOME );
		shell.export( this , "PATH" , "$JAVA_HOME/bin:$PATH" );

		String MAVEN_HOME = builder.BUILDER_HOMEPATH;
		String MAVEN_CMD = "mvn versions:set -DnewVersion=" + BUILDVERSION;

		shell.export( this , "M2_HOME" , MAVEN_HOME );
		shell.export( this , "M2" , "$M2_HOME/bin" );
		shell.export( this , "PATH" , "$M2:$PATH" );

		info( "execute: " + MAVEN_CMD + " ..." );
		shell.mvnCheckStatus( this , CODEPATH.folderPath , MAVEN_CMD );

		// handle git specifics
		if( scopeProject.sourceProject.isGitVCS( this ) )
			shell.gitAddPomFiles( this , CODEPATH.folderPath );
	}
	
}
