package org.urm.server;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.ExitException;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerResources {

	public ServerRegistry registry;
	public ServerEngine engine;

	Map<String,ServerAuthResource> resourceMap;

	public ServerResources( ServerRegistry registry ) {
		this.registry = registry;
		this.engine = registry.loader.engine;
		
		resourceMap = new HashMap<String,ServerAuthResource>();
	}

	public ServerResources copy() throws Exception {
		ServerResources r = new ServerResources( registry );
		
		for( ServerAuthResource res : resourceMap.values() ) {
			ServerAuthResource rc = res.copy( r );
			r.resourceMap.put( rc.NAME , rc );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , "resource" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerAuthResource res = new ServerAuthResource( this );
			res.load( node );

			resourceMap.put( res.NAME , res );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		for( ServerAuthResource res : resourceMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "resource" );
			res.save( doc , resElement , ServerAuth.AUTH_GROUP_RESOURCE );
		}
	}

	public ServerAuthResource findResource( String name ) {
		ServerAuthResource res = resourceMap.get( name );
		return( res );
	}

	public ServerAuthResource getResource( String name ) throws Exception {
		ServerAuthResource res = resourceMap.get( name );
		if( res == null )
			throw new ExitException( "unknown resource=" + name );
		return( res );
	}

	public String[] getList() {
		return( Common.getSortedKeys( resourceMap ) );
	}
	
	public void createResource( ServerTransaction transaction , ServerAuthResource res ) throws Exception {
		if( resourceMap.get( res.NAME ) != null )
			transaction.exit( "resource already exists name=" + res.NAME );
			
		res.createProperties();
		resourceMap.put( res.NAME , res );
	}
	
	public void deleteResource( ServerTransaction transaction , ServerAuthResource res ) throws Exception {
		if( resourceMap.get( res.NAME ) == null )
			transaction.exit( "unknown resource name=" + res.NAME );
			
		resourceMap.remove( res.NAME );
	}

}
