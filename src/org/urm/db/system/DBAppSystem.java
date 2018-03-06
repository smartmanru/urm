package org.urm.db.system;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.db.DBQueries;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBAppSystem {

	public static AppSystem importxmlSystem( EngineLoader loader , EngineDirectory directory , Node node ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineSettings settings = loader.getSettings();
		ObjectProperties props = entities.createSystemProps( settings.getEngineProperties() );
		
		AppSystem system = new AppSystem( directory , props );
		system.createSystem(
				ConfReader.getAttrValue( node , AppSystem.PROPERTY_NAME ) ,
				ConfReader.getAttrValue( node , AppSystem.PROPERTY_DESC ) 
				);
		system.setOffline( ConfReader.getBooleanAttrValue( node , AppSystem.PROPERTY_OFFLINE , true ) );
		modifySystem( c , system , true );
		props.setOwnerId( system.ID );
		DBSettings.importxml( loader , node , props , false , true , system.SV );
		
		return( system );
	}

	public static void exportxml( EngineLoader loader , EngineDirectory directory , AppSystem system , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , system.NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , system.DESC );
		Common.xmlSetElementAttr( doc , root , "offline" , Common.getBooleanValue( system.OFFLINE ) );
	}
	
	public static AppSystem[] loaddb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		DBConnection c = loader.getConnection();
		List<AppSystem> systems = new LinkedList<AppSystem>();
		
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppDirectorySystem;
		EngineSettings settings = loader.getSettings();
		ObjectProperties engineProps = settings.getEngineProperties();
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				ObjectProperties props = entities.createSystemProps( engineProps );
				
				AppSystem system = new AppSystem( directory , props );
				system.ID = entity.loaddbId( rs );
				system.SV = entity.loaddbVersion( rs );
				system.createSystem(
						entity.loaddbString( rs , AppSystem.PROPERTY_NAME ) , 
						entity.loaddbString( rs , AppSystem.PROPERTY_DESC ) 
						);
				system.setOffline( entity.loaddbBoolean( rs , AppSystem.PROPERTY_OFFLINE ) );
				system.setMatched( entity.loaddbBoolean( rs , AppSystem.PROPERTY_MATCHED ) );
				
				props.setOwnerId( system.ID );
				systems.add( system );
			}
		}
		finally {
			c.closeQuery();
		}

		for( AppSystem system : systems ) {
			ObjectProperties props = system.getParameters();
			DBSettings.loaddbCustomEntity( c , props );
			props.createCustom();
			DBSettings.loaddbValues( loader , props );
		}
		
		return( systems.toArray( new AppSystem[0] ) );
	}
	
	public static void modifySystem( DBConnection c , AppSystem system , boolean insert ) throws Exception {
		if( insert )
			system.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , system.NAME , DBEnumObjectType.APPSYSTEM );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , system.NAME , system.ID , DBEnumObjectType.APPSYSTEM );
		
		system.SV = c.getNextSystemVersion( system );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppDirectorySystem , system.ID , system.SV , new String[] {
				EngineDB.getString( system.NAME ) , 
				EngineDB.getString( system.DESC ) ,
				EngineDB.getBoolean( system.OFFLINE ) ,
				EngineDB.getBoolean( system.MATCHED )
				} , insert );
	}
	
	public static void delete( DBConnection c , AppSystem system ) throws Exception {
		int SV = c.getNextSystemVersion( system , true );
		DBSettings.dropObjectSettings( c , system.ID );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.deleteAppObject( c , entities.entityAppDirectorySystem , system.ID , SV );
	}

	public static void matchSystem( EngineLoader loader , EngineDirectory directory , AppSystem system , boolean update ) throws Exception {
		if( update )
			matchdone( loader , directory , system );
	}

	private static void matchdone( EngineLoader loader , EngineDirectory directory , AppSystem system ) throws Exception {
		DBConnection c = loader.getConnection();
		
		system.SV = c.getNextSystemVersion( system );
		if( !c.modify( DBQueries.MODIFY_SYSTEM_MATCHED3 , new String[] {
				EngineDB.getInteger( system.ID ) , 
				EngineDB.getBoolean( system.MATCHED ) ,
				EngineDB.getInteger( system.SV ) 
				} ) )
			Common.exitUnexpected();
	}
	
	public static void updateCustomProperties( EngineTransaction transaction , AppSystem system ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ObjectProperties ops = system.getParameters();
		int version = c.getNextSystemVersion( system );
		DBSettings.savedbPropertyValues( c , ops , false , true , version );
		ops.recalculateChildProperties();
	}
	
}
