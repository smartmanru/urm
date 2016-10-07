package org.urm.meta.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
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
	
}
