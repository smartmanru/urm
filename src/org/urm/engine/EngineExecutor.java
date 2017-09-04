package org.urm.engine;

public class EngineExecutor {

	Engine engine;
	
	public EngineExecutor( Engine engine ) {
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
