package org.urm.meta.product;

import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;

public class MetaProductBuildSettings {

	// version
	public static String PROPERTY_RELEASE_LASTMAJOR = "release.lastmajor";
	public static String PROPERTY_RELEASE_NEXTMAJOR = "release.nextmajor";
	public static String PROPERTY_RELEASE_LASTMINOR = "release.lastminor";
	public static String PROPERTY_RELEASE_NEXTMINOR = "release.nextminor";
	public static String PROPERTY_RELEASE_VERSION = "release.version";
	public static String PROPERTY_APPVERSION = "app.version";
	public static String PROPERTY_LOGPATH = "app.logpath";

	// build operation
	public static String PROPERTY_ARTEFACTDIR = "build.artefacts";
	public static String PROPERTY_NEXUS_REPO = "nexus.repo";
	public static String PROPERTY_NEXUS_REPO_THIRDPARTY = "nexus.thirdparty";
	public static String PROPERTY_MAVEN_CFGFILE = "maven.conf";

	// build source code
	public static String PROPERTY_BRANCHNAME = "source.branch";
	public static String PROPERTY_RELEASE_GROUPFOLDER = "release.group";

	public static String BUILDER_TYPE_MAVEN = "Maven";
	public static String BUILDER_TYPE_GRADLE = "Gradle";
	public static String BUILDER_TYPE_DOTNET = ".NET";
	
	public String name;
	public Meta meta;
	public MetaProductSettings settings;
	
	public String CONFIG_RELEASE_LASTMAJOR;
	public String CONFIG_RELEASE_NEXTMAJOR;
	public String CONFIG_RELEASE_LASTMINOR;
	public String CONFIG_RELEASE_NEXTMINOR;
	public String CONFIG_RELEASE_VERSION;
	public String CONFIG_APPVERSION;
	public String CONFIG_LOGPATH;
	
	public String CONFIG_ARTEFACTDIR;
	public String CONFIG_NEXUS_REPO;
	public String CONFIG_NEXUS_REPO_THIRDPARTY;
	public String CONFIG_MAVEN_CFGFILE;

	public String CONFIG_COMMIT_TRACKERLIST;
	public String CONFIG_BRANCHNAME;
	public String CONFIG_RELEASE_GROUPFOLDER;

	public ObjectProperties ops;
	
	public MetaProductBuildSettings( String name , Meta meta , MetaProductSettings settings ) {
		this.name = name;
		this.meta = meta;
		this.settings = settings;
	}
	
	public ObjectProperties getProperties() {
		return( ops );
	}
	
	public void scatterProperties() throws Exception {
		CONFIG_RELEASE_LASTMAJOR = ops.getStringProperty( PROPERTY_RELEASE_LASTMAJOR );
		CONFIG_RELEASE_NEXTMAJOR = ops.getStringProperty( PROPERTY_RELEASE_NEXTMAJOR );
		CONFIG_RELEASE_LASTMINOR = ops.getStringProperty( PROPERTY_RELEASE_LASTMINOR );
		CONFIG_RELEASE_NEXTMINOR = ops.getStringProperty( PROPERTY_RELEASE_NEXTMINOR );
		CONFIG_RELEASE_VERSION = ops.getStringProperty( PROPERTY_RELEASE_VERSION );
		CONFIG_APPVERSION = ops.getStringProperty( PROPERTY_APPVERSION );
		CONFIG_LOGPATH = ops.getPathProperty( PROPERTY_LOGPATH );
		
		CONFIG_ARTEFACTDIR = ops.getPathProperty( PROPERTY_ARTEFACTDIR );
		CONFIG_NEXUS_REPO = ops.getStringProperty( PROPERTY_NEXUS_REPO );
		CONFIG_NEXUS_REPO_THIRDPARTY = ops.getStringProperty( PROPERTY_NEXUS_REPO_THIRDPARTY );
		CONFIG_MAVEN_CFGFILE = ops.getPathProperty( PROPERTY_MAVEN_CFGFILE );

		CONFIG_BRANCHNAME = ops.getStringProperty( PROPERTY_BRANCHNAME );
		CONFIG_RELEASE_GROUPFOLDER = ops.getStringProperty( PROPERTY_RELEASE_GROUPFOLDER );
	}

	public void createSettings( ObjectProperties ops ) throws Exception {
		this.ops = ops;
		ops.updateProperties();
		ops.initFinished();
	}
	
	public MetaProductBuildSettings copy( Meta meta , MetaProductSettings product , ObjectProperties rparent ) throws Exception {
		MetaProductBuildSettings r = new MetaProductBuildSettings( name , meta , product );
		r.ops = ops.copy( rparent );
		r.scatterProperties();
		return( r );
	}
	
	public void setProperties( PropertySet props ) throws Exception {
		ops.updateProperties( props , true );
	}

}
