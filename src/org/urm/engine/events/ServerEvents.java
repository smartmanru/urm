package org.urm.engine.events;

import org.urm.engine.ServerEngine;
import org.urm.meta.ServerObject;

public class ServerEvents extends ServerObject {

	ServerEngine engine;
	
	public static int EVENT_FINISHSTATE = 1;
	public static int EVENT_SECONDTIMER = 2;
	public static int EVENT_BLOTTEREVENT = 3;
	public static int EVENT_NOTIFY = 4;
	public static int EVENT_FINISHCHILDSTATE = 5;
	public static int EVENT_MONITORSTATECHANGED = 11;
	public static int EVENT_MONITORCHILDCHANGED = 12;
	public static int EVENT_MONITORGRAPHCHANGED = 13;
	public static int EVENT_CACHE_PRODUCT = 20;
	public static int EVENT_CACHE_ENV = 21;
	public static int EVENT_CACHE_SEGMENT = 22;
	public static int EVENT_CACHE_SERVER = 23;
	public static int EVENT_CACHE_NODE = 24;
	public static int EVENT_MONITORING_SEGMENT = 50;
	public static int EVENT_MONITORING_SERVER = 51;
	public static int EVENT_MONITORING_NODE = 52;
	public static int EVENT_MONITORING_SGITEMS = 150;
	public static int EVENT_MONITORING_SERVERITEMS = 151;
	public static int EVENT_MONITORING_NODEITEMS = 152;

	ServerEventsTimer timer;
	ServerEventsNotifier notifier;
	
	public ServerEvents( ServerEngine engine ) {
		super( null );
		this.engine = engine;
		
		timer = new ServerEventsTimer( this );
		notifier = new ServerEventsNotifier( this ); 
	}

	@Override
	public String getName() {
		return( "server-events" );
	}
	
	public void init() throws Exception {
	}

	public void start() throws Exception {
		timer.start();
		notifier.start();
	}

	public void stop() throws Exception {
		notifier.stop();
		timer.stop();
	}

	public ServerEventsApp createApp( String appId ) {
		engine.trace( "start events management for application=" + appId );
		return( new ServerEventsApp( this , appId ) );
	}

	public void deleteApp( ServerEventsApp app ) {
		app.close();
		engine.trace( "stop events management for application=" + app.appId );
	}

	public ServerEventsSubscription subscribeTimer( ServerEventsApp app , ServerEventsListener listener ) {
		return( app.subscribe( timer , listener ) );
	}			


	public void notifyListener( ServerEventsApp app , ServerEventsListener listener , Object eventData ) {
		notifier.addEvent( app , listener , eventData );
	}
	
}
