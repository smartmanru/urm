package ru.egov.urm.run.build;

import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.BuildStorage;

public abstract class Builder {

	public String BUILDER;
	public MetaSourceProject project;
	public BuildStorage storage;
	public String TAG;
	public String APPVERSION;
	
	abstract public ShellExecutor createShell( ActionBase action ) throws Exception;
	abstract public boolean exportCode( ActionBase action ) throws Exception;
	abstract public boolean prepareSource( ActionBase action ) throws Exception;
	abstract public boolean checkSourceCode( ActionBase action ) throws Exception;
	abstract public boolean runBuild( ActionBase action ) throws Exception;
	abstract public void removeExportedCode( ActionBase action ) throws Exception;
	
	protected Builder( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		this.BUILDER = BUILDER;
		this.project = project;
		this.storage = storage;
		this.TAG = TAG;
		this.APPVERSION = APPVERSION;
	}

	public String getNexusPath( ActionBase action , MetaSourceProject project ) throws Exception {
		return( action.meta.product.CONFIG_NEXUS_BASE + "/content/repositories/" + action.meta.product.CONFIG_NEXUS_REPO );
	}

}
