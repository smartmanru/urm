package org.urm.engine;

abstract public class EngineExecutorTask {

	abstract public void execute() throws Exception;
	
	public String name;
	public EngineExecutorThread thread;
	public boolean runFailed;
	public Throwable runException;
	public int iteration;
	
	protected EngineExecutorTask( String name ) {
		this.name = name;
		runFailed = false;
		iteration = 0;
	}

	public void setThread( EngineExecutorThread thread ) {
		this.thread = thread;
	}

	public void start() {
		iteration++;
		runFailed = false;
		runException = null;
	}
	
	public boolean isRunning() {
		if( thread == null )
			return( false );
		return( thread.isRunning() );
	}

	public void finishSuccessful() {
		runFailed = false;
	}
	
	public void finishFailed( Throwable e ) {
		runFailed = true;
		runException = e;
	}
	
}
