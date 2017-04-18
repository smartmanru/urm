package org.urm.engine;

abstract public class ServerExecutorTask {

	abstract public void execute();
	
	public String name;
	public ServerExecutorThread thread;
	
	protected ServerExecutorTask( String name ) {
		this.name = name;
	}

	public void setThread( ServerExecutorThread thread ) {
		this.thread = thread;
	}

	public boolean isRunning() {
		if( thread == null )
			return( false );
		return( thread.isRunning() );
	}
	
}
