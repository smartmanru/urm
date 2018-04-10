package org.urm.engine.cache;

import java.util.HashMap;
import java.util.Map;

import org.urm.engine.CacheService;
import org.urm.engine.EventService;
import org.urm.engine.events.EngineEventsSource;
import org.urm.engine.events.EngineEventsState;
import org.urm.engine.status.ObjectState;

public class EngineCacheObject extends EngineEventsSource {
	
	CacheService cache;
	String group;
	String item;
	
	Object main;
	Map<String,Object> data;
	
	public EngineCacheObject( CacheService cache , String group , String item , EventService events , String eventsSourceId ) {
		super( events , eventsSourceId );
		this.cache = cache;
		this.group = group;
		this.item = item;
		
		data = new HashMap<String,Object>(); 
	}

	@Override
	public EngineEventsState getState() {
		return( new EngineCacheObjectState( this , super.getStateId() , main ) );
	}

	public synchronized void setState( Object data ) {
		this.main = data;
	}
	
	public synchronized void setState( String key , Object value ) {
		data.put( key , value );
	}

	public synchronized EngineCacheObjectState getState( String key ) {
		return( new EngineCacheObjectState( this , super.getStateId() , data.get( key ) ) );
	}
	
	public void notifyState( int eventOwner , int eventType , ObjectState state ) {
		super.notify( eventOwner , eventType , state );
	}
	
}
