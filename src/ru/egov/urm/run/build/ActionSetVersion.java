package ru.egov.urm.run.build;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.storage.BuildStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.vcs.ProjectVersionControl;

public class ActionSetVersion extends ActionBase {

	String BUILDVERSION;
	
	public ActionSetVersion( ActionBase action , String stream , String BUILDVERSION ) {
		super( action , stream );
		this.BUILDVERSION = BUILDVERSION;
	}

	private void updateVersion( ActionScopeTarget scopeProject , LocalFolder PATCHPATH ) throws Exception {
		LocalFolder CODEPATH = PATCHPATH;
		String JAVE_VERSION = scopeProject.sourceProject.getJavaVersion( this );
		
		session.export( this , "JAVA_HOME" , "/usr/java/" + JAVE_VERSION );
		session.export( this , "PATH" , "$JAVA_HOME/bin:$PATH" );

		String MAVEN_VERSION = scopeProject.sourceProject.getBuilderVersion( this );
		String MAVEN_CMD = "mvn versions:set -DnewVersion=" + BUILDVERSION;

		session.export( this , "M2_HOME" , "/usr/local/apache-maven-" + MAVEN_VERSION );
		session.export( this , "M2" , "$M2_HOME/bin" );
		session.export( this , "PATH" , "$M2:$PATH" );

		log( "execute: " + MAVEN_CMD + " ..." );
		session.mvnCheckStatus( this , CODEPATH.folderPath , MAVEN_CMD );

		// handle git specifics
		if( scopeProject.sourceProject.isGitVCS( this ) )
			session.gitAddPomFiles( this , CODEPATH.folderPath );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget scopeProject ) throws Exception {
		// ignore if builder is not maven
		if( !scopeProject.sourceProject.getBuilder( this ).equals( "maven" ) ) {
			log( "project=" + scopeProject.sourceProject.PROJECT + " is not built by maven. Skipped." );
			return( true );
		}
		
		// checkout
		BuildStorage PATCHPATH = artefactory.getEmptyBuildStorage( this , scopeProject.sourceProject );
		String BRANCH = scopeProject.getProjectBuildBranch( this );
		
		log( "setVersionProject: PROJECT=" + scopeProject.sourceProject.PROJECT + ", REPOSITORY=" + scopeProject.sourceProject.REPOSITORY + 
				", PATH=" + scopeProject.sourceProject.PATH + ", BRANCH=" + BRANCH + ", VERSION=" + BUILDVERSION + ", PATCHPATH=" + PATCHPATH.buildFolder.folderPath + " ..." );

		ProjectVersionControl vcs = new ProjectVersionControl( this );
		
		if( !vcs.checkout( PATCHPATH.buildFolder , scopeProject.sourceProject , BRANCH ) )
			exit( "setVersionProject: error calling checkout" );

		// set version
		context.pool.createDedicatedLocalShell( this , "build"  );
		updateVersion( scopeProject , PATCHPATH.buildFolder );
		vcs.commit( PATCHPATH.buildFolder , scopeProject.sourceProject , meta.product.CONFIG_ADM_TRACKER + "-0000: set version " + BUILDVERSION );
		return( true );
	}
}
