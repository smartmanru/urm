package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.CommandOptions.FLAG;
import ru.egov.urm.storage.MetadataStorage;

public class MetaEnv {

	boolean loaded = false;

	Metadata meta;

	PropertySet properties;
	PropertySet secretProperties;
	
	public String ID;
	private String BASELINE;
	public String REDISTPATH;
	public boolean DISTR_USELOCAL;
	public String DISTR_HOSTLOGIN;
	public String DISTR_PATH;
	public String UPGRADE_PATH;
	public String CONF_SECRETPROPERTYFILE;
	public String CONF_SECRETFILESPATH;
	public String CHATROOMFILE;
	public String KEYNAME;
	public String DB_AUTHFILE;
	public boolean PROD;
	
	// properties, affecting options
	public FLAG DB_AUTH;
	public FLAG OBSOLETE;
	public FLAG SHOWONLY;
	public FLAG BACKUP;
	public FLAG CONF_DEPLOY;
	public FLAG CONF_KEEPALIVE;

	public static String SECRETPROPERTYFILE = "secret.properties";
	
	List<MetaEnvDC> originalList;
	Map<String,MetaEnvDC> dcMap;
	List<String> systemProps;
	
	public MetaEnv( Metadata meta ) {
		this.meta = meta;
	}
	
	public boolean hasBaseline( ActionBase action ) throws Exception {
		if( BASELINE.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getBaselineFile( ActionBase action ) throws Exception {
		return( BASELINE + ".xml" );
	}
	
	public void load( ActionBase action , MetadataStorage storage , String envFile , boolean loadProps ) throws Exception {
		systemProps = new LinkedList<String>();
		if( loaded )
			return;

		loaded = true;

		// read xml
		String file = storage.getEnvFile( action , envFile );
		
		action.debug( "read environment definition file " + file + "..." );
		Document doc = ConfReader.readXmlFile( action , file );
		loadProperties( action , doc.getDocumentElement() , loadProps );
		loadDatacenters( action , doc.getDocumentElement() , loadProps );
		resolveLinks( action );
	}
	
	private void loadProperties( ActionBase action , Node node , boolean loadProps ) throws Exception {
		if( node == null || !node.getNodeName().equals( "env" ) )
			action.exit( "unable to find environment root node=env" );
		
		secretProperties = new PropertySet( "secret" , meta.product.props ); 		
		properties = new PropertySet( "env" , secretProperties );
		properties.loadFromAttributes( action , node );
		scatterSystemProperties( action );
		
		if( loadProps )
			properties.loadFromElements( action , node );
	}

	public void loadSecretProperties( ActionBase action ) throws Exception {
		if( !action.context.CTX_HIDDENPATH.isEmpty() ) {
			String propFile = Common.getPath( action.context.CTX_HIDDENPATH , SECRETPROPERTYFILE );
			secretProperties.loadFromFile( action , propFile );
		}
	}
	
	public String[] getPropertyList( ActionBase action ) throws Exception {
		return( properties.getOwnProperties( action ) );
	}

	public String getPropertyValue( ActionBase action , String var ) throws Exception {
		return( properties.getProperty( action , var ) );
	}
	
	private void scatterSystemProperties( ActionBase action ) throws Exception {
		systemProps.clear();
		
		ID = properties.getSystemRequiredProperty( action , "id" , systemProps );
		action.trace( "load properties of env=" + ID );
		
		CONF_SECRETFILESPATH = properties.getSystemProperty( action , "configuration-secretfilespath" , "" , systemProps );
		BASELINE = properties.getSystemProperty( action , "configuration-baseline" , "" , systemProps );
		REDISTPATH = properties.getSystemProperty( action , "redist-path" , meta.product.CONFIG_REDISTPATH , systemProps );
		DISTR_USELOCAL = properties.getSystemBooleanProperty( action , "distr-use-local" , true , systemProps );
		if( DISTR_USELOCAL )
			DISTR_HOSTLOGIN = action.context.account.HOSTLOGIN;
		else
			DISTR_HOSTLOGIN = properties.getSystemProperty( action , "distr-hostlogin" , meta.product.CONFIG_DISTR_HOSTLOGIN , systemProps );
		
		DISTR_PATH = properties.getSystemProperty( action , "distr-path" , meta.product.CONFIG_DISTR_PATH , systemProps );
		UPGRADE_PATH = properties.getSystemProperty( action , "upgrade-path" , meta.product.CONFIG_UPGRADE_PATH , systemProps );
		CHATROOMFILE = properties.getSystemProperty( action , "chatroomfile" , "" , systemProps );
		KEYNAME = properties.getSystemProperty( action , "keyname" , "" , systemProps );
		DB_AUTHFILE = properties.getSystemProperty( action , "db-authfile" , "" , systemProps );
		PROD = properties.getSystemBooleanProperty( action , "prod" , false , systemProps );

		// affect runtime options
		DB_AUTH = getOptionFlag( action , "db-auth" );
		OBSOLETE = getOptionFlag( action , "obsolete" );
		SHOWONLY = getOptionFlag( action , "showonly" );
		BACKUP = getOptionFlag( action , "backup" );
		CONF_DEPLOY = getOptionFlag( action , "configuration-deploy" );
		CONF_KEEPALIVE = getOptionFlag( action , "configuration-keepalive" );

		properties.checkUnexpected( action , systemProps );
	}

	private FLAG getOptionFlag( ActionBase action , String envParam ) throws Exception {
		systemProps.add( properties.set + "." + envParam );
		String value = properties.findProperty( action , envParam , null );
		
		FLAG retval;
		if( value == null || value.isEmpty() )
			retval = FLAG.DEFAULT;
		else {
			if( Common.getBooleanValue( value ) )
				retval = FLAG.YES;
			else
				retval = FLAG.NO;
		}
		
		return( retval );
	}
	
	private void loadDatacenters( ActionBase action , Node node , boolean loadProps ) throws Exception {
		originalList = new LinkedList<MetaEnvDC>();
		dcMap = new HashMap<String,MetaEnvDC>();
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "datacenter" );
		if( items == null )
			return;
		
		for( Node dcnode : items ) {
			MetaEnvDC dc = new MetaEnvDC( this );
			dc.load( action , dcnode , loadProps );
			originalList.add( dc );
			dcMap.put( dc.NAME , dc );
		}
	}

	private void resolveLinks( ActionBase action ) throws Exception {
		for( MetaEnvDC dc : originalList )
			dc.resolveLinks( action );
	}
	
	public MetaEnvDC getDC( ActionBase action , String name ) throws Exception {
		MetaEnvDC dc = dcMap.get( name );
		if( dc == null )
			action.exit( "unknown datacenter=" + dc );
		return( dc );
	}

	public List<MetaEnvDC> getOriginalDCList( ActionBase action ) throws Exception {
		return( originalList );
	}
	
	public Map<String,MetaEnvDC> getDCMap( ActionBase action ) throws Exception {
		return( dcMap );
	}

	public boolean isMultiDC( ActionBase action ) throws Exception {
		return( originalList.size() > 1 );
	}
	
	public MetaEnvDC getMainDC( ActionBase action ) throws Exception {
		if( originalList.size() == 0 )
			action.exit( "no datacenter defined" );
		if( originalList.size() > 1 )
			action.exitUnexpectedState();
		return( originalList.get( 0 ) );
	}
	
}
