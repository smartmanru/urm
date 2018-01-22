package org.urm.db.product;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaProductDoc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaDocs {

	public static String ELEMENT_DOCUMENT = "document";
	
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
				entity.importxmlStringAttr( root , MetaProductDoc.PROPERTY_EXT ) ,
				entity.importxmlBooleanAttr( root , MetaProductDoc.PROPERTY_UNITBOUND , false )
				);
		
		modifyDoc( c , storage , doc , true );
		return( doc );
	}
	
	private static void modifyDoc( DBConnection c , ProductMeta storage , MetaProductDoc doc , boolean insert ) throws Exception {
		if( insert )
			doc.ID = DBNames.getNameIndex( c , storage.ID , doc.NAME , DBEnumObjectType.META_DOC );
		else
			DBNames.updateName( c , storage.ID , doc.NAME , doc.ID , DBEnumObjectType.META_DOC );
		
		doc.PV = c.getNextProductVersion( storage.product );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaDoc , doc.ID , doc.PV , new String[] {
				EngineDB.getInteger( storage.ID ) , 
				EngineDB.getString( doc.NAME ) ,
				EngineDB.getString( doc.DESC ) ,
				EngineDB.getString( doc.EXT ) ,
				EngineDB.getBoolean( doc.UNITBOUND )
				} , insert );
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , MetaDocs docs , Document doc , Element root ) throws Exception {
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
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				MetaProductDoc doc = new MetaProductDoc( storage.meta , docs );
				doc.ID = entity.loaddbId( rs );
				doc.PV = entity.loaddbVersion( rs );
				doc.createDoc( 
						entity.loaddbString( rs , MetaProductDoc.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaProductDoc.PROPERTY_DESC ) ,
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

	public static MetaProductDoc createDoc( EngineTransaction transaction , ProductMeta storage , MetaDocs docs , 
			String name , String desc , String ext , boolean unitbound ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( docs.findDoc( name ) != null )
			transaction.exitUnexpectedState();
		
		MetaProductDoc doc = new MetaProductDoc( storage.meta , docs );
		doc.createDoc( name , desc , ext , unitbound );
		modifyDoc( c , storage , doc , true );
		
		docs.addDoc( doc );
		return( doc );
	}

	public static void modifyDoc( EngineTransaction transaction , ProductMeta storage , MetaDocs docs , MetaProductDoc doc , 
			String name , String desc , String ext , boolean unitbound ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		doc.modifyDoc( name , desc , ext , unitbound );
		modifyDoc( c , storage , doc , false );
		
		docs.updateDoc( doc );
	}
	
	public static void deleteDoc( EngineTransaction transaction , ProductMeta storage , MetaDocs docs , MetaProductDoc doc ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		MetaDistr distr = storage.getDistr();
		DBMetaDistr.deleteDocument( transaction , storage , distr , doc );
		
		DBEngineEntities.deleteAppObject( c , entities.entityAppMetaDoc , doc.ID , c.getNextProductVersion( storage.product ) );
		docs.removeDoc( doc );
	}
	
}