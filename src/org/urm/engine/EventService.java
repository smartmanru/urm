package org.urm.engine;

import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsNotifier;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.events.EngineEventsTimer;
import org.urm.engine.events.NotifyEvent;
import org.urm.engine.events.SourceEvent;
import org.urm.meta.loader.EngineObject;

public class EventService extends EngineObject {

	public Engine engine;
	
	public static int OWNER_ENGINE = 1;
	public static int OWNER_ENGINESTATUS = 2;
	public static int OWNER_ENGINEBUILDPLAN = 3;
	public static int OWNER_ENGINEDEPLOYPLAN = 4;
	
	public static int EVENT_STARTSTATE = 1;
	public static int EVENT_FINISHSTATE = 2;
	public static int EVENT_SECONDTIMER = 3;
	public static int EVENT_BLOTTEREVENT = 4;
	public static int EVENT_RUNASYNC = 5;
	public static int EVENT_STARTCHILDSTATE = 6;
	public static int EVENT_FINISHCHILDSTATE = 7;
	public static int EVENT_STARTACTION = 8;
	public static int EVENT_FINISHACTION = 9;
	public static int EVENT_ACTIONLOG = 10;
	public static int EVENT_STATECHANGED = 11;
	public static int EVENT_MONITORCHILDCHANGED = 12;
	public static int EVENT_MONITORGRAPHCHANGED = 13;
	public static int EVENT_STARTMACROSTATE = 14;
	public static int EVENT_FINISHMACROSTATE = 15;
	public static int EVENT_STARTCHILDACTION = 20;
	public static int EVENT_FINISHCHILDACTION = 21;
	public static int EVENT_ADDFACT = 22;
	public static int EVENT_ADDCHILDFACT = 23;
	public static int EVENT_SEGMENT = 50;
	public static int EVENT_SERVER = 51;
	public static int EVENT_NODE = 52;
	public static int EVENT_SGITEMS = 150;
	public static int EVENT_SERVERITEMS = 151;
	public static int EVENT_NODEITEMS = 152;
	public static int EVENT_RELEASEREPOCHANGED = 201;
	public static int EVENT_DISTREPOCHANGED = 202;

	EngineEventsTimer timer;
	EngineEventsNotifier notifier;
	
	public EventService( Engine engine ) {
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
		engine.trace( "stop events ..." );
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

	public void waitDelivered( EngineEventsSource source ) {
		notifier.waitDelivered( source );
	}
	
}
