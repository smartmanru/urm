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
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.loader.Types.EnumModifyType;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaDocs {

	public static String ELEMENT_DOCUMENT = "document";

	public static void createdb( EngineLoader loader , ProductMeta storage ) throws Exception {
		MetaDocs docs = new MetaDocs( storage , storage.meta );
		storage.setDocs( docs );
	}
	
	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		MetaDocs docs = new MetaDocs( storage , storage.meta );
		storage.setDocs( docs );
		
		if( root != null )
			importxmlDocSet( loader , storage , docs , root );
	}
	
	private static void importxmlDocSet( EngineLoader loader , ProductMeta storage , MetaDocs docs , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_DOCUMENT );
		if( items == null )
			return;
		
		for( Node docNode : items ) {
			MetaProductDoc item = importxmlDoc( loader , storage , docs , docNode );
			docs.addDoc( item );
		}
	}

	public static MetaProductDoc importxmlDoc( EngineLoader loader , ProductMeta storage , MetaDocs docs , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDoc;
		
		MetaProductDoc doc = new MetaProductDoc( storage.meta , docs );
		doc.createDoc( 
				entity.importxmlStringAttr( root , MetaProductDoc.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaProductDoc.PROPERTY_DESC ) ,
				DBEnumDocCategoryType.getValue( entity.importxmlEnumAttr( root , MetaProductDoc.PROPERTY_CATEGORY ) , true ) ,
				entity.importxmlStringAttr( root , MetaProductDoc.PROPERTY_EXT ) ,
				entity.importxmlBooleanAttr( root , MetaProductDoc.PROPERTY_UNITBOUND , false )
				);
		
		modifyDoc( c , storage , doc , true , EnumModifyType.ORIGINAL );
		return( doc );
	}
	
	private static void modifyDoc( DBConnection c , ProductMeta storage , MetaProductDoc doc , boolean insert , EnumModifyType type ) throws Exception {
		if( insert )
			doc.ID = DBNames.getNameIndex( c , storage.ID , doc.NAME , DBEnumParamEntityType.PRODUCT_DOC );
		else
			DBNames.updateName( c , storage.ID , doc.NAME , doc.ID , DBEnumParamEntityType.PRODUCT_DOC );
		
		doc.PV = c.getNextProductVersion( storage );
		doc.CHANGETYPE = EngineDB.getChangeModify( insert , doc.CHANGETYPE , type );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaDoc , doc.ID , doc.PV , new String[] {
				EngineDB.getInteger( storage.ID ) , 
				EngineDB.getString( doc.NAME ) ,
				EngineDB.getString( doc.DESC ) ,
				EngineDB.getEnum( doc.DOC_CATEGORY ) ,
				EngineDB.getString( doc.EXT ) ,
				EngineDB.getBoolean( doc.UNITBOUND )
				} , insert , doc.CHANGETYPE );
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		MetaDocs docs = storage.getDocs();
		
		for( String name : docs.getDocNames() ) {
			MetaProductDoc pdoc = docs.findDoc( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DOCUMENT );
			exportxmlDoc( loader , storage , pdoc , doc , node );
		}
	}

	public static void exportxmlDoc( EngineLoader loader , ProductMeta storage , MetaProductDoc pdoc , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDoc;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( pdoc.NAME ) ,
				entity.exportxmlString( pdoc.DESC ) ,
				entity.exportxmlEnum( pdoc.DOC_CATEGORY ) ,
				entity.exportxmlString( pdoc.EXT ) ,
				entity.exportxmlBoolean( pdoc.UNITBOUND )
		} , true );
	}

	public static void loaddb( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaDoc;

		MetaDocs docs = new MetaDocs( storage , storage.meta );
		storage.setDocs( docs );
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		try {
			while( rs.next() ) {
				MetaProductDoc doc = new MetaProductDoc( storage.meta , docs );
				doc.ID = entity.loaddbId( rs );
				doc.PV = entity.loaddbVersion( rs );
				doc.CHANGETYPE = entity.loaddbChangeType( rs );
				doc.createDoc( 
						entity.loaddbString( rs , MetaProductDoc.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaProductDoc.PROPERTY_DESC ) ,
						DBEnumDocCategoryType.getValue( entity.loaddbEnum( rs , MetaProductDoc.PROPERTY_CATEGORY ) , true ) ,
						entity.loaddbString( rs , MetaProductDoc.PROPERTY_EXT ) ,
						entity.loaddbBoolean( rs , MetaProductDoc.PROPERTY_UNITBOUND )
						);
				docs.addDoc( doc );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void copydb( TransactionBase transaction , ProductMeta src , ProductMeta dst ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaDocs docsSrc = src.getDocs();
		MetaDocs docs = new MetaDocs( dst , dst.meta );
		dst.setDocs( docs );
		for( MetaProductDoc docSrc : docsSrc.getDocList() ) {
			MetaProductDoc doc = docSrc.copy( dst.meta , docs );
			modifyDoc( c , dst , doc , true , EnumModifyType.ORIGINAL );
			docs.addDoc( doc );
		}
	}
	
	public static MetaProductDoc createDoc( EngineTransaction transaction , ProductMeta storage , MetaDocs docs , 
			String name , String desc , DBEnumDocCategoryType category , String ext , boolean unitbound ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( docs.findDoc( name ) != null )
			transaction.exitUnexpectedState();
		
		MetaProductDoc doc = new MetaProductDoc( storage.meta , docs );
		doc.createDoc( name , desc , category , ext , unitbound );
		modifyDoc( c , storage , doc , true , EnumModifyType.NORMAL );
		
		docs.addDoc( doc );
		return( doc );
	}

	public static void modifyDoc( EngineTransaction transaction , ProductMeta storage , MetaDocs docs , MetaProductDoc doc , 
			String name , String desc , DBEnumDocCategoryType category , String ext , boolean unitbound ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		doc.modifyDoc( name , desc , category , ext , unitbound );
		modifyDoc( c , storage , doc , false , EnumModifyType.NORMAL );
		
		docs.updateDoc( doc );
	}
	
	public static void deleteDoc( EngineTransaction transaction , ProductMeta storage , MetaDocs docs , MetaProductDoc doc ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		MetaDistr distr = storage.getDistr();
		DBMetaDistr.deleteDocument( transaction , storage , distr , doc );
		
		doc.CHANGETYPE = EngineDB.getChangeDelete( doc.CHANGETYPE );
		DBEngineEntities.deleteAppObject( c , entities.entityAppMetaDoc , doc.ID , c.getNextProductVersion( storage ) , doc.CHANGETYPE );
		if( doc.CHANGETYPE == null )
			docs.removeDoc( doc );
	}

}
