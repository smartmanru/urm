package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.engine.cache.EngineCacheObject;
import org.urm.meta.loader.EngineObject;

public class CacheService extends EngineObject {

	Engine engine;
	
	Map<String,Map<String,EngineCacheObject>> data;
	
	public CacheService( Engine engine ) {
		super( null );
		this.engine = engine;
		
		data = new HashMap<String,Map<String,EngineCacheObject>>(); 
	}

	@Override
	public String getName() {
		return( "engine-cache" );
	}
	
	public synchronized void init() {
		data.clear();
	}
	
	public synchronized void clear() {
		data.clear();
	}
	
	public synchronized EngineCacheObject getObject( String group , String item ) {
		Map<String,EngineCacheObject> items = data.get( group );
		if( items == null ) {
			items = new HashMap<String,EngineCacheObject>();
			data.put( group , items );
		}
		
		EngineCacheObject object = items.get( item );
		if( object == null ) {
			String sourceId = "cache-" + group + "-" + item;
			object = new EngineCacheObject( this , group , item , engine.getEvents() , sourceId );
			items.put( item , object );
		}
		
		return( object );
	}
	
}
