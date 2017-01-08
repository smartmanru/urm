package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaProductSettings;

public class ActionSetVersion extends ActionBase {

	String BUILDVERSION;
	
	public ActionSetVersion( ActionBase action , String stream , String BUILDVERSION ) {
		super( action , stream );
		this.BUILDVERSION = BUILDVERSION;
	}

	private void updateVersion( ActionScopeTarget scopeProject , LocalFolder PATCHPATH ) throws Exception {
		LocalFolder CODEPATH = PATCHPATH;
		String BUILDER = scopeProject.sourceProject.getBuilder( this );
		ServerProjectBuilder builder = super.getBuilder( BUILDER );
		String JAVA_HOME = builder .JAVA_JDKHOMEPATH;
		
		shell.export( this , "JAVA_HOME" , JAVA_HOME );
		shell.export( this , "PATH" , "$JAVA_HOME/bin:$PATH" );

		String MAVEN_HOME = builder.MAVEN_HOMEPATH;
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
	
	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		// ignore if builder is not maven
		if( !scopeProject.sourceProject.getBuilder( this ).equals( "maven" ) ) {
			info( "project=" + scopeProject.sourceProject.NAME + " is not built by maven. Skipped." );
			return( SCOPESTATE.NotRun );
		}
		
		// checkout
		BuildStorage PATCHPATH = artefactory.getEmptyBuildStorage( this , scopeProject.sourceProject );
		String BRANCH = scopeProject.getProjectBuildBranch( this );
		
		info( "setVersionProject: PROJECT=" + scopeProject.sourceProject.NAME + ", REPOSITORY=" + scopeProject.sourceProject.REPOSITORY + 
				", PATH=" + scopeProject.sourceProject.REPOPATH + ", BRANCH=" + BRANCH + ", VERSION=" + BUILDVERSION + ", PATCHPATH=" + PATCHPATH.buildFolder.folderPath + " ..." );

		ProjectVersionControl vcs = new ProjectVersionControl( this );
		
		if( !vcs.checkout( PATCHPATH.buildFolder , scopeProject.sourceProject , BRANCH ) )
			exit1( _Error.UnableCheckout1 , "setVersionProject: error calling checkout" , scopeProject.sourceProject.NAME );

		// set version
		createDedicatedShell( "build"  );
		updateVersion( scopeProject , PATCHPATH.buildFolder );
		MetaProductSettings product = scopeProject.meta.getProductSettings( this );
		vcs.commit( PATCHPATH.buildFolder , scopeProject.sourceProject , product.CONFIG_ADM_TRACKER + "-0000: set version " + BUILDVERSION );
		return( SCOPESTATE.RunSuccess );
	}
}
