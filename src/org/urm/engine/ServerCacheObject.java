package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ScopeState;
import org.urm.engine.events.ServerEvents;
import org.urm.engine.events.ServerEventsSource;
import org.urm.engine.events.ServerEventsState;

public class ServerCacheObject extends ServerEventsSource {
	
	ServerCache cache;
	String group;
	String item;
	
	Object main;
	Map<String,Object> data;
	
	public ServerCacheObject( ServerCache cache , String group , String item , ServerEvents events , String eventsSourceId ) {
		super( events , eventsSourceId );
		this.cache = cache;
		this.group = group;
		this.item = item;
		
		data = new HashMap<String,Object>(); 
	}

	@Override
	public ServerEventsState getState() {
		return( new ServerCacheObjectState( this , super.getStateId() , main ) );
	}

	public synchronized void setState( Object data ) {
		this.main = data;
	}
	
	public synchronized void setState( String key , Object value ) {
		data.put( key , value );
	}

	public synchronized ServerCacheObjectState getState( String key ) {
		return( new ServerCacheObjectState( this , super.getStateId() , data.get( key ) ) );
	}
	
	public void finishScopeItem( int eventType , ScopeState state ) {
		super.trigger( eventType , state );
	}
	
}
