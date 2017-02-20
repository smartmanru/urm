package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProject;

public class BuilderWinbuildMethod extends Builder {

	public BuilderWinbuildMethod( ServerProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		super( builder , project , storage , TAG , APPVERSION );
	}

	@Override public boolean prepareSource( ActionBase action ) throws Exception {
		return( true );
	}
	
	@Override public boolean checkSourceCode( ActionBase action ) throws Exception {
		return( true );
	}
	
	@Override public boolean runBuild( ActionBase action ) throws Exception {
		// msbuilder params
		String MSBUILD_PATH = builder.MSBUILD_HOMEPATH + "\\Bin\\msbuild.exe";
		
		String NUGET_PATH = getNugetSourcePath( action );
		String MSBUILD_OPTIONS = builder.MSBUILD_OPTIONS;
		if( action.context.CTX_SHOWALL )
			MSBUILD_OPTIONS += " /verbosity:detailed";
		String MODULE_ADDITIONAL_OPTIONS = super.getVarString( action , project.BUILDER_ADDOPTIONS );
		MSBUILD_OPTIONS += " " + MODULE_ADDITIONAL_OPTIONS;

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
		String nugetId = project.meta.name + ".project." + project.NAME; 
		String nugetPackCmd = "nuget pack package.nuspec -Version " + APPVERSION + " -Properties id=" + nugetId;
		LocalFolder NUGETPATH = CODEPATH.getSubFolder( action , "packages.build" ); 
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
		RemoteFolder CODEPATH = buildFolder.getSubFolder( action , project.NAME );
		CODEPATH.removeThis( action );
	}

	private String getNugetSourcePath( ActionBase action ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( project.meta );
		ServerAuthResource res = action.getResource( builder.TARGETNEXUS );
		return( res.BASEURL + "/service/local/nuget/" + build.CONFIG_NEXUS_REPO + "-nuget/" );
	}
	
}
