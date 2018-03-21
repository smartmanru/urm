package org.urm.db.product;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.db.env.DBMetaEnv;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaDatabase {

	public static String ELEMENT_SCHEMA = "schema";
	public static String ELEMENT_ADMINISTRATION = "administration";
	
	public static void createdb( EngineLoader loader , ProductMeta storage ) throws Exception {
		MetaDatabase database = new MetaDatabase( storage , storage.meta );
		storage.setDatabase( database );
	}

	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		MetaDatabase database = new MetaDatabase( storage , storage.meta );
		storage.setDatabase( database );
	
		importxmlAdministration( loader , storage , database , root );
		importxmlSchemaSet( loader , storage , database , root );
	}

	private static void importxmlAdministration( EngineLoader loader , ProductMeta storage , MetaDatabase database , Node node ) throws Exception {
	}

	private static void importxmlSchemaSet( EngineLoader loader , ProductMeta storage , MetaDatabase database , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_SCHEMA );
		if( items == null )
			return;
		
		for( Node schemaNode : items ) {
			MetaDatabaseSchema item = importxmlSchema( loader , storage , database , schemaNode );
			database.addSchema( item );
		}
	}

	public static MetaDatabaseSchema importxmlSchema( EngineLoader loader , ProductMeta storage , MetaDatabase database , Node node ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSchema;
		
		MetaDatabaseSchema schema = new MetaDatabaseSchema( storage.meta , database );
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

	private static void modifySchema( DBConnection c , ProductMeta storage , MetaDatabaseSchema schema , boolean insert ) throws Exception {
		if( insert )
			schema.ID = DBNames.getNameIndex( c , storage.ID , schema.NAME , DBEnumParamEntityType.PRODUCT_SCHEMA );
		else
			DBNames.updateName( c , storage.ID , schema.NAME , schema.ID , DBEnumParamEntityType.PRODUCT_SCHEMA );
		
		schema.PV = c.getNextProductVersion( storage );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaSchema , schema.ID , schema.PV , new String[] {
				EngineDB.getInteger( storage.ID ) , 
				EngineDB.getString( schema.NAME ) ,
				EngineDB.getString( schema.DESC ) ,
				EngineDB.getEnum( schema.DBMS_TYPE ) ,
				EngineDB.getString( schema.DBNAMEDEF ) ,
				EngineDB.getString( schema.DBUSERDEF )
				} , insert );
	}

	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		MetaDatabase database = storage.getDatabase();
		
		exportxmlAdministration( loader , storage , database , doc , root );
		exportxmlSchemaSet( loader , storage , database , doc , root );
	}

	private static void exportxmlAdministration( EngineLoader loader , ProductMeta storage , MetaDatabase database , Document doc , Element root ) throws Exception {
		Common.xmlCreateElement( doc , root , ELEMENT_ADMINISTRATION );
	}
	
	private static void exportxmlSchemaSet( EngineLoader loader , ProductMeta storage , MetaDatabase database , Document doc , Element root ) throws Exception {
		for( String name : database.getSchemaNames() ) {
			MetaDatabaseSchema schema = database.findSchema( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_SCHEMA );
			exportxmlSchema( loader , storage , schema , doc , node );
		}
	}

	private static void exportxmlSchema( EngineLoader loader , ProductMeta storage , MetaDatabaseSchema schema , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaSchema;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( schema.NAME ) ,
				entity.exportxmlString( schema.DESC ) ,
				entity.exportxmlEnum( schema.DBMS_TYPE ) ,
				entity.exportxmlString( schema.DBNAMEDEF ) ,
				entity.exportxmlString( schema.DBUSERDEF )
		} , true );
	}

	public static void loaddb( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaSchema;

		MetaDatabase database = new MetaDatabase( storage , storage.meta );
		storage.setDatabase( database );
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		try {
			while( rs.next() ) {
				MetaDatabaseSchema schema = new MetaDatabaseSchema( storage.meta , database );
				schema.ID = entity.loaddbId( rs );
				schema.PV = entity.loaddbVersion( rs );
				schema.createSchema( 
						entity.loaddbString( rs , MetaDatabaseSchema.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaDatabaseSchema.PROPERTY_DESC ) ,
						DBEnumDbmsType.getValue( entity.loaddbEnum( rs , MetaDatabaseSchema.PROPERTY_DBTYPE ) , true ) ,
						entity.loaddbString( rs , MetaDatabaseSchema.PROPERTY_DBNAME ) ,
						entity.loaddbString( rs , MetaDatabaseSchema.PROPERTY_DBUSER )
						);
				database.addSchema( schema );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static MetaDatabaseSchema createSchema( EngineTransaction transaction , ProductMeta storage , MetaDatabase database , String name , String desc , DBEnumDbmsType type , String dbname , String dbuser ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( database.findSchema( name ) != null )
			transaction.exitUnexpectedState();
		
		MetaDatabaseSchema schema = new MetaDatabaseSchema( storage.meta , database );
		schema.createSchema( name , desc , type , dbname , dbuser );
		modifySchema( c , storage , schema , true );
		
		database.addSchema( schema );
		return( schema );
	}
	
	public static void modifySchema( EngineTransaction transaction , ProductMeta storage , MetaDatabase database , MetaDatabaseSchema schema , String name , String desc , DBEnumDbmsType type , String dbname , String dbuser ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		schema.modifySchema( name , desc , type , dbname , dbuser );
		modifySchema( c , storage , schema , false );
		
		database.updateSchema( schema );
	}
	
	public static void deleteSchema( EngineTransaction transaction , ProductMeta storage , MetaDatabase database , MetaDatabaseSchema schema ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();

		DBMetaEnv.deleteDatabaseSchema( transaction , storage , schema );
		
		MetaDistr distr = schema.meta.getDistr();
		DBMetaDistr.deleteDatabaseSchema( transaction , storage , distr , schema );
		
		DBEngineEntities.deleteAppObject( c , entities.entityAppMetaSchema , schema.ID , c.getNextProductVersion( storage ) );
		database.removeSchema( schema );
	}

}
