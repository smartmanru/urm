package org.urm.server.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.action.CommandVar.FLAG;
import org.urm.server.action.ActionBase;
import org.urm.server.action.PropertySet;
import org.urm.server.storage.HiddenFiles;
import org.urm.server.storage.MetadataStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MetaEnv {

	boolean loaded = false;

	protected Metadata meta;

	PropertySet properties;
	PropertySet secretProperties;
	
	public boolean missingSecretProperties = false; 
	
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

	List<MetaEnvDC> originalList;
	Map<String,MetaEnvDC> dcMap;
	
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
	
	public void load( ActionBase action , MetadataStorage storage , String envFile ) throws Exception {
		if( loaded )
			return;

		loaded = true;

		// read xml
		String file = storage.getEnvFile( action , envFile );
		
		action.debug( "read environment definition file " + file + "..." );
		Document doc = action.readXmlFile( file );
		loadProperties( action , doc.getDocumentElement() );
		loadDatacenters( action , doc.getDocumentElement() );
		resolveLinks( action );
	}
	
	private void loadProperties( ActionBase action , Node node ) throws Exception {
		if( node == null || !node.getNodeName().equals( "env" ) )
			action.exit( "unable to find environment root node=env" );
		
		secretProperties = new PropertySet( "secret" , meta.product.props );
		properties = new PropertySet( "env" , secretProperties );
		properties.loadRawFromAttributes( action , node );
		
		CONF_SECRETFILESPATH = properties.getSystemPathProperty( action , "configuration-secretfilespath" , "" );
		
		HiddenFiles hidden = action.artefactory.getHiddenFiles();
		String propFile = hidden.getSecretPropertyFile( action , CONF_SECRETFILESPATH );
		
		boolean loadProps = false;
		if( !propFile.isEmpty() ) {
			loadProps = ( action.shell.checkFileExists( action , propFile ) )? true : false;
			if( !loadProps )
				missingSecretProperties = true;
		}
			
		scatterSystemProperties( action );
		
		if( loadProps ) {
			loadSecretProperties( action );
			properties.loadRawFromElements( action , node );
			properties.moveRawAsStrings( action );
		}
	}

	private void loadSecretProperties( ActionBase action ) throws Exception {
		HiddenFiles hidden = action.artefactory.getHiddenFiles();
		String propFile = hidden.getSecretPropertyFile( action , CONF_SECRETFILESPATH );
		if( propFile.isEmpty() )
			return;
		
		secretProperties.loadRawFromFile( action , propFile );
		secretProperties.moveRawAsStrings( action );
	}
	
	public String[] getPropertyList( ActionBase action ) throws Exception {
		return( properties.getOwnProperties( action ) );
	}

	public String getPropertyValue( ActionBase action , String var ) throws Exception {
		return( properties.getPropertyAny( action , var ) );
	}
	
	private void scatterSystemProperties( ActionBase action ) throws Exception {
		ID = properties.getSystemRequiredStringProperty( action , "id" );
		action.trace( "load properties of env=" + ID );
		
		BASELINE = properties.getSystemStringProperty( action , "configuration-baseline" , "" );
		REDISTPATH = properties.getSystemPathProperty( action , "redist-path" , meta.product.CONFIG_REDISTPATH );
		DISTR_USELOCAL = properties.getSystemBooleanProperty( action , "distr-use-local" , true );
		if( DISTR_USELOCAL )
			DISTR_HOSTLOGIN = action.context.account.getFullName();
		else
			DISTR_HOSTLOGIN = properties.getSystemStringProperty( action , "distr-hostlogin" , meta.product.CONFIG_DISTR_HOSTLOGIN );
		
		DISTR_PATH = properties.getSystemPathProperty( action , "distr-path" , meta.product.CONFIG_DISTR_PATH );
		UPGRADE_PATH = properties.getSystemPathProperty( action , "upgrade-path" , meta.product.CONFIG_UPGRADE_PATH );
		CHATROOMFILE = properties.getSystemPathProperty( action , "chatroomfile" , "" );
		KEYNAME = properties.getSystemPathProperty( action , "keyname" , "" );
		DB_AUTHFILE = properties.getSystemPathProperty( action , "db-authfile" , "" );
		PROD = properties.getSystemBooleanProperty( action , "prod" , false );

		// affect runtime options
		DB_AUTH = getOptionFlag( action , "db-auth" );
		OBSOLETE = getOptionFlag( action , "obsolete" );
		SHOWONLY = getOptionFlag( action , "showonly" );
		BACKUP = getOptionFlag( action , "backup" );
		CONF_DEPLOY = getOptionFlag( action , "configuration-deploy" );
		CONF_KEEPALIVE = getOptionFlag( action , "configuration-keepalive" );

		properties.finishRawProperties( action );
	}

	private FLAG getOptionFlag( ActionBase action , String envParam ) throws Exception {
		String value = properties.getSystemStringProperty( action , envParam , null );
		
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
	
	private void loadDatacenters( ActionBase action , Node node ) throws Exception {
		originalList = new LinkedList<MetaEnvDC>();
		dcMap = new HashMap<String,MetaEnvDC>();
		
		Node[] items = ConfReader.xmlGetChildren( node , "datacenter" );
		if( items == null )
			return;

		boolean loadProps = ( secretProperties == null )? false : true;
		for( Node dcnode : items ) {
			MetaEnvDC dc = new MetaEnvDC( meta , this );
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
			action.exit( "unknown datacenter=" + name );
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
