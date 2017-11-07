package org.urm.meta.engine;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.PropertySet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineContext {

	public EngineSettings settings;
	public RunContext execrc;
	public PropertySet execprops;
	public PropertySet properties;

	// properties
	public int CONNECTION_JMX_PORT;
	public int CONNECTION_JMXWEB_PORT;

	// storage
	public String DIST_ROOT;
	public String DIST_APPFOLDER;
	public String DIST_PLATFORMPATH;
	
	public String SECURE_CONFPATH;
	
	public String WORK_ARTEFACTS;
	public String WORK_MIRRORPATH;
	public String WORK_BUILDLOGS;
	public String WORK_DEPLOYLOGS;

	public String STAGING_LINUXPATH;
	public String STAGING_WINPATH;
	
	public String MON_RESPATH;
	public String MON_RESURL;
	public String MON_DATAPATH;
	public String MON_REPORTPATH;
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
	public static String PROPERTY_DIST_PLATFORMPATH = "dist.basepath";

	public static String PROPERTY_SECURE_CONFPATH = "secure.confpath";

	public static String PROPERTY_WORK_ARTEFACTS = "work.artefacts";
	public static String PROPERTY_WORK_MIRRORPATH = "work.mirrorpath";
	public static String PROPERTY_WORK_BUILDLOGS = "work.buildlogs";
	public static String PROPERTY_WORK_DEPLOYLOGS = "work.deploylogs";

	public static String PROPERTY_STAGING_LINUXPATH = "staging.linuxpath";
	public static String PROPERTY_STAGING_WINPATH = "staging.winpath";
	
	public static String PROPERTY_MON_RESPATH = "mon.respath";
	public static String PROPERTY_MON_RESURL = "mon.resurl";
	public static String PROPERTY_MON_DATAPATH = "mon.datapath";
	public static String PROPERTY_MON_REPORTPATH = "mon.reportpath";
	public static String PROPERTY_MON_LOGPATH = "mon.logpath";

	public static String PROPERTY_SHELL_SILENTMAX = "shell.silentmax";
	public static String PROPERTY_SHELL_UNAVAILABLE_SKIPTIME = "shell.unavailable";
	public static String PROPERTY_SHELL_HOUSEKEEP_TIME = "shell.housekeep";

	public static String PROPERTY_CHAT_USING = "chat.using";
	public static String PROPERTY_CHAT_TYPE = "chat.type";
	public static String PROPERTY_CHAT_JABBER_RESOURCE = "chat.jabber.resource";
	public static String PROPERTY_CHAT_JABBER_CONFERENCESERVER = "chat.jabber.conferenceserver";
	public static String PROPERTY_CHAT_ROCKET_RESOURCE = "chat.rocket.resource";
	
	private EngineContext() {
	}
	
	public EngineContext( EngineSettings settings ) {
		this.settings = settings;
		this.execrc = null;
		
		execprops = new PropertySet( "execrc" , null );
		properties = new PropertySet( "engine" , execprops );
	}

	public EngineContext copy() throws Exception {
		EngineContext r = new EngineContext();
		r.settings = settings;
		r.execrc = execrc;
		r.execprops = execprops.copy( null );
		r.properties = properties.copy( r.execprops );
		r.scatterSystemProperties();
		return( r );
	}
	
	public void load( Node root , RunContext execrc ) throws Exception {
		this.execrc = execrc;

		getExecProperties( execprops );
		properties.loadFromNodeElements( root , false );
		scatterSystemProperties();
		properties.finishRawProperties();
	}

	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root , false );
	}
	
	private void scatterSystemProperties() throws Exception {
		CONNECTION_JMX_PORT = properties.getSystemIntProperty( PROPERTY_CONNECTION_JMX_PORT , 6000 , true );
		CONNECTION_JMXWEB_PORT = properties.getSystemIntProperty( PROPERTY_CONNECTION_JMXWEB_PORT , 6001 , true );

		DIST_ROOT = properties.getSystemPathProperty( PROPERTY_DIST_ROOT , execrc , execrc.installPath + "/dist" , false );
		DIST_APPFOLDER = properties.getSystemPathProperty( PROPERTY_DIST_APPFOLDER , execrc , "systems" , false );
		DIST_PLATFORMPATH = properties.getSystemPathProperty( PROPERTY_DIST_PLATFORMPATH , execrc , "platform" , false );

		SECURE_CONFPATH = properties.getSystemPathProperty( PROPERTY_SECURE_CONFPATH , execrc , execrc.installPath + "/secured" , false );
		
		WORK_ARTEFACTS = properties.getSystemPathProperty( PROPERTY_WORK_ARTEFACTS , execrc , execrc.installPath + "/artefacts" , false );
		WORK_MIRRORPATH = properties.getSystemPathProperty( PROPERTY_WORK_MIRRORPATH , execrc , execrc.installPath + "/mirror" , false );
		WORK_BUILDLOGS = properties.getSystemPathProperty( PROPERTY_WORK_BUILDLOGS , execrc , execrc.installPath + "/logs/build" , false );
		WORK_DEPLOYLOGS = properties.getSystemPathProperty( PROPERTY_WORK_DEPLOYLOGS , execrc , execrc.installPath + "/logs/deploy" , false );

		STAGING_LINUXPATH = properties.getSystemPathProperty( PROPERTY_STAGING_LINUXPATH , execrc , "/redist" , false );
		STAGING_WINPATH = properties.getSystemPathProperty( PROPERTY_STAGING_WINPATH , execrc , "C:/redist" , false );
		
		MON_RESPATH = properties.getSystemPathProperty( PROPERTY_MON_RESPATH , execrc , execrc.installPath + "/monitoring/resources" , false ); 
		MON_RESURL = properties.getSystemStringProperty( PROPERTY_MON_RESURL , "" , false ); 
		MON_DATAPATH = properties.getSystemPathProperty( PROPERTY_MON_DATAPATH , execrc , execrc.installPath + "/monitoring/data" , false );
		MON_REPORTPATH = properties.getSystemPathProperty( PROPERTY_MON_REPORTPATH , execrc , execrc.installPath + "/monitoring/reports" , false );
		MON_LOGPATH = properties.getSystemPathProperty( PROPERTY_MON_LOGPATH , execrc , execrc.installPath + "/logs/monitoring" , false );

		SHELL_SILENTMAX = properties.getSystemIntProperty( PROPERTY_SHELL_SILENTMAX , 60000 , false );
		SHELL_UNAVAILABLE_SKIPTIME = properties.getSystemIntProperty( PROPERTY_SHELL_UNAVAILABLE_SKIPTIME , 30000 , false );
		SHELL_HOUSEKEEP_TIME = properties.getSystemIntProperty( PROPERTY_SHELL_HOUSEKEEP_TIME , 30000 , false );
		
		CHAT_USING = properties.getSystemBooleanProperty( PROPERTY_CHAT_USING , false , false );
		CHAT_TYPE = properties.getSystemStringProperty( PROPERTY_CHAT_TYPE , "" , false );
		CHAT_JABBER_RESOURCE = properties.getSystemStringProperty( PROPERTY_CHAT_JABBER_RESOURCE , "" , false );
		CHAT_JABBER_CONFERENCESERVER = properties.getSystemStringProperty( PROPERTY_CHAT_JABBER_CONFERENCESERVER , "" , false );
		CHAT_ROCKET_RESOURCE = properties.getSystemStringProperty( PROPERTY_CHAT_ROCKET_RESOURCE , "" , false );
	}

	public void setServerProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		properties.updateProperties( props , true );
	}

	public void resolveServerProperties( EngineTransaction transaction ) throws Exception {
		properties.resolveRawProperties();
		scatterSystemProperties();
	}
	
	public void getExecProperties( PropertySet set ) throws Exception {
		RunContext rc = execrc;
		set.setStringProperty( RunContext.PROPERTY_OS_TYPE , Common.getEnumLower( rc.osType ) );
		set.setPathProperty( RunContext.PROPERTY_INSTALL_PATH , rc.installPath , null );
		set.setPathProperty( RunContext.PROPERTY_WORK_PATH , rc.workPath , null );
		set.setPathProperty( RunContext.PROPERTY_USER_HOME , rc.userHome , null );
		set.setPathProperty( RunContext.PROPERTY_AUTH_PATH , rc.authPath , null );
		set.setStringProperty( RunContext.PROPERTY_HOSTNAME , rc.hostName );
		set.setPathProperty( RunContext.PROPERTY_SERVER_CONFPATH , rc.installPath + "/etc" , null );
		set.setPathProperty( RunContext.PROPERTY_SERVER_MASTERPATH , rc.installPath + "/master" , null );
		set.setPathProperty( RunContext.PROPERTY_SERVER_PRODUCTSPATH , rc.installPath + "/products" , null );
		
		set.resolveRawProperties();
	}
	
}
