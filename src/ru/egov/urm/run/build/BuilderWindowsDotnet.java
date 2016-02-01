package ru.egov.urm.run.build;

import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.BuildStorage;

public class BuilderWindowsDotnet extends Builder {

	public BuilderWindowsDotnet( MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( project , storage , TAG , BUILD_OPTIONS , APPVERSION );
	}

	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		return( action.context.pool.createDedicatedLocalShell( action , "build" ) );
	}

	@Override public boolean exportCode( ActionBase action ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public boolean prepareSource( ActionBase action ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public boolean runBuild( ActionBase action ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}
	
	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		action.exitNotImplemented();
	}
	
	
}
