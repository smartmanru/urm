package org.urm.engine.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.engine.ServerProductContext;
import org.urm.engine.ServerSettings;
import org.urm.engine.ServerTransaction;
import org.urm.engine.meta.Meta.VarBUILDMODE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductSettings extends PropertyController {

	protected Meta meta;

	public PropertySet execprops;
	public MetaProductBuildSettings buildCommon;
	public Map<VarBUILDMODE,MetaProductBuildSettings> buildModes;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	public int CONFIG_LASTPRODTAG;
	public int CONFIG_NEXTPRODTAG;
	public int CONFIG_VERSION_BRANCH_MAJOR;
	public int CONFIG_VERSION_BRANCH_MINOR;
	public int CONFIG_VERSION_BRANCH_NEXTMAJOR;
	public int CONFIG_VERSION_BRANCH_NEXTMINOR;

	public String CONFIG_REDISTWIN_PATH;
	public String CONFIG_REDISTLINUX_PATH;
	public String CONFIG_WORKPATH;
	public String CONFIG_DISTR_PATH;
	public String CONFIG_DISTR_HOSTLOGIN;
	public String CONFIG_UPGRADE_PATH;
	public String CONFIG_BASE_PATH;
	public String CONFIG_MIRRORPATH;
	public String CONFIG_BUILDBASE_PATH;
	public String CONFIG_WINBUILD_HOSTLOGIN;
	public String CONFIG_ADM_TRACKER;
	public String CONFIG_COMMIT_TRACKERLIST;
	public String CONFIG_META_MIRROR;
	public String CONFIG_SOURCE_MIRROR;
	
	public String CONFIG_CUSTOM_BUILD;
	public String CONFIG_CUSTOM_DEPLOY;
	public String CONFIG_CUSTOM_DATABASE;

	// context
	public static String PROPERTY_PRODUCT_NAME = "product";
	public static String PROPERTY_PRODUCT_HOME = "product.home";
	public static String PROPERTY_LASTPRODTAG = MetaProductVersion.PROPERTY_PROD_LASTTAG;
	public static String PROPERTY_NEXTPRODTAG = MetaProductVersion.PROPERTY_PROD_NEXTTAG;
	public static String PROPERTY_VERSION_BRANCH_MAJOR = MetaProductVersion.PROPERTY_MAJOR_FIRST;
	public static String PROPERTY_VERSION_BRANCH_MINOR = MetaProductVersion.PROPERTY_MAJOR_LAST;
	public static String PROPERTY_VERSION_BRANCH_NEXTMAJOR = MetaProductVersion.PROPERTY_NEXT_MAJOR_FIRST;
	public static String PROPERTY_VERSION_BRANCH_NEXTMINOR = MetaProductVersion.PROPERTY_NEXT_MAJOR_LAST;
	
	public static String PROPERTY_WORKPATH = "work.path";
	public static String PROPERTY_REDISTWIN_PATH = "redist.win.path";
	public static String PROPERTY_REDISTLINUX_PATH = "redist.linux.path";
	public static String PROPERTY_DISTR_PATH  = "distr.path";
	public static String PROPERTY_DISTR_HOSTLOGIN = "distr.hostlogin";
	public static String PROPERTY_UPGRADE_PATH = "upgrade.path";
	public static String PROPERTY_BASE_PATH = "base.path";
	public static String PROPERTY_MIRRORPATH = "mirror.path";
	public static String PROPERTY_BUILDBASE_PATH = "buildbase.path";
	public static String PROPERTY_WINBUILD_HOSTLOGIN = "winbuild.hostlogin";
	public static String PROPERTY_ADM_TRACKER = "adm.tracker";
	public static String PROPERTY_COMMIT_TRACKERLIST = "source.trackers";
	public static String PROPERTY_META_MIRROR = "meta.mirror";
	public static String PROPERTY_SOURCE_MIRROR = "conf.mirror";

	public static String PROPERTY_CUSTOM_BUILD = "custom.build";
	public static String PROPERTY_CUSTOM_DEPLOY = "custom.deploy";
	public static String PROPERTY_CUSTOM_DATABASE = "custom.database";
	
	public MetaProductSettings( Meta meta , PropertySet execprops ) {
		super( "product" );
		
		this.meta = meta;
		this.execprops = execprops;
		meta.setProduct( this );
		buildModes = new HashMap<VarBUILDMODE,MetaProductBuildSettings>();
	}

	@Override
	public boolean isValid() {
		if( super.isLoadFailed() || buildCommon == null )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		CONFIG_REDISTWIN_PATH = super.getPathPropertyRequired( action , PROPERTY_REDISTWIN_PATH );
		CONFIG_REDISTLINUX_PATH = super.getPathPropertyRequired( action , PROPERTY_REDISTLINUX_PATH );
		CONFIG_WORKPATH = super.getPathPropertyRequired( action , PROPERTY_WORKPATH );
		CONFIG_DISTR_PATH = super.getPathPropertyRequired( action , PROPERTY_DISTR_PATH );
		CONFIG_DISTR_HOSTLOGIN = super.getStringProperty( action , PROPERTY_DISTR_HOSTLOGIN );
		CONFIG_UPGRADE_PATH = super.getPathPropertyRequired( action , PROPERTY_UPGRADE_PATH );
		CONFIG_BASE_PATH = super.getPathPropertyRequired( action , PROPERTY_BASE_PATH );
		CONFIG_MIRRORPATH = super.getPathPropertyRequired( action , PROPERTY_MIRRORPATH );
		CONFIG_BUILDBASE_PATH = super.getPathPropertyRequired( action , PROPERTY_BUILDBASE_PATH );
		CONFIG_WINBUILD_HOSTLOGIN = super.getPathPropertyRequired( action , PROPERTY_WINBUILD_HOSTLOGIN );
		CONFIG_ADM_TRACKER = super.getStringProperty( action , PROPERTY_ADM_TRACKER );
		CONFIG_COMMIT_TRACKERLIST = super.getStringProperty( action , PROPERTY_COMMIT_TRACKERLIST );
		CONFIG_META_MIRROR = super.getStringProperty( action , PROPERTY_META_MIRROR );
		CONFIG_SOURCE_MIRROR = super.getStringProperty( action , PROPERTY_SOURCE_MIRROR );
		
		CONFIG_CUSTOM_BUILD = super.getStringProperty( action , PROPERTY_CUSTOM_BUILD );
		CONFIG_CUSTOM_DEPLOY = super.getStringProperty( action , PROPERTY_CUSTOM_DEPLOY );
		CONFIG_CUSTOM_DATABASE = super.getStringProperty( action , PROPERTY_CUSTOM_DATABASE );
	}

	@Override
	public void gatherProperties( ActionBase action ) throws Exception {
	}
	
	public MetaProductSettings copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings r = new MetaProductSettings( meta , execprops );
		r.initCopyStarted( this , execprops );
		
		r.CONFIG_PRODUCT = CONFIG_PRODUCT;
		r.CONFIG_PRODUCTHOME = CONFIG_PRODUCTHOME;
		
		r.CONFIG_LASTPRODTAG = CONFIG_LASTPRODTAG;
		r.CONFIG_NEXTPRODTAG = CONFIG_NEXTPRODTAG;
		r.CONFIG_VERSION_BRANCH_MAJOR = CONFIG_VERSION_BRANCH_MAJOR;
		r.CONFIG_VERSION_BRANCH_MINOR = CONFIG_VERSION_BRANCH_MINOR;
		r.CONFIG_VERSION_BRANCH_NEXTMAJOR = CONFIG_VERSION_BRANCH_NEXTMAJOR;
		r.CONFIG_VERSION_BRANCH_NEXTMINOR = CONFIG_VERSION_BRANCH_NEXTMINOR;
		
		if( buildCommon != null )
			r.buildCommon = buildCommon.copy( action , meta , r , r.properties ); 
		for( VarBUILDMODE mode : buildModes.keySet() ) {
			MetaProductBuildSettings modeSet = buildModes.get( mode );
			r.buildModes.put( mode , modeSet.copy( action , meta , r , r.buildCommon.getProperties() ) );
		}

		r.updateProperties( action );
		r.initFinished();

		return( r );
	}
	
	public void create( ActionBase action , ServerSettings settings , ServerProductContext productContext ) throws Exception {
		if( !super.initCreateStarted( execprops ) )
			return;

		// create initial
		setContextProperties( action , productContext );
		properties.copyOriginalPropertiesToRaw( settings.getDefaultProductProperties() );
		super.updateProperties( action );
		
		// build
		buildCommon = new MetaProductBuildSettings( "build.common" , meta , this );
		buildCommon.create( action , settings.getDefaultProductBuildProperties() , properties );
		for( VarBUILDMODE mode : VarBUILDMODE.values() ) {
			if( mode == VarBUILDMODE.UNKNOWN )
				continue;
			
			String modeName = Common.getEnumLower( mode );
			MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "build." + modeName , meta , this );
			PropertySet set = settings.getDefaultProductBuildProperties( mode );
			buildMode.create( action , set , buildCommon.getProperties() );
			buildModes.put( mode , buildMode );
		}
		
		super.initFinished();
	}

	public void load( ActionBase action , ServerProductContext productContext , Node root ) throws Exception {
		if( !initCreateStarted( execprops ) )
			return;

		setContextProperties( action , productContext );
		
		properties.loadFromNodeElements( root );
		super.updateProperties( action );

		buildCommon = new MetaProductBuildSettings( "build" , meta , this );
		Node build = ConfReader.xmlGetFirstChild( root , "build" );
		if( build != null ) {
			buildCommon.load( action , build , properties );
			Node[] items = ConfReader.xmlGetChildren( build , "mode" );
			if( items != null ) {
				for( Node node : items ) {
					String modeName = ConfReader.getAttrValue( node , "name" );
					VarBUILDMODE mode = Meta.getBuildMode( modeName );
					
					MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "mode" , meta , this );
					buildMode.load( action , node , buildCommon.getProperties() );
					buildModes.put( mode , buildMode );
				}
			}
		}
		
		initFinished();
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;

		properties.saveAsElements( doc , root );
		
		Element buildElement = Common.xmlCreateElement( doc , root , "build" );
		buildCommon.save( action , doc , buildElement );
		
		for( VarBUILDMODE mode : buildModes.keySet() ) {
			MetaProductBuildSettings buildMode = buildModes.get( mode );
			Element buildModeElement = Common.xmlCreateElement( doc , buildElement , "mode" );
			buildModeElement.setAttribute( "name" , Common.getEnumLower( mode ) );
			buildMode.save( action , doc , buildModeElement );
		}
	}

	public Map<String,String> getExportProperties( ActionBase action ) throws Exception {
		// export all variables
		Map<String,String> map = new HashMap<String,String>();
		String prefix = "export.";
		
		for( String var : properties.getRunningProperties() ) {
			String name = ( String )var;
			if( name.startsWith( prefix ) ) {
				String value = properties.getFinalProperty( name , action.shell.account , true , false );
				if( value != null )
					map.put( name.substring( prefix.length() ) , value );
			}
		}
		
		return( map );
	}

	private void setContextProperties( ActionBase action , ServerProductContext productContext ) throws Exception {
		CONFIG_PRODUCT = productContext.CONFIG_PRODUCT;
		CONFIG_PRODUCTHOME = productContext.CONFIG_PRODUCTHOME;
		
		CONFIG_LASTPRODTAG = productContext.CONFIG_LASTPRODTAG;
		CONFIG_NEXTPRODTAG = productContext.CONFIG_NEXTPRODTAG;
		CONFIG_VERSION_BRANCH_MAJOR = productContext.CONFIG_VERSION_BRANCH_MAJOR;
		CONFIG_VERSION_BRANCH_MINOR = productContext.CONFIG_VERSION_BRANCH_MINOR;
		CONFIG_VERSION_BRANCH_NEXTMAJOR = productContext.CONFIG_VERSION_BRANCH_NEXTMAJOR;
		CONFIG_VERSION_BRANCH_NEXTMINOR = productContext.CONFIG_VERSION_BRANCH_NEXTMINOR;
		
		properties.setStringProperty( PROPERTY_PRODUCT_NAME , CONFIG_PRODUCT );
		properties.setPathProperty( PROPERTY_PRODUCT_HOME , CONFIG_PRODUCTHOME , action.shell );
		
		properties.setNumberProperty( MetaProductVersion.PROPERTY_MAJOR_FIRST , CONFIG_VERSION_BRANCH_MAJOR );
		properties.setNumberProperty( MetaProductVersion.PROPERTY_MAJOR_LAST , CONFIG_VERSION_BRANCH_MINOR );
		properties.setNumberProperty( MetaProductVersion.PROPERTY_NEXT_MAJOR_FIRST , CONFIG_VERSION_BRANCH_NEXTMAJOR );
		properties.setNumberProperty( MetaProductVersion.PROPERTY_NEXT_MAJOR_LAST , CONFIG_VERSION_BRANCH_NEXTMINOR );
		properties.setNumberProperty( MetaProductVersion.PROPERTY_PROD_LASTTAG , CONFIG_LASTPRODTAG );
		properties.setNumberProperty( MetaProductVersion.PROPERTY_PROD_NEXTTAG , CONFIG_NEXTPRODTAG );
		properties.recalculateProperties();
	}

	public MetaProductBuildSettings getBuildCommonSettings( ActionBase action ) throws Exception {
		return( buildCommon );
	}
	
	public MetaProductBuildSettings getBuildModeSettings( ActionBase action , VarBUILDMODE buildMode ) throws Exception {
		String mode = Common.getEnumLower( buildMode );
		MetaProductBuildSettings settings = buildModes.get( mode );
		if( settings == null )
			action.exit1( _Error.UnableGetBuildModeSettings1 , "unable to get build settings for mode=" + mode , mode );
		return( settings );
	}
	
	public MetaProductBuildSettings getBuildSettings( ActionBase action ) throws Exception {
		if( action.context.buildMode == VarBUILDMODE.UNKNOWN )
			return( buildCommon );

		return( getBuildModeSettings( action , action.context.buildMode ) );
	}
    
	public String getPropertyAny( ActionBase action , String name ) throws Exception {
		return( properties.getPropertyAny( name ) );
	}
	
	public void setProperties( ServerTransaction transaction , PropertySet props , boolean system ) throws Exception {
		super.updateProperties( transaction , props , system );
	}

	public void setBuildCommonProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		buildCommon.setProperties( transaction , props );
	}
	
	public void setBuildModeProperties( ServerTransaction transaction , VarBUILDMODE mode , PropertySet props ) throws Exception {
		MetaProductBuildSettings set = buildModes.get( mode );
		if( set == null ) {
			MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "mode" , meta , this );
			buildModes.put( mode , buildMode );
		}
		
		set.setProperties( transaction , props );
	}

}
