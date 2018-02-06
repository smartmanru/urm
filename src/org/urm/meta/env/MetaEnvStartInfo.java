package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.meta.product.Meta;

public class MetaEnvStartInfo {

	protected Meta meta;
	public MetaEnvSegment sg;
	
	Map<String,MetaEnvStartGroup> groupMap;
	List<MetaEnvStartGroup> groups;
	
	public MetaEnvStartInfo( Meta meta , MetaEnvSegment sg ) {
		this.meta = meta;
		this.sg = sg;
		groups = new LinkedList<MetaEnvStartGroup>();
		groupMap = new HashMap<String,MetaEnvStartGroup>();
	}
	
	public MetaEnvStartInfo copy( Meta rmeta , MetaEnvSegment rsg ) throws Exception {
		MetaEnvStartInfo r = new MetaEnvStartInfo( rmeta , rsg );
		for( MetaEnvStartGroup group : groups ) {
			MetaEnvStartGroup rg = group.copy( rmeta , r );
			r.addGroup( rg );
		}
		return( r );
	}
	
	public void addGroup( MetaEnvStartGroup sg ) {
		groupMap.put( sg.NAME , sg );
		groups.add( sg );
	}
	
	public MetaEnvStartGroup[] getForwardGroupList() {
		return( groups.toArray( new MetaEnvStartGroup[0] ) );
	}

	public MetaEnvStartGroup[] getReverseGroupList() {
		List<MetaEnvStartGroup> revs = new LinkedList<MetaEnvStartGroup>();
		for( int k = groups.size() - 1; k >= 0; k-- )
			revs.add( groups.get( k ) );
		return( revs.toArray( new MetaEnvStartGroup[0] ) );
	}

	public void removeServer( MetaEnvServer server ) {
		MetaEnvStartGroup startGroup = server.getStartGroup();
		if( startGroup != null )
			startGroup.removeServer( server );
	}

	public MetaEnvStartGroup findServerGroup( String serverName ) {
		for( MetaEnvStartGroup group : groups ) {
			MetaEnvServer server = group.findServer( serverName );
			if( server != null )
				return( group );
		}
		return( null );
	}

	public String[] getMissingServerNames() {
		List<String> missing = new LinkedList<String>();
		for( String serverName : sg.getServerNames() ) {
			MetaEnvServer server = sg.findServer( serverName );
			MetaEnvStartGroup startGroup = server.getStartGroup();
			
			if( startGroup == null )
				missing.add( serverName );
		}
		
		return( missing.toArray( new String[0] ) );
	}
	
}
