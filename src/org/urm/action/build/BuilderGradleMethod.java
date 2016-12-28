package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProject;

public class BuilderGradleMethod extends Builder {

	public BuilderGradleMethod( ServerProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		super( builder , project , storage , TAG , APPVERSION );
	}
	
	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		return( action.createDedicatedShell( "build" ) );
	}
	
	@Override public boolean prepareSource( ActionBase action ) throws Exception {
		return( true );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		return( true );
	}

	@Override public boolean runBuild( ActionBase action ) throws Exception {
		// set java and gradle environment
		String BUILD_JAVA_HOME = builder.JAVA_JDKHOMEPATH;
		String BUILD_GRADLE_HOME = builder.GRADLE_HOMEPATH; 
		String MODULE_ADDITIONAL_OPTIONS = project.BUILDER_ADDOPTIONS;

		ShellExecutor session = action.shell;
		session.export( action , "JAVA_HOME" , session.getLocalPath( BUILD_JAVA_HOME ) );
		session.export( action , "GR_HOME" , session.getLocalPath( BUILD_GRADLE_HOME ) );
		session.export( action , "GR" , session.getLocalPath( session.getVariable( "GR_HOME" ) + "/bin" ) );
		
		session.export( action , "PATH" , session.getVariable( "GR" ) + session.getPathBreak() + 
				session.getLocalPath( session.getVariable( "JAVA_HOME" + "/bin" ) ) + session.getPathBreak() +
				session.getVariable( "PATH" ) );

		MetaProductBuildSettings build = action.getBuildSettings( project.meta );
		String GRADLE_CMD = "gradle clean war publish -Dmaven.settings=" + session.getLocalPath( build.CONFIG_MAVEN_CFGFILE ) + " " + MODULE_ADDITIONAL_OPTIONS;

		// execute gradle
		action.info( "using gradle:" );
		session.customCheckErrorsNormal( action , "gradle --version" );
		
		action.info( "execute: " + GRADLE_CMD );
		int timeout = action.setTimeoutUnlimited();
		int status = session.customGetStatusNormal( action , CODEPATH.folderPath , GRADLE_CMD );
		action.setTimeout( timeout );

		if( status != 0 ) {
			action.error( "buildGradle: gradle build failed" );
			return( false );
		}
		
		action.info( "buildGradle: gradle build successfully finished" );
		return( true );
	}

	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	}

}
