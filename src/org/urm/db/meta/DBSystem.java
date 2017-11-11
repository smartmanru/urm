package org.urm.db.meta;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBData;
import org.urm.db.DBEnumTypes.DBEnumObjectType;
import org.urm.db.DBNames;
import org.urm.db.DBQueries;
import org.urm.engine.EngineDB;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.Product;
import org.urm.meta.engine.System;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBSystem {

	public static System load( EngineDirectory directory , Node node ) throws Exception {
		System system = new System( directory ); 
		system.NAME = ConfReader.getAttrValue( node , "name" );
		system.DESC = ConfReader.getAttrValue( node , "desc" );
		system.OFFLINE = ConfReader.getBooleanAttrValue( node , "offline" , true );
		
		Node[] items = ConfReader.xmlGetChildren( node , "product" );
		if( items == null )
			return( system );
		
		for( Node itemNode : items ) {
			Product item = new Product( directory , system );
			item.load( itemNode );
			system.addProduct( item );
		}
		return( system );
	}
	
	public static void save( System system , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , system.NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , system.DESC );
		Common.xmlSetElementAttr( doc , root , "offline" , Common.getBooleanValue( system.OFFLINE ) );
		
		for( String productName : system.getProductNames() ) {
			Product product = system.findProduct( productName );
			Element elementProduct = Common.xmlCreateElement( doc , root , "product" );
			product.save( doc , elementProduct );
		}
	}
	
	public static void insert( DBConnection c , int CV , System system ) throws Exception {
		system.ID = DBNames.getNameIndex( c , DBData.CORE_ID , system.NAME , DBEnumObjectType.SYSTEM );
		system.CV = CV;
		if( !c.update( DBQueries.MODIFY_SYSTEM_ADD5 , new String[] {
				"" + system.ID , 
				EngineDB.getString( system.NAME ) , 
				EngineDB.getString( system.DESC ) ,
				EngineDB.getBoolean( system.OFFLINE ) ,
				"" + system.CV 
				} ) )
			Common.exitUnexpected();
	}

	public static void update( DBConnection c , int CV , System system ) throws Exception {
		system.CV = CV;
		if( !c.update( DBQueries.MODIFY_SYSTEM_UPDATE4 , new String[] {
				"" + system.ID , 
				EngineDB.getString( system.NAME ) , 
				EngineDB.getString( system.DESC ) ,
				"" + system.CV 
				} ) )
			Common.exitUnexpected();
	}

	public static System[] load( DBConnection c , EngineDirectory directory ) throws Exception {
		List<System> systems = new LinkedList<System>();
		
		ResultSet rs = c.query( DBQueries.QUERY_SYSTEM_GETALL0 );
		if( rs == null )
			Common.exitUnexpected();
		
		while( rs.next() ) {
			System system = new System( directory );
			system.ID = rs.getInt( 1 );
			system.NAME = rs.getString( 2 );
			system.DESC = rs.getString( 3 );
			system.OFFLINE = rs.getBoolean( 4 );
			system.CV = rs.getInt( 5 );
			systems.add( system );
		}
		
		return( systems.toArray( new System[0] ) );
	}
	
	public static void delete( DBConnection c , int CV , System system ) throws Exception {
		if( !c.update( DBQueries.MODIFY_SYSTEM_DELETEALLPARAMS2 , new String[] { "" + system.ID , "" + system.CV } ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_SYSTEM_DELETE2 , new String[] { "" + system.ID , "" + system.CV } ) )
			Common.exitUnexpected();
	}

}
