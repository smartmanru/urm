package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.action.ActionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMirror {

	public ServerRegistry registry;
	public ServerEngine engine;

	Map<String,ServerMirrorRepository> repoMap;

	public ServerMirror( ServerRegistry registry ) {
		this.registry = registry;
		this.engine = registry.loader.engine;
		
		repoMap = new HashMap<String,ServerMirrorRepository>();
	}

	public ServerMirror copy() throws Exception {
		ServerMirror r = new ServerMirror( registry );
		
		for( ServerMirrorRepository repo : repoMap.values() ) {
			ServerMirrorRepository rc = repo.copy( r );
			r.repoMap.put( rc.NAME , rc );
		}
		return( r );
	}
	
	public Map<String,ServerMirrorRepository> getRepositories() {
		return( repoMap );
	}
	
	public ServerMirrorRepository findRepository( String name ) {
		return( repoMap.get( name ) );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( root , "repository" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerMirrorRepository repo = new ServerMirrorRepository( this );
			repo.load( node );

			repoMap.put( repo.NAME , repo );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		for( ServerMirrorRepository repo : repoMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "repository" );
			repo.save( doc , resElement );
		}
	}

}
