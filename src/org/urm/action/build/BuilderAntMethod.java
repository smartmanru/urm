package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaSourceProject;

public class BuilderAntMethod extends Builder {

	public BuilderAntMethod( ServerProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		super( builder , project , storage , TAG , APPVERSION );
	}

	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		if( builder.remote )
			action.exitNotImplemented();
		
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
		// maven params
		LocalFolder CODEPATH = storage.buildFolder; 

		action.info( "build PATCHPATH=" + CODEPATH.folderPath + " using ant " + builder.VERSION + " ..." );

		// set environment
		String BUILD_JAVA_HOME = builder.JAVA_JDKHOMEPATH;
		String BUILD_ANT_HOME = builder.ANT_HOMEPATH; 
		String ANT_CMD = "ant";
		if( !action.isDebug() )
			ANT_CMD += " -silent";

		ShellExecutor session = action.shell;
		session.export( action , "JAVA_HOME" , BUILD_JAVA_HOME );
		session.export( action , "ANT_HOME" , BUILD_ANT_HOME );
		String delimiter = ( action.isLocalWindows() )? ";" : ":";
		session.export( action , "PATH" , "$ANT_HOME" + delimiter + "$JAVA_HOME/bin" + delimiter + "$PATH" );

		// execute maven
		action.info( "using ant:" );
		session.customCheckErrorsNormal( action , "ant -version" );
		
		action.info( "execute: " + ANT_CMD );
		int timeout = action.setTimeoutUnlimited();
		int status = session.customGetStatusNormal( action , CODEPATH.folderPath , ANT_CMD );
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
