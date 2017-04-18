package org.urm.engine;

import org.urm.common.Common;

public class ServerHouseKeeping {

	ServerEngine engine;
	
	class ServerExecutorTaskHouseKeep extends ServerExecutorTask {
		ServerExecutorTaskHouseKeep() {
			super( "house keeping" );
		}
		
		@Override
		public void execute() {
			try {
				Common.sleep( 1000 );
				runHouseKeeping();
			}
			catch( Throwable e ) {
				engine.handle( "thread pool house keeping error" , e );
			}
		}
	};		
	
	private ServerExecutorTaskHouseKeep task;
	
	public ServerHouseKeeping( ServerEngine engine ) {
		this.engine = engine;
		task = new ServerExecutorTaskHouseKeep();
	}
	
	public void start() {
        engine.executor.executeCycle( task );
	}
	
	public void stop() {
		engine.executor.stopTask( task );
	}

	private void runHouseKeeping() throws Exception {
		long time = System.currentTimeMillis();
		engine.shellPool.runHouseKeeping( time );
		engine.blotter.runHouseKeeping( time );
	}
	
}
