
package org.urm.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumOwnerStatusType;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.engine.Engine;
import org.urm.engine.properties.EngineEntities;
import org.urm.meta.OwnerObjectVersion;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.product.ProductMeta;

public class DBConnection {

	static int FAST_TIMEOUT = 5;

	public Engine engine;
	public EngineEntities entities;
	public ActionBase action;
	
	private Connection connection;
	private Statement stmt;
	private ResultSet rs;

	private boolean pendingUpdates;
	private Map<Integer,OwnerObjectVersion> versions;
	
	public DBConnection( Engine engine , EngineEntities entities , ActionBase action , Connection connection ) {
		this.engine = engine;
		this.entities = entities;
		this.action = action;
		this.connection = connection;
		
		versions = new HashMap<Integer,OwnerObjectVersion>();
		pendingUpdates = false;
	}
	
	public void init() throws Exception {
		action.trace( "connection created" );
		stmt = connection.createStatement();
	}
	
	public void close( boolean commit ) {
		try {
			if( pendingUpdates )
				save( commit );
			
			stmt.close();
			stmt = null;
			connection.close();
			connection = null;
			action.trace( "connection closed" );
		}
		catch( Throwable e ) {
			log( "close statement" , e );
		}
	}

	public void save( boolean commit ) {
		try {
			versions.clear();
			closeQuery();
			
			if( commit ) {
				connection.commit();
				action.trace( "commit transaction" );
			}
			else {
				connection.rollback();
				action.trace( "rollback transaction" );
			}
		}
		catch( Throwable e ) {
			log( "close statement" , e );
		}
		
		pendingUpdates = false;
	}

	public EngineEntities getEntities() {
		return( entities );
	}
	
	public String queryValue( String query ) throws Exception {
		return( queryValue( query , null , FAST_TIMEOUT ) );
	}
	
	public String queryValue( String query , int timeout ) throws Exception {
		return( queryValue( query , null , timeout ) );
	}
	
	public String queryValue( String query , String[] args ) throws Exception {
		return( queryValue( query , args , FAST_TIMEOUT ) );
	}
	
	public String queryValue( String query , String[] args , int timeout ) throws Exception {
		ResultSet set = query( query , args , timeout );
		if( set == null )
			return( null );

		try {
			if( !set.next() )
				return( null );
			
			String value = set.getString( 1 );
			if( value == null )
				return( "" );
			return( value );
		}
		catch( Throwable e ) {
			log( "query value read failed" , e );
			return( null );
		}
		finally {
			closeQuery();
		}
	}
	
	public ResultSet query( String query ) throws Exception {
		return( query( query , null , FAST_TIMEOUT ) );
	}
	
	public ResultSet query( String query , int timeout ) throws Exception {
		return( query( query , null , timeout ) );
	}

	public ResultSet query( String query , String[] args ) throws Exception {
		return( query( query , args , FAST_TIMEOUT ) );
	}
	
	public ResultSet query( String query , String[] args , int timeout ) throws Exception {
		if( rs != null )
			Common.exitUnexpected();
			
		String queryDB = getFinalQuery( query , args );
		trace( "read query=" + queryDB + " ..." );
		rs = null;
		try {
			stmt.setQueryTimeout( timeout );
			rs = stmt.executeQuery( queryDB );
		}
		catch( Throwable e ) {
			log( "read query failed, statement=" + query , e );
			Common.exitUnexpected();
		}
		return( rs );
	}
	
	public void closeQuery() {
		try {
			if( rs != null ) {
				rs.close();
				rs = null;
			}
		}
		catch( Throwable e ) {
			log( "close query failed" , e );
		}
	}

	public boolean modify( String query ) {
		return( modify( query , null , FAST_TIMEOUT ) );
	}
	
	public boolean modify( String query , int timeout ) {
		return( modify( query , null , timeout ) );
	}

	public boolean modify( String query , String[] args ) {
		return( modify( query , args , FAST_TIMEOUT ) );
	}	
	
	public boolean modify( String query , String[] args , int timeout ) {
		String queryDB = getFinalQuery( query , args );
		trace( "modify query=" + queryDB + " ..." );
		try {
			pendingUpdates = true;
			stmt.setQueryTimeout( timeout );
			stmt.executeUpdate( queryDB );
		}
		catch( Throwable e ) {
			log( "modify query failed, statement=" + query , e );
			return( false );
		}
		return( true );
	}

	private String getFinalQuery( String query , String[] args ) {
		String queryDB = query;
		if( args != null ) {
			if( queryDB.contains( "@values@" ) ) {
				String list = "";
				for( int k = 1; k <= args.length; k++ )
					list = Common.addToList( list , args[ k - 1 ] , " , " );
				queryDB = Common.replace( queryDB , "@values@" , list );
			}
		else {
			for( int k = 1; k <= args.length; k++ )
				queryDB = Common.replace( queryDB , "@" + k + "@" , args[ k - 1 ] );
		}
		}
		return( queryDB );
	}

	private void trace( String s ) {
		if( action != null )
			action.trace( s );
		else
			engine.trace( s );
	}

	private void log( String p , Throwable e ) {
		if( action != null )
			action.log( p ,  e );
		else
			engine.log( p , e );
	}

	public synchronized OwnerObjectVersion getObjectVersion( int objectId , DBEnumObjectVersionType type ) throws Exception {
		OwnerObjectVersion version = versions.get( objectId );
		if( version != null )
			return( version );
		
		version = DBVersions.readObjectVersion( this , objectId , type );
		versions.put( objectId , version );
		return( version );
	}
	
	public synchronized int getCurrentObjectVersion( int objectId , DBEnumObjectVersionType type ) throws Exception {
		OwnerObjectVersion version = getObjectVersion( objectId , type );
		return( version.VERSION );
	}
	
	public synchronized int getLastObjectVersion( int objectId , DBEnumObjectVersionType type ) throws Exception {
		OwnerObjectVersion version = getObjectVersion( objectId , type );
		if( version.nextVersion > 0 )
			return( version.nextVersion );
		return( version.VERSION );
	}
	
	public synchronized int getCurrentAppVersion() throws Exception {
		return( getCurrentObjectVersion( DBVersions.APP_ID , DBEnumObjectVersionType.APP ) );
	}
	
	public synchronized void setAppVersion( int value ) throws Exception {
		OwnerObjectVersion version = getObjectVersion( DBVersions.APP_ID , DBEnumObjectVersionType.APP );
		if( version.VERSION == 0 ) {
			version.LAST_NAME = "app";
			version.OWNER_STATUS_TYPE = DBEnumOwnerStatusType.ACTIVE;
		}
		DBVersions.setNextVersion( this , version , value );
	}

	public synchronized int getCurrentCoreVersion() throws Exception {
		return( getCurrentObjectVersion( DBVersions.CORE_ID , DBEnumObjectVersionType.CORE ) );
	}
	
	public synchronized int getNextCoreVersion() throws Exception {
		OwnerObjectVersion version = getObjectVersion( DBVersions.CORE_ID , DBEnumObjectVersionType.CORE );
		if( version.nextVersion < 0 ) {
			if( version.VERSION == 0 ) {
				version.LAST_NAME = "core";
				version.OWNER_STATUS_TYPE = DBEnumOwnerStatusType.ACTIVE;
			}
			DBVersions.setNextVersion( this , version , version.VERSION + 1 );
		}
		return( version.nextVersion );
	}
	
	public synchronized int getCoreVersion() throws Exception {
		return( getLastObjectVersion( DBVersions.CORE_ID , DBEnumObjectVersionType.CORE ) );
	}
	
	public synchronized int getCurrentLocalVersion() throws Exception {
		return( getCurrentObjectVersion( DBVersions.LOCAL_ID , DBEnumObjectVersionType.LOCAL ) );
	}
	
	public synchronized int getNextLocalVersion() throws Exception {
		OwnerObjectVersion version = getObjectVersion( DBVersions.LOCAL_ID , DBEnumObjectVersionType.LOCAL );
		if( version.nextVersion < 0 ) {
			if( version.VERSION == 0 ) {
				version.LAST_NAME = "auth";
				version.OWNER_STATUS_TYPE = DBEnumOwnerStatusType.ACTIVE;
			}
			DBVersions.setNextVersion( this , version , version.VERSION + 1 );
		}
		return( version.nextVersion );
	}
	
	public synchronized int getLocalVersion() throws Exception {
		return( getLastObjectVersion( DBVersions.LOCAL_ID , DBEnumObjectVersionType.LOCAL ) );
	}
	
	public synchronized int getCurrentSystemVersion( int systemId ) throws Exception {
		return( getCurrentObjectVersion( systemId , DBEnumObjectVersionType.SYSTEM ) );
	}

	public synchronized int getNextSystemVersion( AppSystem system ) throws Exception {
		return( getNextSystemVersion( system , false ) );
	}
	
	public synchronized int getNextSystemVersion( AppSystem system , boolean delete ) throws Exception {
		OwnerObjectVersion version = getObjectVersion( system.ID , DBEnumObjectVersionType.SYSTEM );
		if( version.nextVersion < 0 ) {
			version.LAST_NAME = system.NAME;
			version.OWNER_STATUS_TYPE = ( delete )? DBEnumOwnerStatusType.DELETED : DBEnumOwnerStatusType.ACTIVE;
			DBVersions.setNextVersion( this , version , version.VERSION + 1 );
		}
		return( version.nextVersion );
	}
	
	public synchronized int getSystemVersion( int systemId ) throws Exception {
		return( getLastObjectVersion( systemId , DBEnumObjectVersionType.SYSTEM ) );
	}
	
	public synchronized int getCurrentProductVersion( ProductMeta storage ) throws Exception {
		return( getCurrentObjectVersion( storage.ID , DBEnumObjectVersionType.PRODUCT ) );
	}
	
	public synchronized int getNextProductVersion( ProductMeta storage , boolean delete ) throws Exception {
		OwnerObjectVersion version = getObjectVersion( storage.ID , DBEnumObjectVersionType.PRODUCT );
		if( version.nextVersion < 0 ) {
			version.LAST_NAME = storage.name;
			version.OWNER_STATUS_TYPE = ( delete )? DBEnumOwnerStatusType.DELETED : DBEnumOwnerStatusType.ACTIVE;
			DBVersions.setNextVersion( this , version , version.VERSION + 1 );
		}
		return( version.nextVersion );
	}
	
	public synchronized int getNextProductVersion( ProductMeta storage ) throws Exception {
		return( getNextProductVersion( storage , false ) );
	}
	
	public synchronized int getProductVersion( ProductMeta storage ) throws Exception {
		return( getLastObjectVersion( storage.ID , DBEnumObjectVersionType.PRODUCT ) );
	}
	
	public synchronized int getCurrentEnvironmentVersion( int envId ) throws Exception {
		return( getCurrentObjectVersion( envId , DBEnumObjectVersionType.ENVIRONMENT ) );
	}

	public synchronized int getCurrentEnvironmentVersion( MetaEnv env ) throws Exception {
		return( getCurrentEnvironmentVersion( env.ID ) );
	}
	
	public synchronized int getNextEnvironmentVersion( MetaEnv env ) throws Exception {
		return( getNextEnvironmentVersion( env , false ) );
	}
	
	public synchronized int getNextEnvironmentVersion( MetaEnv env , boolean delete ) throws Exception {
		OwnerObjectVersion version = getObjectVersion( env.ID , DBEnumObjectVersionType.ENVIRONMENT );
		if( version.nextVersion < 0 ) {
			version.LAST_NAME = env.NAME;
			version.OWNER_STATUS_TYPE = ( delete )? DBEnumOwnerStatusType.DELETED : DBEnumOwnerStatusType.ACTIVE;
			DBVersions.setNextVersion( this , version , version.VERSION + 1 );
		}
		return( version.nextVersion );
	}
	
	public synchronized int getEnvironmentVersion( int envId ) throws Exception {
		return( getLastObjectVersion( envId , DBEnumObjectVersionType.ENVIRONMENT ) );
	}

	public Integer getNullInt( ResultSet rc , int column ) throws Exception {
		int value = rc.getInt( column );
		if( value == 0 )
			return( null );
		return( value );
	}
	
	public int getNextSequenceValue() throws Exception {
		String value = queryValue( DBQueries.QUERY_SEQ_GETNEXTVAL0 );
		if( value == null )
			Common.exitUnexpected();
		return( Integer.parseInt( value ) );
	}
	
}
