package ru.egov.urm.run.build;

import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.BuildStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.vcs.ProjectVersionControl;

public class BuilderLinuxGradle extends Builder {

	public BuilderLinuxGradle( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( BUILDER , project , storage , TAG , BUILD_OPTIONS , APPVERSION );
	}
	
	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		return( action.context.pool.createDedicatedLocalShell( action , "build" ) );
	}
	
	@Override public boolean exportCode( ActionBase action ) throws Exception {
		// drop old
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	
		// checkout
		ProjectVersionControl vcs = new ProjectVersionControl( action ); 
		if( !vcs.export( CODEPATH , project , "" , TAG , "" ) ) {
			action.log( "patchCheckout: having problem to export code" );
			return( false );
		}
		
		return( true );
	}

	@Override public boolean prepareSource( ActionBase action ) throws Exception {
		return( true );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		return( true );
	}

	@Override public boolean runBuild( ActionBase action ) throws Exception {
		// set java and gradle environment
		String BUILD_JAVA_VERSION = project.getJavaVersion( action );
		String BUILD_GRADLE_VERSION = project.getBuilderVersion( action ); 

		ShellExecutor session = action.session;
		session.export( action , "JAVA_HOME" , action.meta.product.CONFIG_BUILDBASE + "/" + BUILD_JAVA_VERSION );
		session.export( action , "GR_HOME" , action.meta.product.CONFIG_BUILDBASE + "/" + BUILD_GRADLE_VERSION );
		session.export( action , "GR" , "$GR_HOME/bin" );
		session.export( action , "PATH" , "$GR:$JAVA_HOME/bin:$PATH" );

		String GRADLE_CMD = "gradle clean war publish -Dmaven.settings=" + action.meta.product.CONFIG_MAVEN_CFGFILE;

		// execute gradle
		session.cd( action , storage.buildFolder.folderPath );
		action.log( "using gradle:" );
		session.customCheckErrorsNormal( action , "which gradle" );
		session.customCheckErrorsNormal( action , "gradle --version" );
		
		action.log( "execute: " + GRADLE_CMD );
		session.setTimeoutUnlimited( action );
		int status = session.customGetStatusNormal( action , GRADLE_CMD );

		if( status != 0 ) {
			action.log( "buildGradle: gradle build failed" );
			return( false );
		}
		
		action.log( "buildGradle: gradle build successfully finished" );
		return( true );
	}

	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	}

}
