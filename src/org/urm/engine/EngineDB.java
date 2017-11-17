package org.urm.engine;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;

import org.postgresql.ds.PGConnectionPoolDataSource;
import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBEnums;
import org.urm.db.DBNames;
import org.urm.meta.EngineData;

public class EngineDB {

	public EngineData data;
	
	private PGConnectionPoolDataSource pool;
	
	public EngineDB( EngineData data ) {
		this.data = data;
	}
	
	public void init() throws Exception {
		if( pool != null ) {
			pool.setReadOnly( true );
			pool = null;
		}
		
		String jdbcProperties = data.engine.execrc.dbPath;
		File poolFile = new File( jdbcProperties );
		if( !poolFile.isFile() )
			throw new RuntimeException( "missing database configuration file=" + jdbcProperties );
		
		Properties props = ConfReader.readPropertyFile( data.engine.execrc , jdbcProperties );
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
			
		DBConnection connection = null;
		try {
			data.engine.trace( "connecting to " + host + ":" + port + "/" + db + "[" + schema + "] as user=" + user + " ..." );
			connection = getConnection( data.engine.serverAction );
			data.engine.trace( "checking client/server consistency ..." );
			initData( connection );
		}
		finally {
			if( connection != null )
				connection.close( true );			
		}
	}

	public DBConnection getConnection( ActionBase action ) throws Exception {
		Connection connection = pool.getConnection();
		connection.setAutoCommit( false );
		DBConnection dbc = new DBConnection( data.engine , action , connection );
		dbc.init();
		return( dbc );
	}

	public void releaseConnection( DBConnection connection ) throws Exception {
		if( connection != null )
			connection.close( true );
	}

	public void clearProduct( String productName ) {
	}

	public boolean getBoolean( String value ) {
		if( value == null || value.equals( "Y" ) == false )
			return( false );
		return( true );
	}

	public static String getString( String value ) {
		if( value == null || value.isEmpty() )
			return( "null" );
		return( "'" + value + "'" );
	}

	public static String getBoolean( boolean value ) {
		return( ( value )? "'yes'" : "'no'" );
	}

	public void initData( DBConnection connection ) throws Exception {
		DBNames.load( connection );
		
		boolean dbUpdate = Common.getBooleanValue( System.getProperty( "dbupdate" ) );
		if( dbUpdate )
			DBEnums.updateDatabase( data.engine , connection );
		else
			DBEnums.verifyDatabase( data.engine , connection );
	}
	
}
