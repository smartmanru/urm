package org.urm.db.system;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.EngineDB;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.AppProduct;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBProduct {

	public static AppProduct importxmlProduct( EngineLoader loader , EngineDirectory directory , AppSystem system , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDirectoryProduct;
		
		AppProduct product = new AppProduct( directory , system );
		product.createProduct(
				entity.importxmlStringAttr( root , AppProduct.PROPERTY_NAME ) , 
				entity.importxmlStringAttr( root , AppProduct.PROPERTY_DESC ) , 
				entity.importxmlStringAttr( root , AppProduct.PROPERTY_PATH )
				);
		product.setOffline( entity.importxmlBooleanAttr( root , AppProduct.PROPERTY_OFFLINE , true ) );
		product.setMonitoringEnabled( entity.importxmlBooleanAttr( root , AppProduct.PROPERTY_MONITORING_ENABLED , false ) );
		modifyProduct( c , product , true );
		
		return( product );
	}
	
	public static void exportxmlProduct( EngineLoader loader , AppProduct product , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDirectoryProduct;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( product.NAME ) ,
				entity.exportxmlString( product.DESC ) ,
				entity.exportxmlString( product.PATH ) ,
				entity.exportxmlBoolean( product.OFFLINE ) ,
				entity.exportxmlBoolean( product.MONITORING_ENABLED )
		} , true );
	}

	public static AppProduct[] loaddb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppDirectoryProduct;
		List<AppProduct> products = new LinkedList<AppProduct>();
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				int systemId = entity.loaddbInt( rs , DBEngineDirectory.FIELD_PRODUCT_SYSTEM_ID );
				AppSystem system = directory.getSystem( systemId );
				
				AppProduct product = new AppProduct( directory , system );
				product.ID = entity.loaddbId( rs );
				product.SV = entity.loaddbVersion( rs );
				product.createProduct( 
						entity.loaddbString( rs , AppProduct.PROPERTY_NAME ) , 
						entity.loaddbString( rs , AppProduct.PROPERTY_DESC ) ,
						entity.loaddbString( rs , AppProduct.PROPERTY_PATH )
						);
				product.setOffline( entity.loaddbBoolean( rs , AppProduct.PROPERTY_OFFLINE ) );
				product.setMonitoringEnabled( entity.loaddbBoolean( rs , AppProduct.PROPERTY_MONITORING_ENABLED ) );
				
				products.add( product );
			}
		}
		finally {
			c.closeQuery();
		}
		
		return( products.toArray( new AppProduct[0] ) );
	}
	
	public static void matchxml( EngineLoader loader , EngineDirectory directory , AppProduct product ) throws Exception {
	}
	
	public static void matchdb( EngineLoader loader , EngineDirectory directory , AppProduct product ) throws Exception {
	}
	
	public static void modifyProduct( DBConnection c , AppProduct product , boolean insert ) throws Exception {
		if( insert )
			product.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , product.NAME , DBEnumObjectType.APPPRODUCT );
		product.SV = c.getNextSystemVersion( product.system );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppBaseGroup , product.ID , product.SV , new String[] {
				EngineDB.getObject( product.system.ID ) ,
				EngineDB.getString( product.DESC ) , 
				EngineDB.getString( product.PATH ) ,
				EngineDB.getBoolean( product.OFFLINE ) ,
				EngineDB.getBoolean( product.MONITORING_ENABLED )
				} , insert );
			Common.exitUnexpected();
	}

}
