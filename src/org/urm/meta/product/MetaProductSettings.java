package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.ProductContext;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductSettings {

	public static String PROPERTY_LAST_MAJOR_FIRST = "major.first";
	public static String PROPERTY_LAST_MAJOR_SECOND = "major.last";
	public static String PROPERTY_NEXT_MAJOR_FIRST = "next.major.first";
	public static String PROPERTY_NEXT_MAJOR_SECOND = "next.major.last";
	public static String PROPERTY_LAST_MINOR_FIRST = "prod.lasttag";
	public static String PROPERTY_LAST_MINOR_SECOND = "prod.lasturgent";
	public static String PROPERTY_NEXT_MINOR_FIRST = "prod.nexttag";
	public static String PROPERTY_NEXT_MINOR_SECOND = "prod.nexturgent";
	
	public static String PROPERTY_PRODUCT_NAME = "product";
	public static String PROPERTY_PRODUCT_HOME = "product.home";
	
	public Meta meta;
	public MetaProductSettings settings;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	public int CONFIG_LASTPRODTAG;
	public int CONFIG_NEXTPRODTAG;
	public int CONFIG_VERSION_BRANCH_MAJOR;
	public int CONFIG_VERSION_BRANCH_MINOR;
	public int CONFIG_VERSION_BRANCH_NEXTMAJOR;
	public int CONFIG_VERSION_BRANCH_NEXTMINOR;

	// context and custom product properties
	private ObjectProperties ops;
	
	// detailed product properties
	public MetaProductCoreSettings core;
	public MetaProductBuildSettings buildCommon;
	public Map<DBEnumBuildModeType,MetaProductBuildSettings> buildModes;
	
	public MetaProductSettings( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setSettings( this );
		core = new MetaProductCoreSettings( meta , this ); 
		buildModes = new HashMap<DBEnumBuildModeType,MetaProductBuildSettings>();
	}

	public MetaProductSettings copy( ActionBase action , Meta rmeta , ObjectProperties parent ) throws Exception {
		MetaProductSettings r = new MetaProductSettings( rmeta.getStorage() , rmeta );
		
		// context
		r.CONFIG_PRODUCT = CONFIG_PRODUCT;
		r.CONFIG_PRODUCTHOME = CONFIG_PRODUCTHOME;
		
		r.CONFIG_LASTPRODTAG = CONFIG_LASTPRODTAG;
		r.CONFIG_NEXTPRODTAG = CONFIG_NEXTPRODTAG;
		r.CONFIG_VERSION_BRANCH_MAJOR = CONFIG_VERSION_BRANCH_MAJOR;
		r.CONFIG_VERSION_BRANCH_MINOR = CONFIG_VERSION_BRANCH_MINOR;
		r.CONFIG_VERSION_BRANCH_NEXTMAJOR = CONFIG_VERSION_BRANCH_NEXTMAJOR;
		r.CONFIG_VERSION_BRANCH_NEXTMINOR = CONFIG_VERSION_BRANCH_NEXTMINOR;

		r.ops = ops.copy( parent );
		r.core = core.copy( action , rmeta , r );
		
		if( buildCommon != null )
			r.buildCommon = buildCommon.copy( action , rmeta , r , r.getProperties() ); 
		for( DBEnumBuildModeType mode : buildModes.keySet() ) {
			MetaProductBuildSettings modeSet = buildModes.get( mode );
			r.buildModes.put( mode , modeSet.copy( action , rmeta , r , r.buildCommon.getProperties() ) );
		}

		return( r );
	}

	public ObjectProperties getProperties() {
		return( ops );
	}
	
	public void createSettings( TransactionBase transaction , EngineSettings settings , ProductContext productContext ) throws Exception {
		ActionBase action = transaction.action;
		EngineEntities entities = action.getServerEntities();
		ops = entities.createMetaProps( meta , null );
				
		setContextProperties( transaction.action , productContext );
		
		// create initial
		core.create( transaction.action );
		
		ops.copyOriginalPropertiesToRaw( settings.getDefaultProductProperties() );
		ops.recalculateProperties();
		
		// build
		buildCommon = new MetaProductBuildSettings( "build.common" , meta , this );
		buildCommon.createSettings( transaction , settings.getDefaultProductBuildProperties() , ops );
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			String modeName = Common.getEnumLower( mode );
			MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "build." + modeName , meta , this );
			ObjectProperties set = settings.getDefaultProductBuildObjectProperties( mode );
			buildMode.createSettings( transaction , set , buildCommon.getProperties() );
			buildModes.put( mode , buildMode );
		}
	}

	public void setContextProperties( ActionBase action , ProductContext productContext ) throws Exception {
		CONFIG_PRODUCT = productContext.CONFIG_PRODUCT;
		CONFIG_PRODUCTHOME = productContext.CONFIG_PRODUCTHOME;
		
		CONFIG_LASTPRODTAG = productContext.CONFIG_LASTPRODTAG;
		CONFIG_NEXTPRODTAG = productContext.CONFIG_NEXTPRODTAG;
		CONFIG_VERSION_BRANCH_MAJOR = productContext.CONFIG_VERSION_BRANCH_MAJOR;
		CONFIG_VERSION_BRANCH_MINOR = productContext.CONFIG_VERSION_BRANCH_MINOR;
		CONFIG_VERSION_BRANCH_NEXTMAJOR = productContext.CONFIG_VERSION_BRANCH_NEXTMAJOR;
		CONFIG_VERSION_BRANCH_NEXTMINOR = productContext.CONFIG_VERSION_BRANCH_NEXTMINOR;
		
		ops.setManualStringProperty( PROPERTY_PRODUCT_NAME , CONFIG_PRODUCT );
		ops.setManualPathProperty( PROPERTY_PRODUCT_HOME , CONFIG_PRODUCTHOME , action.shell );
		
		ops.setManualIntProperty( PROPERTY_LAST_MAJOR_FIRST , CONFIG_VERSION_BRANCH_MAJOR );
		ops.setManualIntProperty( PROPERTY_LAST_MAJOR_SECOND , CONFIG_VERSION_BRANCH_MINOR );
		ops.setManualIntProperty( PROPERTY_NEXT_MAJOR_FIRST , CONFIG_VERSION_BRANCH_NEXTMAJOR );
		ops.setManualIntProperty( PROPERTY_NEXT_MAJOR_SECOND , CONFIG_VERSION_BRANCH_NEXTMINOR );
		ops.setManualIntProperty( PROPERTY_LAST_MINOR_FIRST , CONFIG_LASTPRODTAG );
		ops.setManualIntProperty( PROPERTY_NEXT_MINOR_FIRST , CONFIG_NEXTPRODTAG );
	}
	
	public void updateSettings( TransactionBase transaction , ProductContext productContext ) throws Exception {
		setContextProperties( transaction.action , productContext );
		ops.recalculateProperties();
		ops.recalculateChildProperties();
	}
	
	public void load( ActionBase action , ProductContext productContext , Node root ) throws Exception {
		core.load( action , productContext , root );
		
		//ops.loadFromNodeElements( action , root , false );
		//Node custom = ConfReader.xmlGetFirstChild( root , "custom" );
		//if( custom != null )
		//	ops.loadFromNodeElements( action , custom , true );
		ops.recalculateProperties();

		buildCommon = new MetaProductBuildSettings( "build" , meta , this );
		Node build = ConfReader.xmlGetFirstChild( root , "build" );
		if( build != null ) {
			buildCommon.load( action , build , ops );
			Node[] items = ConfReader.xmlGetChildren( build , "mode" );
			if( items != null ) {
				for( Node node : items ) {
					String modeName = ConfReader.getAttrValue( node , "name" );
					DBEnumBuildModeType mode = DBEnumBuildModeType.getValue( modeName , false );
					
					MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "mode" , meta , this );
					buildMode.load( action , node , buildCommon.getProperties() );
					buildModes.put( mode , buildMode );
				}
			}
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element coreElement = Common.xmlCreateElement( doc , root , "core" );
		core.save( action , doc , coreElement );
		
		//Element customElement = Common.xmlCreateElement( doc , root , "custom" );
		// ops.saveAsElements( doc , customElement , true );
		
		Element buildElement = Common.xmlCreateElement( doc , root , "build" );
		buildCommon.save( action , doc , buildElement );
		
		for( DBEnumBuildModeType mode : buildModes.keySet() ) {
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
		
		for( String name : ops.getPropertyList() ) {
			if( name.startsWith( prefix ) ) {
				String value = ops.getFinalProperty( name , action.shell.account , true , false );
				if( value != null )
					map.put( name.substring( prefix.length() ) , value );
			}
		}
		
		return( map );
	}

	public MetaProductCoreSettings getCoreSettings() {
		return( core );
	}
	
	public MetaProductBuildSettings getBuildCommonSettings( ActionBase action ) throws Exception {
		return( buildCommon );
	}
	
	public MetaProductBuildSettings getBuildModeSettings( ActionBase action , DBEnumBuildModeType buildMode ) throws Exception {
		String mode = Common.getEnumLower( buildMode );
		MetaProductBuildSettings settings = buildModes.get( buildMode );
		if( settings == null )
			action.exit1( _Error.UnableGetBuildModeSettings1 , "unable to get build settings for mode=" + mode , mode );
		return( settings );
	}
	
	public MetaProductBuildSettings getBuildSettings( ActionBase action ) throws Exception {
		if( action.context.buildMode == DBEnumBuildModeType.UNKNOWN )
			return( buildCommon );

		return( getBuildModeSettings( action , action.context.buildMode ) );
	}
    
	public void setProperties( EngineTransaction transaction , PropertySet props , boolean system ) throws Exception {
		ops.updateProperties( transaction , props , system );
	}

	public void setBuildCommonProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		buildCommon.setProperties( transaction , props );
	}
	
	public void setBuildModeProperties( EngineTransaction transaction , DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		MetaProductBuildSettings set = buildModes.get( mode );
		if( set == null ) {
			MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "mode" , meta , this );
			buildModes.put( mode , buildMode );
			set = buildMode;
		}
		
		set.setProperties( transaction , props );
	}

}
