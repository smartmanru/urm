package org.urm.server;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerResources {

	public ServerLoader loader;
	Map<String,ServerAuthResource> resourceMap;

	public ServerResources( ServerLoader loader ) {
		this.loader = loader;
		
		resourceMap = new HashMap<String,ServerAuthResource>();
	}

	public ServerResources copy() throws Exception {
		ServerResources r = new ServerResources( loader );
		
		for( ServerAuthResource res : resourceMap.values() ) {
			ServerAuthResource rc = res.copy( r );
			r.resourceMap.put( rc.NAME , rc );
		}
		return( r );
	}
	
	public void load( String propertyFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "resource" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerAuthResource res = new ServerAuthResource( this );
			res.load( node );

			resourceMap.put( res.NAME , res );
		}
	}

	public void save( String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "resources" );
		Element root = doc.getDocumentElement();

		ServerAuth auth = loader.engine.getAuth();
		auth.deleteGroupData( ServerAuth.AUTH_GROUP_RESOURCE );
		
		for( ServerAuthResource res : resourceMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "resource" );
			res.save( doc , resElement , ServerAuth.AUTH_GROUP_RESOURCE );
		}
		
		Common.xmlSaveDoc( doc , path );
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
