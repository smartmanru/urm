package org.urm.meta.env;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.meta.product.Meta;

public class MetaEnvStartGroup {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_SERVERS = "servers";
	
	public Meta meta;
	public MetaEnvStartInfo startInfo;
	
	public int ID;
	public String NAME;
	public String DESC;
	public int POS;
	public int EV;
	
	public List<MetaEnvServer> servers;
	
	public MetaEnvStartGroup( Meta meta , MetaEnvStartInfo startInfo ) {
		this.meta = meta;
		this.startInfo = startInfo;
		servers = new LinkedList<MetaEnvServer>();
	}

	public MetaEnvStartGroup copy( Meta rmeta , MetaEnvStartInfo rstartInfo ) throws Exception {
		MetaEnvStartGroup r = new MetaEnvStartGroup( rmeta , rstartInfo );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.POS = POS;
		r.EV = EV;
		
		for( MetaEnvServer server : servers ) {
			MetaEnvServer rserver = startInfo.sg.getServer( server.NAME );
			r.addServer( rserver );
		}
		
		return( r );
	}
	
	public void createGroup( String name , String desc , int pos ) {
		modifyGroup( name , desc );
		setPos( pos );
	}

	public void setPos( int pos ) {
		this.POS = pos;
	}
	
	public void modifyGroup( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
	}

	public void addServer( MetaEnvServer server ) throws Exception {
		servers.add( server );
		server.setStartGroup( this );
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

	public void removeServer( MetaEnvServer server ) {
		servers.remove( server );
		server.setStartGroup( null );
	}

	public MetaEnvServer findServer( String serverName ) {
		for( MetaEnvServer server : servers ) {
			if( server.NAME.equals( serverName ) )
				return( server );
		}
		return( null );
	}

}
