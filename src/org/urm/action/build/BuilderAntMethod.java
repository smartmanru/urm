package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.common.PropertySet;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaSourceProject;

public class BuilderAntMethod extends Builder {

	public BuilderAntMethod( ServerProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		super( builder , project , storage , TAG , APPVERSION );
	}

	@Override public boolean prepareSource( ActionBase action ) throws Exception {
		return( true );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		return( true );
	}

	@Override public boolean runBuild( ActionBase action ) throws Exception {
		// ant params
		action.info( "build PATCHPATH=" + CODEPATH.folderPath + " using ant " + builder.VERSION + " ..." );
		PropertySet props = super.createProperties( action , project );

		// set environment
		String BUILD_JAVA_HOME = builder.JAVA_JDKHOMEPATH;
		String BUILD_ANT_HOME = builder.ANT_HOMEPATH; 
		String MODULE_ADDITIONAL_OPTIONS = super.getVarString( action , props , project.BUILDER_ADDOPTIONS );
		String ANT_CMD = "ant ";
		if( !action.isDebug() )
			ANT_CMD += "-silent ";
		ANT_CMD += MODULE_ADDITIONAL_OPTIONS;

		ShellExecutor session = action.shell;
		session.export( action , "JAVA_HOME" , session.getLocalPath( BUILD_JAVA_HOME ) );
		session.export( action , "ANT_HOME" , session.getLocalPath( BUILD_ANT_HOME ) );
		session.export( action , "PATH" , session.getLocalPath( session.getVariable( "ANT_HOME" ) + "/bin" ) + session.getPathBreak() + 
				session.getLocalPath( session.getVariable( "JAVA_HOME" ) + "/bin" ) + session.getPathBreak() + 
				session.getVariable( "PATH" ) );

		// execute ant
		action.info( "using ant:" );
		session.customCheckErrorsNormal( action , "ant -version" );
		
		action.info( "execute: " + ANT_CMD );
		int timeout = action.setTimeoutUnlimited();
		int status = session.customGetStatusCheckErrors( action , CODEPATH.folderPath , ANT_CMD );
		action.setTimeout( timeout );

		if( status != 0 ) {
			action.error( "build: ant build failed" );
			return( false );
		}
					
		action.info( "build: ant build successfully finished" );
		return( true );
	}

	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	}

}
