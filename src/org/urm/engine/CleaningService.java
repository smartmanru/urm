package org.urm.engine;

import org.urm.common.Common;
import org.urm.engine.run.EngineExecutorTask;

public class CleaningService {

	Engine engine;
	
	class ServerExecutorTaskHouseKeep extends EngineExecutorTask {
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
	
	public CleaningService( Engine engine ) {
		this.engine = engine;
		task = new ServerExecutorTaskHouseKeep();
	}
	
	public void start() {
        engine.tasks.executeCycle( task );
	}
	
	public void stop() {
		engine.tasks.stopTask( task );
	}

	private void runHouseKeeping() throws Exception {
		long time = System.currentTimeMillis();
		engine.shellPool.runHouseKeeping( time );
		engine.blotter.runHouseKeeping( time );
	}
	
}
