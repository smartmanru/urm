package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;

public class EngineInfrastructure extends EngineObject {

	public Engine engine;
	
	private Map<String,Datacenter> mapDatacenters;
	private Map<Integer,Datacenter> mapDatacentersById;
	private Map<Integer,Network> mapNetworksById;
	private Map<Integer,NetworkHost> mapHostsById;
	
	public EngineInfrastructure( Engine engine ) {
		super( null );
		this.engine = engine;
		mapDatacenters = new HashMap<String,Datacenter>(); 
		mapDatacentersById = new HashMap<Integer,Datacenter>();
		mapNetworksById = new HashMap<Integer,Network>();
		mapHostsById = new HashMap<Integer,NetworkHost>();
	}
	
	@Override
	public String getName() {
		return( "engine-infrastructure" );
	}
	
	public void addDatacenter( Datacenter datacenter ) {
		mapDatacenters.put( datacenter.NAME , datacenter );
		mapDatacentersById.put( datacenter.ID , datacenter );
	}

	public void addNetwork( Network network ) {
		network.datacenter.addNetwork( network );
		mapNetworksById.put( network.ID , network );
	}

	public void addHost( NetworkHost host ) {
		host.network.addHost( host );
		mapHostsById.put( host.ID , host );
	}

	public void addAccount( HostAccount account ) {
		account.host.addAccount( account );
	}

	public void updateDatacenter( Datacenter datacenter ) throws Exception {
		Common.changeMapKey( mapDatacenters , datacenter , datacenter.NAME );
	}
	
	public void updateNetwork( Network network ) throws Exception {
		network.datacenter.updateNetwork( network );
	}
	
	public Datacenter findDatacenter( String name ) {
		return( mapDatacenters.get( name ) );
	}

	public Datacenter getDatacenter( String name ) throws Exception {
		Datacenter datacenter = mapDatacenters.get( name );
		if( datacenter == null )
			Common.exit1( _Error.UnknownDatacenter1 , "Unknown datacenter=" + name , name );
		return( datacenter );
	}

	public Datacenter getDatacenter( int id ) throws Exception {
		Datacenter datacenter = mapDatacentersById.get( id );
		if( datacenter == null )
			Common.exit1( _Error.UnknownDatacenter1 , "Unknown datacenter=" + id , "" + id );
		return( datacenter );
	}

	public Network getNetwork( int id ) throws Exception {
		Network network = mapNetworksById.get( id );
		if( network == null )
			Common.exit1( _Error.UnknownNetwork1 , "Unknown network=" + id , "" + id );
		return( network );
	}

	public NetworkHost getHost( int id ) throws Exception {
		NetworkHost host = mapHostsById.get( id );
		if( host == null )
			Common.exit1( _Error.UnknownHost1 , "Unknown host=" + id , "" + id );
		return( host );
	}

	public String[] getDatacenterNames() {
		return( Common.getSortedKeys( mapDatacenters ) );
	}

	public void removeDatacenter( Datacenter datacenter ) throws Exception {
		mapDatacenters.remove( datacenter.NAME );
		mapDatacentersById.remove( datacenter.ID );
	}

	public void removeNetwork( Network network ) throws Exception {
		network.datacenter.removeNetwork( network );
		mapNetworksById.remove( network.ID );
	}

	public void updateHost( NetworkHost host ) throws Exception {
		host.network.updateHost( host );
	}
	
	public void removeHost( NetworkHost host ) throws Exception {
		host.network.removeHost( host );
		mapHostsById.remove( host.ID );
	}

	public void updateAccount( HostAccount account ) throws Exception {
		account.host.updateAccount( account );
	}
	
	public void removeAccount( HostAccount account ) throws Exception {
		account.host.removeAccount( account );
	}
	
}
