package org.urm.engine;

import org.urm.common.Common;

public class ServerHouseKeeping implements Runnable {

	ServerEngine engine;
	
	private ServerThread thread;
	
	public ServerHouseKeeping( ServerEngine engine ) {
		this.engine = engine;
		thread = new ServerThread( engine , this , "house keeping" , true );
	}
	
	@Override
	public void run() {
		try {
			Common.sleep( 1000 );
			runHouseKeeping();
		}
		catch( Throwable e ) {
			engine.handle( "thread pool house keeping error" , e );
		}
	}

	public void start() {
        thread.start();
	}
	
	public void stop() {
		thread.stop();
	}

	private void runHouseKeeping() throws Exception {
		long time = System.currentTimeMillis();
		engine.shellPool.runHouseKeeping( time );
		engine.blotter.runHouseKeeping( time );
	}
	
}
