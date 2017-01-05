package org.urm.engine;

import org.urm.meta.ServerObject;

public class ServerEvents extends ServerObject {

	ServerEngine engine;
	
	public static int EVENT_FINISHSTATE = 1;
	public static int EVENT_SECONDTIMER = 2;
	public static int EVENT_MONITORSTATECHANGED = 11;
	public static int EVENT_MONITORCHILDCHANGED = 12;
	public static int EVENT_MONITORGRAPHCHANGED = 13;
	public static int EVENT_MONITORING_SEGMENT = 50;
	public static int EVENT_MONITORING_SERVER = 51;
	public static int EVENT_MONITORING_NODE = 52;
	public static int EVENT_MONITORING_SGITEMS = 150;
	public static int EVENT_MONITORING_SERVERITEMS = 151;
	public static int EVENT_MONITORING_NODEITEMS = 152;

	ServerEventsTimer timer;
	
	public ServerEvents( ServerEngine engine ) {
		super( null );
		this.engine = engine;
		
		timer = new ServerEventsTimer( this );
	}

	public void init() throws Exception {
		timer.start();
	}

	public void stop() throws Exception {
		timer.stop();
	}

	public ServerEventsApp createApp( String appId ) {
		engine.serverAction.trace( "start events management for application=" + appId );
		return( new ServerEventsApp( this , appId ) );
	}

	public void deleteApp( ServerEventsApp app ) {
		app.deleteSubscriptions();
		engine.serverAction.trace( "stop events management for application=" + app.appId );
	}

	public ServerEventsSubscription subscribeTimer( ServerEventsApp app , ServerEventsListener listener ) {
		return( app.subscribe( timer , listener ) );
	}			
			
}
