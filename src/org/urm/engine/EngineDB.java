package org.urm.engine;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.postgresql.ds.PGConnectionPoolDataSource;
import org.urm.common.ConfReader;
import org.urm.meta.EngineLoader;

public class EngineDB {

	EngineLoader loader;
	
	private PGConnectionPoolDataSource pool;
	
	public EngineDB( EngineLoader loader ) {
		this.loader = loader;
	}
	
	public void init() throws Exception {
		if( pool != null ) {
			pool.setReadOnly( true );
			pool = null;
		}
		
		String jdbcProperties = loader.engine.execrc.dbPath;
		File poolFile = new File( jdbcProperties );
		if( !poolFile.isFile() )
			throw new RuntimeException( "missing database configuration file=" + jdbcProperties );
		
		Properties props = ConfReader.readPropertyFile( loader.engine.execrc , jdbcProperties );
		String host = props.getProperty( "host" );
		String port = props.getProperty( "port" );
		String db = props.getProperty( "db" , "urmdb" );
		String schema = props.getProperty( "schema" , "main" );
		String user = props.getProperty( "user" );
		String password = props.getProperty( "password" );

		pool = new PGConnectionPoolDataSource();
		pool.setPassword( password );
		pool.setUser( user );
		pool.setServerName( host );
		pool.setPortNumber( Integer.parseInt( port ));
		pool.setDatabaseName( db );
		pool.setCurrentSchema( schema );
		pool.setDefaultAutoCommit( false );
			
		Connection connection = null;
		try {
			connection = pool.getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select * from urm_system where 1 = 2" );
			rs.next();
		}
		finally {
			if( connection != null )
				connection.close();			
		}
	}

	public Connection getConnection() throws Exception {
		Connection connection = pool.getConnection();
		connection.setAutoCommit( false );
		return( connection );
	}

	public void releaseConnection( Connection connection ) throws Exception {
		if( connection != null )
			connection.close();			
	}

	public void clearServer() {
	}
	
	public void clearProduct( String productName ) {
	}

	public boolean getBoolean( String value ) {
		if( value == null || value.equals( "Y" ) == false )
			return( false );
		return( true );
	}

	public String getQuoted( String value ) {
		return( "'" + value + "'" );
	}

	public String getBoolean( boolean value ) {
		return( ( value )? "'Y'" : "'N'" );
	}
	
}
