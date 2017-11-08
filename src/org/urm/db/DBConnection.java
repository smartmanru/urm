package org.urm.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.Engine;

public class DBConnection {

	public Engine engine;
	public ActionBase action;
	
	private Connection connection;
	private Statement stmt;

	static int FAST_TIMEOUT = 5;
	
	public DBConnection( Engine engine , ActionBase action , Connection connection ) {
		this.engine = engine;
		this.action = action;
		this.connection = connection;
	}
	
	public void init() throws Exception {
		stmt = connection.createStatement();
	}
	
	public void close( boolean commit ) {
		try {
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
		trace( "update query=" + queryDB + " ..." );
		try {
			stmt.setQueryTimeout( timeout );
			stmt.executeUpdate( queryDB );
		}
		catch( Throwable e ) {
			log( "update query failed, statement=" + query , e );
			return( false );
		}
		return( true );
	}

	public void rollback() {
		try {
			connection.rollback();
		}
		catch( Throwable e ) {
			log( "rollback failed" , e );
		}
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
	
}
