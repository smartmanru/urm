package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.meta.product.Meta;

public class MetaEnvStartInfo {

	protected Meta meta;
	public MetaEnvSegment sg;
	
	Map<Integer,MetaEnvStartGroup> groupMapById;
	
	public MetaEnvStartInfo( Meta meta , MetaEnvSegment sg ) {
		this.meta = meta;
		this.sg = sg;
		groupMapById = new HashMap<Integer,MetaEnvStartGroup>();
	}
	
	public MetaEnvStartInfo copy( Meta rmeta , MetaEnvSegment rsg ) throws Exception {
		MetaEnvStartInfo r = new MetaEnvStartInfo( rmeta , rsg );
		for( MetaEnvStartGroup group : groupMapById.values() ) {
			MetaEnvStartGroup rg = group.copy( rmeta , r );
			r.addGroup( rg );
		}
		return( r );
	}
	
	public void addGroup( MetaEnvStartGroup sg ) {
		groupMapById.put( sg.ID , sg );
	}
	
	public MetaEnvStartGroup[] getForwardGroupList() {
		MetaEnvStartGroup[] list = new MetaEnvStartGroup[ groupMapById.size() ];
		int pos = -1;
		for( int k = 0; k < list.length; k++ ) {
			MetaEnvStartGroup next = null;
			int posmin = -1;
			for( MetaEnvStartGroup group : groupMapById.values() ) {
				if( group.POS > pos && ( posmin < 0 || group.POS < posmin ) ) {
					posmin = group.POS;
					next = group;
				}
			}
			list[ k ] = next;
			pos = next.POS;
		}
		return( list );
	}

	public MetaEnvStartGroup[] getReverseGroupList() {
		MetaEnvStartGroup[] list = getForwardGroupList();
		MetaEnvStartGroup[] revlist = new MetaEnvStartGroup[ list.length ];
		for( int k = 0; k < list.length; k++ )
			revlist[ k ] = list[ list.length - k - 1 ];
		return( revlist );
	}

	public void removeServer( MetaEnvServer server ) {
		MetaEnvStartGroup startGroup = server.getStartGroup();
		if( startGroup != null )
			startGroup.removeServer( server );
	}

	public MetaEnvStartGroup findServerGroup( String serverName ) {
		for( MetaEnvStartGroup group : groupMapById.values() ) {
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
	
	public MetaEnvStartGroup findStartGroup( int id ) {
		return( groupMapById.get( id ) );
	}

	public int getLastStartGroupPos() {
		int pos = 0;
		for( MetaEnvStartGroup group : groupMapById.values() ) {
			if( pos == 0 || pos < group.POS )
				pos = group.POS;
		}
		return( pos );
	}

	public void removeGroup( MetaEnvStartGroup groupDelete ) {
		for( MetaEnvStartGroup group : groupMapById.values() ) {
			if( group.POS > groupDelete.POS )
				group.setPos( group.POS - 1 );
		}
		
		groupMapById.remove( groupDelete.ID );
	}
	
}
