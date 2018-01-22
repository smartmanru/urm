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
import org.urm.meta.product.MetaProductUnit;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaUnits;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaUnits {

	public static String ELEMENT_UNIT = "unit";
	
	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		MetaUnits units = new MetaUnits( storage , storage.meta );
		storage.setUnits( units );
		
		if( root != null )
			importxmlUnitSet( loader , storage , units , root );
	}

	private static void importxmlUnitSet( EngineLoader loader , ProductMeta storage , MetaUnits units , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "unit" );
		if( items == null )
			return;
		
		for( Node unitNode : items ) {
			MetaProductUnit item = importxmlUnit( loader , storage , units , unitNode );
			units.addUnit( item );
		}
	}

	public static MetaProductUnit importxmlUnit( EngineLoader loader , ProductMeta storage , MetaUnits units , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaUnit;
		
		MetaProductUnit unit = new MetaProductUnit( storage.meta , units );
		unit.createUnit( 
				entity.importxmlStringAttr( root , MetaProductUnit.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , MetaProductUnit.PROPERTY_DESC )
				);
		
		modifyUnit( c , storage , unit , true );
		return( unit );
	}
	
	private static void modifyUnit( DBConnection c , ProductMeta storage , MetaProductUnit unit , boolean insert ) throws Exception {
		if( insert )
			unit.ID = DBNames.getNameIndex( c , storage.ID , unit.NAME , DBEnumObjectType.META_UNIT );
		else
			DBNames.updateName( c , storage.ID , unit.NAME , unit.ID , DBEnumObjectType.META_UNIT );
		
		unit.PV = c.getNextProductVersion( storage );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaUnit , unit.ID , unit.PV , new String[] {
				EngineDB.getInteger( storage.ID ) , 
				EngineDB.getString( unit.NAME ) ,
				EngineDB.getString( unit.DESC )
				} , insert );
	}
	
	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		MetaUnits units = storage.getUnits();
		
		for( String name : units.getUnitNames() ) {
			MetaProductUnit unit = units.findUnit( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_UNIT );
			exportxmlUnit( loader , storage , unit , doc , node );
		}
	}

	public static void exportxmlUnit( EngineLoader loader , ProductMeta storage , MetaProductUnit unit , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppMetaUnit;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( unit.NAME ) ,
				entity.exportxmlString( unit.DESC )
		} , true );
	}

	public static void loaddb( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaUnit;

		MetaUnits units = new MetaUnits( storage , storage.meta );
		storage.setUnits( units );
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				MetaProductUnit unit = new MetaProductUnit( storage.meta , units );
				unit.ID = entity.loaddbId( rs );
				unit.PV = entity.loaddbVersion( rs );
				unit.createUnit( 
						entity.loaddbString( rs , MetaProductUnit.PROPERTY_NAME ) , 
						entity.loaddbString( rs , MetaProductUnit.PROPERTY_DESC )
						);
				units.addUnit( unit );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static MetaProductUnit createUnit( EngineTransaction transaction , ProductMeta storage , MetaUnits units , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( units.findUnit( name ) != null )
			transaction.exitUnexpectedState();
		
		MetaProductUnit unit = new MetaProductUnit( storage.meta , units );
		unit.createUnit( name , desc );
		modifyUnit( c , storage , unit , true );
		
		units.addUnit( unit );
		return( unit );
	}
	
	public static void modifyUnit( EngineTransaction transaction , ProductMeta storage , MetaUnits units , MetaProductUnit unit , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		unit.modifyUnit( name , desc );
		modifyUnit( c , storage , unit , false );
		
		units.updateUnit( unit );
	}
	
	public static void deleteUnit( EngineTransaction transaction , ProductMeta storage , MetaUnits units , MetaProductUnit unit ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		MetaDistr distr = storage.getDistr();
		DBMetaDistr.deleteUnit( transaction , storage , distr , unit );
		MetaSources sources = storage.getSources();
		DBMetaSources.deleteUnit( transaction , storage , sources , unit );
		
		DBEngineEntities.deleteAppObject( c , entities.entityAppMetaUnit , unit.ID , c.getNextProductVersion( storage ) );
		units.removeUnit( unit );
	}
	
}
