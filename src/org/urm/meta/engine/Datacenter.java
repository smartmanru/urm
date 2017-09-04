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

public class Datacenter extends EngineObject {

	EngineInfrastructure infra;
	
	public String ID;
	public String DESC;
	
	Map<String,Network> mapNetworks;
	
	public Datacenter( EngineInfrastructure infra ) {
		super( infra );
		this.infra = infra;
		mapNetworks = new HashMap<String,Network>();
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public Datacenter copy() throws Exception {
		Datacenter r = new Datacenter( infra );
		r.ID = ID;
		r.DESC = DESC;
		
		for( Network net : mapNetworks.values() ) {
			Network rnet = net.copy( r );
			r.addNetwork( rnet );
		}
		return( r );
	}
	
	private void addNetwork( Network net ) {
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
			Network net = new Network( this );
			net.load( node );
			addNetwork( net );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		
		for( Network host : mapNetworks.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "network" );
			host.save( doc , element );
		}
	}

	public Network findNetwork( String id ) {
		return( mapNetworks.get( id ) );
	}
	
	public String[] getNetworkNames() {
		return( Common.getSortedKeys( mapNetworks ) );
	}
	
	public Network[] getNetworks() {
		return( mapNetworks.values().toArray( new Network[0] ) );
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
	
	public void createNetwork( EngineTransaction transaction , Network network ) throws Exception {
		addNetwork( network );
	}
	
	public void modifyNetwork( EngineTransaction transaction , Network network ) throws Exception {
		for( Entry<String,Network> entry : mapNetworks.entrySet() ) {
			if( entry.getValue() == network ) {
				mapNetworks.remove( entry.getKey() );
				break;
			}
		}
		
		addNetwork( network );
	}
	
	public void deleteNetwork( EngineTransaction transaction , Network network ) throws Exception {
		mapNetworks.remove( network.ID );
		network.deleteNetwork( transaction );
	}

	public NetworkHost findNetworkHost( String id ) {
		for( Network network : mapNetworks.values() ) {
			NetworkHost host = network.findHost( id );
			if( host != null )
				return( host );
		}
		return( null );
	}

	public Network findNetworkByFinalAccount( String hostLogin ) {
		if( hostLogin.isEmpty() )
			return( null );
		
		Account account = Account.getDatacenterAccount( ID , hostLogin );
		return( findNetworkByHost( account.HOST ) );
	}

	public Network findNetworkByHost( String hostName ) {
		if( hostName.isEmpty() )
			return( null );
		
		Account account = Account.getDatacenterAccount( ID , "ignore@" + hostName );
		if( account.isHostName() ) {
			NetworkHost host = findNetworkHost( account.HOST );
			if( host != null )
				return( host.network );
			return( null );
		}
		
		// find network by mask
		for( Network network : mapNetworks.values() ) {
			if( network.checkIpIn( account.HOST ) )
				return( network );
		}
		
		return( null );
	}

	public HostAccount getFinalAccount( ActionBase action , String hostLogin ) throws Exception {
		HostAccount account = findFinalAccount( hostLogin );
		if( account == null )
			action.exit1( _Error.UnknownHostAccount1 , "Unknown host account: " + hostLogin , hostLogin );
		return( account );
	}
	
	public HostAccount findFinalAccount( String hostLogin ) {
		for( Network network : mapNetworks.values() ) {
			HostAccount account = network.findFinalAccount( hostLogin );
			if( account != null )
				return( account );
		}
		return( null );
	}
	
	public void getApplicationReferences( List<AccountReference> refs ) {
		for( Network network : mapNetworks.values() )
			network.getApplicationReferences( refs );
	}
	
}
