package org.urm.db.engine;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.ProjectBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBEngineBuilders {

	public static String ELEMENT_BUILDER = "builder";
	public static String TABLE_BUILDER = "urm_project_builder";
	public static String FIELD_BUILDER_ID = "builder_id";
	public static String FIELD_BUILDER_DESC = "xdesc";
	public static String FIELD_BUILDER_METHOD = "buildermethod_type";
	public static String FIELD_BUILDER_TARGET = "buildertarget_type";
	public static String FIELD_BUILDER_TARGET_RESOURCE = "target_resource_id";
	public static String FIELD_BUILDER_TARGET_PATH = "target_path";
	public static String FIELD_BUILDER_TARGET_PLATFORM = "target_platform";
	public static String FIELD_BUILDER_COMMAND = "builder_command";
	public static String FIELD_BUILDER_HOMEPATH = "builder_homepath";
	public static String FIELD_BUILDER_OPTIONS = "builder_options";
	public static String FIELD_BUILDER_JDKPATH = "java_jdkhomepath";
	public static String FIELD_BUILDER_REMOTEOSTYPE = "remote_os_type";
	public static String FIELD_BUILDER_REMOTEHOSTLOGIN = "remote_hostlogin";
	public static String FIELD_BUILDER_REMOTEPORT = "remote_port";
	public static String FIELD_BUILDER_REMOTEAUTHRESOURCE = "remote_auth_resource_id";
	
	public static PropertyEntity upgradeEntityBuilder( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BUILDER , DBEnumParamEntityType.BUILDER , DBEnumObjectVersionType.CORE , TABLE_BUILDER , FIELD_BUILDER_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( ProjectBuilder.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( ProjectBuilder.PROPERTY_DESC , FIELD_BUILDER_DESC , ProjectBuilder.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( ProjectBuilder.PROPERTY_VERSION , "Version" , true , null ) ,
				EntityVar.metaEnumVar( ProjectBuilder.PROPERTY_BUILDERTYPE , FIELD_BUILDER_METHOD , ProjectBuilder.PROPERTY_BUILDERTYPE , "Build method" , true , DBEnumBuilderMethodType.UNKNOWN ) ,
				EntityVar.metaStringVar( ProjectBuilder.PROPERTY_BUILDER_COMMAND , FIELD_BUILDER_COMMAND , ProjectBuilder.PROPERTY_BUILDER_COMMAND , "Method command" , true , null ) ,
				EntityVar.metaStringVar( ProjectBuilder.PROPERTY_BUILDER_HOMEPATH , FIELD_BUILDER_HOMEPATH , ProjectBuilder.PROPERTY_BUILDER_HOMEPATH , "Builder home path" , false , null ) ,
				EntityVar.metaStringVar( ProjectBuilder.PROPERTY_BUILDER_OPTIONS , FIELD_BUILDER_OPTIONS , ProjectBuilder.PROPERTY_BUILDER_OPTIONS , "Builder options" , false , null ) ,
				EntityVar.metaStringVar( ProjectBuilder.PROPERTY_JAVA_JDKHOMEPATH , FIELD_BUILDER_JDKPATH , ProjectBuilder.PROPERTY_JAVA_JDKHOMEPATH , "JDK path" , false , null ) ,
				EntityVar.metaEnumVar( ProjectBuilder.PROPERTY_TARGETTYPE , FIELD_BUILDER_TARGET , ProjectBuilder.PROPERTY_TARGETTYPE , "Build target" , true , DBEnumBuilderTargetType.UNKNOWN ) ,
				EntityVar.metaObjectVar( ProjectBuilder.PROPERTY_TARGETRESOURCE , FIELD_BUILDER_TARGET_RESOURCE , ProjectBuilder.PROPERTY_TARGETRESOURCE , "Target resource" , DBEnumObjectType.RESOURCE , false ) ,
				EntityVar.metaStringVar( ProjectBuilder.PROPERTY_TARGETPATH , FIELD_BUILDER_TARGET_PATH , ProjectBuilder.PROPERTY_TARGETPATH , "Target path" , false , null ) ,
				EntityVar.metaStringVar( ProjectBuilder.PROPERTY_TARGETPLATFORM , FIELD_BUILDER_TARGET_PLATFORM , ProjectBuilder.PROPERTY_TARGETPLATFORM , "Target platform" , false , null ) ,
				EntityVar.metaBoolean( ProjectBuilder.PROPERTY_REMOTE , "Remote build" , false , false ) ,
				EntityVar.metaEnumVar( ProjectBuilder.PROPERTY_REMOTEOSTYPE , FIELD_BUILDER_REMOTEOSTYPE , ProjectBuilder.PROPERTY_REMOTEOSTYPE , "Remote host OS type" , false , DBEnumOSType.UNKNOWN ) ,
				EntityVar.metaStringVar( ProjectBuilder.PROPERTY_REMOTEHOSTLOGIN , FIELD_BUILDER_REMOTEHOSTLOGIN , ProjectBuilder.PROPERTY_REMOTEHOSTLOGIN , "Remote host login" , false , null ) ,
				EntityVar.metaIntegerVar( ProjectBuilder.PROPERTY_REMOTEPORT , FIELD_BUILDER_REMOTEPORT , ProjectBuilder.PROPERTY_REMOTEPORT , "Remote access port" , false , 22 ) ,
				EntityVar.metaObjectVar( ProjectBuilder.PROPERTY_REMOTEAUTHRESOURCE , FIELD_BUILDER_REMOTEAUTHRESOURCE , ProjectBuilder.PROPERTY_REMOTEAUTHRESOURCE , "Target resource" , DBEnumObjectType.RESOURCE , false ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityBuilder( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.BUILDER , DBEnumParamEntityType.BUILDER , DBEnumObjectVersionType.CORE , TABLE_BUILDER , FIELD_BUILDER_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static void importxml( EngineLoader loader , EngineBuilders builders , Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_BUILDER );
		if( list != null ) {
			for( Node node : list ) {
				ProjectBuilder builder = importxmlBuilder( loader , builders , node );
				builders.addBuilder( builder );
			}
		}
	}
	
	private static ProjectBuilder importxmlBuilder( EngineLoader loader , EngineBuilders builders , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProjectBuilder;
		
		ProjectBuilder builder = new ProjectBuilder( builders );
		builder.createBuilder(
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_NAME ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_DESC ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_VERSION )
				);
		builder.setMethodData(
				DBEnumBuilderMethodType.getValue( entity.importxmlEnumProperty( root , ProjectBuilder.PROPERTY_BUILDERTYPE ) , true ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_BUILDER_COMMAND ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_BUILDER_HOMEPATH ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_BUILDER_OPTIONS ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_JAVA_JDKHOMEPATH )
				);
		builder.setTargetData(
				DBEnumBuilderTargetType.getValue( entity.importxmlEnumProperty( root , ProjectBuilder.PROPERTY_TARGETTYPE ) , true ) ,
				entity.importxmlObjectProperty( loader , root , ProjectBuilder.PROPERTY_TARGETRESOURCE ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_TARGETPATH ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_TARGETPLATFORM )
				);
		builder.setRemoteData(
				entity.importxmlBooleanProperty( root , ProjectBuilder.PROPERTY_REMOTE , false ) ,
				DBEnumOSType.getValue( entity.importxmlEnumProperty( root , ProjectBuilder.PROPERTY_REMOTEOSTYPE ) , false ) ,
				entity.importxmlStringProperty( root , ProjectBuilder.PROPERTY_REMOTEHOSTLOGIN ) ,
				entity.importxmlIntProperty( root , ProjectBuilder.PROPERTY_REMOTEPORT ) ,
				entity.importxmlObjectProperty( loader , root , ProjectBuilder.PROPERTY_REMOTEAUTHRESOURCE )
				);
		modifyBuilder( c , builder , true );

		return( builder );
	}
	
	private static void modifyBuilder( DBConnection c , ProjectBuilder builder , boolean insert ) throws Exception {
		if( insert )
			builder.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , builder.NAME , DBEnumObjectType.BUILDER );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , builder.NAME , builder.ID , DBEnumObjectType.BUILDER );
		
		builder.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppProjectBuilder , builder.ID , builder.CV , new String[] {
				EngineDB.getString( builder.NAME ) ,
				EngineDB.getString( builder.DESC ) ,
				EngineDB.getString( builder.VERSION ) ,
				EngineDB.getEnum( builder.BUILDER_METHOD_TYPE ) ,
				EngineDB.getString( builder.BUILDER_COMMAND ) ,
				EngineDB.getString( builder.BUILDER_HOMEPATH ) ,
				EngineDB.getString( builder.BUILDER_OPTIONS ) ,
				EngineDB.getString( builder.JAVA_JDKHOMEPATH ) ,
				EngineDB.getEnum( builder.BUILDER_TARGET_TYPE ) ,
				EngineDB.getObject( builder.TARGET_RESOURCE_ID ) ,
				EngineDB.getString( builder.TARGET_PATH ) ,
				EngineDB.getString( builder.TARGET_PLATFORM ) ,
				EngineDB.getBoolean( builder.REMOTE ) ,
				EngineDB.getEnum( builder.REMOTE_OS_TYPE ) ,
				EngineDB.getString( builder.REMOTE_HOSTLOGIN ) ,
				EngineDB.getInteger( builder.REMOTE_PORT ) ,
				EngineDB.getObject( builder.REMOTE_AUTH_RESOURCE_ID )
		} , insert );
	}

	public static void exportxml( EngineLoader loader , EngineBuilders builders , Document doc , Element root ) throws Exception {
		for( String name : builders.getBuilderNames() ) {
			ProjectBuilder builder = builders.findBuilder( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_BUILDER );
			exportxmlBuilder( loader , builder , doc , node );
		}
	}
	
	private static void exportxmlBuilder( EngineLoader loader , ProjectBuilder builder , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppProjectBuilder;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( builder.NAME ) ,
				entity.exportxmlString( builder.DESC ) ,
				entity.exportxmlString( builder.VERSION ) ,
				entity.exportxmlEnum( builder.BUILDER_METHOD_TYPE ) ,
				entity.exportxmlString( builder.BUILDER_COMMAND ) ,
				entity.exportxmlString( builder.BUILDER_HOMEPATH ) ,
				entity.exportxmlString( builder.BUILDER_OPTIONS ) ,
				entity.exportxmlString( builder.JAVA_JDKHOMEPATH ) ,
				entity.exportxmlEnum( builder.BUILDER_TARGET_TYPE ) ,
				entity.exportxmlObject( loader , ProjectBuilder.PROPERTY_TARGETRESOURCE , builder.TARGET_RESOURCE_ID ) ,
				entity.exportxmlString( builder.TARGET_PATH ) ,
				entity.exportxmlString( builder.TARGET_PLATFORM ) ,
				entity.exportxmlBoolean( builder.REMOTE ) ,
				entity.exportxmlEnum( builder.REMOTE_OS_TYPE ) ,
				entity.exportxmlString( builder.REMOTE_HOSTLOGIN ) ,
				entity.exportxmlInt( builder.REMOTE_PORT ) ,
				entity.exportxmlObject( loader , ProjectBuilder.PROPERTY_REMOTEAUTHRESOURCE , builder.REMOTE_AUTH_RESOURCE_ID )
		} , false );
	}

	public static void loaddb( EngineLoader loader , EngineBuilders builders ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppProjectBuilder;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				ProjectBuilder builder = new ProjectBuilder( builders );
				builder.ID = entity.loaddbId( rs );
				builder.CV = entity.loaddbVersion( rs );
				builder.createBuilder(
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_NAME ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_DESC ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_VERSION )
						);
				builder.setMethodData(
						DBEnumBuilderMethodType.getValue( entity.loaddbEnum( rs , ProjectBuilder.PROPERTY_BUILDERTYPE ) , true ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_BUILDER_COMMAND ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_BUILDER_HOMEPATH ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_BUILDER_OPTIONS ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_JAVA_JDKHOMEPATH )
						);
				builder.setTargetData(
						DBEnumBuilderTargetType.getValue( entity.loaddbEnum( rs , ProjectBuilder.PROPERTY_TARGETTYPE ) , true ) ,
						entity.loaddbObject( rs , ProjectBuilder.PROPERTY_TARGETRESOURCE ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_TARGETPATH ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_TARGETPLATFORM )
						);
				builder.setRemoteData(
						entity.loaddbBoolean( rs , ProjectBuilder.PROPERTY_REMOTE ) ,
						DBEnumOSType.getValue( entity.loaddbEnum( rs , ProjectBuilder.PROPERTY_REMOTEOSTYPE ) , false ) ,
						entity.loaddbString( rs , ProjectBuilder.PROPERTY_REMOTEHOSTLOGIN ) ,
						entity.loaddbInt( rs , ProjectBuilder.PROPERTY_REMOTEPORT ) ,
						entity.loaddbObject( rs , ProjectBuilder.PROPERTY_REMOTEAUTHRESOURCE )
						);
				builders.addBuilder( builder );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static ProjectBuilder createBuilder( EngineTransaction transaction , EngineBuilders builders , ProjectBuilder bdata ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( builders.findBuilder( bdata.NAME ) != null )
			transaction.exit1( _Error.DuplicateBuilder1 , "builder already exists name=" + bdata.NAME , bdata.NAME );
			
		ProjectBuilder builder = new ProjectBuilder( builders );
		builder.createBuilder( bdata.NAME , bdata.DESC , bdata.VERSION );
		builder.setMethodData( bdata.BUILDER_METHOD_TYPE , bdata.BUILDER_COMMAND , bdata.BUILDER_HOMEPATH , bdata.BUILDER_OPTIONS , bdata.JAVA_JDKHOMEPATH );
		builder.setTargetData( bdata.BUILDER_TARGET_TYPE , bdata.TARGET_RESOURCE_ID , bdata.TARGET_PATH , bdata.TARGET_PLATFORM );
		builder.setRemoteData( bdata.REMOTE , bdata.REMOTE_OS_TYPE , bdata.REMOTE_HOSTLOGIN , bdata.REMOTE_PORT , bdata.REMOTE_AUTH_RESOURCE_ID );
		modifyBuilder( c , builder , true );
		
		builders.addBuilder( builder );
		return( builder );
	}
	
	public static void modifyBuilder( EngineTransaction transaction , EngineBuilders builders , ProjectBuilder builder , ProjectBuilder bdata ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		builder.modifyBuilder( bdata.NAME , bdata.DESC , bdata.VERSION );
		builder.setMethodData( bdata.BUILDER_METHOD_TYPE , bdata.BUILDER_COMMAND , bdata.BUILDER_HOMEPATH , bdata.BUILDER_OPTIONS , bdata.JAVA_JDKHOMEPATH );
		builder.setTargetData( bdata.BUILDER_TARGET_TYPE , bdata.TARGET_RESOURCE_ID , bdata.TARGET_PATH , bdata.TARGET_PLATFORM );
		builder.setRemoteData( bdata.REMOTE , bdata.REMOTE_OS_TYPE , bdata.REMOTE_HOSTLOGIN , bdata.REMOTE_PORT , bdata.REMOTE_AUTH_RESOURCE_ID );
		modifyBuilder( c , builder , false );
		
		builders.updateBuilder( builder );
	}
	
	public static void deleteBuilder( EngineTransaction transaction , EngineBuilders builders , ProjectBuilder builder ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
			
		DBEngineEntities.deleteAppObject( c , entities.entityAppProjectBuilder , builder.ID , c.getNextCoreVersion() );
		builders.removeBuilder( builder );
		builder.deleteObject();
	}

}
