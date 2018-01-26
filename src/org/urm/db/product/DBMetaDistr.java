package org.urm.db.product;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.core.DBNames;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.env.MetaEnvs;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaProductUnit;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaUnits;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaDistr {

	public static String ELEMENT_DELIVERIES = "deliveries"; 
	public static String ELEMENT_COMPONENTS = "components";
	public static String ELEMENT_DELIVERY = "delivery";
	public static String ELEMENT_COMPONENT = "component";
	public static String ELEMENT_BINARYITEM = "distitem";
	public static String ELEMENT_CONFITEM = "confitem";
	public static String ELEMENT_DATABASE = "schema";
	public static String ELEMENT_DOCUMENT = "document";
	public static String ELEMENT_COMPITEM_BINARYITEM = "distitem";
	public static String ELEMENT_COMPITEM_CONFITEM = "confitem";
	public static String ELEMENT_COMPITEM_SCHEMA = "schema";
	public static String ELEMENT_COMPITEM_WS = "webservice";
	public static String ATTR_DELIVERY_SCHEMA = "name";
	public static String ATTR_DELIVERY_DOCNAME = "name";
	
	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		MetaDistr distr = new MetaDistr( storage , storage.meta );
		storage.setDistr( distr );
	
		importxmlDeliveries( loader , storage , distr , ConfReader.xmlGetPathNode( root , ELEMENT_DELIVERIES ) );
		importxmlComponents( loader , storage , distr , ConfReader.xmlGetPathNode( root , ELEMENT_COMPONENTS ) );
	}

	private static void importxmlDeliveries( EngineLoader loader , ProductMeta storage , MetaDistr distr , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_DELIVERY );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaDistrDelivery delivery = importxmlDelivery( loader , storage , distr , node );
			distr.addDelivery( delivery );
		}
	}
	
	private static void importxmlComponents( EngineLoader loader , ProductMeta storage , MetaDistr distr , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_COMPONENT );
		if( items == null )
			return;
		
		for( Node node : items ) {
			MetaDistrComponent comp = importxmlComponent( loader , storage , distr , node );
			distr.addComponent( comp );
		}
	}
	
	private static MetaDistrDelivery importxmlDelivery( EngineLoader loader , ProductMeta storage , MetaDistr distr , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrDelivery;
		
		MetaUnits units = storage.getUnits();
		Integer unitId = units.getUnitId( entity.importxmlStringAttr( root , MetaDistrDelivery.PROPERTY_UNIT_NAME ) );
		
		MetaDistrDelivery delivery = new MetaDistrDelivery( storage.meta , distr , storage.getDatabase() , storage.getDocs() );
		delivery.createDelivery(
				unitId , 
				entity.importxmlStringAttr( root , MetaDistrDelivery.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaDistrDelivery.PROPERTY_DESC ) ,
				entity.importxmlStringAttr( root , MetaDistrDelivery.PROPERTY_FOLDER )
				);
		delivery.setDatabaseAll( entity.importxmlBooleanAttr( root , MetaDistrDelivery.PROPERTY_SCHEMA_ANY , false ) );
		delivery.setDocAll( entity.importxmlBooleanAttr( root , MetaDistrDelivery.PROPERTY_DOC_ANY , false ) );
		modifyDelivery( c , storage , delivery , true );
		
		importxmlDeliveryBinaryItems( loader , storage , distr , delivery , root );
		importxmlDeliveryConfItems( loader , storage , distr , delivery , root );
		importxmlDeliverySchemaItems( loader , storage , distr , delivery , root );
		importxmlDeliveryDocItems( loader , storage , distr , delivery , root );
		
		return( delivery );
	}
	
	private static void importxmlDeliveryBinaryItems( EngineLoader loader , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , Node root ) throws Exception {
		Node[] distitems = ConfReader.xmlGetChildren( root , ELEMENT_BINARYITEM );
		if( distitems == null )
			return;
		
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrBinaryItem;
		
		Map<String,Node> items = new HashMap<String,Node>();
		Map<String,String> deps = new HashMap<String,String>();
		
		List<Node> orderNode = new LinkedList<Node>();
		Map<String,Node> orderMap  = new HashMap<String,Node>();
		for( Node node : distitems ) {
			String name = entity.importxmlStringAttr( node , MetaDistrBinaryItem.PROPERTY_NAME );
			String srcname = entity.importxmlStringAttr( node , MetaDistrBinaryItem.PROPERTY_SRCDISTITEM_NAME );
			items.put( name , node );
			if( !srcname.isEmpty() )
				deps.put( name ,  srcname );
			else {
				orderNode.add( node );
				orderMap.put( name , node );
			}
		}
		
		while( true ) {
			boolean added = false;
			boolean pending = false;
			for( String name : deps.keySet() ) {
				String srcname = deps.get( name );
				if( !orderNode.contains( name ) ) {
					if( orderNode.contains( srcname ) ) {
						added = true;
						Node node = items.get( name );
						orderNode.add( node );
						orderMap.put( name , node );
					}
					else
						pending = true;
				}
			}
			
			if( pending == false )
				break;
			
			if( added == false )
				Common.exitUnexpected();
		}
		
		for( Node node : orderNode ) {
			MetaDistrBinaryItem item = importxmlBinaryItem( loader , storage , distr , delivery , node );
			distr.addBinaryItem( delivery , item );
		}
	}
	
	private static MetaDistrBinaryItem importxmlBinaryItem( EngineLoader loader , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrBinaryItem;
		
		MetaDistrBinaryItem item = new MetaDistrBinaryItem( storage.meta , delivery );
		DBEnumDistItemType itemType = DBEnumDistItemType.getValue( entity.importxmlEnumAttr( root , MetaDistrBinaryItem.PROPERTY_DISTITEMTYPE ) , true );
		String ext = entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_EXT );
		String staticExt = "";
		if( itemType == DBEnumDistItemType.STATICWAR ) {
			ext = ".war";
			staticExt = ext;
		}
		
		item.createBinaryItem(
				entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_DESC ) ,
				itemType ,
				entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_DISTNAME ) ,
				entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_DEPLOYNAME ) ,
				ext ,
				DBEnumDeployVersionType.getValue( entity.importxmlEnumAttr( root , MetaDistrBinaryItem.PROPERTY_DEPLOYVERSIONTYPE ) , false ) ,
				staticExt ,
				entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_WARCONTEXT ) ,
				entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_ARCHIVEFILES ) ,
				entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_ARCHIVEEXCLUDE )
				);

		MetaSources sources = storage.getSources();
		Integer srcItemId = sources.getProjectItemId( entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_SRCITEM_NAME ) );
		Integer srcDistId = distr.getBinaryItemId( entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_SRCDISTITEM_NAME ) );
		
		item.setSource(
				DBEnumItemOriginType.getValue( entity.importxmlEnumAttr( root , MetaDistrBinaryItem.PROPERTY_ITEMORIGIN ) , true ) ,
				srcItemId ,
				srcDistId ,
				entity.importxmlStringAttr( root , MetaDistrBinaryItem.PROPERTY_SRCITEMPATH ) 
				);
		
		item.setCustom(
				entity.importxmlBooleanAttr( root , MetaDistrBinaryItem.PROPERTY_CUSTOMGET , false ) ,
				entity.importxmlBooleanAttr( root , MetaDistrBinaryItem.PROPERTY_CUSTOMDEPLOY , false )
				);
		
		modifyBinaryItem( c , storage , item , true );
		
		return( item );
	}
	
	private static void importxmlDeliveryConfItems( EngineLoader loader , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , Node root ) throws Exception {
		Node[] distitems = ConfReader.xmlGetChildren( root , ELEMENT_CONFITEM );
		if( distitems == null )
			return;
		
		for( Node node : distitems ) {
			MetaDistrConfItem item = importxmlConfItem( loader , storage , distr , delivery , node );
			distr.addConfItem( delivery , item );
		}
	}
	
	private static MetaDistrConfItem importxmlConfItem( EngineLoader loader , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrConfItem;
		
		MetaDistrConfItem item = new MetaDistrConfItem( storage.meta , delivery );
		item.createConfItem(
				entity.importxmlStringAttr( root , MetaDistrConfItem.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaDistrConfItem.PROPERTY_DESC ) ,
				DBEnumConfItemType.getValue( entity.importxmlEnumAttr( root , MetaDistrConfItem.PROPERTY_TYPE ) , true ) ,
				entity.importxmlStringAttr( root , MetaDistrConfItem.PROPERTY_FILES ) ,
				entity.importxmlStringAttr( root , MetaDistrConfItem.PROPERTY_TEMPLATES ) ,
				entity.importxmlStringAttr( root , MetaDistrConfItem.PROPERTY_SECURED ) ,
				entity.importxmlStringAttr( root , MetaDistrConfItem.PROPERTY_EXCLUDE ) ,
				entity.importxmlStringAttr( root , MetaDistrConfItem.PROPERTY_EXTCONF )
				);
		modifyConfItem( c , storage , item , true );
		
		return( item );
	}

	private static void importxmlDeliverySchemaItems( EngineLoader loader , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		
		Node[] distitems = ConfReader.xmlGetChildren( root , ELEMENT_DATABASE );
		if( distitems == null )
			return;
		
		int version = c.getNextProductVersion( storage );
		
		MetaDatabase database = storage.getDatabase();
		for( Node node : distitems ) {
			String name = ConfReader.getAttrValue( node , ATTR_DELIVERY_SCHEMA );
			MetaDatabaseSchema schema = database.getSchema( name );
			distr.addDeliverySchema( delivery , schema );
			
			if( !c.modify( DBQueries.MODIFY_DISTR_ADDDELIVERYSCHEMA4 , new String[] {
					EngineDB.getInteger( delivery.ID ) ,
					EngineDB.getInteger( schema.ID ) ,
					EngineDB.getInteger( storage.ID ) ,
					EngineDB.getInteger( version )
				} ) )
				Common.exitUnexpected();
		}
	}
	
	private static void importxmlDeliveryDocItems( EngineLoader loader , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		
		Node[] distitems = ConfReader.xmlGetChildren( root , ELEMENT_DOCUMENT );
		if( distitems == null )
			return;
		
		int version = c.getNextProductVersion( storage );
		
		MetaDocs docs = storage.getDocs();
		for( Node node : distitems ) {
			String name = ConfReader.getAttrValue( node , ATTR_DELIVERY_DOCNAME );
			MetaProductDoc doc = docs.getDoc( name );
			distr.addDeliveryDoc( delivery , doc );
			
			if( !c.modify( DBQueries.MODIFY_DISTR_ADDDELIVERYDOC4 , new String[] {
					EngineDB.getInteger( delivery.ID ) ,
					EngineDB.getInteger( doc.ID ) ,
					EngineDB.getInteger( storage.ID ) ,
					EngineDB.getInteger( version )
				} ) )
				Common.exitUnexpected();
		}
	}
	
	private static MetaDistrComponent importxmlComponent( EngineLoader loader , ProductMeta storage , MetaDistr distr , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrComponent;
		
		MetaDistrComponent comp = new MetaDistrComponent( storage.meta , distr );
		comp.createComponent(
				entity.importxmlStringAttr( root , MetaDistrComponent.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaDistrComponent.PROPERTY_DESC )
				);
		modifyComponent( c , storage , comp , true );

		// component items
		Node[] compitems = ConfReader.xmlGetChildren( root , ELEMENT_COMPITEM_BINARYITEM );
		if( compitems != null ) {
			for( Node node : compitems ) {
				MetaDistrComponentItem item = importxmlComponentItem( loader , storage , distr , comp , DBEnumCompItemType.BINARY , node );
				comp.addBinaryItem( item );
			}
		}
		compitems = ConfReader.xmlGetChildren( root , ELEMENT_COMPITEM_CONFITEM );
		if( compitems != null ) {
			for( Node node : compitems ) {
				MetaDistrComponentItem item = importxmlComponentItem( loader , storage , distr , comp , DBEnumCompItemType.CONF , node );
				comp.addConfItem( item );
			}
		}
		compitems = ConfReader.xmlGetChildren( root , ELEMENT_COMPITEM_SCHEMA );
		if( compitems != null ) {
			for( Node node : compitems ) {
				MetaDistrComponentItem item = importxmlComponentItem( loader , storage , distr , comp , DBEnumCompItemType.SCHEMA , node );
				comp.addSchemaItem( item );
			}
		}
		compitems = ConfReader.xmlGetChildren( root , ELEMENT_COMPITEM_WS );
		if( compitems != null ) {
			for( Node node : compitems ) {
				MetaDistrComponentItem item = importxmlComponentItem( loader , storage , distr , comp , DBEnumCompItemType.WSDL , node );
				comp.addWebService( item );
			}
		}
		
		return( comp );
	}

	private static MetaDistrComponentItem importxmlComponentItem( EngineLoader loader , ProductMeta storage , MetaDistr distr , MetaDistrComponent comp , DBEnumCompItemType type , Node root ) throws Exception {
		MetaDistrComponentItem item = new MetaDistrComponentItem( storage.meta , comp );
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrCompItem;
		
		if( type == DBEnumCompItemType.BINARY ) {
			MetaDistrBinaryItem binaryItem = distr.getBinaryItem( entity.importxmlStringAttr( root , MetaDistrComponentItem.PROPERTY_NAME ) );
			item.createBinaryItem( binaryItem , entity.importxmlStringAttr( root , MetaDistrComponentItem.PROPERTY_DEPLOYNAME ) );
		}
		else
		if( type == DBEnumCompItemType.CONF ) {
			MetaDistrConfItem confItem = distr.getConfItem( entity.importxmlStringAttr( root , MetaDistrComponentItem.PROPERTY_NAME ) );
			item.createConfItem( confItem );
		}
		else
		if( type == DBEnumCompItemType.SCHEMA ) {
			MetaDatabase database = storage.getDatabase();
			MetaDatabaseSchema schema = database.getSchema( entity.importxmlStringAttr( root , MetaDistrComponentItem.PROPERTY_NAME ) );
			item.createSchemaItem( schema , entity.importxmlStringAttr( root , MetaDistrComponentItem.PROPERTY_DEPLOYNAME ) );
		}
		else
		if( type == DBEnumCompItemType.WSDL ) {
			String url = entity.importxmlStringAttr( root , MetaDistrComponentItem.PROPERTY_WSDL );
			item.createWsdlItem( url );
		}
		else
			Common.exitUnexpected();
		
		return( item );
	}

	public static void loaddb( EngineLoader loader , ProductMeta storage ) throws Exception {
		MetaDistr distr = new MetaDistr( storage , storage.meta );
		storage.setDistr( distr );
		
		loaddbDeliveries( loader , storage , distr );
		loaddbComponents( loader , storage , distr );
	}

	public static void loaddbDeliveries( EngineLoader loader , ProductMeta storage , MetaDistr distr ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrDelivery;
		
		MetaDatabase db = storage.getDatabase();
		MetaDocs docs = storage.getDocs();
		MetaUnits units = storage.getUnits();
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID , new String[] { EngineDB.getInteger( storage.ID ) } );
		try {
			while( rs.next() ) {
				MetaDistrDelivery delivery = new MetaDistrDelivery( storage.meta , distr , db , docs );
				delivery.ID = entity.loaddbId( rs );
				delivery.PV = entity.loaddbVersion( rs );
				delivery.createDelivery( 
						units.getUnitId( entity.loaddbString( rs , DBProductData.FIELD_DELIVERY_UNIT_ID ) ) ,
						entity.loaddbString( rs , MetaDistrDelivery.PROPERTY_NAME ) ,
						entity.loaddbString( rs , MetaDistrDelivery.PROPERTY_DESC ) ,
						entity.loaddbString( rs , MetaDistrDelivery.PROPERTY_FOLDER )
						);
				delivery.setDatabaseAll( entity.loaddbBoolean( rs , MetaDistrDelivery.PROPERTY_SCHEMA_ANY ) );
				delivery.setDocAll( entity.loaddbBoolean( rs , MetaDistrDelivery.PROPERTY_DOC_ANY ) );
				distr.addDelivery( delivery );
			}
		}
		finally {
			c.closeQuery();
		}
		
		loaddbDeliveryBinaryItems( loader , storage , distr );
		loaddbDeliveryConfItems( loader , storage , distr );
		loaddbDeliverySchemes( loader , storage , distr );
		loaddbDeliveryDocs( loader , storage , distr );
	}

	public static void loaddbDeliveryBinaryItems( EngineLoader loader , ProductMeta storage , MetaDistr distr ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrBinaryItem;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID , new String[] { EngineDB.getInteger( storage.ID ) } );
		try {
			while( rs.next() ) {
				MetaDistrDelivery delivery = distr.getDelivery( entity.loaddbObject( rs , DBProductData.FIELD_DELIVERY_ID ) );
				MetaDistrBinaryItem item = new MetaDistrBinaryItem( storage.meta , delivery );
				item.ID = entity.loaddbId( rs );
				item.PV = entity.loaddbVersion( rs );
				item.createBinaryItem( 
						entity.loaddbString( rs , MetaDistrBinaryItem.PROPERTY_NAME ) ,
						entity.loaddbString( rs , MetaDistrBinaryItem.PROPERTY_DESC ) ,
						DBEnumDistItemType.getValue( entity.loaddbEnum( rs , MetaDistrBinaryItem.PROPERTY_DISTITEMTYPE ) , true ) ,
						entity.loaddbString( rs , MetaDistrBinaryItem.PROPERTY_DISTNAME ) ,
						entity.loaddbString( rs , MetaDistrBinaryItem.PROPERTY_DEPLOYNAME ) ,
						entity.loaddbString( rs , MetaDistrBinaryItem.PROPERTY_EXT ) ,
						DBEnumDeployVersionType.getValue( entity.loaddbEnum( rs , MetaDistrBinaryItem.PROPERTY_DEPLOYVERSIONTYPE ) , false ) ,
						entity.loaddbString( rs , DBProductData.FIELD_BINARYITEM_WARSTATICEXT ) ,
						entity.loaddbString( rs , MetaDistrBinaryItem.PROPERTY_WARCONTEXT ) ,
						entity.loaddbString( rs , MetaDistrBinaryItem.PROPERTY_ARCHIVEFILES ) ,
						entity.loaddbString( rs , MetaDistrBinaryItem.PROPERTY_ARCHIVEEXCLUDE )
						);
				distr.addBinaryItem( delivery , item );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbDeliveryConfItems( EngineLoader loader , ProductMeta storage , MetaDistr distr ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrConfItem;
		
		ResultSet rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID , new String[] { EngineDB.getInteger( storage.ID ) } );
		try {
			while( rs.next() ) {
				MetaDistrDelivery delivery = distr.getDelivery( entity.loaddbObject( rs , DBProductData.FIELD_DELIVERY_ID ) );
				MetaDistrConfItem item = new MetaDistrConfItem( storage.meta , delivery );
				item.ID = entity.loaddbId( rs );
				item.PV = entity.loaddbVersion( rs );
				item.createConfItem(
						entity.loaddbString( rs , MetaDistrConfItem.PROPERTY_NAME ) ,
						entity.loaddbString( rs , MetaDistrConfItem.PROPERTY_DESC ) ,
						DBEnumConfItemType.getValue( entity.loaddbEnum( rs , MetaDistrConfItem.PROPERTY_TYPE ) , true ) ,
						entity.loaddbString( rs , MetaDistrConfItem.PROPERTY_FILES ) ,
						entity.loaddbString( rs , MetaDistrConfItem.PROPERTY_TEMPLATES ) ,
						entity.loaddbString( rs , MetaDistrConfItem.PROPERTY_SECURED ) ,
						entity.loaddbString( rs , MetaDistrConfItem.PROPERTY_EXCLUDE ) ,
						entity.loaddbString( rs , MetaDistrConfItem.PROPERTY_EXTCONF )
						);
				distr.addConfItem( delivery , item );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbDeliverySchemes( EngineLoader loader , ProductMeta storage , MetaDistr distr ) throws Exception {
		DBConnection c = loader.getConnection();
		MetaDatabase db = storage.getDatabase();
		
		ResultSet rs = c.query( DBQueries.QUERY_DISTR_GETALLDELIVERYSCHEMES1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		try {
			while( rs.next() ) {
				MetaDistrDelivery delivery = distr.getDelivery( rs.getInt( 1 ) );
				MetaDatabaseSchema schema = db.getSchema( rs.getInt( 2 ) );
				delivery.addSchema( schema );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbDeliveryDocs( EngineLoader loader , ProductMeta storage , MetaDistr distr ) throws Exception {
		DBConnection c = loader.getConnection();
		MetaDocs docs = storage.getDocs();
		
		ResultSet rs = c.query( DBQueries.QUERY_DISTR_GETALLDELIVERYDOCS1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		try {
			while( rs.next() ) {
				MetaDistrDelivery delivery = distr.getDelivery( rs.getInt( 1 ) );
				MetaProductDoc doc = docs.getDoc( rs.getInt( 2 ) );
				delivery.addDocument( doc );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbComponents( EngineLoader loader , ProductMeta storage , MetaDistr distr ) throws Exception {
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		MetaDistr distr = storage.getDistr();

		exportxmlDeliveries( loader , storage , distr , doc , Common.xmlCreateElement( doc , root , ELEMENT_DELIVERIES ) );
		exportxmlComponents( loader , storage , distr , doc , Common.xmlCreateElement( doc , root , ELEMENT_COMPONENTS ) );
	}

	public static void exportxmlDeliveries( EngineLoader loader , ProductMeta storage , MetaDistr distr , Document doc , Element root ) throws Exception {
		for( String name : distr.getDeliveryNames() ) {
			MetaDistrDelivery delivery = distr.findDelivery( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DELIVERY );
			exportxmlDelivery( loader , storage , delivery , doc , node );
		}
	}
	
	public static void exportxmlDelivery( EngineLoader loader , ProductMeta storage , MetaDistrDelivery delivery , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrDelivery;
		
		MetaUnits units = storage.getUnits();
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( units.getUnitName( delivery.UNIT_ID ) ) ,
				entity.exportxmlString( delivery.NAME ) ,
				entity.exportxmlString( delivery.DESC ) ,
				entity.exportxmlString( delivery.FOLDER ) ,
				entity.exportxmlBoolean( delivery.SCHEMA_ANY ) ,
				entity.exportxmlBoolean( delivery.DOC_ANY )
		} , true );
		
		exportxmlDeliveryBinaryItems( loader , storage , delivery , doc , root );
		exportxmlDeliveryConfItems( loader , storage , delivery , doc , root );
		exportxmlDeliverySchemes( loader , storage , delivery , doc , root );
		exportxmlDeliveryDocs( loader , storage , delivery , doc , root );
	}

	public static void exportxmlDeliveryBinaryItems( EngineLoader loader , ProductMeta storage , MetaDistrDelivery delivery , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrBinaryItem;
		MetaSources sources = storage.getSources();
		MetaDistr distr = storage.getDistr();
		
		for( String name : delivery.getBinaryItemNames() ) {
			MetaDistrBinaryItem item = delivery.findBinaryItem( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_BINARYITEM );
			
			String EXT = ( item.DISTITEM_TYPE == DBEnumDistItemType.STATICWAR )? item.WAR_STATICEXT : item.EXT;
			DBEngineEntities.exportxmlAppObject( doc , node , entity , new String[] {
					entity.exportxmlString( item.NAME ) ,
					entity.exportxmlString( item.DESC ) ,
					entity.exportxmlEnum( item.DISTITEM_TYPE ) ,
					entity.exportxmlString( item.BASENAME_DIST ) ,
					entity.exportxmlString( item.BASENAME_DEPLOY ) ,
					entity.exportxmlString( EXT ) ,
					entity.exportxmlEnum( item.DEPLOYVERSION_TYPE ) ,
					entity.exportxmlEnum( item.ITEMORIGIN_TYPE ) ,
					entity.exportxmlString( sources.getProjectItemName( item.SRCITEM_ID ) ) ,
					entity.exportxmlString( distr.getBinaryItemName( item.SRC_BINARY_ID ) ) ,
					entity.exportxmlString( item.SRC_ITEMPATH ) ,
					entity.exportxmlString( item.ARCHIVE_FILES ) ,
					entity.exportxmlString( item.ARCHIVE_EXCLUDE ) ,
					entity.exportxmlString( item.WAR_CONTEXT ) ,
					entity.exportxmlBoolean( item.CUSTOM_GET ) ,
					entity.exportxmlBoolean( item.CUSTOM_DEPLOY )
			} , true );
		}
	}
	
	public static void exportxmlDeliveryConfItems( EngineLoader loader , ProductMeta storage , MetaDistrDelivery delivery , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaDistrConfItem;
		
		for( String name : delivery.getBinaryItemNames() ) {
			MetaDistrConfItem item = delivery.findConfItem( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_CONFITEM );
			
			DBEngineEntities.exportxmlAppObject( doc , node , entity , new String[] {
					entity.exportxmlString( item.NAME ) ,
					entity.exportxmlString( item.DESC ) ,
					entity.exportxmlEnum( item.CONFITEM_TYPE ) ,
					entity.exportxmlString( item.FILES ) ,
					entity.exportxmlString( item.TEMPLATES ) ,
					entity.exportxmlString( item.SECURED ) ,
					entity.exportxmlString( item.EXCLUDE ) ,
					entity.exportxmlString( item.EXTCONF )
			} , true );
		}
	}
	
	public static void exportxmlComponents( EngineLoader loader , ProductMeta storage , MetaDistr distr , Document doc , Element root ) throws Exception {
	}
	
	public static void exportxmlDeliverySchemes( EngineLoader loader , ProductMeta storage , MetaDistrDelivery delivery , Document doc , Element root ) throws Exception {
		for( String name : delivery.getDatabaseSchemaNames() ) {
			MetaDatabaseSchema schema = delivery.findSchema( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DATABASE );
			Common.xmlSetElementAttr( doc , node , ATTR_DELIVERY_SCHEMA , schema.NAME );
		}
	}
	
	public static void exportxmlDeliveryDocs( EngineLoader loader , ProductMeta storage , MetaDistrDelivery delivery , Document doc , Element root ) throws Exception {
		for( String name : delivery.getDocNames() ) {
			MetaProductDoc pdoc = delivery.findDoc( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_DATABASE );
			Common.xmlSetElementAttr( doc , node , ATTR_DELIVERY_SCHEMA , pdoc.NAME );
		}
	}
	
	private static void modifyDelivery( DBConnection c , ProductMeta storage , MetaDistrDelivery delivery , boolean insert ) throws Exception {
		if( insert )
			delivery.ID = DBNames.getNameIndex( c , storage.ID , delivery.NAME , DBEnumObjectType.META_DIST_DELIVERY );
		else
			DBNames.updateName( c , storage.ID , delivery.NAME , delivery.ID , DBEnumObjectType.META_DIST_DELIVERY );
		
		delivery.PV = c.getNextProductVersion( storage );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaDistrDelivery , delivery.ID , delivery.PV , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getObject( delivery.UNIT_ID ) ,
				EngineDB.getString( delivery.NAME ) ,
				EngineDB.getString( delivery.DESC ) ,
				EngineDB.getString( delivery.FOLDER ) ,
				EngineDB.getBoolean( delivery.SCHEMA_ANY ) ,
				EngineDB.getBoolean( delivery.DOC_ANY )
				} , insert );
	}
	
	private static void modifyBinaryItem( DBConnection c , ProductMeta storage , MetaDistrBinaryItem item , boolean insert ) throws Exception {
		if( insert )
			item.ID = DBNames.getNameIndex( c , storage.ID , item.NAME , DBEnumObjectType.META_DIST_BINARYITEM );
		else
			DBNames.updateName( c , storage.ID , item.NAME , item.ID , DBEnumObjectType.META_DIST_BINARYITEM );
		
		item.PV = c.getNextProductVersion( storage );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaDistrBinaryItem , item.ID , item.PV , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getObject( item.delivery.ID ) ,
				EngineDB.getString( item.NAME ) ,
				EngineDB.getString( item.DESC ) ,
				EngineDB.getEnum( item.DISTITEM_TYPE ) ,
				EngineDB.getString( item.BASENAME_DIST ) ,
				EngineDB.getString( item.BASENAME_DEPLOY ) ,
				EngineDB.getString( item.EXT ) ,
				EngineDB.getEnum( item.DEPLOYVERSION_TYPE ) ,
				EngineDB.getEnum( item.ITEMORIGIN_TYPE ) ,
				EngineDB.getObject( item.SRCITEM_ID ) ,
				EngineDB.getObject( item.SRC_BINARY_ID ) ,
				EngineDB.getString( item.SRC_ITEMPATH ) ,
				EngineDB.getString( item.ARCHIVE_FILES ) ,
				EngineDB.getString( item.ARCHIVE_EXCLUDE ) ,
				EngineDB.getString( item.WAR_STATICEXT ) ,
				EngineDB.getString( item.WAR_CONTEXT ) ,
				EngineDB.getBoolean( item.CUSTOM_GET ) ,
				EngineDB.getBoolean( item.CUSTOM_DEPLOY )
				} , insert );
	}
	
	private static void modifyConfItem( DBConnection c , ProductMeta storage , MetaDistrConfItem item , boolean insert ) throws Exception {
		if( insert )
			item.ID = DBNames.getNameIndex( c , storage.ID , item.NAME , DBEnumObjectType.META_DIST_CONFITEM );
		else
			DBNames.updateName( c , storage.ID , item.NAME , item.ID , DBEnumObjectType.META_DIST_CONFITEM );
		
		item.PV = c.getNextProductVersion( storage );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaDistrConfItem , item.ID , item.PV , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getObject( item.delivery.ID ) ,
				EngineDB.getString( item.NAME ) ,
				EngineDB.getString( item.DESC ) ,
				EngineDB.getEnum( item.CONFITEM_TYPE ) ,
				EngineDB.getString( item.FILES ) ,
				EngineDB.getString( item.TEMPLATES ) ,
				EngineDB.getString( item.SECURED ) ,
				EngineDB.getString( item.EXCLUDE ) ,
				EngineDB.getString( item.EXTCONF )
				} , insert );
	}
	
	private static void modifyComponent( DBConnection c , ProductMeta storage , MetaDistrComponent comp , boolean insert ) throws Exception {
		if( insert )
			comp.ID = DBNames.getNameIndex( c , storage.ID , comp.NAME , DBEnumObjectType.META_DIST_COMPONENT );
		else
			DBNames.updateName( c , storage.ID , comp.NAME , comp.ID , DBEnumObjectType.META_DIST_COMPONENT );
		
		comp.PV = c.getNextProductVersion( storage );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaDistrComponent , comp.ID , comp.PV , new String[] {
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getString( comp.NAME ) ,
				EngineDB.getString( comp.DESC )
				} , insert );
	}

	public static MetaDistrDelivery createDelivery( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , Integer unitId , String name , String desc , String folder ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaDatabase db = storage.getDatabase();
		MetaDocs docs = storage.getDocs();
		MetaDistrDelivery delivery = new MetaDistrDelivery( storage.meta , distr , db , docs );
		delivery.createDelivery( unitId , name , desc , folder );
		modifyDelivery( c , storage , delivery , true );
		
		distr.addDelivery( delivery );
		return( delivery );
	}
	
	public static void modifyDelivery( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , Integer unitId , String name , String desc , String folder ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		delivery.modifyDelivery( unitId , name , desc , folder );
		modifyDelivery( c , storage , delivery , false );
		
		distr.updateDelivery( delivery );
	}
	
	public static void deleteDelivery( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		if( !delivery.isEmpty() )
			Common.exitUnexpected();
		
		DBEngineEntities.deleteAppObject( c , entities.entityAppMetaDistrDelivery , delivery.ID , c.getNextProductVersion( storage ) );
		distr.removeDelivery( delivery );
	}

	public static MetaDistrBinaryItem createBinaryItem( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , 
			String name , String desc ,
			DBEnumDistItemType itemType , String basename , String ext , String archiveFiles , String archiveExclude ,
			String deployname , DBEnumDeployVersionType versionType , 
			MetaSourceProjectItem itemSrcProject , MetaDistrBinaryItem itemSrcDist , String originPath ,
			boolean customGet , boolean customDeploy ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaDistrBinaryItem item = new MetaDistrBinaryItem( storage.meta , delivery );
		item.createBinaryItem( name , desc );
		item.setDistData( itemType , basename , ext , archiveFiles , archiveExclude );
		item.setDeployData( deployname , versionType );
		
		if( itemSrcProject != null )
			item.setBuildOrigin( itemSrcProject );
		else
		if( itemSrcDist != null )
			item.setDistOrigin( itemSrcDist , originPath );
		else
			item.setManualOrigin();
		item.setCustom( customGet , customDeploy );
		
		modifyBinaryItem( c , storage , item , true );
		
		distr.addBinaryItem( delivery , item );
		return( item );
	}
	
	public static void modifyBinaryItem( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrBinaryItem item , 
			String name , String desc ,
			DBEnumDistItemType itemType , String basename , String ext , String archiveFiles , String archiveExclude ,
			String deployname , DBEnumDeployVersionType versionType , 
			MetaSourceProjectItem itemSrcProject , MetaDistrBinaryItem itemSrcDist , String originPath ,
			boolean customGet , boolean customDeploy ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( itemSrcProject != null && item.isProjectItem() )
			item.sourceProjectItem.setDistItem( null );
		
		item.modifyBinaryItem( name , desc );
		item.setDistData( itemType , basename , ext , archiveFiles , archiveExclude );
		item.setDeployData( deployname , versionType );
		
		if( itemSrcProject != null )
			item.setBuildOrigin( itemSrcProject );
		else
		if( itemSrcDist != null )
			item.setDistOrigin( itemSrcDist , originPath );
		else
			item.setManualOrigin();
		item.setCustom( customGet , customDeploy );
		
		modifyBinaryItem( c , storage , item , false );
		
		distr.updateBinaryItem( item );
	}
	
	public static void deleteBinaryItem( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrBinaryItem item ) throws Exception {
		MetaEnvs envs = storage.getEnviroments();
		envs.deleteBinaryItemFromEnvironments( transaction , item );
		
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		if( !c.modify( DBQueries.MODIFY_DISTR_CASCADEBINARY_COMPITEM1 , new String[] { EngineDB.getInteger( item.ID ) } ) )
			Common.exitUnexpected();
		
		for( MetaDistrComponent comp : distr.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findBinaryItem( item.NAME );
			if( compItem != null )
				comp.removeCompItem( compItem );
		}

		DBEngineEntities.deleteAppObject( c , entities.entityAppMetaDistrBinaryItem , item.ID , c.getNextProductVersion( storage ) );
		
		distr.removeBinaryItem( item.delivery , item );
	}

	public static void changeBinaryItemDelivery( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrBinaryItem item , MetaDistrDelivery delivery ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		delivery.moveItemToThis( item );
		modifyBinaryItem( c , storage , item , false );
	}
	
	public static void changeBinaryItemProjectToManual( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrBinaryItem item ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		item.changeProjectToManual();
		modifyBinaryItem( c , storage , item , false );
	}

	public static MetaDistrConfItem createConfItem( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery ,
			String name , String desc , DBEnumConfItemType type , String files , String templates , String secured , String exclude , String extconf ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaDistrConfItem item = new MetaDistrConfItem( storage.meta , delivery );
		item.createConfItem( name , desc , type , files , templates , secured , exclude , extconf );
		modifyConfItem( c , storage , item , true );
		
		distr.addConfItem( delivery , item );
		return( item );
	}
	
	public static void modifyConfItem( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrConfItem item ,
			String name , String desc , DBEnumConfItemType type , String files , String templates , String secured , String exclude , String extconf ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		item.modifyConfItem( name , desc , type , files , templates , secured , exclude , extconf );
		modifyConfItem( c , storage , item , false );
		
		distr.updateConfItem( item );
	}
	
	public static void deleteConfItem( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrConfItem item ) throws Exception {
		MetaEnvs envs = storage.getEnviroments();
		envs.deleteConfItemFromEnvironments( transaction , item );
		
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		if( !c.modify( DBQueries.MODIFY_DISTR_CASCADECONF_COMPITEM1 , new String[] { EngineDB.getInteger( item.ID ) } ) )
			Common.exitUnexpected();
		
		for( MetaDistrComponent comp : distr.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findConfItem( item.NAME );
			if( compItem != null )
				comp.removeCompItem( compItem );
		}

		DBEngineEntities.deleteAppObject( c , entities.entityAppMetaDistrConfItem , item.ID , c.getNextProductVersion( storage ) );
		
		distr.removeConfItem( item.delivery , item );
	}

	public static void setDeliveryDatabaseAll( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , boolean all ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		delivery.setDatabaseAll( all );
		if( all ) {
			if( !c.modify( DBQueries.MODIFY_DISTR_DELETEDELIVERYSCHEMES1 , new String[] { EngineDB.getInteger( delivery.ID ) } ) )
				Common.exitUnexpected();
		}
		
		modifyDelivery( c , storage , delivery , false );
	}
	
	public static void setDeliveryDatabaseSet( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , MetaDatabaseSchema[] set ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		boolean needUpdate = false;
		if( delivery.SCHEMA_ANY )
			needUpdate = true;
			
		delivery.setDatabaseSet( set );

		if( !c.modify( DBQueries.MODIFY_DISTR_DELETEDELIVERYSCHEMES1 , new String[] { EngineDB.getInteger( delivery.ID ) } ) )
			Common.exitUnexpected();
		
		if( needUpdate )
			modifyDelivery( c , storage , delivery , false );

		int version = c.getNextProductVersion( storage );
		for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() ) {
			if( !c.modify( DBQueries.MODIFY_DISTR_ADDDELIVERYSCHEMA4 , new String[] { 
					EngineDB.getInteger( delivery.ID ) ,
					EngineDB.getInteger( schema.ID ) ,
					EngineDB.getInteger( storage.ID ) ,
					EngineDB.getInteger( version )
					} ) )
				Common.exitUnexpected();
		}
	}
	
	public static void setDeliveryDocSet( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDistrDelivery delivery , MetaProductDoc[] set ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		boolean needUpdate = false;
		if( delivery.DOC_ANY )
			needUpdate = true;
			
		delivery.setDocSet( set );

		if( !c.modify( DBQueries.MODIFY_DISTR_DELETEDELIVERYDOCS1 , new String[] { EngineDB.getInteger( delivery.ID ) } ) )
			Common.exitUnexpected();
		
		if( needUpdate )
			modifyDelivery( c , storage , delivery , false );

		int version = c.getNextProductVersion( storage );
		for( MetaProductDoc doc : delivery.getDocs() ) {
			if( !c.modify( DBQueries.MODIFY_DISTR_ADDDELIVERYDOC4 , new String[] { 
					EngineDB.getInteger( delivery.ID ) ,
					EngineDB.getInteger( doc.ID ) ,
					EngineDB.getInteger( storage.ID ) ,
					EngineDB.getInteger( version )
					} ) )
				Common.exitUnexpected();
		}
	}
	
	public static void deleteDatabaseSchema( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaDatabaseSchema schema ) throws Exception {
		for( MetaDistrDelivery delivery : distr.getDeliveries() ) {
			if( delivery.findSchema( schema.NAME ) != null )
				delivery.removeSchema( schema );
		}
		for( MetaDistrComponent comp : distr.getComponents() ) {
			MetaDistrComponentItem compItem = comp.findSchemaItem( schema.NAME );
			if( compItem != null )
				comp.removeCompItem( compItem );
		}
	}
	
	public static void createComponent( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
	}
	
	public static void modifyComponent( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
	}
	
	public static void deleteDistrComponent( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
	}

	public static void deleteUnit( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaProductUnit unit ) throws Exception {
		for( MetaDistrDelivery delivery : distr.getDeliveries() ) {
			if( Common.equalsIntegers( delivery.UNIT_ID , unit.ID ) )
				delivery.clearUnit();
		}
	}	
	
	public static void deleteDocument( EngineTransaction transaction , ProductMeta storage , MetaDistr distr , MetaProductDoc doc ) throws Exception {
		for( MetaDistrDelivery delivery : distr.getDeliveries() ) {
			if( delivery.findDoc( doc.NAME ) != null )
				delivery.removeDoc( doc );
		}
	}	
	
}
