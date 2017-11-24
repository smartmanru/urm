package org.urm.db.system;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.engine.EngineDB;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.Product;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBProduct {

	public static Product[] loaddb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		DBConnection c = loader.getConnection();
		List<Product> products = new LinkedList<Product>();
		
		ResultSet rs = c.query( DBQueries.QUERY_PRODUCT_GETALL0 );
		if( rs == null )
			Common.exitUnexpected();
		
		while( rs.next() ) {
			int systemId = rs.getInt( 2 );
			AppSystem system = directory.getSystem( systemId );
			Product product = new Product( directory , system );
			product.ID = rs.getInt( 1 );
			product.SYSTEM = systemId;
			product.NAME = rs.getString( 3 );
			product.DESC = rs.getString( 4 );
			product.PATH = rs.getString( 5 );
			product.OFFLINE = rs.getBoolean( 6 );
			product.MONITORING_ENABLED = rs.getBoolean( 7 );
			product.SV = rs.getInt( 8 );
			products.add( product );
		}
		
		return( products.toArray( new Product[0] ) );
	}
	
	public static Product importxml( EngineLoader loader , EngineDirectory directory , AppSystem system , Node node ) throws Exception {
		Product product = new Product( directory , system );
		product.NAME = ConfReader.getAttrValue( node , "name" );
		product.DESC = ConfReader.getAttrValue( node , "desc" );
		product.PATH = ConfReader.getAttrValue( node , "path" );
		product.OFFLINE = ConfReader.getBooleanAttrValue( node , "offline" , true );
		product.MONITORING_ENABLED = false;
		return( product );
	}
	
	public static void resolvexml( EngineLoader loader , EngineDirectory directory , Product product ) throws Exception {
	}
	
	public static void resolvedb( EngineLoader loader , EngineDirectory directory , Product product ) throws Exception {
	}
	
	public static void matchxml( EngineLoader loader , EngineDirectory directory , Product product ) throws Exception {
	}
	
	public static void matchdb( EngineLoader loader , EngineDirectory directory , Product product ) throws Exception {
	}
	
	public static void exportxml( EngineLoader loader , EngineDirectory directory , Product product , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , product.NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , product.DESC );
		Common.xmlSetElementAttr( doc , root , "path" , product.PATH );
		Common.xmlSetElementAttr( doc , root , "offline" , Common.getBooleanValue( product.OFFLINE ) );
	}

	public static void importsavedb( EngineLoader loader , EngineDirectory directory , Product product ) throws Exception {
		DBConnection c = loader.getConnection();
		int productId = DBNames.getNameIndex( c , DBVersions.CORE_ID , product.NAME , DBEnumObjectType.PRODUCT );
		insert( c , productId , product );
	}		
	
	public static void insert( DBConnection c , int productId , Product product ) throws Exception {
		product.ID = productId;
		product.SV = c.getNextSystemVersion( product.system.ID );
		if( !c.update( DBQueries.MODIFY_PRODUCT_ADD8 , new String[] {
				"" + product.ID , 
				"" + product.system.ID , 
				EngineDB.getString( product.NAME ) , 
				EngineDB.getString( product.DESC ) ,
				EngineDB.getString( product.PATH ) ,
				EngineDB.getBoolean( product.OFFLINE ) ,
				EngineDB.getBoolean( product.MONITORING_ENABLED ) ,
				"" + product.SV 
				} ) )
			Common.exitUnexpected();
	}

}
