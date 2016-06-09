package org.urm.server.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Metadata.VarNAMETYPE;
import org.w3c.dom.Node;

public class MetaEnvStartGroup {

	Metadata meta;
	public MetaEnvStartInfo startInfo;
	
	public String NAME;
	public String SERVERS;
	public List<MetaEnvServer> servers;
	public Map<String,MetaEnvServer> serverMap;
	
	public MetaEnvStartGroup( Metadata meta , MetaEnvStartInfo startInfo ) {
		this.meta = meta;
		this.startInfo = startInfo;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		serverMap = new HashMap<String,MetaEnvServer>();
		servers = new LinkedList<MetaEnvServer>();
		
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		SERVERS = ConfReader.getAttrValue( node , "servers" );
		
		for( String name : Common.splitSpaced( SERVERS ) ) {
			MetaEnvServer server = startInfo.dc.getServer( action , name );
			servers.add( server );
			serverMap.put( server.NAME , server );
			server.setStartGroup( action , this );
		}
	}

	public Map<String,MetaEnvServer> getServers( ActionBase action ) throws Exception {
		return( serverMap );
	}
	
}
