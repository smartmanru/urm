package ru.egov.urm.run.build;

import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.BuildStorage;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;
import ru.egov.urm.vcs.ProjectVersionControl;

public class BuilderWindowsDotnet extends Builder {

	public BuilderWindowsDotnet( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( BUILDER , project , storage , TAG , BUILD_OPTIONS , APPVERSION );
	}

	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		Account account = action.getWinBuildAccount();
		return( action.context.pool.getExecutor( action , account , "build" ));
	}

	@Override public boolean exportCode( ActionBase action ) throws Exception {
		ShellExecutor session = createShell( action );
		
		// drop old
		RedistStorage storage = action.artefactory.getRedistStorage( "build" , session.account );
		RemoteFolder buildFolder = storage.getRedistTmpFolder( action );
		buildFolder.ensureExists( action );
		RemoteFolder CODEPATH = buildFolder.getSubFolder( action , project.PROJECT );
		CODEPATH.removeThis( action );
	
		// checkout
		ProjectVersionControl vcs = new ProjectVersionControl( action , true ); 
		if( !vcs.export( CODEPATH , project , "" , TAG , "" ) ) {
			action.log( "patchCheckout: having problem to export code" );
			return( false );
		}
		
		return( true );
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
