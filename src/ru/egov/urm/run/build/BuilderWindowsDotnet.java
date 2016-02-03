package ru.egov.urm.run.build;

import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.BuildStorage;

public class BuilderWindowsDotnet extends Builder {

	public BuilderWindowsDotnet( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( BUILDER , project , storage , TAG , BUILD_OPTIONS , APPVERSION );
	}

	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		Account account = Account.getAccount( action , action.meta.product.CONFIG_WINBUILD_HOSTLOGIN , VarOSTYPE.WINDOWS );
		return( action.context.pool.getExecutor( action , account , "build" ));
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
