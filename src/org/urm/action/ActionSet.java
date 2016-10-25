package org.urm.action;

import java.util.LinkedList;
import java.util.List;

public class ActionSet {

	ActionBase owner;
	String name;
	
	List<ActionSetItem> actions;
	public ThreadGroup threadGroup;
	
	public ActionSet( ActionBase owner , String name ) {
		this.owner = owner;
		this.name = name;
		
		actions = new LinkedList<ActionSetItem>();
        threadGroup = new ThreadGroup( name );
        owner.trace( "create thread group: " + name );
	}

	public boolean waitDone() {
		owner.debug( "wait for completion of action set=" + name + " ..." );
		try {
	        // wait for all the threads to complete
	        while( threadGroup.activeCount() > 0 ) {
	            synchronized( threadGroup ) {
	                threadGroup.wait( 1000 );
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
		actions.add( item );
        owner.trace( "start thread group=" + name + ", thread=" + item.threadName );
        Thread thread = new Thread( threadGroup , item , item.threadName );
        thread.start();
	}
	
}
