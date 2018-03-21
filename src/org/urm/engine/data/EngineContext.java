package org.urm.engine.data;

import org.urm.common.RunContext;
import org.urm.db.core.DBEnums.DBEnumChatType;
import org.urm.engine.properties.ObjectProperties;

public class EngineContext {

	public EngineSettings settings;
	public RunContext execrc;
	public ObjectProperties properties;

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
	public DBEnumChatType CHAT_TYPE;
	public Integer CHAT_JABBER_RESOURCE;
	public String CHAT_JABBER_CONFERENCESERVER;
	public Integer CHAT_ROCKET_RESOURCE;
	
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
	
	public static String PROPERTY_MON_ENABLED = "monitoring.enabled";
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
	
	public EngineContext( RunContext execrc , ObjectProperties properties ) {
		this.execrc = execrc;
		this.properties = properties;
	}

	public EngineContext copy( ObjectProperties properties ) throws Exception {
		EngineContext r = new EngineContext( execrc , properties );
		r.scatterProperties();
		return( r );
	}
	
	public void scatterProperties() throws Exception {
		CONNECTION_JMX_PORT = properties.getIntProperty( PROPERTY_CONNECTION_JMX_PORT );
		CONNECTION_JMXWEB_PORT = properties.getIntProperty( PROPERTY_CONNECTION_JMXWEB_PORT );

		DIST_ROOT = properties.getPathProperty( PROPERTY_DIST_ROOT );
		DIST_APPFOLDER = properties.getPathProperty( PROPERTY_DIST_APPFOLDER );
		DIST_PLATFORMPATH = properties.getPathProperty( PROPERTY_DIST_PLATFORMPATH );

		SECURE_CONFPATH = properties.getPathProperty( PROPERTY_SECURE_CONFPATH );
		
		WORK_ARTEFACTS = properties.getPathProperty( PROPERTY_WORK_ARTEFACTS );
		WORK_MIRRORPATH = properties.getPathProperty( PROPERTY_WORK_MIRRORPATH );
		WORK_BUILDLOGS = properties.getPathProperty( PROPERTY_WORK_BUILDLOGS );
		WORK_DEPLOYLOGS = properties.getPathProperty( PROPERTY_WORK_DEPLOYLOGS );

		STAGING_LINUXPATH = properties.getPathProperty( PROPERTY_STAGING_LINUXPATH );
		STAGING_WINPATH = properties.getPathProperty( PROPERTY_STAGING_WINPATH );
		
		MON_RESPATH = properties.getPathProperty( PROPERTY_MON_RESPATH ); 
		MON_RESURL = properties.getStringProperty( PROPERTY_MON_RESURL ); 
		MON_DATAPATH = properties.getPathProperty( PROPERTY_MON_DATAPATH );
		MON_REPORTPATH = properties.getPathProperty( PROPERTY_MON_REPORTPATH );
		MON_LOGPATH = properties.getPathProperty( PROPERTY_MON_LOGPATH );

		SHELL_SILENTMAX = properties.getIntProperty( PROPERTY_SHELL_SILENTMAX );
		SHELL_UNAVAILABLE_SKIPTIME = properties.getIntProperty( PROPERTY_SHELL_UNAVAILABLE_SKIPTIME );
		SHELL_HOUSEKEEP_TIME = properties.getIntProperty( PROPERTY_SHELL_HOUSEKEEP_TIME );
		
		CHAT_USING = properties.getBooleanProperty( PROPERTY_CHAT_USING );
		CHAT_TYPE = DBEnumChatType.getValue( properties.getEnumProperty( PROPERTY_CHAT_TYPE ) , false );
		CHAT_JABBER_RESOURCE = properties.getObjectProperty( PROPERTY_CHAT_JABBER_RESOURCE );
		CHAT_JABBER_CONFERENCESERVER = properties.getStringProperty( PROPERTY_CHAT_JABBER_CONFERENCESERVER );
		CHAT_ROCKET_RESOURCE = properties.getObjectProperty( PROPERTY_CHAT_ROCKET_RESOURCE );
	}

}
