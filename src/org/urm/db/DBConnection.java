
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
import org.urm.meta.engine.Product;
import org.urm.meta.product.MetaEnv;

public class DBConnection {

	public Engine engine;
	public EngineEntities entities;
	public ActionBase action;
	
	private Connection connection;
	private Statement stmt;

	static int FAST_TIMEOUT = 5;

	private Map<Integer,OwnerObjectVersion> versions;
	
	public DBConnection( Engine engine , EngineEntities entities , ActionBase action , Connection connection ) {
		this.engine = engine;
		this.entities = entities;
		this.action = action;
		this.connection = connection;
		
		versions = new HashMap<Integer,OwnerObjectVersion>();
	}
	
	public void init() throws Exception {
		stmt = connection.createStatement();
	}
	
	public void close( boolean commit ) {
		try {
			save( commit );
			
			stmt.close();
			stmt = null;
			connection.close();
			connection = null;
		}
		catch( Throwable e ) {
			log( "close statement" , e );
		}
	}

	public void save( boolean commit ) {
		try {
			versions.clear();
			
			if( commit )
				connection.commit();
			else
				connection.rollback();
		}
		catch( Throwable e ) {
			log( "close statement" , e );
		}
	}

	public EngineEntities getEntities() {
		return( entities );
	}
	
	public String queryValue( String query ) {
		return( queryValue( query , null , FAST_TIMEOUT ) );
	}
	
	public String queryValue( String query , int timeout ) {
		return( queryValue( query , null , timeout ) );
	}
	
	public String queryValue( String query , String[] args ) {
		return( queryValue( query , args , FAST_TIMEOUT ) );
	}
	
	public String queryValue( String query , String[] args , int timeout ) {
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
	}
	
	public ResultSet query( String query ) throws Exception {
		return( query( query , null , FAST_TIMEOUT ) );
	}
	
	public ResultSet query( String query , int timeout ) throws Exception {
		return( query( query , null , timeout ) );
	}

	public ResultSet query( String query , String[] args ) {
		return( query( query , args , FAST_TIMEOUT ) );
	}
	
	public ResultSet query( String query , String[] args , int timeout ) {
		String queryDB = getFinalQuery( query , args );
		trace( "read query=" + queryDB + " ..." );
		ResultSet rs = null;
		try {
			stmt.setQueryTimeout( timeout );
			rs = stmt.executeQuery( queryDB );
		}
		catch( Throwable e ) {
			log( "read query failed, statement=" + query , e );
			return( null );
		}
		return( rs );
	}

	public boolean update( String query ) {
		return( update( query , null , FAST_TIMEOUT ) );
	}
	
	public boolean update( String query , int timeout ) {
		return( update( query , null , timeout ) );
	}

	public boolean update( String query , String[] args ) {
		return( update( query , args , FAST_TIMEOUT ) );
	}	
	
	public boolean update( String query , String[] args , int timeout ) {
		String queryDB = getFinalQuery( query , args );
		trace( "modify query=" + queryDB + " ..." );
		try {
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
	
	public synchronized int getLastObjectVersion( int objectId , DBEnumObjectVersionType type ) throws Exception {
		OwnerObjectVersion version = getObjectVersion( objectId , type );
		if( version.nextVersion > 0 )
			return( version.nextVersion );
		return( version.VERSION );
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
	
	public synchronized int getCurrentProductVersion( int productId ) throws Exception {
		return( getCurrentObjectVersion( productId , DBEnumObjectVersionType.PRODUCT ) );
	}
	
	public synchronized int getNextProductVersion( Product product , boolean delete ) throws Exception {
		OwnerObjectVersion version = getObjectVersion( product.ID , DBEnumObjectVersionType.PRODUCT );
		if( version.nextVersion < 0 ) {
			version.LAST_NAME = product.NAME;
			version.OWNER_STATUS_TYPE = ( delete )? DBEnumOwnerStatusType.DELETED : DBEnumOwnerStatusType.ACTIVE;
			DBVersions.setNextVersion( this , version , version.VERSION + 1 );
		}
		return( version.nextVersion );
	}
	
	public synchronized int getNextProductVersion( Product product ) throws Exception {
		return( getNextProductVersion( product , false ) );
	}
	
	public synchronized int getProductVersion( int productId ) throws Exception {
		return( getLastObjectVersion( productId , DBEnumObjectVersionType.PRODUCT ) );
	}
	
	public synchronized int getCurrentEnvironmentVersion( int envId ) throws Exception {
		return( getCurrentObjectVersion( envId , DBEnumObjectVersionType.ENVIRONMENT ) );
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
	
}
