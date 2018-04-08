package org.urm.db.product;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumChangeType;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineMatcher;
import org.urm.meta.MatchItem;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.MetaProductPolicy;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaPolicy {

	public static void createdb( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		
		MetaProductPolicy policy = new MetaProductPolicy( storage , storage.meta );
		storage.setPolicy( policy );
		
		// policy record
		boolean urgentsAll = true;
		policy.setAttrs( urgentsAll );
		modifyPolicy( c , storage , policy , true , DBEnumChangeType.CREATED );
	}
	
	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineLifecycles lifecycles = loader.getLifecycles();
		
		MetaProductPolicy policy = new MetaProductPolicy( storage , storage.meta );
		storage.setPolicy( policy );
		
		// policy record
		boolean urgentsAll = ConfReader.getBooleanPropertyValue( root , MetaProductPolicy.PROPERTY_RELEASELC_URGENTANY , false );
		policy.setAttrs( urgentsAll );
		modifyPolicy( c , storage , policy , true , DBEnumChangeType.CREATED );
		
		// lifecycle list
		String major = ConfReader.getPropertyValue( root , MetaProductPolicy.PROPERTY_RELEASELC_MAJOR , "" );
		String minor = ConfReader.getPropertyValue( root , MetaProductPolicy.PROPERTY_RELEASELC_MINOR , "" );
		
		String URGENTS = "";
		if( !urgentsAll )
			URGENTS = ConfReader.getPropertyValue( root , MetaProductPolicy.PROPERTY_RELEASELC_URGENTS , "" );
		String[] urgents = Common.splitSpaced( URGENTS );
		
		EngineMatcher matcher = loader.getMatcher();
		major = matcher.matchProductBefore( storage , major , storage.ID , entities.entityAppMetaPolicy , MetaProductPolicy.PROPERTY_RELEASELC_MAJOR , null );
		MatchItem RELEASELC_MAJOR = lifecycles.matchLifecycle( major );
		matcher.matchProductDone( RELEASELC_MAJOR );
		
		major = matcher.matchProductBefore( storage , minor , storage.ID , entities.entityAppMetaPolicy , MetaProductPolicy.PROPERTY_RELEASELC_MINOR , null );
		MatchItem RELEASELC_MINOR = lifecycles.matchLifecycle( minor );
		matcher.matchProductDone( RELEASELC_MINOR );
		
		MatchItem[] RELEASELC_URGENT_LIST;
		if( !urgentsAll ) {
			RELEASELC_URGENT_LIST = new MatchItem[ urgents.length ];
			for( int k = 0; k < urgents.length; k++ ) {
				String name = urgents[ k ];
				name = matcher.matchProductBefore( storage , name , storage.ID , entities.entityAppMetaPolicy , MetaProductPolicy.PROPERTY_RELEASELC_URGENTS , "" + k );
				MatchItem item = lifecycles.matchLifecycle( name );
				matcher.matchProductDone( item );
				RELEASELC_URGENT_LIST[ k ] = item;
			}
		}
		else
			RELEASELC_URGENT_LIST = new MatchItem[0];
		
		policy.setLifecycles( RELEASELC_MAJOR , RELEASELC_MINOR , RELEASELC_URGENT_LIST );
		
		modifyLifecycles( c , storage , policy );
	}
	
	private static void modifyLifecycles( DBConnection c , ProductMeta storage , MetaProductPolicy policy ) throws Exception {
		if( policy.LC_MAJOR != null )
			modifyLifecycle( c , storage , policy.LC_MAJOR , 1 , DBEnumChangeType.CREATED );
		if( policy.LC_MINOR != null )
			modifyLifecycle( c , storage , policy.LC_MINOR , 2 , DBEnumChangeType.CREATED );
		
		for( int k = 0; k < policy.LC_URGENT_LIST.length; k++ ) {
			MatchItem lc = policy.LC_URGENT_LIST[ k ];
			modifyLifecycle( c , storage , lc , k + 3 , DBEnumChangeType.CREATED );
		}
	}

	private static void modifyPolicy( DBConnection c , ProductMeta storage , MetaProductPolicy policy , boolean insert , DBEnumChangeType type ) throws Exception {
		policy.PV = c.getNextProductVersion( storage );
		policy.CHANGETYPE = type;
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppMetaPolicy , storage.ID , policy.PV , new String[] {
				EngineDB.getBoolean( policy.LCUrgentAll )
				} , insert , type );
	}
	
	public static void modifyLifecycle( DBConnection c , ProductMeta storage , MatchItem item , int index , DBEnumChangeType type ) throws Exception {
		EngineEntities entities = c.getEntities();
		int version = c.getNextProductVersion( storage );
		DBEngineEntities.modifyAppEntity( c , entities.entityAppMetaPolicyLifecycle , version , new String[] { 
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getInteger( index ) ,
				EngineDB.getMatchId( item ) ,
				EngineDB.getMatchName( item )
				} , true , type );
	}

	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		MetaProductPolicy policy = storage.getPolicy();
		
		EngineLifecycles lifecycles = loader.getLifecycles();
		if( policy.LC_MAJOR != null ) {
			ReleaseLifecycle lc = lifecycles.getLifecycle( policy.LC_MAJOR.FKID );
			Common.xmlCreatePropertyElement( doc , root , MetaProductPolicy.PROPERTY_RELEASELC_MAJOR , lc.NAME );
		}
		if( policy.LC_MINOR != null ) {
			ReleaseLifecycle lc = lifecycles.getLifecycle( policy.LC_MINOR.FKID );
			Common.xmlCreatePropertyElement( doc , root , MetaProductPolicy.PROPERTY_RELEASELC_MINOR , lc.NAME );
		}
		
		Common.xmlCreateBooleanPropertyElement( doc , root , MetaProductPolicy.PROPERTY_RELEASELC_URGENTANY , policy.LCUrgentAll );
		if( !policy.LCUrgentAll ) {
			String[] names = new String[ policy.LC_URGENT_LIST.length ];
			for( int k = 0; k < names.length; k++ ) {
				ReleaseLifecycle lc = lifecycles.getLifecycle( policy.LC_URGENT_LIST[ k ].FKID );
				names[ k ] = lc.NAME; 
			}
			Common.xmlCreatePropertyElement( doc , root , MetaProductPolicy.PROPERTY_RELEASELC_URGENTS , Common.getList( names ) );
		}
	}

	public static void loaddb( EngineLoader loader , ProductMeta storage ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppMetaPolicy;

		MetaProductPolicy policy = new MetaProductPolicy( storage , storage.meta );
		storage.setPolicy( policy );
		
		// master attrs
		ResultSet rs = DBEngineEntities.listSingleAppObject( c , entity , storage.ID );
		try {
			policy.PV = entity.loaddbVersion( rs );
			policy.CHANGETYPE = entity.loaddbChangeType( rs );
			policy.setAttrs( entity.loaddbBoolean( rs , MetaProductPolicy.PROPERTY_RELEASELC_URGENTANY ) );
		}
		finally {
			c.closeQuery();
		}

		// lifecycles
		MatchItem RELEASELC_MAJOR = null;
		MatchItem RELEASELC_MINOR = null;
		List<MatchItem> list = new LinkedList<MatchItem>();

		EngineMatcher matcher = loader.getMatcher();
		EngineLifecycles lifecycles = loader.getLifecycles();
		entity = entities.entityAppMetaPolicyLifecycle;
		rs = DBEngineEntities.listAppObjectsFiltered( c , entity , DBQueries.FILTER_META_ID1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		try {
			while( rs.next() ) {
				int index = rs.getInt( 2 );
				Integer id = c.getNullInt( rs , 3 );
				String name = rs.getString( 4 );
				
				MatchItem lcMatch = lifecycles.matchLifecycle( id , name );
				
				if( index == 1 ) {
					RELEASELC_MAJOR = lcMatch;
					matcher.matchProductDone( lcMatch , storage , storage.ID , entity , MetaProductPolicy.PROPERTY_RELEASELC_MAJOR , null );
				}
				else
				if( index == 2 ) {
					RELEASELC_MINOR = lcMatch;
					matcher.matchProductDone( lcMatch , storage , storage.ID , entity , MetaProductPolicy.PROPERTY_RELEASELC_MINOR , null );
				}
				else {
					list.add( lcMatch );
					matcher.matchProductDone( lcMatch , storage , storage.ID , entity , MetaProductPolicy.PROPERTY_RELEASELC_URGENTS , "" + index );
				}
			}
		}
		finally {
			c.closeQuery();
		}
		
		policy.setLifecycles( RELEASELC_MAJOR , RELEASELC_MINOR , list.toArray( new MatchItem[0] ) );
	}
	
	public static void setProductLifecycles( EngineTransaction transaction , ProductMeta storage , MetaProductPolicy policy , String major , String minor , boolean urgentsAll , String [] urgents ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		EngineLifecycles lifecycles = transaction.getLifecycles();
		MatchItem lcMajor = lifecycles.matchLifecycle( null , major );
		MatchItem lcMinor = lifecycles.matchLifecycle( null , minor );
		
		MatchItem[] lcs = ( urgentsAll )? new MatchItem[0] : new MatchItem[ urgents.length ];
		if( !urgentsAll ) {
			for( int k = 0; k < urgents.length; k++ )
				lcs[ k ] = lifecycles.matchLifecycle( null , urgents[ k ] );
		}
		
		policy.setAttrs( urgentsAll );
		policy.setLifecycles( lcMajor , lcMinor , lcs );
		
		modifyPolicy( c , storage , policy , false , DBEnumChangeType.UPDATED );
		
		DBEngineEntities.dropAppObjects( c , entities.entityAppMetaPolicyLifecycle , DBQueries.FILTER_META_ID1 , new String[] { EngineDB.getInteger( policy.meta.getId() ) } );
		modifyLifecycles( c , storage , policy );
	}

}
