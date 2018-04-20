package org.urm.db.product;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Node;

public class DBMeta {

	public static void createdb( EngineLoader loader , AppProduct product , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		
		modifyMeta( c , product , storage , true );
	}
	
	public static void copydb( TransactionBase transaction , AppProduct product , ProductMeta src , ProductMeta dst ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		modifyMeta( c , product , dst , true );
	}
	
	public static void importxml( EngineLoader loader , AppProduct product , ProductMeta storage , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		
		modifyMeta( c , product , storage , true );
	}

	private static void modifyMeta( DBConnection c , AppProduct product , ProductMeta storage , boolean insert ) throws Exception {
		if( insert )
			storage.ID = DBNames.getNameIndex( c , product.ID , storage.REVISION , DBEnumParamEntityType.PRODUCT );
		else
			DBNames.updateName( c , product.ID , storage.REVISION , storage.ID , DBEnumParamEntityType.PRODUCT );
		
		storage.PV = c.getNextProductVersion( storage );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMeta , storage.ID , storage.PV , new String[] {
				EngineDB.getInteger( product.ID ) ,
				EngineDB.getString( storage.NAME ) ,
				EngineDB.getString( storage.REVISION ) ,
				EngineDB.getBoolean( storage.DRAFT ) ,
				EngineDB.getDate( storage.SAVEDATE ) ,
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

	public static ProductMeta[] loaddbMeta( EngineLoader loader , EngineProduct ep ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMeta;
		List<ProductMeta> products = new LinkedList<ProductMeta>();
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_NAME1 , new String[] { EngineDB.getString( ep.productName ) } );
		try {
			while( rs.next() ) {
				ProductMeta meta = new ProductMeta( ep );
				meta.ID = entity.loaddbId( rs );
				meta.PV = entity.loaddbVersion( rs );
				meta.create(
						entity.loaddbString( rs , DBProductData.FIELD_META_PRODUCT_REVISION ) ,
						entity.loaddbBoolean( rs , DBProductData.FIELD_META_PRODUCT_DRAFT ) ,
						entity.loaddbDate( rs , DBProductData.FIELD_META_PRODUCT_SAVEDATE ) ,
						entity.loaddbBoolean( rs , DBProductData.FIELD_META_PRODUCT_MATCHED )
						);
				products.add( meta );
			}
		}
		finally {
			c.closeQuery();
		}
		
		return( products.toArray( new ProductMeta[0] ) );
	}

	public static void renameRevision( EngineTransaction transaction , ProductMeta storage , String name ) throws Exception {
		DBConnection c = transaction.getConnection();
		AppProduct product = storage.getProduct();
		EngineProductRevisions revisions = product.findRevisions();
		
		ProductMeta metaOther = revisions.findRevision( name );
		if( metaOther != null && metaOther != storage )
			Common.exitUnexpected();
		
		storage.setRevision( name );
		modifyMeta( c , product , storage , false );
	}

	public static void saveRevision( EngineTransaction transaction , ProductMeta storage ) throws Exception {
		DBConnection c = transaction.getConnection();
		AppProduct product = storage.getProduct();
		if( !storage.isDraft() )
			Common.exitUnexpected();
		
		storage.setDraft( false );
		modifyMeta( c , product , storage , false );
	}
	
}
