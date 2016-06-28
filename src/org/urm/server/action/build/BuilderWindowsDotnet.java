package org.urm.server.action.build;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.shell.Account;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.BuildStorage;
import org.urm.server.storage.RedistStorage;
import org.urm.server.storage.RemoteFolder;
import org.urm.server.vcs.ProjectVersionControl;

public class BuilderWindowsDotnet extends Builder {

	RemoteFolder CODEPATH;
	
	public BuilderWindowsDotnet( String BUILDER , MetaSourceProject project , BuildStorage storage , String TAG , String BUILD_OPTIONS , String APPVERSION ) {
		super( BUILDER , project , storage , TAG , APPVERSION );
	}

	@Override public ShellExecutor createShell( ActionBase action ) throws Exception {
		Account account = action.getWinBuildAccount();
		return( action.engine.pool.getExecutor( action , account , "build" ));
	}

	@Override public boolean exportCode( ActionBase action ) throws Exception {
		ShellExecutor session = createShell( action );
		
		// drop old
		RedistStorage storage = action.artefactory.getRedistStorage( action , session.account );
		RemoteFolder buildFolder = storage.getRedistTmpFolder( action , "build" );
		CODEPATH = buildFolder.getSubFolder( action , project.PROJECT );
		CODEPATH.removeThis( action );
	
		// checkout
		ProjectVersionControl vcs = new ProjectVersionControl( action , true ); 
		if( !vcs.export( CODEPATH , project , "" , TAG , "" ) ) {
			action.error( "patchCheckout: having problem to export code" );
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
		String MSBUILD_OPTIONS = "/t:Clean,Build /p:Configuration=Release /p:preferreduilang=en-US";
		if( action.context.CTX_SHOWALL )
			MSBUILD_OPTIONS += " /verbosity:detailed";

		String BUILD_CMD = MSBUILD_PATH + " " + MSBUILD_OPTIONS;
		action.info( "build PATCHPATH=" + CODEPATH.folderPath + ", options=" + MSBUILD_OPTIONS + ", cmd=" + BUILD_CMD + 
				" using nuget to nexus path " + NUGET_PATH + "..." );

		ShellExecutor session = createShell( action );
		int timeout = action.setTimeoutUnlimited();
		int status = session.customGetStatusNormal( action , CODEPATH.folderPath , BUILD_CMD );
		action.setTimeout( timeout );

		if( status != 0 ) {
			action.error( "buildDotnet: msbuild failed" );
			return( false );
		}

		// upload package
		String nugetId = action.meta.product.CONFIG_PRODUCT + ".project." + project.PROJECT; 
		String nugetPackCmd = "nuget pack package.nuspec -Version " + APPVERSION + " -Properties id=" + nugetId;
		RemoteFolder NUGETPATH = CODEPATH.getSubFolder( action , "packages.build" ); 
		timeout = action.setTimeoutUnlimited();
		status = session.customGetStatusNormal( action , NUGETPATH.folderPath , nugetPackCmd );
		action.setTimeout( timeout );
		
		if( status != 0 ) {
			action.error( "buildDotnet: nuget pack failed" );
			return( false );
		}

		String packageName = nugetId + "." + APPVERSION + ".nupkg";
		String nugetUploadCmd = "nuget push " + packageName + " -Source " + NUGET_PATH;
		timeout = action.setTimeoutUnlimited();
		status = session.customGetStatusNormal( action , NUGETPATH.folderPath , nugetUploadCmd );
		action.setTimeout( timeout );
		
		if( status != 0 ) {
			action.error( "buildDotnet: nuget push failed" );
			return( false );
		}

		action.info( "buildDotnet: msbuild successfully finished" );
		return( true );
	}
	
	@Override public void removeExportedCode( ActionBase action ) throws Exception {
		ShellExecutor session = createShell( action );
		RedistStorage storage = action.artefactory.getRedistStorage( action , session.account );
		RemoteFolder buildFolder = storage.getRedistTmpFolder( action , "export" );
		RemoteFolder CODEPATH = buildFolder.getSubFolder( action , project.PROJECT );
		CODEPATH.removeThis( action );
	}

	private String getNugetSourcePath( ActionBase action ) throws Exception {
		return( action.meta.product.CONFIG_NEXUS_BASE + "/service/local/nuget/" + action.meta.product.CONFIG_NEXUS_REPO + "-nuget/" );
	}
	
}
