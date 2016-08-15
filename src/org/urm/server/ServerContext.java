package org.urm.server;

import org.urm.common.PropertySet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerContext {

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

	public ServerContext() {
		properties = new PropertySet( "engine" , null );
	}

	public ServerContext copy() throws Exception {
		ServerContext r = new ServerContext();
		r.properties = properties.copy( null );
		r.scatterSystemProperties();
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		properties.loadRawFromNodeElements( root );
		scatterSystemProperties();
		properties.finishRawProperties();
	}

	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}
	
	private void scatterSystemProperties() throws Exception {
		CONNECTION_JMX_PORT = properties.getSystemStringProperty( CONNECTION_JMX_PORT , "6000" );
		CONNECTION_JMXWEB_PORT = properties.getSystemStringProperty( CONNECTION_JMXWEB_PORT , "6001" );

		JABBER_ACCOUNT = properties.getSystemStringProperty( "jabber.account" , "" );
		JABBER_PASSWORD = properties.getSystemStringProperty( "jabber.password" , "" );
		JABBER_SERVER = properties.getSystemStringProperty( "jabber.server" , "" );
		JABBER_CONFERENCESERVER = properties.getSystemStringProperty( "jabber.conferenceserver" , "" );
		JABBER_INCLUDE = properties.getSystemStringProperty( "jabber.include" , "" );
		JABBER_EXCLUDE = properties.getSystemStringProperty( "jabber.exclude" , "" );

		DISTR_PATH = properties.getSystemStringProperty( "distr.path" , "" );
	}

}
