package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.engine.action.ActionBase;
import org.w3c.dom.Node;

public class MetaEnvStartInfo {

	protected Meta meta;
	public MetaEnvDC dc;
	
	Map<String,MetaEnvStartGroup> groupMap;
	List<MetaEnvStartGroup> groups;
	
	public MetaEnvStartInfo( Meta meta , MetaEnvDC dc ) {
		this.meta = meta;
		this.dc = dc;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		groupMap = new HashMap<String,MetaEnvStartGroup>();
		groups = new LinkedList<MetaEnvStartGroup>();
		
		Node[] items = ConfReader.xmlGetChildren( node , "startgroup" );
		if( items == null )
			return;
		
		for( Node sgnode : items ) {
			MetaEnvStartGroup sg = new MetaEnvStartGroup( meta , this );
			sg.load( action , sgnode );
			groupMap.put( sg.NAME , sg );
			groups.add( sg );
		}
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
}
