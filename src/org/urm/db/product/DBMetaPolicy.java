package org.urm.db.product;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.MatchItem;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.MetaProductPolicy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaPolicy {

	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		MetaProductPolicy policy = new MetaProductPolicy( storage , storage.meta );
		storage.setPolicy( policy );
		
		String major = ConfReader.getPropertyValue( root , MetaProductPolicy.PROPERTY_RELEASELC_MAJOR , "" );
		String minor = ConfReader.getPropertyValue( root , MetaProductPolicy.PROPERTY_RELEASELC_MINOR , "" );
		boolean urgentsAll = ConfReader.getBooleanPropertyValue( root , MetaProductPolicy.PROPERTY_RELEASELC_URGENTANY , false );
		
		String URGENTS = "";
		if( !urgentsAll )
			URGENTS = ConfReader.getPropertyValue( root , MetaProductPolicy.PROPERTY_RELEASELC_URGENTS , "" );
		String[] urgents = Common.splitSpaced( URGENTS );
		
		MatchItem RELEASELC_MAJOR = modifyLifecycle( loader , storage , major , 1 );
		MatchItem RELEASELC_MINOR = modifyLifecycle( loader , storage , minor , 2 );
		
		MatchItem[] RELEASELC_URGENT_LIST;
		if( !urgentsAll ) {
			RELEASELC_URGENT_LIST = new MatchItem[ urgents.length ];
			for( int k = 0; k < urgents.length; k++ )
				RELEASELC_URGENT_LIST[ k ] = modifyLifecycle( loader , storage , urgents[ k ] , 3 + k );
		}
		else
			RELEASELC_URGENT_LIST = new MatchItem[0];
		
		policy.setLifecycles( RELEASELC_MAJOR , RELEASELC_MINOR , urgentsAll , RELEASELC_URGENT_LIST );
	}

	private static MatchItem modifyLifecycle( EngineLoader loader , ProductMeta storage , String name , int index ) throws Exception {
		DBConnection c = loader.getConnection();
		
		EngineLifecycles lifecycles = loader.getLifecycles();
		if( name.isEmpty() )
			return( null );
		
		MatchItem lcMatch;
		ReleaseLifecycle lc = lifecycles.findLifecycle( name );
		if( lc == null )
			lcMatch = new MatchItem( name );
		else
			lcMatch = new MatchItem( lc.ID );
		
		modifyLifecycle( c , storage , lcMatch , index );
		return( lcMatch );
	}

	public static void modifyLifecycle( DBConnection c , ProductMeta storage , MatchItem item , int index ) throws Exception {
		if( !c.modify( DBQueries.MODIFY_METALC_ADD5 , new String[] { 
				EngineDB.getInteger( storage.ID ) ,
				EngineDB.getInteger( index ) ,
				EngineDB.getInteger( item.FKID ) ,
				EngineDB.getString( item.FKNAME ) ,
				EngineDB.getInteger( c.getNextProductVersion( storage.product ) )
				} ) )
			Common.exitUnexpected();
	}

	public static void exportxml( EngineLoader loader , ProductMeta storage , MetaProductPolicy policy , Document doc , Element root ) throws Exception {
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

	public static void setProductLifecycles( EngineTransaction transaction , ProductMeta storage , MetaProductPolicy policy , String major , String minor , boolean urgentsAll , String [] urgents ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( !c.modify( DBQueries.MODIFY_METALC_DELETEALL1 , new String[] { 
				EngineDB.getInteger( policy.meta.getId() )
				} ) )
			Common.exitUnexpected();
		
		EngineLifecycles lifecycles = transaction.getLifecycles();
		MatchItem lcMajor = null;
		if( !major.isEmpty() ); {
			lcMajor = new MatchItem( lifecycles.getLifecycle( major ).ID );
			modifyLifecycle( c , storage , lcMajor , 1 );
		}
		MatchItem lcMinor = null;
		if( !minor.isEmpty() ) {
			lcMinor = new MatchItem( lifecycles.getLifecycle( minor ).ID );
			modifyLifecycle( c , storage , lcMinor , 2 );
		}
		
		MatchItem[] lcs = ( urgentsAll )? new MatchItem[0] : new MatchItem[ urgents.length ];
		if( !urgentsAll ) {
			for( int k = 0; k < urgents.length; k++ ) {
				ReleaseLifecycle lc = lifecycles.getLifecycle( urgents[ k ] );
				lcs[ k ] = new MatchItem( lc.ID );
				modifyLifecycle( c , storage , lcs[ k ] , k + 3 );
			}
		}
		
		policy.setLifecycles( lcMajor , lcMinor , urgentsAll , lcs );
	}
	
}
