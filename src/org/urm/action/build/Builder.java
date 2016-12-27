package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProject;

public abstract class Builder {

	public ServerProjectBuilder builder;
	public MetaSourceProject project;
	public BuildStorage storage;
	public String TAG;
	public String APPVERSION;
	
	public LocalFolder CODEPATH;
	
	abstract public boolean prepareSource( ActionBase action ) throws Exception;
	abstract public boolean checkSourceCode( ActionBase action ) throws Exception;
	abstract public boolean runBuild( ActionBase action ) throws Exception;
	abstract public void removeExportedCode( ActionBase action ) throws Exception;
	
	protected Builder( ServerProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		this.builder = builder;
		this.project = project;
		this.storage = storage;
		this.TAG = TAG;
		this.APPVERSION = APPVERSION;
	}

	public ShellExecutor createShell( ActionBase action ) throws Exception {
		if( builder.remote ) {
			Account account = builder.getRemoteAccount( action );
			return( action.createDedicatedRemoteShell( "build" , account , builder.AUTHRESOURCE ) );
		}
		
		return( action.createDedicatedShell( "build" ) );
	}

	public boolean exportCode( ActionBase action ) throws Exception {
		// drop old
		RedistStorage storage = action.artefactory.getRedistStorage( action , action.shell.account );
		RemoteFolder buildFolder = storage.getRedistTmpFolder( action , "build-" + action.ID );
		LocalFolder buildParent = action.getLocalFolder( buildFolder.folderPath );
		buildParent.ensureExists( action );
		
		CODEPATH = buildParent.getSubFolder( action , project.NAME );
		CODEPATH.removeThis( action );
	
		// checkout
		ProjectVersionControl vcs = new ProjectVersionControl( action );
		LocalFolder path = action.getLocalFolder( CODEPATH.folderPath );
		if( !vcs.export( path , project , "" , TAG , "" ) ) {
			action.error( "patchCheckout: having problem to export code" );
			return( false );
		}
		
		return( true );
	}
	
	public String getNexusPath( ActionBase action , MetaSourceProject project ) throws Exception {
		ServerAuthResource res = action.getResource( builder.TARGETNEXUS );
		MetaProductBuildSettings build = action.getBuildSettings( project.meta );
		return( res.BASEURL + "/content/repositories/" + build.CONFIG_NEXUS_REPO );
	}

}
