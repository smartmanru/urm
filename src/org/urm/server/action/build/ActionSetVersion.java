package org.urm.server.action.build;

import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.storage.BuildStorage;
import org.urm.server.storage.LocalFolder;
import org.urm.server.vcs.ProjectVersionControl;

public class ActionSetVersion extends ActionBase {

	String BUILDVERSION;
	
	public ActionSetVersion( ActionBase action , String stream , String BUILDVERSION ) {
		super( action , stream );
		this.BUILDVERSION = BUILDVERSION;
	}

	private void updateVersion( ActionScopeTarget scopeProject , LocalFolder PATCHPATH ) throws Exception {
		LocalFolder CODEPATH = PATCHPATH;
		String JAVE_VERSION = scopeProject.sourceProject.getJavaVersion( this );
		
		shell.export( this , "JAVA_HOME" , "/usr/java/" + JAVE_VERSION );
		shell.export( this , "PATH" , "$JAVA_HOME/bin:$PATH" );

		String MAVEN_VERSION = scopeProject.sourceProject.getBuilderVersion( this );
		String MAVEN_CMD = "mvn versions:set -DnewVersion=" + BUILDVERSION;

		shell.export( this , "M2_HOME" , "/usr/local/apache-maven-" + MAVEN_VERSION );
		shell.export( this , "M2" , "$M2_HOME/bin" );
		shell.export( this , "PATH" , "$M2:$PATH" );

		info( "execute: " + MAVEN_CMD + " ..." );
		shell.mvnCheckStatus( this , CODEPATH.folderPath , MAVEN_CMD );

		// handle git specifics
		if( scopeProject.sourceProject.isGitVCS( this ) )
			shell.gitAddPomFiles( this , CODEPATH.folderPath );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		// ignore if builder is not maven
		if( !scopeProject.sourceProject.getBuilder( this ).equals( "maven" ) ) {
			info( "project=" + scopeProject.sourceProject.PROJECT + " is not built by maven. Skipped." );
			return( true );
		}
		
		// checkout
		BuildStorage PATCHPATH = artefactory.getEmptyBuildStorage( this , scopeProject.sourceProject );
		String BRANCH = scopeProject.getProjectBuildBranch( this );
		
		info( "setVersionProject: PROJECT=" + scopeProject.sourceProject.PROJECT + ", REPOSITORY=" + scopeProject.sourceProject.REPOSITORY + 
				", PATH=" + scopeProject.sourceProject.PATH + ", BRANCH=" + BRANCH + ", VERSION=" + BUILDVERSION + ", PATCHPATH=" + PATCHPATH.buildFolder.folderPath + " ..." );

		ProjectVersionControl vcs = new ProjectVersionControl( this , false );
		
		if( !vcs.checkout( PATCHPATH.buildFolder , scopeProject.sourceProject , BRANCH ) )
			exit1( _Error.UnableCheckout1 , "setVersionProject: error calling checkout" , scopeProject.sourceProject.PROJECT );

		// set version
		createDedicatedShell( "build"  );
		updateVersion( scopeProject , PATCHPATH.buildFolder );
		vcs.commit( PATCHPATH.buildFolder , scopeProject.sourceProject , meta.product.CONFIG_ADM_TRACKER + "-0000: set version " + BUILDVERSION );
		return( true );
	}
}
