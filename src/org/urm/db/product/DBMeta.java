package org.urm.db.product;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.ProductContext;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMeta {

	public static void createdb( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		
		MetaProductVersion version = new MetaProductVersion( storage , storage.meta );
		storage.setVersion( version );
		
		version.createVersion( 1 , 0 , 0 , 0 , 1 , 1 , 1 , 1 ); 
		modifyMeta( c , storage , version , true );
	}
	
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

	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaVersion;
		
		MetaProductVersion version = storage.getVersion();
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlInt( version.majorLastFirstNumber ) , 
				entity.exportxmlInt( version.majorLastSecondNumber) , 
				entity.exportxmlInt( version.lastProdTag ) , 
				entity.exportxmlInt( version.lastUrgentTag ) , 
				entity.exportxmlInt( version.majorNextFirstNumber ) , 
				entity.exportxmlInt( version.majorNextSecondNumber ) , 
				entity.exportxmlInt( version.nextProdTag ) , 
				entity.exportxmlInt( version.nextUrgentTag ) 
		} , false );
	}

	private static void modifyMeta( DBConnection c , ProductMeta storage , MetaProductVersion version , boolean insert ) throws Exception {
		if( insert )
			storage.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , storage.name , DBEnumParamEntityType.PRODUCT );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , storage.name , storage.ID , DBEnumParamEntityType.PRODUCT );
		
		storage.PV = c.getNextProductVersion( storage );
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

	public static void setMatched( EngineLoader loader , ProductMeta storage , boolean matched ) throws Exception {
		DBConnection c = loader.getConnection();
		if( !c.modify( DBQueries.MODIFY_META_SETSTATUS2 , new String[] { 
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getBoolean( matched )
				} ) )
			Common.exitUnexpected();
	}

	public static void modifyVersion( EngineTransaction transaction , ProductMeta storage , MetaProductVersion version , int majorFirstNumber , int majorSecondNumber , int lastProdTag , int lastUrgentTag , int majorNextFirstNumber , int majorNextSecondNumber , int nextProdTag , int nextUrgentTag ) throws Exception {
		DBConnection c = transaction.getConnection();
		version.modifyVersion( majorFirstNumber , majorSecondNumber , lastProdTag , lastUrgentTag , majorNextFirstNumber , majorNextSecondNumber , nextProdTag , nextUrgentTag );
		modifyMeta( c , storage , version , false );
		
		MetaProductSettings settings = storage.getSettings();
		settings.updateSettings( version );
	}
	
}
