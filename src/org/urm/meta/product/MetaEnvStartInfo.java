package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvStartInfo {

	protected Meta meta;
	public MetaEnvDC dc;
	
	Map<String,MetaEnvStartGroup> groupMap;
	List<MetaEnvStartGroup> groups;
	
	public MetaEnvStartInfo( Meta meta , MetaEnvDC dc ) {
		this.meta = meta;
		this.dc = dc;
		groups = new LinkedList<MetaEnvStartGroup>();
		groupMap = new HashMap<String,MetaEnvStartGroup>();
	}
	
	public MetaEnvStartInfo copy( ActionBase action , Meta meta , MetaEnvDC dc ) throws Exception {
		MetaEnvStartInfo r = new MetaEnvStartInfo( meta , dc );
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

	private void addGroup( MetaEnvStartGroup sg ) {
		groupMap.put( sg.NAME , sg );
		groups.add( sg );
	}
	
	public List<MetaEnvStartGroup> getForwardGroupList( ActionBase action ) throws Exception {
		return( groups );
	}

	public List<MetaEnvStartGroup> getReverseGroupList( ActionBase action ) throws Exception {
		List<MetaEnvStartGroup> revs = new LinkedList<MetaEnvStartGroup>();
		for( int k = groups.size() - 1; k >= 0; k-- )
			revs.add( groups.get( k ) );
		return( revs );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		for( MetaEnvStartGroup group : groups ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "startgroup" );
			group.save( action , doc , itemElement );
		}
	}

	public void removeServer( ServerTransaction transaction , MetaEnvServer server ) {
		if( server.startGroup != null ) {
			server.startGroup.removeServer( transaction , server );
		}
			
	}
	
}
