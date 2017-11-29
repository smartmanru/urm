package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.EngineCore;
import org.urm.meta.EngineObject;

public class EngineInfrastructure extends EngineObject {

	public EngineCore core;
	
	private Map<String,Datacenter> mapDatacenters;
	
	public EngineInfrastructure( EngineCore core ) {
		super( null );
		this.core = core;
		mapDatacenters = new HashMap<String,Datacenter>(); 
	}
	
	@Override
	public String getName() {
		return( "engine-infrastructure" );
	}
	
	public void addDatacenter( Datacenter datacenter ) {
		mapDatacenters.put( datacenter.NAME , datacenter );
	}

	public void addNetwork( Network network ) {
		network.datacenter.addNetwork( network );
	}

	public void addHost( NetworkHost host ) {
		host.network.addHost( host );
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

	public String[] getDatacenters() {
		return( Common.getSortedKeys( mapDatacenters ) );
	}

	public void removeDatacenter( Datacenter datacenter ) throws Exception {
		mapDatacenters.remove( datacenter.NAME );
	}

	public void removeNetwork( Network network ) throws Exception {
		network.datacenter.removeNetwork( network );
	}

	public void updateHost( NetworkHost host ) throws Exception {
		host.network.updateHost( host );
	}
	
	public void removeHost( NetworkHost host ) throws Exception {
		host.network.removeHost( host );
	}

	public void updateAccount( HostAccount account ) throws Exception {
		account.host.updateAccount( account );
	}
	
	public void removeAccount( HostAccount account ) throws Exception {
		account.host.removeAccount( account );
	}
	
}
