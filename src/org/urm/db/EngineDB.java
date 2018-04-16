package org.urm.db;

import java.io.File;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.postgresql.ds.PGConnectionPoolDataSource;
import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnumInterface;
import org.urm.db.core.DBEnums;
import org.urm.engine.Engine;
import org.urm.meta.MatchItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;

public class EngineDB {

	private Engine engine;
	
	private PGConnectionPoolDataSource pool;
	public static int APP_VERSION = 1020303;
	
	public EngineDB( Engine engine ) {
		this.engine = engine;
	}
	
	public void init() throws Exception {
		if( pool != null ) {
			pool.setReadOnly( true );
			pool = null;
		}
		
		String jdbcProperties = engine.execrc.dbPath;
		File poolFile = new File( jdbcProperties );
		if( !poolFile.isFile() )
			throw new RuntimeException( "missing database configuration file=" + jdbcProperties );
		
		Properties props = ConfReader.readPropertyFile( engine.execrc , jdbcProperties );
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
			engine.trace( "connecting to " + host + ":" + port + "/" + db + "[" + schema + "] as user=" + user + " ..." );
			connection = getConnection( engine.serverAction );
		}
		finally {
			if( connection != null )
				connection.close( true );			
		}
	}

	public DBConnection getConnection( ActionBase action ) throws Exception {
		Connection connection = pool.getConnection();
		connection.setAutoCommit( false );
		DBConnection dbc = new DBConnection( engine , action.getServerEntities() , action , connection );
		dbc.init();
		return( dbc );
	}

	public void releaseConnection( DBConnection connection ) throws Exception {
		if( connection != null )
			connection.close( true );
	}

	public static String getInteger( int value ) {
		return( "" + value );
	}

	public static String getObject( Integer value ) {
		if( value == null )
			return( "null" );
		return( "" + value );
	}

	public static String getMatchId( MatchItem item ) {
		if( item == null )
			return( "null" );
		return( getObject( item.FKID ) );
	}

	public static String getMatchName( MatchItem item ) {
		if( item == null )
			return( "null" );
		return( getString( item.FKNAME ) );
	}

	public static String getBooleanString( String value ) {
		if( value == null || value.isEmpty() )
			return( "null" );
		return( "'" + value + "'" );
	}

	public static String getIntegerString( String value ) {
		if( value == null || value.isEmpty() )
			return( "null" );
		return( value );
	}

	public static String getEnumString( String value ) {
		if( value == null || value.isEmpty() )
			return( "" + DBEnums.VALUE_UNKNOWN );
		return( value );
	}

	public static String getString( String value ) {
		if( value == null || value.isEmpty() )
			return( "null" );
		return( "'" + value + "'" );
	}

	public static String getDate( Date value ) {
		if( value == null )
			return( "null" );
		DateFormat format = new SimpleDateFormat( "yyyyMMdd" );
		String dateString = format.format( value );
		return( "to_date('" + dateString + "','YYYYMMDD')" );
	}
	
	public static String getLong( Long value ) {
		if( value == null )
			return( "null" );
		return( "" + value );
	}
	
	public static String getBoolean( boolean value ) {
		return( ( value )? "'yes'" : "'no'" );
	}

	public static String getEnum( DBEnumInterface item ) {
		if( item == null )
			return( "" + DBEnums.VALUE_UNKNOWN );
		return( "" + item.code() );
	}

	public static String getObject( MetaDistrBinaryItem item ) {
		return( getObject( Meta.getObject( item ) ) );
	}
	
	public static String getObject( MetaDistrConfItem item ) {
		return( getObject( Meta.getObject( item ) ) );
	}
	
	public static String getObject( MetaDatabaseSchema schema ) {
		return( getObject( Meta.getObject( schema ) ) );
	}

}
