package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;

public class EngineInfrastructure extends EngineObject {

	public Engine engine;
	
	private Map<String,Datacenter> mapDatacenters;
	private Map<Integer,Datacenter> mapDatacentersById;
	private Map<String,Network> mapNetworks;
	private Map<Integer,Network> mapNetworksById;
	private Map<Integer,NetworkHost> mapHostsById;
	private Map<Integer,HostAccount> mapAccountsById;
	
	public EngineInfrastructure( Engine engine ) {
		super( null );
		this.engine = engine;
		mapDatacenters = new HashMap<String,Datacenter>(); 
		mapDatacentersById = new HashMap<Integer,Datacenter>();
		mapNetworks = new HashMap<String,Network>();
		mapNetworksById = new HashMap<Integer,Network>();
		mapHostsById = new HashMap<Integer,NetworkHost>();
		mapAccountsById = new HashMap<Integer,HostAccount>();
	}
	
	@Override
	public String getName() {
		return( "engine-infrastructure" );
	}
	
	public void addDatacenter( Datacenter datacenter ) {
		mapDatacenters.put( datacenter.NAME , datacenter );
		mapDatacentersById.put( datacenter.ID , datacenter );
	}

	public void removeDatacenter( Datacenter datacenter ) throws Exception {
		mapDatacenters.remove( datacenter.NAME );
		mapDatacentersById.remove( datacenter.ID );
	}

	public void updateDatacenter( Datacenter datacenter ) throws Exception {
		Common.changeMapKey( mapDatacenters , datacenter , datacenter.NAME );
	}
	
	public void addNetwork( Network network ) {
		network.datacenter.addNetwork( network );
		mapNetworks.put( network.NAME , network );
		mapNetworksById.put( network.ID , network );
	}

	public void updateNetwork( Network network ) throws Exception {
		Common.changeMapKey( mapNetworks , network , network.NAME );
		network.datacenter.updateNetwork( network );
	}
	
	public void removeNetwork( Network network ) throws Exception {
		network.datacenter.removeNetwork( network );
		mapNetworks.remove( network.NAME );
		mapNetworksById.remove( network.ID );
	}

	public void addHost( NetworkHost host ) {
		host.network.addHost( host );
		mapHostsById.put( host.ID , host );
	}

	public void updateHost( NetworkHost host ) throws Exception {
		host.network.updateHost( host );
	}
	
	public void removeHost( NetworkHost host ) throws Exception {
		host.network.removeHost( host );
		mapHostsById.remove( host.ID );
	}

	public void addAccount( HostAccount account ) {
		account.host.addAccount( account );
		mapAccountsById.put( account.ID , account );
	}

	public void updateAccount( HostAccount account ) throws Exception {
		account.host.updateAccount( account );
	}
	
	public void removeAccount( HostAccount account ) throws Exception {
		account.host.removeAccount( account );
		mapAccountsById.remove( account.ID );
	}

	public Datacenter findDatacenter( String name ) {
		return( mapDatacenters.get( name ) );
	}

	public Network findNetwork( String name ) {
		return( mapNetworks.get( name ) );
	}

	public Network findNetwork( int id ) {
		return( mapNetworksById.get( id ) );
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

	public Datacenter getDatacenter( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getDatacenter( item.FKID ) );
		
		Datacenter datacenter = findDatacenter( item.FKNAME );
		if( datacenter != null )
			Common.exitUnexpected();
		return( datacenter );
	}

	public Network getNetwork( int id ) throws Exception {
		Network network = mapNetworksById.get( id );
		if( network == null )
			Common.exit1( _Error.UnknownNetwork1 , "Unknown network=" + id , "" + id );
		return( network );
	}

	public Network getNetwork( String name ) throws Exception {
		Network network = mapNetworks.get( name );
		if( network == null )
			Common.exit1( _Error.UnknownNetwork1 , "Unknown network=" + name , "" + name );
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

	public HostAccount getHostAccount( int id ) throws Exception {
		HostAccount account = mapAccountsById.get( id );
		if( account == null )
			Common.exit1( _Error.UnknownAccount1 , "Unknown account=" + id , "" + id );
		return( account );
	}

	public HostAccount findHostAccount( String hostLogin ) {
		for( Datacenter datacenter : mapDatacenters.values() ) {
			HostAccount account = datacenter.findAccountByFinal( hostLogin );
			if( account != null )
				return( account );
		}
		return( null );
	}

	public Integer getHostAccountId( String hostLogin ) throws Exception {
		if( hostLogin == null || hostLogin.isEmpty() )
			return( null );
		
		HostAccount account = findHostAccount( hostLogin );
		if( account != null )
			Common.exitUnexpected();
		
		return( account.ID );
	}
	
	public String getHostAccountName( Integer id ) throws Exception {
		if( id == null )
			return( "" );
		
		HostAccount account = getHostAccount( id );
		if( account != null )
			Common.exitUnexpected();
		
		return( account.getFinalAccount() );
	}

	public HostAccount getHostAccount( MatchItem item ) throws Exception {
		if( item == null )
			return( null );
		if( item.MATCHED )
			return( getHostAccount( item.FKID ) );
		
		HostAccount account = findHostAccount( item.FKNAME );
		if( account != null )
			Common.exitUnexpected();
		return( account );
	}

	public MatchItem matchAccountByHostlogin( String hostLogin ) throws Exception {
		if( hostLogin == null || hostLogin.isEmpty() )
			return( null );
		
		HostAccount account = findHostAccount( hostLogin );
		if( account == null )
			return( new MatchItem( hostLogin ) );
		return( new MatchItem( account.ID ) );
	}

	public MatchItem matchAccount( Integer id , String name ) throws Exception {
		if( id == null && name.isEmpty() )
			return( null );
		HostAccount account = ( id == null )? findHostAccount( name ) : getHostAccount( id );
		MatchItem match = ( account == null )? new MatchItem( name ) : new MatchItem( account.ID );
		return( match );
	}
	
	public MatchItem matchDatacenter( String name ) throws Exception {
		if( name == null || name.isEmpty() )
			return( null );
		
		Datacenter dc = findDatacenter( name );
		if( dc == null )
			return( new MatchItem( name ) );
		return( new MatchItem( dc.ID ) );
	}

}
