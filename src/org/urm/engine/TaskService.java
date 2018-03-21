package org.urm.engine;

import org.urm.engine.run.EngineExecutorTask;
import org.urm.engine.run.EngineExecutorThread;

public class TaskService {

	public Engine engine;
	
	public TaskService( Engine engine ) {
		this.engine = engine;
	}
	
	public void executeOnce( EngineExecutorTask task ) {
		EngineExecutorThread thread = new EngineExecutorThread( this , task , false );
		thread.start();
	}
	
	public void executeCycle( EngineExecutorTask task ) {
		EngineExecutorThread thread = new EngineExecutorThread( this , task , true );
		thread.start();
	}

	public void stopTask( EngineExecutorTask task ) {
		task.thread.stop();
	}
	
}
