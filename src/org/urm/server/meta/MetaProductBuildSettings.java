package org.urm.server.meta;

import java.nio.charset.Charset;

import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaProductBuildSettings extends PropertyController {

	public String name;
	public Meta meta;
	public MetaProductSettings product;
	public PropertySet props;
	
	public String CONFIG_RELEASE_LASTMAJOR;
	public String CONFIG_RELEASE_NEXTMAJOR;
	public String CONFIG_RELEASE_LASTMINOR;
	public String CONFIG_RELEASE_NEXTMINOR;
	public String CONFIG_RELEASE_VERSION;
	public String CONFIG_APPVERSION;
	
	public String CONFIG_NEXUS_RESOURCE;
	public String CONFIG_NEXUS_REPO;
	public String CONFIG_NEXUS_REPO_THIRDPARTY;
	public String CONFIG_BUILDER_TYPE;
	public String CONFIG_BUILDER_VERSION;
	public String CONFIG_BUILDER_OPTIONS;
	public String CONFIG_MAVEN_VERSION;
	public String CONFIG_MAVEN_PROFILES;
	public String CONFIG_MAVEN_CFGFILE;
	public String CONFIG_MAVEN_CMD;
	public String CONFIG_MAVEN_ADDITIONAL_OPTIONS;
	public String CONFIG_JAVA_VERSION;
	public String CONFIG_BRANCHNAME;
	public String CONFIG_ARTEFACTDIR;

	public String CONFIG_COMMIT_TRACKERLIST;
	public String CONFIG_SOURCE_RESOURCE;
	public String CONFIG_SOURCE_CHARSET;
	public String CONFIG_SOURCE_REPOSITORY;
	public String CONFIG_SOURCE_RELEASEROOTDIR;
	public String CONFIG_RELEASE_GROUPFOLDER;
	public String CONFIG_SOURCE_CFG_ROOTDIR;
	public String CONFIG_SOURCE_CFG_LIVEROOTDIR;
	public String CONFIG_SOURCE_SQL_POSTREFRESH;
	public String CONFIG_SQL_LOGDIR;

	// version
	public static String PROPERTY_RELEASE_LASTMAJOR = "release.lastmajor";
	public static String PROPERTY_RELEASE_NEXTMAJOR = "release.nextmajor";
	public static String PROPERTY_RELEASE_LASTMINOR = "release.lastminor";
	public static String PROPERTY_RELEASE_NEXTMINOR = "release.lastminor";
	public static String PROPERTY_RELEASE_VERSION = "release.version";
	public static String PROPERTY_APPVERSION = "app.version";

	public Charset charset;
	
	public MetaProductBuildSettings( String name , Meta meta , MetaProductSettings product ) {
		this.name = name;
		this.meta = meta;
		this.product = product;
	}
	
	public void create( ActionBase action , PropertySet src , PropertySet parent ) throws Exception {
		if( !loadStarted() )
			return;

		props = new PropertySet( name , parent );
		props.copyOriginalPropertiesToRaw( src );
		scatterVariables( action );
		
		loadFinished();
	}
	
	public MetaProductBuildSettings copy( ActionBase action , Meta meta , MetaProductSettings product , PropertySet parent ) throws Exception {
		MetaProductBuildSettings r = new MetaProductBuildSettings( name , meta , product );
		r.loadStarted();
		r.props = props.copy( parent );
		r.scatterVariables( action );
		r.loadFinished();
		return( r );
	}
	
	private void scatterVariables( ActionBase action ) throws Exception {
		CONFIG_RELEASE_LASTMAJOR = getStringPropertyRequired( action , props , PROPERTY_RELEASE_LASTMAJOR , getVarExpr( MetaProductSettings.PROPERTY_VERSION_BRANCH_MAJOR ) + "." + getVarExpr( MetaProductSettings.PROPERTY_VERSION_BRANCH_MINOR ) );
		CONFIG_RELEASE_NEXTMAJOR = getStringPropertyRequired( action , props , PROPERTY_RELEASE_NEXTMAJOR , getVarExpr( MetaProductSettings.PROPERTY_VERSION_BRANCH_NEXTMAJOR ) + "." + getVarExpr( MetaProductSettings.PROPERTY_VERSION_BRANCH_NEXTMINOR ) );
		CONFIG_RELEASE_LASTMINOR = getStringProperty( action , props , PROPERTY_RELEASE_LASTMINOR );
		CONFIG_RELEASE_NEXTMINOR = getStringProperty( action , props , PROPERTY_RELEASE_NEXTMINOR );
		CONFIG_RELEASE_VERSION = getStringProperty( action , props , PROPERTY_RELEASE_VERSION );
		CONFIG_APPVERSION = getStringProperty( action , props , PROPERTY_APPVERSION );
		
		charset = Charset.availableCharsets().get( CONFIG_SOURCE_CHARSET );
		if( charset == null )
			action.exit( "unknown database files charset=" + CONFIG_SOURCE_CHARSET );
	}
	
	public void load( ActionBase action , Node root , PropertySet parent ) throws Exception {
		if( !loadStarted() )
			return;

		props = new PropertySet( name , parent );
		props.loadRawFromNodeElements( root );
		props.finishRawProperties();
		
		loadFinished();
	}
	
}
