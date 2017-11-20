package org.urm.db.system;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBEnums.DBEnumObjectType;
import org.urm.db.DBNames;
import org.urm.db.DBQueries;
import org.urm.db.DBSettings;
import org.urm.db.DBVersions;
import org.urm.engine.EngineDB;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineMatcher;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine.Product;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBSystem {

	public static AppSystem loadxml( EngineDirectory directory , Node node ) throws Exception {
		EngineEntities entities = directory.data.getEntities();
		EngineSettings settings = directory.data.getServerSettings();
		ObjectProperties props = entities.createSystemProps( settings.getEngineProperties() );
		
		AppSystem system = new AppSystem( directory , props );
		system.NAME = ConfReader.getAttrValue( node , "name" );
		system.DESC = ConfReader.getAttrValue( node , "desc" );
		system.OFFLINE = ConfReader.getBooleanAttrValue( node , "offline" , true );
		
		DBSettings.loadxml( node , props );
		
		Node[] items = ConfReader.xmlGetChildren( node , "product" );
		if( items == null )
			return( system );
		
		for( Node itemNode : items ) {
			Product product = DBProduct.loadxml( directory , system , itemNode );
			system.addProduct( product );
		}
		
		return( system );
	}

	public static AppSystem[] loaddb( EngineDirectory directory , DBConnection c ) throws Exception {
		List<AppSystem> systems = new LinkedList<AppSystem>();
		
		ResultSet rs = c.query( DBQueries.QUERY_SYSTEM_GETALL0 );
		if( rs == null )
			Common.exitUnexpected();
		
		EngineEntities entities = directory.data.getEntities();
		EngineSettings settings = directory.data.getServerSettings();
		while( rs.next() ) {
			ObjectProperties props = entities.createSystemProps( settings.getEngineProperties() );
			
			AppSystem system = new AppSystem( directory , props );
			system.ID = rs.getInt( 1 );
			system.NAME = rs.getString( 2 );
			system.DESC = rs.getString( 3 );
			system.OFFLINE = rs.getBoolean( 4 );
			system.MATCHED = rs.getBoolean( 5 );
			system.SV = rs.getInt( 6 );
			systems.add( system );
			
			DBSettings.loaddb( c , system.ID , props );
		}
		
		return( systems.toArray( new AppSystem[0] ) );
	}
	
	public static void resolvexml( EngineDirectory directory , AppSystem system ) throws Exception {
		for( Product product : system.getProducts() )
			DBProduct.resolvexml( directory , product );
	}

	public static void resolvedb( EngineDirectory directory , AppSystem system ) throws Exception {
		for( Product product : system.getProducts() )
			DBProduct.resolvedb( directory , product );
	}
	
	public static void matchxml( EngineDirectory directory , AppSystem system ) throws Exception {
		for( Product product : system.getProducts() )
			DBProduct.matchxml( directory , product );
		system.MATCHED = true;
	}
	
	public static void matchdb( EngineDirectory directory , EngineMatcher matcher , AppSystem system ) throws Exception {
		for( Product product : system.getProducts() )
			DBProduct.matchdb( directory , matcher , product );
	}
	
	public static void savexml( EngineDirectory directory , AppSystem system , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , system.NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , system.DESC );
		Common.xmlSetElementAttr( doc , root , "offline" , Common.getBooleanValue( system.OFFLINE ) );
		
		for( String productName : system.getProductNames() ) {
			Product product = system.findProduct( productName );
			Element elementProduct = Common.xmlCreateElement( doc , root , "product" );
			DBProduct.savexml( directory , product , doc , elementProduct );
		}
	}
	
	public static int getSystemIdByName( String name , DBConnection c ) throws Exception {
		return( DBNames.getNameIndex( c , DBVersions.CORE_ID , name , DBEnumObjectType.SYSTEM ) );
	}
	
	public static void savedb( EngineDirectory directory , AppSystem system , DBConnection c ) throws Exception {
		int systemId = getSystemIdByName( system.NAME , c );
		insert( c , systemId , system );
		
		for( Product product : system.getProducts() )
			DBProduct.savedb( directory , product , c );
	}
	
	public static void insert( DBConnection c , int systemId , AppSystem system ) throws Exception {
		system.ID = systemId;
		system.SV = c.getNextSystemVersion( systemId );
		if( !c.update( DBQueries.MODIFY_SYSTEM_ADD6 , new String[] {
				"" + system.ID , 
				EngineDB.getString( system.NAME ) , 
				EngineDB.getString( system.DESC ) ,
				EngineDB.getBoolean( system.OFFLINE ) ,
				EngineDB.getBoolean( system.MATCHED ) ,
				"" + system.SV 
				} ) )
			Common.exitUnexpected();
	}

	public static void update( DBConnection c , AppSystem system ) throws Exception {
		system.SV = c.getNextSystemVersion( system.ID );
		if( !c.update( DBQueries.MODIFY_SYSTEM_UPDATE4 , new String[] {
				"" + system.ID , 
				EngineDB.getString( system.NAME ) , 
				EngineDB.getString( system.DESC ) ,
				"" + system.SV 
				} ) )
			Common.exitUnexpected();
	}

	public static void delete( DBConnection c , AppSystem system ) throws Exception {
		int SV = c.getNextSystemVersion( system.ID );
		if( !c.update( DBQueries.MODIFY_SYSTEM_DELETEALLPARAMS2 , new String[] { "" + system.ID , "" + SV } ) )
			Common.exitUnexpected();
		if( !c.update( DBQueries.MODIFY_SYSTEM_DELETE2 , new String[] { "" + system.ID , "" + SV } ) )
			Common.exitUnexpected();
	}

}
