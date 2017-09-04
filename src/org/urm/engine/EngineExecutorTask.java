package org.urm.engine;

abstract public class EngineExecutorTask {

	abstract public void execute();
	
	public String name;
	public EngineExecutorThread thread;
	
	protected EngineExecutorTask( String name ) {
		this.name = name;
	}

	public void setThread( EngineExecutorThread thread ) {
		this.thread = thread;
	}

	public boolean isRunning() {
		if( thread == null )
			return( false );
		return( thread.isRunning() );
	}
	
}
