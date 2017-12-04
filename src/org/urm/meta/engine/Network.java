package org.urm.meta.engine;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineObject;

public class Network extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_MASK = "mask";
	
	public Datacenter datacenter;
	
	public int ID;
	public String NAME;
	public String DESC;
	public String MASK;
	public int CV;
	
	private Map<String,NetworkHost> hostMap;
	
	public Network( Datacenter datacenter ) {
		super( datacenter );
		this.datacenter = datacenter;
		hostMap = new HashMap<String,NetworkHost>();
		ID = -1;
		CV = 0;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public Network copy( Datacenter datacenter ) throws Exception {
		Network r = new Network( datacenter );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.MASK = MASK;
		r.CV = CV;
		
		for( NetworkHost host : hostMap.values() ) {
			NetworkHost rhost = host.copy( r );
			r.addHost( rhost );
		}
		return( r );
	}
	
	public void addHost( NetworkHost host ) {
		hostMap.put( host.NAME , host );
	}
	
	public NetworkHost findHost( String id ) {
		return( hostMap.get( id ) );
	}
	
	public NetworkHost findHost( Account account ) {
		for( NetworkHost host : hostMap.values() ) {
			if( host.isEqualsHost( account ) )
				return( host );
		}
		return( null );
	}
	
	public String[] getHostNames() {
		return( Common.getSortedKeys( hostMap ) );
	}
	
	public NetworkHost[] getHosts() {
		return( hostMap.values().toArray( new NetworkHost[0] ) );
	}
	
	public void createNetwork( String name , String desc , String mask ) throws Exception {
		modifyNetwork( name , desc , mask );
	}

	public void modifyNetwork( String name , String desc , String mask ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.MASK = mask;
	}

	public String[] getFinalAccounts() {
		List<String> list = new LinkedList<String>();
		for( NetworkHost host : hostMap.values() ) {
			String[] accounts = host.getFinalAccounts();
			for( String account : accounts )
				list.add( account );
		}
		return( Common.getSortedList( list ) );
	}

	public HostAccount findFinalAccount( String finalAccount ) {
		for( NetworkHost host : hostMap.values() ) {
			HostAccount account = host.findFinalAccount( finalAccount );
			if( account != null )
				return( account );
		}
		return( null );
	}
	
	public boolean checkIpIn( String ip ) {
		return( netMatch( MASK , ip ) ); 
	}
	
	public static boolean netMatch( String subnet , String ipTest ) {
		// http://stackoverflow.com/questions/4209760/validate-an-ip-address-with-mask
        String[] parts = Common.split( subnet , "/" );
        String ip = parts[0];
        int prefix;

        if( parts.length < 2 ) {
            prefix = 0;
        } else {
            prefix = Integer.parseInt( parts[1] );
        }

        Inet4Address a = null;
        Inet4Address a1 = null;
        try {
            a = ( Inet4Address )InetAddress.getByName( ip );
            a1 = ( Inet4Address )InetAddress.getByName( ipTest );
        }
        catch ( UnknownHostException e ) {
        }

        byte[] b = a.getAddress();
        int ipInt = ((b[0] & 0xFF) << 24) |
                         ((b[1] & 0xFF) << 16) |
                         ((b[2] & 0xFF) << 8)  |
                         ((b[3] & 0xFF) << 0);

        byte[] b1 = a1.getAddress();
        int ipInt1 = ((b1[0] & 0xFF) << 24) |
                         ((b1[1] & 0xFF) << 16) |
                         ((b1[2] & 0xFF) << 8)  |
                         ((b1[3] & 0xFF) << 0);

        int mask = ~((1 << (32 - prefix)) - 1);

        if ((ipInt & mask) == (ipInt1 & mask)) {
            return true;
        }
        else {
            return false;
        }
	}
	
	public void removeHost( NetworkHost host ) {
		hostMap.remove( host.NAME );
	}
	
	public void updateHost( NetworkHost host ) throws Exception {
		Common.changeMapKey( hostMap , host , host.NAME );
	}

	public void getApplicationReferences( ActionBase action , List<AccountReference> refs ) {
		for( NetworkHost host : hostMap.values() )
			host.getApplicationReferences( action , refs );
	}

	public boolean isEmpty() {
		if( hostMap.isEmpty() )
			return( true );
		return( false );
	}
	
}
