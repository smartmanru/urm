package org.urm.action;

public class ActionSetItem implements Runnable {

	ActionSet set;
	public String threadName;
	
    boolean threadFailed;
    Exception exceptionCatched;
    private boolean failed;
	
	boolean runSimple = false;
	boolean runScope = false;
	
	public ActionBase action;
	ActionScope scope;
	
	public ActionSetItem( ActionSet set , String threadName ) {
		this.set = set;
		this.threadName = threadName;
	}

	public void createSimple( ActionBase action ) throws Exception {
		runSimple = true;
		this.action = action;
	}

	public void createScope( ActionBase action , ActionScope scope ) throws Exception {
		runScope = true;
		this.action = action;
		this.scope = scope;
	}

    @Override
	public void run() {
    	failed = false;
    	
    	set.owner.trace( "begin thread=" + threadName );
        try {
            threadFailed = false;
            execute();

            if( action.isFailed() ) {
            	failed = true;
            	action.trace( action.NAME + ": failed=true" );
            }
        }
        catch (Exception e) {
        	failed = true;
            threadFailed = true;

            // output error message
            exceptionCatched = e;
            action.handle( exceptionCatched );
        }
    	set.owner.trace( "end thread=" + threadName );
        set.finishedItem( this );
    }

    private void execute() throws Exception {
    	if( runSimple ) {
    		if( !action.runSimple() )
    			failed = true;
    	}
    	else
    	if( runScope ) {
    		if( !action.runAll( scope ) )
    			failed = true;
    	}
    	else
    		action.exitUnexpectedState();
    }
    
    public boolean isFailed() {
    	return( failed );
    }
}
