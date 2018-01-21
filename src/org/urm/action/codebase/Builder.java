package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BuildStorage;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.engine.vcs.ProjectVersionControl;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductUnit;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaUnits;

public abstract class Builder {

	public ProjectBuilder builder;
	public MetaSourceProject project;
	public BuildStorage storage;
	public String TAG;
	public String APPVERSION;
	
	public LocalFolder CODEPATH;
	
	abstract public boolean prepareSource( ActionBase action ) throws Exception;
	abstract public boolean checkSourceCode( ActionBase action ) throws Exception;
	abstract public boolean runBuild( ActionBase action ) throws Exception;

	static String PROPERTY_PROJECTNAME = "project.name";
	static String PROPERTY_PROJECTDESC = "project.desc";
	static String PROPERTY_REPOSITORY = "project.repository";
	static String PROPERTY_UNIT = "project.unit";
	static String PROPERTY_GROUPPOS = "project.grouppos";
	static String PROPERTY_REPOPATH = "project.repopath";
	static String PROPERTY_CODEPATH = "project.codepath";
	static String PROPERTY_BUILDTAG = "build.tag";
	static String PROPERTY_BUILDVERSION = "build.version";
	
	protected Builder( ProjectBuilder builder , MetaSourceProject project , BuildStorage storage , String TAG , String APPVERSION ) {
		this.builder = builder;
		this.project = project;
		this.storage = storage;
		this.TAG = TAG;
		this.APPVERSION = APPVERSION;
	}

	public void removeExportedCode( ActionBase action ) throws Exception {
		CODEPATH.removeThis( action );
	}

	public static Builder createBuilder( ActionBase action , MetaSourceProject project , String TAG , String VERSION ) throws Exception {
		ProjectBuilder builder = project.getBuilder( action );
		
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
			String method = Common.getEnumLower( builder.BUILDER_METHOD_TYPE );
			action.exit2( _Error.UnknownBuilderMethod2 , "unknown builder method=" + method + " (builder=" + builder.NAME + ")" , method , builder.NAME );
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
		AuthResource res = action.getResource( builder.TARGET_RESOURCE_ID );
		MetaProductBuildSettings build = action.getBuildSettings( project.meta );
		return( res.BASEURL + "/content/repositories/" + build.CONFIG_NEXUS_REPO );
	}

	public PropertySet createProperties( ActionBase action , MetaSourceProject project ) throws Exception {
		MetaProductSettings product = project.meta.getProductSettings();
		MetaProductBuildSettings settings = product.getBuildSettings( action );
		MirrorRepository mirror = project.getMirror( action );
		MetaUnits units = project.meta.getUnits();
		String unitName = "";
		if( project.UNIT_ID != null ) {
			MetaProductUnit unit = units.getUnit( project.UNIT_ID );
			unitName = unit.NAME;
		}
		
		ObjectProperties ops = settings.getProperties();
		PropertySet props = ops.getProperties();
		PropertySet propsGenerated = new PropertySet( "build" , props );
		propsGenerated.setManualStringProperty( PROPERTY_PROJECTNAME , project.NAME );
		propsGenerated.setManualStringProperty( PROPERTY_PROJECTDESC , project.DESC );
		propsGenerated.setManualStringProperty( PROPERTY_REPOSITORY , mirror.RESOURCE_REPO );
		propsGenerated.setManualStringProperty( PROPERTY_UNIT , unitName );
		propsGenerated.setManualStringProperty( PROPERTY_GROUPPOS , "" + project.PROJECT_POS );
		propsGenerated.setManualStringProperty( PROPERTY_REPOPATH , mirror.RESOURCE_ROOT );
		propsGenerated.setManualStringProperty( PROPERTY_CODEPATH , mirror.RESOURCE_DATA );
		propsGenerated.setManualStringProperty( PROPERTY_BUILDTAG , TAG );
		propsGenerated.setManualStringProperty( PROPERTY_BUILDVERSION , APPVERSION );
		return( propsGenerated );
	}
	
	public String getVarString( ActionBase action , PropertySet props , String value ) throws Exception {
		String res = props.getFinalString( value , action.shell.isWindows() , true , false );
		return( res );
	}
	
}
