package org.urm.db.product;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums.DBEnumDbmsType;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaProductUnit;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaSources;
import org.w3c.dom.Node;

public class DBMetaSources {

	public static String ELEMENT_SET = "projectset";
	
	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		MetaSources sources = new MetaSources( storage , storage.meta );
		storage.setSources( sources );
	
		Node[] sets = ConfReader.xmlGetChildren( root , ELEMENT_SET );
		if( sets == null )
			return;
		
		for( Node node : sets ) {
			MetaSourceProjectSet projectset = importxmlProjectSet( loader , storage , sources , node );
			sources.addProjectSet( projectset );
		}
	}

	public static MetaSourceProjectSet importxmlProjectSet( EngineLoader loader , ProductMeta storage , MetaSources sources , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSourceSet;
		
		MetaSourceProjectSet set = new MetaSourceProjectSet( storage.meta , sources );
		schema.createSchema( 
				entity.importxmlStringAttr( node , MetaDatabaseSchema.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( node , MetaDatabaseSchema.PROPERTY_DESC ) ,
				DBEnumDbmsType.getValue( entity.importxmlEnumAttr( node , MetaDatabaseSchema.PROPERTY_DBTYPE ) , true ) ,
				entity.importxmlStringAttr( node , MetaDatabaseSchema.PROPERTY_DBNAME ) ,
				entity.importxmlStringAttr( node , MetaDatabaseSchema.PROPERTY_DBUSER )
				);
		
		modifySchema( c , storage , schema , true );
		return( schema );
	}
	
	public static void deleteUnit( EngineTransaction transaction , ProductMeta storage , MetaSources sources , MetaProductUnit unit ) throws Exception {
	}
	
}
