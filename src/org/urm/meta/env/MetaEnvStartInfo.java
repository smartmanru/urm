package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
	
	public MetaEnvStartInfo copy( Meta meta , MetaEnvSegment sg ) throws Exception {
		MetaEnvStartInfo r = new MetaEnvStartInfo( meta , sg );
		for( MetaEnvStartGroup group : groups ) {
			MetaEnvStartGroup rg = group.copy( action , meta , r );
			r.addGroup( rg );
		}
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , "startgroup" );
		if( items == null )
			return;
		
		for( Node sgnode : items ) {
			MetaEnvStartGroup sg = new MetaEnvStartGroup( meta , this );
			sg.load( action , sgnode );
			addGroup( sg );
		}
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

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaEnvStartGroup group : groups ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "startgroup" );
			group.save( action , doc , itemElement );
		}
	}

	public void removeServer( MetaEnvServer server ) {
		if( server.startGroup != null )
			server.startGroup.removeServer( server );
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
			if( server.startGroup == null )
				missing.add( serverName );
		}
		
		return( missing.toArray( new String[0] ) );
	}
	
}
