package org.urm.engine.events;

import org.urm.engine.Engine;
import org.urm.meta.EngineObject;

public class EngineEvents extends EngineObject {

	Engine engine;
	
	public static int OWNER_ENGINE = 1;
	public static int OWNER_ENGINESTATUS = 2;
	public static int OWNER_ENGINEBUILDPLAN = 3;
	public static int OWNER_ENGINEDEPLOYPLAN = 4;
	
	public static int EVENT_STARTSTATE = 1;
	public static int EVENT_FINISHSTATE = 2;
	public static int EVENT_SECONDTIMER = 3;
	public static int EVENT_BLOTTEREVENT = 4;
	public static int EVENT_RUNASYNC = 5;
	public static int EVENT_FINISHCHILDSTATE = 6;
	public static int EVENT_STARTACTION = 7;
	public static int EVENT_FINISHACTION = 8;
	public static int EVENT_STATECHANGED = 11;
	public static int EVENT_MONITORCHILDCHANGED = 12;
	public static int EVENT_MONITORGRAPHCHANGED = 13;
	public static int EVENT_SEGMENT = 50;
	public static int EVENT_SERVER = 51;
	public static int EVENT_NODE = 52;
	public static int EVENT_SGITEMS = 150;
	public static int EVENT_SERVERITEMS = 151;
	public static int EVENT_NODEITEMS = 152;

	EngineEventsTimer timer;
	EngineEventsNotifier notifier;
	
	public EngineEvents( Engine engine ) {
		super( null );
		this.engine = engine;
		
		timer = new EngineEventsTimer( this );
		notifier = new EngineEventsNotifier( this ); 
	}

	@Override
	public String getName() {
		return( "engine-events" );
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

	public EngineEventsApp createApp( String appId ) {
		engine.trace( "start events management for application=" + appId );
		return( new EngineEventsApp( this , appId ) );
	}

	public void deleteApp( EngineEventsApp app ) {
		app.close();
		engine.trace( "stop events management for application=" + app.appId );
	}

	public EngineEventsSubscription subscribeTimer( EngineEventsApp app , EngineEventsListener listener ) {
		return( app.subscribe( timer , listener ) );
	}			

	public void notifyApp( EngineEventsApp app , SourceEvent eventData ) {
		app.notifyEvent( eventData );
	}

	public void notifyListener( EngineEventsApp app , EngineEventsListener listener , SourceEvent eventData ) {
		NotifyEvent event = new NotifyEvent( app , listener , eventData );
		notifier.addEvent( event );
	}
	
	public void notifyEvent( NotifyEvent event ) {
		notifier.addEvent( event );
	}
	
}
