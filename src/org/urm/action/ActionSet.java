package org.urm.action;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.status.ScopeState;
import org.urm.meta.product.MetaEnv;

public class ActionSet {

	ScopeState parentState;
	ActionBase owner;
	String name;
	
	List<ActionSetItem> actions;
	public ThreadGroup threadGroup;
	public int threadCount;
	
	public ActionSet( ScopeState parentState , ActionBase owner , String name ) {
		this.parentState = parentState;
		this.owner = owner;
		this.name = name;
		
		threadCount = 0;
		actions = new LinkedList<ActionSetItem>();
        threadGroup = new ThreadGroup( name );
        owner.trace( "create thread group: " + name );
	}

	public ActionSetItem[] getActions() {
		return( actions.toArray( new ActionSetItem[0] ) );
	}
	
	public boolean waitDone() {
		owner.debug( "wait for completion of action set=" + name + " ..." );
		try {
	        // wait for all the threads to complete
	        while( true ) {
	            synchronized( threadGroup ) {
	            	if( threadCount == 0 )
	            		break;
	            	
	        		owner.debug( "waiting for action set=" + name + ", count = " + threadCount + " ..." );
	                threadGroup.wait( 10000 );
	            }
	        }
		}
		catch( Throwable e ) {
			owner.handle( e );
		}
        
        boolean ok = true;
		for( ActionSetItem item : actions ) {
        	if( item.isFailed() )
        		ok = false;
        }

		owner.debug( "finished action set=" + name + ", status=" + ok );
		return( ok );
	}
	
	public void runSimpleProduct( ActionBase action , String productName , SecurityAction sa , boolean readOnly ) throws Exception {
		String threadName = "AT." + actions.size();
		ActionSetItem item = new ActionSetItem( this , threadName );
		item.createSimpleProduct( parentState , action , productName , sa , readOnly );
		startItem( item );
	}

	public void runSimpleEnv( ActionBase action , MetaEnv env , SecurityAction sa , boolean readOnly ) throws Exception {
		String threadName = "AT." + actions.size();
		ActionSetItem item = new ActionSetItem( this , threadName );
		item.createSimpleEnv( parentState , action , env , sa , readOnly );
		startItem( item );
	}
	
	public void runScope( ActionBase action , ActionScope scope , MetaEnv env , SecurityAction sa , boolean readOnly ) throws Exception {
		String threadName = "AT." + actions.size();
		ActionSetItem item = new ActionSetItem( this , threadName );
		item.createScope( parentState , action , scope , env , sa , readOnly );
		startItem( item );
	}

	private void startItem( ActionSetItem item ) throws Exception {
		synchronized( threadGroup ) {
        	threadCount++;
        }
		
		actions.add( item );
        owner.trace( "start thread group=" + name + ", thread=" + item.threadName );
        Thread thread = new Thread( threadGroup , item , item.threadName );
        thread.start();
	}
	
	public void finishedItem( ActionSetItem item ) {
        synchronized( threadGroup ) {
        	threadCount--;
            threadGroup.notifyAll();
        }
	}
	
}
