package org.urm.server;

import org.urm.common.PropertySet;
import org.urm.common.PropertyValue;
import org.urm.common.RunContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerContext {

	public ServerRegistry registry;
	public PropertySet execprops;
	public PropertySet properties;
	
	public String CONNECTION_JMX_PORT;
	public String CONNECTION_JMXWEB_PORT;
	
	public String JABBER_ACCOUNT;
	public String JABBER_PASSWORD;
	public String JABBER_SERVER;
	public String JABBER_CONFERENCESERVER;
	public String JABBER_INCLUDE;
	public String JABBER_EXCLUDE;

	public String DISTR_PATH;

	public static String PROPERTY_CONNECTION_JMX_PORT = "connection.jmx.port";
	public static String PROPERTY_CONNECTION_JMXWEB_PORT = "connection.jmxweb.port";

	private ServerContext() {
	}
	
	public ServerContext( ServerRegistry registry ) {
		this.registry = registry;
		
		execprops = new PropertySet( "execrc" , null );
		properties = new PropertySet( "engine" , execprops );
	}

	public ServerContext copy() throws Exception {
		ServerContext r = new ServerContext();
		r.registry = registry;
		r.execprops = execprops.copy( null );
		r.properties = properties.copy( r.execprops );
		r.scatterSystemProperties();
		return( r );
	}
	
	public void load( RunContext execrc , Node root ) throws Exception {
		execrc.getProperties( execprops );
		properties.loadRawFromNodeElements( root );
		scatterSystemProperties();
		properties.finishRawProperties();
	}

	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}
	
	private void scatterSystemProperties() throws Exception {
		CONNECTION_JMX_PORT = properties.getSystemStringProperty( PROPERTY_CONNECTION_JMX_PORT , "6000" );
		CONNECTION_JMXWEB_PORT = properties.getSystemStringProperty( PROPERTY_CONNECTION_JMXWEB_PORT , "6001" );

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
			properties.setOriginalProperty( prop , pv.type , value );
		}
	}

	public void resolveRegistryServerProperties( ServerTransaction transaction ) throws Exception {
		properties.resolveRawProperties();
		scatterSystemProperties();
	}
	
}
