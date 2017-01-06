package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaSourceProject;

public class BuilderGenericMethod extends Builder {

	public BuilderGenericMethod( ServerProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		super( builder , project , storage , TAG , APPVERSION );
	}

	@Override public boolean prepareSource( ActionBase action ) throws Exception {
		return( true );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		return( true );
	}

	@Override public boolean runBuild( ActionBase action ) throws Exception {
		// generic params
		action.info( "build PATCHPATH=" + CODEPATH.folderPath + " using ant " + builder.VERSION + " ..." );

		// set environment
		String GENERIC_CMD = super.getVarString( action , builder.GENERIC_COMMAND );
		String GENERIC_OPTIONS = super.getVarString( action , project.BUILDER_ADDOPTIONS );
		GENERIC_CMD += " " + GENERIC_OPTIONS;

		ShellExecutor session = action.shell;

		// execute maven
		action.info( "using generic: " + GENERIC_CMD );
		int timeout = action.setTimeoutUnlimited();
		int status = session.customGetStatusCheckErrors( action , CODEPATH.folderPath , GENERIC_CMD );
		action.setTimeout( timeout );

		if( status != 0 ) {
			action.error( "build: generic build failed" );
			return( false );
		}
					
		action.info( "build: generic build successfully finished" );
		return( true );
	}

	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		LocalFolder CODEPATH = storage.buildFolder; 
		CODEPATH.removeThis( action );
	}

}