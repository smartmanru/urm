package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.w3c.dom.Node;

public class DistRepositoryItem {

	DistRepository repo;
	
	public String VERSION;
	
	public DistRepositoryItem( DistRepository repo ) {
		this.repo = repo;
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		VERSION = ConfReader.getAttrValue( root , "version" );
	}
	
}
