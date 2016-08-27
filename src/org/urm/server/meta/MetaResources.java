package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaResources {

	private boolean loaded;
	public boolean loadFailed;

	Map<String,MetaWebResource> webMap;
	
	protected Meta meta;
	
	public MetaResources( Meta meta ) {
		this.meta = meta;
		loaded = false;
		loadFailed = false;
	}

	public void load( ActionBase action , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		webMap = new HashMap<String,MetaWebResource>();
		
		Node[] list = ConfReader.xmlGetChildren( root , "web" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			MetaWebResource res = new MetaWebResource( meta , this );
			res.load( action , node );

			webMap.put( res.NAME , res );
		}
	}

	public MetaWebResource getResource( ActionBase action , String name ) throws Exception {
		MetaWebResource res = webMap.get( name );
		if( res == null )
			action.exit( "unknown resource=" + name );
		return( res );
	}
	
}
