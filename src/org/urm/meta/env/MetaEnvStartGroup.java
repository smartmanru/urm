package org.urm.meta.env;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvStartGroup {

	protected Meta meta;
	public MetaEnvStartInfo startInfo;
	
	public int ID;
	public String NAME;
	public String DESC;
	public int EV;
	
	public List<MetaEnvServer> servers;
	
	public MetaEnvStartGroup( Meta meta , MetaEnvStartInfo startInfo ) {
		this.meta = meta;
		this.startInfo = startInfo;
		servers = new LinkedList<MetaEnvServer>();
	}

	public MetaEnvStartGroup copy( ActionBase action , Meta meta , MetaEnvStartInfo startInfo ) throws Exception {
		MetaEnvStartGroup r = new MetaEnvStartGroup( meta , startInfo );
		r.NAME = NAME;
		r.SERVERS = SERVERS;
		for( MetaEnvServer server : servers ) {
			MetaEnvServer rserver = startInfo.sg.getServer( action , server.NAME );
			r.addServer( action , rserver );
		}
		
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		SERVERS = ConfReader.getAttrValue( node , "servers" );
		
		for( String name : Common.splitSpaced( SERVERS ) ) {
			MetaEnvServer server = startInfo.sg.getServer( action , name );
			addServer( action , server );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		SERVERS = "";
		for( MetaEnvServer server : servers )
			SERVERS = Common.addToList( SERVERS , server.NAME , " " );
		Common.xmlSetElementAttr( doc , root , "servers" , SERVERS );
	}
	
	public void addServer( ActionBase action , MetaEnvServer server ) throws Exception {
		servers.add( server );
		server.setStartGroup( action , this );
	}
	
	public MetaEnvServer[] getServers() {
		return( servers.toArray( new MetaEnvServer[0] ) );
	}

	public String[] getServerNames() {
		List<String> names = new LinkedList<String>();
		for( MetaEnvServer server : servers )
			names.add( server.NAME );
		return( Common.getSortedList( names ) );
	}

	public void removeServer( EngineTransaction transaction , MetaEnvServer server ) {
		servers.remove( server );
		server.setStartGroup( transaction.action , null );
	}

	public MetaEnvServer findServer( String serverName ) {
		for( MetaEnvServer server : servers ) {
			if( server.NAME.equals( serverName ) )
				return( server );
		}
		return( null );
	}

	public void create( EngineTransaction transaction , String name ) {
		this.NAME = name;
	}
	
}
