package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerBuilders;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductSettings;
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

	static String PROPERTY_PROJECTNAME = "project.name";
	static String PROPERTY_PROJECTDESC = "project.desc";
	static String PROPERTY_REPOSITORY = "project.repository";
	static String PROPERTY_BUILDGROUP = "project.buildgroup";
	static String PROPERTY_GROUPPOS = "project.grouppos";
	static String PROPERTY_REPOPATH = "project.repopath";
	static String PROPERTY_CODEPATH = "project.codepath";
	static String PROPERTY_BUILDTAG = "build.tag";
	static String PROPERTY_BUILDVERSION = "build.version";
	
	protected Builder( ServerProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		this.builder = builder;
		this.project = project;
		this.storage = storage;
		this.TAG = TAG;
		this.APPVERSION = APPVERSION;
	}

	public static Builder createBuilder( ActionBase action , MetaSourceProject project , String TAG , String VERSION ) throws Exception {
		String BUILDER = project.getBuilder( action );
		
		ServerBuilders builders = action.getServerBuilders();
		ServerProjectBuilder builder = builders.getBuilder( BUILDER );
		
		Builder projectBuilder = null;
		
		BuildStorage storage = action.artefactory.getEmptyBuildStorage( action , project );
		if( builder.isGeneric() )
			projectBuilder = new BuilderGenericMethod( builder , project , storage , TAG , VERSION );
		else
		if( builder.isAnt() )
			projectBuilder = new BuilderAntMethod( builder , project , storage , TAG , VERSION );
		else
		if( builder.isMaven() )
			projectBuilder = new BuilderMavenMethod( builder , project , storage , TAG , VERSION );
		else
		if( builder.isGradle() )
			projectBuilder = new BuilderGradleMethod( builder , project , storage , TAG , VERSION );
		else
		if( builder.isWinBuild() )
			projectBuilder = new BuilderWinbuildMethod( builder , project , storage , TAG , VERSION );
		else {
			String method = Common.getEnumLower( builder.builderMethod );
			action.exit2( _Error.UnknownBuilderMethod2 , "unknown builder method=" + method + " (builder=" + BUILDER + ")" , method , BUILDER );
		}
		
		return( projectBuilder );
	}

	public ShellExecutor createShell( ActionBase action ) throws Exception {
		return( builder.createShell( action , true ) );
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

	public PropertySet createProperties( ActionBase action , MetaSourceProject project ) throws Exception {
		MetaProductSettings product = project.meta.getProductSettings( action );
		MetaProductBuildSettings settings = product.getBuildSettings( action );
		PropertySet props = settings.getProperties();
		PropertySet propsGenerated = new PropertySet( "build" , props );
		propsGenerated.setManualStringProperty( PROPERTY_PROJECTNAME , project.NAME );
		propsGenerated.setManualStringProperty( PROPERTY_PROJECTDESC , project.DESC );
		propsGenerated.setManualStringProperty( PROPERTY_REPOSITORY , project.REPOSITORY );
		propsGenerated.setManualStringProperty( PROPERTY_BUILDGROUP , project.BUILDGROUP );
		propsGenerated.setManualStringProperty( PROPERTY_GROUPPOS , "" + project.POS );
		propsGenerated.setManualStringProperty( PROPERTY_REPOPATH , project.REPOPATH );
		propsGenerated.setManualStringProperty( PROPERTY_CODEPATH , project.CODEPATH );
		propsGenerated.setManualStringProperty( PROPERTY_BUILDTAG , TAG );
		propsGenerated.setManualStringProperty( PROPERTY_BUILDVERSION , APPVERSION );
		return( propsGenerated );
	}
	
	public String getVarString( ActionBase action , PropertySet props , String value ) throws Exception {
		String res = props.getFinalString( value , action.shell.isWindows() , true , false );
		return( res );
	}
	
}
