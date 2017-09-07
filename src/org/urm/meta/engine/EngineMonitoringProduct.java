package org.urm.meta.engine;

import org.urm.action.monitor.ActionMonitorTop;
import org.urm.engine.Engine;
import org.urm.engine.EngineExecutorTask;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.events.EngineSourceEvent;
import org.urm.engine.status.StatusSource;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.product.MetaMonitoringTarget;

public class EngineMonitoringProduct implements EngineEventsListener {
	
	class ServerExecutorTaskMonitorProduct extends EngineExecutorTask {
		ServerExecutorTaskMonitorProduct( String productName ) {
			super( "monitoring::" + productName );
		}
		
		@Override
		public void execute() {
			runTop();
		}
	};		
	
	private ServerExecutorTaskMonitorProduct task;

	EngineMonitoring monitoring;
	String productName;
	StatusSource source;
	Engine engine;
	
	ActionMonitorTop ca;
	EngineEventsApp eventsApp;

	public EngineMonitoringProduct( EngineMonitoring monitoring , String productName , StatusSource source , EngineEventsApp eventsApp ) {
		this.monitoring = monitoring;
		this.productName = productName;
		this.source = source;
		this.engine = monitoring.engine;
		this.eventsApp = eventsApp;
		
		task = new ServerExecutorTaskMonitorProduct( productName ); 
	}
	
	public void runTop() {
		try {
			ca = new ActionMonitorTop( engine.serverAction , productName , productName );
			eventsApp.subscribe( ca.eventSource , this );
			ca.runSimpleProduct( productName , SecurityAction.ACTION_MONITOR , false );
		}
		catch( Throwable e ) {
			engine.handle( "thread pool house keeping error" , e );
		}
		
		eventsApp.unsubscribe( this );
		ca = null;
	}

	@Override
	public void triggerEvent( EngineSourceEvent event ) {
		if( event.eventType == EngineEvents.EVENT_MONITORGRAPHCHANGED ) {
			MetaMonitoringTarget target = ( MetaMonitoringTarget )event.data;
			StatusSource sgSource = monitoring.findTargetSource( target );
			if( sgSource == null )
				return;
			
			sgSource.customEvent( EngineEvents.EVENT_MONITORGRAPHCHANGED , target );
			return;
		}
	}

	@Override
	public void triggerSubscriptionRemoved( EngineEventsSubscription sub ) {
	}
	
	public synchronized void start() {
		engine.executor.executeOnce( task );
	}
	
	public synchronized void stop() {
		ca.stopRunning();
		engine.executor.stopTask( task );
	}

}
