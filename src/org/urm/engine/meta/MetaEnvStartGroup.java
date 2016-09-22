package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.meta.Meta.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaEnvStartGroup {

	protected Meta meta;
	public MetaEnvStartInfo startInfo;
	
	public String NAME;
	public String SERVERS;
	public List<MetaEnvServer> servers;
	public Map<String,MetaEnvServer> serverMap;
	
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
		serverMap = new HashMap<String,MetaEnvServer>();
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
		serverMap.put( server.NAME , server );
		server.setStartGroup( action , this );
	}
	
	public Map<String,MetaEnvServer> getServers( ActionBase action ) throws Exception {
		return( serverMap );
	}
	
}
