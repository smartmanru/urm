package ru.egov.urm.shell;

import ru.egov.urm.run.ActionBase;

public abstract class WaiterCommand implements Runnable {

	public ActionBase action;
	public Thread thread;
	
	public boolean finished;
	public boolean exception;
	
	abstract void run( ActionBase action ) throws Exception;
	
    public void run() {
        try {
            finished = false;
            exception = false;
            
            run( action );
            finished = true;
            
            synchronized ( this ) {
                notifyAll();
            }
        }
        catch (Exception e) {
            exception = true;
            finished = true;
            
            synchronized ( this ) {
                notifyAll();
            }
        }
    }
	
}
