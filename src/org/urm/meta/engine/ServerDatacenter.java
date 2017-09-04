package org.urm.meta.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerDatacenter extends EngineObject {

	ServerInfrastructure infra;
	
	public String ID;
	public String DESC;
	
	Map<String,ServerNetwork> mapNetworks;
	
	public ServerDatacenter( ServerInfrastructure infra ) {
		super( infra );
		this.infra = infra;
		mapNetworks = new HashMap<String,ServerNetwork>();
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public ServerDatacenter copy() throws Exception {
		ServerDatacenter r = new ServerDatacenter( infra );
		r.ID = ID;
		r.DESC = DESC;
		
		for( ServerNetwork net : mapNetworks.values() ) {
			ServerNetwork rnet = net.copy( r );
			r.addNetwork( rnet );
		}
		return( r );
	}
	
	private void addNetwork( ServerNetwork net ) {
		mapNetworks.put( net.ID , net );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "network" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerNetwork net = new ServerNetwork( this );
			net.load( node );
			addNetwork( net );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		
		for( ServerNetwork host : mapNetworks.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "network" );
			host.save( doc , element );
		}
	}

	public ServerNetwork findNetwork( String id ) {
		return( mapNetworks.get( id ) );
	}
	
	public String[] getNetworkNames() {
		return( Common.getSortedKeys( mapNetworks ) );
	}
	
	public ServerNetwork[] getNetworks() {
		return( mapNetworks.values().toArray( new ServerNetwork[0] ) );
	}
	
	public void createDatacenter( EngineTransaction transaction  , String ID , String DESC ) throws Exception {
		this.ID = ID;
		this.DESC = DESC;
	}

	public void modifyDatacenter( EngineTransaction transaction  , String ID , String DESC ) throws Exception {
		this.ID = ID;
		this.DESC = DESC;
	}

	public void deleteDatacenter( EngineTransaction transaction ) throws Exception {
		super.deleteObject();
	}
	
	public void createNetwork( EngineTransaction transaction , ServerNetwork network ) throws Exception {
		addNetwork( network );
	}
	
	public void modifyNetwork( EngineTransaction transaction , ServerNetwork network ) throws Exception {
		for( Entry<String,ServerNetwork> entry : mapNetworks.entrySet() ) {
			if( entry.getValue() == network ) {
				mapNetworks.remove( entry.getKey() );
				break;
			}
		}
		
		addNetwork( network );
	}
	
	public void deleteNetwork( EngineTransaction transaction , ServerNetwork network ) throws Exception {
		mapNetworks.remove( network.ID );
		network.deleteNetwork( transaction );
	}

	public ServerNetworkHost findNetworkHost( String id ) {
		for( ServerNetwork network : mapNetworks.values() ) {
			ServerNetworkHost host = network.findHost( id );
			if( host != null )
				return( host );
		}
		return( null );
	}

	public ServerNetwork findNetworkByFinalAccount( String hostLogin ) {
		if( hostLogin.isEmpty() )
			return( null );
		
		Account account = Account.getDatacenterAccount( ID , hostLogin );
		return( findNetworkByHost( account.HOST ) );
	}

	public ServerNetwork findNetworkByHost( String hostName ) {
		if( hostName.isEmpty() )
			return( null );
		
		Account account = Account.getDatacenterAccount( ID , "ignore@" + hostName );
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

	public ServerHostAccount getFinalAccount( ActionBase action , String hostLogin ) throws Exception {
		ServerHostAccount account = findFinalAccount( hostLogin );
		if( account == null )
			action.exit1( _Error.UnknownHostAccount1 , "Unknown host account: " + hostLogin , hostLogin );
		return( account );
	}
	
	public ServerHostAccount findFinalAccount( String hostLogin ) {
		for( ServerNetwork network : mapNetworks.values() ) {
			ServerHostAccount account = network.findFinalAccount( hostLogin );
			if( account != null )
				return( account );
		}
		return( null );
	}
	
	public void getApplicationReferences( List<ServerAccountReference> refs ) {
		for( ServerNetwork network : mapNetworks.values() )
			network.getApplicationReferences( refs );
	}
	
}
