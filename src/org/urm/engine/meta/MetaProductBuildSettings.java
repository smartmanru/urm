package org.urm.engine.meta;

import java.nio.charset.Charset;

import org.urm.action.ActionBase;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductBuildSettings extends PropertyController {

	public String name;
	public Meta meta;
	public MetaProductSettings product;
	
	public String CONFIG_RELEASE_LASTMAJOR;
	public String CONFIG_RELEASE_NEXTMAJOR;
	public String CONFIG_RELEASE_LASTMINOR;
	public String CONFIG_RELEASE_NEXTMINOR;
	public String CONFIG_RELEASE_VERSION;
	public String CONFIG_APPVERSION;
	public String CONFIG_LOGPATH;
	
	public String CONFIG_NEXUS_RESOURCE;
	public String CONFIG_NEXUS_REPO;
	public String CONFIG_NEXUS_REPO_THIRDPARTY;
	public String CONFIG_BUILDER_TYPE;
	public String CONFIG_BUILDER_VERSION;
	public String CONFIG_BUILDER_OPTIONS;
	public String CONFIG_BUILDER_REMOTE;
	public String CONFIG_MAVEN_VERSION;
	public String CONFIG_MAVEN_CFGFILE;
	public String CONFIG_MAVEN_OPTIONS;
	public String CONFIG_MAVEN_JAVA_VERSION;
	public String CONFIG_ARTEFACTDIR;

	public String CONFIG_COMMIT_TRACKERLIST;
	public String CONFIG_BRANCHNAME;
	public String CONFIG_SOURCE_CHARSET;
	public String CONFIG_SOURCE_RELEASEROOTDIR;
	public String CONFIG_RELEASE_GROUPFOLDER;
	public String CONFIG_SOURCE_CFG_ROOTDIR;
	public String CONFIG_SOURCE_CFG_LIVEROOTDIR;
	public String CONFIG_SOURCE_SQL_POSTREFRESH;

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
	public static String PROPERTY_BUILDER_TYPE = "builder.type";
	public static String PROPERTY_BUILDER_VERSION = "builder.version";
	public static String PROPERTY_BUILDER_OPTIONS = "builder.options";
	public static String PROPERTY_BUILDER_REMOTE = "builder.hostlogin";
	public static String PROPERTY_NEXUS_RESOURCE = "nexus.resource";
	public static String PROPERTY_NEXUS_REPO = "nexus.repo";
	public static String PROPERTY_NEXUS_REPO_THIRDPARTY = "nexus.thirdparty";
	public static String PROPERTY_MAVEN_VERSION = "maven.version";
	public static String PROPERTY_MAVEN_CFGFILE = "maven.conf";
	public static String PROPERTY_MAVEN_JAVA_VERSION = "maven.java";

	// build source code
	public static String PROPERTY_BRANCHNAME = "source.branch";
	public static String PROPERTY_SOURCE_CHARSET = "release.charset";
	public static String PROPERTY_SOURCE_REPOSITORY = "release.repo";
	public static String PROPERTY_SOURCE_RELEASEROOTDIR = "release.root";
	public static String PROPERTY_RELEASE_GROUPFOLDER = "release.group";
	public static String PROPERTY_SOURCE_CFG_ROOTDIR = "config.root";
	public static String PROPERTY_SOURCE_CFG_LIVEROOTDIR = "config.live";
	public static String PROPERTY_SOURCE_SQL_POSTREFRESH = "conig.postrefresh";
	
	public Charset charset;
	
	public MetaProductBuildSettings( String name , Meta meta , MetaProductSettings product ) {
		super( name );
		
		this.name = name;
		this.meta = meta;
		this.product = product;
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	public void create( ActionBase action , PropertySet src , PropertySet parent ) throws Exception {
		if( !initCreateStarted( parent ) )
			return;

		if( src != null )
			properties.copyOriginalPropertiesToRaw( src );
		
		scatterVariables( action );
		
		initFinished();
	}
	
	public MetaProductBuildSettings copy( ActionBase action , Meta meta , MetaProductSettings product , PropertySet parent ) throws Exception {
		MetaProductBuildSettings r = new MetaProductBuildSettings( name , meta , product );
		r.initCopyStarted( this , parent );
		r.scatterVariables( action );
		r.initFinished();
		
		return( r );
	}
	
	private void scatterVariables( ActionBase action ) throws Exception {
		CONFIG_RELEASE_LASTMAJOR = super.getStringProperty( action , PROPERTY_RELEASE_LASTMAJOR );
		CONFIG_RELEASE_NEXTMAJOR = super.getStringProperty( action , PROPERTY_RELEASE_NEXTMAJOR );
		CONFIG_RELEASE_LASTMINOR = super.getStringProperty( action , PROPERTY_RELEASE_LASTMINOR );
		CONFIG_RELEASE_NEXTMINOR = super.getStringProperty( action , PROPERTY_RELEASE_NEXTMINOR );
		CONFIG_RELEASE_VERSION = super.getStringProperty( action , PROPERTY_RELEASE_VERSION );
		CONFIG_APPVERSION = super.getStringProperty( action , PROPERTY_APPVERSION );
		CONFIG_LOGPATH = super.getPathProperty( action , PROPERTY_LOGPATH );
		
		if( CONFIG_SOURCE_CHARSET != null ) {
			charset = Charset.availableCharsets().get( CONFIG_SOURCE_CHARSET );
			if( charset == null )
				action.exit1( _Error.UnknownDatabaseFilesCharset1 , "unknown database files charset=" + CONFIG_SOURCE_CHARSET , CONFIG_SOURCE_CHARSET );
		}
		
		CONFIG_NEXUS_RESOURCE = super.getStringProperty( action , PROPERTY_NEXUS_RESOURCE );
		CONFIG_NEXUS_REPO = super.getStringProperty( action , PROPERTY_NEXUS_REPO );
		CONFIG_NEXUS_REPO_THIRDPARTY = super.getStringProperty( action , PROPERTY_NEXUS_REPO_THIRDPARTY );
		CONFIG_BUILDER_TYPE = super.getStringProperty( action , PROPERTY_BUILDER_TYPE );
		CONFIG_BUILDER_VERSION = super.getStringProperty( action , PROPERTY_BUILDER_VERSION );
		CONFIG_BUILDER_OPTIONS = super.getStringProperty( action , PROPERTY_BUILDER_OPTIONS );
		CONFIG_BUILDER_REMOTE = super.getStringProperty( action , PROPERTY_BUILDER_REMOTE );
		CONFIG_MAVEN_VERSION = super.getStringProperty( action , PROPERTY_MAVEN_VERSION );
		CONFIG_MAVEN_CFGFILE = super.getStringProperty( action , PROPERTY_MAVEN_CFGFILE );
		CONFIG_MAVEN_JAVA_VERSION = super.getStringProperty( action , PROPERTY_MAVEN_JAVA_VERSION );
		CONFIG_ARTEFACTDIR = super.getStringProperty( action , PROPERTY_ARTEFACTDIR );

		CONFIG_BRANCHNAME = super.getStringProperty( action , PROPERTY_BRANCHNAME );
		CONFIG_SOURCE_CHARSET = super.getStringProperty( action , PROPERTY_SOURCE_CHARSET );
		CONFIG_SOURCE_RELEASEROOTDIR = super.getStringProperty( action , PROPERTY_SOURCE_RELEASEROOTDIR );
		CONFIG_RELEASE_GROUPFOLDER = super.getStringProperty( action , PROPERTY_RELEASE_GROUPFOLDER );
		CONFIG_SOURCE_CFG_ROOTDIR = super.getStringProperty( action , PROPERTY_SOURCE_CFG_ROOTDIR );
		CONFIG_SOURCE_CFG_LIVEROOTDIR = super.getStringProperty( action , PROPERTY_SOURCE_CFG_LIVEROOTDIR );
		CONFIG_SOURCE_SQL_POSTREFRESH = super.getStringProperty( action , PROPERTY_SOURCE_SQL_POSTREFRESH );
	}
	
	public void load( ActionBase action , Node root , PropertySet parent ) throws Exception {
		if( !initCreateStarted( parent ) )
			return;

		properties = new PropertySet( name , parent );
		properties.loadRawFromNodeElements( root );
		scatterVariables( action );
		
		initFinished();
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;

		properties.saveAsElements( doc , root );
	}
	
}
