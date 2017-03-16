package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.meta.ServerObject;

public class ServerCache extends ServerObject {

	ServerEngine engine;
	
	Map<String,Map<String,ServerCacheObject>> data;
	
	public ServerCache( ServerEngine engine ) {
		super( null );
		this.engine = engine;
		
		data = new HashMap<String,Map<String,ServerCacheObject>>(); 
	}

	@Override
	public String getName() {
		return( "server-cache" );
	}
	
	public synchronized void init() {
		data.clear();
	}
	
	public synchronized void clear() {
		data.clear();
	}
	
	public synchronized ServerCacheObject getObject( String group , String item ) {
		Map<String,ServerCacheObject> items = data.get( group );
		if( items == null ) {
			items = new HashMap<String,ServerCacheObject>();
			data.put( group , items );
		}
		
		ServerCacheObject object = items.get( item );
		if( object == null ) {
			String sourceId = "cache-" + group + "-" + item;
			object = new ServerCacheObject( this , group , item , engine.getEvents() , sourceId );
			items.put( item , object );
		}
		
		return( object );
	}
	
}
