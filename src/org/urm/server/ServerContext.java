package org.urm.server;

import org.urm.common.PropertySet;
import org.urm.common.PropertyValue;
import org.urm.common.RunContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerContext {

	public ServerRegistry registry;
	public RunContext execrc;
	public PropertySet execprops;
	public PropertySet properties;

	// properties
	public int CONNECTION_JMX_PORT;
	public int CONNECTION_JMXWEB_PORT;
	
	public String DIST_ROOT;
	public String DIST_APPFOLDER;
	public String DIST_BASEPATH;
	
	public String SECURE_AUTHPATH;
	public String SECURE_CONFPATH;
	
	public String JABBER_ACCOUNT;
	public String JABBER_PASSWORD;
	public String JABBER_SERVER;
	public String JABBER_CONFERENCESERVER;
	public String JABBER_INCLUDE;
	public String JABBER_EXCLUDE;

	public String DISTR_PATH;

	// xml field names
	public static String PROPERTY_CONNECTION_JMX_PORT = "connection.jmx.port";
	public static String PROPERTY_CONNECTION_JMXWEB_PORT = "connection.jmxweb.port";

	public static String PROPERTY_DIST_ROOT = "dist.root";
	public static String PROPERTY_DIST_APPFOLDER = "dist.appfolder";
	public static String PROPERTY_DIST_BASEPATH = "dist.basepath";

	public static String PROPERTY_SECURE_AUTHPATH = "secure.authpath";
	public static String PROPERTY_SECURE_CONFPATH = "secure.confpath";
	
	private ServerContext() {
	}
	
	public ServerContext( ServerRegistry registry ) {
		this.registry = registry;
		this.execrc = null;
		
		execprops = new PropertySet( "execrc" , null );
		properties = new PropertySet( "engine" , execprops );
	}

	public ServerContext copy() throws Exception {
		ServerContext r = new ServerContext();
		r.registry = registry;
		r.execrc = execrc;
		r.execprops = execprops.copy( null );
		r.properties = properties.copy( r.execprops );
		r.scatterSystemProperties();
		return( r );
	}
	
	public void load( Node root , RunContext execrc ) throws Exception {
		this.execrc = execrc;

		execrc.getProperties( execprops );
		properties.loadRawFromNodeElements( root );
		scatterSystemProperties();
		properties.finishRawProperties();
	}

	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}
	
	private void scatterSystemProperties() throws Exception {
		CONNECTION_JMX_PORT = properties.getSystemIntProperty( PROPERTY_CONNECTION_JMX_PORT , 6000 );
		CONNECTION_JMXWEB_PORT = properties.getSystemIntProperty( PROPERTY_CONNECTION_JMXWEB_PORT , 6001 );

		DIST_ROOT = properties.getSystemPathProperty( PROPERTY_DIST_ROOT , execrc.installPath + "/dist" , execrc );
		DIST_APPFOLDER = properties.getSystemPathProperty( PROPERTY_DIST_APPFOLDER , "systems" , execrc );
		DIST_BASEPATH = properties.getSystemPathProperty( PROPERTY_DIST_BASEPATH , "base" , execrc );

		SECURE_AUTHPATH = properties.getSystemPathProperty( PROPERTY_SECURE_AUTHPATH , execrc.userHome + "/.auth" , execrc );
		SECURE_CONFPATH = properties.getSystemPathProperty( PROPERTY_SECURE_CONFPATH , execrc.installPath + "/secured" , execrc );
		
		JABBER_ACCOUNT = properties.getSystemStringProperty( "jabber.account" , "" );
		JABBER_PASSWORD = properties.getSystemStringProperty( "jabber.password" , "" );
		JABBER_SERVER = properties.getSystemStringProperty( "jabber.server" , "" );
		JABBER_CONFERENCESERVER = properties.getSystemStringProperty( "jabber.conferenceserver" , "" );
		JABBER_INCLUDE = properties.getSystemStringProperty( "jabber.include" , "" );
		JABBER_EXCLUDE = properties.getSystemStringProperty( "jabber.exclude" , "" );

		DISTR_PATH = properties.getSystemStringProperty( "distr.path" , "" );
	}

	public void setRegistryServerProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		for( String prop : props.getOriginalProperties() ) {
			PropertyValue pv = properties.getOwnByProperty( prop );
			if( pv == null )
				transaction.exit( "unknown property: " + prop );
			
			String value = props.getOriginalByProperty( prop );
			properties.updateOriginalProperty( prop , value );
		}
	}

	public void resolveRegistryServerProperties( ServerTransaction transaction ) throws Exception {
		properties.resolveRawProperties();
		scatterSystemProperties();
	}
	
}
