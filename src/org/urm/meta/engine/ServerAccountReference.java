package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvServerNode;

public class ServerAccountReference {

	public ServerHostAccount account;
	public MetaEnvServerNode node;
	
	public ServerAccountReference( ServerHostAccount account , MetaEnvServerNode node ) {
		this.account = account;
		this.node = node;
	}
	
	
	public static List<Meta> getReferencedProducts( ActionBase action , List<ServerAccountReference> refs ) throws Exception {
		List<Meta> products = new LinkedList<Meta>();
		for( ServerAccountReference ref : refs ) {
			Meta meta = action.getProductMetadata( ref.node.meta.name );
			if( !products.contains( meta ) )
				products.add( meta );
		}
		return( products );
	}

	public static List<ServerNetwork> getReferencedNetworks( List<ServerAccountReference> refs ) {
		List<ServerNetwork> networks = new LinkedList<ServerNetwork>();
		for( ServerAccountReference ref : refs ) {
			ServerNetwork network = ref.account.host.network;
			if( !networks.contains( network ) )
				networks.add( network );
		}
		return( networks );
	}

	public static List<ServerNetworkHost> getReferencedHosts( List<ServerAccountReference> refs , ServerNetwork network ) {
		List<ServerNetworkHost> hosts = new LinkedList<ServerNetworkHost>();
		for( ServerAccountReference ref : refs ) {
			ServerNetworkHost host = ref.account.host;
			if( host.network == network && !hosts.contains( host ) )
				hosts.add( host );
		}
		return( hosts );
	}

	public static void sort( ActionBase action , List<ServerAccountReference> refs , boolean infraFirst ) {
		ServerDirectory directory = action.getServerDirectory();
		Map<String,ServerAccountReference> initial = new HashMap<String,ServerAccountReference>();
		for( ServerAccountReference ref : refs ) {
			String productName = ref.node.server.sg.env.meta.name;
			ServerProduct product = directory.findProduct( productName );
			String appKey = product.system.NAME + "-" + productName + "-" + ref.node.server.sg.env.ID + "-" + 
				ref.node.server.sg.NAME + "-" + ref.node.server.NAME + "-" + ref.node.POS;
			String infraKey = ref.account.host.network.ID + "-" + ref.account.host.ID + "-" + ref.account.ID;
			String key = ( infraFirst )? infraKey + "-" + appKey : appKey + "-" + infraKey;
			initial.put( key , ref );
		}
		String[] keys = Common.getSortedKeys( initial );
		refs.clear();
		for( String key : keys )
			refs.add( initial.get( key ) );
	}
	
}
