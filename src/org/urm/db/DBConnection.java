package org.urm.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBEnums.DBEnumObjectVersionType;
import org.urm.engine.Engine;

public class DBConnection {

	public Engine engine;
	public ActionBase action;
	
	private Connection connection;
	private Statement stmt;

	static int FAST_TIMEOUT = 5;

	private int currentCV;
	private int nextCV;
	private Map<Integer,Integer> currentVersions;
	private Map<Integer,Integer> nextVersions;
	
	public DBConnection( Engine engine , ActionBase action , Connection connection ) {
		this.engine = engine;
		this.action = action;
		this.connection = connection;
		
		currentCV = -1;
		nextCV = -1;
		currentVersions = new HashMap<Integer,Integer>();
		nextVersions = new HashMap<Integer,Integer>();
	}
	
	public void init() throws Exception {
		stmt = connection.createStatement();
	}
	
	public void close( boolean commit ) {
		try {
			currentCV = -1;
			nextCV = -1;
			currentVersions.clear();
			nextVersions.clear();
			
			if( commit )
				connection.commit();
			else
				connection.rollback();
			
			stmt.close();
			stmt = null;
			connection.close();
			connection = null;
		}
		catch( Throwable e ) {
			log( "close statement" , e );
		}
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
			for( int k = 1; k <= args.length; k++ )
				queryDB = Common.replace( queryDB , "@" + k + "@" , args[ k - 1 ] );
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

	public synchronized int getCurrentCoreVersion() throws Exception {
		if( currentCV >= 0 )
			return( currentCV );
		currentCV = DBVersions.getCurrentCoreVersion( this );
		return( currentCV );
	}
	
	public synchronized int getNextCoreVersion() throws Exception {
		if( nextCV > 0 )
			return( nextCV );
		
		nextCV = getCurrentCoreVersion() + 1;
		DBVersions.setNextCoreVersion( this , nextCV );
		return( nextCV );
	}
	
	public synchronized int getCoreVersion() throws Exception {
		if( nextCV > 0 )
			return( nextCV );
		return( getCurrentCoreVersion() );
	}
	
	public synchronized void setNextCoreVersion() throws Exception {
		getNextCoreVersion();
	}

	public synchronized int getCurrentSystemVersion( int systemId ) throws Exception {
		Integer current = currentVersions.get( systemId );
		if( current != null )
			return( current );
		
		current = DBVersions.getCurrentVersion( this , systemId );
		currentVersions.put( systemId , current );
		return( current );
	}
	
	public synchronized int getNextSystemVersion( int systemId ) throws Exception {
		Integer next = nextVersions.get( systemId );
		if( next != null )
			return( next );
		
		next = getCurrentSystemVersion( systemId ) + 1;
		DBVersions.setNextVersion( this , systemId , next , DBEnumObjectVersionType.SYSTEM );
		nextVersions.put( systemId , next );
		return( next );
	}
	
	public synchronized int getSystemVersion( int systemId ) throws Exception {
		Integer next = nextVersions.get( systemId );
		if( next != null )
			return( next );
		return( getCurrentSystemVersion( systemId ) );
	}
	
	public synchronized int getCurrentProductVersion( int productId ) throws Exception {
		Integer current = currentVersions.get( productId );
		if( current != null )
			return( current );
		
		current = DBVersions.getCurrentVersion( this , productId );
		currentVersions.put( productId , current );
		return( current );
	}
	
	public synchronized int getNextProductVersion( int productId ) throws Exception {
		Integer next = nextVersions.get( productId );
		if( next != null )
			return( next );
		
		next = getCurrentSystemVersion( productId ) + 1;
		DBVersions.setNextVersion( this , productId , next , DBEnumObjectVersionType.PRODUCT );
		nextVersions.put( productId , next );
		return( next );
	}
	
	public synchronized int getProductVersion( int productId ) throws Exception {
		Integer next = nextVersions.get( productId );
		if( next != null )
			return( next );
		return( getCurrentProductVersion( productId ) );
	}
	
	public synchronized int getCurrentEnvVersion( int envId ) throws Exception {
		Integer current = currentVersions.get( envId );
		if( current != null )
			return( current );
		
		current = DBVersions.getCurrentVersion( this , envId );
		currentVersions.put( envId , current );
		return( current );
	}
	
	public synchronized int getNextEnvVersion( int envId ) throws Exception {
		Integer next = nextVersions.get( envId );
		if( next != null )
			return( next );
		
		next = getCurrentSystemVersion( envId ) + 1;
		DBVersions.setNextVersion( this , envId , next , DBEnumObjectVersionType.ENVIRONMENT );
		nextVersions.put( envId , next );
		return( next );
	}
	
	public synchronized int getEnvVersion( int envId ) throws Exception {
		Integer next = nextVersions.get( envId );
		if( next != null )
			return( next );
		return( getCurrentProductVersion( envId ) );
	}
	
}
