package org.urm.meta.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineObject;

public class Datacenter extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	
	EngineInfrastructure infra;
	
	public int ID;
	public String NAME;
	public String DESC;
	public int CV;
	
	Map<String,Network> mapNetworks;
	
	public Datacenter( EngineInfrastructure infra ) {
		super( infra );
		this.infra = infra;
		mapNetworks = new HashMap<String,Network>();
		ID = -1;
		CV = 0;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public Datacenter copy() throws Exception {
		Datacenter r = new Datacenter( infra );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.CV = CV;
		
		for( Network net : mapNetworks.values() ) {
			Network rnet = net.copy( r );
			r.addNetwork( rnet );
		}
		return( r );
	}
	
	public void addNetwork( Network net ) {
		mapNetworks.put( net.NAME , net );
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
	
	public void createDatacenter( String name , String desc ) throws Exception {
		modifyDatacenter( name , desc );
	}

	public void modifyDatacenter( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
	}

	public void updateNetwork( Network network ) throws Exception {
		Common.changeMapKey( mapNetworks , network , network.NAME );
	}
	
	public void removeNetwork( Network network ) throws Exception {
		mapNetworks.remove( network.NAME );
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
		
		Account account = Account.getDatacenterAccount( NAME , hostLogin );
		return( findNetworkByHost( account.HOST ) );
	}

	public Network findNetworkByHost( String hostName ) {
		if( hostName.isEmpty() )
			return( null );
		
		Account account = Account.getDatacenterAccount( NAME , "ignore@" + hostName );
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

	public HostAccount getFinalAccount( String hostLogin ) throws Exception {
		HostAccount account = findFinalAccount( hostLogin );
		if( account == null )
			Common.exit1( _Error.UnknownHostAccount1 , "Unknown host account: " + hostLogin , hostLogin );
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

	public boolean isEmpty() {
		if( mapNetworks.isEmpty() )
			return( true );
		return( false );
	}
	
}
