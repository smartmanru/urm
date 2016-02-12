package ru.egov.urm.run.build;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.BuildStorage;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;
import ru.egov.urm.vcs.ProjectVersionControl;

public class BuilderWindowsDotnet extends Builder {

	RemoteFolder CODEPATH;
	
	public BuilderWindowsDotnet( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( BUILDER , project , storage , TAG , APPVERSION );
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
		CODEPATH = buildFolder.getSubFolder( action , project.PROJECT );
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
		return( true );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		return( true );
	}
	
	@Override public boolean runBuild( ActionBase action ) throws Exception {
		// msbuilder params
		String MSBUILD_PATH = "";
		String BUILDEVERSION = project.getBuilderVersion( action );
		if( BUILDEVERSION.equals( "VS-2013-EXPRESS" ) )
			MSBUILD_PATH = Common.getQuoted( "C:\\Program Files (x86)\\MSBuild\\12.0\\Bin\\msbuild.exe" );
		else
			action.exit( "unexpected builder version=" + BUILDEVERSION );
		
		String NUGET_PATH = getNugetSourcePath( action );
		String MSBUILD_OPTIONS = "/t:Clean,Build /p:Configuration=Release";
		if( action.context.CTX_SHOWALL )
			MSBUILD_OPTIONS += " /verbosity:detailed";

		String BUILD_CMD = MSBUILD_PATH + " " + MSBUILD_OPTIONS;
		action.log( "build PATCHPATH=" + CODEPATH.folderPath + ", options=" + MSBUILD_OPTIONS + ", cmd=" + BUILD_CMD + 
				" using nuget to nexus path " + NUGET_PATH + "..." );

		ShellExecutor session = action.session;
		int timeout = action.setTimeoutUnlimited();
		int status = session.customGetStatusNormal( action , CODEPATH.folderPath , BUILD_CMD );
		action.setTimeout( timeout );

		if( status != 0 ) {
			action.log( "buildDotnet: msbuild failed" );
			return( false );
		}
					
		action.log( "buildDotnet: msbuild successfully finished" );
		return( true );
	}
	
	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		ShellExecutor session = createShell( action );
		RedistStorage storage = action.artefactory.getRedistStorage( "build" , session.account );
		RemoteFolder buildFolder = storage.getRedistTmpFolder( action );
		RemoteFolder CODEPATH = buildFolder.getSubFolder( action , project.PROJECT );
		CODEPATH.removeThis( action );
	}

	private String getNugetSourcePath( ActionBase action ) throws Exception {
		return( action.meta.product.CONFIG_NEXUS_BASE + "/service/local/nuget/" + action.meta.product.CONFIG_NEXUS_REPO + "-nuget/" );
	}
	
}
