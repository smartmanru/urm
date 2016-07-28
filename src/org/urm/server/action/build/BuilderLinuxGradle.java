package org.urm.server.action.build;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.BuildStorage;
import org.urm.server.storage.LocalFolder;
import org.urm.server.vcs.ProjectVersionControl;

public class BuilderLinuxGradle extends Builder {

	public BuilderLinuxGradle( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( BUILDER , project , storage , TAG , APPVERSION );
	}
	
	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		return( action.createDedicatedShell( "build" ) );
	}
	
	@Override public boolean exportCode( ActionBase action ) throws Exception {
		// drop old
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	
		// checkout
		ProjectVersionControl vcs = new ProjectVersionControl( action , true ); 
		if( !vcs.export( CODEPATH , project , "" , TAG , "" ) ) {
			action.error( "patchCheckout: having problem to export code" );
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

		ShellExecutor session = action.shell;
		session.export( action , "JAVA_HOME" , action.meta.product.CONFIG_BUILDBASE + "/" + BUILD_JAVA_VERSION );
		session.export( action , "GR_HOME" , action.meta.product.CONFIG_BUILDBASE + "/" + BUILD_GRADLE_VERSION );
		session.export( action , "GR" , "$GR_HOME/bin" );
		session.export( action , "PATH" , "$GR:$JAVA_HOME/bin:$PATH" );

		String GRADLE_CMD = "gradle clean war publish -Dmaven.settings=" + action.meta.product.CONFIG_MAVEN_CFGFILE;

		// execute gradle
		action.info( "using gradle:" );
		session.customCheckErrorsNormal( action , "which gradle" );
		session.customCheckErrorsNormal( action , "gradle --version" );
		
		action.info( "execute: " + GRADLE_CMD );
		int timeout = action.setTimeoutUnlimited();
		int status = session.customGetStatusNormal( action , storage.buildFolder.folderPath , GRADLE_CMD );
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
