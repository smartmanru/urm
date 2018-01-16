package org.urm.db.product;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductContext;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.w3c.dom.Node;

public class DBMeta {

	public static ProductContext[] getProducts( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMeta;
		List<ProductContext> products = new LinkedList<ProductContext>();
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				ProductContext context = new ProductContext(
						entity.loaddbId( rs ) ,
						entity.loaddbInt( rs , DBProductData.FIELD_META_PRODUCT_ID ) ,
						entity.loaddbString( rs , DBProductData.FIELD_META_PRODUCT_NAME ) ,
						entity.loaddbBoolean( rs , DBProductData.FIELD_META_PRODUCT_MATCHED ) ,
						entity.loaddbVersion( rs )
						);
				products.add( context );
			}
		}
		finally {
			c.closeQuery();
		}
		
		return( products.toArray( new ProductContext[0] ) );
	}

	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaVersion;
		
		MetaProductVersion version = new MetaProductVersion( storage , storage.meta );
		storage.setVersion( version );
		
		version.createVersion( 
				entity.importxmlIntProperty( root , MetaProductSettings.PROPERTY_LAST_MAJOR_FIRST , 1 ) ,
				entity.importxmlIntProperty( root , MetaProductSettings.PROPERTY_LAST_MAJOR_SECOND , 0 ) ,
				entity.importxmlIntProperty( root , MetaProductSettings.PROPERTY_LAST_MINOR_FIRST , 0 ) ,
				entity.importxmlIntProperty( root , MetaProductSettings.PROPERTY_LAST_MINOR_SECOND , 0 ) ,
				entity.importxmlIntProperty( root , MetaProductSettings.PROPERTY_NEXT_MAJOR_FIRST , 1 ) ,
				entity.importxmlIntProperty( root , MetaProductSettings.PROPERTY_NEXT_MAJOR_SECOND , 1 ) ,
				entity.importxmlIntProperty( root , MetaProductSettings.PROPERTY_NEXT_MINOR_FIRST , 1 ) ,
				entity.importxmlIntProperty( root , MetaProductSettings.PROPERTY_NEXT_MINOR_SECOND , 1 )
				);
		
		modifyMeta( c , storage , version , true );
	}
	
	private static void modifyMeta( DBConnection c , ProductMeta storage , MetaProductVersion version , boolean insert ) throws Exception {
		if( insert )
			storage.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , storage.name , DBEnumObjectType.META );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , storage.name , storage.ID , DBEnumObjectType.META );
		
		storage.PV = c.getNextProductVersion( storage.product );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMeta , storage.ID , storage.PV , new String[] {
				EngineDB.getInteger( storage.product.ID ) ,
				EngineDB.getString( null ) ,
				EngineDB.getBoolean( storage.MATCHED ) ,
				EngineDB.getInteger( version.majorLastFirstNumber ) ,
				EngineDB.getInteger( version.majorLastSecondNumber ) ,
				EngineDB.getInteger( version.lastProdTag ) ,
				EngineDB.getInteger( version.lastUrgentTag ) ,
				EngineDB.getInteger( version.majorNextFirstNumber ) ,
				EngineDB.getInteger( version.majorNextSecondNumber ) ,
				EngineDB.getInteger( version.nextProdTag ) ,
				EngineDB.getInteger( version.nextUrgentTag )
				} , insert );
	}

	public static void loaddb( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMeta;
		
		ResultSet rs = DBEngineEntities.listSingleAppObject( c , entity , storage.ID );
		try {
			MetaProductVersion version = new MetaProductVersion( storage , storage.meta );
			storage.setVersion( version );
			
			version.createVersion( 
					entity.loaddbInt( rs , DBProductData.FIELD_META_LAST_MAJOR1 ) ,
					entity.loaddbInt( rs , DBProductData.FIELD_META_LAST_MAJOR2 ) ,
					entity.loaddbInt( rs , DBProductData.FIELD_META_LAST_MINOR1 ) ,
					entity.loaddbInt( rs , DBProductData.FIELD_META_LAST_MINOR2 ) ,
					entity.loaddbInt( rs , DBProductData.FIELD_META_NEXT_MAJOR1 ) ,
					entity.loaddbInt( rs , DBProductData.FIELD_META_NEXT_MAJOR2 ) ,
					entity.loaddbInt( rs , DBProductData.FIELD_META_NEXT_MINOR1 ) ,
					entity.loaddbInt( rs , DBProductData.FIELD_META_NEXT_MINOR2 )
					);
		}
		finally {
			c.closeQuery();
		}
	}
	
}
