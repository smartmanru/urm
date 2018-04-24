package org.urm.action;

import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.status.ScopeState;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.product.Meta;

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
	public ScopeState parentState;
	ActionScope scope;
	
	Meta meta;
	MetaEnv env;
	SecurityAction sa;
	boolean readOnly;
	
	public ActionSetItem( ActionSet set , String threadName ) {
		this.set = set;
		this.threadName = threadName;
	}

	public void createSimpleProduct( ScopeState parentState , ActionBase action , Meta meta , SecurityAction sa , boolean readOnly ) throws Exception {
		runSimpleProduct = true;
		this.parentState = parentState;
		this.action = action;
		this.meta = meta;
		this.sa = sa;
		this.readOnly = readOnly;
	}

	public void createSimpleEnv( ScopeState parentState , ActionBase action , MetaEnv env , SecurityAction sa , boolean readOnly ) throws Exception {
		runSimpleEnv = true;
		this.parentState = parentState;
		this.action = action;
		this.env = env;
		this.sa = sa;
		this.readOnly = readOnly;
	}

	public void createScope( ScopeState parentState , ActionBase action , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) throws Exception {
		runScope = true;
		this.parentState = parentState;
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
    		if( !action.runSimpleProduct( parentState , meta , sa , readOnly ) )
    			failed = true;
    	}
    	else
    	if( runSimpleEnv ) {
    		if( !action.runSimpleEnv( parentState , env , sa , readOnly ) )
    			failed = true;
    	}
    	else
    	if( runScope ) {
    		if( !action.runAll( parentState , scope , env , sa , readOnly ) )
    			failed = true;
    	}
    	else
    		action.exitUnexpectedState();
    }
    
    public boolean isFailed() {
    	return( failed );
    }
}
