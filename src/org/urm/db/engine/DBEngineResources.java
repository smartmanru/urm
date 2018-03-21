package org.urm.db.engine;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.core.DBEnums.DBEnumResourceType;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineResources;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine._Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBEngineResources {

	public static String ELEMENT_RESOURCE = "resource";
	public static String TABLE_RESOURCE = "urm_resource";
	public static String FIELD_RESOURCE_ID = "resource_id";
	public static String FIELD_RESOURCE_DESC = "xdesc";
	public static String XMLPROP_RESOURCE_TYPE = "resource_type";
	
	public static PropertyEntity upgradeEntityResource( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RESOURCE , DBEnumParamEntityType.RESOURCE , DBEnumObjectVersionType.CORE , TABLE_RESOURCE , FIELD_RESOURCE_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( AuthResource.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( AuthResource.PROPERTY_DESC , FIELD_RESOURCE_DESC , AuthResource.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( AuthResource.PROPERTY_RESOURCE_TYPE , XMLPROP_RESOURCE_TYPE , AuthResource.PROPERTY_RESOURCE_TYPE , "Function type" , true , DBEnumResourceType.UNKNOWN ) ,
				EntityVar.metaString( AuthResource.PROPERTY_BASEURL , "Base URL" , false , null ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthResource.PROPERTY_VERIFIED , "Access verified" , false , false ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityResource( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RESOURCE , DBEnumParamEntityType.RESOURCE , DBEnumObjectVersionType.CORE , TABLE_RESOURCE , FIELD_RESOURCE_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static void importxml( EngineLoader loader , EngineResources resources , Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_RESOURCE );
		if( list != null ) {
			for( Node node : list ) {
				AuthResource rc = importxmlResource( loader , resources , node );
				resources.addResource( rc );
			}
		}
	}
	
	private static AuthResource importxmlResource( EngineLoader loader , EngineResources resources , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppResource;
		
		AuthResource rc = new AuthResource( resources );
		rc.createResource(
				entity.importxmlStringProperty( root , AuthResource.PROPERTY_NAME ) ,
				entity.importxmlStringProperty( root , AuthResource.PROPERTY_DESC ) ,
				DBEnumResourceType.getValue( entity.importxmlEnumProperty( root , AuthResource.PROPERTY_RESOURCE_TYPE ) , true ) ,
				entity.importxmlStringProperty( root , AuthResource.PROPERTY_BASEURL )
				);
		modifyResource( c , rc , true );
		
		return( rc );
	}
	
	private static void modifyResource( DBConnection c , AuthResource rc , boolean insert ) throws Exception {
		if( insert )
			rc.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , rc.NAME , DBEnumParamEntityType.RESOURCE );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , rc.NAME , rc.ID , DBEnumParamEntityType.RESOURCE );
		
		rc.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppResource , rc.ID , rc.CV , new String[] {
				EngineDB.getString( rc.NAME ) , 
				EngineDB.getString( rc.DESC ) ,
				EngineDB.getEnum( rc.RESOURCE_TYPE ) ,
				EngineDB.getString( rc.BASEURL ) ,
				EngineDB.getBoolean( rc.VERIFIED )
				} , insert );
	}

	public static void exportxml( EngineLoader loader , EngineResources resources , Document doc , Element root ) throws Exception {
		for( String name : resources.getResourceNames() ) {
			AuthResource rc = resources.findResource( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_RESOURCE );
			exportxmlResource( loader , rc , doc , node );
		}
	}
	
	public static void exportxmlResource( EngineLoader loader , AuthResource rc , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppResource;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( rc.NAME ) ,
				entity.exportxmlString( rc.DESC ) ,
				entity.exportxmlEnum( rc.RESOURCE_TYPE ) ,
				entity.exportxmlString( rc.BASEURL )
		} , false );
	}

	public static void loaddb( EngineLoader loader , EngineResources resources ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppResource;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				AuthResource rc = new AuthResource( resources );
				rc.ID = entity.loaddbId( rs );
				rc.CV = entity.loaddbVersion( rs );
				rc.createResource( 
						entity.loaddbString( rs , AuthResource.PROPERTY_NAME ) , 
						entity.loaddbString( rs , AuthResource.PROPERTY_DESC ) ,
						DBEnumResourceType.getValue( entity.loaddbEnum( rs , AuthResource.PROPERTY_RESOURCE_TYPE ) , true ) ,
						entity.loaddbString( rs , AuthResource.PROPERTY_BASEURL )
						);
				rc.setVerified( entity.loaddbBoolean( rs , AuthResource.PROPERTY_VERIFIED ) );
				resources.addResource( rc );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static AuthResource createResource( EngineTransaction transaction , EngineResources resources , AuthResource rcdata ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( resources.findResource( rcdata.NAME ) != null )
			transaction.exit1( _Error.DuplicateResource1 , "resource already exists name=" + rcdata.NAME , rcdata.NAME );
			
		AuthResource rc = new AuthResource( resources );
		rc.createResource( rcdata.NAME , rcdata.DESC , rcdata.RESOURCE_TYPE , rcdata.BASEURL );
		
		if( rcdata.ac != null ) {
			rc.setAuthData( rcdata.ac );
			rc.saveAuthData();
		}

		modifyResource( c , rc , true );
		
		resources.addResource( rc );
		return( rc );
	}
	
	public static void modifyResource( EngineTransaction transaction , EngineResources resources , AuthResource rc , AuthResource rcdata ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( rc.RESOURCE_TYPE != rcdata.RESOURCE_TYPE || rc.BASEURL.equals( rcdata.BASEURL ) )
			dropResourceMirrors( transaction , rc );
		
		rc.modifyResource( rcdata.NAME , rcdata.DESC , rcdata.RESOURCE_TYPE , rcdata.BASEURL );
		if( rcdata.ac != null ) {
			rc.setAuthData( rcdata.ac );
			rc.saveAuthData();
		}

		modifyResource( c , rc , false );
		
		resources.updateResource( rc );
	}
	
	public static void modifyResourceAuth( EngineTransaction transaction , EngineResources resources , AuthResource rc , AuthResource rcdata ) throws Exception {
		rc.setAuthData( rcdata.ac );
		rc.saveAuthData();
	}
	
	public static void verifyResource( EngineTransaction transaction , EngineResources resources , AuthResource rc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		rc.setVerified( true );
		modifyResource( c , rc , false );
	}
	
	public static void deleteResource( EngineTransaction transaction , EngineResources resources , AuthResource rc ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
			
		dropResourceMirrors( transaction , rc );
		
		DBEngineEntities.deleteAppObject( c , entities.entityAppResource , rc.ID , c.getNextCoreVersion() );
		resources.removeResource( rc );
		rc.deleteObject();
	}

	private static void dropResourceMirrors( EngineTransaction transaction , AuthResource rc ) throws Exception {
		if( !rc.isVCS() )
			return;
		
		ActionBase action = transaction.getAction();
		EngineMirrors mirrors = action.getServerMirrors();
		DBEngineMirrors.dropResourceMirrors( transaction , mirrors , rc );
	}
	
}
