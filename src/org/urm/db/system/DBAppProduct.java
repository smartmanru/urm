package org.urm.db.system;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppProductPolicy;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.ReleaseLifecycle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBAppProduct {

	public static AppProduct importxmlProduct( EngineLoader loader , EngineDirectory directory , AppSystem system , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDirectoryProduct;
		
		AppProduct product = new AppProduct( directory , system );
		product.createProduct(
				entity.importxmlStringAttr( root , AppProduct.PROPERTY_NAME ) , 
				entity.importxmlStringAttr( root , AppProduct.PROPERTY_DESC ) , 
				entity.importxmlStringAttr( root , AppProduct.PROPERTY_PATH )
				);
		product.setOffline( entity.importxmlBooleanAttr( root , AppProduct.PROPERTY_OFFLINE , true ) );
		product.setMonitoringEnabled( entity.importxmlBooleanAttr( root , AppProduct.PROPERTY_MONITORING_ENABLED , false ) );

		product.setVersions( 
				entity.importxmlIntProperty( root , AppProduct.PROPERTY_LAST_MAJOR_FIRST , 1 ) ,
				entity.importxmlIntProperty( root , AppProduct.PROPERTY_LAST_MAJOR_SECOND , 0 ) ,
				entity.importxmlIntProperty( root , AppProduct.PROPERTY_LAST_MINOR_FIRST , 0 ) ,
				entity.importxmlIntProperty( root , AppProduct.PROPERTY_LAST_MINOR_SECOND , 0 ) ,
				entity.importxmlIntProperty( root , AppProduct.PROPERTY_NEXT_MAJOR_FIRST , 1 ) ,
				entity.importxmlIntProperty( root , AppProduct.PROPERTY_NEXT_MAJOR_SECOND , 1 ) ,
				entity.importxmlIntProperty( root , AppProduct.PROPERTY_NEXT_MINOR_FIRST , 1 ) ,
				entity.importxmlIntProperty( root , AppProduct.PROPERTY_NEXT_MINOR_SECOND , 1 )
				);
		
		modifyProduct( c , product , true );

		return( product );
	}
	
	public static void exportxmlProduct( EngineLoader loader , AppProduct product , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDirectoryProduct;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( product.NAME ) ,
				entity.exportxmlString( product.DESC ) ,
				entity.exportxmlString( product.PATH ) ,
				entity.exportxmlBoolean( product.OFFLINE ) ,
				entity.exportxmlBoolean( product.MONITORING_ENABLED ) ,
				entity.exportxmlInt( product.LAST_MAJOR1 ) , 
				entity.exportxmlInt( product.LAST_MAJOR2 ) , 
				entity.exportxmlInt( product.LAST_MINOR1  ) , 
				entity.exportxmlInt( product.LAST_MINOR2 ) , 
				entity.exportxmlInt( product.NEXT_MAJOR1 ) , 
				entity.exportxmlInt( product.NEXT_MAJOR2 ) , 
				entity.exportxmlInt( product.NEXT_MINOR1 ) , 
				entity.exportxmlInt( product.NEXT_MINOR2 ) 
		} , true );
	}

	public static AppProduct[] loaddb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppDirectoryProduct;
		List<AppProduct> products = new LinkedList<AppProduct>();
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				int systemId = entity.loaddbInt( rs , DBEngineDirectory.FIELD_PRODUCT_SYSTEM_ID );
				AppSystem system = directory.getSystem( systemId );
				
				AppProduct product = new AppProduct( directory , system );
				product.ID = entity.loaddbId( rs );
				product.SV = entity.loaddbVersion( rs );
				
				product.createProduct( 
						entity.loaddbString( rs , AppProduct.PROPERTY_NAME ) , 
						entity.loaddbString( rs , AppProduct.PROPERTY_DESC ) ,
						entity.loaddbString( rs , AppProduct.PROPERTY_PATH )
						);
				product.setOffline( entity.loaddbBoolean( rs , AppProduct.PROPERTY_OFFLINE ) );
				product.setMonitoringEnabled( entity.loaddbBoolean( rs , AppProduct.PROPERTY_MONITORING_ENABLED ) );
				
				product.setVersions(
					entity.loaddbInt( rs , AppProduct.PROPERTY_LAST_MAJOR_FIRST ) ,
					entity.loaddbInt( rs , AppProduct.PROPERTY_LAST_MAJOR_SECOND ) ,
					entity.loaddbInt( rs , AppProduct.PROPERTY_LAST_MINOR_FIRST ) ,
					entity.loaddbInt( rs , AppProduct.PROPERTY_LAST_MINOR_SECOND ) ,
					entity.loaddbInt( rs , AppProduct.PROPERTY_NEXT_MAJOR_FIRST ) ,
					entity.loaddbInt( rs , AppProduct.PROPERTY_NEXT_MAJOR_SECOND ) ,
					entity.loaddbInt( rs , AppProduct.PROPERTY_NEXT_MINOR_FIRST ) ,
					entity.loaddbInt( rs , AppProduct.PROPERTY_NEXT_MINOR_SECOND )
				);
				
				products.add( product );
			}
		}
		finally {
			c.closeQuery();
		}
		
		return( products.toArray( new AppProduct[0] ) );
	}
	
	public static void modifyProduct( DBConnection c , AppProduct product , boolean insert ) throws Exception {
		if( insert )
			product.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , product.NAME , DBEnumParamEntityType.APPPRODUCT );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , product.NAME , product.ID , DBEnumParamEntityType.APPPRODUCT );
			
		product.SV = c.getNextSystemVersion( product.system );
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppDirectoryProduct;
		DBEngineEntities.modifyAppObject( c , entity , product.ID , product.SV , new String[] {
				EngineDB.getObject( product.system.ID ) ,
				EngineDB.getString( product.NAME ) , 
				EngineDB.getString( product.DESC ) , 
				EngineDB.getString( product.PATH ) ,
				EngineDB.getBoolean( product.OFFLINE ) ,
				EngineDB.getBoolean( product.MONITORING_ENABLED ) ,
				EngineDB.getInteger( product.LAST_MAJOR1 ) ,
				EngineDB.getInteger( product.LAST_MAJOR2 ) ,
				EngineDB.getInteger( product.LAST_MINOR1 ) ,
				EngineDB.getInteger( product.LAST_MINOR2 ) ,
				EngineDB.getInteger( product.NEXT_MAJOR1 ) ,
				EngineDB.getInteger( product.NEXT_MAJOR2 ) ,
				EngineDB.getInteger( product.NEXT_MINOR1 ) ,
				EngineDB.getInteger( product.NEXT_MINOR2 )
				} , insert );
	}

	public static void deleteProduct( DBConnection c , AppProduct product ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextSystemVersion( product.system );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductPolicyLifecycle , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		DBEngineEntities.deleteAppObject( c , entities.entityAppProductPolicy , product.ID , version );
		DBEngineEntities.deleteAppObject( c , entities.entityAppDirectoryProduct , product.ID , version );
	}

	public static void createdbPolicy( DBConnection c , EngineDirectory directory , AppProduct product ) throws Exception {
		AppProductPolicy policy = new AppProductPolicy( directory , product );
		product.setPolicy( policy );
		
		// policy record
		boolean urgentsAll = true;
		policy.setAttrs( urgentsAll );
		modifyPolicy( c , product , policy , true );
	}
	
	private static void modifyPolicy( DBConnection c , AppProduct product , AppProductPolicy policy , boolean insert ) throws Exception {
		policy.SV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppProductPolicy , product.ID , policy.SV , new String[] {
				EngineDB.getBoolean( policy.LCUrgentAll )
				} , insert );
	}
	
	public static void importxmlPolicy( EngineLoader loader , EngineDirectory directory , AppProduct product , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineLifecycles lifecycles = loader.getLifecycles();
		
		AppProductPolicy policy = new AppProductPolicy( directory , product );
		product.setPolicy( policy );
		
		// policy record
		boolean urgentsAll = ConfReader.getBooleanPropertyValue( root , AppProductPolicy.PROPERTY_RELEASELC_URGENTANY , false );
		policy.setAttrs( urgentsAll );
		modifyPolicy( c , product , policy , true );
		
		// lifecycle list
		String major = ConfReader.getPropertyValue( root , AppProductPolicy.PROPERTY_RELEASELC_MAJOR , "" );
		ReleaseLifecycle lcMajor = ( major.isEmpty() )? null : lifecycles.getLifecycle( major );
		
		String minor = ConfReader.getPropertyValue( root , AppProductPolicy.PROPERTY_RELEASELC_MINOR , "" );
		ReleaseLifecycle lcMinor = ( minor.isEmpty() )? null : lifecycles.getLifecycle( minor );
		
		String URGENTS = "";
		if( !urgentsAll )
			URGENTS = ConfReader.getPropertyValue( root , AppProductPolicy.PROPERTY_RELEASELC_URGENTS , "" );
		String[] urgents = Common.splitSpaced( URGENTS );
		ReleaseLifecycle[] lcUrgents = new ReleaseLifecycle[ urgents.length ];
		for( int k = 0; k < urgents.length; k++ )
			lcUrgents[ k ] = lifecycles.getLifecycle( urgents[ k ] );

		policy.setLifecycles( lcMajor , lcMinor , lcUrgents );
		
		modifyLifecycles( c , product , policy , true );
	}
	
	private static void modifyLifecycles( DBConnection c , AppProduct product , AppProductPolicy policy , boolean insert ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		if( !insert )
			DBEngineEntities.dropAppObjects( c , entities.entityAppProductPolicyLifecycle , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
			
		if( policy.hasMajor() )
			modifyLifecycle( c , product , policy.getMajorId() );
		if( policy.hasMinor() )
			modifyLifecycle( c , product , policy.getMinorId() );
		
		Integer[] urgents = policy.getUrgentIds();
		for( int k = 0; k < urgents.length; k++ )
			modifyLifecycle( c , product , urgents[ k ] );
	}

	private static void modifyLifecycle( DBConnection c , AppProduct product , int lcId ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextCoreVersion( );
		DBEngineEntities.modifyAppEntity( c , entities.entityAppProductPolicyLifecycle , version , new String[] { 
				EngineDB.getObject( product.ID ) ,
				EngineDB.getObject( lcId )
				} , true );
	}

	public static void exportxmlPolicy( EngineLoader loader , AppProduct product , Document doc , Element root ) throws Exception {
		AppProductPolicy policy = product.getPolicy();
		
		EngineLifecycles lifecycles = loader.getLifecycles();
		if( policy.hasMajor() ) {
			ReleaseLifecycle lc = lifecycles.getLifecycle( policy.getMajorId() );
			Common.xmlCreatePropertyElement( doc , root , AppProductPolicy.PROPERTY_RELEASELC_MAJOR , lc.NAME );
		}
		if( policy.hasMinor() ) {
			ReleaseLifecycle lc = lifecycles.getLifecycle( policy.getMinorId() );
			Common.xmlCreatePropertyElement( doc , root , AppProductPolicy.PROPERTY_RELEASELC_MINOR , lc.NAME );
		}
		
		Common.xmlCreateBooleanPropertyElement( doc , root , AppProductPolicy.PROPERTY_RELEASELC_URGENTANY , policy.LCUrgentAll );
		if( !policy.LCUrgentAll ) {
			String[] names = policy.getUrgentNames();
			Common.xmlCreatePropertyElement( doc , root , AppProductPolicy.PROPERTY_RELEASELC_URGENTS , Common.getList( names ) );
		}
	}

	public static void loaddbPolicy( EngineLoader loader , AppProduct product ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppProductPolicy;
		EngineLifecycles lifecycles = loader.getLifecycles();

		AppProductPolicy policy = new AppProductPolicy( product.directory , product );
		product.setPolicy( policy );
		
		// master attrs
		ResultSet rs = DBEngineEntities.listSingleAppObject( c , entity , product.ID );
		try {
			policy.SV = entity.loaddbVersion( rs );
			policy.setAttrs( entity.loaddbBoolean( rs , AppProductPolicy.PROPERTY_RELEASELC_URGENTANY ) );
		}
		finally {
			c.closeQuery();
		}

		// lifecycles
		ReleaseLifecycle major = null;
		ReleaseLifecycle minor = null;
		List<ReleaseLifecycle> urgents = new LinkedList<ReleaseLifecycle>();;
		
		entity = entities.entityAppProductPolicyLifecycle;
		rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		try {
			while( rs.next() ) {
				Integer id = c.getNullInt( rs , 1 );
				
				ReleaseLifecycle lc = lifecycles.getLifecycle( id );
				if( lc.isMajor() ) {
					if( major != null )
						Common.exitUnexpected();
					major = lc;
				}
				else
				if( lc.isMinor() ) {
					if( minor != null )
						Common.exitUnexpected();
					minor = lc;
				}
				else
				if( lc.isUrgent() )
					urgents.add( lc );
				else
					Common.exitUnexpected();
			}
		}
		finally {
			c.closeQuery();
		}
		
		policy.setLifecycles( major , minor , urgents.toArray( new ReleaseLifecycle[0] ) );
	}
	
	public static void setProductLifecycles( EngineTransaction transaction , AppProduct product , AppProductPolicy policy , String major , String minor , boolean urgentsAll , String[] urgents ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineLifecycles lifecycles = transaction.getLifecycles();
		
		ReleaseLifecycle lcMajor = ( major.isEmpty() )? null : lifecycles.getLifecycle( major );
		ReleaseLifecycle lcMinor = ( minor.isEmpty() )? null : lifecycles.getLifecycle( minor );
		ReleaseLifecycle[] lcUrgents = new ReleaseLifecycle[ 0 ];
		
		if( !urgentsAll ) {
			lcUrgents = new ReleaseLifecycle[ urgents.length ];
			for( int k = 0; k < urgents.length; k++ )
				lcUrgents[ k ] = lifecycles.getLifecycle( urgents[ k ] );
		}
		
		policy.setAttrs( urgentsAll );
		policy.setLifecycles( lcMajor , lcMinor , lcUrgents );
		
		modifyPolicy( c , product , policy , false );
		modifyLifecycles( c , product , policy , false );
	}

}
