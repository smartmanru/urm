package org.urm.engine;

public class ServerExecutor {

	ServerEngine engine;
	
	public ServerExecutor( ServerEngine engine ) {
		this.engine = engine;
	}
	
	public void executeOnce( ServerExecutorTask task ) {
		ServerExecutorThread thread = new ServerExecutorThread( this , task , false );
		thread.start();
	}
	
	public void executeCycle( ServerExecutorTask task ) {
		ServerExecutorThread thread = new ServerExecutorThread( this , task , true );
		thread.start();
	}

	public void stopTask( ServerExecutorTask task ) {
		task.thread.stop();
	}
	
}
