package org.urm.db.engine;

import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.EngineDB;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductSettings;

public abstract class DBEngineSettings {

	public static PropertyEntity upgradeEntityProduct( DBConnection c ) throws Exception {
		return( DBSettings.savedbEntity( c , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.PRODUCT , false , EngineDB.APP_VERSION , new EntityVar[] { 
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_REDISTLINUX_PATH , "Linux Staging Area Path" , true , null ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_REDISTWIN_PATH , "Windows Staging Area Path" , true , null ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_DISTR_PATH , "Distributives Path" , true , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_DISTR_HOSTLOGIN , "Distributives host@login" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_UPGRADE_PATH , "Upgrade Scripts Path" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_BASE_PATH , "Platform Software Path" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_MIRRORPATH , "Mirror Repositories" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_ADM_TRACKER , "Codebase Control Tracker" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_COMMIT_TRACKERLIST , "Source Task Trackers" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_SOURCE_CHARSET , "Release Source Charset" , false , null ) ,
				EntityVar.metaPathRelative( MetaProductSettings.PROPERTY_SOURCE_RELEASEROOTDIR , "Release Source Root" , false , null ) ,
				EntityVar.metaPathRelative( MetaProductSettings.PROPERTY_SOURCE_CFG_ROOTDIR , "Configuration Root" , false , null ) ,
				EntityVar.metaPathRelative( MetaProductSettings.PROPERTY_SOURCE_CFG_LIVEROOTDIR , "Configuration Live" , false , null ) ,
				EntityVar.metaPathRelative( MetaProductSettings.PROPERTY_SOURCE_SQL_POSTREFRESH , "Database PostRefresh" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_CUSTOM_BUILD , "Custom Builder Plugin" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_CUSTOM_DEPLOY , "Custom Deployer Plugin" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_CUSTOM_DATABASE , "Custom Database Plugin" , false , null )
		} ) );
	}

	public static PropertyEntity upgradeEntityProductBuild( DBConnection c ) throws Exception {
		return( DBSettings.savedbEntity( c , DBEnumObjectVersionType.APP , DBVersions.APP_ID , DBEnumParamEntityType.PRODUCTBUILD , false , EngineDB.APP_VERSION , new EntityVar[] { 
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_LASTMAJOR , "Last Major Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_NEXTMAJOR , "Next Major Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_LASTMINOR , "Last Minor Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_NEXTMINOR , "Next Minor Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_VERSION , "Release Build Version" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_APPVERSION , "Artefacts Version" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_BRANCHNAME , "Source Branch Name" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_GROUPFOLDER , "Release Source Group" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductBuildSettings.PROPERTY_ARTEFACTDIR , "Artefacts Directory" , false , null ) ,
				EntityVar.metaPathRelative( MetaProductBuildSettings.PROPERTY_LOGPATH , "Build Log Path" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_NEXUS_REPO , "Nexus Repository" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_NEXUS_REPO_THIRDPARTY , "Nexus Thirdparty Repository" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductBuildSettings.PROPERTY_MAVEN_CFGFILE , "Maven Settings" , false , null )
		} ) );
	}

}
