package org.urm.meta.engine;

import org.urm.action.monitor.ActionMonitorTop;
import org.urm.engine.ServerEngine;
import org.urm.meta.product.MetaMonitoring;

public class ServerMonitoringProduct implements Runnable {

	ServerMonitoring monitoring;
	MetaMonitoring meta;
	ServerMonitoringSource source;
	ServerEngine engine;
	
	private Thread thread;
	private boolean started = false;
	private boolean stopped = false;
	
	ActionMonitorTop ca;
	
	public ServerMonitoringProduct( ServerMonitoring monitoring , MetaMonitoring meta , ServerMonitoringSource source ) {
		this.monitoring = monitoring;
		this.meta = meta;
		this.source = source;
		this.engine = monitoring.engine;
	}
	
	@Override
	public void run() {
		started = true;
		try {
			ca = new ActionMonitorTop( engine.serverAction , null , meta );
			ca.runSimple();
		}
		catch( Throwable e ) {
			engine.serverAction.handle( "thread pool house keeping error" , e );
		}
		
		synchronized( this ) {
			thread = null;
			ca = null;
			stopped = true;
			notifyAll();
		}
	}

	public synchronized void start() {
		if( started )
			return;
		
        thread = new Thread( null , this , "monitoring:" + meta.meta.name );
        thread.start();
	}
	
	public synchronized void stop() {
		if( started == false || stopped )
			return;
		
		ca.stopRunning();
		try {
			wait();
		}
		catch( Throwable e ) {
			engine.serverAction.log( "ServerMonitoringProduct stop" , e );
		}
	}
	
}
