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
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.EngineLoader;
import org.urm.meta.product.ProductContext;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Node;

public class DBMeta {

	public static void createdb( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		
		modifyMeta( c , storage , true );
	}
	
	public static void copydb( TransactionBase transaction , ProductMeta src , ProductMeta dst ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		modifyMeta( c , dst , true );
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
		
		modifyMeta( c , storage , true );
	}

	private static void modifyMeta( DBConnection c , ProductMeta storage , boolean insert ) throws Exception {
		if( insert )
			storage.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , storage.name , DBEnumParamEntityType.PRODUCT );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , storage.name , storage.ID , DBEnumParamEntityType.PRODUCT );
		
		storage.PV = c.getNextProductVersion( storage );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMeta , storage.ID , storage.PV , new String[] {
				EngineDB.getInteger( storage.product.ID ) ,
				EngineDB.getString( null ) ,
				EngineDB.getBoolean( storage.MATCHED )
				} , insert );
	}

	public static void setMatched( EngineLoader loader , ProductMeta storage , boolean matched ) throws Exception {
		DBConnection c = loader.getConnection();
		if( !c.modify( DBQueries.MODIFY_META_SETSTATUS2 , new String[] { 
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getBoolean( matched )
				} ) )
			Common.exitUnexpected();
	}

}
