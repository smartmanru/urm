package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;

public class MetaEnvStartGroup {

	public MetaEnvDC dc;
	
	public String NAME;
	public String SERVERS;
	public List<MetaEnvServer> servers;
	public Map<String,MetaEnvServer> serverMap;
	
	public MetaEnvStartGroup( MetaEnvDC dc ) {
		this.dc = dc;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		serverMap = new HashMap<String,MetaEnvServer>();
		servers = new LinkedList<MetaEnvServer>();
		
		NAME = ConfReader.getNameAttr( action , node );
		SERVERS = ConfReader.getAttrValue( action , node , "servers" );
		
		for( String name : Common.splitSpaced( SERVERS ) ) {
			MetaEnvServer server = dc.getServer( action , name );
			servers.add( server );
			serverMap.put( server.NAME , server );
			server.setStartGroup( action , this );
		}
	}

	public Map<String,MetaEnvServer> getServers( ActionBase action ) throws Exception {
		return( serverMap );
	}
	
}
