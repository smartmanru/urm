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
	public String BUILD_OPTIONS;
	public String APPVERSION;
	
	boolean MODULEOPTIONS_WAR = false;
	boolean MODULEOPTIONS_POMNEW = false;
	boolean MODULEOPTIONS_SETVERSION = false;
	boolean MODULEOPTIONS_REPLACESNAPSHOTS = false;
	boolean MODULEOPTIONS_COMPACT_STATIC = false;

	abstract public ShellExecutor createShell( ActionBase action ) throws Exception;
	abstract public boolean exportCode( ActionBase action ) throws Exception;
	abstract public boolean prepareSource( ActionBase action ) throws Exception;
	abstract public boolean checkSourceCode( ActionBase action ) throws Exception;
	abstract public boolean runBuild( ActionBase action ) throws Exception;
	abstract public void removeExportedCode( ActionBase action ) throws Exception;
	
	protected Builder( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		this.BUILDER = BUILDER;
		this.project = project;
		this.storage = storage;
		this.TAG = TAG;
		this.BUILD_OPTIONS = BUILD_OPTIONS;
		this.APPVERSION = APPVERSION;

		// war build
		if( BUILD_OPTIONS.indexOf( 'w' ) >= 0 )
			MODULEOPTIONS_WAR = true;
		// replace original files with .new ones
		if( BUILD_OPTIONS.indexOf( 'n' ) >= 0 )
			MODULEOPTIONS_POMNEW = true;
		// add profile for war build
		if( BUILD_OPTIONS.indexOf( 's' ) >= 0 )
			MODULEOPTIONS_COMPACT_STATIC = true;
		// force set version
		if( BUILD_OPTIONS.indexOf( 'v' ) >= 0 )
			MODULEOPTIONS_SETVERSION = true;
		// clear all snapshots from release
		if( BUILD_OPTIONS.indexOf( 'r' ) >= 0 )
			MODULEOPTIONS_REPLACESNAPSHOTS = true;
	}
	
	public String getNexusPath( ActionBase action , MetaSourceProject project ) throws Exception {
		return( action.meta.product.CONFIG_NEXUS_BASE + "/content/repositories/" + action.meta.product.CONFIG_NEXUS_REPO );
	}

}
