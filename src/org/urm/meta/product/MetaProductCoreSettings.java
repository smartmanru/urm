package org.urm.meta.product;

import java.nio.charset.Charset;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.engine.EngineContext;

public class MetaProductCoreSettings {

	// engine overrides
	public static String PROPERTY_REDISTWIN_PATH = EngineContext.PROPERTY_STAGING_WINPATH;
	public static String PROPERTY_REDISTLINUX_PATH = EngineContext.PROPERTY_STAGING_LINUXPATH;
	
	// own properties
	public static String PROPERTY_DISTR_PATH  = "distr.path";
	public static String PROPERTY_DISTR_HOSTLOGIN = "distr.hostlogin";
	public static String PROPERTY_UPGRADE_PATH = "upgrade.path";
	public static String PROPERTY_BASE_PATH = "base.path";
	public static String PROPERTY_MIRRORPATH = "mirror.path";
	
	public static String PROPERTY_ADM_TRACKER = "adm.tracker";
	public static String PROPERTY_COMMIT_TRACKERLIST = "source.trackers";
	
	public static String PROPERTY_SOURCE_CHARSET = "release.charset";
	public static String PROPERTY_SOURCE_RELEASEROOTDIR = "release.root";
	public static String PROPERTY_SOURCE_CFG_ROOTDIR = "config.root";
	public static String PROPERTY_SOURCE_CFG_LIVEROOTDIR = "config.live";
	public static String PROPERTY_SOURCE_SQL_POSTREFRESH = "config.postrefresh";
	
	public static String PROPERTY_CUSTOM_BUILD = "custom.build";
	public static String PROPERTY_CUSTOM_DEPLOY = "custom.deploy";
	public static String PROPERTY_CUSTOM_DATABASE = "custom.database";

	public static String PROPERTY_MONITORING_RESOURCE_URL = "resources.url";
	public static String PROPERTY_MONITORING_DIR_RES = "resources.path";
	public static String PROPERTY_MONITORING_DIR_DATA = "data.path";
	public static String PROPERTY_MONITORING_DIR_REPORTS = "reports.path";
	public static String PROPERTY_MONITORING_DIR_LOGS = "logs.path";
	
	public Meta meta;
	public MetaProductSettings settings;
	public ObjectProperties ops;
	public ObjectProperties mon;
	
	public String CONFIG_REDISTWIN_PATH;
	public String CONFIG_REDISTLINUX_PATH;
	public String CONFIG_DISTR_PATH;
	public String CONFIG_DISTR_HOSTLOGIN;
	public String CONFIG_UPGRADE_PATH;
	public String CONFIG_BASE_PATH;
	public String CONFIG_MIRRORPATH;
	public String CONFIG_ADM_TRACKER;
	public String CONFIG_COMMIT_TRACKERLIST;
	public String CONFIG_META_MIRROR;
	public String CONFIG_SOURCE_MIRROR;
	
	public String CONFIG_SOURCE_CHARSET;
	public Charset charset;
	public String CONFIG_SOURCE_RELEASEROOTDIR;
	public String CONFIG_SOURCE_CFG_ROOTDIR;
	public String CONFIG_SOURCE_CFG_LIVEROOTDIR;
	public String CONFIG_SOURCE_SQL_POSTREFRESH;
	
	public String CONFIG_CUSTOM_BUILD;
	public String CONFIG_CUSTOM_DEPLOY;
	public String CONFIG_CUSTOM_DATABASE;
	
	public String MONITORING_RESOURCE_URL;
	public String MONITORING_DIR_RES;
	public String MONITORING_DIR_DATA;
	public String MONITORING_DIR_REPORTS;
	public String MONITORING_DIR_LOGS;
	
	public MetaProductCoreSettings( Meta meta , MetaProductSettings settings ) {
		this.meta = meta;
		this.settings = settings;
	}

	public MetaProductCoreSettings copy( Meta rmeta , MetaProductSettings rsettings ) throws Exception {
		MetaProductCoreSettings r = new MetaProductCoreSettings( rmeta , rsettings );
		r.ops = ops.copy( rsettings.getProperties() );
		r.scatterPrimaryProperties();
		r.mon = mon.copy( ops );
		r.scatterMonitoringProperties();
		return( r );
	}
	
	private void scatterPrimaryProperties() throws Exception {
		CONFIG_REDISTWIN_PATH = ops.getPathProperty( PROPERTY_REDISTWIN_PATH );
		CONFIG_REDISTLINUX_PATH = ops.getPathProperty( PROPERTY_REDISTLINUX_PATH );
		CONFIG_DISTR_PATH = ops.getPathProperty( PROPERTY_DISTR_PATH );
		CONFIG_DISTR_HOSTLOGIN = ops.getStringProperty( PROPERTY_DISTR_HOSTLOGIN );
		CONFIG_UPGRADE_PATH = ops.getPathProperty( PROPERTY_UPGRADE_PATH );
		CONFIG_BASE_PATH = ops.getPathProperty( PROPERTY_BASE_PATH );
		CONFIG_MIRRORPATH = ops.getPathProperty( PROPERTY_MIRRORPATH );
		CONFIG_ADM_TRACKER = ops.getStringProperty( PROPERTY_ADM_TRACKER );
		CONFIG_COMMIT_TRACKERLIST = ops.getStringProperty( PROPERTY_COMMIT_TRACKERLIST );
		CONFIG_SOURCE_RELEASEROOTDIR = ops.getPathProperty( PROPERTY_SOURCE_RELEASEROOTDIR );
		CONFIG_SOURCE_CFG_ROOTDIR = ops.getPathProperty( PROPERTY_SOURCE_CFG_ROOTDIR );
		CONFIG_SOURCE_CFG_LIVEROOTDIR = ops.getPathProperty( PROPERTY_SOURCE_CFG_LIVEROOTDIR );
		CONFIG_SOURCE_SQL_POSTREFRESH = ops.getPathProperty( PROPERTY_SOURCE_SQL_POSTREFRESH );
		
		CONFIG_SOURCE_CHARSET = ops.getStringProperty( PROPERTY_SOURCE_CHARSET );
		if( !CONFIG_SOURCE_CHARSET.isEmpty() ) {
			charset = Charset.availableCharsets().get( CONFIG_SOURCE_CHARSET.toUpperCase() );
			if( charset == null )
				Common.exit1( _Error.UnknownDatabaseFilesCharset1 , "unknown database files charset=" + CONFIG_SOURCE_CHARSET , CONFIG_SOURCE_CHARSET );
		}
	
		CONFIG_CUSTOM_BUILD = ops.getStringProperty( PROPERTY_CUSTOM_BUILD );
		CONFIG_CUSTOM_DEPLOY = ops.getStringProperty( PROPERTY_CUSTOM_DEPLOY );
		CONFIG_CUSTOM_DATABASE = ops.getStringProperty( PROPERTY_CUSTOM_DATABASE );
	}

	public void createCoreSettings( ObjectProperties ops ) throws Exception {
		this.ops = ops;
		scatterPrimaryProperties();
	}
	
	public String getTargetPath( DBEnumOSType osType , String artefactDir ) {
		if( Common.isAbsolutePath( artefactDir ) )
			return( artefactDir );
		
		String redistPath = ( osType.isWindows() )? CONFIG_REDISTWIN_PATH : CONFIG_REDISTLINUX_PATH;
		String finalPath = Common.getPath( redistPath , artefactDir );
		return( finalPath );
	}

	private void scatterMonitoringProperties() throws Exception {
		MONITORING_RESOURCE_URL = mon.getStringProperty( PROPERTY_MONITORING_RESOURCE_URL );
		MONITORING_DIR_RES = mon.getPathProperty( PROPERTY_MONITORING_DIR_RES );
		MONITORING_DIR_DATA = mon.getPathProperty( PROPERTY_MONITORING_DIR_DATA );
		MONITORING_DIR_REPORTS = mon.getPathProperty( PROPERTY_MONITORING_DIR_REPORTS );
		MONITORING_DIR_LOGS = mon.getPathProperty( PROPERTY_MONITORING_DIR_LOGS );
	}
	
	public void createMonitoringSettings( ObjectProperties mon ) throws Exception {
		this.mon = mon;
		scatterMonitoringProperties();
	}
	
	public void setMonitoringProperties( PropertySet src ) throws Exception {
		mon.setUrlProperty( PROPERTY_MONITORING_RESOURCE_URL , src.getExpressionByProperty( PROPERTY_MONITORING_RESOURCE_URL ) );
		mon.setPathProperty( PROPERTY_MONITORING_DIR_RES , src.getExpressionByProperty( PROPERTY_MONITORING_DIR_RES ) );
		mon.setPathProperty( PROPERTY_MONITORING_DIR_DATA , src.getExpressionByProperty( PROPERTY_MONITORING_DIR_DATA ) );
		mon.setPathProperty( PROPERTY_MONITORING_DIR_REPORTS , src.getExpressionByProperty( PROPERTY_MONITORING_DIR_REPORTS ) );
		mon.setPathProperty( PROPERTY_MONITORING_DIR_LOGS , src.getExpressionByProperty( PROPERTY_MONITORING_DIR_LOGS ) );
		scatterMonitoringProperties();
	}

	public boolean isValidMonitoringSettings() {
		if( MONITORING_DIR_RES.isEmpty() || 
			MONITORING_DIR_DATA.isEmpty() || 
			MONITORING_DIR_REPORTS.isEmpty() || 
			MONITORING_DIR_LOGS.isEmpty() )
			return( false );
		return( true );
	}
	
}
