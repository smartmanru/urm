package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerNetwork extends ServerObject {

	ServerInfrastructure infra;
	
	public String ID;
	public String NAME;
	
	private Map<String,ServerNetworkHost> hostMap;
	
	public ServerNetwork( ServerInfrastructure infra ) {
		super( infra );
		hostMap = new HashMap<String,ServerNetworkHost>();
	}
	
	public ServerNetwork copy() throws Exception {
		ServerNetwork r = new ServerNetwork( infra );
		
		for( ServerNetworkHost host : hostMap.values() ) {
			ServerNetworkHost rhost = host.copy( r );
			r.addNetworkHost( rhost );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		ID = ConfReader.getAttrValue( root , "id" );
		NAME = ConfReader.getAttrValue( root , "name" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "host" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerNetworkHost host = new ServerNetworkHost( this );
			host.load( node );
			addNetworkHost( host );
		}
	}

	private void addNetworkHost( ServerNetworkHost host ) {
		hostMap.put( host.ID , host );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		
		for( ServerNetworkHost host : hostMap.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "host" );
			host.save( doc , element );
		}
	}

}
