package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProject;

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
		MetaProductBuildSettings build = action.getBuildSettings( project.meta );
		ServerAuthResource res = action.getResource( build.CONFIG_NEXUS_RESOURCE );
		return( res.BASEURL + "/content/repositories/" + build.CONFIG_NEXUS_REPO );
	}

}
