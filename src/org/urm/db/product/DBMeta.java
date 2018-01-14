package org.urm.db.product;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.db.DBConnection;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductContext;

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
	
}
