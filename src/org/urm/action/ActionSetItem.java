package org.urm.action;

public class ActionSetItem implements Runnable {

	ActionSet set;
    boolean threadFailed;
    Exception exceptionCatched;
    private boolean failed;
	
	boolean runSimple = false;
	boolean runScope = false;
	
	ActionBase action;
	ActionScope scope;
	
	public ActionSetItem( ActionSet set ) {
		this.set = set;
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

    public void run() {
    	failed = false;
    	
        try {
            threadFailed = false;
            execute();

            if( action.isFailed() ) {
            	failed = true;
            	action.trace( action.NAME + ": failed=true" );
            }
            
            synchronized( set.threadGroup ) {
                set.threadGroup.notifyAll();
            }
        }
        catch (Exception e) {
        	failed = true;
            threadFailed = true;

            // output error message
            exceptionCatched = e;
            action.handle( exceptionCatched );
        }
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