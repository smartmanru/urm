package org.urm.server.meta;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.PropertyValue;
import org.urm.server.ServerProductContext;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarBUILDMODE;
import org.w3c.dom.Node;

public class MetaProduct {

	public PropertySet props;
	public Map<String,PropertySet> modeProps;
	private boolean loaded;
	protected Meta meta;
	public Charset charset;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	public String CONFIG_LASTPRODTAG;
	public String CONFIG_NEXTPRODTAG;
	public String CONFIG_VERSION_BRANCH_MAJOR;
	public String CONFIG_VERSION_BRANCH_MINOR;
	public String CONFIG_VERSION_BRANCH_NEXTMINOR;

	public String CONFIG_REDISTPATH;
	public String CONFIG_BUILDBASE;
	public String CONFIG_PROD_TAG;

	public String CONFIG_NEXUS_BASE;
	public String CONFIG_NEXUS_REPO;
	public String CONFIG_NEXUS_REPO_THIRDPARTY;
	public String CONFIG_SVNOLD_PATH;
	public String CONFIG_SVNNEW_PATH;
	public String CONFIG_SVNOLD_AUTH;
	public String CONFIG_SVNNEW_AUTH;
	public String CONFIG_GITMIRRORPATH;
	
	public String CONFIG_BUILDER_TYPE;
	public String CONFIG_BUILDER_VERSION;
	public String CONFIG_JAVA_VERSION;
	public String CONFIG_MAVEN_VERSION;
	public String CONFIG_MODULE_BUILD_OPTIONS_CORE;
	public String CONFIG_APPVERSION;
	public String CONFIG_WORKPATH;
	public String CONFIG_ARTEFACTDIR;
	public String CONFIG_NEXT_MAJORRELEASE;
	public String CONFIG_MAVEN_PROFILES;
	public String CONFIG_MAVEN_CFGFILE;
	public String CONFIG_MAVEN_CMD;
	public String CONFIG_MAVEN_ADDITIONAL_OPTIONS;
	public String CONFIG_ADM_TRACKER;
	public String CONFIG_COMMIT_TRACKERLIST;
	public String CONFIG_RELEASEVER;
	public String CONFIG_DISTR_PATH;
	public String CONFIG_DISTR_HOSTLOGIN;
	public String CONFIG_UPGRADE_PATH;
	public String CONFIG_BASE_PATH;
	public String CONFIG_LAST_VERSION_BUILD;
	public String CONFIG_NEXT_VERSION_BUILD;
	public String CONFIG_VERSION_LAST_FULL;
	public String CONFIG_VERSION_NEXT_FULL;
	public String CONFIG_BRANCHNAME;
	public String CONFIG_APPVERSION_TAG;
	public String CONFIG_VERSIONBRANCH;
	
	public String CONFIG_SOURCE_VCS;
	public String CONFIG_SOURCE_REPOSITORY;
	public String CONFIG_SOURCE_RELEASEROOTDIR;
	public String CONFIG_RELEASE_GROUPFOLDER;
	public String CONFIG_SOURCE_CFG_ROOTDIR;
	public String CONFIG_SOURCE_CFG_LIVEROOTDIR;
	public String CONFIG_SOURCE_SQL_POSTREFRESH;
	public String CONFIG_SOURCE_SQL_CHARSET;
	public String CONFIG_SQL_LOGDIR;
	public String CONFIG_WINBUILD_HOSTLOGIN;

	public String CONFIG_CUSTOM_BUILD;
	public String CONFIG_CUSTOM_DEPLOY;
	public String CONFIG_CUSTOM_DATABASE;

	public boolean initial;
	
	public static String[] modes = { "devtrunk" , "trunk" , "majorbranch" , "devbranch" , "branch" };
	
	public MetaProduct( Meta meta ) {
		this.meta = meta;
		loaded = false;
		modeProps = new HashMap<String,PropertySet>();
	}
	
	private void scatterVariables( ActionBase action ) throws Exception {
		CONFIG_REDISTPATH = getPathPropertyRequired( action , "CONFIG_REDISTPATH" );
		CONFIG_BUILDBASE = getPathPropertyBuildRequired( action , "CONFIG_BUILDBASE" );
		CONFIG_NEXUS_BASE = getStringPropertyRequired( action , "CONFIG_NEXUS_BASE" );
		CONFIG_NEXUS_REPO_THIRDPARTY = getStringProperty( action , "CONFIG_NEXUS_REPO_THIRDPARTY" );
		CONFIG_SVNOLD_PATH = getStringPropertyRequired( action , "CONFIG_SVNOLD_PATH" );
		CONFIG_SVNNEW_PATH = getStringPropertyRequired( action , "CONFIG_SVNNEW_PATH" );
		CONFIG_SVNOLD_AUTH = getPathPropertyRequired( action , "CONFIG_SVNOLD_AUTH" );
		CONFIG_SVNNEW_AUTH = getPathPropertyRequired( action , "CONFIG_SVNNEW_AUTH" );
		CONFIG_GITMIRRORPATH = getPathProperty( action , "CONFIG_GITMIRRORPATH" );
		CONFIG_UPGRADE_PATH = getPathPropertyRequired( action , "CONFIG_UPGRADE_PATH" );
		CONFIG_BASE_PATH = getPathPropertyRequired( action , "CONFIG_BASE_PATH" );
		CONFIG_WORKPATH = getPathPropertyRequired( action , "CONFIG_WORKPATH" );
		CONFIG_ARTEFACTDIR = getPathPropertyRequired( action , "CONFIG_ARTEFACTDIR" );
		CONFIG_DISTR_PATH = getPathPropertyRequired( action , "CONFIG_DISTR_PATH" );
		CONFIG_DISTR_HOSTLOGIN = getStringPropertyRequired( action , "CONFIG_DISTR_HOSTLOGIN" );
		
		CONFIG_BUILDER_TYPE = getStringProperty( action , "CONFIG_BUILDER_TYPE" , "maven" );
		CONFIG_BUILDER_VERSION = getStringProperty( action , "CONFIG_BUILDER_VERSION" );
		CONFIG_JAVA_VERSION = getStringProperty( action , "CONFIG_JAVA_VERSION" );
		CONFIG_MAVEN_VERSION = getStringPropertyRequired( action , "CONFIG_MAVEN_VERSION" );
		CONFIG_MODULE_BUILD_OPTIONS_CORE = getStringProperty( action , "CONFIG_MODULE_BUILD_OPTIONS_CORE" );
		CONFIG_MAVEN_PROFILES = getStringPropertyRequired( action , "CONFIG_MAVEN_PROFILES" );
		CONFIG_MAVEN_CMD = getStringProperty( action , "CONFIG_MAVEN_CMD" );
		CONFIG_MAVEN_ADDITIONAL_OPTIONS = getStringProperty( action , "CONFIG_MAVEN_ADDITIONAL_OPTIONS" );
		CONFIG_ADM_TRACKER = getStringPropertyRequired( action , "CONFIG_ADM_TRACKER" );
		CONFIG_COMMIT_TRACKERLIST = getStringPropertyRequired( action , "CONFIG_COMMIT_TRACKERLIST" );
		CONFIG_LAST_VERSION_BUILD = getStringProperty( action , "CONFIG_LAST_VERSION_BUILD" );
		CONFIG_NEXT_VERSION_BUILD = getStringProperty( action , "CONFIG_NEXT_VERSION_BUILD" );
		CONFIG_VERSIONBRANCH = getStringProperty( action , "CONFIG_VERSIONBRANCH" );
		CONFIG_NEXT_MAJORRELEASE = getStringPropertyRequired( action , "CONFIG_NEXT_MAJORRELEASE" );
		CONFIG_VERSION_LAST_FULL = getStringProperty( action , "CONFIG_VERSION_LAST_FULL" );
		CONFIG_VERSION_NEXT_FULL = getStringProperty( action , "CONFIG_VERSION_NEXT_FULL" );

		CONFIG_SOURCE_VCS = getStringPropertyRequired( action , "CONFIG_SOURCE_VCS" );
		CONFIG_SOURCE_REPOSITORY = getStringPropertyRequired( action , "CONFIG_SOURCE_REPOSITORY" );
		CONFIG_SOURCE_RELEASEROOTDIR = getStringPropertyRequired( action , "CONFIG_SOURCE_RELEASEROOTDIR" );
		CONFIG_RELEASE_GROUPFOLDER = getStringPropertyRequired( action , "CONFIG_RELEASE_GROUPFOLDER" );
		CONFIG_SOURCE_CFG_ROOTDIR = getStringPropertyRequired( action , "CONFIG_SOURCE_CFG_ROOTDIR" );
		CONFIG_SOURCE_CFG_LIVEROOTDIR = getStringPropertyRequired( action , "CONFIG_SOURCE_CFG_LIVEROOTDIR" );
		CONFIG_SOURCE_SQL_POSTREFRESH = getStringPropertyRequired( action , "CONFIG_SOURCE_SQL_POSTREFRESH" );
		CONFIG_SOURCE_SQL_CHARSET = getStringPropertyRequired( action , "CONFIG_SOURCE_SQL_CHARSET" );
		CONFIG_SQL_LOGDIR = getPathPropertyRequired( action , "CONFIG_SQL_LOGDIR" );
		CONFIG_WINBUILD_HOSTLOGIN = getStringProperty( action , "CONFIG_WINBUILD_HOSTLOGIN" );

		CONFIG_APPVERSION = getStringProperty( action , "CONFIG_APPVERSION" );
		CONFIG_APPVERSION_TAG = getStringProperty( action , "CONFIG_APPVERSION_TAG" );
		CONFIG_BRANCHNAME = getStringProperty( action , "CONFIG_BRANCHNAME" );
		CONFIG_MAVEN_CFGFILE = getPathProperty( action , "CONFIG_MAVEN_CFGFILE" );
		CONFIG_NEXUS_REPO = getStringProperty( action , "CONFIG_NEXUS_REPO" );
		CONFIG_PROD_TAG = getStringProperty( action , "CONFIG_PROD_TAG" );
		CONFIG_RELEASEVER = getStringProperty( action , "CONFIG_RELEASEVER" );
		
		CONFIG_CUSTOM_BUILD = getStringProperty( action , "CONFIG_CUSTOM_BUILD" );
		CONFIG_CUSTOM_DEPLOY = getStringProperty( action , "CONFIG_CUSTOM_DEPLOY" );
		CONFIG_CUSTOM_DATABASE = getStringProperty( action , "CONFIG_CUSTOM_DATABASE" );
		
		charset = Charset.availableCharsets().get( CONFIG_SOURCE_SQL_CHARSET );
		if( charset == null )
			action.exit( "unknown database files charset=" + CONFIG_SOURCE_SQL_CHARSET );
	}

	public void create( ActionBase action , ServerRegistry registry , ServerProductContext productContext ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		setContextProperties( action , productContext );
		
		// create initial
		props = new PropertySet( "product" , null );
		registry.setProductDefaults( props );
	}
	
	public void load( ActionBase action , ServerProductContext productContext , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		setContextProperties( action , productContext );
		
		// load from file
		props = new PropertySet( "product" , null );
		props.loadRawFromNodeElements( root );
		
		Node[] items = ConfReader.xmlGetChildren( root , "mode" );
		if( items != null ) {
			for( Node node : items ) {
				String modeName = ConfReader.getAttrValue( node , "name" );
				if( Common.getIndexOf( modes , modeName ) < 0 )
					continue;
				
				PropertySet modeSet = new PropertySet( "build." + modeName , props );
				modeSet.loadRawFromNodeElements( node );
				modeSet.resolveRawProperties();
				
				props.copyRunningPropertiesToRunning( modeSet );
				modeProps.put( modeName , modeSet );
			}
		}
		
		// resolve properties
		initial = true;
		scatterVariables( action );
		props.finishRawProperties();
	}

	public void updateProperties( ActionBase action ) throws Exception {
		// get variables
		initial = false;
		scatterVariables( action );
	}
	
	private String getStringProperty( ActionBase action , String name ) throws Exception {
		return( getPropertyType( action , name , "S" ) );
	}
	
	private String getStringProperty( ActionBase action , String name , String defValue ) throws Exception {
		String value = getPropertyType( action , name , "S" );
		if( value == null || value.isEmpty() )
			return( defValue );
		return( value );
	}
	
	private String getStringPropertyRequired( ActionBase action , String name ) throws Exception {
		String value = getPropertyType( action , name , "S" );
		if( value.isEmpty() )
			action.exit( "product property is not set: " + name );
		
		return( value );
	}

	private String getPathProperty( ActionBase action , String name ) throws Exception {
		return( getPropertyType( action , name , "P" ) );
	}
	
	private String getPathPropertyRequired( ActionBase action , String name ) throws Exception {
		String value = getPropertyType( action , name , "P" );
		if( value.isEmpty() )
			action.exit( "product property is not set: " + name );
		
		return( value );
	}
	
	private String getPathPropertyBuildRequired( ActionBase action , String name ) throws Exception {
		String value = getPropertyType( action , name , "P" );
		if( value.isEmpty() && action.context.buildMode != VarBUILDMODE.UNKNOWN )
			action.exit( "product property is not set: " + name );
		
		return( value );
	}
	
	private String getPropertyType( ActionBase action , String name , String type ) throws Exception {
		if( initial ) {
			getPropertyInitial( action , name , type );
			for( String mode : modes )
				getPropertyInitial( action , mode + "." + name , type );
		}
		
		String value;
		if( action.context.buildMode == VarBUILDMODE.UNKNOWN ) {
			value = getPropertyBase( action , name , type );
		}
		else {
			String buildMode = Common.getEnumLower( action.context.buildMode );
			value = getPropertyBase( action , buildMode + "." + name , type );
			if( value == null || value.isEmpty() )
				value = getPropertyBase( action , name , type );
		}
		if( value == null )
			return( "" );
		
		return( value );
	}

	private void getPropertyInitial( ActionBase action , String name , String type ) throws Exception {
		if( type.equals( "S" ) )
			props.getSystemStringProperty( name , null );
		else
		if( type.equals( "P" ) )
			props.getSystemPathProperty( name , null , action.session.execrc );
		else
		if( type.equals( "B" ) )
			props.getSystemBooleanProperty( name , false );
		else
		if( type.equals( "N" ) )
			props.getSystemIntProperty( name , 0 );
		else
			action.exitUnexpectedState();
	}
	
	private String getPropertyBase( ActionBase action , String name , String type ) throws Exception {
		if( type.equals( "S" ) )
			return( props.getStringProperty( name , null ) );
		else
		if( type.equals( "P" ) )
			return( props.getPathProperty( name , null ) );
		else
		if( type.equals( "B" ) ) {
			boolean value = props.getBooleanProperty( name , false );
			return( Common.getBooleanValue( value ) );
		}
		else
		if( type.equals( "N" ) )
			props.getIntProperty( name , 0 );
		else
			action.exitUnexpectedState();
		
		return( null );
	}
	
	public String getPropertyAny( ActionBase action , String name ) throws Exception {
		String value = props.findPropertyAny( name );
		return( value );
	}
	
	public Map<String,String> getExportProperties( ActionBase action ) throws Exception {
		// export all variables
		Map<String,String> map = new HashMap<String,String>();
		String prefix = "export.";
		
		for( String var : props.getOwnProperties() ) {
			String name = ( String )var;
			if( name.startsWith( prefix ) ) {
				PropertyValue pv = props.getFinalProperty( name , action.session.execrc , true , false );
				if( pv != null )
					map.put( name.substring( prefix.length() ) , pv.getData() );
			}
		}
		
		return( map );
	}

	public void setContextProperties( ActionBase action , ServerProductContext productContext ) throws Exception {
		CONFIG_PRODUCT = productContext.CONFIG_PRODUCT;
		CONFIG_PRODUCTHOME = productContext.CONFIG_PRODUCTHOME;
		CONFIG_LASTPRODTAG = productContext.CONFIG_LASTPRODTAG;
		CONFIG_NEXTPRODTAG = productContext.CONFIG_NEXTPRODTAG;
		
		props.setStringProperty( "CONFIG_PRODUCT" , CONFIG_PRODUCT );
		props.setPathProperty( "CONFIG_PRODUCTHOME" , CONFIG_PRODUCTHOME , action.session.execrc );
		props.setStringProperty( "CONFIG_LASTPRODTAG" , CONFIG_LASTPRODTAG );
		props.setStringProperty( "CONFIG_NEXTPRODTAG" , CONFIG_NEXTPRODTAG );
	}
	
}
