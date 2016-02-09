package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.MetadataStorage;

public class MetaProduct {

	public PropertySet props;
	boolean loaded = false;
	
	Metadata meta;
	String lastProdTagFile;

	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	public String CONFIG_REDISTPATH;
	public String CONFIG_BUILDBASE;
	
	public String CONFIG_LASTPRODTAG;
	public String CONFIG_NEXTPRODTAG;
	public String CONFIG_PROD_TAG;

	public String CONFIG_NEXUS_BASE;
	public String CONFIG_NEXUS_REPO;
	public String CONFIG_NEXUS_REPO_THIRDPARTY;
	public String CONFIG_SVNOLD_PATH;
	public String CONFIG_SVNNEW_PATH;
	public String CONFIG_SVNOLD_AUTH;
	public String CONFIG_SVNNEW_AUTH;
	public String CONFIG_GITMIRRORPATH;
	public String CONFIG_GITMIRRORPATHWIN;
	
	public String CONFIG_BUILDER_TYPE;
	public String CONFIG_BUILDER_VERSION;
	public String CONFIG_JAVA_VERSION;
	public String CONFIG_MAVEN_VERSION;
	public String CONFIG_MODULE_BUILD_OPTIONS_CORE;
	public String CONFIG_APPVERSION;
	public String CONFIG_BUILDPATH;
	public String CONFIG_ARTEFACTDIR;
	public String CONFIG_NEXT_MAJORRELEASE;
	public String CONFIG_MAVEN_PROFILES;
	public String CONFIG_MAVEN_CFGFILE;
	public String CONFIG_MAVEN_CMD;
	public String CONFIG_MAVEN_ADDITIONAL_OPTIONS;
	public String CONFIG_ADM_TRACKER;
	public String CONFIG_RELEASEVER;
	public String CONFIG_DISTR_PATH;
	public String CONFIG_DISTR_HOSTLOGIN;
	public String CONFIG_UPGRADE_PATH;
	public String CONFIG_VERSION_LAST_FULL;
	public String CONFIG_VERSION_NEXT_FULL;
	public String CONFIG_BRANCHNAME;
	
	public String CONFIG_SOURCE_VCS;
	public String CONFIG_SOURCE_REPOSITORY;
	public String CONFIG_SOURCE_RELEASEROOTDIR;
	public String CONFIG_RELEASE_GROUPFOLDER;
	public String CONFIG_SOURCE_CFG_ROOTDIR;
	public String CONFIG_SOURCE_CFG_LIVEROOTDIR;
	public String CONFIG_SOURCE_SQL_POSTREFRESH;
	public String CONFIG_SQL_LOGDIR;
	public String CONFIG_WINBUILD_HOSTLOGIN;
	
	Map<String,String> refMap; 
	
	private void scatterVariables( ActionBase action ) throws Exception {
		CONFIG_PRODUCT = getPropertyRequired( action , "CONFIG_PRODUCT" );
		CONFIG_PRODUCTHOME = getPropertyRequired( action , "CONFIG_PRODUCTHOME" );
		CONFIG_REDISTPATH = getPropertyRequired( action , "CONFIG_REDISTPATH" );
		CONFIG_BUILDBASE = getPropertyBuildRequired( action , "CONFIG_BUILDBASE" );
		
		CONFIG_LASTPRODTAG = getPropertyRequired( action , "CONFIG_LASTPRODTAG" );
		CONFIG_NEXTPRODTAG = getPropertyRequired( action , "CONFIG_NEXTPRODTAG" );
		CONFIG_PROD_TAG = getProperty( action , "CONFIG_PROD_TAG" );

		CONFIG_NEXUS_BASE = getPropertyRequired( action , "CONFIG_NEXUS_BASE" );
		CONFIG_NEXUS_REPO = getProperty( action , "CONFIG_NEXUS_REPO" );
		CONFIG_NEXUS_REPO_THIRDPARTY = getProperty( action , "CONFIG_NEXUS_REPO_THIRDPARTY" );
		CONFIG_SVNOLD_PATH = getPropertyRequired( action , "CONFIG_SVNOLD_PATH" );
		CONFIG_SVNNEW_PATH = getPropertyRequired( action , "CONFIG_SVNNEW_PATH" );
		CONFIG_SVNOLD_AUTH = getPropertyRequired( action , "CONFIG_SVNOLD_AUTH" );
		CONFIG_SVNNEW_AUTH = getPropertyRequired( action , "CONFIG_SVNNEW_AUTH" );
		CONFIG_GITMIRRORPATH = getProperty( action , "CONFIG_GITMIRRORPATH" );
		CONFIG_GITMIRRORPATHWIN = getProperty( action , "CONFIG_GITMIRRORPATHWIN" );
		
		CONFIG_BUILDER_TYPE = getProperty( action , "CONFIG_BUILDER_TYPE" , "maven" );
		CONFIG_BUILDER_VERSION = getProperty( action , "CONFIG_BUILDER_VERSION" );
		CONFIG_JAVA_VERSION = getProperty( action , "CONFIG_JAVA_VERSION" );
		CONFIG_MAVEN_VERSION = getPropertyRequired( action , "CONFIG_MAVEN_VERSION" );
		CONFIG_MODULE_BUILD_OPTIONS_CORE = getProperty( action , "CONFIG_MODULE_BUILD_OPTIONS_CORE" );
		CONFIG_APPVERSION = getProperty( action , "CONFIG_APPVERSION" );
		CONFIG_BUILDPATH = getPropertyRequired( action , "CONFIG_BUILDPATH" );
		CONFIG_ARTEFACTDIR = getPropertyRequired( action , "CONFIG_ARTEFACTDIR" );
		CONFIG_NEXT_MAJORRELEASE = getPropertyRequired( action , "CONFIG_NEXT_MAJORRELEASE" );
		CONFIG_MAVEN_PROFILES = getPropertyRequired( action , "CONFIG_MAVEN_PROFILES" );
		CONFIG_MAVEN_CFGFILE = getProperty( action , "CONFIG_MAVEN_CFGFILE" );
		CONFIG_MAVEN_CMD = getProperty( action , "CONFIG_MAVEN_CMD" );
		CONFIG_MAVEN_ADDITIONAL_OPTIONS = getProperty( action , "CONFIG_MAVEN_ADDITIONAL_OPTIONS" );
		CONFIG_ADM_TRACKER = getPropertyRequired( action , "CONFIG_ADM_TRACKER" );
		CONFIG_RELEASEVER = getProperty( action , "CONFIG_RELEASEVER" );
		CONFIG_DISTR_PATH = getPropertyRequired( action , "CONFIG_DISTR_PATH" );
		CONFIG_DISTR_HOSTLOGIN = getPropertyRequired( action , "CONFIG_DISTR_HOSTLOGIN" );
		CONFIG_UPGRADE_PATH = getPropertyRequired( action , "CONFIG_UPGRADE_PATH" );
		CONFIG_VERSION_LAST_FULL = getProperty( action , "CONFIG_VERSION_LAST_FULL" );
		CONFIG_VERSION_NEXT_FULL = getProperty( action , "CONFIG_VERSION_NEXT_FULL" );
		CONFIG_BRANCHNAME = getProperty( action , "CONFIG_BRANCHNAME" );

		CONFIG_SOURCE_VCS = getPropertyRequired( action , "CONFIG_SOURCE_VCS" );
		CONFIG_SOURCE_REPOSITORY = getPropertyRequired( action , "CONFIG_SOURCE_REPOSITORY" );
		CONFIG_SOURCE_RELEASEROOTDIR = getPropertyRequired( action , "CONFIG_SOURCE_RELEASEROOTDIR" );
		CONFIG_RELEASE_GROUPFOLDER = getPropertyRequired( action , "CONFIG_RELEASE_GROUPFOLDER" );
		CONFIG_SOURCE_CFG_ROOTDIR = getPropertyRequired( action , "CONFIG_SOURCE_CFG_ROOTDIR" );
		CONFIG_SOURCE_CFG_LIVEROOTDIR = getPropertyRequired( action , "CONFIG_SOURCE_CFG_LIVEROOTDIR" );
		CONFIG_SOURCE_SQL_POSTREFRESH = getPropertyRequired( action , "CONFIG_SOURCE_SQL_POSTREFRESH" );
		CONFIG_SQL_LOGDIR = getPropertyRequired( action , "CONFIG_SQL_LOGDIR" );
		CONFIG_WINBUILD_HOSTLOGIN = getProperty( action , "CONFIG_WINBUILD_HOSTLOGIN" );
	}

	public MetaProduct( Metadata meta ) {
		this.meta = meta;
		refMap = new HashMap<String,String>();
		props = new PropertySet( "product" , null );
	}
	
	public void load( ActionBase action , MetadataStorage storage ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		lastProdTagFile = storage.getLastProdTagFile( action );
		String file = storage.getProductConfFile( action );
		
		// add predefined properties
		addPredefined( action );

		// add from file
		props.loadFromFile( action , file );
		
		// resolve properties
		updateProperties( action );
	}

	public void updateProperties( ActionBase action ) throws Exception {
		refMap.clear();
		
		// resolve properties
		resolveProperties( action );
		
		// get variables
		scatterVariables( action );
	}
	
	private void addPredefined( ActionBase action ) throws Exception {
		// get last prod tag
		String lastProdTag = ConfReader.readStringFile( action , lastProdTagFile );
		int nextProdTag = Integer.parseInt( lastProdTag ) + 1; 
		
		props.setProperty( "CONFIG_PRODUCTHOME" , action.context.productHome );
		props.setProperty( "CONFIG_LASTPRODTAG" , lastProdTag );
		props.setProperty( "CONFIG_NEXTPRODTAG" , "" + nextProdTag );
	}
	
	private void resolveProperties( ActionBase action ) throws Exception {
		// substitute variables
		String[] names = props.getOwnProperties( action );
		for( int k = 0; k < 100; k++ ) {
			int varCount = 0;
			for( Object item : names ) {
				String name = ( String )item;
				String setname = name.substring( props.set.length() + 1 );
				
				String val = props.getProperty( action , name );
				if( val.startsWith( "~/") )
					val = ConfReader.userHome + val.substring( 1 );
				
				// trim quotes
				val = val.trim();
				if( val.startsWith( "\"" ) && val.endsWith( "\"" ) )
					val = val.substring( 1 , val.length() - 1 );
				props.setProperty( setname , val );
				
				int pos = val.indexOf( '@' );
				if( pos >= 0 ) {
					int end = val.indexOf( '@' , pos + 1 );
					if( end >= 0 ) {
						varCount++;
						String var = val.substring( pos + 1 , end );
						String value = props.getProperty( action , var );
						String vb = ( pos > 0 )? val.substring( 0 , pos ) : ""; 
						String va = val.substring( end + 1 );
						val = vb + value + va;
						props.setProperty( setname , val );
					}
				}
			}
			
			if( varCount == 0 )
				return;
		}
		
		// ensure CONFIG_* properties are from list
		String[] modes = { "devtrunk" , "trunk" , "majorbranch" , "devbranch" , "branch" };
		for( Object item : names ) {
			String name = ( String )item;
			if( !checkCorrectName( action , name , modes ) )
				action.exit( "unexpected product configuration property: " + name );
		}
	}

	public boolean checkCorrectName( ActionBase action , String name , String[] modes ) {
		if( refMap.containsKey( name ) )
			return( true );
		
		for( String mode : modes ) {
			if( name.startsWith( mode + "." ) ) {
				name = name.substring( mode.length() + 1 );
				if( refMap.containsKey( name ) )
					return( true );
				
				if( !name.startsWith( "CONFIG_" ) )
					return( true );
				
				return( false );
			}
		}
		
		if( !name.startsWith( "CONFIG_" ) )
			return( true );
		
		return( false );
	}
	
	public Map<String,String> getExportProperties( ActionBase action ) throws Exception {
		// export all variables
		Map<String,String> map = new HashMap<String,String>();
		String prefix = "export.";
		for( Object var : props.keySet() ) {
			String name = ( String )var;
			if( name.startsWith( prefix ) )
				map.put( name.substring( prefix.length() ) , props.getProperty( action , name ) ); 
		}
		
		return( map );
	}
	
	public Map<String,String> getProductProperties() {
		return( refMap );
	}

	public String getPropertyBase( ActionBase action , String name ) throws Exception {
		String value = props.findProperty( action , name );
		return( value );
	}
	
	private String getProperty( ActionBase action , String name ) throws Exception {
		String value;
		if( action.context.buildMode == VarBUILDMODE.UNKNOWN ) {
			value = getPropertyBase( action , name );
		}
		else {
			String buildMode = Common.getEnumLower( action.context.buildMode );
			value = getPropertyBase( action , buildMode + "." + name );
			if( value == null || value.isEmpty() )
				value = getPropertyBase( action , name );
		}
		if( value == null )
			return( "" );
		
		refMap.put( name , value );
		return( value );
	}
	
	private String getPropertyBuildRequired( ActionBase action , String name ) throws Exception {
		String value = getProperty( action , name );
		if( value.isEmpty() && action.context.buildMode != VarBUILDMODE.UNKNOWN )
			action.exit( "product property is not set: " + name );
		
		return( value );
	}
	
	private String getPropertyRequired( ActionBase action , String name ) throws Exception {
		String value = getProperty( action , name );
		if( value.isEmpty() )
			action.exit( "product property is not set: " + name );
		
		return( value );
	}
	
	private String getProperty( ActionBase action , String name , String defaultValue ) throws Exception {
		String s = getProperty( action , name );
		if( s == null || s.isEmpty() )
			s = defaultValue;
		return( s );
	}

}
