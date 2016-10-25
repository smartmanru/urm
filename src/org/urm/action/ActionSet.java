package org.urm.action;

import java.util.LinkedList;
import java.util.List;

public class ActionSet {

	ActionBase owner;
	String name;
	
	List<ActionSetItem> actions;
	public ThreadGroup threadGroup;
	public int threadCount;
	
	public ActionSet( ActionBase owner , String name ) {
		this.owner = owner;
		this.name = name;
		
		threadCount = 0;
		actions = new LinkedList<ActionSetItem>();
        threadGroup = new ThreadGroup( name );
        owner.trace( "create thread group: " + name );
	}

	public boolean waitDone() {
		owner.debug( "wait for completion of action set=" + name + " ..." );
		try {
	        // wait for all the threads to complete
	        while( true ) {
	            synchronized( threadGroup ) {
	            	if( threadCount == 0 )
	            		break;
	            	
	                threadGroup.wait( 10000 );
	        		owner.debug( "waiting for action set=" + name + ", count = " + threadCount + " ..." );
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

		owner.debug( "finished action set=" + name + ", status" + ok );
		return( ok );
	}
	
	public void runSimple( ActionBase action ) throws Exception {
		String threadName = "AT." + actions.size();
		ActionSetItem item = new ActionSetItem( this , threadName );
		item.createSimple( action );
		startItem( item );
	}
	
	public void runScope( ActionBase action , ActionScope scope ) throws Exception {
		String threadName = "AT." + actions.size();
		ActionSetItem item = new ActionSetItem( this , threadName );
		item.createScope( action , scope );
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
