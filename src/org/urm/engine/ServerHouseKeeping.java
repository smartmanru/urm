package org.urm.engine;

import org.urm.common.Common;

public class ServerHouseKeeping implements Runnable {

	ServerEngine engine;
	
	private Thread thread;
	private boolean started = false;
	private boolean stop = false;
	private boolean stopped = false;
	
	public ServerHouseKeeping( ServerEngine engine ) {
		this.engine = engine;
	}
	
	@Override
	public void run() {
		while( !stop ) {
			try {
				Common.sleep( 1000 );
				runHouseKeeping();
			}
			catch( Throwable e ) {
				engine.handle( "thread pool house keeping error" , e );
			}
		}
		
		synchronized( thread ) {
			stopped = true;
			thread.notifyAll();
		}
	}

	public void start() {
        thread = new Thread( null , this , "House Keeping" );
        thread.start();
	}
	
	public void stop() {
		engine.debug( "stop house keeping ..." );
		try {
			if( started ) {
				stop = true;
				thread.notifyAll();

				while( true ) {
					synchronized( thread ) {
						if( !stopped )
							thread.wait();
						if( stopped )
							break;
					}
				}
			}
		}
		catch( Throwable e ) {
			engine.handle( "exception when stopping house keeping" , e );
		}
		
		engine.debug( "house keeping has been stopped" );
	}

	private void runHouseKeeping() {
		long time = System.currentTimeMillis();
		
		try {
			engine.shellPool.runHouseKeeping( time );
			engine.blotter.runHouseKeeping( time );
		}
		catch( Throwable e ) {
			engine.log( "house keeping" , e );
		}
	}
	
}
