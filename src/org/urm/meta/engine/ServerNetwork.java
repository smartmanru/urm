package org.urm.meta.engine;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerNetwork extends ServerObject {

	public ServerDatacenter datacenter;
	
	public String ID;
	public String MASK;
	public String DESC;
	
	private Map<String,ServerNetworkHost> hostMap;
	
	public ServerNetwork( ServerDatacenter datacenter ) {
		super( datacenter );
		this.datacenter = datacenter;
		hostMap = new HashMap<String,ServerNetworkHost>();
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public ServerNetwork copy( ServerDatacenter datacenter ) throws Exception {
		ServerNetwork r = new ServerNetwork( datacenter );
		r.ID = ID;
		r.MASK = MASK;
		r.DESC = DESC;
		
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
		MASK = ConfReader.getAttrValue( root , "mask" );
		DESC = ConfReader.getAttrValue( root , "desc" );
		
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
		Common.xmlSetElementAttr( doc , root , "mask" , MASK );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		
		for( ServerNetworkHost host : hostMap.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "host" );
			host.save( doc , element );
		}
	}

	public ServerNetworkHost findHost( String id ) {
		return( hostMap.get( id ) );
	}
	
	public String[] getHostNames() {
		return( Common.getSortedKeys( hostMap ) );
	}
	
	public ServerNetworkHost[] getHosts() {
		return( hostMap.values().toArray( new ServerNetworkHost[0] ) );
	}
	
	public void createNetwork( ServerTransaction transaction  , String ID , String MASK , String DESC ) throws Exception {
		this.ID = ID;
		this.MASK = MASK;
		this.DESC = DESC;
	}

	public void modifyNetwork( ServerTransaction transaction  , String ID , String MASK , String DESC ) throws Exception {
		this.ID = ID;
		this.MASK = MASK;
		this.DESC = DESC;
	}

	public String[] getFinalAccounts() {
		List<String> list = new LinkedList<String>();
		for( ServerNetworkHost host : hostMap.values() ) {
			String[] accounts = host.getFinalAccounts();
			for( String account : accounts )
				list.add( account );
		}
		return( Common.getSortedList( list ) );
	}

	public ServerHostAccount findFinalAccount( String finalAccount ) {
		for( ServerNetworkHost host : hostMap.values() ) {
			ServerHostAccount account = host.findFinalAccount( finalAccount );
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
	
	public void modify( ServerTransaction transaction , String MASK , String DESC ) {
		this.MASK = MASK;
		this.DESC = DESC;
	}

	public void createHost( ServerTransaction transaction , ServerNetworkHost host ) {
		addNetworkHost( host );
	}
	
	public void deleteHost( ServerTransaction transaction , ServerNetworkHost host ) {
		hostMap.remove( host.ID );
	}
	
	public void modifyHost( ServerTransaction transaction , ServerNetworkHost host ) {
		String oldId = null;
		for( Entry<String,ServerNetworkHost> entry : hostMap.entrySet() ) {
			if( entry.getValue() == host ) {
				oldId = entry.getKey();
			}
		}
		hostMap.remove( oldId );
		addNetworkHost( host );
	}

	public ServerNetworkHost createHost( ServerTransaction transaction , Account account ) throws Exception {
		for( ServerNetworkHost host : hostMap.values() ) {
			if( host.isEqualsHost( account ) )
				return( host );
		}

		ServerNetworkHost host = new ServerNetworkHost( this );
		host.createHost( transaction , account.osType , account.HOST , account.IP , account.PORT , "" );
		createHost( transaction , host );
		return( host );
	}

	public void deleteNetwork( ServerTransaction transaction ) throws Exception {
		super.deleteObject();
	}

	public void getApplicationReferences( List<ServerAccountReference> refs ) {
		for( ServerNetworkHost host : hostMap.values() )
			host.getApplicationReferences( refs );
	}
	
}
