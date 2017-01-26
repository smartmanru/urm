package org.urm.action;

import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.product.MetaEnv;

public class ActionSetItem implements Runnable {

	ActionSet set;
	public String threadName;
	
    boolean threadFailed;
    Exception exceptionCatched;
    private boolean failed;
	
	boolean runSimpleProduct = false;
	boolean runSimpleEnv = false;
	boolean runScope = false;
	
	public ActionBase action;
	ActionScope scope;
	
	String productName;
	MetaEnv env;
	SecurityAction sa;
	boolean readOnly;
	
	public ActionSetItem( ActionSet set , String threadName ) {
		this.set = set;
		this.threadName = threadName;
	}

	public void createSimpleProduct( ActionBase action , String productName , SecurityAction sa , boolean readOnly ) throws Exception {
		runSimpleProduct = true;
		this.action = action;
		this.productName = productName;
		this.sa = sa;
		this.readOnly = readOnly;
	}

	public void createSimpleEnv( ActionBase action , MetaEnv env , SecurityAction sa , boolean readOnly ) throws Exception {
		runSimpleEnv = true;
		this.action = action;
		this.env = env;
		this.sa = sa;
		this.readOnly = readOnly;
	}

	public void createScope( ActionBase action , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) throws Exception {
		runScope = true;
		this.action = action;
		this.scope = scope;
		this.env = env;
		this.sa = sa;
		this.readOnly = readOnly;
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
    	if( runSimpleProduct ) {
    		if( !action.runSimpleProduct( productName , sa , readOnly ) )
    			failed = true;
    	}
    	else
    	if( runSimpleEnv ) {
    		if( !action.runSimpleEnv( env , sa , readOnly ) )
    			failed = true;
    	}
    	else
    	if( runScope ) {
    		if( !action.runAll( scope , env , sa , readOnly ) )
    			failed = true;
    	}
    	else
    		action.exitUnexpectedState();
    }
    
    public boolean isFailed() {
    	return( failed );
    }
}
