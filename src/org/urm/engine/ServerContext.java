package org.urm.engine;

import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerContext {

	public ServerSettings settings;
	public RunContext execrc;
	public PropertySet execprops;
	public PropertySet properties;

	// properties
	public int CONNECTION_JMX_PORT;
	public int CONNECTION_JMXWEB_PORT;

	// storage
	public String DIST_ROOT;
	public String DIST_APPFOLDER;
	public String DIST_BASEPATH;
	
	public String SECURE_CONFPATH;
	
	public String WORK_ARTEFACTS;
	public String WORK_MIRRORPATH;
	public String WORK_BUILDLOGS;
	public String WORK_DEPLOYLOGS;

	public String STAGING_LINUXPATH;
	public String STAGING_WINPATH;
	
	public String MON_RESOURCES;
	public String MON_DATAPATH;
	public String MON_LOGPATH;

	public int SHELL_SILENTMAX;
	public int SHELL_UNAVAILABLE_SKIPTIME;
	public int SHELL_HOUSEKEEP_TIME;

	public boolean CHAT_USING;
	public String CHAT_TYPE;
	public static String CHAT_TYPE_JABBER = "jabber";
	public static String CHAT_TYPE_ROCKET = "rocket";
	public String CHAT_JABBER_RESOURCE;
	public String CHAT_JABBER_CONFERENCESERVER;
	public String CHAT_ROCKET_RESOURCE;
	
	// xml field names
	public static String PROPERTY_CONNECTION_JMX_PORT = "connection.jmx.port";
	public static String PROPERTY_CONNECTION_JMXWEB_PORT = "connection.jmxweb.port";

	public static String PROPERTY_DIST_ROOT = "dist.root";
	public static String PROPERTY_DIST_APPFOLDER = "dist.appfolder";
	public static String PROPERTY_DIST_BASEPATH = "dist.basepath";

	public static String PROPERTY_SECURE_CONFPATH = "secure.confpath";

	public static String PROPERTY_WORK_ARTEFACTS = "work.artefacts";
	public static String PROPERTY_WORK_MIRRORPATH = "work.mirrorpath";
	public static String PROPERTY_WORK_BUILDLOGS = "work.buildlogs";
	public static String PROPERTY_WORK_DEPLOYLOGS = "work.deploylogs";

	public static String PROPERTY_STAGING_LINUXPATH = "staging.linuxpath";
	public static String PROPERTY_STAGING_WINPATH = "staging.winpath";
	
	public static String PROPERTY_MON_RESOURCES = "mon.respath";
	public static String PROPERTY_MON_DATAPATH = "mon.datapath";
	public static String PROPERTY_MON_LOGPATH = "mon.logpath";

	public static String PROPERTY_SHELL_SILENTMAX = "shell.silentmax";
	public static String PROPERTY_SHELL_UNAVAILABLE_SKIPTIME = "shell.unavailable";
	public static String PROPERTY_SHELL_HOUSEKEEP_TIME = "shell.housekeep";

	public static String PROPERTY_CHAT_USING = "chat.using";
	public static String PROPERTY_CHAT_TYPE = "chat.type";
	public static String PROPERTY_CHAT_JABBER_RESOURCE = "chat.jabber.resource";
	public static String PROPERTY_CHAT_JABBER_CONFERENCESERVER = "chat.jabber.conferenceserver";
	public static String PROPERTY_CHAT_ROCKET_RESOURCE = "chat.rocket.resource";
	
	private ServerContext() {
	}
	
	public ServerContext( ServerSettings settings ) {
		this.settings = settings;
		this.execrc = null;
		
		execprops = new PropertySet( "execrc" , null );
		properties = new PropertySet( "engine" , execprops );
	}

	public ServerContext copy() throws Exception {
		ServerContext r = new ServerContext();
		r.settings = settings;
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

		SECURE_CONFPATH = properties.getSystemPathProperty( PROPERTY_SECURE_CONFPATH , execrc.installPath + "/secured" , execrc );
		
		WORK_ARTEFACTS = properties.getSystemPathProperty( PROPERTY_WORK_ARTEFACTS , execrc.installPath + "/artefacts" , execrc );
		WORK_MIRRORPATH = properties.getSystemPathProperty( PROPERTY_WORK_MIRRORPATH , execrc.installPath + "/mirror" , execrc );
		WORK_BUILDLOGS = properties.getSystemPathProperty( PROPERTY_WORK_BUILDLOGS , execrc.installPath + "/logs/build" , execrc );
		WORK_DEPLOYLOGS = properties.getSystemPathProperty( PROPERTY_WORK_DEPLOYLOGS , execrc.installPath + "/logs/deploy" , execrc );

		STAGING_LINUXPATH = properties.getSystemPathProperty( PROPERTY_STAGING_LINUXPATH , "/redist" , execrc );
		STAGING_WINPATH = properties.getSystemPathProperty( PROPERTY_STAGING_WINPATH , "C:/redist" , execrc );
		
		MON_RESOURCES = properties.getSystemPathProperty( PROPERTY_MON_RESOURCES , execrc.installPath + "/monitoring/resources" , execrc );
		MON_DATAPATH = properties.getSystemPathProperty( PROPERTY_MON_DATAPATH , execrc.installPath + "/monitoring/data" , execrc );
		MON_LOGPATH = properties.getSystemPathProperty( PROPERTY_MON_LOGPATH , execrc.installPath + "/logs/monitoring" , execrc );

		SHELL_SILENTMAX = properties.getSystemIntProperty( PROPERTY_SHELL_SILENTMAX , 60000 );
		SHELL_UNAVAILABLE_SKIPTIME = properties.getSystemIntProperty( PROPERTY_SHELL_UNAVAILABLE_SKIPTIME , 30000 );
		SHELL_HOUSEKEEP_TIME = properties.getSystemIntProperty( PROPERTY_SHELL_HOUSEKEEP_TIME , 30000 );
		
		CHAT_USING = properties.getSystemBooleanProperty( PROPERTY_CHAT_USING , false );
		CHAT_TYPE = properties.getSystemStringProperty( PROPERTY_CHAT_TYPE , "" );
		CHAT_JABBER_RESOURCE = properties.getSystemStringProperty( PROPERTY_CHAT_JABBER_RESOURCE , "" );
		CHAT_JABBER_CONFERENCESERVER = properties.getSystemStringProperty( PROPERTY_CHAT_JABBER_CONFERENCESERVER , "" );
		CHAT_ROCKET_RESOURCE = properties.getSystemStringProperty( PROPERTY_CHAT_ROCKET_RESOURCE , "" );
	}

	public void setServerProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		properties.updateProperties( props );
	}

	public void resolveServerProperties( ServerTransaction transaction ) throws Exception {
		properties.resolveRawProperties();
		scatterSystemProperties();
	}
	
}