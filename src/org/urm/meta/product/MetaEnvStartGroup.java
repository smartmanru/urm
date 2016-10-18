package org.urm.meta.product;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.meta.product.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaEnvStartGroup {

	protected Meta meta;
	public MetaEnvStartInfo startInfo;
	
	public String NAME;
	public String SERVERS;
	public List<MetaEnvServer> servers;
	
	public MetaEnvStartGroup( Meta meta , MetaEnvStartInfo startInfo ) {
		this.meta = meta;
		this.startInfo = startInfo;
	}

	public MetaEnvStartGroup copy( ActionBase action , Meta meta , MetaEnvStartInfo startInfo ) throws Exception {
		MetaEnvStartGroup r = new MetaEnvStartGroup( meta , startInfo );
		r.NAME = NAME;
		r.SERVERS = SERVERS;
		for( MetaEnvServer server : servers ) {
			MetaEnvServer rserver = startInfo.dc.getServer( action , server.NAME );
			r.addServer( action , rserver );
		}
		
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		servers = new LinkedList<MetaEnvServer>();
		
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		SERVERS = ConfReader.getAttrValue( node , "servers" );
		
		for( String name : Common.splitSpaced( SERVERS ) ) {
			MetaEnvServer server = startInfo.dc.getServer( action , name );
			addServer( action , server );
		}
	}

	public void addServer( ActionBase action , MetaEnvServer server ) throws Exception {
		servers.add( server );
		server.setStartGroup( action , this );
	}
	
	public List<MetaEnvServer> getServers( ActionBase action ) throws Exception {
		return( servers );
	}

	public void removeServer( ServerTransaction transaction , MetaEnvServer server ) {
		servers.remove( server );
		server.setStartGroup( transaction.action , null );
	}
	
}
