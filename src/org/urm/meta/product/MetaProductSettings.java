package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.engine.AppProduct;

public class MetaProductSettings {

	public static String PROPERTY_PRODUCT_NAME = "product";
	public static String PROPERTY_PRODUCT_HOME = "product.home";
	
	public Meta meta;
	
	// context and custom product properties
	public ObjectProperties ops;
	public ObjectProperties mon;
	
	// detailed product properties
	public MetaProductCoreSettings core;
	public MetaProductBuildSettings buildCommon;
	public Map<DBEnumBuildModeType,MetaProductBuildSettings> buildModes;
	
	public MetaProductSettings( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		meta.setSettings( this );
		core = new MetaProductCoreSettings( meta , this ); 
		buildModes = new HashMap<DBEnumBuildModeType,MetaProductBuildSettings>();
		
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

	public MetaProductSettings copy( Meta rmeta , ObjectProperties parent ) throws Exception {
		MetaProductSettings r = new MetaProductSettings( rmeta.getStorage() , rmeta );
		
		// context
		r.ops = ops.copy( parent );
		r.mon = mon.copy( r.ops );
		r.core = core.copy( rmeta , r );
		
		if( buildCommon != null )
			r.buildCommon = buildCommon.copy( rmeta , r , r.getParameters() ); 
		for( DBEnumBuildModeType mode : buildModes.keySet() ) {
			MetaProductBuildSettings modeSet = buildModes.get( mode );
			r.buildModes.put( mode , modeSet.copy( rmeta , r , r.buildCommon.getProperties() ) );
		}
		
		return( r );
	}

	public ObjectProperties getParameters() {
		return( ops );
	}
	
	public ObjectProperties getMonitoringProperties() {
		return( mon );
	}
	
	public void createCoreSettings( ObjectProperties ops ) throws Exception {
		this.ops = ops;
		core.createSettings();
	}

	public void createMonitoringSettings( ObjectProperties mon ) throws Exception {
		this.mon = mon;
		core.createMonitoringSettings();
	}
	
	public void createBuildCommonSettings( ObjectProperties opsBuild ) throws Exception {
		buildCommon.createSettings( opsBuild );
	}

	public void createBuildModeSettings( DBEnumBuildModeType mode , ObjectProperties opsBuild ) throws Exception {
		MetaProductBuildSettings buildMode = getBuildModeSettings( mode );
		buildMode.createSettings( opsBuild );
	}

	public void setContextProperties( ObjectProperties ops , ProductContext context ) throws Exception {
		ops.setStringProperty( PROPERTY_PRODUCT_NAME , meta.name );
		ops.setPathProperty( PROPERTY_PRODUCT_HOME , context.home.folderPath );
		
		updateVersion( ops , context.product );
	}
	
	private void updateVersion( ObjectProperties ops , AppProduct product ) throws Exception {
		ops.setIntProperty( AppProduct.PROPERTY_LAST_MAJOR_FIRST , product.LAST_MAJOR1 );
		ops.setIntProperty( AppProduct.PROPERTY_LAST_MAJOR_SECOND , product.LAST_MAJOR2 );
		ops.setIntProperty( AppProduct.PROPERTY_LAST_MINOR_FIRST , product.LAST_MINOR1 );
		ops.setIntProperty( AppProduct.PROPERTY_LAST_MINOR_SECOND , product.LAST_MINOR2 );
		ops.setIntProperty( AppProduct.PROPERTY_NEXT_MAJOR_FIRST , product.NEXT_MAJOR1 );
		ops.setIntProperty( AppProduct.PROPERTY_NEXT_MAJOR_SECOND , product.NEXT_MAJOR2 );
		ops.setIntProperty( AppProduct.PROPERTY_NEXT_MINOR_FIRST , product.NEXT_MINOR1 );
		ops.setIntProperty( AppProduct.PROPERTY_NEXT_MINOR_SECOND , product.NEXT_MINOR2 );
	}
	
	public void updateSettings( AppProduct product ) throws Exception {
		updateVersion( ops , product );
		ops.recalculateProperties();
		ops.recalculateChildProperties();
		updateContextSettings();
	}

	public void updateContextSettings() throws Exception {
		updateCoreSettings();
	}
	
	public void updateCoreSettings() throws Exception {
		core.scatterPrimaryProperties();
		core.scatterMonitoringProperties();
		updateBuildSettings();
	}

	public void updateBuildSettings() throws Exception {
		buildCommon.scatterProperties();
		for( MetaProductBuildSettings settings : buildModes.values() )
			settings.scatterProperties();
	}
	
	public void updateMonitoringSettings() throws Exception {
		core.scatterMonitoringProperties();
	}
	
	public Map<String,String> getExportProperties( ActionBase action ) throws Exception {
		// export all variables
		Map<String,String> map = new HashMap<String,String>();
		String prefix = "export.";
		
		ObjectMeta meta = ops.getMeta();
		PropertyEntity entity  = meta.getCustomEntity();
		for( String name : entity.getVarNames() ) {
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

}
