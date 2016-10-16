package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerInfrastructure extends ServerObject {

	public ServerLoader loader;
	
	private Map<String,ServerNetwork> mapNetworks;
	
	public ServerInfrastructure( ServerLoader loader ) {
		super( null );
		this.loader = loader;
		mapNetworks = new HashMap<String,ServerNetwork>(); 
	}
	
	public void load( String infrastructureFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , infrastructureFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "network" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerNetwork network = new ServerNetwork( this );
			network.load( node );
			addNetwork( network );
		}
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( mapNetworks ) ) {
			ServerNetwork network = mapNetworks.get( id );
			Element node = Common.xmlCreateElement( doc , root , "network" );
			network.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addNetwork( ServerNetwork network ) {
		mapNetworks.put( network.ID , network );
	}

	public ServerNetwork findNetwork( String id ) {
		return( mapNetworks.get( id ) );
	}

	public ServerNetworkHost findNetworkHost( String id ) {
		for( ServerNetwork network : mapNetworks.values() ) {
			ServerNetworkHost host = network.findHost( id );
			if( host != null )
				return( host );
		}
		return( null );
	}

	public String[] getNetworks() {
		return( Common.getSortedKeys( mapNetworks ) );
	}

	public void createNetwork( ServerTransaction transaction , ServerNetwork network ) throws Exception {
		addNetwork( network );
	}
	
	public void modifyNetwork( ServerTransaction transaction , ServerNetwork network ) throws Exception {
		for( Entry<String,ServerNetwork> entry : mapNetworks.entrySet() ) {
			if( entry.getValue() == network ) {
				mapNetworks.remove( entry.getKey() );
				break;
			}
		}
		
		addNetwork( network );
	}
	
	public void deleteNetwork( ServerTransaction transaction , ServerNetwork network ) throws Exception {
		mapNetworks.remove( network.ID );
		network.deleteNetwork( transaction );
	}

	public ServerNetwork findNetworkByFinalAccount( String hostLogin ) {
		if( hostLogin.isEmpty() )
			return( null );
		
		Account account = Account.getAnyAccount( hostLogin );
		return( findNetworkByHost( account.HOST ) );
	}

	public ServerNetwork findNetworkByHost( String hostName ) {
		if( hostName.isEmpty() )
			return( null );
		
		Account account = Account.getAnyAccount( "ignore@" + hostName );
		if( account.isHostName() ) {
			ServerNetworkHost host = findNetworkHost( account.HOST );
			if( host != null )
				return( host.network );
			return( null );
		}
		
		// find network by mask
		for( ServerNetwork network : mapNetworks.values() ) {
			if( network.checkIpIn( account.HOST ) )
				return( network );
		}
		
		return( null );
	}

	public ServerHostAccount findFinalAccount( String hostLogin ) {
		for( ServerNetwork network : mapNetworks.values() ) {
			ServerHostAccount account = network.findFinalAccount( hostLogin );
			if( account != null )
				return( account );
		}
		return( null );
	}
	
}