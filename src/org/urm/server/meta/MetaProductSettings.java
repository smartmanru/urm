package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.server.ServerProductContext;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarBUILDMODE;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductSettings extends PropertyController {

	protected Meta meta;

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
		buildModes = new HashMap<String,MetaProductBuildSettings>();
	}

	public MetaProductSettings copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings r = new MetaProductSettings( meta , execprops );
		r.loadStarted();
		
		if( props != null )
			r.props = props.copy( execprops );
		
		if( buildCommon != null )
			r.buildCommon = buildCommon.copy( action , meta , r , r.props ); 
		for( String modeKey : buildModes.keySet() ) {
			MetaProductBuildSettings modeSet = buildModes.get( modeKey );
			r.buildModes.put( modeKey , modeSet.copy( action , meta , r , r.buildCommon.props ) );
		}

		r.scatterVariables( action );
		r.loadFinished();

		return( r );
	}
	
	private void scatterVariables( ActionBase action ) throws Exception {
		CONFIG_REDISTPATH = super.getPathPropertyRequired( action , props , PROPERTY_REDIST_PATH );
		CONFIG_WORKPATH = getPathPropertyRequired( action , props , PROPERTY_WORKPATH );
		CONFIG_DISTR_PATH = getPathPropertyRequired( action , props , PROPERTY_DISTR_PATH );
		CONFIG_DISTR_HOSTLOGIN = getStringProperty( action , props , PROPERTY_DISTR_HOSTLOGIN );
		CONFIG_UPGRADE_PATH = getPathPropertyRequired( action , props , PROPERTY_UPGRADE_PATH );
		CONFIG_BASE_PATH = getPathPropertyRequired( action , props , PROPERTY_BASE_PATH );
		CONFIG_MIRRORPATH = getPathPropertyRequired( action , props , PROPERTY_MIRRORPATH );
		CONFIG_BUILDBASE_PATH = getPathPropertyRequired( action , props , PROPERTY_BUILDBASE_PATH );
		CONFIG_WINBUILD_HOSTLOGIN = getPathPropertyRequired( action , props , PROPERTY_WINBUILD_HOSTLOGIN );
		CONFIG_ADM_TRACKER = getStringProperty( action , props , PROPERTY_ADM_TRACKER );
		
		CONFIG_CUSTOM_BUILD = getStringProperty( action , props , PROPERTY_CUSTOM_BUILD );
		CONFIG_CUSTOM_DEPLOY = getStringProperty( action , props , PROPERTY_CUSTOM_DEPLOY );
		CONFIG_CUSTOM_DATABASE = getStringProperty( action , props , PROPERTY_CUSTOM_DATABASE );
	}

	public void create( ActionBase action , ServerRegistry registry , ServerProductContext productContext ) throws Exception {
		if( !loadStarted() )
			return;

		// create initial
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
		
		loadFinished();
	}

	public void load( ActionBase action , ServerProductContext productContext , Node root ) throws Exception {
		if( !loadStarted() )
			return;

		props = new PropertySet( "product" , null );
		setContextProperties( action , productContext );
		
		// load from file
		props.loadRawFromNodeElements( root );
		
		// resolve properties
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
		
		loadFinished();
	}

	public void save( ActionBase action , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;

		props.saveAsElements( root.getOwnerDocument() , root );
	}

	public void updateProperties( ActionBase action ) throws Exception {
		// get variables
		scatterVariables( action );
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
    
	public String getPropertyAny( ActionBase action , String name ) throws Exception {
		return( props.getPropertyAny( name ) );
	}
	
}
