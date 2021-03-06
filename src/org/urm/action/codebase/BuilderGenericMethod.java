package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.shell.Shell;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.product.MetaSourceProject;

public class BuilderGenericMethod extends Builder {

	public BuilderGenericMethod( ProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		super( builder , project , storage , TAG , APPVERSION );
	}

	@Override 
	public boolean prepareSource( ActionBase action ) throws Exception {
		return( true );
	}
	
	@Override 
	public boolean checkSourceCode( ActionBase action ) throws Exception {
		return( true );
	}

	@Override 
	public boolean runBuild( ActionBase action ) throws Exception {
		// generic params
		action.info( "build PATCHPATH=" + CODEPATH.folderPath + " using generic method=" + builder.NAME + " ..." );
		PropertySet props = super.createProperties( action , project );

		// set environment
		String GENERIC_CMD = super.getVarString( action , props , builder.BUILDER_COMMAND );
		String GENERIC_OPTIONS = super.getVarString( action , props , project.BUILDER_ADDOPTIONS );
		GENERIC_CMD += " " + GENERIC_OPTIONS;

		ShellExecutor session = action.shell;

		// execute maven
		action.info( "using generic: " + GENERIC_CMD );
		int status = session.customGetStatusNormal( action , CODEPATH.folderPath , GENERIC_CMD , Shell.WAIT_INFINITE );

		if( status != 0 ) {
			action.error( "build: generic build failed" );
			return( false );
		}
					
		action.info( "build: generic build successfully finished" );
		return( true );
	}

}
