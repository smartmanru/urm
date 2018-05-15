package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.Meta;

public class AccountReference {

	public HostAccount account;
	public MetaEnvServerNode node;
	
	public AccountReference( HostAccount account , MetaEnvServerNode node ) {
		this.account = account;
		this.node = node;
	}
	
	public static List<Meta> getReferencedProducts( ActionBase action , List<AccountReference> refs ) throws Exception {
		List<Meta> products = new LinkedList<Meta>();
		for( AccountReference ref : refs ) {
			Meta meta = ref.node.meta;
			if( !products.contains( meta ) )
				products.add( meta );
		}
		return( products );
	}

	public static List<Network> getReferencedNetworks( List<AccountReference> refs ) {
		List<Network> networks = new LinkedList<Network>();
		for( AccountReference ref : refs ) {
			Network network = ref.account.host.network;
			if( !networks.contains( network ) )
				networks.add( network );
		}
		return( networks );
	}

	public static List<NetworkHost> getReferencedHosts( List<AccountReference> refs , Network network ) {
		List<NetworkHost> hosts = new LinkedList<NetworkHost>();
		for( AccountReference ref : refs ) {
			NetworkHost host = ref.account.host;
			if( host.network == network && !hosts.contains( host ) )
				hosts.add( host );
		}
		return( hosts );
	}

	public static void sort( ActionBase action , List<AccountReference> refs , boolean infraFirst ) {
		Map<String,AccountReference> initial = new HashMap<String,AccountReference>();
		for( AccountReference ref : refs ) {
			String productName = ref.node.server.sg.env.meta.name;
			AppProduct product = action.findProduct( productName );
			String appKey = product.system.NAME + "-" + productName + "-" + ref.node.server.sg.env.NAME + "-" + 
				ref.node.server.sg.NAME + "-" + ref.node.server.NAME + "-" + ref.node.POS;
			String infraKey = ref.account.host.network.NAME + "-" + ref.account.host.NAME + "-" + ref.account.NAME;
			String key = ( infraFirst )? infraKey + "-" + appKey : appKey + "-" + infraKey;
			initial.put( key , ref );
		}
		String[] keys = Common.getSortedKeys( initial );
		refs.clear();
		for( String key : keys )
			refs.add( initial.get( key ) );
	}
	
}
