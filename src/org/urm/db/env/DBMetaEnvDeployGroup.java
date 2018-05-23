package org.urm.db.env;

import java.sql.ResultSet;

import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvDeployGroup;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvDeployGroup {

	public static MetaEnvDeployGroup importxml( EngineLoader loader , ProductMeta storage , MetaEnv env , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppEnvDeployGroup;
		
		MetaEnvDeployGroup dg = new MetaEnvDeployGroup( storage.meta , env );
		
		// primary
		dg.createGroup(
				entity.importxmlStringAttr( root , MetaEnvDeployGroup.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaEnvDeployGroup.PROPERTY_DESC )
				);
		modifyDeployGroup( c , storage , env , dg , true );
		
 		return( dg );
	}

	public static void exportxml( EngineLoader loader , ProductMeta storage , MetaEnv env , MetaEnvDeployGroup dg , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppEnvDeployGroup;
		
		// primary
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( dg.NAME ) ,
				entity.exportxmlString( dg.DESC )
		} , true );
	}
	
	private static void modifyDeployGroup( DBConnection c , ProductMeta storage , MetaEnv env , MetaEnvDeployGroup dg , boolean insert ) throws Exception {
		if( insert )
			dg.ID = DBNames.getNameIndex( c , env.ID , dg.NAME , DBEnumParamEntityType.ENV_DEPLOYGROUP );
		else
			DBNames.updateName( c , env.ID , dg.NAME , dg.ID , DBEnumParamEntityType.ENV_DEPLOYGROUP );
		
		dg.EV = c.getNextEnvironmentVersion( env );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppEnvDeployGroup , dg.ID , dg.EV , new String[] {
				EngineDB.getObject( env.ID ) ,
				EngineDB.getString( dg.NAME ) ,
				EngineDB.getString( dg.DESC )
				} , insert );
	}
	
	public static void loaddb( EngineLoader loader , ProductMeta storage , MetaEnv env ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppEnvDeployGroup;
		
		// load segments
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_ENV_ID1 , new String[] { EngineDB.getInteger( env.ID ) } );
		try {
			while( rs.next() ) {
				MetaEnvDeployGroup dg = new MetaEnvDeployGroup( storage.meta , env );
				dg.ID = entity.loaddbId( rs );
				dg.EV = entity.loaddbVersion( rs );
				
				dg.createGroup(
						entity.loaddbString( rs , MetaEnvDeployGroup.PROPERTY_NAME ) ,
						entity.loaddbString( rs , MetaEnvDeployGroup.PROPERTY_DESC )
						);
				
				env.addDeployGroup( dg );
			}
		}
		finally {
			c.closeQuery();
		}
	}	

	public static MetaEnvDeployGroup createDeployGroup( EngineTransaction transaction , ProductMeta storage , MetaEnv env , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( env.findDeployGroup( name ) != null )
			transaction.exitUnexpectedState();
		
		MetaEnvDeployGroup dg = new MetaEnvDeployGroup( storage.meta , env );
		dg.createGroup( name , desc );
		
		modifyDeployGroup( c , storage , env , dg , true );
		env.addDeployGroup( dg );
		return( dg );
	}

	public static void modifyDeployGroup( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvDeployGroup dg , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		dg.modifyGroup( name , desc );
		
		modifyDeployGroup( c , storage , env , dg , false );
		env.updateDeployGroup( dg );
	}

	public static void deleteDeployGroup( EngineTransaction transaction , ProductMeta storage , MetaEnv env , MetaEnvDeployGroup dg ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppEnvDeployGroup;
		
		int version = c.getNextEnvironmentVersion( env );
		DBEngineEntities.deleteAppObject( c , entity , dg.ID , version);
		
		env.removeDeployGroup( dg );
	}
	
}
