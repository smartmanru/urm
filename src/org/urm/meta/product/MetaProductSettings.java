package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.engine.EngineMonitoring;

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
	
	// context and custom product properties
	public ObjectProperties ctx;
	
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

	public MetaProductSettings copy( Meta rmeta , ObjectProperties parent ) throws Exception {
		MetaProductSettings r = new MetaProductSettings( rmeta.getStorage() , rmeta );
		
		// context
		r.ctx = ctx.copy( parent );
		r.core = core.copy( rmeta , r );
		
		if( buildCommon != null )
			r.buildCommon = buildCommon.copy( rmeta , r , r.getProperties() ); 
		for( DBEnumBuildModeType mode : buildModes.keySet() ) {
			MetaProductBuildSettings modeSet = buildModes.get( mode );
			r.buildModes.put( mode , modeSet.copy( rmeta , r , r.buildCommon.getProperties() ) );
		}

		return( r );
	}

	public ObjectProperties getProperties() {
		return( ctx );
	}
	
	public void createSettings( ObjectProperties ctx , ProductContext context ) throws Exception {
		this.ctx = ctx;
		setContextProperties( context );
		
		// build
		buildCommon = new MetaProductBuildSettings( "build.common" , meta , this );
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			String modeName = Common.getEnumLower( mode );
			MetaProductBuildSettings buildMode = new MetaProductBuildSettings( "build." + modeName , meta , this );
			buildModes.put( mode , buildMode );
		}
	}

	public void createCoreSettings( ObjectProperties opsCore , ObjectProperties opsMon , EngineMonitoring sm ) throws Exception {
		core.createSettings( opsCore , opsMon , sm );
	}
	
	public void createBuildCommonSettings( ObjectProperties opsBuild ) throws Exception {
		buildCommon.createSettings( opsBuild );
	}

	public void createBuildModeSettings( DBEnumBuildModeType mode , ObjectProperties opsBuild ) throws Exception {
		MetaProductBuildSettings buildMode = getBuildModeSettings( mode );
		buildMode.createSettings( opsBuild );
	}

	public void setContextProperties( ProductContext context ) throws Exception {
		ctx.setStringProperty( PROPERTY_PRODUCT_NAME , meta.name );
		ctx.setPathProperty( PROPERTY_PRODUCT_HOME , context.home.folderPath );
		
		MetaProductVersion version = meta.getVersion();
		updateVersion( version );
	}
	
	private void updateVersion( MetaProductVersion version ) throws Exception {
		ctx.setIntProperty( PROPERTY_LAST_MAJOR_FIRST , version.majorLastFirstNumber );
		ctx.setIntProperty( PROPERTY_LAST_MAJOR_SECOND , version.majorLastSecondNumber );
		ctx.setIntProperty( PROPERTY_NEXT_MAJOR_FIRST , version.majorNextFirstNumber );
		ctx.setIntProperty( PROPERTY_NEXT_MAJOR_SECOND , version.majorNextSecondNumber );
		ctx.setIntProperty( PROPERTY_LAST_MINOR_FIRST , version.lastProdTag );
		ctx.setIntProperty( PROPERTY_NEXT_MINOR_FIRST , version.nextProdTag );
		ctx.setIntProperty( PROPERTY_LAST_MINOR_SECOND , version.lastUrgentTag );
		ctx.setIntProperty( PROPERTY_NEXT_MINOR_SECOND , version.nextUrgentTag );
	}
	
	public void updateSettings( MetaProductVersion version ) throws Exception {
		updateVersion( version );
		ctx.recalculateProperties();
		ctx.recalculateChildProperties();
	}
	
	public Map<String,String> getExportProperties( ActionBase action ) throws Exception {
		// export all variables
		Map<String,String> map = new HashMap<String,String>();
		String prefix = "export.";
		
		for( String name : ctx.getPropertyList() ) {
			if( name.startsWith( prefix ) ) {
				String value = ctx.getFinalProperty( name , action.shell.account , true , false );
				if( value != null )
					map.put( name.substring( prefix.length() ) , value );
			}
		}
		
		return( map );
	}

	public MetaProductCoreSettings getCoreSettings() {
		return( core );
	}
	
	public MetaProductBuildSettings getBuildCommonSettings() {
		return( buildCommon );
	}
	
	public MetaProductBuildSettings getBuildModeSettings( DBEnumBuildModeType buildMode ) throws Exception {
		String mode = Common.getEnumLower( buildMode );
		MetaProductBuildSettings settings = buildModes.get( buildMode );
		if( settings == null )
			Common.exit1( _Error.UnableGetBuildModeSettings1 , "unable to get build settings for mode=" + mode , mode );
		return( settings );
	}
	
	public MetaProductBuildSettings getBuildSettings( ActionBase action ) throws Exception {
		if( action.context.buildMode == DBEnumBuildModeType.UNKNOWN )
			return( buildCommon );

		return( getBuildModeSettings( action.context.buildMode ) );
	}
    
	public void setProperties( PropertySet props , boolean system ) throws Exception {
		ctx.updateProperties( props , system );
	}

	public void setBuildCommonProperties( PropertySet props ) throws Exception {
		buildCommon.setProperties( props );
	}
	
	public void setBuildModeProperties( DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		MetaProductBuildSettings set = buildModes.get( mode );
		if( set == null )
			Common.exitUnexpected();
		
		set.setProperties( props );
	}

}
