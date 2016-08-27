package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.server.ServerProductContext;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarBUILDMODE;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductSettings {

	protected Meta meta;
	private boolean loaded;
	public boolean loadFailed;

	public PropertySet execprops;
	public PropertySet props;
	public MetaProductBuildSettings buildCommon;
	public Map<String,MetaProductBuildSettings> buildModes;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	public int CONFIG_LASTPRODTAG;
	public int CONFIG_NEXTPRODTAG;
	public int CONFIG_VERSION_BRANCH_MAJOR;
	public int CONFIG_VERSION_BRANCH_MINOR;
	public int CONFIG_VERSION_BRANCH_NEXTMAJOR;
	public int CONFIG_VERSION_BRANCH_NEXTMINOR;

	public String CONFIG_REDISTPATH;
	public String CONFIG_WORKPATH;
	public String CONFIG_DISTR_PATH;
	public String CONFIG_DISTR_HOSTLOGIN;
	public String CONFIG_UPGRADE_PATH;
	public String CONFIG_BASE_PATH;
	public String CONFIG_MIRRORPATH;
	public String CONFIG_BUILDBASE_PATH;
	public String CONFIG_WINBUILD_HOSTLOGIN;
	public String CONFIG_ADM_TRACKER;
	public String CONFIG_URM_VCS;
	
	public String CONFIG_CUSTOM_BUILD;
	public String CONFIG_CUSTOM_DEPLOY;
	public String CONFIG_CUSTOM_DATABASE;

	public boolean initial;
	
	public static String[] modes = { "devtrunk" , "trunk" , "majorbranch" , "devbranch" , "branch" };

	// context
	public static String PROPERTY_PRODUCT_NAME = "product.name";
	public static String PROPERTY_PRODUCT_HOME = "product.home";
	public static String PROPERTY_LASTPRODTAG = MetaProductVersion.PROPERTY_PROD_LASTTAG;
	public static String PROPERTY_NEXTPRODTAG = MetaProductVersion.PROPERTY_PROD_NEXTTAG;
	public static String PROPERTY_VERSION_BRANCH_MAJOR = MetaProductVersion.PROPERTY_MAJOR_FIRST;
	public static String PROPERTY_VERSION_BRANCH_MINOR = MetaProductVersion.PROPERTY_MAJOR_LAST;
	public static String PROPERTY_VERSION_BRANCH_NEXTMAJOR = MetaProductVersion.PROPERTY_NEXT_MAJOR_FIRST;
	public static String PROPERTY_VERSION_BRANCH_NEXTMINOR = MetaProductVersion.PROPERTY_NEXT_MAJOR_LAST;
	
	public static String PROPERTY_WORKPATH = "work.path";
	public static String PROPERTY_REDIST_PATH = "redist.path";
	public static String PROPERTY_DISTR_PATH  = "distr.path";
	public static String PROPERTY_DISTR_HOSTLOGIN = "distr.hostport";
	public static String PROPERTY_UPGRADE_PATH = "upgrade.path";
	public static String PROPERTY_BASE_PATH = "base.path";
	public static String PROPERTY_MIRRORPATH = "mirror.path";
	public static String PROPERTY_BUILDBASE_PATH = "buildbase.path";
	public static String PROPERTY_WINBUILD_HOSTLOGIN = "winbuild.hostlogin";
	public static String PROPERTY_ADM_TRACKER = "adm.tracker";

	public static String PROPERTY_CUSTOM_BUILD = "custom.build";
	public static String PROPERTY_CUSTOM_DEPLOY = "custom.deploy";
	public static String PROPERTY_CUSTOM_DATABASE = "custom.database";
	
	public MetaProductSettings( Meta meta , PropertySet execprops ) {
		this.meta = meta;
		loaded = false;
		loadFailed = false;
		
		buildModes = new HashMap<String,MetaProductBuildSettings>();
	}

	public MetaProductSettings copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings r = new MetaProductSettings( meta , execprops );
		r.loaded = loaded;
		if( props != null )
			r.props = props.copy( execprops );
		
		if( buildCommon != null )
			r.buildCommon = buildCommon.copy( action , meta , r , r.props ); 
		for( String modeKey : buildModes.keySet() ) {
			MetaProductBuildSettings modeSet = buildModes.get( modeKey );
			r.buildModes.put( modeKey , modeSet.copy( action , meta , r , r.buildCommon.props ) );
		}
		r.initial = initial;
		try {
			r.scatterVariables( action );
			r.loadFailed = false;
		}
		catch( Throwable e ) {
			r.loadFailed = true;
		}

		return( r );
	}
	
	public void setLoadFailed() {
		loadFailed = true;
	}
	
	private void scatterVariables( ActionBase action ) throws Exception {
		CONFIG_REDISTPATH = getPathPropertyRequired( action , PROPERTY_REDIST_PATH );
		CONFIG_WORKPATH = getPathPropertyRequired( action , PROPERTY_WORKPATH );
		CONFIG_DISTR_PATH = getPathPropertyRequired( action , PROPERTY_DISTR_PATH );
		CONFIG_DISTR_HOSTLOGIN = getStringProperty( action , PROPERTY_DISTR_HOSTLOGIN );
		CONFIG_UPGRADE_PATH = getPathPropertyRequired( action , PROPERTY_UPGRADE_PATH );
		CONFIG_BASE_PATH = getPathPropertyRequired( action , PROPERTY_BASE_PATH );
		CONFIG_MIRRORPATH = getPathPropertyRequired( action , PROPERTY_MIRRORPATH );
		CONFIG_BUILDBASE_PATH = getPathPropertyRequired( action , PROPERTY_BUILDBASE_PATH );
		CONFIG_WINBUILD_HOSTLOGIN = getPathPropertyRequired( action , PROPERTY_WINBUILD_HOSTLOGIN );
		CONFIG_ADM_TRACKER = getStringProperty( action , PROPERTY_ADM_TRACKER );
		
		CONFIG_CUSTOM_BUILD = getStringProperty( action , PROPERTY_CUSTOM_BUILD );
		CONFIG_CUSTOM_DEPLOY = getStringProperty( action , PROPERTY_CUSTOM_DEPLOY );
		CONFIG_CUSTOM_DATABASE = getStringProperty( action , PROPERTY_CUSTOM_DATABASE );
	}

	public void create( ActionBase action , ServerRegistry registry , ServerProductContext productContext ) throws Exception {
		if( loaded )
			return;

		// create initial
		loaded = true;
		props = new PropertySet( "product" , execprops );
		setContextProperties( action , productContext );
		props.copyOriginalPropertiesToRaw( registry.getDefaultProductProperties() );
		
		// build
		buildCommon = new MetaProductBuildSettings( "build.common" , meta , this );
		buildCommon.create( action , registry.getDefaultProductBuildProperties() , props );
		for( VarBUILDMODE mode : VarBUILDMODE.values() ) {
			MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "build." + Common.getEnumLower( mode ) , meta , this );
			PropertySet set = registry.getDefaultProductBuildProperties( mode );
			buildMode.create( action , set , buildCommon.props );
		}
		
		loadFailed = false;
	}

	public void load( ActionBase action , ServerProductContext productContext , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		props = new PropertySet( "product" , null );
		setContextProperties( action , productContext );
		
		// load from file
		props.loadRawFromNodeElements( root );
		
		// resolve properties
		initial = true;
		scatterVariables( action );
		props.finishRawProperties();

		buildCommon = new MetaProductBuildSettings( "build" , meta , this );
		Node build = ConfReader.xmlGetFirstChild( root , "build" );
		if( build != null ) {
			buildCommon.load( action , build , props );
			Node[] items = ConfReader.xmlGetChildren( build , "mode" );
			if( items != null ) {
				for( Node node : items ) {
					String modeName = ConfReader.getAttrValue( node , "name" );
					if( Common.getIndexOf( modes , modeName ) < 0 )
						continue;
					
					MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "mode" , meta , this );
					buildMode.load( action , node , buildCommon.props );
					buildModes.put( modeName , buildMode );
				}
			}
		}
		
		loadFailed = false;
	}

	public void save( ActionBase action , Element root ) throws Exception {
		if( !loaded )
			return;

		props.saveAsElements( root.getOwnerDocument() , root );
	}

	public void updateProperties( ActionBase action ) throws Exception {
		// get variables
		initial = false;
		scatterVariables( action );
	}
	
	public String getStringProperty( ActionBase action , String name ) throws Exception {
		return( getPropertyType( action , name , "S" , "" ) );
	}
	
	public String getStringProperty( ActionBase action , String name , String defaultValue ) throws Exception {
		String value = getPropertyType( action , name , "S" , defaultValue );
		return( value );
	}
	
	public String getStringPropertyRequired( ActionBase action , String name , String defaultValue ) throws Exception {
		String value = getPropertyType( action , name , "S" , defaultValue );
		if( value.isEmpty() ) {
			action.error( "product property is not set: " + name );
			loadFailed = true;
		}
		
		return( value );
	}

	public String getPathProperty( ActionBase action , String name ) throws Exception {
		return( getPropertyType( action , name , "P" , "" ) );
	}
	
	private String getPathPropertyRequired( ActionBase action , String name ) throws Exception {
		String value = getPropertyType( action , name , "P" , "" );
		if( value.isEmpty() )
			action.exit( "product property is not set: " + name );
		
		return( value );
	}
	
	public String getPathPropertyBuildRequired( ActionBase action , String name ) throws Exception {
		String value = getPropertyType( action , name , "P" , "" );
		if( value.isEmpty() && action.context.buildMode != VarBUILDMODE.UNKNOWN )
			action.exit( "product property is not set: " + name );
		
		return( value );
	}
	
	private String getPropertyType( ActionBase action , String name , String type , String defaultValue ) throws Exception {
		if( initial ) {
			getPropertyInitial( action , name , type , defaultValue );
			for( String mode : modes )
				getPropertyInitial( action , mode + "." + name , type , defaultValue );
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

	private void getPropertyInitial( ActionBase action , String name , String type , String defaultValue ) throws Exception {
		if( type.equals( "S" ) )
			props.getSystemStringProperty( name , defaultValue );
		else
		if( type.equals( "P" ) )
			props.getSystemPathProperty( name , defaultValue , action.session.execrc );
		else
		if( type.equals( "B" ) )
			props.getSystemBooleanProperty( name , Common.getBooleanValue( defaultValue ) );
		else
		if( type.equals( "N" ) )
			props.getSystemIntProperty( name , Integer.parseInt( defaultValue ) );
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
		
		for( String var : props.getRunningProperties() ) {
			String name = ( String )var;
			if( name.startsWith( prefix ) ) {
				String value = props.getFinalProperty( name , action.session.execrc , true , false );
				if( value != null )
					map.put( name.substring( prefix.length() ) , value );
			}
		}
		
		return( map );
	}

	public void setContextProperties( ActionBase action , ServerProductContext productContext ) throws Exception {
		CONFIG_PRODUCT = productContext.CONFIG_PRODUCT;
		CONFIG_PRODUCTHOME = productContext.CONFIG_PRODUCTHOME;
		
		CONFIG_LASTPRODTAG = productContext.CONFIG_LASTPRODTAG;
		CONFIG_NEXTPRODTAG = productContext.CONFIG_NEXTPRODTAG;
		CONFIG_VERSION_BRANCH_MAJOR = productContext.CONFIG_VERSION_BRANCH_MAJOR;
		CONFIG_VERSION_BRANCH_MINOR = productContext.CONFIG_VERSION_BRANCH_MINOR;
		CONFIG_VERSION_BRANCH_NEXTMAJOR = productContext.CONFIG_VERSION_BRANCH_NEXTMAJOR;
		CONFIG_VERSION_BRANCH_NEXTMINOR = productContext.CONFIG_VERSION_BRANCH_NEXTMINOR;
		
		props.setStringProperty( PROPERTY_PRODUCT_NAME , CONFIG_PRODUCT );
		props.setPathProperty( PROPERTY_PRODUCT_HOME , CONFIG_PRODUCTHOME , action.session.execrc );
		
		props.setNumberProperty( MetaProductVersion.PROPERTY_MAJOR_FIRST , CONFIG_VERSION_BRANCH_MAJOR );
		props.setNumberProperty( MetaProductVersion.PROPERTY_MAJOR_LAST , CONFIG_VERSION_BRANCH_MINOR );
		props.setNumberProperty( MetaProductVersion.PROPERTY_NEXT_MAJOR_FIRST , CONFIG_VERSION_BRANCH_NEXTMAJOR );
		props.setNumberProperty( MetaProductVersion.PROPERTY_NEXT_MAJOR_LAST , CONFIG_VERSION_BRANCH_NEXTMINOR );
		props.setNumberProperty( MetaProductVersion.PROPERTY_PROD_LASTTAG , CONFIG_LASTPRODTAG );
		props.setNumberProperty( MetaProductVersion.PROPERTY_PROD_NEXTTAG , CONFIG_NEXTPRODTAG );
		props.recalculateProperties();
	}
	
	public MetaProductBuildSettings getBuildSettings( ActionBase action ) throws Exception {
		if( action.context.buildMode == VarBUILDMODE.UNKNOWN )
			return( buildCommon );
		
		String mode = Common.getEnumLower( action.context.buildMode );
		MetaProductBuildSettings settings = buildModes.get( mode );
		if( settings == null )
			action.exit( "unable to get build settings for mode=" + mode );
		
		return( settings );
	}
    
}
